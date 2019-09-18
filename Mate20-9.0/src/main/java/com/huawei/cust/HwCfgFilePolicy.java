package com.huawei.cust;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

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
    public static final String HW_ACTION_CARRIER_CONFIG_CHANGED = "com.huawei.action.CARRIER_CONFIG_CHANGED";
    public static final String HW_CARRIER_CONFIG_CHANGE_STATE = "state";
    public static final String HW_CARRIER_CONFIG_OPKEY = "opkey";
    public static final String HW_CARRIER_CONFIG_SLOT = "slot";
    public static final int HW_CONFIG_STATE_PARA_UPDATE = 3;
    public static final int HW_CONFIG_STATE_SIM_ABSENT = 2;
    public static final int HW_CONFIG_STATE_SIM_LOADED = 1;
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

    public static String getOpKey() {
        return huawei.cust.HwCfgFilePolicy.getOpKey();
    }

    public static String getOpKey(int slotId) {
        return huawei.cust.HwCfgFilePolicy.getOpKey(slotId);
    }

    public static <T> T getValue(String key, Class<T> clazz) {
        return huawei.cust.HwCfgFilePolicy.getValue(key, clazz);
    }

    public static <T> T getValue(String key, int slotId, Class<T> clazz) {
        return huawei.cust.HwCfgFilePolicy.getValue(key, slotId, clazz);
    }

    public static Map getFileConfig(String fileName) {
        return huawei.cust.HwCfgFilePolicy.getFileConfig(fileName);
    }

    public static Map getFileConfig(String fileName, int slotId) {
        return huawei.cust.HwCfgFilePolicy.getFileConfig(fileName, slotId);
    }

    public static ArrayList<File> getCfgFileList(String fileName, int type, int slotId) throws NoClassDefFoundError {
        return huawei.cust.HwCfgFilePolicy.getCfgFileList(fileName, type, slotId);
    }

    public static File getCfgFile(String fileName, int type, int slotId) throws NoClassDefFoundError {
        return huawei.cust.HwCfgFilePolicy.getCfgFile(fileName, type, slotId);
    }

    public static String[] getCfgPolicyDir(int type, int slotId) throws NoClassDefFoundError {
        return huawei.cust.HwCfgFilePolicy.getCfgPolicyDir(type, slotId);
    }
}
