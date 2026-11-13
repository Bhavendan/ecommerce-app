package org.example.ecommerceapp.serviceIMPL;

import jakarta.transaction.Transactional;
import org.antlr.v4.runtime.misc.LogManager;
import org.example.ecommerceapp.dto.ProductDto;
import org.example.ecommerceapp.entity.Category;
import org.example.ecommerceapp.entity.Inventory;
import org.example.ecommerceapp.entity.Product;
import org.example.ecommerceapp.exception.BadRequestException;
import org.example.ecommerceapp.exception.ResourceNotFoundException;
import org.example.ecommerceapp.repository.CategoryRepository;
import org.example.ecommerceapp.repository.InventoryRepository;
import org.example.ecommerceapp.repository.CartItemRepository;
import org.example.ecommerceapp.repository.InventoryMovementRepository;
import org.example.ecommerceapp.repository.OrderItemRepository;
import org.example.ecommerceapp.repository.ProductRepository;
import org.example.ecommerceapp.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import org.example.ecommerceapp.entity.InventoryMovementType;
import org.example.ecommerceapp.service.InventoryMovementService;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;
    private final InventoryRepository inventoryRepo;
    private final CartItemRepository cartItemRepo;
    private final OrderItemRepository orderItemRepo;
    private final InventoryMovementRepository movementRepo;
    private final InventoryMovementServiceImpl inventoryMovementService;

    public ProductServiceImpl(ProductRepository productRepo, CategoryRepository categoryRepo, InventoryRepository inventoryRepo, CartItemRepository cartItemRepo, OrderItemRepository orderItemRepo, InventoryMovementRepository movementRepo,InventoryMovementServiceImpl inventoryMovementService) {

        this.productRepo = productRepo;
        this.categoryRepo = categoryRepo;
        this.inventoryRepo=inventoryRepo;
        this.cartItemRepo = cartItemRepo;
        this.orderItemRepo = orderItemRepo;
        this.movementRepo = movementRepo;

        this.inventoryMovementService = inventoryMovementService;
    }

    @Transactional
    public ProductDto createProduct(ProductDto dto) {

        Category category = categoryRepo.findById(dto.getCategoryId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found"));

        Product product = dtoToEntity(dto);
        product.setCategory(category);

        Product savedProduct = productRepo.save(product);

        // Create inventory for the newly created product
        Inventory inventory = new Inventory();
        inventory.setProduct(savedProduct);
        inventory.setQuantityOnHand(dto.getStock());
        inventory.setReservedQuantity(0);
        inventory.setUpdatedAt(LocalDateTime.now());


        inventoryRepo.save(inventory);
        int initialStock = dto.getStock();

        if (initialStock > 0) {
            String changedBy = SecurityContextHolder.getContext()
                    .getAuthentication()
                    .getName();

            inventoryMovementService.recordMovement(
                    inventory,
                    InventoryMovementType.STOCK_ADJUSTMENT,
                    initialStock,
                    0,
                    0,
                    null,
                    changedBy
            );
        }

        return entityToDto(savedProduct);
    }

    @Override
    public ProductDto getProductById(Long id) {

        Product product = productRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        return entityToDto(product);
    }

    @Override
    public Page<ProductDto> getAllProducts(int pageNo, int size, String sortBy, String direction) {
        Sort sort=direction.equalsIgnoreCase("desc")?Sort.by(sortBy).descending():Sort.by(sortBy).ascending();
        Pageable page= PageRequest.of(pageNo,size,sort);
        return productRepo.findAll(page).map(this::entityToDto);
    }

    @Override
    public Page<ProductDto> searchProducts(String keyword, int page, int size) {
        Pageable pageable=PageRequest.of(page,size);
        return productRepo.findByNameContainingIgnoreCase(keyword,pageable).map(this::entityToDto);
    }

    @Transactional
    public ProductDto updateProduct(Long id, ProductDto dto) {

        Product product = productRepo.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found"));

        Inventory inventory = inventoryRepo.findByProductIdWithLock(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Inventory not found"));

        Category category = categoryRepo.findById(dto.getCategoryId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Category not found"));

        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setCategory(category);

        // dto.stock represents the desired available quantity
        int newAvailableQuantity = dto.getStock();

        if (newAvailableQuantity < 0) {
            throw new BadRequestException("Stock cannot be negative");
        }

        // Capture previous inventory state
        int previousOnHand = inventory.getQuantityOnHand();
        int previousReserved = inventory.getReservedQuantity();
        int previousAvailable = inventory.getAvailableQuantity();

// Adjust available stock
        inventory.adjustAvailableQuantity(dto.getStock());

// Record movement only if available stock changed
        if (previousAvailable != dto.getStock()) {

            String changedBy = SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    .getName();

            inventoryMovementService.recordMovement(
                    inventory,
                    InventoryMovementType.STOCK_ADJUSTMENT,
                    Math.abs(dto.getStock() - previousAvailable),
                    previousOnHand,
                    previousReserved,
                    null,
                    changedBy
            );
        }



        productRepo.save(product);
        inventoryRepo.save(inventory);

        return entityToDto(product);
    }

    @Transactional
    public void deleteProduct(Long id) {

        Product product = productRepo.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product not found"));

        Inventory inventory = inventoryRepo.findByProductIdWithLock(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Inventory not found"));

        if (inventory.getReservedQuantity() > 0) {
            throw new BadRequestException(
                    "Cannot delete product with reserved stock"
            );
        }
        if (cartItemRepo.existsByProductId(id)) {
            throw new BadRequestException(
                    "Cannot delete product referenced by cart items"
            );
        }
        if (orderItemRepo.existsByProductId(id)) {
            throw new BadRequestException(
                    "Cannot delete product because it is referenced by existing orders"
            );
        }


        if (movementRepo.existsByInventoryId(inventory.getId())) {
            throw new BadRequestException(
                    "Cannot delete product with inventory movement history"
            );
        }

        inventoryRepo.delete(inventory);
        productRepo.delete(product);
    }

    // ======================
    // DTO MAPPING
    // ======================

    private ProductDto entityToDto(Product product) {
        Inventory inventory = inventoryRepo.findByProductId(product.getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Inventory not found"));
        return new ProductDto(product.getId(), product.getName(), product.getDescription(), product.getPrice(), inventory.getAvailableQuantity(),

                product.getCategory().getId(),

                product.getCategory().getName());
    }

    private Product dtoToEntity(ProductDto dto) {

        Product product = new Product();

        product.setName(dto.getName());

        product.setDescription(dto.getDescription());

        product.setPrice(dto.getPrice());

        return product;
    }
}