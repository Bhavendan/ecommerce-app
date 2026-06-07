package org.example.ecommerceapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDto {

    private Long id;

    @NotBlank(message = "Name required")
    private String name;

    @Email(message = "Invalid email")
    private String email;

    // NEVER expose password in response
    private String password;

    private String role;

    public UserDto() {
    }

    public UserDto(Long id,
                   String name,
                   String email,
                   String role) {

        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    // getters setters
}