package com.android.internal.telephony.cat;

import android.graphics.Bitmap;

/* access modifiers changed from: package-private */
/* compiled from: CommandParams */
public class CallSetupParams extends CommandParams {
    TextMessage mCallMsg;
    TextMessage mConfirmMsg;

    CallSetupParams(CommandDetails cmdDet, TextMessage confirmMsg, TextMessage callMsg) {
        super(cmdDet);
        this.mConfirmMsg = confirmMsg;
        this.mCallMsg = callMsg;
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.internal.telephony.cat.CommandParams
    public boolean setIcon(Bitmap icon) {
        if (icon == null) {
            return false;
        }
        TextMessage textMessage = this.mConfirmMsg;
        if (textMessage == null || textMessage.icon != null) {
            TextMessage textMessage2 = this.mCallMsg;
            if (textMessage2 == null || textMessage2.icon != null) {
                return false;
            }
            this.mCallMsg.icon = icon;
            return true;
        }
        this.mConfirmMsg.icon = icon;
        return true;
    }
}
