package com.android.ims;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.Intent;
import android.os.PersistableBundle;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.CarrierConfigManager;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.telephony.ims.aidl.IImsMmTelFeature;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwModemCapability;
import huawei.android.provider.HwSettings;

public class HwImsManagerInner {
    public static final String ACTION_IMS_FACTORY_RESET = "com.huawei.ACTION_NETWORK_FACTORY_RESET";
    private static final boolean DBG = true;
    private static final int DEFAULT_WFC_MODE = 2;
    private static final boolean FEATURE_DUAL_VOWIFI = SystemProperties.getBoolean("ro.config.hw_dual_vowifi", true);
    private static final boolean FEATURE_SHOW_VOLTE_SWITCH = SystemProperties.getBoolean("ro.config.hw_volte_show_switch", true);
    private static final boolean FEATURE_VOLTE_DYN = SystemProperties.getBoolean("ro.config.hw_volte_dyn", false);
    public static final String HW_QCOM_VOLTE_USER_SWITCH = "volte_vt_enabled";
    public static final String HW_VOLTE_USER_SWITCH = "hw_volte_user_switch";
    private static final String[] HW_VOLTE_USER_SWITCH_DUALIMS = {"hw_volte_user_switch_0", "hw_volte_user_switch_1"};
    private static final String IMS_SERVICE = "ims";
    private static final int INT_INVALID_VALUE = -1;
    public static final String KEY_CARRIER_DEFAULT_VOLTE_SWITCH_ON_BOOL = "carrier_default_volte_switch_on_bool";
    public static final String KEY_CARRIER_DEFAULT_WFC_IMS_ROAMING_MODE_INT = "carrier_default_wfc_ims_roaming_mode_int";
    public static final String PROP_VILTE_ENABLE = "ro.config.hw_vtlte_on";
    public static final String PROP_VOLTE_ENABLE = "ro.config.hw_volte_on";
    public static final String PROP_VOWIFI_ENABLE = "ro.vendor.config.hw_vowifi";
    public static final String SUBID = "subId";
    private static final int SUBID_0 = 0;
    private static final int SUBID_1 = 1;
    private static final String TAG = "HwImsManagerInner";
    private static final int VOWIFI_PREFER_INVALID = 3;
    private static final String[] VT_IMS_ENABLED_DUALIMS = {"vt_ims_enabled_0", "vt_ims_enabled_1"};
    private static final boolean isATT;
    private static boolean mConfigUpdated = false;
    private static int[] userSelectWfcMode = {3, 3};

    static {
        boolean z = true;
        if (!"07".equals(SystemProperties.get("ro.config.hw_opta")) || !"840".equals(SystemProperties.get("ro.config.hw_optb"))) {
            z = false;
        }
        isATT = z;
    }

    public static boolean isEnhanced4gLteModeSettingEnabledByUser(Context context, int subId) {
        int enabled;
        log("isEnhanced4gLteModeSettingEnabledByUser :: subId -> " + subId);
        boolean z = false;
        if (!isValidParameter(context, subId)) {
            loge("subId is wrong or context is null, the result is false, subId is " + subId);
            return false;
        }
        int currentSubId = subId;
        String dbName = HW_VOLTE_USER_SWITCH;
        if (!isDualImsAvailable()) {
            currentSubId = HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId();
            log("isEnhanced4gLteModeSettingEnabledByUser :: dual-ims is not support, subId is main-subId");
        } else {
            dbName = HW_VOLTE_USER_SWITCH_DUALIMS[currentSubId];
        }
        if (FEATURE_VOLTE_DYN) {
            if (!getBooleanCarrierConfig(context, "carrier_volte_available_bool", currentSubId)) {
                log("KEY_CARRIER_VOLTE_AVAILABLE_BOOL is false, return false");
                return false;
            } else if (!getBooleanCarrierConfig(context, "carrier_volte_show_switch_bool", currentSubId)) {
                log("KEY_CARRIER_VOLTE_SHOW_SWITCH_BOOL is false, return true");
                return true;
            } else {
                enabled = Settings.System.getInt(context.getContentResolver(), dbName, getBooleanCarrierConfig(context, KEY_CARRIER_DEFAULT_VOLTE_SWITCH_ON_BOOL, currentSubId) ? 1 : 0);
                log("FEATURE_VOLTE_DYN is true, enabled -> " + enabled + ", currentSubId ->" + currentSubId);
            }
        } else if (!FEATURE_SHOW_VOLTE_SWITCH) {
            return true;
        } else {
            enabled = Settings.System.getInt(context.getContentResolver(), dbName, 0);
        }
        log("isEnhanced4gLteModeSettingEnabledByUser enabled -> " + enabled + ", currentSubId -> " + currentSubId);
        if (enabled == 1) {
            z = true;
        }
        return z;
    }

