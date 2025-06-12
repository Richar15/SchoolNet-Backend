package com.RichardDev.SchoolNet.util.datainitializer;


import com.RichardDev.SchoolNet.persistence.entity.TeacherEntity;
import com.RichardDev.SchoolNet.persistence.repository.TeacherRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final TeacherRepository teacherRepository;

    public DataInitializer(TeacherRepository teacherRepository) {
        this.teacherRepository = teacherRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (teacherRepository.count() == 0) {
            ObjectMapper mapper = new ObjectMapper();
            InputStream inputStream = new ClassPathResource("teachers.json").getInputStream();
            List<TeacherEntity> professors = mapper.readValue(inputStream, new TypeReference<List<TeacherEntity>>() {
            });
            teacherRepository.saveAll(professors);
        }
    }
}
