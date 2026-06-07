package org.example.ecommerceapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.example.ecommerceapp.dto.LoginRequest;
import org.example.ecommerceapp.dto.UserDto;
import org.example.ecommerceapp.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public")
@Tag(
        name = "Authentication",
        description = "APIs for user registration and JWT authentication"
)
public class AuthController {

    private final UserService service;

    public AuthController(UserService service) {
        this.service = service;
    }

    // REGISTER USER

    @PostMapping("/register")
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account and returns the created user details"
    )
    public ResponseEntity<UserDto> createUser(
            @Valid
            @RequestBody UserDto dto) {

        UserDto user =
                service.createUser(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(user);
    }

    // LOGIN USER

    @PostMapping("/login")
    @Operation(
            summary = "Authenticate user",
            description = "Validates email and password and returns a JWT token"
    )
    public ResponseEntity<String> login(
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        String token =
                service.verifyUser(request);

        return ResponseEntity.ok(token);
    }
}