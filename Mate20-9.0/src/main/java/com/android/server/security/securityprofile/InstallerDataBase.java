package com.android.server.security.securityprofile;

import android.content.Context;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class InstallerDataBase {
    private static InstallerDataBase sInstance = null;
    private final String DB_FILE_PATH = "/data/system/securityprofileinstallerDB.json";
    private final String PACKAGE_NAME = "packageName";
    private final String PACKAGE_NAME_INSTALLER = "installer";
    private final String TAG = "InstallerDataBase";
    private final String databaseName = "";
    private Path mFile = Paths.get("/data/system/securityprofileinstallerDB.json", new String[0]);
    PackageManager mPackageManager = null;

    private class AppInstallerInfo {
        private String installer;
        private String packageName;

        public AppInstallerInfo(String packageName2, String installer2) {
            this.installer = installer2;
            this.packageName = packageName2;
        }

        public String getPackageName() {
            return this.packageName;
        }

        public String getInstaller() {
            return this.installer;
        }

        public void setPackageName(String packageName2) {
            this.packageName = packageName2;
        }

        public void setInstaller(String installer2) {
            this.installer = installer2;
        }
    }

    public static InstallerDataBase getInstance() {
        if (sInstance == null) {
            sInstance = new InstallerDataBase();
        }
        return sInstance;
    }

    private InstallerDataBase() {
    }

    private String getInstallerFromSys(Context context, String packageName) {
        String installer = null;
        PackageManager packageManager = context.getPackageManager();
        if (packageManager == null) {
            Log.e("InstallerDataBase", "[getInstaller] mPackageManager is null");
            return null;
        }
        try {
            installer = packageManager.getInstallerPackageName(packageName);
            if (installer != null && !TextUtils.isEmpty(installer)) {
                Log.d("InstallerDataBase", "[From sys]" + packageName + " got installer from system api installer = " + installer);
            }
        } catch (IllegalArgumentException e) {
            Log.e("InstallerDataBase", "getInstaller IllegalArgumentException:" + e.getMessage());
        }
        return installer;
    }

    public JSONArray setInstallerPackageName(Context context, String packageName) {
        JSONArray res;
        if (context == null || packageName == null || TextUtils.isEmpty(packageName)) {
            Log.e("InstallerDataBase", "[addInstallerInfo] context is null or packagename error");
            return null;
        }
        String installer = getInstallerFromSys(context, packageName);
        try {
            String allInstallerInfo = readDataBase();
            if (allInstallerInfo == null || TextUtils.isEmpty(allInstallerInfo)) {
                res = new JSONArray();
            } else {
                res = new JSONArray(allInstallerInfo);
                int index = indexOfJSONArray(res, packageName);
                Log.d("InstallerDataBase", "index = " + index);
                if (index > -1) {
                    res.remove(index);
                }
            }
            JSONObject installInfo = new JSONObject();
            installInfo.put("packageName", packageName);
            installInfo.put("installer", installer);
            Log.d("InstallerDataBase", "add  jsonObject = " + installInfo);
            res.put(installInfo);
            writeDataBase(res.toString());
            return res;
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getInstaller(Context context, String packageName) {
        String installer = getInstallerFromSys(context, packageName);
        if (installer == null || TextUtils.isEmpty(installer)) {
            installer = getInstallerFromDb(packageName);
            if (installer != null && !TextUtils.isEmpty(installer)) {
                Log.d("InstallerDataBase", "[From seapp]" + packageName + " got installer from seapp database installer = " + installer);
            }
        }
        return installer;
    }

    private String getInstallerFromDb(String packageName) {
        if (packageName == null || TextUtils.isEmpty(packageName)) {
            Log.e("InstallerDataBase", "getInstallerFromDb input para error");
            return null;
        }
        String installerList = readDataBase();
        if (installerList == null) {
            return null;
        }
        try {
            return parseJSONHashMap(installerList).get(packageName);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String readDataBase() {
        String installerJson = "";
        if (!Files.exists(this.mFile, new LinkOption[0])) {
            Log.d("InstallerDataBase", "install db file is not exist");
            return null;
        }
        try {
            installerJson = new String(Files.readAllBytes(this.mFile), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return installerJson;
    }

    private void writeDataBase(String installerJson) {
        if (!Files.exists(this.mFile, new LinkOption[0])) {
            try {
                Files.write(this.mFile, installerJson.getBytes(StandardCharsets.UTF_8), new OpenOption[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                Files.write(this.mFile, installerJson.getBytes(StandardCharsets.UTF_8), new OpenOption[0]);
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }

    private HashMap<String, String> parseJSONHashMap(String installerList) throws JSONException {
        HashMap<String, String> res = new HashMap<>();
        JSONArray appInstalledInfoArray = new JSONArray(installerList);
        int length = appInstalledInfoArray.length();
        for (int i = 0; i < length; i++) {
            JSONObject appInstalledInfo = (JSONObject) appInstalledInfoArray.get(i);
            res.put(appInstalledInfo.optString("packageName"), appInstalledInfo.optString("installer"));
        }
        return res;
    }

    private ArrayList<AppInstallerInfo> parseJSONArrayList(String installerList) throws JSONException {
        ArrayList<AppInstallerInfo> res = new ArrayList<>();
        JSONArray appInstalledInfoArray = new JSONArray(installerList);
        int length = appInstalledInfoArray.length();
        for (int i = 0; i < length; i++) {
            JSONObject appInstalledInfo = (JSONObject) appInstalledInfoArray.get(i);
            res.add(new AppInstallerInfo(appInstalledInfo.optString("packageName"), appInstalledInfo.optString("installer")));
        }
        return res;
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

    private JSONArray generateJSON(ArrayList<AppInstallerInfo> appInstalledInfoArray) throws JSONException {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        int size = appInstalledInfoArray.size();
        for (int i = 0; i < size; i++) {
            jsonObject.put("packageName", appInstalledInfoArray.get(i).getPackageName());
            jsonObject.put("installer", appInstalledInfoArray.get(i).getInstaller());
            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }
}
