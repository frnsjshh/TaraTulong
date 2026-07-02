package com.francis.taratulong.user.admin;

import com.francis.taratulong.user.admin.dto.AdminMapper;
import com.francis.taratulong.user.admin.dto.AdminRequestDTO;
import com.francis.taratulong.user.admin.dto.AdminResponseDTO;
import com.francis.taratulong.user.admin.dto.AdminUpdateEmailRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/{id}")
    public AdminResponseDTO getAdmin(@PathVariable Long id){
        return AdminMapper.toResponse(adminService.getAdmin(id));
    }

    @PutMapping("/{id}")
    public AdminResponseDTO updateAdmin(@PathVariable Long id, @Valid@RequestBody AdminRequestDTO requestDTO) {
        return AdminMapper.toResponse(adminService.updateAdmin(id, AdminMapper.toEntity(requestDTO)));
    }

    @PatchMapping("/{id}/email")
    public AdminResponseDTO updateEmail(@PathVariable Long id, @Valid @RequestBody AdminUpdateEmailRequestDTO emailRequestDTO) {
        return AdminMapper.toResponse(adminService.updateEmail(id, emailRequestDTO.email()));
    }

    @PatchMapping("/{id}/approve/{orgId}")
    public void approveOrg(@PathVariable Long id, @PathVariable Long orgId){
        adminService.approveOrg(id, orgId);
    }

    @DeleteMapping("/{id}")
    public void deleteAdmin(@PathVariable Long id){
        adminService.deleteAdmin(id);
    }
}
