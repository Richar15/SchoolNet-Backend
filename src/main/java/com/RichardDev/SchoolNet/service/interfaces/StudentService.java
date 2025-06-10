package com.RichardDev.SchoolNet.service.interfaces;

import com.RichardDev.SchoolNet.presentation.dto.StudentDto;

import java.util.List;

public interface StudentService {

    StudentDto create(StudentDto dto);
    StudentDto update(Long id, StudentDto dto);
    List<StudentDto> getAll();
    void delete(Long id);
    List<StudentDto> searchByKeyword(String keyword);


}

