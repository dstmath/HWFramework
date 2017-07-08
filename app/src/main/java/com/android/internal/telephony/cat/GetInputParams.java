package com.android.internal.telephony.cat;

import android.graphics.Bitmap;

/* compiled from: CommandParams */
class GetInputParams extends CommandParams {
    Input mInput;

    GetInputParams(CommandDetails cmdDet, Input input) {
        super(cmdDet);
        this.mInput = null;
        this.mInput = input;
    }

    boolean setIcon(Bitmap icon) {
        if (!(icon == null || this.mInput == null)) {
            this.mInput.icon = icon;
        }
        return true;
    }
}
