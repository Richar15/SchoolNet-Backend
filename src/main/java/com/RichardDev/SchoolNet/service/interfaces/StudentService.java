package com.RichardDev.SchoolNet.service.interfaces;

import com.RichardDev.SchoolNet.presentation.dto.StudentDTO;

import java.util.List;

public interface StudentService {

    StudentDTO create(StudentDTO dto);
    StudentDTO update(Long id, StudentDTO dto);
    List<StudentDTO> getAll();
    void delete(Long id);
    List<StudentDTO> searchByKeyword(String keyword);


}

