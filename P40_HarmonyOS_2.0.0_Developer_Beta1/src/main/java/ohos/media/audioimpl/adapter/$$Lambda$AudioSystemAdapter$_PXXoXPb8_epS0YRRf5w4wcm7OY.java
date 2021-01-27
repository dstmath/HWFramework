package ohos.media.audioimpl.adapter;

import android.media.AudioPort;
import java.util.function.Predicate;

/* renamed from: ohos.media.audioimpl.adapter.-$$Lambda$AudioSystemAdapter$_PXXoXPb8_epS0YRRf5w4wcm7OY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$AudioSystemAdapter$_PXXoXPb8_epS0YRRf5w4wcm7OY implements Predicate {
    public static final /* synthetic */ $$Lambda$AudioSystemAdapter$_PXXoXPb8_epS0YRRf5w4wcm7OY INSTANCE = new $$Lambda$AudioSystemAdapter$_PXXoXPb8_epS0YRRf5w4wcm7OY();

    private /* synthetic */ $$Lambda$AudioSystemAdapter$_PXXoXPb8_epS0YRRf5w4wcm7OY() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return AudioSystemAdapter.isWiredHeadsetPort((AudioPort) obj);
    }
}
