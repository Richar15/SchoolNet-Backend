package com.RichardDev.SchoolNet.persistence.entity;

import com.RichardDev.SchoolNet.constant.Rol;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "admin")
@Getter
@Setter
public class AdminEntity extends UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public AdminEntity() {
        super.setRol(Rol.ADMIN);
    }

}

