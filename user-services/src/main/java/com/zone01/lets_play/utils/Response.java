package com.zone01.lets_play.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.ResponseEntity;

@Data
@Builder
@AllArgsConstructor
public class Response<T> {
    private int status;
    private T data;
    private String message;
}
