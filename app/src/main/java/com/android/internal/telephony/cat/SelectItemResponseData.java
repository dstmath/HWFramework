package com.android.internal.telephony.cat;

import com.google.android.mms.pdu.PduPart;
import java.io.ByteArrayOutputStream;

/* compiled from: ResponseData */
class SelectItemResponseData extends ResponseData {
    private int mId;

    public SelectItemResponseData(int id) {
        this.mId = id;
    }

    public void format(ByteArrayOutputStream buf) {
        buf.write(ComprehensionTlvTag.ITEM_ID.value() | PduPart.P_Q);
        buf.write(1);
        buf.write(this.mId);
    }
}
