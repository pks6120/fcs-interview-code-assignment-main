package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.exceptions.WarehouseBusinessException;
import com.fulfilment.application.monolith.warehouses.exceptions.WarehouseBusinessException.Reason;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;

@ApplicationScoped
public class WarehouseLocationValidator {

    private final LocationResolver locationResolver;
    private final WarehouseStore warehouseStore;

    public WarehouseLocationValidator(LocationResolver locationResolver, WarehouseStore warehouseStore) {
        this.locationResolver = locationResolver;
        this.warehouseStore = warehouseStore;
    }


    public void validate(Warehouse candidate, Warehouse replacing) {
        if (candidate.businessUnitCode == null || candidate.businessUnitCode.isBlank()) {
            throw new WarehouseBusinessException(Reason.INVALID, "Business unit code is required.");
        }
        if (candidate.location == null || candidate.location.isBlank()) {
            throw new WarehouseBusinessException(Reason.INVALID, "Location is required.");
        }
        if (candidate.capacity == null || candidate.capacity <= 0) {
            throw new WarehouseBusinessException(Reason.INVALID, "Capacity must be greater than zero.");
        }
        if (candidate.stock == null || candidate.stock < 0) {
            throw new WarehouseBusinessException(Reason.INVALID, "Stock must be zero or more.");
        }
        if (candidate.stock > candidate.capacity) {
            throw new WarehouseBusinessException(Reason.INVALID, "Stock cannot exceed the warehouse capacity.");
        }

        Location location = locationResolver.resolveByIdentifier(candidate.location);
        if (location == null) {
            throw new WarehouseBusinessException(
                    Reason.INVALID, "Location '" + candidate.location + "' does not exist.");
        }

        List<Warehouse> activeAtLocation =
                warehouseStore.getAll().stream()
                        .filter(w -> w.archivedAt == null)
                        .filter(w -> location.identification.equalsIgnoreCase(w.location))
                        .filter(w -> replacing == null || !w.businessUnitCode.equals(replacing.businessUnitCode))
                        .toList();

        if (activeAtLocation.size() >= location.maxNumberOfWarehouses) {
            throw new WarehouseBusinessException(
                    Reason.CONFLICT,
                    "Maximum number of warehouses (" + location.maxNumberOfWarehouses
                            + ") reached for location " + location.identification + ".");
        }

        int usedCapacity = activeAtLocation.stream().mapToInt(w -> w.capacity).sum();
        if (usedCapacity + candidate.capacity > location.maxCapacity) {
            throw new WarehouseBusinessException(
                    Reason.INVALID,
                    "Capacity exceeds the maximum of " + location.maxCapacity + " for location "
                            + location.identification + " (already used: " + usedCapacity + ").");
        }
    }
}