    public static boolean isNonTtyOrTtyOnVolteEnabled(Context context, int subId) {
        boolean result;
        boolean result2 = false;
        if (!isValidParameter(context, subId)) {
            loge("subId is wrong or context is null, the result is false, subId is " + subId);
            return false;
        } else if (!isDualImsAvailable()) {
            log("isNonTtyOrTtyOnVolteEnabled :: dual-ims is not support");
            return ImsManager.isNonTtyOrTtyOnVolteEnabled(context);
        } else if (getBooleanCarrierConfig(context, "carrier_volte_tty_supported_bool", subId)) {
            return true;
        } else {
            if (Settings.Secure.getInt(context.getContentResolver(), "preferred_tty_mode", 0) == 0) {
                result2 = true;
            }
            log("isNonTtyOrTtyOnVolteEnabled result -> " + result + ", subId " + subId);
            return result;
        }
    }

    public static boolean isVolteEnabledByPlatform(Context context, int subId) {
        boolean result = false;
        if (!isValidParameter(context, subId)) {
            loge("subId is wrong or context is null, the result is false, subId is " + subId);
            return false;
        } else if (!isDualImsAvailable()) {
            log("isVolteEnabledByPlatform :: dual-ims is not support");
            return ImsManager.isVolteEnabledByPlatform(context);
        } else if (!SystemProperties.getBoolean(PROP_VOLTE_ENABLE, false)) {
            log("hw_volte_on is false");
            return false;
        } else {
            boolean result1 = context.getResources().getBoolean(17956925);
            boolean result2 = getBooleanCarrierConfig(context, "carrier_volte_available_bool", subId);
            boolean result3 = isGbaValid(context, subId);
            log("Volte sim adp : Device = " + result1 + ", XML_CarrierConfig = " + result2 + ", GbaValid = " + result3 + ", subId = " + subId);
            if (result1 && result2 && result3) {
                result = true;
            }
            return result;
        }
    }

    public static boolean isVtEnabledByPlatform(Context context, int subId) {
        boolean z = false;
        if (!isValidParameter(context, subId)) {
            loge("subId is wrong or context is null, the result is false, subId is " + subId);
            return false;
        } else if (!isDualImsAvailable()) {
            log("isVtEnabledByPlatform :: dual-ims is not support");
            return ImsManager.isVtEnabledByPlatform(context);
        } else if (!SystemProperties.getBoolean(PROP_VILTE_ENABLE, false)) {
            log("isVtEnabledByPlatform ro.config.hw_vtlte_on is false");
            return false;
        } else {
            if (context.getResources().getBoolean(17956926) && getBooleanCarrierConfig(context, "carrier_vt_available_bool", subId) && isGbaValid(context, subId)) {
                z = true;
            }
            return z;
        }
    }

    public static boolean isVtEnabledByUser(Context context, int subId) {
        boolean z = false;
        if (!isValidParameter(context, subId)) {
            loge("subId is wrong or context is null, the result is false, subId is " + subId);
            return false;
        } else if (!isDualImsAvailable()) {
            log("isVtEnabledByUser :: dual-ims is not support");
            return ImsManager.isVtEnabledByUser(context);
        } else {
            if (Settings.Global.getInt(context.getContentResolver(), VT_IMS_ENABLED_DUALIMS[subId], 1) == 1) {
                z = true;
            }
            return z;
        }
    }

    public static boolean isWfcEnabledByUser(Context context, int subId) {
        if (!isValidParameter(context, subId)) {
            loge("subId is wrong or context is null, the result is false, subId is " + subId);
            return false;
        } else if (isDualImsAvailable() && FEATURE_DUAL_VOWIFI) {
            return ImsManager.getInstance(context, subId).isWfcEnabledByUser();
        } else {
            log("isWfcEnabledByUser :: dual-ims is not support");
            if (HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId() == subId) {
                return ImsManager.isWfcEnabledByUser(context);
            }
            loge("isWfcEnabledByUser error, subId should be the mainsubId");
            return false;
        }
    }

