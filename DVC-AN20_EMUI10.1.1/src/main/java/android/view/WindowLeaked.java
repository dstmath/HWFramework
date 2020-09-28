package android.view;

import android.annotation.UnsupportedAppUsage;
import android.util.AndroidRuntimeException;

/* access modifiers changed from: package-private */
/* compiled from: WindowManagerGlobal */
public final class WindowLeaked extends AndroidRuntimeException {
    @UnsupportedAppUsage
    public WindowLeaked(String msg) {
        super(msg);
    }
}
