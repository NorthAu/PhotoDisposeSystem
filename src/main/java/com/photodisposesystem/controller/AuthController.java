package com.photodisposesystem.controller;

import com.photodisposesystem.dto.AuthRequest;
import com.photodisposesystem.dto.AuthResponse;
import com.photodisposesystem.model.User;
import com.photodisposesystem.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody AuthRequest request) {
        User user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new AuthResponse(user.getId(), user.getUsername(), user.getRole(), "Registration successful"));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        User user = authService.login(request);
        return ResponseEntity.ok(new AuthResponse(user.getId(), user.getUsername(), user.getRole(), "Login successful"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<AuthResponse> handleAuthError(IllegalArgumentException ex) {
        return ResponseEntity.badRequest()
                .body(new AuthResponse(null, null, null, ex.getMessage()));
    }
}
