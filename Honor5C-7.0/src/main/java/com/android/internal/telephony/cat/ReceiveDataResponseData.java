package com.android.internal.telephony.cat;

import com.android.internal.telephony.CallFailCause;
import com.google.android.mms.pdu.PduHeaders;
import com.google.android.mms.pdu.PduPart;
import java.io.ByteArrayOutputStream;

/* compiled from: ResponseData */
class ReceiveDataResponseData extends ResponseData {
    private byte[] mData;
    private int mLength;

    public ReceiveDataResponseData(byte[] data, int len) {
        this.mData = null;
        this.mLength = 0;
        this.mData = data;
        this.mLength = len;
    }

    public void format(ByteArrayOutputStream buf) {
        int tag = ComprehensionTlvTag.CHANNEL_DATA.value() | PduPart.P_Q;
        if (this.mData != null) {
            buf.write(tag);
            if (this.mData.length > CallFailCause.INTERWORKING_UNSPECIFIED) {
                buf.write(PduPart.P_DISPOSITION_ATTACHMENT);
            }
            buf.write(this.mData.length & PduHeaders.STORE_STATUS_ERROR_END);
            for (byte b : this.mData) {
                buf.write(b);
            }
        }
        buf.write(ComprehensionTlvTag.CHANNEL_DATA_LENGTH.value() | PduPart.P_Q);
        buf.write(1);
        buf.write(this.mLength & PduHeaders.STORE_STATUS_ERROR_END);
    }
}
