package org.example.ecommerceapp.service;

import org.example.ecommerceapp.dto.ProductDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ProductService {
    ProductDto createProduct(ProductDto dto);
    ProductDto getProductById(Long id);
    Page<ProductDto> getAllProducts(int page,int size,String sortBy,String direction);
    Page<ProductDto>searchProducts(String keyword,int page,int size);
    ProductDto updateProduct(Long id,ProductDto dto);
    void deleteProduct(Long id);
}
