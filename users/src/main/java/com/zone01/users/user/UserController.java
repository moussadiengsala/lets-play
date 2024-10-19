package com.zone01.users.user;

import com.zone01.users.config.TokenService;
import com.zone01.users.utils.AuthenticationResponse;
import com.zone01.users.utils.Response;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;
    private final TokenService tokenService;

    @PostAuthorize("returnObject.body.data.id == authentication.principal.id")
    @GetMapping("/{id}")
    public ResponseEntity<Response<User>> getUserById(@PathVariable String id) {
        return userService.getUserById(id)
                .map(user -> {
                    Response<User> response = Response.<User>builder()
                            .status(HttpStatus.OK.value())
                            .data(user)
                            .message("success")
                            .build();
                    return ResponseEntity.status(HttpStatus.OK).body(response);
                })
                .orElseGet(() -> {
                    Response<User> response = Response.<User>builder()
                            .status(HttpStatus.NOT_FOUND.value())
                            .data(null)
                            .message("User not found")
                            .build();
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
                });
    }

    @PostMapping("/auth/register")
    public ResponseEntity<Response<AuthenticationResponse>> createUser(@Valid @RequestBody User user) {
        Response<AuthenticationResponse> response = userService.createUser(user);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<Response<AuthenticationResponse>> authentificate(@Valid @RequestBody LoginRequest user) {
        Response<AuthenticationResponse> response = userService.authentificate(user);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/auth/refresh-token")
    public ResponseEntity<Response<AuthenticationResponse>> refreshToken(HttpServletRequest request) {
        Response<AuthenticationResponse> response = tokenService.refreshToken(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/validate-access")
    public ResponseEntity<Response<User>> validateAccess() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Response<User> response = Response.<User>builder()
                .status(HttpStatus.OK.value())
                .data(currentUser)
                .message("User has been validated successfully")
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PreAuthorize("#id == authentication.principal.id")
    @PutMapping("/{id}")
    public ResponseEntity<Response<User>> updateProduct(
            @PathVariable String id,
            @Validated @RequestBody User productDetails,
            HttpServletRequest request) {
        Response<User> updatedProduct = userService.updateUser(id, productDetails);
        return ResponseEntity.status(updatedProduct.getStatus()).body(updatedProduct);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Response<User>> deleteUser(@PathVariable String id) {
        Response<User> deletedUser = userService.deleteUser(id);
        return ResponseEntity.status(deletedUser.getStatus()).body(deletedUser);
    }

}

// eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJtb2lzQGdtYWlsLmNvbSIsImlhdCI6MTcyOTM1MTc0NiwiZXhwIjoxNzI5NDM4MTQ2fQ.TaLbsqMGBRqb4_GtaTzsjTszfUfOqlPFpI8RMjB5Bj73lHSyM4OfIvDYlSPuKYIn USER
// eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJtb2lzMUBnbWFpbC5jb20iLCJpYXQiOjE3MjkzNTE5MjAsImV4cCI6MTcyOTQzODMyMH0.JQnA_h-Ygun7qqZxUm5mkCtyKw6KO8tEyAuEYunzgUi0ygqEag75kGj0oqpgqjTV