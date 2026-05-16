package com.interviewprep.platform.domain;
import jakarta.persistence.*; import lombok.*; import java.math.BigDecimal; import java.time.Instant;
@Entity @Table(name="orders") @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Order { @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id; private String customerEmail; private BigDecimal totalAmount; private Instant createdAt; }
