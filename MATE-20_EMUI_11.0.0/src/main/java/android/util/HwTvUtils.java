package android.util;

import android.os.RemoteException;
import android.os.SystemProperties;
import com.android.internal.telephony.PhoneConstants;
import java.util.HashMap;
import java.util.Map;
import vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface;

public class HwTvUtils {
    private static final String DISABLE_BOOT_TIME_OPT = "0";
    private static final String ENABLE_BOOT_TIME_OPT = "1";
    private static final int FACTORY_OEM_DATA_SIZE = 1;
    private static final int FACTORY_OEM_MAIN_ID = 204;
    private static final int FACTORY_OEM_SUB_ID = 45;
    private static final boolean IS_TV = "tv".equals(SystemProperties.get("ro.build.characteristics", PhoneConstants.APN_TYPE_DEFAULT));
    private static final String TAG = "HwTvUtils";

    public static boolean isBootTimeOpt() {
        if (!IS_TV) {
            Slog.i(TAG, "isBootTimeOpt not tv");
            return false;
        }
        try {
            IHwFactoryInterface hwFactoryInterface = IHwFactoryInterface.getService("hwfactoryinterface_hal");
            if (hwFactoryInterface == null) {
                Slog.e(TAG, "isBootTimeOpt read failed!");
                return false;
            }
            final Map<String, Object> result = new HashMap<>();
            result.put("ret", -1);
            hwFactoryInterface.oeminfo_Read_reused(204, 45, 1, new IHwFactoryInterface.oeminfo_Read_reusedCallback() {
                /* class android.util.HwTvUtils.AnonymousClass1 */

                @Override // vendor.huawei.hardware.hwfactoryinterface.V1_1.IHwFactoryInterface.oeminfo_Read_reusedCallback
                public void onValues(int ret, String out) {
                    result.put("ret", Integer.valueOf(ret));
                    result.put("out", out);
                }
            });
            Object retFlag = result.get("ret");
            Object output = result.get("out");
            if (!(retFlag instanceof Integer) || !(output instanceof String)) {
                return false;
            }
            String oemInfoValue = (String) output;
            if (((Integer) retFlag).intValue() != 0 || oemInfoValue == null) {
                return false;
            }
            Slog.i(TAG, "isBootTimeOpt is " + oemInfoValue);
            if (!oemInfoValue.startsWith("1")) {
                return false;
            }
            Slog.i(TAG, "isBootTimeOpt enable, need disable right now");
            if (hwFactoryInterface.oeminfo_write_reused(204, 45, "0".length(), "0") != 0) {
                Slog.e(TAG, "isBootTimeOpt set disable failed");
            }
            return true;
        } catch (RemoteException e) {
            Slog.e(TAG, "read isBootTimeOpt exception");
            return false;
        }
    }
}
