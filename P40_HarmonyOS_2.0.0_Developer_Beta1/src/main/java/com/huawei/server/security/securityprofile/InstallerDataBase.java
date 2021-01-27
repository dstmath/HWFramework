package com.huawei.server.security.securityprofile;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.hwpartsecurityservices.BuildConfig;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class InstallerDataBase {
    private static final boolean DEBUG = SecurityProfileUtils.DEBUG;
    private static final int DEFAULT_PACKAGES_WITH_INSTALLER_CAPACITY = 128;
    private static final Object LOCK = new Object();
    private static final String PACKAGE_NAME = "packageName";
    private static final String PACKAGE_NAME_INSTALLER = "installer";
    private static final String TAG = "SecurityProfileInstaller";
    private static volatile InstallerDataBase sInstance = null;
    private final Path mFile = Paths.get("/data/system/securityprofileinstallerDB.json", new String[0]);

    private InstallerDataBase() {
    }

    public static InstallerDataBase getInstance() {
        if (sInstance == null) {
            synchronized (LOCK) {
                if (sInstance == null) {
                    sInstance = new InstallerDataBase();
                }
            }
        }
        return sInstance;
    }

    private String getInstallerPackageNameFromSysSafe(Context context, @NonNull String packageName) {
        PackageManager packageManager = context.getPackageManager();
        if (packageManager == null) {
            Log.e(TAG, "[getInstaller] mPackageManager is null");
            return null;
        }
        try {
            String outerInstaller = packageManager.getInstallerPackageName(packageName);
            if (TextUtils.isEmpty(outerInstaller)) {
                return null;
            }
            String installer = SecurityProfileUtils.replaceLineSeparator(outerInstaller);
            if (!TextUtils.isEmpty(installer) && DEBUG) {
                Log.d(TAG, "[From sys]" + packageName + " got installer from system api installer = " + installer);
            }
            return installer;
        } catch (IllegalArgumentException e) {
            Log.w(TAG, "getInstallerPackageName given packageName not installed");
            return null;
        }
    }

    public JSONArray setInstallerPackageName(Context context, String packageName) {
        JSONArray installers;
        if (context == null || TextUtils.isEmpty(packageName)) {
            Log.w(TAG, "[setInstallerPackageName] context is null or packageName error");
            return null;
        }
        String installer = getInstallerPackageNameFromSysSafe(context, packageName);
        try {
            String allInstallerInfo = readDataBase();
            if (!TextUtils.isEmpty(allInstallerInfo)) {
                installers = new JSONArray(allInstallerInfo);
                int index = indexOfJSONArray(installers, packageName);
                if (index > -1) {
                    installers.remove(index);
                }
            } else {
                installers = new JSONArray();
            }
            JSONObject installInfo = new JSONObject();
            installInfo.put("packageName", packageName);
            installInfo.put(PACKAGE_NAME_INSTALLER, installer);
            if (DEBUG) {
                Log.d(TAG, "add jsonObject = " + installInfo);
            }
            installers.put(installInfo);
            writeDataBase(installers.toString());
            return installers;
        } catch (JSONException e) {
            Log.w(TAG, "setInstallerPackageName JSONException: " + e.getMessage());
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    @Nullable
    public String getInstallerPackageName(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName)) {
            Log.w(TAG, "getInstallerPackageName args packageName null");
            return null;
        }
        String installer = getInstallerPackageNameFromSysSafe(context, packageName);
        if (TextUtils.isEmpty(installer)) {
            return getInstallerPackageNameFromDbSafe(packageName);
        }
        return installer;
    }

    private String getInstallerPackageNameFromDbSafe(@NonNull String packageName) {
        String installerList = readDataBase();
        if (installerList == null) {
            return null;
        }
        try {
            String installerSFS = parseJsonToHashMap(installerList).get(packageName);
            if (!TextUtils.isEmpty(installerSFS)) {
                installerSFS = SecurityProfileUtils.replaceLineSeparator(installerSFS);
                if (DEBUG) {
                    Log.d(TAG, "[From seapp]" + packageName + " got installer from seapp database installer = " + installerSFS);
                }
            }
            return installerSFS;
        } catch (JSONException e) {
            Log.w(TAG, "getInstallerPackageNameFromDbSafe JSONException: " + e.getMessage());
            return null;
        }
    }

    private String readDataBase() {
        if (!Files.exists(this.mFile, new LinkOption[0])) {
            Log.w(TAG, "install db file is not exist");
            return null;
        }
        try {
            return new String(Files.readAllBytes(this.mFile), StandardCharsets.UTF_8);
        } catch (IOException e) {
            Log.e(TAG, "readDataBase IOException: " + e.getMessage());
            return BuildConfig.FLAVOR;
        }
    }

    private void writeDataBase(String installerJson) {
        if (!Files.exists(this.mFile, new LinkOption[0])) {
            try {
                Files.write(this.mFile, installerJson.getBytes(StandardCharsets.UTF_8), new OpenOption[0]);
            } catch (IOException e) {
                Log.e(TAG, "writeDataBase IOException: " + e.getMessage());
            }
        } else {
            try {
                Files.write(this.mFile, installerJson.getBytes(StandardCharsets.UTF_8), new OpenOption[0]);
            } catch (IOException e2) {
                Log.e(TAG, "writeDataBase IOException: " + e2.getMessage());
            }
        }
    }

    @NonNull
    private Map<String, String> parseJsonToHashMap(String installerList) throws JSONException {
        Map<String, String> installers = new HashMap<>(128);
        JSONArray appInstalledInfoArray = new JSONArray(installerList);
        int length = appInstalledInfoArray.length();
        for (int i = 0; i < length; i++) {
            JSONObject appInstalledInfo = (JSONObject) appInstalledInfoArray.get(i);
            installers.put(appInstalledInfo.optString("packageName"), appInstalledInfo.optString(PACKAGE_NAME_INSTALLER));
        }
        return installers;
    }

    private int indexOfJSONArray(JSONArray jsonArray, String value) {
        if (jsonArray == null) {
            return -1;
        }
        int length = jsonArray.length();
        for (int i = 0; i < length; i++) {
            JSONObject jsonObject = jsonArray.optJSONObject(i);
            if (jsonObject != null && jsonObject.optString("packageName").equals(value)) {
                return i;
            }
        }
        return -1;
    }
}
