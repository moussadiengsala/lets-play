package com.zone01.lets_play.config;

import com.zone01.lets_play.user.Permission;
import com.zone01.lets_play.user.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    private final AccessDeniedHandler accessDeniedHandler;
//    private final LogoutHandler logoutHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req ->
                        req.requestMatchers(new String[]{"/api/v1/users/login", "/api/v1/users/register", "/api/v1/token/refresh-token"})
                                .permitAll()
                                .requestMatchers("/api/v1/users/profile").hasAnyAuthority(Permission.USER_READ.getPermission(), Permission.ADMIN_READ.getPermission())
                                .requestMatchers(HttpMethod.GET, "/api/v1/products/**").hasAnyAuthority(Permission.ADMIN_READ.getPermission(), Permission.USER_READ.getPermission())
                                .requestMatchers(HttpMethod.POST, "/api/v1/products/**").hasAnyAuthority(Permission.ADMIN_CREATE.getPermission())
                                .requestMatchers(HttpMethod.PUT, "/api/v1/products/**").hasAnyAuthority(Permission.ADMIN_UPDATE.getPermission())
                                .requestMatchers(HttpMethod.DELETE, "/api/v1/products/**").hasAnyAuthority(Permission.ADMIN_DELETE.getPermission())
                                .anyRequest()
                                .authenticated()
                )
                .exceptionHandling(ex -> ex.accessDeniedHandler(accessDeniedHandler))
                // This line configures how the session is managed in your application.
                // SessionCreationPolicy.STATELESS: This means that the application will not create or use an HTTP session to store the user's security context.
                // Stateless: Every request must be independently authenticated because no session state is maintained between request
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // This specifies which authentication provider is responsible for verifying user credentials and providing the authenticated user's details.
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

//                .logout(logout ->
//                        logout.logoutUrl("/api/v1/auth/logout")
//                                .addLogoutHandler(logoutHandler)
//                                .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext())
//                )
        ;

        return http.build();
    }
}

