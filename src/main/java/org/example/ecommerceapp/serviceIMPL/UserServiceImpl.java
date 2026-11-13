package org.example.ecommerceapp.serviceIMPL;

import org.example.ecommerceapp.dto.LoginRequest;
import org.example.ecommerceapp.dto.UserDto;
import org.example.ecommerceapp.entity.User;
import org.example.ecommerceapp.exception.ResourceNotFoundException;
import org.example.ecommerceapp.repository.UserRepository;
import org.example.ecommerceapp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;

    public UserServiceImpl(UserRepository repo, PasswordEncoder encoder, AuthenticationManager authenticationManager, JWTService jwtService) {

        this.repo = repo;
        this.encoder=encoder;
        this.authenticationManager=authenticationManager;
        this.jwtService = jwtService;
    }

    @Override
    public UserDto createUser(UserDto dto) {

        User user = dtoToEntity(dto);
        user.setPassword(encoder.encode(user.getPassword()));

        User saved = repo.save(user);

        return entityToDto(saved);
    }

    @Override
    public UserDto getUserById(Long id) {

        User user = repo.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return entityToDto(user);
    }

    @Override
    public Page<UserDto> getAllUsers(int page,int size,String sortBy,String direction) {
        Sort sort=direction.equalsIgnoreCase("desc")?Sort.by(sortBy).descending():Sort.by(sortBy).ascending();
        Pageable pageable= PageRequest.of(page,size,sort);
        return repo.findAll(pageable).map(this::entityToDto);
    }


    @Override
    public UserDto updateUser(Long id, UserDto dto) {

        User user = repo.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        user.setName(dto.getName());

        user.setEmail(dto.getEmail());

        // Update password only if a new password is provided
        if (dto.getPassword() != null &&
                !dto.getPassword().isBlank()) {

            user.setPassword(encoder.encode(dto.getPassword()));
        }

        // Role changes are allowed through the ADMIN management API
        user.setRole(dto.getRole());

        User updated = repo.save(user);

        return entityToDto(updated);
    }


    @Override
    public void deleteUser(Long id) {

        repo.deleteById(id);
    }

    // =====================
    // DTO Mapping
    // =====================

    private UserDto entityToDto(User user) {

        return new UserDto(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }

    private User dtoToEntity(UserDto dto) {

        User user = new User();

        user.setName(dto.getName());

        user.setEmail(dto.getEmail());

        user.setPassword(dto.getPassword());

        user.setRole(dto.getRole());

        return user;
    }
    public String verifyUser(LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate
                (new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        return jwtService.generateToken(request);
    }
}