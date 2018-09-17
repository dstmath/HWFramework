package com.android.internal.telephony.cat;

import android.graphics.Bitmap;

/* compiled from: CommandParams */
class LaunchBrowserParams extends CommandParams {
    TextMessage mConfirmMsg;
    LaunchBrowserMode mMode;
    String mUrl;

    LaunchBrowserParams(CommandDetails cmdDet, TextMessage confirmMsg, String url, LaunchBrowserMode mode) {
        super(cmdDet);
        this.mConfirmMsg = confirmMsg;
        this.mMode = mode;
        this.mUrl = url;
    }

    boolean setIcon(Bitmap icon) {
        if (icon == null || this.mConfirmMsg == null) {
            return false;
        }
        this.mConfirmMsg.icon = icon;
        return true;
    }

    public String toString() {
        return "TextMessage=" + this.mConfirmMsg + " " + super.toString();
    }
}
