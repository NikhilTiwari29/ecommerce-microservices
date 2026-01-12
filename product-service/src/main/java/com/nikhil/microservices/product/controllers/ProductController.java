package com.nikhil.microservices.product.controllers;

import com.nikhil.microservices.product.dto.ProductRequest;
import com.nikhil.microservices.product.dto.ProductResponse;
import com.nikhil.microservices.product.services.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@RequestBody @Valid ProductRequest productRequest) {
        ProductResponse product = productService.createProduct(productRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        List<ProductResponse> products = productService.getAllProducts();

        return ResponseEntity.status(HttpStatus.OK).body(products);
    }
}
