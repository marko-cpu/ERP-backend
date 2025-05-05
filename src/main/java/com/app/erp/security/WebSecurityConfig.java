    package com.app.erp.security;
    
    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;
    import org.springframework.security.authentication.AuthenticationManager;
    import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
    import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
    import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
    import org.springframework.security.config.annotation.web.builders.HttpSecurity;
    import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
    import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
    import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
    import org.springframework.security.config.http.SessionCreationPolicy;
    import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
    import org.springframework.security.crypto.password.PasswordEncoder;
    import org.springframework.security.web.SecurityFilterChain;
    import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
    import org.springframework.web.servlet.config.annotation.CorsRegistry;
    import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
    
    
    @Configuration
    @EnableWebSecurity
    @EnableGlobalMethodSecurity(prePostEnabled = true)
    public class WebSecurityConfig implements WebMvcConfigurer {
    
    
        @Autowired
        private JwtAuthenticationEntryPoint unauthorizedHandler;
    
        @Bean
        public JwtAuthenticationFilter authenticationJwtTokenFilter() {
            return new JwtAuthenticationFilter();
        }
    
    
        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }
    
    
        @Override
        public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/**")
                    .allowedOrigins("http://localhost:3000")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true);
        }
    
        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
            return authConfig.getAuthenticationManager();
        }
    
    
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
            http
                    .csrf(AbstractHttpConfigurer::disable)

                    .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
                    .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests((authorize) -> authorize
                            .requestMatchers("health","info").permitAll()
                            .requestMatchers("/api/auth/**").permitAll()
                            .requestMatchers("/api/notifications","/api/notifications/**").permitAll()
                            .requestMatchers("/ws/**", "/ws",  "/topic/**").permitAll()
                            .requestMatchers("/websocket").permitAll()
                            .requestMatchers("/verify").permitAll()

                            .requestMatchers("/users/me/**").permitAll()
                            .requestMatchers("/api/product/**").permitAll()
                            .requestMatchers("/api/orders/**").permitAll()
                            .requestMatchers("/api/customers/**").permitAll()
                            .requestMatchers("/api/warehouses/**").permitAll()
                            .requestMatchers("/api/product-warehouse/**").permitAll()
                            .requestMatchers("/api/article-warehouse/**").permitAll()
                            .requestMatchers("/api/invoices/**").permitAll()
                            .requestMatchers("/api/accountings/**").permitAll()
                            .requestMatchers("/api/reservations/**").permitAll()
                            .requestMatchers("/api/admin/**").permitAll()
    
                            .anyRequest().authenticated()
                    )

                    .logout(LogoutConfigurer::permitAll);
    
            http.authenticationProvider(authenticationProvider());
    
            http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);
    
            return http.build();
        }
    
        @Bean
        public UserDetailsService userDetailsService() {
            return new UserDetailsService();
        }
        @Bean
        DaoAuthenticationProvider authenticationProvider() {
            DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
            authProvider.setUserDetailsService(userDetailsService());
            authProvider.setPasswordEncoder(passwordEncoder());
    
            return authProvider;
        }
    
    
    }
