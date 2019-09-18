package com.android.server.am;

import android.content.res.HwPCMultiWindowCompatibility;
import android.text.TextUtils;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;

public class HwPCMultiWindowSettingsDefault {
    private static final String DEFAULT_FILE_PATH = "/system/etc/multiwindow_compat_app.conf";
    public HashMap<String, Integer> mSettingsDefaults;

    public HwPCMultiWindowSettingsDefault(HwPCMultiWindowManager settings) {
        loadSettingsDefaults();
    }

    public int getAppDefaultMode(String pkgName, int screenOrientation) {
        Integer mode = this.mSettingsDefaults.get(pkgName);
        if (mode != null) {
            return mode.intValue();
        }
        if (screenOrientation == 0) {
            return HwPCMultiWindowCompatibility.getLandscapeWithAllAction();
        }
        return HwPCMultiWindowCompatibility.getPortraitWithAllAction();
    }

    public int getAppDefaultMode(String pkgName) {
        Integer in = this.mSettingsDefaults.get(pkgName);
        if (in != null) {
            return in.intValue();
        }
        return HwPCMultiWindowCompatibility.getPortraitWithAllAction();
    }

    public void setAppDefaultMode(String pkgName, int mode) {
        if (!TextUtils.isEmpty(pkgName)) {
            this.mSettingsDefaults.put(pkgName, Integer.valueOf(mode));
        }
    }

    private String getDefaultFilePath() {
        return DEFAULT_FILE_PATH;
    }

    private void loadSettingsDefaults() {
        this.mSettingsDefaults = new HashMap<>();
        File file = new File(getDefaultFilePath());
        if (!file.exists()) {
            this.mSettingsDefaults.put("com.huawei.desktop.explorer", Integer.valueOf(HwPCMultiWindowCompatibility.getLandscapeWithAllAction()));
            this.mSettingsDefaults.put("com.chaozhuo.browser", Integer.valueOf(HwPCMultiWindowCompatibility.getLandscapeWithAllAction()));
            this.mSettingsDefaults.put("com.android.calendar", Integer.valueOf(HwPCMultiWindowCompatibility.getLandscapeWithAllAction()));
            this.mSettingsDefaults.put("com.android.email", Integer.valueOf(HwPCMultiWindowCompatibility.getLandscapeWithAllAction()));
            this.mSettingsDefaults.put("com.example.android.notepad", Integer.valueOf(HwPCMultiWindowCompatibility.getLandscapeWithAllAction()));
            this.mSettingsDefaults.put("com.huawei.android.internal.app", 4);
            return;
        }
        FileInputStream fis = null;
        BufferedReader reader = null;
        try {
            fis = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(fis, Charset.defaultCharset()));
            String line = reader.readLine();
            if (line != null) {
                line = line.trim();
            }
            while (line != null) {
                int index = line.indexOf(32);
                if (index != -1) {
                    String packageName = line.substring(0, index);
                    try {
                        this.mSettingsDefaults.put(packageName, Integer.valueOf(Integer.parseInt(line.substring(index + 1).trim())));
                    } catch (Exception e) {
                        Log.e("MultiWindowManager", "WindowMode Parse Error: " + line);
                    }
                }
                line = reader.readLine();
            }
            try {
                reader.close();
                fis.close();
            } catch (IOException e2) {
                Log.e("MultiWindowManager", "loadSettingsDefaults close IOException");
            }
        } catch (FileNotFoundException e3) {
            Log.e("MultiWindowManager", "loadSettingsDefaults FileNotFoundException");
            if (reader != null) {
                reader.close();
            }
            if (fis != null) {
                fis.close();
            }
        } catch (IOException e4) {
            Log.e("MultiWindowManager", "loadSettingsDefaults IOException");
            if (reader != null) {
                reader.close();
            }
            if (fis != null) {
                fis.close();
            }
        } catch (Throwable th) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e5) {
                    Log.e("MultiWindowManager", "loadSettingsDefaults close IOException");
                    throw th;
                }
            }
            if (fis != null) {
                fis.close();
            }
            throw th;
        }
    }
}
