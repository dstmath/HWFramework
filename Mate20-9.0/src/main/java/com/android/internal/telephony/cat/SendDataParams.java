package com.android.internal.telephony.cat;

/* compiled from: CommandParams */
class SendDataParams extends CommandParams {
    int channel = 0;
    byte[] data = null;
    TextMessage textMsg;

    SendDataParams(CommandDetails cmdDet, int channel2, byte[] data2, TextMessage textMsg2) {
        super(cmdDet);
        this.channel = channel2;
        this.data = data2;
        this.textMsg = textMsg2;
    }
}
