package com.android.internal.telephony.cat;

import android.graphics.Bitmap;

/* compiled from: CommandParams */
class CallSetupParams extends CommandParams {
    TextMessage mCallMsg;
    TextMessage mConfirmMsg;

    CallSetupParams(CommandDetails cmdDet, TextMessage confirmMsg, TextMessage callMsg) {
        super(cmdDet);
        this.mConfirmMsg = confirmMsg;
        this.mCallMsg = callMsg;
    }

    boolean setIcon(Bitmap icon) {
        if (icon == null) {
            return false;
        }
        if (this.mConfirmMsg != null && this.mConfirmMsg.icon == null) {
            this.mConfirmMsg.icon = icon;
            return true;
        } else if (this.mCallMsg == null || this.mCallMsg.icon != null) {
            return false;
        } else {
            this.mCallMsg.icon = icon;
            return true;
        }
    }
}
