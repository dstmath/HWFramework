package com.android.server.wifi.util;

import android.net.wifi.WifiConfiguration;
import android.telephony.HwTelephonyManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.android.server.wifi.HwWifiCHRStateManager;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.hotspot2.anqp.Constants;
import com.huawei.android.os.SystemPropertiesEx;

public class TelephonyUtil {
    private static final boolean IS_HI3650_PLATFORM = "hi3650".equalsIgnoreCase(SystemPropertiesEx.get("ro.board.platform"));
    private static final int MIN_MCCMNC_LENGTH = 5;
    public static final String TAG = "TelephonyUtil";
    private static int mAssignedSubId = HwTelephonyManager.getDefault().getDefault4GSlotId();

    public static class SimAuthRequestData {
        public String[] data;
        public int networkId;
        public int protocol;
        public String ssid;

        public SimAuthRequestData(int networkId, int protocol, String ssid, String[] data) {
            this.networkId = networkId;
            this.protocol = protocol;
            this.ssid = ssid;
            this.data = data;
        }
    }

    public static class SimAuthResponseData {
        public String response;
        public String type;

        public SimAuthResponseData(String type, String response) {
            this.type = type;
            this.response = response;
        }
    }

    public static String getSimIdentity(TelephonyManager tm, WifiConfiguration config) {
        if (tm == null) {
            Log.e(TAG, "No valid TelephonyManager");
            return null;
        }
        HwTelephonyManager hwTelephonyManager = HwTelephonyManager.getDefault();
        int subId = getEapSubId(tm, hwTelephonyManager, config);
        Log.d(TAG, "get identity by sim-card from subId:" + subId);
        String imsi = tm.getSubscriberId(subId);
        String cdmaGsmImsi = hwTelephonyManager.getCdmaGsmImsi();
        if (cdmaGsmImsi != null && hwTelephonyManager.isCDMASimCard(subId)) {
            String[] cdmaGsmImsiArray = cdmaGsmImsi.split(",");
            if (2 == cdmaGsmImsiArray.length) {
                imsi = cdmaGsmImsiArray[1];
                Log.d(TAG, "cdma prefer USIM/GSM imsi");
            }
        }
        String mccMnc = "";
        if (5 == tm.getSimState(subId)) {
            mccMnc = tm.getSimOperator(subId);
            if (IS_HI3650_PLATFORM && hwTelephonyManager.isCTSimCard(subId) && (TextUtils.isEmpty(imsi) ^ 1) != 0 && imsi.length() > 5) {
                mccMnc = imsi.substring(0, 5);
                Log.d(TAG, "cdma prefer USIM/GSM mccmnc");
            }
        }
        return buildIdentity(getSimMethodForConfig(config), imsi, mccMnc);
    }

    private static String buildIdentity(int eapMethod, String imsi, String mccMnc) {
        if (imsi == null || imsi.isEmpty()) {
            Log.e(TAG, "No IMSI or IMSI is null");
            return null;
        }
        String prefix;
        String mcc;
        String mnc;
        if (eapMethod == 4) {
            prefix = "1";
        } else if (eapMethod == 5) {
            prefix = HwWifiCHRStateManager.TYPE_AP_VENDOR;
        } else if (eapMethod == 6) {
            prefix = "6";
        } else {
            Log.e(TAG, "Invalid EAP method");
            return null;
        }
        if (mccMnc == null || (mccMnc.isEmpty() ^ 1) == 0) {
            mcc = imsi.substring(0, 3);
            mnc = imsi.substring(3, 6);
        } else {
            mcc = mccMnc.substring(0, 3);
            mnc = mccMnc.substring(3);
            if (mnc.length() == 2) {
                mnc = HwWifiCHRStateManager.TYPE_AP_VENDOR + mnc;
            }
        }
        return prefix + imsi + "@wlan.mnc" + mnc + ".mcc" + mcc + ".3gppnetwork.org";
    }

    private static int getSimMethodForConfig(WifiConfiguration config) {
        if (config == null || config.enterpriseConfig == null) {
            return -1;
        }
        int eapMethod = config.enterpriseConfig.getEapMethod();
        if (eapMethod == 0) {
            switch (config.enterpriseConfig.getPhase2Method()) {
                case 5:
                    eapMethod = 4;
                    break;
                case 6:
                    eapMethod = 5;
                    break;
                case 7:
                    eapMethod = 6;
                    break;
            }
        }
        if (!isSimEapMethod(eapMethod)) {
            eapMethod = -1;
        }
        return eapMethod;
    }

