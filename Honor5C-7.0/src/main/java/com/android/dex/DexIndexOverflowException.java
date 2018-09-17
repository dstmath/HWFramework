package com.android.dex;

public final class DexIndexOverflowException extends DexException {
    public DexIndexOverflowException(String message) {
        super(message);
    }

    public DexIndexOverflowException(Throwable cause) {
        super(cause);
    }
}
