package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
//import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.exceptions.WarehouseBusinessException;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  @Inject private WarehouseRepository warehouseRepository;
  @Inject
  CreateWarehouseOperation createWarehouseOperation;
  @Inject
  ReplaceWarehouseOperation replaceWarehouseOperation;
  @Inject
  ArchiveWarehouseOperation archiveWarehouseOperation;

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    return warehouseRepository.getAll()
            .stream()
            .filter(w -> w.archivedAt == null)
            .map(this::toWarehouseResponse)
            .toList();

  }

  @Override
  @Transactional
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    var domain = toDomain(data);
    createWarehouseOperation.create(domain);
    return toWarehouseResponse(domain);
  }

  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
    var found = warehouseRepository.findByBusinessUnitCode(id);
    if (found == null) {
      throw new WarehouseBusinessException(WarehouseBusinessException.Reason.NOT_FOUND, "Warehouse '" + id + "' not found.");
    }
    return toWarehouseResponse(found);
  }

  @Override
  @Transactional
  public void archiveAWarehouseUnitByID(String id) {

    var domain = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    domain.businessUnitCode = id;
    archiveWarehouseOperation.archive(domain);
  }

  @Override
  @Transactional
  public Warehouse replaceTheCurrentActiveWarehouse(
      String businessUnitCode, @NotNull Warehouse data) {
    var domain = toDomain(data);
    domain.businessUnitCode = businessUnitCode; // the URL decides which warehouse is replaced
    replaceWarehouseOperation.replace(domain);
    return toWarehouseResponse(domain);
  }
  private com.fulfilment.application.monolith.warehouses.domain.models.Warehouse toDomain(Warehouse data) {
    var domain = new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    domain.businessUnitCode = data.getBusinessUnitCode();
    domain.location = data.getLocation();
    domain.capacity = data.getCapacity();
    domain.stock = data.getStock();
    return domain;
  }

  private Warehouse toWarehouseResponse(
      com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {
    var response = new Warehouse();
    response.setBusinessUnitCode(warehouse.businessUnitCode);
    response.setLocation(warehouse.location);
    response.setCapacity(warehouse.capacity);
    response.setStock(warehouse.stock);

    return response;
  }
}