    public static boolean isSimConfig(WifiConfiguration config) {
        return getSimMethodForConfig(config) != -1;
    }

    public static boolean isSimEapMethod(int eapMethod) {
        if (eapMethod == 4 || eapMethod == 5 || eapMethod == 6) {
            return true;
        }
        return false;
    }

    private static int parseHex(char ch) {
        if ('0' <= ch && ch <= '9') {
            return ch - 48;
        }
        if ('a' <= ch && ch <= 'f') {
            return (ch - 97) + 10;
        }
        if ('A' <= ch && ch <= 'F') {
            return (ch - 65) + 10;
        }
        throw new NumberFormatException("" + ch + " is not a valid hex digit");
    }

    private static byte[] parseHex(String hex) {
        if (hex == null) {
            return new byte[0];
        }
        if (hex.length() % 2 != 0) {
            throw new NumberFormatException(hex + " is not a valid hex string");
        }
        byte[] result = new byte[((hex.length() / 2) + 1)];
        result[0] = (byte) (hex.length() / 2);
        int i = 0;
        int j = 1;
        while (i < hex.length()) {
            result[j] = (byte) (((parseHex(hex.charAt(i)) * 16) + parseHex(hex.charAt(i + 1))) & Constants.BYTE_MASK);
            i += 2;
            j++;
        }
        return result;
    }

