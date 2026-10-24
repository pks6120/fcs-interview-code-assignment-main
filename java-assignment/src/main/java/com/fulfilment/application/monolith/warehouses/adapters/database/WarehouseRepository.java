package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.fulfilment.application.monolith.warehouses.exceptions.WarehouseBusinessException;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Override
  public List<Warehouse> getAll() {

    return this.listAll().stream().map(DbWarehouse::toWarehouse).toList();
  }

  @Override
  @Transactional
  public void create(Warehouse warehouse) {
    DbWarehouse db = new DbWarehouse();
    db.businessUnitCode = warehouse.businessUnitCode;
    db.location = warehouse.location;
    db.capacity = warehouse.capacity;
    db.stock = warehouse.stock;
    db.createdAt = warehouse.createdAt != null ? warehouse.createdAt : LocalDateTime.now();
    db.archivedAt = warehouse.archivedAt;
    persist(db);
  }

  @Override
  @Transactional
  public void update(Warehouse warehouse) {
    DbWarehouse db = findActive(warehouse.businessUnitCode);
    if (db == null) {
      throw new WarehouseBusinessException(
              WarehouseBusinessException.Reason.NOT_FOUND, "No active warehouse with code '" + warehouse.businessUnitCode + "'.");
    }
    // db is a managed entity: changes are flushed automatically at commit
    db.location = warehouse.location;
    db.capacity = warehouse.capacity;
    db.stock = warehouse.stock;
    db.archivedAt = warehouse.archivedAt;
    flush();
  }

  @Override
  @Transactional
  public void remove(Warehouse warehouse) {
    DbWarehouse db = findActive(warehouse.businessUnitCode);
    if (db != null) {
      delete(db);
    }
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    DbWarehouse db = findActive(buCode);
    return db == null ? null : db.toWarehouse();
  }
  private DbWarehouse findActive(String buCode) {
    return find("businessUnitCode = ?1 and archivedAt is null", buCode).firstResult();
  }
}
