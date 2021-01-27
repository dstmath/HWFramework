package com.android.internal.telephony.cat;

import java.io.ByteArrayOutputStream;

/* access modifiers changed from: package-private */
/* compiled from: ResponseData */
public class ChannelStatusResponseData extends ResponseData {
    private int[] channelStatus;

    public ChannelStatusResponseData(int[] channelStatus2) {
        this.channelStatus = channelStatus2;
    }

    @Override // com.android.internal.telephony.cat.ResponseData
    public void format(ByteArrayOutputStream buf) {
        if (this.channelStatus != null) {
            int tag = ComprehensionTlvTag.CHANNEL_STATUS.value();
            int[] iArr = this.channelStatus;
            for (int status : iArr) {
                if (status > 0) {
                    buf.write(tag);
                    buf.write(2);
                    buf.write((status >> 8) & 255);
                    buf.write(status & 255);
                }
            }
        }
    }
}
