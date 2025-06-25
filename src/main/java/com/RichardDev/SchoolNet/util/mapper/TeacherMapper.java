package com.RichardDev.SchoolNet.util.mapper;

import com.RichardDev.SchoolNet.persistence.entity.TeacherEntity;
import com.RichardDev.SchoolNet.presentation.dto.TeacherDto;

public class TeacherMapper {

    public static TeacherDto toDto(TeacherEntity entity) {
        TeacherDto dto = new TeacherDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setLastName(entity.getLastName());
        dto.setUsername(entity.getUsername());
        dto.setPassword(entity.getPassword());
        dto.setEmail(entity.getEmail());
        dto.setPhone(entity.getPhone());
        dto.setAddress(entity.getAddress());
        dto.setAreaOfExpertise(entity.getAreaOfExpertise());
        return dto;
    }

    public static void updateEntityFromDto(TeacherDto dto, TeacherEntity entity) {
        if (dto.getId() != null) entity.setId(dto.getId());
        if (dto.getName() != null) entity.setName(dto.getName());
        if (dto.getLastName() != null) entity.setLastName(dto.getLastName());
        if (dto.getUsername() != null) entity.setUsername(dto.getUsername());
        if (dto.getPassword() != null) entity.setPassword(dto.getPassword());
        if (dto.getEmail() != null) entity.setEmail(dto.getEmail());
        if (dto.getPhone() != null) entity.setPhone(dto.getPhone());
        if (dto.getAddress() != null) entity.setAddress(dto.getAddress());
        if (dto.getAreaOfExpertise() != null) entity.setAreaOfExpertise(dto.getAreaOfExpertise());
    }

    public static TeacherEntity toEntity(TeacherDto dto) {
        TeacherEntity entity = new TeacherEntity();
        updateEntityFromDto(dto, entity);
        return entity;
    }
}
