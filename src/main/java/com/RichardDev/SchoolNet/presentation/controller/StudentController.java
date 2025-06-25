package com.RichardDev.SchoolNet.presentation.controller;

import com.RichardDev.SchoolNet.presentation.dto.GradeDTO;
import com.RichardDev.SchoolNet.presentation.dto.StudentDTO;
import com.RichardDev.SchoolNet.presentation.dto.StudentGradeDto;
import com.RichardDev.SchoolNet.service.implementation.StudentServiceimpl;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@AllArgsConstructor
public class StudentController {

    private final StudentServiceimpl studentServiceimpl;

    @PostMapping("create")
    public StudentDTO create(@RequestBody @Valid StudentDTO dto) {
        return studentServiceimpl.create(dto);
    }

    @GetMapping("/search")
    public List<StudentDTO> searchByKeyword(@RequestParam String keyword) {
        return studentServiceimpl.searchByKeyword(keyword);
    }

    @PutMapping("/{id}")
    public StudentDTO update(@PathVariable Long id, @RequestBody @Valid StudentDTO dto) {
        return studentServiceimpl.update(id, dto);
    }
    @GetMapping
    public List<StudentDTO> getAll() {
        return studentServiceimpl.getAll();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        studentServiceimpl.delete(id);
    }




}
