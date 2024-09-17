package com.enterprise.inventorymanagemet.repository;

import com.enterprise.inventorymanagemet.model.Enterprise;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnterpriseRepository extends JpaRepository<Enterprise, Long> {
}
