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

    @GetMapping
    public Response<List<User>> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public Response<User> getUserById(@PathVariable String id) {
        return userService.getUserById(id);
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
    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        tokenService.refreshToken(request, response);
    }

    //    @ResponseStatus(HttpStatus.BAD_REQUEST)
    //    @ExceptionHandler(MethodArgumentNotValidException.class)
    //    public Response<Map<String, String>> HandleValidationException(MethodArgumentNotValidException ex) {
    //        Map<String, String> errors = new HashMap<String, String>();
    //        ex.getBindingResult().getAllErrors().forEach((error) -> {
    //            String fieldName = ((FieldError) error).getField();
    //            String errorMessage = error.getDefaultMessage();
    //            errors.put(fieldName, errorMessage);
    //        });
    //
    //        return new Response<Map<String, String>>(400, errors, "");
    //    }


    //    @PutMapping("/{id}")
    //    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody User user) {
    //        return userService.updateUser(id, user);
    //    }
    //
    //    @DeleteMapping("/{id}")
    //    public ResponseEntity<Void> deleteUser(@PathVariable String id) {
    //        return userService.deleteUser(id);
    //    }
}
