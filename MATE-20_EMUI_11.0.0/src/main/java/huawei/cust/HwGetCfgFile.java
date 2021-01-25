package huawei.cust;

import android.common.HwCfgKey;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HwGetCfgFile implements IHwGetCfgFileConfig {
    private static final String LOG_TAG = "HwGetCfgFile";
    private static final int SIM_NUM = TelephonyManager.getDefault().getPhoneCount();
    private static HwGetCfgFile sInstance = null;
    private Map[] cfgFileData = new Map[2];

    private HwGetCfgFile() {
    }

    public static synchronized HwGetCfgFile getDefault() {
        HwGetCfgFile hwGetCfgFile;
        synchronized (HwGetCfgFile.class) {
            synchronized (HwGetCfgFile.class) {
                if (sInstance == null) {
                    sInstance = new HwGetCfgFile();
                }
                hwGetCfgFile = sInstance;
            }
            return hwGetCfgFile;
        }
        return hwGetCfgFile;
    }

    public Map getCfgFileMap(int slotId) {
        if (isValidSlot(slotId)) {
            return this.cfgFileData[slotId];
        }
        return null;
    }

    private boolean isAvail(String str) {
        return !TextUtils.isEmpty(str);
    }

    public <T> T getCfgFileData(HwCfgKey keyCollection, Class<T> clazz) {
        List<Map> datalist;
        if (keyCollection == null || clazz == null) {
            Rlog.e(LOG_TAG, "getCfgFileData param invalid ");
            return null;
        }
        int slotid = keyCollection.slotid;
        if (!isValidSlot(slotid)) {
            Rlog.e(LOG_TAG, "getCfgFileData Error slotId ");
            return null;
        } else if (!isAvail(keyCollection.itkey) && isAvail(keyCollection.iskey) && isAvail(keyCollection.ifkey)) {
            return (T) getCfgFileData(keyCollection.key, keyCollection.ifkey, keyCollection.iskey, keyCollection.rkey, keyCollection.fvalue, keyCollection.svalue, slotid, clazz);
        } else {
            if (!(isAvail(keyCollection.itkey) || isAvail(keyCollection.iskey) || !isAvail(keyCollection.ifkey))) {
                return (T) getCfgFileData(keyCollection.key, keyCollection.ifkey, keyCollection.rkey, keyCollection.fvalue, slotid, clazz);
            }
            if (!(this.cfgFileData[keyCollection.slotid] == null || (datalist = (List) this.cfgFileData[keyCollection.slotid].get(keyCollection.key)) == null)) {
                for (Map data : datalist) {
                    String fdata = (String) data.get(keyCollection.ifkey);
                    String sdata = (String) data.get(keyCollection.iskey);
                    String tdata = (String) data.get(keyCollection.itkey);
                    boolean isAvailSdata = false;
                    boolean isAvailRplmn = isAvail(fdata) && fdata.equals(keyCollection.fvalue);
                    boolean isAnyRplmn = isAvail(fdata) && fdata.equals("00000");
                    boolean isAvailNetworktype = isAvail(tdata) && tdata.equals(keyCollection.tvalue);
                    if (isAvail(sdata) && sdata.equals(keyCollection.svalue)) {
                        isAvailSdata = true;
                    }
                    if (isAvailRplmn && isAvailSdata && isAvailNetworktype) {
                        return (T) data.get(keyCollection.rkey);
                    }
                    if (isAvailRplmn && isAvailSdata && !isAvail(tdata)) {
                        return (T) data.get(keyCollection.rkey);
                    }
                    if (isAnyRplmn && isAvailSdata && isAvailNetworktype) {
                        return (T) data.get(keyCollection.rkey);
                    }
                    if (isAnyRplmn && isAvailSdata && !isAvail(tdata)) {
                        return (T) data.get(keyCollection.rkey);
                    }
                    Rlog.d(LOG_TAG, "Can't match this scene");
                }
            }
            return null;
        }
    }

    private <T> T getCfgFileData(String key, String ifkey, String iskey, String rkey, String fvalue, String svalue, int slotid, Class<T> clazz) {
        List<Map> datalist;
        if (!isAvail(key) || clazz == null || !isAvail(rkey)) {
            Rlog.e(LOG_TAG, "getCfgFileData param invalid ");
            return null;
        } else if (!isValidSlot(slotid)) {
            Rlog.e(LOG_TAG, "getCfgFileData Error slotId ");
            return null;
        } else if (!isAvail(iskey) && isAvail(ifkey)) {
            return (T) getCfgFileData(key, ifkey, rkey, fvalue, slotid, clazz);
        } else {
            Map[] mapArr = this.cfgFileData;
            if (!(mapArr[slotid] == null || (datalist = (List) mapArr[slotid].get(key)) == null)) {
                for (Map data : datalist) {
                    String fdata = (String) data.get(ifkey);
                    String sdata = (String) data.get(iskey);
                    if (isAvail(fdata) && isAvail(sdata) && fdata.equals(fvalue) && sdata.equals(svalue)) {
                        return (T) data.get(rkey);
                    }
                    if (isAvail(fdata) && !isAvail(sdata) && fdata.equals(fvalue)) {
                        return (T) data.get(rkey);
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
            Map[] mapArr = this.cfgFileData;
            if (mapArr[slotid] != null) {
                List<Map> datalist = (List) mapArr[slotid].get(key);
                HashMap hashMap = new HashMap(1);
                if (datalist != null) {
                    for (Map data : datalist) {
                        String idata = (String) data.get(ikey);
                        if (isAvail(idata) && idata.equals(ivalue)) {
                            return (T) data.get(rkey);
                        }
                        if (isAvail(idata) && isAvail(ivalue) && idata.equals("00000")) {
                            hashMap.put("00000", data.get(rkey));
                        }
                    }
                }
                if (hashMap.containsKey("00000")) {
                    return (T) hashMap.get("00000");
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
        Map[] mapArr = this.cfgFileData;
        if (mapArr[slotId] != null) {
            mapArr[slotId] = null;
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
