package com.android.server.wifi.hwUtil;

import android.util.Log;
import com.android.server.wifi.util.NativeUtil;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class HwWifiConfigurationUtilEx {
    private static final int ENCLOSING_QUTOES_LEN = 2;
    private static final int SSID_GBK_MAX_LEN = 34;
    private static final int SSID_GBK_MIN_LEN = 3;
    private static final String TAG = "WifiConfigurationUtil";

    public static boolean isGBKEncodedString(String ssid) {
        if (ssid.isEmpty()) {
            Log.e(TAG, "ssid is empty");
            return false;
        }
        ArrayList<Byte> ssidArray = ScanResultRecords.getDefault().getOriSsid(NativeUtil.removeEnclosingQuotes(ssid));
        if (ssidArray != null) {
            byte[] buff = NativeUtil.byteArrayFromArrayList(ssidArray);
            try {
                if (Charset.isSupported("GBK")) {
                    byte[] newBuff = NativeUtil.removeEnclosingQuotes(ssid).getBytes("GBK");
                    if (buff.length == newBuff.length) {
                        for (int i = 0; i < buff.length; i++) {
                            if (buff[i] != newBuff[i]) {
                                return false;
                            }
                        }
                        return true;
                    }
                }
            } catch (UnsupportedEncodingException e) {
            }
        }
        return false;
    }

    public static boolean isSsidGBKValidLen(String ssid) {
        if (ssid.length() < 3) {
            Log.e(TAG, "validateSsid failed: GBK ssid string size too small: " + ssid.length());
            return false;
        } else if (ssid.length() <= 34) {
            return true;
        } else {
            Log.e(TAG, "validateSsid failed: GBK ssid string size too large: " + ssid.length());
            return false;
        }
    }
}
