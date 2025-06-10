package com.RichardDev.SchoolNet.presentation.controller;

import com.RichardDev.SchoolNet.presentation.dto.StudentDto;
import com.RichardDev.SchoolNet.service.implementation.StudentServiceimpl;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
@AllArgsConstructor
public class StudentController {

    private final StudentServiceimpl studentServiceimpl;

    @PostMapping("create")
    public StudentDto create(@RequestBody @Valid StudentDto dto) {
        return studentServiceimpl.create(dto);
    }

    @PutMapping("/{id}")
    public StudentDto update(@PathVariable Long id, @RequestBody @Valid StudentDto dto) {
        return studentServiceimpl.update(id, dto);
    }


    @GetMapping("/search")
    public List<StudentDto> searchByKeyword(@RequestParam String keyword) {
        return studentServiceimpl.searchByKeyword(keyword);
    }


    @GetMapping
    public List<StudentDto> getAll() {
        return studentServiceimpl.getAll();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        studentServiceimpl.delete(id);
    }

}
