DO $$
DECLARE
    owner_role_id bigint;
    enterprise_owner_role_id bigint;
BEGIN
    -- Get the IDs of both roles
    SELECT id INTO owner_role_id FROM roles WHERE name = 'ROLE_OWNER';
    SELECT id INTO enterprise_owner_role_id FROM roles WHERE name = 'ROLE_ENTERPRISE_OWNER';

    -- If both roles exist, migrate permissions and users from ROLE_OWNER to ROLE_ENTERPRISE_OWNER
    IF owner_role_id IS NOT NULL AND enterprise_owner_role_id IS NOT NULL THEN
        -- Migrate permissions
        INSERT INTO role_permissions (role_id, permission_id)
        SELECT enterprise_owner_role_id, permission_id
        FROM role_permissions
        WHERE role_id = owner_role_id
        AND permission_id NOT IN (
            SELECT permission_id 
            FROM role_permissions 
            WHERE role_id = enterprise_owner_role_id
        );

        -- Update users
        UPDATE users 
        SET role_id = enterprise_owner_role_id 
        WHERE role_id = owner_role_id;

        -- Delete old role permissions
        DELETE FROM role_permissions WHERE role_id = owner_role_id;

        -- Delete old role
        DELETE FROM roles WHERE id = owner_role_id;
    END IF;

    -- Update any remaining old role names if they exist
    UPDATE roles SET name = 'ROLE_ENTERPRISE_OWNER' WHERE name = 'OWNER';
    UPDATE roles SET name = 'ROLE_MANAGER' WHERE name = 'MANAGER';
    UPDATE roles SET name = 'ROLE_EMPLOYEE' WHERE name = 'EMPLOYEE';
    UPDATE roles SET name = 'ROLE_ADMIN' WHERE name = 'ADMIN';
    UPDATE roles SET name = 'ROLE_WAREHOUSE_OPERATOR' WHERE name = 'WAREHOUSE_OPERATOR';
    UPDATE roles SET name = 'ROLE_UNAFFILIATED' WHERE name = 'UNAFFILIATED';
END $$; 