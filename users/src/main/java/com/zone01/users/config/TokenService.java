package com.zone01.users.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zone01.users.user.User;
import com.zone01.users.user.UserRepository;
import com.zone01.users.utils.AuthenticationResponse;
import com.zone01.users.utils.Response;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final UserRepository repository;
    private final JwtService jwtService;

    public Response<AuthenticationResponse> refreshToken(HttpServletRequest request) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Response.<AuthenticationResponse>builder()
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .data(null)
                    .message("Authorization header is missing or invalid")
                    .build();
        }

        refreshToken = authHeader.substring(7);
        try {
            userEmail = jwtService.extractUsername(refreshToken);
        } catch (Exception e) {
            return Response.<AuthenticationResponse>builder()
                    .status(HttpStatus.BAD_REQUEST.value())
                    .data(null)
                    .message("Invalid token")
                    .build();
        }

        if (userEmail == null) {
            return Response.<AuthenticationResponse>builder()
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .data(null)
                    .message("Invalid token: user information is missing")
                    .build();
        }

        // Find the user in the repository
        Optional<User> userOptional = repository.findUserByEmail(userEmail);
        if (userOptional.isEmpty()) {
            return Response.<AuthenticationResponse>builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .data(null)
                    .message("User not found")
                    .build();
        }

        User user = userOptional.get();

        // Validate the refresh token for the user
        if (!jwtService.isTokenValid(refreshToken, user)) {
            return Response.<AuthenticationResponse>builder()
                    .status(HttpStatus.UNAUTHORIZED.value())
                    .data(null)
                    .message("Invalid or expired refresh token")
                    .build();
        }

        String accessToken = jwtService.generateToken(user);
        AuthenticationResponse authResponse = AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        return Response.<AuthenticationResponse>builder()
                .status(HttpStatus.OK.value())
                .data(authResponse)
                .message("Token refreshed successfully")
                .build();
    }
}
