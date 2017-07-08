package com.android.internal.telephony.cat;

import com.google.android.mms.pdu.PduHeaders;
import java.io.ByteArrayOutputStream;

/* compiled from: ResponseData */
class ChannelStatusResponseData extends ResponseData {
    private int[] channelStatus;

    public ChannelStatusResponseData(int[] channelStatus) {
        this.channelStatus = channelStatus;
    }

    public void format(ByteArrayOutputStream buf) {
        if (this.channelStatus != null) {
            int tag = ComprehensionTlvTag.CHANNEL_STATUS.value();
            for (int status : this.channelStatus) {
                if (status > 0) {
                    buf.write(tag);
                    buf.write(2);
                    buf.write((status >> 8) & PduHeaders.STORE_STATUS_ERROR_END);
                    buf.write(status & PduHeaders.STORE_STATUS_ERROR_END);
                }
            }
        }
    }
}
