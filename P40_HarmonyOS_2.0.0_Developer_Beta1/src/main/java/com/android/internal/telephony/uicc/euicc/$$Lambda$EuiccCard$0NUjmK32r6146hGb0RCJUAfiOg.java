package com.android.internal.telephony.uicc.euicc;

import com.android.internal.telephony.uicc.euicc.EuiccCard;

/* renamed from: com.android.internal.telephony.uicc.euicc.-$$Lambda$EuiccCard$0NUjmK32-r6146hGb0RCJUAfiOg  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$EuiccCard$0NUjmK32r6146hGb0RCJUAfiOg implements EuiccCard.ApduResponseHandler {
    public static final /* synthetic */ $$Lambda$EuiccCard$0NUjmK32r6146hGb0RCJUAfiOg INSTANCE = new $$Lambda$EuiccCard$0NUjmK32r6146hGb0RCJUAfiOg();

    private /* synthetic */ $$Lambda$EuiccCard$0NUjmK32r6146hGb0RCJUAfiOg() {
    }

    @Override // com.android.internal.telephony.uicc.euicc.EuiccCard.ApduResponseHandler
    public final Object handleResult(byte[] bArr) {
        return EuiccCard.lambda$resetMemory$17(bArr);
    }
}
