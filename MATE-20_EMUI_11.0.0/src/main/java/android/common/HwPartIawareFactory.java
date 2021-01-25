package android.common;

import android.graphics.DefaultAwareBitmapCacherImpl;
import android.graphics.IAwareBitmapCacher;
import android.hwgallerycache.HwGalleryCacheManager;
import android.hwgallerycache.HwGalleryCacheManagerDummy;
import android.iawareperf.DefaultRtgSchedImpl;
import android.iawareperf.IHwRtgSchedImpl;
import android.rms.DefaultHwAppInnerBoostImpl;
import android.rms.IHwAppInnerBoost;
import android.rms.iaware.DefaultDynBufManager;
import android.rms.iaware.HwDynBufManager;
import android.util.Log;
import android.widget.DefaultHwWechatOptimizeImpl;
import android.widget.IHwWechatOptimize;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class HwPartIawareFactory {
    public static final String IAWARE_FACTORY_IMPL_NAME = "android.common.HwPartIawareFactoryImpl";
    private static final String TAG = "HwPartIawareFactory";
    private static HwPartIawareFactory sFactory;

    public static HwPartIawareFactory loadFactory(String factoryName) {
        HwPartIawareFactory hwPartIawareFactory = sFactory;
        if (hwPartIawareFactory != null) {
            return hwPartIawareFactory;
        }
        Object object = FactoryLoader.loadFactory(factoryName);
        if (object == null || !(object instanceof HwPartIawareFactory)) {
            sFactory = new HwPartIawareFactory();
        } else {
            sFactory = (HwPartIawareFactory) object;
        }
        if (sFactory != null) {
            Log.i(TAG, "add " + factoryName + " to memory.");
            return sFactory;
        }
        throw new RuntimeException("can't load any iaware factory");
    }

    public IAwareBitmapCacher getAwareBitmapCacherImpl() {
        return DefaultAwareBitmapCacherImpl.getDefault();
    }

    public IHwWechatOptimize getHwWechatOptimizeImpl() {
        return new DefaultHwWechatOptimizeImpl();
    }

    public IHwRtgSchedImpl getHwRtgSchedImpl() {
        return DefaultRtgSchedImpl.getInstance();
    }

    public IHwAppInnerBoost getHwAppInnerBoostImpl() {
        return DefaultHwAppInnerBoostImpl.getDefault();
    }

    public HwGalleryCacheManager.IHwGalleryCacheManager getGalleryCacheManagerInstance() {
        return HwGalleryCacheManagerDummy.getDefault();
    }

    public HwDynBufManager.IDynBufManager getDynBufManagerImpl() {
        return DefaultDynBufManager.getDefault();
    }
}
