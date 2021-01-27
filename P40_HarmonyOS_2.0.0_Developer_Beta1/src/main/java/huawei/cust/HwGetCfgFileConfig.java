package huawei.cust;

import android.common.HwCfgKey;
import android.common.HwFrameworkFactory;
import android.util.Log;
import java.util.Map;

public class HwGetCfgFileConfig {
    private static final String TAG = "HwGetCfgFileConfig";
    private static IHwGetCfgFileConfig hwCarrierConfig = HwFrameworkFactory.getHwCfgFileConfig();

    public static Map getCfgFileMap(int slotId) {
        IHwGetCfgFileConfig iHwGetCfgFileConfig = hwCarrierConfig;
        if (iHwGetCfgFileConfig != null) {
            return iHwGetCfgFileConfig.getCfgFileMap(slotId);
        }
        Log.e(TAG, "getCfgFileMap: Error: hwCarrierConfigPolicy is null");
        return null;
    }

    public static <T> T getCfgFileData(HwCfgKey keyCollection, Class<T> clazz) {
        IHwGetCfgFileConfig iHwGetCfgFileConfig = hwCarrierConfig;
        if (iHwGetCfgFileConfig != null) {
            return (T) iHwGetCfgFileConfig.getCfgFileData(keyCollection, clazz);
        }
        Log.e(TAG, "getCfgFileData: Error: hwCarrierConfigPolicy is null");
        return null;
    }

    public static void clearCfgFileConfig(int slotId) {
        IHwGetCfgFileConfig iHwGetCfgFileConfig = hwCarrierConfig;
        if (iHwGetCfgFileConfig == null) {
            Log.e(TAG, "clearCfgFileConfig: Error: hwCarrierConfigPolicy is null");
        } else {
            iHwGetCfgFileConfig.clearCfgFileConfig(slotId);
        }
    }

    public static void readCfgFileConfig(String fileName, int slotId) {
        IHwGetCfgFileConfig iHwGetCfgFileConfig = hwCarrierConfig;
        if (iHwGetCfgFileConfig == null) {
            Log.e(TAG, "readCfgFileConfig: Error: hwCarrierConfigPolicy is null");
        } else {
            iHwGetCfgFileConfig.readCfgFileConfig(fileName, slotId);
        }
    }
}
