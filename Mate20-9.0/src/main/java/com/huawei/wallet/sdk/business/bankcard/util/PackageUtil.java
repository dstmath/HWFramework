package com.huawei.wallet.sdk.business.bankcard.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.wallet.sdk.common.log.LogC;
import com.huawei.wallet.sdk.common.utils.crypto.SHA_256;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

public class PackageUtil {
    private static final int APPINFO_DEFAULT_FLAGS = 0;
    private static final String KEY_APP_CHANNEL = "APP_CHANNEL";
    private static int sVersionCode = 0;

    public static String getAppVersion(Context context) {
        return getVersion(context, null, false, "");
    }

    public static String getVersionName(Context context) {
        return getVersion(context, null, false, null);
    }

    public static String getVersionName(Context context, String packageName) {
        return getVersion(context, packageName, true, null);
    }

    private static String getVersion(Context context, String packageName, boolean mustPkgName, String defaultVersion) {
        String version = defaultVersion;
        if (context == null) {
            LogC.e("getVersion context is null.", false);
            return version;
        } else if (!mustPkgName || !TextUtils.isEmpty(packageName)) {
            try {
                PackageManager packageManager = context.getPackageManager();
                if (packageManager != null) {
                    PackageInfo packInfo = packageManager.getPackageInfo(TextUtils.isEmpty(packageName) ? context.getPackageName() : packageName, 0);
                    if (packInfo != null) {
                        version = packInfo.versionName;
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                LogC.i("getVersion fail NameNotFoundException ", false);
            }
            return version;
        } else {
            LogC.e("getVersion packageName is null.", false);
            return version;
        }
    }

    public static int getVersionCode(Context context, String packageName) {
        return getVersionCode(context, packageName, true);
    }

    public static int getVersionCode(Context context) {
        if (sVersionCode == 0) {
            sVersionCode = getVersionCode(context, null, false);
        }
        return sVersionCode;
    }

    private static int getVersionCode(Context ctx, String packageName, boolean mustPkgName) {
        int versionCode = 0;
        if (ctx == null) {
            LogC.e("getVersionCode context is null.", false);
            return 0;
        } else if (!mustPkgName || !TextUtils.isEmpty(packageName)) {
            try {
                PackageInfo pkInfo = ctx.getPackageManager().getPackageInfo(TextUtils.isEmpty(packageName) ? ctx.getPackageName() : packageName, 0);
                if (pkInfo != null) {
                    versionCode = pkInfo.versionCode;
                }
            } catch (PackageManager.NameNotFoundException e) {
                LogC.e("get the app versioncode fail", (Throwable) e, false);
            }
            return versionCode;
        } else {
            LogC.e("getVersion packageName is null.", false);
            return 0;
        }
    }

    public static boolean isAppInstalled(Context context, String packageName) {
        return true ^ TextUtils.isEmpty(getVersion(context, packageName, true, null));
    }

    public static boolean[] isPackagesHasInstalled(Context context, String bankPackageName, String appMarketPackageName) {
        boolean[] result = {false, false};
        if (context == null) {
            LogC.e("isPackageHasInstalled context is null.", false);
            return result;
        }
        if (!TextUtils.isEmpty(bankPackageName)) {
            bankPackageName = bankPackageName.replaceAll(" ", "");
        }
        if (!TextUtils.isEmpty(appMarketPackageName)) {
            appMarketPackageName = appMarketPackageName.replaceAll(" ", "");
        }
        result[0] = isAppInstalled(context, bankPackageName);
        result[1] = isAppInstalled(context, appMarketPackageName);
        return result;
    }

    public static String getAppChannel(Context context) {
        String channel = String.valueOf(0);
        if (context != null) {
            ApplicationInfo appInfo = null;
            try {
                appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 128);
            } catch (PackageManager.NameNotFoundException e) {
                LogC.i("getAppChannel NameNotFoundException", false);
            }
            if (appInfo != null) {
                channel = String.valueOf(appInfo.metaData.getInt(KEY_APP_CHANNEL));
            }
        }
        LogC.i("package channel is :" + channel, false);
        return channel;
    }

    public static boolean hasPermission(Context ctx, String pkgName, String strPermission) {
        if (ctx == null || TextUtils.isEmpty(pkgName) || TextUtils.isEmpty(strPermission)) {
            return false;
        }
        try {
            for (String strPerm : ctx.getPackageManager().getPackageInfo(pkgName, 4096).requestedPermissions) {
                if (strPermission.equals(strPerm)) {
                    return true;
                }
            }
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            LogC.e("get the app hasPermission fail", (Throwable) e, false);
            return false;
        }
    }

    public static String getApkSignHashCode(Context mContext) {
        try {
            PackageInfo packageInfo = mContext.getPackageManager().getPackageInfo(mContext.getPackageName(), 64);
            if (packageInfo != null) {
                StringBuilder sb = new StringBuilder(packageInfo.packageName);
                if (packageInfo.signatures != null) {
                    for (Signature sign : packageInfo.signatures) {
                        sb.append(":");
                        sb.append(byteToHex(sign.toByteArray()));
                    }
                }
                return sha256(sb.toString().toLowerCase(Locale.getDefault()));
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("PackageUtil", e.getMessage());
        }
        return "";
    }

    @TargetApi(19)
    private static String sha256(String msg) {
        try {
            return bytesToHexString(MessageDigest.getInstance(SHA_256.ALGORITHM_SHA256).digest(msg.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            Log.e("PackageUtil", e.getMessage());
            return null;
        }
    }

    public static final String bytesToHexString(byte[] bArray) {
        if (bArray == null || bArray.length == 0) {
            return null;
        }
        StringBuffer sb = new StringBuffer(bArray.length);
        for (byte b : bArray) {
            String sTemp = Integer.toHexString(255 & b);
            if (sTemp.length() < 2) {
                sb.append(0);
            }
            sb.append(sTemp.toLowerCase(Locale.getDefault()));
        }
        return sb.toString();
    }

    public static String byteToHex(byte[] data) {
        if (data == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (byte dat : data) {
            if ((dat & 255) < 16) {
                builder.append('0');
            }
            builder.append(Integer.toHexString(dat & 255));
        }
        return builder.toString();
    }
}
