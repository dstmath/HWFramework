package com.android.ims;

import android.content.Context;
import android.content.Intent;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.telephony.CarrierConfigManager;
import android.telephony.HwTelephonyManagerInner;
import android.telephony.TelephonyManager;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.internal.telephony.HwModemCapability;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.CarrierConfigManagerEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.SubscriptionManagerEx;
import com.huawei.android.telephony.TelephonyManagerEx;
import com.huawei.ims.ImsConfigEx;
import com.huawei.ims.ImsExceptionEx;
import com.huawei.ims.ImsManagerExt;
import com.huawei.ims.MmTelFeatureConnectionEx;
import com.huawei.utils.HwPartResourceUtils;
import huawei.cust.HwCfgFilePolicy;

public class HwImsManagerInner {
    public static final String ACTION_IMS_FACTORY_RESET = "com.huawei.ACTION_NETWORK_FACTORY_RESET";
    private static final boolean CL_VOLTE_SWITCH_ON = SystemPropertiesEx.getBoolean("ro.config.cl_volte_switch_on", (boolean) DBG);
    private static final boolean DBG = true;
    private static final int DEFAULT_WFC_MODE = 2;
    private static final boolean FEATURE_DUAL_VOWIFI = SystemPropertiesEx.getBoolean("ro.config.hw_dual_vowifi", (boolean) DBG);
    private static final boolean FEATURE_SHOW_VOLTE_SWITCH = SystemPropertiesEx.getBoolean("ro.config.hw_volte_show_switch", (boolean) DBG);
    private static final boolean FEATURE_VOLTE_DYN = SystemPropertiesEx.getBoolean("ro.config.hw_volte_dyn", false);
    private static final byte GBA_MASK = 2;
    public static final String HW_QCOM_VOLTE_USER_SWITCH = "volte_vt_enabled";
    public static final String HW_VOLTE_USER_SWITCH = "hw_volte_user_switch";
    private static final String[] HW_VOLTE_USER_SWITCH_DUALIMS = {"hw_volte_user_switch_0", "hw_volte_user_switch_1"};
    private static final String IMS_SERVICE = "ims";
    private static final int INT_INVALID_VALUE = -1;
    private static final boolean ISATT;
    public static final String KEY_CARRIER_DEFAULT_VOLTE_SWITCH_ON_BOOL = "carrier_default_volte_switch_on_bool";
    public static final String KEY_CARRIER_DEFAULT_WFC_IMS_ROAMING_MODE_INT = "carrier_default_wfc_ims_roaming_mode_int";
    private static final int NR_MODE_NSA = 1;
    private static final int NR_MODE_UNKNOWN = 0;
    private static final String NR_OPTION_MODE = "nr_option_mode";
    public static final String PROP_VILTE_ENABLE = "ro.config.hw_vtlte_on";
    public static final String PROP_VOLTE_ENABLE = "ro.config.hw_volte_on";
    public static final String PROP_VOWIFI_ENABLE = "ro.vendor.config.hw_vowifi";
    private static final int SLOTID_0 = 0;
    private static final int SLOTID_1 = 1;
    public static final String SUBID = "subId";
    private static final String TAG = "HwImsManagerInner";
    private static final int VOWIFI_PREFER_INVALID = 3;
    private static final String[] VT_IMS_ENABLED_DUALIMS = {"vt_ims_enabled_0", "vt_ims_enabled_1"};
    private static boolean mIsConfigUpdated = false;
    private static int[] userSelectWfcMode = {3, 3};

    static {
        boolean z = DBG;
        if (!"07".equals(SystemPropertiesEx.get("ro.config.hw_opta")) || !"840".equals(SystemPropertiesEx.get("ro.config.hw_optb"))) {
            z = false;
        }
        ISATT = z;
    }

    public static boolean isEnhanced4gLteModeSettingEnabledByUser(Context context, int slotId) {
        int enabled;
        log("isEnhanced4gLteModeSettingEnabledByUser :: slotId -> " + slotId);
        if (!isValidParameter(context, slotId)) {
            loge("slotId is wrong or context is null, the result is false, slotId is" + slotId);
            return false;
        }
        int currentSlotId = slotId;
        String dbName = HW_VOLTE_USER_SWITCH;
        if (!isDualImsAvailable()) {
            currentSlotId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
            log("isEnhanced4gLteModeSettingEnabledByUser :: dual-ims is not support, slotId is main-slotId");
        } else {
            dbName = HW_VOLTE_USER_SWITCH_DUALIMS[currentSlotId];
        }
        if (!isSupport5G() || !is5GOpen(currentSlotId) || isShowVolteSwitchInNsa(context, currentSlotId)) {
            if (FEATURE_VOLTE_DYN) {
                if (!getBooleanCarrierConfig(context, "carrier_volte_available_bool", currentSlotId)) {
                    log("KEY_CARRIER_VOLTE_AVAILABLE_BOOL is false, return false");
                    return false;
                } else if (!getBooleanCarrierConfig(context, "carrier_volte_show_switch_bool", currentSlotId)) {
                    log("KEY_CARRIER_VOLTE_SHOW_SWITCH_BOOL is false, return true");
                    return DBG;
                } else {
                    enabled = Settings.System.getInt(context.getContentResolver(), dbName, getVolteDefaultValue(context, currentSlotId));
                    log("FEATURE_VOLTE_DYN is true, result -> " + enabled + "slotId ->" + currentSlotId);
                }
            } else if (!FEATURE_SHOW_VOLTE_SWITCH) {
                return DBG;
            } else {
                enabled = Settings.System.getInt(context.getContentResolver(), dbName, 0);
            }
            log("isEnhanced4gLteModeSettingEnabledByUser result -> " + enabled + "currentSlotId -> " + currentSlotId);
            if (enabled == 1) {
                return DBG;
            }
            return false;
        }
        log("isEnhanced4gLteModeSettingEnabledByUser :: 5G is open, return true, currentSlotId -> " + currentSlotId);
        return DBG;
    }

