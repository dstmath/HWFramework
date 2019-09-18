package huawei.cust;

import android.common.HwCfgKey;
import android.common.HwFrameworkFactory;
import android.util.Log;
import java.util.Map;

public class HwGetCfgFileConfig {
    private static String TAG = "HwGetCfgFileConfig";
    private static IHwGetCfgFileConfig hwCarrierConfig = HwFrameworkFactory.getHwCfgFileConfig();

    public static Map getCfgFileMap(int slotId) {
        if (hwCarrierConfig != null) {
            return hwCarrierConfig.getCfgFileMap(slotId);
        }
        Log.e(TAG, "Error: hwCarrierConfigPolicy is null");
        return null;
    }

    public static <T> T getCfgFileData(HwCfgKey keyCollection, Class<T> clazz) {
        if (hwCarrierConfig != null) {
            return hwCarrierConfig.getCfgFileData(keyCollection, clazz);
        }
        Log.e(TAG, "Error: hwCarrierConfigPolicy is null");
        return null;
    }

    public static void clearCfgFileConfig(int slotId) {
        if (hwCarrierConfig == null) {
            Log.e(TAG, "Error: hwCarrierConfigPolicy is null");
        } else {
            hwCarrierConfig.clearCfgFileConfig(slotId);
        }
    }

    public static void readCfgFileConfig(String fileName, int slotId) {
        if (hwCarrierConfig == null) {
            Log.e(TAG, "Error: hwCarrierConfigPolicy is null");
        } else {
            hwCarrierConfig.readCfgFileConfig(fileName, slotId);
        }
    }
}
