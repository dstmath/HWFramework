package com.android.internal.telephony.uicc.euicc;

import com.android.internal.telephony.uicc.euicc.EuiccCard;

/* renamed from: com.android.internal.telephony.uicc.euicc.-$$Lambda$EuiccCard$wgj93ukgzqjttFzrDLqGFk_Sd5A  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$EuiccCard$wgj93ukgzqjttFzrDLqGFk_Sd5A implements EuiccCard.ApduResponseHandler {
    public static final /* synthetic */ $$Lambda$EuiccCard$wgj93ukgzqjttFzrDLqGFk_Sd5A INSTANCE = new $$Lambda$EuiccCard$wgj93ukgzqjttFzrDLqGFk_Sd5A();

    private /* synthetic */ $$Lambda$EuiccCard$wgj93ukgzqjttFzrDLqGFk_Sd5A() {
    }

    @Override // com.android.internal.telephony.uicc.euicc.EuiccCard.ApduResponseHandler
    public final Object handleResult(byte[] bArr) {
        return EuiccCard.lambda$setDefaultSmdpAddress$23(bArr);
    }
}
