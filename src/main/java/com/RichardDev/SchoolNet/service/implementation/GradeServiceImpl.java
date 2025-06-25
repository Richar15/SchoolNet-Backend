package com.RichardDev.SchoolNet.service.implementation;

import com.RichardDev.SchoolNet.persistence.entity.GradeEntity;
import com.RichardDev.SchoolNet.persistence.entity.ProfessorAssignmentEntity;
import com.RichardDev.SchoolNet.persistence.entity.StudentEntity;
import com.RichardDev.SchoolNet.persistence.repository.GradeRepository;
import com.RichardDev.SchoolNet.persistence.repository.ProfessorAssignmentRepository;
import com.RichardDev.SchoolNet.persistence.repository.StudentRepository;
import com.RichardDev.SchoolNet.presentation.dto.GradeDTO;
import com.RichardDev.SchoolNet.service.exeption.IGradeException;
import com.RichardDev.SchoolNet.service.interfaces.GradeService;
import com.RichardDev.SchoolNet.util.mapper.GradeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GradeServiceImpl implements GradeService {
    private final GradeRepository gradeRepository;
    private final StudentRepository studentRepository;
    private final ProfessorAssignmentRepository assignmentRepository;
    private final GradeMapper gradeMapper;

    @Override
    @Transactional
    public GradeDTO assignGrade(Long professorId, GradeDTO gradeDTO) {
        // Buscar estudiante y asignación
        StudentEntity student = studentRepository.findById(gradeDTO.getStudentId())
                .orElseThrow(() -> new IGradeException("Estudiante no encontrado"));
        ProfessorAssignmentEntity assignment = assignmentRepository.findById(gradeDTO.getAssignmentId())
                .orElseThrow(() -> new IGradeException("Asignación no encontrada"));

        // Validar que el assignment pertenezca al profesor
        if (!assignment.getProfessor().getId().equals(professorId)) {
            throw new IGradeException("No autorizado para calificar este grado/materia");
        }

        // Validar notas
        for (Double g : new Double[]{gradeDTO.getGrade1(), gradeDTO.getGrade2(), gradeDTO.getGrade3(), gradeDTO.getGrade4()}) {
            if (g < 0.0 || g > 5.0) throw new IGradeException("Notas fuera de rango");
        }

        // Calcular nota final
        double finalGrade = (gradeDTO.getGrade1() + gradeDTO.getGrade2() + gradeDTO.getGrade3() + gradeDTO.getGrade4()) / 4.0;

        // Guardar
        GradeEntity entity = GradeEntity.builder()
                .student(student)
                .assignment(assignment)
                .grade1(gradeDTO.getGrade1())
                .grade2(gradeDTO.getGrade2())
                .grade3(gradeDTO.getGrade3())
                .grade4(gradeDTO.getGrade4())
                .finalGrade(finalGrade)
                .build();
        gradeRepository.save(entity);
        return gradeMapper.toDTO(entity);
    }

    @Override
    public List<GradeDTO> getGradesByStudentId(Long studentId) {
        List<GradeEntity> grades = gradeRepository.findByStudentId(studentId);
        return grades.stream()
                .map(gradeMapper::toDTO)
                .collect(Collectors.toList());
    }


    public List<GradeDTO> getGradesAssignedByProfessor(Long professorId) {
        List<GradeEntity> gradeEntities = gradeRepository.findGradesByProfessorId(professorId);
        return gradeEntities.stream()
                .map(gradeMapper::toDTO)
                .collect(Collectors.toList());
    }

}
