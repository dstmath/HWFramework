package com.android.internal.telephony.uicc.euicc;

import com.android.internal.telephony.uicc.asn1.Asn1Node;
import com.android.internal.telephony.uicc.euicc.EuiccCard;
import com.android.internal.telephony.uicc.euicc.apdu.RequestBuilder;

/* renamed from: com.android.internal.telephony.uicc.euicc.-$$Lambda$EuiccCard$tPSWjOKtm9yQg21kHmLX49PPf_4  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$EuiccCard$tPSWjOKtm9yQg21kHmLX49PPf_4 implements EuiccCard.ApduRequestBuilder {
    public static final /* synthetic */ $$Lambda$EuiccCard$tPSWjOKtm9yQg21kHmLX49PPf_4 INSTANCE = new $$Lambda$EuiccCard$tPSWjOKtm9yQg21kHmLX49PPf_4();

    private /* synthetic */ $$Lambda$EuiccCard$tPSWjOKtm9yQg21kHmLX49PPf_4() {
    }

    @Override // com.android.internal.telephony.uicc.euicc.EuiccCard.ApduRequestBuilder
    public final void build(RequestBuilder requestBuilder) {
        requestBuilder.addStoreData(Asn1Node.newBuilder(48956).build().toHex());
    }
}
