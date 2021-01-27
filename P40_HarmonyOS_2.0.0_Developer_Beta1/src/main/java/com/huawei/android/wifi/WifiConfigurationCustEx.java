package com.huawei.android.wifi;

import android.net.wifi.WifiConfiguration;
import com.huawei.android.util.NoExtAPIException;

public class WifiConfigurationCustEx {
    public static final int WAPI_ASCII_PASSWORD = -1;
    public static final String WAPI_CERT_FLAG = "";
    public static final int WAPI_HEX_PASSWORD = -1;
    public static final String WAPI_PSK_FLAG = "";

    public static class KeyMgmtEx {
        public static final int WAPI_CERT = -1;
        public static final int WAPI_PSK = -1;
    }

    public static void setWapiAsCert(WifiConfiguration config, String value) {
        throw new NoExtAPIException("method not supported.");
    }

    public static void setWapiUserCert(WifiConfiguration config, String value) {
        throw new NoExtAPIException("method not supported.");
    }

    public static void setWapiCertIndex(WifiConfiguration config, int value) {
        throw new NoExtAPIException("method not supported.");
    }

    public static void setWapiPskType(WifiConfiguration config, int value) {
        throw new NoExtAPIException("method not supported.");
    }

    public static String getWapiAsCert(WifiConfiguration config) {
        throw new NoExtAPIException("method not supported.");
    }

    public static String getWapiUserCert(WifiConfiguration config) {
        throw new NoExtAPIException("method not supported.");
    }

    public static int getWapiCertIndex(WifiConfiguration config) {
        throw new NoExtAPIException("method not supported.");
    }

    public static int getWapiPskType(WifiConfiguration config) {
        throw new NoExtAPIException("method not supported.");
    }

    public static void makeConfig(WifiConfiguration config, String password) {
        throw new NoExtAPIException("method not supported.");
    }

    public static boolean copyFile(String cert, String alias, byte[] data) {
        throw new NoExtAPIException("method not supported.");
    }
}
