package com.android.server.appactcontrol;

import android.content.ComponentName;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.pm.PackageManagerService;
import com.android.server.pm.UserManagerService;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Iterator;
import libcore.util.HexEncoding;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class AppActUtils {
    private static final boolean IS_NO_GMS_SCHEME = TextUtils.isEmpty(SystemProperties.get("ro.com.google.gmsversion", ""));
    private static final int MAX_FILE_SIZE = 10485760;
    private static final String PRODUCT_APP_PATH = "/system/product/app";
    private static final String PRODUCT_PRIV_APP_PATH = "/system/product/priv-app";
    private static final String STRING_EMPTY = "";
    private static final String TAG = "AppActUtils";
    private static HashSet<String> forbidPreasUpdateSet = new HashSet<>();
    private static PackageManagerService sPms;

    public static void setPms(PackageManagerService pms) {
        sPms = pms;
    }

    public static void recordPreasApp(String pkgName, String appPath) {
        if (!IS_NO_GMS_SCHEME) {
            if (TextUtils.isEmpty(pkgName) || TextUtils.isEmpty(appPath)) {
                Log.i(TAG, "recordPreasApp pkgName or appPath is empty");
            } else if (appPath.startsWith(PRODUCT_PRIV_APP_PATH) || appPath.startsWith(PRODUCT_APP_PATH)) {
                forbidPreasUpdateSet.add(pkgName);
            }
        }
    }

    static boolean isPreasApp(String pkgName) {
        return forbidPreasUpdateSet.contains(pkgName);
    }

    static void setComponentState(HashSet<ComponentName> componentSet, boolean isDisable) {
        if (componentSet == null || sPms == null) {
            Log.e(TAG, "componentSet or sPms is null");
            return;
        }
        int state = isDisable ? 2 : 0;
        Iterator<ComponentName> it = componentSet.iterator();
        while (it.hasNext()) {
            ComponentName componentName = it.next();
            for (int userId : UserManagerService.getInstance().getUserIds()) {
                try {
                    sPms.setComponentEnabledSetting(componentName, state, 1, userId);
                } catch (IllegalArgumentException | SecurityException e) {
                    Log.e(TAG, "setComponentState IllegalArgumentException or SecurityException");
                } catch (Exception e2) {
                    Log.e(TAG, "setComponentState Exception");
                }
            }
        }
    }

    static boolean isSafeMode() {
        PackageManagerService packageManagerService = sPms;
        if (packageManagerService != null) {
            return packageManagerService.isSafeMode();
        }
        Log.e(TAG, "sPms is null, return");
        return false;
    }

    static HashSet<String> readPackageName(int xmlEventType, XmlPullParser xmlParser) throws XmlPullParserException, IOException {
        HashSet<String> packageNameSet = new HashSet<>();
        String nodeName = xmlParser.getName();
        while (true) {
            if (xmlEventType == 3 && !"item".equals(nodeName)) {
                return packageNameSet;
            }
            xmlEventType = xmlParser.next();
            nodeName = xmlParser.getName();
            if (xmlEventType == 2 && "item".equals(nodeName)) {
                packageNameSet.add(xmlParser.getAttributeValue(null, AppActConstant.ATTR_PACKAGE_NAME));
            }
        }
    }

    static boolean verifyFile(String filePath) {
        File file = new File(filePath);
        return file.exists() && file.length() < 10485760;
    }

    static String getHashCodeForString(String pkgName) {
        if (TextUtils.isEmpty(pkgName)) {
            return "";
        }
        try {
            return sha256(pkgName.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "getHashCodeForString UnsupportedEncodingException");
            return "";
        }
    }

    private static String sha256(byte[] data) {
        if (data == null) {
            return "";
        }
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(data);
            return HexEncoding.encodeToString(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            Log.e(TAG, "sha256 NoSuchAlgorithmException");
            return "";
        }
    }
}
