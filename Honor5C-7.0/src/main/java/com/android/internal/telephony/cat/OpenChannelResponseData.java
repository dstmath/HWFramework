package com.android.internal.telephony.cat;

import com.google.android.mms.pdu.PduHeaders;
import java.io.ByteArrayOutputStream;

/* compiled from: ResponseData */
class OpenChannelResponseData extends ResponseData {
    protected static final int BEARER_TYPE_E_UTRAN = 11;
    protected static final int E_UTRAN_BEARER_PARAMETERS_LEN = 10;
    protected static final int PDN_TYPE_INDEX = 9;
    protected static final int QCI_INDEX = 0;
    protected static final int SPECIAL_QCI = 9;
    private BearerDescription bearer;
    private int bufSize;
    private Integer channelStatus;

    public OpenChannelResponseData(int bufSize, Integer channelStatus, BearerDescription bearer) {
        this.bufSize = bufSize;
        this.channelStatus = channelStatus;
        this.bearer = bearer;
    }

    public void format(ByteArrayOutputStream buf) {
        if (this.channelStatus != null) {
            buf.write(ComprehensionTlvTag.CHANNEL_STATUS.value());
            buf.write(2);
            buf.write((this.channelStatus.intValue() >> 8) & PduHeaders.STORE_STATUS_ERROR_END);
            buf.write(this.channelStatus.intValue() & PduHeaders.STORE_STATUS_ERROR_END);
        }
        if (this.bearer != null) {
            buf.write(ComprehensionTlvTag.BEARER_DESC.value());
            if (this.bearer.parameters != null) {
                int len;
                boolean skipExtenedBitRateValues = false;
                if ((this.bearer.type.value() & PduHeaders.STORE_STATUS_ERROR_END) == BEARER_TYPE_E_UTRAN && this.bearer.parameters.length >= E_UTRAN_BEARER_PARAMETERS_LEN && (byte) 9 == this.bearer.parameters[QCI_INDEX]) {
                    skipExtenedBitRateValues = true;
                }
                CatLog.d((Object) this, "skipExtenedBitRateValues:" + skipExtenedBitRateValues);
                if (skipExtenedBitRateValues) {
                    len = 3;
                } else {
                    len = this.bearer.parameters.length + 1;
                }
                buf.write(len);
                buf.write(this.bearer.type.value() & PduHeaders.STORE_STATUS_ERROR_END);
                if (skipExtenedBitRateValues) {
                    buf.write(this.bearer.parameters[QCI_INDEX]);
                    buf.write(this.bearer.parameters[SPECIAL_QCI]);
                } else {
                    buf.write(this.bearer.parameters, QCI_INDEX, this.bearer.parameters.length);
                }
            } else {
                buf.write(1);
                buf.write(this.bearer.type.value() & PduHeaders.STORE_STATUS_ERROR_END);
            }
        }
        buf.write(ComprehensionTlvTag.BUFFER_SIZE.value());
        buf.write(2);
        buf.write((this.bufSize >> 8) & PduHeaders.STORE_STATUS_ERROR_END);
        buf.write(this.bufSize & PduHeaders.STORE_STATUS_ERROR_END);
    }
}
