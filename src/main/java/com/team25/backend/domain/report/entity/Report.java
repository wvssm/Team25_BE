package com.team25.backend.domain.report.entity;

import com.team25.backend.domain.reservation.entity.Reservation;
import com.team25.backend.domain.report.enumdomain.MedicineTime;
import jakarta.annotation.Nullable;
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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reports")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id",nullable = false)
    private Reservation reservation;

    @Column(name = "doctor_summary",nullable = false)
    private String doctorSummary;

    @Column(name = "frequency",nullable = false)
    private int frequency;

    @Column(name = "meal_time",nullable = false)
    @Enumerated(EnumType.STRING)
    private MedicineTime medicineTime;

    @Column(name = "time_of_day",nullable = false)
    private String timeOfDay;
}