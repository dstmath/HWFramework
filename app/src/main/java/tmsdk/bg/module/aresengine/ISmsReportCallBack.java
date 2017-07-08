package tmsdk.bg.module.aresengine;

import tmsdk.common.ErrorCode;
import tmsdkobf.fs;
import tmsdkobf.lg;

/* compiled from: Unknown */
public abstract class ISmsReportCallBack implements lg {
    public static final int TYPE_INTER_FOR_INTELLIGENT = 0;
    public static final int TYPE_INTER_FOR_OTHERS = 1;

    public void onFinish(int i, int i2, int i3, int i4, fs fsVar) {
        if (i3 != 0) {
            onReprotFinish(ErrorCode.fromESharkCode(i3));
        } else {
            onReprotFinish(TYPE_INTER_FOR_INTELLIGENT);
        }
    }

    public abstract void onReprotFinish(int i);
}
