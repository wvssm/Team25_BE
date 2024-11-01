package com.team25.backend.dto.request;

import com.team25.backend.enumdomain.MedicineTime;

public record ReportSearchRequest(
    MedicineTime medicineTime
    ) {
}
