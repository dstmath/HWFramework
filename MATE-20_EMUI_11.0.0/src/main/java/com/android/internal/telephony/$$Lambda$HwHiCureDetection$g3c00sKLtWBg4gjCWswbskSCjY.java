package com.android.internal.telephony;

import com.android.internal.telephony.HwHiCureDetection;
import java.util.function.Predicate;

/* renamed from: com.android.internal.telephony.-$$Lambda$HwHiCureDetection$g3c00sKLtWBg4-gjCWswbskSCjY  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwHiCureDetection$g3c00sKLtWBg4gjCWswbskSCjY implements Predicate {
    public static final /* synthetic */ $$Lambda$HwHiCureDetection$g3c00sKLtWBg4gjCWswbskSCjY INSTANCE = new $$Lambda$HwHiCureDetection$g3c00sKLtWBg4gjCWswbskSCjY();

    private /* synthetic */ $$Lambda$HwHiCureDetection$g3c00sKLtWBg4gjCWswbskSCjY() {
    }

    @Override // java.util.function.Predicate
    public final boolean test(Object obj) {
        return ((HwHiCureDetection.ApnInfo) obj).mType.contains(HwHiCureDetection.DEFAULT_APN_TYPE);
    }
}
