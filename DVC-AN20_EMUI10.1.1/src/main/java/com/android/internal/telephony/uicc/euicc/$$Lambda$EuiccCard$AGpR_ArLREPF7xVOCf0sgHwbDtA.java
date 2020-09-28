package com.android.internal.telephony.uicc.euicc;

import com.android.internal.telephony.uicc.euicc.EuiccCard;

/* renamed from: com.android.internal.telephony.uicc.euicc.-$$Lambda$EuiccCard$AGpR_ArLREPF7xVOCf0sgHwbDtA  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$EuiccCard$AGpR_ArLREPF7xVOCf0sgHwbDtA implements EuiccCard.ApduResponseHandler {
    public static final /* synthetic */ $$Lambda$EuiccCard$AGpR_ArLREPF7xVOCf0sgHwbDtA INSTANCE = new $$Lambda$EuiccCard$AGpR_ArLREPF7xVOCf0sgHwbDtA();

    private /* synthetic */ $$Lambda$EuiccCard$AGpR_ArLREPF7xVOCf0sgHwbDtA() {
    }

    @Override // com.android.internal.telephony.uicc.euicc.EuiccCard.ApduResponseHandler
    public final Object handleResult(byte[] bArr) {
        return EuiccCard.parseResponse(bArr).getChild(128, new int[0]).asBytes();
    }
}
