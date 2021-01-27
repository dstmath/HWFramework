package com.android.internal.telephony;

import android.content.Context;
import com.android.internal.telephony.PhoneSubInfoController;

/* renamed from: com.android.internal.telephony.-$$Lambda$PhoneSubInfoController$EYZUPU0CYhRoptGCGJ9y78u-jQM  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PhoneSubInfoController$EYZUPU0CYhRoptGCGJ9y78ujQM implements PhoneSubInfoController.PermissionCheckHelper {
    public static final /* synthetic */ $$Lambda$PhoneSubInfoController$EYZUPU0CYhRoptGCGJ9y78ujQM INSTANCE = new $$Lambda$PhoneSubInfoController$EYZUPU0CYhRoptGCGJ9y78ujQM();

    private /* synthetic */ $$Lambda$PhoneSubInfoController$EYZUPU0CYhRoptGCGJ9y78ujQM() {
    }

    @Override // com.android.internal.telephony.PhoneSubInfoController.PermissionCheckHelper
    public final boolean checkPermission(Context context, int i, String str, String str2) {
        return TelephonyPermissions.checkCallingOrSelfReadSubscriberIdentifiers(context, i, str, str2);
    }
}
