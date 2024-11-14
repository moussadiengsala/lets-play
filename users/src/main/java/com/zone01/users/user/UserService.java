package com.zone01.users.user;

import com.zone01.users.config.JwtService;
import com.zone01.users.utils.AuthenticationResponse;
import com.zone01.users.utils.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public Optional<UserDTO> getUserById(String id) {
        return userRepository.findById(id)
                .map(user -> new UserDTO(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    user.getRole()
                ));
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
        Optional<User> userOptional = userRepository.findById(id);

        // Check if the product exists
        if (userOptional.isEmpty()) {
            return Response.<User>builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .data(null)
                    .message("User not found")
                    .build();
        }

        return Response.<User>builder()
                .status(HttpStatus.OK.value())
                .data(userOptional.get())
                .build();
    }

    public Response<UserDTO> updateUser(String id, UpdateRequest userDetails) {

        Response<User> authorizationResponse = authorizeAndGetUser(id);
        if (authorizationResponse.getStatus() != HttpStatus.OK.value()) {
            return Response.<UserDTO>builder()
                    .status(authorizationResponse.getStatus())
                    .data(null)
                    .message(authorizationResponse.getMessage())
                    .build();
        }

        // Update product details
        User user = authorizationResponse.getData();
        user.setName(userDetails.getName());
        user.setRole(userDetails.getRole());

        User updatedProduct = userRepository.save(user);
        UserDTO updatedUserDTO = new UserDTO(
                updatedProduct.getId(),
                updatedProduct.getName(),
                updatedProduct.getEmail(),
                updatedProduct.getRole()
        );

        // Build and return response
        return Response.<UserDTO>builder()
                .status(HttpStatus.OK.value())
                .data(updatedUserDTO)
                .message("User updated successfully")
                .build();
    }

    public Response<UserDTO> deleteUser(String id) {

        Response<User> authorizationResponse = authorizeAndGetUser(id);
        if (authorizationResponse.getStatus() != HttpStatus.OK.value()) {
            return Response.<UserDTO>builder()
                    .status(authorizationResponse.getStatus())
                    .data(null)
                    .message(authorizationResponse.getMessage())
                    .build();
        }

        userRepository.deleteById(id);

        User user = authorizationResponse.getData();
        UserDTO deletedUserDTO = new UserDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole()
        );

        // Return success response
        return Response.<UserDTO>builder()
                .status(HttpStatus.OK.value())
                .data(deletedUserDTO)
                .message("User deleted successfully")
                .build();
    }

    public Response<List<UserDTO>> getAllUsers() {
        List<UserDTO> userDTOList = userRepository.findAll().stream()
                .map(user -> new UserDTO(
                        user.getId(),
                        user.getName(),
                        user.getEmail(),
                        user.getRole()
                ))
                .collect(Collectors.toList());

        return Response.<List<UserDTO>>builder()
                .data(userDTOList)
                .status(200)
                .message("success")
                .build();
    }
}
