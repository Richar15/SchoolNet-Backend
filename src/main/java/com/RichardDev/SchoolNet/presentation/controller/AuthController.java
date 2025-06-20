package com.RichardDev.SchoolNet.presentation.controller;


import com.RichardDev.SchoolNet.presentation.dto.AuthResponseDTO;
import com.RichardDev.SchoolNet.presentation.dto.LoginRequestDTO;
import com.RichardDev.SchoolNet.service.exeption.CredencialesInvalidasException;
import com.RichardDev.SchoolNet.service.implementation.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

 @PostMapping("/login")
 public ResponseEntity<?> login(@RequestBody LoginRequestDTO request) {
     try {
         AuthResponseDTO response = authService.login(request);
         return ResponseEntity.ok(response);
     } catch (CredencialesInvalidasException e) {
         return ResponseEntity.status(401).body(
             Map.of("error", "Usuario o contraseña incorrectos")
         );
     }
 }

}
