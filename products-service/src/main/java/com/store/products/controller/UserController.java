package com.store.products.controller;

import com.store.products.dto.ChangePasswordDTO;
import com.store.products.dto.ResponseWrapper;
import com.store.products.dto.UserResponseDTO;
import com.store.products.dto.UserUpdateDTO;
import com.store.products.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import org.springframework.lang.NonNull;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<ResponseWrapper<List<UserResponseDTO>>> listUsers() {
        return ResponseEntity.ok(new ResponseWrapper<>(userService.listAllUsers()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ResponseWrapper<UserResponseDTO>> updateUser(
            @PathVariable @NonNull UUID id,
            @Valid @RequestBody UserUpdateDTO updateDTO) {
        return ResponseEntity.ok(new ResponseWrapper<>(userService.updateUser(id, updateDTO)));
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<ResponseWrapper<String>> changePassword(
            @PathVariable @NonNull UUID id,
            @Valid @RequestBody ChangePasswordDTO passwordDTO) {
        userService.changePassword(id, passwordDTO);
        return ResponseEntity.ok(new ResponseWrapper<>("Password updated successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable @NonNull UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
