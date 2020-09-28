package com.android.internal.telephony.cat;

import android.graphics.Bitmap;

/* access modifiers changed from: package-private */
/* compiled from: CommandParams */
public class BIPClientParams extends CommandParams {
    boolean mHasAlphaId;
    TextMessage mTextMsg;

    BIPClientParams(CommandDetails cmdDet, TextMessage textMsg, boolean has_alpha_id) {
        super(cmdDet);
        this.mTextMsg = textMsg;
        this.mHasAlphaId = has_alpha_id;
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
