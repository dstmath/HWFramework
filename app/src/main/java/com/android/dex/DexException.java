package com.android.dex;

import com.android.dex.util.ExceptionWithContext;

public class DexException extends ExceptionWithContext {
    public DexException(String message) {
        super(message);
    }

    public DexException(Throwable cause) {
        super(cause);
    }
}
