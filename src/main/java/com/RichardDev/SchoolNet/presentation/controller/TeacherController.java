package com.RichardDev.SchoolNet.presentation.controller;

import com.RichardDev.SchoolNet.presentation.dto.TeacherDto;
import com.RichardDev.SchoolNet.service.implementation.TeacherServiceImpl;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teachers")
@AllArgsConstructor
public class TeacherController {

    private final TeacherServiceImpl teacherServiceImpl;


    @PostMapping("create")
    public TeacherDto create(@RequestBody @Valid TeacherDto dto) {
        return teacherServiceImpl.create(dto);
    }

    @PutMapping("/{id}")
    public TeacherDto update(@PathVariable Long id, @RequestBody @Valid TeacherDto dto) {
        return teacherServiceImpl.update(id, dto);
    }

    @GetMapping("/search")
    public List<TeacherDto> searchByKeyword(@RequestParam String keyword) {
        return teacherServiceImpl.searchByKeyword(keyword);
    }

    @GetMapping
    public List<TeacherDto> getAll() {
        return teacherServiceImpl.getAll();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        teacherServiceImpl.delete(id);
    }

}
