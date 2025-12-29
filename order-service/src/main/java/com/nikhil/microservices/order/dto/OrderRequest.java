package com.nikhil.microservices.order.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record OrderRequest(

        @Size(max = 255, message = "{order.orderNumber.size}")
        String orderNumber,

        @Size(max = 255, message = "{order.skuCode.size}")
        String skuCode,

        @Digits(integer = 17, fraction = 2, message = "{order.price.digits}")
        BigDecimal price,

        @Min(value = 0, message = "{order.quantity.min}")
        Integer quantity

) {
}
