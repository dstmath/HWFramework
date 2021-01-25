package com.android.internal.telephony;

import android.content.Context;
import android.content.Intent;
import android.database.DatabaseUtils;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.telephony.data.ApnSetting;
import android.text.TextUtils;
import android.util.ArraySet;
import com.huawei.android.net.NetworkCapabilitiesEx;
import com.huawei.android.net.TrafficStatsEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.telephony.RlogEx;
import com.huawei.android.telephony.data.ApnSettingEx;
import com.huawei.hwparttelephonyopt.BuildConfig;
import com.huawei.internal.telephony.PhoneExt;
import com.huawei.internal.telephony.dataconnection.ApnContextEx;
import com.huawei.internal.telephony.dataconnection.DataConnectionEx;
import com.huawei.internal.telephony.dataconnection.DcTrackerEx;
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
    private static final int INVALID_AUTH_TYPE = -1;
    private static final int LEN_ICCID = 19;
    private static final int LEN_IMEI = 14;
    private static final int LEN_IMSI = 15;
    private static final int LEN_MSN = 11;
    private static final int NORMAL_NO_NEED_TO_REPORT = 1;
    private static final int[] NR_SLICE_DATA_FAIL_REASON = {-1};
    private static final String PACKAGE_NAME = "com.android.internal.telephony";
    private static final int REPORT_FAIL_REASON = 805;
    private static final String SEPARATOR = ",";
    private static final int SWITCHING_STATE = 1;
    private static final int SWITCH_DONE = 2;
    private static final String TAG = "HwDataConnectionManagerImpl";
    private static final int UNSPECIFIED_INT = -1;
    private static HwDataConnectionManager sInstance = new HwDataConnectionManagerImpl();

    public static native String sbmcgmGenId(String str, String str2, String str3);

    public static native String sbmcgmGenPasswd(String str);

    static {
        if (SystemPropertiesEx.getBoolean("ro.config.sbmcgm", false)) {
            try {
                System.load(SystemPropertiesEx.get("ro.config.sbmjni_uri"));
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

    private static void log(String msg) {
        RlogEx.i(TAG, msg);
    }

    private static void loge(String msg) {
        RlogEx.e(TAG, msg);
    }

    public boolean needSetUserDataEnabled(boolean enabled) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        boolean z = true;
        try {
            IBinder connectivityServiceBinder = DataConnectionEx.getConnectivityManagerBinder();
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
            long temp = TrafficStatsEx.getTxPackets(iface);
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
            long temp = TrafficStatsEx.getRxPackets(iface);
            if (temp == 0) {
                log("getThisModemMobileRxPackets is 0");
            }
            total += temp;
        }
        return total;
    }

    private String[] getThisModemMobileIfaces(HashMap<String, Integer> ifacePhoneHashMap, int phoneId) {
        ArraySet<String> mobileIfaces = new ArraySet<>();
        String[] allActiveIfaces = TrafficStatsEx.getMobileIfacesEx();
        for (String iface : allActiveIfaces) {
            if (ifacePhoneHashMap.get(iface) == null || ifacePhoneHashMap.get(iface).equals(Integer.valueOf(phoneId))) {
                mobileIfaces.add(iface);
            }
        }
        return (String[]) mobileIfaces.toArray(new String[mobileIfaces.size()]);
    }

    public boolean getNamSwitcherForSoftbank() {
        return SystemPropertiesEx.getBoolean("ro.config.sbmcgm", false);
    }

    public boolean isSoftBankCard(PhoneExt phone) {
        if (phone == null) {
            return false;
        }
        String softbankPlmns = Settings.System.getString(phone.getContext().getContentResolver(), "hw_softbank_plmn");
        TelephonyManager tm = (TelephonyManager) phone.getContext().getSystemService("phone");
        String operator = tm != null ? tm.getSimOperator() : BuildConfig.FLAVOR;
        loge("sbnam:isSoftBankCard sbnam hw_softbank_plmn:" + softbankPlmns + " operator:" + operator);
        if (softbankPlmns != null) {
            String[] plmns = softbankPlmns.split(SEPARATOR);
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
        String line1Number;
        if (phone == null || (line1Number = phone.getLine1Number()) == null) {
            return false;
        }
        return !line1Number.isEmpty();
    }

    public HashMap<String, String> encryptApnInfoForSoftBank(PhoneExt phone, ApnSetting apnSetting) {
        TelephonyManager tm;
        if (phone == null || apnSetting == null) {
            return null;
        }
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
            if (msn != null && msn.length() > LEN_MSN) {
                msn = msn.substring(0, LEN_MSN);
            }
            String imei = tm.getDeviceId();
            if (imei != null && imei.length() > LEN_IMEI) {
                imei = imei.substring(0, LEN_IMEI);
            }
            String imsi = tm.getSubscriberId();
            if (imsi != null && imsi.length() > 15) {
                imsi = imsi.substring(0, 15);
            }
            String iccid = tm.getSimSerialNumber();
            if (iccid != null && iccid.length() > LEN_ICCID) {
                iccid = iccid.substring(0, LEN_ICCID);
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
        int switchState = SystemPropertiesEx.getInt("persist.sys.smart_switch_state", 0);
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
                String[] plmns = plmnsConfig.split(SEPARATOR);
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
            resultTypes.addAll(Arrays.asList(ApnSettingEx.getApnTypesStringFromBitmask(currentApnSetting.getApnTypeBitmask()).split(SEPARATOR)));
            if (allApnSettings == null) {
                return (String[]) resultTypes.toArray(new String[0]);
            }
            int listSize = allApnSettings.size();
            for (int i = 0; i < listSize; i++) {
                ApnSetting apn = allApnSettings.get(i);
                if (!currentApnSetting.equals(apn) && apnSettingsSimilar(currentApnSetting, apn)) {
                    String[] types = ApnSettingEx.getApnTypesStringFromBitmask(apn.getApnTypeBitmask()).split(SEPARATOR);
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
            return ApnSettingEx.getApnTypesStringFromBitmask(currentApnSetting.getApnTypeBitmask()).split(SEPARATOR);
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
        ApnSettingEx firstEx = new ApnSettingEx();
        firstEx.setApnSetting(first);
        ApnSettingEx secondEx = new ApnSettingEx();
        firstEx.setApnSetting(second);
        return Objects.equals(first.getApnName(), second.getApnName()) && (first.getAuthType() == second.getAuthType() || first.getAuthType() == -1 || second.getAuthType() == -1) && Objects.equals(first.getUser(), second.getUser()) && Objects.equals(first.getPassword(), second.getPassword()) && Objects.equals(first.getProxyAddressAsString(), second.getProxyAddressAsString()) && xorEqualsInt(first.getProxyPort(), second.getProxyPort()) && xorEqualsProtocol(ApnSettingEx.getProtocolStringFromInt(first.getProtocol()), ApnSettingEx.getProtocolStringFromInt(second.getProtocol())) && xorEqualsProtocol(ApnSettingEx.getProtocolStringFromInt(first.getRoamingProtocol()), ApnSettingEx.getProtocolStringFromInt(second.getRoamingProtocol())) && first.isEnabled() == second.isEnabled() && first.getNetworkTypeBitmask() == second.getNetworkTypeBitmask() && firstEx.getMtu() == secondEx.getMtu() && xorEquals(firstMmsc, secondMmsc) && xorEquals(first.getMmsProxyAddressAsString(), second.getMmsProxyAddressAsString()) && xorEqualsInt(first.getMmsProxyPort(), second.getMmsProxyPort());
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

    public void addCapAccordingToType(NetworkCapabilities resultOri, String type) {
        if (resultOri != null && type != null) {
            NetworkCapabilitiesEx result = new NetworkCapabilitiesEx(resultOri);
            char c = 65535;
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
                } else if (type.equals("xcap")) {
                    c = 7;
                }
            } else if (type.equals("internaldefault")) {
                c = '\b';
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
                case HwCarrierConfigCardManager.HW_CARRIER_FILE_SPN /* 7 */:
                    result.addCapability(9);
                    return;
                case '\b':
                    result.addCapability(32);
                    return;
                default:
                    return;
            }
        }
    }

    public String calTcpBufferSizesByPropName(String oldSizes, String tcpBufferSizePropName, PhoneExt phone) {
        String hwTcpBuffer;
        String result = oldSizes;
        String custTcpBuffer = SystemPropertiesEx.get(tcpBufferSizePropName);
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

    public void addCapForApnTypeAll(NetworkCapabilities resultOri) {
        NetworkCapabilitiesEx result = new NetworkCapabilitiesEx(resultOri);
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
        if (apnContext == null) {
            loge("apnContext is null");
            return -3;
        }
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
        return DcTrackerEx.reportBoosterPara(PACKAGE_NAME, (int) REPORT_FAIL_REASON, data);
    }
}
