package com.android.internal.telephony.uicc.euicc;

import com.android.internal.telephony.uicc.IccIoResult;
import com.android.internal.telephony.uicc.euicc.EuiccCard;

/* renamed from: com.android.internal.telephony.uicc.euicc.-$$Lambda$EuiccCard$dwMNgp0nb8jQ75klP-URUuDP17U  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$EuiccCard$dwMNgp0nb8jQ75klPURUuDP17U implements EuiccCard.ApduIntermediateResultHandler {
    public static final /* synthetic */ $$Lambda$EuiccCard$dwMNgp0nb8jQ75klPURUuDP17U INSTANCE = new $$Lambda$EuiccCard$dwMNgp0nb8jQ75klPURUuDP17U();

    private /* synthetic */ $$Lambda$EuiccCard$dwMNgp0nb8jQ75klPURUuDP17U() {
    }

    @Override // com.android.internal.telephony.uicc.euicc.EuiccCard.ApduIntermediateResultHandler
    public final boolean shouldContinue(IccIoResult iccIoResult) {
        return EuiccCard.lambda$loadBoundProfilePackage$38(iccIoResult);
    }
}
