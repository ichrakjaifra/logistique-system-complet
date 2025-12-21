package com.logistique.logistique_system_complet.repository;

import com.logistique.logistique_system_complet.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByLogin(String login);
    List<User> findByRole(User.Role role);
    List<User> findByRoleAndSpecialite(User.Role role, User.Specialite specialite);
    Page<User> findByRole(User.Role role, Pageable pageable);
    Page<User> findByRoleAndSpecialite(User.Role role, User.Specialite specialite, Pageable pageable);
    boolean existsByLogin(String login);
    List<User> findByActiveTrue();
}