    public static void setWfcSetting(Context context, boolean enabled, int subId) {
        if (!isValidParameter(context, subId)) {
            loge("subId is wrong or context is null, subId is " + subId);
            return;
        }
        if (!isDualImsAvailable() || !FEATURE_DUAL_VOWIFI) {
            log("setWfcSetting :: dual-ims is not support");
            if (HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId() != subId) {
                loge("setWfcSetting error, subId should be the mainsubId");
                return;
            }
            ImsManager.setWfcSetting(context, enabled);
        } else {
            log("setwfcSetting subId ->" + subId + ", enabled ->" + enabled);
            SubscriptionManager.setSubscriptionProperty(subId, "wfc_ims_enabled", enabled ? "1" : HwSettings.System.FINGERSENSE_KNUCKLE_GESTURE_OFF);
            ImsManager imsManager = ImsManager.getInstance(context, subId);
            if (imsManager != null) {
                try {
                    ImsConfig configInterface = imsManager.getConfigInterface();
                    TelephonyManager tm = (TelephonyManager) context.getSystemService("phone");
                    Boolean isRoaming = Boolean.valueOf(tm != null ? tm.isNetworkRoaming(subId) : false);
                    Boolean isVowifiEnable = Boolean.valueOf(isWfcEnabledByPlatform(context, subId));
                    if (isVowifiEnable.booleanValue() && 3 == userSelectWfcMode[subId]) {
                        userSelectWfcMode[subId] = getWfcMode(context, isRoaming.booleanValue(), subId);
                    }
                    int i = 1;
                    changeMmTelCapability(context, subId, 1, 1, enabled);
                    if (enabled) {
                        if (isVowifiEnable.booleanValue()) {
                            log("isVowifiEnable = true, setWfcModeInternal - setting = " + userSelectWfcMode[subId] + ", subId = " + subId);
                            setWfcModeInternal(context, userSelectWfcMode[subId], subId);
                        }
                        log("setWfcSetting() : turnOnIms");
                        turnOnIms(imsManager, context, subId);
                    } else if (getBooleanCarrierConfig(context, "carrier_allow_turnoff_ims_bool", subId) && (!isVolteEnabledByPlatform(context, subId) || !isEnhanced4gLteModeSettingEnabledByUser(context, subId))) {
                        log("setWfcSetting() : imsServiceAllowTurnOff -> turnOffIms");
                        turnOffIms(imsManager, context, subId);
                    }
                    if (enabled) {
                        i = getWfcMode(context, isRoaming.booleanValue(), subId);
                    }
                    setWfcModeInternal(context, i, subId);
                } catch (ImsException e) {
                    loge("setWfcSetting(): ", e);
                }
            }
        }
    }

    public static int getWfcMode(Context context, int subId) {
        if (!isValidParameter(context, subId)) {
            loge("subId is wrong or context is null, the result is deault_wfc_mode, subId is " + subId);
            return 2;
        } else if (isDualImsAvailable() && FEATURE_DUAL_VOWIFI) {
            return ImsManager.getInstance(context, subId).getWfcMode();
        } else {
            log("getWfcMode :: dual-ims is not support");
            if (HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId() == subId) {
                return ImsManager.getWfcMode(context);
            }
            loge("getWfcMode error, subId should be the mainsubId");
            return 2;
        }
    }

    public static void setWfcMode(Context context, int wfcMode, int subId) {
        if (!isValidParameter(context, subId)) {
            loge("subId is wrong or context is null, subId is " + subId);
            return;
        }
        if (!isDualImsAvailable() || !FEATURE_DUAL_VOWIFI) {
            log("setWfcMode :: dual-ims is not support");
            if (HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId() != subId) {
                loge("setWfcMode error, subId should be the mainsubId");
                return;
            }
            ImsManager.setWfcMode(context, wfcMode);
        } else {
            ImsManager.getInstance(context, subId).setWfcMode(wfcMode);
            log("setWfcMode - subId=" + subId + ", wfcMode=" + wfcMode);
            if (isWfcEnabledByPlatform(context, subId)) {
                userSelectWfcMode[subId] = wfcMode;
            }
        }
    }

