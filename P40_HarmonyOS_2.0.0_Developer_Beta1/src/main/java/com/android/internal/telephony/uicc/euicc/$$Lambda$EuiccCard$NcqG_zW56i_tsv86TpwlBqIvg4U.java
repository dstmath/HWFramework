package com.android.internal.telephony.uicc.euicc;

import com.android.internal.telephony.uicc.euicc.EuiccCard;

/* renamed from: com.android.internal.telephony.uicc.euicc.-$$Lambda$EuiccCard$NcqG_zW56i_tsv86TpwlBqIvg4U  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$EuiccCard$NcqG_zW56i_tsv86TpwlBqIvg4U implements EuiccCard.ApduResponseHandler {
    public static final /* synthetic */ $$Lambda$EuiccCard$NcqG_zW56i_tsv86TpwlBqIvg4U INSTANCE = new $$Lambda$EuiccCard$NcqG_zW56i_tsv86TpwlBqIvg4U();

    private /* synthetic */ $$Lambda$EuiccCard$NcqG_zW56i_tsv86TpwlBqIvg4U() {
    }

    @Override // com.android.internal.telephony.uicc.euicc.EuiccCard.ApduResponseHandler
    public final Object handleResult(byte[] bArr) {
        return EuiccCard.lambda$retrieveNotificationList$44(bArr);
    }
}