    public static boolean isNonTtyOrTtyOnVolteEnabled(Context context, int slotId) {
        boolean isNonTtyOrTtyOnVolteEnabled = false;
        if (!isValidParameter(context, slotId)) {
            loge("slotId is wrong or context is null, the result is false, slotId is" + slotId);
            return false;
        } else if (!isDualImsAvailable()) {
            log("isNonTtyOrTtyOnVolteEnabled :: dual-ims is not support");
            return ImsManagerExt.isNonTtyOrTtyOnVolteEnabled(context);
        } else if (getBooleanCarrierConfig(context, "carrier_volte_tty_supported_bool", slotId)) {
            return DBG;
        } else {
            if (Settings.Secure.getInt(context.getContentResolver(), "preferred_tty_mode", 0) == 0) {
                isNonTtyOrTtyOnVolteEnabled = true;
            }
            log("isNonTtyOrTtyOnVolteEnabled result -> " + isNonTtyOrTtyOnVolteEnabled + "slotId" + slotId);
            return isNonTtyOrTtyOnVolteEnabled;
        }
    }

    public static boolean isVolteEnabledByPlatform(Context context, int slotId) {
        if (!isValidParameter(context, slotId)) {
            loge("slotId is wrong or context is null, the result is false, slotId is" + slotId);
            return false;
        } else if (!isDualImsAvailable()) {
            log("isVolteEnabledByPlatform :: dual-ims is not support");
            return ImsManagerExt.isVolteEnabledByPlatform(context);
        } else if (!SystemPropertiesEx.getBoolean(PROP_VOLTE_ENABLE, false)) {
            log("hw_volte_on is false");
            return false;
        } else {
            boolean isCfgVolteEnable = context.getResources().getBoolean(HwPartResourceUtils.getResourceId("config_device_volte_available"));
            boolean isCarrierVolteEnable = getBooleanCarrierConfig(context, "carrier_volte_available_bool", slotId);
            boolean isGbaValid = isGbaValid(context, slotId);
            log("Volte sim adp : Device =" + isCfgVolteEnable + " XML_CarrierConfig =" + isCarrierVolteEnable + " GbaValid =" + isGbaValid + " slotId =" + slotId);
            if (!isCfgVolteEnable || !isCarrierVolteEnable || !isGbaValid) {
                return false;
            }
            return DBG;
        }
    }

    public static boolean isVtEnabledByPlatform(Context context, int slotId) {
        if (!isValidParameter(context, slotId)) {
            loge("slotId is wrong or context is null, the result is false, slotId is" + slotId);
            return false;
        } else if (!isDualImsAvailable()) {
            log("isVtEnabledByPlatform :: dual-ims is not support");
            return ImsManagerExt.isVtEnabledByPlatform(context);
        } else if (!SystemPropertiesEx.getBoolean(PROP_VILTE_ENABLE, false)) {
            log("isVtEnabledByPlatform ro.config.hw_vtlte_on is false");
            return false;
        } else if (!context.getResources().getBoolean(HwPartResourceUtils.getResourceId("config_device_vt_available")) || !getBooleanCarrierConfig(context, "carrier_vt_available_bool", slotId) || !isGbaValid(context, slotId)) {
            return false;
        } else {
            return DBG;
        }
    }

    public static boolean isVtEnabledByUser(Context context, int slotId) {
        if (!isValidParameter(context, slotId)) {
            loge("slotId is wrong or context is null, the result is false, slotId is" + slotId);
            return false;
        } else if (!isDualImsAvailable()) {
            log("isVtEnabledByUser :: dual-ims is not support");
            return ImsManagerExt.isVtEnabledByUser(context);
        } else if (Settings.Global.getInt(context.getContentResolver(), VT_IMS_ENABLED_DUALIMS[slotId], 1) == 1) {
            return DBG;
        } else {
            return false;
        }
    }

