package android.telephony;

import android.common.HwPartBaseTelephonyFactory;
import com.android.internal.telephony.HwBaseInnerSmsManager;
import com.android.internal.telephony.HwBaseInnerSmsManagerImpl;

public class HwPartBaseTelephonyFactoryImpl extends HwPartBaseTelephonyFactory {
    public HwBaseInnerSmsManager createHwBaseInnerSmsManager() {
        return HwBaseInnerSmsManagerImpl.getDefault();
    }

    public HwInnerTelephonyManager createHwInnerTelephonyManager() {
        return HwInnerTelephonyManagerImpl.getDefault();
    }
}
