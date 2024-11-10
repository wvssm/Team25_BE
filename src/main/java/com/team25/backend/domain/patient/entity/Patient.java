package com.team25.backend.domain.patient.entity;

import com.team25.backend.domain.patient.enumdomain.PatientGender;
import com.team25.backend.domain.reservation.entity.Reservation;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Table(name = "patient")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "patient_gender", nullable = false)
    @Enumerated(EnumType.STRING)
    private PatientGender gender;

    @Column(name = "patient_birth", nullable = false)
    private LocalDate birthDate;

    @Column(name = "nok_phone", nullable = false)
    private String nokPhone;

    @Column(name = "patient_relation", nullable = false)
    private String patientRelation;

    @ToString.Exclude
    @OneToOne(mappedBy = "patient")
    private Reservation reservation;


}