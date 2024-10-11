package com.zone01.lets_play.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Response<T> {
    private int status;
    private T data;
    private String message;
}
