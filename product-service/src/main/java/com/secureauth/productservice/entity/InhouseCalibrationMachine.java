package com.secureauth.productservice.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "inhouse_calibration_machines")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InhouseCalibrationMachine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String machineName;

    @Column(nullable = false, unique = true)
    private String instrumentName;

    @Column(nullable = false, unique = true)
    private String instrumentCode;

    @Column(nullable = false)
    private String accuracy;

    @Column(nullable = false)
    private String resolution;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String manufacturer;

    @Column(nullable = false, unique = true)
    private String machineEquipmentNumber;

    @Column(nullable = false)
    private LocalDate guaranteeExpiryDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gage_type_id", nullable = false)
    private GageType gageType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gage_sub_type_id", nullable = false)
    private GageSubType gageSubType;

    // Bidirectional mapping to Gages
    @OneToMany(mappedBy = "inhouseCalibrationMachine", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Gage> gages = new HashSet<>();
}