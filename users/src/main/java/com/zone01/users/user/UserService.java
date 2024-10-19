package com.zone01.users.user;

import com.zone01.users.config.JwtService;
import com.zone01.users.utils.AuthenticationResponse;
import com.zone01.users.utils.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Autowired // can be omitted if the class has only one constructor
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }

    public Response<AuthenticationResponse> createUser(User user) {
        Optional<User> userOptional = userRepository.findUserByEmail(user.getEmail());
        if (userOptional.isPresent()) {
            return Response.<AuthenticationResponse>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .data(null)
                    .message("Email already in use.")
                    .build();
        }

        var new_user = User.builder()
                .name(user.getName())
                .email(user.getEmail())
                .password(passwordEncoder.encode(user.getPassword()))
                .role(user.getRole())
                .build();

        var savedUser = userRepository.save(new_user);
        var jwtToken = jwtService.generateToken(new_user);
        var refreshToken = jwtService.generateRefreshToken(new_user);
        return Response.<AuthenticationResponse>builder()
                .status(HttpStatus.CREATED.value())
                .message("user has been created successfully.")
                .data(AuthenticationResponse.builder()
                        .accessToken(jwtToken)
                        .refreshToken(refreshToken)
                        .build())
                .build();
    }

    public Response<AuthenticationResponse> authentificate(LoginRequest loginRequest) {
        Optional<User> userOptional = userRepository.findUserByEmail(loginRequest.getEmail());
        if (userOptional.isEmpty()) {
            return Response.<AuthenticationResponse>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .data(null)
                    .message("Email does not exist.")
                    .build();
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        User user = userOptional.get();
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        return Response.<AuthenticationResponse>builder()
                .status(HttpStatus.OK.value())
                .message("user has been authenticated successfully.")
                .data(AuthenticationResponse.builder()
                        .accessToken(jwtToken)
                        .refreshToken(refreshToken)
                        .build())
                .build();
    }

    private Response<User> authorizeAndGetUser(String id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        // Find the product by its ID
        Optional<User> productOptional = userRepository.findById(id);

        // Check if the product exists
        if (productOptional.isEmpty()) {
            return Response.<User>builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .data(null)
                    .message("User not found")
                    .build();
        }

        // Get the product from the optional
//        User user = productOptional.get();

//        // Check if the current user is authorized to update or delete this product (i.e., if they own it)
//        if (!user.getId().equals(currentUser.getId())) {
//            return Response.<User>builder()
//                    .status(HttpStatus.UNAUTHORIZED.value())
//                    .data(null)
//                    .message("Unauthorized to access this user info.")
//                    .build();
//        }

        return Response.<User>builder()
                .status(HttpStatus.OK.value())
                .data(productOptional.get())
                .build();
    }

    public Response<User> updateUser(String id, User productDetails) {

        // Authorize and get the product
        Response<User> authorizationResponse = authorizeAndGetUser(id);
        if (authorizationResponse.getStatus() != HttpStatus.OK.value()) {
            return authorizationResponse;
        }

        // Update product details
        User user = authorizationResponse.getData();
        user.setName(productDetails.getName());

        // Save updated product
        User updatedProduct = userRepository.save(user);

        // Build and return response
        return Response.<User>builder()
                .status(HttpStatus.OK.value())
                .data(updatedProduct)
                .message("User updated successfully")
                .build();
    }

    public Response<User> deleteUser(String id) {

        // Authorize and get the product
        Response<User> authorizationResponse = authorizeAndGetUser(id);
        if (authorizationResponse.getStatus() != HttpStatus.OK.value()) {
            return authorizationResponse;
        }

        // Delete the product
        userRepository.deleteById(id);

        // Return success response
        return Response.<User>builder()
                .status(HttpStatus.OK.value())
                .data(authorizationResponse.getData())
                .message("User deleted successfully")
                .build();
    }
}
