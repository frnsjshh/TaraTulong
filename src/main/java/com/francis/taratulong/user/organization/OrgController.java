package com.francis.taratulong.user.organization;

import com.francis.taratulong.user.organization.dto.OrgEmailUpdateRequestDTO;
import com.francis.taratulong.user.organization.dto.OrgMapper;
import com.francis.taratulong.user.organization.dto.OrgRequestDTO;
import com.francis.taratulong.user.organization.dto.OrgResponseDTO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/org")
public class OrgController {
    private final OrgService orgService;

    public OrgController(OrgService orgService) {
        this.orgService = orgService;
    }
    @PostMapping
    public ResponseEntity<OrgResponseDTO> saveOrg(@Valid @RequestBody OrgRequestDTO requestDTO) {
        OrgResponseDTO response = OrgMapper.toResponse(
                orgService.saveOrg(OrgMapper.toEntity(requestDTO))
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrgResponseDTO> getOrg(@PathVariable Long id) {
        OrgResponseDTO response = OrgMapper.toResponse(orgService.getOrg(id));

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrgResponseDTO> updateOrg(
            @PathVariable Long id,
            @Valid @RequestBody OrgRequestDTO orgRequestDTO
    ) {
        OrgResponseDTO response = OrgMapper.toResponse(
                orgService.updateOrg(id, OrgMapper.toEntity(orgRequestDTO))
        );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/email")
    public ResponseEntity<OrgResponseDTO> updateEmail(
            @PathVariable Long id,
            @Valid @RequestBody OrgEmailUpdateRequestDTO emailUpdateRequestDTO
    ) {
        OrgResponseDTO response = OrgMapper.toResponse(
                orgService.updateEmail(id, emailUpdateRequestDTO.email())
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrg(@PathVariable Long id) {
        orgService.deleteOrg(id);

        return ResponseEntity.noContent().build();
    }

}
