package com.android.internal.telephony.fullnetwork;

import android.content.Context;
import android.content.SharedPreferences;
import android.telephony.HwTelephonyManager;
import android.telephony.HwTelephonyManagerInnerUtils;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwAESCryptoUtil;
import com.android.internal.telephony.HwNetworkTypeUtils;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.hwparttelephonyfullnetwork.BuildConfig;
import java.util.Iterator;
import java.util.Map;

public class DualNrSwitchHelper {
    private static final String LOG_TAG = "DualNrSwitchHelper";
    private static final String NR_SWITCH = "nr_switch";
    private static final int NR_SWITCH_CLOSE = 0;
    private static final int NR_SWITCH_NO_RECORD = 2;
    private static final int NR_SWITCH_OPEN = 1;
    private static final int NR_SWITCH_RECORD_MAX = 100;
    private static final int SWITCH_OFF = 0;
    public static final int SWITCH_ON = 1;
    private Context mContext;

    private DualNrSwitchHelper() {
    }

    public static DualNrSwitchHelper getInstance() {
        return SingletonInstance.INSTANCE;
    }

    public void init(Context context) {
        if (!isDualNrSwitchFeatureEnabled() || context == null) {
            loge("context is null, fail to init DualNrSwitchHelper.");
            return;
        }
        this.mContext = context;
        log("DualNrSwitchHelper init success.");
    }

    public int getNrSwitchRecord(String iccId) {
        if (!isParamValid(iccId)) {
            return 2;
        }
        SharedPreferences sp = this.mContext.getSharedPreferences(NR_SWITCH, 0);
        if (sp.getAll() == null) {
            return 2;
        }
        for (Map.Entry<String, ?> item : sp.getAll().entrySet()) {
            String decryptedIccid = BuildConfig.FLAVOR;
            if (item.getKey() != null) {
                decryptedIccid = decryptIccId(item.getKey());
            }
            if (iccId.equals(decryptedIccid)) {
                return sp.getInt(item.getKey(), 2);
            }
        }
        return 2;
    }

    public void putNrSwitchRecord(String iccId, boolean isNrSwitch) {
        if (isParamValid(iccId)) {
            checkTopLimit();
            if (!updateRecord(iccId, isNrSwitch)) {
                String encryptIccId = encryptIccId(iccId);
                if (encryptIccId == null) {
                    loge("invalid iccId");
                } else {
                    cacheRecord(encryptIccId, isNrSwitch);
                }
            }
        }
    }

    public void setNrSwitchAutoForMtkPlant() {
        if (isDualNrSwitchFeatureEnabled()) {
            for (int i = 0; i < HwFullNetworkConstantsInner.SIM_NUM; i++) {
                processNrSwitchBySim(i);
            }
        }
    }

    public boolean isDualNrSwitchFeatureEnabled() {
        return HuaweiTelephonyConfigs.isMTKPlatform() && HwTelephonyManagerInnerUtils.getDefault().isDualNrSupported();
    }

    public void setPropertyFromSwitchRecord() {
        int result;
        for (int slotId = 0; slotId < HwFullNetworkConstantsInner.SIM_NUM; slotId++) {
            int nrSwitch = getNrSwitchRecord(HwFullNetworkChipCommon.getInstance().mIccIds[slotId]);
            if (nrSwitch == 0) {
                result = HwNetworkTypeUtils.getNrOffMappingMode();
            } else if (nrSwitch != 1) {
                result = HwNetworkTypeUtils.getNetworkModeFromDB(this.mContext, slotId);
            } else {
                result = HwNetworkTypeUtils.getNrOnMappingMode();
            }
            SystemPropertiesEx.set(HwFullNetworkConstantsInner.PERSIST_RADIO_NETWORK_MODE + slotId, String.valueOf(result));
            log("setPropertyIfNecessary, slotId = " + slotId + ", result = " + result);
        }
    }

