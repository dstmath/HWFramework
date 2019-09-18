package com.android.internal.telephony.uicc.euicc;

import com.android.internal.telephony.uicc.asn1.Asn1Node;
import com.android.internal.telephony.uicc.euicc.EuiccCard;
import com.android.internal.telephony.uicc.euicc.apdu.RequestBuilder;

/* renamed from: com.android.internal.telephony.uicc.euicc.-$$Lambda$EuiccCard$8wofF-Li1V6a8rJQc-M2IGeJ26E  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$EuiccCard$8wofFLi1V6a8rJQcM2IGeJ26E implements EuiccCard.ApduRequestBuilder {
    public static final /* synthetic */ $$Lambda$EuiccCard$8wofFLi1V6a8rJQcM2IGeJ26E INSTANCE = new $$Lambda$EuiccCard$8wofFLi1V6a8rJQcM2IGeJ26E();

    private /* synthetic */ $$Lambda$EuiccCard$8wofFLi1V6a8rJQcM2IGeJ26E() {
    }

    public final void build(RequestBuilder requestBuilder) {
        requestBuilder.addStoreData(Asn1Node.newBuilder(48942).build().toHex());
    }
}
