package com.francis.taratulong.user.v1;


import com.francis.taratulong.user.AppUser;
import com.francis.taratulong.user.AppUserService;
import com.francis.taratulong.user.v1.dto.AppUserRequestPasswordDTO;
import com.francis.taratulong.user.v1.dto.AppUserRequestEmailDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User")
@RestController
@RequestMapping("/user")
public class AppUserController {
    private final AppUserService appUserService;

    public AppUserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }


    @PutMapping("/password")
    public ResponseEntity<Void> updatePassword(@AuthenticationPrincipal AppUser appUser, @RequestBody AppUserRequestPasswordDTO requestDTO) {
        appUserService.updatePassword(appUser.getId(), requestDTO.currentPassword(),requestDTO.password());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/email")
    public ResponseEntity<Void> updateEmail(@AuthenticationPrincipal AppUser appUser, @RequestBody AppUserRequestEmailDTO requestEmailDTO) {
        appUserService.updateEmail(appUser.getId(), requestEmailDTO.email());
        return ResponseEntity.noContent().build();
    }
}
