package com.android.server;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class HiLinkUtil {
    private static final String Device_App_PackageName = "com.huawei.smarthome";
    public static final int HILINK_ROUTER = 1;
    private static final String Hilink_App_PackageName = "com.huawei.mw";
    private static final String IGNOR_DEVICE1 = "002";
    private static final String IGNOR_DEVICE2 = "009";
    public static final int NONE = 0;
    public static final String SCHEME_GATEWAY = "http://192.168.3.1";
    private static final String SCHEME_PREFIX = "higuide://com.huawei.higuide.action.LAUNCH?";
    private static final String SEPARATER = "&";
    public static final int SMARTHOME_DEVICE = 2;
    private static final String STATUS_FLAG = "110";
    private static final String TAG = "HiLinkUtil";
    private static final String TYPE_FLAG = "23";

    private static boolean isHiSsid(String ssid) {
        StringBuilder stringBuilder = new StringBuilder(ssid);
        if (stringBuilder.length() != 32 || !stringBuilder.substring(0, 2).equals("Hi")) {
            return false;
        }
        char version = stringBuilder.charAt(2);
        if (stringBuilder.charAt(9) != 'A') {
            return true;
        }
        if (version == '0') {
            return (IGNOR_DEVICE1.equals(stringBuilder.substring(21, 24)) || (IGNOR_DEVICE2.equals(stringBuilder.substring(21, 24)) ^ 1) == 0 || !"00000000000".equals(stringBuilder.substring(10, 21))) ? false : true;
        } else {
            String status = stringBuilder.substring(10, 13);
            String type = stringBuilder.substring(13, 15);
            if (STATUS_FLAG.equals(status) && TYPE_FLAG.equals(type)) {
                return version >= '3' || version <= '0' || !(stringBuilder.substring(15, 18).equals(IGNOR_DEVICE1) || stringBuilder.substring(15, 18).equals(IGNOR_DEVICE2));
            } else {
                return false;
            }
        }
    }

    private static boolean isSmartHomeDeviceOrNot(String ssid) {
        if (ssid.charAt(9) == 'A') {
            if (ssid.charAt(2) == '0') {
                if (ssid.substring(21, 24).equals("001")) {
                    return false;
                }
            } else if (ssid.substring(15, 18).equals("001")) {
                return false;
            }
        }
        return true;
    }

    private static String getHiSsidFromCurrentSsid(Context context, String bSsid, List<ScanResult> scanResults) {
        if (scanResults == null || (scanResults.isEmpty() ^ 1) == 0) {
            return "";
        }
        Long mLong = Long.valueOf(Long.parseLong(bSsid.replaceAll(":", ""), 16));
        StringBuilder targString1 = new StringBuilder(Long.toHexString(mLong.longValue() + 1));
        StringBuilder targString2 = new StringBuilder(Long.toHexString(mLong.longValue() + 2));
        StringBuilder targString3 = new StringBuilder(Long.toHexString(mLong.longValue() + 3));
        StringBuilder targString4 = new StringBuilder(Long.toHexString(mLong.longValue() - 1));
        StringBuilder stringBuilder = new StringBuilder(Long.toHexString(mLong.longValue() - 2));
        StringBuilder targString6 = new StringBuilder(Long.toHexString(mLong.longValue() - 3));
        int stringLength = targString1.length();
        for (int i = 0; i < 12 - stringLength; i++) {
            targString1.insert(0, 0);
            targString2.insert(0, 0);
            targString3.insert(0, 0);
            targString4.insert(0, 0);
            stringBuilder.insert(0, 0);
            targString6.insert(0, 0);
        }
        String mShowSsid = "";
        for (ScanResult item : scanResults) {
            String checkBssid = item.BSSID.replaceAll(":", "");
            if (isHiSsid(item.SSID) && (checkBssid.equals(targString1.toString()) || checkBssid.equals(targString2.toString()) || checkBssid.equals(targString3.toString()) || checkBssid.equals(targString4.toString()) || checkBssid.equals(stringBuilder.toString()) || checkBssid.equals(targString6.toString()))) {
                mShowSsid = item.SSID;
                break;
            }
        }
        return mShowSsid;
    }

    private static List<ScanResult> getScanResults(Context context) {
        return ((WifiManager) context.getApplicationContext().getSystemService("wifi")).getScanResults();
    }

    private static boolean isAppInstalled(Context context, String packageName) {
        List<PackageInfo> pinfo = context.getPackageManager().getInstalledPackages(0);
        List<String> pName = new ArrayList();
        if (pinfo != null) {
            for (int i = 0; i < pinfo.size(); i++) {
                pName.add(((PackageInfo) pinfo.get(i)).packageName);
            }
        }
        return pName.contains(packageName);
    }

    private static boolean isSmartHomeAppVersionNew(Context context) {
        List<PackageInfo> pinfo = context.getPackageManager().getInstalledPackages(0);
        if (pinfo != null) {
            int i = 0;
            while (i < pinfo.size()) {
                String pn = ((PackageInfo) pinfo.get(i)).packageName;
                if (pn == null || !pn.equals(Device_App_PackageName)) {
                    i++;
                } else {
                    Log.d(TAG, "versionName " + ((PackageInfo) pinfo.get(i)).versionName + " versionCode " + ((PackageInfo) pinfo.get(i)).versionCode);
                    return ((PackageInfo) pinfo.get(i)).versionCode >= 1100023000;
                }
            }
        }
        return false;
    }

    private static String getHiModel(String hiSsid) {
        return hiSsid.substring(4, 8);
    }

    private static String buildScheme(String hi_protocol, String hi_vendor, String hi_model, String hi_action, String version) {
        String scheme = SCHEME_PREFIX;
        if (hi_protocol == null) {
            return null;
        }
        scheme = scheme + "hi_protocol=" + hi_protocol + SEPARATER;
        if (hi_vendor == null) {
            return null;
        }
        scheme = scheme + "hi_vendor=" + hi_vendor + SEPARATER;
        if (hi_model == null) {
            return null;
        }
        scheme = scheme + "hi_model=" + hi_model + SEPARATER;
        if (hi_action == null) {
            return null;
        }
        scheme = scheme + "hi_action=" + hi_action;
        if (version != null) {
            scheme = scheme + SEPARATER + "hi_version=" + version;
        }
        return scheme;
    }

    public static int getHiLinkSsidType(Context context, String ssid, String bSsid) {
        if (ssid == null || bSsid == null || context == null) {
            Log.e(TAG, "something is null");
            return 0;
        } else if (isHiSsid(ssid)) {
            if (isSmartHomeDeviceOrNot(ssid)) {
                Log.i(TAG, "Found new Hilink SmartHomeDevice ");
                return 2;
            }
            Log.i(TAG, "Found new Hilink Router ");
            return 1;
        } else if (TextUtils.isEmpty(getHiSsidFromCurrentSsid(context, bSsid, getScanResults(context)))) {
            return 0;
        } else {
            Log.i(TAG, "Found new Unconfig Router ");
            return 1;
        }
    }

    public static String getLaunchAppForSsid(Context context, String ssid, String bSsid) {
        if (ssid == null || bSsid == null || context == null) {
            Log.e(TAG, "something is null");
            return null;
        }
        String hi_vendor = "hilink";
        String hi_action = "setup";
        String version = null;
        boolean smartHomeAppVersionNew = isSmartHomeAppVersionNew(context);
        boolean hinkappInstalled = isAppInstalled(context, Hilink_App_PackageName);
        String hi_protocol;
        if (isHiSsid(ssid)) {
            if (isSmartHomeDeviceOrNot(ssid)) {
                hi_protocol = "hilink";
            } else {
                hi_protocol = "hilink_router";
                if (smartHomeAppVersionNew) {
                    version = "2.0";
                } else if (!hinkappInstalled) {
                    return SCHEME_GATEWAY;
                } else {
                    version = "1.0";
                }
            }
            return buildScheme(hi_protocol, hi_vendor, getHiModel(ssid), hi_action, version);
        }
        String hiSsid = getHiSsidFromCurrentSsid(context, bSsid, getScanResults(context));
        if (TextUtils.isEmpty(hiSsid)) {
            return null;
        }
        hi_protocol = "hilink_router";
        if (smartHomeAppVersionNew) {
            version = "2.0";
        } else if (!hinkappInstalled) {
            return SCHEME_GATEWAY;
        } else {
            version = "1.0";
        }
        return buildScheme(hi_protocol, hi_vendor, getHiModel(hiSsid), hi_action, version);
    }

    public static void startDeviceGuide(Context context, String uri) {
        Log.i(TAG, "startDeviceGuide " + uri);
        Intent intent = new Intent();
        intent.setAction("com.huawei.iconnect.deviceguide.action.LAUNCH");
        intent.setData(Uri.parse(uri));
        try {
            Intent launchIntent = createExplicitFromImplicitIntent(context, intent);
            if (launchIntent == null) {
                Log.i(TAG, "launchIntent is null");
            } else {
                context.startService(launchIntent);
            }
        } catch (Exception e) {
            Log.i(TAG, "exception in startDeviceGuide " + e.getMessage());
        }
    }

    private static Intent createExplicitFromImplicitIntent(Context context, Intent implicitIntent) {
        List<ResolveInfo> resolveInfo = context.getPackageManager().queryIntentServices(implicitIntent, 0);
        if (resolveInfo == null || resolveInfo.size() != 1) {
            Log.e(TAG, "resolveInfo not found");
            return null;
        }
        ResolveInfo serviceInfo = (ResolveInfo) resolveInfo.get(0);
        ComponentName component = new ComponentName(serviceInfo.serviceInfo.packageName, serviceInfo.serviceInfo.name);
        Intent explicitIntent = new Intent(implicitIntent);
        explicitIntent.setComponent(component);
        return explicitIntent;
    }
}
