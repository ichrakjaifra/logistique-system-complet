package com.logistique.logistique_system_complet.service;

import com.logistique.logistique_system_complet.model.User;
import com.logistique.logistique_system_complet.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User admin;
    private User transporteurStandard;
    private User transporteurFragile;
    private User transporteurFrigo;

    @BeforeEach
    void setUp() {
        // Création des utilisateurs de test
        admin = User.builder()
                .id("admin1")
                .login("admin")
                .password("encodedPassword")
                .role(User.Role.ADMIN)
                .active(true)
                .build();

        transporteurStandard = User.builder()
                .id("t1")
                .login("transporteur1")
                .password("encodedPassword")
                .role(User.Role.TRANSPORTEUR)
                .active(true)
                .statut(User.StatutTransporteur.DISPONIBLE)
                .specialite(User.Specialite.STANDARD)
                .build();

        transporteurFragile = User.builder()
                .id("t2")
                .login("transporteur2")
                .password("encodedPassword")
                .role(User.Role.TRANSPORTEUR)
                .active(true)
                .statut(User.StatutTransporteur.DISPONIBLE)
                .specialite(User.Specialite.FRAGILE)
                .build();

        transporteurFrigo = User.builder()
                .id("t3")
                .login("transporteur3")
                .password("encodedPassword")
                .role(User.Role.TRANSPORTEUR)
                .active(true)
                .statut(User.StatutTransporteur.DISPONIBLE)
                .specialite(User.Specialite.FRIGO)
                .build();
    }

    @Test
    void testCreateUser_Success_Transporteur() {
        // Given
        User newTransporteur = User.builder()
                .login("newtransporteur")
                .password("password123")
                .role(User.Role.TRANSPORTEUR)
                .specialite(User.Specialite.STANDARD)
                .build();

        when(userRepository.existsByLogin("newtransporteur")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId("generated-id");
            return user;
        });

        // When
        User result = userService.createUser(newTransporteur);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertTrue(result.isActive());
        assertEquals(User.StatutTransporteur.DISPONIBLE, result.getStatut());
        assertEquals("encodedPassword", result.getPassword());

        verify(userRepository, times(1)).existsByLogin("newtransporteur");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testCreateUser_Success_Admin() {
        // Given
        User newAdmin = User.builder()
                .login("newadmin")
                .password("admin123")
                .role(User.Role.ADMIN)
                .build();

        when(userRepository.existsByLogin("newadmin")).thenReturn(false);
        when(passwordEncoder.encode("admin123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId("admin-id");
            return user;
        });

        // When
        User result = userService.createUser(newAdmin);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertTrue(result.isActive());
        assertEquals(User.Role.ADMIN, result.getRole());
        assertNull(result.getStatut()); // Admin n'a pas de statut
        assertNull(result.getSpecialite()); // Admin n'a pas de spécialité

        verify(userRepository, times(1)).existsByLogin("newadmin");
        verify(passwordEncoder, times(1)).encode("admin123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testCreateUser_LoginAlreadyExists() {
        // Given
        User newUser = User.builder()
                .login("existing")
                .password("password")
                .role(User.Role.TRANSPORTEUR)
                .build();

        when(userRepository.existsByLogin("existing")).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.createUser(newUser));

        assertTrue(exception.getMessage().contains("Login déjà utilisé"));

        verify(userRepository, times(1)).existsByLogin("existing");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testGetAllTransporteurs_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<User> transporteurs = Arrays.asList(transporteurStandard, transporteurFragile, transporteurFrigo);
        Page<User> userPage = new PageImpl<>(transporteurs, pageable, transporteurs.size());

        when(userRepository.findByRole(User.Role.TRANSPORTEUR, pageable))
                .thenReturn(userPage);

        // When
        Page<User> result = userService.getAllTransporteurs(pageable);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        assertTrue(result.getContent().stream()
                .allMatch(u -> u.getRole() == User.Role.TRANSPORTEUR));

        verify(userRepository, times(1)).findByRole(User.Role.TRANSPORTEUR, pageable);
    }

    @Test
    void testGetTransporteursBySpecialite_Success() {
        // Given
        User.Specialite specialite = User.Specialite.STANDARD;
        Pageable pageable = PageRequest.of(0, 10);
        List<User> transporteurs = Arrays.asList(transporteurStandard);
        Page<User> userPage = new PageImpl<>(transporteurs, pageable, transporteurs.size());

        when(userRepository.findByRoleAndSpecialite(User.Role.TRANSPORTEUR, specialite, pageable))
                .thenReturn(userPage);

        // When
        Page<User> result = userService.getTransporteursBySpecialite(specialite, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(specialite, result.getContent().get(0).getSpecialite());

        verify(userRepository, times(1))
                .findByRoleAndSpecialite(User.Role.TRANSPORTEUR, specialite, pageable);
    }

    @Test
    void testGetAllUsers_Success() {
        // Given
        List<User> users = Arrays.asList(admin, transporteurStandard, transporteurFragile, transporteurFrigo);

        when(userRepository.findAll()).thenReturn(users);

        // When
        List<User> result = userService.getAllUsers();

        // Then
        assertNotNull(result);
        assertEquals(4, result.size());

        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testUpdateUser_Success_Transporteur() {
        // Given
        String userId = "t1";
        User userDetails = User.builder()
                .active(false)
                .statut(User.StatutTransporteur.EN_LIVRAISON)
                .specialite(User.Specialite.FRAGILE)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(transporteurStandard));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User result = userService.updateUser(userId, userDetails);

        // Then
        assertNotNull(result);
        assertFalse(result.isActive());
        assertEquals(User.StatutTransporteur.EN_LIVRAISON, result.getStatut());
        assertEquals(User.Specialite.FRAGILE, result.getSpecialite());

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testUpdateUser_Success_Admin() {
        // Given
        String userId = "admin1";
        User userDetails = User.builder()
                .active(false)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(admin));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        User result = userService.updateUser(userId, userDetails);

        // Then
        assertNotNull(result);
        assertFalse(result.isActive());
        assertNull(result.getStatut()); // Admin n'a pas de statut
        assertNull(result.getSpecialite()); // Admin n'a pas de spécialité

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testUpdateUser_UserNotFound() {
        // Given
        String userId = "non-existent";
        User userDetails = User.builder().build();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.updateUser(userId, userDetails));

        assertTrue(exception.getMessage().contains("Utilisateur non trouvé"));

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testGetUserById_Success() {
        // Given
        String userId = "t1";

        when(userRepository.findById(userId)).thenReturn(Optional.of(transporteurStandard));

        // When
        Optional<User> result = userService.getUserById(userId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getId());
        assertEquals("transporteur1", result.get().getLogin());

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void testGetUserById_NotFound() {
        // Given
        String userId = "non-existent";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.getUserById(userId);

        // Then
        assertFalse(result.isPresent());

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void testDeleteUser_Success() {
        // Given
        String userId = "t1";

        doNothing().when(userRepository).deleteById(userId);

        // When
        userService.deleteUser(userId);

        // Then
        verify(userRepository, times(1)).deleteById(userId);
    }

    @Test
    void testCreateUser_TransporteurWithoutStatutAutoSet() {
        // Given
        User newTransporteur = User.builder()
                .login("testtrans")
                .password("testpass")
                .role(User.Role.TRANSPORTEUR)
                .specialite(User.Specialite.STANDARD)
                .build();

        when(userRepository.existsByLogin("testtrans")).thenReturn(false);
        when(passwordEncoder.encode("testpass")).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId("test-id");
            return user;
        });

        // When
        User result = userService.createUser(newTransporteur);

        // Then
        assertNotNull(result);
        assertEquals(User.StatutTransporteur.DISPONIBLE, result.getStatut()); // Statut auto-défini

        verify(userRepository, times(1)).existsByLogin("testtrans");
        verify(passwordEncoder, times(1)).encode("testpass");
        verify(userRepository, times(1)).save(any(User.class));
    }
}