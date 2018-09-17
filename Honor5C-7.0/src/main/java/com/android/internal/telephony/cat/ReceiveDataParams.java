package com.android.internal.telephony.cat;

/* compiled from: CommandParams */
class ReceiveDataParams extends CommandParams {
    int channel;
    int datLen;
    TextMessage textMsg;

    ReceiveDataParams(CommandDetails cmdDet, int channel, int datLen, TextMessage textMsg) {
        super(cmdDet);
        this.datLen = 0;
        this.channel = 0;
        this.channel = channel;
        this.datLen = datLen;
        this.textMsg = textMsg;
    }
}
