package huawei.cust;

import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.telephony.Rlog;
import android.telephony.TelephonyManager;
import com.huawei.uikit.effect.BuildConfig;
import huawei.cust.aidl.IHwCarrierConfigService;
import huawei.cust.aidl.SimFileInfo;
import huawei.cust.aidl.SimMatchRule;
import java.util.Map;

public class HwCarrierConfigPolicy implements IHwCarrierConfigPolicy {
    private static final String HW_CARRIER_CONFIG_SERVICE = "hwCarrierConfig";
    private static final String LOG_TAG = "HwCarrierConfigPolicy";
    private static final String OPKEY_PROP = "persist.sys.opkey";
    private static final int SIM_NUM = TelephonyManager.getDefault().getPhoneCount();
    private static final int SLOT_0 = 0;
    private static final int SLOT_1 = 1;
    private static final boolean isMultiSimEnabled = TelephonyManager.getDefault().isMultiSimEnabled();
    private static HwCarrierConfigPolicy sInstance;

    public static HwCarrierConfigPolicy getDefault() {
        HwCarrierConfigPolicy hwCarrierConfigPolicy;
        synchronized (HwCarrierConfigPolicy.class) {
            if (sInstance == null) {
                sInstance = new HwCarrierConfigPolicy();
            }
            hwCarrierConfigPolicy = sInstance;
        }
        return hwCarrierConfigPolicy;
    }

    private HwCarrierConfigPolicy() {
    }

    public String getOpKey() {
        return getOpKey(getDefaultSlotId());
    }

    public String getOpKey(int slotId) {
        if (!isValidSlot(slotId)) {
            loge("Error slotId " + slotId);
            return null;
        }
        String opKey = SystemProperties.get(OPKEY_PROP + slotId, (String) null);
        if (opKey == null || BuildConfig.FLAVOR.equals(opKey.trim())) {
            return null;
        }
        return opKey;
    }

    public <T> T getValue(String key, Class<T> clazz) {
        return (T) getValue(key, getDefaultSlotId(), clazz);
    }

    public <T> T getValue(String key, int slotId, Class<T> clazz) {
        T t;
        if (key == null || clazz == null) {
            loge("getValue param invalid");
            return null;
        }
        try {
            IHwCarrierConfigService mHwCarrierConfigService = getHwCarrierConfigService(slotId);
            if (mHwCarrierConfigService == null) {
                loge("getConfigForSlotId Error mHwCarrierConfigService is null ");
                return null;
            }
            Map data = mHwCarrierConfigService.getConfigForSlotId(key, slotId);
            if (data == null || (t = (T) data.get(key)) == null) {
                return null;
            }
            if (t.getClass().getName().equals(clazz.getName())) {
                return t;
            }
            loge("getValue type error :key = " + key + " clazz =" + clazz.getName() + " slotId = " + slotId);
            return null;
        } catch (RemoteException e) {
            loge("Error getValue for slotId " + slotId + ": " + e.toString());
            return null;
        }
    }

    public Map getFileConfig(String fileName) {
        return getFileConfig(fileName, getDefaultSlotId());
    }

    public Map getFileConfig(String fileName, int slotId) {
        if (isValidSlot(slotId)) {
            return HwCarrierConfigXmlParse.parse(fileName, slotId);
        }
        loge("Error slotId " + slotId);
        return null;
    }

    public SimMatchRule querySimMatchRule(String mccmnc, String iccid, String imsi) {
        return querySimMatchRule(mccmnc, iccid, imsi, getDefaultSlotId());
    }

    public SimMatchRule querySimMatchRule(String mccmnc, String iccid, String imsi, int slotId) {
        log("querySimMatchRule mccmnc = " + mccmnc + " ,slotId = " + slotId);
        try {
            IHwCarrierConfigService mHwCarrierConfigService = getHwCarrierConfigService(slotId);
            if (mHwCarrierConfigService != null) {
                return mHwCarrierConfigService.querySimMatchRule(mccmnc, iccid, imsi, slotId);
            }
            loge("Error HwCarrierConfigService is null");
            return null;
        } catch (RemoteException e) {
            loge("Error querySimRule for slotId " + slotId + ": " + e.toString());
            return null;
        }
    }

    public void updateSimFileInfo(SimFileInfo simFileInfo) {
        updateSimFileInfo(simFileInfo, getDefaultSlotId());
    }

    public void updateSimFileInfo(SimFileInfo simFileInfo, int slotId) {
        log("updateSimFileInfo slotId = " + slotId);
        try {
            IHwCarrierConfigService mHwCarrierConfigService = getHwCarrierConfigService(slotId);
            if (mHwCarrierConfigService == null) {
                loge("Error HwCarrierConfigService is null");
            } else {
                mHwCarrierConfigService.updateSimFileInfo(simFileInfo, slotId);
            }
        } catch (RemoteException e) {
            loge("Error updateSimFileInfo for slotId " + slotId + ": " + e.toString());
        }
    }

    private IHwCarrierConfigService getHwCarrierConfigService(int slotId) {
        if (!isValidSlot(slotId)) {
            loge("Error slotId " + slotId);
            return null;
        }
        try {
            IBinder binder = ServiceManager.checkService(HW_CARRIER_CONFIG_SERVICE);
            if (binder != null) {
                return IHwCarrierConfigService.Stub.asInterface(binder);
            }
            loge("checkService hwCarrierConfig is Error");
            return null;
        } catch (Exception e) {
            loge("getHwCarrierConfigService hwCarrierConfig is Error");
            return null;
        }
    }

    private boolean isValidSlot(int slotId) {
        return slotId >= 0 && slotId < SIM_NUM;
    }

    private int getDefaultSlotId() {
        String opKey;
        String opKey2;
        if (!isMultiSimEnabled || (((opKey = getOpKey(0)) != null && !BuildConfig.FLAVOR.equals(opKey.trim())) || (opKey2 = getOpKey(1)) == null || BuildConfig.FLAVOR.equals(opKey2.trim()))) {
            return 0;
        }
        return 1;
    }

    private static void log(String msg) {
        Rlog.d(LOG_TAG, msg);
    }

    private static void loge(String msg) {
        Rlog.e(LOG_TAG, msg);
    }
}
