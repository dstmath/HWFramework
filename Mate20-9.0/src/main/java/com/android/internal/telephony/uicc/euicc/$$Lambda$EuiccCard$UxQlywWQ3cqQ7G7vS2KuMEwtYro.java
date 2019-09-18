package com.android.internal.telephony.uicc.euicc;

import com.android.internal.telephony.uicc.asn1.Asn1Node;
import com.android.internal.telephony.uicc.euicc.EuiccCard;
import com.android.internal.telephony.uicc.euicc.apdu.RequestBuilder;

/* renamed from: com.android.internal.telephony.uicc.euicc.-$$Lambda$EuiccCard$UxQlywWQ3cqQ7G7vS2KuMEwtYro  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$EuiccCard$UxQlywWQ3cqQ7G7vS2KuMEwtYro implements EuiccCard.ApduRequestBuilder {
    public static final /* synthetic */ $$Lambda$EuiccCard$UxQlywWQ3cqQ7G7vS2KuMEwtYro INSTANCE = new $$Lambda$EuiccCard$UxQlywWQ3cqQ7G7vS2KuMEwtYro();

    private /* synthetic */ $$Lambda$EuiccCard$UxQlywWQ3cqQ7G7vS2KuMEwtYro() {
    }

    public final void build(RequestBuilder requestBuilder) {
        requestBuilder.addStoreData(Asn1Node.newBuilder(48930).build().toHex());
    }
}
