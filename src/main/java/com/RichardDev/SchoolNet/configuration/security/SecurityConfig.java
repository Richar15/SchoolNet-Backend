package com.RichardDev.SchoolNet.configuration.security;

import com.RichardDev.SchoolNet.service.implementation.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;


@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                    // Permitir acceso a endpoints de Swagger/OpenAPI
                    .requestMatchers("/swagger-ui/**").permitAll()
                    .requestMatchers("/v3/api-docs/**").permitAll()
                    .requestMatchers("/swagger-resources/**").permitAll()
                    .requestMatchers("/webjars/**").permitAll()

                    // Endpoints públicos de autenticación
                    .requestMatchers("/api/auth/**").permitAll()

                    // Endpoints de ESTUDIANTES
                    .requestMatchers(HttpMethod.POST, "/api/students/create").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/students/search").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/students").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/students/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/students/**").hasRole("ADMIN")

                    // Endpoints de PROFESORES - mismo patrón que estudiantes
                    .requestMatchers(HttpMethod.POST, "/api/teachers/create").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/teachers/search").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/teachers").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/teachers/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/teachers/**").hasRole("ADMIN")

                    // Endpoints de HORARIOS
                    .requestMatchers(HttpMethod.GET, "/api/schedules/**").hasAnyRole("ADMIN", "TEACHER", "STUDENT")
                    .requestMatchers(HttpMethod.POST, "/api/schedules/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/schedules/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/schedules/**").hasRole("ADMIN")

                    // Endpoints específicos por rol
                    .requestMatchers("/api/teacher/**").hasAnyRole("TEACHER", "ADMIN")
                    .requestMatchers("/api/student/**").hasAnyRole("STUDENT", "ADMIN")

                    .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
}
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
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