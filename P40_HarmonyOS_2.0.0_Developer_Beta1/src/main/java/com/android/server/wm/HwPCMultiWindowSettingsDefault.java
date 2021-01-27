package com.android.server.wm;

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
    private static final String TAG = "MultiWindowManager";
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
            loadSettingsForFileNotExist();
        } else {
            loadSettingsForFileExist(file);
        }
    }

    private void loadSettingsForFileExist(File file) {
        FileInputStream fis = null;
        BufferedReader reader = null;
        try {
            FileInputStream fis2 = new FileInputStream(file);
            BufferedReader reader2 = new BufferedReader(new InputStreamReader(fis2, Charset.defaultCharset()));
            String line = reader2.readLine();
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
                        Log.e(TAG, "WindowMode Parse Error: " + line);
                    }
                }
                line = reader2.readLine();
            }
            try {
                reader2.close();
            } catch (IOException e2) {
                Log.e(TAG, "loadSettingsDefaults close IOException");
            }
            try {
                fis2.close();
            } catch (IOException e3) {
                Log.e(TAG, "loadSettingsDefaults close IOException");
            }
        } catch (FileNotFoundException e4) {
            Log.e(TAG, "loadSettingsDefaults FileNotFoundException");
            if (0 != 0) {
                try {
                    reader.close();
                } catch (IOException e5) {
                    Log.e(TAG, "loadSettingsDefaults close IOException");
                }
            }
            if (0 != 0) {
                fis.close();
            }
        } catch (IOException e6) {
            Log.e(TAG, "loadSettingsDefaults IOException");
            if (0 != 0) {
                try {
                    reader.close();
                } catch (IOException e7) {
                    Log.e(TAG, "loadSettingsDefaults close IOException");
                }
            }
            if (0 != 0) {
                fis.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    reader.close();
                } catch (IOException e8) {
                    Log.e(TAG, "loadSettingsDefaults close IOException");
                }
            }
            if (0 != 0) {
                try {
                    fis.close();
                } catch (IOException e9) {
                    Log.e(TAG, "loadSettingsDefaults close IOException");
                }
            }
            throw th;
        }
    }

    private void loadSettingsForFileNotExist() {
        this.mSettingsDefaults.putAll(new HashMap<String, Integer>() {
            /* class com.android.server.wm.HwPCMultiWindowSettingsDefault.AnonymousClass1 */

            {
                put("com.huawei.desktop.explorer", Integer.valueOf(HwPCMultiWindowCompatibility.getLandscapeWithAllAction()));
                put("com.chaozhuo.browser", Integer.valueOf(HwPCMultiWindowCompatibility.getLandscapeWithAllAction()));
                put("com.android.calendar", Integer.valueOf(HwPCMultiWindowCompatibility.getLandscapeWithAllAction()));
                put("com.huawei.calendar", Integer.valueOf(HwPCMultiWindowCompatibility.getLandscapeWithAllAction()));
                put("com.android.email", Integer.valueOf(HwPCMultiWindowCompatibility.getLandscapeWithAllAction()));
                put("com.huawei.email", Integer.valueOf(HwPCMultiWindowCompatibility.getLandscapeWithAllAction()));
                put("com.example.android.notepad", Integer.valueOf(HwPCMultiWindowCompatibility.getLandscapeWithAllAction()));
                put("com.huawei.notepad", Integer.valueOf(HwPCMultiWindowCompatibility.getLandscapeWithAllAction()));
                put("com.huawei.android.internal.app", 4);
            }
        });
    }
}
