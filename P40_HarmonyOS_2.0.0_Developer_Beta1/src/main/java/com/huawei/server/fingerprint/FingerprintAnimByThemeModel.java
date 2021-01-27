package com.huawei.server.fingerprint;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.util.Log;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.provider.SettingsEx;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FingerprintAnimByThemeModel {
    private static final boolean DEBUG = Log.isLoggable(TAG, 3);
    private static final int DEFAULT_ANIM_DURATION = 60;
    private static final List<String> EMPTY_LIST = Collections.emptyList();
    private static final String FINGER_PRINT_ANIM_TYPE = "fp_theme_dir";
    private static final float FLOAT_THRESHOLD = 1.0E-4f;
    private static final String FP_ANIM_KEYGUARD = "/res/light";
    private static final String FP_ANIM_PATH = "/hw_product/";
    private static final String FP_LIGHT_FPS = "fp_light_fps";
    private static final String FP_LIGHT_RES = "fp_light_res";
    public static final int MAX_AIM_PICUTURES_NUM = 30;
    private static final int MAX_DEBUG_COUNT = 100000;
    private static final String TAG = "FingerprintAnimByThemeModel";
    private static String sCurrentFpAnimDir;
    private static int sDebugDestoryAmount = 0;
    private static int sDebugLoadAmount = 0;
    private static String sFpAnimDir;

    private FingerprintAnimByThemeModel() {
    }

    public static List<String> preloadFilesFromPath(Context context) {
        if (context == null) {
            return Collections.emptyList();
        }
        String canonicalPath = FP_ANIM_PATH + sFpAnimDir + FP_ANIM_KEYGUARD;
        List<String> animFileNamesList = new ArrayList<>(30);
        try {
            File canonicalFiles = new File(canonicalPath).getCanonicalFile();
            String canonicalPath2 = canonicalFiles.getPath();
            if (!canonicalFiles.exists()) {
                Log.w(TAG, "predecodeResourceFromPath the folder does not exist ");
                return EMPTY_LIST;
            }
            String[] fileAnimNames = canonicalFiles.list();
            String fpLightRes = SettingsEx.Secure.getStringForUser(context.getContentResolver(), FP_LIGHT_RES, ActivityManagerEx.getCurrentUser());
            if (fpLightRes == null) {
                Log.e(TAG, "The fpAnimLightRes is null,use default");
                return EMPTY_LIST;
            }
            List<String> fpLightResNames = Arrays.asList(fpLightRes.split(","));
            if (fileAnimNames != null) {
                for (String fileAnimName : fileAnimNames) {
                    if (fpLightResNames.contains(fileAnimName)) {
                        animFileNamesList.add(canonicalPath2 + File.separator + fileAnimName);
                    }
                    if (animFileNamesList.size() == 30) {
                        break;
                    }
                }
            }
            Collections.sort(animFileNamesList);
            return animFileNamesList;
        } catch (SecurityException e) {
            Log.e(TAG, "Read files has SecurityException");
            return EMPTY_LIST;
        } catch (IOException e2) {
            Log.e(TAG, "getCanonicalFile files has IOException");
            return EMPTY_LIST;
        }
    }

    public static int getAnimationHeight(Context context) {
        List<String> animFiles;
        if (context == null || (animFiles = preloadFilesFromPath(context)) == null || animFiles.size() <= 0) {
            return 0;
        }
        return BitmapFactory.decodeFile(animFiles.get(0)).getHeight();
    }

    public static int getFpAnimFps(Context context) {
        if (context == null) {
            return DEFAULT_ANIM_DURATION;
        }
        return SettingsEx.Secure.getIntForUser(context.getContentResolver(), FP_LIGHT_FPS, (int) DEFAULT_ANIM_DURATION, ActivityManagerEx.getCurrentUser());
    }

    public static void setLoadAmount() {
        if (DEBUG) {
            int i = sDebugLoadAmount;
            sDebugLoadAmount = i < MAX_DEBUG_COUNT ? i + 1 : 0;
            Log.d(TAG, "load mFingerprintAnimViewByTheme amount is " + sDebugLoadAmount);
        }
    }

    public static void setDestoryAmount() {
        if (DEBUG) {
            int i = sDebugDestoryAmount;
            sDebugDestoryAmount = i < MAX_DEBUG_COUNT ? i + 1 : 0;
            Log.d(TAG, "destory mFingerprintAnimViewByTheme amount is " + sDebugDestoryAmount);
        }
    }

    public static boolean isThemChanged(FingerprintAnimByThemeView fingerprintAnimByThemeView, float currentScale) {
        if (fingerprintAnimByThemeView == null || !sCurrentFpAnimDir.equals(sFpAnimDir) || Math.abs(fingerprintAnimByThemeView.getScale() - currentScale) >= FLOAT_THRESHOLD) {
            sFpAnimDir = sCurrentFpAnimDir;
            return true;
        }
        Log.i(TAG, "the theme is same,use default");
        return false;
    }

    public static boolean isGetThemeError(Context context) {
        if (context == null) {
            return true;
        }
        sCurrentFpAnimDir = SettingsEx.Secure.getStringForUser(context.getContentResolver(), FINGER_PRINT_ANIM_TYPE, ActivityManagerEx.getCurrentUser());
        if (sCurrentFpAnimDir != null) {
            return false;
        }
        sFpAnimDir = null;
        Log.i(TAG, "we get the fpAnimDir is null and we use default theme");
        return true;
    }
}
