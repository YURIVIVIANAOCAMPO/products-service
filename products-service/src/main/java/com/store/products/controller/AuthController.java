package com.store.products.controller;



import com.store.products.dto.LoginRequestDTO;
import com.store.products.dto.RegisterRequestDTO;
import com.store.products.dto.ResponseWrapper;
import com.store.products.entity.User;
import com.store.products.exception.DuplicateResourceException;
import com.store.products.repository.UserRepository;
import com.store.products.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<ResponseWrapper<Map<String, String>>> login(@Valid @RequestBody LoginRequestDTO request) {
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        String token = jwtUtil.generateToken(authentication.getName());
        return ResponseEntity.ok(new ResponseWrapper<>(Map.of("token", token)));
    }

    @PostMapping("/register")
    public ResponseEntity<ResponseWrapper<Map<String, String>>> register(@Valid @RequestBody RegisterRequestDTO request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new DuplicateResourceException("Username already exists");
        }

        User newUser = java.util.Objects.requireNonNull(User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("ROLE_USER")
                .build(), "User cannot be null");
        
        userRepository.save(newUser);

        return new ResponseEntity<>(new ResponseWrapper<>(Map.of("message", "User created successfully")), HttpStatus.CREATED);
    }
}
