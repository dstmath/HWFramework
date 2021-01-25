package com.android.server.devicepolicy;

import android.content.ComponentName;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcel;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.devicepolicy.plugins.SettingsMDMPlugin;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class TransactionProcessor {
    private static final String BDREPORT_MDM_KEY_APINAME = "apiName";
    private static final String BDREPORT_MDM_KEY_PACKAGE = "package";
    private static final String CONFIG_VPN = "config-vpn";
    private static final String GET_ADMIN_DEACTIVE_TIME = "get_admin_deactive_time";
    private static final int GET_POLICY = 1;
    private static final boolean HWDBG = false;
    private static final boolean HWFLOW = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final String IS_FORCED_ACTIVE_ADMIN = "is_forced_active_admin";
    private static final Map<String, String> POLICY_NAME_MAP = new HashMap<String, String>() {
        /* class com.android.server.devicepolicy.TransactionProcessor.AnonymousClass1 */

        {
            put("wifi-ssid-blocklist", "wifi-ssid-blacklist");
            put("wifi-ssid-trustlist", "wifi-ssid-whitelist");
            put("bt-block-list", "bt-black-list");
            put("bt-trust-list", "bt-white-list");
            put("network-block-list", "network-black-list");
            put("install-packages-block-list", "install-packages-black-list");
            put("super-trustlist-hwsystemmanager", "super-whitelist-hwsystemmanager");
            put("enterprise-trustlist-hwsystemmanager", "enterprise-whitelist-hwsystemmanager");
        }
    };
    private static final String POLICY_NETWORK_ETHERNET = "network-set-ethernet-config";
    private static final String POLICY_TOP_PACKAGE_NAME = "policy-top-packagename";
    private static final String QUERY_BROWSING_HISTORY = "queryBrowsingHistory";
    private static final String REMOVE_ACTIVE_ADMIN = "remove_active_admin";
    private static final int REMOVE_POLICY = 3;
    private static final String SET_DELAY_DEACTIVE_ADMIN = "set_delay_deactive_admin";
    private static final String SET_FORCED_ACTIVE_ADMIN = "set_forced_active_admin";
    private static final int SET_POLICY = 2;
    private static final String SET_SYSTEM_LANGUAGE = "set-system-language";
    private static final String TAG = "TransactionProcessor";
    private IHwDevicePolicyManager mService;

    TransactionProcessor(IHwDevicePolicyManager service) {
        this.mService = service;
    }

    private void processSetPolicy(int code, Parcel data, Parcel reply) {
        data.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
        ComponentName who = null;
        if (data.readInt() != 0) {
            who = ComponentName.readFromParcel(data);
        }
        boolean disable = true;
        if (data.readInt() != 1) {
            disable = false;
        }
        int userHandle = data.readInt();
        try {
            bdReportMdmPolicy(who, LegacyPolicy.getPolicyName(code));
            setFunctionDisabled(code, who, disable, userHandle);
            reply.writeNoException();
        } catch (RuntimeException e) {
            HwLog.e(TAG, "processSetPolicy occur RuntimeException");
            reply.writeException(e);
        }
    }

    private void processGetPolicy(int code, Parcel data, Parcel reply) {
        data.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
        ComponentName who = null;
        if (data.readInt() != 0) {
            who = ComponentName.readFromParcel(data);
        }
        boolean isDisabled = isFunctionDisabled(code, who, data.readInt());
        reply.writeNoException();
        reply.writeInt(isDisabled ? 1 : 0);
    }

    private void processGetListCommand(int code, Parcel data, Parcel reply) {
        data.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
        ComponentName who = null;
        if (data.readInt() != 0) {
            who = ComponentName.readFromParcel(data);
        }
        try {
            List<String> packagelist = getListCommand(code, who, data.readInt());
            reply.writeNoException();
            reply.writeStringList(packagelist);
        } catch (RuntimeException e) {
            HwLog.e(TAG, "getListCommand occur RuntimeException");
            reply.writeException(e);
        }
    }

    private void processExecCommand(int code, int type, Parcel data, Parcel reply) {
        data.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
        ComponentName who = null;
        if (data.readInt() != 0) {
            who = ComponentName.readFromParcel(data);
        }
        String parameter = null;
        List<String> listParam = null;
        if (type != 0) {
            if (type == 1) {
                parameter = data.readString();
            } else if (type == 2) {
                listParam = new ArrayList<>();
                data.readStringList(listParam);
            }
        }
        int userHandle = data.readInt();
        try {
            bdReportMdmPolicy(who, LegacyPolicy.getPolicyName(code));
            if (type == 0) {
                execCommand(code, who, userHandle);
            } else if (type == 1) {
                execCommand(code, who, parameter, userHandle);
            } else if (type == 2) {
                execCommand(code, who, listParam, userHandle);
            }
            reply.writeNoException();
        } catch (RuntimeException e) {
            HwLog.e(TAG, "processExecCommand occur RuntimeException");
            reply.writeException(e);
        }
    }

    private void processCaptureScreen(int code, Parcel data, Parcel reply) {
        data.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
        ComponentName who = null;
        if (data.readInt() != 0) {
            who = ComponentName.readFromParcel(data);
        }
        int userHandle = data.readInt();
        bdReportMdmPolicy(who, LegacyPolicy.getPolicyName(code));
        Bitmap bitmapScreen = this.mService.captureScreen(who, userHandle);
        reply.writeNoException();
        if (bitmapScreen != null) {
            reply.writeInt(1);
            bitmapScreen.writeToParcel(reply, 0);
            return;
        }
        reply.writeInt(0);
    }

    private ApnInfo getApnInfoFromParameter(int code, Parcel data) {
        Map<String, String> apnInfo = new HashMap<>();
        String apnId = null;
        switch (code) {
            case 5001:
                data.readMap(apnInfo, null);
                break;
            case 5003:
                data.readMap(apnInfo, null);
                apnId = data.readString();
                break;
            case 5004:
                apnId = data.readString();
                break;
            case 5005:
                data.readMap(apnInfo, null);
                break;
        }
        return new ApnInfo(apnInfo, apnId);
    }

    private void dealApn(ComponentName who, int code, ApnInfo info, Parcel reply, int userHandle) {
        switch (code) {
            case 5001:
                this.mService.addApn(who, info.getApnInfo(), userHandle);
                reply.writeNoException();
                return;
            case 5002:
            default:
                return;
            case 5003:
                this.mService.updateApn(who, info.getApnInfo(), info.getApnId(), userHandle);
                reply.writeNoException();
                return;
            case 5004:
                Map<String, String> apnInfoResult = this.mService.getApnInfo(who, info.getApnId(), userHandle);
                reply.writeNoException();
                reply.writeMap(apnInfoResult);
                return;
            case 5005:
                List<String> ids = this.mService.queryApn(who, info.getApnInfo(), userHandle);
                reply.writeNoException();
                reply.writeStringList(ids);
                return;
        }
    }

    private void processVpn(int code, Parcel data, Parcel reply) {
        if (HWFLOW) {
            HwLog.i(TAG, "HwDPMS processVpn. the code: " + code);
        }
        data.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
        ComponentName who = null;
        if (data.readInt() != 0) {
            who = ComponentName.readFromParcel(data);
        }
        ApnInfo info = getApnInfoFromParameter(code, data);
        int userHandle = data.readInt();
        try {
            bdReportMdmPolicy(who, LegacyPolicy.getPolicyName(code));
            dealApn(who, code, info, reply, userHandle);
        } catch (RuntimeException e) {
            HwLog.e(TAG, "processVpn occur RuntimeException");
            reply.writeException(e);
        }
    }

    private void processCarrierLockScreenPassword(int code, Parcel data, Parcel reply) {
        data.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
        ComponentName who = null;
        if (data.readInt() != 0) {
            who = ComponentName.readFromParcel(data);
        }
        String password = data.readString();
        String phoneNumber = null;
        if (code == 7004) {
            phoneNumber = data.readString();
        }
        int userHandle = data.readInt();
        boolean isResultOk = false;
        try {
            bdReportMdmPolicy(who, LegacyPolicy.getPolicyName(code));
            if (code == 7004) {
                isResultOk = setCarrierLockScreenPassword(who, password, phoneNumber, userHandle);
            } else if (code == 7005) {
                isResultOk = clearCarrierLockScreenPassword(who, password, userHandle);
            }
            reply.writeNoException();
            int i = 1;
            if (!isResultOk) {
                i = 0;
            }
            reply.writeInt(i);
        } catch (RuntimeException e) {
            HwLog.e(TAG, "processCarrierLockScreenPassword occur RuntimeException");
            reply.writeException(e);
        }
    }

    private void processInstallCertificateWithType(int code, Parcel data, Parcel reply) {
        ComponentName who;
        String str;
        String str2;
        RuntimeException e;
        boolean installResult;
        data.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
        if (data.readInt() != 0) {
            who = ComponentName.readFromParcel(data);
        } else {
            who = null;
        }
        int type = data.readInt();
        int len = data.readInt();
        if (len < 0) {
            str = TAG;
        } else if (len > 1048576) {
            str = TAG;
        } else {
            byte[] certBuffer = new byte[len];
            data.readByteArray(certBuffer);
            String name = data.readString();
            String password = data.readString();
            int flag = data.readInt();
            boolean requestAccess = data.readInt() == 1;
            int userHandle = data.readInt();
            try {
                bdReportMdmPolicy(who, LegacyPolicy.getPolicyName(code));
                int i = 1;
                str2 = TAG;
                try {
                    installResult = installCertificateWithType(who, type, certBuffer, name, password, flag, requestAccess, userHandle);
                } catch (RuntimeException e2) {
                    e = e2;
                    HwLog.e(str2, "processInstallCertificateWithType occur RuntimeException");
                    reply.writeException(e);
                    return;
                }
                try {
                    reply.writeNoException();
                    if (!installResult) {
                        i = 0;
                    }
                    reply.writeInt(i);
                    return;
                } catch (RuntimeException e3) {
                    e = e3;
                    HwLog.e(str2, "processInstallCertificateWithType occur RuntimeException");
                    reply.writeException(e);
                    return;
                }
            } catch (RuntimeException e4) {
                e = e4;
                str2 = TAG;
                HwLog.e(str2, "processInstallCertificateWithType occur RuntimeException");
                reply.writeException(e);
                return;
            }
        }
        HwLog.e(str, "processInstallCertificateWithType Certificate exceeds length limit");
        reply.writeNoException();
        reply.writeInt(0);
    }

    private boolean processDefault(int code, Parcel data, Parcel reply) {
        if (LegacySetPolicy.isLegacySetPolicy(code)) {
            processSetPolicy(code, data, reply);
            return true;
        } else if (LegacyGetPolicy.isLegacyGetPolicy(code)) {
            processGetPolicy(code, data, reply);
            return true;
        } else if (LegacyGetListPolicy.isLegacyGetListPolicy(code)) {
            processGetListCommand(code, data, reply);
            return true;
        } else {
            int cmdType = LegacyExecCommandPolicy.getLegacyExecCommandPolicyType(code);
            if (cmdType == -1) {
                return processTransactionEx(code, data, reply);
            }
            processExecCommand(code, cmdType, data, reply);
            return true;
        }
    }

    private void processPackage(int code, Parcel data, Parcel reply) {
        data.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
        ComponentName who = null;
        if (data.readInt() != 0) {
            who = ComponentName.readFromParcel(data);
        }
        String packageName = data.readString();
        boolean keepData = true;
        if (data.readInt() != 1) {
            keepData = false;
        }
        int userHandle = data.readInt();
        try {
            if (HWFLOW) {
                HwLog.i(TAG, "HwDPMS processPackage packageName: " + packageName + ", keepData: " + keepData + ", user: " + userHandle);
            }
            bdReportMdmPolicy(who, LegacyPolicy.getPolicyName(code));
            this.mService.uninstallPackage(who, packageName, keepData, userHandle);
            reply.writeNoException();
        } catch (RuntimeException e) {
            HwLog.e(TAG, "processPackage occur RuntimeException");
            reply.writeException(e);
        }
    }

    private void processEmail(int code, Parcel data, Parcel reply) {
        data.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
        ComponentName who = null;
        if (data.readInt() != 0) {
            who = ComponentName.readFromParcel(data);
        }
        Bundle paraex = new Bundle();
        String domain = null;
        if (code == 3501) {
            paraex.readFromParcel(data);
        } else if (code == 3502) {
            domain = data.readString();
        }
        int userHandle = data.readInt();
        try {
            if (HWFLOW) {
                HwLog.i(TAG, "HwDPMS processEmail. code: " + code + ", user: " + userHandle);
            }
            bdReportMdmPolicy(who, LegacyPolicy.getPolicyName(code));
            if (code == 3501) {
                this.mService.configExchangeMailProvider(who, paraex, userHandle);
                reply.writeNoException();
            } else if (code == 3502) {
                Bundle para = this.mService.getMailProviderForDomain(who, domain, userHandle);
                reply.writeNoException();
                reply.writeInt(para == null ? 0 : 1);
                if (para != null) {
                    para.writeToParcel(reply, 0);
                }
            }
        } catch (RuntimeException e) {
            HwLog.e(TAG, "processEmail occur RuntimeException");
            reply.writeException(e);
        }
    }

    private void processDefaultLauncher(int code, Parcel data, Parcel reply) {
        data.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
        ComponentName who = null;
        if (data.readInt() != 0) {
            who = ComponentName.readFromParcel(data);
        }
        String packageName = data.readString();
        String className = data.readString();
        int userHandle = data.readInt();
        if (HWFLOW) {
            HwLog.i(TAG, "HwDPMS processDefaultLauncher packageName: " + packageName + ", className: " + className + ", user: " + userHandle);
        }
        try {
            bdReportMdmPolicy(who, LegacyPolicy.getPolicyName(code));
            this.mService.setDefaultLauncher(who, packageName, className, userHandle);
        } catch (RuntimeException e) {
            HwLog.e(TAG, "processDefaultLauncher occur RuntimeException");
            reply.writeException(e);
        }
    }

    private void processAccount(int code, Parcel data, Parcel reply) {
        data.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
        ComponentName who = null;
        if (data.readInt() != 0) {
            who = ComponentName.readFromParcel(data);
        }
        String accountType = data.readString();
        boolean disabled = false;
        int i = 0;
        if (code == 5018) {
            disabled = data.readInt() == 1;
        } else if (code != 5019) {
        }
        int userHandle = data.readInt();
        try {
            bdReportMdmPolicy(who, LegacyPolicy.getPolicyName(code));
            if (code == 5018) {
                setAccountDisabled(who, accountType, disabled, userHandle);
                reply.writeNoException();
            } else if (code == 5019) {
                boolean isDisable = isAccountDisabled(who, accountType, userHandle);
                reply.writeNoException();
                if (isDisable) {
                    i = 1;
                }
                reply.writeInt(i);
            }
        } catch (RuntimeException e) {
            HwLog.e(TAG, "processAccount occur RuntimeException");
            reply.writeException(e);
        }
    }

    private void processSdCard(int code, Parcel data, Parcel reply) {
        data.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
        ComponentName who = null;
        if (data.readInt() != 0) {
            who = ComponentName.readFromParcel(data);
        }
        String diskId = data.readString();
        int userHandle = data.readInt();
        try {
            bdReportMdmPolicy(who, LegacyPolicy.getPolicyName(code));
            boolean isFormatSuccess = formatSDCard(who, diskId, userHandle);
            reply.writeNoException();
            int i = 1;
            if (!isFormatSuccess) {
                i = 0;
            }
            reply.writeInt(i);
        } catch (RuntimeException e) {
            HwLog.e(TAG, "processSdCard occur RuntimeException");
            reply.writeException(e);
        }
    }

    private void processSDCardEncryptionStatus(int code, Parcel data, Parcel reply) {
        data.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
        int encryptionStatus = getSDCardEncryptionStatus(code);
        reply.writeNoException();
        reply.writeInt(encryptionStatus);
    }

    /* access modifiers changed from: package-private */
    public boolean processTransaction(int code, Parcel data, Parcel reply) {
        if (code == 1506) {
            processDefaultLauncher(code, data, reply);
            return true;
        } else if (code == 1509) {
            processCaptureScreen(code, data, reply);
            return true;
        } else if (code != 2502) {
            if (code != 5001) {
                if (code == 5010) {
                    processSDCardEncryptionStatus(code, data, reply);
                    return true;
                } else if (code == 3501 || code == 3502) {
                    processEmail(code, data, reply);
                    return true;
                } else if (code == 7004 || code == 7005) {
                    processCarrierLockScreenPassword(code, data, reply);
                    return true;
                } else {
                    switch (code) {
                        case 5003:
                        case 5004:
                        case 5005:
                            break;
                        default:
                            switch (code) {
                                case 5017:
                                    processSdCard(code, data, reply);
                                    return true;
                                case 5018:
                                case 5019:
                                    processAccount(code, data, reply);
                                    return true;
                                case 5020:
                                    processInstallCertificateWithType(code, data, reply);
                                    return true;
                                default:
                                    return processDefault(code, data, reply);
                            }
                    }
                }
            }
            processVpn(code, data, reply);
            return true;
        } else {
            processPackage(code, data, reply);
            return true;
        }
    }

    private void processDefaultDataCard(int code, Parcel data, Parcel reply) {
        data.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
        ComponentName who = null;
        if (data.readInt() != 0) {
            who = ComponentName.readFromParcel(data);
        }
        Message response = null;
        if (data.readInt() != 0) {
            response = (Message) Message.CREATOR.createFromParcel(data);
        }
        int slotId = data.readInt();
        int userHandle = data.readInt();
        try {
            bdReportMdmPolicy(who, LegacyPolicy.getPolicyName(code));
            boolean result = setDefaultDataCard(who, slotId, response, userHandle);
            reply.writeNoException();
            reply.writeInt(result ? 1 : 0);
        } catch (RuntimeException e) {
            HwLog.e(TAG, "processDefaultDataCard occur RuntimeException");
            reply.writeException(e);
        }
    }

    private boolean processTransactionEx(int code, Parcel data, Parcel reply) {
        if (code != 1514) {
            return false;
        }
        processDefaultDataCard(code, data, reply);
        return true;
    }

    /* access modifiers changed from: package-private */
    public void setFunctionDisabled(int code, ComponentName who, boolean isDisabled, int userHandle) {
        if (HWFLOW) {
            HwLog.i(TAG, "HwDPMS setFunctionDisabled. the code: " + code + ", value: " + isDisabled + ", user: " + userHandle);
        }
        switch (code) {
            case 1004:
                this.mService.setWifiDisabled(who, isDisabled, userHandle);
                return;
            case 1006:
                this.mService.setWifiApDisabled(who, isDisabled, userHandle);
                return;
            case 1008:
                this.mService.setBootLoaderDisabled(who, isDisabled, userHandle);
                return;
            case 1010:
                this.mService.setUSBDataDisabled(who, isDisabled, userHandle);
                return;
            case 1012:
                this.mService.setExternalStorageDisabled(who, isDisabled, userHandle);
                return;
            case 1014:
                this.mService.setNFCDisabled(who, isDisabled, userHandle);
                return;
            case 1016:
                this.mService.setDataConnectivityDisabled(who, isDisabled, userHandle);
                return;
            case 1018:
                this.mService.setVoiceDisabled(who, isDisabled, userHandle);
                return;
            case 1020:
                this.mService.setSMSDisabled(who, isDisabled, userHandle);
                return;
            case 1022:
                this.mService.setStatusBarExpandPanelDisabled(who, isDisabled, userHandle);
                return;
            case 1024:
                this.mService.setBluetoothDisabled(who, isDisabled, userHandle);
                return;
            case 1026:
                this.mService.setGPSDisabled(who, isDisabled, userHandle);
                return;
            case 1028:
                this.mService.setAdbDisabled(who, isDisabled, userHandle);
                return;
            case 1030:
                this.mService.setUSBOtgDisabled(who, isDisabled, userHandle);
                return;
            case 1032:
                this.mService.setSafeModeDisabled(who, isDisabled, userHandle);
                return;
            case 1034:
                this.mService.setTaskButtonDisabled(who, isDisabled, userHandle);
                return;
            case 1036:
                this.mService.setHomeButtonDisabled(who, isDisabled, userHandle);
                return;
            case 1038:
                this.mService.setBackButtonDisabled(who, isDisabled, userHandle);
                return;
            case 1504:
                this.mService.turnOnGPS(who, isDisabled, userHandle);
                return;
            case 1513:
                this.mService.turnOnMobiledata(who, isDisabled, userHandle);
                return;
            case 5011:
                this.mService.setSDCardDecryptionDisabled(who, isDisabled, userHandle);
                return;
            default:
                return;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isFunctionDisabled(int code, ComponentName who, int userHandle) {
        if (HWFLOW) {
            HwLog.i(TAG, "HwDPMS isFunctionDisabled. the code: " + code + ", user: " + userHandle);
        }
        if (code == 5012) {
            return this.mService.isSDCardDecryptionDisabled(who, userHandle);
        }
        if (userHandle != 0) {
            return false;
        }
        switch (code) {
            case 1005:
                return this.mService.isWifiDisabled(who, userHandle);
            case 1007:
                return this.mService.isWifiApDisabled(who, userHandle);
            case 1009:
                return this.mService.isBootLoaderDisabled(who, userHandle);
            case 1011:
                return this.mService.isUSBDataDisabled(who, userHandle);
            case 1013:
                return this.mService.isExternalStorageDisabled(who, userHandle);
            case 1015:
                return this.mService.isNFCDisabled(who, userHandle);
            case 1017:
                return this.mService.isDataConnectivityDisabled(who, userHandle);
            case 1019:
                return this.mService.isVoiceDisabled(who, userHandle);
            case 1021:
                return this.mService.isSMSDisabled(who, userHandle);
            case 1023:
                return this.mService.isStatusBarExpandPanelDisabled(who, userHandle);
            case 1025:
                return this.mService.isBluetoothDisabled(who, userHandle);
            case 1027:
                return this.mService.isGPSDisabled(who, userHandle);
            case 1029:
                return this.mService.isAdbDisabled(who, userHandle);
            case 1031:
                return this.mService.isUSBOtgDisabled(who, userHandle);
            case 1033:
                return this.mService.isSafeModeDisabled(who, userHandle);
            case 1035:
                return this.mService.isTaskButtonDisabled(who, userHandle);
            case 1037:
                return this.mService.isHomeButtonDisabled(who, userHandle);
            case 1039:
                return this.mService.isBackButtonDisabled(who, userHandle);
            case 1503:
                return this.mService.isRooted(who, userHandle);
            case 1505:
                return this.mService.isGPSTurnOn(who, userHandle);
            case 2506:
                return this.mService.isInstallSourceDisabled(who, userHandle);
            case 4011:
                return this.mService.isSafeModeDisabled(null, userHandle);
            default:
                if (HwAdminCachedPolicy.isCachedPolicy(code, false)) {
                    return this.mService.getHwAdminCachedValue(code);
                }
                return false;
        }
    }

    /* access modifiers changed from: package-private */
    public void execCommand(int code, ComponentName who, int userHandle) {
        log(code, userHandle);
        if (code == 1501 || code == 1502) {
            this.mService.shutdownOrRebootDevice(code, who, userHandle);
        } else if (code == 1507) {
            this.mService.clearDefaultLauncher(who, userHandle);
        } else if (code == 1512) {
            this.mService.clearDeviceOwnerApp(userHandle);
        } else if (code == 2001) {
            this.mService.hangupCalling(who, userHandle);
        } else if (code == 2504) {
            this.mService.enableInstallPackage(who, userHandle);
        } else if (code == 3503) {
            this.mService.resetNetorkSetting(who, userHandle);
        } else if (code == 6001) {
            this.mService.setSilentActiveAdmin(who, userHandle);
        }
    }

    private void setSystemTime(ComponentName who, String param, int userHandle) {
        try {
            this.mService.setSysTime(who, Long.parseLong(param), userHandle);
        } catch (NumberFormatException e) {
            HwLog.e(TAG, "setSysTime : NumberFormatException");
        }
    }

    private void log(int code, int userHandle) {
        if (HWFLOW) {
            HwLog.i(TAG, "HwDPMS execCommand. the code: " + code + ", user: " + userHandle);
        }
    }

    private void execCommand(int code, ComponentName who, String param, int userHandle) {
        log(code, userHandle);
        if (code == 1510) {
            setSystemTime(who, param, userHandle);
        } else if (code == 1511) {
            this.mService.setDeviceOwnerApp(who, param, userHandle);
        } else if (code == 2501) {
            this.mService.installPackage(who, param, userHandle);
        } else if (code == 2503) {
            this.mService.clearPackageData(who, param, userHandle);
        } else if (code == 3007) {
            this.mService.killApplicationProcess(who, param, userHandle);
        } else if (code == 5002) {
            this.mService.deleteApn(who, param, userHandle);
        } else if (code == 5006) {
            this.mService.setPreferApn(who, param, userHandle);
        }
    }

    /* access modifiers changed from: package-private */
    public void execCommand(int code, ComponentName who, List<String> param, int userHandle) {
        log(code, userHandle);
        if (code == 1508) {
            this.mService.setCustomSettingsMenu(who, param, userHandle);
        } else if (code != 2505) {
            if (code != 2508) {
                if (code != 2509) {
                    if (code == 2511) {
                        this.mService.addDisallowedUninstallPackages(who, param, userHandle);
                        return;
                    } else if (code == 2512) {
                        this.mService.removeDisallowedUninstallPackages(who, param, userHandle);
                        return;
                    } else if (code == 2514) {
                        this.mService.addDisabledDeactivateMdmPackages(who, param, userHandle);
                        return;
                    } else if (code == 2515) {
                        this.mService.removeDisabledDeactivateMdmPackages(who, param, userHandle);
                        return;
                    } else if (code != 2517) {
                        if (code != 2518) {
                            if (code == 3001) {
                                this.mService.addPersistentApp(who, param, userHandle);
                                return;
                            } else if (code == 3002) {
                                this.mService.removePersistentApp(who, param, userHandle);
                                return;
                            } else if (code == 3004) {
                                this.mService.addDisallowedRunningApp(who, param, userHandle);
                                return;
                            } else if (code == 3005) {
                                this.mService.removeDisallowedRunningApp(who, param, userHandle);
                                return;
                            } else if (code == 5007) {
                                this.mService.addNetworkAccessWhitelist(who, param, userHandle);
                                return;
                            } else if (code == 5008) {
                                this.mService.removeNetworkAccessWhitelist(who, param, userHandle);
                                return;
                            } else {
                                return;
                            }
                        }
                    }
                }
                this.mService.removeInstallPackageWhiteList(who, param, userHandle);
                return;
            }
            this.mService.addInstallPackageWhiteList(who, param, userHandle);
        } else {
            this.mService.disableInstallSource(who, param, userHandle);
        }
    }

    /* access modifiers changed from: package-private */
    public List<String> getListCommand(int code, ComponentName who, int userHandle) {
        if (userHandle != 0) {
            return null;
        }
        if (HWFLOW) {
            HwLog.i(TAG, "HwDPMS getListCommand. the code: " + code + ", user: " + userHandle);
        }
        if (code == 2507) {
            return this.mService.getInstallPackageSourceWhiteList(who, userHandle);
        }
        if (code == 2510) {
            return this.mService.getInstallPackageWhiteList(who, userHandle);
        }
        if (code == 2513) {
            return this.mService.getDisallowedUninstallPackageList(who, userHandle);
        }
        if (code == 2516) {
            return this.mService.getDisabledDeactivateMdmPackageList(who, userHandle);
        }
        if (code == 3003) {
            return this.mService.getPersistentApp(who, userHandle);
        }
        if (code == 3006) {
            return this.mService.getDisallowedRunningApp(who, userHandle);
        }
        if (code == 5009) {
            return this.mService.getNetworkAccessWhitelist(who, userHandle);
        }
        if (HwAdminCachedPolicy.isCachedPolicy(code, true)) {
            return this.mService.getHwAdminCachedList(code);
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public int getSDCardEncryptionStatus(int code) {
        if (code != 5010) {
            return 0;
        }
        return this.mService.getSDCardEncryptionStatus();
    }

    private void dealPolicyByType(Parcel data, Parcel reply, int type) {
        data.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
        ComponentName who = null;
        if (data.readInt() != 0) {
            who = ComponentName.readFromParcel(data);
        }
        int policyUser = data.readInt();
        String policyName = data.readString();
        Bundle policyData = data.readBundle();
        int policyType = data.readInt();
        if (type == 1) {
            Bundle getPolicyData = getPolicy(who, policyName, policyData, policyUser, policyType);
            reply.writeNoException();
            reply.writeBundle(getPolicyData);
        } else if (type == 2) {
            int setPolicyResult = setPolicy(who, policyName, policyData, policyUser, policyType);
            reply.writeNoException();
            reply.writeInt(setPolicyResult);
        } else if (type == 3) {
            try {
                int removePolicyResult = removePolicy(who, policyName, policyData, policyUser, policyType);
                reply.writeNoException();
                reply.writeInt(removePolicyResult);
            } catch (RuntimeException e) {
                HwLog.e(TAG, "getPolicy occur RuntimeException");
                reply.writeException(e);
            }
        }
    }

    private void dealHwPolicy(Parcel data, Parcel reply) {
        data.enforceInterface("com.huawei.android.app.admin.hwdevicepolicymanagerex");
        try {
            boolean isDisabled = hasHwPolicy(data.readInt());
            reply.writeNoException();
            reply.writeInt(isDisabled ? 1 : 0);
        } catch (RuntimeException e) {
            HwLog.e(TAG, "hasHwPolicy occur RuntimeException");
            reply.writeException(e);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean processTransactionWithPolicyName(int code, Parcel data, Parcel reply) {
        switch (code) {
            case 5013:
                dealPolicyByType(data, reply, 1);
                return true;
            case 5014:
                dealPolicyByType(data, reply, 2);
                return true;
            case 5015:
                dealPolicyByType(data, reply, 3);
                return true;
            case 5016:
                dealHwPolicy(data, reply);
                return true;
            default:
                return false;
        }
    }

    /* access modifiers changed from: package-private */
    public Bundle getPolicy(ComponentName who, String policyName, Bundle keyWords, int userHandle, int type) {
        if (type == 0) {
            return this.mService.getPolicy(who, policyName, userHandle);
        }
        if (type == 1) {
            if (HWFLOW) {
                Log.i(TAG, "getPolicy receive policy: " + policyName);
            }
            char c = 65535;
            switch (policyName.hashCode()) {
                case -1913111060:
                    if (policyName.equals(GET_ADMIN_DEACTIVE_TIME)) {
                        c = 4;
                        break;
                    }
                    break;
                case -1208014361:
                    if (policyName.equals(IS_FORCED_ACTIVE_ADMIN)) {
                        c = 3;
                        break;
                    }
                    break;
                case -1078880066:
                    if (policyName.equals(POLICY_TOP_PACKAGE_NAME)) {
                        c = 2;
                        break;
                    }
                    break;
                case 635128942:
                    if (policyName.equals(POLICY_NETWORK_ETHERNET)) {
                        c = 5;
                        break;
                    }
                    break;
                case 830361577:
                    if (policyName.equals(CONFIG_VPN)) {
                        c = 1;
                        break;
                    }
                    break;
                case 1115753445:
                    if (policyName.equals(QUERY_BROWSING_HISTORY)) {
                        c = 0;
                        break;
                    }
                    break;
            }
            if (c == 0) {
                Bundle result = new Bundle();
                result.putStringArrayList(SettingsMDMPlugin.STATE_VALUE, this.mService.queryBrowsingHistory(who, userHandle));
                return result;
            } else if (c != 1) {
                if (c == 2) {
                    return this.mService.getTopAppPackageName(who, userHandle);
                }
                if (c == 3) {
                    return this.mService.isForcedActiveDeviceAdmin(who, userHandle);
                }
                if (c == 4) {
                    return this.mService.getDeviceAdminDeactiveTime(who, userHandle);
                }
                if (c != 5) {
                    return null;
                }
                return this.mService.getEthernetConfiguration(who, keyWords, userHandle);
            } else if (keyWords == null) {
                return this.mService.getVpnList(who, null, userHandle);
            } else {
                return this.mService.getVpnProfile(who, keyWords, userHandle);
            }
        } else if (type == 2) {
            return this.mService.getHwAdminCachedBundle(policyName);
        } else {
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public int setPolicy(ComponentName who, String policyName, Bundle policyData, int userHandle, int type) {
        bdReportMdmPolicy(who, policyName);
        if (type == 0) {
            return this.mService.setPolicy(who, POLICY_NAME_MAP.containsKey(policyName) ? POLICY_NAME_MAP.get(policyName) : policyName, policyData, userHandle);
        } else if (type != 1) {
            return 0;
        } else {
            if (HWFLOW) {
                HwLog.i(TAG, "setPolicy receive policy: " + policyName);
            }
            char c = 65535;
            switch (policyName.hashCode()) {
                case -335033904:
                    if (policyName.equals(SET_DELAY_DEACTIVE_ADMIN)) {
                        c = 3;
                        break;
                    }
                    break;
                case -20378337:
                    if (policyName.equals(SET_FORCED_ACTIVE_ADMIN)) {
                        c = 2;
                        break;
                    }
                    break;
                case 635128942:
                    if (policyName.equals(POLICY_NETWORK_ETHERNET)) {
                        c = 4;
                        break;
                    }
                    break;
                case 830361577:
                    if (policyName.equals(CONFIG_VPN)) {
                        c = 0;
                        break;
                    }
                    break;
                case 1456366027:
                    if (policyName.equals(SET_SYSTEM_LANGUAGE)) {
                        c = 1;
                        break;
                    }
                    break;
            }
            if (c == 0) {
                return this.mService.configVpnProfile(who, policyData, userHandle);
            }
            if (c == 1) {
                return this.mService.setSystemLanguage(who, policyData, userHandle);
            }
            if (c == 2) {
                return this.mService.setForcedActiveDeviceAdmin(who, userHandle);
            }
            if (c == 3) {
                return this.mService.setDelayDeactiveDeviceAdmin(who, policyData, userHandle);
            }
            if (c != 4) {
                return 0;
            }
            return this.mService.setEthernetConfiguration(who, policyData, userHandle);
        }
    }

    /* access modifiers changed from: package-private */
    public int removePolicy(ComponentName who, String policyName, Bundle policyData, int userHandle, int type) {
        int result = 0;
        if (type == 0) {
            result = this.mService.removePolicy(who, policyName, policyData, userHandle);
        }
        if (type != 1) {
            return result;
        }
        if (HWFLOW) {
            HwLog.i(TAG, "removePolicy receive policy: " + policyName);
        }
        char c = 65535;
        int hashCode = policyName.hashCode();
        if (hashCode != -1130401615) {
            if (hashCode == 830361577 && policyName.equals(CONFIG_VPN)) {
                c = 0;
            }
        } else if (policyName.equals(REMOVE_ACTIVE_ADMIN)) {
            c = 1;
        }
        if (c == 0) {
            return this.mService.removeVpnProfile(who, policyData, userHandle);
        }
        if (c != 1) {
            return result;
        }
        return this.mService.removeActiveDeviceAdmin(who, userHandle);
    }

    private void bdReportMdmPolicy(ComponentName who, String policyName) {
        if (!TextUtils.isEmpty(policyName)) {
            try {
                JSONObject obj = new JSONObject();
                obj.put(BDREPORT_MDM_KEY_PACKAGE, who == null ? SettingsMDMPlugin.EMPTY_STRING : who.getPackageName());
                obj.put(BDREPORT_MDM_KEY_APINAME, policyName);
                if (this.mService != null) {
                    this.mService.bdReport(991310126, obj.toString());
                }
            } catch (JSONException e) {
                HwLog.e(TAG, "JSONException can not put on obj");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasHwPolicy(int userHandle) {
        return this.mService.hasHwPolicy(userHandle);
    }

    /* access modifiers changed from: package-private */
    public void setAccountDisabled(ComponentName who, String accountType, boolean isDisabled, int userHandle) {
        this.mService.setAccountDisabled(who, accountType, isDisabled, userHandle);
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
    public boolean installCertificateWithType(ComponentName who, int type, byte[] certBuffer, String name, String password, int flag, boolean isRequestAccess, int userHandle) {
        return this.mService.installCertificateWithType(who, type, certBuffer, name, password, flag, isRequestAccess, userHandle);
    }

    /* access modifiers changed from: package-private */
    public boolean setCarrierLockScreenPassword(ComponentName who, String password, String phoneNumber, int userHandle) {
        return this.mService.setCarrierLockScreenPassword(who, password, phoneNumber, userHandle);
    }

    /* access modifiers changed from: package-private */
    public boolean clearCarrierLockScreenPassword(ComponentName who, String password, int userHandle) {
        return this.mService.clearCarrierLockScreenPassword(who, password, userHandle);
    }

    /* access modifiers changed from: package-private */
    public boolean setDefaultDataCard(ComponentName who, int slot, Message response, int userHandle) {
        return this.mService.setDefaultDataCard(who, slot, response, userHandle);
    }
}
