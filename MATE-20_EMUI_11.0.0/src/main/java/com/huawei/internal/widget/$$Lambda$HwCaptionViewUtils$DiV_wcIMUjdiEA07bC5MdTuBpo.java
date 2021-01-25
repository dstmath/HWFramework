package com.huawei.internal.widget;

import java.util.concurrent.ThreadFactory;

/* renamed from: com.huawei.internal.widget.-$$Lambda$HwCaptionViewUtils$DiV_wcIMU-jdiEA07bC5MdTuBpo  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwCaptionViewUtils$DiV_wcIMUjdiEA07bC5MdTuBpo implements ThreadFactory {
    public static final /* synthetic */ $$Lambda$HwCaptionViewUtils$DiV_wcIMUjdiEA07bC5MdTuBpo INSTANCE = new $$Lambda$HwCaptionViewUtils$DiV_wcIMUjdiEA07bC5MdTuBpo();

    private /* synthetic */ $$Lambda$HwCaptionViewUtils$DiV_wcIMUjdiEA07bC5MdTuBpo() {
    }

    @Override // java.util.concurrent.ThreadFactory
    public final Thread newThread(Runnable runnable) {
        return HwCaptionViewUtils.lambda$startToBlur$0(runnable);
    }
}
