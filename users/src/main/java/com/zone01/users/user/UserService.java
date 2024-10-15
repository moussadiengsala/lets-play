package com.zone01.users.user;

import com.zone01.users.config.JwtService;
import com.zone01.users.config.TokenService;
import com.zone01.users.utils.AuthenticationResponse;
import com.zone01.users.utils.Response;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;

    public Response<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return new Response<>(200, users, "Users retrieved successfully");
    }

    public Response<User> getUserById(String id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(value -> new Response<>(200, value, "User retrieved successfully"))
                .orElseGet(() -> new Response<>(404, null, "User not found"));
    }

    public AuthenticationResponse createUser(User user) {
//        Optional<User> userOptional = userRepository.findUserByEmail(user.getEmail());
//        return userOptional.map(value -> new Response<User>(400, null, "Email already exists"))
//                .orElseGet(() -> {
//                    User savedUser = userRepository.save(user);
//                    return new Response<User>(201, savedUser, "User created successfully");
//                });
        var new_user = User.builder()
                .name(user.getName())
                .email(user.getEmail())
                .password(passwordEncoder.encode(user.getPassword()))
                .role(user.getRole())
                .build();

        var savedUser = userRepository.save(new_user);
        var jwtToken = jwtService.generateToken(new_user);
        var refreshToken = jwtService.generateRefreshToken(new_user);
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    public AuthenticationResponse authentificate(LoginRequest loginRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        var user = userRepository.findUserByEmail(loginRequest.getEmail())
                .orElseThrow();

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    // Add a method to hash passwords (You will need to implement this)
    private String hashPassword(String password) {
        // Implement password hashing logic (e.g., BCrypt)
        return password; // Placeholder
    }

}
