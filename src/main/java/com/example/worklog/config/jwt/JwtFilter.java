package com.example.worklog.config.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// Marks this class as a Spring Component so it can be managed by the application context
@Component
public class JwtFilter extends OncePerRequestFilter {
    
    // Injects the utility class used to parse and validate JWT tokens
    @Autowired
    private JwtUtil jwtUtil;

    // Injects the Spring Security service used to load user details from the database
    @Autowired
    private UserDetailsService userDetailsService;

    /**
     * This method intercepts every incoming HTTP request to check for a valid JWT token.
     * It extends OncePerRequestFilter to ensure it's only executed once per request dispatch.
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 1. Extract the Authorization header from the incoming request
        final String authHeader = request.getHeader("Authorization");

        String email = null;
        String jwt = null;

        // 2. Check if the header exists and starts with the expected "Bearer " prefix
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // Extract the actual token string by removing the "Bearer " prefix (length of 7)
            jwt = authHeader.substring(7);
            // Use our JwtUtil to extract the user's email from the token payload
            email = jwtUtil.extractEmail(jwt);
        }

        // 3. If an email was found in the token AND the user is not yet authenticated in the current security context
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Load the user's full details (password, roles, etc.) from the database using their email
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // 4. Validate the token to ensure it belongs to this user and hasn't expired
            if (jwtUtil.validateToken(jwt, userDetails)) {
                
                // Create an authentication token object with the user details and their granted authorities (roles)
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                // Attach additional details about the web request (e.g., IP address, session ID) to the auth token
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 5. Officially set the user as "authenticated" in Spring Security's context for this request
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 6. Continue the filter chain, allowing the request to proceed to the next filter or controller
        filterChain.doFilter(request, response);
    }
}
