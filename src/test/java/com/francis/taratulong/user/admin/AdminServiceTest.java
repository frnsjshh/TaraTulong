package com.francis.taratulong.user.admin;

import com.francis.taratulong.Status;
import com.francis.taratulong.exception.UserAlreadyExistsException;
import com.francis.taratulong.exception.UserNotFoundException;
import com.francis.taratulong.user.Role;
import com.francis.taratulong.user.organization.Org;
import com.francis.taratulong.user.organization.OrgService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private OrgService orgService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminService adminService;

    private Admin admin;

    @BeforeEach
    void setUp() {
        admin = new Admin();
        admin.setId(1L);
        admin.setEmail("admin@email.com");
        admin.setPassword("rawPassword");
        admin.setName("Super Admin");
    }

    // ══════════════════════════════════════════════════════════════════
    // saveAdmin
    // ══════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("saveAdmin")
    class SaveAdmin {

        @Test
        @DisplayName("should save admin when email is not yet taken")
        void happyPath() {
            when(adminRepository.findByEmail("admin@email.com")).thenReturn(Optional.empty());
            when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
            when(adminRepository.save(any(Admin.class))).thenAnswer(i -> {
                Admin saved = i.getArgument(0);
                saved.setId(1L);
                return saved;
            });

            Admin result = adminService.saveAdmin(admin);

            assertNotNull(result);
            assertEquals("encodedPassword", result.getPassword());
            assertEquals(Role.ADMIN, result.getRole());
            verify(adminRepository, times(1)).save(any(Admin.class));
        }

        @Test
        @DisplayName("should throw UserAlreadyExistsException when non-deleted admin with same email exists")
        void shouldThrowWhenEmailExists() {
            Admin existingAdmin = new Admin();
            existingAdmin.setEmail("admin@email.com");
            existingAdmin.setDeleted(false);

            when(adminRepository.findByEmail("admin@email.com")).thenReturn(Optional.of(existingAdmin));

            assertThrows(
                    UserAlreadyExistsException.class,
                    () -> adminService.saveAdmin(admin)
            );
            verify(adminRepository, never()).save(any());
        }

        @Test
        @DisplayName("should allow saving when previous admin with same email was deleted")
        void shouldAllowWhenPreviousAdminDeleted() {
            Admin deletedAdmin = new Admin();
            deletedAdmin.setEmail("admin@email.com");
            deletedAdmin.setDeleted(true);

            when(adminRepository.findByEmail("admin@email.com")).thenReturn(Optional.of(deletedAdmin));
            when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPassword");
            when(adminRepository.save(any(Admin.class))).thenAnswer(i -> i.getArgument(0));

            Admin result = adminService.saveAdmin(admin);

            assertNotNull(result);
            assertEquals(Role.ADMIN, result.getRole());
            verify(adminRepository).save(any(Admin.class));
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // getAdmin
    // ══════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("getAdmin")
    class GetAdmin {

        @Test
        @DisplayName("should return admin when found")
        void happyPath() {
            when(adminRepository.findById(1L)).thenReturn(Optional.of(admin));

            Admin result = adminService.getAdmin(1L);

            assertEquals(1L, result.getId());
            assertEquals("Super Admin", result.getName());
        }

        @Test
        @DisplayName("should throw UserNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(adminRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(
                    UserNotFoundException.class,
                    () -> adminService.getAdmin(999L)
            );
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // updateAdmin
    // ══════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("updateAdmin")
    class UpdateAdmin {

        @Test
        @DisplayName("should update admin name")
        void happyPath() {
            Admin updateRequest = new Admin();
            updateRequest.setName("Updated Admin");

            when(adminRepository.findById(1L)).thenReturn(Optional.of(admin));

            Admin result = adminService.updateAdmin(1L, updateRequest);

            assertEquals("Updated Admin", result.getName());
        }

        @Test
        @DisplayName("should throw UserNotFoundException when admin doesn't exist")
        void shouldThrowWhenNotFound() {
            when(adminRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(
                    UserNotFoundException.class,
                    () -> adminService.updateAdmin(999L, new Admin())
            );
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // approveOrg
    // ══════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("approveOrg")
    class ApproveOrg {

        @Test
        @DisplayName("should set org status to APPROVED and link admin as approvedBy")
        void happyPath() {
            Org org = new Org();
            org.setId(100L);
            org.setStatus(Status.PENDING);

            when(orgService.getOrg(100L)).thenReturn(org);
            when(adminRepository.findById(1L)).thenReturn(Optional.of(admin));

            adminService.approveOrg(1L, 100L);

            assertEquals(Status.APPROVED, org.getStatus());
            assertEquals(admin, org.getApprovedBy());
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // rejectOrg
    // ══════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("rejectOrg")
    class RejectOrg {

        @Test
        @DisplayName("should set org status to REJECTED")
        void happyPath() {
            Org org = new Org();
            org.setId(100L);
            org.setStatus(Status.PENDING);

            when(orgService.getOrg(100L)).thenReturn(org);

            adminService.rejectOrg(100L);

            assertEquals(Status.REJECTED, org.getStatus());
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // getOrgs
    // ══════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("getOrgs")
    class GetOrgs {

        @Test
        @DisplayName("should return paginated organizations filtered by status")
        void shouldReturnOrgsFilteredByStatus() {
            Org org = new Org();
            org.setId(100L);
            org.setStatus(Status.PENDING);
            Pageable pageable = PageRequest.of(0, 10);
            Page<Org> orgPage = new PageImpl<>(List.of(org), pageable, 1);

            when(orgService.getOrgs(Status.PENDING, pageable)).thenReturn(orgPage);

            Page<Org> result = adminService.getOrgs(Status.PENDING, pageable);

            assertEquals(1, result.getTotalElements());
            assertEquals(Status.PENDING, result.getContent().get(0).getStatus());
            verify(orgService).getOrgs(Status.PENDING, pageable);
        }

        @Test
        @DisplayName("should return all organizations when status is null")
        void shouldReturnAllOrgsWhenStatusNull() {
            Org pendingOrg = new Org();
            pendingOrg.setId(100L);
            pendingOrg.setStatus(Status.PENDING);

            Org approvedOrg = new Org();
            approvedOrg.setId(101L);
            approvedOrg.setStatus(Status.APPROVED);

            Pageable pageable = PageRequest.of(0, 10);
            Page<Org> orgPage = new PageImpl<>(List.of(pendingOrg, approvedOrg), pageable, 2);

            when(orgService.getOrgs(null, pageable)).thenReturn(orgPage);

            Page<Org> result = adminService.getOrgs(null, pageable);

            assertEquals(2, result.getTotalElements());
            verify(orgService).getOrgs(null, pageable);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // getOrgDetails
    // ══════════════════════════════════════════════════════════════════
    @Nested
    @DisplayName("getOrgDetails")
    class GetOrgDetails {

        @Test
        @DisplayName("should return organization details by id")
        void shouldReturnOrgDetails() {
            Org org = new Org();
            org.setId(100L);
            org.setName("Red Cross");
            org.setStatus(Status.PENDING);

            when(orgService.getOrg(100L)).thenReturn(org);

            Org result = adminService.getOrgDetails(100L);

            assertNotNull(result);
            assertEquals(100L, result.getId());
            assertEquals("Red Cross", result.getName());
            verify(orgService).getOrg(100L);
        }
    }
}
