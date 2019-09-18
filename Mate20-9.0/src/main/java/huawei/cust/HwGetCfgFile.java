package huawei.cust;

import android.common.HwCfgKey;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import java.util.List;
import java.util.Map;

public class HwGetCfgFile implements IHwGetCfgFileConfig {
    public static final String LOG_TAG = "HwGetCfgFile";
    private static final int SIM_NUM = TelephonyManager.getDefault().getPhoneCount();
    private static HwGetCfgFile sInstance = null;
    private Map[] cfgFileData = new Map[2];

    public static synchronized HwGetCfgFile getDefault() {
        HwGetCfgFile hwGetCfgFile;
        synchronized (HwGetCfgFile.class) {
            synchronized (HwGetCfgFile.class) {
                if (sInstance == null) {
                    sInstance = new HwGetCfgFile();
                }
                hwGetCfgFile = sInstance;
            }
        }
        return hwGetCfgFile;
    }

    public Map getCfgFileMap(int slotId) {
        if (this.cfgFileData[slotId] != null) {
            return this.cfgFileData[slotId];
        }
        return null;
    }

    private boolean isAvail(String str) {
        return str != null && !"".equals(str);
    }

    public <T> T getCfgFileData(HwCfgKey keyCollection, Class<T> clazz) {
        HwCfgKey hwCfgKey = keyCollection;
        if (hwCfgKey == null || clazz == null) {
            Rlog.e(LOG_TAG, "getCfgFileData param invalid ");
            return null;
        }
        int slotid = hwCfgKey.slotid;
        if (!isValidSlot(slotid)) {
            Rlog.e(LOG_TAG, "getCfgFileData Error slotId ");
            return null;
        } else if (!isAvail(hwCfgKey.itkey) && isAvail(hwCfgKey.iskey) && isAvail(hwCfgKey.ifkey)) {
            return getCfgFileData(hwCfgKey.key, hwCfgKey.ifkey, hwCfgKey.iskey, hwCfgKey.rkey, hwCfgKey.fvalue, hwCfgKey.svalue, slotid, clazz);
        } else if (isAvail(hwCfgKey.itkey) || isAvail(hwCfgKey.iskey) || !isAvail(hwCfgKey.ifkey)) {
            if (this.cfgFileData[hwCfgKey.slotid] != null) {
                List<Map> datalist = (List) this.cfgFileData[hwCfgKey.slotid].get(hwCfgKey.key);
                if (datalist != null) {
                    for (Map data : datalist) {
                        String fdata = (String) data.get(hwCfgKey.ifkey);
                        String sdata = (String) data.get(hwCfgKey.iskey);
                        String tdata = (String) data.get(hwCfgKey.itkey);
                        boolean z = true;
                        boolean isAvailRplmn = isAvail(fdata) && fdata.equals(hwCfgKey.fvalue);
                        boolean isAnyRplmn = isAvail(fdata) && fdata.equals("00000");
                        boolean isAvailNetworktype = isAvail(tdata) && tdata.equals(hwCfgKey.tvalue);
                        if (!isAvail(sdata) || !sdata.equals(hwCfgKey.svalue)) {
                            z = false;
                        }
                        boolean isAvailSdata = z;
                        if (isAvailRplmn && isAvailSdata && isAvailNetworktype) {
                            return data.get(hwCfgKey.rkey);
                        }
                        if (isAvailRplmn && isAvailSdata && !isAvail(tdata)) {
                            return data.get(hwCfgKey.rkey);
                        }
                        if (isAnyRplmn && isAvailSdata && isAvailNetworktype) {
                            return data.get(hwCfgKey.rkey);
                        }
                        if (isAnyRplmn && isAvailSdata && !isAvail(tdata)) {
                            return data.get(hwCfgKey.rkey);
                        }
                        Rlog.d(LOG_TAG, "Can't match this scene");
                    }
                }
            }
            return null;
        } else {
            return getCfgFileData(hwCfgKey.key, hwCfgKey.ifkey, hwCfgKey.rkey, hwCfgKey.fvalue, slotid, clazz);
        }
    }

    private <T> T getCfgFileData(String key, String ifkey, String iskey, String rkey, String fvalue, String svalue, int slotid, Class<T> clazz) {
        if (!isAvail(key) || clazz == null || !isAvail(rkey)) {
            Rlog.e(LOG_TAG, "getCfgFileData param invalid ");
            return null;
        } else if (!isValidSlot(slotid)) {
            Rlog.e(LOG_TAG, "getCfgFileData Error slotId ");
            return null;
        } else if (!isAvail(iskey) && isAvail(ifkey)) {
            return getCfgFileData(key, ifkey, rkey, fvalue, slotid, clazz);
        } else {
            if (this.cfgFileData[slotid] != null) {
                List<Map> datalist = (List) this.cfgFileData[slotid].get(key);
                if (datalist != null) {
                    for (Map data : datalist) {
                        String fdata = (String) data.get(ifkey);
                        String sdata = (String) data.get(iskey);
                        if (isAvail(fdata) && isAvail(sdata) && fdata.equals(fvalue) && sdata.equals(svalue)) {
                            return data.get(rkey);
                        }
                        if (isAvail(fdata) && !isAvail(sdata) && fdata.equals(fvalue)) {
                            return data.get(rkey);
                        }
                    }
                }
            }
            return null;
        }
    }

    private <T> T getCfgFileData(String key, String ikey, String rkey, String ivalue, int slotid, Class<T> clazz) {
        if (!isAvail(key) || clazz == null || !isAvail(rkey)) {
            Rlog.e(LOG_TAG, "getCfgFileData param invalid ");
            return null;
        } else if (!isValidSlot(slotid)) {
            Rlog.e(LOG_TAG, "getCfgFileData Error slotId ");
            return null;
        } else {
            if (this.cfgFileData[slotid] != null) {
                List<Map> datalist = (List) this.cfgFileData[slotid].get(key);
                if (datalist != null) {
                    for (Map data : datalist) {
                        String idata = (String) data.get(ikey);
                        if (!isAvail(idata)) {
                            return data.get(rkey);
                        }
                        if (isAvail(idata) && idata.equals(ivalue)) {
                            return data.get(rkey);
                        }
                    }
                }
            }
            return null;
        }
    }

    private static boolean isValidSlot(int slotId) {
        return slotId >= 0 && slotId < SIM_NUM;
    }

    public void clearCfgFileConfig(int slotId) {
        if (!isValidSlot(slotId)) {
            Rlog.e(LOG_TAG, "ClearCfgFileConfig Error slotId ");
            return;
        }
        if (this.cfgFileData[slotId] != null) {
            this.cfgFileData[slotId] = null;
        }
    }

    public void readCfgFileConfig(String fileName, int slotId) {
        if (!isValidSlot(slotId)) {
            Rlog.e(LOG_TAG, "ClearCfgFileConfig Error slotId ");
            return;
        }
        try {
            Map data = HwCfgFilePolicy.getFileConfig(fileName, slotId);
            if (data == null || this.cfgFileData[slotId] == null) {
                this.cfgFileData[slotId] = data;
            } else {
                this.cfgFileData[slotId].putAll(data);
            }
        } catch (Exception e) {
            Rlog.e(LOG_TAG, "Exception: read CfgFileConfig error ");
        }
    }
}
