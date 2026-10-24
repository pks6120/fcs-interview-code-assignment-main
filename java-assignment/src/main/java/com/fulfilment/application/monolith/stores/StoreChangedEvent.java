package com.fulfilment.application.monolith.stores;

public record StoreChangedEvent(Store store, Type type) {
    public enum Type { CREATED, UPDATED }
}
