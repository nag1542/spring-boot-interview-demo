package com.interviewprep.platform.domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.Set;

@Entity @Table(name = "users") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(unique=true,nullable=false) private String email;
 @Column(nullable=false) private String password;
 private String fullName;
 @ElementCollection(fetch = FetchType.EAGER)
 @CollectionTable(name="user_roles", joinColumns=@JoinColumn(name="user_id"))
 @Enumerated(EnumType.STRING) @Column(name="role")
 private Set<Role> roles;
}
