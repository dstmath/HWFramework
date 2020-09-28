package android.common;

import android.location.DefaultHwInnerLocationManager;
import android.location.IHwInnerLocationManager;
import android.util.Log;

public class HwPartBaseLocationFactory {
    private static final String LOCATION_FACTORY_IMPL_NAME = "android.location.HwPartBaseLocationFactoryImpl";
    private static final String TAG = "HwPartBaseLocationFactory";
    private static HwPartBaseLocationFactory mFactory;

    public static HwPartBaseLocationFactory loadFactory() {
        HwPartBaseLocationFactory hwPartBaseLocationFactory = mFactory;
        if (hwPartBaseLocationFactory != null) {
            return hwPartBaseLocationFactory;
        }
        Object object = FactoryLoader.loadFactory(LOCATION_FACTORY_IMPL_NAME);
        if (object == null || !(object instanceof HwPartBaseLocationFactory)) {
            mFactory = new HwPartBaseLocationFactory();
        } else {
            mFactory = (HwPartBaseLocationFactory) object;
        }
        if (mFactory != null) {
            Log.i(TAG, "add HwPartBaseLocationFactoryImpl to memory.");
            return mFactory;
        }
        throw new RuntimeException("can't load any location factory");
    }

    public IHwInnerLocationManager createHwInnerLocationManager() {
        Log.d(TAG, "createHwInnerLocationManager");
        return DefaultHwInnerLocationManager.getDefault();
    }
}
