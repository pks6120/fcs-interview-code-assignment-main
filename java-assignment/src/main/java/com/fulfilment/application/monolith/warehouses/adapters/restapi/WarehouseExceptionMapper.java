package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.exceptions.WarehouseBusinessException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.Map;

@Provider
public class WarehouseExceptionMapper implements ExceptionMapper<WarehouseBusinessException> {

    @Override
    public Response toResponse(WarehouseBusinessException e) {
        int status =
                switch (e.getReason()) {
                    case NOT_FOUND -> 404;
                    case CONFLICT -> 409;
                    case INVALID -> 400;
                };
        return Response.status(status)
                .type(MediaType.APPLICATION_JSON)
                .entity(Map.of("code", status, "error", e.getMessage()))
                .build();
    }
}
