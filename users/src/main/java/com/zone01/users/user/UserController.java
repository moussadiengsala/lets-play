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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.FieldError;
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
        AuthenticationResponse authenticationResponse = userService.createUser(user);
        Response<AuthenticationResponse> response = Response.<AuthenticationResponse>builder()
                .status(HttpStatus.CREATED.value())
                .data(authenticationResponse)
                .message("User has been register successfully")
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<Response<AuthenticationResponse>> authentificate(@Valid @RequestBody LoginRequest user) {
        AuthenticationResponse authenticationResponse = userService.authentificate(user);
        Response<AuthenticationResponse> response = Response.<AuthenticationResponse>builder()
                .status(HttpStatus.OK.value())
                .data(authenticationResponse)
                .message("User has been login successfully")
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/auth/refresh-token")
    public ResponseEntity<Response<AuthenticationResponse>> refreshToken(HttpServletRequest request) {
        Response<AuthenticationResponse> response = tokenService.refreshToken(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

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
}
