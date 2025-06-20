package com.RichardDev.SchoolNet.util.mapper;
import com.RichardDev.SchoolNet.persistence.entity.StudentEntity;
import com.RichardDev.SchoolNet.presentation.dto.StudentDTO;


public class StudentMapper {
    public static StudentDTO toDto(StudentEntity entity) {
        StudentDTO dto = new StudentDTO();
        dto.setName(entity.getName());
        dto.setLastName(entity.getLastName());
        dto.setUsername(entity.getUsername());
        dto.setPassword(entity.getPassword());
        dto.setEmail(entity.getEmail());
        dto.setPhone(entity.getPhone());
        dto.setAddress(entity.getAddress());
        dto.setGrade(entity.getGrade());
        return dto;
    }

    public static void updateEntityFromDto(StudentDTO dto, StudentEntity entity) {
        if (dto.getName() != null) entity.setName(dto.getName());
        if (dto.getLastName() != null) entity.setLastName(dto.getLastName());
        if (dto.getUsername() != null) entity.setUsername(dto.getUsername());
        if (dto.getPassword() != null) entity.setPassword(dto.getPassword());
        if (dto.getEmail() != null) entity.setEmail(dto.getEmail());
        if (dto.getPhone() != null) entity.setPhone(dto.getPhone());
        if (dto.getAddress() != null) entity.setAddress(dto.getAddress());
        if (dto.getGrade() != null) entity.setGrade(dto.getGrade());
    }

    public static StudentEntity toEntity(StudentDTO dto) {
        StudentEntity entity = new StudentEntity();
        updateEntityFromDto(dto, entity);
        return entity;
    }
}