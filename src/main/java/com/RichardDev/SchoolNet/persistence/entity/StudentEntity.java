package com.RichardDev.SchoolNet.persistence.entity;

import com.RichardDev.SchoolNet.constant.Grade;
import com.RichardDev.SchoolNet.constant.Rol;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "student")
@Getter
@Setter
@AllArgsConstructor
public class StudentEntity extends UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El campo 'grade' no puede ser nulo")
    @Enumerated(EnumType.STRING)
    private Grade grade;


    public StudentEntity() {
        super.setRol(Rol.STUDENT);
    }

}