    private void processNrSwitchBySim(int slotId) {
        int ability;
        HwTelephonyManager hwTelephonyManager = HwTelephonyManager.getDefault();
        if (hwTelephonyManager != null) {
            int nrSwitch = getNrSwitchRecord(HwFullNetworkChipCommon.getInstance().mIccIds[slotId]);
            boolean z = true;
            int oldAbility = hwTelephonyManager.getServiceAbility(slotId, 1);
            if (nrSwitch == 0) {
                ability = 0;
            } else if (nrSwitch == 1) {
                ability = 1;
            } else if (nrSwitch != 2) {
                log("invalid record for nr switch return.");
                return;
            } else {
                ability = oldAbility;
            }
            log("setNrServiceAbility slotId: " + slotId + " ability : " + ability + " oldAbility : " + oldAbility + " nrSwitch from db : " + nrSwitch);
            if (oldAbility != ability) {
                hwTelephonyManager.setServiceAbility(slotId, 1, ability);
                return;
            }
            String str = HwFullNetworkChipCommon.getInstance().mIccIds[slotId];
            if (ability != 1) {
                z = false;
            }
            putNrSwitchRecord(str, z);
        }
    }

    private boolean updateRecord(String iccId, boolean isNrSwitch) {
        SharedPreferences sp = this.mContext.getSharedPreferences(NR_SWITCH, 0);
        if (sp.getAll() != null) {
            for (Map.Entry<String, ?> item : sp.getAll().entrySet()) {
                String tempEncryptedIccId = item.getKey();
                if (tempEncryptedIccId != null && iccId.equals(decryptIccId(tempEncryptedIccId))) {
                    cacheRecord(tempEncryptedIccId, isNrSwitch);
                    return true;
                }
            }
        }
        return false;
    }

    private void cacheRecord(String encryptIccId, boolean isNrSwitch) {
        SharedPreferences.Editor editor = this.mContext.getSharedPreferences(NR_SWITCH, 0).edit();
        editor.putInt(encryptIccId, isNrSwitch ? 1 : 0);
        editor.commit();
    }

    private void checkTopLimit() {
        SharedPreferences sp = this.mContext.getSharedPreferences(NR_SWITCH, 0);
        if (sp.getAll() != null && sp.getAll().size() >= 100) {
            Map<String, ?> values = sp.getAll();
            String deleteIccId = BuildConfig.FLAVOR;
            Iterator<Map.Entry<String, ?>> it = values.entrySet().iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                Map.Entry<String, ?> item = it.next();
                if (item.getKey() != null) {
                    if (!isLoadedCard(item.getKey())) {
                        deleteIccId = item.getKey();
                        break;
                    }
                } else {
                    break;
                }
            }
            if (!BuildConfig.FLAVOR.equals(deleteIccId)) {
                SharedPreferences.Editor editor = sp.edit();
                editor.remove(deleteIccId);
                editor.commit();
            }
        }
    }

    private boolean isLoadedCard(String iccId) {
        String decryptedIccid = decryptIccId(iccId);
        if (decryptedIccid == null || BuildConfig.FLAVOR.equals(decryptedIccid)) {
            return false;
        }
        for (String loadedCard : HwFullNetworkChipCommon.getInstance().mIccIds) {
            if (decryptedIccid.equals(loadedCard)) {
                return true;
            }
        }
        return false;
    }

    private final String encryptIccId(String iccId) {
        try {
            return HwAESCryptoUtil.encrypt(HwFullNetworkConstantsInner.MASTER_PASSWORD, iccId);
        } catch (IllegalArgumentException e) {
            loge("HwAESCryptoUtil encrypt IllegalArgumentException.");
            return null;
        } catch (Exception e2) {
            loge("HwAESCryptoUtil encrypt excepiton.");
            return null;
        }
    }

    private final String decryptIccId(String iccId) {
        try {
            return HwAESCryptoUtil.decrypt(HwFullNetworkConstantsInner.MASTER_PASSWORD, iccId);
        } catch (IllegalArgumentException e) {
            loge("HwAESCryptoUtil decrypt IllegalArgumentException.");
            return null;
        } catch (Exception e2) {
            loge("HwAESCryptoUtil decrypt excepiton.");
            return null;
        }
    }

    private boolean isParamValid() {
        if (isDualNrSwitchFeatureEnabled() && this.mContext != null) {
            return true;
        }
        return false;
    }

    private boolean isParamValid(String iccId) {
        if (isParamValid() && iccId != null && !BuildConfig.FLAVOR.equals(iccId)) {
            return true;
        }
        return false;
    }

    private void log(String msg) {
        RlogEx.i(LOG_TAG, msg);
    }

    private void loge(String msg) {
        RlogEx.e(LOG_TAG, msg);
    }

    private static class SingletonInstance {
        private static final DualNrSwitchHelper INSTANCE = new DualNrSwitchHelper();

        private SingletonInstance() {
        }
    }
}
