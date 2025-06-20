package com.RichardDev.SchoolNet.service.implementation;

import com.RichardDev.SchoolNet.persistence.entity.AdminEntity;
import com.RichardDev.SchoolNet.persistence.entity.StudentEntity;
import com.RichardDev.SchoolNet.persistence.entity.TeacherEntity;
import com.RichardDev.SchoolNet.persistence.entity.UserEntity;
import com.RichardDev.SchoolNet.persistence.repository.AdminRepository;
import com.RichardDev.SchoolNet.persistence.repository.StudentRepository;
import com.RichardDev.SchoolNet.persistence.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        Optional<StudentEntity> student = studentRepository.findByUsername(username);
        if (student.isPresent()) {
            return buildUserDetails(student.get());
        }

        Optional<TeacherEntity> teacher = teacherRepository.findByUsername(username);
        if (teacher.isPresent()) {
            return buildUserDetails(teacher.get());
        }

        Optional<AdminEntity> admin = adminRepository.findByUsername(username);
        if (admin.isPresent()) {
            return buildUserDetails(admin.get());
        }

        throw new UsernameNotFoundException("Usuario no encontrado: " + username);
    }

    private UserDetails buildUserDetails(UserEntity user) {
        SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRol().name());

        return new User(
                user.getUsername(),
                user.getPassword(),
                true, true, true, true,
                Collections.singleton(authority)
        );
    }

    public Long getUserIdFromUsername(String username) throws UsernameNotFoundException {
        Optional<StudentEntity> student = studentRepository.findByUsername(username);
        if (student.isPresent()) {
            return student.get().getId();
        }

        Optional<TeacherEntity> teacher = teacherRepository.findByUsername(username);
        if (teacher.isPresent()) {
            return teacher.get().getId();
        }

        Optional<AdminEntity> admin = adminRepository.findByUsername(username);
        if (admin.isPresent()) {
            return admin.get().getId();
        }

        throw new UsernameNotFoundException("Usuario no encontrado: " + username);
    }

}
