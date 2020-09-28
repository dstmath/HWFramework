package com.android.internal.telephony.uicc.euicc;

import com.android.internal.telephony.uicc.euicc.EuiccCard;

/* renamed from: com.android.internal.telephony.uicc.euicc.-$$Lambda$EuiccCard$WoM2ziweCgrYxAgllxpAtHF-3Es  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$EuiccCard$WoM2ziweCgrYxAgllxpAtHF3Es implements EuiccCard.ApduResponseHandler {
    public static final /* synthetic */ $$Lambda$EuiccCard$WoM2ziweCgrYxAgllxpAtHF3Es INSTANCE = new $$Lambda$EuiccCard$WoM2ziweCgrYxAgllxpAtHF3Es();

    private /* synthetic */ $$Lambda$EuiccCard$WoM2ziweCgrYxAgllxpAtHF3Es() {
    }

    @Override // com.android.internal.telephony.uicc.euicc.EuiccCard.ApduResponseHandler
    public final Object handleResult(byte[] bArr) {
        return EuiccCard.lambda$removeNotificationFromList$48(bArr);
    }
}
