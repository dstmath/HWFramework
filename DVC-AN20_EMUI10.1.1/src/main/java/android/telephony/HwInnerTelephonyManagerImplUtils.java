package android.telephony;

import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class HwInnerTelephonyManagerImplUtils {
    public static final int inRangeOrUnavailable(int value, int rangeMin, int rangeMax) {
        return CellSignalStrength.inRangeOrUnavailable(value, rangeMin, rangeMax);
    }

    public static final int inRangeOrUnavailable(int value, int rangeMin, int rangeMax, int special) {
        return CellSignalStrength.inRangeOrUnavailable(value, rangeMin, rangeMax, special);
    }
}
