package com.zone01.products.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class Response<T> {
    private int status;
    private T data;
    private String message;
}
