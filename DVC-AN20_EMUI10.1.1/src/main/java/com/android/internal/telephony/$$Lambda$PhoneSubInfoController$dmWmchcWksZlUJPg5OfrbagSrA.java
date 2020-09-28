package com.android.internal.telephony;

import com.android.internal.telephony.PhoneSubInfoController;

/* renamed from: com.android.internal.telephony.-$$Lambda$PhoneSubInfoController$dmWm-chcWksZlUJPg5OfrbagSrA  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PhoneSubInfoController$dmWmchcWksZlUJPg5OfrbagSrA implements PhoneSubInfoController.CallPhoneMethodHelper {
    public static final /* synthetic */ $$Lambda$PhoneSubInfoController$dmWmchcWksZlUJPg5OfrbagSrA INSTANCE = new $$Lambda$PhoneSubInfoController$dmWmchcWksZlUJPg5OfrbagSrA();

    private /* synthetic */ $$Lambda$PhoneSubInfoController$dmWmchcWksZlUJPg5OfrbagSrA() {
    }

    @Override // com.android.internal.telephony.PhoneSubInfoController.CallPhoneMethodHelper
    public final Object callMethod(Phone phone) {
        return phone.getMsisdn();
    }
}