    public static int getWfcMode(Context context, boolean roaming, int subId) {
        if (!isValidParameter(context, subId)) {
            loge("subId is wrong or context is null, the result is deault_wfc_mode, subId is " + subId);
            return 2;
        } else if (isDualImsAvailable() && FEATURE_DUAL_VOWIFI) {
            return ImsManager.getInstance(context, subId).getWfcMode(roaming);
        } else {
            log("getWfcMode :: dual-ims is not support, roaming is " + roaming);
            if (HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId() == subId) {
                return ImsManager.getWfcMode(context, roaming);
            }
            loge("getWfcMode error, subId should be the mainsubId");
            return 2;
        }
    }

    public static void setWfcMode(Context context, int wfcMode, boolean roaming, int subId) {
        if (!isValidParameter(context, subId)) {
            loge("subId is wrong or context is null, subId is " + subId);
            return;
        }
        if (!isDualImsAvailable() || !FEATURE_DUAL_VOWIFI) {
            log("setWfcMode :: dual-ims is not support, roaming is " + roaming);
            if (HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId() != subId) {
                loge("setWfcMode error, subId should be the mainsubId");
                return;
            }
            ImsManager.setWfcMode(context, wfcMode, roaming);
        } else if (isWfcEnabledByPlatform(context, subId)) {
            ImsManager.getInstance(context, subId).setWfcMode(wfcMode, roaming);
            userSelectWfcMode[subId] = wfcMode;
        }
    }

    private static void setWfcModeInternal(Context context, int wfcMode, int subId) {
        final ImsManager imsManager = ImsManager.getInstance(context, subId);
        if (imsManager != null) {
            final int value = wfcMode;
            new Thread(new Runnable() {
                public void run() {
                    try {
                        imsManager.getConfigInterface().setProvisionedValue(27, value);
                    } catch (ImsException e) {
                        HwImsManagerInner.loge("setWfcModeInternal(): ", e);
                    }
                }
            }).start();
        }
    }

    public static boolean isWfcRoamingEnabledByUser(Context context, int subId) {
        boolean z = false;
        if (!isValidParameter(context, subId)) {
            loge("subId is wrong or context is null, the result is false, subId is " + subId);
            return false;
        } else if (!isDualImsAvailable() || !FEATURE_DUAL_VOWIFI) {
            log("isWfcRoamingEnabledByUser :: dual-ims is not support");
            if (HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId() == subId) {
                return ImsManager.isWfcRoamingEnabledByUser(context);
            }
            loge("isWfcRoamingEnabledByUser error, subId should be the mainsubId");
            return false;
        } else {
            if (SubscriptionManager.getIntegerSubscriptionProperty(subId, "wfc_ims_roaming_enabled", getBooleanCarrierConfig(context, "carrier_default_wfc_ims_roaming_enabled_bool", subId) ? 1 : 0, context) == 1) {
                z = true;
            }
            return z;
        }
    }

    public static void setWfcRoamingSetting(Context context, boolean enabled, int subId) {
        if (!isValidParameter(context, subId)) {
            loge("subId is wrong or context is null, subId is " + subId);
            return;
        }
        if (!isDualImsAvailable() || !FEATURE_DUAL_VOWIFI) {
            log("setWfcRoamingSetting :: dual-ims is not support");
            if (HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId() != subId) {
                loge("setWfcRoamingSetting error, subId should be the mainsubId");
                return;
            }
            ImsManager.setWfcRoamingSetting(context, enabled);
        } else {
            SubscriptionManager.setSubscriptionProperty(subId, "wfc_ims_roaming_enabled", Integer.toString((int) enabled));
            setWfcRoamingSettingInternal(context, enabled, subId);
        }
    }

    private static void setWfcRoamingSettingInternal(Context context, boolean enabled, int subId) {
        final int value;
        final ImsManager imsManager = ImsManager.getInstance(context, subId);
        if (imsManager != null) {
            if (enabled) {
                value = 1;
            } else {
                value = 0;
            }
            new Thread(new Runnable() {
                public void run() {
                    try {
                        imsManager.getConfigInterface().setProvisionedValue(26, value);
                    } catch (ImsException e) {
                        HwImsManagerInner.loge("setWfcRoamingSettingInternal(): ", e);
                    }
                }
            }).start();
        }
    }

