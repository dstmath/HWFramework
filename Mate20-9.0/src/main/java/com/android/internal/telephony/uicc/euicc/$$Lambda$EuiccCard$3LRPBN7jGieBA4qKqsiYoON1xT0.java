package com.android.internal.telephony.uicc.euicc;

import com.android.internal.telephony.uicc.asn1.Asn1Node;
import com.android.internal.telephony.uicc.euicc.EuiccCard;
import com.android.internal.telephony.uicc.euicc.apdu.RequestBuilder;

/* renamed from: com.android.internal.telephony.uicc.euicc.-$$Lambda$EuiccCard$3LRPBN7jGieBA4qKqsiYoON1xT0  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$EuiccCard$3LRPBN7jGieBA4qKqsiYoON1xT0 implements EuiccCard.ApduRequestBuilder {
    public static final /* synthetic */ $$Lambda$EuiccCard$3LRPBN7jGieBA4qKqsiYoON1xT0 INSTANCE = new $$Lambda$EuiccCard$3LRPBN7jGieBA4qKqsiYoON1xT0();

    private /* synthetic */ $$Lambda$EuiccCard$3LRPBN7jGieBA4qKqsiYoON1xT0() {
    }

    public final void build(RequestBuilder requestBuilder) {
        requestBuilder.addStoreData(Asn1Node.newBuilder(48956).build().toHex());
    }
}
