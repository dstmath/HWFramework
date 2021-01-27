package com.android.internal.telephony;

import com.android.internal.telephony.PhoneSubInfoController;

/* renamed from: com.android.internal.telephony.-$$Lambda$PhoneSubInfoController$1zkPy06BwndFkKrGCUI1ORIPJcI  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PhoneSubInfoController$1zkPy06BwndFkKrGCUI1ORIPJcI implements PhoneSubInfoController.CallPhoneMethodHelper {
    public static final /* synthetic */ $$Lambda$PhoneSubInfoController$1zkPy06BwndFkKrGCUI1ORIPJcI INSTANCE = new $$Lambda$PhoneSubInfoController$1zkPy06BwndFkKrGCUI1ORIPJcI();

    private /* synthetic */ $$Lambda$PhoneSubInfoController$1zkPy06BwndFkKrGCUI1ORIPJcI() {
    }

    @Override // com.android.internal.telephony.PhoneSubInfoController.CallPhoneMethodHelper
    public final Object callMethod(Phone phone) {
        return phone.getIccSerialNumber();
    }
}
