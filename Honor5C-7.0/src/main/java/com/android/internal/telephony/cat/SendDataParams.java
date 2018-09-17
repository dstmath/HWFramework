package com.android.internal.telephony.cat;

/* compiled from: CommandParams */
class SendDataParams extends CommandParams {
    int channel;
    byte[] data;
    TextMessage textMsg;

    SendDataParams(CommandDetails cmdDet, int channel, byte[] data, TextMessage textMsg) {
        super(cmdDet);
        this.data = null;
        this.channel = 0;
        this.channel = channel;
        this.data = data;
        this.textMsg = textMsg;
    }
}
