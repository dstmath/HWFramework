package com.android.internal.telephony.cat;

/* compiled from: CommandParams */
class CloseChannelParams extends CommandParams {
    TextMessage alertMsg;
    int channel;

    CloseChannelParams(CommandDetails cmdDet, TextMessage alertMsg, int channel) {
        super(cmdDet);
        this.alertMsg = null;
        this.channel = 0;
        this.alertMsg = alertMsg;
        this.channel = channel;
    }
}
