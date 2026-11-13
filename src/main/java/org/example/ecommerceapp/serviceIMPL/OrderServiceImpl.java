
package org.example.ecommerceapp.serviceIMPL;

import org.springframework.transaction.annotation.Transactional;
import org.example.ecommerceapp.dto.OrderDto;
import org.example.ecommerceapp.dto.OrderItemDto;

import org.example.ecommerceapp.dto.OrderStatusHistoryDto;
import org.example.ecommerceapp.dto.ShippingAddressDto;
import org.example.ecommerceapp.entity.*;

import org.example.ecommerceapp.exception.BadRequestException;
import org.example.ecommerceapp.exception.ResourceNotFoundException;

import org.example.ecommerceapp.repository.*;

import org.example.ecommerceapp.service.InventoryMovementService;
import org.example.ecommerceapp.service.OrderService;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepo;

    private final CartRepository cartRepo;

    private final ProductRepository productRepo;
    private final InventoryRepository inventoryRepo;
    private final InventoryMovementService movementService;

    public OrderServiceImpl(
            OrderRepository orderRepo,
            CartRepository cartRepo,
            ProductRepository productRepo, InventoryRepository inventoryRepo, InventoryMovementService movementService) {

        this.orderRepo = orderRepo;
        this.cartRepo = cartRepo;
        this.productRepo = productRepo;
        this.inventoryRepo = inventoryRepo;
        this.movementService = movementService;
    }

    // =====================================
    // PLACE ORDER
    // =====================================

    @Override
    @Transactional
    public OrderDto placeOrder(String email, ShippingAddressDto address) {

        Cart cart = cartRepo.findByUserEmailWithLock(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cart not found"));
        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty");
        }

        Order order = new Order();

        order.setUser(cart.getUser());

        order.setOrderDate(LocalDateTime.now());

        order.setStatus(OrderStatus.PENDING);
        recordStatusChange(
                order,
                null,
                OrderStatus.PENDING,
                email
        );

        order.setItems(new ArrayList<>());

        double total = 0;

        // CONVERT CART ITEMS -> ORDER ITEMS
        List<CartItem> cartItems = cart.getItems().stream()
                .sorted(Comparator.comparing(
                        item -> item.getProduct().getId()))
                .toList();
        List<PendingReservationMovement> pendingMovements =
                new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            Long productId = cartItem.getProduct().getId();
            // Lock the inventory row
            Inventory inventory = inventoryRepo
                    .findByProductIdWithLock(productId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Inventory not found for product: " + productId
                            ));

            Product product = inventory.getProduct();

            int quantity = cartItem.getQuantity();

            // Validate available stock
            if (inventory.getAvailableQuantity() < quantity) {
                throw new BadRequestException(
                        "Insufficient stock for product: " + product.getName()
                );
            }

            // Capture previous inventory state
            int previousOnHand = inventory.getQuantityOnHand();
            int previousReserved = inventory.getReservedQuantity();

            // Reserve inventory
            inventory.reserve(quantity);
            // Store movement details for later
            pendingMovements.add(
                    new PendingReservationMovement(
                            inventory,
                            quantity,
                            previousOnHand,
                            previousReserved
                    )
            );



            OrderItem orderItem = new OrderItem();

            orderItem.setOrder(order);

            orderItem.setProduct(product);

// Save historical product details
            orderItem.setProductIdSnapshot(product.getId());
            orderItem.setProductNameSnapshot(product.getName());

            orderItem.setQuantity(cartItem.getQuantity());