    public static boolean isWfcEnabledByPlatform(Context context, int subId) {
        boolean result = false;
        if (!isValidParameter(context, subId)) {
            loge("subId is wrong or context is null, the result is false, subId is " + subId);
            return false;
        } else if (!SystemProperties.getBoolean(PROP_VOWIFI_ENABLE, false)) {
            loge("hw_vowifi prop is false, return false");
            return false;
        } else if (!isDualImsAvailable() || !FEATURE_DUAL_VOWIFI) {
            log("isWfcEnabledByPlatform :: dual-ims is not support");
            if (HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId() == subId) {
                return ImsManager.isWfcEnabledByPlatform(context);
            }
            loge("isWfcEnabledByPlatform error, subId should be the mainsubId, return false");
            return false;
        } else {
            boolean result1 = context.getResources().getBoolean(17956927);
            boolean result2 = getBooleanCarrierConfig(context, "carrier_wfc_ims_available_bool", subId);
            boolean result3 = isGbaValid(context, subId);
            log("Vowifi sim adp : Device =" + result1 + " XML_CarrierConfig =" + result2 + " GbaValid =" + result3 + " subId =" + subId);
            if (result1 && result2 && result3) {
                result = true;
            }
            return result;
        }
    }

    private static boolean isGbaValid(Context context, int subId) {
        boolean capable = true;
        if (!getBooleanCarrierConfig(context, "carrier_ims_gba_required_bool", subId)) {
            return true;
        }
        String efIst = TelephonyManager.getDefault().getIsimIst();
        if (efIst == null) {
            loge("ISF is NULL");
            return true;
        }
        if (efIst.length() <= 1 || (2 & ((byte) efIst.charAt(1))) == 0) {
            capable = false;
        }
        log("GBA capable=" + capable + ", ISF=" + efIst);
        return capable;
    }

    public static void updateImsServiceConfig(Context context, int inputSubId, boolean force) {
        int subId = inputSubId;
        if (!isValidParameter(context, subId)) {
            loge("subId is wrong or context is null, the result is false, subId is " + subId);
            return;
        }
        boolean isMtk = HuaweiTelephonyConfigs.isMTKPlatform();
        if (!isDualImsAvailable() && isMtk) {
            int mainSubId = HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId();
            log("updateImsServiceConfig :: dual-ims is not support and MTK platform, change subId from " + subId + " to mainSubId " + mainSubId);
            subId = mainSubId;
        }
        if (isDualImsAvailable() == 0 && !isMtk) {
            log("updateImsServiceConfig :: dual-ims is not support");
            ImsManager.updateImsServiceConfig(context, HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId(), force);
        } else if (force || TelephonyManager.getDefault().getSimState(subId) == 5) {
            ImsManager imsManager = ImsManager.getInstance(context, subId);
            if (imsManager != null && (!mConfigUpdated || force)) {
                try {
                    if (!(updateVolteFeatureValue(context, subId) | updateVideoCallFeatureValue(context, subId)) && !updateWfcFeatureAndProvisionedValues(context, subId)) {
                        if (getBooleanCarrierConfig(context, "carrier_allow_turnoff_ims_bool", subId)) {
                            log("updateImsServiceConfig: turnOffIms, subId is " + subId);
                            turnOffIms(imsManager, context, subId);
                            mConfigUpdated = true;
                        }
                    }
                    log("updateImsServiceConfig: turnOnIms, subId is " + subId);
                    turnOnIms(imsManager, context, subId);
                    mConfigUpdated = true;
                } catch (ImsException e) {
                    loge("updateImsServiceConfig: ", e);
                    mConfigUpdated = false;
                }
            }
        } else {
            log("updateImsServiceConfig: SIM not ready, subId is " + subId);
        }
    }

