package com.couriertracking.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Custom API Key Authentication Filter
 * This filter intercepts HTTP requests and validates the API key provided in the request header.
 * If a valid API key is provided, it sets up the security context with authentication.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ApiKeyAuthFilter extends OncePerRequestFilter {
    
    @Value("${courier-tracking.api.key}")
    private String validApiKey;
    
    @Value("${courier-tracking.api.header-name:X-API-Key}")
    private String apiKeyHeaderName;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        String apiKey = request.getHeader(apiKeyHeaderName);
        
        log.debug("Processing request to: {} with API key: {}", requestURI, 
                  apiKey != null ? "***" + apiKey.substring(Math.max(0, apiKey.length() - 4)) : "null");
        
        // Check if API key is provided and valid
        if (apiKey != null && validApiKey.equals(apiKey)) {
            // Create authentication token for valid API key
            UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(
                    "api-client", 
                    null, 
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_API_CLIENT"))
                );
            
            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);
            log.debug("Valid API key provided. Authentication set for request: {}", requestURI);
        } else {
            log.debug("No valid API key provided for request: {}", requestURI);
        }
        
        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // Skip filtering for actuator endpoints
        return path.startsWith("/actuator");
    }
}