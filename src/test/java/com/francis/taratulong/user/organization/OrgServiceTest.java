package com.francis.taratulong.user.organization;

import com.francis.taratulong.exception.UserAlreadyExistsException;
import com.francis.taratulong.exception.UserNotFoundException;
import com.francis.taratulong.user.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrgServiceTest {

    @Mock
    private OrgRepository orgRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private OrgService orgService;

    private Org org;

    @BeforeEach
    void setUp() {
        org = new Org();
        org.setId(100L);
        org.setEmail("org@email.com");
        org.setPassword("rawPassword");
        org.setName("Test Organization");
        org.setDescription("A test org");
        org.setLocation("Manila");
    }

    // ══════════════════════════════════════════════════════════════════
    // saveOrg
    // ══════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("saveOrg")
    class SaveOrg {

        @Test
        @DisplayName("should save org when email is not yet taken")
        void happyPath() {
            when(orgRepository.findByEmail("org@email.com")).thenReturn(Optional.empty());
            when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
            when(orgRepository.save(any(Org.class))).thenAnswer(i -> {
                Org saved = i.getArgument(0);
                saved.setId(100L);
                return saved;
            });

            Org result = orgService.saveOrg(org);

            assertNotNull(result);
            assertEquals("encodedPassword", result.getPassword());
            assertEquals(Role.ORG, result.getRole());
            verify(orgRepository, times(1)).save(any(Org.class));
        }

        @Test
        @DisplayName("should throw UserAlreadyExistsException when email is taken by a non-deleted org")
        void shouldThrowWhenEmailExists() {
            // The existing org is NOT deleted
            Org existingOrg = new Org();
            existingOrg.setEmail("org@email.com");
            existingOrg.setDeleted(false);

            when(orgRepository.findByEmail("org@email.com")).thenReturn(Optional.of(existingOrg));

            assertThrows(
                    UserAlreadyExistsException.class,
                    () -> orgService.saveOrg(org)
            );
            verify(orgRepository, never()).save(any());
        }

        @Test
        @DisplayName("should allow saving when previous org with same email was deleted")
        void shouldAllowWhenPreviousOrgDeleted() {
            Org deletedOrg = new Org();
            deletedOrg.setEmail("org@email.com");
            deletedOrg.setDeleted(true);

            when(orgRepository.findByEmail("org@email.com")).thenReturn(Optional.of(deletedOrg));
            when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
            when(orgRepository.save(any(Org.class))).thenAnswer(i -> i.getArgument(0));

            Org result = orgService.saveOrg(org);

            assertNotNull(result);
            assertEquals(Role.ORG, result.getRole());
            verify(orgRepository).save(any(Org.class));
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // getOrg
    // ══════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("getOrg")
    class GetOrg {

        @Test
        @DisplayName("should return org when found")
        void happyPath() {
            when(orgRepository.findById(100L)).thenReturn(Optional.of(org));

            Org result = orgService.getOrg(100L);

            assertEquals(100L, result.getId());
            assertEquals("Test Organization", result.getName());
        }

        @Test
        @DisplayName("should throw UserNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(orgRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(
                    UserNotFoundException.class,
                    () -> orgService.getOrg(999L)
            );
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // updateOrg
    // ══════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("updateOrg")
    class UpdateOrg {

        @Test
        @DisplayName("should update description and location")
        void happyPath() {
            Org updateRequest = new Org();
            updateRequest.setDescription("Updated description");
            updateRequest.setLocation("Cebu");

            when(orgRepository.findById(100L)).thenReturn(Optional.of(org));

            Org result = orgService.updateOrg(100L, updateRequest);

            assertEquals("Updated description", result.getDescription());
            assertEquals("Cebu", result.getLocation());
        }

        @Test
        @DisplayName("should throw UserNotFoundException when org doesn't exist")
        void shouldThrowWhenNotFound() {
            when(orgRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(
                    UserNotFoundException.class,
                    () -> orgService.updateOrg(999L, new Org())
            );
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // orgExist
    // ══════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("orgExist")
    class OrgExist {

        @Test
        @DisplayName("should return true when org exists")
        void shouldReturnTrueWhenExists() {
            when(orgRepository.existsById(100L)).thenReturn(true);

            assertTrue(orgService.orgExist(100L));
        }

        @Test
        @DisplayName("should return false when org does not exist")
        void shouldReturnFalseWhenNotExists() {
            when(orgRepository.existsById(999L)).thenReturn(false);

            assertFalse(orgService.orgExist(999L));
        }
    }
}
