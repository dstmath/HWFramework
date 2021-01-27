package com.android.internal.telephony;

import android.content.Context;
import com.android.internal.telephony.PhoneSubInfoController;

/* renamed from: com.android.internal.telephony.-$$Lambda$PhoneSubInfoController$1TnOMFYcM13ZTJNoLjxguPwVcxw  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PhoneSubInfoController$1TnOMFYcM13ZTJNoLjxguPwVcxw implements PhoneSubInfoController.PermissionCheckHelper {
    public static final /* synthetic */ $$Lambda$PhoneSubInfoController$1TnOMFYcM13ZTJNoLjxguPwVcxw INSTANCE = new $$Lambda$PhoneSubInfoController$1TnOMFYcM13ZTJNoLjxguPwVcxw();

    private /* synthetic */ $$Lambda$PhoneSubInfoController$1TnOMFYcM13ZTJNoLjxguPwVcxw() {
    }

    @Override // com.android.internal.telephony.PhoneSubInfoController.PermissionCheckHelper
    public final boolean checkPermission(Context context, int i, String str, String str2) {
        return TelephonyPermissions.checkCallingOrSelfReadPhoneNumber(context, i, str, str2);
    }
}
