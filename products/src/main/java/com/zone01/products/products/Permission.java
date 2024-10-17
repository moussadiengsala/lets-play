package com.zone01.products.products;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Permission {

    ADMIN_READ("admin:read"),
    ADMIN_UPDATE("admin:update"),
    ADMIN_CREATE("admin:create"),
    ADMIN_DELETE("admin:delete"),

    // Permissions for Users
    USER_READ("user:read"),
    USER_UPDATE("user:update"),
    USER_CREATE("user:create"),
    USER_DELETE("user:delete"),

    // Product-specific Permissions
    PRODUCT_READ("product:read"),   // Both User and Admin can read products
    PRODUCT_CREATE("product:create"), // Only Admin can create products
    PRODUCT_UPDATE("product:update"), // Only Admin can update products
    PRODUCT_DELETE("product:delete");

    private final String permission;
}
