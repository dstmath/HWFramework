package com.android.internal.telephony.cat;

import android.graphics.Bitmap;

/* compiled from: CommandParams */
class DisplayTextParams extends CommandParams {
    TextMessage mTextMsg;

    DisplayTextParams(CommandDetails cmdDet, TextMessage textMsg) {
        super(cmdDet);
        this.mTextMsg = textMsg;
    }

    boolean setIcon(Bitmap icon) {
        if (icon == null || this.mTextMsg == null) {
            return false;
        }
        this.mTextMsg.icon = icon;
        return true;
    }

    public String toString() {
        return "TextMessage=" + this.mTextMsg + " " + super.toString();
    }
}
