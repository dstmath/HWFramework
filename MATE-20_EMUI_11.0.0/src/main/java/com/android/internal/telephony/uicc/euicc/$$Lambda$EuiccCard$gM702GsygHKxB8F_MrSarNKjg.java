package com.android.internal.telephony.uicc.euicc;

import com.android.internal.telephony.uicc.euicc.EuiccCard;

/* renamed from: com.android.internal.telephony.uicc.euicc.-$$Lambda$EuiccCard$gM-702GsygHKxB8F-_MrSarNKjg  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$EuiccCard$gM702GsygHKxB8F_MrSarNKjg implements EuiccCard.ApduResponseHandler {
    public static final /* synthetic */ $$Lambda$EuiccCard$gM702GsygHKxB8F_MrSarNKjg INSTANCE = new $$Lambda$EuiccCard$gM702GsygHKxB8F_MrSarNKjg();

    private /* synthetic */ $$Lambda$EuiccCard$gM702GsygHKxB8F_MrSarNKjg() {
    }

    @Override // com.android.internal.telephony.uicc.euicc.EuiccCard.ApduResponseHandler
    public final Object handleResult(byte[] bArr) {
        return EuiccCard.lambda$listNotifications$42(bArr);
    }
}
