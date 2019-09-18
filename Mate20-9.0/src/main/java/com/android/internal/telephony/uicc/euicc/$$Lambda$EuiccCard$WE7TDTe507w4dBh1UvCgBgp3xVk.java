package com.android.internal.telephony.uicc.euicc;

import com.android.internal.telephony.uicc.asn1.Asn1Node;
import com.android.internal.telephony.uicc.euicc.EuiccCard;
import com.android.internal.telephony.uicc.euicc.apdu.RequestBuilder;

/* renamed from: com.android.internal.telephony.uicc.euicc.-$$Lambda$EuiccCard$WE7TDTe507w4dBh1UvCgBgp3xVk  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$EuiccCard$WE7TDTe507w4dBh1UvCgBgp3xVk implements EuiccCard.ApduRequestBuilder {
    public static final /* synthetic */ $$Lambda$EuiccCard$WE7TDTe507w4dBh1UvCgBgp3xVk INSTANCE = new $$Lambda$EuiccCard$WE7TDTe507w4dBh1UvCgBgp3xVk();

    private /* synthetic */ $$Lambda$EuiccCard$WE7TDTe507w4dBh1UvCgBgp3xVk() {
    }

    public final void build(RequestBuilder requestBuilder) {
        requestBuilder.addStoreData(Asn1Node.newBuilder(48928).build().toHex());
    }
}
