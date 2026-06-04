package com.fmcg.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "warehouses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type; // STORE / WAREHOUSE

    private String address;

    private BigDecimal lat;

    private BigDecimal lng;

    @Column
    private Integer capacity;

    @Column
    private Integer usedCapacity = 0;

    @Column
    private String shortLocation;

    @Column
    private String city;

    @Column
    private String state;

    @Column
    private String pincode;

    @Column
    private Integer staffCount;

    @Column
    private String operatingHours;

    @Column
    private String managerName;

    @Column
    private String contactNumber;

    @Builder.Default
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
