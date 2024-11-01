package com.enterprise.inventorymanagement;

import com.enterprise.inventorymanagement.exceptions.ResourceNotFoundException;
import com.enterprise.inventorymanagement.model.*;
import com.enterprise.inventorymanagement.model.request.ItemRequest;
import com.enterprise.inventorymanagement.model.request.RequestStatus;
import com.enterprise.inventorymanagement.repository.*;
import com.enterprise.inventorymanagement.service.InventoryItemService;
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
public class InventoryItemServiceTest {
    @Autowired
    private InventoryItemService inventoryItemService;

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
        inventoryItemRepository.deleteAll();
        userRepository.deleteAll();
        roleRepository.deleteAll();
        permissionRepository.deleteAll();

        // Create permissions
        Permission createItemPermission = new Permission("CREATE_ITEM");
        Permission updateItemPermission = new Permission("UPDATE_ITEM");
        Permission deleteItemPermission = new Permission("DELETE_ITEM");
        Permission viewItemsPermission = new Permission("VIEW_ITEMS");
        Permission requestItemPermission = new Permission("REQUEST_ITEM");
        Permission viewAvailableItemsPermission = new Permission("VIEW_AVAILABLE_ITEMS");
        permissionRepository.saveAll(List.of(
                createItemPermission, updateItemPermission, deleteItemPermission,
                viewItemsPermission, requestItemPermission, viewAvailableItemsPermission
        ));

        // Create roles
        Role adminRole = new Role();
        adminRole.setName(RoleName.ADMIN.label);
        adminRole.setPermissions(Set.of(
                createItemPermission, updateItemPermission, deleteItemPermission, viewItemsPermission
        ));
        roleRepository.save(adminRole);

        Role managerRole = new Role();
        managerRole.setName(RoleName.MANAGER.label);
        managerRole.setPermissions(Set.of(updateItemPermission, viewItemsPermission));
        roleRepository.save(managerRole);

        Role employeeRole = new Role();
        employeeRole.setName(RoleName.EMPLOYEE.label);
        employeeRole.setPermissions(Set.of(viewItemsPermission, requestItemPermission, viewAvailableItemsPermission));
        roleRepository.save(employeeRole);

