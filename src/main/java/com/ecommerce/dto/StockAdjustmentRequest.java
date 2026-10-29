package com.ecommerce.dto;

import com.ecommerce.entity.MovementType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class StockAdjustmentRequest {

    @NotNull(message = "Quantity change is required")
    private Integer quantityChange;

    @NotNull(message = "Movement type is required")
    private MovementType movementType = MovementType.RESTOCK;

    @NotBlank(message = "Reason is required")
    private String reason;

    public StockAdjustmentRequest() {
    }

    public StockAdjustmentRequest(Integer quantityChange, MovementType movementType, String reason) {
        this.quantityChange = quantityChange;
        this.movementType = movementType;
        this.reason = reason;
    }

    public Integer getQuantityChange() {
        return quantityChange;
    }

    public void setQuantityChange(Integer quantityChange) {
        this.quantityChange = quantityChange;
    }

    public MovementType getMovementType() {
        return movementType;
    }

    public void setMovementType(MovementType movementType) {
        this.movementType = movementType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
