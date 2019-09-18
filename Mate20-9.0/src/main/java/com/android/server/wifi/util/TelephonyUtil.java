package com.android.server.wifi.util;

import android.net.wifi.WifiConfiguration;
import android.telephony.ImsiEncryptionInfo;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.wifi.HwWifiServiceFactory;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.hotspot2.anqp.Constants;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.HashMap;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class TelephonyUtil {
    public static final String DEFAULT_EAP_PREFIX = "\u0000";
    private static final HashMap<Integer, String> EAP_METHOD_PREFIX = new HashMap<>();
    private static final String IMSI_CIPHER_TRANSFORMATION = "RSA/ECB/OAEPwithSHA-256andMGF1Padding";
    public static final String TAG = "TelephonyUtil";
    private static final String THREE_GPP_NAI_REALM_FORMAT = "wlan.mnc%s.mcc%s.3gppnetwork.org";
    private static int mAssignedSubId = HwWifiServiceFactory.getHwTelphonyUtils().getDefault4GSlotId();

    public static class SimAuthRequestData {
        public String[] data;
        public int networkId;
        public int protocol;
        public String ssid;

        public SimAuthRequestData() {
        }

        public SimAuthRequestData(int networkId2, int protocol2, String ssid2, String[] data2) {
            this.networkId = networkId2;
            this.protocol = protocol2;
            this.ssid = ssid2;
            this.data = data2;
        }
    }

    public static class SimAuthResponseData {
        public String response;
        public String type;

        public SimAuthResponseData(String type2, String response2) {
            this.type = type2;
            this.response = response2;
        }
    }

    static {
        EAP_METHOD_PREFIX.put(5, "0");
        EAP_METHOD_PREFIX.put(4, "1");
        EAP_METHOD_PREFIX.put(6, "6");
    }

    public static Pair<String, String> getSimIdentity(TelephonyManager tm, TelephonyUtil telephonyUtil, WifiConfiguration config) {
        if (tm == null) {
            Log.e(TAG, "No valid TelephonyManager");
            return null;
        }
        int subId = getEapSubId(tm, config);
        Log.d(TAG, "get identity by sim-card from subId:" + subId);
        String imsi = tm.getSubscriberId(subId);
        String cdmaGsmImsi = HwWifiServiceFactory.getHwTelphonyUtils().getCdmaGsmImsi();
        if (cdmaGsmImsi != null && HwWifiServiceFactory.getHwTelphonyUtils().isCDMASimCard(subId)) {
            String[] cdmaGsmImsiArray = cdmaGsmImsi.split(",");
            if (2 == cdmaGsmImsiArray.length) {
                imsi = cdmaGsmImsiArray[1];
                Log.d(TAG, "cdma prefer USIM/GSM imsi");
            }
        }
        String mccMnc = "";
        if (5 == tm.getSimState(subId)) {
            mccMnc = tm.getSimOperator(subId);
        }
        try {
            ImsiEncryptionInfo imsiEncryptionInfo = tm.getCarrierInfoForImsiEncryption(2);
            String identity = buildIdentity(getSimMethodForConfig(config), imsi, mccMnc, false);
            if (identity == null) {
                Log.e(TAG, "Failed to build the identity");
                return null;
            }
            String encryptedIdentity = buildEncryptedIdentity(telephonyUtil, getSimMethodForConfig(config), imsi, mccMnc, imsiEncryptionInfo);
            if (encryptedIdentity == null) {
                encryptedIdentity = "";
            }
            return Pair.create(identity, encryptedIdentity);
        } catch (RuntimeException e) {
            Log.e(TAG, "Failed to get imsi encryption info: " + e.getMessage());
            return null;
        }
    }

    @VisibleForTesting
    public String encryptDataUsingPublicKey(PublicKey key, byte[] data) {
        try {
            Cipher cipher = Cipher.getInstance(IMSI_CIPHER_TRANSFORMATION);
            cipher.init(1, key);
            byte[] encryptedBytes = cipher.doFinal(data);
            return Base64.encodeToString(encryptedBytes, 0, encryptedBytes.length, 0);
        } catch (InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            Log.e(TAG, "Encryption failed: " + e.getMessage());
            return null;
        }
    }

    private static String buildEncryptedIdentity(TelephonyUtil telephonyUtil, int eapMethod, String imsi, String mccMnc, ImsiEncryptionInfo imsiEncryptionInfo) {
        if (imsiEncryptionInfo == null) {
            return null;
        }
        if (EAP_METHOD_PREFIX.get(Integer.valueOf(eapMethod)) == null) {
            return null;
        }
        String encryptedImsi = telephonyUtil.encryptDataUsingPublicKey(imsiEncryptionInfo.getPublicKey(), (prefix + imsi).getBytes());
        if (encryptedImsi == null) {
            Log.e(TAG, "Failed to encrypt IMSI");
            return null;
        }
        String encryptedIdentity = buildIdentity(eapMethod, encryptedImsi, mccMnc, true);
        if (imsiEncryptionInfo.getKeyIdentifier() != null) {
            encryptedIdentity = encryptedIdentity + "," + imsiEncryptionInfo.getKeyIdentifier();
        }
        return encryptedIdentity;
    }

    private static String buildIdentity(int eapMethod, String imsi, String mccMnc, boolean isEncrypted) {
        String mcc;
        String mnc;
        if (imsi == null || imsi.isEmpty()) {
            Log.e(TAG, "No IMSI or IMSI is null");
            return null;
        }
        if ((isEncrypted ? DEFAULT_EAP_PREFIX : EAP_METHOD_PREFIX.get(Integer.valueOf(eapMethod))) == null) {
            return null;
        }
        if (mccMnc == null || mccMnc.isEmpty()) {
            mcc = imsi.substring(0, 3);
            mnc = imsi.substring(3, 6);
        } else {
            mcc = mccMnc.substring(0, 3);
            mnc = mccMnc.substring(3);
            if (mnc.length() == 2) {
                mnc = "0" + mnc;
            }
        }
        String naiRealm = String.format(THREE_GPP_NAI_REALM_FORMAT, new Object[]{mnc, mcc});
        return prefix + imsi + "@" + naiRealm;
    }

    private static int getSimMethodForConfig(WifiConfiguration config) {
        int i = -1;
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
        if (isSimEapMethod(eapMethod)) {
            i = eapMethod;
        }
        return i;
    }

    public static boolean isSimConfig(WifiConfiguration config) {
        return getSimMethodForConfig(config) != -1;
    }

    public static boolean isSimEapMethod(int eapMethod) {
        return eapMethod == 4 || eapMethod == 5 || eapMethod == 6;
    }

    private static int parseHex(char ch) {
        if ('0' <= ch && ch <= '9') {
            return ch - '0';
        }
        if ('a' <= ch && ch <= 'f') {
            return (ch - 'a') + 10;
        }
        if ('A' <= ch && ch <= 'F') {
            return (ch - 'A') + 10;
        }
        throw new NumberFormatException("" + ch + " is not a valid hex digit");
    }

    private static byte[] parseHex(String hex) {
        if (hex == null) {
            return new byte[0];
        }
        if (hex.length() % 2 == 0) {
            int j = 1;
            byte[] result = new byte[((hex.length() / 2) + 1)];
            result[0] = (byte) (hex.length() / 2);
            int i = 0;
            while (i < hex.length()) {
                result[j] = (byte) (((parseHex(hex.charAt(i)) * 16) + parseHex(hex.charAt(i + 1))) & Constants.BYTE_MASK);
                i += 2;
                j++;
            }
            return result;
        }
        throw new NumberFormatException(hex + " is not a valid hex string");
    }

    private static String makeHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", new Object[]{Byte.valueOf(b)}));
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
        byte[] result = new byte[(array1.length + array2.length)];
        int index = 0;
        if (array1.length != 0) {
            int index2 = 0;
            for (byte b : array1) {
                result[index2] = b;
                index2++;
            }
            index = index2;
        }
        if (array2.length != 0) {
            for (byte b2 : array2) {
                result[index] = b2;
                index++;
            }
        }
        return result;
    }

    public static String getGsmSimAuthResponse(String[] requestData, TelephonyManager tm) {
        WifiConfiguration wifiConfiguration;
        String[] strArr = requestData;
        TelephonyManager telephonyManager = tm;
        WifiConfiguration wifiConfiguration2 = null;
        if (telephonyManager == null) {
            Log.e(TAG, "No valid TelephonyManager");
            return null;
        }
        StringBuilder sb = new StringBuilder();
        int length = strArr.length;
        int i = 0;
        int i2 = 0;
        while (i2 < length) {
            String challenge = strArr[i2];
            if (challenge == null || challenge.isEmpty()) {
                wifiConfiguration = wifiConfiguration2;
            } else {
                Log.d(TAG, "RAND = " + challenge);
                WifiConfiguration wifiConfiguration3 = wifiConfiguration2;
                try {
                    byte[] rand = parseHex(challenge);
                    String base64Challenge = Base64.encodeToString(rand, 2);
                    int subId = getEapSubId(telephonyManager, wifiConfiguration2);
                    Log.d(TAG, "get SIM_AUTH response by EAP-SIM method, sim-card from subId:" + subId);
                    String tmResponse = telephonyManager.getIccAuthentication(subId, 2, 128, base64Challenge);
                    if (tmResponse == null) {
                        tmResponse = telephonyManager.getIccAuthentication(subId, 1, 128, base64Challenge);
                    }
                    Log.v(TAG, "Raw Response - " + tmResponse);
                    if (tmResponse == null) {
                    } else if (tmResponse.length() <= 4) {
                        byte[] bArr = rand;
                    } else {
                        byte[] result = Base64.decode(tmResponse, i);
                        Log.v(TAG, "Hex Response -" + makeHex(result));
                        byte sresLen = result[i];
                        if (sresLen >= result.length) {
                            byte[] bArr2 = rand;
                        } else if (sresLen < 0) {
                            byte[] bArr3 = rand;
                        } else {
                            String sres = makeHex(result, 1, sresLen);
                            int kcOffset = 1 + sresLen;
                            if (kcOffset >= result.length) {
                                Log.e(TAG, "malfomed response - " + tmResponse);
                                return null;
                            }
                            byte kcLen = result[kcOffset];
                            byte[] bArr4 = rand;
                            if (kcOffset + kcLen > result.length) {
                                Log.e(TAG, "malfomed response - " + tmResponse);
                                return null;
                            }
                            String kc = makeHex(result, 1 + kcOffset, kcLen);
                            sb.append(":" + kc + ":" + sres);
                            Log.v(TAG, "kc:" + kc + " sres:" + sres);
                            wifiConfiguration = null;
                        }
                        Log.e(TAG, "malfomed response - " + tmResponse);
                        return null;
                    }
                    Log.e(TAG, "bad response - " + tmResponse);
                    return null;
                } catch (NumberFormatException e) {
                    wifiConfiguration = wifiConfiguration2;
                    NumberFormatException numberFormatException = e;
                    Log.e(TAG, "malformed challenge");
                }
            }
            i2++;
            wifiConfiguration2 = wifiConfiguration;
            strArr = requestData;
            telephonyManager = tm;
            i = 0;
        }
        return sb.toString();
    }

    public static SimAuthResponseData get3GAuthResponse(SimAuthRequestData requestData, TelephonyManager tm) {
        String response;
        String ik;
        int subId;
        SimAuthRequestData simAuthRequestData = requestData;
        TelephonyManager telephonyManager = tm;
        StringBuilder sb = new StringBuilder();
        byte[] rand = null;
        byte[] authn = null;
        String resType = WifiNative.SIM_AUTH_RESP_TYPE_UMTS_AUTH;
        if (simAuthRequestData.data.length == 2) {
            try {
                rand = parseHex(simAuthRequestData.data[0]);
                authn = parseHex(simAuthRequestData.data[1]);
            } catch (NumberFormatException e) {
                Log.e(TAG, "malformed challenge");
            }
        } else {
            Log.e(TAG, "malformed challenge");
        }
        String tmResponse = "";
        if (!(rand == null || authn == null)) {
            String base64Challenge = Base64.encodeToString(concatHex(rand, authn), 2);
            if (telephonyManager != null) {
                Log.d(TAG, "get SIM_AUTH response by EAP-AKA/AKA' method, sim-card from subId:" + subId);
                tmResponse = telephonyManager.getIccAuthentication(subId, 2, 129, base64Challenge);
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
            if (tag == -37) {
                Log.v(TAG, "successful 3G authentication ");
                byte resLen = result[1];
                String res = makeHex(result, 2, resLen);
                byte ckLen = result[resLen + 2];
                String ck = makeHex(result, resLen + 3, ckLen);
                sb.append(":" + ik + ":" + ck + ":" + res);
                StringBuilder sb2 = new StringBuilder();
                byte[] bArr = rand;
                sb2.append("ik:");
                sb2.append(ik);
                sb2.append("ck:");
                sb2.append(ck);
                sb2.append(" res:");
                sb2.append(res);
                Log.v(TAG, sb2.toString());
                goodReponse = true;
            } else {
                if (tag == -36) {
                    Log.e(TAG, "synchronisation failure");
                    String auts = makeHex(result, 2, result[1]);
                    sb.append(":" + auts);
                    Log.v(TAG, "auts:" + auts);
                    goodReponse = true;
                    resType = WifiNative.SIM_AUTH_RESP_TYPE_UMTS_AUTS;
                } else {
                    Log.e(TAG, "bad response - unknown tag = " + tag);
                }
            }
        }
        if (!goodReponse) {
            return null;
        }
        Log.v(TAG, "Supplicant Response -" + response);
        return new SimAuthResponseData(resType, response);
    }

    private static int getEapSubId(TelephonyManager tm, WifiConfiguration config) {
        if (config == null) {
            Log.d(TAG, "getEapSubId(): config is null, get subId=" + mAssignedSubId);
            return mAssignedSubId;
        }
        int subId = config.enterpriseConfig.getEapSubId();
        boolean isMultiSimEnabled = tm.isMultiSimEnabled();
        int sub1State = tm.getSimState(0);
        int sub2State = tm.getSimState(1);
        if (!isMultiSimEnabled || sub1State != 5 || sub2State != 5 || subId == Integer.MAX_VALUE) {
            subId = HwWifiServiceFactory.getHwTelphonyUtils().getDefault4GSlotId();
        }
        mAssignedSubId = subId;
        Log.d(TAG, "checkUseDefaultSubId: isMultiSimEnabled=" + isMultiSimEnabled + ", sub1State=" + sub1State + ", sub2State=" + sub2State + ", subId=" + subId + ", mAssignedSubId=" + mAssignedSubId);
        return subId;
    }
}
