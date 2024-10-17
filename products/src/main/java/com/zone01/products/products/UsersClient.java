package com.zone01.products.products;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "users")
public interface UsersClient {

    @GetMapping("/api/v1/users/validate-access")
    User validateAccess(@RequestHeader("Authorization") String authorization);
}

