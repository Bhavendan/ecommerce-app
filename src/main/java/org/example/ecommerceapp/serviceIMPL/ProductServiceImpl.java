package org.example.ecommerceapp.serviceIMPL;

import org.example.ecommerceapp.dto.ProductDto;
import org.example.ecommerceapp.entity.Category;
import org.example.ecommerceapp.entity.Product;
import org.example.ecommerceapp.exception.ResourceNotFoundException;
import org.example.ecommerceapp.repository.CategoryRepository;
import org.example.ecommerceapp.repository.ProductRepository;
import org.example.ecommerceapp.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepo;

    private final CategoryRepository categoryRepo;

    public ProductServiceImpl(ProductRepository productRepo, CategoryRepository categoryRepo) {

        this.productRepo = productRepo;
        this.categoryRepo = categoryRepo;
    }

    @Override
    public ProductDto createProduct(ProductDto dto) {

        Category category = categoryRepo.findById(dto.getCategoryId()).orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Product product = dtoToEntity(dto);

        product.setCategory(category);

        Product saved = productRepo.save(product);

        return entityToDto(saved);
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

    @Override
    public ProductDto updateProduct(Long id, ProductDto dto) {

        Product product = productRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        product.setName(dto.getName());

        product.setDescription(dto.getDescription());

        product.setPrice(dto.getPrice());

        product.setStock(dto.getStock());

        Product updated = productRepo.save(product);

        return entityToDto(updated);
    }

    @Override
    public void deleteProduct(Long id) {

        productRepo.deleteById(id);
    }

    // ======================
    // DTO MAPPING
    // ======================

    private ProductDto entityToDto(Product product) {

        return new ProductDto(product.getId(), product.getName(), product.getDescription(), product.getPrice(), product.getStock(),

                product.getCategory().getId(),

                product.getCategory().getName());
    }

    private Product dtoToEntity(ProductDto dto) {

        Product product = new Product();

        product.setName(dto.getName());

        product.setDescription(dto.getDescription());

        product.setPrice(dto.getPrice());

        product.setStock(dto.getStock());

        return product;
    }
}