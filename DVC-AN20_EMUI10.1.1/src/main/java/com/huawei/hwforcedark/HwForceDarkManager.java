package com.huawei.hwforcedark;

import android.R;
import android.app.UiModeManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.SystemProperties;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import com.huawei.android.content.pm.HwPackageManager;
import com.huawei.android.hwforcedark.IHwForceDarkManager;
import huawei.android.view.inputmethod.HwSecImmHelper;
import huawei.cust.HwCfgFilePolicy;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

public class HwForceDarkManager implements IHwForceDarkManager {
    private static final int APP_NAME_END2END_LEN = 3;
    private static final int APP_NAME_START_IDX = 10;
    private static final String APP_TAGNAME = "<app";
    private static final int APP_TYPE_VAL_END2END_LEN = 2;
    private static final int APP_TYPE_VAL_START_IDX = 15;
    private static final String CONFIG_PATH = "xml/hw_forcedark_apptype_config.xml";
    private static final int DEFAULT_APP_TYPE = 0;
    private static final int DEFAULT_THEME = 0;
    private static final int DISABLE_HW_FORCE_DARK = 0;
    private static final int ENABLE_HW_FORCE_DARK = 1;
    private static final int HW_DARK_THEME = 1;
    private static final int PROCESS_SHIFT = 4;
    private static final int SYSTEM_APP_PROCESS = 2;
    private static final String TAG = "HwForceDarkManager";
    private static final int TAGNAME_END_IDX = 4;
    private static final int TAGNAME_START_IDX = 1;
    private static final int THIRD_APP_PROCESS = 0;
    private static final String TYPE_TAGNAME = "<type";
    private static final int UNKNOW_APP_TYPE = -1;
    private static final int UNKNOW_PROCESS = -1;
    private static final int UNKNOW_THEME = -1;
    private static final int WHITE_LIST_PROCESS = 1;
    private static HashSet<String> sBlackListSet = new HashSet<>();
    private static HwForceDarkManager sInstances = new HwForceDarkManager();
    private static HashSet<String> sWhiteListSet = new HashSet<>();
    private int appType = -1;
    private String currProcessPackageName = null;
    private int currProcessState = -1;
    private boolean isConfigChangeState = false;
    private String lastProcessPackageName = null;
    private boolean packageNameChange = false;

    static {
        sBlackListSet.add(HwSecImmHelper.SECURE_IME_PACKAGENAME);
        sBlackListSet.add("com.baidu.input_huawei");
        sBlackListSet.add("android");
        sBlackListSet.add("androidhwxt");
        sBlackListSet.add("com.android.internal");
        sBlackListSet.add("com.android.systemui");
        sBlackListSet.add("com.huawei.android.launcher");
        sBlackListSet.add("com.android.settings");
        sBlackListSet.add("com.android.mms");
        sBlackListSet.add("com.huawei.contacts");
        sBlackListSet.add("com.android.phone");
        sBlackListSet.add("com.android.incallui");
        sBlackListSet.add("com.android.gallery3d");
        sBlackListSet.add("com.huawei.photos");
        sBlackListSet.add("com.huawei.camera");
        sBlackListSet.add("com.huawei.calendar");
        sBlackListSet.add("com.huawei.email");
    }

    public static IHwForceDarkManager getDefault() {
        return sInstances;
    }

    public int updateHwForceDarkState(Context context, View rootView, WindowManager.LayoutParams lp) {
        this.isConfigChangeState = true;
        if (context == null) {
            return 0;
        }
        initProcessState(context);
        if (!isSettingsNightMode(context)) {
            return 0;
        }
        int i = this.currProcessState;
        if (i == 1 || i == 2) {
            return 1;
        }
        if (!isAppForceDarkEnabled(context) || !canUseAutoDark(context)) {
            return 0;
        }
        readAppTypeConfFileTxt(context, CONFIG_PATH);
        return 1;
    }

    private void initProcessState(Context context) {
        String packageName = context.getPackageName();
        String str = this.currProcessPackageName;
        if (str != null && !str.equals(packageName)) {
            this.currProcessState = -1;
            this.packageNameChange = true;
        }
        this.currProcessPackageName = packageName;
        if (this.currProcessState == -1) {
            if (isInWhiteList(context)) {
                this.currProcessState = 1;
            } else if (isInBlackList(context) || isHuaweiDarkTheme(context)) {
                this.currProcessState = 2;
            } else {
                this.currProcessState = 0;
            }
        }
    }

    private boolean isAppForceDarkEnabled(Context context) {
        if (SystemProperties.getInt("persist.sys.hw.forcedark_policy", 0) == 0) {
            return false;
        }
        String packageName = context.getPackageName();
        String str = this.lastProcessPackageName;
        if (str == null) {
            this.lastProcessPackageName = packageName;
        } else if (packageName != null && !packageName.equals(str) && packageName.startsWith(this.lastProcessPackageName)) {
            packageName = this.lastProcessPackageName;
        }
        if (HwPackageManager.getForceDarkSetting(packageName) == 1) {
            return true;
        }
        return false;
    }

