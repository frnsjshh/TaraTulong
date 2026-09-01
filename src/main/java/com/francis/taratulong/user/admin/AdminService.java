package com.francis.taratulong.user.admin;

import com.francis.taratulong.Status;
import com.francis.taratulong.exception.UserAlreadyExistsException;
import com.francis.taratulong.exception.UserNotFoundException;
import com.francis.taratulong.user.Role;
import com.francis.taratulong.user.organization.Org;
import com.francis.taratulong.user.organization.OrgService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class AdminService {
    private final AdminRepository adminRepository;
    private final OrgService orgService;
    private final PasswordEncoder passwordEncoder;

    public Admin saveAdmin(Admin admin){
        log.debug("Attempting to save admin with email={}", admin.getEmail());
        Admin adminDB = adminRepository.findByEmail(admin.getEmail()).orElse(null);
        if(adminDB!=null&&!adminDB.isDeleted()){
            log.warn("Admin registration denied: email {} already exists", admin.getEmail());
            throw new UserAlreadyExistsException("Cannot create admin. User already exist", adminDB.getEmail());
        }
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        admin.setRole(Role.ADMIN);
        Admin saved = adminRepository.save(admin);
        log.info("Admin created: id={}, email={}", saved.getId(), saved.getEmail());
        return saved;
    }

    public Admin getAdmin(Long id) {
        return adminRepository.findById(id).orElseThrow(()-> new UserNotFoundException("User not found."));
    }

    public Admin updateAdmin(Long id, Admin admin){
        Admin adminDB = adminRepository.findById(id).orElseThrow(()-> new UserNotFoundException("Cannot update user. User not found."));
        adminDB.setName(admin.getName());
        return adminDB;
    }

    public void approveOrg(Long adminID, Long orgID) {
        Org org = orgService.getOrg(orgID);
        org.setStatus(Status.APPROVED);
        org.setApprovedBy(getAdmin(adminID));
        log.info("Organization approved: orgId={}, approvedBy adminId={}", orgID, adminID);
    }

    public void rejectOrg(Long orgID) {
        Org org = orgService.getOrg(orgID);
        org.setStatus(Status.REJECTED);
        log.info("Organization rejected: orgId={}", orgID);
    }
}