// Purchase-time values
            double purchasePrice = product.getPrice();
            double subtotal = purchasePrice * cartItem.getQuantity();

            orderItem.setPrice(purchasePrice);
            orderItem.setSubtotal(subtotal);

            total += subtotal;

            order.getItems().add(orderItem);
        }

        order.setTotalAmount(total);
        order.setShippingRecipient(address.getRecipient());
        order.setShippingPhone(address.getPhone());
        order.setShippingAddressLine1(address.getAddressLine1());
        order.setShippingAddressLine2(address.getAddressLine2());
        order.setShippingCity(address.getCity());
        order.setShippingState(address.getState());
        order.setShippingPostalCode(address.getPostalCode());
        order.setShippingCountry(address.getCountry());
        Order saved = orderRepo.save(order);

        // Record reservation movements using the generated order ID
        for (PendingReservationMovement pending : pendingMovements) {

            movementService.recordMovement(
                    pending.inventory(),
                    InventoryMovementType.RESERVATION,
                    pending.quantity(),
                    pending.previousOnHand(),
                    pending.previousReserved(),
                    saved.getId(),
                    email
            );
        }


        // CLEAR CART

        cart.getItems().clear();

        cart.setTotalAmount(0);

        cartRepo.save(cart);

        return mapToDto(saved);
    }

    // =====================================
    // GET USER ORDERS
    // =====================================

    @Transactional(readOnly = true)
    @Override
    public List<OrderDto> getUserOrders(String email) {

        return orderRepo.findByUserEmail(email)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    // =====================================
    // GET ORDER BY ID
    // =====================================

    @Override
    public OrderDto getOrderById(Long orderId, String email) {

        Order order = orderRepo.findByIdAndUserEmail(orderId, email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found"));

        return mapToDto(order);
    }

    // =====================================
    // CANCEL ORDER
    // =====================================

    @Override
    @Transactional
    public void cancelOrder(Long orderId, String email) {

        Order order = orderRepo.findByIdAndUserEmailWithLock(orderId, email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found"));

        processCancellation(order,email);
    }
    // =====================================
    // ADMIN ONLY
    // =====================================
    @Override
    @Transactional
    public List<OrderDto> getAllOrders() {

        return orderRepo.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    @Override
    @Transactional
    public OrderDto getAdminOrderById(Long orderId) {

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found"));

        return mapToDto(order);
    }
    @Override
    @Transactional
    public OrderDto updateOrderStatus(Long orderId, String status, String changedBy) {

        if (status == null || status.isBlank()) {
            throw new BadRequestException("Order status is required");
        }

        OrderStatus newStatus;

        try {
            newStatus = OrderStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid order status");
        }

        Order order = orderRepo.findByIdWithLock(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found"));

        OrderStatus currentStatus = order.getStatus();

        if (!currentStatus.canTransitionTo(newStatus)) {
            throw new BadRequestException(
                    "Invalid order status transition: "
                            + currentStatus + " -> " + newStatus
            );
        }

        OrderStatus previousStatus = order.getStatus();

        if (newStatus == OrderStatus.CANCELLED) {

            processCancellation(order, changedBy);

        } else {

            if (newStatus == OrderStatus.SHIPPED) {
                processShipment(order,changedBy);
            }

            order.setStatus(newStatus);

            recordStatusChange(order, previousStatus, newStatus, changedBy);
        }

        Order saved = orderRepo.save(order);

        return mapToDto(saved);
    }

    private void processCancellation(Order order, String changedBy) {
        if (!order.getStatus().canTransitionTo(OrderStatus.CANCELLED)) {
            throw new BadRequestException(
                    "Order cannot be cancelled from status: "
                            + order.getStatus()
            );
        }



        List<OrderItem> items = order.getItems().stream()
                .sorted(Comparator.comparing(
                        item -> item.getProduct().getId()
                ))
                .toList();

        for (OrderItem item : items) {

            Long productId = item.getProduct().getId();

            Inventory inventory = inventoryRepo
                    .findByProductIdWithLock(productId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Inventory not found for product: " + productId
                            ));

            int quantity = item.getQuantity();

            if (inventory.getReservedQuantity() < quantity) {
                throw new BadRequestException(
                        "Invalid inventory reservation for product: " + productId
                );
            }

            // Capture previous inventory state
            int previousOnHand = inventory.getQuantityOnHand();
            int previousReserved = inventory.getReservedQuantity();

// Release the reservation
            inventory.releaseReservation(quantity);

// Record release movement
            movementService.recordMovement(
                    inventory,
                    InventoryMovementType.RELEASE,
                    quantity,
                    previousOnHand,
                    previousReserved,
                    order.getId(),
                    changedBy
            );

        }
        OrderStatus previousStatus=order.getStatus();

        order.setStatus(OrderStatus.CANCELLED);

        recordStatusChange(
                order,
                previousStatus  ,
                OrderStatus.CANCELLED,
                changedBy
        );
    }
    private void processShipment(Order order,String changedBy) {

        List<OrderItem> items = order.getItems().stream()
                .sorted(Comparator.comparing(
                        item -> item.getProduct().getId()
                ))
                .toList();

        for (OrderItem item : items) {

            Long productId = item.getProduct().getId();

            Inventory inventory = inventoryRepo
                    .findByProductIdWithLock(productId)
                    .orElseThrow(() ->
                            new ResourceNotFoundException(
                                    "Inventory not found for product: " + productId
                            ));

            int quantity = item.getQuantity();

            if (inventory.getReservedQuantity() < quantity
                    || inventory.getQuantityOnHand() < quantity) {

                throw new BadRequestException(
                        "Invalid inventory state for product: " + productId
                );
            }

            // Capture previous inventory state
            int previousOnHand = inventory.getQuantityOnHand();
            int previousReserved = inventory.getReservedQuantity();

// Physically remove shipped stock
            inventory.shipReserved(quantity);

// Record shipment movement
            movementService.recordMovement(
                    inventory,
                    InventoryMovementType.SHIPMENT,
                    quantity,
                    previousOnHand,
                    previousReserved,
                    order.getId(),
                    changedBy
            );

        }
    }
    private record PendingReservationMovement(
            Inventory inventory,
            int quantity,
            int previousOnHand,
            int previousReserved
    ) {}
    private void recordStatusChange(
            Order order,
            OrderStatus previousStatus,
            OrderStatus newStatus,
            String changedBy
    ) {
        OrderStatusHistory history = new OrderStatusHistory();

        history.setOrder(order);
        history.setPreviousStatus(previousStatus);
        history.setNewStatus(newStatus);
        history.setChangedBy(changedBy);
        history.setChangedAt(LocalDateTime.now());

        order.getStatusHistory().add(history);
    }

    // =====================================
    // DTO MAPPING
    // =====================================

    private OrderDto mapToDto(Order order) {

        List<OrderItemDto> items = order.getItems()
                .stream()
                .map(item -> new OrderItemDto(
                        item.getProductIdSnapshot(),
                        item.getProductNameSnapshot(),
                        item.getQuantity(),
                        item.getPrice(),
                        item.getSubtotal()))
                .toList();
    List<OrderStatusHistoryDto>statushistory=order.getStatusHistory().stream().map(orderStatusHistory -> new OrderStatusHistoryDto(orderStatusHistory.getPreviousStatus(),orderStatusHistory.getNewStatus(),orderStatusHistory.getChangedBy(),orderStatusHistory.getChangedAt())).toList();
        return new OrderDto(
                order.getId(),
                order.getUser().getId(),
                order.getUser().getName(),
                items,
                order.getTotalAmount(),
                order.getStatus(),
                order.getOrderDate(),

                // Shipping snapshot
                order.getShippingRecipient(),
                order.getShippingPhone(),
                order.getShippingAddressLine1(),
                order.getShippingAddressLine2(),
                order.getShippingCity(),
                order.getShippingState(),
                order.getShippingPostalCode(),
                order.getShippingCountry(),
                statushistory
        );
    }
}
