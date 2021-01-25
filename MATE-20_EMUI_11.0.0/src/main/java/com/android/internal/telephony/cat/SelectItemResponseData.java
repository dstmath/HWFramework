package com.android.internal.telephony.cat;

import java.io.ByteArrayOutputStream;

/* access modifiers changed from: package-private */
/* compiled from: ResponseData */
public class SelectItemResponseData extends ResponseData {
    private int mId;

    public SelectItemResponseData(int id) {
        this.mId = id;
    }

    @Override // com.android.internal.telephony.cat.ResponseData
    public void format(ByteArrayOutputStream buf) {
        buf.write(ComprehensionTlvTag.ITEM_ID.value() | 128);
        buf.write(1);
        buf.write(this.mId);
    }
}
