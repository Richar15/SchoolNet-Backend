package com.RichardDev.SchoolNet.util.datainitializer;

import com.RichardDev.SchoolNet.persistence.entity.TeacherEntity;
import com.RichardDev.SchoolNet.persistence.repository.TeacherRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
public class DataInitializerTeacher implements CommandLineRunner {

    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializerTeacher(TeacherRepository teacherRepository, PasswordEncoder passwordEncoder) {
        this.teacherRepository = teacherRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (teacherRepository.count() == 0) {
            ObjectMapper mapper = new ObjectMapper();
            InputStream inputStream = new ClassPathResource("teachers.json").getInputStream();
            List<TeacherEntity> professors = mapper.readValue(inputStream, new TypeReference<List<TeacherEntity>>() {
            });


            for (TeacherEntity teacher : professors) {
                teacher.setPassword(passwordEncoder.encode(teacher.getPassword()));
            }

            teacherRepository.saveAll(professors);
        }
    }
}