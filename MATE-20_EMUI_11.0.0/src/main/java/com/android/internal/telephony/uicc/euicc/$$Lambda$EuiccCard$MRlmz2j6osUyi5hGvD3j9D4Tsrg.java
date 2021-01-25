package com.android.internal.telephony.uicc.euicc;

import com.android.internal.telephony.uicc.euicc.EuiccCard;

/* renamed from: com.android.internal.telephony.uicc.euicc.-$$Lambda$EuiccCard$MRlmz2j6osUyi5hGvD3j9D4Tsrg  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$EuiccCard$MRlmz2j6osUyi5hGvD3j9D4Tsrg implements EuiccCard.ApduResponseHandler {
    public static final /* synthetic */ $$Lambda$EuiccCard$MRlmz2j6osUyi5hGvD3j9D4Tsrg INSTANCE = new $$Lambda$EuiccCard$MRlmz2j6osUyi5hGvD3j9D4Tsrg();

    private /* synthetic */ $$Lambda$EuiccCard$MRlmz2j6osUyi5hGvD3j9D4Tsrg() {
    }

    @Override // com.android.internal.telephony.uicc.euicc.EuiccCard.ApduResponseHandler
    public final Object handleResult(byte[] bArr) {
        return EuiccCard.lambda$authenticateServer$33(bArr);
    }
}
