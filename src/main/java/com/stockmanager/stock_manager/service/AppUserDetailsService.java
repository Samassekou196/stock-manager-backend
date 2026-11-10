package com.stockmanager.stock_manager.service;

import com.stockmanager.stock_manager.entity.AppUser;
import com.stockmanager.stock_manager.repository.AppUserRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService implements UserDetailsService {
    private final AppUserRepository users;
    public AppUserDetailsService(AppUserRepository users) { this.users = users; }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = users.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Identifiants incorrects"));
        return User.withUsername(user.getUsername()).password(user.getPasswordHash())
                .roles(user.getRole().name()).disabled(!user.isEnabled()).build();
    }
}
