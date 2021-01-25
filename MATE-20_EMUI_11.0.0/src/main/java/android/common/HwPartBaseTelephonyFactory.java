package android.common;

import android.telephony.DefaultHwInnerTelephonyManager;
import android.telephony.HwInnerTelephonyManager;
import android.util.Log;
import com.android.internal.telephony.DefaultHwBaseInnerSmsManager;
import com.android.internal.telephony.HwBaseInnerSmsManager;

public class HwPartBaseTelephonyFactory {
    private static final String TAG = "HwPartBaseTelephonyFactory";
    private static final String TELEPHONY_FACTORY_IMPL_NAME = "android.telephony.HwPartBaseTelephonyFactoryImpl";
    private static HwPartBaseTelephonyFactory mFactory;

    public static HwPartBaseTelephonyFactory loadFactory() {
        HwPartBaseTelephonyFactory hwPartBaseTelephonyFactory = mFactory;
        if (hwPartBaseTelephonyFactory != null) {
            return hwPartBaseTelephonyFactory;
        }
        Object object = FactoryLoader.loadFactory(TELEPHONY_FACTORY_IMPL_NAME);
        if (object == null || !(object instanceof HwPartBaseTelephonyFactory)) {
            mFactory = new HwPartBaseTelephonyFactory();
        } else {
            mFactory = (HwPartBaseTelephonyFactory) object;
        }
        if (mFactory != null) {
            Log.i(TAG, "add HwPartBaseTelephonyFactoryImpl to memory.");
            return mFactory;
        }
        throw new RuntimeException("can't load any telephony factory");
    }

    public HwBaseInnerSmsManager createHwBaseInnerSmsManager() {
        Log.d(TAG, "createHwBaseInnerSmsManager");
        return DefaultHwBaseInnerSmsManager.getDefault();
    }

    public HwInnerTelephonyManager createHwInnerTelephonyManager() {
        Log.d(TAG, "createHwInnerTelephonyManager");
        return DefaultHwInnerTelephonyManager.getDefault();
    }
}
