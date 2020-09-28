package android.telephony;

import com.android.internal.telephony.HwInnerVSimManager;
import com.android.internal.telephony.HwInnerVSimManagerImpl;
import com.android.internal.telephony.HwPartTelephonyFactory;

public class HwPartTelephonyVSimFactoryImpl extends HwPartTelephonyFactory {
    public HwInnerVSimManager createHwInnerVSimManager() {
        return HwInnerVSimManagerImpl.getDefault();
    }
}
