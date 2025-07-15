package com.forkmyfolio.config;

import com.forkmyfolio.security.CustomUserDetailsService;
import com.forkmyfolio.security.JwtAuthenticationEntryPoint;
import com.forkmyfolio.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

/**
 * Main security configuration class for the application.
 * Configures Spring Security, including password encoding, JWT authentication,
 * and HTTP security rules.
 */
@Configuration
@EnableWebSecurity // debug = true removed
@EnableMethodSecurity(jsr250Enabled = true, securedEnabled = true) // Enables @RolesAllowed, @Secured
public class SecurityConfig {

    private static final String[] PUBLIC_MATCHERS = {
            "/auth/**",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/api-docs/**",
            "/h2-console/**", // Allow H2 console access for development
            // Publicly accessible GET requests for projects and skills
            "/api/v1/projects",
            "/api/v1/skills",
            // Publicly accessible POST for contact messages
            "/api/v1/contact-messages"
    };
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtAuthenticationEntryPoint unauthorizedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    @Value("${app.cors.allowed-origins}")
    private String[] allowedOrigins;

    /**
     * Constructs the SecurityConfig with necessary custom components.
     *
     * @param customUserDetailsService Service to load user-specific data.
     * @param unauthorizedHandler      Handles unauthorized access attempts.
     * @param jwtAuthenticationFilter  Filter to process JWT tokens.
     */
    @Autowired
    public SecurityConfig(CustomUserDetailsService customUserDetailsService,
                          JwtAuthenticationEntryPoint unauthorizedHandler,
                          JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.customUserDetailsService = customUserDetailsService;
        this.unauthorizedHandler = unauthorizedHandler;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }


    /**
     * Configures a CorsFilter bean for handling Cross-Origin Resource Sharing (CORS).
     * Allows all origins, headers, and methods for simplicity during development.
     * This should be restricted in a production environment.
     *
     * @return A CorsFilter instance.
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        // config.addAllowedOriginPattern("*"); // Replaced by specific origins
        if (allowedOrigins != null && allowedOrigins.length > 0) {
            config.setAllowedOrigins(Arrays.asList(allowedOrigins));
        }
        config.setAllowedHeaders(Arrays.asList("Origin", "Content-Type", "Accept", "Authorization", "X-Requested-With", "Access-Control-Request-Method", "Access-Control-Request-Headers"));
        config.setExposedHeaders(Arrays.asList("Origin", "Content-Type", "Accept", "Authorization", "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }


    /**
     * Defines the security filter chain for HTTP requests.
     * Configures CSRF, session management, exception handling, and authorization rules.
     *
     * @param http The HttpSecurity object to configure.
     * @return The configured SecurityFilterChain.
     * @throws Exception If an error occurs during configuration.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults()) // This will use the CorsFilter bean if available
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(unauthorizedHandler) // Handle unauthorized attempts
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // Use stateless sessions
                )
                .authorizeHttpRequests(authorize -> authorize
                        // --- Rule Order: Most Specific to Most General ---

                        // 1. PUBLIC endpoints that anyone can access.
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/h2-console/**",
                                "/api/v1/profile",
                                "/api/v1/projects/**",
                                "/api/v1/skills/**",
                                "/api/v1/experience/**",
                                "/api/v1/testimonials/**",
                                "/api/v1/qualifications/**",
                                "/api/v1/qualifications/**" ,
                                "/api/v1/contact-messages" ,
                                "/api/v1/contact-messages/**" ,

                                "/api/v1/portfolio-profile" // <-- RENAMED from /api/v1/profile



                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/profile", "/api/v1/projects/**", "/api/v1/skills/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/contact").permitAll()

                        // 2. ADMIN endpoints. Only users with the 'ADMIN' role can access these.
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                        // 3. ANY OTHER request that hasn't been matched yet must be authenticated.
                        //    This rule MUST BE LAST.
                        .anyRequest().authenticated()
                );

        // Add JWT token filter before UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        // For H2 console frame options if Spring Security is enabled
        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));


        return http.build();
    }
}
