package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.location.LocationGateway;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;


import com.fulfilment.application.monolith.warehouses.exceptions.WarehouseBusinessException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

public class CreateWarehouseUseCaseTest {
    WarehouseStore store = Mockito.mock(WarehouseStore.class);
    LocationResolver resolver = new LocationGateway();
    CreateWarehouseUseCase useCase =
            new CreateWarehouseUseCase(store, new WarehouseLocationValidator(resolver, store));

    private Warehouse warehouse(String code, String location, int capacity, int stock) {
        Warehouse w = new Warehouse();
        w.businessUnitCode = code; w.location = location; w.capacity = capacity; w.stock = stock;
        return w;
    }

    @Test
    void createsValidWarehouse() {
        when(store.getAll()).thenReturn(List.of());
        useCase.create(warehouse("BU-1", "ZWOLLE-001", 30, 10));
        verify(store).create(any());
    }

    @Test
    void rejectsDuplicateCode() {
        when(store.findByBusinessUnitCode("BU-1")).thenReturn(new Warehouse());
        assertThrows(WarehouseBusinessException.class,
                () -> useCase.create(warehouse("BU-1", "ZWOLLE-001", 30, 10)));
    }

    @Test
    void rejectsUnknownLocation() {
        assertThrows(WarehouseBusinessException.class,
                () -> useCase.create(warehouse("BU-2", "MARS-001", 30, 10)));
    }

    @Test
    void rejectsCapacityAboveLocationMax() {
        assertThrows(WarehouseBusinessException.class,
                () -> useCase.create(warehouse("BU-3", "ZWOLLE-001", 41, 10)));
    }
}
