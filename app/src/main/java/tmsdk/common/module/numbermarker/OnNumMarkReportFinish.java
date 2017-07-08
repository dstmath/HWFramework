package tmsdk.common.module.numbermarker;

import tmsdk.common.ErrorCode;
import tmsdkobf.fs;
import tmsdkobf.lg;

/* compiled from: Unknown */
public abstract class OnNumMarkReportFinish implements lg {
    public void onFinish(int i, int i2, int i3, int i4, fs fsVar) {
        onReportFinish(ErrorCode.fromESharkCode(i3));
    }

    public abstract void onReportFinish(int i);
}
