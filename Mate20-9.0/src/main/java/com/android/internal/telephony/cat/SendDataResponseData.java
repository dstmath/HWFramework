package com.android.internal.telephony.cat;

import java.io.ByteArrayOutputStream;

/* compiled from: ResponseData */
class SendDataResponseData extends ResponseData {
    private int mLength = 0;

    public SendDataResponseData(int len) {
        this.mLength = len;
    }

    public void format(ByteArrayOutputStream buf) {
        buf.write(ComprehensionTlvTag.CHANNEL_DATA_LENGTH.value());
        buf.write(1);
        buf.write(this.mLength & 255);
    }
}
