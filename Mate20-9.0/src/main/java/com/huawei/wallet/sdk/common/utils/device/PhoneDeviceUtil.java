package com.huawei.wallet.sdk.common.utils.device;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.AndroidRuntimeException;
import com.huawei.android.os.BuildEx;
import com.huawei.wallet.sdk.common.apdu.multicard.MultiCard;
import com.huawei.wallet.sdk.common.apdu.multicard.MultiCardFactory;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.utils.StringUtil;
import com.huawei.wallet.sdk.common.utils.crypto.SHA_256;
import java.util.UUID;

public class PhoneDeviceUtil {
    private static final int ANDROID_M_CODE = 23;
    private static String GLOBAL_DEVICE_ID = "";
    private static String GLOBAL_UDID = "";
    private static String GLOBAL_UDID_812 = "";
    public static final String INVALIDATE_MCC = "99999";
    private static String phoneId = "";
    private static String sMccId = "99999";

    public static String getUDID(Context context) {
        String UDID = "";
        if (!TextUtils.isEmpty(GLOBAL_UDID)) {
            return GLOBAL_UDID;
        }
        try {
            UDID = BuildEx.getUDID();
        } catch (NoSuchMethodError e) {
            LogC.e("can not getUDID NoSuchMethodError", false);
        } catch (AndroidRuntimeException e2) {
            LogC.e("can not getUDID AndroidRuntimeException", false);
        } catch (NoClassDefFoundError e3) {
            LogC.e("can not getUDID NoClassDefFoundError", false);
        } catch (SecurityException e4) {
            LogC.e("can not getUDID SecurityException", false);
        } catch (Exception e5) {
            LogC.e("can not getUDID Exception", false);
        }
        GLOBAL_UDID = UDID;
        return UDID;
    }

    public static String getUDID812(Context context) {
        String UDID = "";
        if (!TextUtils.isEmpty(GLOBAL_UDID_812)) {
            return GLOBAL_UDID_812;
        }
        try {
            UDID = BuildEx.getUDID();
        } catch (NoSuchMethodError e) {
            LogC.e("can not getUDID NoSuchMethodError", false);
        } catch (AndroidRuntimeException e2) {
            LogC.e("can not getUDID AndroidRuntimeException", false);
        } catch (NoClassDefFoundError e3) {
            LogC.e("can not getUDID NoClassDefFoundError", false);
        } catch (SecurityException e4) {
            LogC.e("can not getUDID SecurityException", false);
        } catch (Exception e5) {
            LogC.e("can not getUDID Exception", false);
        }
        if (TextUtils.isEmpty(UDID) && !isHuaWeiPhone()) {
            UDID = Build.SERIAL;
            if (!TextUtils.isEmpty(UDID)) {
                UDID = encryptSerialNumber(UDID);
            }
        }
        GLOBAL_UDID_812 = UDID;
        return UDID;
    }

    private static boolean isHuaWeiPhone() {
        return "HUAWEI".equals(Build.MANUFACTURER);
    }

    public static String getSerialNumber() {
        return Build.SERIAL;
    }

    private static String encryptSerialNumber(String sn) {
        return SHA_256.encrypt(sn, SHA_256.ALGORITHM_SHA256);
    }

    public static String getDeviceID(Context context) {
        if (!TextUtils.isEmpty(GLOBAL_DEVICE_ID)) {
            return GLOBAL_DEVICE_ID;
        }
        String deviceID = getUDID812(context);
        if (!StringUtil.isEmpty(deviceID, true)) {
            LogC.i("getDeviceID getudid success", false);
            GLOBAL_DEVICE_ID = deviceID;
            return deviceID;
        }
        String deviceID2 = getRealDeviceId(context);
        GLOBAL_DEVICE_ID = deviceID2;
        return deviceID2;
    }

    public static String getRealDeviceId(Context context) {
        String deviceID = null;
        if (MultiCardFactory.isMultiSimEnabled()) {
            LogC.d("multicard device", false);
            deviceID = MultiCardFactory.createIfGemini().getDeviceId(0);
        }
        try {
            if (TextUtils.isEmpty(deviceID) != 0) {
                return getPhoneId(context);
            }
            return deviceID;
        } catch (RuntimeException e) {
            LogC.v("can not getDeviceID RuntimeException", false);
            return deviceID;
        } catch (Exception e2) {
            LogC.e("can not getDeviceID", false);
            return deviceID;
        }
    }

    public static String getPhoneId(Context mContext) {
        String deviceID = null;
        if (!TextUtils.isEmpty(phoneId)) {
            return phoneId;
        }
        if (mContext == null) {
            LogC.w("PhoneDeviceUtil getDeviceId context is null", false);
            return null;
        }
        TelephonyManager tm = (TelephonyManager) mContext.getSystemService("phone");
        if (tm != null) {
            try {
                deviceID = tm.getDeviceId();
            } catch (SecurityException e) {
                LogC.i("getPhoneId imei SecurityException", false);
            }
        }
        phoneId = deviceID;
        return deviceID;
    }

    public static String getAnotherDeviceId(Context context) {
        if (context == null || !MultiCardFactory.isMultiSimEnabled()) {
            return "";
        }
        LogC.d("getAnotherDeviceId multicard device", false);
        MultiCard mutiCard = MultiCardFactory.createIfGemini();
        String defaultDeviceId = getDeviceID(context);
        String otherDeviceId = mutiCard.getDeviceId(1);
        if (TextUtils.isEmpty(otherDeviceId) || !defaultDeviceId.equals(otherDeviceId)) {
            return otherDeviceId;
        }
        return mutiCard.getDeviceId(0);
    }

    public static String getNumUUID(int number) {
        if (number < 0) {
            LogC.i("getUUID, Invalid argument", false);
            return "";
        }
        String uuid = UUID.randomUUID().toString().replace("-", "");
        if (uuid.length() > number - 1) {
            return uuid.substring(0, number);
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < number - uuid.length(); i++) {
            stringBuilder.append("0");
        }
        return stringBuilder + uuid;
    }

    public static String getDeviceType() {
        return Build.MODEL;
    }

    public static String getMccCode(Context context) {
        if (!TextUtils.isEmpty(sMccId) && !sMccId.equals("99999")) {
            return sMccId;
        }
        if (context == null) {
            return "99999";
        }
        TelephonyManager telManager = (TelephonyManager) context.getSystemService("phone");
        if (telManager != null) {
            String mccId = telManager.getSimOperator();
            if (mccId != null && mccId.length() >= 5) {
                sMccId = mccId;
                return sMccId;
            }
        }
        return "99999";
    }
}
