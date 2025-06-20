
package com.RichardDev.SchoolNet.service.implementation;

import com.RichardDev.SchoolNet.constant.Grade;
import com.RichardDev.SchoolNet.constant.Subject;
import com.RichardDev.SchoolNet.persistence.entity.ProfessorAssignmentEntity;
import com.RichardDev.SchoolNet.persistence.entity.TeacherEntity;
import com.RichardDev.SchoolNet.persistence.repository.ProfessorAssignmentRepository;
import com.RichardDev.SchoolNet.persistence.repository.TeacherRepository;
import com.RichardDev.SchoolNet.service.interfaces.ProfessorAssignmentService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ProfessorAssignmentServiceImpl implements ProfessorAssignmentService {

    private final ProfessorAssignmentRepository professorAssignmentRepository;
    private final TeacherRepository teacherRepository;

    @Override
    public List<ProfessorAssignmentEntity> getAssignmentsForProfessor(Long professorId) {
        Optional<TeacherEntity> teacherOpt = teacherRepository.findById(professorId);
        return teacherOpt.map(professorAssignmentRepository::findByProfessor)
                .orElse(Collections.emptyList());
    }


    @Override
    public void distributeAssignments() {
        Subject[] subjects = Subject.values();
        Grade[] grades = Grade.values();

        // Elimina todas las asignaciones previas
        professorAssignmentRepository.deleteAll();

        List<TeacherEntity> allTeachers = teacherRepository.findAll();

        for (Subject subject : subjects) {
            // Filtra profesores por área de especialidad que coincida con la materia
            List<TeacherEntity> subjectTeachers = allTeachers.stream()
                .filter(t -> t.getAreaOfExpertise() != null &&
                        t.getAreaOfExpertise().toString().equalsIgnoreCase(subject.name()))
                .collect(Collectors.toList());

            if (subjectTeachers.isEmpty()) continue;

            int teacherCount = subjectTeachers.size();
            for (int i = 0; i < grades.length; i++) {
                TeacherEntity teacher = subjectTeachers.get(i % teacherCount);
                ProfessorAssignmentEntity assignment = new ProfessorAssignmentEntity();
                assignment.setProfessor(teacher);
                assignment.setSubject(subject);
                assignment.setGrade(grades[i]);
                professorAssignmentRepository.save(assignment);
            }
        }
    }

    @Override
    public void assignOnNewProfessor(TeacherEntity newProfessor) {
        if (newProfessor.getId() == null) {
            teacherRepository.save(newProfessor);
        }
        distributeAssignments();
    }
}