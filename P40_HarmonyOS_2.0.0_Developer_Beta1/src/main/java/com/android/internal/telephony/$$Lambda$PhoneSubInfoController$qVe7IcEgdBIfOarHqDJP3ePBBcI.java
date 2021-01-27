package com.android.internal.telephony;

import android.content.Context;
import com.android.internal.telephony.PhoneSubInfoController;

/* renamed from: com.android.internal.telephony.-$$Lambda$PhoneSubInfoController$qVe7IcEgdBIfOarHqDJP3ePBBcI  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PhoneSubInfoController$qVe7IcEgdBIfOarHqDJP3ePBBcI implements PhoneSubInfoController.PermissionCheckHelper {
    public static final /* synthetic */ $$Lambda$PhoneSubInfoController$qVe7IcEgdBIfOarHqDJP3ePBBcI INSTANCE = new $$Lambda$PhoneSubInfoController$qVe7IcEgdBIfOarHqDJP3ePBBcI();

    private /* synthetic */ $$Lambda$PhoneSubInfoController$qVe7IcEgdBIfOarHqDJP3ePBBcI() {
    }

    @Override // com.android.internal.telephony.PhoneSubInfoController.PermissionCheckHelper
    public final boolean checkPermission(Context context, int i, String str, String str2) {
        return TelephonyPermissions.checkCallingOrSelfReadDeviceIdentifiers(context, i, str, str2);
    }
}
