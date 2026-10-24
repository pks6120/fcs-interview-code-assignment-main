package com.fulfilment.application.monolith.warehouses.exceptions;

public class WarehouseBusinessException extends RuntimeException {

    public enum Reason { NOT_FOUND, CONFLICT, INVALID }

    private final Reason reason;

    public WarehouseBusinessException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
