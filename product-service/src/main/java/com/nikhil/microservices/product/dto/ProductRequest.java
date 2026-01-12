package com.nikhil.microservices.product.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductRequest(

        @NotBlank(message = "{product.name.notBlank}")
        @Size(min = 3, max = 100, message = "{product.name.size}")
        String name,

        @NotBlank(message = "{product.description.notBlank}")
        @Size(max = 500, message = "{product.description.size}")
        String description,

        @NotNull(message = "{product.price.notNull}")
        @DecimalMin(value = "0.01", inclusive = true, message = "{product.price.min}")
        BigDecimal price
) {
}
