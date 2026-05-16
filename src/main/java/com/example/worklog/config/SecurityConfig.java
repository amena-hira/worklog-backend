package com.example.worklog.config;

import com.example.worklog.config.jwt.JwtFilter;
import com.example.worklog.exception.CustomAccessDeniedHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

// Indicates this class contains Spring Bean configurations
@Configuration
// Enables Spring Security's web security support and provides the Spring MVC integration
@EnableWebSecurity
public class SecurityConfig {
    
    // Injects the custom JWT filter that we created to validate tokens
    @Autowired
    private JwtFilter jwtFilter;

    @Autowired
    private CustomAccessDeniedHandler customAccessDeniedHandler;

    /**
     * Exposes the AuthenticationManager as a Bean.
     * This is required to process authentication requests (like verifying email/password) in our AuthController.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception{
        return authConfig.getAuthenticationManager();
    }

    /**
     * This is the core method of Spring Security configuration.
     * It defines which API endpoints are public, which are protected, and how sessions are handled.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable) // Disables Cross-Site Request Forgery protection (not needed for stateless JWT APIs)
                .cors(cors -> cors.configurationSource(corsConfigurationSource)) // Applies our custom CORS configuration (defined below)
                .authorizeHttpRequests(auth -> auth
                        // Permits all incoming requests to the authentication endpoints (login, register) without a token
                        .requestMatchers("/api/auth/**").permitAll()
                        
                        // User-specific restrictions
                        .requestMatchers(HttpMethod.PUT, "/api/users/me").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/users/me").hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/api/users/**").hasRole("ADMIN") // Only ADMIN can access other /api/users endpoints

                        // Project-specific restrictions
                        .requestMatchers(HttpMethod.GET, "/api/projects").hasRole("ADMIN") // Only ADMIN can get ALL projects

                        // Task-specific restrictions
                        .requestMatchers(HttpMethod.GET, "/api/tasks").hasRole("ADMIN") // Only ADMIN can get ALL tasks
                        
                        // Requires either the USER or ADMIN role for all other API endpoints
                        .requestMatchers("/api/**").hasAnyRole("USER", "ADMIN")

                        // Enforces authentication for absolutely any other request not explicitly matched above
                        .anyRequest().authenticated())
                // Configures Spring Security to NOT create an HTTP session.
                // We use JWT tokens for every request, making our application completely stateless
                .sessionManagement(smc-> smc.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Attach our custom AccessDeniedHandler to format 403 errors into our standard JSON response
                .exceptionHandling(ex -> ex.accessDeniedHandler(customAccessDeniedHandler));

        // Registers our custom JwtFilter to execute BEFORE Spring Security's default UsernamePasswordAuthenticationFilter.
        // This ensures the JWT is validated and the security context is populated before Spring checks endpoint authorizations.
        http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    /**
     * Configures the password encoder Bean.
     * BCrypt is a strong hashing algorithm used to securely store passwords in the database.
     */
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    /**
     * Configures Cross-Origin Resource Sharing (CORS).
     * This tells the backend to accept requests from specific external frontend domains.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true); // Allows cookies/credentials to be sent (though we use headers mostly)
        config.addAllowedOrigin("http://localhost:4200"); // Explicitly allows the Angular frontend running on port 4200
        config.addAllowedHeader("*"); // Allows any HTTP headers in the request (like Authorization, Content-Type)
        config.addAllowedMethod("*"); // Allows any HTTP methods (GET, POST, PUT, DELETE, OPTIONS, etc.)

        // Applies this specific CORS configuration to all paths ("/**") on the backend
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

}
