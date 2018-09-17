package com.android.internal.telephony.cat;

/* compiled from: CommandParams */
class SendDataParams extends CommandParams {
    int channel = 0;
    byte[] data = null;
    TextMessage textMsg;

    SendDataParams(CommandDetails cmdDet, int channel, byte[] data, TextMessage textMsg) {
        super(cmdDet);
        this.channel = channel;
        this.data = data;
        this.textMsg = textMsg;
    }
}
