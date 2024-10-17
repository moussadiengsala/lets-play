//package com.zone01.products.config;
//
//import com.zone01.products.products.Permission;
//import com.zone01.products.products.UsersClient;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.authentication.AuthenticationProvider;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.access.AccessDeniedHandler;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
//
//@Configuration
//@EnableWebSecurity
//@RequiredArgsConstructor
//@EnableMethodSecurity
//public class SecurityConfiguration {
//    private final AccessValidation accessValidation;
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(AbstractHttpConfigurer::disable)
//                .authorizeHttpRequests(req ->
//                        req
//                                .requestMatchers(HttpMethod.GET, "/api/v1/products/**").hasAnyAuthority(Permission.ADMIN_READ.getPermission(), Permission.USER_READ.getPermission())
//                                .requestMatchers(HttpMethod.POST, "/api/v1/products/**").hasAnyAuthority(Permission.ADMIN_CREATE.getPermission())
//                                .requestMatchers(HttpMethod.PUT, "/api/v1/products/**").hasAnyAuthority(Permission.ADMIN_UPDATE.getPermission())
//                                .requestMatchers(HttpMethod.DELETE, "/api/v1/products/**").hasAnyAuthority(Permission.ADMIN_DELETE.getPermission())
//                                .anyRequest()
//                                .authenticated()
//                )
//                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .addFilterAt(accessValidation, UsernamePasswordAuthenticationFilter.class)  // Position of the filter in the chain
//                .authenticationProvider(null);
//
//        return http.build();
//    }
//}
