package com.zone01.lets_play.user;

import com.zone01.lets_play.config.JwtService;
import com.zone01.lets_play.token.TokenRepository;
import com.zone01.lets_play.token.TokenService;
import com.zone01.lets_play.utils.AuthenticationResponse;
import com.zone01.lets_play.utils.Response;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
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
        tokenService.saveUserToken(savedUser, jwtToken);
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
        tokenService.revokeAllUserTokens(user);
        tokenService.saveUserToken(user, jwtToken);
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }



//    public ResponseEntity<User> updateUser(String id, User userDetails) {
//        Optional<User> optionalUser = userRepository.findById(id);
//        if (optionalUser.isPresent()) {
//            User user = optionalUser.get();
//            user.setName(userDetails.getName());
//            user.setEmail(userDetails.getEmail());
//            user.setPassword(hashPassword(userDetails.getPassword())); // Hash the new password
//            user.setRole(userDetails.getRole());
//            User updatedUser = userRepository.save(user);
//            return ResponseEntity.ok(updatedUser);
//        }
//        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//    }

//    public ResponseEntity<Void> deleteUser(String id) {
//        if (userRepository.existsById(id)) {
//            userRepository.deleteById(id);
//            return ResponseEntity.noContent().build();
//        }
//        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//    }

    // Add a method to hash passwords (You will need to implement this)
    private String hashPassword(String password) {
        // Implement password hashing logic (e.g., BCrypt)
        return password; // Placeholder
    }

}
