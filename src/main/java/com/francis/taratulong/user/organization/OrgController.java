package com.francis.taratulong.user.organization;

import com.francis.taratulong.user.organization.dto.OrgEmailUpdateRequestDTO;
import com.francis.taratulong.user.organization.dto.OrgMapper;
import com.francis.taratulong.user.organization.dto.OrgRequestDTO;
import com.francis.taratulong.user.organization.dto.OrgResponseDTO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/org")
public class OrgController {
    private final OrgService orgService;

    public OrgController(OrgService orgService) {
        this.orgService = orgService;
    }
    @PostMapping
    public OrgResponseDTO saveOrg(@Valid@RequestBody OrgRequestDTO requestDTO){
        return OrgMapper.toResponse(orgService.saveOrg(OrgMapper.toEntity(requestDTO)));
    }

    @GetMapping("/{id}")
    public OrgResponseDTO getOrg(@PathVariable Long id) {
        return OrgMapper.toResponse(orgService.getOrg(id));
    }

    @PutMapping("/{id}")
    public OrgResponseDTO updateOrg(@PathVariable Long id, @Valid@RequestBody OrgRequestDTO orgRequestDTO){
        return OrgMapper.toResponse(orgService.updateOrg(id, OrgMapper.toEntity(orgRequestDTO)));
    }
    @PatchMapping("/{id}/email")
    public OrgResponseDTO updateEmail(@PathVariable Long id, @Valid@RequestBody OrgEmailUpdateRequestDTO emailUpdateRequestDTO){
        return OrgMapper.toResponse(orgService.updateEmail(id, emailUpdateRequestDTO.email()));
    }
    @DeleteMapping("/{id}")
    public void deleteOrg(@PathVariable Long id){
        orgService.deleteOrg(id);
    }

}
