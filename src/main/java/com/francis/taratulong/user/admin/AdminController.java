package com.francis.taratulong.user.admin;

import com.francis.taratulong.user.AppUser;
import com.francis.taratulong.user.admin.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @PostMapping
    public AdminResponseDTO saveAdmin(@Valid@RequestBody AdminRequestDTO requestDTO) {
        return AdminMapper.toResponse(adminService.saveAdmin(AdminMapper.toEntity(requestDTO)));
    }

    @GetMapping("/me")
    public AdminResponseDTO getAdmin(@AuthenticationPrincipal AppUser appUser){
        return AdminMapper.toResponse(adminService.getAdmin(appUser.getId()));
    }

    @PutMapping("/me")
    public AdminResponseDTO updateAdmin(
            @AuthenticationPrincipal AppUser appUser,
            @Valid@RequestBody AdminRequestUpdateProfile requestDTO
    ) {
        return AdminMapper.toResponse(adminService.updateAdmin(appUser.getId(), AdminMapper.toEntity(requestDTO)));
    }

    @PatchMapping("/me/approve/{orgId}")
    public void approveOrg(
            @AuthenticationPrincipal AppUser appUser,
            @PathVariable Long orgId
    ){
        adminService.approveOrg(appUser.getId(), orgId);
    }

    @PatchMapping("/me/reject/{orgId}")
    public void rejectOrg(
            @PathVariable Long orgId
    ){
        adminService.rejectOrg(orgId);
    }


    @DeleteMapping("/me")
    public void deleteAdmin(@AuthenticationPrincipal AppUser appUser){
        adminService.deleteAdmin(appUser.getId());
    }
}