    public static boolean isWfcEnabledByUser(Context context, int slotId) {
        if (!isValidParameter(context, slotId)) {
            loge("slotId is wrong or context is null, the result is false, slotId is" + slotId);
            return false;
        } else if (isDualImsAvailable() && FEATURE_DUAL_VOWIFI) {
            return ImsManagerExt.getInstance(context, slotId).isWfcEnabledByUser();
        } else {
            log("isWfcEnabledByUser :: dual-ims is not support");
            if (HwTelephonyManagerInner.getDefault().getDefault4GSlotId() == slotId) {
                return ImsManagerExt.isWfcEnabledByUser(context);
            }
            loge("isWfcEnabledByUser error, slotId should be the mainslotId");
            return false;
        }
    }

    public static void setWfcSetting(Context context, boolean isEnabled, int slotId) {
        if (!isValidParameter(context, slotId)) {
            loge("slotId is wrong or context is null, slotId is " + slotId);
            return;
        }
        boolean isVowifiEnable = isWfcEnabledByPlatform(context, slotId);
        if (!isVowifiEnable) {
            loge("setWfcSetting(), isVowifiEnable is false, slotId is " + slotId);
        } else if (isDualImsAvailable() && FEATURE_DUAL_VOWIFI) {
            setWfcSettingInternal(context, isEnabled, slotId, isVowifiEnable);
        } else if (HwTelephonyManagerInner.getDefault().getDefault4GSlotId() != slotId) {
            loge("setWfcSetting error, slotId should be the mainSlotId");
        } else {
            ImsManagerExt.setWfcSetting(context, isEnabled);
        }
    }

    private static void setWfcSettingInternal(Context context, boolean isEnabled, int slotId, boolean isVowifiEnable) {
        int subId = getSubIdUsingSlotId(slotId);
        log("setWfcSettingInternal slotId ->" + slotId + "subId ->" + subId + "result ->" + isEnabled);
        SubscriptionManagerEx.setSubscriptionProperty(subId, "wfc_ims_enabled", isEnabled ? "1" : "0");
        ImsManagerExt imsManager = ImsManagerExt.getInstance(context, slotId);
        if (imsManager != null) {
            try {
                boolean isRoaming = TelephonyManagerEx.isNetworkRoaming(subId);
                if (isVowifiEnable && userSelectWfcMode[slotId] == 3) {
                    userSelectWfcMode[slotId] = getWfcMode(context, isRoaming, slotId);
                }
                int i = 1;
                changeMmTelCapability(context, slotId, 1, 1, isEnabled);
                if (isEnabled) {
                    if (isVowifiEnable) {
                        log("isVowifiEnable = true, setWfcModeInternal - setting = " + userSelectWfcMode[slotId]);
                        setWfcModeInternal(context, userSelectWfcMode[slotId], slotId);
                    }
                    log("setWfcSettingInternal() : turnOnIms");
                    turnOnIms(imsManager, context, slotId);
                } else if (!getBooleanCarrierConfig(context, "carrier_allow_turnoff_ims_bool", slotId) || (isVolteEnabledByPlatform(context, slotId) && isEnhanced4gLteModeSettingEnabledByUser(context, slotId))) {
                    loge("UnexpectedException, slotId = " + slotId);
                } else {
                    log("setWfcSettingInternal() : imsServiceAllowTurnOff -> turnOffIms");
                    turnOffIms(imsManager, context, slotId);
                }
                if (isEnabled) {
                    i = getWfcMode(context, isRoaming, slotId);
                }
                setWfcModeInternal(context, i, slotId);
            } catch (ImsExceptionEx e) {
                loge("setWfcSettingInternal(): ImsException");
            }
        }
    }

    public static int getWfcMode(Context context, int slotId) {
        if (!isValidParameter(context, slotId)) {
            loge("slotId is wrong or context is null, the result is deault_wfc_mode, slotId is" + slotId);
            return 2;
        } else if (isDualImsAvailable() && FEATURE_DUAL_VOWIFI) {
            return ImsManagerExt.getInstance(context, slotId).getWfcMode();
        } else {
            log("getWfcMode :: dual-ims is not support");
            if (HwTelephonyManagerInner.getDefault().getDefault4GSlotId() == slotId) {
                return ImsManagerExt.getWfcMode(context);
            }
            loge("getWfcMode error, subId should be the mainslotId");
            return 2;
        }
    }

