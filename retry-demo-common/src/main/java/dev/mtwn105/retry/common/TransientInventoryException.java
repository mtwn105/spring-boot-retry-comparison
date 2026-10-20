package dev.mtwn105.retry.common;

public class TransientInventoryException extends RuntimeException {
    public TransientInventoryException(String message) {
        super(message);
    }
}
