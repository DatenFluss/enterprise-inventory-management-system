package com.enterprise.inventorymanagement;

import com.enterprise.inventorymanagement.exceptions.ResourceNotFoundException;
import com.enterprise.inventorymanagement.model.*;
import com.enterprise.inventorymanagement.model.dto.EnterpriseDTO;
import com.enterprise.inventorymanagement.model.request.EnterpriseRegistrationRequest;
import com.enterprise.inventorymanagement.repository.*;
import com.enterprise.inventorymanagement.service.EnterpriseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithSecurityContextTestExecutionListener;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@TestExecutionListeners(
        listeners = {WithSecurityContextTestExecutionListener.class},
        mergeMode = TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS
)
public class EnterpriseServiceTest {

    @Autowired
    private EnterpriseService enterpriseService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private EnterpriseRepository enterpriseRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // Clear repositories
        userRepository.deleteAll();
        roleRepository.deleteAll();
        permissionRepository.deleteAll();
        enterpriseRepository.deleteAll();

        // Create permissions
        Permission manageEnterprisePermission = new Permission("MANAGE_ENTERPRISE");
        Permission viewEnterprisePermission = new Permission("VIEW_ENTERPRISE");
        permissionRepository.saveAll(List.of(manageEnterprisePermission, viewEnterprisePermission));

        // Create roles
        Role adminRole = new Role();
        adminRole.setName(RoleName.ADMIN.name());
        adminRole.setPermissions(Set.of(manageEnterprisePermission, viewEnterprisePermission));
        roleRepository.save(adminRole);

        Role ownerRole = new Role();
        ownerRole.setName(RoleName.OWNER.name());
        ownerRole.setPermissions(Set.of(manageEnterprisePermission, viewEnterprisePermission));
        roleRepository.save(ownerRole);

        Role employeeRole = new Role();
        employeeRole.setName(RoleName.EMPLOYEE.name());
        employeeRole.setPermissions(Set.of(viewEnterprisePermission));
        roleRepository.save(employeeRole);

