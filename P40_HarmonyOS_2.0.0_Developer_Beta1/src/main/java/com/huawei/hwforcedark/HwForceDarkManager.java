package com.huawei.hwforcedark;

import android.R;
import android.app.UiModeManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.view.WindowManager;
import com.huawei.android.content.pm.HwPackageManager;
import com.huawei.android.content.pm.PackageManagerExt;
import com.huawei.android.hwforcedark.IHwForceDarkManager;
import com.huawei.android.os.storage.StorageManagerExt;
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
    private static final String KEY_SRE_ACTIVE = "persist.sys.hw.sre_forcecolor_active";
    private static final String KEY_SRE_FORCE_COLOR_STATE = "hw_sc.sre_forcecolor_state";
    private static final int MSG_CHECK_SRE_SATE = 1;
    private static final int POLICY_BRANDCOLOR = 2;
    private static final int POLICY_DISABLE = 0;
    private static final int POLICY_ENABLE = 1;
    private static final int POLICY_FORCEDARK = 1;
    private static final int POLICY_READABILITY = 4;
    private static final int PROCESS_SHIFT = 4;
    private static final int SRE_CHECK_REFRESH_COUNT = 50;
    private static final int SRE_FEATURE_STATE = SystemProperties.getInt(KEY_SRE_FORCE_COLOR_STATE, 0);
    private static final int SRE_STATE_ALWAYS_ON = 2;
    private static final int SRE_STATE_DISABLE = 0;
    private static final int SRE_STATE_LUX_TRIGGER = 1;
    private static final int SYSTEM_APP_PROCESS = 2;
    private static final String TAG = "HwForceDarkManager";
    private static final int TAGNAME_END_IDX = 4;
    private static final int TAGNAME_START_IDX = 1;
    private static final int THIRD_APP_PROCESS = 0;
    private static final String TYPE_TAGNAME = "<type";
    private static final int UNKNOW_APP_TYPE = -1;
    private static final int UNKNOW_PROCESS = -1;
    private static final int UNKNOW_THEME = -1;
    private static HashSet<String> sBlackListSet = new HashSet<>();
    private static int[] sHuaweiAccentColor = {-16744961, -15884293, -16748315, -16749346, -15041567, -12609559};
    private static HwForceDarkManager sInstances = new HwForceDarkManager();
    private static SparseArray<int[]> sPolicyParams = new SparseArray<>();
    private static String sPolicyParamsStr = null;
    private boolean isCurrentInSysNightMode = false;
    private int mAppType = -1;
    private String mCurrProcessPackageName = null;
    private int mCurrProcessState = -1;
    private Handler mHandler = new Handler() {
        /* class com.huawei.hwforcedark.HwForceDarkManager.AnonymousClass1 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                HwForceDarkManager.this.updateSreForceColorState();
            }
        }
    };
    private boolean mIsConfigChangeState = false;
    private boolean mIsPackageNameChange = false;
    private boolean mIsSreForceColorActive = false;
    private String mLastProcessPackageName = null;
    private int mSreStateCheckCounter = 0;

    static {
        sBlackListSet.add(HwSecImmHelper.SECURE_IME_PACKAGENAME);
        sBlackListSet.add("com.baidu.input_huawei");
        sBlackListSet.add("android");
        sBlackListSet.add("androidhwxt");
        sBlackListSet.add("com.android.internal");
        sBlackListSet.add("com.android.systemui");
        sBlackListSet.add(PackageManagerExt.HW_LAUNCHER_PACKAGE_NAME);
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
        this.mIsConfigChangeState = true;
        if (context == null) {
            return 0;
        }
        this.isCurrentInSysNightMode = isSettingsNightMode(context);
        if (!this.isCurrentInSysNightMode) {
            if (SRE_FEATURE_STATE != 0) {
                updateSreForceColorState();
            }
            return 0;
        }
        initProcessState(context);
        if (this.mCurrProcessState == 2) {
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
        String str = this.mCurrProcessPackageName;
        if (str != null && !str.equals(packageName)) {
            this.mCurrProcessState = -1;
            this.mIsPackageNameChange = true;
        }
        this.mCurrProcessPackageName = packageName;
        if (this.mCurrProcessState == -1) {
            if (isInBlackList(context) || isSystemAppDarkTheme(context)) {
                this.mCurrProcessState = 2;
            } else {
                this.mCurrProcessState = 0;
            }
        }
    }

    private boolean isAppForceDarkEnabled(Context context) {
        if (SystemProperties.getInt("persist.sys.hw.forcedark_policy", 0) == 0) {
            return false;
        }
        String packageName = context.getPackageName();
        String str = this.mLastProcessPackageName;
        if (str == null) {
            this.mLastProcessPackageName = packageName;
        } else if (packageName != null && !packageName.equals(str) && packageName.startsWith(this.mLastProcessPackageName)) {
            packageName = this.mLastProcessPackageName;
        }
        if (HwPackageManager.getForceDarkSetting(packageName) == 1) {
            return true;
        }
        return false;
    }

    private void readAppTypeConfFileTxt(Context context, String path) {
        if (this.mAppType == -1) {
            this.mAppType = 0;
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
                                    this.mAppType = appTypeValue;
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
        if (SRE_FEATURE_STATE != 0) {
            processSreForceColorState(context);
        }
        if (this.mCurrProcessState == 2) {
            if (!this.mIsPackageNameChange) {
                return false;
            }
            hwForceDarkState = 0;
        }
        if (context == null || canvas == null) {
            return false;
        }
        boolean isCurrentHwForceDark = true;
        if (hwForceDarkState != 1) {
            isCurrentHwForceDark = false;
        }
        if (!isViewAllowedForceDark) {
            isCurrentHwForceDark = false;
        }
        boolean isLastHuaweiForceDark = canvas.getAllowedHwForceDark();
        if (isCurrentHwForceDark == isLastHuaweiForceDark && !this.mIsConfigChangeState) {
            return isLastHuaweiForceDark;
        }
        this.mIsConfigChangeState = false;
        if (this.mAppType == -1) {
            this.mAppType = 0;
        }
        updateForceDarkState(context, canvas, isCurrentHwForceDark);
        return isCurrentHwForceDark;
    }

    public int updateHwForceDarkSystemUIVisibility(int systemUIVisiblity) {
        return systemUIVisiblity & -17 & -8193;
    }

    public void enableAccentColorPolicy(Context context, int[] srcColors, int[] dstColors) {
        if (!needLoadSystemAccentColor(srcColors, dstColors)) {
            enableBrandColorPolicy(srcColors, dstColors);
        } else if (context == null) {
            Log.d(TAG, "Args is error,return");
        } else {
            int[] systemDstColors = getSystemAccentColors(context);
            if (systemDstColors.length != 0) {
                int length = systemDstColors.length;
                int[] iArr = sHuaweiAccentColor;
                if (length == iArr.length) {
                    if (iArr[0] == systemDstColors[0] || iArr[0] == systemDstColors[1]) {
                        Log.d(TAG, "Accent Color doesn't change ,return");
                    } else {
                        enableBrandColorPolicy(iArr, systemDstColors);
                    }
                }
            }
        }
    }

    private static String getForceColorSettings() {
        if (sPolicyParamsStr == null) {
            StringBuilder settingStrBuilder = new StringBuilder();
            for (int i = 0; i < sPolicyParams.size(); i++) {
                settingStrBuilder.append(intArrayToString(sPolicyParams.valueAt(i)));
                if (i != sPolicyParams.size() - 1) {
                    settingStrBuilder.append(System.lineSeparator());
                }
            }
            sPolicyParamsStr = settingStrBuilder.toString();
        }
        return sPolicyParamsStr;
    }

    private boolean isInBlackList(Context context) {
        String packageName = context.getPackageName();
        if (packageName == null) {
            return false;
        }
        return sBlackListSet.contains(packageName);
    }

    private boolean canUseAutoDark(Context context) {
        TypedArray attributes = context.obtainStyledAttributes(R.styleable.Theme);
        boolean isUseAutoDark = attributes.getBoolean(278, true);
        attributes.recycle();
        return isUseAutoDark;
    }

    private boolean isSystemAppDarkTheme(Context context) {
        int i = this.mCurrProcessState;
        if (i == 2) {
            return true;
        }
        if (i != -1) {
            return false;
        }
        int systemflag = 0;
        try {
            systemflag = context.getPackageManager().getApplicationInfo(context.getPackageName(), 128).flags;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, "isSystemAppDarkTheme NameNotFoundException packageName=" + context.getPackageName());
        }
        boolean isSystemApp = (systemflag & 1) == 1;
        boolean isUpdateSystemApp = (systemflag & 128) == 128;
        if (isSystemApp || isUpdateSystemApp) {
            return true;
        }
        return false;
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

    private void updateForceDarkState(Context context, Canvas canvas, boolean enable) {
        int[] params;
        if (enable) {
            DisplayMetrics dm = context.getResources().getDisplayMetrics();
            params = new int[]{1, 1, this.mAppType, dm.widthPixels, dm.heightPixels, dm.densityDpi};
        } else {
            params = new int[]{1, 0};
        }
        canvas.updateCanvasForceColorPolicy(params);
        sPolicyParams.put(1, params);
        sPolicyParamsStr = null;
    }

    /* access modifiers changed from: private */
    /* JADX DEBUG: Can't convert new array creation: APUT found in different block: 0x0026: APUT  (r1v1 'params' int[]), (1 ??[boolean, int, float, short, byte, char]), (r3v1 int) */
    /* access modifiers changed from: public */
    private void updateSreForceColorState() {
        boolean isActive;
        int i = 0;
        if (this.isCurrentInSysNightMode) {
            isActive = false;
        } else {
            isActive = SRE_FEATURE_STATE == 2 || SystemProperties.getBoolean(KEY_SRE_ACTIVE, false);
        }
        if (isActive != this.mIsSreForceColorActive) {
            int[] params = new int[2];
            params[0] = 4;
            if (isActive) {
                i = 1;
            }
            params[1] = i;
            Canvas.updateGlobalForceColorPolicy(params);
            this.mIsSreForceColorActive = isActive;
            sPolicyParams.put(4, params);
            sPolicyParamsStr = null;
        }
    }

    private final void processSreForceColorState(Context context) {
        int i = SRE_FEATURE_STATE;
        if (i == 2) {
            updateSreForceColorState();
        } else if (i == 1) {
            this.mSreStateCheckCounter++;
            if (this.mSreStateCheckCounter > 50) {
                this.mHandler.sendEmptyMessage(1);
                this.mSreStateCheckCounter = 0;
            }
        }
    }

    private static boolean needLoadSystemAccentColor(int[] srcColors, int[] dstColors) {
        return srcColors == null || dstColors == null || srcColors.length != dstColors.length;
    }

    private static int[] getSystemAccentColors(Context context) {
        Context emuiContext = new ContextThemeWrapper(context, 33947662);
        TypedValue typedValue = new TypedValue();
        Resources.Theme emuiTheme = emuiContext.getTheme();
        emuiTheme.resolveAttribute(16843829, typedValue, true);
        int accentColor = emuiContext.getResources().getColor(typedValue.resourceId, emuiTheme);
        emuiTheme.resolveAttribute(33620152, typedValue, true);
        int accentColorInverse = emuiContext.getResources().getColor(typedValue.resourceId, emuiTheme);
        int fabPressed = accentColor;
        int colorId = context.getResources().getIdentifier("emui_fab_bg_pressed", "color", context.getPackageName());
        if (colorId != 0) {
            fabPressed = context.getResources().getColor(colorId, emuiTheme);
        }
        int darkFabPressed = accentColorInverse;
        int colorId2 = context.getResources().getIdentifier("emui_fab_bg_pressed_dark", "color", context.getPackageName());
        if (colorId2 != 0) {
            darkFabPressed = context.getResources().getColor(colorId2, emuiTheme);
        }
        new ContextThemeWrapper(context, 33947853);
        Resources.Theme emuiTheme2 = emuiContext.getTheme();
        emuiTheme2.resolveAttribute(16843829, typedValue, true);
        int darkAccentColor = emuiContext.getResources().getColor(typedValue.resourceId, emuiTheme2);
        emuiTheme2.resolveAttribute(33620227, typedValue, true);
        return (accentColor == 0 || accentColorInverse == 0) ? new int[0] : new int[]{accentColor, accentColorInverse, fabPressed, darkAccentColor, darkFabPressed, emuiContext.getResources().getColor(typedValue.resourceId, emuiTheme2)};
    }

    private static void enableBrandColorPolicy(int[] srcColors, int[] dstColors) {
        if (srcColors.length == dstColors.length) {
            int length = srcColors.length;
            int[] params = new int[((length + 1) << 1)];
            int index = 0 + 1;
            params[0] = 2;
            int index2 = index + 1;
            params[index] = 1;
            for (int i = 0; i < length; i++) {
                int index3 = index2 + 1;
                params[index2] = srcColors[i];
                index2 = index3 + 1;
                params[index3] = dstColors[i];
            }
            Canvas.updateGlobalForceColorPolicy(params);
            sPolicyParams.put(2, params);
            sPolicyParamsStr = null;
        }
    }

    private static void disableBrandColorPolicy() {
        int[] params = {2, 0};
        Canvas.updateGlobalForceColorPolicy(params);
        sPolicyParams.put(2, params);
        sPolicyParamsStr = null;
    }

    private static String intArrayToString(int[] params) {
        if (params == null) {
            return StorageManagerExt.INVALID_KEY_DESC;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.length; i++) {
            sb.append(params[i]);
            if (i != params.length - 1) {
                sb.append(",");
            }
        }
        return sb.toString();
    }
}