    private static boolean updateVolteFeatureValue(Context context, int subId) throws ImsException {
        boolean available = isVolteEnabledByPlatform(context, subId);
        boolean enabled = isEnhanced4gLteModeSettingEnabledByUser(context, subId);
        boolean isNonTty = isNonTtyOrTtyOnVolteEnabled(context, subId);
        boolean isFeatureOn = available && enabled && isNonTty;
        log("updateVolteFeatureValue: available = " + available + ", enabled = " + enabled + ", nonTTY = " + isNonTty);
        changeMmTelCapability(context, subId, 1, 0, isFeatureOn);
        return isFeatureOn;
    }

    public static void changeMmTelCapability(Context context, int subId, int capability, int radioTech, boolean isEnabled) throws ImsException {
        ImsManager.getInstance(context, subId).changeMmTelCapability(capability, radioTech, isEnabled);
    }

    private static boolean updateVideoCallFeatureValue(Context context, int subId) throws ImsException {
        boolean available = isVtEnabledByPlatform(context, subId);
        boolean enabled = isVtEnabledByUser(context, subId);
        boolean isNonTty = isNonTtyOrTtyOnVolteEnabled(context, subId);
        boolean isFeatureOn = available && enabled && isNonTty;
        log("updateVideoCallFeatureValue: available = " + available + ", enabled = " + enabled + ", nonTTY = " + isNonTty + ", subId = " + subId);
        changeMmTelCapability(context, subId, 2, 0, isFeatureOn);
        return isFeatureOn;
    }

    private static boolean updateWfcFeatureAndProvisionedValues(Context context, int subId) throws ImsException {
        boolean isNetworkRoaming = TelephonyManager.getDefault().isNetworkRoaming(subId);
        boolean available = isWfcEnabledByPlatform(context, subId);
        boolean enabled = isWfcEnabledByUser(context, subId);
        int mode = getWfcMode(context, isNetworkRoaming, subId);
        boolean roaming = isWfcRoamingEnabledByUser(context, subId);
        boolean isFeatureOn = available && enabled;
        log("updateWfcFeatureAndProvisionedValues: available = " + available + ", enabled = " + enabled + ", mode = " + mode + ", roaming = " + roaming + ", subId = " + subId);
        changeMmTelCapability(context, subId, 1, 1, isFeatureOn);
        if (!isFeatureOn) {
            mode = 1;
            roaming = false;
        }
        setWfcModeInternal(context, mode, subId);
        setWfcRoamingSettingInternal(context, roaming, subId);
        return isFeatureOn;
    }

