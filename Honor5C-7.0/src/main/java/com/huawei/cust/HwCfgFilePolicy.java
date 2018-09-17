package com.huawei.cust;

import java.io.File;
import java.util.ArrayList;

public class HwCfgFilePolicy {
    public static final int BASE = 3;
    public static final int CLOUD_APN = 7;
    public static final int CLOUD_DPLMN = 6;
    public static final int CLOUD_MCC = 5;
    public static final int CUST = 4;
    public static final int CUST_TYPE_CONFIG = 0;
    public static final int CUST_TYPE_MEDIA = 1;
    public static final int EMUI = 1;
    public static final int GLOBAL = 0;
    public static final int PC = 2;

    public static ArrayList<File> getCfgFileList(String fileName, int type) throws NoClassDefFoundError {
        return huawei.cust.HwCfgFilePolicy.getCfgFileList(fileName, type);
    }

    public static File getCfgFile(String fileName, int type) throws NoClassDefFoundError {
        return huawei.cust.HwCfgFilePolicy.getCfgFile(fileName, type);
    }

    public static String[] getCfgPolicyDir(int type) throws NoClassDefFoundError {
        return huawei.cust.HwCfgFilePolicy.getCfgPolicyDir(type);
    }

    public static String getCfgVersion(int cfgType) throws NoClassDefFoundError {
        return huawei.cust.HwCfgFilePolicy.getCfgVersion(cfgType);
    }

    public static String[] getDownloadCfgFile(String verDir, String filePath) throws NoClassDefFoundError {
        return huawei.cust.HwCfgFilePolicy.getDownloadCfgFile(verDir, filePath);
    }
}
