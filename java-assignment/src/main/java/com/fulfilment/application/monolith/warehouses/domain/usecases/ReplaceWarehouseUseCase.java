package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.exceptions.WarehouseBusinessException;
import com.fulfilment.application.monolith.warehouses.exceptions.WarehouseBusinessException.Reason;

import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final WarehouseLocationValidator validator;

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore, WarehouseLocationValidator validator) {

    this.warehouseStore = warehouseStore;
    this.validator = validator;
  }


  @Override
  public void replace(Warehouse newWarehouse) {
    // 1. There must be an active warehouse with this code to replace
    Warehouse current = warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (current == null) {
      throw new WarehouseBusinessException(
              Reason.NOT_FOUND,
              "No active warehouse with business unit code '" + newWarehouse.businessUnitCode + "'.");
    }

    // 2. Same location / capacity / stock validations as creation
    validator.validate(newWarehouse, current);

    // 3. Replacement-specific rules
    if (newWarehouse.capacity < current.stock) {
      throw new WarehouseBusinessException(
              Reason.INVALID, "New warehouse capacity cannot accommodate the stock of the replaced warehouse.");
    }
    if (!current.stock.equals(newWarehouse.stock)) {
      throw new WarehouseBusinessException(
              Reason.INVALID, "Stock of the new warehouse must match the stock of the replaced warehouse.");
    }

    // 4. Archive the old one, then create the new one with the SAME business unit code
    LocalDateTime now = LocalDateTime.now();
    current.archivedAt = now;
    warehouseStore.update(current);

    newWarehouse.createdAt = now;
    newWarehouse.archivedAt = null;
    warehouseStore.create(newWarehouse);
  }
}