    private void readAppTypeConfFileTxt(Context context, String path) {
        if (this.appType == -1) {
            this.appType = 0;
            File file = HwCfgFilePolicy.getCfgFile(path, 0);
            if (file != null) {
                FileReader fileReader = null;
                BufferedReader bufferedReader = null;
                try {
                    FileReader fileReader2 = new FileReader(file);
                    BufferedReader bufferedReader2 = new BufferedReader(fileReader2);
                    int appTypeValue = -1;
                    String appPackageName = context.getPackageName();
                    while (true) {
                        String initAppInfo = bufferedReader2.readLine();
                        if (initAppInfo != null) {
                            String appInfo = initAppInfo.trim();
                            int length = appInfo.length();
                            if (appInfo.startsWith(APP_TAGNAME)) {
                                if (appInfo.substring(10, length - 3).equals(appPackageName)) {
                                    this.appType = appTypeValue;
                                    break;
                                }
                            } else if (appInfo.startsWith(TYPE_TAGNAME)) {
                                appTypeValue = Integer.parseInt(appInfo.substring(15, length - 2));
                            }
                        }
                    }
                    try {
                        bufferedReader2.close();
                        fileReader2.close();
                    } catch (IOException e) {
                        Log.e(TAG, "readAppTypeConfFileTxt close file IOException");
                    }
                } catch (FileNotFoundException e2) {
                    Log.e(TAG, "readAppTypeConfFileTxt FileNotFoundException");
                    if (0 != 0) {
                        bufferedReader.close();
                    }
                    if (0 != 0) {
                        fileReader.close();
                    }
                } catch (IOException e3) {
                    Log.e(TAG, "readAppTypeConfFileTxt IOException");
                    if (0 != 0) {
                        bufferedReader.close();
                    }
                    if (0 != 0) {
                        fileReader.close();
                    }
                } catch (Throwable th) {
                    if (0 != 0) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e4) {
                            Log.e(TAG, "readAppTypeConfFileTxt close file IOException");
                            throw th;
                        }
                    }
                    if (0 != 0) {
                        fileReader.close();
                    }
                    throw th;
                }
            }
        }
    }

    public boolean setAllowedHwForceDark(Context context, Canvas canvas, int hwForceDarkState, boolean isViewAllowedForceDark, WindowManager.LayoutParams lp) {
        if (this.currProcessState == 2) {
            if (!this.packageNameChange) {
                return false;
            }
            hwForceDarkState = 0;
        }
        if (context == null || canvas == null) {
            return false;
        }
        boolean isCurrentHwForceDark = hwForceDarkState == 1;
        if (!isViewAllowedForceDark) {
            isCurrentHwForceDark = false;
        }
        boolean isLastHuaweiForceDark = canvas.getAllowedHwForceDark();
        if (isCurrentHwForceDark == isLastHuaweiForceDark && !this.isConfigChangeState) {
            return isLastHuaweiForceDark;
        }
        this.isConfigChangeState = false;
        if (lp != null) {
            int i = lp.type;
        }
        if (lp != null) {
            int windowType = lp.type;
            if (lp.y > 0 && lp.height == -2) {
                int i2 = 1000;
                if (windowType >= 1000) {
                    i2 = windowType;
                }
            }
        }
        if (this.appType == -1) {
            this.appType = 0;
        }
        canvas.setAllowedHwForceDark(isCurrentHwForceDark, this.appType, context.getResources().getDisplayMetrics().widthPixels);
        return isCurrentHwForceDark;
    }

    public int updateHwForceDarkSystemUIVisibility(int systemUIVisiblity) {
        return systemUIVisiblity & -17 & -8193;
    }

    private boolean isInBlackList(Context context) {
        String packageName = context.getPackageName();
        if (packageName == null) {
            return false;
        }
        return sBlackListSet.contains(packageName);
    }

    private boolean isInWhiteList(Context context) {
        String packageName = context.getPackageName();
        if (packageName == null) {
            return false;
        }
        boolean result = sWhiteListSet.contains(packageName);
        if (result) {
            this.appType = 2;
        }
        return result;
    }

    private boolean canUseAutoDark(Context context) {
        TypedArray attributes = context.obtainStyledAttributes(R.styleable.Theme);
        boolean isUseAutoDark = attributes.getBoolean(278, true);
        attributes.recycle();
        return isUseAutoDark;
    }

    private boolean isHuaweiDarkTheme(Context context) {
        int i = this.currProcessState;
        if (i == 2) {
            return true;
        }
        if (i != -1) {
            return false;
        }
        int systemflag = 0;
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 128);
            if (appInfo.metaData != null && (appInfo.metaData.getInt("hw.theme_type", 0) & 1) == 1) {
                return true;
            }
            systemflag = appInfo.flags;
            boolean isSystemApp = (systemflag & 1) == 1;
            boolean isUpdateSystemApp = (systemflag & 128) == 128;
            if (isSystemApp || isUpdateSystemApp) {
                return true;
            }
            return false;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "isHuaweiDarkTheme NameNotFoundException packageName=" + context.getPackageName());
        }
    }

    private boolean isSettingsNightMode(Context context) {
        try {
            UiModeManager uiModeManager = (UiModeManager) context.getSystemService(UiModeManager.class);
            if (uiModeManager != null && uiModeManager.getNightMode() == 2) {
                return true;
            }
            return false;
        } catch (RuntimeException e) {
            Log.e(TAG, "getSettingsNightMode failed : " + e.getMessage());
            return false;
        }
    }
}
