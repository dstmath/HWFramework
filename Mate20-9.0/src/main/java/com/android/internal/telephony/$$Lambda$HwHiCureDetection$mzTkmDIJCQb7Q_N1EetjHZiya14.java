package com.android.internal.telephony;

import com.android.internal.telephony.HwHiCureDetection;
import java.util.function.Predicate;

/* renamed from: com.android.internal.telephony.-$$Lambda$HwHiCureDetection$mzTkmDIJCQb7Q_N1EetjHZiya14  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$HwHiCureDetection$mzTkmDIJCQb7Q_N1EetjHZiya14 implements Predicate {
    public static final /* synthetic */ $$Lambda$HwHiCureDetection$mzTkmDIJCQb7Q_N1EetjHZiya14 INSTANCE = new $$Lambda$HwHiCureDetection$mzTkmDIJCQb7Q_N1EetjHZiya14();

    private /* synthetic */ $$Lambda$HwHiCureDetection$mzTkmDIJCQb7Q_N1EetjHZiya14() {
    }

    public final boolean test(Object obj) {
        return ((HwHiCureDetection.ApnInfo) obj).mType.contains(HwHiCureDetection.DEFAULT_APN_TYPE);
    }
}
