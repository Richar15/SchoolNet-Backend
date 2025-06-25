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

                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-resources/**").permitAll()
                        .requestMatchers("/webjars/**").permitAll()

                        .requestMatchers("/api/auth/**").permitAll()


                        .requestMatchers(HttpMethod.POST, "/api/students/create").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/students/search").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/students").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/students/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/students/**").permitAll()


                        .requestMatchers(HttpMethod.POST, "/api/teachers/create").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/teachers/search").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/teachers").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/teachers/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/teachers/**").permitAll()


                        .requestMatchers(HttpMethod.GET, "/api/schedules/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/schedules/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/schedules/**").permitAll()

                        .requestMatchers(HttpMethod.POST, "/api/grades/assign").hasRole("TEACHER")
                        .requestMatchers(HttpMethod.GET, "/api/grades/gradeOfStudent").hasAnyRole("TEACHER","STUDENT")
                        .requestMatchers(HttpMethod.GET, "/api/grades/studentsByAssignment").hasRole("TEACHER")
                        .requestMatchers(HttpMethod.GET, "/api/grades/gradesAssignedByProfessor").hasRole("TEACHER")
                        .requestMatchers(HttpMethod.GET, "/api/grades/byProfessor").hasRole("TEACHER")


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