package android.os;

import android.os.BatteryStats;

/* renamed from: android.os.-$$Lambda$IyvVQC-0mKtsfXbnO0kDL64hrk0  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$IyvVQC0mKtsfXbnO0kDL64hrk0 implements BatteryStats.IntToString {
    public static final /* synthetic */ $$Lambda$IyvVQC0mKtsfXbnO0kDL64hrk0 INSTANCE = new $$Lambda$IyvVQC0mKtsfXbnO0kDL64hrk0();

    private /* synthetic */ $$Lambda$IyvVQC0mKtsfXbnO0kDL64hrk0() {
    }

    public final String applyAsString(int i) {
        return UserHandle.formatUid(i);
    }
}
