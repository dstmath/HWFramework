package com.android.internal.telephony;

import android.database.DatabaseUtils;
import android.net.IConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.TrafficStats;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.provider.Settings;
import android.telephony.Rlog;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.ArraySet;
import com.android.internal.telephony.dataconnection.AbstractDcTrackerBase;
import com.android.internal.telephony.dataconnection.ApnSetting;
import com.android.internal.telephony.dataconnection.DcTracker;
import com.android.internal.telephony.dataconnection.HwDcTrackerBaseReference;
import com.android.internal.telephony.fullnetwork.HwFullNetworkConstants;
import huawei.cust.HwCfgFilePolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

public class HwDataConnectionManagerImpl implements HwDataConnectionManager {
    private static int CONNECTIVITY_SERVICE_NEED_SET_USER_DATA = HwFullNetworkConstants.EVENT_CHIP_HISI1_BASE;
    private static final int DEFAULT_VALUE_STATE = 0;
    private static final int SWITCHING_STATE = 1;
    private static final int SWITCH_DONE = 2;
    private static final String TAG = "HwDataConnectionManagerImpl";
    private static HwDataConnectionManager mInstance = new HwDataConnectionManagerImpl();

    public static native String sbmcgmGenId(String str, String str2, String str3);

    public static native String sbmcgmGenPasswd(String str);

    static {
        if (SystemProperties.getBoolean("ro.config.sbmcgm", false)) {
            try {
                System.load(SystemProperties.get("ro.config.sbmjni_uri"));
            } catch (UnsatisfiedLinkError e) {
                Rlog.e("SBM", "sbnam load sbm jni fail:", e);
            }
        }
    }

    public AbstractDcTrackerBase.DcTrackerBaseReference createHwDcTrackerBaseReference(AbstractDcTrackerBase dcTrackerBase) {
        return new HwDcTrackerBaseReference((DcTracker) dcTrackerBase);
    }

    public static HwDataConnectionManager getDefault() {
        return mInstance;
    }

    public boolean needSetUserDataEnabled(boolean enabled) {
        int result;
        IConnectivityManager cm = IConnectivityManager.Stub.asInterface(ServiceManager.getService("connectivity"));
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        boolean z = true;
        try {
            IBinder connectivityServiceBinder = cm.asBinder();
            if (connectivityServiceBinder != null) {
                data.writeInterfaceToken("android.net.IConnectivityManager");
                data.writeInt(enabled);
                connectivityServiceBinder.transact(CONNECTIVITY_SERVICE_NEED_SET_USER_DATA + 1, data, reply, 0);
            }
            DatabaseUtils.readExceptionFromParcel(reply);
            Rlog.d("HwDataConnectionManager", "needSetUserDataEnabled result = " + result);
            if (result != 1) {
                z = false;
            }
            return z;
        } catch (RemoteException localRemoteException) {
            localRemoteException.printStackTrace();
            return true;
        } finally {
            reply.recycle();
            data.recycle();
        }
    }

    public long getThisModemMobileTxPackets(HashMap<String, Integer> mIfacePhoneHashMap, int phoneId) {
        long total = 0;
        for (String iface : getThisModemMobileIfaces(mIfacePhoneHashMap, phoneId)) {
            long temp = TrafficStats.getTxPackets(iface);
            if (temp == 0) {
                Rlog.d("HwDataConnectionManager", "getThisModemMobileTxPackets is 0");
            }
            total += temp;
        }
        return total;
    }

    public long getThisModemMobileRxPackets(HashMap<String, Integer> mIfacePhoneHashMap, int phoneId) {
        long total = 0;
        for (String iface : getThisModemMobileIfaces(mIfacePhoneHashMap, phoneId)) {
            long temp = TrafficStats.getRxPackets(iface);
            if (temp == 0) {
                Rlog.d("HwDataConnectionManager", "getThisModemMobileRxPackets is 0");
            }
            total += temp;
        }
        return total;
    }

    private String[] getThisModemMobileIfaces(HashMap<String, Integer> mIfacePhoneHashMap, int phoneId) {
        ArraySet<String> mobileIfaces = new ArraySet<>();
        for (String iface : TrafficStats.getMobileIfacesEx()) {
            if (mIfacePhoneHashMap.get(iface) == null || mIfacePhoneHashMap.get(iface).equals(Integer.valueOf(phoneId))) {
                mobileIfaces.add(iface);
            }
        }
        return (String[]) mobileIfaces.toArray(new String[mobileIfaces.size()]);
    }

    public boolean getNamSwitcherForSoftbank() {
        return SystemProperties.getBoolean("ro.config.sbmcgm", false);
    }

