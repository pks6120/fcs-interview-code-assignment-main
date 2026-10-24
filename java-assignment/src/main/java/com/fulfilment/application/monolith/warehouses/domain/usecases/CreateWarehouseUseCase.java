package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.exceptions.WarehouseBusinessException;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final WarehouseLocationValidator warehouseLocationValidator;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore, WarehouseLocationValidator warehouseLocationValidator) {
    this.warehouseStore = warehouseStore;
    this.warehouseLocationValidator = warehouseLocationValidator;
  }

  @Override
  public void create(Warehouse warehouse) {
    if (warehouse.businessUnitCode != null
            && warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode) != null) {
      throw new WarehouseBusinessException(
              WarehouseBusinessException.Reason.CONFLICT,
              "Warehouse with business unit code '" + warehouse.businessUnitCode + "' already exists.");
    }

    warehouseLocationValidator.validate(warehouse, null);

    warehouse.createdAt = LocalDateTime.now();
    warehouse.archivedAt = null;
    warehouseStore.create(warehouse);
  }
}
