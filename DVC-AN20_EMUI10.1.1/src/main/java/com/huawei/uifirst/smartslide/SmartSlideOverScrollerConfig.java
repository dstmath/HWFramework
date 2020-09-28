package com.huawei.uifirst.smartslide;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.XmlResourceParser;
import android.os.Binder;
import android.util.Log;
import com.huawei.uikit.effect.BuildConfig;
import huawei.android.provider.HanziToPinyin;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParserException;

public class SmartSlideOverScrollerConfig {
    private static final String DECELERATION_TIME_CONSTANT = "decelerationTimeConstant";
    private static final String DECELERATION_TIME_CONSTANT_DEFAULT = "-0.405";
    private static final String DECELERATION_TIME_SLOPE = "decelerationTimeSlope";
    private static final String DECELERATION_TIME_SLOPE_DEFAULT = "0.528";
    private static final String DEVICE_SCREEN_SIZE_DEFAULT = "6.0";
    private static final String EXP_COEFFICIENT = "expCoefficient";
    private static final String EXP_COEFFICIENT_DEFAULT = "4.2";
    private static final String EXP_COFFICIENT_SLOW_DOWN = "expCofficientSlowDown";
    private static final String EXP_COFFICIENT_SLOW_DOWN_DEFAULT = "6.5";
    private static final String FLING_TIME_THRESHOLD = "flingTimeThreshold";
    private static final String FLING_TIME_THRESHOLD_DEFAULT = "900.0";
    private static final int ID_OVERSCROLLER_CONFIG = 34340864;
    private static final int INDEX = -1;
    private static final String IS_ENABLE = "isEnable";
    private static final String IS_ENABLE_DEFAULT = "true";
    private static final boolean IS_SCREEN_SIZE_FIX = true;
    private static final String LOG_TAG = "OverScrollerOptimization";
    private static final int MAX_READ_FROM_FILE = 3000;
    private static final String OVERSCROLLER_CONFIG = "overscroller_config";
    private static final String OVERSCROLLER_DEVICE_FILE_PATH = "sys/devices/virtual/graphics/fb0/lcd_model";
    private static final String PACKAGE_NAME = "packageName";
    private static final String SCREEN_DISPLAY_PIXELS = "screendisplaypixels";
    private static final float SCREEN_HEIGHT = 1920.0f;
    private static final float SCREEN_HEIGHT_DEFAULT = 1920.0f;
    private static final float SCREEN_WIDHT_DEFAULT = 1080.0f;
    private static final float SCREEN_WIDTH = 1080.0f;
    private static final String VELOCITY_MULTIPLIER = "velocityMultiplier";
    private static final String VELOCITY_MULTIPLIER_DEFAULT = "1.5";
    private HashMap<String, String> mReadConfigData = new HashMap<>();
    private float mScreenHeight = 1920.0f;
    private float mScreenWidth = 1080.0f;

    public SmartSlideOverScrollerConfig(Context context) {
        if (context != null) {
            this.mScreenHeight = (float) context.getResources().getDisplayMetrics().heightPixels;
            this.mScreenWidth = (float) context.getResources().getDisplayMetrics().widthPixels;
            return;
        }
        this.mScreenHeight = 1920.0f;
        this.mScreenWidth = 1080.0f;
    }

    public float getScreenHeight() {
        return this.mScreenHeight;
    }

    public float getScreenWidth() {
        return this.mScreenWidth;
    }

    public float getScreenSize() {
        float screenSize = Float.parseFloat(DEVICE_SCREEN_SIZE_DEFAULT);
        if (screenSize <= 0.0f) {
            return Float.parseFloat(DEVICE_SCREEN_SIZE_DEFAULT);
        }
        return screenSize;
    }

    private String getCallerProcessName(Context context) {
        if (context == null) {
            return BuildConfig.FLAVOR;
        }
        String callingApp = BuildConfig.FLAVOR;
        int uid = Binder.getCallingUid();
        PackageManager pm = context.getPackageManager();
        if (pm != null) {
            callingApp = pm.getNameForUid(uid);
        }
        if (callingApp == null) {
            return BuildConfig.FLAVOR;
        }
        if (callingApp.indexOf(":") != -1) {
            return callingApp.split(":")[0].trim();
        }
        return callingApp;
    }

    public float getScreenPpiByResources() {
        float screenSize = getScreenSize();
        float f = this.mScreenHeight;
        float f2 = this.mScreenWidth;
        return (float) (Math.sqrt((double) ((f * f) + (f2 * f2))) / ((double) screenSize));
    }

    private String readLcdDeviceFile(String lcdDeviceFile) throws IOException {
        StringBuilder lcdProp = null;
        try {
            BufferedReader stdin = new BufferedReader(new InputStreamReader(new FileInputStream(lcdDeviceFile), StandardCharsets.UTF_8));
            StringBuilder lcdProp2 = new StringBuilder();
            int intC = stdin.read();
            while (true) {
                if (intC == -1) {
                    break;
                }
                char chr = (char) intC;
                String lineSeparator = System.lineSeparator();
                if (lineSeparator.equals(chr + BuildConfig.FLAVOR)) {
                    break;
                } else if (lcdProp2.length() < MAX_READ_FROM_FILE) {
                    lcdProp2.append(chr);
                    intC = stdin.read();
                } else {
                    throw new IOException("input too long");
                }
            }
            String sb = lcdProp2.toString();
            lcdProp2.delete(0, lcdProp2.length());
            closeFileStreamNotThrow(stdin);
            return sb;
        } catch (Throwable th) {
            if (0 != 0) {
                lcdProp.delete(0, lcdProp.length());
            }
            closeFileStreamNotThrow(null);
            throw th;
        }
    }

