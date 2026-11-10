package com.stockmanager.stock_manager.controller;

import com.stockmanager.stock_manager.entity.AppUser;
import com.stockmanager.stock_manager.entity.Role;
import com.stockmanager.stock_manager.repository.AppUserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    public UserController(AppUserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateUserRequest request) {
        String username = request.username().trim();
        if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
            return ResponseEntity.badRequest().body(Map.of("message", "Le mot de passe est trop long."));
        }
        if (users.existsByUsername(username)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Ce nom d'utilisateur est déjà utilisé."));
        }
        AppUser saved = users.save(new AppUser(username, passwordEncoder.encode(request.password()),
                request.name().trim(), request.role()));
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "username", saved.getUsername(), "name", saved.getName(), "role", saved.getRole().name()));
    }

    public record CreateUserRequest(@NotBlank @Size(max = 80) String username,
            @NotBlank @Size(min = 12, max = 72) String password,
            @NotBlank @Size(max = 120) String name, @NotNull Role role) {}
}
