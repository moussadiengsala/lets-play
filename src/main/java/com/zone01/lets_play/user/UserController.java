package com.zone01.lets_play.user;

import com.zone01.lets_play.utils.AuthenticationResponse;
import com.zone01.lets_play.utils.Response;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    @GetMapping
    public Response<List<User>> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public Response<User> getUserById(@PathVariable String id) {
        return userService.getUserById(id);
    }

    @PostMapping("/register")
    public AuthenticationResponse createUser(@Valid @RequestBody User user) {
        return userService.createUser(user);
    }

    @PostMapping("/login")
    public AuthenticationResponse authentificate(@Valid @RequestBody LoginRequest user) {
        return userService.authentificate(user);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Response<Map<String, String>> HandleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<String, String>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return new Response<Map<String, String>>(400, errors, "");
    }


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