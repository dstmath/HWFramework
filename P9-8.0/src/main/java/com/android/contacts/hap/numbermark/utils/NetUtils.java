package com.android.contacts.hap.numbermark.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.telephony.TelephonyManager;
import com.android.contacts.util.SharePreferenceUtil;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

public class NetUtils {
    private static final String ENCODE_UTF8 = "utf8";
    private static final String PREFS_UUID = "uuid";
    private static UUID mUuid;

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService("connectivity");
        NetworkInfo infos = manager.getActiveNetworkInfo();
        if (infos == null || !infos.isAvailable()) {
            return false;
        }
        State wifi = manager.getNetworkInfo(1).getState();
        State state = manager.getNetworkInfo(0).getState();
        if (State.CONNECTED == wifi) {
            return true;
        }
        if (State.CONNECTED == state) {
            return true;
        }
        return false;
    }

    public static String getTelimei(Context context) {
        return ((TelephonyManager) context.getSystemService("phone")).getDeviceId();
    }

    public static String getUuid(Context context) {
        if (context == null) {
            return "";
        }
        if (mUuid == null) {
            SharedPreferences prefs = SharePreferenceUtil.getDefaultSp_de(context);
            String id = prefs.getString(PREFS_UUID, null);
            if (id != null) {
                mUuid = UUID.fromString(id);
            } else {
                try {
                    String deviceId = getTelimei(context);
                    mUuid = deviceId == null ? UUID.randomUUID() : UUID.nameUUIDFromBytes(deviceId.getBytes(ENCODE_UTF8));
                    prefs.edit().putString(PREFS_UUID, mUuid.toString()).commit();
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return mUuid.toString();
    }
}
