package com.RichardDev.SchoolNet.service.interfaces;

import com.RichardDev.SchoolNet.presentation.dto.TeacherDto;

import java.util.List;

public interface TeacherService {
    TeacherDto create(TeacherDto dto);
    TeacherDto update(Long id, TeacherDto dto);
    List<TeacherDto> getAll();
    void delete(Long id);
    List<TeacherDto> searchByKeyword(String keyword);
}
