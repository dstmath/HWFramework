package com.android.internal.telephony.ims;

import android.telephony.ims.stub.ImsFeatureConfiguration;
import java.util.function.Predicate;

/* renamed from: com.android.internal.telephony.ims.-$$Lambda$ImsResolver$SIkPixr-qGLIK-usUJIKu6S5BBs  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ImsResolver$SIkPixrqGLIKusUJIKu6S5BBs implements Predicate {
    public static final /* synthetic */ $$Lambda$ImsResolver$SIkPixrqGLIKusUJIKu6S5BBs INSTANCE = new $$Lambda$ImsResolver$SIkPixrqGLIKusUJIKu6S5BBs();

    private /* synthetic */ $$Lambda$ImsResolver$SIkPixrqGLIKusUJIKu6S5BBs() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return ImsResolver.lambda$shouldFeaturesCauseBind$6((ImsFeatureConfiguration.FeatureSlotPair) obj);
    }
}
