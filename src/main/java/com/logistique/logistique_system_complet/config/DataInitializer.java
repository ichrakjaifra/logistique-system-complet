package com.logistique.logistique_system_complet.config;

import com.logistique.logistique_system_complet.model.User;
import com.logistique.logistique_system_complet.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.findByLogin("admin").isEmpty()) {
            User admin = User.builder()
                    .login("admin")
                    .password(passwordEncoder.encode("admin123"))
                    .role(User.Role.ADMIN)
                    .active(true)
                    .build();
            userRepository.save(admin);
            System.out.println("Admin créé: login=admin, password=admin123");
        }

        String[] transporteurs = {
                "transporteur1:trans123:STANDARD",
                "transporteur2:trans123:FRAGILE",
                "transporteur3:trans123:FRIGO"
        };

        for (String trans : transporteurs) {
            String[] parts = trans.split(":");
            String login = parts[0];

            if (userRepository.findByLogin(login).isEmpty()) {
                User transporteur = User.builder()
                        .login(login)
                        .password(passwordEncoder.encode(parts[1]))
                        .role(User.Role.TRANSPORTEUR)
                        .active(true)
                        .statut(User.StatutTransporteur.DISPONIBLE)
                        .specialite(User.Specialite.valueOf(parts[2]))
                        .build();
                userRepository.save(transporteur);
                System.out.println("Transporteur créé: " + login);
            }
        }
        System.out.println("Données d'initialisation créées!");
    }
}
