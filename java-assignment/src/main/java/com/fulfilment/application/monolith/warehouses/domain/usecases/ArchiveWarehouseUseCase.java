package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.exceptions.WarehouseBusinessException;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDateTime;

@ApplicationScoped
public class ArchiveWarehouseUseCase implements ArchiveWarehouseOperation {

  private final WarehouseStore warehouseStore;

  public ArchiveWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  public void archive(Warehouse warehouse) {
    Warehouse current = warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode);
    if (current == null) {
      throw new WarehouseBusinessException(
              WarehouseBusinessException.Reason.NOT_FOUND,
              "No active warehouse with business unit code '" + warehouse.businessUnitCode + "'.");
    }
    current.archivedAt = LocalDateTime.now();
    warehouseStore.update(current);
  }
}
