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

//    @PostAuthorize("returnObject.body.data != null && returnObject.body.data.id == authentication.principal.id")
    @GetMapping("/{id}")
    public ResponseEntity<Response<UserDTO>> getUserById(@PathVariable String id) {
        return userService.getUserById(id)
                .map(user -> {
                    Response<UserDTO> response = Response.<UserDTO>builder()
                            .status(HttpStatus.OK.value())
                            .data(user)
                            .message("success")
                            .build();
                    return ResponseEntity.status(HttpStatus.OK).body(response);
                })
                .orElseGet(() -> {
                    Response<UserDTO> response = Response.<UserDTO>builder()
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

    @PostAuthorize("returnObject != null && returnObject.body != null && returnObject.body.data != null && returnObject.body.data.role != null && returnObject.body.data.role == T(com.zone01.users.user.Role).ADMIN")
    @GetMapping("/validate-access")
    public ResponseEntity<Response<UserDTO>> validateAccess() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        Response<UserDTO> response = Response.<UserDTO>builder()
                .status(HttpStatus.OK.value())
                .data(new UserDTO(
                        currentUser.getId(),
                        currentUser.getName(),
                        currentUser.getEmail(),
                        currentUser.getRole()
                ))
                .message("User has been validated successfully")
                .build();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PreAuthorize("#id == authentication.principal.id || hasRole(T(com.zone01.users.user.Role).ADMIN)")
    @PutMapping("/{id}")
    public ResponseEntity<Response<UserDTO>> updateUser(
            @PathVariable String id,
            @Validated @RequestBody UpdateRequest userDetails,
            HttpServletRequest request) {
        Response<UserDTO> updatedUser = userService.updateUser(id, userDetails);
        return ResponseEntity.status(updatedUser.getStatus()).body(updatedUser);
    }

    @PreAuthorize("#id == authentication.principal.id || hasRole(T(com.zone01.users.user.Role).ADMIN)")
    @DeleteMapping("/{id}")
    public ResponseEntity<Response<UserDTO>> deleteUser(@PathVariable String id) {
        Response<UserDTO> deletedUser = userService.deleteUser(id);
        return ResponseEntity.status(deletedUser.getStatus()).body(deletedUser);
    }

}


// eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJtb2lzMUBnbWFpbC5jb20iLCJpYXQiOjE3Mjk2OTMzNTksImV4cCI6MTcyOTc3OTc1OX0.OemvEnSNeOypjGKMh98s5yUNjwoS4-GFn20pczI3zK7lAiI1cRjQfvGcs9XaRDDN ADMIN
// eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJtb2lzQGdtYWlsLmNvbSIsImlhdCI6MTcyOTY5MzMwNiwiZXhwIjoxNzI5Nzc5NzA2fQ.kgg96tVwUSGx3aFyhvO0qF5rXi7qtYEVTonHrtCe9pn0O9pss4QgfUQk5k446Khu USER