    public boolean isSoftBankCard(Phone phone) {
        String operator;
        String softbankPlmns = Settings.System.getString(phone.getContext().getContentResolver(), "hw_softbank_plmn");
        TelephonyManager tm = (TelephonyManager) phone.getContext().getSystemService("phone");
        Rlog.e("HwDataConnectionManager", "sbnam:isSoftBankCard sbnam hw_softbank_plmn:" + softbankPlmns + " operator:" + operator);
        if (softbankPlmns != null) {
            String[] plmns = softbankPlmns.split(",");
            int length = plmns.length;
            int i = 0;
            while (i < length) {
                String plmn = plmns[i];
                if (plmn == null || !plmn.equals(operator)) {
                    i++;
                } else {
                    Rlog.e("HwDataConnectionManager", "sbnam:isSoftBankCard sbnam find softbank card " + operator);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isValidMsisdn(Phone phone) {
        String line1Number = phone.getLine1Number();
        if (line1Number != null) {
            return !line1Number.isEmpty();
        }
        return false;
    }

    public HashMap<String, String> encryptApnInfoForSoftBank(Phone phone, ApnSetting apnSetting) {
        ApnSetting apnSetting2 = apnSetting;
        String sUsername = apnSetting2.user;
        String sPassword = apnSetting2.password;
        Rlog.e("HwDataConnectionManager", "softbanknam: before encryption");
        if (!TextUtils.isEmpty(sUsername) || !TextUtils.isEmpty(sPassword)) {
            return null;
        }
        TelephonyManager tm = (TelephonyManager) phone.getContext().getSystemService("phone");
        if (tm == null) {
            return null;
        }
        if (!isSoftBankCard(phone)) {
            Rlog.e("HwDataConnectionManager", "softbanknam: not softbank card");
            return null;
        } else if (!isValidMsisdn(phone)) {
            Rlog.e("HwDataConnectionManager", "softbanknam: no msisdn softbank card");
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
            Rlog.e("HwDataConnectionManager", "softbanknam: after encryption finish");
            return userInfo;
        }
    }

    public boolean isDeactivatingSlaveData() {
        HwPhoneService service = HwPhoneService.getInstance();
        if (service == null) {
            return false;
        }
        return service.isDeactivatingSlaveData();
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

    private boolean enableCompatibleSimilarApnSettings(Phone phone, String operator) {
        if (!HuaweiTelephonyConfigs.isQcomPlatform() && phone != null) {
            boolean similarApnState = false;
            boolean hasHwCfgConfig = false;
            Boolean similarApnSign = (Boolean) HwCfgFilePolicy.getValue("compatible_apn_switch", SubscriptionManager.getSlotIndex(phone.getSubId()), Boolean.class);
            if (similarApnSign != null) {
                hasHwCfgConfig = true;
                similarApnState = similarApnSign.booleanValue();
            }
            if (similarApnState) {
                Rlog.d(TAG, "enableCompatibleSimilarApnSettings: similarApnSign=" + similarApnSign);
                return true;
            } else if (!hasHwCfgConfig || similarApnState) {
                String plmnsConfig = Settings.System.getString(phone.getContext().getContentResolver(), "compatible_apn_plmn");
                if (TextUtils.isEmpty(plmnsConfig)) {
                    return false;
                }
                String[] plmns = plmnsConfig.split(",");
                int length = plmns.length;
                int i = 0;
                while (i < length) {
                    String plmn = plmns[i];
                    if (TextUtils.isEmpty(plmn) || !plmn.equals(operator)) {
                        i++;
                    } else {
                        Rlog.d(TAG, "enableCompatibleSimilarApnSettings: operator: " + operator);
                        return true;
                    }
                }
            } else {
                Rlog.d(TAG, "enableCompatibleSimilarApnSettings: similarApnSign=" + similarApnSign);
                return false;
            }
        }
        return false;
    }

    public String[] getCompatibleSimilarApnSettingsTypes(Phone phone, String operator, ApnSetting currentApnSetting, ArrayList<ApnSetting> allApnSettings) {
        boolean apnSimilar;
        if (!enableCompatibleSimilarApnSettings(phone, operator)) {
            return currentApnSetting.types;
        }
        ArrayList<String> resultTypes = new ArrayList<>();
        if (currentApnSetting == null) {
            return (String[]) resultTypes.toArray(new String[0]);
        }
        resultTypes.addAll(Arrays.asList(currentApnSetting.types));
        if (allApnSettings == null) {
            return (String[]) resultTypes.toArray(new String[0]);
        }
        int listSize = allApnSettings.size();
        for (int i = 0; i < listSize; i++) {
            ApnSetting apn = allApnSettings.get(i);
            if (!currentApnSetting.equals(apn)) {
                if (HuaweiTelephonyConfigs.isMTKPlatform()) {
                    apnSimilar = getApnSettingsSimilar(currentApnSetting, apn);
                } else {
                    apnSimilar = apnSettingsSimilar(currentApnSetting, apn);
                }
                if (apnSimilar) {
                    for (String type : apn.types) {
                        if (!resultTypes.contains(type)) {
                            resultTypes.add(type);
                        }
                    }
                }
            }
        }
        Rlog.d(TAG, "getCompatibleSimilarApnSettingsTypes:  resultTypes " + resultTypes);
        return (String[]) resultTypes.toArray(new String[0]);
    }

    public boolean getApnSettingsSimilar(ApnSetting first, ApnSetting second) {
        if (Objects.equals(first.apn, second.apn) && xorEqualsProtocol(first.protocol, second.protocol) && xorEqualsProtocol(first.roamingProtocol, second.roamingProtocol) && first.carrierEnabled == second.carrierEnabled && xorEquals(first.mmsc, second.mmsc) && xorEquals(first.mmsProxy, second.mmsProxy) && xorEquals(first.mmsPort, second.mmsPort)) {
            return true;
        }
        return false;
    }

    private boolean apnSettingsSimilar(ApnSetting first, ApnSetting second) {
        return Objects.equals(first.apn, second.apn) && (first.authType == second.authType || -1 == first.authType || -1 == second.authType) && Objects.equals(first.user, second.user) && Objects.equals(first.password, second.password) && Objects.equals(first.proxy, second.proxy) && Objects.equals(first.port, second.port) && xorEqualsProtocol(first.protocol, second.protocol) && xorEqualsProtocol(first.roamingProtocol, second.roamingProtocol) && first.carrierEnabled == second.carrierEnabled && first.bearerBitmask == second.bearerBitmask && first.mtu == second.mtu && xorEquals(first.mmsc, second.mmsc) && xorEquals(first.mmsProxy, second.mmsProxy) && xorEquals(first.mmsPort, second.mmsPort);
    }

    private boolean xorEquals(String first, String second) {
        return Objects.equals(first, second) || TextUtils.isEmpty(first) || TextUtils.isEmpty(second);
    }

    private boolean xorEqualsProtocol(String first, String second) {
        return Objects.equals(first, second) || ("IPV4V6".equals(first) && ("IP".equals(second) || "IPV6".equals(second))) || (("IP".equals(first) && "IPV4V6".equals(second)) || ("IPV6".equals(first) && "IPV4V6".equals(second)));
    }

    /* JADX WARNING: Can't fix incorrect switch cases order */
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
        if (hashCode == -1490587420) {
            if (type.equals("internaldefault")) {
                c = 8;
                switch (c) {
                    case 0:
                        break;
                    case 1:
                        break;
                    case 2:
                        break;
                    case 3:
                        break;
                    case 4:
                        break;
                    case 5:
                        break;
                    case 6:
                        break;
                    case 7:
                        break;
                    case 8:
                        break;
                }
            }
        } else if (hashCode != 3673178) {
            switch (hashCode) {
                case 3023943:
                    if (type.equals("bip0")) {
                        c = 0;
                        break;
                    }
                case 3023944:
                    if (type.equals("bip1")) {
                        c = 1;
                        break;
                    }
                case 3023945:
                    if (type.equals("bip2")) {
                        c = 2;
                        break;
                    }
                case 3023946:
                    if (type.equals("bip3")) {
                        c = 3;
                        break;
                    }
                case 3023947:
                    if (type.equals("bip4")) {
                        c = 4;
                        break;
                    }
                case 3023948:
                    if (type.equals("bip5")) {
                        c = 5;
                        break;
                    }
                case 3023949:
                    if (type.equals("bip6")) {
                        c = 6;
                        break;
                    }
            }
        } else if (type.equals("xcap")) {
            c = 7;
            switch (c) {
                case 0:
                    result.addCapability(23);
                    return;
                case 1:
                    result.addCapability(24);
                    return;
                case 2:
                    result.addCapability(25);
                    return;
                case 3:
                    result.addCapability(26);
                    return;
                case 4:
                    result.addCapability(27);
                    return;
                case 5:
                    result.addCapability(28);
                    return;
                case 6:
                    result.addCapability(29);
                    return;
                case 7:
                    result.addCapability(9);
                    return;
                case 8:
                    result.addCapability(30);
                    return;
                default:
                    return;
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
            case 3:
                break;
            case 4:
                break;
            case 5:
                break;
            case 6:
                break;
            case 7:
                break;
            case 8:
                break;
        }
    }

    public String calTcpBufferSizesByPropName(String oldSizes, String tcpBufferSizePropName, Phone phone) {
        String result = oldSizes;
        String custTcpBuffer = SystemProperties.get(tcpBufferSizePropName);
        Rlog.d(TAG, "calTcpBufferSizesByPropName: custTcpBuffer = " + custTcpBuffer);
        if (custTcpBuffer != null && custTcpBuffer.length() > 0) {
            result = custTcpBuffer;
        }
        if (phone == null) {
            return result;
        }
        String hwTcpBuffer = (String) HwCfgFilePolicy.getValue(tcpBufferSizePropName, SubscriptionManager.getSlotIndex(phone.getPhoneId()), String.class);
        if (hwTcpBuffer == null || hwTcpBuffer.length() <= 0) {
            return result;
        }
        String result2 = hwTcpBuffer;
        Rlog.d(TAG, "calTcpBufferSizesByPropName: custTcpBuffer = " + result2);
        return result2;
    }

    public void addCapForApnTypeAll(NetworkCapabilities result) {
        result.addCapability(23);
        result.addCapability(24);
        result.addCapability(25);
        result.addCapability(26);
        result.addCapability(27);
        result.addCapability(28);
        result.addCapability(29);
        result.addCapability(9);
        result.addCapability(30);
    }
}
