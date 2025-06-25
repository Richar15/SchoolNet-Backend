package com.RichardDev.SchoolNet.service.implementation;

import com.RichardDev.SchoolNet.constant.Rol;
import com.RichardDev.SchoolNet.persistence.entity.AdminEntity;
import com.RichardDev.SchoolNet.persistence.entity.StudentEntity;
import com.RichardDev.SchoolNet.persistence.entity.TeacherEntity;
import com.RichardDev.SchoolNet.persistence.repository.AdminRepository;
import com.RichardDev.SchoolNet.persistence.repository.StudentRepository;
import com.RichardDev.SchoolNet.persistence.repository.TeacherRepository;
import com.RichardDev.SchoolNet.presentation.dto.AuthResponseDTO;
import com.RichardDev.SchoolNet.presentation.dto.LoginRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final AdminRepository adminRepository;

    public AuthResponseDTO login(LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Cargar detalles del usuario
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        String token = jwtService.generateToken(userDetails);

        // Buscar el ID y rol específico
        Long userId = null;
        Rol userRol = null;

        // Buscar en repositorios
        Optional<StudentEntity> student = studentRepository.findByUsername(request.getUsername());
        if (student.isPresent()) {
            userId = student.get().getId();
            userRol = student.get().getRol();
        } else {
            Optional<TeacherEntity> teacher = teacherRepository.findByUsername(request.getUsername());
            if (teacher.isPresent()) {
                userId = teacher.get().getId();
                userRol = teacher.get().getRol();
            } else {
                Optional<AdminEntity> admin = adminRepository.findByUsername(request.getUsername());
                if (admin.isPresent()) {
                    userId = admin.get().getId();
                    userRol = admin.get().getRol();
                }
            }
        }

        return AuthResponseDTO.builder()
                .token(token)
                .username(request.getUsername())
                .rol(userRol != null ? userRol.name() : null)
                .userId(userId)
                .build();
    }
}