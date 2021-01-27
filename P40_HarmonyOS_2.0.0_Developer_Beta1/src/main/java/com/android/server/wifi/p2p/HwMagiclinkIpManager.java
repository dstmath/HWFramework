package com.android.server.wifi.p2p;

import android.text.TextUtils;
import android.util.wifi.HwHiSlog;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

public class HwMagiclinkIpManager {
    private static final int MAGIC_IPPOOL_END = 100;
    private static final int MAGIC_IPPOOL_START = 3;
    private static final String PATTERN_IP = "^(([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.){3}([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
    private static final String TAG = "HwMagicIpManager";
    private static LinkedList<String> sIplist = new LinkedList<>();

    public static String getIp() {
        try {
            HwHiSlog.d(TAG, false, "get ip,now sIplist size %{public}d", new Object[]{Integer.valueOf(sIplist.size())});
            return sIplist.removeFirst();
        } catch (NoSuchElementException e) {
            HwHiSlog.e(TAG, false, "getIp fail", new Object[0]);
            return "";
        }
    }

    private static boolean validateIp(String ip) {
        if (!TextUtils.isEmpty(ip)) {
            return Pattern.compile(PATTERN_IP).matcher(ip).matches();
        }
        HwHiSlog.e(TAG, false, "validateIp: invalid ip", new Object[0]);
        return false;
    }

    public static void releaseIp(String ip) {
        Iterator<String> it = sIplist.iterator();
        while (it.hasNext()) {
            if (it.next().equals(ip)) {
                return;
            }
        }
        if (validateIp(ip)) {
            sIplist.addLast(ip);
        }
    }

    public static void resetIpPool(String sIP) {
        if (validateIp(sIP)) {
            StringBuilder sb = new StringBuilder();
            String[] ips = sIP.trim().split("\\.");
            if (ips.length > 1) {
                sb.append(ips[0]);
                for (int i = 1; i < ips.length - 1; i++) {
                    sb.append(".");
                    sb.append(ips[i]);
                }
            }
            sb.append(".");
            String serverIPHead = sb.toString();
            sIplist.clear();
            for (int i2 = 3; i2 <= 100; i2++) {
                LinkedList<String> linkedList = sIplist;
                linkedList.add(serverIPHead + i2);
            }
            return;
        }
        HwHiSlog.e(TAG, false, "resetIpPool: ip invalid", new Object[0]);
    }
}
