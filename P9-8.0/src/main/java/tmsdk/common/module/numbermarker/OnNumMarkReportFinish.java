package tmsdk.common.module.numbermarker;

import com.qq.taf.jce.JceStruct;
import tmsdk.common.NumMarker;
import tmsdk.common.utils.f;
import tmsdkobf.jy;

public abstract class OnNumMarkReportFinish implements jy {
    public void onFinish(int i, int i2, int i3, int i4, JceStruct jceStruct) {
        f.f(NumMarker.Tag, "cloudReportPhoneNum-retCode[" + i3 + "]");
        if (i3 != 0) {
            onReportFinish(i3);
        } else {
            onReportFinish(0);
        }
    }

    public abstract void onReportFinish(int i);
}
