package com.logistique.logistique_system_complet.service;

import com.logistique.logistique_system_complet.model.User;
import com.logistique.logistique_system_complet.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(User user) {
        if (userRepository.existsByLogin(user.getLogin())) {
            throw new RuntimeException("Login déjà utilisé");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setActive(true);

        if (user.getRole() == User.Role.TRANSPORTEUR) {
            user.setStatut(User.StatutTransporteur.DISPONIBLE);
        }

        return userRepository.save(user);
    }

    public Page<User> getAllTransporteurs(Pageable pageable) {
        return userRepository.findByRole(User.Role.TRANSPORTEUR, pageable);
    }

    public Page<User> getTransporteursBySpecialite(User.Specialite specialite, Pageable pageable) {
        return userRepository.findByRoleAndSpecialite(User.Role.TRANSPORTEUR, specialite, pageable);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }

    public User updateUser(String id, User userDetails) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        user.setActive(userDetails.isActive());

        if (user.getRole() == User.Role.TRANSPORTEUR) {
            if (userDetails.getStatut() != null) {
                user.setStatut(userDetails.getStatut());
            }
            if (userDetails.getSpecialite() != null) {
                user.setSpecialite(userDetails.getSpecialite());
            }
        }

        return userRepository.save(user);
    }

    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }
}
