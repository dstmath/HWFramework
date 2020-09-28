package com.android.internal.telephony.uicc.euicc;

import com.android.internal.telephony.uicc.euicc.EuiccCard;

/* renamed from: com.android.internal.telephony.uicc.euicc.-$$Lambda$EuiccCard$ADB4BKXCYw8oHd-aqHgRFEm7vGg  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$EuiccCard$ADB4BKXCYw8oHdaqHgRFEm7vGg implements EuiccCard.ApduResponseHandler {
    public static final /* synthetic */ $$Lambda$EuiccCard$ADB4BKXCYw8oHdaqHgRFEm7vGg INSTANCE = new $$Lambda$EuiccCard$ADB4BKXCYw8oHdaqHgRFEm7vGg();

    private /* synthetic */ $$Lambda$EuiccCard$ADB4BKXCYw8oHdaqHgRFEm7vGg() {
    }

    @Override // com.android.internal.telephony.uicc.euicc.EuiccCard.ApduResponseHandler
    public final Object handleResult(byte[] bArr) {
        return EuiccCard.lambda$retrieveNotification$46(bArr);
    }
}
