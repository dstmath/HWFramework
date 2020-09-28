package com.android.internal.telephony;

import com.android.internal.telephony.PhoneSubInfoController;

/* renamed from: com.android.internal.telephony.-$$Lambda$PhoneSubInfoController$Pb4HmeqsjasrNaXBByGh_-CFogk  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PhoneSubInfoController$Pb4HmeqsjasrNaXBByGh_CFogk implements PhoneSubInfoController.CallPhoneMethodHelper {
    public static final /* synthetic */ $$Lambda$PhoneSubInfoController$Pb4HmeqsjasrNaXBByGh_CFogk INSTANCE = new $$Lambda$PhoneSubInfoController$Pb4HmeqsjasrNaXBByGh_CFogk();

    private /* synthetic */ $$Lambda$PhoneSubInfoController$Pb4HmeqsjasrNaXBByGh_CFogk() {
    }

    @Override // com.android.internal.telephony.PhoneSubInfoController.CallPhoneMethodHelper
    public final Object callMethod(Phone phone) {
        return phone.resetCarrierKeysForImsiEncryption();
    }
}
