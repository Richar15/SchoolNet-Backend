package com.RichardDev.SchoolNet.presentation.dto;

import com.RichardDev.SchoolNet.constant.Grade;
import lombok.Data;

@Data
public class StudentDTO {

    private String name;
    private String lastName;
    private String username;
    private String password;
    private String email;
    private String phone;
    private String address;
    private Grade grade;
}
