package com.android.server.devicepolicy.plugins;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.hdm.HwDeviceManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.DiskInfo;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import com.android.internal.telephony.HuaweiTelephonyConfigs;
import com.android.server.devicepolicy.DevicePolicyPlugin;
import com.android.server.devicepolicy.HwDevicePolicyManagerService;
import com.android.server.devicepolicy.HwLog;
import com.android.server.devicepolicy.PolicyStruct;
import com.huawei.kidsmode.HallManager;
import dalvik.system.DexClassLoader;
import huawei.android.os.HwProtectAreaManager;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class DeviceControlPlugin extends DevicePolicyPlugin {
    private static final String ALLOW_CHARGING_ADB = "allow_charging_adb";
    private static final String BUNDLE_VALUE_NAME = "value";
    private static final String DB_EYES_PROTECTION_MODE = "eyes_protection_mode";
    private static final String EYES_INTERFACE_REGISTER_DISTANCE = "registerDistanceEyesProtect";
    private static final String EYES_INTERFACE_REGISTER_FILP = "registerFlipEyesProtect";
    private static final String EYES_INTERFACE_REGISTER_LIGHT = "registerLightEyesProtect";
    private static final String EYES_INTERFACE_UNREGISTER_DISTANCE = "unRegisterDistanceEyesProtect";
    private static final String EYES_INTERFACE_UNREGISTER_FILP = "unRegisterFlipEyesProtect";
    private static final String EYES_INTERFACE_UNREGISTER_LIGHT = "unRegisterLightEyesProtect";
    private static final String EYES_PATCH = "/hw_product/jar/HiEyeCare/HiEyeCareLib.jar";
    private static final int EYE_CARE_RESULT_REGESITER_AGAIN = 105;
    private static final int EYE_CARE_RESULT_SUCCESS = 0;
    private static final int EYE_CARE_RESULT_UNREGESITER = 106;
    private static final int EYE_DISTANCE_OFF = 0;
    private static final int EYE_DISTANCE_ON = 1;
    private static final int EYE_PROTECTION_CLOSE = 0;
    private static final int EYE_PROTECTION_ON = 1;
    private static final boolean IS_SUPPORT_THIRD_VOICE_ASSISTANT = SystemProperties.getBoolean("hw_mc.voiceassistant.support_third_voiceassistant", false);
    private static final String OEMINFO_CC_MODE_STATE = "SYSTEM_CCMODE_STATE";
    public static final String POLICY_TURN_ON_EYE_COMFORT = "device_control_turn_on_eye_comfort";
    private static final int READ_LENGTH = 8;
    public static final String TAG = "DeviceControlPlugin";
    private static final String UPDATE_NOTIFICATION_VOICE_ASSISTANT = "com.huawei.ai.wakeup.action.MDM_POLICY_UPDATE";
    private static Class sClassEyesProtect;
    private static DexClassLoader sDexClassLoader;
    private static boolean sIsEyesProtectInited = false;
    private static Method sMethodEyesProtect;
    private static Object sObjectEyesProtect;

    public DeviceControlPlugin(Context context) {
        super(context);
    }

    public String getPluginName() {
        return getClass().getSimpleName();
    }

    public PolicyStruct getPolicyStruct() {
        HwLog.i(TAG, "getPolicyStruct");
        PolicyStruct struct = new PolicyStruct(this);
        struct.addStruct(POLICY_TURN_ON_EYE_COMFORT, PolicyStruct.PolicyType.STATE, new String[]{"value"});
        struct.addStruct("device_control_turn_on_cc_mode", PolicyStruct.PolicyType.STATE, new String[]{"value"});
        struct.addStruct("device_control_unmount_usb_devices", PolicyStruct.PolicyType.STATE, new String[]{"value"});
        struct.addStruct("device_control_turn_on_usb_debug_mode", PolicyStruct.PolicyType.STATE, new String[]{"value"});
        struct.addStruct("device_control_set_media_control_disabled", PolicyStruct.PolicyType.STATE, new String[]{"value"});
        struct.addStruct("sleep-time-interval-after-power-on", PolicyStruct.PolicyType.CONFIGURATION, new String[]{"value"});
        struct.addStruct("default-voice-assistant", PolicyStruct.PolicyType.CONFIGURATION, new String[]{"voice-assistant", "front-apps"});
        struct.addStruct("policy_enable_distance_eyes_protect", PolicyStruct.PolicyType.STATE, new String[]{"value"});
        struct.addStruct("policy_enable_flip_eyes_protect", PolicyStruct.PolicyType.STATE, new String[]{"value"});
        struct.addStruct("policy_enable_light_eyes_protect", PolicyStruct.PolicyType.STATE, new String[]{"value"});
        return struct;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public boolean checkCallingPermission(ComponentName who, String policyName) {
        char c;
        HwLog.i(TAG, "checkCallingPermission");
        switch (policyName.hashCode()) {
            case -2079610824:
                if (policyName.equals("device_control_unmount_usb_devices")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case -1082045907:
                if (policyName.equals("policy_enable_distance_eyes_protect")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case -637888534:
                if (policyName.equals("policy_enable_light_eyes_protect")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case -503372106:
                if (policyName.equals("sleep-time-interval-after-power-on")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case -19490507:
                if (policyName.equals("policy_enable_flip_eyes_protect")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            case 596120695:
                if (policyName.equals("default-voice-assistant")) {
                    c = '\b';
                    break;
                }
                c = 65535;
                break;
            case 1494933747:
                if (policyName.equals("device_control_turn_on_usb_debug_mode")) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case 1834280257:
                if (policyName.equals("device_control_set_media_control_disabled")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 1877933817:
                if (policyName.equals("device_control_turn_on_cc_mode")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
            case 1:
            case 2:
            case 3:
            case HwDevicePolicyManagerService.SD_CRYPT_STATE_DECRYPTING /* 4 */:
            case HwDevicePolicyManagerService.SD_CRYPT_STATE_MISMATCH /* 5 */:
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_DEVICE_MANAGER", "does not have device_manager MDM permission!");
                break;
            case HwDevicePolicyManagerService.SD_CRYPT_STATE_WAIT_UNLOCK /* 6 */:
            case 7:
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_USB", "does not have MDM_USB permission!");
                break;
            case READ_LENGTH /* 8 */:
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_VOICEASSISTANT", "does not have voiceAsistant mdm permission!");
                break;
            default:
                this.mContext.enforceCallingOrSelfPermission("com.huawei.permission.sec.MDM_APP_MANAGEMENT", "does not have app_management MDM permission!");
                break;
        }
        return true;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    public boolean onSetPolicy(ComponentName who, String policyName, Bundle policyData, boolean isEffective) {
        Log.i(TAG, "onSetPolicy: " + policyName);
        char c = 0;
        if (!checkCallingPermission(who, policyName) || policyData == null) {
            return false;
        }
        boolean isSetSuccess = true;
        long identityToken = Binder.clearCallingIdentity();
        try {
            switch (policyName.hashCode()) {
                case -2079610824:
                    if (policyName.equals("device_control_unmount_usb_devices")) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case -1082045907:
                    if (policyName.equals("policy_enable_distance_eyes_protect")) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case -637888534:
                    if (policyName.equals("policy_enable_light_eyes_protect")) {
                        c = 7;
                        break;
                    }
                    c = 65535;
                    break;
                case -19490507:
                    if (policyName.equals("policy_enable_flip_eyes_protect")) {
                        c = 6;
                        break;
                    }
                    c = 65535;
                    break;
                case 596120695:
                    if (policyName.equals("default-voice-assistant")) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case 1494933747:
                    if (policyName.equals("device_control_turn_on_usb_debug_mode")) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case 1576234773:
                    if (policyName.equals(POLICY_TURN_ON_EYE_COMFORT)) {
                        break;
                    }
                    c = 65535;
                    break;
                case 1877933817:
                    if (policyName.equals("device_control_turn_on_cc_mode")) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            switch (c) {
                case 0:
                    onSetEyeComfotPolicy(policyData);
                    break;
                case 1:
                    isSetSuccess = onSetCCModePolicy(policyData);
                    break;
                case 2:
                    unmountUsbDevices(policyData);
                    break;
                case 3:
                    isSetSuccess = onSetUsbDebugModePolicy(policyData);
                    break;
                case HwDevicePolicyManagerService.SD_CRYPT_STATE_DECRYPTING /* 4 */:
                    isSetSuccess = IS_SUPPORT_THIRD_VOICE_ASSISTANT;
                    break;
                case HwDevicePolicyManagerService.SD_CRYPT_STATE_MISMATCH /* 5 */:
                case HwDevicePolicyManagerService.SD_CRYPT_STATE_WAIT_UNLOCK /* 6 */:
                case 7:
                    if (!sIsEyesProtectInited && initialization(this.mContext.getApplicationContext())) {
                        sIsEyesProtectInited = true;
                    }
                    isSetSuccess = false;
                    if (sIsEyesProtectInited) {
                        isSetSuccess = protectEyes(who, policyName, policyData.getBoolean("value"));
                        break;
                    }
                    break;
            }
            return isSetSuccess;
        } finally {
            Binder.restoreCallingIdentity(identityToken);
        }
    }

    private void onSetEyeComfotPolicy(Bundle policyData) {
        if (policyData == null) {
            HwLog.i(TAG, "onSetEyeComfotPolicy policyData is null");
            return;
        }
        Settings.System.putInt(this.mContext.getContentResolver(), DB_EYES_PROTECTION_MODE, policyData.getBoolean("value", false) ? 1 : 0);
    }

    private boolean onSetCCModePolicy(Bundle policyData) {
        if (!HuaweiTelephonyConfigs.isHisiPlatform()) {
            HwLog.i(TAG, "current platform unsupport cc mode");
            return false;
        }
        String modeState = policyData.getBoolean("value", false) ? "enable" : "disable";
        String[] readBuf = {"AA"};
        int[] errorRet = new int[1];
        int ret = HwProtectAreaManager.getInstance().writeProtectArea(OEMINFO_CC_MODE_STATE, modeState.length(), modeState, errorRet);
        if (ret != 0) {
            HwLog.i(TAG, "writeProtectArea: ret = " + ret + " errorRet = " + Arrays.toString(errorRet));
            return false;
        }
        int ret2 = HwProtectAreaManager.getInstance().readProtectArea(OEMINFO_CC_MODE_STATE, (int) READ_LENGTH, readBuf, errorRet);
        if (ret2 != 0 || readBuf.length <= 0) {
            HwLog.i(TAG, "readProtectArea: ret = " + ret2 + " errorRet = " + Arrays.toString(errorRet));
            return false;
        }
        HwLog.i(TAG, "writeValue = " + modeState + " readValue = " + readBuf[0]);
        return modeState.equals(readBuf[0]);
    }

    private void unmountUsbDevices(Bundle policyData) {
        List<VolumeInfo> volumes;
        DiskInfo diskInfo;
        StorageManager storageManager = (StorageManager) this.mContext.getSystemService(StorageManager.class);
        if (storageManager == null) {
            HwLog.e(TAG, "storageManager is null, return!");
            return;
        }
        String uuid = policyData.getString("value");
        if (uuid != null) {
            VolumeInfo vol = storageManager.findVolumeByUuid(uuid);
            if (vol == null) {
                HwLog.i(TAG, "unmount USB device failed, VolumeInfo is null");
                return;
            } else {
                volumes = new ArrayList<>(1);
                volumes.add(vol);
            }
        } else {
            volumes = storageManager.getVolumes();
        }
        for (VolumeInfo vol2 : volumes) {
            if (!(vol2 == null || (diskInfo = vol2.getDisk()) == null || !diskInfo.isUsb())) {
                storageManager.unmount(vol2.getId());
            }
        }
    }

    /* JADX WARN: Type inference failed for: r1v2, types: [boolean, int] */
    /* JADX WARNING: Unknown variable types count: 1 */
    private boolean onSetUsbDebugModePolicy(Bundle policyData) {
        UserManager userManager = UserManager.get(this.mContext);
        if (userManager == null) {
            HwLog.e(TAG, "UserManager is null!");
            return false;
        }
        boolean hasUsbDebugRestriction = userManager.hasUserRestriction("no_debugging_features");
        if (HwDeviceManager.disallowOp(11) || hasUsbDebugRestriction) {
            HwLog.i(TAG, "adb is disabled");
            return false;
        }
        ?? r1 = policyData.getBoolean("value", false);
        if (!Settings.Global.putInt(this.mContext.getContentResolver(), ALLOW_CHARGING_ADB, r1 == true ? 1 : 0) || !Settings.Global.putInt(this.mContext.getContentResolver(), "adb_enabled", r1)) {
            return false;
        }
        return true;
    }

    public boolean onGetPolicy(ComponentName who, String policyName, Bundle policyData) {
        Log.i(TAG, "onGetPolicy: " + policyName);
        boolean isTurnedOn = false;
        if (!checkCallingPermission(who, policyName) || policyData == null) {
            return false;
        }
        long identityToken = Binder.clearCallingIdentity();
        char c = 65535;
        try {
            if (policyName.hashCode() == 1576234773 && policyName.equals(POLICY_TURN_ON_EYE_COMFORT)) {
                c = 0;
            }
            if (c == 0) {
                if (Settings.System.getInt(this.mContext.getContentResolver(), DB_EYES_PROTECTION_MODE, 0) != 0) {
                    isTurnedOn = true;
                }
                policyData.putBoolean("value", isTurnedOn);
            }
            return true;
        } finally {
            Binder.restoreCallingIdentity(identityToken);
        }
    }

    public boolean onRemovePolicy(ComponentName who, String policyName, Bundle policyData, boolean isEffective) {
        HwLog.i(TAG, "onRemovePolicy");
        return true;
    }

    public boolean onActiveAdminRemoved(ComponentName who, ArrayList<PolicyStruct.PolicyItem> removedPolicies) {
        HwLog.i(TAG, "onActiveAdminRemoved");
        Iterator<PolicyStruct.PolicyItem> it = removedPolicies.iterator();
        while (it.hasNext()) {
            PolicyStruct.PolicyItem removedPolicy = it.next();
            if (removedPolicy != null) {
                String policyName = removedPolicy.getPolicyName();
                if ("default-voice-assistant".equals(policyName)) {
                    sendVoiceAssistantChangeBroadcast();
                }
                unRegisterEyesCareKitListener(who, policyName);
            }
        }
        return true;
    }

    private void unRegisterEyesCareKitListener(ComponentName who, String policyName) {
        if (who != null && !TextUtils.isEmpty(policyName)) {
            char c = 65535;
            int hashCode = policyName.hashCode();
            if (hashCode != -1082045907) {
                if (hashCode != -637888534) {
                    if (hashCode == -19490507 && policyName.equals("policy_enable_flip_eyes_protect")) {
                        c = 1;
                    }
                } else if (policyName.equals("policy_enable_light_eyes_protect")) {
                    c = 2;
                }
            } else if (policyName.equals("policy_enable_distance_eyes_protect")) {
                c = 0;
            }
            if (c == 0 || c == 1 || c == 2) {
                protectEyes(who, policyName, false);
            }
        }
    }

    public void onSetPolicyCompleted(ComponentName who, String policyName, boolean changed) {
        if ("default-voice-assistant".equals(policyName) && changed) {
            sendVoiceAssistantChangeBroadcast();
        }
    }

    private void sendVoiceAssistantChangeBroadcast() {
        this.mContext.sendBroadcastAsUser(new Intent(UPDATE_NOTIFICATION_VOICE_ASSISTANT), UserHandle.getUserHandleForUid(Binder.getCallingUid()), "com.huawei.permission.sec.MDM_VOICEASSISTANT");
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x0058  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0074  */
    private boolean protectEyes(ComponentName who, String policyName, boolean isEnabled) {
        char c;
        HwLog.i(TAG, "protectEyes start eyesProtectType = " + policyName + " isEnabled = " + isEnabled);
        String policyEvent = SettingsMDMPlugin.EMPTY_STRING;
        int hashCode = policyName.hashCode();
        if (hashCode != -1082045907) {
            if (hashCode != -637888534) {
                if (hashCode == -19490507 && policyName.equals("policy_enable_flip_eyes_protect")) {
                    c = 1;
                    if (c == 0) {
                        if (c == 1) {
                            policyEvent = isEnabled ? EYES_INTERFACE_REGISTER_FILP : EYES_INTERFACE_UNREGISTER_FILP;
                        } else if (c != 2) {
                            HwLog.i(TAG, "protectEyes error eyes policy name");
                        } else {
                            policyEvent = isEnabled ? EYES_INTERFACE_REGISTER_LIGHT : EYES_INTERFACE_UNREGISTER_LIGHT;
                        }
                    } else if (!isSupportDistanceEyeProtect()) {
                        HwLog.i(TAG, "this device is not support distance protect eye");
                        return false;
                    } else if (setEyeCareSoIrMode(isEnabled)) {
                        policyEvent = isEnabled ? EYES_INTERFACE_REGISTER_DISTANCE : EYES_INTERFACE_UNREGISTER_DISTANCE;
                    }
                    return invokeEyesProtect(policyEvent, who);
                }
            } else if (policyName.equals("policy_enable_light_eyes_protect")) {
                c = 2;
                if (c == 0) {
                }
                return invokeEyesProtect(policyEvent, who);
            }
        } else if (policyName.equals("policy_enable_distance_eyes_protect")) {
            c = 0;
            if (c == 0) {
            }
            return invokeEyesProtect(policyEvent, who);
        }
        c = 65535;
        if (c == 0) {
        }
        return invokeEyesProtect(policyEvent, who);
    }

    private boolean setEyeCareSoIrMode(boolean isEnabled) {
        if (isEnabled) {
            return HallManager.setPsExternalIrMode(1) != -1;
        }
        int code = HallManager.setPsExternalIrMode(0);
        HwLog.i(TAG, "unRegitster setPsExternalIrMode code = " + code);
        return true;
    }

    private boolean isSupportDistanceEyeProtect() {
        SensorManager sensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        if (sensorManager == null || sensorManager.getDefaultSensor(READ_LENGTH) == null) {
            return false;
        }
        return initEyeProtectSo();
    }

    private Context getAdminContext(String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            return null;
        }
        try {
            return this.mContext.createPackageContext(packageName, 2);
        } catch (PackageManager.NameNotFoundException e) {
            HwLog.e(TAG, "get admin context NameNotFoundException");
            return null;
        }
    }

    private boolean invokeEyesProtect(String methodName, ComponentName who) {
        Context context;
        if (TextUtils.isEmpty(methodName) || (context = getAdminContext(who.getPackageName())) == null) {
            return false;
        }
        try {
            if (sClassEyesProtect != null && sObjectEyesProtect != null) {
                return isEyeCareStateSetSuccess(sClassEyesProtect.getMethod(methodName, Context.class).invoke(sObjectEyesProtect, context));
            }
            HwLog.e(TAG, "invokeEyesProtect sClassEyesProtect is error");
            return false;
        } catch (NoSuchMethodException e) {
            HwLog.e(TAG, "invokeEyesProtect NoSuchMethodException");
        } catch (IllegalAccessException e2) {
            HwLog.e(TAG, "invokeEyesProtect IllegalAccessException");
        } catch (InvocationTargetException e3) {
            HwLog.e(TAG, "invokeEyesProtect InvocationTargetException");
        }
    }

    private boolean isEyeCareStateSetSuccess(Object object) {
        if (object == null || !(object instanceof Bundle)) {
            return false;
        }
        int code = ((Bundle) object).getInt("code");
        HwLog.i(TAG, "invokeEyesProtect code = " + code);
        if (code == 0 || code == EYE_CARE_RESULT_REGESITER_AGAIN || code == EYE_CARE_RESULT_UNREGESITER) {
            return true;
        }
        HwLog.e(TAG, "invokeEyesProtect failed");
        return false;
    }

    private DexClassLoader getEyesProtectDexClassLoader() {
        if (sDexClassLoader == null) {
            sDexClassLoader = new DexClassLoader(EYES_PATCH, null, null, Context.class.getClassLoader());
        }
        return sDexClassLoader;
    }

    private boolean initialization(Context context) {
        if (context == null) {
            HwLog.e(TAG, "cannot init eyecareket jar, applicatin context is null");
            return false;
        }
        try {
            sClassEyesProtect = getEyesProtectDexClassLoader().loadClass("com.huawei.hweyecarekit.EyesProtectionAPI");
            sMethodEyesProtect = sClassEyesProtect.getMethod("getInstance", new Class[0]);
            sObjectEyesProtect = sMethodEyesProtect.invoke(sClassEyesProtect, new Object[0]);
            sClassEyesProtect.getMethod("init", Context.class).invoke(sObjectEyesProtect, context);
            return true;
        } catch (ClassNotFoundException e) {
            HwLog.e(TAG, "init eyes protect ClassNotFoundException");
            return false;
        } catch (NoSuchMethodException e2) {
            HwLog.e(TAG, "init eyes protect NoSuchMethodException");
            return false;
        } catch (IllegalAccessException e3) {
            HwLog.e(TAG, "init eyes protect IllegalAccessException");
            return false;
        } catch (InvocationTargetException e4) {
            HwLog.e(TAG, "init eyes protect InvocationTargetException");
            return false;
        }
    }

    private boolean initEyeProtectSo() {
        try {
            System.loadLibrary("eyescarekit_jni");
            return true;
        } catch (UnsatisfiedLinkError e) {
            HwLog.e(TAG, "init eyes protect so UnsatisfiedLinkError");
            return false;
        } catch (SecurityException e2) {
            HwLog.e(TAG, "init eyes protect so SecurityException");
            return false;
        }
    }
}
