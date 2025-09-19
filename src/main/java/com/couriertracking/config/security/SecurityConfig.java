package com.couriertracking.config.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security Configuration for API Key Authentication
 * 
 * This configuration:
 * - Disables CSRF (not needed for API key authentication)
 * - Sets session policy to stateless (no sessions needed)
 * - Protects all /api/** endpoints with API key authentication
 * - Allows public access to actuator endpoints and H2 console
 * - Adds custom API key filter before username/password filter
 * - Provides custom authentication entry point for unauthorized requests
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {
    
    private final ApiKeyAuthFilter apiKeyAuthFilter;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            // Disable CSRF - not needed for API key authentication
            .csrf(AbstractHttpConfigurer::disable)
            
            // Disable form login and HTTP basic auth
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            
            // Set session management to stateless
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configure authorization rules
            .authorizeHttpRequests(auth -> auth
                // Allow public access to actuator endpoints
                .requestMatchers("/actuator/**").permitAll()
                
                // Allow public access to H2 console (development only)
                .requestMatchers("/h2-console/**").permitAll()
                
                // Require API_CLIENT role for all /api/** endpoints
                .requestMatchers("/api/**").hasRole("API_CLIENT")
                
                // All other requests need authentication
                .anyRequest().authenticated()
            )
            
            // Add our custom API key filter before the standard username/password filter
            .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class)
            
            // Custom authentication entry point for unauthorized requests
            .exceptionHandling(ex -> ex.authenticationEntryPoint(
                (request, response, authException) -> {
                    log.warn("Unauthorized access attempt to: {} from IP: {}", 
                            request.getRequestURI(), request.getRemoteAddr());
                    
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    
                    String jsonResponse = """
                        {
                          "error": "UNAUTHORIZED",
                          "message": "Invalid or missing API key. Please provide a valid X-API-Key header.",
                          "status": 401,
                          "path": "%s",
                          "timestamp": "%s"
                        }
                        """.formatted(
                            request.getRequestURI(),
                            java.time.LocalDateTime.now().toString()
                        );
                    
                    response.getWriter().write(jsonResponse);
                }
            ))
            
            // Allow frames for H2 console (development only)
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            
            .build();
    }
}