    public static void setWfcMode(Context context, int wfcMode, int slotId) {
        if (!isValidParameter(context, slotId)) {
            loge("subId is wrong or context is null, subID is" + slotId);
            return;
        }
        boolean isVowifiEnable = isWfcEnabledByPlatform(context, slotId);
        if (!isDualImsAvailable() || !FEATURE_DUAL_VOWIFI) {
            log("setWfcMode :: dual-ims is not support");
            if (HwTelephonyManagerInner.getDefault().getDefault4GSlotId() != slotId || !isVowifiEnable) {
                loge("setWfcMode error, subId should be the mainslotId or vowifi is not enabled");
            } else {
                ImsManagerExt.setWfcMode(context, wfcMode);
            }
        } else {
            ImsManagerExt mgr = ImsManagerExt.getInstance(context, slotId);
            if (isVowifiEnable) {
                mgr.setWfcMode(wfcMode);
                log("setWfcMode - slotId=" + slotId + "setting=" + wfcMode);
                userSelectWfcMode[slotId] = wfcMode;
            }
        }
    }

    public static int getWfcMode(Context context, boolean isRoaming, int slotId) {
        if (!isValidParameter(context, slotId)) {
            loge("slotId is wrong or context is null, the result is deault_wfc_mode, slotId is" + slotId);
            return 2;
        } else if (isDualImsAvailable() && FEATURE_DUAL_VOWIFI) {
            return ImsManagerExt.getInstance(context, slotId).getWfcMode(isRoaming);
        } else {
            log("getWfcMode :: dual-ims is not supportroaming is " + isRoaming);
            if (HwTelephonyManagerInner.getDefault().getDefault4GSlotId() == slotId) {
                return ImsManagerExt.getWfcMode(context, isRoaming);
            }
            loge("getWfcMode error, subId should be the mainslotId");
            return 2;
        }
    }

    public static void setWfcMode(Context context, int wfcMode, boolean isRoaming, int slotId) {
        if (!isValidParameter(context, slotId)) {
            loge("slotId is wrong or context is null, slotId is" + slotId);
            return;
        }
        boolean isVowifiEnable = isWfcEnabledByPlatform(context, slotId);
        if (!isDualImsAvailable() || !FEATURE_DUAL_VOWIFI) {
            log("setWfcMode :: dual-ims is not supportroaming is " + isRoaming);
            if (HwTelephonyManagerInner.getDefault().getDefault4GSlotId() != slotId || !isVowifiEnable) {
                loge("setWfcMode error, slotId should be the mainslotId or vowifi is not enabled");
            } else {
                ImsManagerExt.setWfcMode(context, wfcMode, isRoaming);
            }
        } else if (isVowifiEnable) {
            ImsManagerExt.getInstance(context, slotId).setWfcMode(wfcMode, isRoaming);
            userSelectWfcMode[slotId] = wfcMode;
        }
    }

    private static void setWfcModeInternal(Context context, final int wfcMode, int slotId) {
        final ImsManagerExt imsManager = ImsManagerExt.getInstance(context, slotId);
        if (imsManager != null) {
            new Thread(new Runnable() {
                /* class com.android.ims.HwImsManagerInner.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    try {
                        imsManager.getConfigInterface().setProvisionedValue(27, wfcMode);
                    } catch (ImsExceptionEx e) {
                        HwImsManagerInner.loge("setWfcModeInternal(): ", e);
                    }
                }
            }).start();
        }
    }

    public static boolean isWfcRoamingEnabledByUser(Context context, int slotId) {
        if (!isValidParameter(context, slotId)) {
            loge("slotId is wrong or context is null, the result is false, slotId is" + slotId);
            return false;
        } else if (!isDualImsAvailable() || !FEATURE_DUAL_VOWIFI) {
            log("isWfcRoamingEnabledByUser :: dual-ims is not support");
            if (HwTelephonyManagerInner.getDefault().getDefault4GSlotId() == slotId) {
                return ImsManagerExt.isWfcRoamingEnabledByUser(context);
            }
            loge("isWfcRoamingEnabledByUser error, slotId should be the mainsubId");
            return false;
        } else {
            if (SubscriptionManagerEx.getIntegerSubscriptionProperty(getSubIdUsingSlotId(slotId), "wfc_ims_roaming_enabled", getBooleanCarrierConfig(context, "carrier_default_wfc_ims_roaming_enabled_bool", slotId) ? 1 : 0, context) == 1) {
                return DBG;
            }
            return false;
        }
    }

    public static void setWfcRoamingSetting(Context context, boolean isEnabled, int slotId) {
        if (!isValidParameter(context, slotId)) {
            loge("slotId is wrong or context is null, slotId is" + slotId);
        } else if (!isDualImsAvailable() || !FEATURE_DUAL_VOWIFI) {
            log("setWfcRoamingSetting :: dual-ims is not support");
            if (HwTelephonyManagerInner.getDefault().getDefault4GSlotId() != slotId) {
                loge("setWfcRoamingSetting error, slotId should be the mainsubId");
            } else {
                ImsManagerExt.setWfcRoamingSetting(context, isEnabled);
            }
        } else {
            SubscriptionManagerEx.setSubscriptionProperty(getSubIdUsingSlotId(slotId), "wfc_ims_roaming_enabled", Integer.toString(isEnabled ? 1 : 0));
            setWfcRoamingSettingInternal(context, isEnabled, slotId);
        }
    }

    private static void setWfcRoamingSettingInternal(Context context, boolean isEnabled, int slotId) {
        final ImsManagerExt imsManager = ImsManagerExt.getInstance(context, slotId);
        if (imsManager != null) {
            final int i = isEnabled ? 1 : 0;
            new Thread(new Runnable() {
                /* class com.android.ims.HwImsManagerInner.AnonymousClass2 */

                @Override // java.lang.Runnable
                public void run() {
                    try {
                        imsManager.getConfigInterface().setProvisionedValue(26, i);
                    } catch (ImsExceptionEx e) {
                        HwImsManagerInner.loge("setWfcRoamingSettingInternal(): ", e);
                    }
                }
            }).start();
        }
    }

