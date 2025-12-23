package com.secureauth.productservice.entity;

import com.secureauth.productservice.entity.CalibrationHistory;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "calibration_media")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CalibrationMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calibration_history_id", nullable = false)
    private CalibrationHistory calibrationHistory;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String fileType; // document, image, video

    @Column(nullable = false)
    private String mimeType;

    @Column(columnDefinition = "BYTEA", nullable = false)
    private byte[] fileData;

    private String description;

    private Long fileSize;

    @Column(nullable = false)
    private LocalDate uploadedAt;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDate.now();
    }
}
