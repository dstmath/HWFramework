package com.huawei.displayengine;

import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.util.Slog;
import com.huawei.displayengine.DisplayEngineDataCleanerXMLLoader;
import com.huawei.displayengine.DisplayEngineDbManager;
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
    private static final Object mLock = new Object();
    private static int mOutdoorLevelFloor;
    private static DisplayEngineDataCleanerXMLLoader.Data mParameters = DisplayEngineDataCleanerXMLLoader.getData(getXmlFileName());
    private Context mContext;

    static {
        setParameters();
    }

    private DisplayEngineDataCleaner(Context context) {
        this.mContext = context;
        DeLog.v(TAG, "Default ambient curve: " + Arrays.toString(mAmbientLightLUT.toArray()));
        DeLog.v(TAG, "Default brightness curve: " + Arrays.toString(mBrightnessLevelLUT.toArray()));
    }

    public static DisplayEngineDataCleaner getInstance(Context context) {
        if (mInstance == null) {
            synchronized (mLock) {
                if (mInstance == null) {
                    mInstance = new DisplayEngineDataCleaner(context);
                    DeLog.d(TAG, "DisplayEngineDataCleaner initialized.");
                }
            }
        }
        return mInstance;
    }

    public ArrayList<Bundle> cleanData(ArrayList<Bundle> records, int userId) {
        DeLog.d(TAG, "enter cleanData func.");
        if (userId < 0 || records == null || records.isEmpty()) {
            DeLog.i(TAG, "clean data error! userId=" + userId);
            return null;
        }
        DeLog.v(TAG, "start to  cleanData");
        int rangeFlag = calculateRangeFlag(records, userId);
        DeLog.d(TAG, "rangeFlag=" + rangeFlag);
        if (rangeFlag == 0) {
            return null;
        }
        DeLog.v(TAG, "raw records size:" + records.size());
        int curInd = 0;
        while (curInd < records.size()) {
            Bundle data = records.get(curInd);
            if (data != null) {
                if (data.getInt(DisplayEngineDbManager.DragInformationKey.GAME_STATE) == 1) {
                    records.remove(curInd);
                    curInd--;
                    DeLog.i(TAG, "records index " + curInd + " removed because of game state == 1");
                } else {
                    float orgBrightnessLevel = getOriginalBrightnessLevel(data.getInt("AmbientLight"));
                    if (Float.compare(orgBrightnessLevel, 0.0f) > 0) {
                        Bundle data2 = cleanDataWithHumanFactorPolicy(data, rangeFlag, orgBrightnessLevel);
                        if (data2 != null) {
                            data2 = cleanDataWithDarkEnvironmentPolicy(data2, orgBrightnessLevel);
                        }
                        if (data2 != null) {
                            cleanDataWithHighBrightnessEnvironmentPolicy(data2);
                        }
                    } else {
                        DeLog.v(TAG, "orgBrightnessLevel=" + orgBrightnessLevel);
                    }
                }
            }
            curInd++;
        }
        DeLog.v(TAG, "cleaned records size:" + records.size());
        return records;
    }

    private int calculateRangeFlag(List<Bundle> records, int userId) {
        int rangeFlag;
        int rangeFlag2;
        float weightedCounter;
        List<Bundle> list = records;
        int i = 0;
        if (userId < 0 || list == null || records.isEmpty()) {
            return 0;
        }
        int previousRangeFlag = getRangeFlagByUserId(userId);
        DeLog.v(TAG, "previoudRangeFlag=" + previousRangeFlag);
        int safeZoneCounter = 0;
        int comfortZoneCounter = 0;
        int outlierZoneCounter = 0;
        float weightedCounter2 = 0.0f;
        int rangeFlag3 = 0;
        DisplayEngineDbManager dbManager = DisplayEngineDbManager.getInstance(this.mContext);
        if (dbManager == null) {
            return 0;
        }
        int ind = 0;
        while (ind < records.size()) {
            Bundle data = list.get(ind);
            if (data == null) {
                DeLog.e(TAG, "Bundle data is null!");
                return i;
            }
            boolean isCovered = data.getBoolean(DisplayEngineDbManager.DragInformationKey.PROXIMITY_POSITIVE);
            if (isCovered) {
                DeLog.d(TAG, "isCovered=" + isCovered);
                weightedCounter = weightedCounter2;
                rangeFlag2 = rangeFlag3;
            } else {
                float startPoint = data.getFloat(DisplayEngineDbManager.DragInformationKey.START_POINT);
                float stopPoint = data.getFloat(DisplayEngineDbManager.DragInformationKey.STOP_POINT);
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
                DeLog.v(TAG, sb.toString());
                float orgBrightnessLevel = getOriginalBrightnessLevel(ambientLight);
                DeLog.v(TAG, "orgBrightnessLevel=" + orgBrightnessLevel);
                if (previousRangeFlag == 0) {
                    if (isDataInComfortZone(stopPoint, orgBrightnessLevel)) {
                        comfortZoneCounter++;
                    } else if (isDataInSafeZone(stopPoint, orgBrightnessLevel)) {
                        safeZoneCounter++;
                    } else {
                        outlierZoneCounter++;
                    }
                } else if (previousRangeFlag != 1) {
                    DeLog.v(TAG, "previousRangeFlag=" + previousRangeFlag);
                } else if (!isDataInComfortZone(stopPoint, orgBrightnessLevel)) {
                    if (isDataInSafeZone(stopPoint, orgBrightnessLevel)) {
                        safeZoneCounter++;
                    } else {
                        outlierZoneCounter++;
                    }
                }
            }
            ind++;
            list = records;
            weightedCounter2 = weightedCounter;
            rangeFlag3 = rangeFlag2;
            i = 0;
        }
        DeLog.d(TAG, "comfortZoneCounter=" + comfortZoneCounter + " safeZoneCounter=" + safeZoneCounter + " outlierZoneCounter=" + outlierZoneCounter);
        float weightedCounter3 = getWeightedCounter(comfortZoneCounter, safeZoneCounter, outlierZoneCounter);
        StringBuilder sb2 = new StringBuilder();
        sb2.append("weightedCounter=");
        sb2.append(weightedCounter3);
        sb2.append(" COUNTER_WEIGHT_THRES=");
        sb2.append(COUNTER_WEIGHT_THRES);
        DeLog.d(TAG, sb2.toString());
        if (Float.compare(weightedCounter3, COUNTER_WEIGHT_THRES) > 0) {
            rangeFlag = 2;
        } else {
            rangeFlag = 1;
        }
        DeLog.d(TAG, "rangeFlag=" + rangeFlag);
        Bundle data2 = new Bundle();
        data2.putInt("UserID", userId);
        data2.putInt(DisplayEngineDbManager.DataCleanerKey.RANGE_FLAG, rangeFlag);
        data2.putLong("TimeStamp", System.currentTimeMillis());
        dbManager.addOrUpdateRecord("DataCleaner", data2);
        return rangeFlag;
    }

    private int getRangeFlagByUserId(int userId) {
        List<Bundle> records;
        if (userId < 0 || (records = DisplayEngineDbManager.getInstance(this.mContext).getAllRecords("DataCleaner", new Bundle())) == null) {
            return 0;
        }
        for (int curInd = 0; curInd < records.size(); curInd++) {
            Bundle data = records.get(curInd);
            if (data == null) {
                DeLog.e(TAG, "Bundle data is null!");
                return 0;
            } else if (userId == data.getInt("UserID")) {
                return data.getInt(DisplayEngineDbManager.DataCleanerKey.RANGE_FLAG);
            }
        }
        return 0;
    }

    private float getOriginalBrightnessLevel(int currentLight) {
        int ambientLutSize = mAmbientLightLUT.size();
        if (mAmbientLightLUT.isEmpty() || mBrightnessLevelLUT.isEmpty() || ambientLutSize != mBrightnessLevelLUT.size()) {
            DeLog.i(TAG, "getOriginalBrightnessLevel invalid input!");
            return -1.0f;
        }
        int ind = 0;
        while (ind < ambientLutSize && mAmbientLightLUT.get(ind).intValue() <= currentLight) {
            ind++;
        }
        if (ind >= 1 && ind <= ambientLutSize - 1) {
            int a = mAmbientLightLUT.get(ind - 1).intValue();
            float alpha = ((float) (currentLight - a)) / ((float) (mAmbientLightLUT.get(ind).intValue() - a));
            return (mBrightnessLevelLUT.get(ind).floatValue() * alpha) + ((1.0f - alpha) * mBrightnessLevelLUT.get(ind - 1).floatValue());
        } else if (ind == 0) {
            return mBrightnessLevelLUT.get(0).floatValue();
        } else {
            if (ind > ambientLutSize - 1) {
                return mBrightnessLevelLUT.get(ambientLutSize - 1).floatValue();
            }
            DeLog.i(TAG, "Invalid ind!");
            return -1.0f;
        }
    }

    private boolean isDataInComfortZone(float stopPoint, float originalBrightnessLevel) {
        if (Float.compare(stopPoint, 0.0f) < 0 || Float.compare(originalBrightnessLevel, 0.0f) < 0) {
            return false;
        }
        float comfortRoof = originalBrightnessLevel * 1.5f;
        if (stopPoint < originalBrightnessLevel / 1.5f || stopPoint > comfortRoof) {
            return false;
        }
        return true;
    }

    private boolean isDataInSafeZone(float stopPoint, float originalBrightnessLevel) {
        if (Float.compare(stopPoint, 0.0f) < 0 || Float.compare(originalBrightnessLevel, 0.0f) < 0) {
            return false;
        }
        float safeRoof = originalBrightnessLevel * 3.0f;
        if (stopPoint < originalBrightnessLevel / 3.0f || stopPoint > safeRoof) {
            return false;
        }
        return true;
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
        DeLog.d(TAG, "startPoint=" + rawData.getFloat(DisplayEngineDbManager.DragInformationKey.START_POINT) + " stopPoint=" + rawData.getFloat(DisplayEngineDbManager.DragInformationKey.STOP_POINT));
        if (rangeFlag == 2) {
            rawData.putFloat(DisplayEngineDbManager.DragInformationKey.STOP_POINT, clamp(orgBrightnessLevel * 3.0f, orgBrightnessLevel / 3.0f, rawData.getFloat(DisplayEngineDbManager.DragInformationKey.STOP_POINT)));
        } else if (rangeFlag == 1) {
            rawData.putFloat(DisplayEngineDbManager.DragInformationKey.STOP_POINT, clamp(orgBrightnessLevel * 1.5f, orgBrightnessLevel / 1.5f, rawData.getFloat(DisplayEngineDbManager.DragInformationKey.STOP_POINT)));
        }
        DeLog.d(TAG, "cleaned stopPoint=" + rawData.getFloat(DisplayEngineDbManager.DragInformationKey.STOP_POINT));
        return rawData;
    }

    private Bundle cleanDataWithDarkEnvironmentPolicy(Bundle rawData, float orgBrightnessLevel) {
        int index;
        if (rawData == null) {
            DeLog.i(TAG, "rawData is null.");
            return null;
        }
        int ambientLight = rawData.getInt("AmbientLight");
        float stopPoint = rawData.getFloat(DisplayEngineDbManager.DragInformationKey.STOP_POINT);
        DeLog.v(TAG, "cleanDataWithDarkEnvironmentPolicy stopPoint=" + stopPoint + " ambientLight=" + ambientLight);
        if (ambientLight >= 0 && ambientLight < THRES_AL_DARK && (index = mDarkLevelLUT.indexOf(Integer.valueOf((int) Math.floor((double) orgBrightnessLevel)))) >= 0) {
            float stopPoint2 = Float.compare(stopPoint, mDarkLevelRoofLUT.get(index).floatValue()) > 0 ? mDarkLevelRoofLUT.get(index).floatValue() : stopPoint;
            rawData.putFloat(DisplayEngineDbManager.DragInformationKey.STOP_POINT, stopPoint2);
            DeLog.v(TAG, "cleanDataWithDarkEnvironmentPolicy new stopPoint=" + stopPoint2 + " roof=" + mDarkLevelRoofLUT.get(index));
        }
        return rawData;
    }

    private Bundle cleanDataWithHighBrightnessEnvironmentPolicy(Bundle rawData) {
        if (rawData == null) {
            DeLog.i(TAG, "rawData is null.");
            return null;
        }
        int ambientLight = rawData.getInt("AmbientLight");
        float stopPoint = rawData.getFloat(DisplayEngineDbManager.DragInformationKey.STOP_POINT);
        DeLog.v(TAG, "cleanDataWithHighBrightnessEnvironmentPolicy stopPoint=" + stopPoint);
        if (ambientLight >= 0 && ambientLight >= THRES_HBM) {
            float stopPoint2 = Float.compare(stopPoint, (float) mOutdoorLevelFloor) > 0 ? stopPoint : (float) mOutdoorLevelFloor;
            rawData.putFloat(DisplayEngineDbManager.DragInformationKey.STOP_POINT, stopPoint2);
            DeLog.v(TAG, "cleanDataWithHighBrightnessEnvironmentPolicy new stopPoint=" + stopPoint2);
        }
        return rawData;
    }

    private float clamp(float roof, float floor, float value) {
        float ret = value;
        if (Float.compare(ret, roof) > 0) {
            ret = roof;
        }
        return Float.compare(ret, floor) < 0 ? floor : ret;
    }

    private static String getXmlFileName() {
        String panelName = getLcdPanelName();
        String panelVersion = getVersionFromLCD();
        String xmlPathWithPanelName = String.format("%s%s_%s%s", XML_DIR, XML_FILENAME_WITHOUT_EXT, panelName, XML_EXT);
        String xmlPathWithoutPanelName = String.format("%s%s", XML_DIR, XML_FILENAME);
        String xmlPathWithPanelNameAndPanelVersion = String.format("%s%s_%s_%s%s", XML_DIR, XML_FILENAME_WITHOUT_EXT, panelName, panelVersion, XML_EXT);
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
            String panelVersion = null;
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
            String panelName = null;
            try {
                panelName = new String(name, "UTF-8").trim().replace(' ', '_');
                return panelName.replace('\'', '_');
            } catch (UnsupportedEncodingException e) {
                Slog.e(TAG, "Unsupported encoding type!");
                return panelName;
            }
        } catch (RemoteException e2) {
            Slog.e(TAG, "getLcdPanelName() RemoteException " + e2);
            return null;
        }
    }
}
