package com.francis.taratulong.user.organization.v1;

import com.francis.taratulong.user.AppUserService;
import com.francis.taratulong.user.organization.OrgService;
import com.francis.taratulong.user.organization.v1.dto.OrgMapper;
import com.francis.taratulong.user.organization.v1.dto.OrgRequestDTO;
import com.francis.taratulong.user.organization.v1.dto.OrgResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Tag(name = "Organization")
@RestController
@RequestMapping("api/v1/orgs")
@RequiredArgsConstructor
public class OrgController {
    private final OrgService orgService;
    private final AppUserService appUserService;
    private final OrgMapper orgMapper;


    @PostMapping
    public ResponseEntity<OrgResponseDTO> saveOrg(@Valid @RequestBody OrgRequestDTO requestDTO) {
        OrgResponseDTO response = orgMapper.toResponse(
                orgService.saveOrg(orgMapper.toEntity(requestDTO))
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrgResponseDTO> getOrg(@PathVariable Long id) {
        OrgResponseDTO response = orgMapper.toResponse(orgService.getOrg(id));

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrgResponseDTO> updateOrg(
            @PathVariable Long id,
            @Valid @RequestBody OrgRequestDTO orgRequestDTO
    ) {
        OrgResponseDTO response = orgMapper.toResponse(
                orgService.updateOrg(id, orgMapper.toEntity(orgRequestDTO))
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrg(@PathVariable Long id) {
        appUserService.deleteUser(id);

        return ResponseEntity.noContent().build();
    }
}
