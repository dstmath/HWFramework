package ohos.global.huaweicust;

import com.huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class CfgFilePolicy {
    public static final int CUST_TYPE_CONFIG = 0;
    public static final int CUST_TYPE_MEDIA = 1;
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218111488, "CfgFilePolicy");

    public static ArrayList<File> getCfgFileList(String str, int i) {
        try {
            return HwCfgFilePolicy.getCfgFileList(str, i);
        } catch (NoClassDefFoundError unused) {
            HiLog.error(LABEL, "HwCfgFilePolicy NoClassDefFoundError", new Object[0]);
            return null;
        }
    }

    public static File getCfgFile(String str, int i) {
        try {
            return HwCfgFilePolicy.getCfgFile(str, i);
        } catch (NoClassDefFoundError unused) {
            HiLog.error(LABEL, "HwCfgFilePolicy NoClassDefFoundError", new Object[0]);
            return null;
        }
    }

    public static String[] getCfgPolicyDir(int i) {
        try {
            return HwCfgFilePolicy.getCfgPolicyDir(i);
        } catch (NoClassDefFoundError unused) {
            HiLog.error(LABEL, "HwCfgFilePolicy NoClassDefFoundError", new Object[0]);
            return null;
        }
    }

    public static String getOpKey() {
        return HwCfgFilePolicy.getOpKey();
    }

    public static String getOpKey(int i) {
        return HwCfgFilePolicy.getOpKey(i);
    }

    public static <T> T getValue(String str, Class<T> cls) {
        return (T) HwCfgFilePolicy.getValue(str, cls);
    }

    public static <T> T getValue(String str, int i, Class<T> cls) {
        return (T) HwCfgFilePolicy.getValue(str, i, cls);
    }

    public static Map getFileConfig(String str) {
        return HwCfgFilePolicy.getFileConfig(str);
    }

    public static Map getFileConfig(String str, int i) {
        return HwCfgFilePolicy.getFileConfig(str, i);
    }

    public static ArrayList<File> getCfgFileList(String str, int i, int i2) {
        try {
            return HwCfgFilePolicy.getCfgFileList(str, i, i2);
        } catch (NoClassDefFoundError unused) {
            HiLog.error(LABEL, "HwCfgFilePolicy NoClassDefFoundError", new Object[0]);
            return null;
        }
    }

    public static File getCfgFile(String str, int i, int i2) {
        try {
            return HwCfgFilePolicy.getCfgFile(str, i, i2);
        } catch (NoClassDefFoundError unused) {
            HiLog.error(LABEL, "HwCfgFilePolicy NoClassDefFoundError", new Object[0]);
            return null;
        }
    }

    public static String[] getCfgPolicyDir(int i, int i2) {
        try {
            return HwCfgFilePolicy.getCfgPolicyDir(i, i2);
        } catch (NoClassDefFoundError unused) {
            HiLog.error(LABEL, "HwCfgFilePolicy NoClassDefFoundError", new Object[0]);
            return null;
        }
    }
}
