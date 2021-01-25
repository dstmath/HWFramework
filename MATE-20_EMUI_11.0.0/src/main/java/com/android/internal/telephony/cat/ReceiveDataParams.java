package com.android.internal.telephony.cat;

/* access modifiers changed from: package-private */
/* compiled from: CommandParams */
public class ReceiveDataParams extends CommandParams {
    int channel = 0;
    int datLen = 0;
    TextMessage textMsg;

    ReceiveDataParams(CommandDetails cmdDet, int channel2, int datLen2, TextMessage textMsg2) {
        super(cmdDet);
        this.channel = channel2;
        this.datLen = datLen2;
        this.textMsg = textMsg2;
    }
}
