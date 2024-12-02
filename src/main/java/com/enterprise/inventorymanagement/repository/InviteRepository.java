package com.enterprise.inventorymanagement.repository;

import com.enterprise.inventorymanagement.model.EnterpriseInvite;
import com.enterprise.inventorymanagement.model.InviteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InviteRepository extends JpaRepository<EnterpriseInvite, Long> {
    List<EnterpriseInvite> findByEmailAndStatus(String email, InviteStatus status);
    boolean existsByEmailAndEnterpriseIdAndStatus(String email, Long enterpriseId, InviteStatus status);
    List<EnterpriseInvite> findByEmail(String email);
}
