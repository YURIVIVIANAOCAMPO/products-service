package com.store.products.service;

import com.store.products.dto.ChangePasswordDTO;
import com.store.products.dto.UserResponseDTO;
import com.store.products.dto.UserUpdateDTO;
import com.store.products.entity.User;
import com.store.products.exception.DuplicateResourceException;
import com.store.products.exception.ResourceNotFoundException;
import com.store.products.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserResponseDTO> listAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }


    @Transactional
    public UserResponseDTO updateUser(@NonNull UUID id, UserUpdateDTO updateDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));

        if (!user.getUsername().equals(updateDTO.getUsername()) && userRepository.findByUsername(updateDTO.getUsername()).isPresent()) {
            throw new DuplicateResourceException("Username already exists");
        }

        user.setUsername(updateDTO.getUsername());
        user.setRole(updateDTO.getRole());

        return mapToDTO(userRepository.save(user));
    }

    @Transactional
    public void changePassword(@NonNull UUID id, ChangePasswordDTO passwordDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));

        user.setPassword(passwordEncoder.encode(passwordDTO.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(@NonNull UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found: " + id);
        }
        userRepository.deleteById(id);
    }

    private UserResponseDTO mapToDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
