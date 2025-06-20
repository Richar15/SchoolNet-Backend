package com.RichardDev.SchoolNet.presentation.controller;

import com.RichardDev.SchoolNet.persistence.entity.ProfessorAssignmentEntity;
import com.RichardDev.SchoolNet.presentation.dto.GradeDTO;
import com.RichardDev.SchoolNet.presentation.dto.StudentGradeDto;
import com.RichardDev.SchoolNet.service.implementation.GradeServiceImpl;
import com.RichardDev.SchoolNet.service.implementation.JwtService;
import com.RichardDev.SchoolNet.service.implementation.StudentServiceimpl;
import com.RichardDev.SchoolNet.service.interfaces.GradeService;
import com.RichardDev.SchoolNet.service.interfaces.ProfessorAssignmentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grades")
@RequiredArgsConstructor
public class GradeController {

    private final GradeService gradeService;
    private final JwtService jwtService;
    private final ProfessorAssignmentService professorAssignmentService;
    private final StudentServiceimpl studentServiceimpl;
    private final GradeServiceImpl gradeServiceImpl;

    @PostMapping("/assign")
    public ResponseEntity<GradeDTO> assignGrade(
            HttpServletRequest request,
            @RequestBody GradeDTO gradeDTO
    ) {
        String authHeader = request.getHeader("Authorization");
        String token = authHeader != null && authHeader.startsWith("Bearer ") ? authHeader.substring(7) : null;
        Long professorId = jwtService.extractUserId(token);
        GradeDTO result = gradeService.assignGrade(professorId, gradeDTO);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/byProfessor")
    public ResponseEntity<List<ProfessorAssignmentEntity>> getAssignmentsForLoggedProfessor(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token = (authHeader != null && authHeader.startsWith("Bearer ")) ? authHeader.substring(7) : null;

        Long professorId = jwtService.extractUserId(token);

        List<ProfessorAssignmentEntity> assignments = professorAssignmentService.getAssignmentsForProfessor(professorId);

        return ResponseEntity.ok(assignments);
    }

    @GetMapping("/studentsByAssignment/{id}")
    public ResponseEntity<List<StudentGradeDto>> getStudentsByAssignment(@PathVariable Long id) {
        List<StudentGradeDto> students = studentServiceimpl.getStudentsByAssignment(id);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/gradeOfStudent")
    public ResponseEntity<List<GradeDTO>> getStudentGrades(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token = authHeader != null && authHeader.startsWith("Bearer ") ? authHeader.substring(7) : null;
        Long studentId = jwtService.extractUserId(token);
        List<GradeDTO> grades = gradeService.getGradesByStudentId(studentId);
        return ResponseEntity.ok(grades);
    }


    @GetMapping("/gradesAssignedByProfessor")
    public ResponseEntity<List<GradeDTO>> getGradesAssignedByProfessor(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String token = authHeader != null && authHeader.startsWith("Bearer ") ? authHeader.substring(7) : null;
        Long professorId = jwtService.extractUserId(token);
        List<GradeDTO> grades = gradeServiceImpl.getGradesAssignedByProfessor(professorId);
        return ResponseEntity.ok(grades);
    }

}