    private static String makeHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        int length = bytes.length;
        for (int i = 0; i < length; i++) {
            sb.append(String.format("%02x", new Object[]{Byte.valueOf(bytes[i])}));
        }
        return sb.toString();
    }

    private static String makeHex(byte[] bytes, int from, int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(String.format("%02x", new Object[]{Byte.valueOf(bytes[from + i])}));
        }
        return sb.toString();
    }

    private static byte[] concatHex(byte[] array1, byte[] array2) {
        int length;
        int i = 0;
        byte[] result = new byte[(array1.length + array2.length)];
        int index = 0;
        if (array1.length != 0) {
            for (byte b : array1) {
                result[index] = b;
                index++;
            }
        }
        if (array2.length != 0) {
            length = array2.length;
            while (i < length) {
                result[index] = array2[i];
                index++;
                i++;
            }
        }
        return result;
    }

    public static String getGsmSimAuthResponse(String[] requestData, TelephonyManager tm) {
        if (tm == null) {
            Log.e(TAG, "No valid TelephonyManager");
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (String challenge : requestData) {
            if (!(challenge == null || challenge.isEmpty())) {
                Log.d(TAG, "RAND = " + challenge);
                try {
                    String base64Challenge = Base64.encodeToString(parseHex(challenge), 2);
                    int subId = getEapSubId(tm, HwTelephonyManager.getDefault(), null);
                    Log.d(TAG, "get SIM_AUTH response by EAP-SIM method, sim-card from subId:" + subId);
                    String tmResponse = tm.getIccAuthentication(subId, 2, 128, base64Challenge);
                    if (tmResponse == null) {
                        tmResponse = tm.getIccAuthentication(subId, 1, 128, base64Challenge);
                    }
                    Log.v(TAG, "Raw Response - " + tmResponse);
                    if (tmResponse == null || tmResponse.length() <= 4) {
                        Log.e(TAG, "bad response - " + tmResponse);
                        return null;
                    }
                    byte[] result = Base64.decode(tmResponse, 0);
                    Log.v(TAG, "Hex Response -" + makeHex(result));
                    int sresLen = result[0];
                    if (sresLen >= result.length || sresLen < 0) {
                        Log.e(TAG, "malfomed response - " + tmResponse);
                        return null;
                    }
                    String sres = makeHex(result, 1, sresLen);
                    int kcOffset = sresLen + 1;
                    if (kcOffset >= result.length) {
                        Log.e(TAG, "malfomed response - " + tmResponse);
                        return null;
                    }
                    int kcLen = result[kcOffset];
                    if (kcOffset + kcLen > result.length) {
                        Log.e(TAG, "malfomed response - " + tmResponse);
                        return null;
                    }
                    String kc = makeHex(result, kcOffset + 1, kcLen);
                    sb.append(":").append(kc).append(":").append(sres);
                    Log.v(TAG, "kc:" + kc + " sres:" + sres);
                } catch (NumberFormatException e) {
                    Log.e(TAG, "malformed challenge");
                }
            }
        }
        return sb.toString();
    }

    public static SimAuthResponseData get3GAuthResponse(SimAuthRequestData requestData, TelephonyManager tm) {
        StringBuilder sb = new StringBuilder();
        byte[] rand = null;
        byte[] authn = null;
        String resType = WifiNative.SIM_AUTH_RESP_TYPE_UMTS_AUTH;
        if (requestData.data.length == 2) {
            try {
                rand = parseHex(requestData.data[0]);
                authn = parseHex(requestData.data[1]);
            } catch (NumberFormatException e) {
                Log.e(TAG, "malformed challenge");
            }
        } else {
            Log.e(TAG, "malformed challenge");
        }
        String tmResponse = "";
        if (!(rand == null || authn == null)) {
            String base64Challenge = Base64.encodeToString(concatHex(rand, authn), 2);
            if (tm != null) {
                int subId = getEapSubId(tm, HwTelephonyManager.getDefault(), null);
                Log.d(TAG, "get SIM_AUTH response by EAP-AKA/AKA' method, sim-card from subId:" + subId);
                tmResponse = tm.getIccAuthentication(subId, 2, 129, base64Challenge);
                Log.v(TAG, "Raw Response - " + tmResponse);
            } else {
                Log.e(TAG, "No valid TelephonyManager");
            }
        }
        boolean goodReponse = false;
        if (tmResponse == null || tmResponse.length() <= 4) {
            Log.e(TAG, "bad response - " + tmResponse);
        } else {
            byte[] result = Base64.decode(tmResponse, 0);
            Log.e(TAG, "Hex Response - " + makeHex(result));
            byte tag = result[0];
            if (tag == (byte) -37) {
                Log.v(TAG, "successful 3G authentication ");
                int resLen = result[1];
                String res = makeHex(result, 2, resLen);
                int ckLen = result[resLen + 2];
                String ck = makeHex(result, resLen + 3, ckLen);
                String ik = makeHex(result, (resLen + ckLen) + 4, result[(resLen + ckLen) + 3]);
                sb.append(":").append(ik).append(":").append(ck).append(":").append(res);
                Log.v(TAG, "ik:" + ik + "ck:" + ck + " res:" + res);
                goodReponse = true;
            } else if (tag == (byte) -36) {
                Log.e(TAG, "synchronisation failure");
                String auts = makeHex(result, 2, result[1]);
                resType = WifiNative.SIM_AUTH_RESP_TYPE_UMTS_AUTS;
                sb.append(":").append(auts);
                Log.v(TAG, "auts:" + auts);
                goodReponse = true;
            } else {
                Log.e(TAG, "bad response - unknown tag = " + tag);
            }
        }
        if (!goodReponse) {
            return null;
        }
        String response = sb.toString();
        Log.v(TAG, "Supplicant Response -" + response);
        return new SimAuthResponseData(resType, response);
    }

    private static int getEapSubId(TelephonyManager tm, HwTelephonyManager hwTelephonyManager, WifiConfiguration config) {
        int i = 1;
        if (config == null) {
            Log.d(TAG, "getEapSubId(): config is null, get subId=" + mAssignedSubId);
            return mAssignedSubId;
        }
        int subId = config.enterpriseConfig.getEapSubId();
        boolean isMultiSimEnabled = tm.isMultiSimEnabled();
        int sub1State = tm.getSimState(0);
        int sub2State = tm.getSimState(1);
        if (!isMultiSimEnabled || sub1State != 5) {
            i = 0;
        } else if (sub2State != 5) {
            i = 0;
        }
        if (i == 0 || subId == Integer.MAX_VALUE) {
            subId = hwTelephonyManager.getDefault4GSlotId();
        }
        mAssignedSubId = subId;
        Log.d(TAG, "checkUseDefaultSubId: isMultiSimEnabled=" + isMultiSimEnabled + ", sub1State=" + sub1State + ", sub2State=" + sub2State + ", subId=" + subId + ", mAssignedSubId=" + mAssignedSubId);
        return subId;
    }
}
