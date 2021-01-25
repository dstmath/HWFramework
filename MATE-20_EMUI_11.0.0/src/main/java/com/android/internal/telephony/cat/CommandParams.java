package com.android.internal.telephony.cat;

import android.annotation.UnsupportedAppUsage;
import android.graphics.Bitmap;
import com.android.internal.telephony.cat.AppInterface;

public class CommandParams {
    @UnsupportedAppUsage
    CommandDetails mCmdDet;
    boolean mLoadIconFailed = false;

    @UnsupportedAppUsage
    CommandParams(CommandDetails cmdDet) {
        this.mCmdDet = cmdDet;
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
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
