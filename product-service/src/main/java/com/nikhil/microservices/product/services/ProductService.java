package com.nikhil.microservices.product.services;

import com.nikhil.microservices.product.dto.ProductRequest;
import com.nikhil.microservices.product.dto.ProductResponse;
import com.nikhil.microservices.product.entities.Product;
import com.nikhil.microservices.product.exceptions.ProductCreationException;
import com.nikhil.microservices.product.exceptions.ProductFetchException;
import com.nikhil.microservices.product.repositories.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    public ProductResponse createProduct(ProductRequest productRequest) {

        try {

            Product product = new Product(
                    productRequest.name(),
                    productRequest.description(),
                    productRequest.price()
            );

            Product saved = productRepository.save(product);

            log.info("Product created successfully with id={}", saved.getId());

            return new ProductResponse(
                    saved.getId(),
                    saved.getName(),
                    saved.getDescription(),
                    saved.getPrice()
            );

        } catch (DataAccessException ex) {
            log.error("Database error while creating product", ex);
            throw new ProductCreationException("Unable to create product at this time", ex);
        }
    }

    public List<ProductResponse> getAllProducts() {

        try {
            return productRepository.findAll().stream()
                    .map(product -> new ProductResponse(
                            product.getId(),
                            product.getName(),
                            product.getDescription(),
                            product.getPrice()
                    ))
                    .toList();

        } catch (DataAccessException ex) {
            log.error("Database error while fetching products", ex);
            throw new ProductFetchException("Unable to fetch products at this time", ex);
        }
    }
}
