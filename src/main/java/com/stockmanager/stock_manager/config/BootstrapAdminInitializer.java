package com.stockmanager.stock_manager.config;

import com.stockmanager.stock_manager.entity.AppUser;
import com.stockmanager.stock_manager.entity.Role;
import com.stockmanager.stock_manager.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;

@Component
public class BootstrapAdminInitializer implements ApplicationRunner {
    private final AppUserRepository users;
    private final PasswordEncoder encoder;
    private final String username, password, name;

    public BootstrapAdminInitializer(AppUserRepository users, PasswordEncoder encoder,
            @Value("${app.bootstrap.admin.username:}") String username,
            @Value("${app.bootstrap.admin.password:}") String password,
            @Value("${app.bootstrap.admin.name:Administrateur}") String name) {
        this.users = users; this.encoder = encoder; this.username = username;
        this.password = password; this.name = name;
    }

    @Override public void run(ApplicationArguments args) {
        if (username.isBlank() && password.isBlank()) return;
        if (username.isBlank() || password.isBlank()) {
            throw new IllegalStateException("Les deux variables APP_BOOTSTRAP_ADMIN_USERNAME et APP_BOOTSTRAP_ADMIN_PASSWORD sont requises.");
        }
        if (password.length() < 12 || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalStateException("Le mot de passe administrateur initial doit contenir entre 12 et 72 caractères.");
        }
        if (!users.existsByUsername(username.trim())) {
            users.save(new AppUser(username.trim(), encoder.encode(password), name, Role.ADMIN));
        }
    }
}
