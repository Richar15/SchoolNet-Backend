package com.RichardDev.SchoolNet.persistence.entity;

import com.RichardDev.SchoolNet.constant.Rol;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El nombre no puede ser nulo")
    @NotBlank(message = "El nombre no puede estar vacío")
    @Size(min = 4, max = 50, message = "El nombre debe tener entre 4 y 50 caracteres")
    @Column(unique = true)
    protected String name;

    @NotBlank(message = "El apellido no puede estar vacío")
    @NotNull(message = "El apellido no puede ser nulo")
    @Size(min = 4, max = 50, message = "El apellido debe tener entre 4 y 50 caracteres")
    @Column(unique = false)
    protected String lastName;

    @NotBlank(message = "El nombre de usuario no puede estar vacío")
    @NotNull(message = "El nombre de usuario no puede ser nulo")
    @Size(min = 4, max = 50, message = "El nombre de usuario debe tener entre 4 y 50 caracteres")
    @Column(unique = true)
    protected String username;

    @NotBlank(message = "La contraseña no puede estar vacía")
    @NotNull(message = "La contraseña no puede ser nula")
    @Size(min = 8, message = "La contraseña debe tener minimo 8 caracteres")
    @Column(unique = true)
    protected String password;

    @Email(message = "El correo electrónico debe ser válido")
    @NotBlank(message = "El correo electrónico no puede estar vacío")
    @NotNull(message = "El correo electrónico no puede ser nulo")
    @Column(unique = true)
    protected String email;

    @Size(max = 10, message = "El número de teléfono debe tener 10 dígitos")
    @NotBlank(message = "El número de teléfono no puede estar vacío")
    @NotNull(message = "El número de teléfono no puede ser nulo")
    @Column(unique = true)
    protected String phone;

    @Size(min = 8, max = 20, message = "La dirección debe tener entre 8 y 20 caracteres")
    @NotBlank(message = "La dirección no puede estar vacía")
    @NotNull(message = "La dirección no puede ser nula")
    protected String address;

    @Enumerated(EnumType.STRING)
    protected Rol rol;
}
