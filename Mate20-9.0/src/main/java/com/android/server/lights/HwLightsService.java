package com.android.server.lights;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.HwBrightnessProcessor;
import android.os.Message;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.util.ArrayMap;
import android.util.Log;
import android.util.Slog;
import com.android.server.hidata.hinetwork.HwHiNetworkParmStatistics;
import com.android.server.rms.iaware.appmng.AwareDefaultConfigList;
import com.huawei.displayengine.DisplayEngineManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class HwLightsService extends LightsService {
    /* access modifiers changed from: private */
    public static boolean DEBUG = (Log.HWINFO || (Log.HWModuleLog && Log.isLoggable(TAG, 4)));
    private static final int MAX_BRIGHTNESS = 10000;
    private static String MAX_BRIGHTNESS_NODE = "/sys/class/leds/lcd_backlight0/max_brightness";
    private static final int MIN_BRIGHTNESS = 156;
    private static final int MSG_UPDATE_BRIGHTNESS = 0;
    private static final int NORMALIZED_DEFAULT_MAX_BRIGHTNESS = 255;
    private static final int NORMALIZED_DEFAULT_MIN_BRIGHTNESS = 4;
    private static final int NORMALIZED_MAX_BRIGHTNESS = 10000;
    private static String PANEL_INFO_NODE = "/sys/class/graphics/fb0/panel_info";
    private static final int REFRESH_FRAMES_CMD = 1;
    private static final int SRE_REFRESH_FRAMES_CMD = 1;
    static final String TAG = "HwLightsService";
    private boolean mAutoBrightnessEnabled = false;
    private BackLightLevelLogPrinter mBackLightLevelPrinter = null;
    private int mBrightnessLevel = -1;
    /* access modifiers changed from: private */
    public ContentResolver mContentResolver = getContext().getContentResolver();
    private int mCurrentBrightnessLevelForHighPrecision = 0;
    /* access modifiers changed from: private */
    public int mCurrentUserId = 0;
    private int mDeviceActualBrightnessNit = 0;
    private int mDeviceBrightnessLevel = 0;
    private int mDeviceStandardBrightnessNit = 0;
    private DisplayEngineManager mDisplayEngineManager;
    private final ArrayMap<String, HwBrightnessProcessor> mHwBrightnessProcessors = new ArrayMap<>();
    private HwNormalizedBrightnessMapping mHwNormalizedBrightnessMapping;
    private boolean mHwNormalizedBrightnessMappingEnableLoaded = false;
    /* access modifiers changed from: private */
    public volatile boolean mLightsBypass = false;
    private boolean mNeedBrightnessMappingEnable = false;
    private int mNormalizedMaxBrightness = -1;
    private int mNormalizedMinBrightness = -1;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                if ("android.intent.action.ACTION_SHUTDOWN".equals(action)) {
                    Log.i(HwLightsService.TAG, "handle ACTION_SHUTDOWN broadcast");
                    LightsService.mHasShutDown = true;
                } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                    if (HwLightsService.DEBUG) {
                        Slog.d(HwLightsService.TAG, "handle ACTION_SCREEN_OFF broadcast");
                    }
                    if (HwLightsService.this.mLightsBypass) {
                        HwLightsService.this.setManufactureBrightness(-1, -1, -1, -1);
                    }
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public Handler mRefreshFramesHandler = new Handler() {
        public void handleMessage(Message msg) {
            HwLightsService.this.mRefreshFramesHandler.removeMessages(1);
            LightsService.refreshFrames_native();
            HwLightsService.access$310(HwLightsService.this);
            if (HwLightsService.this.mSBLFrameCount > 0) {
                HwLightsService.this.mRefreshFramesHandler.sendEmptyMessageDelayed(1, 16);
            } else if (HwLightsService.this.mSBLSetAfterRefresh) {
                HwLightsService.this.sendSmartBackLightWithRefreshFramesImpl(HwLightsService.this.mSBLId, HwLightsService.this.mSBLEnable, HwLightsService.this.mSBLLevel, HwLightsService.this.mSBLValue, 0, false, 0, 0);
                boolean unused = HwLightsService.this.mSBLSetAfterRefresh = false;
            }
        }
    };
    /* access modifiers changed from: private */
    public int mSBLEnable;
    /* access modifiers changed from: private */
    public int mSBLFrameCount;
    /* access modifiers changed from: private */
    public int mSBLId;
    /* access modifiers changed from: private */
    public int mSBLLevel;
    /* access modifiers changed from: private */
    public boolean mSBLSetAfterRefresh;
    /* access modifiers changed from: private */
    public int mSBLValue;
    /* access modifiers changed from: private */
    public int mSREFrameCount;
    /* access modifiers changed from: private */
    public int mSREId;
    /* access modifiers changed from: private */
    public Handler mSRERefreshFramesHandler = new Handler() {
        public void handleMessage(Message msg) {
            HwLightsService.this.mSRERefreshFramesHandler.removeMessages(1);
            LightsService.refreshFrames_native();
            HwLightsService.access$1010(HwLightsService.this);
            if (HwLightsService.this.mSREFrameCount > 0) {
                HwLightsService.this.mSRERefreshFramesHandler.sendEmptyMessageDelayed(1, 16);
            } else if (HwLightsService.this.mSRESetAfterRefresh) {
                LightsService.setLight_native(HwLightsService.this.mSREId, HwLightsService.this.mSREValue, 0, 0, 0, 0);
                boolean unused = HwLightsService.this.mSRESetAfterRefresh = false;
            }
        }
    };
    /* access modifiers changed from: private */
    public boolean mSRESetAfterRefresh;
    /* access modifiers changed from: private */
    public int mSREValue;
    private int mSupportAmoled = 0;
    private boolean mSupportAmoled_isloaded = false;
    private int mSupportGammaFix = 0;
    private boolean mSupportGammaFix_isloaded = false;
    private int mSupportRGLed = 0;
    private boolean mSupportRGLed_isloaded = false;
    private int mSupportXCC = 0;
    private boolean mSupportXCC_isloaded = false;
    private Handler mUpdateBrightnessHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                int brightnessOut = (int) Math.ceil((double) ((((float) (65535 & HwLightsService.this.getLcdBrightnessMode())) * 255.0f) / 10000.0f));
                Settings.System.putIntForUser(HwLightsService.this.mContentResolver, "screen_auto_brightness", brightnessOut, HwLightsService.this.mCurrentUserId);
                if (brightnessOut != 0 && 1 == Settings.System.getIntForUser(HwLightsService.this.mContentResolver, "screen_brightness_mode", 1, HwLightsService.this.mCurrentUserId)) {
                    Settings.System.putIntForUser(HwLightsService.this.mContentResolver, "screen_brightness", brightnessOut, HwLightsService.this.mCurrentUserId);
                }
            }
        }
    };

    private static class BackLightLevelLogPrinter {
        private String mLogTag = null;
        private int mPrintedLevel = 0;
        private float mThresholdPercent = 0.1f;

        public BackLightLevelLogPrinter(String logTag, float thresholdPercent) {
            this.mThresholdPercent = thresholdPercent;
            this.mLogTag = logTag;
        }

        public void printLevel(int level) {
            if (HwLightsService.DEBUG) {
                if ((level != 0 || this.mPrintedLevel == 0) && (level == 0 || this.mPrintedLevel != 0)) {
                    int threshold = (int) (((float) this.mPrintedLevel) * this.mThresholdPercent);
                    int i = 2;
                    if (threshold >= 2) {
                        i = threshold;
                    }
                    int threshold2 = i;
                    int delta = level - this.mPrintedLevel;
                    if ((delta < 0 ? -delta : delta) > threshold2) {
                        Slog.i(HwLightsService.TAG, this.mLogTag + " = " + level);
                        this.mPrintedLevel = level;
                    }
                    return;
                }
                Slog.i(HwLightsService.TAG, this.mLogTag + " = " + level);
                this.mPrintedLevel = level;
            }
        }
    }

    private final class CurrentBrightnessProcessor extends HwBrightnessProcessor {
        public CurrentBrightnessProcessor() {
        }

        public boolean getData(Bundle data, int[] retValue) {
            data.putInt("Brightness", HwLightsService.this.getCurrentBrightess());
            return true;
        }
    }

    private enum ManufactureBrightnessMode {
        DEFAULT(-1),
        OFF(0),
        ON(1);
        
        private final int mValue;

        private ManufactureBrightnessMode(int value) {
            this.mValue = value;
        }

        public int getValue() {
            return this.mValue;
        }
    }

    private final class ManufactureBrightnessProcessor extends HwBrightnessProcessor {
        public ManufactureBrightnessProcessor() {
        }

        public boolean setData(Bundle data, int[] retValue) {
            retValue[0] = HwLightsService.this.setManufactureBrightness(data.getInt("ManufactureProcess", -1), data.getInt("Scene", -1), data.getInt(HwHiNetworkParmStatistics.LEVEL, -1), data.getInt("AnimationTime", -1));
            return true;
        }
    }

    static /* synthetic */ int access$1010(HwLightsService x0) {
        int i = x0.mSREFrameCount;
        x0.mSREFrameCount = i - 1;
        return i;
    }

    static /* synthetic */ int access$310(HwLightsService x0) {
        int i = x0.mSBLFrameCount;
        x0.mSBLFrameCount = i - 1;
        return i;
    }

    public HwLightsService(Context context) {
        super(context);
        boolean z = false;
        getNormalizedBrightnessRangeFromKernel();
        this.mIsHighPrecision = this.mNormalizedMaxBrightness > 255 ? true : z;
        setBackLightMaxLevel_native(this.mNormalizedMaxBrightness);
        this.mDisplayEngineManager = new DisplayEngineManager();
        loadHwBrightnessProcessors();
        this.mHwNormalizedBrightnessMapping = new HwNormalizedBrightnessMapping(MIN_BRIGHTNESS, 10000, this.mNormalizedMinBrightness, this.mNormalizedMaxBrightness);
    }

    public void onBootPhase(int phase) {
        HwLightsService.super.onBootPhase(phase);
        if (phase == 1000) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.ACTION_SHUTDOWN");
            filter.addAction("android.intent.action.SCREEN_OFF");
            getContext().registerReceiver(this.mReceiver, filter);
        }
    }

    private void refreshSmartBackLightFrames(int id, int count, boolean setAfterRefresh, int enable, int level, int value) {
        this.mSBLId = id;
        this.mSBLFrameCount = count;
        this.mSBLSetAfterRefresh = setAfterRefresh;
        this.mSBLEnable = enable;
        this.mSBLLevel = level;
        this.mSBLValue = value;
        if (count > 0) {
            this.mRefreshFramesHandler.sendEmptyMessage(1);
        }
    }

    public void sendSmartBackLightWithRefreshFramesImpl(int id, int enable, int level, int value, int frames, boolean setAfterRefresh, int enable2, int value2) {
        int i = id;
        if (i != 257) {
            Slog.e(TAG, "id = " + i + ", error! this mothod only used for SBL!");
            return;
        }
        synchronized (this) {
            int i2 = value;
            int i3 = level;
            setLight_native(i, (65535 & (i2 > 65535 ? 65535 : i2)) | ((enable & 1) << 24) | ((i3 & 255) << 16), 0, 0, 0, 0);
            refreshSmartBackLightFrames(i, frames, setAfterRefresh, enable2, i3, value2);
        }
    }

    private void refreshSREFrames(int id, int count, boolean setAfterRefresh, int value) {
        this.mSREId = id;
        this.mSREFrameCount = count;
        this.mSRESetAfterRefresh = setAfterRefresh;
        this.mSREValue = value;
        if (count > 0) {
            this.mSRERefreshFramesHandler.sendEmptyMessage(1);
        }
    }

    public void sendSREWithRefreshFramesImpl(int id, int enable, int ambientLightThreshold, int ambientLight, int frames, boolean setAfterRefresh, int enable2, int ambientLight2) {
        synchronized (this) {
            int ambientLightThresholdMin = 4095;
            int i = ambientLightThreshold;
            if (i <= 4095) {
                ambientLightThresholdMin = i;
            }
            int i2 = ambientLight;
            int ambientLightMin = i2 > 65535 ? 65535 : i2;
            int sreValue2 = (65535 & ambientLightMin) | ((enable2 & 1) << 28) | ((ambientLightThresholdMin & 4095) << 16);
            try {
                setLight_native(id, ((enable & 1) << 28) | ((ambientLightThresholdMin & 4095) << 16) | (ambientLightMin & 65535), 0, 0, 0, 0);
                refreshSREFrames(id, frames, setAfterRefresh, sreValue2);
            } catch (Throwable th) {
                th = th;
                throw th;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void sendUpdateaAutoBrightnessDbMsg() {
        if (this.mWriteAutoBrightnessDbEnable && this.mAutoBrightnessEnabled && this.mUpdateBrightnessHandler != null) {
            if (this.mUpdateBrightnessHandler.hasMessages(0)) {
                this.mUpdateBrightnessHandler.removeMessages(0);
            }
            this.mUpdateBrightnessHandler.sendEmptyMessage(0);
        }
    }

    /* access modifiers changed from: protected */
    public void updateBrightnessMode(boolean mode) {
        this.mAutoBrightnessEnabled = mode;
    }

    /* access modifiers changed from: protected */
    public void updateCurrentUserId(int userId) {
        if (DEBUG) {
            Slog.d(TAG, "user change from  " + this.mCurrentUserId + " into " + userId);
        }
        this.mCurrentUserId = userId;
    }

    private boolean getBrightnessRangeFromPanelInfo() {
        File file = new File(PANEL_INFO_NODE);
        if (!file.exists()) {
            if (DEBUG) {
                Slog.w(TAG, "getBrightnessRangeFromPanelInfo PANEL_INFO_NODE:" + PANEL_INFO_NODE + " isn't exist");
            }
            return false;
        }
        BufferedReader reader = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
            String readLine = reader.readLine();
            String tempString = readLine;
            if (readLine != null) {
                if (DEBUG) {
                    Slog.i(TAG, "getBrightnessRangeFromPanelInfo String = " + tempString);
                }
                if (tempString.length() == 0) {
                    Slog.e(TAG, "getBrightnessRangeFromPanelInfo error! String is null");
                    reader.close();
                    close(reader, fis);
                    return false;
                }
                String[] stringSplited = tempString.split(",");
                if (stringSplited.length < 2) {
                    Slog.e(TAG, "split failed! String = " + tempString);
                    reader.close();
                    close(reader, fis);
                    return false;
                } else if (parsePanelInfo(stringSplited)) {
                    reader.close();
                    close(reader, fis);
                    return true;
                }
            }
        } catch (FileNotFoundException e) {
            Slog.e(TAG, "getBrightnessRangeFromPanelInfo error! FileNotFoundException");
        } catch (IOException e2) {
            Slog.e(TAG, "getBrightnessRangeFromPanelInfo error! IOException " + e2);
        } catch (Exception e3) {
            Slog.e(TAG, "getBrightnessRangeFromPanelInfo error! Exception " + e3);
        } catch (Throwable th) {
            close(reader, fis);
            throw th;
        }
        close(reader, fis);
        return false;
    }

    private boolean parsePanelInfo(String[] stringSplited) {
        if (stringSplited == null) {
            return false;
        }
        String key = null;
        int standardMaxNit = 0;
        int actualMaxNit = 0;
        int deviceLevel = 0;
        int min = -1;
        int max = -1;
        int i = 0;
        while (i < stringSplited.length) {
            try {
                key = "blmax:";
                int index = stringSplited[i].indexOf(key);
                if (index != -1) {
                    max = Integer.parseInt(stringSplited[i].substring(key.length() + index));
                } else {
                    key = "blmin:";
                    int index2 = stringSplited[i].indexOf(key);
                    if (index2 != -1) {
                        min = Integer.parseInt(stringSplited[i].substring(key.length() + index2));
                    } else {
                        key = "bldevicelevel:";
                        int index3 = stringSplited[i].indexOf(key);
                        if (index3 != -1) {
                            deviceLevel = Integer.parseInt(stringSplited[i].substring(key.length() + index3));
                        } else {
                            key = "blmax_nit_actual:";
                            int index4 = stringSplited[i].indexOf(key);
                            if (index4 != -1) {
                                actualMaxNit = Integer.parseInt(stringSplited[i].substring(key.length() + index4));
                            } else {
                                key = "blmax_nit_standard:";
                                int index5 = stringSplited[i].indexOf(key);
                                if (index5 != -1) {
                                    standardMaxNit = Integer.parseInt(stringSplited[i].substring(key.length() + index5));
                                }
                            }
                        }
                    }
                }
                i++;
            } catch (NumberFormatException e) {
                Slog.e(TAG, "parsePanelInfo() error! " + key + e);
                return false;
            }
        }
        if (max == -1 || min == -1) {
            return false;
        }
        if (DEBUG) {
            Slog.i(TAG, "getBrightnessRangeFromPanelInfo success! min = " + min + ", max = " + max + ", deviceLevel = " + deviceLevel + ",actualMaxNit=" + actualMaxNit + ",standardMaxNit=" + standardMaxNit);
        }
        this.mNormalizedMaxBrightness = max;
        this.mNormalizedMinBrightness = min;
        this.mDeviceBrightnessLevel = deviceLevel;
        this.mDeviceActualBrightnessNit = actualMaxNit;
        this.mDeviceStandardBrightnessNit = standardMaxNit;
        return true;
    }

    private boolean getBrightnessRangeFromMaxBrightness() {
        File file = new File(MAX_BRIGHTNESS_NODE);
        if (!file.exists()) {
            if (DEBUG) {
                Slog.w(TAG, "getBrightnessRangeFromMaxBrightness MAX_BRIGHTNESS_NODE:" + MAX_BRIGHTNESS_NODE + " isn't exist");
            }
            return false;
        }
        BufferedReader reader = null;
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
            String readLine = reader.readLine();
            String tempString = readLine;
            if (readLine != null) {
                this.mNormalizedMaxBrightness = Integer.parseInt(tempString);
                this.mNormalizedMinBrightness = (this.mNormalizedMaxBrightness * 4) / 255;
                if (DEBUG) {
                    Slog.i(TAG, "getBrightnessRangeFromMaxBrightness success! min = " + this.mNormalizedMinBrightness + ", max = " + this.mNormalizedMaxBrightness);
                }
                reader.close();
                close(reader, fis);
                return true;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        } catch (Exception e3) {
            e3.printStackTrace();
        } catch (Throwable th) {
            close(null, null);
            throw th;
        }
        close(reader, fis);
        return false;
    }

    private void checkNormalizedBrightnessRange() {
        if (this.mNormalizedMinBrightness < 0 || this.mNormalizedMinBrightness >= this.mNormalizedMaxBrightness || this.mNormalizedMaxBrightness > 10000) {
            this.mNormalizedMinBrightness = 4;
            this.mNormalizedMaxBrightness = 255;
            Slog.e(TAG, "checkNormalizedBrightnessRange failed! load default brightness range: min = " + this.mNormalizedMinBrightness + ", max = " + this.mNormalizedMaxBrightness);
            return;
        }
        Slog.i(TAG, "checkNormalizedBrightnessRange success! range: min = " + this.mNormalizedMinBrightness + ", max = " + this.mNormalizedMaxBrightness);
    }

    /* access modifiers changed from: protected */
    public void getNormalizedBrightnessRangeFromKernel() {
        try {
            if (getBrightnessRangeFromPanelInfo()) {
                checkNormalizedBrightnessRange();
                return;
            }
            if (DEBUG) {
                Slog.w(TAG, "getBrightnessRangeFromPanelInfo failed");
            }
            if (getBrightnessRangeFromMaxBrightness()) {
                checkNormalizedBrightnessRange();
                return;
            }
            if (DEBUG) {
                Slog.w(TAG, "getBrightnessRangeFromMaxBrightness failed");
            }
            checkNormalizedBrightnessRange();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int mapIntoRealBacklightLevelIfNeedXNit(int level) {
        int brightnessvalue;
        this.mBrightnessLevel = level;
        int brightnessvalue2 = level;
        int brightnessHighPrecision = level;
        if (!this.mHwNormalizedBrightnessMappingEnableLoaded) {
            this.mNeedBrightnessMappingEnable = this.mHwNormalizedBrightnessMapping.needBrightnessMappingEnable();
            this.mHwNormalizedBrightnessMappingEnableLoaded = true;
        }
        if (!this.mSupportXCC_isloaded) {
            this.mSupportXCC = this.mDisplayEngineManager.getSupported(16);
            this.mSupportXCC_isloaded = true;
            Slog.i(TAG, "mSupportXCC  = " + this.mSupportXCC);
        }
        if (this.mSupportXCC != 1 || this.mLightsBypass) {
            brightnessvalue = convertBrightnessHighPrecisionToDevicePrecision(level, false);
        } else {
            PersistableBundle bundle = new PersistableBundle();
            bundle.putInt("MinBrightness", this.mNormalizedMinBrightness);
            bundle.putInt("MaxBrightness", this.mNormalizedMaxBrightness);
            bundle.putInt("brightnesslevel", brightnessvalue2);
            brightnessvalue = this.mDisplayEngineManager.setData(6, bundle);
            if (brightnessvalue <= 0) {
                brightnessvalue = convertBrightnessHighPrecisionToDevicePrecision(level, false);
            }
        }
        if (!this.mSupportAmoled_isloaded) {
            this.mSupportAmoled = this.mDisplayEngineManager.getSupported(25);
            this.mSupportAmoled_isloaded = true;
            Slog.i(TAG, "mSupportAmoled  = " + this.mSupportAmoled);
        }
        if (!this.mSupportGammaFix_isloaded) {
            byte[] status = new byte[1];
            if (this.mDisplayEngineManager.getEffect(7, 0, status, 1) == 0) {
                this.mSupportGammaFix = status[0];
                Slog.i(TAG, "[effect] getEffect(DE_FEATURE_GAMMA):" + this.mSupportGammaFix);
            }
            this.mSupportGammaFix_isloaded = true;
            Slog.i(TAG, "mSupportGammaFix  = " + this.mSupportGammaFix);
        }
        if (this.mSupportRGLed_isloaded == 0) {
            this.mSupportRGLed = this.mDisplayEngineManager.getSupported(19);
            this.mSupportRGLed_isloaded = true;
            Slog.i(TAG, "mSupportRGLed  = " + this.mSupportRGLed);
        }
        if (!this.mLightsBypass && (this.mSupportAmoled == 1 || this.mSupportGammaFix == 1 || this.mSupportRGLed == 1)) {
            if (this.mNeedBrightnessMappingEnable) {
                brightnessHighPrecision = this.mHwNormalizedBrightnessMapping.getMappingBrightnessHighPrecision(level);
            }
            this.mDisplayEngineManager.setScene(26, (brightnessHighPrecision << 16) | brightnessvalue);
        }
        return brightnessvalue;
    }

    /* access modifiers changed from: protected */
    public int mapIntoRealBacklightLevel(int level) {
        this.mCurrentBrightnessLevelForHighPrecision = level;
        if (this.mBackLightLevelPrinter != null) {
            this.mBackLightLevelPrinter.printLevel(level);
        } else {
            this.mBackLightLevelPrinter = new BackLightLevelLogPrinter("back light level before map", 0.1f);
        }
        if (level == 0) {
            return 0;
        }
        if (level < MIN_BRIGHTNESS) {
            return this.mNormalizedMinBrightness;
        }
        if (level > 10000) {
            return this.mNormalizedMaxBrightness;
        }
        return mapIntoRealBacklightLevelIfNeedXNit(level);
    }

    public int getDeviceActualBrightnessLevelImpl() {
        return this.mDeviceBrightnessLevel;
    }

    public int getDeviceActualBrightnessNitImpl() {
        return this.mDeviceActualBrightnessNit;
    }

    public int getDeviceStandardBrightnessNitImpl() {
        return this.mDeviceStandardBrightnessNit;
    }

    /* access modifiers changed from: protected */
    public int getNormalizedMaxBrightness() {
        return this.mNormalizedMaxBrightness;
    }

    private void close(BufferedReader reader, FileInputStream fis) {
        if (reader != null || fis != null) {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    public boolean hwBrightnessSetDataImpl(String name, Bundle data, int[] result) {
        HwBrightnessProcessor processor = this.mHwBrightnessProcessors.get(name);
        if (processor != null) {
            return processor.setData(data, result);
        }
        return false;
    }

    public boolean hwBrightnessGetDataImpl(String name, Bundle data, int[] result) {
        HwBrightnessProcessor processor = this.mHwBrightnessProcessors.get(name);
        if (processor != null) {
            return processor.getData(data, result);
        }
        return false;
    }

    private void loadHwBrightnessProcessors() {
        this.mHwBrightnessProcessors.put("ManufactureBrightness", new ManufactureBrightnessProcessor());
        this.mHwBrightnessProcessors.put("CurrentBrightness", new CurrentBrightnessProcessor());
    }

    public int setManufactureBrightness(int manufactureProcess, int scene, int level, int animationTime) {
        boolean isOverrideLight;
        int devicePrecisionLevel;
        int highPrecisionLevel;
        if (DEBUG) {
            Slog.i(TAG, "ManufactureBrightness: manufactureProcess=" + manufactureProcess + ",scene=" + scene + ",level=" + level + ",animationTime=" + animationTime);
        }
        if (manufactureProcess < 0 || scene < 0 || level < 0 || animationTime < 0) {
            highPrecisionLevel = this.mBrightnessLevel;
            devicePrecisionLevel = mapIntoRealBacklightLevelIfNeedXNit(highPrecisionLevel);
            if (this.mNeedBrightnessMappingEnable) {
                highPrecisionLevel = this.mHwNormalizedBrightnessMapping.getMappingBrightnessHighPrecision(highPrecisionLevel);
            }
            isOverrideLight = false;
        } else if (level == 0) {
            highPrecisionLevel = 0;
            devicePrecisionLevel = 0;
            isOverrideLight = true;
        } else {
            if (level < 4) {
                level = 4;
            } else if (level > 255) {
                level = 255;
            }
            int highPrecisionLevel2 = convertBrightnessLowPrecisionToHighPrecisionImpl(level);
            boolean isFactoryMode = getManufactureBrightnessHBMMode(scene) == ManufactureBrightnessMode.OFF;
            int devicePrecisionLevel2 = convertBrightnessHighPrecisionToDevicePrecision(highPrecisionLevel2, isFactoryMode);
            if (!isFactoryMode && this.mNeedBrightnessMappingEnable) {
                highPrecisionLevel2 = this.mHwNormalizedBrightnessMapping.getMappingBrightnessHighPrecision(highPrecisionLevel2);
            }
            isOverrideLight = true;
            highPrecisionLevel = highPrecisionLevel2;
            devicePrecisionLevel = devicePrecisionLevel2;
        }
        setLightsBrightnessOverride(isOverrideLight, devicePrecisionLevel);
        setManufactureBrightnessToDisplayEngine(manufactureProcess, scene, (highPrecisionLevel << 16) | devicePrecisionLevel);
        return 0;
    }

    public boolean isLightsBypassed() {
        return this.mLightsBypass;
    }

    private void setLightsBrightnessOverride(boolean isBypass, int devicePrecisionBrightness) {
        if (DEBUG) {
            Slog.i(TAG, "ManufactureBrightness: setLightsBrightnessOverride isBypass = " + isBypass + ", devicePrecisionBrightness =" + devicePrecisionBrightness);
        }
        this.mLightsBypass = isBypass;
        synchronized (this) {
            setLight_native(AwareDefaultConfigList.HW_PERCEPTIBLE_APP_ADJ, devicePrecisionBrightness, 0, 0, 0, 0);
        }
    }

    private int convertBrightnessHighPrecisionToDevicePrecision(int highPrecisionBrightness, boolean isManufactureMode) {
        int brightnessValue = -1;
        if (this.mNeedBrightnessMappingEnable && this.mHwNormalizedBrightnessMapping != null) {
            brightnessValue = isManufactureMode ? this.mHwNormalizedBrightnessMapping.getMappingBrightnessForManufacture(highPrecisionBrightness) : this.mHwNormalizedBrightnessMapping.getMappingBrightness(highPrecisionBrightness);
        }
        if (brightnessValue < 0) {
            return this.mNormalizedMinBrightness + (((highPrecisionBrightness - 156) * (this.mNormalizedMaxBrightness - this.mNormalizedMinBrightness)) / 9844);
        }
        return brightnessValue;
    }

    private int convertBrightnessLowPrecisionToHighPrecisionImpl(int lowPrecisionBrightness) {
        return (lowPrecisionBrightness * 10000) / 255;
    }

    private ManufactureBrightnessMode getManufactureBrightnessHBMMode(int scene) {
        switch (scene) {
            case -1:
                return ManufactureBrightnessMode.DEFAULT;
            case 0:
                return ManufactureBrightnessMode.OFF;
            case 1:
                return ManufactureBrightnessMode.ON;
            case 2:
                return ManufactureBrightnessMode.ON;
            default:
                Slog.w(TAG, "ManufactureBrightness: Unsupported scene, using default param!");
                return ManufactureBrightnessMode.DEFAULT;
        }
    }

    private ManufactureBrightnessMode getManufactureBrightnessAmOLEDDimmingMode(int scene) {
        switch (scene) {
            case -1:
                return ManufactureBrightnessMode.DEFAULT;
            case 0:
                return ManufactureBrightnessMode.OFF;
            case 1:
                return ManufactureBrightnessMode.OFF;
            case 2:
                return ManufactureBrightnessMode.ON;
            default:
                Slog.w(TAG, "ManufactureBrightness: Unsupported scene, using default param!");
                return ManufactureBrightnessMode.DEFAULT;
        }
    }

    private void setManufactureBrightnessToDisplayEngine(int manufactureProcess, int scene, int brightness) {
        int[] param = {this.mNormalizedMaxBrightness, brightness, getManufactureBrightnessHBMMode(scene).getValue(), getManufactureBrightnessAmOLEDDimmingMode(scene).getValue()};
        PersistableBundle bundle = new PersistableBundle();
        bundle.putIntArray("Buffer", param);
        bundle.putInt("BufferLength", param.length * 4);
        this.mDisplayEngineManager.setData(11, bundle);
    }

    /* access modifiers changed from: private */
    public int getCurrentBrightess() {
        int currentBrightness = 0;
        if (this.mCurrentBrightnessLevelForHighPrecision == 0) {
            return 0;
        }
        if (this.mCurrentBrightnessLevelForHighPrecision > 0) {
            currentBrightness = (int) (((((float) this.mCurrentBrightnessLevelForHighPrecision) * 255.0f) / 10000.0f) + 0.5f);
        }
        if (currentBrightness < 4) {
            currentBrightness = 4;
        }
        if (currentBrightness > 255) {
            currentBrightness = 255;
        }
        Slog.i(TAG, "mCurrentlForHighPrecision=" + this.mCurrentBrightnessLevelForHighPrecision + ",currentBrightness=" + currentBrightness);
        return currentBrightness;
    }
}
