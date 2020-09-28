package android.os;

import android.annotation.UnsupportedAppUsage;

/* access modifiers changed from: package-private */
/* compiled from: ZygoteProcess */
public class ZygoteStartFailedEx extends Exception {
    @UnsupportedAppUsage
    ZygoteStartFailedEx(String s) {
        super(s);
    }

    @UnsupportedAppUsage
    ZygoteStartFailedEx(Throwable cause) {
        super(cause);
    }

    ZygoteStartFailedEx(String s, Throwable cause) {
        super(s, cause);
    }
}
