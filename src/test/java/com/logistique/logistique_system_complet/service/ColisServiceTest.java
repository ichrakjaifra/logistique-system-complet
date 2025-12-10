package com.logistique.logistique_system_complet.service;

import com.logistique.logistique_system_complet.model.Colis;
import com.logistique.logistique_system_complet.model.User;
import com.logistique.logistique_system_complet.repository.ColisRepository;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ColisServiceTest {

    @Mock
    private ColisRepository colisRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ColisService colisService;

    private Colis colisStandard;
    private Colis colisFragile;
    private Colis colisFrigo;
    private User transporteurStandard;
    private User transporteurFragile;
    private User transporteurFrigo;

    @BeforeEach
    void setUp() {
        // Création des colis de test
        colisStandard = Colis.builder()
                .id("1")
                .type(Colis.TypeColis.STANDARD)
                .poids(2.5)
                .adresseDestination("123 Rue Test, Ville")
                .statut(Colis.StatutColis.EN_ATTENTE)
                .dateCreation(LocalDateTime.now())
                .dateModification(LocalDateTime.now())
                .build();

        colisFragile = Colis.builder()
                .id("2")
                .type(Colis.TypeColis.FRAGILE)
                .poids(1.5)
                .adresseDestination("456 Rue Fragile, Ville")
                .statut(Colis.StatutColis.EN_ATTENTE)
                .instructionsManutention("Ne pas empiler")
                .dateCreation(LocalDateTime.now())
                .dateModification(LocalDateTime.now())
                .build();

        colisFrigo = Colis.builder()
                .id("3")
                .type(Colis.TypeColis.FRIGO)
                .poids(3.0)
                .adresseDestination("789 Rue Frigo, Ville")
                .statut(Colis.StatutColis.EN_ATTENTE)
                .temperatureMin(2.0)
                .temperatureMax(8.0)
                .dateCreation(LocalDateTime.now())
                .dateModification(LocalDateTime.now())
                .build();

        // Création des transporteurs de test
        transporteurStandard = User.builder()
                .id("t1")
                .login("transporteur1")
                .password("password")
                .role(User.Role.TRANSPORTEUR)
                .active(true)
                .statut(User.StatutTransporteur.DISPONIBLE)
                .specialite(User.Specialite.STANDARD)
                .build();

        transporteurFragile = User.builder()
                .id("t2")
                .login("transporteur2")
                .password("password")
                .role(User.Role.TRANSPORTEUR)
                .active(true)
                .statut(User.StatutTransporteur.DISPONIBLE)
                .specialite(User.Specialite.FRAGILE)
                .build();

        transporteurFrigo = User.builder()
                .id("t3")
                .login("transporteur3")
                .password("password")
                .role(User.Role.TRANSPORTEUR)
                .active(true)
                .statut(User.StatutTransporteur.DISPONIBLE)
                .specialite(User.Specialite.FRIGO)
                .build();
    }

    @Test
    void testCreateColis_Success() {
        // Given
        Colis newColis = Colis.builder()
                .type(Colis.TypeColis.STANDARD)
                .poids(5.0)
                .adresseDestination("Test Address")
                .build();

        when(colisRepository.save(any(Colis.class))).thenAnswer(invocation -> {
            Colis colis = invocation.getArgument(0);
            colis.setId("generated-id");
            colis.setDateCreation(LocalDateTime.now());
            colis.setDateModification(LocalDateTime.now());
            colis.setStatut(Colis.StatutColis.EN_ATTENTE);
            return colis;
        });

        // When
        Colis result = colisService.createColis(newColis);

        // Then
        assertNotNull(result);
        assertNotNull(result.getId());
        assertNotNull(result.getDateCreation());
        assertNotNull(result.getDateModification());
        assertEquals(Colis.StatutColis.EN_ATTENTE, result.getStatut());
        assertEquals("Test Address", result.getAdresseDestination());

        verify(colisRepository, times(1)).save(any(Colis.class));
    }

    @Test
    void testGetAllColis_Success() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        List<Colis> colisList = Arrays.asList(colisStandard, colisFragile, colisFrigo);
        Page<Colis> colisPage = new PageImpl<>(colisList, pageable, colisList.size());

        when(colisRepository.findAll(pageable)).thenReturn(colisPage);

        // When
        Page<Colis> result = colisService.getAllColis(pageable);

        // Then
        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        verify(colisRepository, times(1)).findAll(pageable);
    }

    @Test
    void testGetColisByTransporteur_Success() {
        // Given
        String transporteurId = "t1";
        Pageable pageable = PageRequest.of(0, 10);
        List<Colis> colisList = Arrays.asList(colisStandard);
        Page<Colis> colisPage = new PageImpl<>(colisList, pageable, colisList.size());

        when(colisRepository.findByTransporteurId(transporteurId, pageable)).thenReturn(colisPage);

        // When
        Page<Colis> result = colisService.getColisByTransporteur(transporteurId, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(colisRepository, times(1)).findByTransporteurId(transporteurId, pageable);
    }

    @Test
    void testAssignerColis_Success() {
        // Given
        String colisId = "1";
        String transporteurId = "t1";

        when(colisRepository.findById(colisId)).thenReturn(Optional.of(colisStandard));
        when(userRepository.findById(transporteurId)).thenReturn(Optional.of(transporteurStandard));
        when(colisRepository.save(any(Colis.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Colis result = colisService.assignerColis(colisId, transporteurId);

        // Then
        assertNotNull(result);
        assertEquals(transporteurId, result.getTransporteurId());
        assertEquals(Colis.StatutColis.EN_TRANSIT, result.getStatut());
        assertNotNull(result.getDateModification());

        verify(colisRepository, times(1)).findById(colisId);
        verify(userRepository, times(1)).findById(transporteurId);
        verify(colisRepository, times(1)).save(any(Colis.class));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testAssignerColis_TransporteurWrongSpecialite() {
        // Given
        String colisId = "2"; // FRAGILE
        String transporteurId = "t1"; // STANDARD

        when(colisRepository.findById(colisId)).thenReturn(Optional.of(colisFragile));
        when(userRepository.findById(transporteurId)).thenReturn(Optional.of(transporteurStandard));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> colisService.assignerColis(colisId, transporteurId));

        assertTrue(exception.getMessage().contains("spécialité requise"));

        verify(colisRepository, times(1)).findById(colisId);
        verify(userRepository, times(1)).findById(transporteurId);
        verify(colisRepository, never()).save(any(Colis.class));
    }

    @Test
    void testAssignerColis_TransporteurNotAvailable() {
        // Given
        String colisId = "1";
        String transporteurId = "t1";
        transporteurStandard.setStatut(User.StatutTransporteur.EN_LIVRAISON);

        when(colisRepository.findById(colisId)).thenReturn(Optional.of(colisStandard));
        when(userRepository.findById(transporteurId)).thenReturn(Optional.of(transporteurStandard));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> colisService.assignerColis(colisId, transporteurId));

        assertTrue(exception.getMessage().contains("disponible"));

        verify(colisRepository, times(1)).findById(colisId);
        verify(userRepository, times(1)).findById(transporteurId);
        verify(colisRepository, never()).save(any(Colis.class));
    }

    @Test
    void testAssignerColis_TransporteurNotFound() {
        // Given
        String colisId = "1";
        String transporteurId = "non-existent";

        when(colisRepository.findById(colisId)).thenReturn(Optional.of(colisStandard));
        when(userRepository.findById(transporteurId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> colisService.assignerColis(colisId, transporteurId));

        assertTrue(exception.getMessage().contains("Transporteur non trouvé"));

        verify(colisRepository, times(1)).findById(colisId);
        verify(userRepository, times(1)).findById(transporteurId);
        verify(colisRepository, never()).save(any(Colis.class));
    }

    @Test
    void testUpdateStatutColis_Success_Admin() {
        // Given
        String colisId = "1";
        Colis.StatutColis nouveauStatut = Colis.StatutColis.LIVRE;
        String transporteurId = null; // Admin update

        colisStandard.setTransporteurId("t1");

        when(colisRepository.findById(colisId)).thenReturn(Optional.of(colisStandard));
        when(userRepository.findById("t1")).thenReturn(Optional.of(transporteurStandard));
        when(colisRepository.save(any(Colis.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Colis result = colisService.updateStatutColis(colisId, nouveauStatut, transporteurId);

        // Then
        assertNotNull(result);
        assertEquals(nouveauStatut, result.getStatut());
        assertEquals(User.StatutTransporteur.DISPONIBLE, transporteurStandard.getStatut());

        verify(colisRepository, times(1)).findById(colisId);
        verify(colisRepository, times(1)).save(any(Colis.class));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testUpdateStatutColis_Success_Transporteur() {
        // Given
        String colisId = "1";
        Colis.StatutColis nouveauStatut = Colis.StatutColis.EN_TRANSIT;
        String transporteurId = "t1";

        colisStandard.setTransporteurId("t1");

        when(colisRepository.findById(colisId)).thenReturn(Optional.of(colisStandard));
        when(colisRepository.save(any(Colis.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Colis result = colisService.updateStatutColis(colisId, nouveauStatut, transporteurId);

        // Then
        assertNotNull(result);
        assertEquals(nouveauStatut, result.getStatut());

        verify(colisRepository, times(1)).findById(colisId);
        verify(colisRepository, times(1)).save(any(Colis.class));
    }

    @Test
    void testUpdateStatutColis_TransporteurUnauthorized() {
        // Given
        String colisId = "1";
        Colis.StatutColis nouveauStatut = Colis.StatutColis.EN_TRANSIT;
        String transporteurId = "t2"; // Different transporteur

        colisStandard.setTransporteurId("t1");

        when(colisRepository.findById(colisId)).thenReturn(Optional.of(colisStandard));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> colisService.updateStatutColis(colisId, nouveauStatut, transporteurId));

        assertTrue(exception.getMessage().contains("autorisé"));

        verify(colisRepository, times(1)).findById(colisId);
        verify(colisRepository, never()).save(any(Colis.class));
    }

    @Test
    void testUpdateStatutColis_ColisNotFound() {
        // Given
        String colisId = "non-existent";
        Colis.StatutColis nouveauStatut = Colis.StatutColis.LIVRE;

        when(colisRepository.findById(colisId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> colisService.updateStatutColis(colisId, nouveauStatut, null));

        assertTrue(exception.getMessage().contains("Colis non trouvé"));

        verify(colisRepository, times(1)).findById(colisId);
        verify(colisRepository, never()).save(any(Colis.class));
    }

    @Test
    void testSearchColisByAdresse_Success() {
        // Given
        String adresse = "Rue";
        Pageable pageable = PageRequest.of(0, 10);
        List<Colis> colisList = Arrays.asList(colisStandard, colisFragile);
        Page<Colis> colisPage = new PageImpl<>(colisList, pageable, colisList.size());

        when(colisRepository.findByAdresseDestinationContainingIgnoreCase(adresse, pageable))
                .thenReturn(colisPage);

        // When
        Page<Colis> result = colisService.searchColisByAdresse(adresse, pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        verify(colisRepository, times(1))
                .findByAdresseDestinationContainingIgnoreCase(adresse, pageable);
    }

    @Test
    void testFilterColisByTypeAndStatut_Success() {
        // Given
        Colis.TypeColis type = Colis.TypeColis.STANDARD;
        Colis.StatutColis statut = Colis.StatutColis.EN_ATTENTE;
        Pageable pageable = PageRequest.of(0, 10);
        List<Colis> colisList = Arrays.asList(colisStandard);
        Page<Colis> colisPage = new PageImpl<>(colisList, pageable, colisList.size());

        when(colisRepository.findByTypeAndStatut(type, statut, pageable))
                .thenReturn(colisPage);

        // When
        Page<Colis> result = colisService.filterColisByTypeAndStatut(type, statut, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(colisRepository, times(1))
                .findByTypeAndStatut(type, statut, pageable);
    }

    @Test
    void testUpdateColis_Success() {
        // Given
        String colisId = "1";
        Colis colisDetails = Colis.builder()
                .poids(3.0)
                .adresseDestination("Nouvelle Adresse")
                .build();

        when(colisRepository.findById(colisId)).thenReturn(Optional.of(colisStandard));
        when(colisRepository.save(any(Colis.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Colis result = colisService.updateColis(colisId, colisDetails);

        // Then
        assertNotNull(result);
        assertEquals(3.0, result.getPoids());
        assertEquals("Nouvelle Adresse", result.getAdresseDestination());
        assertNotNull(result.getDateModification());

        verify(colisRepository, times(1)).findById(colisId);
        verify(colisRepository, times(1)).save(any(Colis.class));
    }

    @Test
    void testUpdateColis_ColisNotFound() {
        // Given
        String colisId = "non-existent";
        Colis colisDetails = Colis.builder().build();

        when(colisRepository.findById(colisId)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> colisService.updateColis(colisId, colisDetails));

        assertTrue(exception.getMessage().contains("Colis non trouvé"));

        verify(colisRepository, times(1)).findById(colisId);
        verify(colisRepository, never()).save(any(Colis.class));
    }

    @Test
    void testUpdateColis_UpdateFragileColis() {
        // Given
        String colisId = "2";
        Colis colisDetails = Colis.builder()
                .poids(2.0)
                .adresseDestination("Nouvelle Adresse Fragile")
                .instructionsManutention("Nouvelles instructions")
                .build();

        when(colisRepository.findById(colisId)).thenReturn(Optional.of(colisFragile));
        when(colisRepository.save(any(Colis.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Colis result = colisService.updateColis(colisId, colisDetails);

        // Then
        assertNotNull(result);
        assertEquals(2.0, result.getPoids());
        assertEquals("Nouvelle Adresse Fragile", result.getAdresseDestination());
        assertEquals("Nouvelles instructions", result.getInstructionsManutention());

        verify(colisRepository, times(1)).findById(colisId);
        verify(colisRepository, times(1)).save(any(Colis.class));
    }

    @Test
    void testUpdateColis_UpdateFrigoColis() {
        // Given
        String colisId = "3";
        Colis colisDetails = Colis.builder()
                .poids(4.0)
                .adresseDestination("Nouvelle Adresse Frigo")
                .temperatureMin(0.0)
                .temperatureMax(5.0)
                .build();

        when(colisRepository.findById(colisId)).thenReturn(Optional.of(colisFrigo));
        when(colisRepository.save(any(Colis.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        Colis result = colisService.updateColis(colisId, colisDetails);

        // Then
        assertNotNull(result);
        assertEquals(4.0, result.getPoids());
        assertEquals("Nouvelle Adresse Frigo", result.getAdresseDestination());
        assertEquals(0.0, result.getTemperatureMin());
        assertEquals(5.0, result.getTemperatureMax());

        verify(colisRepository, times(1)).findById(colisId);
        verify(colisRepository, times(1)).save(any(Colis.class));
    }

    @Test
    void testDeleteColis_Success() {
        // Given
        String colisId = "1";

        doNothing().when(colisRepository).deleteById(colisId);

        // When
        colisService.deleteColis(colisId);

        // Then
        verify(colisRepository, times(1)).deleteById(colisId);
    }
}