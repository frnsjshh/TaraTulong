package com.francis.taratulong.user;

import com.francis.taratulong.exception.UserAlreadyExistsException;
import com.francis.taratulong.exception.UserNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class AppUserService {
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;


    public AppUserService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {

        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void updateEmail(Long id, String email) {
        AppUser appUser = appUserRepository.findById(id).orElseThrow(()->new UserNotFoundException("Cannot update email. User not found"));
        AppUser appUserEmail = appUserRepository.findByEmail(email).orElse(null);
        if(appUserEmail!=null) throw new UserAlreadyExistsException("Cannot update email. Email already in use", email);
        appUser.setEmail(email);
    }
    public void updatePassword(Long id, String currentPassword, String password) {
        AppUser appUser = appUserRepository.findById(id).orElseThrow(()->new UserNotFoundException("Cannot change password. User not found"));
        if(passwordEncoder.matches(currentPassword, appUser.getPassword())) {
            appUser.setPassword(passwordEncoder.encode(password));
        }
    }

}
