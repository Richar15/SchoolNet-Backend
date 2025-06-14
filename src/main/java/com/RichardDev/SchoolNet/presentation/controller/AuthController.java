package com.RichardDev.SchoolNet.presentation.controller;


import com.RichardDev.SchoolNet.presentation.dto.AuthResponseDTO;
import com.RichardDev.SchoolNet.presentation.dto.LoginRequestDTO;
import com.RichardDev.SchoolNet.service.implementation.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

   @PostMapping("/login")
   public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequestDTO request) {
       AuthResponseDTO response = authService.login(request);

       if (response.isError()) {
           return ResponseEntity.status(response.getStatus()).body(response);
       }

       return ResponseEntity.ok(response);
   }
}
