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

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {
    
    private final ApiKeyAuthFilter apiKeyAuthFilter;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**").permitAll()                
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/api/**").hasRole("API_CLIENT")
                
                // All other requests need authentication
                .anyRequest().authenticated()
            )
            
            .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class)
            
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