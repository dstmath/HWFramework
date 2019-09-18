package com.huawei.displayengine;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Slog;
import com.huawei.displayengine.DisplayEngineDBManager;
import com.huawei.displayengine.DisplayEngineDataCleanerXMLLoader;
import com.huawei.displayengine.IDisplayEngineServiceEx;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DisplayEngineDataCleaner {
    private static float COMFORT_ZONE_COUNTER_WEIGHT = 0.2f;
    private static float COUNTER_WEIGHT_THRES = 0.5f;
    private static boolean DEBUG = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static float OUTLIER_ZONE_COUNTER_WEIGHT = 0.5f;
    private static final int RANGE_FLAG_COMFORT = 1;
    private static final int RANGE_FLAG_DEFAULT = 0;
    private static final int RANGE_FLAG_INVALID = -1;
    private static final int RANGE_FLAG_OUTLIER = 3;
    private static final int RANGE_FLAG_SAFE = 2;
    private static float SAFE_ZONE_COUNTER_WEIGHT = 0.3f;
    private static final String TAG = "DE J DisplayEngineDataCleaner";
    private static int THRES_AL_DARK = 10;
    private static int THRES_HBM = 3000;
    private static final String XML_DIR = "/product/etc/display/effect/displayengine/";
    private static final String XML_EXT = ".xml";
    private static final String XML_FILENAME = "DataCleanerConfig.xml";
    private static final String XML_FILENAME_WITHOUT_EXT = "DataCleanerConfig";
    private static ArrayList<Integer> mAmbientLightLUT;
    private static ArrayList<Float> mBrightnessLevelLUT;
    private static ArrayList<Integer> mDarkLevelLUT;
    private static ArrayList<Float> mDarkLevelRoofLUT;
    private static volatile DisplayEngineDataCleaner mInstance;
    private static Object mLock = new Object();
    private static int mOutdoorLevelFloor;
    private static DisplayEngineDataCleanerXMLLoader.Data mParameters = DisplayEngineDataCleanerXMLLoader.getData(getXmlFileName());
    private Context mContext;

    static {
        setParameters();
    }

    private DisplayEngineDataCleaner(Context context) {
        this.mContext = context;
        DElog.v(TAG, "Default ambient curve: " + Arrays.toString(mAmbientLightLUT.toArray()));
        DElog.v(TAG, "Default brightness curve: " + Arrays.toString(mBrightnessLevelLUT.toArray()));
    }

    public static DisplayEngineDataCleaner getInstance(Context context) {
        if (mInstance == null) {
            synchronized (mLock) {
                if (mInstance == null) {
                    mInstance = new DisplayEngineDataCleaner(context);
                    DElog.d(TAG, "DisplayEngineDataCleaner initialized.");
                }
            }
        }
        return mInstance;
    }

    public ArrayList<Bundle> cleanData(ArrayList<Bundle> records, int userId) {
        DElog.d(TAG, "enter cleanData func.");
        if (userId < 0 || records == null || records.isEmpty()) {
            DElog.i(TAG, "clean data error! userId=" + userId);
            return null;
        }
        DElog.v(TAG, "start to  cleanData");
        int rangeFlag = calculateRangeFlag(records, userId);
        DElog.d(TAG, "rangeFlag=" + rangeFlag);
        if (rangeFlag == 0) {
            return null;
        }
        DElog.v(TAG, "raw records size:" + records.size());
        int curInd = 0;
        while (curInd < records.size()) {
            Bundle data = records.get(curInd);
            if (data != null) {
                if (data.getInt(DisplayEngineDBManager.DragInformationKey.GAMESTATE) == 1) {
                    records.remove(curInd);
                    curInd--;
                    DElog.i(TAG, "records index " + curInd + " removed because of game state == 1");
                } else {
                    float orgBrightnessLevel = getOriginalBrightnessLevel(data.getInt("AmbientLight"));
                    if (Float.compare(orgBrightnessLevel, 0.0f) > 0) {
                        Bundle data2 = cleanDataWithHumanFactorPolicy(data, rangeFlag, orgBrightnessLevel);
                        if (data2 != null) {
                            data2 = cleanDataWithDarkEnvironmentPolicy(data2, orgBrightnessLevel);
                        }
                        if (data2 != null) {
                            Bundle data3 = cleanDataWithHighBrightnessEnvironmentPolicy(data2);
                        }
                    } else {
                        DElog.v(TAG, "orgBrightnessLevel=" + orgBrightnessLevel);
                    }
                }
            }
            curInd++;
        }
        DElog.v(TAG, "cleaned records size:" + records.size());
        return records;
    }

    private int calculateRangeFlag(List<Bundle> records, int userId) {
        int rangeFlag;
        int rangeFlag2;
        float weightedCounter;
        List<Bundle> list = records;
        int i = userId;
        int ind = 0;
        if (i < 0 || list == null || records.isEmpty()) {
            return 0;
        }
        int previousRangeFlag = getRangeFlagByUserId(i);
        DElog.v(TAG, "previoudRangeFlag=" + previousRangeFlag);
        int safeZoneCounter = 0;
        int comfortZoneCounter = 0;
        int outlierZoneCounter = 0;
        float weightedCounter2 = 0.0f;
        int rangeFlag3 = false;
        DisplayEngineDBManager dbManager = DisplayEngineDBManager.getInstance(this.mContext);
        if (dbManager == null) {
            return 0;
        }
        while (ind < records.size()) {
            Bundle data = list.get(ind);
            boolean isCovered = data.getBoolean(DisplayEngineDBManager.DragInformationKey.PROXIMITYPOSITIVE);
            if (isCovered) {
                DElog.d(TAG, "isCovered=" + isCovered);
                weightedCounter = weightedCounter2;
                rangeFlag2 = rangeFlag3;
            } else {
                float startPoint = data.getFloat(DisplayEngineDBManager.DragInformationKey.STARTPOINT);
                float stopPoint = data.getFloat(DisplayEngineDBManager.DragInformationKey.STOPPOINT);
                int ambientLight = data.getInt("AmbientLight");
                weightedCounter = weightedCounter2;
                StringBuilder sb = new StringBuilder();
                rangeFlag2 = rangeFlag3;
                sb.append("startPoint=");
                sb.append(startPoint);
                sb.append(" stopPoint=");
                sb.append(stopPoint);
                sb.append(" ambientLight=");
                sb.append(ambientLight);
                DElog.v(TAG, sb.toString());
                float orgBrightnessLevel = getOriginalBrightnessLevel(ambientLight);
                StringBuilder sb2 = new StringBuilder();
                Bundle bundle = data;
                sb2.append("orgBrightnessLevel=");
                sb2.append(orgBrightnessLevel);
                DElog.v(TAG, sb2.toString());
                if (previousRangeFlag == 0) {
                    if (isDataInComfortZone(stopPoint, orgBrightnessLevel)) {
                        comfortZoneCounter++;
                    } else if (isDataInSafeZone(stopPoint, orgBrightnessLevel)) {
                        safeZoneCounter++;
                    } else {
                        outlierZoneCounter++;
                    }
                } else if (previousRangeFlag != 1) {
                    DElog.v(TAG, "previousRangeFlag=" + previousRangeFlag);
                } else if (!isDataInComfortZone(stopPoint, orgBrightnessLevel)) {
                    if (isDataInSafeZone(stopPoint, orgBrightnessLevel)) {
                        safeZoneCounter++;
                    } else {
                        outlierZoneCounter++;
                    }
                }
            }
            ind++;
            weightedCounter2 = weightedCounter;
            rangeFlag3 = rangeFlag2;
            list = records;
        }
        int i2 = rangeFlag3;
        DElog.d(TAG, "comfortZoneCounter=" + comfortZoneCounter + " safeZoneCounter=" + safeZoneCounter + " outlierZoneCounter=" + outlierZoneCounter);
        float weightedCounter3 = getWeightedCounter(comfortZoneCounter, safeZoneCounter, outlierZoneCounter);
        StringBuilder sb3 = new StringBuilder();
        sb3.append("weightedCounter=");
        sb3.append(weightedCounter3);
        sb3.append(" COUNTER_WEIGHT_THRES=");
        sb3.append(COUNTER_WEIGHT_THRES);
        DElog.d(TAG, sb3.toString());
        if (Float.compare(weightedCounter3, COUNTER_WEIGHT_THRES) > 0) {
            rangeFlag = 2;
        } else {
            rangeFlag = 1;
        }
        DElog.d(TAG, "rangeFlag=" + rangeFlag);
        Bundle data2 = new Bundle();
        data2.putInt("UserID", i);
        data2.putInt(DisplayEngineDBManager.DataCleanerKey.RANGEFLAG, rangeFlag);
        data2.putLong("TimeStamp", System.currentTimeMillis());
        dbManager.addorUpdateRecord("DataCleaner", data2);
        return rangeFlag;
    }

    private int getRangeFlagByUserId(int userId) {
        int rangeFlag = 0;
        if (userId < 0) {
            return 0;
        }
        List<Bundle> records = DisplayEngineDBManager.getInstance(this.mContext).getAllRecords("DataCleaner", new Bundle());
        if (records != null) {
            int curInd = 0;
            while (true) {
                if (curInd >= records.size()) {
                    break;
                }
                Bundle data = records.get(curInd);
                if (userId == data.getInt("UserID")) {
                    rangeFlag = data.getInt(DisplayEngineDBManager.DataCleanerKey.RANGEFLAG);
                    break;
                }
                curInd++;
            }
        }
        return rangeFlag;
    }

    private float getOriginalBrightnessLevel(int currentLight) {
        float orgBrightnessLevel;
        int ambientLutSize = mAmbientLightLUT.size();
        if (mAmbientLightLUT.isEmpty() || mBrightnessLevelLUT.isEmpty() || ambientLutSize != mBrightnessLevelLUT.size()) {
            DElog.i(TAG, "getOriginalBrightnessLevel invalid input!");
            return -1.0f;
        }
        int ind = 0;
        while (ind < ambientLutSize && mAmbientLightLUT.get(ind).intValue() <= currentLight) {
            ind++;
        }
        if (ind >= 1 && ind <= ambientLutSize - 1) {
            int a = mAmbientLightLUT.get(ind - 1).intValue();
            float alpha = ((float) (currentLight - a)) / ((float) (mAmbientLightLUT.get(ind).intValue() - a));
            orgBrightnessLevel = (mBrightnessLevelLUT.get(ind).floatValue() * alpha) + ((1.0f - alpha) * mBrightnessLevelLUT.get(ind - 1).floatValue());
        } else if (ind == 0) {
            orgBrightnessLevel = mBrightnessLevelLUT.get(0).floatValue();
        } else if (ind > ambientLutSize - 1) {
            orgBrightnessLevel = mBrightnessLevelLUT.get(ambientLutSize - 1).floatValue();
        } else {
            DElog.i(TAG, "Invalid ind!");
            orgBrightnessLevel = -1.0f;
        }
        return orgBrightnessLevel;
    }

    private boolean isDataInComfortZone(float stopPoint, float originalBrightnessLevel) {
        boolean z = false;
        if (Float.compare(stopPoint, 0.0f) < 0 || Float.compare(originalBrightnessLevel, 0.0f) < 0) {
            return false;
        }
        float comfortRoof = originalBrightnessLevel * 1.5f;
        if (stopPoint >= originalBrightnessLevel / 1.5f && stopPoint <= comfortRoof) {
            z = true;
        }
        return z;
    }

    private boolean isDataInSafeZone(float stopPoint, float originalBrightnessLevel) {
        boolean z = false;
        if (Float.compare(stopPoint, 0.0f) < 0 || Float.compare(originalBrightnessLevel, 0.0f) < 0) {
            return false;
        }
        float safeRoof = originalBrightnessLevel * 3.0f;
        if (stopPoint >= originalBrightnessLevel / 3.0f && stopPoint <= safeRoof) {
            z = true;
        }
        return z;
    }

    private float getWeightedCounter(int comfortZoneCounter, int safeZoneCounter, int outlierZoneCounter) {
        int sumCounter = comfortZoneCounter + safeZoneCounter + outlierZoneCounter;
        if (sumCounter == 0) {
            return 0.0f;
        }
        return (((COMFORT_ZONE_COUNTER_WEIGHT * ((float) comfortZoneCounter)) + (SAFE_ZONE_COUNTER_WEIGHT * ((float) safeZoneCounter))) + (OUTLIER_ZONE_COUNTER_WEIGHT * ((float) outlierZoneCounter))) / ((float) sumCounter);
    }

    private static void setParameters() {
        COMFORT_ZONE_COUNTER_WEIGHT = mParameters.comfortZoneCounterWeight;
        SAFE_ZONE_COUNTER_WEIGHT = mParameters.safeZoneCounterWeight;
        OUTLIER_ZONE_COUNTER_WEIGHT = mParameters.outlierZoneCounterWeight;
        COUNTER_WEIGHT_THRES = mParameters.counterWeightThresh;
        THRES_AL_DARK = mParameters.alDarkThresh;
        THRES_HBM = mParameters.hbmTresh;
        mOutdoorLevelFloor = mParameters.outDoorLevelFloor;
        mAmbientLightLUT = mParameters.ambientLightLUT;
        mBrightnessLevelLUT = mParameters.brightnessLevelLUT;
        mDarkLevelLUT = mParameters.darkLevelLUT;
        mDarkLevelRoofLUT = mParameters.darkLevelRoofLUT;
    }

    private Bundle cleanDataWithHumanFactorPolicy(Bundle rawData, int rangeFlag, float orgBrightnessLevel) {
        if (rawData == null) {
            return null;
        }
        DElog.d(TAG, "startPoint=" + rawData.getFloat(DisplayEngineDBManager.DragInformationKey.STARTPOINT) + " stopPoint=" + rawData.getFloat(DisplayEngineDBManager.DragInformationKey.STOPPOINT));
        if (rangeFlag == 2) {
            rawData.putFloat(DisplayEngineDBManager.DragInformationKey.STOPPOINT, clamp(3.0f * orgBrightnessLevel, orgBrightnessLevel / 3.0f, rawData.getFloat(DisplayEngineDBManager.DragInformationKey.STOPPOINT)));
        } else if (rangeFlag == 1) {
            rawData.putFloat(DisplayEngineDBManager.DragInformationKey.STOPPOINT, clamp(1.5f * orgBrightnessLevel, orgBrightnessLevel / 1.5f, rawData.getFloat(DisplayEngineDBManager.DragInformationKey.STOPPOINT)));
        }
        DElog.d(TAG, "cleaned stopPoint=" + rawData.getFloat(DisplayEngineDBManager.DragInformationKey.STOPPOINT));
        return rawData;
    }

    private Bundle cleanDataWithDarkEnvironmentPolicy(Bundle rawData, float orgBrightnessLevel) {
        if (rawData == null) {
            DElog.i(TAG, "rawData is null.");
            return null;
        }
        int ambientLight = rawData.getInt("AmbientLight");
        float stopPoint = rawData.getFloat(DisplayEngineDBManager.DragInformationKey.STOPPOINT);
        DElog.v(TAG, "cleanDataWithDarkEnvironmentPolicy stopPoint=" + stopPoint + " ambientLight=" + ambientLight);
        if (ambientLight >= 0 && ambientLight < THRES_AL_DARK) {
            int index = mDarkLevelLUT.indexOf(Integer.valueOf((int) Math.floor((double) orgBrightnessLevel)));
            if (index >= 0) {
                float stopPoint2 = Float.compare(stopPoint, mDarkLevelRoofLUT.get(index).floatValue()) > 0 ? mDarkLevelRoofLUT.get(index).floatValue() : stopPoint;
                rawData.putFloat(DisplayEngineDBManager.DragInformationKey.STOPPOINT, stopPoint2);
                DElog.v(TAG, "cleanDataWithDarkEnvironmentPolicy new stopPoint=" + stopPoint2 + " roof=" + mDarkLevelRoofLUT.get(index));
            }
        }
        return rawData;
    }

    private Bundle cleanDataWithHighBrightnessEnvironmentPolicy(Bundle rawData) {
        if (rawData == null) {
            DElog.i(TAG, "rawData is null.");
            return null;
        }
        int ambientLight = rawData.getInt("AmbientLight");
        float stopPoint = rawData.getFloat(DisplayEngineDBManager.DragInformationKey.STOPPOINT);
        DElog.v(TAG, "cleanDataWithHighBrightnessEnvironmentPolicy stopPoint=" + stopPoint);
        if (ambientLight >= 0 && ambientLight >= THRES_HBM) {
            float stopPoint2 = Float.compare(stopPoint, (float) mOutdoorLevelFloor) > 0 ? stopPoint : (float) mOutdoorLevelFloor;
            rawData.putFloat(DisplayEngineDBManager.DragInformationKey.STOPPOINT, stopPoint2);
            DElog.v(TAG, "cleanDataWithHighBrightnessEnvironmentPolicy new stopPoint=" + stopPoint2);
        }
        return rawData;
    }

    private float clamp(float roof, float floor, float value) {
        float ret = value;
        if (Float.compare(ret, roof) > 0) {
            ret = roof;
        }
        if (Float.compare(ret, floor) < 0) {
            return floor;
        }
        return ret;
    }

    private static String getXmlFileName() {
        String panelName = getLcdPanelName();
        String panelVersion = getVersionFromLCD();
        String xmlPathWithPanelName = String.format("%s%s_%s%s", new Object[]{XML_DIR, XML_FILENAME_WITHOUT_EXT, panelName, XML_EXT});
        String xmlPathWithoutPanelName = String.format("%s%s", new Object[]{XML_DIR, XML_FILENAME});
        String xmlPathWithPanelNameAndPanelVersion = String.format("%s%s_%s_%s%s", new Object[]{XML_DIR, XML_FILENAME_WITHOUT_EXT, panelName, panelVersion, XML_EXT});
        File xmlFileWithPanelName = new File(xmlPathWithPanelName);
        File xmlFileWithoutPanelName = new File(xmlPathWithoutPanelName);
        if (new File(xmlPathWithPanelNameAndPanelVersion).exists()) {
            return xmlPathWithPanelNameAndPanelVersion;
        }
        if (xmlFileWithPanelName.exists()) {
            return xmlPathWithPanelName;
        }
        if (xmlFileWithoutPanelName.exists()) {
            return xmlPathWithoutPanelName;
        }
        Slog.i(TAG, "DataCleanerConfig.xml missing.");
        return null;
    }

    private static String getVersionFromLCD() {
        IBinder binder = ServiceManager.getService(DisplayEngineManager.SERVICE_NAME);
        String panelVersion = null;
        if (binder == null) {
            Slog.i(TAG, "getLcdPanelName() binder is null!");
            return null;
        }
        IDisplayEngineServiceEx mService = IDisplayEngineServiceEx.Stub.asInterface(binder);
        if (mService == null) {
            Slog.e(TAG, "getLcdPanelName() mService is null!");
            return null;
        }
        byte[] name = new byte[32];
        try {
            int ret = mService.getEffect(14, 3, name, name.length);
            if (ret != 0) {
                Slog.e(TAG, "getLcdPanelName() getEffect failed! ret=" + ret);
                return null;
            }
            try {
                String lcdVersion = new String(name, "UTF-8").trim();
                int index = lcdVersion.indexOf("VER:");
                Slog.i(TAG, "getVersionFromLCD() index=" + index + ",lcdVersion=" + lcdVersion);
                if (index != -1) {
                    panelVersion = lcdVersion.substring("VER:".length() + index);
                }
            } catch (UnsupportedEncodingException e) {
                Slog.e(TAG, "Unsupported encoding type!");
            }
            Slog.i(TAG, "getVersionFromLCD() panelVersion=" + panelVersion);
            return panelVersion;
        } catch (RemoteException e2) {
            Slog.e(TAG, "getLcdPanelName() RemoteException " + e2);
            return null;
        }
    }

    private static String getLcdPanelName() {
        IBinder binder = ServiceManager.getService(DisplayEngineManager.SERVICE_NAME);
        String panelName = null;
        if (binder == null) {
            Slog.i(TAG, "getLcdPanelName() binder is null!");
            return null;
        }
        IDisplayEngineServiceEx mService = IDisplayEngineServiceEx.Stub.asInterface(binder);
        if (mService == null) {
            Slog.e(TAG, "getLcdPanelName() mService is null!");
            return null;
        }
        byte[] name = new byte[256];
        try {
            int ret = mService.getEffect(14, 0, name, name.length);
            if (ret != 0) {
                Slog.e(TAG, "getLcdPanelName() getEffect failed! ret=" + ret);
                return null;
            }
            try {
                panelName = new String(name, "UTF-8").trim().replace(' ', '_').replace('\'', '_');
            } catch (UnsupportedEncodingException e) {
                Slog.e(TAG, "Unsupported encoding type!");
            }
            return panelName;
        } catch (RemoteException e2) {
            Slog.e(TAG, "getLcdPanelName() RemoteException " + e2);
            return null;
        }
    }
}
