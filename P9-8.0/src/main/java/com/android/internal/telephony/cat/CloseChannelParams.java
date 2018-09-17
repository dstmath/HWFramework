package com.android.internal.telephony.cat;

/* compiled from: CommandParams */
class CloseChannelParams extends CommandParams {
    TextMessage alertMsg = null;
    int channel = 0;

    CloseChannelParams(CommandDetails cmdDet, TextMessage alertMsg, int channel) {
        super(cmdDet);
        this.alertMsg = alertMsg;
        this.channel = channel;
    }
}
