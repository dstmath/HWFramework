package com.android.internal.telephony.uicc.euicc;

import com.android.internal.telephony.uicc.asn1.Asn1Node;
import com.android.internal.telephony.uicc.euicc.EuiccCard;
import com.android.internal.telephony.uicc.euicc.apdu.RequestBuilder;

/* renamed from: com.android.internal.telephony.uicc.euicc.-$$Lambda$EuiccCard$HBn5KBGylwjLqIEm3rBhXnUU_8U  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$EuiccCard$HBn5KBGylwjLqIEm3rBhXnUU_8U implements EuiccCard.ApduRequestBuilder {
    public static final /* synthetic */ $$Lambda$EuiccCard$HBn5KBGylwjLqIEm3rBhXnUU_8U INSTANCE = new $$Lambda$EuiccCard$HBn5KBGylwjLqIEm3rBhXnUU_8U();

    private /* synthetic */ $$Lambda$EuiccCard$HBn5KBGylwjLqIEm3rBhXnUU_8U() {
    }

    @Override // com.android.internal.telephony.uicc.euicc.EuiccCard.ApduRequestBuilder
    public final void build(RequestBuilder requestBuilder) {
        requestBuilder.addStoreData(Asn1Node.newBuilder(48958).addChildAsBytes(92, new byte[]{90}).build().toHex());
    }
}
