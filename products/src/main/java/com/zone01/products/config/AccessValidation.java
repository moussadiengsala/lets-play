package com.zone01.products.config;



import com.zone01.products.products.User;
import com.zone01.products.products.UsersClient;
import feign.FeignException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AccessValidation extends OncePerRequestFilter {
    private static final String USER = "currentUser";
    private final UsersClient usersClient;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        // Check if the request has a valid Authorization header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // Validate token and fetch user permissions from the users service
            User user = usersClient.validateAccess(authHeader);

            if (user == null) {
                // User does not have the necessary permissions
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "You do not have the required permission");
                return;
            }
            request.setAttribute(USER, user);

        } catch (FeignException e) {
            // Handle if the users service is unavailable
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "User service is unavailable");
            return;
        } catch (Exception e) {
            // Handle token validation or other issues
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token or user not found");
            return;
        }

        // Continue with the filter chain if user validation was successful
        filterChain.doFilter(request, response);
    }

    public static User getCurrentUser(HttpServletRequest request) {
        return (User) request.getAttribute(USER);
    }
}