        // Create users
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("password"));
        admin.setEmail("admin@example.com");
        admin.setActive(true);
        admin.setRole(adminRole);
        userRepository.save(admin);

        // **Create and save the employee user**
        User employee = new User();
        employee.setUsername("employee");
        employee.setPassword(passwordEncoder.encode("password"));
        employee.setEmail("employee@example.com");
        employee.setActive(true);
        employee.setRole(employeeRole);
        userRepository.save(employee);

        // Authenticate as admin by default
        authenticateAs(admin);
    }


    private void authenticateAs(User user) {
        List<SimpleGrantedAuthority> authorities = user.getRole().getPermissions().stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getName()))
                .collect(Collectors.toList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getUsername(), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void testRegisterEnterprise_Success() {
        // Arrange
        EnterpriseRegistrationRequest request = new EnterpriseRegistrationRequest();
        request.setEnterpriseName("TechCorp");
        request.setAddress("123 Tech Street");
        request.setContactEmail("contact@techcorp.com");
        request.setOwnerUsername("owner");
        request.setOwnerPassword("ownerpass");
        request.setOwnerEmail("owner@techcorp.com");

        // Act
        enterpriseService.registerEnterprise(request);

        // Assert
        List<Enterprise> enterprises = enterpriseRepository.findAll();
        assertEquals(1, enterprises.size(), "There should be one enterprise registered");

        Enterprise enterprise = enterprises.get(0);
        assertEquals("TechCorp", enterprise.getName());

        User owner = userRepository.findByUsername("owner").orElseThrow();
        assertEquals("owner@techcorp.com", owner.getEmail());
        assertEquals(RoleName.OWNER, owner.getRole().getName());
        assertEquals(enterprise.getId(), owner.getEnterprise().getId());
    }

    @Test
    void testGetEnterpriseById_Success() throws ResourceNotFoundException {
        // Arrange
        Enterprise enterprise = new Enterprise();
        enterprise.setName("TechCorp");
        enterprise.setAddress("123 Tech Street");
        enterprise.setContactEmail("contact@techcorp.com");
        enterpriseRepository.save(enterprise);

        // Act
        EnterpriseDTO enterpriseDTO = enterpriseService.getEnterpriseById(enterprise.getId());

        // Assert
        assertEquals("TechCorp", enterpriseDTO.getName());
        assertEquals("123 Tech Street", enterpriseDTO.getAddress());
        assertEquals("contact@techcorp.com", enterpriseDTO.getContactEmail());
    }

    @Test
    void testUpdateEnterprise_Success() throws ResourceNotFoundException {
        // Arrange
        Enterprise enterprise = new Enterprise();
        enterprise.setName("TechCorp");
        enterprise.setAddress("123 Tech Street");
        enterprise.setContactEmail("contact@techcorp.com");
        enterpriseRepository.save(enterprise);

        EnterpriseDTO updatedEnterprise = new EnterpriseDTO();
        updatedEnterprise.setName("TechCorp International");
        updatedEnterprise.setAddress("456 Innovation Drive");
        updatedEnterprise.setContactEmail("contact@techcorpintl.com");

        // Act
        EnterpriseDTO result = enterpriseService.updateEnterprise(enterprise.getId(), updatedEnterprise);

        // Assert
        assertEquals("TechCorp International", result.getName());
        assertEquals("456 Innovation Drive", result.getAddress());
        assertEquals("contact@techcorpintl.com", result.getContactEmail());
    }

    @Test
    void testDeleteEnterprise_Success() throws ResourceNotFoundException {
        // Arrange
        Enterprise enterprise = new Enterprise();
        enterprise.setName("TechCorp");
        enterprise.setAddress("123 Tech Street");
        enterprise.setContactEmail("contact@techcorp.com");
        enterpriseRepository.save(enterprise);

        // Act
        enterpriseService.deleteEnterprise(enterprise.getId());

        // Assert
        boolean exists = enterpriseRepository.existsById(enterprise.getId());
        assertFalse(exists, "Enterprise should be deleted");
    }

    @Test
    void testAddEmployeeToEnterprise_Success() throws ResourceNotFoundException {
        // Arrange
        Enterprise enterprise = new Enterprise();
        enterprise.setName("TechCorp");
        enterprise.setAddress("123 Tech Street");
        enterprise.setContactEmail("contact@techcorp.com");
        enterpriseRepository.save(enterprise);

        User employee = new User();
        employee.setUsername("employee1"); // Use unique username
        employee.setPassword(passwordEncoder.encode("password"));
        employee.setEmail("employee1@example.com");
        employee.setActive(true);
        employee.setRole(roleRepository.findByName(RoleName.EMPLOYEE.name()).orElseThrow());
        userRepository.save(employee);

        // Act
        enterpriseService.addEmployeeToEnterprise(enterprise.getId(), employee.getId());

        // Assert
        User updatedEmployee = userRepository.findById(employee.getId()).orElseThrow();
        assertEquals(enterprise.getId(), updatedEmployee.getEnterprise().getId());
    }

    @Test
    void testRemoveEmployeeFromEnterprise_Success() throws ResourceNotFoundException {
        // Arrange
        Enterprise enterprise = new Enterprise();
        enterprise.setName("TechCorp");
        enterprise.setAddress("123 Tech Street");
        enterprise.setContactEmail("contact@techcorp.com");
        enterpriseRepository.save(enterprise);

        User employee = new User();
        employee.setUsername("employee2"); // Use unique username
        employee.setPassword(passwordEncoder.encode("password"));
        employee.setEmail("employee2@example.com");
        employee.setActive(true);
        employee.setRole(roleRepository.findByName(RoleName.EMPLOYEE.name()).orElseThrow());
        employee.setEnterprise(enterprise);
        userRepository.save(employee);

        // Act
        enterpriseService.removeEmployeeFromEnterprise(enterprise.getId(), employee.getId());

        // Assert
        User updatedEmployee = userRepository.findById(employee.getId()).orElseThrow();
        assertNull(updatedEmployee.getEnterprise(), "Employee should no longer be associated with any enterprise");
    }

    @Test
    void testRegisterEnterprise_MissingData_Failure() {
        // Arrange
        EnterpriseRegistrationRequest request = new EnterpriseRegistrationRequest();
        // Missing enterprise name and owner username

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            enterpriseService.registerEnterprise(request);
        });
    }

    @Test
    void testDeleteEnterprise_Unauthorized_Failure() {
        // Arrange
        Enterprise enterprise = new Enterprise();
        enterprise.setName("TechCorp");
        enterprise.setAddress("123 Tech Street");
        enterprise.setContactEmail("contact@techcorp.com");
        enterpriseRepository.save(enterprise);

        User employee = userRepository.findByUsername("employee").orElseThrow();
        authenticateAs(employee);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> {
            enterpriseService.deleteEnterprise(enterprise.getId());
        });
    }

}