        // Create users
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("password"));
        admin.setEmail("admin@example.com");
        admin.setActive(true);
        admin.setRole(adminRole);
        userRepository.save(admin);

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

        // Create inventory items
        InventoryItem item1 = new InventoryItem();
        item1.setName("Laptop");
        item1.setDescription("Dell XPS 13");
        item1.setQuantity(10);
        inventoryItemRepository.save(item1);

        InventoryItem item2 = new InventoryItem();
        item2.setName("Monitor");
        item2.setDescription("Dell 24-inch");
        item2.setQuantity(15);
        inventoryItemRepository.save(item2);

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
    void testCreateInventoryItem_Admin_Success() {
        // Arrange
        User admin = userRepository.findByUsername("admin").orElseThrow();
        authenticateAs(admin);

        InventoryItem newItem = new InventoryItem();
        newItem.setName("Keyboard");
        newItem.setDescription("Mechanical Keyboard");
        newItem.setQuantity(20);

        // Act
        InventoryItem createdItem = inventoryItemService.saveItem(newItem);

        // Assert
        assertNotNull(createdItem.getId(), "Created item should have an ID");
        assertEquals("Keyboard", createdItem.getName());
        assertEquals(20, createdItem.getQuantity());
    }

    @Test
    void testGetItemById_Success() {
        // Arrange
        InventoryItem item = inventoryItemRepository.findByName("Laptop").orElseThrow();

        // Act
        InventoryItem foundItem = inventoryItemService.getItemById(item.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Item not found"));

        // Assert
        assertEquals(item.getId(), foundItem.getId());
        assertEquals("Laptop", foundItem.getName());
    }

    @Test
    void testGetItemById_NotFound() {
        // Arrange
        Long nonExistentId = 999L;

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            inventoryItemService.getItemById(nonExistentId)
                    .orElseThrow(() -> new ResourceNotFoundException("Item not found"));
        });
    }

    @Test
    void testUpdateItem_Manager_Success() {
        // Arrange
        User manager = userRepository.findByUsername("manager").orElseThrow();
        authenticateAs(manager);

        InventoryItem item = inventoryItemRepository.findByName("Monitor").orElseThrow();
        InventoryItem updatedItemData = new InventoryItem();
        updatedItemData.setName(item.getName());
        updatedItemData.setDescription(item.getDescription());
        updatedItemData.setQuantity(20); // Update quantity

        // Act
        InventoryItem updatedItem = inventoryItemService.updateItem(item.getId(), updatedItemData);

        // Assert
        assertEquals(20, updatedItem.getQuantity());
    }

    @Test
    void testUpdateItem_Employee_Failure() {
        // Arrange
        User employee = userRepository.findByUsername("employee").orElseThrow();
        authenticateAs(employee);

        InventoryItem item = inventoryItemRepository.findByName("Monitor").orElseThrow();
        InventoryItem updatedItemData = new InventoryItem();
        updatedItemData.setName(item.getName());
        updatedItemData.setDescription(item.getDescription());
        updatedItemData.setQuantity(25);

        // Act & Assert
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () -> {
            inventoryItemService.updateItem(item.getId(), updatedItemData);
        });
        assertEquals("Access Denied", exception.getMessage());
    }

    @Test
    void testDeleteItem_Admin_Success() {
        // Arrange
        User admin = userRepository.findByUsername("admin").orElseThrow();
        authenticateAs(admin);

        InventoryItem item = inventoryItemRepository.findByName("Laptop").orElseThrow();

        // Act
        inventoryItemService.deleteItem(item.getId());

        // Assert
        assertFalse(inventoryItemRepository.existsById(item.getId()), "Item should be deleted");
    }

    @Test
    void testListInventoryItems_Employee_Success() {
        // Arrange
        User employee = userRepository.findByUsername("employee").orElseThrow();
        authenticateAs(employee);

        // Act
        List<InventoryItem> items = inventoryItemService.getAllItems();

        // Assert
        assertEquals(2, items.size(), "There should be two inventory items");
        List<String> itemNames = items.stream().map(InventoryItem::getName).toList();
        assertTrue(itemNames.contains("Laptop"));
        assertTrue(itemNames.contains("Monitor"));
    }

    @Test
    void testCreateInventoryItem_DuplicateName_Failure() {
        // Arrange
        User admin = userRepository.findByUsername("admin").orElseThrow();
        authenticateAs(admin);

        InventoryItem newItem = new InventoryItem();
        newItem.setName("Laptop"); // Duplicate name
        newItem.setDescription("HP Spectre");
        newItem.setQuantity(5);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            inventoryItemService.saveItem(newItem);
        });
        assertEquals("An item with this name already exists.", exception.getMessage());
    }

    @Test
    void testRequestItem_Employee_Success() {
        // Arrange
        User employee = userRepository.findByUsername("employee").orElseThrow();
        authenticateAs(employee);

        InventoryItem item = inventoryItemRepository.findByName("Laptop").orElseThrow();

        // Act
        inventoryItemService.requestItem(item.getId(), 2, "Need for project");

        // Assert
        // Fetch the item requests and verify
        List<ItemRequest> requests = itemRequestRepository.findAll();
        assertEquals(1, requests.size(), "There should be one item request");

        ItemRequest request = requests.get(0);
        assertEquals(employee.getId(), request.getRequester().getId());
        assertEquals(item.getId(), request.getInventoryItem().getId());
        assertEquals(2, request.getQuantity());
        assertEquals(RequestStatus.PENDING, request.getStatus());
    }

    @Test
    void testViewAvailableItems_Employee_Success() {
        // Arrange
        User employee = userRepository.findByUsername("employee").orElseThrow();
        authenticateAs(employee);

        // Act
        List<InventoryItem> availableItems = inventoryItemService.viewAvailableItems();

        // Assert
        assertFalse(availableItems.isEmpty(), "Available items should not be empty");
        assertTrue(availableItems.stream().allMatch(item -> item.getQuantity() > 0), "All items should have quantity greater than zero");
    }

}
