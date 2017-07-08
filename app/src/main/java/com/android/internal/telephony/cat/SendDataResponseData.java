package com.android.internal.telephony.cat;

import com.google.android.mms.pdu.PduHeaders;
import java.io.ByteArrayOutputStream;

/* compiled from: ResponseData */
class SendDataResponseData extends ResponseData {
    private int mLength;

    public SendDataResponseData(int len) {
        this.mLength = 0;
        this.mLength = len;
    }

    public void format(ByteArrayOutputStream buf) {
        buf.write(ComprehensionTlvTag.CHANNEL_DATA_LENGTH.value());
        buf.write(1);
        buf.write(this.mLength & PduHeaders.STORE_STATUS_ERROR_END);
    }
}