    public static boolean isWfcEnabledByPlatform(Context context, int slotId) {
        if (!isValidParameter(context, slotId)) {
            loge("slotId is wrong or context is null, the result is false, slotId is" + slotId);
            return false;
        } else if (!SystemPropertiesEx.getBoolean(PROP_VOWIFI_ENABLE, false)) {
            loge("hw_vowifi prop is false, return false");
            return false;
        } else if (!isDualImsAvailable() || !FEATURE_DUAL_VOWIFI) {
            log("isWfcEnabledByPlatform :: dual-ims is not support");
            if (HwTelephonyManagerInner.getDefault().getDefault4GSlotId() == slotId) {
                return ImsManagerExt.isWfcEnabledByPlatform(context);
            }
            loge("isWfcEnabledByPlatform error, slotId should be the mainslotId, return false");
            return false;
        } else {
            boolean isCfgWfcEnabled = context.getResources().getBoolean(HwPartResourceUtils.getResourceId("config_device_wfc_ims_available"));
            boolean isCarrierWfcEnabled = getBooleanCarrierConfig(context, "carrier_wfc_ims_available_bool", slotId);
            boolean isGbaValid = isGbaValid(context, slotId);
            log("Vowifi sim adp : Device =" + isCfgWfcEnabled + " XML_CarrierConfig =" + isCarrierWfcEnabled + " GbaValid =" + isGbaValid + " slotId =" + slotId);
            if (!isCfgWfcEnabled || !isCarrierWfcEnabled || !isGbaValid) {
                return false;
            }
            return DBG;
        }
    }

    private static boolean isGbaValid(Context context, int slotId) {
        boolean booleanCarrierConfig = getBooleanCarrierConfig(context, "carrier_ims_gba_required_bool", slotId);
        boolean isGbaValid = DBG;
        if (!booleanCarrierConfig) {
            return DBG;
        }
        String efIst = TelephonyManagerEx.getIsimIst();
        if (efIst == null) {
            loge("ISF is NULL");
            return DBG;
        }
        if (efIst.length() <= 1 || (((byte) efIst.charAt(1)) & GBA_MASK) == 0) {
            isGbaValid = false;
        }
        log("GBA capable=" + isGbaValid + ", ISF=" + efIst);
        return isGbaValid;
    }

    public static void updateImsServiceConfig(Context context, int inputSlotId, boolean isForced) {
        int slotId = inputSlotId;
        if (!isValidParameter(context, slotId)) {
            loge("slotId is wrong or context is null, the result is false, slotId is" + slotId);
            return;
        }
        boolean isMtk = HuaweiTelephonyConfigs.isMTKPlatform();
        if (!isDualImsAvailable() && isMtk) {
            int mainSlotId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
            log("updateImsServiceConfig :: dual-ims is not support and MTK platform, change slotId from " + slotId + " to mainSlotId " + mainSlotId);
            slotId = mainSlotId;
        }
        if (!isDualImsAvailable() && !isMtk) {
            log("updateImsServiceConfig :: dual-ims is not support");
            ImsManagerExt.updateImsServiceConfig(context, HwTelephonyManagerInner.getDefault().getDefault4GSlotId(), isForced);
        } else if (isForced || TelephonyManagerEx.getDefault().getSimState(slotId) == 5) {
            ImsManagerExt imsManager = ImsManagerExt.getInstance(context, slotId);
            if (imsManager == null) {
                return;
            }
            if (!mIsConfigUpdated || isForced) {
                try {
                    if (!(updateVolteFeatureValue(context, slotId) | updateVideoCallFeatureValue(context, slotId)) && !updateWfcFeatureAndProvisionedValues(context, slotId)) {
                        if (getBooleanCarrierConfig(context, "carrier_allow_turnoff_ims_bool", slotId)) {
                            log("updateImsServiceConfig: turnOffIms, slotId is" + slotId);
                            turnOffIms(imsManager, context, slotId);
                            mIsConfigUpdated = DBG;
                        }
                    }
                    log("updateImsServiceConfig: turnOnIms, slotId is" + slotId);
                    turnOnIms(imsManager, context, slotId);
                    mIsConfigUpdated = DBG;
                } catch (ImsExceptionEx e) {
                    loge("updateImsServiceConfig: ", e);
                    mIsConfigUpdated = false;
                }
            }
        } else {
            log("updateImsServiceConfig: SIM not ready, slotId is" + slotId);
        }
    }

