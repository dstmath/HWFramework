package com.huawei.uifirst.smartslide;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.XmlResourceParser;
import android.os.Binder;
import android.util.Log;
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
    private static final String IS_ENABLE = "isEnable";
    private static final String IS_ENABLE_DEFAULT = "true";
    private static final boolean IS_SCREEN_SIZE_FIX = true;
    private static final String LOG_TAG = "OverScrollerOptimization";
    private static final int MAX_READ_FROM_FILE = 3000;
    private static final String OVERSCROLLER_CONFIG = "overscroller_config";
    private static final String OVERSCROLLER_DEVICE_FILE_PATH = "sys/devices/virtual/graphics/fb0/lcd_model";
    private static final String PACKAGE_NAME = "packageName";
    private static final String SCREEN_DISPLAY_PIXELS = "screendisplaypixels";
    private static final float SCREEN_HEIGHT_DEFAULT = 1920.0f;
    private static final float SCREEN_WIDHT_DEFAULT = 1080.0f;
    private static final String VELOCITY_MULTIPLIER = "velocityMultiplier";
    private static final String VELOCITY_MULTIPLIER_DEFAULT = "1.5";
    private HashMap<String, String> mReadConfigData = new HashMap<>();
    private float mScreenHeight = SCREEN_HEIGHT_DEFAULT;
    private float mScreenWidth = SCREEN_WIDHT_DEFAULT;

    public SmartSlideOverScrollerConfig(Context context) {
        if (context != null) {
            this.mScreenHeight = (float) context.getResources().getDisplayMetrics().heightPixels;
            this.mScreenWidth = (float) context.getResources().getDisplayMetrics().widthPixels;
            return;
        }
        this.mScreenHeight = SCREEN_HEIGHT_DEFAULT;
        this.mScreenWidth = SCREEN_WIDHT_DEFAULT;
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
        String callingApp = "";
        int uid = Binder.getCallingUid();
        PackageManager pm = context.getPackageManager();
        if (pm != null) {
            callingApp = pm.getNameForUid(uid);
        }
        if (callingApp == null) {
            return "";
        }
        if (callingApp.indexOf(":") != -1) {
            callingApp = callingApp.split(":")[0].trim();
        }
        return callingApp;
    }

    public float getScreenHeight_ByResources(Context context) {
        return (float) context.getResources().getDisplayMetrics().heightPixels;
    }

    public float getScreenWidth_ByResources(Context context) {
        return (float) context.getResources().getDisplayMetrics().widthPixels;
    }

    public float getScreenPPI_ByResources(Context context) {
        return (float) (Math.sqrt((double) ((this.mScreenHeight * this.mScreenHeight) + (this.mScreenWidth * this.mScreenWidth))) / ((double) getScreenSize()));
    }

    private String readLcdDeviceFile(String lcdDeviceFile) throws Exception {
        StringBuilder lcdProp = null;
        try {
            BufferedReader stdin = new BufferedReader(new InputStreamReader(new FileInputStream(lcdDeviceFile), StandardCharsets.UTF_8));
            StringBuilder lcdProp2 = new StringBuilder();
            while (true) {
                int read = stdin.read();
                int intC = read;
                if (read == -1) {
                    break;
                }
                char c = (char) intC;
                if (c == 10) {
                    break;
                } else if (lcdProp2.length() < MAX_READ_FROM_FILE) {
                    lcdProp2.append(c);
                } else {
                    throw new Exception("input too long");
                }
            }
            String sb = lcdProp2.toString();
            lcdProp2.delete(0, lcdProp2.length());
            closeFileStreamNotThrow(stdin);
            return sb;
        } catch (Throwable th) {
            if (lcdProp != null) {
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

    public String getScreenSize_ByDeviceFile() {
        String screenSize = DEVICE_SCREEN_SIZE_DEFAULT;
        try {
            String[] arr = readLcdDeviceFile(OVERSCROLLER_DEVICE_FILE_PATH).split(" ");
            int i = 0;
            while (true) {
                if (i >= arr.length) {
                    break;
                } else if (arr[i].indexOf("'") != -1) {
                    screenSize = arr[i].substring(0, arr[i].length() - 1);
                    break;
                } else {
                    i++;
                }
            }
            if (screenSize == null || screenSize.isEmpty()) {
                screenSize = DEVICE_SCREEN_SIZE_DEFAULT;
            }
            return screenSize;
        } catch (Exception e) {
            return DEVICE_SCREEN_SIZE_DEFAULT;
        }
    }

    private HashMap<String, String> parseXML(Context context, String packageName) {
        HashMap<String, String> configMap = new HashMap<>();
        XmlResourceParser overScrollerConfigXml = null;
        try {
            overScrollerConfigXml = context.getResources().getXml(ID_OVERSCROLLER_CONFIG);
            if (overScrollerConfigXml != null) {
                for (int eventType = overScrollerConfigXml.next(); eventType != 1; eventType = overScrollerConfigXml.next()) {
                    if (eventType == 2) {
                        int count = overScrollerConfigXml.getAttributeCount();
                        if (overScrollerConfigXml.getName().equals(OVERSCROLLER_CONFIG)) {
                            for (int i = 0; i < count; i++) {
                                configMap.put(overScrollerConfigXml.getAttributeName(i), overScrollerConfigXml.getAttributeValue(i));
                            }
                            if (configMap.get("packageName").equals(packageName)) {
                                if (overScrollerConfigXml != null) {
                                    overScrollerConfigXml.close();
                                }
                                return configMap;
                            }
                            configMap.clear();
                        }
                    }
                }
            }
            if (overScrollerConfigXml != null) {
                overScrollerConfigXml.close();
            }
            return configMap;
        } catch (XmlPullParserException e) {
            if (overScrollerConfigXml != null) {
                overScrollerConfigXml.close();
            }
            return configMap;
        } catch (IOException e2) {
            if (overScrollerConfigXml != null) {
                overScrollerConfigXml.close();
            }
            return configMap;
        } catch (Throwable th) {
            if (overScrollerConfigXml != null) {
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
            this.mReadConfigData = parseXML(context, packageName);
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
