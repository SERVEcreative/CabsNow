package com.servecreative.WholeProject.DTO;

import jakarta.validation.constraints.NotNull;

public class PaymentConfirmRequest {
    @NotNull
    private Integer dutyId;

    public Integer getDutyId() { return dutyId; }
    public void setDutyId(Integer dutyId) { this.dutyId = dutyId; }
}
