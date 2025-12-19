package com.enterprise.inventorymanagement;

import com.enterprise.inventorymanagement.model.*;
import com.enterprise.inventorymanagement.model.Enterprise;
import com.enterprise.inventorymanagement.model.Warehouse;
import com.enterprise.inventorymanagement.model.Department;
import com.enterprise.inventorymanagement.model.dto.ItemRequestDTO;
import com.enterprise.inventorymanagement.model.dto.RequestItemDTO;
import com.enterprise.inventorymanagement.model.request.RequestStatus;
import com.enterprise.inventorymanagement.repository.*;
import com.enterprise.inventorymanagement.service.ItemRequestService;
import com.enterprise.inventorymanagement.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private EnterpriseRepository enterpriseRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Enterprise enterprise;
    private Warehouse warehouse;
    private Department department;

    @BeforeEach
    void setUp() {
        // Clear repositories
        itemRequestRepository.deleteAll();
        inventoryItemRepository.deleteAll();
        userRepository.deleteAll();
        departmentRepository.deleteAll();
        warehouseRepository.deleteAll();
        enterpriseRepository.deleteAll();
        roleRepository.deleteAll();
        permissionRepository.deleteAll();

        // Create permissions
        Permission deactivateUserPermission = new Permission("DEACTIVATE_USER");
        Permission deactivateNonPrivilegedUserPermission = new Permission("DEACTIVATE_NON_PRIVILEGED_USER");
        Permission requestItemPermission = new Permission("REQUEST_ITEM");
        Permission viewAvailableItemsPermission = new Permission("VIEW_AVAILABLE_ITEMS");
        permissionRepository.save(deactivateUserPermission);
        permissionRepository.save(deactivateNonPrivilegedUserPermission);
        permissionRepository.save(requestItemPermission);
        permissionRepository.save(viewAvailableItemsPermission);

        // Create roles
        Role managerRole = new Role();
        managerRole.setName(RoleName.ROLE_MANAGER);
        managerRole.setPermissions(Set.of(deactivateNonPrivilegedUserPermission));
        roleRepository.save(managerRole);

        Role employeeRole = new Role();
        employeeRole.setName(RoleName.ROLE_EMPLOYEE);
        employeeRole.setPermissions(Set.of(requestItemPermission, viewAvailableItemsPermission));
        roleRepository.save(employeeRole);

        enterprise = new Enterprise();
        enterprise.setName("Test Enterprise");
        enterprise.setAddress("123 Test");
        enterprise.setContactEmail("contact@test.com");
        enterpriseRepository.save(enterprise);

        warehouse = new Warehouse();
        warehouse.setName("Main Warehouse");
        warehouse.setLocation("HQ");
        warehouse.setEnterprise(enterprise);
        warehouseRepository.save(warehouse);

        department = new Department();
        department.setName("IT");
        department.setEnterprise(enterprise);
        departmentRepository.save(department);

        // Create users
        User manager = new User();
        manager.setUsername("manager");
        manager.setPassword(passwordEncoder.encode("password"));
        manager.setEmail("manager@example.com");
        manager.setActive(true);
        manager.setRole(managerRole);
        manager.setEnterprise(enterprise);
        userRepository.save(manager);

        User employee = new User();
        employee.setUsername("employee");
        employee.setPassword(passwordEncoder.encode("password"));
        employee.setEmail("employee@example.com");
        employee.setActive(true);
        employee.setRole(employeeRole);
        employee.setEnterprise(enterprise);
        employee.setDepartment(department);
        userRepository.save(employee);

        // Authenticate as manager by default
        authenticateAs(manager);
    }

    private void authenticateAs(User user) {
        List<GrantedAuthority> authorities = user.getRole().getPermissions().stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getName()))
                .collect(Collectors.toList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getUsername(), null, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }


    @Test
    void testDeactivateUser_NonPrivilegedUserByManager_Success() {
        // Arrange
        User employee = userRepository.findByUsername("employee").orElseThrow();

        // Act
        userService.deactivateUser(employee.getId());

        // Assert
        User deactivatedEmployee = userRepository.findById(employee.getId()).orElseThrow();
        assertFalse(deactivatedEmployee.getActive(), "Employee should be deactivated");
    }

    @Test
    void testDeactivateUser_PrivilegedUserByManager_Failure() {
        // Arrange
        User manager = userRepository.findByUsername("manager").orElseThrow();

        User anotherManager = new User();
        anotherManager.setUsername("anotherManager");
        anotherManager.setPassword(passwordEncoder.encode("password"));
        anotherManager.setEmail("another.manager@example.com");
        anotherManager.setActive(true);
        anotherManager.setRole(manager.getRole());
        userRepository.save(anotherManager);

        // Act & Assert
        assertThrows(AccessDeniedException.class, () -> userService.deactivateUser(anotherManager.getId()));
    }

    @Test
    void testRequestItem_Success() {
        // Arrange
        User employee = userRepository.findByUsername("employee").orElseThrow();
        authenticateAs(employee);

        InventoryItem item = new InventoryItem();
        item.setName("Laptop");
        item.setDescription("Dell XPS 13");
        item.setQuantity(10);
        item.setEnterprise(enterprise);
        item.setWarehouse(warehouse);
        inventoryItemRepository.save(item);

        ItemRequestDTO requestDTO = ItemRequestDTO.builder()
                .warehouseId(warehouse.getId())
                .departmentId(department.getId())
                .comments("Need for new project")
                .requestItems(List.of(RequestItemDTO.builder()
                        .itemId(item.getId())
                        .quantity(2)
                        .build()))
                .build();

        // Act
        ItemRequestDTO result = itemRequestService.createItemRequest(employee.getId(), requestDTO);

        // Assert
        assertEquals(employee.getId(), result.getRequesterId());
        assertEquals(RequestStatus.PENDING, result.getStatus());
        assertEquals(1, result.getRequestItems().size());
        RequestItemDTO createdItem = result.getRequestItems().get(0);
        assertEquals(item.getId(), createdItem.getItemId());
        assertEquals(2, createdItem.getQuantity());
    }
}
