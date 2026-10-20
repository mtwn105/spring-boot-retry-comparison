package dev.mtwn105.retry.common;

@FunctionalInterface
public interface InventoryLookup {
    String lookup(InventoryProbe probe);
}
