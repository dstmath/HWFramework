package com.android.server.devicepolicy;

import android.content.ComponentName;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;
import com.android.server.location.HwGpsPowerTracker;
import com.android.server.mtm.condition.ConditionMatchor;
import com.android.server.pm.HwPackageManagerService;
import com.android.server.rms.iaware.memory.utils.EventTracker;
import huawei.com.android.server.policy.HwGlobalActionsData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionProcessor {
    protected static final boolean HWDBG = false;
    protected static final boolean HWFLOW = false;
    private static final String TAG = "TransactionProcessor";
    private IHwDevicePolicyManager mService;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.devicepolicy.TransactionProcessor.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.devicepolicy.TransactionProcessor.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.devicepolicy.TransactionProcessor.<clinit>():void");
    }

    TransactionProcessor(IHwDevicePolicyManager service) {
        this.mService = service;
    }

    boolean processTransaction(int code, Parcel data, Parcel reply) {
        ComponentName who;
        String packageName;
        int userHandle;
        Map<String, String> apnInfo;
        String apnId;
        switch (code) {
            case EventTracker.TRACK_TYPE_END /*1004*/:
            case HwPackageManagerService.transaction_sendLimitedPackageBroadcast /*1006*/:
            case HwPackageManagerService.TRANSACTION_CODE_CHECK_GMS_IS_UNINSTALLED /*1008*/:
            case ConditionMatchor.PROCESSNAME /*1010*/:
            case ConditionMatchor.PACKAGENAMECONTAINS /*1012*/:
            case 1014:
            case 1016:
            case 1018:
            case 1020:
            case 1022:
            case HwGlobalActionsData.FLAG_SILENTMODE_NORMAL /*1024*/:
            case 1026:
            case 1028:
            case 1030:
            case 1032:
            case 1034:
            case 1036:
            case 1038:
            case 1504:
            case 5011:
                data.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
                who = null;
                if (data.readInt() != 0) {
                    who = ComponentName.readFromParcel(data);
                }
                try {
                    setFunctionDisabled(code, who, data.readInt() == 1 ? true : HWFLOW, data.readInt());
                    reply.writeNoException();
                } catch (Exception e) {
                    Log.e(TAG, "setFunctionDisabled exception is " + e);
                    reply.writeException(e);
                }
                return true;
            case EventTracker.TRACK_TYPE_STOP /*1005*/:
            case HwPackageManagerService.TRANSACTION_CODE_GET_PREINSTALLED_APK_LIST /*1007*/:
            case HwPackageManagerService.TRANSACTION_CODE_DELTE_GMS_FROM_UNINSTALLED_DELAPP /*1009*/:
            case ConditionMatchor.SCREENOFF /*1011*/:
            case ConditionMatchor.PROCESSNAMECONTAINS /*1013*/:
            case 1015:
            case 1017:
            case 1019:
            case 1021:
            case 1023:
            case 1025:
            case 1027:
            case 1029:
            case 1031:
            case 1033:
            case 1035:
            case 1037:
            case 1039:
            case 1503:
            case 1505:
            case 2506:
            case 4001:
            case 4002:
            case 4003:
            case 4009:
            case 4011:
            case 4012:
            case 4013:
            case 4014:
            case 4015:
            case 4016:
            case 4017:
            case 4018:
            case 5012:
                data.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
                who = null;
                if (data.readInt() != 0) {
                    who = ComponentName.readFromParcel(data);
                }
                boolean isDisabled = isFunctionDisabled(code, who, data.readInt());
                reply.writeNoException();
                reply.writeInt(isDisabled ? 1 : 0);
                return true;
            case 1501:
            case 1502:
            case 1507:
            case 2001:
            case 2504:
                data.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
                who = null;
                if (data.readInt() != 0) {
                    who = ComponentName.readFromParcel(data);
                }
                try {
                    execCommand(code, who, data.readInt());
                    reply.writeNoException();
                } catch (Exception e2) {
                    Log.e(TAG, "execCommand exception is " + e2);
                    reply.writeException(e2);
                }
                return true;
            case 1506:
                data.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
                who = null;
                if (data.readInt() != 0) {
                    who = ComponentName.readFromParcel(data);
                }
                packageName = data.readString();
                String className = data.readString();
                userHandle = data.readInt();
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_setDefaultLauncher packageName: " + packageName + ", className: " + className + ", user: " + userHandle);
                }
                this.mService.setDefaultLauncher(who, packageName, className, userHandle);
                reply.writeNoException();
                return true;
            case 1508:
            case 2505:
            case 2508:
            case 2509:
            case 2511:
            case 2512:
            case 2514:
            case 2515:
            case HwGpsPowerTracker.EVENT_REMOVE_PACKAGE_LOCATION /*3001*/:
            case 3002:
            case 3004:
            case 3005:
            case 5007:
            case 5008:
                data.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
                who = null;
                if (data.readInt() != 0) {
                    who = ComponentName.readFromParcel(data);
                }
                List<String> listParam = new ArrayList();
                data.readStringList(listParam);
                try {
                    execCommand(code, who, (List) listParam, data.readInt());
                    reply.writeNoException();
                } catch (Exception e22) {
                    Log.e(TAG, "execCommand exception is " + e22);
                    reply.writeException(e22);
                }
                return true;
            case 1509:
                data.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
                who = null;
                if (data.readInt() != 0) {
                    who = ComponentName.readFromParcel(data);
                }
                userHandle = data.readInt();
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_captureScreen user : " + userHandle);
                }
                Bitmap bitmapScreen = this.mService.captureScreen(who, userHandle);
                reply.writeNoException();
                if (bitmapScreen != null) {
                    reply.writeInt(1);
                    bitmapScreen.writeToParcel(reply, 0);
                } else {
                    reply.writeInt(0);
                }
                return true;
            case 1510:
            case 2501:
            case 2503:
            case 3007:
            case 5002:
            case 5006:
                data.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
                who = null;
                if (data.readInt() != 0) {
                    who = ComponentName.readFromParcel(data);
                }
                try {
                    execCommand(code, who, data.readString(), data.readInt());
                    reply.writeNoException();
                } catch (Exception e222) {
                    Log.e(TAG, "execCommand exception is " + e222);
                    reply.writeException(e222);
                }
                return true;
            case 2502:
                data.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
                who = null;
                if (data.readInt() != 0) {
                    who = ComponentName.readFromParcel(data);
                }
                packageName = data.readString();
                boolean keepData = data.readInt() == 1 ? true : HWFLOW;
                userHandle = data.readInt();
                try {
                    if (HWFLOW) {
                        Log.i(TAG, "HwDPMS received transaction_uninstallPackage packageName: " + packageName + ", keepData: " + keepData + ", user: " + userHandle);
                    }
                    this.mService.uninstallPackage(who, packageName, keepData, userHandle);
                    reply.writeNoException();
                } catch (Exception e2222) {
                    Log.e(TAG, "execCommand exception is " + e2222);
                    reply.writeException(e2222);
                }
                return true;
            case 2507:
            case 2510:
            case 2513:
            case 2516:
            case 3003:
            case 3006:
            case 4004:
            case 4005:
            case 4006:
            case 4007:
            case 4008:
            case 4010:
            case 4019:
            case 5009:
                data.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
                who = null;
                if (data.readInt() != 0) {
                    who = ComponentName.readFromParcel(data);
                }
                try {
                    List<String> packagelist = getListCommand(code, who, data.readInt());
                    reply.writeNoException();
                    reply.writeStringList(packagelist);
                } catch (Exception e22222) {
                    Log.e(TAG, "getListCommand exception is " + e22222);
                    reply.writeException(e22222);
                }
                return true;
            case 3501:
                data.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
                who = null;
                Bundle paraex = new Bundle();
                if (data.readInt() != 0) {
                    who = ComponentName.readFromParcel(data);
                }
                paraex.readFromParcel(data);
                userHandle = data.readInt();
                try {
                    if (HWFLOW) {
                        Log.i(TAG, "HwDPMS received ConstantValue.transaction_configExchangeMail, user: " + userHandle);
                    }
                    this.mService.configExchangeMailProvider(who, paraex, userHandle);
                    reply.writeNoException();
                } catch (Exception e222222) {
                    Log.e(TAG, "configExchangeMailProvider exception is " + e222222);
                    reply.writeException(e222222);
                }
                return true;
            case 3502:
                data.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
                who = null;
                if (data.readInt() != 0) {
                    who = ComponentName.readFromParcel(data);
                }
                String domain = data.readString();
                userHandle = data.readInt();
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_configExchangeMail domain: " + domain + ", user: " + userHandle);
                }
                Bundle para = this.mService.getMailProviderForDomain(who, domain, userHandle);
                reply.writeNoException();
                if (para != null) {
                    reply.writeInt(1);
                    para.writeToParcel(reply, 0);
                } else {
                    reply.writeInt(0);
                }
                return true;
            case 5001:
                data.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
                who = null;
                if (data.readInt() != 0) {
                    who = ComponentName.readFromParcel(data);
                }
                apnInfo = new HashMap();
                data.readMap(apnInfo, null);
                userHandle = data.readInt();
                try {
                    if (HWFLOW) {
                        Log.i(TAG, "HwDPMS received ConstantValue.transaction_addApn, user: " + userHandle);
                    }
                    this.mService.addApn(who, apnInfo, userHandle);
                    reply.writeNoException();
                } catch (Exception e2222222) {
                    Log.e(TAG, "addApn exception is " + e2222222);
                    reply.writeException(e2222222);
                }
                return true;
            case 5003:
                data.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
                who = null;
                if (data.readInt() != 0) {
                    who = ComponentName.readFromParcel(data);
                }
                apnInfo = new HashMap();
                data.readMap(apnInfo, null);
                apnId = data.readString();
                userHandle = data.readInt();
                try {
                    if (HWFLOW) {
                        Log.i(TAG, "HwDPMS received ConstantValue.transaction_updateApn, user: " + userHandle);
                    }
                    this.mService.updateApn(who, apnInfo, apnId, userHandle);
                    reply.writeNoException();
                } catch (Exception e22222222) {
                    Log.e(TAG, "addApn exception is " + e22222222);
                    reply.writeException(e22222222);
                }
                return true;
            case 5004:
                data.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
                who = null;
                if (data.readInt() != 0) {
                    who = ComponentName.readFromParcel(data);
                }
                apnId = data.readString();
                userHandle = data.readInt();
                try {
                    if (HWFLOW) {
                        Log.i(TAG, "HwDPMS received ConstantValue.transaction_updateApn, user: " + userHandle);
                    }
                    apnInfo = this.mService.getApnInfo(who, apnId, userHandle);
                    reply.writeNoException();
                    reply.writeMap(apnInfo);
                } catch (Exception e222222222) {
                    Log.e(TAG, "addApn exception is " + e222222222);
                    reply.writeException(e222222222);
                }
                return true;
            case 5005:
                data.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
                who = null;
                if (data.readInt() != 0) {
                    who = ComponentName.readFromParcel(data);
                }
                apnInfo = new HashMap();
                data.readMap(apnInfo, null);
                userHandle = data.readInt();
                try {
                    if (HWFLOW) {
                        Log.i(TAG, "HwDPMS received ConstantValue.transaction_updateApn, user: " + userHandle);
                    }
                    List<String> ids = this.mService.queryApn(who, apnInfo, userHandle);
                    reply.writeNoException();
                    reply.writeStringList(ids);
                } catch (Exception e2222222222) {
                    Log.e(TAG, "addApn exception is " + e2222222222);
                    reply.writeException(e2222222222);
                }
                return true;
            case 5010:
                data.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
                int encryptionStatus = getSDCardEncryptionStatus(code);
                reply.writeNoException();
                reply.writeInt(encryptionStatus);
                return true;
            default:
                return HWFLOW;
        }
    }

    void setFunctionDisabled(int code, ComponentName who, boolean disabled, int userHandle) {
        switch (code) {
            case EventTracker.TRACK_TYPE_END /*1004*/:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_setWifiDisabled, disabled: " + disabled + ", user: " + userHandle);
                }
                this.mService.setWifiDisabled(who, disabled, userHandle);
            case HwPackageManagerService.transaction_sendLimitedPackageBroadcast /*1006*/:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_setWifiApDisabled, disabled: " + disabled + ", user: " + userHandle);
                }
                this.mService.setWifiApDisabled(who, disabled, userHandle);
            case HwPackageManagerService.TRANSACTION_CODE_CHECK_GMS_IS_UNINSTALLED /*1008*/:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_setBootLoaderDisabled, disabled: " + disabled + ", user: " + userHandle);
                }
                this.mService.setBootLoaderDisabled(who, disabled, userHandle);
            case ConditionMatchor.PROCESSNAME /*1010*/:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_setUSBDataDisabled, disabled: " + disabled + ", user: " + userHandle);
                }
                this.mService.setUSBDataDisabled(who, disabled, userHandle);
            case ConditionMatchor.PACKAGENAMECONTAINS /*1012*/:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_setExternalStorageDisabled, disabled: " + disabled + ", user: " + userHandle);
                }
                this.mService.setExternalStorageDisabled(who, disabled, userHandle);
            case 1014:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_setNFCDisabled, disabled: " + disabled + ", user: " + userHandle);
                }
                this.mService.setNFCDisabled(who, disabled, userHandle);
            case 1016:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_setDataConnectivityDisabled, disabled: " + disabled + ", user: " + userHandle);
                }
                this.mService.setDataConnectivityDisabled(who, disabled, userHandle);
            case 1018:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_setVoiceDisabled, disabled: " + disabled + ", user: " + userHandle);
                }
                this.mService.setVoiceDisabled(who, disabled, userHandle);
            case 1020:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_setSMSDisabled, disabled: " + disabled + ", user: " + userHandle);
                }
                this.mService.setSMSDisabled(who, disabled, userHandle);
            case 1022:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_setStatusBarExpandPanelDisabled, disabled: " + disabled + ", user: " + userHandle);
                }
                this.mService.setStatusBarExpandPanelDisabled(who, disabled, userHandle);
            case HwGlobalActionsData.FLAG_SILENTMODE_NORMAL /*1024*/:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_setBluetoothDisabled, disabled: " + disabled + ", user: " + userHandle);
                }
                this.mService.setBluetoothDisabled(who, disabled, userHandle);
            case 1026:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_setGPSDisabled, disabled: " + disabled + ", user: " + userHandle);
                }
                this.mService.setGPSDisabled(who, disabled, userHandle);
            case 1028:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_setAdbDisabled, disabled: " + disabled + ", user: " + userHandle);
                }
                this.mService.setAdbDisabled(who, disabled, userHandle);
            case 1030:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_setUSBOtgDisabled, disabled: " + disabled + ", user: " + userHandle);
                }
                this.mService.setUSBOtgDisabled(who, disabled, userHandle);
            case 1032:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_setSafeModeDisabled, disabled: " + disabled + ", user: " + userHandle);
                }
                this.mService.setSafeModeDisabled(who, disabled, userHandle);
            case 1034:
                this.mService.setTaskButtonDisabled(who, disabled, userHandle);
            case 1036:
                this.mService.setHomeButtonDisabled(who, disabled, userHandle);
            case 1038:
                this.mService.setBackButtonDisabled(who, disabled, userHandle);
            case 1504:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_turnOnGPS, disabled: " + disabled + ", user: " + userHandle);
                }
                this.mService.turnOnGPS(who, disabled, userHandle);
            case 5011:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_setSDcardDecryptionDisabled, disabled: " + disabled + ", user: " + userHandle);
                }
                this.mService.setSDCardDecryptionDisabled(who, disabled, userHandle);
            default:
        }
    }

    boolean isFunctionDisabled(int code, ComponentName who, int userHandle) {
        boolean bDisabled;
        if (code == 5012) {
            bDisabled = this.mService.isSDCardDecryptionDisabled(who, userHandle);
            if (HWFLOW) {
                Log.i(TAG, "HwDPMS received transaction_isSDcardDecryptionDisabled, the ret: " + bDisabled);
            }
            return bDisabled;
        } else if (userHandle != 0) {
            return HWFLOW;
        } else {
            switch (code) {
                case EventTracker.TRACK_TYPE_STOP /*1005*/:
                    bDisabled = this.mService.isWifiDisabled(who, userHandle);
                    if (HWFLOW) {
                        Log.i(TAG, "HwDPMS received transaction_isWifiDisabled, the ret: " + bDisabled);
                    }
                    return bDisabled;
                case HwPackageManagerService.TRANSACTION_CODE_GET_PREINSTALLED_APK_LIST /*1007*/:
                    bDisabled = this.mService.isWifiApDisabled(who, userHandle);
                    if (HWFLOW) {
                        Log.i(TAG, "HwDPMS received transaction_isWifiApDisabled, the ret: " + bDisabled);
                    }
                    return bDisabled;
                case HwPackageManagerService.TRANSACTION_CODE_DELTE_GMS_FROM_UNINSTALLED_DELAPP /*1009*/:
                    bDisabled = this.mService.isBootLoaderDisabled(who, userHandle);
                    if (HWFLOW) {
                        Log.i(TAG, "HwDPMS received transaction_isBootLoaderDisabled, the ret: " + bDisabled);
                    }
                    return bDisabled;
                case ConditionMatchor.SCREENOFF /*1011*/:
                    bDisabled = this.mService.isUSBDataDisabled(who, userHandle);
                    if (HWFLOW) {
                        Log.i(TAG, "HwDPMS received transaction_isUSBDataDisabled, the ret: " + bDisabled);
                    }
                    return bDisabled;
                case ConditionMatchor.PROCESSNAMECONTAINS /*1013*/:
                    bDisabled = this.mService.isExternalStorageDisabled(who, userHandle);
                    if (HWFLOW) {
                        Log.i(TAG, "HwDPMS received transaction_isExternalStorageDisabled, the ret: " + bDisabled);
                    }
                    return bDisabled;
                case 1015:
                    bDisabled = this.mService.isNFCDisabled(who, userHandle);
                    if (HWFLOW) {
                        Log.i(TAG, "HwDPMS received transaction_isNFCDisabled, the ret: " + bDisabled);
                    }
                    return bDisabled;
                case 1017:
                    bDisabled = this.mService.isDataConnectivityDisabled(who, userHandle);
                    if (HWFLOW) {
                        Log.i(TAG, "HwDPMS received transaction_isDataConnectivityDisabled, the ret: " + bDisabled);
                    }
                    return bDisabled;
                case 1019:
                    bDisabled = this.mService.isVoiceDisabled(who, userHandle);
                    if (HWFLOW) {
                        Log.i(TAG, "HwDPMS received transaction_isVoiceDisabled, the ret: " + bDisabled);
                    }
                    return bDisabled;
                case 1021:
                    bDisabled = this.mService.isSMSDisabled(who, userHandle);
                    if (HWFLOW) {
                        Log.i(TAG, "HwDPMS received transaction_isSMSDisabled, the ret: " + bDisabled);
                    }
                    return bDisabled;
                case 1023:
                    bDisabled = this.mService.isStatusBarExpandPanelDisabled(who, userHandle);
                    if (HWFLOW) {
                        Log.i(TAG, "HwDPMS received transaction_isStatusBarExpandPanelDisabled, the ret: " + bDisabled);
                    }
                    return bDisabled;
                case 1025:
                    bDisabled = this.mService.isBluetoothDisabled(who, userHandle);
                    if (HWFLOW) {
                        Log.i(TAG, "HwDPMS received transaction_isBluetoothDisabled, the ret: " + bDisabled);
                    }
                    return bDisabled;
                case 1027:
                    bDisabled = this.mService.isGPSDisabled(who, userHandle);
                    if (HWFLOW) {
                        Log.i(TAG, "HwDPMS received transaction_isGPSDisabled, the ret: " + bDisabled);
                    }
                    return bDisabled;
                case 1029:
                    bDisabled = this.mService.isAdbDisabled(who, userHandle);
                    if (HWFLOW) {
                        Log.i(TAG, "HwDPMS received transaction_isAdbDisabled, the ret: " + bDisabled);
                    }
                    return bDisabled;
                case 1031:
                    bDisabled = this.mService.isUSBOtgDisabled(who, userHandle);
                    if (HWFLOW) {
                        Log.i(TAG, "HwDPMS received transaction_isUSBOtgDisabled, the ret: " + bDisabled);
                    }
                    return bDisabled;
                case 1033:
                    bDisabled = this.mService.isSafeModeDisabled(who, userHandle);
                    if (HWFLOW) {
                        Log.i(TAG, "HwDPMS received transaction_isSafeModeDisabled, the ret: " + bDisabled);
                    }
                    return bDisabled;
                case 1035:
                    return this.mService.isTaskButtonDisabled(who, userHandle);
                case 1037:
                    return this.mService.isHomeButtonDisabled(who, userHandle);
                case 1039:
                    return this.mService.isBackButtonDisabled(who, userHandle);
                case 1503:
                    bDisabled = this.mService.isRooted(who, userHandle);
                    if (HWFLOW) {
                        Log.i(TAG, "HwDPMS received transaction_isRooted, the ret: " + bDisabled);
                    }
                    return bDisabled;
                case 1505:
                    bDisabled = this.mService.isGPSTurnOn(who, userHandle);
                    if (HWFLOW) {
                        Log.i(TAG, "HwDPMS received transaction_isGPSTurnOn, the ret: " + bDisabled);
                    }
                    return bDisabled;
                case 2506:
                    bDisabled = this.mService.isInstallSourceDisabled(who, userHandle);
                    if (HWFLOW) {
                        Log.i(TAG, "HwDPMS received transaction_isStatusBarExpandPanelDisabled, the ret: " + bDisabled);
                    }
                    return bDisabled;
                case 4001:
                case 4002:
                case 4003:
                case 4009:
                case 4012:
                case 4013:
                case 4014:
                case 4015:
                case 4016:
                case 4017:
                case 4018:
                    bDisabled = this.mService.getHwAdminCachedValue(code);
                    if (HWFLOW) {
                        Log.i(TAG, "HwDPMS received transaction_isHwFrameworkAdminAllowed, the ret: " + bDisabled);
                    }
                    return bDisabled;
                case 4011:
                    bDisabled = this.mService.isSafeModeDisabled(null, userHandle);
                    if (HWFLOW) {
                        Log.i(TAG, "HwDPMS received transaction_isSafeModeDisabled, the ret: " + bDisabled);
                    }
                    return bDisabled;
                default:
                    return HWFLOW;
            }
        }
    }

    void execCommand(int code, ComponentName who, int userHandle) {
        switch (code) {
            case 1501:
            case 1502:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_rebootDevice, user: " + userHandle);
                }
                this.mService.shutdownOrRebootDevice(code, who, userHandle);
            case 1507:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_clearDefaultLauncher, user: " + userHandle);
                }
                this.mService.clearDefaultLauncher(who, userHandle);
            case 2001:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_hangupCalling, user: " + userHandle);
                }
                this.mService.hangupCalling(who, userHandle);
            case 2504:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_enableInstallPackage, user: " + userHandle);
                }
                this.mService.enableInstallPackage(who, userHandle);
            default:
        }
    }

    void execCommand(int code, ComponentName who, String param, int userHandle) {
        switch (code) {
            case 1510:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_setSysTime, packageName: " + param + ", user: " + userHandle);
                }
                this.mService.setSysTime(who, Long.parseLong(param), userHandle);
            case 2501:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_installPackage packagePath: " + param + ", user: " + userHandle);
                }
                this.mService.installPackage(who, param, userHandle);
            case 2503:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_clearPackageData, packageName: " + param + ", user: " + userHandle);
                }
                this.mService.clearPackageData(who, param, userHandle);
            case 3007:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_killApplicationProcess packageName: " + param + ", user: " + userHandle);
                }
                this.mService.killApplicationProcess(who, param, userHandle);
            case 5002:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_deleteApn, apnId: " + param + ", user: " + userHandle);
                }
                this.mService.deleteApn(who, param, userHandle);
            case 5006:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_setPreferApn, apnId: " + param + ", user: " + userHandle);
                }
                this.mService.setPreferApn(who, param, userHandle);
            default:
        }
    }

    void execCommand(int code, ComponentName who, List<String> param, int userHandle) {
        switch (code) {
            case 1508:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_setCustomSettingsMenu user: " + userHandle);
                }
                this.mService.setCustomSettingsMenu(who, param, userHandle);
            case 2505:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_disableInstallPackage user: " + userHandle);
                }
                this.mService.disableInstallSource(who, param, userHandle);
            case 2508:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_addInstallPackageWhiteList user: " + userHandle);
                }
                this.mService.addInstallPackageWhiteList(who, param, userHandle);
            case 2509:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_removeInstallPackageWhiteList user: " + userHandle);
                }
                this.mService.removeInstallPackageWhiteList(who, param, userHandle);
            case 2511:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_addDisallowedUninstallPackages user: " + userHandle);
                }
                this.mService.addDisallowedUninstallPackages(who, param, userHandle);
            case 2512:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_removeDisallowedUninstallPackages user: " + userHandle);
                }
                this.mService.removeDisallowedUninstallPackages(who, param, userHandle);
            case 2514:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_addDisabledDeactivateMdmPackages user: " + userHandle);
                }
                this.mService.addDisabledDeactivateMdmPackages(who, param, userHandle);
            case 2515:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_removeDisabledDeactivateMdmPackages user: " + userHandle);
                }
                this.mService.removeDisabledDeactivateMdmPackages(who, param, userHandle);
            case HwGpsPowerTracker.EVENT_REMOVE_PACKAGE_LOCATION /*3001*/:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_addPersistentApp user: " + userHandle);
                }
                this.mService.addPersistentApp(who, param, userHandle);
            case 3002:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_removePersistentApp user: " + userHandle);
                }
                this.mService.removePersistentApp(who, param, userHandle);
            case 3004:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_addDisallowedRunningApp user: " + userHandle);
                }
                this.mService.addDisallowedRunningApp(who, param, userHandle);
            case 3005:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_removeDisallowedRunningApp user: " + userHandle);
                }
                this.mService.removeDisallowedRunningApp(who, param, userHandle);
            case 5007:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_addNetworkAccessWhitelist user: " + userHandle);
                }
                this.mService.addNetworkAccessWhitelist(who, param, userHandle);
            case 5008:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_removeNetworkAccessWhitelist user: " + userHandle);
                }
                this.mService.removeNetworkAccessWhitelist(who, param, userHandle);
            default:
        }
    }

    List<String> getListCommand(int code, ComponentName who, int userHandle) {
        if (userHandle != 0) {
            return null;
        }
        switch (code) {
            case 2507:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_getInstallPackageSourceWhiteList user: " + userHandle);
                }
                return this.mService.getInstallPackageSourceWhiteList(who, userHandle);
            case 2510:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_getInstallPackageWhiteList user: " + userHandle);
                }
                return this.mService.getInstallPackageWhiteList(who, userHandle);
            case 2513:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_getDisallowedUninstallPackageList user: " + userHandle);
                }
                return this.mService.getDisallowedUninstallPackageList(who, userHandle);
            case 2516:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_getDisabledDeactivateMdmPackageList user: " + userHandle);
                }
                return this.mService.getDisabledDeactivateMdmPackageList(who, userHandle);
            case 3003:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_getPersistentApp user: " + userHandle);
                }
                return this.mService.getPersistentApp(who, userHandle);
            case 3006:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_getDisallowedRunningApp user: " + userHandle);
                }
                return this.mService.getDisallowedRunningApp(who, userHandle);
            case 4004:
            case 4005:
            case 4006:
            case 4007:
            case 4008:
            case 4010:
            case 4019:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_getHwFrameworkAdminList user: " + userHandle + ":" + code);
                }
                return this.mService.getHwAdminCachedList(code);
            case 5009:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_getNetworkAccessWhitelist user: " + userHandle);
                }
                return this.mService.getNetworkAccessWhitelist(who, userHandle);
            default:
                return null;
        }
    }

    int getSDCardEncryptionStatus(int code) {
        switch (code) {
            case 5010:
                return this.mService.getSDCardEncryptionStatus();
            default:
                return 0;
        }
    }
}
