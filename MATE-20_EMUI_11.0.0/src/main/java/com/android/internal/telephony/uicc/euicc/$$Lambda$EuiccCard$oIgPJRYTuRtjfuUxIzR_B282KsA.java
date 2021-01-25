package com.android.internal.telephony.uicc.euicc;

import com.android.internal.telephony.uicc.euicc.EuiccCard;

/* renamed from: com.android.internal.telephony.uicc.euicc.-$$Lambda$EuiccCard$oIgPJRYTuRtjfuUxIzR_B282KsA  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$EuiccCard$oIgPJRYTuRtjfuUxIzR_B282KsA implements EuiccCard.ApduResponseHandler {
    public static final /* synthetic */ $$Lambda$EuiccCard$oIgPJRYTuRtjfuUxIzR_B282KsA INSTANCE = new $$Lambda$EuiccCard$oIgPJRYTuRtjfuUxIzR_B282KsA();

    private /* synthetic */ $$Lambda$EuiccCard$oIgPJRYTuRtjfuUxIzR_B282KsA() {
    }

    @Override // com.android.internal.telephony.uicc.euicc.EuiccCard.ApduResponseHandler
    public final Object handleResult(byte[] bArr) {
        return EuiccCard.parseResponseAndCheckSimpleError(bArr, 4).toBytes();
    }
}
