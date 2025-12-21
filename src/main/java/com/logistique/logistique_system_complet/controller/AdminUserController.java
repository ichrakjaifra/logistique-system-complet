package com.logistique.logistique_system_complet.controller;

import com.logistique.logistique_system_complet.dto.UserRequest;
import com.logistique.logistique_system_complet.dto.UserResponse;
import com.logistique.logistique_system_complet.mapper.UserMapper;
import com.logistique.logistique_system_complet.model.User;
import com.logistique.logistique_system_complet.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
@Tag(name = "Admin - Gestion des Utilisateurs", description = "API de gestion des utilisateurs pour les administrateurs")
public class AdminUserController {

    private final UserService userService;
    private final UserMapper userMapper;

    public AdminUserController(UserService userService, UserMapper userMapper) {
        this.userService = userService;
        this.userMapper = userMapper;
    }

    @GetMapping("/users")
    @Operation(summary = "Lister tous les utilisateurs")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<UserResponse> responses = users.stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/transporteurs")
    @Operation(summary = "Lister les transporteurs avec pagination")
    public ResponseEntity<Page<UserResponse>> getAllTransporteurs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> transporteursPage = userService.getAllTransporteurs(pageable);
        Page<UserResponse> responsePage = transporteursPage.map(userMapper::toResponse);
        return ResponseEntity.ok(responsePage);
    }

    @GetMapping("/transporteurs/filter")
    @Operation(summary = "Filtrer les transporteurs par spécialité")
    public ResponseEntity<Page<UserResponse>> getTransporteursBySpecialite(
            @RequestParam User.Specialite specialite,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<User> transporteursPage = userService.getTransporteursBySpecialite(specialite, pageable);
        Page<UserResponse> responsePage = transporteursPage.map(userMapper::toResponse);
        return ResponseEntity.ok(responsePage);
    }

    @PostMapping("/transporteurs")
    @Operation(summary = "Créer un nouveau transporteur")
    public ResponseEntity<UserResponse> createTransporteur(@Valid @RequestBody UserRequest request) {
        User user = userMapper.toEntity(request);
        user.setRole(User.Role.TRANSPORTEUR);
        User savedUser = userService.createUser(user);
        UserResponse response = userMapper.toResponse(savedUser);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admins")
    @Operation(summary = "Créer un nouvel administrateur")
    public ResponseEntity<UserResponse> createAdmin(@Valid @RequestBody UserRequest request) {
        User user = userMapper.toEntity(request);
        user.setRole(User.Role.ADMIN);
        User savedUser = userService.createUser(user);
        UserResponse response = userMapper.toResponse(savedUser);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/transporteurs/{id}")
    @Operation(summary = "Modifier un transporteur")
    public ResponseEntity<UserResponse> updateTransporteur(@PathVariable String id, @RequestBody UserRequest request) {
        User userDetails = userMapper.toEntity(request);
        User updatedUser = userService.updateUser(id, userDetails);
        UserResponse response = userMapper.toResponse(updatedUser);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/transporteurs/{id}")
    @Operation(summary = "Supprimer un transporteur")
    public ResponseEntity<Void> deleteTransporteur(@PathVariable String id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/users/{id}/activate")
    @Operation(summary = "Activer/Désactiver un utilisateur")
    public ResponseEntity<UserResponse> toggleUserActive(@PathVariable String id, @RequestParam boolean active) {
        User user = new User();
        user.setActive(active);
        User updatedUser = userService.updateUser(id, user);
        UserResponse response = userMapper.toResponse(updatedUser);
        return ResponseEntity.ok(response);
    }
}