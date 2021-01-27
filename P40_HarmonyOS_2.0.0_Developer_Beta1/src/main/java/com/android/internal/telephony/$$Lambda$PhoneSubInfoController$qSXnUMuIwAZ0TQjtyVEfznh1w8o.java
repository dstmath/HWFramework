package com.android.internal.telephony;

import android.content.Context;
import com.android.internal.telephony.PhoneSubInfoController;

/* renamed from: com.android.internal.telephony.-$$Lambda$PhoneSubInfoController$qSXnUMuIwAZ0TQjtyVEfznh1w8o  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PhoneSubInfoController$qSXnUMuIwAZ0TQjtyVEfznh1w8o implements PhoneSubInfoController.PermissionCheckHelper {
    public static final /* synthetic */ $$Lambda$PhoneSubInfoController$qSXnUMuIwAZ0TQjtyVEfznh1w8o INSTANCE = new $$Lambda$PhoneSubInfoController$qSXnUMuIwAZ0TQjtyVEfznh1w8o();

    private /* synthetic */ $$Lambda$PhoneSubInfoController$qSXnUMuIwAZ0TQjtyVEfznh1w8o() {
    }

    @Override // com.android.internal.telephony.PhoneSubInfoController.PermissionCheckHelper
    public final boolean checkPermission(Context context, int i, String str, String str2) {
        return TelephonyPermissions.checkCallingOrSelfReadPhoneState(context, i, str, str2);
    }
}
