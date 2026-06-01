package com.interviewprep.platform.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;
    private String fullName;
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<Role> roles;

    // Production default should usually be LAZY for collections.
    // Demo switch:
    // - LAZY: N+1 appears when service code touches user.getOrders() inside a loop.
    // - EAGER: Hibernate may load orders automatically while loading users, causing unexpected extra SQL.
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Order> orders = new LinkedHashSet<>();
}
