package com.android.server.lights;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import android.provider.Settings.System;
import android.util.Log;
import android.util.Slog;
import android.view.ViewRootImpl;
import com.android.server.emcom.daemon.CommandsInterface;
import com.huawei.displayengine.DisplayEngineManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class HwLightsService extends LightsService {
    private static boolean DEBUG = false;
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
    private ContentResolver mContentResolver = getContext().getContentResolver();
    private int mCurrentUserId = 0;
    private int mDeviceActualBrightnessNit = 0;
    private int mDeviceBrightnessLevel = 0;
    private int mDeviceStandardBrightnessNit = 0;
    private DisplayEngineManager mDisplayEngineManager;
    private int mNormalizedMaxBrightness = -1;
    private int mNormalizedMinBrightness = -1;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && "android.intent.action.ACTION_SHUTDOWN".equals(action)) {
                Log.i(HwLightsService.TAG, "handle ACTION_SHUTDOWN broadcast");
                HwLightsService.mHasShutDown = true;
            }
        }
    };
    private Handler mRefreshFramesHandler = new Handler() {
        public void handleMessage(Message msg) {
            HwLightsService.this.mRefreshFramesHandler.removeMessages(1);
            HwLightsService.refreshFrames_native();
            HwLightsService hwLightsService = HwLightsService.this;
            hwLightsService.mSBLFrameCount = hwLightsService.mSBLFrameCount - 1;
            if (HwLightsService.this.mSBLFrameCount > 0) {
                HwLightsService.this.mRefreshFramesHandler.sendEmptyMessageDelayed(1, 16);
            } else if (HwLightsService.this.mSBLSetAfterRefresh) {
                HwLightsService.this.sendSmartBackLightWithRefreshFramesImpl(HwLightsService.this.mSBLId, HwLightsService.this.mSBLEnable, HwLightsService.this.mSBLLevel, HwLightsService.this.mSBLValue, 0, false, 0, 0);
                HwLightsService.this.mSBLSetAfterRefresh = false;
            }
        }
    };
    private int mSBLEnable;
    private int mSBLFrameCount;
    private int mSBLId;
    private int mSBLLevel;
    private boolean mSBLSetAfterRefresh;
    private int mSBLValue;
    private int mSREFrameCount;
    private int mSREId;
    private Handler mSRERefreshFramesHandler = new Handler() {
        public void handleMessage(Message msg) {
            HwLightsService.this.mSRERefreshFramesHandler.removeMessages(1);
            HwLightsService.refreshFrames_native();
            HwLightsService hwLightsService = HwLightsService.this;
            hwLightsService.mSREFrameCount = hwLightsService.mSREFrameCount - 1;
            if (HwLightsService.this.mSREFrameCount > 0) {
                HwLightsService.this.mSRERefreshFramesHandler.sendEmptyMessageDelayed(1, 16);
            } else if (HwLightsService.this.mSRESetAfterRefresh) {
                HwLightsService.setLight_native(HwLightsService.this.mSREId, HwLightsService.this.mSREValue, 0, 0, 0, 0);
                HwLightsService.this.mSRESetAfterRefresh = false;
            }
        }
    };
    private boolean mSRESetAfterRefresh;
    private int mSREValue;
    private int mSupportHBM = 0;
    private boolean mSupportHBM_isloaded = false;
    private int mSupportXCC = 0;
    private boolean mSupportXCC_isloaded = false;
    private Handler mUpdateBrightnessHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    int brightnessOut = (int) Math.ceil((double) ((((float) (65535 & HwLightsService.this.getLcdBrightnessMode())) * 255.0f) / 10000.0f));
                    System.putIntForUser(HwLightsService.this.mContentResolver, "screen_auto_brightness", brightnessOut, HwLightsService.this.mCurrentUserId);
                    if (brightnessOut != 0) {
                        System.putIntForUser(HwLightsService.this.mContentResolver, "screen_brightness", brightnessOut, HwLightsService.this.mCurrentUserId);
                        return;
                    }
                    return;
                default:
                    return;
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
            if (!HwLightsService.DEBUG) {
                return;
            }
            if ((level != 0 || this.mPrintedLevel == 0) && (level == 0 || this.mPrintedLevel != 0)) {
                int threshold = (int) (((float) this.mPrintedLevel) * this.mThresholdPercent);
                if (threshold < 2) {
                    threshold = 2;
                }
                int delta = level - this.mPrintedLevel;
                if (delta < 0) {
                    delta = -delta;
                }
                if (delta > threshold) {
                    Slog.i(HwLightsService.TAG, this.mLogTag + " = " + level);
                    this.mPrintedLevel = level;
                }
                return;
            }
            Slog.i(HwLightsService.TAG, this.mLogTag + " = " + level);
            this.mPrintedLevel = level;
        }
    }

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false : true;
        DEBUG = isLoggable;
    }

    public HwLightsService(Context context) {
        boolean z = false;
        super(context);
        getNormalizedBrightnessRangeFromKernel();
        if (this.mNormalizedMaxBrightness > 255) {
            z = true;
        }
        this.mIsHighPrecision = z;
        setBackLightMaxLevel_native(this.mNormalizedMaxBrightness);
        this.mDisplayEngineManager = new DisplayEngineManager();
    }

    public void onBootPhase(int phase) {
        super.onBootPhase(phase);
        if (phase == 1000) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.ACTION_SHUTDOWN");
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
        if (id != CommandsInterface.EMCOM_SD_XENGINE_START_ACC) {
            Slog.e(TAG, "id = " + id + ", error! this mothod only used for SBL!");
            return;
        }
        synchronized (this) {
            if (value > 65535) {
                value = 65535;
            }
            setLight_native(id, (((enable & 1) << 24) | ((level & 255) << 16)) | (65535 & value), 0, 0, 0, 0);
            refreshSmartBackLightFrames(id, frames, setAfterRefresh, enable2, level, value2);
            ViewRootImpl.setEnablePartialUpdate(enable == 0);
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
            int ambientLightThresholdMin = ambientLightThreshold > 4095 ? 4095 : ambientLightThreshold;
            int ambientLightMin = ambientLight > 65535 ? 65535 : ambientLight;
            int sreValue2 = (((enable2 & 1) << 28) | ((ambientLightThresholdMin & 4095) << 16)) | (65535 & ambientLightMin);
            setLight_native(id, (((enable & 1) << 28) | ((ambientLightThresholdMin & 4095) << 16)) | (65535 & ambientLightMin), 0, 0, 0, 0);
            refreshSREFrames(id, frames, setAfterRefresh, sreValue2);
        }
    }

    protected void sendUpdateaAutoBrightnessDbMsg() {
        if (this.mWriteAutoBrightnessDbEnable && this.mAutoBrightnessEnabled && this.mUpdateBrightnessHandler != null) {
            if (this.mUpdateBrightnessHandler.hasMessages(0)) {
                this.mUpdateBrightnessHandler.removeMessages(0);
            }
            this.mUpdateBrightnessHandler.sendEmptyMessage(0);
        }
    }

    protected void updateBrightnessMode(boolean mode) {
        this.mAutoBrightnessEnabled = mode;
    }

    protected void updateCurrentUserId(int userId) {
        if (DEBUG) {
            Slog.d(TAG, "user change from  " + this.mCurrentUserId + " into " + userId);
        }
        this.mCurrentUserId = userId;
    }

    private boolean getBrightnessRangeFromPanelInfo() {
        IOException e;
        Exception e2;
        Throwable th;
        File file = new File(PANEL_INFO_NODE);
        if (file.exists()) {
            BufferedReader reader = null;
            FileInputStream fis = null;
            try {
                BufferedReader reader2;
                FileInputStream fis2 = new FileInputStream(file);
                try {
                    reader2 = new BufferedReader(new InputStreamReader(fis2, "UTF-8"));
                } catch (FileNotFoundException e3) {
                    fis = fis2;
                    Slog.e(TAG, "getBrightnessRangeFromPanelInfo error! FileNotFoundException");
                    close(reader, fis);
                    return false;
                } catch (IOException e4) {
                    e = e4;
                    fis = fis2;
                    Slog.e(TAG, "getBrightnessRangeFromPanelInfo error! IOException " + e);
                    close(reader, fis);
                    return false;
                } catch (Exception e5) {
                    e2 = e5;
                    fis = fis2;
                    try {
                        Slog.e(TAG, "getBrightnessRangeFromPanelInfo error! Exception " + e2);
                        close(reader, fis);
                        return false;
                    } catch (Throwable th2) {
                        th = th2;
                        close(reader, fis);
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    fis = fis2;
                    close(reader, fis);
                    throw th;
                }
                try {
                    String tempString = reader2.readLine();
                    if (tempString != null) {
                        if (DEBUG) {
                            Slog.i(TAG, "getBrightnessRangeFromPanelInfo String = " + tempString);
                        }
                        if (tempString.length() == 0) {
                            Slog.e(TAG, "getBrightnessRangeFromPanelInfo error! String is null");
                            reader2.close();
                            close(reader2, fis2);
                            return false;
                        }
                        String[] stringSplited = tempString.split(",");
                        if (stringSplited.length < 2) {
                            Slog.e(TAG, "split failed! String = " + tempString);
                            reader2.close();
                            close(reader2, fis2);
                            return false;
                        } else if (parsePanelInfo(stringSplited)) {
                            reader2.close();
                            close(reader2, fis2);
                            return true;
                        }
                    }
                    close(reader2, fis2);
                    reader = reader2;
                } catch (FileNotFoundException e6) {
                    fis = fis2;
                    reader = reader2;
                    Slog.e(TAG, "getBrightnessRangeFromPanelInfo error! FileNotFoundException");
                    close(reader, fis);
                    return false;
                } catch (IOException e7) {
                    e = e7;
                    fis = fis2;
                    reader = reader2;
                    Slog.e(TAG, "getBrightnessRangeFromPanelInfo error! IOException " + e);
                    close(reader, fis);
                    return false;
                } catch (Exception e8) {
                    e2 = e8;
                    fis = fis2;
                    reader = reader2;
                    Slog.e(TAG, "getBrightnessRangeFromPanelInfo error! Exception " + e2);
                    close(reader, fis);
                    return false;
                } catch (Throwable th4) {
                    th = th4;
                    fis = fis2;
                    reader = reader2;
                    close(reader, fis);
                    throw th;
                }
            } catch (FileNotFoundException e9) {
                Slog.e(TAG, "getBrightnessRangeFromPanelInfo error! FileNotFoundException");
                close(reader, fis);
                return false;
            } catch (IOException e10) {
                e = e10;
                Slog.e(TAG, "getBrightnessRangeFromPanelInfo error! IOException " + e);
                close(reader, fis);
                return false;
            } catch (Exception e11) {
                e2 = e11;
                Slog.e(TAG, "getBrightnessRangeFromPanelInfo error! Exception " + e2);
                close(reader, fis);
                return false;
            }
            return false;
        }
        if (DEBUG) {
            Slog.w(TAG, "getBrightnessRangeFromPanelInfo PANEL_INFO_NODE:" + PANEL_INFO_NODE + " isn't exist");
        }
        return false;
    }

    private boolean parsePanelInfo(String[] stringSplited) {
        if (stringSplited == null) {
            return false;
        }
        int max = -1;
        int min = -1;
        int deviceLevel = 0;
        int actualMaxNit = 0;
        int standardMaxNit = 0;
        String key = null;
        int i = 0;
        while (i < stringSplited.length) {
            try {
                key = "blmax:";
                int index = stringSplited[i].indexOf(key);
                if (index != -1) {
                    max = Integer.parseInt(stringSplited[i].substring(key.length() + index));
                } else {
                    key = "blmin:";
                    index = stringSplited[i].indexOf(key);
                    if (index != -1) {
                        min = Integer.parseInt(stringSplited[i].substring(key.length() + index));
                    } else {
                        key = "bldevicelevel:";
                        index = stringSplited[i].indexOf(key);
                        if (index != -1) {
                            deviceLevel = Integer.parseInt(stringSplited[i].substring(key.length() + index));
                        } else {
                            key = "blmax_nit_actual:";
                            index = stringSplited[i].indexOf(key);
                            if (index != -1) {
                                actualMaxNit = Integer.parseInt(stringSplited[i].substring(key.length() + index));
                            } else {
                                key = "blmax_nit_standard:";
                                index = stringSplited[i].indexOf(key);
                                if (index != -1) {
                                    standardMaxNit = Integer.parseInt(stringSplited[i].substring(key.length() + index));
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
        FileNotFoundException e;
        IOException e2;
        Exception e3;
        Throwable th;
        File file = new File(MAX_BRIGHTNESS_NODE);
        if (file.exists()) {
            BufferedReader reader = null;
            FileInputStream fis = null;
            try {
                BufferedReader reader2;
                FileInputStream fis2 = new FileInputStream(file);
                try {
                    reader2 = new BufferedReader(new InputStreamReader(fis2, "UTF-8"));
                } catch (FileNotFoundException e4) {
                    e = e4;
                    fis = fis2;
                    e.printStackTrace();
                    close(reader, fis);
                    return false;
                } catch (IOException e5) {
                    e2 = e5;
                    fis = fis2;
                    e2.printStackTrace();
                    close(reader, fis);
                    return false;
                } catch (Exception e6) {
                    e3 = e6;
                    fis = fis2;
                    try {
                        e3.printStackTrace();
                        close(reader, fis);
                        return false;
                    } catch (Throwable th2) {
                        th = th2;
                        close(reader, fis);
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    fis = fis2;
                    close(reader, fis);
                    throw th;
                }
                try {
                    String tempString = reader2.readLine();
                    if (tempString != null) {
                        this.mNormalizedMaxBrightness = Integer.parseInt(tempString);
                        this.mNormalizedMinBrightness = (this.mNormalizedMaxBrightness * 4) / 255;
                        if (DEBUG) {
                            Slog.i(TAG, "getBrightnessRangeFromMaxBrightness success! min = " + this.mNormalizedMinBrightness + ", max = " + this.mNormalizedMaxBrightness);
                        }
                        reader2.close();
                        close(reader2, fis2);
                        return true;
                    }
                    close(reader2, fis2);
                    reader = reader2;
                    return false;
                } catch (FileNotFoundException e7) {
                    e = e7;
                    fis = fis2;
                    reader = reader2;
                    e.printStackTrace();
                    close(reader, fis);
                    return false;
                } catch (IOException e8) {
                    e2 = e8;
                    fis = fis2;
                    reader = reader2;
                    e2.printStackTrace();
                    close(reader, fis);
                    return false;
                } catch (Exception e9) {
                    e3 = e9;
                    fis = fis2;
                    reader = reader2;
                    e3.printStackTrace();
                    close(reader, fis);
                    return false;
                } catch (Throwable th4) {
                    th = th4;
                    fis = fis2;
                    reader = reader2;
                    close(reader, fis);
                    throw th;
                }
            } catch (FileNotFoundException e10) {
                e = e10;
                e.printStackTrace();
                close(reader, fis);
                return false;
            } catch (IOException e11) {
                e2 = e11;
                e2.printStackTrace();
                close(reader, fis);
                return false;
            } catch (Exception e12) {
                e3 = e12;
                e3.printStackTrace();
                close(reader, fis);
                return false;
            }
        }
        if (DEBUG) {
            Slog.w(TAG, "getBrightnessRangeFromMaxBrightness MAX_BRIGHTNESS_NODE:" + MAX_BRIGHTNESS_NODE + " isn't exist");
        }
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

    protected void getNormalizedBrightnessRangeFromKernel() {
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
        int brightnessvalue = level;
        if (!this.mSupportXCC_isloaded) {
            this.mSupportXCC = this.mDisplayEngineManager.getSupported(16);
            this.mSupportXCC_isloaded = true;
        }
        if (this.mSupportXCC == 1) {
            PersistableBundle bundle = new PersistableBundle();
            bundle.putInt("MinBrightness", this.mNormalizedMinBrightness);
            bundle.putInt("MaxBrightness", this.mNormalizedMaxBrightness);
            bundle.putInt("brightnesslevel", level);
            brightnessvalue = this.mDisplayEngineManager.setData(6, bundle);
            if (brightnessvalue <= 0) {
                brightnessvalue = this.mNormalizedMinBrightness + (((level - 156) * (this.mNormalizedMaxBrightness - this.mNormalizedMinBrightness)) / 9844);
            }
        } else {
            brightnessvalue = this.mNormalizedMinBrightness + (((level - 156) * (this.mNormalizedMaxBrightness - this.mNormalizedMinBrightness)) / 9844);
        }
        if (!this.mSupportHBM_isloaded) {
            this.mSupportHBM = this.mDisplayEngineManager.getSupported(20);
            this.mSupportHBM_isloaded = true;
        }
        if (this.mSupportHBM == 1) {
            this.mDisplayEngineManager.setScene(22, brightnessvalue);
        }
        return brightnessvalue;
    }

    protected int mapIntoRealBacklightLevel(int level) {
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

    protected int getNormalizedMaxBrightness() {
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
}
