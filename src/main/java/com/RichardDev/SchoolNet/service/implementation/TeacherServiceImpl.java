package com.RichardDev.SchoolNet.service.implementation;


import com.RichardDev.SchoolNet.persistence.entity.TeacherEntity;
import com.RichardDev.SchoolNet.persistence.repository.TeacherRepository;
import com.RichardDev.SchoolNet.presentation.dto.TeacherDto;
import com.RichardDev.SchoolNet.service.exeption.UniqueFieldException;
import com.RichardDev.SchoolNet.service.exeption.UserNotFoundException;
import com.RichardDev.SchoolNet.service.interfaces.TeacherService;
import com.RichardDev.SchoolNet.util.mapper.TeacherMapper;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class TeacherServiceImpl implements TeacherService {

    private final TeacherRepository teacherRepository;

    @Override
    public TeacherDto create(TeacherDto dto) {
        try {
            TeacherEntity entity = TeacherMapper.toEntity(dto);
            return TeacherMapper.toDto(teacherRepository.save(entity));
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new UniqueFieldException("Ya existe un profesor con el mismo nombre, username, email, teléfono o apellido.");
        }
    }
    @Override
    public TeacherDto update(Long id, TeacherDto dto) {
        TeacherEntity entity = teacherRepository.findById(id).orElse(null);
        if (entity == null) {
            throw new UserNotFoundException("Profesor no encontrado con id: " + id);
        }
        TeacherMapper.updateEntityFromDto(dto, entity);
        return TeacherMapper.toDto(teacherRepository.save(entity));
    }

    @Override
    public List<TeacherDto> getAll() {
        List<TeacherEntity> teachers = teacherRepository.findAll();
        if (teachers.isEmpty()) {
            throw new UserNotFoundException("No se encontraron profesores registrados.");
        }
        return teachers.stream()
                .map(TeacherMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TeacherDto> searchByKeyword(String keyword) {
        List<TeacherEntity> teachers = teacherRepository.searchByKeyword(keyword);
        if (teachers.isEmpty()) {
            throw new UserNotFoundException("No se encontraron profesores que coincidan con: " + keyword);
        }
        return teachers.stream()
                .map(TeacherMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        teacherRepository.deleteById(id);
    }

}
