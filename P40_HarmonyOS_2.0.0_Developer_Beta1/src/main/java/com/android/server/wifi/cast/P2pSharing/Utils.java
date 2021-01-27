package com.android.server.wifi.cast.P2pSharing;

import android.content.Context;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.wifi.HwHiLog;
import com.android.server.wifi.WifiInjector;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

public class Utils {
    public static final int BYTE_LENGTH = 16;
    private static final int BYTE_MASK = 255;
    private static final String GO_SUFFIX = "1";
    private static final int INVALID_VALUE = -1;
    private static final String MARKETING_NAME_PROPERTY = "ro.config.marketing_name";
    private static final int MAX_DATA_LEN = 1024;
    private static final int MOVE_BITS = 8;
    public static final int MSG_TYPE_LENGTH = 4;
    public static final int MULTIPLE = 2;
    private static final String PATTERN_IP = "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
    private static final String PRODUCT_NAME_PROPERTY = "ro.product.name";
    private static final String TAG = "P2pSharing:Utils";

    private Utils() {
    }

    static String getP2pIpAddress() {
        NetworkInterface ni = getP2pNetworkInterface();
        if (ni == null) {
            return "";
        }
        Enumeration<InetAddress> addrs = ni.getInetAddresses();
        while (addrs.hasMoreElements()) {
            InetAddress addr = addrs.nextElement();
            if (addr instanceof Inet4Address) {
                return addr.getHostAddress();
            }
        }
        return "";
    }

    static NetworkInterface getP2pNetworkInterface() {
        NetworkInterface ni = getNetworkInterfaceByName("p2p-p2p0");
        if (ni == null) {
            ni = getNetworkInterfaceByName("p2p0");
        }
        if (ni == null) {
            ni = getNetworkInterfaceByName("p2p-eth0");
        }
        if (ni == null) {
            return getNetworkInterfaceByName("p2p-wlan");
        }
        return ni;
    }

    private static NetworkInterface getNetworkInterfaceByName(String ifName) {
        Enumeration<NetworkInterface> nis = null;
        try {
            nis = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            HwHiLog.e(TAG, false, "nis is null, failed to get network interfaces: " + ifName, new Object[0]);
        }
        if (nis == null) {
            return null;
        }
        while (nis.hasMoreElements()) {
            NetworkInterface ni = nis.nextElement();
            if (ni != null && ni.getName().startsWith(ifName)) {
                return ni;
            }
        }
        return null;
    }

    static String getLocalNetworkInterfaceName() {
        Enumeration<NetworkInterface> nis = null;
        try {
            nis = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            HwHiLog.e(TAG, false, "getNetworkInterfaceByName fail", new Object[0]);
        }
        if (nis == null) {
            return "";
        }
        while (nis.hasMoreElements()) {
            NetworkInterface ni = nis.nextElement();
            if (ni != null && ni.getName().startsWith("p2p-p2p0")) {
                return ni.getName();
            }
        }
        return "";
    }

    public static byte[] packageData(int type, byte[] data) {
        byte[] temp = data;
        if (data == null) {
            temp = new byte[0];
        }
        if (temp.length > 1024) {
            return new byte[0];
        }
        byte[] typeArray = convertInt2Byte(type);
        byte[] lengthArray = convertInt2Byte(temp.length);
        byte[] result = new byte[(typeArray.length + lengthArray.length + temp.length)];
        System.arraycopy(typeArray, 0, result, 0, 4);
        System.arraycopy(lengthArray, 0, result, 4, 4);
        System.arraycopy(temp, 0, result, 8, temp.length);
        return result;
    }

    static byte[] combineData(byte[] origData, byte[] expandData) {
        if (isEmptyByteArray(origData) || isEmptyByteArray(expandData) || origData.length > 1024 || expandData.length > 1024) {
            return new byte[0];
        }
        byte[] result = new byte[(origData.length + expandData.length + 8)];
        System.arraycopy(convertInt2Byte(origData.length), 0, result, 0, 4);
        System.arraycopy(origData, 0, result, 4, origData.length);
        System.arraycopy(convertInt2Byte(expandData.length), 0, result, origData.length + 4, 4);
        System.arraycopy(expandData, 0, result, origData.length + 8, expandData.length);
        return result;
    }

    static int getDataType(byte[] data) {
        byte[] typeBytes = new byte[4];
        System.arraycopy(data, 0, typeBytes, 0, 4);
        return convertByte2Int(typeBytes);
    }

    static boolean isDataValid(byte[] data) {
        if (isEmptyByteArray(data) || data.length < 8) {
            HwHiLog.w(TAG, false, "Data is invalid", new Object[0]);
            return false;
        }
        byte[] lenBytes = new byte[4];
        System.arraycopy(data, 4, lenBytes, 0, 4);
        if (convertByte2Int(lenBytes) == data.length - 8) {
            return true;
        }
        HwHiLog.w(TAG, false, "Data format error", new Object[0]);
        return false;
    }

    static byte[] getDataValue(byte[] data) {
        int len = data.length - 8;
        if (len < 0 || len > 1024) {
            return new byte[0];
        }
        byte[] valueBytes = new byte[len];
        System.arraycopy(data, 8, valueBytes, 0, len);
        return valueBytes;
    }

    static byte[] convertInt2Byte(int num) {
        return new byte[]{(byte) ((num >> 24) & BYTE_MASK), (byte) ((num >> 16) & BYTE_MASK), (byte) ((num >> 8) & BYTE_MASK), (byte) (num & BYTE_MASK)};
    }

