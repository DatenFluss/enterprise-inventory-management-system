package com.enterprise.inventorymanagement;

import com.enterprise.inventorymanagement.model.*;
import com.enterprise.inventorymanagement.model.request.ItemRequest;
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
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        // Clear repositories
        itemRequestRepository.deleteAll();
        inventoryItemRepository.deleteAll();
        userRepository.deleteAll();
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
        managerRole.setName(RoleName.MANAGER.name());
        managerRole.setPermissions(Set.of(deactivateNonPrivilegedUserPermission));
        roleRepository.save(managerRole);

        Role employeeRole = new Role();
        employeeRole.setName(RoleName.EMPLOYEE.name());
        employeeRole.setPermissions(Set.of(requestItemPermission, viewAvailableItemsPermission));
        roleRepository.save(employeeRole);

        // Create users
        User manager = new User();
        manager.setUsername("manager");
        manager.setPassword(passwordEncoder.encode("password"));
        manager.setEmail("manager@example.com");
        manager.setActive(true);
        manager.setRole(managerRole);
        userRepository.save(manager);

        User employee = new User();
        employee.setUsername("employee");
        employee.setPassword(passwordEncoder.encode("password"));
        employee.setEmail("employee@example.com");
        employee.setActive(true);
        employee.setRole(employeeRole);
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
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            userService.deactivateUser(anotherManager.getId());
        });

        assertEquals("Access Denied", exception.getMessage());
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
        inventoryItemRepository.save(item);

        // Act
        itemRequestService.requestItem(item.getId(), 2, "Need for new project");

        // Assert
        List<ItemRequest> requests = itemRequestRepository.findAll();
        assertEquals(1, requests.size(), "There should be one item request");

        ItemRequest request = requests.get(0);
        assertEquals(employee.getId(), request.getRequester().getId());
        assertEquals(item.getId(), request.getInventoryItem().getId());
        assertEquals(2, request.getQuantity());
        assertEquals(RequestStatus.PENDING, request.getStatus());
    }
}

