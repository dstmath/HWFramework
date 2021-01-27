package android.app;

import android.annotation.UnsupportedAppUsage;
import android.util.AndroidRuntimeException;

/* access modifiers changed from: package-private */
/* compiled from: LoadedApk */
public final class ServiceConnectionLeaked extends AndroidRuntimeException {
    @UnsupportedAppUsage
    public ServiceConnectionLeaked(String msg) {
        super(msg);
    }
}
