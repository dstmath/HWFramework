package android.common;

import android.magicwin.DefaultHwMagicWindowManager;
import android.magicwin.HwMagicWindow;
import android.util.Log;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class HwPartMagicWindowFactory {
    public static final String MW_PART_FACTORY_IMPL_NAME = "android.common.HwPartMagicWindowFactoryImpl";
    private static final String TAG = "HwPartMagicWindowFactory";
    private static HwPartMagicWindowFactory sFactory;

    @HwSystemApi
    public static HwPartMagicWindowFactory loadFactory() {
        HwPartMagicWindowFactory hwPartMagicWindowFactory = sFactory;
        if (hwPartMagicWindowFactory != null) {
            return hwPartMagicWindowFactory;
        }
        Object object = FactoryLoader.loadFactory(MW_PART_FACTORY_IMPL_NAME);
        if (object == null || !(object instanceof HwPartMagicWindowFactory)) {
            sFactory = new HwPartMagicWindowFactory();
        } else {
            sFactory = (HwPartMagicWindowFactory) object;
        }
        if (sFactory != null) {
            Log.i(TAG, "add android.common.HwPartMagicWindowFactoryImpl to memory.");
            return sFactory;
        }
        throw new RuntimeException("Load PadEdu-MW Part Factory Instance Null failure");
    }

    @HwSystemApi
    public HwMagicWindow getHwMagicWindowManager() {
        return DefaultHwMagicWindowManager.getInstance();
    }
}
