package com.android.internal.os;

import android.annotation.UnsupportedAppUsage;

/* access modifiers changed from: package-private */
public class ZygoteSecurityException extends RuntimeException {
    @UnsupportedAppUsage
    ZygoteSecurityException(String message) {
        super(message);
    }
}
