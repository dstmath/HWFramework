package com.huawei.opcollect.utils;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.nb.client.DataServiceProxy;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class OPCollectUtils {
    private static final long DAY_IN_MILLISECOND = 86400000;
    public static final String DUMP_PRINT_PREFIX = "    ";
    public static final String ODMF_PACKAGE_NAME = "com.huawei.nb.service";
    public static final long ONEDAYINSECOND = 86400;
    public static final String OPCOLLECT_PERMISSION = "com.huawei.permission.OP_COLLECT";
    public static final int PRINT_OUT_INDET = 4;
    private static final String TAG = "OPCollectUtils";

    public static String getVersionName(Context context, String pkgName) {
        if (context == null || pkgName == null) {
            return "";
        }
        PackageManager packageManager = context.getPackageManager();
        if (packageManager == null) {
            OPCollectLog.i(TAG, "getVersionName getPacageManager = null");
            return "";
        }
        try {
            PackageInfo pInfo = packageManager.getPackageInfo(pkgName, 0);
            if (pInfo != null) {
                return pInfo.versionName;
            }
        } catch (PackageManager.NameNotFoundException e) {
            OPCollectLog.e(TAG, pkgName + " getPackageInfo NameNotFoundException");
        }
        return "";
    }

    public static Date getCurrentTime() {
        return new Date(System.currentTimeMillis());
    }

    public static String formatCurrentTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    public static String formatTimeInSecond(long secondFromMidnight) {
        StringBuilder sb = new StringBuilder();
        sb.append(Integer.toString(((((int) secondFromMidnight) / 3600) % 24) + 100).substring(1));
        sb.append(":").append(Integer.toString(((((int) secondFromMidnight) / 60) % 60) + 100).substring(1));
        sb.append(":").append(Integer.toString((((int) secondFromMidnight) % 60) + 100).substring(1));
        return sb.toString();
    }

    public static long getTimeInMsFromMidnight(Calendar cal) {
        return (((((((long) cal.get(11)) * 60) + ((long) cal.get(12))) * 60) + ((long) cal.get(13))) * 1000) + ((long) cal.get(14));
    }

    public static long getTimeSpanToNextDay(Calendar cal) {
        return DAY_IN_MILLISECOND - getTimeInMsFromMidnight(cal);
    }

    public static boolean isChineseZone() {
        return SystemPropertiesEx.getInt("ro.config.hw_optb", -1) == 156;
    }

    public static long getDayStartTimeMills() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(1), calendar.get(2), calendar.get(5), 0, 0, 0);
        return calendar.getTimeInMillis();
    }

    public static long getDayEndTimeMills() {
        return (DAY_IN_MILLISECOND + getDayStartTimeMills()) - 1;
    }

    public static long getCurrentTimeMills() {
        return Calendar.getInstance().getTimeInMillis();
    }

    public static boolean isPkgInstalled(Context context, String pkgName) {
        PackageInfo packageInfo;
        if (context == null || TextUtils.isEmpty(pkgName)) {
            OPCollectLog.i(TAG, "isPkgInstalled context = null or pkg is empty.");
            return false;
        }
        PackageManager packageManager = context.getPackageManager();
        if (packageManager == null) {
            OPCollectLog.i(TAG, "isPkgInstalled context getPacageManager = null");
            return false;
        }
        try {
            packageInfo = packageManager.getPackageInfo(pkgName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            packageInfo = null;
            OPCollectLog.i(TAG, pkgName + " is not installed");
        }
        if (packageInfo != null) {
            return true;
        }
        return false;
    }

    public static String getThirdPartyAppList(Context context) {
        if (context == null) {
            OPCollectLog.e(TAG, "context is null");
            return "";
        }
        List<PackageInfo> packages = context.getPackageManager().getInstalledPackages(0);
        StringBuilder sb = new StringBuilder();
        for (PackageInfo packageInfo : packages) {
            if ((packageInfo.applicationInfo.flags & 1) == 0) {
                sb.append(packageInfo.packageName).append(";");
            }
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    public static String getAppUsageState(Context context) {
        if (context == null) {
            OPCollectLog.e(TAG, "context is null");
            return "";
        }
        long time = System.currentTimeMillis();
        Map<String, Long> aggregateResult = new HashMap<>();
        List<UsageStats> stats = ((UsageStatsManager) context.getSystemService("usagestats")).queryUsageStats(0, time - DAY_IN_MILLISECOND, time);
        JSONObject object = new JSONObject();
        JSONArray jsonarray = new JSONArray();
        if (stats != null) {
            for (UsageStats usageStats : stats) {
                String packageName = usageStats.getPackageName();
                long timeInForground = usageStats.getTotalTimeInForeground();
                if (aggregateResult.containsKey(packageName)) {
                    aggregateResult.put(packageName, Long.valueOf(aggregateResult.get(packageName).longValue() + timeInForground));
                } else {
                    aggregateResult.put(packageName, Long.valueOf(timeInForground));
                }
            }
            for (Map.Entry<String, Long> entry : aggregateResult.entrySet()) {
                JSONObject jsonObj = new JSONObject();
                try {
                    jsonObj.put("appname", entry.getKey());
                    jsonObj.put("time", entry.getValue());
                } catch (JSONException e) {
                    OPCollectLog.e(TAG, "JSONException:" + e.getMessage());
                }
                jsonarray.put(jsonObj);
            }
        }
        try {
            object.put("appusage", jsonarray);
        } catch (JSONException e2) {
            OPCollectLog.e(TAG, "JSONException:" + e2.getMessage());
        }
        return object.toString();
    }

    public static String getODMFApiVersion(Context context) {
        if (context == null) {
            return "";
        }
        String version = new DataServiceProxy(context).getApiVersion();
        OPCollectLog.i(TAG, "ODMF Api Version:" + version);
        return version;
    }

    public static boolean checkODMFApiVersion(Context context, String needVersion) {
        String realVersion = getODMFApiVersion(context);
        if (realVersion == null || realVersion.length() == 0) {
            return false;
        }
        if (needVersion == null || needVersion.length() == 0) {
            return false;
        }
        String[] realVersions = realVersion.split("\\.");
        String[] needVersions = needVersion.split("\\.");
        if (realVersions.length != 3 || needVersions.length != 3) {
            return false;
        }
        int i = 0;
        while (i < 3) {
            try {
                if (Integer.parseInt(realVersions[i]) > Integer.parseInt(needVersions[i])) {
                    return true;
                }
                if (Integer.parseInt(realVersions[i]) < Integer.parseInt(needVersions[i])) {
                    return false;
                }
                if (i == 2) {
                    return true;
                }
                i++;
            } catch (NumberFormatException e) {
                OPCollectLog.i(TAG, "exception:" + e.getMessage());
                return false;
            }
        }
        return false;
    }

    public static String generateJson(String key, String value) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(key, value);
        } catch (JSONException e) {
            OPCollectLog.e(TAG, "put " + e.getMessage());
        }
        return jsonObject.toString();
    }
}
