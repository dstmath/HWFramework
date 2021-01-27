package huawei.cust;

import android.common.HwCfgKey;
import java.util.Map;

public interface IHwGetCfgFileConfig {
    void clearCfgFileConfig(int i);

    <T> T getCfgFileData(HwCfgKey hwCfgKey, Class<T> cls);

    Map getCfgFileMap(int i);

    void readCfgFileConfig(String str, int i);
}
