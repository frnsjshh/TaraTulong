package com.francis.taratulong.user.admin;

import com.francis.taratulong.user.AppUser;
import com.francis.taratulong.user.admin.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @PostMapping
    public ResponseEntity<AdminResponseDTO> saveAdmin(@Valid@RequestBody AdminRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                AdminMapper.toResponse(adminService.saveAdmin(AdminMapper.toEntity(requestDTO)))
        );
    }

    @GetMapping("/me")
    public ResponseEntity<AdminResponseDTO> getAdmin(@AuthenticationPrincipal AppUser appUser){
        return ResponseEntity.ok(
                AdminMapper.toResponse(adminService.getAdmin(appUser.getId()))
        );
    }

    @PutMapping("/me")
    public ResponseEntity<AdminResponseDTO> updateAdmin(
            @AuthenticationPrincipal AppUser appUser,
            @Valid@RequestBody AdminRequestUpdateProfile requestDTO
    ) {
        return ResponseEntity.ok(
                AdminMapper.toResponse(adminService.updateAdmin(appUser.getId(), AdminMapper.toEntity(requestDTO)))
        );
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
        adminService.deleteAdmin(appUser.getId());
        return ResponseEntity.noContent().build();
    }
}
