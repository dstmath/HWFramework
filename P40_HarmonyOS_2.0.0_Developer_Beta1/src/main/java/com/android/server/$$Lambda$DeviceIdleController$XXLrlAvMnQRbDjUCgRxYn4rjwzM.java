package com.android.server;

import com.android.server.deviceidle.DeviceIdleConstraintTracker;
import java.util.function.Predicate;

/* renamed from: com.android.server.-$$Lambda$DeviceIdleController$XXLrlAvMnQRbDjUCgRxYn4rjwzM  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$DeviceIdleController$XXLrlAvMnQRbDjUCgRxYn4rjwzM implements Predicate {
    public static final /* synthetic */ $$Lambda$DeviceIdleController$XXLrlAvMnQRbDjUCgRxYn4rjwzM INSTANCE = new $$Lambda$DeviceIdleController$XXLrlAvMnQRbDjUCgRxYn4rjwzM();

    private /* synthetic */ $$Lambda$DeviceIdleController$XXLrlAvMnQRbDjUCgRxYn4rjwzM() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return ((DeviceIdleConstraintTracker) obj).active;
    }
}
