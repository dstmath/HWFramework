package com.android.internal.telephony.imsphone;

import android.telephony.PhoneNumberUtils;
import com.android.internal.telephony.imsphone.ImsPhoneCallTracker;

/* renamed from: com.android.internal.telephony.imsphone.-$$Lambda$ImsPhoneCallTracker$QlPVd_3u4_verjHUDnkn6zaSe54  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ImsPhoneCallTracker$QlPVd_3u4_verjHUDnkn6zaSe54 implements ImsPhoneCallTracker.PhoneNumberUtilsProxy {
    public static final /* synthetic */ $$Lambda$ImsPhoneCallTracker$QlPVd_3u4_verjHUDnkn6zaSe54 INSTANCE = new $$Lambda$ImsPhoneCallTracker$QlPVd_3u4_verjHUDnkn6zaSe54();

    private /* synthetic */ $$Lambda$ImsPhoneCallTracker$QlPVd_3u4_verjHUDnkn6zaSe54() {
    }

    @Override // com.android.internal.telephony.imsphone.ImsPhoneCallTracker.PhoneNumberUtilsProxy
    public final boolean isEmergencyNumber(String str) {
        return PhoneNumberUtils.isEmergencyNumber(str);
    }
}
