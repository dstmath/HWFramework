package com.android.internal.telephony.cat;

import java.io.ByteArrayOutputStream;

/* compiled from: ResponseData */
class SelectItemResponseData extends ResponseData {
    private int mId;

    public SelectItemResponseData(int id) {
        this.mId = id;
    }

    public void format(ByteArrayOutputStream buf) {
        buf.write(ComprehensionTlvTag.ITEM_ID.value() | 128);
        buf.write(1);
        buf.write(this.mId);
    }
}
