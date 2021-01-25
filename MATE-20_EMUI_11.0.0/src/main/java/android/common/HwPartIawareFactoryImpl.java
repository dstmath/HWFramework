package android.common;

import android.graphics.AwareBitmapCacher;
import android.graphics.IAwareBitmapCacher;
import android.iawareperf.IHwRtgSchedImpl;
import android.iawareperf.RtgSched;
import android.rms.HwAppInnerBoostImpl;
import android.rms.IHwAppInnerBoost;
import android.rms.iaware.DynBufManagerImpl;
import android.rms.iaware.HwDynBufManager;
import android.widget.IHwWechatOptimize;
import huawei.android.widget.HwWechatOptimizeImpl;

public class HwPartIawareFactoryImpl extends HwPartIawareFactory {
    public IAwareBitmapCacher getAwareBitmapCacherImpl() {
        return AwareBitmapCacher.getDefault();
    }

    public IHwWechatOptimize getHwWechatOptimizeImpl() {
        return HwWechatOptimizeImpl.getInstance();
    }

    public IHwRtgSchedImpl getHwRtgSchedImpl() {
        return RtgSched.getInstance();
    }

    public IHwAppInnerBoost getHwAppInnerBoostImpl() {
        return HwAppInnerBoostImpl.getDefault();
    }

    public HwDynBufManager.IDynBufManager getDynBufManagerImpl() {
        return DynBufManagerImpl.getDefault();
    }
}
