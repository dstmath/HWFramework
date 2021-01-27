package android.telephony;

import android.content.Context;
import com.huawei.internal.telephony.HwCommonPhoneCallback;

public class HwTelephonyManagerInnerUtils {
    private static final Object LOCK = new Object();
    private static HwTelephonyManagerInnerUtils sInstance;

    public static synchronized HwTelephonyManagerInnerUtils getDefault() {
        HwTelephonyManagerInnerUtils hwTelephonyManagerInnerUtils;
        synchronized (HwTelephonyManagerInnerUtils.class) {
            if (sInstance == null) {
                sInstance = new HwTelephonyManagerInnerUtils();
            }
            hwTelephonyManagerInnerUtils = sInstance;
        }
        return hwTelephonyManagerInnerUtils;
    }

    public String getPesn() {
        return HwTelephonyManagerInner.getDefault().getPesn();
    }

    public int getNrLevel(CellSignalStrengthNr strength) {
        return HwTelephonyManagerInner.getDefault().getNrLevel(strength);
    }

    public void printCallingAppNameInfo(boolean isEnabled, Context context) {
        HwTelephonyManagerInner.getDefault().printCallingAppNameInfo(isEnabled, context);
    }

    public String getUniqueDeviceId(int scope, String callingPackageName) {
        return HwTelephonyManagerInner.getDefault().getUniqueDeviceId(scope, callingPackageName);
    }

    public String getCallingAppName(Context context) {
        return HwTelephonyManagerInner.getDefault().getCallingAppName(context);
    }

    public int getLevelHw(CellSignalStrength signalStrength) {
        return HwTelephonyManagerInner.getDefault().getLevelHw(signalStrength);
    }

    public int getEvdoLevel(CellSignalStrengthCdma signalStrength) {
        return HwTelephonyManagerInner.getDefault().getEvdoLevel(signalStrength);
    }

    public int getCdmaLevel(CellSignalStrengthCdma signalStrength) {
        return HwTelephonyManagerInner.getDefault().getCdmaLevel(signalStrength);
    }

    public boolean isNrSlicesSupported() {
        return HwTelephonyManagerInner.getDefault().isNrSlicesSupported();
    }

    public String getCtOperator(int slotId, String operator) {
        return HwTelephonyManagerInner.getDefault().getCTOperator(slotId, operator);
    }

    public boolean registerForRadioStateChanged(HwCommonPhoneCallback callback) {
        return HwTelephonyManagerInner.getDefault().registerForRadioStateChanged(callback);
    }

    public boolean unregisterForRadioStateChanged(HwCommonPhoneCallback callback) {
        return HwTelephonyManagerInner.getDefault().unregisterForRadioStateChanged(callback);
    }

    public boolean isDualNrSupported() {
        return HwTelephonyManagerInner.getDefault().isDualNrSupported();
    }
}
