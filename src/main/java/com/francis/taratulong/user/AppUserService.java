package com.francis.taratulong.user;

import com.francis.taratulong.exception.UserAlreadyExistsException;
import com.francis.taratulong.exception.UserNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AppUserService {
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public void updateEmail(Long id, String email) {
        log.debug("Attempting email update for userId={}", id);
        AppUser appUser = appUserRepository.findById(id).orElseThrow(()->new UserNotFoundException("Cannot update email. User not found"));
        AppUser appUserEmail = appUserRepository.findByEmail(email).orElse(null);
        if(appUserEmail!=null) {
            log.warn("Email update denied for userId={}: email already in use", id);
            throw new UserAlreadyExistsException("Cannot update email. Email already in use", email);
        }
        appUser.setEmail(email);
        log.info("Email updated for userId={}", id);
    }
    public void updatePassword(Long id, String currentPassword, String password) {
        log.debug("Attempting password update for userId={}", id);
        AppUser appUser = appUserRepository.findById(id).orElseThrow(()->new UserNotFoundException("Cannot change password. User not found"));

        if(passwordEncoder.matches(currentPassword, appUser.getPassword())) {
            appUser.setPassword(passwordEncoder.encode(password));
            log.info("Password updated for userId={}", id);
        } else {
            log.warn("Password update failed for userId={}: current password mismatch", id);
            throw new BadCredentialsException("Cannot change password. Current password mismatch");
        }
    }

    public void deleteUser(Long id) {
        log.debug("Attempting to delete user with id={}", id);
        AppUser appUser = appUserRepository.findById(id).orElseThrow(()->new UserNotFoundException("Cannot delete user. User not found"));
        appUser.setDeleted(true);
        appUser.setEmail("DELETED_"+appUser.getEmail()+ "_"+ LocalDateTime.now());
        appUser.setPassword(null);
        log.info("User deleted with id={}", id);
    }

}
