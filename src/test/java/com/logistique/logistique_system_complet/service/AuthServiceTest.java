package com.logistique.logistique_system_complet.service;

import com.logistique.logistique_system_complet.dto.LoginRequest;
import com.logistique.logistique_system_complet.dto.LoginResponse;
import com.logistique.logistique_system_complet.model.User;
import com.logistique.logistique_system_complet.repository.UserRepository;
import com.logistique.logistique_system_complet.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User activeUser;
    private User inactiveUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        // Création des utilisateurs de test
        activeUser = User.builder()
                .id("user1")
                .login("testuser")
                .password("encodedPassword")
                .role(User.Role.TRANSPORTEUR)
                .active(true)
                .build();

        inactiveUser = User.builder()
                .id("user2")
                .login("inactiveuser")
                .password("encodedPassword")
                .role(User.Role.TRANSPORTEUR)
                .active(false)
                .build();

        adminUser = User.builder()
                .id("admin1")
                .login("admin")
                .password("encodedAdminPassword")
                .role(User.Role.ADMIN)
                .active(true)
                .build();
    }

    @Test
    void testLogin_Success_Transporteur() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setLogin("testuser");
        request.setPassword("password123");

        String expectedToken = "jwt.token.here";

        when(userRepository.findByLogin("testuser")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken("testuser", "TRANSPORTEUR", "user1")).thenReturn(expectedToken);

        // When
        LoginResponse result = authService.login(request);

        // Then
        assertNotNull(result);
        assertEquals(expectedToken, result.getToken());
        assertEquals("testuser", result.getLogin());
        assertEquals(User.Role.TRANSPORTEUR, result.getRole());
        assertEquals("user1", result.getUserId());
        assertEquals("Connexion réussie", result.getMessage());

        verify(userRepository, times(1)).findByLogin("testuser");
        verify(passwordEncoder, times(1)).matches("password123", "encodedPassword");
        verify(jwtUtil, times(1)).generateToken("testuser", "TRANSPORTEUR", "user1");
    }

    @Test
    void testLogin_Success_Admin() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setLogin("admin");
        request.setPassword("admin123");

        String expectedToken = "admin.jwt.token";

        when(userRepository.findByLogin("admin")).thenReturn(Optional.of(adminUser));
        when(passwordEncoder.matches("admin123", "encodedAdminPassword")).thenReturn(true);
        when(jwtUtil.generateToken("admin", "ADMIN", "admin1")).thenReturn(expectedToken);

        // When
        LoginResponse result = authService.login(request);

        // Then
        assertNotNull(result);
        assertEquals(expectedToken, result.getToken());
        assertEquals("admin", result.getLogin());
        assertEquals(User.Role.ADMIN, result.getRole());
        assertEquals("admin1", result.getUserId());
        assertEquals("Connexion réussie", result.getMessage());

        verify(userRepository, times(1)).findByLogin("admin");
        verify(passwordEncoder, times(1)).matches("admin123", "encodedAdminPassword");
        verify(jwtUtil, times(1)).generateToken("admin", "ADMIN", "admin1");
    }

    @Test
    void testLogin_UserNotFound() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setLogin("wronguser");
        request.setPassword("wrongpass");

        when(userRepository.findByLogin("wronguser")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.login(request));

        assertEquals("Login ou mot de passe incorrect", exception.getMessage());

        verify(userRepository, times(1)).findByLogin("wronguser");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyString(), anyString(), anyString());
    }

    @Test
    void testLogin_InvalidPassword() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setLogin("testuser");
        request.setPassword("wrongpass");

        when(userRepository.findByLogin("testuser")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("wrongpass", "encodedPassword")).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.login(request));

        assertEquals("Login ou mot de passe incorrect", exception.getMessage());

        verify(userRepository, times(1)).findByLogin("testuser");
        verify(passwordEncoder, times(1)).matches("wrongpass", "encodedPassword");
        verify(jwtUtil, never()).generateToken(anyString(), anyString(), anyString());
    }

    @Test
    void testLogin_InactiveUser() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setLogin("inactiveuser");
        request.setPassword("password123");

        when(userRepository.findByLogin("inactiveuser")).thenReturn(Optional.of(inactiveUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.login(request));

        assertEquals("Compte désactivé", exception.getMessage());

        verify(userRepository, times(1)).findByLogin("inactiveuser");
        verify(passwordEncoder, times(1)).matches("password123", "encodedPassword");
        verify(jwtUtil, never()).generateToken(anyString(), anyString(), anyString());
    }

    @Test
    void testLogin_TransporteurInactive() {
        // Given
        User transporteurInactive = User.builder()
                .id("t99")
                .login("transinactive")
                .password("encodedPass")
                .role(User.Role.TRANSPORTEUR)
                .active(false)
                .statut(User.StatutTransporteur.DISPONIBLE)
                .specialite(User.Specialite.STANDARD)
                .build();

        LoginRequest request = new LoginRequest();
        request.setLogin("transinactive");
        request.setPassword("password123");

        when(userRepository.findByLogin("transinactive")).thenReturn(Optional.of(transporteurInactive));
        when(passwordEncoder.matches("password123", "encodedPass")).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.login(request));

        assertEquals("Compte désactivé", exception.getMessage());

        verify(userRepository, times(1)).findByLogin("transinactive");
        verify(passwordEncoder, times(1)).matches("password123", "encodedPass");
        verify(jwtUtil, never()).generateToken(anyString(), anyString(), anyString());
    }

    @Test
    void testLogin_EmptyLogin() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setLogin("");
        request.setPassword("password123");

        when(userRepository.findByLogin("")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.login(request));

        assertEquals("Login ou mot de passe incorrect", exception.getMessage());

        verify(userRepository, times(1)).findByLogin("");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyString(), anyString(), anyString());
    }

    @Test
    void testLogin_NullLogin() {
        // Given
        LoginRequest request = new LoginRequest();
        request.setLogin(null);
        request.setPassword("password123");

        when(userRepository.findByLogin(null)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> authService.login(request));

        assertEquals("Login ou mot de passe incorrect", exception.getMessage());

        verify(userRepository, times(1)).findByLogin(null);
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(jwtUtil, never()).generateToken(anyString(), anyString(), anyString());
    }

    @Test
    void testLogin_TransporteurWithStatut() {
        // Given
        User transporteurEnLivraison = User.builder()
                .id("t50")
                .login("translivraison")
                .password("encodedPass")
                .role(User.Role.TRANSPORTEUR)
                .active(true)
                .statut(User.StatutTransporteur.EN_LIVRAISON)
                .specialite(User.Specialite.FRAGILE)
                .build();

        String expectedToken = "transporteur.token";
        LoginRequest request = new LoginRequest();
        request.setLogin("translivraison");
        request.setPassword("password123");

        when(userRepository.findByLogin("translivraison")).thenReturn(Optional.of(transporteurEnLivraison));
        when(passwordEncoder.matches("password123", "encodedPass")).thenReturn(true);
        when(jwtUtil.generateToken("translivraison", "TRANSPORTEUR", "t50")).thenReturn(expectedToken);

        // When
        LoginResponse result = authService.login(request);

        // Then
        assertNotNull(result);
        assertEquals(expectedToken, result.getToken());
        assertEquals("translivraison", result.getLogin());
        assertEquals(User.Role.TRANSPORTEUR, result.getRole());
        assertEquals("t50", result.getUserId());
        assertEquals("Connexion réussie", result.getMessage());

        verify(userRepository, times(1)).findByLogin("translivraison");
        verify(passwordEncoder, times(1)).matches("password123", "encodedPass");
        verify(jwtUtil, times(1)).generateToken("translivraison", "TRANSPORTEUR", "t50");
    }

    @Test
    void testLogin_AdminWithoutSpecialite() {
        // Given
        User adminWithoutFields = User.builder()
                .id("admin2")
                .login("admin2")
                .password("adminPass")
                .role(User.Role.ADMIN)
                .active(true)
                .build();

        String expectedToken = "admin2.token";
        LoginRequest request = new LoginRequest();
        request.setLogin("admin2");
        request.setPassword("admin123");

        when(userRepository.findByLogin("admin2")).thenReturn(Optional.of(adminWithoutFields));
        when(passwordEncoder.matches("admin123", "adminPass")).thenReturn(true);
        when(jwtUtil.generateToken("admin2", "ADMIN", "admin2")).thenReturn(expectedToken);

        // When
        LoginResponse result = authService.login(request);

        // Then
        assertNotNull(result);
        assertEquals(expectedToken, result.getToken());
        assertEquals("admin2", result.getLogin());
        assertEquals(User.Role.ADMIN, result.getRole());
        assertEquals("admin2", result.getUserId());
        assertEquals("Connexion réussie", result.getMessage());

        verify(userRepository, times(1)).findByLogin("admin2");
        verify(passwordEncoder, times(1)).matches("admin123", "adminPass");
        verify(jwtUtil, times(1)).generateToken("admin2", "ADMIN", "admin2");
    }

    @Test
    void testLogin_TransporteurFrigo() {
        // Given
        User transporteurFrigo = User.builder()
                .id("t100")
                .login("transfrigo")
                .password("frigoPass")
                .role(User.Role.TRANSPORTEUR)
                .active(true)
                .statut(User.StatutTransporteur.DISPONIBLE)
                .specialite(User.Specialite.FRIGO)
                .build();

        String expectedToken = "frigo.token";
        LoginRequest request = new LoginRequest();
        request.setLogin("transfrigo");
        request.setPassword("frigo123");

        when(userRepository.findByLogin("transfrigo")).thenReturn(Optional.of(transporteurFrigo));
        when(passwordEncoder.matches("frigo123", "frigoPass")).thenReturn(true);
        when(jwtUtil.generateToken("transfrigo", "TRANSPORTEUR", "t100")).thenReturn(expectedToken);

        // When
        LoginResponse result = authService.login(request);

        // Then
        assertNotNull(result);
        assertEquals(expectedToken, result.getToken());
        assertEquals("transfrigo", result.getLogin());
        assertEquals(User.Role.TRANSPORTEUR, result.getRole());
        assertEquals("t100", result.getUserId());
        assertEquals("Connexion réussie", result.getMessage());

        verify(userRepository, times(1)).findByLogin("transfrigo");
        verify(passwordEncoder, times(1)).matches("frigo123", "frigoPass");
        verify(jwtUtil, times(1)).generateToken("transfrigo", "TRANSPORTEUR", "t100");
    }

    @Test
    void testLogin_TransporteurFragile() {
        // Given
        User transporteurFragile = User.builder()
                .id("t200")
                .login("transfragile")
                .password("fragilePass")
                .role(User.Role.TRANSPORTEUR)
                .active(true)
                .statut(User.StatutTransporteur.DISPONIBLE)
                .specialite(User.Specialite.FRAGILE)
                .build();

        String expectedToken = "fragile.token";
        LoginRequest request = new LoginRequest();
        request.setLogin("transfragile");
        request.setPassword("fragile123");

        when(userRepository.findByLogin("transfragile")).thenReturn(Optional.of(transporteurFragile));
        when(passwordEncoder.matches("fragile123", "fragilePass")).thenReturn(true);
        when(jwtUtil.generateToken("transfragile", "TRANSPORTEUR", "t200")).thenReturn(expectedToken);

        // When
        LoginResponse result = authService.login(request);

        // Then
        assertNotNull(result);
        assertEquals(expectedToken, result.getToken());
        assertEquals("transfragile", result.getLogin());
        assertEquals(User.Role.TRANSPORTEUR, result.getRole());
        assertEquals("t200", result.getUserId());
        assertEquals("Connexion réussie", result.getMessage());

        verify(userRepository, times(1)).findByLogin("transfragile");
        verify(passwordEncoder, times(1)).matches("fragile123", "fragilePass");
        verify(jwtUtil, times(1)).generateToken("transfragile", "TRANSPORTEUR", "t200");
    }

    @Test
    void testLogin_ResponseStructure() {
        // Given
        String expectedToken = "structured.token";

        LoginRequest request = new LoginRequest();
        request.setLogin("testuser");
        request.setPassword("password123");

        when(userRepository.findByLogin("testuser")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("password123", "encodedPassword")).thenReturn(true);
        when(jwtUtil.generateToken("testuser", "TRANSPORTEUR", "user1")).thenReturn(expectedToken);

        // When
        LoginResponse result = authService.login(request);

        // Then - Vérification complète de la structure de la réponse
        assertNotNull(result);
        assertEquals(expectedToken, result.getToken());
        assertEquals("testuser", result.getLogin());
        assertEquals(User.Role.TRANSPORTEUR, result.getRole());
        assertEquals("user1", result.getUserId());
        assertEquals("Connexion réussie", result.getMessage());

        // Vérification que tous les champs sont remplis
        assertNotNull(result.getToken());
        assertNotNull(result.getLogin());
        assertNotNull(result.getRole());
        assertNotNull(result.getUserId());
        assertNotNull(result.getMessage());

        // Vérification que le message est correct
        assertTrue(result.getMessage().contains("réussie"));

        verify(userRepository, times(1)).findByLogin("testuser");
        verify(passwordEncoder, times(1)).matches("password123", "encodedPassword");
        verify(jwtUtil, times(1)).generateToken("testuser", "TRANSPORTEUR", "user1");
    }

    @Test
    void testLogin_CaseSensitiveLogin() {
        // Given - Login en majuscules mais stocké en minuscules
        User userMixedCase = User.builder()
                .id("user999")
                .login("TestUser")
                .password("encodedPass")
                .role(User.Role.TRANSPORTEUR)
                .active(true)
                .build();

        String expectedToken = "case.token";

        LoginRequest request = new LoginRequest();
        request.setLogin("TestUser");
        request.setPassword("password123");

        when(userRepository.findByLogin("TestUser")).thenReturn(Optional.of(userMixedCase));
        when(passwordEncoder.matches("password123", "encodedPass")).thenReturn(true);
        when(jwtUtil.generateToken("TestUser", "TRANSPORTEUR", "user999")).thenReturn(expectedToken);

        // When
        LoginResponse result = authService.login(request);

        // Then
        assertNotNull(result);
        assertEquals(expectedToken, result.getToken());
        assertEquals("TestUser", result.getLogin());
        assertEquals(User.Role.TRANSPORTEUR, result.getRole());
        assertEquals("user999", result.getUserId());
        assertEquals("Connexion réussie", result.getMessage());

        verify(userRepository, times(1)).findByLogin("TestUser");
        verify(passwordEncoder, times(1)).matches("password123", "encodedPass");
        verify(jwtUtil, times(1)).generateToken("TestUser", "TRANSPORTEUR", "user999");
    }
}