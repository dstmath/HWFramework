package tmsdk.bg.module.aresengine;

import com.qq.taf.jce.JceStruct;
import tmsdk.common.ErrorCode;
import tmsdkobf.jy;

public abstract class ISmsReportCallBack implements jy {
    public static final int TYPE_INTER_FOR_INTELLIGENT = 0;
    public static final int TYPE_INTER_FOR_OTHERS = 1;

    public void onFinish(int i, int i2, int i3, int i4, JceStruct jceStruct) {
        if (i3 != 0) {
            onReprotFinish(ErrorCode.fromESharkCode(i3));
        } else {
            onReprotFinish(0);
        }
    }

    public abstract void onReprotFinish(int i);
}
