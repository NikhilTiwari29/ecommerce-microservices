package com.nikhil.microservices.product.service;

import com.nikhil.microservices.product.dto.ProductRequest;
import com.nikhil.microservices.product.dto.ProductResponse;
import com.nikhil.microservices.product.entities.Product;
import com.nikhil.microservices.product.exceptions.ProductCreationException;
import com.nikhil.microservices.product.exceptions.ProductFetchException;
import com.nikhil.microservices.product.repositories.ProductRepository;
import com.nikhil.microservices.product.services.ProductService;
import org.junit.jupiter.api.BeforeAll;
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
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private static Product product1;
    private static Product product2;
    private static ProductRequest productRequest;

    private static void setId(Product product, String id) {
        try {
            var field = Product.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(product, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeAll
    static void setup(){
        product1 = new Product("iphone", "apple phone", new BigDecimal(100));
        setId(product1, "1");
        product2 = new Product("pixel", "google phone", new BigDecimal(100));
        setId(product2, "2");

        productRequest = new ProductRequest("iphone", "apple phone", new BigDecimal(100));
    }

    @Test
    void shouldCreateProductSuccessFully(){
        //ARRANGE
        when(productRepository.save(any(Product.class))).thenReturn(product1);

        //ACT
        ProductResponse productResponse = productService.createProduct(productRequest);

        //ASSERT
        assertThat(productResponse).isNotNull();
        assertThat(productResponse.id()).isEqualTo("1");
        assertThat(productResponse.name()).isEqualTo("iphone");
        assertThat(productResponse.description()).isEqualTo("apple phone");
        assertThat(productResponse.price()).isEqualTo(new BigDecimal(100));

        //VERIFY
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void shouldThrowProductCreationException_whenDatabaseFails() {
        // ARRANGE
        when(productRepository.save(any(Product.class)))
                .thenThrow(new DataAccessException("DB error") {});

        // ACT + ASSERT
        assertThatThrownBy(() -> productService.createProduct(productRequest))
                .isInstanceOf(ProductCreationException.class)
                .hasMessage("Unable to create product at this time")
                .hasCauseInstanceOf(DataAccessException.class)
                .hasRootCauseMessage("DB error");

        // VERIFY
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void shouldReturnAllProducts() {

        when(productRepository.findAll()).thenReturn(List.of(product1, product2));

        // ACT
        List<ProductResponse> responses = productService.getAllProducts();

        // ASSERT
        assertThat(responses).hasSize(2);

        assertThat(responses)
                .extracting(
                        ProductResponse::id,
                        ProductResponse::name,
                        ProductResponse::description,
                        ProductResponse::price
                )
                .containsExactlyInAnyOrder(
                        tuple("1", "iphone", "apple phone", new BigDecimal(100)),
                        tuple("2", "pixel", "google phone", new BigDecimal(100))
                );

        assertThat(responses)
                .allSatisfy(r -> {
                    assertThat(r.id()).isNotNull();
                    assertThat(r.name()).isNotBlank();
                    assertThat(r.price()).isPositive();
                });

        // VERIFY
        verify(productRepository, times(1)).findAll();
        verifyNoMoreInteractions(productRepository);
    }

    @Test
    void shouldThrowRuntimeException_whenFetchingFails() {

        // ARRANGE
        when(productRepository.findAll())
                .thenThrow(new DataAccessException("DB error") {});

        // ACT + ASSERT
        assertThatThrownBy(() -> productService.getAllProducts())
                .isInstanceOf(ProductFetchException.class)
                .hasMessage("Unable to fetch products at this time")
                .hasCauseInstanceOf(DataAccessException.class)
                .hasRootCauseMessage("DB error");

        // VERIFY
        verify(productRepository).findAll();
    }

}
