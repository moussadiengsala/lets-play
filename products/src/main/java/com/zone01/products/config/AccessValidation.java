package com.zone01.products.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zone01.products.products.Role;
import com.zone01.products.products.User;
import com.zone01.products.products.UsersClient;
import com.zone01.products.utils.Response;
import feign.FeignException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j  // For logging
public class AccessValidation extends OncePerRequestFilter {
    private static final String USER = "currentUser";
    private final UsersClient usersClient;
    private final ObjectMapper jacksonObjectMapper;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        if ("GET".equals(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        // Check if the request has a valid Authorization header
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            setErrorResponse(response, HttpStatus.UNAUTHORIZED, "Missing or invalid Authorization header.");
            return;
        }

        try {
            // Validate token and fetch user permissions from the users service
            Response<User> userResponse = usersClient.validateAccess(authHeader);

            if (userResponse == null || userResponse.getData() == null) {
                log.warn("User validation failed: {}", userResponse != null ? userResponse.getMessage() : "No response from user service");
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "User validation failed or user does not have required permissions.");
                return;
            }

            // Store validated user in request attributes for downstream use
            User user = userResponse.getData();
//            if (user.getRole() == Role.USER) {
//                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "You are not allowed to access this resource.");
//                return;
//            }
            request.setAttribute(USER, user);

        }  catch (FeignException.Unauthorized e) {
            setErrorResponse(response, HttpStatus.UNAUTHORIZED, "Invalid token or user unauthorized.");
            return;
        } catch (FeignException.ServiceUnavailable e) {
            setErrorResponse(response, HttpStatus.SERVICE_UNAVAILABLE, "User service is unavailable.");
            return;
        } catch (FeignException e) {
            setErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "Error during user validation.");
            return;
        } catch (Exception e) {
            setErrorResponse(response, HttpStatus.UNAUTHORIZED, "Invalid token or user not found.");
            return;
        }

        // Continue with the filter chain if user validation was successful
        filterChain.doFilter(request, response);
    }

    public static User getCurrentUser(HttpServletRequest request) {
        return (User) request.getAttribute(USER);
    }

    private void setErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Response<Object> errorResponse = Response.<Object>builder()
                .status(status.value())
                .message(message)
                .data(null)
                .build();
        jacksonObjectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
