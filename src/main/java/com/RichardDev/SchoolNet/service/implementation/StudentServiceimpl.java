package com.RichardDev.SchoolNet.service.implementation;

import com.RichardDev.SchoolNet.persistence.entity.ProfessorAssignmentEntity;
import com.RichardDev.SchoolNet.persistence.entity.StudentEntity;
import com.RichardDev.SchoolNet.persistence.repository.ProfessorAssignmentRepository;
import com.RichardDev.SchoolNet.persistence.repository.StudentRepository;
import com.RichardDev.SchoolNet.presentation.dto.StudentDTO;
import com.RichardDev.SchoolNet.presentation.dto.StudentGradeDto;
import com.RichardDev.SchoolNet.service.exeption.UniqueFieldException;
import com.RichardDev.SchoolNet.service.exeption.UserNotFoundException;
import com.RichardDev.SchoolNet.service.interfaces.StudentService;
import com.RichardDev.SchoolNet.util.mapper.StudentMapper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class StudentServiceimpl implements StudentService {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProfessorAssignmentRepository professorAssignmentRepository;

    @Override
public StudentDTO create(StudentDTO dto) {
    try {
        StudentEntity entity = StudentMapper.toEntity(dto);

        entity.setPassword(passwordEncoder.encode(entity.getPassword()));
        return StudentMapper.toDto(studentRepository.save(entity));
    } catch (org.springframework.dao.DataIntegrityViolationException e) {
        throw new UniqueFieldException("Ya existe un estudiante con el mismo nombre, username, email, teléfono o apellido.");
    }
}

   @Override
   public StudentDTO update(Long id, StudentDTO dto) {
       StudentEntity entity = studentRepository.findById(id).orElse(null);
       if (entity == null) {
           throw new UserNotFoundException("Estudiante no encontrado con id: " + id);
       }

       if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
           dto.setPassword(passwordEncoder.encode(dto.getPassword()));
       }

       StudentMapper.updateEntityFromDto(dto, entity);
       return StudentMapper.toDto(studentRepository.save(entity));
   }


    @Override
    public List<StudentDTO> getAll() {
        List<StudentEntity> students = studentRepository.findAll();
        if (students.isEmpty()) {
            throw new UserNotFoundException("No se encontraron estudiantes registrados.");
        }
        return students.stream()
                .map(StudentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<StudentDTO> searchByKeyword(String keyword) {
        List<StudentEntity> students = studentRepository.searchByKeyword(keyword);
        if (students.isEmpty()) {
            throw new UserNotFoundException("No se econtraron estudiantes que coincidan con: " + keyword);
        }
        return students.stream()
                .map(StudentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        studentRepository.deleteById(id);
    }


    public List<StudentGradeDto> getStudentsByAssignment(Long assignmentId) {
        ProfessorAssignmentEntity assignment = professorAssignmentRepository.findById(assignmentId)
                .orElseThrow(() -> new RuntimeException("Asignación no encontrada"));

        List<StudentEntity> students = studentRepository.findByGrade(assignment.getGrade());

        return students.stream()
                .map(s -> {
                    StudentGradeDto dto = new StudentGradeDto();
                    dto.setId(s.getId());
                    dto.setName(s.getName());
                    return dto;
                })
                .collect(Collectors.toList());
    }


}