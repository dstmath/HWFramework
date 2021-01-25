package android.app;

import android.annotation.UnsupportedAppUsage;
import android.util.AndroidRuntimeException;

/* access modifiers changed from: package-private */
/* compiled from: LoadedApk */
public final class IntentReceiverLeaked extends AndroidRuntimeException {
    @UnsupportedAppUsage
    public IntentReceiverLeaked(String msg) {
        super(msg);
    }
}
