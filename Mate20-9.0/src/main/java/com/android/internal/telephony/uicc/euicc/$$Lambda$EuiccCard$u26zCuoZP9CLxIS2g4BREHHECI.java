package com.android.internal.telephony.uicc.euicc;

import com.android.internal.telephony.uicc.euicc.EuiccCard;

/* renamed from: com.android.internal.telephony.uicc.euicc.-$$Lambda$EuiccCard$u2-6zCuoZP9CLxIS2g4BREHHECI  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$EuiccCard$u26zCuoZP9CLxIS2g4BREHHECI implements EuiccCard.ApduResponseHandler {
    public static final /* synthetic */ $$Lambda$EuiccCard$u26zCuoZP9CLxIS2g4BREHHECI INSTANCE = new $$Lambda$EuiccCard$u26zCuoZP9CLxIS2g4BREHHECI();

    private /* synthetic */ $$Lambda$EuiccCard$u26zCuoZP9CLxIS2g4BREHHECI() {
    }

    public final Object handleResult(byte[] bArr) {
        return EuiccCard.parseResponse(bArr).getChild(129, new int[0]).asString();
    }
}