    private static Boolean checkCarrierConfigKeyExist(Context context, String key, int subId) {
        Boolean ifExist = false;
        CarrierConfigManager configManager = (CarrierConfigManager) context.getSystemService("carrier_config");
        PersistableBundle b = null;
        if (configManager != null) {
            b = configManager.getConfigForSubId(subId);
        }
        if (!(b == null || b.get(key) == null)) {
            log("checkCarrierConfigKeyExist, b.getkey = " + b.get(key) + ", subId is " + subId);
            ifExist = true;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("carrierConfig key[");
        sb.append(key);
        sb.append("] ");
        sb.append(ifExist.booleanValue() ? "exists" : "does not exist");
        log(sb.toString());
        return ifExist;
    }

    private static boolean getBooleanCarrierConfig(Context context, String key, int subId) {
        CarrierConfigManager configManager = (CarrierConfigManager) context.getSystemService("carrier_config");
        PersistableBundle b = null;
        if (configManager != null) {
            b = configManager.getConfigForSubId(subId);
        }
        if (b == null || b.get(key) == null) {
            return CarrierConfigManager.getDefaultConfig().getBoolean(key);
        }
        return b.getBoolean(key);
    }

    private static int getIntCarrierConfig(Context context, String key, int subId) {
        CarrierConfigManager configManager = (CarrierConfigManager) context.getSystemService("carrier_config");
        PersistableBundle b = null;
        if (configManager != null) {
            b = configManager.getConfigForSubId(subId);
        }
        if (b == null || b.get(key) == null) {
            return CarrierConfigManager.getDefaultConfig().getInt(key);
        }
        return b.getInt(key);
    }

    private static void checkAndThrowExceptionIfServiceUnavailable(ImsManager imsManager, Context context, int subId) throws ImsException {
        if (imsManager.getImsServiceProxy() == null || !imsManager.getImsServiceProxy().isBinderAlive()) {
            createImsService(imsManager, context, subId);
            if (imsManager.getImsServiceProxy() == null) {
                throw new ImsException("Service is unavailable", CharacterSets.DEFAULT_CHARSET);
            }
        }
    }

    private static void createImsService(ImsManager imsManager, Context context, int subId) {
        imsManager.createImsServiceProxy(getServiceProxy(imsManager, context, subId));
    }

    private static MmTelFeatureConnection getServiceProxy(ImsManager imsManager, Context context, int subId) {
        MmTelFeatureConnection serviceProxy = new MmTelFeatureConnection(context, subId);
        TelephonyManager tm = (TelephonyManager) context.getSystemService("phone");
        if (tm == null) {
            log("create: TelephonyManager is null!");
            return serviceProxy;
        }
        IImsMmTelFeature binder = tm.getImsMmTelFeatureAndListen(subId, serviceProxy.getListener());
        if (binder != null) {
            serviceProxy.setBinder(binder.asBinder());
            serviceProxy.getFeatureState();
        } else {
            log("create: binder is null! Slot Id: " + subId);
        }
        return serviceProxy;
    }

    private static void log(String s) {
        Rlog.d(TAG, s);
    }

    private static void loge(String s) {
        Rlog.e(TAG, s);
    }

    /* access modifiers changed from: private */
    public static void loge(String s, Throwable t) {
        Rlog.e(TAG, s, t);
    }

    private static void turnOnIms(ImsManager imsManager, Context context, int subId) throws ImsException {
        checkAndThrowExceptionIfServiceUnavailable(imsManager, context, subId);
        if (!isATT || isEnhanced4gLteModeSettingEnabledByUser(context, subId)) {
            ((TelephonyManager) context.getSystemService("phone")).enableIms(subId);
        } else {
            log("turnOnIms: Enhanced LTE Service is off, return.");
        }
    }

    private static void turnOffIms(ImsManager imsManager, Context context, int subId) throws ImsException {
        checkAndThrowExceptionIfServiceUnavailable(imsManager, context, subId);
        if (!isATT || !isEnhanced4gLteModeSettingEnabledByUser(context, subId)) {
            ((TelephonyManager) context.getSystemService("phone")).disableIms(subId);
        } else {
            log("turnOffIms: Enhanced LTE Service is on, return.");
        }
    }

    public static void factoryReset(Context context, int subId) {
        if (!isValidParameter(context, subId)) {
            loge("subId is wrong or context is null, subId is " + subId);
            return;
        }
        int currentsubId = subId;
        String volteDB = HW_VOLTE_USER_SWITCH_DUALIMS[currentsubId];
        if (!isDualImsAvailable()) {
            log("factoryReset :: dual-ims is not support");
            volteDB = HW_VOLTE_USER_SWITCH;
            currentsubId = HwFrameworkFactory.getHwInnerTelephonyManager().getDefault4GSlotId();
        }
        int wfcroaming = 0;
        if (Settings.System.getInt(context.getContentResolver(), volteDB, -1) != -1) {
            Settings.System.putInt(context.getContentResolver(), volteDB, getBooleanCarrierConfig(context, KEY_CARRIER_DEFAULT_VOLTE_SWITCH_ON_BOOL, currentsubId) ? 1 : 0);
        }
        if (Settings.Global.getInt(context.getContentResolver(), HW_QCOM_VOLTE_USER_SWITCH, -1) != -1) {
            Settings.Global.putInt(context.getContentResolver(), HW_QCOM_VOLTE_USER_SWITCH, getBooleanCarrierConfig(context, KEY_CARRIER_DEFAULT_VOLTE_SWITCH_ON_BOOL, currentsubId) ? 1 : 0);
        }
        if (hasFiledInDBByFiledName(currentsubId, context, "wfc_ims_enabled")) {
            SubscriptionManager.setSubscriptionProperty(currentsubId, "wfc_ims_enabled", Integer.toString(getBooleanCarrierConfig(context, "carrier_default_wfc_ims_enabled_bool", currentsubId) ? 1 : 0));
        }
        if (hasFiledInDBByFiledName(currentsubId, context, "wfc_ims_mode")) {
            SubscriptionManager.setSubscriptionProperty(currentsubId, "wfc_ims_mode", Integer.toString(getIntCarrierConfig(context, "carrier_default_wfc_ims_mode_int", currentsubId)));
        }
        if (hasFiledInDBByFiledName(currentsubId, context, "wfc_ims_roaming_mode")) {
            SubscriptionManager.setSubscriptionProperty(currentsubId, "wfc_ims_roaming_mode", Integer.toString(getIntCarrierConfig(context, KEY_CARRIER_DEFAULT_WFC_IMS_ROAMING_MODE_INT, currentsubId)));
        }
        if (hasFiledInDBByFiledName(currentsubId, context, "wfc_ims_roaming_enabled")) {
            if (getBooleanCarrierConfig(context, "carrier_default_wfc_ims_roaming_enabled_bool", currentsubId)) {
                wfcroaming = 1;
            }
            SubscriptionManager.setSubscriptionProperty(currentsubId, "wfc_ims_roaming_enabled", Integer.toString(wfcroaming));
        }
        updateImsServiceConfig(context, currentsubId, true);
        context.sendBroadcast(new Intent(ACTION_IMS_FACTORY_RESET).putExtra(SUBID, currentsubId));
    }

    private static boolean hasFiledInDBByFiledName(int currentsubId, Context context, String filedName) {
        boolean z = false;
        if (context == null || filedName == null) {
            return false;
        }
        if (-1 != SubscriptionManager.getIntegerSubscriptionProperty(currentsubId, filedName, -1, context)) {
            z = true;
        }
        return z;
    }

    public static boolean isDualImsAvailable() {
        if (TelephonyManager.getDefault().isMultiSimEnabled()) {
            return HwModemCapability.isCapabilitySupport(21);
        }
        log("the device is not support multisim");
        return false;
    }

    private static boolean isValidParameter(Context context, int subId) {
        if ((subId == 0 || subId == 1) && context != null) {
            return true;
        }
        return false;
    }

    public static void updateWfcMode(Context context, boolean roaming, int subId) throws ImsException {
        boolean isVowifiEnable = isWfcEnabledByPlatform(context, subId);
        int mode = getWfcMode(context, roaming, subId);
        log("updateWfcMode: isVowifiEnable = " + isVowifiEnable + ", mode = " + mode + ", roaming = " + roaming + ", subId = " + subId);
        if (true == isVowifiEnable) {
            boolean hasCust = checkCarrierConfigKeyExist(context, KEY_CARRIER_DEFAULT_WFC_IMS_ROAMING_MODE_INT, subId).booleanValue();
            TelephonyManager tm = (TelephonyManager) context.getSystemService("phone");
            if ((tm != null && roaming == tm.isNetworkRoaming(subId)) || !hasCust) {
                setWfcModeInternal(context, mode, subId);
            }
        }
    }

    public static int setImsConfig(Context context, int subId, String configKey, PersistableBundle configValue) {
        ImsManager imsManager = ImsManager.getInstance(context, subId);
        if (imsManager == null) {
            return -1;
        }
        try {
            ImsConfig config = imsManager.getConfigInterface();
            if (config != null) {
                return config.setImsConfig(configKey, configValue);
            }
            return -1;
        } catch (ImsException e) {
            loge("setImsConfig() got ImsException");
            return -1;
        }
    }

    public static PersistableBundle getImsConfig(Context context, int subId, String configKey) {
        ImsManager imsManager = ImsManager.getInstance(context, subId);
        if (imsManager != null) {
            try {
                ImsConfig config = imsManager.getConfigInterface();
                if (config != null) {
                    return config.getImsConfig(configKey);
                }
            } catch (ImsException e) {
                loge("getImsConfig() got ImsException");
            }
        }
        return null;
    }

    public static void setVolteSwitch(Context context, int subId, boolean enabled) {
        log("setVolteSwitch subId = " + subId + " enabled = " + enabled);
        if (!isValidParameter(context, subId)) {
            loge("setVolteSwitch subId is wrong or context is null, subId is " + subId);
            return;
        }
        Settings.System.putInt(context.getContentResolver(), isDualImsAvailable() ? HW_VOLTE_USER_SWITCH_DUALIMS[subId] : HW_VOLTE_USER_SWITCH, enabled ? 1 : 0);
        ImsManager mgr = ImsManager.getInstance(context, subId);
        if (mgr != null) {
            mgr.setEnhanced4gLteModeSetting(enabled);
        }
    }
}