    private static boolean updateVolteFeatureValue(Context context, int slotId) throws ImsExceptionEx {
        boolean available = isVolteEnabledByPlatform(context, slotId);
        boolean enabled = isEnhanced4gLteModeSettingEnabledByUser(context, slotId);
        boolean isNonTty = isNonTtyOrTtyOnVolteEnabled(context, slotId);
        boolean isFeatureOn = available && enabled && isNonTty;
        log("updateVolteFeatureValue: available = " + available + ", enabled = " + enabled + ", nonTTY = " + isNonTty);
        changeMmTelCapability(context, slotId, 1, 0, isFeatureOn);
        return isFeatureOn;
    }

    public static void changeMmTelCapability(Context context, int slotId, int capability, int radioTech, boolean isEnabled) throws ImsExceptionEx {
        ImsManagerExt.getInstance(context, slotId).changeMmTelCapability(capability, radioTech, isEnabled);
    }

    private static boolean isShowVolteSwitchInNsa(Context context, int slotId) {
        if (!isValidParameter(context, slotId)) {
            loge("isShowVolteSwitchInNsa: slotId is wrong or context is null, slotId -> " + slotId);
            return false;
        } else if (Settings.System.getInt(context.getContentResolver(), NR_OPTION_MODE, 0) != 1) {
            log("isShowVolteSwitchInNsa: nr option mode is not nsa, slotId -> " + slotId);
            return false;
        } else {
            Boolean isVolteSwitchInNsa = (Boolean) HwCfgFilePolicy.getValue("show_volte_switch_in_nsa", slotId, Boolean.class);
            if (isVolteSwitchInNsa != null && isVolteSwitchInNsa.booleanValue()) {
                return DBG;
            }
            log("isShowVolteSwitchInNsa: carrier parameter is false, slotId -> " + slotId);
            return false;
        }
    }

    private static boolean updateVideoCallFeatureValue(Context context, int slotId) throws ImsExceptionEx {
        boolean available = isVtEnabledByPlatform(context, slotId);
        boolean enabled = isVtEnabledByUser(context, slotId);
        boolean isNonTty = isNonTtyOrTtyOnVolteEnabled(context, slotId);
        boolean isFeatureOn = (!available || !enabled || !isNonTty) ? false : DBG;
        log("updateVideoCallFeatureValue: available = " + available + ", enabled = " + enabled + ", nonTTY = " + isNonTty + ", slotId = " + slotId);
        changeMmTelCapability(context, slotId, 2, 0, isFeatureOn);
        return isFeatureOn;
    }

    private static boolean updateWfcFeatureAndProvisionedValues(Context context, int slotId) throws ImsExceptionEx {
        boolean isNetworkRoaming = TelephonyManagerEx.isNetworkRoaming(getSubIdUsingSlotId(slotId));
        boolean available = isWfcEnabledByPlatform(context, slotId);
        boolean enabled = isWfcEnabledByUser(context, slotId);
        int mode = getWfcMode(context, isNetworkRoaming, slotId);
        boolean roaming = isWfcRoamingEnabledByUser(context, slotId);
        boolean isFeatureOn = available && enabled;
        log("updateWfcFeatureAndProvisionedValues: available = " + available + ", enabled = " + enabled + ", mode = " + mode + ", roaming = " + roaming + ", slotId = " + slotId);
        changeMmTelCapability(context, slotId, 1, 1, isFeatureOn);
        if (!isFeatureOn) {
            mode = 1;
            roaming = false;
        }
        setWfcModeInternal(context, mode, slotId);
        setWfcRoamingSettingInternal(context, roaming, slotId);
        return isFeatureOn;
    }

