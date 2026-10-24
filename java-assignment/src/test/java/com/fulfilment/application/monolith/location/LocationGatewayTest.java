package com.fulfilment.application.monolith.location;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

public class LocationGatewayTest {

  private final LocationGateway gateway = new LocationGateway();

  @Test
  void resolvesExistingLocation() {
    Location l = gateway.resolveByIdentifier("ZWOLLE-001");
    assertNotNull(l);
    assertEquals(1, l.maxNumberOfWarehouses);
    assertEquals(40, l.maxCapacity);
  }

  @Test
  void returnsNullForUnknownLocation() {
    assertNull(gateway.resolveByIdentifier("NOWHERE-999"));
  }

  @Test
  public void testWhenResolveExistingLocationShouldReturn() {
    // given
     LocationGateway locationGateway = new LocationGateway();

     //when
     Location location = locationGateway.resolveByIdentifier("ZWOLLE-001");

     //then
     assertEquals(location.identification, "ZWOLLE-001");


  }
}
