package com.couriertracking.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * API Key Authentication Filter
 * This filter intercepts HTTP requests and validates the API key provided in the request header.
 */
@Component
@RequiredArgsConstructor
public class ApiKeyAuthFilter extends OncePerRequestFilter {
    
    @Value("${courier-tracking.api.key}")
    private String validApiKey;
    
    @Value("${courier-tracking.api.header-name:X-API-Key}")
    private String apiKeyHeaderName;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String apiKey = request.getHeader(apiKeyHeaderName);
        
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
        }
        
        // Continue with the filter chain
        filterChain.doFilter(request, response);
    }
    
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator");
    }
}