package com.RichardDev.SchoolNet.util.datainitializer;

import com.RichardDev.SchoolNet.constant.Rol;
import com.RichardDev.SchoolNet.persistence.entity.AdminEntity;
import com.RichardDev.SchoolNet.persistence.repository.AdminRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;


@Configuration
public class AdminInitializer {

    @Bean
    public CommandLineRunner initAdmin(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (!adminRepository.existsByUsername("admin")) {
                AdminEntity admin = new AdminEntity();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin1234"));
                admin.setEmail("admin@gmail.com");
                admin.setPhone("3172719261");
                admin.setName("Richard Antonio");
                admin.setLastName("Assis Trujillo");
                admin.setAddress("Calle 45 #22-18");
                admin.setRol(Rol.ADMIN);
                adminRepository.save(admin);
            }
        };
    }
}
