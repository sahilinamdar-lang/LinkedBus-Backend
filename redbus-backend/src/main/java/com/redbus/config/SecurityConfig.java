package com.redbus.config;

import com.redbus.adminrepository.AdminRepository;
import com.redbus.adminsecurity.AdminPresenceFilter;
import com.redbus.repository.UserRepository;
import com.redbus.security.JwtFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    private final JwtFilter jwtFilter;
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    public SecurityConfig(JwtFilter jwtFilter,
                          UserRepository userRepository,
                          AdminRepository adminRepository) {
        this.jwtFilter = jwtFilter;
        this.userRepository = userRepository;
        this.adminRepository = adminRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // wire our CORS configuration
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // stateless, disable CSRF for token-based APIs
            .csrf(csrf -> csrf.disable())

            // authorization rules
            .authorizeHttpRequests(auth -> auth
                // public endpoints (support endpoint intentionally public)
                .requestMatchers(
                    "/api/auth/**",
                    "/api/bus/**",
                    "/api/seats/**",
                    "/api/support/**",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/swagger-ui.html",
                    "/actuator/**"
                ).permitAll()

                // allow preflight for any path
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // admin endpoints
                .requestMatchers("/admin-api/**").hasRole("ADMIN")

                // require auth for invoice download and bookings/users endpoints
                .requestMatchers(HttpMethod.GET, "/api/bookings/*/invoice").authenticated()
                .requestMatchers("/api/bookings/**", "/api/users/**").authenticated()

                // fallback
                .anyRequest().authenticated()
            )

            // stateless session management
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // add JWT filter before UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        // Register AdminPresenceFilter AFTER JwtFilter so SecurityContext is populated
        http.addFilterAfter(new AdminPresenceFilter(userRepository, adminRepository), JwtFilter.class);

        return http.build();
    }

    /**
     * CORS configuration:
     * - Use setAllowedOriginPatterns with "http://localhost:*" so multiple dev ports are accepted.
     * - Keep allowCredentials(true) so cookies/Authorization header are allowed.
     * - Explicitly allow common headers and methods.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Allow dev origins (pattern) and specific production origins if you want.
        // Using patterns allows "http://localhost:3000", "http://localhost:5173", etc.
        configuration.setAllowedOriginPatterns(List.of(
            "http://localhost:*",
            "https://your.production.frontend.com" // replace/remove as appropriate
        ));

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        // Allow headers needed by the browser and your app
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept", "X-Requested-With"));
        // Expose headers to browser if needed (e.g., Location on 201 responses)
        configuration.setExposedHeaders(List.of("Location", "Content-Disposition"));
        configuration.setAllowCredentials(true); // keep true if you need cookies/auth
        // Optional: reduce preflight caching during dev. Increase for production if desired.
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        // Optional dev logging: uncomment to log CORS incoming origins (helpful while debugging)
        // log.info("CORS patterns: {}", configuration.getAllowedOriginPatterns());

        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
