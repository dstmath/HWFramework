package com.android.server.devicepolicy;

import android.content.ComponentName;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;
import com.android.server.hidata.arbitration.HwArbitrationDEFS;
import com.android.server.location.HwGpsPowerTracker;
import com.android.server.pm.HwPackageManagerService;
import com.android.server.rms.iaware.cpu.CPUFeature;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionProcessor {
    protected static final boolean HWDBG = false;
    protected static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final String TAG = "TransactionProcessor";
    private IHwDevicePolicyManager mService;

    TransactionProcessor(IHwDevicePolicyManager service) {
        this.mService = service;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v0, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v1, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v1, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v4, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v32, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v10, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v38, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v100, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v101, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v102, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r4v20, resolved type: boolean} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v105, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v107, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v108, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v112, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v113, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v115, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v116, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v118, resolved type: int} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v119, resolved type: int} */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:166:0x0435, code lost:
        r12.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
        r0 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:167:0x043f, code lost:
        if (r27.readInt() == 0) goto L_0x0445;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:168:0x0441, code lost:
        r0 = android.content.ComponentName.readFromParcel(r27);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:171:?, code lost:
        r0 = getListCommand(r11, r0, r27.readInt());
        r28.writeNoException();
        r13.writeStringList(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:172:0x0456, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:173:0x0457, code lost:
        android.util.Log.e(TAG, "getListCommand exception is " + r0);
        r13.writeException(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:190:0x04e1, code lost:
        r12.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
        r0 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:191:0x04eb, code lost:
        if (r27.readInt() == 0) goto L_0x04f1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:192:0x04ed, code lost:
        r0 = android.content.ComponentName.readFromParcel(r27);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:195:?, code lost:
        execCommand(r11, r0, r27.readString(), r27.readInt());
        r28.writeNoException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:196:0x0502, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:197:0x0503, code lost:
        android.util.Log.e(TAG, "execCommand exception is " + r0);
        r13.writeException(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:210:0x0561, code lost:
        r12.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
        r0 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:211:0x056b, code lost:
        if (r27.readInt() == 0) goto L_0x0571;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:212:0x056d, code lost:
        r0 = android.content.ComponentName.readFromParcel(r27);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:213:0x0571, code lost:
        r2 = r0;
        r3 = new java.util.ArrayList<>();
        r12.readStringList(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:215:?, code lost:
        execCommand(r11, r2, r3, r27.readInt());
        r28.writeNoException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:216:0x0587, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:217:0x0588, code lost:
        android.util.Log.e(TAG, "execCommand exception is " + r0);
        r13.writeException(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:227:0x05f1, code lost:
        r12.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
        r0 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:228:0x05fb, code lost:
        if (r27.readInt() == 0) goto L_0x0601;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:229:0x05fd, code lost:
        r0 = android.content.ComponentName.readFromParcel(r27);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:232:?, code lost:
        execCommand(r11, r0, r27.readInt());
        r28.writeNoException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:233:0x060e, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:234:0x060f, code lost:
        android.util.Log.e(TAG, "execCommand exception is " + r0);
        r13.writeException(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:236:0x0629, code lost:
        r12.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
        r0 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:237:0x0634, code lost:
        if (r27.readInt() == 0) goto L_0x063a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:238:0x0636, code lost:
        r0 = android.content.ComponentName.readFromParcel(r27);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:239:0x063a, code lost:
        r1 = isFunctionDisabled(r11, r0, r27.readInt());
        r28.writeNoException();
        r13.writeInt(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:240:0x0648, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:241:0x0649, code lost:
        r12.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
        r1 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:242:0x0653, code lost:
        if (r27.readInt() == 0) goto L_0x0659;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:243:0x0655, code lost:
        r1 = android.content.ComponentName.readFromParcel(r27);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:245:0x065d, code lost:
        if (r27.readInt() != 1) goto L_0x0661;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:246:0x065f, code lost:
        r0 = 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:249:?, code lost:
        setFunctionDisabled(r11, r1, r0, r27.readInt());
        r28.writeNoException();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:250:0x066e, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:251:0x066f, code lost:
        android.util.Log.e(TAG, "setFunctionDisabled exception is " + r0);
        r13.writeException(r0);
     */
    /* JADX WARNING: Multi-variable type inference failed */
    public boolean processTransaction(int code, Parcel data, Parcel reply) {
        int i = code;
        Parcel parcel = data;
        Parcel parcel2 = reply;
        int disabled = 0;
        switch (i) {
            case 1004:
            case HwArbitrationDEFS.MSG_WIFI_STATE_DISCONNECT /*1006*/:
            case HwArbitrationDEFS.MSG_CELL_STATE_DISABLE /*1008*/:
            case HwArbitrationDEFS.MSG_CELL_STATE_DISCONNECT /*1010*/:
            case HwArbitrationDEFS.MSG_SCREEN_IS_TURNOFF /*1012*/:
            case HwArbitrationDEFS.MSG_DATA_ROAMING_DISABLE /*1014*/:
            case HwArbitrationDEFS.MSG_STATE_NO_CONNECTION /*1016*/:
            case HwArbitrationDEFS.MSG_VPN_STATE_CHANGED /*1018*/:
            case HwArbitrationDEFS.MSG_STATE_OUT_OF_SERVICE /*1020*/:
            case 1022:
            case 1024:
            case 1026:
            case 1028:
            case HwArbitrationDEFS.MSG_NOTIFY_CURRENT_NETWORK /*1030*/:
            case 1032:
            case 1034:
            case 1036:
            case 1038:
                break;
            case 1005:
            case HwArbitrationDEFS.MSG_CELL_STATE_ENABLE /*1007*/:
            case HwArbitrationDEFS.MSG_CELL_STATE_CONNECTED /*1009*/:
            case HwPackageManagerService.TRANSACTION_CODE_GET_HDB_KEY /*1011*/:
            case HwArbitrationDEFS.MSG_DATA_ROAMING_ENABLE /*1013*/:
            case HwArbitrationDEFS.MSG_STATE_IS_ROAMING /*1015*/:
            case HwArbitrationDEFS.MSG_SCREEN_IS_ON /*1017*/:
            case HwArbitrationDEFS.MSG_STATE_IN_SERVICE /*1019*/:
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
                break;
            default:
                switch (i) {
                    case 1501:
                    case 1502:
                    case 1507:
                    case 1512:
                        break;
                    case 1503:
                    case 1505:
                        break;
                    case 1504:
                    case 1513:
                        break;
                    case 1506:
                        parcel.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
                        ComponentName who = null;
                        if (data.readInt() != 0) {
                            who = ComponentName.readFromParcel(data);
                        }
                        String packageName = data.readString();
                        String className = data.readString();
                        int userHandle = data.readInt();
                        if (HWFLOW) {
                            Log.i(TAG, "HwDPMS received transaction_setDefaultLauncher packageName: " + packageName + ", className: " + className + ", user: " + userHandle);
                        }
                        this.mService.setDefaultLauncher(who, packageName, className, userHandle);
                        reply.writeNoException();
                        return true;
                    case 1508:
                        break;
                    case 1509:
                        parcel.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
                        ComponentName who2 = null;
                        if (data.readInt() != 0) {
                            who2 = ComponentName.readFromParcel(data);
                        }
                        int userHandle2 = data.readInt();
                        if (HWFLOW) {
                            Log.i(TAG, "HwDPMS received transaction_captureScreen user : " + userHandle2);
                        }
                        Bitmap bitmapScreen = this.mService.captureScreen(who2, userHandle2);
                        reply.writeNoException();
                        if (bitmapScreen != null) {
                            parcel2.writeInt(1);
                            bitmapScreen.writeToParcel(parcel2, 0);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 1510:
                    case 1511:
                        break;
                    default:
                        switch (i) {
                            case 2501:
                            case 2503:
                                break;
                            case 2502:
                                parcel.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
                                ComponentName who3 = null;
                                if (data.readInt() != 0) {
                                    who3 = ComponentName.readFromParcel(data);
                                }
                                String packageName2 = data.readString();
                                if (data.readInt() == 1) {
                                    disabled = 1;
                                }
                                boolean keepData = disabled;
                                int userHandle3 = data.readInt();
                                try {
                                    if (HWFLOW) {
                                        Log.i(TAG, "HwDPMS received transaction_uninstallPackage packageName: " + packageName2 + ", keepData: " + keepData + ", user: " + userHandle3);
                                    }
                                    this.mService.uninstallPackage(who3, packageName2, keepData, userHandle3);
                                    reply.writeNoException();
                                } catch (Exception e) {
                                    Log.e(TAG, "execCommand exception is " + e);
                                    parcel2.writeException(e);
                                }
                                return true;
                            case 2504:
                                break;
                            case 2505:
                            case 2508:
                            case 2509:
                            case 2511:
                            case 2512:
                            case 2514:
                            case 2515:
                                break;
                            case 2506:
                                break;
                            case 2507:
                            case 2510:
                            case 2513:
                            case 2516:
                                break;
                            default:
                                switch (i) {
                                    case HwGpsPowerTracker.EVENT_REMOVE_PACKAGE_LOCATION /*3001*/:
                                    case HwArbitrationDEFS.MSG_Display_Start_Monitor_Network /*3002*/:
                                    case HwArbitrationDEFS.MSG_Display_Start_Monitor_SmartMP /*3004*/:
                                    case HwArbitrationDEFS.MSG_Display_Stop_Monitor_SmartMP /*3005*/:
                                        break;
                                    case HwArbitrationDEFS.MSG_Display_stop_Monotor_network /*3003*/:
                                    case 3006:
                                        break;
                                    case 3007:
                                        break;
                                    default:
                                        switch (i) {
                                            case 3501:
                                                parcel.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
                                                ComponentName who4 = null;
                                                Bundle paraex = new Bundle();
                                                if (data.readInt() != 0) {
                                                    who4 = ComponentName.readFromParcel(data);
                                                }
                                                ComponentName who5 = who4;
                                                paraex.readFromParcel(parcel);
                                                int userHandle4 = data.readInt();
                                                try {
                                                    if (HWFLOW) {
                                                        Log.i(TAG, "HwDPMS received ConstantValue.transaction_configExchangeMail, user: " + userHandle4);
                                                    }
                                                    this.mService.configExchangeMailProvider(who5, paraex, userHandle4);
                                                    reply.writeNoException();
                                                } catch (Exception e2) {
                                                    Log.e(TAG, "configExchangeMailProvider exception is " + e2);
                                                    parcel2.writeException(e2);
                                                }
                                                return true;
                                            case 3502:
                                                parcel.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
                                                ComponentName who6 = null;
                                                if (data.readInt() != 0) {
                                                    who6 = ComponentName.readFromParcel(data);
                                                }
                                                String domain = data.readString();
                                                int userHandle5 = data.readInt();
                                                if (HWFLOW) {
                                                    Log.i(TAG, "HwDPMS received transaction_configExchangeMail domain: " + domain + ", user: " + userHandle5);
                                                }
                                                Bundle para = this.mService.getMailProviderForDomain(who6, domain, userHandle5);
                                                reply.writeNoException();
                                                if (para != null) {
                                                    parcel2.writeInt(1);
                                                    para.writeToParcel(parcel2, 0);
                                                } else {
                                                    parcel2.writeInt(0);
                                                }
                                                return true;
                                            case 3503:
                                                break;
                                            default:
                                                switch (i) {
                                                    case HwArbitrationDEFS.MSG_SMARTNW_TOTAL_SETTING_CHANGED /*4001*/:
                                                    case HwArbitrationDEFS.MSG_SMARTNW_APP_SETTING_CHANGED /*4002*/:
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
                                                    case 4021:
                                                    case 4022:
                                                    case 4023:
                                                    case 4024:
                                                    case 4025:
                                                    case 4026:
                                                        break;
                                                    case 4004:
                                                    case 4005:
                                                    case 4006:
                                                    case 4007:
                                                    case 4008:
                                                    case 4010:
                                                    case 4019:
                                                    case 4020:
                                                    case 4027:
                                                    case 4028:
                                                        break;
                                                    default:
                                                        switch (i) {
                                                            case 5001:
                                                                parcel.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
                                                                ComponentName who7 = null;
                                                                if (data.readInt() != 0) {
                                                                    who7 = ComponentName.readFromParcel(data);
                                                                }
                                                                ComponentName who8 = who7;
                                                                Map<String, String> apnInfo = new HashMap<>();
                                                                parcel.readMap(apnInfo, null);
                                                                int userHandle6 = data.readInt();
                                                                try {
                                                                    if (HWFLOW) {
                                                                        Log.i(TAG, "HwDPMS received ConstantValue.transaction_addApn, user: " + userHandle6);
                                                                    }
                                                                    this.mService.addApn(who8, apnInfo, userHandle6);
                                                                    reply.writeNoException();
                                                                } catch (Exception e3) {
                                                                    Log.e(TAG, "addApn exception is " + e3);
                                                                    parcel2.writeException(e3);
                                                                }
                                                                return true;
                                                            case 5002:
                                                            case 5006:
                                                                break;
                                                            case 5003:
                                                                parcel.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
                                                                ComponentName who9 = null;
                                                                if (data.readInt() != 0) {
                                                                    who9 = ComponentName.readFromParcel(data);
                                                                }
                                                                ComponentName who10 = who9;
                                                                Map<String, String> apnInfo2 = new HashMap<>();
                                                                parcel.readMap(apnInfo2, null);
                                                                String apnId = data.readString();
                                                                int userHandle7 = data.readInt();
                                                                try {
                                                                    if (HWFLOW) {
                                                                        Log.i(TAG, "HwDPMS received ConstantValue.transaction_updateApn, user: " + userHandle7);
                                                                    }
                                                                    this.mService.updateApn(who10, apnInfo2, apnId, userHandle7);
                                                                    reply.writeNoException();
                                                                } catch (Exception e4) {
                                                                    Log.e(TAG, "addApn exception is " + e4);
                                                                    parcel2.writeException(e4);
                                                                }
                                                                return true;
                                                            case 5004:
                                                                parcel.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
                                                                ComponentName who11 = null;
                                                                if (data.readInt() != 0) {
                                                                    who11 = ComponentName.readFromParcel(data);
                                                                }
                                                                ComponentName who12 = who11;
                                                                String apnId2 = data.readString();
                                                                int userHandle8 = data.readInt();
                                                                try {
                                                                    if (HWFLOW) {
                                                                        Log.i(TAG, "HwDPMS received ConstantValue.transaction_updateApn, user: " + userHandle8);
                                                                    }
                                                                    Map<String, String> apnInfo3 = this.mService.getApnInfo(who12, apnId2, userHandle8);
                                                                    reply.writeNoException();
                                                                    parcel2.writeMap(apnInfo3);
                                                                } catch (Exception e5) {
                                                                    Log.e(TAG, "addApn exception is " + e5);
                                                                    parcel2.writeException(e5);
                                                                }
                                                                return true;
                                                            case 5005:
                                                                parcel.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
                                                                ComponentName who13 = null;
                                                                if (data.readInt() != 0) {
                                                                    who13 = ComponentName.readFromParcel(data);
                                                                }
                                                                ComponentName who14 = who13;
                                                                Map<String, String> apnInfo4 = new HashMap<>();
                                                                parcel.readMap(apnInfo4, null);
                                                                int userHandle9 = data.readInt();
                                                                try {
                                                                    if (HWFLOW) {
                                                                        Log.i(TAG, "HwDPMS received ConstantValue.transaction_updateApn, user: " + userHandle9);
                                                                    }
                                                                    List<String> ids = this.mService.queryApn(who14, apnInfo4, userHandle9);
                                                                    reply.writeNoException();
                                                                    parcel2.writeStringList(ids);
                                                                } catch (Exception e6) {
                                                                    Log.e(TAG, "addApn exception is " + e6);
                                                                    parcel2.writeException(e6);
                                                                }
                                                                return true;
                                                            case 5007:
                                                            case 5008:
                                                                break;
                                                            case 5009:
                                                                break;
                                                            case 5010:
                                                                parcel.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
                                                                int encryptionStatus = getSDCardEncryptionStatus(code);
                                                                reply.writeNoException();
                                                                parcel2.writeInt(encryptionStatus);
                                                                return true;
                                                            case 5011:
                                                                break;
                                                            case 5012:
                                                                break;
                                                            default:
                                                                switch (i) {
                                                                    case 5017:
                                                                        parcel.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
                                                                        ComponentName who15 = null;
                                                                        if (data.readInt() != 0) {
                                                                            who15 = ComponentName.readFromParcel(data);
                                                                        }
                                                                        try {
                                                                            boolean formatResult = formatSDCard(who15, data.readString(), data.readInt());
                                                                            reply.writeNoException();
                                                                            if (formatResult) {
                                                                                disabled = 1;
                                                                            }
                                                                            parcel2.writeInt(disabled);
                                                                        } catch (Exception e7) {
                                                                            Log.e(TAG, "formatSDCard exception is " + e7);
                                                                            parcel2.writeException(e7);
                                                                        }
                                                                        return true;
                                                                    case 5018:
                                                                        parcel.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
                                                                        ComponentName who16 = null;
                                                                        if (data.readInt() != 0) {
                                                                            who16 = ComponentName.readFromParcel(data);
                                                                        }
                                                                        String accountType = data.readString();
                                                                        if (data.readInt() == 1) {
                                                                            disabled = 1;
                                                                        }
                                                                        try {
                                                                            setAccountDisabled(who16, accountType, disabled, data.readInt());
                                                                            reply.writeNoException();
                                                                        } catch (Exception e8) {
                                                                            Log.e(TAG, "formatSDCard exception is " + e8);
                                                                            parcel2.writeException(e8);
                                                                        }
                                                                        return true;
                                                                    case 5019:
                                                                        parcel.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
                                                                        ComponentName who17 = null;
                                                                        if (data.readInt() != 0) {
                                                                            who17 = ComponentName.readFromParcel(data);
                                                                        }
                                                                        try {
                                                                            boolean result = isAccountDisabled(who17, data.readString(), data.readInt());
                                                                            reply.writeNoException();
                                                                            if (result) {
                                                                                disabled = 1;
                                                                            }
                                                                            parcel2.writeInt(disabled);
                                                                        } catch (Exception e9) {
                                                                            Log.e(TAG, "formatSDCard exception is " + e9);
                                                                            parcel2.writeException(e9);
                                                                        }
                                                                        return true;
                                                                    case 5020:
                                                                        parcel.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
                                                                        ComponentName who18 = null;
                                                                        if (data.readInt() != 0) {
                                                                            who18 = ComponentName.readFromParcel(data);
                                                                        }
                                                                        ComponentName who19 = who18;
                                                                        int type = data.readInt();
                                                                        int len = data.readInt();
                                                                        byte[] certBuffer = new byte[len];
                                                                        parcel.readByteArray(certBuffer);
                                                                        byte[] bArr = certBuffer;
                                                                        int i2 = len;
                                                                        try {
                                                                            boolean installResult = installCertificateWithType(who19, type, certBuffer, data.readString(), data.readString(), data.readInt(), data.readInt() == 1, data.readInt());
                                                                            try {
                                                                                reply.writeNoException();
                                                                                if (installResult) {
                                                                                    disabled = 1;
                                                                                }
                                                                                parcel2.writeInt(disabled);
                                                                            } catch (Exception e10) {
                                                                                e = e10;
                                                                                Log.e(TAG, "install user cert exception is " + e);
                                                                                parcel2.writeException(e);
                                                                                return true;
                                                                            }
                                                                        } catch (Exception e11) {
                                                                            e = e11;
                                                                            Log.e(TAG, "install user cert exception is " + e);
                                                                            parcel2.writeException(e);
                                                                            return true;
                                                                        }
                                                                        return true;
                                                                    case 5021:
                                                                    case 5022:
                                                                        break;
                                                                    default:
                                                                        switch (i) {
                                                                            case 7004:
                                                                                parcel.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
                                                                                ComponentName who20 = null;
                                                                                if (data.readInt() != 0) {
                                                                                    who20 = ComponentName.readFromParcel(data);
                                                                                }
                                                                                try {
                                                                                    boolean formatResult2 = setCarrierLockScreenPassword(who20, data.readString(), data.readString(), data.readInt());
                                                                                    reply.writeNoException();
                                                                                    if (formatResult2) {
                                                                                        disabled = 1;
                                                                                    }
                                                                                    parcel2.writeInt(disabled);
                                                                                } catch (Exception e12) {
                                                                                    Log.e(TAG, "set carrierlockscreenpassword exception is " + e12);
                                                                                    parcel2.writeException(e12);
                                                                                }
                                                                                return true;
                                                                            case 7005:
                                                                                parcel.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
                                                                                ComponentName who21 = null;
                                                                                if (data.readInt() != 0) {
                                                                                    who21 = ComponentName.readFromParcel(data);
                                                                                }
                                                                                try {
                                                                                    boolean formatResult3 = clearCarrierLockScreenPassword(who21, data.readString(), data.readInt());
                                                                                    reply.writeNoException();
                                                                                    if (formatResult3) {
                                                                                        disabled = 1;
                                                                                    }
                                                                                    parcel2.writeInt(disabled);
                                                                                } catch (Exception e13) {
                                                                                    Log.e(TAG, "clear carrierlockscreenpassword exception is " + e13);
                                                                                    parcel2.writeException(e13);
                                                                                }
                                                                                return true;
                                                                            default:
                                                                                switch (i) {
                                                                                    case 2001:
                                                                                    case 6001:
                                                                                        break;
                                                                                    default:
                                                                                        return false;
                                                                                }
                                                                        }
                                                                }
                                                        }
                                                }
                                        }
                                }
                        }
                }
        }
        return true;
        return true;
        return true;
        return true;
        return true;
    }

    /* access modifiers changed from: package-private */
    public void setFunctionDisabled(int code, ComponentName who, boolean disabled, int userHandle) {
        switch (code) {
            case 1004:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_setWifiDisabled, disabled: " + disabled + ", user: " + userHandle);
                }
                this.mService.setWifiDisabled(who, disabled, userHandle);
                return;
            case HwArbitrationDEFS.MSG_WIFI_STATE_DISCONNECT /*1006*/:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_setWifiApDisabled, disabled: " + disabled + ", user: " + userHandle);
                }
                this.mService.setWifiApDisabled(who, disabled, userHandle);
                return;
            case HwArbitrationDEFS.MSG_CELL_STATE_DISABLE /*1008*/:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_setBootLoaderDisabled, disabled: " + disabled + ", user: " + userHandle);
                }
                this.mService.setBootLoaderDisabled(who, disabled, userHandle);
                return;
            case HwArbitrationDEFS.MSG_CELL_STATE_DISCONNECT /*1010*/:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_setUSBDataDisabled, disabled: " + disabled + ", user: " + userHandle);
                }
                this.mService.setUSBDataDisabled(who, disabled, userHandle);
                return;
            case HwArbitrationDEFS.MSG_SCREEN_IS_TURNOFF /*1012*/:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_setExternalStorageDisabled, disabled: " + disabled + ", user: " + userHandle);
                }
                this.mService.setExternalStorageDisabled(who, disabled, userHandle);
                return;
            case HwArbitrationDEFS.MSG_DATA_ROAMING_DISABLE /*1014*/:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_setNFCDisabled, disabled: " + disabled + ", user: " + userHandle);
                }
                this.mService.setNFCDisabled(who, disabled, userHandle);
                return;
            case HwArbitrationDEFS.MSG_STATE_NO_CONNECTION /*1016*/:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_setDataConnectivityDisabled, disabled: " + disabled + ", user: " + userHandle);
                }
                this.mService.setDataConnectivityDisabled(who, disabled, userHandle);
                return;
            case HwArbitrationDEFS.MSG_VPN_STATE_CHANGED /*1018*/:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_setVoiceDisabled, disabled: " + disabled + ", user: " + userHandle);
                }
                this.mService.setVoiceDisabled(who, disabled, userHandle);
                return;
            case HwArbitrationDEFS.MSG_STATE_OUT_OF_SERVICE /*1020*/:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_setSMSDisabled, disabled: " + disabled + ", user: " + userHandle);
                }
                this.mService.setSMSDisabled(who, disabled, userHandle);
                return;
            case 1022:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_setStatusBarExpandPanelDisabled, disabled: " + disabled + ", user: " + userHandle);
                }
                this.mService.setStatusBarExpandPanelDisabled(who, disabled, userHandle);
                return;
            case 1024:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_setBluetoothDisabled, disabled: " + disabled + ", user: " + userHandle);
                }
                this.mService.setBluetoothDisabled(who, disabled, userHandle);
                return;
            case 1026:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_setGPSDisabled, disabled: " + disabled + ", user: " + userHandle);
                }
                this.mService.setGPSDisabled(who, disabled, userHandle);
                return;
            case 1028:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_setAdbDisabled, disabled: " + disabled + ", user: " + userHandle);
                }
                this.mService.setAdbDisabled(who, disabled, userHandle);
                return;
            case HwArbitrationDEFS.MSG_NOTIFY_CURRENT_NETWORK /*1030*/:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_setUSBOtgDisabled, disabled: " + disabled + ", user: " + userHandle);
                }
                this.mService.setUSBOtgDisabled(who, disabled, userHandle);
                return;
            case 1032:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_setSafeModeDisabled, disabled: " + disabled + ", user: " + userHandle);
                }
                this.mService.setSafeModeDisabled(who, disabled, userHandle);
                return;
            case 1034:
                this.mService.setTaskButtonDisabled(who, disabled, userHandle);
                return;
            case 1036:
                this.mService.setHomeButtonDisabled(who, disabled, userHandle);
                return;
            case 1038:
                this.mService.setBackButtonDisabled(who, disabled, userHandle);
                return;
            case 1504:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_turnOnGPS, disabled: " + disabled + ", user: " + userHandle);
                }
                this.mService.turnOnGPS(who, disabled, userHandle);
                return;
            case 1513:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_turnOnMobiledata, disabled: " + disabled + ", user: " + userHandle);
                }
                this.mService.turnOnMobiledata(who, disabled, userHandle);
                return;
            case 5011:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_setSDcardDecryptionDisabled, disabled: " + disabled + ", user: " + userHandle);
                }
                this.mService.setSDCardDecryptionDisabled(who, disabled, userHandle);
                return;
            default:
                return;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isFunctionDisabled(int code, ComponentName who, int userHandle) {
        if (code == 5012) {
            boolean bDisabled = this.mService.isSDCardDecryptionDisabled(who, userHandle);
            if (HWFLOW) {
                Log.i(TAG, "HwDPMS received transaction_isSDcardDecryptionDisabled, the ret: " + bDisabled);
            }
            return bDisabled;
        } else if (userHandle != 0) {
            return false;
        } else {
            switch (code) {
                case HwArbitrationDEFS.MSG_SMARTNW_TOTAL_SETTING_CHANGED /*4001*/:
                case HwArbitrationDEFS.MSG_SMARTNW_APP_SETTING_CHANGED /*4002*/:
                case 4003:
                    break;
                default:
                    switch (code) {
                        case 4011:
                            boolean bDisabled2 = this.mService.isSafeModeDisabled(null, userHandle);
                            if (HWFLOW) {
                                Log.i(TAG, "HwDPMS received transaction_isSafeModeDisabled, the ret: " + bDisabled2);
                            }
                            return bDisabled2;
                        case 4012:
                        case 4013:
                        case 4014:
                        case 4015:
                        case 4016:
                        case 4017:
                        case 4018:
                            break;
                        default:
                            switch (code) {
                                case 4021:
                                case 4022:
                                case 4023:
                                case 4024:
                                case 4025:
                                case 4026:
                                    break;
                                default:
                                    switch (code) {
                                        case 5021:
                                        case 5022:
                                            break;
                                        default:
                                            switch (code) {
                                                case 1005:
                                                    boolean bDisabled3 = this.mService.isWifiDisabled(who, userHandle);
                                                    if (HWFLOW) {
                                                        Log.i(TAG, "HwDPMS received transaction_isWifiDisabled, the ret: " + bDisabled3);
                                                    }
                                                    return bDisabled3;
                                                case HwArbitrationDEFS.MSG_CELL_STATE_ENABLE /*1007*/:
                                                    boolean bDisabled4 = this.mService.isWifiApDisabled(who, userHandle);
                                                    if (HWFLOW) {
                                                        Log.i(TAG, "HwDPMS received transaction_isWifiApDisabled, the ret: " + bDisabled4);
                                                    }
                                                    return bDisabled4;
                                                case HwArbitrationDEFS.MSG_CELL_STATE_CONNECTED /*1009*/:
                                                    boolean bDisabled5 = this.mService.isBootLoaderDisabled(who, userHandle);
                                                    if (HWFLOW) {
                                                        Log.i(TAG, "HwDPMS received transaction_isBootLoaderDisabled, the ret: " + bDisabled5);
                                                    }
                                                    return bDisabled5;
                                                case HwPackageManagerService.TRANSACTION_CODE_GET_HDB_KEY /*1011*/:
                                                    boolean bDisabled6 = this.mService.isUSBDataDisabled(who, userHandle);
                                                    if (HWFLOW) {
                                                        Log.i(TAG, "HwDPMS received transaction_isUSBDataDisabled, the ret: " + bDisabled6);
                                                    }
                                                    return bDisabled6;
                                                case HwArbitrationDEFS.MSG_DATA_ROAMING_ENABLE /*1013*/:
                                                    boolean bDisabled7 = this.mService.isExternalStorageDisabled(who, userHandle);
                                                    if (HWFLOW) {
                                                        Log.i(TAG, "HwDPMS received transaction_isExternalStorageDisabled, the ret: " + bDisabled7);
                                                    }
                                                    return bDisabled7;
                                                case HwArbitrationDEFS.MSG_STATE_IS_ROAMING /*1015*/:
                                                    boolean bDisabled8 = this.mService.isNFCDisabled(who, userHandle);
                                                    if (HWFLOW) {
                                                        Log.i(TAG, "HwDPMS received transaction_isNFCDisabled, the ret: " + bDisabled8);
                                                    }
                                                    return bDisabled8;
                                                case HwArbitrationDEFS.MSG_SCREEN_IS_ON /*1017*/:
                                                    boolean bDisabled9 = this.mService.isDataConnectivityDisabled(who, userHandle);
                                                    if (HWFLOW) {
                                                        Log.i(TAG, "HwDPMS received transaction_isDataConnectivityDisabled, the ret: " + bDisabled9);
                                                    }
                                                    return bDisabled9;
                                                case HwArbitrationDEFS.MSG_STATE_IN_SERVICE /*1019*/:
                                                    boolean bDisabled10 = this.mService.isVoiceDisabled(who, userHandle);
                                                    if (HWFLOW) {
                                                        Log.i(TAG, "HwDPMS received transaction_isVoiceDisabled, the ret: " + bDisabled10);
                                                    }
                                                    return bDisabled10;
                                                case 1021:
                                                    boolean bDisabled11 = this.mService.isSMSDisabled(who, userHandle);
                                                    if (HWFLOW) {
                                                        Log.i(TAG, "HwDPMS received transaction_isSMSDisabled, the ret: " + bDisabled11);
                                                    }
                                                    return bDisabled11;
                                                case 1023:
                                                    boolean bDisabled12 = this.mService.isStatusBarExpandPanelDisabled(who, userHandle);
                                                    if (HWFLOW) {
                                                        Log.i(TAG, "HwDPMS received transaction_isStatusBarExpandPanelDisabled, the ret: " + bDisabled12);
                                                    }
                                                    return bDisabled12;
                                                case 1025:
                                                    boolean bDisabled13 = this.mService.isBluetoothDisabled(who, userHandle);
                                                    if (HWFLOW) {
                                                        Log.i(TAG, "HwDPMS received transaction_isBluetoothDisabled, the ret: " + bDisabled13);
                                                    }
                                                    return bDisabled13;
                                                case 1027:
                                                    boolean bDisabled14 = this.mService.isGPSDisabled(who, userHandle);
                                                    if (HWFLOW) {
                                                        Log.i(TAG, "HwDPMS received transaction_isGPSDisabled, the ret: " + bDisabled14);
                                                    }
                                                    return bDisabled14;
                                                case 1029:
                                                    boolean bDisabled15 = this.mService.isAdbDisabled(who, userHandle);
                                                    if (HWFLOW) {
                                                        Log.i(TAG, "HwDPMS received transaction_isAdbDisabled, the ret: " + bDisabled15);
                                                    }
                                                    return bDisabled15;
                                                case 1031:
                                                    boolean bDisabled16 = this.mService.isUSBOtgDisabled(who, userHandle);
                                                    if (HWFLOW) {
                                                        Log.i(TAG, "HwDPMS received transaction_isUSBOtgDisabled, the ret: " + bDisabled16);
                                                    }
                                                    return bDisabled16;
                                                case 1033:
                                                    boolean bDisabled17 = this.mService.isSafeModeDisabled(who, userHandle);
                                                    if (HWFLOW) {
                                                        Log.i(TAG, "HwDPMS received transaction_isSafeModeDisabled, the ret: " + bDisabled17);
                                                    }
                                                    return bDisabled17;
                                                case 1035:
                                                    return this.mService.isTaskButtonDisabled(who, userHandle);
                                                case 1037:
                                                    return this.mService.isHomeButtonDisabled(who, userHandle);
                                                case 1039:
                                                    return this.mService.isBackButtonDisabled(who, userHandle);
                                                case 1503:
                                                    boolean bDisabled18 = this.mService.isRooted(who, userHandle);
                                                    if (HWFLOW) {
                                                        Log.i(TAG, "HwDPMS received transaction_isRooted, the ret: " + bDisabled18);
                                                    }
                                                    return bDisabled18;
                                                case 1505:
                                                    boolean bDisabled19 = this.mService.isGPSTurnOn(who, userHandle);
                                                    if (HWFLOW) {
                                                        Log.i(TAG, "HwDPMS received transaction_isGPSTurnOn, the ret: " + bDisabled19);
                                                    }
                                                    return bDisabled19;
                                                case 2506:
                                                    boolean bDisabled20 = this.mService.isInstallSourceDisabled(who, userHandle);
                                                    if (HWFLOW) {
                                                        Log.i(TAG, "HwDPMS received transaction_isStatusBarExpandPanelDisabled, the ret: " + bDisabled20);
                                                    }
                                                    return bDisabled20;
                                                case 4009:
                                                    break;
                                                default:
                                                    return false;
                                            }
                                    }
                            }
                    }
            }
            boolean bDisabled21 = this.mService.getHwAdminCachedValue(code);
            if (HWFLOW) {
                Log.i(TAG, "HwDPMS received transaction_isHwFrameworkAdminAllowed, the ret: " + bDisabled21);
            }
            return bDisabled21;
        }
    }

    /* access modifiers changed from: package-private */
    public void execCommand(int code, ComponentName who, int userHandle) {
        if (code == 1507) {
            if (HWFLOW) {
                Log.i(TAG, "HwDPMS received transaction_clearDefaultLauncher, user: " + userHandle);
            }
            this.mService.clearDefaultLauncher(who, userHandle);
        } else if (code == 1512) {
            if (HWFLOW) {
                Log.i(TAG, "HwDPMS received transaction_clearDeviceOwnerApp, user: " + userHandle);
            }
            this.mService.clearDeviceOwnerApp(userHandle);
        } else if (code == 2001) {
            if (HWFLOW) {
                Log.i(TAG, "HwDPMS received transaction_hangupCalling, user: " + userHandle);
            }
            this.mService.hangupCalling(who, userHandle);
        } else if (code == 2504) {
            if (HWFLOW) {
                Log.i(TAG, "HwDPMS received transaction_enableInstallPackage, user: " + userHandle);
            }
            this.mService.enableInstallPackage(who, userHandle);
        } else if (code == 3503) {
            if (HWFLOW) {
                Log.i(TAG, "HwDPMS received transaction_resetNetworkSetting, user: " + userHandle);
            }
            this.mService.resetNetorkSetting(who, userHandle);
        } else if (code != 6001) {
            switch (code) {
                case 1501:
                case 1502:
                    if (HWFLOW) {
                        Log.i(TAG, "HwDPMS received transaction_rebootDevice, user: " + userHandle);
                    }
                    this.mService.shutdownOrRebootDevice(code, who, userHandle);
                    return;
                default:
                    return;
            }
        } else {
            if (HWFLOW) {
                Log.i(TAG, "HwDPMS received transaction_setSilentActiveAdmin, user: " + userHandle);
            }
            this.mService.setSilentActiveAdmin(who, userHandle);
        }
    }

    /* access modifiers changed from: package-private */
    public void execCommand(int code, ComponentName who, String param, int userHandle) {
        if (code == 2501) {
            if (HWFLOW) {
                Log.i(TAG, "HwDPMS received transaction_installPackage packagePath: " + param + ", user: " + userHandle);
            }
            this.mService.installPackage(who, param, userHandle);
        } else if (code == 2503) {
            if (HWFLOW) {
                Log.i(TAG, "HwDPMS received transaction_clearPackageData, packageName: " + param + ", user: " + userHandle);
            }
            this.mService.clearPackageData(who, param, userHandle);
        } else if (code == 3007) {
            if (HWFLOW) {
                Log.i(TAG, "HwDPMS received transaction_killApplicationProcess packageName: " + param + ", user: " + userHandle);
            }
            this.mService.killApplicationProcess(who, param, userHandle);
        } else if (code == 5002) {
            if (HWFLOW) {
                Log.i(TAG, "HwDPMS received transaction_deleteApn, apnId: " + param + ", user: " + userHandle);
            }
            this.mService.deleteApn(who, param, userHandle);
        } else if (code != 5006) {
            switch (code) {
                case 1510:
                    if (HWFLOW) {
                        Log.i(TAG, "HwDPMS received transaction_setSysTime, packageName: " + param + ", user: " + userHandle);
                    }
                    this.mService.setSysTime(who, Long.parseLong(param), userHandle);
                    return;
                case 1511:
                    if (HWFLOW) {
                        Log.i(TAG, "HwDPMS received transaction_setDeviceOwnerApp, ownnerName: " + param + ", user: " + userHandle);
                    }
                    this.mService.setDeviceOwnerApp(who, param, userHandle);
                    return;
                default:
                    return;
            }
        } else {
            if (HWFLOW) {
                Log.i(TAG, "HwDPMS received transaction_setPreferApn, apnId: " + param + ", user: " + userHandle);
            }
            this.mService.setPreferApn(who, param, userHandle);
        }
    }

    /* access modifiers changed from: package-private */
    public void execCommand(int code, ComponentName who, List<String> param, int userHandle) {
        switch (code) {
            case 1508:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_setCustomSettingsMenu user: " + userHandle);
                }
                this.mService.setCustomSettingsMenu(who, param, userHandle);
                return;
            case 2505:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_disableInstallPackage user: " + userHandle);
                }
                this.mService.disableInstallSource(who, param, userHandle);
                return;
            case 2508:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_addInstallPackageWhiteList user: " + userHandle);
                }
                this.mService.addInstallPackageWhiteList(who, param, userHandle);
                return;
            case 2509:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_removeInstallPackageWhiteList user: " + userHandle);
                }
                this.mService.removeInstallPackageWhiteList(who, param, userHandle);
                return;
            case 2511:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_addDisallowedUninstallPackages user: " + userHandle);
                }
                this.mService.addDisallowedUninstallPackages(who, param, userHandle);
                return;
            case 2512:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_removeDisallowedUninstallPackages user: " + userHandle);
                }
                this.mService.removeDisallowedUninstallPackages(who, param, userHandle);
                return;
            case 2514:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_addDisabledDeactivateMdmPackages user: " + userHandle);
                }
                this.mService.addDisabledDeactivateMdmPackages(who, param, userHandle);
                return;
            case 2515:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_removeDisabledDeactivateMdmPackages user: " + userHandle);
                }
                this.mService.removeDisabledDeactivateMdmPackages(who, param, userHandle);
                return;
            case HwGpsPowerTracker.EVENT_REMOVE_PACKAGE_LOCATION /*3001*/:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_addPersistentApp user: " + userHandle);
                }
                this.mService.addPersistentApp(who, param, userHandle);
                return;
            case HwArbitrationDEFS.MSG_Display_Start_Monitor_Network /*3002*/:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_removePersistentApp user: " + userHandle);
                }
                this.mService.removePersistentApp(who, param, userHandle);
                return;
            case HwArbitrationDEFS.MSG_Display_Start_Monitor_SmartMP /*3004*/:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_addDisallowedRunningApp user: " + userHandle);
                }
                this.mService.addDisallowedRunningApp(who, param, userHandle);
                return;
            case HwArbitrationDEFS.MSG_Display_Stop_Monitor_SmartMP /*3005*/:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_removeDisallowedRunningApp user: " + userHandle);
                }
                this.mService.removeDisallowedRunningApp(who, param, userHandle);
                return;
            case 5007:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_addNetworkAccessWhitelist user: " + userHandle);
                }
                this.mService.addNetworkAccessWhitelist(who, param, userHandle);
                return;
            case 5008:
                if (HWFLOW) {
                    Log.i(TAG, "HwDPMS received transaction_removeNetworkAccessWhitelist user: " + userHandle);
                }
                this.mService.removeNetworkAccessWhitelist(who, param, userHandle);
                return;
            default:
                return;
        }
    }

    /* access modifiers changed from: package-private */
    public List<String> getListCommand(int code, ComponentName who, int userHandle) {
        if (userHandle != 0 && code != 3003) {
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
            case HwArbitrationDEFS.MSG_Display_stop_Monotor_network /*3003*/:
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
            case 4020:
            case 4027:
            case 4028:
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

    /* access modifiers changed from: package-private */
    public int getSDCardEncryptionStatus(int code) {
        if (code != 5010) {
            return 0;
        }
        return this.mService.getSDCardEncryptionStatus();
    }

    /* access modifiers changed from: package-private */
    public boolean processTransactionWithPolicyName(int code, Parcel data, Parcel reply) {
        Parcel parcel = data;
        Parcel parcel2 = reply;
        int i = 0;
        switch (code) {
            case 5013:
                parcel.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
                ComponentName whoGetPolicy = null;
                if (data.readInt() != 0) {
                    whoGetPolicy = ComponentName.readFromParcel(data);
                }
                int getPolicyUser = data.readInt();
                String getPolicyName = data.readString();
                Bundle keyWords = data.readBundle();
                try {
                    reply.writeNoException();
                    parcel2.writeBundle(getPolicy(whoGetPolicy, getPolicyName, keyWords, getPolicyUser, data.readInt()));
                } catch (Exception e) {
                    Log.e(TAG, "getPolicy exception is " + e);
                    parcel2.writeException(e);
                }
                return true;
            case 5014:
                parcel.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
                ComponentName whoSetPolicy = null;
                if (data.readInt() != 0) {
                    whoSetPolicy = ComponentName.readFromParcel(data);
                }
                try {
                    int setPolicyResult = setPolicy(whoSetPolicy, data.readString(), data.readBundle(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    parcel2.writeInt(setPolicyResult);
                } catch (Exception e2) {
                    Log.e(TAG, "setPolicy exception is " + e2);
                    parcel2.writeException(e2);
                }
                return true;
            case 5015:
                parcel.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
                ComponentName whoRemovePolicy = null;
                if (data.readInt() != 0) {
                    whoRemovePolicy = ComponentName.readFromParcel(data);
                }
                try {
                    int removePolicyResult = removePolicy(whoRemovePolicy, data.readString(), data.readBundle(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    parcel2.writeInt(removePolicyResult);
                } catch (Exception e3) {
                    Log.e(TAG, "removePolicy exception is " + e3);
                    parcel2.writeException(e3);
                }
                return true;
            case 5016:
                parcel.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
                try {
                    boolean isDisabled = hasHwPolicy(data.readInt());
                    reply.writeNoException();
                    if (isDisabled) {
                        i = 1;
                    }
                    parcel2.writeInt(i);
                } catch (Exception e4) {
                    Log.e(TAG, "hasHwPolicy exception is " + e4);
                    parcel2.writeException(e4);
                }
                return true;
            default:
                return false;
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x003f, code lost:
        if (r8.equals("policy-top-packagename") != false) goto L_0x0043;
     */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x0046  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0061  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x0082  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00ad  */
    public Bundle getPolicy(ComponentName who, String policyName, Bundle keyWords, int userHandle, int type) {
        if (type == 0) {
            return this.mService.getPolicy(who, policyName, userHandle);
        }
        char c = 2;
        if (type == 1) {
            int hashCode = policyName.hashCode();
            if (hashCode != -1078880066) {
                if (hashCode == 830361577) {
                    if (policyName.equals("config-vpn")) {
                        c = 1;
                        switch (c) {
                            case 0:
                                break;
                            case 1:
                                break;
                            case 2:
                                break;
                        }
                    }
                } else if (hashCode == 1115753445 && policyName.equals("queryBrowsingHistory")) {
                    c = 0;
                    switch (c) {
                        case 0:
                            Bundle bundle = new Bundle();
                            bundle.putStringArrayList("value", this.mService.queryBrowsingHistory(who, userHandle));
                            return bundle;
                        case 1:
                            if (HWFLOW) {
                                Log.i(TAG, "receive get config-vpn policy: " + policyName);
                            }
                            if (keyWords == null) {
                                return this.mService.getVpnList(who, null, userHandle);
                            }
                            return this.mService.getVpnProfile(who, keyWords, userHandle);
                        case 2:
                            if (HWFLOW) {
                                Log.i(TAG, "get top application packagename policy: " + policyName);
                            }
                            return this.mService.getTopAppPackageName(who, userHandle);
                        default:
                            if (HWFLOW) {
                                Log.i(TAG, "don't have this get policy: " + policyName);
                                break;
                            }
                            break;
                    }
                }
            }
            c = 65535;
            switch (c) {
                case 0:
                    break;
                case 1:
                    break;
                case 2:
                    break;
            }
        } else if (type == 2) {
            return this.mService.getHwAdminCachedBundle(policyName);
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0045, code lost:
        if (r7.equals("set-system-language") == false) goto L_0x0052;
     */
    /* JADX WARNING: Removed duplicated region for block: B:17:0x0056  */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x0071  */
    /* JADX WARNING: Removed duplicated region for block: B:25:0x0093  */
    public int setPolicy(ComponentName who, String policyName, Bundle policyData, int userHandle, int type) {
        this.mService.bdReport(CPUFeature.MSG_SET_CPUSETCONFIG_SCREENON, "policyName: " + policyName + ", type: " + type);
        if (type == 0) {
            return this.mService.setPolicy(who, policyName, policyData, userHandle);
        }
        boolean z = true;
        if (type == 1) {
            int hashCode = policyName.hashCode();
            if (hashCode != 830361577) {
                if (hashCode == 1456366027) {
                }
            } else if (policyName.equals("config-vpn")) {
                z = false;
                switch (z) {
                    case false:
                        if (HWFLOW) {
                            Log.i(TAG, "receive set config-vpn policy: " + policyName);
                        }
                        return this.mService.configVpnProfile(who, policyData, userHandle);
                    case true:
                        if (HWFLOW) {
                            Log.i(TAG, "receive set system language policy: " + policyName);
                        }
                        return this.mService.setSystemLanguage(who, policyData, userHandle);
                    default:
                        if (HWFLOW) {
                            Log.i(TAG, "don't have this set policy: " + policyName);
                        }
                        return 0;
                }
            }
            z = true;
            switch (z) {
                case false:
                    break;
                case true:
                    break;
            }
        } else {
            return 0;
        }
    }

    /* access modifiers changed from: package-private */
    public int removePolicy(ComponentName who, String policyName, Bundle policyData, int userHandle, int type) {
        if (type == 0) {
            return this.mService.removePolicy(who, policyName, policyData, userHandle);
        }
        if (type == 1) {
            char c = 65535;
            if (policyName.hashCode() == 830361577 && policyName.equals("config-vpn")) {
                c = 0;
            }
            if (c == 0) {
                if (HWFLOW) {
                    Log.i(TAG, "receive remove config-vpn policy: " + policyName);
                }
                return this.mService.removeVpnProfile(who, policyData, userHandle);
            } else if (HWFLOW) {
                Log.i(TAG, "don't have this remove policy: " + policyName);
            }
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    public boolean hasHwPolicy(int userHandle) {
        return this.mService.hasHwPolicy(userHandle);
    }

    /* access modifiers changed from: package-private */
    public void setAccountDisabled(ComponentName who, String accountType, boolean disabled, int userHandle) {
        this.mService.setAccountDisabled(who, accountType, disabled, userHandle);
    }

    /* access modifiers changed from: package-private */
    public boolean isAccountDisabled(ComponentName who, String accountType, int userHandle) {
        return this.mService.isAccountDisabled(who, accountType, userHandle);
    }

    /* access modifiers changed from: package-private */
    public boolean formatSDCard(ComponentName who, String diskId, int userHandle) {
        return this.mService.formatSDCard(who, diskId, userHandle);
    }

    /* access modifiers changed from: package-private */
    public boolean installCertificateWithType(ComponentName who, int type, byte[] certBuffer, String name, String password, int flag, boolean requestAccess, int userHandle) {
        return this.mService.installCertificateWithType(who, type, certBuffer, name, password, flag, requestAccess, userHandle);
    }

    /* access modifiers changed from: package-private */
    public boolean setCarrierLockScreenPassword(ComponentName who, String password, String phoneNumber, int userHandle) {
        return this.mService.setCarrierLockScreenPassword(who, password, phoneNumber, userHandle);
    }

    /* access modifiers changed from: package-private */
    public boolean clearCarrierLockScreenPassword(ComponentName who, String password, int userHandle) {
        return this.mService.clearCarrierLockScreenPassword(who, password, userHandle);
    }
}
