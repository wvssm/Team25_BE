package com.team25.backend.domain.patient.dto.response;

import com.team25.backend.domain.patient.enumdomain.PatientGender;

public record PatientResponse(
    String name,
    String phoneNumber,
    PatientGender patientGender,
    String patientRelation,
    String birthDate,
    String nokPhone
) {

}
