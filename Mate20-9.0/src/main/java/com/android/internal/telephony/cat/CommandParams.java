package com.android.internal.telephony.cat;

import android.graphics.Bitmap;
import com.android.internal.telephony.cat.AppInterface;

public class CommandParams {
    CommandDetails mCmdDet;
    boolean mLoadIconFailed = false;

    CommandParams(CommandDetails cmdDet) {
        this.mCmdDet = cmdDet;
    }

    /* access modifiers changed from: package-private */
    public AppInterface.CommandType getCommandType() {
        return AppInterface.CommandType.fromInt(this.mCmdDet.typeOfCommand);
    }

    /* access modifiers changed from: package-private */
    public boolean setIcon(Bitmap icon) {
        return true;
    }

    public String toString() {
        return this.mCmdDet.toString();
    }
}
