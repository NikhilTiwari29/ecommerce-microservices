package com.nikhil.microservices.product.services;

import com.nikhil.microservices.product.dto.ProductRequest;
import com.nikhil.microservices.product.dto.ProductResponse;
import com.nikhil.microservices.product.entities.Product;
import com.nikhil.microservices.product.exceptions.ProductCreationException;
import com.nikhil.microservices.product.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private ProductRequest productRequest;
    private Product product;

    private void setId(Product product, String id) {
        try {
            var field = Product.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(product, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void setUp() {
        // ARRANGE (shared setup)
        productRequest = new ProductRequest("iphone", "apple phone", new BigDecimal("1000"));
        product = new Product("iphone", "apple phone", new BigDecimal("1000"));
        setId(product, "1");
    }

    // ---------- createProduct ----------

    @Test
    void shouldCreateProductSuccessfully() {

        // ARRANGE
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // ACT
        ProductResponse response = productService.createProduct(productRequest);

        // ASSERT
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo("1");
        assertThat(response.name()).isEqualTo("iphone");
        assertThat(response.description()).isEqualTo("apple phone");
        assertThat(response.price()).isEqualByComparingTo(new BigDecimal("1000"));

        // VERIFY
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void shouldThrowProductCreationException_whenDatabaseFails() {

        // ARRANGE
        when(productRepository.save(any(Product.class)))
                .thenThrow(new DataAccessException("DB down") {});

        // ACT + ASSERT
        assertThatThrownBy(() -> productService.createProduct(productRequest))
                .isInstanceOf(ProductCreationException.class)
                .hasMessageContaining("Unable to create product");

        // VERIFY
        verify(productRepository).save(any(Product.class));
    }

    // ---------- getAllProducts ----------

    @Test
    void shouldReturnAllProducts() {

        // ARRANGE
        Product p1 = new Product("iphone", "apple phone", new BigDecimal("1000"));
        setId(p1, "1");

        Product p2 = new Product("pixel", "google phone", new BigDecimal("900"));
        setId(p2, "2");

        when(productRepository.findAll()).thenReturn(List.of(p1, p2));

        // ACT
        List<ProductResponse> responses = productService.getAllProducts();

        // ASSERT
        assertThat(responses).hasSize(2);
        assertThat(responses)
                .extracting(ProductResponse::name)
                .containsExactlyInAnyOrder("iphone", "pixel");

        // VERIFY
        verify(productRepository).findAll();
    }

    @Test
    void shouldThrowRuntimeException_whenFetchingFails() {

        // ARRANGE
        when(productRepository.findAll())
                .thenThrow(new DataAccessException("DB error") {});

        // ACT + ASSERT
        assertThatThrownBy(() -> productService.getAllProducts())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Unable to fetch products");

        // VERIFY
        verify(productRepository).findAll();
    }
}