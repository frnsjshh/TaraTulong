package com.francis.taratulong.user;


import com.francis.taratulong.user.dto.AppUserRequestPasswordDTO;
import com.francis.taratulong.user.dto.AppUserRequestEmailDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
public class AppUserController {
    private final AppUserService appUserService;

    public AppUserController(AppUserService appUserService) {
        this.appUserService = appUserService;
    }


    @PatchMapping("/password")
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