    private static boolean checkCarrierConfigKeyExist(Context context, String key, int slotId) {
        boolean isExist = false;
        CarrierConfigManager configManager = (CarrierConfigManager) context.getSystemService("carrier_config");
        PersistableBundle b = null;
        if (configManager != null) {
            b = configManager.getConfigForSubId(getSubIdUsingSlotId(slotId));
        }
        if (!(b == null || b.get(key) == null)) {
            log("checkCarrierConfigKeyExist, b.getkey = " + b.get(key) + "slotId" + slotId);
            isExist = DBG;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("carrierConfig key[");
        sb.append(key);
        sb.append("] ");
        sb.append(isExist ? "exists" : "does not exist");
        log(sb.toString());
        return isExist;
    }

    private static boolean getBooleanCarrierConfig(Context context, String key, int slotId) {
        CarrierConfigManager configManager = (CarrierConfigManager) context.getSystemService("carrier_config");
        PersistableBundle b = null;
        if (configManager != null) {
            b = configManager.getConfigForSubId(getSubIdUsingSlotId(slotId));
        }
        if (b == null || b.get(key) == null) {
            return CarrierConfigManagerEx.getDefaultConfig().getBoolean(key);
        }
        return b.getBoolean(key);
    }

    private static int getIntCarrierConfig(Context context, String key, int slotId) {
        CarrierConfigManager configManager = (CarrierConfigManager) context.getSystemService("carrier_config");
        PersistableBundle b = null;
        if (configManager != null) {
            b = configManager.getConfigForSubId(getSubIdUsingSlotId(slotId));
        }
        if (b == null || b.get(key) == null) {
            return CarrierConfigManagerEx.getDefaultConfig().getInt(key);
        }
        return b.getInt(key);
    }

    private static void checkAndThrowExceptionIfServiceUnavailable(ImsManagerExt imsManager, Context context, int slotId) throws ImsExceptionEx {
        if (imsManager.isImsServiceProxyNull() || !imsManager.isImsServiceProxyBinderAlive()) {
            createImsService(imsManager, context, slotId);
            if (imsManager.isImsServiceProxyNull()) {
                throw new ImsExceptionEx("Service is unavailable", 106);
            }
        }
    }

    private static void createImsService(ImsManagerExt imsManager, Context context, int slotId) {
        imsManager.createImsServiceProxy(getServiceProxy(imsManager, context, slotId));
    }

    private static MmTelFeatureConnectionEx getServiceProxy(ImsManagerExt imsManager, Context context, int subId) {
        return HwImsManagerInnerUtils.getServiceProxy(imsManager, context, subId);
    }

    private static void log(String s) {
        RlogEx.i(TAG, s);
    }

    private static void loge(String s) {
        RlogEx.e(TAG, s);
    }

    /* access modifiers changed from: private */
    public static void loge(String s, Throwable t) {
        RlogEx.e(TAG, s, t);
    }

    private static void turnOnIms(ImsManagerExt imsManager, Context context, int slotId) throws ImsExceptionEx {
        checkAndThrowExceptionIfServiceUnavailable(imsManager, context, slotId);
        if (!ISATT || isEnhanced4gLteModeSettingEnabledByUser(context, slotId)) {
            TelephonyManagerEx.enableIms((TelephonyManager) context.getSystemService("phone"), slotId);
        } else {
            log("turnOnIms: Enhanced LTE Service is off, return.");
        }
    }

    private static void turnOffIms(ImsManagerExt imsManager, Context context, int slotId) throws ImsExceptionEx {
        checkAndThrowExceptionIfServiceUnavailable(imsManager, context, slotId);
        if (!ISATT || !isEnhanced4gLteModeSettingEnabledByUser(context, slotId)) {
            TelephonyManagerEx.disableIms((TelephonyManager) context.getSystemService("phone"), slotId);
        } else {
            log("turnOffIms: Enhanced LTE Service is on, return.");
        }
    }

    public static void factoryReset(Context context, int slotId) {
        if (!isValidParameter(context, slotId)) {
            loge("slotId is wrong or context is null, slotId is" + slotId);
            return;
        }
        int currentSlotId = slotId;
        String volteDb = HW_VOLTE_USER_SWITCH_DUALIMS[currentSlotId];
        if (!isDualImsAvailable()) {
            log("factoryReset :: dual-ims is not support");
            volteDb = HW_VOLTE_USER_SWITCH;
            currentSlotId = HwTelephonyManagerInner.getDefault().getDefault4GSlotId();
        }
        int currentSubId = getSubIdUsingSlotId(currentSlotId);
        if (Settings.System.getInt(context.getContentResolver(), volteDb, -1) != -1) {
            Settings.System.putInt(context.getContentResolver(), volteDb, getVolteDefaultValue(context, currentSlotId));
        }
        if (Settings.Global.getInt(context.getContentResolver(), HW_QCOM_VOLTE_USER_SWITCH, -1) != -1) {
            Settings.Global.putInt(context.getContentResolver(), HW_QCOM_VOLTE_USER_SWITCH, getVolteDefaultValue(context, currentSlotId));
        }
        if (hasFiledInDbByFiledName(currentSubId, context, "wfc_ims_enabled")) {
            SubscriptionManagerEx.setSubscriptionProperty(currentSubId, "wfc_ims_enabled", Integer.toString(getBooleanCarrierConfig(context, "carrier_default_wfc_ims_enabled_bool", currentSlotId) ? 1 : 0));
        }
        if (hasFiledInDbByFiledName(currentSubId, context, "wfc_ims_mode")) {
            SubscriptionManagerEx.setSubscriptionProperty(currentSubId, "wfc_ims_mode", Integer.toString(getIntCarrierConfig(context, "carrier_default_wfc_ims_mode_int", currentSlotId)));
        }
        if (hasFiledInDbByFiledName(currentSubId, context, "wfc_ims_roaming_mode")) {
            SubscriptionManagerEx.setSubscriptionProperty(currentSubId, "wfc_ims_roaming_mode", Integer.toString(getIntCarrierConfig(context, KEY_CARRIER_DEFAULT_WFC_IMS_ROAMING_MODE_INT, currentSlotId)));
        }
        if (hasFiledInDbByFiledName(currentSubId, context, "wfc_ims_roaming_enabled")) {
            SubscriptionManagerEx.setSubscriptionProperty(currentSubId, "wfc_ims_roaming_enabled", Integer.toString(getBooleanCarrierConfig(context, "carrier_default_wfc_ims_roaming_enabled_bool", currentSlotId) ? 1 : 0));
        }
        updateImsServiceConfig(context, currentSlotId, DBG);
        context.sendBroadcast(new Intent(ACTION_IMS_FACTORY_RESET).putExtra(SUBID, currentSlotId));
    }

    private static int getVolteDefaultValue(Context context, int currentSlotId) {
        return HwTelephonyManagerInner.getDefault().isCTSimCard(currentSlotId) ? CL_VOLTE_SWITCH_ON ? 1 : 0 : getBooleanCarrierConfig(context, KEY_CARRIER_DEFAULT_VOLTE_SWITCH_ON_BOOL, currentSlotId) ? 1 : 0;
    }

    private static boolean hasFiledInDbByFiledName(int currentSubId, Context context, String filedName) {
        if (context == null || filedName == null || SubscriptionManagerEx.getIntegerSubscriptionProperty(currentSubId, filedName, -1, context) == -1) {
            return false;
        }
        return DBG;
    }

    public static boolean isDualImsAvailable() {
        if (TelephonyManagerEx.isMultiSimEnabled()) {
            return HwModemCapability.isCapabilitySupport(21);
        }
        log("the device is not support multisim");
        return false;
    }

    private static boolean isValidParameter(Context context, int slotId) {
        if ((slotId == 0 || slotId == 1) && context != null) {
            return DBG;
        }
        return false;
    }

    public static void updateWfcMode(Context context, boolean isRoaming, int slotId) throws ImsExceptionEx {
        boolean isVowifiEnable = isWfcEnabledByPlatform(context, slotId);
        int mode = getWfcMode(context, isRoaming, slotId);
        log("updateWfcMode: isVowifiEnable = " + isVowifiEnable + ", mode = " + mode + ", roaming = " + isRoaming + ", slotId = " + slotId);
        if (true == isVowifiEnable) {
            boolean hasCust = checkCarrierConfigKeyExist(context, KEY_CARRIER_DEFAULT_WFC_IMS_ROAMING_MODE_INT, slotId);
            if (isRoaming == TelephonyManagerEx.isNetworkRoaming(getSubIdUsingSlotId(slotId)) || !hasCust) {
                setWfcModeInternal(context, mode, slotId);
            }
        }
    }

    public static int setImsConfig(Context context, int slotId, String configKey, PersistableBundle configValue) {
        ImsManagerExt imsManager = ImsManagerExt.getInstance(context, slotId);
        if (imsManager == null) {
            return -1;
        }
        try {
            ImsConfigEx config = imsManager.getConfigInterface();
            if (config != null) {
                return config.setImsConfig(configKey, configValue);
            }
            return -1;
        } catch (ImsExceptionEx e) {
            loge("setImsConfig() got ImsException");
            return -1;
        }
    }

    public static PersistableBundle getImsConfig(Context context, int slotId, String configKey) {
        ImsManagerExt imsManager = ImsManagerExt.getInstance(context, slotId);
        if (imsManager == null) {
            return null;
        }
        try {
            ImsConfigEx config = imsManager.getConfigInterface();
            if (config != null) {
                return config.getImsConfig(configKey);
            }
            return null;
        } catch (ImsExceptionEx e) {
            loge("getImsConfig() got ImsException");
            return null;
        }
    }

    public static void setVolteSwitch(Context context, int slotId, boolean isEnabled) {
        log("setVolteSwitch slotId = " + slotId + " enabled = " + isEnabled);
        if (!isValidParameter(context, slotId)) {
            loge("setVolteSwitch subId is wrong or context is null, slotId is" + slotId);
            return;
        }
        ImsManagerExt mgr = ImsManagerExt.getInstance(context, slotId);
        if (mgr != null) {
            mgr.setEnhanced4gLteModeSetting(isEnabled);
        }
    }

    private static int getSubIdUsingSlotId(int slotId) {
        return SubscriptionManagerEx.getSubIdUsingSlotId(slotId);
    }

    private static boolean isSupport5G() {
        return HwTelephonyManagerInner.getDefault().isNrSupported();
    }

    private static boolean is5GOpen(int slotId) {
        if (1 == HwTelephonyManagerInner.getDefault().getServiceAbility(slotId, 1)) {
            return DBG;
        }
        return false;
    }
}
