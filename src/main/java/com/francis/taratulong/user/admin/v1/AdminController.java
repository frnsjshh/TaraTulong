package com.francis.taratulong.user.admin.v1;

import com.francis.taratulong.Status;
import com.francis.taratulong.user.AppUser;
import com.francis.taratulong.user.AppUserService;
import com.francis.taratulong.user.admin.AdminService;
import com.francis.taratulong.user.admin.v1.dto.AdminMapper;
import com.francis.taratulong.user.admin.v1.dto.AdminOrgResponseDTO;
import com.francis.taratulong.user.admin.v1.dto.AdminRequestDTO;
import com.francis.taratulong.user.admin.v1.dto.AdminRequestUpdateProfile;
import com.francis.taratulong.user.admin.v1.dto.AdminResponseDTO;
import com.francis.taratulong.user.organization.Org;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin")
@RestController
@RequestMapping("api/v1/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;
    private final AppUserService appUserService;
    private final AdminMapper adminMapper;

    @PostMapping
    public ResponseEntity<AdminResponseDTO> saveAdmin(@Valid@RequestBody AdminRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                adminMapper.toResponse(adminService.saveAdmin(adminMapper.toEntity(requestDTO)))
        );
    }

    @GetMapping("/me")
    public ResponseEntity<AdminResponseDTO> getAdmin(@AuthenticationPrincipal AppUser appUser){
        return ResponseEntity.ok(
                adminMapper.toResponse(adminService.getAdmin(appUser.getId()))
        );
    }

    @PutMapping("/me")
    public ResponseEntity<AdminResponseDTO> updateAdmin(
            @AuthenticationPrincipal AppUser appUser,
            @Valid@RequestBody AdminRequestUpdateProfile requestDTO
    ) {
        return ResponseEntity.ok(
                adminMapper.toResponse(adminService.updateAdmin(appUser.getId(), adminMapper.toEntity(requestDTO)))
        );
    }

    @GetMapping("/orgs")
    public ResponseEntity<Page<AdminOrgResponseDTO>> getOrgs(
            @RequestParam(required = false) Status status,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<Org> orgs = adminService.getOrgs(status, pageable);
        return ResponseEntity.ok(orgs.map(adminMapper::toAdminOrgResponseDTO));
    }

    @GetMapping("/orgs/pending")
    public ResponseEntity<Page<AdminOrgResponseDTO>> getPendingOrgs(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<Org> orgs = adminService.getOrgs(Status.PENDING, pageable);
        return ResponseEntity.ok(orgs.map(adminMapper::toAdminOrgResponseDTO));
    }

    @GetMapping("/orgs/{orgId}")
    public ResponseEntity<AdminOrgResponseDTO> getOrgDetails(@PathVariable Long orgId) {
        return ResponseEntity.ok(adminMapper.toAdminOrgResponseDTO(adminService.getOrgDetails(orgId)));
    }

    @PatchMapping("/me/approve/{orgId}")
    public ResponseEntity<Void> approveOrg(
            @AuthenticationPrincipal AppUser appUser,
            @PathVariable Long orgId
    ){
        adminService.approveOrg(appUser.getId(), orgId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/reject/{orgId}")
    public ResponseEntity<Void> rejectOrg(@PathVariable Long orgId){
        adminService.rejectOrg(orgId);
        return ResponseEntity.noContent().build();
    }


    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteAdmin(@AuthenticationPrincipal AppUser appUser){
        appUserService.deleteUser(appUser.getId());
        return ResponseEntity.noContent().build();
    }
}
