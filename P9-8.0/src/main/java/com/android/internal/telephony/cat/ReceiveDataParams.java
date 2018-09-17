package com.android.internal.telephony.cat;

/* compiled from: CommandParams */
class ReceiveDataParams extends CommandParams {
    int channel = 0;
    int datLen = 0;
    TextMessage textMsg;

    ReceiveDataParams(CommandDetails cmdDet, int channel, int datLen, TextMessage textMsg) {
        super(cmdDet);
        this.channel = channel;
        this.datLen = datLen;
        this.textMsg = textMsg;
    }
}
