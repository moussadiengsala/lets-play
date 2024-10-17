package com.zone01.products.products;

import lombok.Builder;
import lombok.Data;

import java.util.Collection;

@Data
@Builder
public class User {
    private String id;
    private String name;
    private String email;
    private String password;
    private Role role;

}
