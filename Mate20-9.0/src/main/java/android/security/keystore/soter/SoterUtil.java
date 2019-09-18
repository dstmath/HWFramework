package android.security.keystore.soter;

import android.util.Base64;
import android.util.Log;
import java.nio.charset.StandardCharsets;
import org.json.JSONException;
import org.json.JSONObject;

public class SoterUtil {
    public static final int FLAG_NULL = 0;
    public static final String JSON_KEY_PUBLIC = "pub_key";
    private static final int MAX_JSON_LENG = 1048576;
    private static final String PARAM_NEED_AUTO_ADD_COUNTER_WHEN_GET_PUBLIC_KEY = "addcounter";
    private static final String PARAM_NEED_AUTO_ADD_SECMSG_FID_COUNTER_WHEN_SIGN = "secmsg_and_counter_signed_when_sign";
    private static final String PARAM_NEED_AUTO_SIGNED_WITH_COMMON_KEY_WHEN_GET_PUBLIC_KEY = "auto_signed_when_get_pubkey";
    private static final String PARAM_NEED_NEXT_ATTK = "next_attk";
    private static final int RAW_LENGTH_PREFIX = 4;
    public static final int SOTER_FLAG_JSON_FORMAT = 1;
    public static final String TAG = "SoterUtil";

    public static SoterRSAKeyGenParameterSpec convertKeyNameToParameterSpec(String name) {
        boolean isNeedNextAttk;
        boolean isAutoAddCounterWhenGetPublicKey;
        if (isNullOrNil(name)) {
            Log.e(TAG, "null or nil when convert key name to parameter");
            return null;
        }
        String[] splits = name.split("\\.");
        if (splits.length <= 1) {
            Log.w(TAG, "pure alias, no parameter");
            return null;
        }
        boolean isAutoSignedWithCommonkWhenGetPublicKey = false;
        String mAutoSignedKeyNameWhenGetPublicKey = "";
        boolean isSecmsgFidCounterSignedWhenSign = false;
        String entireCommonKeyExpr = containsPrefix(PARAM_NEED_AUTO_SIGNED_WITH_COMMON_KEY_WHEN_GET_PUBLIC_KEY, splits);
        if (!isNullOrNil(entireCommonKeyExpr)) {
            String commonKeyName = retrieveKeyNameFromExpr(entireCommonKeyExpr);
            if (!isNullOrNil(commonKeyName)) {
                isAutoSignedWithCommonkWhenGetPublicKey = true;
                mAutoSignedKeyNameWhenGetPublicKey = commonKeyName;
            }
        }
        if (contains(PARAM_NEED_AUTO_ADD_SECMSG_FID_COUNTER_WHEN_SIGN, splits)) {
            isSecmsgFidCounterSignedWhenSign = true;
        }
        boolean isSecmsgFidCounterSignedWhenSign2 = isSecmsgFidCounterSignedWhenSign;
        if (!contains(PARAM_NEED_AUTO_ADD_COUNTER_WHEN_GET_PUBLIC_KEY, splits)) {
            isAutoAddCounterWhenGetPublicKey = false;
        } else if (contains(PARAM_NEED_NEXT_ATTK, splits)) {
            isAutoAddCounterWhenGetPublicKey = true;
            isNeedNextAttk = true;
            SoterRSAKeyGenParameterSpec spec = new SoterRSAKeyGenParameterSpec(true, isAutoSignedWithCommonkWhenGetPublicKey, mAutoSignedKeyNameWhenGetPublicKey, isSecmsgFidCounterSignedWhenSign2, isAutoAddCounterWhenGetPublicKey, isNeedNextAttk);
            return spec;
        } else {
            isAutoAddCounterWhenGetPublicKey = true;
        }
        isNeedNextAttk = false;
        SoterRSAKeyGenParameterSpec spec2 = new SoterRSAKeyGenParameterSpec(true, isAutoSignedWithCommonkWhenGetPublicKey, mAutoSignedKeyNameWhenGetPublicKey, isSecmsgFidCounterSignedWhenSign2, isAutoAddCounterWhenGetPublicKey, isNeedNextAttk);
        return spec2;
    }

    private static String retrieveKeyNameFromExpr(String expr) {
        if (!isNullOrNil(expr)) {
            int startPos = expr.indexOf("(");
            int endPos = expr.indexOf(")");
            if (startPos >= 0 && endPos > startPos) {
                return expr.substring(startPos + 1, endPos);
            }
            Log.e(TAG, "no key name");
            return null;
        }
        Log.e(TAG, "expr is null");
        return null;
    }

    private static boolean contains(String target, String[] src) {
        if (src == null || src.length == 0 || isNullOrNil(target)) {
            Log.e(TAG, "param error");
            throw new IllegalArgumentException("param error");
        }
        for (String item : src) {
            if (target.equals(item)) {
                return true;
            }
        }
        return false;
    }

    private static String containsPrefix(String prefix, String[] src) {
        if (src == null || src.length == 0 || isNullOrNil(prefix)) {
            Log.e(TAG, "param error");
            throw new IllegalArgumentException("param error");
        }
        for (String item : src) {
            if (!isNullOrNil(item) && item.startsWith(prefix)) {
                return item;
            }
        }
        return null;
    }

    public static String getPureKeyAliasFromKeyName(String name) {
        if (isNullOrNil(name)) {
            Log.e(TAG, "null or nil when get pure key alias");
            return null;
        }
        String[] splits = name.split("\\.");
        if (splits.length > 1) {
            return splits[0];
        }
        Log.d(TAG, "pure alias");
        return name;
    }

    public static boolean isNullOrNil(String str) {
        return str == null || str.equals("");
    }

    public static byte[] getDataFromRaw(byte[] origin, String jsonKey) throws JSONException {
        if (isNullOrNil(jsonKey)) {
            Log.e(TAG, "json keyname error");
            return null;
        } else if (origin == null) {
            Log.e(TAG, "json origin null");
            return null;
        } else {
            JSONObject jsonObj = retriveJsonFromExportedData(origin);
            if (jsonObj == null || !jsonObj.has(jsonKey)) {
                return null;
            }
            return Base64.decode(jsonObj.getString(jsonKey).replace("-----BEGIN PUBLIC KEY-----\n", "").replace("\n-----END PUBLIC KEY-----", "").replace("\\n", ""), 0);
        }
    }

    private static JSONObject retriveJsonFromExportedData(byte[] origin) {
        if (origin == null) {
            Log.e(TAG, "raw data is null");
            return null;
        }
        if (origin.length < 4) {
            Log.e(TAG, "raw data length smaller than 4");
        }
        byte[] lengthBytes = new byte[4];
        System.arraycopy(origin, 0, lengthBytes, 0, 4);
        int rawLength = toInt(lengthBytes);
        if (rawLength > MAX_JSON_LENG) {
            Log.e(TAG, "exceed max json length. return null");
            return null;
        }
        byte[] rawJsonBytes = new byte[rawLength];
        if (origin.length <= 4 + rawLength) {
            Log.e(TAG, "length not correct");
            return null;
        }
        System.arraycopy(origin, 4, rawJsonBytes, 0, rawLength);
        try {
            return new JSONObject(new String(rawJsonBytes, StandardCharsets.UTF_8));
        } catch (JSONException e) {
            Log.e(TAG, "can not convert to json");
            return null;
        }
    }

    public static int toInt(byte[] bRefArr) {
        int iOutcome = 0;
        for (int i = 0; i < bRefArr.length; i++) {
            iOutcome += (bRefArr[i] & 255) << (8 * i);
        }
        return iOutcome;
    }
}
