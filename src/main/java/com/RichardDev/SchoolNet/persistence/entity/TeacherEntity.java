package com.RichardDev.SchoolNet.persistence.entity;

import com.RichardDev.SchoolNet.constant.Rol;
import com.RichardDev.SchoolNet.constant.Subject;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;



@Entity
@Table(name = "teachers")
@Getter
@Setter
public class TeacherEntity extends UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El campo 'areaOfExpertise' no puede ser nulo")
    private Subject areaOfExpertise;


    public TeacherEntity() {
        super.setRol(Rol.TEACHER);
    }
}
