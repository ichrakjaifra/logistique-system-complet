package com.logistique.logistique_system_complet.service;

import com.logistique.logistique_system_complet.dto.LoginRequest;
import com.logistique.logistique_system_complet.dto.LoginResponse;
import com.logistique.logistique_system_complet.model.User;
import com.logistique.logistique_system_complet.repository.UserRepository;
import com.logistique.logistique_system_complet.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public LoginResponse login(LoginRequest request) {
        Optional<User> userOpt = userRepository.findByLogin(request.getLogin());

        if (userOpt.isEmpty() || !passwordEncoder.matches(request.getPassword(), userOpt.get().getPassword())) {
            throw new RuntimeException("Login ou mot de passe incorrect");
        }

        User user = userOpt.get();

        if (!user.isActive()) {
            throw new RuntimeException("Compte désactivé");
        }

        String token = jwtUtil.generateToken(user.getLogin(), user.getRole().name(), user.getId());

        return new LoginResponse(token, user.getLogin(), user.getRole(), user.getId());
    }
}
