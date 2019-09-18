package android.mtp;

import android.mtp.MtpStorageManager;
import java.util.function.ToIntFunction;

/* renamed from: android.mtp.-$$Lambda$iwOv5HKUnGm7PVU3weoI9-JmsXc  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$iwOv5HKUnGm7PVU3weoI9JmsXc implements ToIntFunction {
    public static final /* synthetic */ $$Lambda$iwOv5HKUnGm7PVU3weoI9JmsXc INSTANCE = new $$Lambda$iwOv5HKUnGm7PVU3weoI9JmsXc();

    private /* synthetic */ $$Lambda$iwOv5HKUnGm7PVU3weoI9JmsXc() {
    }

    public final int applyAsInt(Object obj) {
        return ((MtpStorageManager.MtpObject) obj).getId();
    }
}