    static int convertByte2Int(byte[] datas) {
        if (datas == null || datas.length != 4) {
            return -1;
        }
        return (datas[3] & 255) | ((datas[2] & 255) << 8) | ((datas[1] & 255) << 16) | ((datas[0] & 255) << 24);
    }

    static boolean isEmptyByteArray(byte[] data) {
        return data == null || data.length == 0;
    }

    static String getServerIpAddress(String localIpAddr) {
        if (!isValidateIp(localIpAddr)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        String[] splited = localIpAddr.trim().split("\\.");
        if (splited.length > 1) {
            sb.append(splited[0]);
            for (int i = 1; i < splited.length - 1; i++) {
                sb.append(".");
                sb.append(splited[i]);
            }
        }
        sb.append(".");
        return sb.toString() + GO_SUFFIX;
    }

    private static boolean isValidateIp(String ip) {
        if (!TextUtils.isEmpty(ip)) {
            return Pattern.compile(PATTERN_IP).matcher(ip).matches();
        }
        return false;
    }

    static int getWiFiSecurity(Context context) {
        if (context == null) {
            return -1;
        }
        Object object = context.getSystemService("wifi");
        if (!(object instanceof WifiManager)) {
            return -1;
        }
        WifiManager wifiManager = (WifiManager) object;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        if (wifiInfo == null || wifiInfo.getSupplicantState() != SupplicantState.COMPLETED) {
            HwHiLog.w(TAG, false, "wifiInfo is null or supplicant state is not completed", new Object[0]);
            return -1;
        }
        String ssid = wifiInfo.getSSID();
        int netWorkId = wifiInfo.getNetworkId();
        List<WifiConfiguration> configuredNetworks = wifiManager.getConfiguredNetworks();
        if (configuredNetworks == null || configuredNetworks.size() == 0) {
            HwHiLog.w(TAG, false, "no configuredNetworks", new Object[0]);
            return -1;
        }
        for (WifiConfiguration cfg : configuredNetworks) {
            if (ssid.equals(cfg.SSID) && netWorkId == cfg.networkId) {
                return getSecurity(cfg);
            }
        }
        return -1;
    }

    private static int getSecurity(WifiConfiguration config) {
        if (config.allowedKeyManagement.get(0) || config.allowedKeyManagement.get(9)) {
            return 0;
        }
        if (config.allowedKeyManagement.get(1)) {
            return 2;
        }
        if (config.allowedKeyManagement.get(8)) {
            return 4;
        }
        if (config.allowedKeyManagement.get(3) || config.allowedKeyManagement.get(2) || config.allowedKeyManagement.get(10)) {
            return 5;
        }
        if (config.wepKeys[0] != null) {
            return 1;
        }
        try {
            if (config.allowedKeyManagement.get(WifiConfiguration.KeyMgmt.class.getDeclaredField("WPA2_PSK").getInt(null))) {
                return 3;
            }
            return -1;
        } catch (IllegalAccessException | NoSuchFieldException e) {
            HwHiLog.e(TAG, false, "no such filed", new Object[0]);
            return -1;
        }
    }

    public static boolean isWiFiConnected(Context context) {
        WifiInfo connectionInfo;
        if (context == null) {
            return false;
        }
        Object object = context.getSystemService("wifi");
        if (!(object instanceof WifiManager)) {
            return false;
        }
        WifiManager wifiManager = (WifiManager) object;
        if (wifiManager.isWifiEnabled() && (connectionInfo = wifiManager.getConnectionInfo()) != null) {
            return SupplicantState.COMPLETED.equals(connectionInfo.getSupplicantState());
        }
        return false;
    }

    static void enableWiFiAutoConnect(Context context, boolean isEnabled) {
        if (context != null) {
            Object object = context.getSystemService("wifi");
            if (object instanceof WifiManager) {
                HwHiLog.d(TAG, false, "enableWiFiAutoConnect:" + isEnabled, new Object[0]);
                WifiManager wifiManager = (WifiManager) object;
                wifiManager.enableWifiConnectivityManager(isEnabled);
                if (!isEnabled) {
                    wifiManager.disconnect();
                }
            }
        }
    }

    static String getDeviceName() {
        String marketingName = SystemProperties.get(MARKETING_NAME_PROPERTY, "");
        if (!TextUtils.isEmpty(marketingName)) {
            return marketingName;
        }
        String productName = SystemProperties.get(PRODUCT_NAME_PROPERTY, "");
        if (!TextUtils.isEmpty(productName)) {
            return productName;
        }
        return "";
    }

    static void resetArrays(byte[] bytes) {
        if (!isEmptyByteArray(bytes)) {
            Integer num = 0;
            Arrays.fill(bytes, num.byteValue());
        }
    }

    static WifiConfiguration getCurrentWifiConfiguration() {
        return WifiInjector.getInstance().getClientModeImpl().getCurrentWifiConfiguration();
    }

    static void connectWiFi(Context context, int networkId) {
        HwHiLog.d(TAG, false, "connectWiFi:" + networkId, new Object[0]);
        if (context != null) {
            Object object = context.getSystemService("wifi");
            if (object instanceof WifiManager) {
                WifiManager wifiManager = (WifiManager) object;
                if (networkId >= 0) {
                    wifiManager.connect(networkId, null);
                } else {
                    wifiManager.reassociate();
                }
            }
        }
    }
}
