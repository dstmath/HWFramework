package com.android.internal.telephony;

import android.common.HwFrameworkFactory;
import android.content.Context;
import android.content.Intent;
import android.database.DatabaseUtils;
import android.net.IConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.TrafficStats;
import android.net.booster.IHwCommBoosterServiceManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.telephony.data.ApnSetting;
import android.text.TextUtils;
import android.util.ArraySet;
import com.huawei.android.telephony.RlogEx;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.dataconnection.ApnContextEx;
import huawei.cust.HwCfgFilePolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class HwDataConnectionManagerImpl extends DefaultDataConnectionManager {
    private static final String ACTION_NETWORK_SLICE_LOST = "com.huawei.intent.action.NETWORK_SLICE_LOST";
    private static final String BOOSTER_REPORT_DNN = "dnn";
    private static final String BOOSTER_REPORT_FAIL_REASON = "failCause";
    private static final String BOOSTER_REPORT_PDUSESSIONTYPE = "pduSessionType";
    private static final String BOOSTER_REPORT_SNSSAI = "sNssai";
    private static final String BOOSTER_REPORT_SSCMODE = "sscMode";
    private static final int CONNECTIVITY_SERVICE_NEED_SET_USER_DATA = 1100;
    private static final int DEFAULT_VALUE_STATE = 0;
    private static final int ERROR_INVALID_PARAM = -3;
    private static final int ERROR_NO_SERVICE = -1;
    private static final int NORMAL_NO_NEED_TO_REPORT = 1;
    private static final int[] NR_SLICE_DATA_FAIL_REASON = new int[1];
    private static final String PACKAGE_NAME = "com.android.internal.telephony";
    private static final int REPORT_FAIL_REASON = 805;
    private static final int SWITCHING_STATE = 1;
    private static final int SWITCH_DONE = 2;
    private static final String TAG = "HwDataConnectionManagerImpl";
    private static final int UNSPECIFIED_INT = -1;
    private static HwDataConnectionManager sInstance = new HwDataConnectionManagerImpl();

    public static native String sbmcgmGenId(String str, String str2, String str3);

    public static native String sbmcgmGenPasswd(String str);

    static {
        if (SystemProperties.getBoolean("ro.config.sbmcgm", false)) {
            try {
                System.load(SystemProperties.get("ro.config.sbmjni_uri"));
            } catch (UnsatisfiedLinkError e) {
                loge("sbnam load sbm jni fail.");
            }
        }
    }

    private HwDataConnectionManagerImpl() {
    }

    public static HwDataConnectionManager getDefault() {
        return sInstance;
    }

    public boolean needSetUserDataEnabled(boolean enabled) {
        IConnectivityManager cm = IConnectivityManager.Stub.asInterface(ServiceManager.getService("connectivity"));
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        boolean z = true;
        try {
            IBinder connectivityServiceBinder = cm.asBinder();
            if (connectivityServiceBinder != null) {
                data.writeInterfaceToken("android.net.IConnectivityManager");
                data.writeInt(enabled ? 1 : 0);
                connectivityServiceBinder.transact(1101, data, reply, 0);
            }
            DatabaseUtils.readExceptionFromParcel(reply);
            int result = reply.readInt();
            log("needSetUserDataEnabled result = " + result);
            if (result != 1) {
                z = false;
            }
            return z;
        } catch (RemoteException e) {
            loge("localRemoteException");
            return true;
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    public long getThisModemMobileTxPackets(HashMap<String, Integer> ifacePhoneHashMap, int phoneId) {
        long total = 0;
        for (String iface : getThisModemMobileIfaces(ifacePhoneHashMap, phoneId)) {
            long temp = TrafficStats.getTxPackets(iface);
            if (temp == 0) {
                log("getThisModemMobileTxPackets is 0");
            }
            total += temp;
        }
        return total;
    }

    public long getThisModemMobileRxPackets(HashMap<String, Integer> ifacePhoneHashMap, int phoneId) {
        long total = 0;
        for (String iface : getThisModemMobileIfaces(ifacePhoneHashMap, phoneId)) {
            long temp = TrafficStats.getRxPackets(iface);
            if (temp == 0) {
                log("getThisModemMobileRxPackets is 0");
            }
            total += temp;
        }
        return total;
    }

    private String[] getThisModemMobileIfaces(HashMap<String, Integer> ifacePhoneHashMap, int phoneId) {
        ArraySet<String> mobileIfaces = new ArraySet<>();
        String[] allActiveIfaces = TrafficStats.getMobileIfacesEx();
        for (String iface : allActiveIfaces) {
            if (ifacePhoneHashMap.get(iface) == null || ifacePhoneHashMap.get(iface).equals(Integer.valueOf(phoneId))) {
                mobileIfaces.add(iface);
            }
        }
        return (String[]) mobileIfaces.toArray(new String[mobileIfaces.size()]);
    }

    public boolean getNamSwitcherForSoftbank() {
        return SystemProperties.getBoolean("ro.config.sbmcgm", false);
    }

    public boolean isSoftBankCard(PhoneExt phone) {
        String softbankPlmns = Settings.System.getString(phone.getContext().getContentResolver(), "hw_softbank_plmn");
        TelephonyManager tm = (TelephonyManager) phone.getContext().getSystemService("phone");
        String operator = tm != null ? tm.getSimOperator() : "";
        loge("sbnam:isSoftBankCard sbnam hw_softbank_plmn:" + softbankPlmns + " operator:" + operator);
        if (softbankPlmns != null) {
            String[] plmns = softbankPlmns.split(",");
            for (String plmn : plmns) {
                if (plmn != null && plmn.equals(operator)) {
                    loge("sbnam:isSoftBankCard sbnam find softbank card " + operator);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isValidMsisdn(PhoneExt phone) {
        String line1Number = phone.getLine1Number();
        if (line1Number != null) {
            return !line1Number.isEmpty();
        }
        return false;
    }

    public HashMap<String, String> encryptApnInfoForSoftBank(PhoneExt phone, ApnSetting apnSetting) {
        TelephonyManager tm;
        String sUsername = apnSetting.getUser();
        String sPassword = apnSetting.getPassword();
        if (!TextUtils.isEmpty(sUsername) || !TextUtils.isEmpty(sPassword) || (tm = (TelephonyManager) phone.getContext().getSystemService("phone")) == null) {
            return null;
        }
        if (!isSoftBankCard(phone)) {
            loge("softbanknam: not softbank card");
            return null;
        } else if (!isValidMsisdn(phone)) {
            loge("softbanknam: no msisdn softbank card");
            return null;
        } else {
            String msn = tm.getLine1Number();
            if (msn != null && msn.length() > 11) {
                msn = msn.substring(0, 11);
            }
            String imei = tm.getDeviceId();
            if (imei != null && imei.length() > 14) {
                imei = imei.substring(0, 14);
            }
            String imsi = tm.getSubscriberId();
            if (imsi != null && imsi.length() > 15) {
                imsi = imsi.substring(0, 15);
            }
            String iccid = tm.getSimSerialNumber();
            if (iccid != null && iccid.length() > 19) {
                iccid = iccid.substring(0, 19);
            }
            String user = sbmcgmGenId(msn, imei, imsi);
            if (TextUtils.isEmpty(user)) {
                return null;
            }
            HashMap<String, String> userInfo = new HashMap<>();
            userInfo.put("username", user);
            userInfo.put("password", sbmcgmGenPasswd(iccid));
            loge("softbanknam: after encryption finish");
            return userInfo;
        }
    }

    public boolean isSlaveActive() {
        HwPhoneService service = HwPhoneService.getInstance();
        if (service == null) {
            return false;
        }
        return service.isSlaveActive();
    }

    public boolean isSwitchingToSlave() {
        HwPhoneService service = HwPhoneService.getInstance();
        if (service == null) {
            return false;
        }
        return service.isSwitchingToSlave();
    }

    public void registerImsCallStates(boolean enable, int phoneId) {
        HwPhoneService service = HwPhoneService.getInstance();
        if (service != null) {
            service.registerImsCallStates(enable, phoneId);
        }
    }

    public boolean isSwitchingSmartCard() {
        int switchState = SystemProperties.getInt("persist.sys.smart_switch_state", 0);
        if (switchState == 1 || switchState == 2) {
            return true;
        }
        return false;
    }

    private boolean enableCompatibleSimilarApnSettings(PhoneExt phone, String operator) {
        if (!HuaweiTelephonyConfigs.isQcomPlatform() && phone != null) {
            boolean similarApnState = false;
            boolean hasHwCfgConfig = false;
            Boolean similarApnSign = (Boolean) HwCfgFilePolicy.getValue("compatible_apn_switch", phone.getPhoneId(), Boolean.class);
            if (similarApnSign != null) {
                hasHwCfgConfig = true;
                similarApnState = similarApnSign.booleanValue();
            }
            if (similarApnState) {
                log("enableCompatibleSimilarApnSettings: similarApnSign=" + similarApnSign);
                return true;
            } else if (!hasHwCfgConfig || similarApnState) {
                String plmnsConfig = Settings.System.getString(phone.getContext().getContentResolver(), "compatible_apn_plmn");
                if (TextUtils.isEmpty(plmnsConfig)) {
                    return false;
                }
                String[] plmns = plmnsConfig.split(",");
                for (String plmn : plmns) {
                    if (!TextUtils.isEmpty(plmn) && plmn.equals(operator)) {
                        log("enableCompatibleSimilarApnSettings: operator: " + operator);
                        return true;
                    }
                }
            } else {
                log("enableCompatibleSimilarApnSettings: similarApnSign=" + similarApnSign);
                return false;
            }
        }
        return false;
    }

    public String[] getCompatibleSimilarApnSettingsTypes(PhoneExt phone, String operator, ApnSetting currentApnSetting, List<ApnSetting> allApnSettings) {
        if (enableCompatibleSimilarApnSettings(phone, operator)) {
            ArrayList<String> resultTypes = new ArrayList<>();
            if (currentApnSetting == null) {
                return (String[]) resultTypes.toArray(new String[0]);
            }
            resultTypes.addAll(Arrays.asList(ApnSetting.getApnTypesStringFromBitmask(currentApnSetting.getApnTypeBitmask()).split(",")));
            if (allApnSettings == null) {
                return (String[]) resultTypes.toArray(new String[0]);
            }
            int listSize = allApnSettings.size();
            for (int i = 0; i < listSize; i++) {
                ApnSetting apn = allApnSettings.get(i);
                if (!currentApnSetting.equals(apn) && apnSettingsSimilar(currentApnSetting, apn)) {
                    String[] types = ApnSetting.getApnTypesStringFromBitmask(apn.getApnTypeBitmask()).split(",");
                    for (String type : types) {
                        if (!resultTypes.contains(type)) {
                            resultTypes.add(type);
                        }
                    }
                }
            }
            log("getCompatibleSimilarApnSettingsTypes: resultTypes " + resultTypes);
            return (String[]) resultTypes.toArray(new String[0]);
        } else if (currentApnSetting != null) {
            return ApnSetting.getApnTypesStringFromBitmask(currentApnSetting.getApnTypeBitmask()).split(",");
        } else {
            return new String[0];
        }
    }

    private boolean apnSettingsSimilar(ApnSetting first, ApnSetting second) {
        String secondMmsc = null;
        String firstMmsc = first.getMmsc() == null ? null : first.getMmsc().toString();
        if (second.getMmsc() != null) {
            secondMmsc = second.getMmsc().toString();
        }
        return Objects.equals(first.getApnName(), second.getApnName()) && (first.getAuthType() == second.getAuthType() || -1 == first.getAuthType() || -1 == second.getAuthType()) && Objects.equals(first.getUser(), second.getUser()) && Objects.equals(first.getPassword(), second.getPassword()) && Objects.equals(first.getProxyAddressAsString(), second.getProxyAddressAsString()) && xorEqualsInt(first.getProxyPort(), second.getProxyPort()) && xorEqualsProtocol(ApnSetting.getProtocolStringFromInt(first.getProtocol()), ApnSetting.getProtocolStringFromInt(second.getProtocol())) && xorEqualsProtocol(ApnSetting.getProtocolStringFromInt(first.getRoamingProtocol()), ApnSetting.getProtocolStringFromInt(second.getRoamingProtocol())) && first.isEnabled() == second.isEnabled() && first.getNetworkTypeBitmask() == second.getNetworkTypeBitmask() && first.getMtu() == second.getMtu() && xorEquals(firstMmsc, secondMmsc) && xorEquals(first.getMmsProxyAddressAsString(), second.getMmsProxyAddressAsString()) && xorEqualsInt(first.getMmsProxyPort(), second.getMmsProxyPort());
    }

    private boolean xorEqualsInt(int first, int second) {
        return first == -1 || second == -1 || Objects.equals(Integer.valueOf(first), Integer.valueOf(second));
    }

    private boolean xorEquals(String first, String second) {
        return Objects.equals(first, second) || TextUtils.isEmpty(first) || TextUtils.isEmpty(second);
    }

    private boolean xorEqualsProtocol(String first, String second) {
        return Objects.equals(first, second) || ("IPV4V6".equals(first) && ("IP".equals(second) || "IPV6".equals(second))) || (("IP".equals(first) && "IPV4V6".equals(second)) || ("IPV6".equals(first) && "IPV4V6".equals(second)));
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0072  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x0078  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x007e  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x0084  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x008a  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x0090  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0096  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x009c  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00a2  */
    /* JADX WARNING: Removed duplicated region for block: B:52:? A[RETURN, SYNTHETIC] */
    public void addCapAccordingToType(NetworkCapabilities result, String type) {
        char c;
        int hashCode = type.hashCode();
        if (hashCode != -1490587420) {
            if (hashCode != 3673178) {
                switch (hashCode) {
                    case 3023943:
                        if (type.equals("bip0")) {
                            c = 0;
                            break;
                        }
                        break;
                    case 3023944:
                        if (type.equals("bip1")) {
                            c = 1;
                            break;
                        }
                        break;
                    case 3023945:
                        if (type.equals("bip2")) {
                            c = 2;
                            break;
                        }
                        break;
                    case 3023946:
                        if (type.equals("bip3")) {
                            c = 3;
                            break;
                        }
                        break;
                    case 3023947:
                        if (type.equals("bip4")) {
                            c = 4;
                            break;
                        }
                        break;
                    case 3023948:
                        if (type.equals("bip5")) {
                            c = 5;
                            break;
                        }
                        break;
                    case 3023949:
                        if (type.equals("bip6")) {
                            c = 6;
                            break;
                        }
                        break;
                }
                switch (c) {
                    case 0:
                        result.addCapability(25);
                        return;
                    case 1:
                        result.addCapability(26);
                        return;
                    case 2:
                        result.addCapability(27);
                        return;
                    case 3:
                        result.addCapability(28);
                        return;
                    case 4:
                        result.addCapability(29);
                        return;
                    case 5:
                        result.addCapability(30);
                        return;
                    case 6:
                        result.addCapability(31);
                        return;
                    case 7:
                        result.addCapability(9);
                        return;
                    case '\b':
                        result.addCapability(32);
                        return;
                    default:
                        return;
                }
            } else if (type.equals("xcap")) {
                c = 7;
                switch (c) {
                }
            }
        } else if (type.equals("internaldefault")) {
            c = '\b';
            switch (c) {
            }
        }
        c = 65535;
        switch (c) {
        }
    }

    public String calTcpBufferSizesByPropName(String oldSizes, String tcpBufferSizePropName, PhoneExt phone) {
        String hwTcpBuffer;
        String result = oldSizes;
        String custTcpBuffer = SystemProperties.get(tcpBufferSizePropName);
        log("calTcpBufferSizesByPropName: custTcpBuffer = " + custTcpBuffer);
        if (custTcpBuffer != null && custTcpBuffer.length() > 0) {
            result = custTcpBuffer;
        }
        if (phone == null || (hwTcpBuffer = (String) HwCfgFilePolicy.getValue(tcpBufferSizePropName, phone.getPhoneId(), String.class)) == null || hwTcpBuffer.length() <= 0) {
            return result;
        }
        log("calTcpBufferSizesByPropName: custTcpBuffer = " + hwTcpBuffer);
        return hwTcpBuffer;
    }

    public void addCapForApnTypeAll(NetworkCapabilities result) {
        result.addCapability(25);
        result.addCapability(26);
        result.addCapability(27);
        result.addCapability(28);
        result.addCapability(29);
        result.addCapability(30);
        result.addCapability(31);
        result.addCapability(9);
        result.addCapability(32);
    }

    public int reportDataFailReason(int reason, ApnContextEx apnContext) {
        IHwCommBoosterServiceManager booster = HwFrameworkFactory.getHwCommBoosterServiceManager();
        if (booster == null) {
            loge("booster not avaliable");
            return -1;
        } else if (apnContext == null) {
            loge("apnContext is null");
            return -3;
        } else {
            String apnType = apnContext.getApnType();
            if (apnType == null) {
                return -3;
            }
            if (!apnType.startsWith("snssai")) {
                return 1;
            }
            String dnn = apnContext.getDnn();
            int pdeSessionType = apnContext.getPduSessionType();
            String snssai = apnContext.getSnssai();
            byte sscMode = apnContext.getSscMode();
            boolean isNrSliceFailReason = false;
            int[] iArr = NR_SLICE_DATA_FAIL_REASON;
            int length = iArr.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                } else if (reason == iArr[i]) {
                    isNrSliceFailReason = true;
                    break;
                } else {
                    i++;
                }
            }
            if (!isNrSliceFailReason) {
                return 1;
            }
            Context context = apnContext.getContext();
            if (context != null) {
                Intent intent = new Intent(ACTION_NETWORK_SLICE_LOST);
                intent.putExtra(BOOSTER_REPORT_DNN, dnn);
                intent.putExtra(BOOSTER_REPORT_PDUSESSIONTYPE, pdeSessionType);
                intent.putExtra(BOOSTER_REPORT_SNSSAI, snssai);
                intent.putExtra(BOOSTER_REPORT_SSCMODE, sscMode);
                context.sendBroadcast(intent);
            }
            Bundle data = new Bundle();
            data.putString(BOOSTER_REPORT_DNN, dnn);
            data.putInt(BOOSTER_REPORT_PDUSESSIONTYPE, pdeSessionType);
            data.putString(BOOSTER_REPORT_SNSSAI, snssai);
            data.putByte(BOOSTER_REPORT_SSCMODE, sscMode);
            data.putInt(BOOSTER_REPORT_FAIL_REASON, reason);
            log("reportDataFailReason, data = " + data);
            return booster.reportBoosterPara(PACKAGE_NAME, (int) REPORT_FAIL_REASON, data);
        }
    }

    private static void log(String msg) {
        RlogEx.i(TAG, msg);
    }

    private static void loge(String msg) {
        RlogEx.e(TAG, msg);
    }
}
