package com.RichardDev.SchoolNet.util.datainitializer;

import com.RichardDev.SchoolNet.persistence.entity.StudentEntity;
import com.RichardDev.SchoolNet.persistence.repository.StudentRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
public class DataInitializerStudent implements CommandLineRunner {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializerStudent(StudentRepository studentRepository, PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (studentRepository.count() == 0) {
            ObjectMapper mapper = new ObjectMapper();
            InputStream inputStream = new ClassPathResource("students.json").getInputStream();
            List<StudentEntity> students = mapper.readValue(inputStream, new TypeReference<List<StudentEntity>>() {
            });


            for (StudentEntity student : students) {
                student.setPassword(passwordEncoder.encode(student.getPassword()));
            }

            studentRepository.saveAll(students);
        }
    }
}