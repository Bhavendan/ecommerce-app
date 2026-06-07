package org.example.ecommerceapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.ecommerceapp.dto.UserDto;
import org.example.ecommerceapp.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@Tag(
        name = "User Management",
        description = "APIs for managing user accounts"
)
public class UserController {

    private final UserService service;

    public UserController(UserService service) {

        this.service = service;
    }

    // GET USER BY ID

    @GetMapping("/{id}")
    @Operation(
            summary = "Get user by ID",
            description = "Returns details of a registered user by ID (ADMIN only)"
    )
    public ResponseEntity<UserDto> getUserById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                service.getUserById(id)
        );
    }

    // GET ALL USERS

    @GetMapping
    @Operation(
            summary = "Get all users",
            description = "Returns paginated user records with sorting support (ADMIN only)"
    )
    public ResponseEntity<Page<UserDto>> getAllUsers(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "5")
            int size,

            @RequestParam(defaultValue = "id")
            String sortBy,

            @RequestParam(defaultValue = "asc")
            String direction) {

        return ResponseEntity.ok(
                service.getAllUsers(
                        page,
                        size,
                        sortBy,
                        direction
                )
        );
    }

    // UPDATE USER

    @PutMapping("/{id}")
    @Operation(
            summary = "Update user",
            description = "Updates an existing user's information (ADMIN only)"
    )
    public ResponseEntity<UserDto> updateUser(

            @PathVariable Long id,

            @Valid
            @RequestBody UserDto dto) {

        return ResponseEntity.ok(
                service.updateUser(id, dto)
        );
    }

    // DELETE USER

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete user",
            description = "Deletes a user account by ID (ADMIN only)"
    )
    public ResponseEntity<Void> deleteUser(
            @PathVariable Long id) {

        service.deleteUser(id);

        return ResponseEntity
                .noContent()
                .build();
    }
}