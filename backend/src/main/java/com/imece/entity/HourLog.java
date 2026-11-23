package com.imece.entity;

import com.imece.entity.enums.HourLogStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Only application_id is stored; project and volunteer are reached through the
// application, so the same fact is never kept in two places (3NF).
@Entity
@Table(name = "hour_logs")
public class HourLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Column(nullable = false)
    private LocalDate workDate;

    // BigDecimal instead of double: 0.5-hour steps must add up exactly in totals.
    @Column(nullable = false, precision = 4, scale = 1)
    private BigDecimal hours;

    private String note;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HourLogStatus status = HourLogStatus.PENDING;

    private String rejectionReason;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime decidedAt;

    protected HourLog() {
        // Required by JPA.
    }

    public HourLog(Application application, LocalDate workDate, BigDecimal hours, String note) {
        this.application = application;
        this.workDate = workDate;
        this.hours = hours;
        this.note = note;
    }

    public Long getId() {
        return id;
    }

    public Application getApplication() {
        return application;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public void setWorkDate(LocalDate workDate) {
        this.workDate = workDate;
    }

    public BigDecimal getHours() {
        return hours;
    }

    public void setHours(BigDecimal hours) {
        this.hours = hours;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public HourLogStatus getStatus() {
        return status;
    }

    public void setStatus(HourLogStatus status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(LocalDateTime decidedAt) {
        this.decidedAt = decidedAt;
    }
}
