package com.francis.taratulong.user.organization.v1;

import com.francis.taratulong.user.organization.OrgService;
import com.francis.taratulong.user.organization.v1.dto.OrgMapper;
import com.francis.taratulong.user.organization.v1.dto.OrgRequestDTO;
import com.francis.taratulong.user.organization.v1.dto.OrgResponseDTO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/orgs")
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrg(@PathVariable Long id) {
        orgService.deleteOrg(id);

        return ResponseEntity.noContent().build();
    }
}
