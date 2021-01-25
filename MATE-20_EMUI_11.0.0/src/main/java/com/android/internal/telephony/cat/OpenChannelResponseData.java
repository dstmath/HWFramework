package com.android.internal.telephony.cat;

import java.io.ByteArrayOutputStream;

/* access modifiers changed from: package-private */
/* compiled from: ResponseData */
public class OpenChannelResponseData extends ResponseData {
    protected static final int BEARER_TYPE_E_UTRAN = 11;
    protected static final int E_UTRAN_BEARER_PARAMETERS_LEN = 10;
    protected static final int PDN_TYPE_INDEX = 9;
    protected static final int QCI_INDEX = 0;
    protected static final int SPECIAL_QCI = 9;
    private BearerDescription bearer;
    private int bufSize;
    private Integer channelStatus;

    public OpenChannelResponseData(int bufSize2, Integer channelStatus2, BearerDescription bearer2) {
        this.bufSize = bufSize2;
        this.channelStatus = channelStatus2;
        this.bearer = bearer2;
    }

    @Override // com.android.internal.telephony.cat.ResponseData
    public void format(ByteArrayOutputStream buf) {
        int len;
        if (this.channelStatus != null) {
            buf.write(ComprehensionTlvTag.CHANNEL_STATUS.value());
            buf.write(2);
            buf.write((this.channelStatus.intValue() >> 8) & 255);
            buf.write(this.channelStatus.intValue() & 255);
        }
        if (this.bearer != null) {
            buf.write(ComprehensionTlvTag.BEARER_DESC.value());
            if (this.bearer.parameters != null) {
                boolean skipExtenedBitRateValues = false;
                if ((this.bearer.type.value() & 255) == 11 && this.bearer.parameters.length >= 10 && 9 == this.bearer.parameters[0]) {
                    skipExtenedBitRateValues = true;
                }
                CatLog.d(this, "skipExtenedBitRateValues:" + skipExtenedBitRateValues);
                if (skipExtenedBitRateValues) {
                    len = 1 + 2;
                } else {
                    len = 1 + this.bearer.parameters.length;
                }
                buf.write(len);
                buf.write(this.bearer.type.value() & 255);
                if (skipExtenedBitRateValues) {
                    buf.write(this.bearer.parameters[0]);
                    buf.write(this.bearer.parameters[9]);
                } else {
                    buf.write(this.bearer.parameters, 0, this.bearer.parameters.length);
                }
            } else {
                buf.write(1);
                buf.write(this.bearer.type.value() & 255);
            }
        }
        buf.write(ComprehensionTlvTag.BUFFER_SIZE.value());
        buf.write(2);
        buf.write((this.bufSize >> 8) & 255);
        buf.write(this.bufSize & 255);
    }
}
