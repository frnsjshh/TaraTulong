package com.francis.taratulong.user.organization;

import com.francis.taratulong.exception.UserAlreadyExistsException;
import com.francis.taratulong.exception.UserNotFoundException;
import com.francis.taratulong.user.Role;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class OrgService {
    private final OrgRepository orgRepository;
    public OrgService(OrgRepository orgRepository) {
        this.orgRepository = orgRepository;
    }

    public Org saveOrg(Org org) {
        Org dbFound = orgRepository.findByEmail(org.getEmail()).orElse(null);
        if(dbFound!=null&&!dbFound.isDeleted()) {
            throw  new UserAlreadyExistsException("Cannot create account. Email already in use", org.getEmail());
        }
        org.setRole(Role.ORG);
        return orgRepository.save(org);
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
    }

    public boolean orgExist(Long id) {
        return orgRepository.existsById(id);
    }
}