    private void closeFileStreamNotThrow(Closeable fis) {
        if (fis != null) {
            try {
                fis.close();
            } catch (IOException e) {
                Log.e(LOG_TAG, "can't delete FileStream");
            }
        }
    }

    public String getScreenSizeByDeviceFile() {
        String screenSize = DEVICE_SCREEN_SIZE_DEFAULT;
        try {
            String[] arrs = readLcdDeviceFile(OVERSCROLLER_DEVICE_FILE_PATH).split(HanziToPinyin.Token.SEPARATOR);
            int length = arrs.length;
            int i = 0;
            while (true) {
                if (i >= length) {
                    break;
                }
                String string = arrs[i];
                if (string.indexOf("'") != -1) {
                    screenSize = string.substring(0, string.length() - 1);
                    break;
                }
                i++;
            }
            if (screenSize == null || screenSize.isEmpty()) {
                return DEVICE_SCREEN_SIZE_DEFAULT;
            }
            return screenSize;
        } catch (IOException e) {
            return DEVICE_SCREEN_SIZE_DEFAULT;
        }
    }

    private HashMap<String, String> parseXml(Context context, String packageName) {
        HashMap<String, String> configMap = new HashMap<>();
        XmlResourceParser overScrollerConfigXml = null;
        if (context == null) {
            return configMap;
        }
        try {
            XmlResourceParser overScrollerConfigXml2 = context.getResources().getXml(ID_OVERSCROLLER_CONFIG);
            if (overScrollerConfigXml2 == null) {
                if (overScrollerConfigXml2 != null) {
                    overScrollerConfigXml2.close();
                }
                return configMap;
            }
            int eventType = overScrollerConfigXml2.next();
            while (eventType != 1) {
                if (eventType != 2) {
                    eventType = overScrollerConfigXml2.next();
                } else {
                    int count = overScrollerConfigXml2.getAttributeCount();
                    if (overScrollerConfigXml2.getName().equals(OVERSCROLLER_CONFIG)) {
                        for (int i = 0; i < count; i++) {
                            configMap.put(overScrollerConfigXml2.getAttributeName(i), overScrollerConfigXml2.getAttributeValue(i));
                        }
                        if (configMap.get("packageName").equals(packageName)) {
                            overScrollerConfigXml2.close();
                            return configMap;
                        }
                        configMap.clear();
                    }
                    eventType = overScrollerConfigXml2.next();
                }
            }
            overScrollerConfigXml2.close();
            return configMap;
        } catch (XmlPullParserException e) {
            if (0 != 0) {
                overScrollerConfigXml.close();
            }
            return configMap;
        } catch (IOException e2) {
            if (0 != 0) {
                overScrollerConfigXml.close();
            }
            return configMap;
        } catch (Throwable th) {
            if (0 != 0) {
                overScrollerConfigXml.close();
            }
            throw th;
        }
    }

    public HashMap<String, String> getOverScrollerConfig(Context context) {
        Log.i(LOG_TAG, "get the overscroller config");
        String packageName = getCallerProcessName(context);
        if (packageName.length() == 0) {
            this.mReadConfigData.put(EXP_COEFFICIENT, EXP_COEFFICIENT_DEFAULT);
            this.mReadConfigData.put(DECELERATION_TIME_SLOPE, DECELERATION_TIME_SLOPE_DEFAULT);
            this.mReadConfigData.put(DECELERATION_TIME_CONSTANT, DECELERATION_TIME_CONSTANT_DEFAULT);
            this.mReadConfigData.put(FLING_TIME_THRESHOLD, FLING_TIME_THRESHOLD_DEFAULT);
            this.mReadConfigData.put(EXP_COFFICIENT_SLOW_DOWN, EXP_COFFICIENT_SLOW_DOWN_DEFAULT);
            this.mReadConfigData.put(VELOCITY_MULTIPLIER, VELOCITY_MULTIPLIER_DEFAULT);
            this.mReadConfigData.put(IS_ENABLE, IS_ENABLE_DEFAULT);
        } else {
            this.mReadConfigData = parseXml(context, packageName);
            isKeyExist(EXP_COEFFICIENT, EXP_COEFFICIENT_DEFAULT);
            isKeyExist(DECELERATION_TIME_SLOPE, DECELERATION_TIME_SLOPE_DEFAULT);
            isKeyExist(DECELERATION_TIME_CONSTANT, DECELERATION_TIME_CONSTANT_DEFAULT);
            isKeyExist(FLING_TIME_THRESHOLD, FLING_TIME_THRESHOLD_DEFAULT);
            isKeyExist(EXP_COFFICIENT_SLOW_DOWN, EXP_COFFICIENT_SLOW_DOWN_DEFAULT);
            isKeyExist(VELOCITY_MULTIPLIER, VELOCITY_MULTIPLIER_DEFAULT);
            isKeyExist(IS_ENABLE, IS_ENABLE_DEFAULT);
        }
        return this.mReadConfigData;
    }

    private void isKeyExist(String keyInput, String defaultValueInput) {
        if (!this.mReadConfigData.containsKey(keyInput)) {
            this.mReadConfigData.put(keyInput, defaultValueInput);
        }
    }
}
