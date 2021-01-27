package com.android.internal.telephony.cat;

import android.annotation.UnsupportedAppUsage;
import android.graphics.Bitmap;

/* access modifiers changed from: package-private */
/* compiled from: CommandParams */
public class PlayToneParams extends CommandParams {
    ToneSettings mSettings;
    TextMessage mTextMsg;

    @UnsupportedAppUsage
    PlayToneParams(CommandDetails cmdDet, TextMessage textMsg, Tone tone, Duration duration, boolean vibrate) {
        super(cmdDet);
        this.mTextMsg = textMsg;
        this.mSettings = new ToneSettings(duration, tone, vibrate);
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.internal.telephony.cat.CommandParams
    public boolean setIcon(Bitmap icon) {
        TextMessage textMessage;
        if (icon == null || (textMessage = this.mTextMsg) == null) {
            return false;
        }
        textMessage.icon = icon;
        return true;
    }
}
