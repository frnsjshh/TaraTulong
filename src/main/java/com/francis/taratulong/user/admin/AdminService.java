package com.francis.taratulong.user.admin;

import com.francis.taratulong.exception.UserAlreadyExistsException;
import com.francis.taratulong.exception.UserNotFoundException;
import com.francis.taratulong.user.Role;
import com.francis.taratulong.user.organization.Org;
import com.francis.taratulong.user.organization.OrgService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AdminService {
    private final AdminRepository adminRepository;
    private final OrgService orgService;

    public AdminService(AdminRepository adminRepository, OrgService orgService) {
        this.orgService = orgService;
        this.adminRepository = adminRepository;
    }

    public Admin saveAdmin(Admin admin){
        Admin adminDB = adminRepository.findByEmail(admin.getEmail()).orElse(null);
        if(adminDB!=null&&!adminDB.isDeleted()){
            throw new UserAlreadyExistsException("Cannot create admin. User already exist", adminDB.getEmail());
        }
        admin.setRole(Role.ADMIN);
        return adminRepository.save(admin);
    }

    public Admin getAdmin(Long id) {
        return adminRepository.findById(id).orElseThrow(()-> new UserNotFoundException("User not found."));
    }

    public Admin updateAdmin(Long id, Admin admin){
        Admin adminDB = adminRepository.findById(id).orElseThrow(()-> new UserNotFoundException("Cannot update user. User not found."));
        adminDB.setName(admin.getName());
        return adminDB;
    }

    public Admin updateEmail(Long id, String email) {
        Admin admin = adminRepository.findById(id).orElseThrow(()-> new UserNotFoundException("User not found." + id));
        Admin userFoundThroughEmail = adminRepository.findByEmail(email).orElse(null);
        if(userFoundThroughEmail!=null && !userFoundThroughEmail.isDeleted()){
            throw new UserAlreadyExistsException("Email already in use.", userFoundThroughEmail.getEmail());
        } else {
            admin.setEmail(email);
            return admin;
        }
    }
    public void deleteAdmin(Long id) {
        Admin admin = adminRepository.findById(id).orElseThrow(()-> new UserNotFoundException("Cannot delete user. User not found."));
        admin.setDeleted(true);
    }

    public void approveOrg(Long adminID, Long orgID) {
        Org org = orgService.getOrg(orgID);
        org.setStatus(com.francis.taratulong.Status.APPROVED);
        org.setApprovedBy(getAdmin(adminID));
    }
}
