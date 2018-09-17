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
        Integer mode = (Integer) this.mSettingsDefaults.get(pkgName);
        if (mode != null) {
            return mode.intValue();
        }
        if (screenOrientation == 0) {
            return HwPCMultiWindowCompatibility.getLandscapeWithAllAction();
        }
        return HwPCMultiWindowCompatibility.getPortraitWithAllAction();
    }

    public int getAppDefaultMode(String pkgName) {
        Integer in = (Integer) this.mSettingsDefaults.get(pkgName);
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

    /* JADX WARNING: Removed duplicated region for block: B:27:0x00fa A:{SYNTHETIC, Splitter: B:27:0x00fa} */
    /* JADX WARNING: Removed duplicated region for block: B:72:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00ff A:{Catch:{ IOException -> 0x0103 }} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x0133 A:{SYNTHETIC, Splitter: B:44:0x0133} */
    /* JADX WARNING: Removed duplicated region for block: B:76:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x0138 A:{Catch:{ IOException -> 0x013d }} */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x014c A:{SYNTHETIC, Splitter: B:52:0x014c} */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x0151 A:{Catch:{ IOException -> 0x0155 }} */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x00fa A:{SYNTHETIC, Splitter: B:27:0x00fa} */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x00ff A:{Catch:{ IOException -> 0x0103 }} */
    /* JADX WARNING: Removed duplicated region for block: B:72:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x0133 A:{SYNTHETIC, Splitter: B:44:0x0133} */
    /* JADX WARNING: Removed duplicated region for block: B:47:0x0138 A:{Catch:{ IOException -> 0x013d }} */
    /* JADX WARNING: Removed duplicated region for block: B:76:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:52:0x014c A:{SYNTHETIC, Splitter: B:52:0x014c} */
    /* JADX WARNING: Removed duplicated region for block: B:55:0x0151 A:{Catch:{ IOException -> 0x0155 }} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void loadSettingsDefaults() {
        Throwable th;
        this.mSettingsDefaults = new HashMap();
        File file = new File(getDefaultFilePath());
        if (file.exists()) {
            FileInputStream fis = null;
            BufferedReader reader = null;
            try {
                BufferedReader reader2;
                FileInputStream fis2 = new FileInputStream(file);
                try {
                    reader2 = new BufferedReader(new InputStreamReader(fis2, Charset.defaultCharset()));
                } catch (FileNotFoundException e) {
                    fis = fis2;
                    try {
                        Log.e("MultiWindowManager", "loadSettingsDefaults FileNotFoundException");
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e2) {
                                Log.e("MultiWindowManager", "loadSettingsDefaults close IOException");
                                return;
                            }
                        }
                        if (fis != null) {
                            fis.close();
                            return;
                        }
                        return;
                    } catch (Throwable th2) {
                        th = th2;
                        if (reader != null) {
                        }
                        if (fis != null) {
                        }
                        throw th;
                    }
                } catch (IOException e3) {
                    fis = fis2;
                    Log.e("MultiWindowManager", "loadSettingsDefaults IOException");
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (IOException e4) {
                            Log.e("MultiWindowManager", "loadSettingsDefaults close IOException");
                            return;
                        }
                    }
                    if (fis != null) {
                        fis.close();
                        return;
                    }
                    return;
                } catch (Throwable th3) {
                    th = th3;
                    fis = fis2;
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
                try {
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
                            } catch (Exception e6) {
                                Log.e("MultiWindowManager", "WindowMode Parse Error: " + line);
                            }
                        }
                        line = reader2.readLine();
                    }
                    if (reader2 != null) {
                        try {
                            reader2.close();
                        } catch (IOException e7) {
                            Log.e("MultiWindowManager", "loadSettingsDefaults close IOException");
                            return;
                        }
                    }
                    if (fis2 != null) {
                        fis2.close();
                    }
                } catch (FileNotFoundException e8) {
                    reader = reader2;
                    fis = fis2;
                    Log.e("MultiWindowManager", "loadSettingsDefaults FileNotFoundException");
                    if (reader != null) {
                    }
                    if (fis != null) {
                    }
                } catch (IOException e9) {
                    reader = reader2;
                    fis = fis2;
                    Log.e("MultiWindowManager", "loadSettingsDefaults IOException");
                    if (reader != null) {
                    }
                    if (fis != null) {
                    }
                } catch (Throwable th4) {
                    th = th4;
                    reader = reader2;
                    fis = fis2;
                    if (reader != null) {
                    }
                    if (fis != null) {
                    }
                    throw th;
                }
            } catch (FileNotFoundException e10) {
                Log.e("MultiWindowManager", "loadSettingsDefaults FileNotFoundException");
                if (reader != null) {
                }
                if (fis != null) {
                }
            } catch (IOException e11) {
                Log.e("MultiWindowManager", "loadSettingsDefaults IOException");
                if (reader != null) {
                }
                if (fis != null) {
                }
            }
        } else {
            this.mSettingsDefaults.put("com.huawei.desktop.explorer", Integer.valueOf(HwPCMultiWindowCompatibility.getLandscapeWithAllAction()));
            this.mSettingsDefaults.put("com.chaozhuo.browser", Integer.valueOf(HwPCMultiWindowCompatibility.getLandscapeWithAllAction()));
            this.mSettingsDefaults.put("com.android.calendar", Integer.valueOf(HwPCMultiWindowCompatibility.getLandscapeWithAllAction()));
            this.mSettingsDefaults.put("com.android.email", Integer.valueOf(HwPCMultiWindowCompatibility.getLandscapeWithAllAction()));
            this.mSettingsDefaults.put("com.example.android.notepad", Integer.valueOf(HwPCMultiWindowCompatibility.getLandscapeWithAllAction()));
            this.mSettingsDefaults.put("com.huawei.android.internal.app", Integer.valueOf(4));
        }
    }
}
