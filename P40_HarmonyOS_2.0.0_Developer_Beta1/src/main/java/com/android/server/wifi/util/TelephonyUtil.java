package com.android.server.wifi.util;

import android.net.wifi.WifiConfiguration;
import android.telephony.ImsiEncryptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.util.Pair;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.wifi.CarrierNetworkConfig;
import com.android.server.wifi.HwWifiServiceFactory;
import com.android.server.wifi.WifiNative;
import com.android.server.wifi.hotspot2.anqp.Constants;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.HashMap;
import javax.annotation.Nonnull;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class TelephonyUtil {
    public static final String ANONYMOUS_IDENTITY = "anonymous";
    public static final int CARRIER_INVALID_TYPE = -1;
    public static final int CARRIER_MNO_TYPE = 0;
    public static final int CARRIER_MVNO_TYPE = 1;
    public static final String DEFAULT_EAP_PREFIX = "\u0000";
    private static final HashMap<Integer, String> EAP_METHOD_PREFIX = new HashMap<>();
    private static final String IMSI_CIPHER_TRANSFORMATION = "RSA/ECB/OAEPwithSHA-256andMGF1Padding";
    private static final int KC_LEN = 8;
    private static final int PHONE_IMSI_NUMBER = 2;
    private static final int SRES_LEN = 4;
    private static final int START_KC_POS = 4;
    private static final int START_SRES_POS = 0;
    public static final String TAG = "TelephonyUtil";
    public static final String THREE_GPP_NAI_REALM_FORMAT = "wlan.mnc%s.mcc%s.3gppnetwork.org";
    private static int sAssignedSubId = SubscriptionManager.getDefaultDataSubscriptionId();

    static {
        EAP_METHOD_PREFIX.put(5, "0");
        EAP_METHOD_PREFIX.put(4, "1");
        EAP_METHOD_PREFIX.put(6, "6");
    }

    public static Pair<String, String> getSimIdentity(TelephonyManager tm, TelephonyUtil telephonyUtil, WifiConfiguration config, CarrierNetworkConfig carrierNetworkConfig) {
        if (tm == null) {
            Log.e(TAG, "No valid TelephonyManager");
            return null;
        }
        int subId = getEapSubId(tm, config);
        if (subId == -1) {
            subId = SubscriptionManager.getDefaultDataSubscriptionId();
        }
        TelephonyManager defaultDataTm = tm.createForSubscriptionId(subId);
        if (carrierNetworkConfig == null) {
            Log.e(TAG, "No valid CarrierNetworkConfig");
            return null;
        }
        String imsi = defaultDataTm.getSubscriberId();
        int slotId = SubscriptionManager.getSlotIndex(subId);
        if (slotId == -1) {
            Log.e(TAG, "Slot id is invalid");
            return null;
        }
        String cdmaGsmImsi = HwWifiServiceFactory.getHwTelphonyUtils().getCdmaGsmImsi();
        if (cdmaGsmImsi != null && HwWifiServiceFactory.getHwTelphonyUtils().isCDMASimCard(slotId)) {
            String[] cdmaGsmImsiArray = cdmaGsmImsi.split(",");
            if (cdmaGsmImsiArray.length == 2) {
                imsi = cdmaGsmImsiArray[1];
                Log.i(TAG, "cdma prefer USIM/GSM imsi");
            }
        }
        String mccMnc = "";
        if (defaultDataTm.getSimState() == 5) {
            mccMnc = defaultDataTm.getSimOperator();
        }
        String identity = buildIdentity(getSimMethodForConfig(config), imsi, mccMnc, false);
        if (identity == null) {
            Log.e(TAG, "Failed to build the identity");
            return null;
        }
        try {
            ImsiEncryptionInfo imsiEncryptionInfo = defaultDataTm.getCarrierInfoForImsiEncryption(2);
            if (imsiEncryptionInfo == null) {
                return Pair.create(identity, "");
            }
            String encryptedIdentity = buildEncryptedIdentity(telephonyUtil, identity, imsiEncryptionInfo);
            if (encryptedIdentity != null) {
                return Pair.create(identity, encryptedIdentity);
            }
            Log.e(TAG, "failed to encrypt the identity");
            return null;
        } catch (RuntimeException e) {
            Log.e(TAG, "Failed to get imsi encryption info: " + e.getMessage());
            return null;
        }
    }

    public static String getAnonymousIdentityWith3GppRealm(@Nonnull TelephonyManager tm) {
        String mccMnc;
        if (tm == null) {
            return null;
        }
        TelephonyManager defaultDataTm = tm.createForSubscriptionId(SubscriptionManager.getDefaultDataSubscriptionId());
        if (defaultDataTm.getSimState() != 5 || (mccMnc = defaultDataTm.getSimOperator()) == null || mccMnc.isEmpty()) {
            return null;
        }
        String mcc = mccMnc.substring(0, 3);
        String mnc = mccMnc.substring(3);
        if (mnc.length() == 2) {
            mnc = "0" + mnc;
        }
        return "anonymous@" + String.format(THREE_GPP_NAI_REALM_FORMAT, mnc, mcc);
    }

    @VisibleForTesting
    public String encryptDataUsingPublicKey(PublicKey key, byte[] data, int encodingFlag) {
        try {
            Cipher cipher = Cipher.getInstance(IMSI_CIPHER_TRANSFORMATION);
            cipher.init(1, key);
            byte[] encryptedBytes = cipher.doFinal(data);
            return Base64.encodeToString(encryptedBytes, 0, encryptedBytes.length, encodingFlag);
        } catch (InvalidKeyException | NoSuchAlgorithmException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException e) {
            Log.e(TAG, "Encryption failed: " + e.getMessage());
            return null;
        }
    }

    private static String buildEncryptedIdentity(TelephonyUtil telephonyUtil, String identity, ImsiEncryptionInfo imsiEncryptionInfo) {
        if (imsiEncryptionInfo == null) {
            Log.e(TAG, "imsiEncryptionInfo is not valid");
            return null;
        } else if (identity == null) {
            Log.e(TAG, "identity is not valid");
            return null;
        } else {
            String encryptedIdentity = telephonyUtil.encryptDataUsingPublicKey(imsiEncryptionInfo.getPublicKey(), identity.getBytes(), 2);
            if (encryptedIdentity == null) {
                Log.e(TAG, "Failed to encrypt IMSI");
                return null;
            }
            String encryptedIdentity2 = DEFAULT_EAP_PREFIX + encryptedIdentity;
            if (imsiEncryptionInfo.getKeyIdentifier() == null) {
                return encryptedIdentity2;
            }
            return encryptedIdentity2 + "," + imsiEncryptionInfo.getKeyIdentifier();
        }
    }

    private static String buildIdentity(int eapMethod, String imsi, String mccMnc, boolean isEncrypted) {
        String mcc;
        String mnc;
        if (imsi == null || imsi.isEmpty()) {
            Log.e(TAG, "No IMSI or IMSI is null");
            return null;
        }
        String prefix = isEncrypted ? DEFAULT_EAP_PREFIX : EAP_METHOD_PREFIX.get(Integer.valueOf(eapMethod));
        if (prefix == null) {
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
        return prefix + imsi + "@" + String.format(THREE_GPP_NAI_REALM_FORMAT, mnc, mcc);
    }

    private static int getSimMethodForConfig(WifiConfiguration config) {
        if (config == null || config.enterpriseConfig == null) {
            return -1;
        }
        int eapMethod = config.enterpriseConfig.getEapMethod();
        if (eapMethod == 0) {
            int phase2Method = config.enterpriseConfig.getPhase2Method();
            if (phase2Method == 5) {
                eapMethod = 4;
            } else if (phase2Method == 6) {
                eapMethod = 5;
            } else if (phase2Method == 7) {
                eapMethod = 6;
            }
        }
        if (isSimEapMethod(eapMethod)) {
            return eapMethod;
        }
        return -1;
    }

    public static boolean isSimConfig(WifiConfiguration config) {
        return getSimMethodForConfig(config) != -1;
    }

    public static boolean isAnonymousAtRealmIdentity(String identity) {
        if (identity == null) {
            return false;
        }
        return identity.startsWith("anonymous@");
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
        throw new NumberFormatException(hex + " is not a valid hex string");
    }

    private static byte[] parseHexWithoutLength(String hex) {
        byte[] tmpRes = parseHex(hex);
        if (tmpRes.length == 0) {
            return tmpRes;
        }
        byte[] result = new byte[(tmpRes.length - 1)];
        System.arraycopy(tmpRes, 1, result, 0, tmpRes.length - 1);
        return result;
    }

    private static String makeHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        int length = bytes.length;
        for (int i = 0; i < length; i++) {
            sb.append(String.format("%02x", Byte.valueOf(bytes[i])));
        }
        return sb.toString();
    }

    private static String makeHex(byte[] bytes, int from, int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append(String.format("%02x", Byte.valueOf(bytes[from + i])));
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
        return getGsmAuthResponseWithLength(requestData, tm, 2);
    }

    public static String getGsmSimpleSimAuthResponse(String[] requestData, TelephonyManager tm) {
        return getGsmAuthResponseWithLength(requestData, tm, 1);
    }

    private static String getGsmAuthResponseWithLength(String[] requestData, TelephonyManager tm, int appType) {
        int subId;
        TelephonyManager defaultDataTm;
        int subId2;
        Object obj;
        String str;
        String[] strArr = requestData;
        Object obj2 = null;
        if (tm == null) {
            Log.e(TAG, "No valid TelephonyManager");
            return null;
        }
        int subId3 = getEapSubId(tm, null);
        if (subId3 == -1) {
            subId = SubscriptionManager.getDefaultDataSubscriptionId();
        } else {
            subId = subId3;
        }
        TelephonyManager defaultDataTm2 = tm.createForSubscriptionId(subId);
        StringBuilder sb = new StringBuilder();
        int length = strArr.length;
        int i = 0;
        int i2 = 0;
        while (i2 < length) {
            String challenge = strArr[i2];
            if (challenge == null) {
                obj = obj2;
                subId2 = subId;
                defaultDataTm = defaultDataTm2;
            } else if (challenge.isEmpty()) {
                obj = obj2;
                subId2 = subId;
                defaultDataTm = defaultDataTm2;
            } else {
                Log.d(TAG, "RAND = " + challenge);
                try {
                    String tmResponse = defaultDataTm2.getIccAuthentication(appType, 128, Base64.encodeToString(parseHex(challenge), 2));
                    Log.v(TAG, "Raw Response - " + tmResponse);
                    if (tmResponse != null) {
                        if (tmResponse.length() > 4) {
                            byte[] result = Base64.decode(tmResponse, i);
                            Log.v(TAG, "Hex Response -" + makeHex(result));
                            byte b = result[0];
                            if (b < 0) {
                                str = null;
                            } else if (b >= result.length) {
                                str = null;
                            } else {
                                String sres = makeHex(result, 1, b);
                                int kcOffset = b + 1;
                                if (kcOffset >= result.length) {
                                    Log.e(TAG, "malformed response - " + tmResponse);
                                    return null;
                                }
                                byte b2 = result[kcOffset];
                                if (b2 >= 0) {
                                    subId2 = subId;
                                    defaultDataTm = defaultDataTm2;
                                    if (kcOffset + b2 <= result.length) {
                                        String kc = makeHex(result, kcOffset + 1, b2);
                                        sb.append(":" + kc + ":" + sres);
                                        Log.v(TAG, "kc:" + kc + " sres:" + sres);
                                        obj = null;
                                    }
                                }
                                Log.e(TAG, "malformed response - " + tmResponse);
                                return null;
                            }
                            Log.e(TAG, "malformed response - " + tmResponse);
                            return str;
                        }
                    }
                    Log.e(TAG, "bad response - " + tmResponse);
                    return null;
                } catch (NumberFormatException e) {
                    obj = obj2;
                    subId2 = subId;
                    defaultDataTm = defaultDataTm2;
                    Log.e(TAG, "malformed challenge");
                }
            }
            i2++;
            i = 0;
            obj2 = obj;
            subId = subId2;
            defaultDataTm2 = defaultDataTm;
            strArr = requestData;
        }
        return sb.toString();
    }

    public static String getGsmSimpleSimNoLengthAuthResponse(String[] requestData, TelephonyManager tm) {
        String str = null;
        if (tm == null) {
            Log.e(TAG, "No valid TelephonyManager");
            return null;
        }
        TelephonyManager defaultDataTm = tm.createForSubscriptionId(SubscriptionManager.getDefaultDataSubscriptionId());
        StringBuilder sb = new StringBuilder();
        int length = requestData.length;
        int i = 0;
        int i2 = 0;
        while (i2 < length) {
            String challenge = requestData[i2];
            if (challenge != null && !challenge.isEmpty()) {
                Log.d(TAG, "RAND = " + challenge);
                try {
                    String tmResponse = defaultDataTm.getIccAuthentication(1, 128, Base64.encodeToString(parseHexWithoutLength(challenge), 2));
                    Log.v(TAG, "Raw Response - " + tmResponse);
                    if (tmResponse == null || tmResponse.length() <= 4) {
                        Log.e(TAG, "bad response - " + tmResponse);
                        return null;
                    }
                    byte[] result = Base64.decode(tmResponse, i);
                    if (12 != result.length) {
                        Log.e(TAG, "malformed response - " + tmResponse);
                        return str;
                    }
                    Log.v(TAG, "Hex Response -" + makeHex(result));
                    String sres = makeHex(result, 0, 4);
                    String kc = makeHex(result, 4, 8);
                    sb.append(":" + kc + ":" + sres);
                    Log.v(TAG, "kc:" + kc + " sres:" + sres);
                    str = null;
                } catch (NumberFormatException e) {
                    Log.e(TAG, "malformed challenge");
                }
            }
            i2++;
            i = 0;
        }
        return sb.toString();
    }

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
                tmResponse = tm.createForSubscriptionId(getEapSubId(tm, null)).getIccAuthentication(2, 129, base64Challenge);
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
                byte b = result[1];
                String res = makeHex(result, 2, b);
                byte b2 = result[b + 2];
                String ck = makeHex(result, b + 3, b2);
                String ik = makeHex(result, b + b2 + 4, result[b + b2 + 3]);
                sb.append(":" + ik + ":" + ck + ":" + res);
                Log.v(TAG, "ik:" + ik + "ck:" + ck + " res:" + res);
                goodReponse = true;
            } else if (tag == -36) {
                Log.e(TAG, "synchronisation failure");
                String auts = makeHex(result, 2, result[1]);
                resType = WifiNative.SIM_AUTH_RESP_TYPE_UMTS_AUTS;
                sb.append(":" + auts);
                Log.v(TAG, "auts:" + auts);
                goodReponse = true;
            } else {
                Log.e(TAG, "bad response - unknown tag = " + ((int) tag));
            }
        }
        if (!goodReponse) {
            return null;
        }
        String response = sb.toString();
        Log.v(TAG, "Supplicant Response -" + response);
        return new SimAuthResponseData(resType, response);
    }

    public static int getCarrierType(TelephonyManager tm) {
        if (tm == null) {
            return -1;
        }
        TelephonyManager defaultDataTm = tm.createForSubscriptionId(SubscriptionManager.getDefaultDataSubscriptionId());
        if (defaultDataTm.getSimState() != 5) {
            return -1;
        }
        if (defaultDataTm.getCarrierIdFromSimMccMnc() == defaultDataTm.getSimCarrierId()) {
            return 0;
        }
        return 1;
    }

    public static boolean isSimPresent(@Nonnull SubscriptionManager sm) {
        return sm.getActiveSubscriptionIdList().length > 0;
    }

    private static int getEapSubId(TelephonyManager telephonyManagerInstance, WifiConfiguration config) {
        int[] subIds;
        int subId = SubscriptionManager.getDefaultDataSubscriptionId();
        if (telephonyManagerInstance == null) {
            Log.e(TAG, "getEapSubId: telephonyManagerInstance is null");
            sAssignedSubId = subId;
            return sAssignedSubId;
        } else if (config == null || config.enterpriseConfig == null) {
            Log.e(TAG, "getEapSubId: config or enterpriseConfig is null, sAssignedSubId: " + sAssignedSubId);
            return sAssignedSubId;
        } else {
            int slotId = config.enterpriseConfig.getEapSubId();
            int slot1State = telephonyManagerInstance.getSimState(0);
            int slot2State = telephonyManagerInstance.getSimState(1);
            boolean isMultiSimEnabled = telephonyManagerInstance.isMultiSimEnabled();
            Log.i(TAG, "getEapSubId: isMultiSimEnabled=" + isMultiSimEnabled + ", slot1State=" + slot1State + ", slot2State=" + slot2State + ", slotId=" + slotId);
            if (isMultiSimEnabled && slotId != Integer.MAX_VALUE && slot1State == 5 && slot2State == 5 && (subIds = SubscriptionManager.getSubId(slotId)) != null && subIds.length > 0) {
                subId = subIds[0];
            }
            sAssignedSubId = subId;
            Log.i(TAG, "getEapSubId: subId=" + subId);
            return subId;
        }
    }
}
