package com.android.server.am;

import android.content.pm.FeatureInfo;
import java.util.Comparator;

/* renamed from: com.android.server.am.-$$Lambda$ActivityManagerShellCommand$yu115wjRB5hvRTjVM9oePAy5cM0  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ActivityManagerShellCommand$yu115wjRB5hvRTjVM9oePAy5cM0 implements Comparator {
    public static final /* synthetic */ $$Lambda$ActivityManagerShellCommand$yu115wjRB5hvRTjVM9oePAy5cM0 INSTANCE = new $$Lambda$ActivityManagerShellCommand$yu115wjRB5hvRTjVM9oePAy5cM0();

    private /* synthetic */ $$Lambda$ActivityManagerShellCommand$yu115wjRB5hvRTjVM9oePAy5cM0() {
    }

    @Override // java.util.Comparator
    public final int compare(Object obj, Object obj2) {
        return ActivityManagerShellCommand.lambda$writeDeviceConfig$0((FeatureInfo) obj, (FeatureInfo) obj2);
    }
}
