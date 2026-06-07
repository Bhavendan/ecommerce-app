package org.example.ecommerceapp.service;

import jakarta.servlet.http.HttpServletRequest;
import org.example.ecommerceapp.dto.LoginRequest;
import org.example.ecommerceapp.dto.UserDto;
import org.springframework.data.domain.Page;
import org.springframework.security.authentication.AuthenticationManager;

import java.util.List;

public interface UserService {

    UserDto createUser(
            UserDto dto);

    UserDto getUserById(
            Long id);

    Page<UserDto> getAllUsers(int page,int size,String sortBy,String direction);

    UserDto updateUser(
            Long id,
            UserDto dto);

    void deleteUser(Long id);
     public String verifyUser(LoginRequest request);
}