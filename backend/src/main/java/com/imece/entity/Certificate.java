package com.imece.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Immutable record: once issued, a certificate is never edited, so it has no setters.
@Entity
@Table(name = "certificates")
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // unique = true enforces "one certificate per application" in the database itself.
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false, unique = true)
    private Application application;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    // Deliberately denormalized: the total at issue time. Hour entry closes once a
    // certificate exists, so this value cannot drift from the hour logs.
    @Column(nullable = false, precision = 5, scale = 1)
    private BigDecimal totalHours;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime issuedAt;

    protected Certificate() {
        // Required by JPA.
    }

    public Certificate(Application application, String code, BigDecimal totalHours) {
        this.application = application;
        this.code = code;
        this.totalHours = totalHours;
    }

    public Long getId() {
        return id;
    }

    public Application getApplication() {
        return application;
    }

    public String getCode() {
        return code;
    }

    public BigDecimal getTotalHours() {
        return totalHours;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }
}
