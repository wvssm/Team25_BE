package com.team25.backend.dto.request;

import com.team25.backend.enumdomain.MedicineTime;

public record ReportRequest(
    String doctorSummary,
    int frequency,
    MedicineTime medicineTime,
    String timeOfDays) {
}
