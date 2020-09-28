package android.common;

import android.content.Context;
import android.pc.AbsHwDecorCaptionView;
import android.pc.DefaultHwPCManager;
import android.pc.HwPCManager;
import android.util.Log;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class HwPartPowerOfficeFactory {
    public static final String POWEROFFICE_FACTORY_IMPL_NAME = "android.common.HwPartPowerOfficeFactoryImpl";
    private static final String TAG = "HwPartPowerOfficeFactory";
    private static HwPartPowerOfficeFactory mFactory;

    @HwSystemApi
    public static HwPartPowerOfficeFactory loadFactory() {
        HwPartPowerOfficeFactory hwPartPowerOfficeFactory = mFactory;
        if (hwPartPowerOfficeFactory != null) {
            return hwPartPowerOfficeFactory;
        }
        Object object = FactoryLoader.loadFactory(POWEROFFICE_FACTORY_IMPL_NAME);
        if (object == null || !(object instanceof HwPartPowerOfficeFactory)) {
            mFactory = new HwPartPowerOfficeFactory();
        } else {
            mFactory = (HwPartPowerOfficeFactory) object;
        }
        Log.i(TAG, "add HwPartPowerOfficeFactoryImpl to memory.");
        return mFactory;
    }

    @HwSystemApi
    public HwPCManager getHwPCManager() {
        return DefaultHwPCManager.getDefault();
    }

    @HwSystemApi
    public AbsHwDecorCaptionView getHwDecorCaptionView(Context context) {
        return new AbsHwDecorCaptionView(context);
    }
}
