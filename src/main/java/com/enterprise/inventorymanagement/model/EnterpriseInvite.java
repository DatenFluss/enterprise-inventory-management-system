package com.enterprise.inventorymanagement.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "enterprise_invites")
@Data
public class EnterpriseInvite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(name = "enterprise_id", nullable = false)
    private Long enterpriseId;

    @Column(name = "inviter_id", nullable = false)
    private Long inviterId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoleName role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InviteStatus status = InviteStatus.PENDING;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
