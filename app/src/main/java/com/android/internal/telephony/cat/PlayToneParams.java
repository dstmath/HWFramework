package com.android.internal.telephony.cat;

import android.graphics.Bitmap;

/* compiled from: CommandParams */
class PlayToneParams extends CommandParams {
    ToneSettings mSettings;
    TextMessage mTextMsg;

    PlayToneParams(CommandDetails cmdDet, TextMessage textMsg, Tone tone, Duration duration, boolean vibrate) {
        super(cmdDet);
        this.mTextMsg = textMsg;
        this.mSettings = new ToneSettings(duration, tone, vibrate);
    }

    boolean setIcon(Bitmap icon) {
        if (icon == null || this.mTextMsg == null) {
            return false;
        }
        this.mTextMsg.icon = icon;
        return true;
    }
}
