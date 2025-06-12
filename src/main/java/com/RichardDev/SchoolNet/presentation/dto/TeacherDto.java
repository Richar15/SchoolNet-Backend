package com.RichardDev.SchoolNet.presentation.dto;

import com.RichardDev.SchoolNet.constant.Subject;
import lombok.Data;

@Data
public class TeacherDto {
    private String name;
    private String lastName;
    private String username;
    private String password;
    private String email;
    private String phone;
    private String address;
    private Subject areaOfExpertise;
}
