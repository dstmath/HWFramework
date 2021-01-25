package com.android.internal.telephony.imsphone;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.android.internal.telephony.imsphone.ImsPhoneCallTracker;

/* renamed from: com.android.internal.telephony.imsphone.-$$Lambda$ImsPhoneCallTracker$Zw03itjXT6-LrhiYuD-9nKFg2Wg  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$ImsPhoneCallTracker$Zw03itjXT6LrhiYuD9nKFg2Wg implements ImsPhoneCallTracker.SharedPreferenceProxy {
    public static final /* synthetic */ $$Lambda$ImsPhoneCallTracker$Zw03itjXT6LrhiYuD9nKFg2Wg INSTANCE = new $$Lambda$ImsPhoneCallTracker$Zw03itjXT6LrhiYuD9nKFg2Wg();

    private /* synthetic */ $$Lambda$ImsPhoneCallTracker$Zw03itjXT6LrhiYuD9nKFg2Wg() {
    }

    @Override // com.android.internal.telephony.imsphone.ImsPhoneCallTracker.SharedPreferenceProxy
    public final SharedPreferences getDefaultSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }
}
