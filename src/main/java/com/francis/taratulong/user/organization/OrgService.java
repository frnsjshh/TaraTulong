package com.francis.taratulong.user.organization;

import com.francis.taratulong.exception.UserAlreadyExistsException;
import com.francis.taratulong.exception.UserNotFoundException;
import com.francis.taratulong.user.Role;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OrgService {
    private final OrgRepository orgRepository;
    private final PasswordEncoder passwordEncoder;

    public Org saveOrg(Org org) {
        log.debug("Attempting to save organization with email={}", org.getEmail());
        Org dbFound = orgRepository.findByEmail(org.getEmail()).orElse(null);
        if(dbFound!=null&&!dbFound.isDeleted()) {
            log.warn("Organization registration denied: email {} already in use", org.getEmail());
            throw  new UserAlreadyExistsException("Cannot create account. Email already in use", org.getEmail());
        }
        org.setPassword(passwordEncoder.encode(org.getPassword()));
        org.setRole(Role.ORG);
        Org saved = orgRepository.save(org);
        log.info("Organization created: id={}, email={}", saved.getId(), saved.getEmail());
        return saved;
    }
    public Org getOrg(Long id) {
        return orgRepository.findById(id).orElseThrow(()-> new UserNotFoundException("Organization not found"));
    }

    public Org updateOrg(Long id, Org org) {
        Org existingOrg = orgRepository.findById(id).orElseThrow(() -> new UserNotFoundException("Organization not found"));
        existingOrg.setDescription(org.getDescription());
        existingOrg.setLocation(org.getLocation());
        return existingOrg;
    }

    public void deleteOrg(Long id) {
        Org org = orgRepository.findById(id).orElseThrow(()-> new UserNotFoundException("Cannot delete user. User not found"));
        org.setDeleted(true);
        log.info("Organization soft-deleted: id={}", id);
    }

    public boolean orgExist(Long id) {
        return orgRepository.existsById(id);
    }
}
