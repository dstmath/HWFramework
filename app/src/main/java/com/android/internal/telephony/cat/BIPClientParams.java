package com.android.internal.telephony.cat;

import android.graphics.Bitmap;

/* compiled from: CommandParams */
class BIPClientParams extends CommandParams {
    boolean mHasAlphaId;
    TextMessage mTextMsg;

    BIPClientParams(CommandDetails cmdDet, TextMessage textMsg, boolean has_alpha_id) {
        super(cmdDet);
        this.mTextMsg = textMsg;
        this.mHasAlphaId = has_alpha_id;
    }

    boolean setIcon(Bitmap icon) {
        if (icon == null || this.mTextMsg == null) {
            return false;
        }
        this.mTextMsg.icon = icon;
        return true;
    }
}
