package com.enterprise.inventorymanagement.repository;

import com.enterprise.inventorymanagement.model.EnterpriseInvite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InviteRepository extends JpaRepository<EnterpriseInvite, Long> {
    List<EnterpriseInvite> findByUserId(Long userId);
    List<EnterpriseInvite> findByEnterpriseId(Long enterpriseId);
    Optional<EnterpriseInvite> findByUserIdAndEnterpriseId(Long userId, Long enterpriseId);
    boolean existsByUserIdAndEnterpriseId(Long userId, Long enterpriseId);
    void deleteByUserIdAndEnterpriseId(Long userId, Long enterpriseId);
}
