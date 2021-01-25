package com.android.server.lights;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.display.HwFoldScreenState;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.RemoteException;
import android.util.ArrayMap;
import android.util.Log;
import com.huawei.android.fsm.HwFoldScreenManagerEx;
import com.huawei.android.os.HwBrightnessProcessorEx;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.android.provider.SettingsEx;
import com.huawei.android.server.SystemServiceEx;
import com.huawei.android.util.SlogEx;
import com.huawei.displayengine.DisplayEngineManager;
import com.huawei.displayengine.IDisplayEngineServiceEx;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import com.huawei.hiai.awareness.client.AwarenessResult;
import com.huawei.util.HwPartCommInterfaceWraper;
import com.huawei.util.LogEx;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class HwLightsService extends LightsServiceEx {
    private static final int BACKLIGHT_THRESHOLD = 2;
    private static final int BL_DEFAULT = -1;
    private static final boolean DEBUG = (LogEx.getLogHWInfo() || (LogEx.getHWModuleLog() && Log.isLoggable(TAG, 4)));
    private static final int FRAMES_INTERVAL = 16;
    private static final int INDEX_BL_MAX = 0;
    private static final int INDEX_BL_MAX_NIT_ACTUAL = 3;
    private static final int INDEX_BL_MAX_NIT_STANDARD = 4;
    private static final int INDEX_BL_MIN = 1;
    private static final int INDEX_DEVICE_LEVEL = 2;
    private static final int INIT_DEFAULT_BRIGHTNESS = -1;
    private static final int MANUFACTURE_BRIGHTNESS_DEFUALT_VALUE = -1;
    private static final int MANUFACTURE_BRIGHTNESS_SIZE = 4;
    private static final int MAX_BRIGHTNESS = 10000;
    private static final String MAX_BRIGHTNESS_NODE = "/sys/class/leds/lcd_backlight0/max_brightness";
    private static final int MIN_BRIGHTNESS = 156;
    private static final int MSG_UPDATE_BRIGHTNESS = 0;
    private static final int NORMALIZED_DEFAULT_MAX_BRIGHTNESS = 255;
    private static final int NORMALIZED_DEFAULT_MIN_BRIGHTNESS = 4;
    private static final int NORMALIZED_MAX_BRIGHTNESS = 10000;
    private static final String PANEL_INFO_NODE = "/sys/class/graphics/fb0/panel_info";
    private static final int PANEL_INFO_SIZE = 5;
    private static final int REFRESH_FRAMES_CMD = 1;
    private static final int STRING_SPLITED_LENGTH = 2;
    private static final int SUCCESS_RETURN_VALUE = 0;
    private static final String TAG = "HwLightsService";
    private static boolean sHasShutDown = false;
    private static boolean sInMirrorLinkBrightnessMode = false;
    private boolean mAutoBrightnessEnabled = false;
    private BackLightLevelLogPrinter mBackLightLevelPrinter = null;
    private int mBrightnessLevel = -1;
    private ContentResolver mContentResolver;
    private Context mContext;
    private int mCurrentBrightnessLevelForHighPrecision = 0;
    private int mCurrentDisplayMode = 0;
    private int mCurrentUserId = 0;
    private int mDefaultNormalizedMaxBrightness = -1;
    private int mDefaultNormalizedMinBrightness = -1;
    private int mDeviceActualBrightnessNit = 0;
    private int mDeviceBrightnessLevel = 0;
    private int mDeviceStandardBrightnessNit = 0;
    private DisplayEngineManager mDisplayEngineManager;
    private final HwFoldScreenManagerEx.FoldDisplayModeListener mFoldDisplayModeListener = new HwFoldScreenManagerEx.FoldDisplayModeListener() {
        /* class com.android.server.lights.HwLightsService.AnonymousClass1 */

        public void onScreenDisplayModeChange(int displayMode) {
            SlogEx.i(HwLightsService.TAG, "onScreenDisplayModeChange displayMode=" + displayMode);
            if (HwLightsService.this.mCurrentDisplayMode != displayMode) {
                if (HwLightsService.DEBUG) {
                    SlogEx.i(HwLightsService.TAG, "onScreenDisplayModeChange mCurrentDisplayMode=" + HwLightsService.this.mCurrentDisplayMode + "-->displayMode=" + displayMode);
                }
                HwLightsService.this.mCurrentDisplayMode = displayMode;
                int minBrightness = HwLightsService.this.mDefaultNormalizedMinBrightness;
                int maxBrightness = HwLightsService.this.mDefaultNormalizedMaxBrightness;
                int i = HwLightsService.this.mCurrentDisplayMode;
                if (i != 1) {
                    if (i != 2) {
                        SlogEx.i(HwLightsService.TAG, "onScreenDisplayModeChange, DISPLAY_MODE_UNKNOWN");
                    } else if (HwLightsService.this.mOutwardNormalizedMinBrightness > 0 && HwLightsService.this.mOutwardNormalizedMaxBrightness > 0) {
                        minBrightness = HwLightsService.this.mOutwardNormalizedMinBrightness;
                        maxBrightness = HwLightsService.this.mOutwardNormalizedMaxBrightness;
                        SlogEx.i(HwLightsService.TAG, "onScreenDisplayModeChange, DISPLAY_MODE_MAIN");
                    }
                } else if (HwLightsService.this.mInwardNormalizedMinBrightness > 0 && HwLightsService.this.mInwardNormalizedMaxBrightness > 0) {
                    minBrightness = HwLightsService.this.mInwardNormalizedMinBrightness;
                    maxBrightness = HwLightsService.this.mInwardNormalizedMaxBrightness;
                    SlogEx.i(HwLightsService.TAG, "onScreenDisplayModeChange, DISPLAY_MODE_FULL");
                }
                HwLightsService.this.updateMinMaxBrightnessForDisplayModeChange(minBrightness, maxBrightness);
            }
        }
    };
    private final ArrayMap<String, HwBrightnessProcessorEx> mHwBrightnessProcessors = new ArrayMap<>();
    private HwNormalizedBrightnessMapping mHwNormalizedBrightnessMapping;
    private boolean mHwNormalizedBrightnessMappingEnableLoaded = false;
    private int mInwardNormalizedMaxBrightness = -1;
    private int mInwardNormalizedMinBrightness = -1;
    private boolean mIsDisplayModeListenerEnabled = false;
    private volatile boolean mLightsBypass = false;
    private boolean mNeedBrightnessMappingEnable = false;
    private int mNormalizedMaxBrightness = -1;
    private int mNormalizedMinBrightness = -1;
    private int mOutwardNormalizedMaxBrightness = -1;
    private int mOutwardNormalizedMinBrightness = -1;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.server.lights.HwLightsService.AnonymousClass2 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                if ("android.intent.action.ACTION_SHUTDOWN".equals(action)) {
                    Log.i(HwLightsService.TAG, "handle ACTION_SHUTDOWN broadcast");
                    boolean unused = HwLightsService.sHasShutDown = true;
                } else if ("android.intent.action.SCREEN_OFF".equals(action)) {
                    if (HwLightsService.DEBUG) {
                        SlogEx.i(HwLightsService.TAG, "handle ACTION_SCREEN_OFF broadcast");
                    }
                    if (HwLightsService.this.mLightsBypass) {
                        HwLightsService.this.setManufactureBrightness(-1, -1, -1, -1);
                    }
                }
            }
        }
    };
    private Handler mRefreshFramesHandler = new Handler() {
        /* class com.android.server.lights.HwLightsService.AnonymousClass3 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            HwLightsService.this.mRefreshFramesHandler.removeMessages(1);
            HwLightsService.this.refreshFramesNative();
            HwLightsService.access$1310(HwLightsService.this);
            if (HwLightsService.this.mSblFrameCount > 0) {
                HwLightsService.this.mRefreshFramesHandler.sendEmptyMessageDelayed(1, 16);
            } else if (HwLightsService.this.mSblSetAfterRefresh) {
                HwLightsService hwLightsService = HwLightsService.this;
                hwLightsService.sendSmartBackLightWithRefreshFramesImpl(hwLightsService.mSblId, HwLightsService.this.mSblEnable, HwLightsService.this.mSblLevel, HwLightsService.this.mSblValue, 0, false, 0, 0);
                HwLightsService.this.mSblSetAfterRefresh = false;
            }
        }
    };
    private int mSblEnable;
    private int mSblFrameCount;
    private int mSblId;
    private int mSblLevel;
    private boolean mSblSetAfterRefresh;
    private int mSblValue;
    private boolean mSupportAmoled;
    private boolean mSupportAmoledLoaded;
    private boolean mSupportGammaFix;
    private boolean mSupportGammaFixLoaded;
    private boolean mSupportRgLed;
    private boolean mSupportRgLedLoaded;
    private boolean mSupportXcc;
    private boolean mSupportXccLoaded;
    private Handler mUpdateBrightnessHandler = new Handler() {
        /* class com.android.server.lights.HwLightsService.AnonymousClass4 */

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                int brightnessOut = (int) Math.ceil((double) ((((float) (65535 & HwLightsService.this.getLcdBrightness())) * 255.0f) / 10000.0f));
                SettingsEx.System.putIntForUser(HwLightsService.this.mContentResolver, "screen_auto_brightness", brightnessOut, HwLightsService.this.mCurrentUserId);
                if (brightnessOut != 0 && SettingsEx.System.getIntForUser(HwLightsService.this.mContentResolver, "screen_brightness_mode", 1, HwLightsService.this.mCurrentUserId) == 1) {
                    SettingsEx.System.putIntForUser(HwLightsService.this.mContentResolver, "screen_brightness", brightnessOut, HwLightsService.this.mCurrentUserId);
                }
            }
        }
    };

    static /* synthetic */ int access$1310(HwLightsService x0) {
        int i = x0.mSblFrameCount;
        x0.mSblFrameCount = i - 1;
        return i;
    }

    public HwLightsService(Context context) {
        super(context);
        boolean z = false;
        this.mContext = context;
        this.mContentResolver = this.mContext.getContentResolver();
        getNormalizedBrightnessRangeFromKernel();
        setIsHighPrecision(this.mNormalizedMaxBrightness > 255 ? true : z);
        this.mDisplayEngineManager = new DisplayEngineManager();
        loadHwBrightnessProcessors();
        this.mHwNormalizedBrightnessMapping = new HwNormalizedBrightnessMapping(MIN_BRIGHTNESS, AwarenessResult.Code.SUCCESS, this.mNormalizedMinBrightness, this.mNormalizedMaxBrightness);
    }

    public void onBootPhase(int phase) {
        if (phase == SystemServiceEx.PHASE_BOOT_COMPLETED) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.ACTION_SHUTDOWN");
            filter.addAction("android.intent.action.SCREEN_OFF");
            this.mContext.registerReceiver(this.mReceiver, filter);
        }
    }

    public void sendSmartBackLightWithRefreshFramesImpl(int id, int enable, int level, int value, int frames, boolean setAfterRefresh, int enable2, int value2) {
        if (id != LightsManagerEx.LIGHT_ID_SMARTBACKLIGHT) {
            SlogEx.e(TAG, "id = " + id + ", error! this mothod only used for SBL!");
            return;
        }
        synchronized (this) {
            try {
                setLightNative(id, (65535 & (value > 65535 ? 65535 : value)) | ((enable & 1) << 24) | ((level & 255) << 16), 0, 0, 0, 0);
                this.mSblId = id;
                this.mSblFrameCount = frames;
                try {
                    this.mSblSetAfterRefresh = setAfterRefresh;
                    try {
                        this.mSblEnable = enable2;
                        this.mSblLevel = level;
                        this.mSblValue = value2;
                        if (frames > 0) {
                            this.mRefreshFramesHandler.sendEmptyMessage(1);
                        }
                    } catch (Throwable th) {
                        th = th;
                        throw th;
                    }
                } catch (Throwable th2) {
                    th = th2;
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void sendUpdateaAutoBrightnessDbMsg() {
        Handler handler;
        if (getWriteAutoBrightnessDbEnable() && this.mAutoBrightnessEnabled && (handler = this.mUpdateBrightnessHandler) != null) {
            if (handler.hasMessages(0)) {
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
            SlogEx.i(TAG, "user change from  " + this.mCurrentUserId + " into " + userId);
        }
        this.mCurrentUserId = userId;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:34:0x009c, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x009d, code lost:
        $closeResource(r5, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00a0, code lost:
        throw r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00a3, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00a4, code lost:
        $closeResource(r4, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00a7, code lost:
        throw r5;
     */
    private boolean getBrightnessRangeFromPanelInfo() {
        File file = new File(PANEL_INFO_NODE);
        if (!file.exists()) {
            if (DEBUG) {
                SlogEx.w(TAG, "getBrightnessRangeFromPanelInfo PANEL_INFO_NODE:/sys/class/graphics/fb0/panel_info isn't exist");
            }
            return false;
        }
        try {
            FileInputStream stream = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            String text = reader.readLine();
            if (text != null) {
                if (!text.isEmpty()) {
                    if (DEBUG) {
                        SlogEx.i(TAG, "getBrightnessRangeFromPanelInfo text = " + text);
                    }
                    String[] stringSplited = text.split(AwarenessInnerConstants.COMMA_KEY);
                    if (stringSplited.length < 2) {
                        SlogEx.e(TAG, "split failed! text = " + text);
                        $closeResource(null, reader);
                        $closeResource(null, stream);
                        return false;
                    } else if (parsePanelInfo(stringSplited)) {
                        $closeResource(null, reader);
                        $closeResource(null, stream);
                        return true;
                    } else {
                        $closeResource(null, reader);
                        $closeResource(null, stream);
                        return false;
                    }
                }
            }
            SlogEx.e(TAG, "getBrightnessRangeFromPanelInfo error! file is empty");
            $closeResource(null, reader);
            $closeResource(null, stream);
            return false;
        } catch (FileNotFoundException e) {
            SlogEx.e(TAG, "getBrightnessRangeFromPanelInfo error!");
        } catch (IOException e2) {
            SlogEx.e(TAG, "getBrightnessRangeFromPanelInfo error! IOException");
        }
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    private boolean parsePanelInfo(String[] stringSplited) {
        if (stringSplited == null) {
            return false;
        }
        String panelInfo = null;
        String[] panelInfos = {"blmax:", "blmin:", "bldevicelevel:", "blmax_nit_actual:", "blmax_nit_standard:"};
        int[] data = {-1, -1, 0, 0, 0};
        for (int i = 0; i < panelInfos.length; i++) {
            try {
                panelInfo = panelInfos[i];
                for (int j = 0; j < stringSplited.length; j++) {
                    int index = stringSplited[j].indexOf(panelInfo);
                    if (index != -1) {
                        data[i] = Integer.parseInt(stringSplited[j].substring(panelInfo.length() + index));
                    }
                }
            } catch (NumberFormatException e) {
                SlogEx.e(TAG, "parsePanelInfo() error! " + panelInfo + e);
                return false;
            }
        }
        if (data[0] == -1 || data[1] == -1 || data.length != 5) {
            return false;
        }
        if (DEBUG) {
            SlogEx.i(TAG, "BrightnessRange success! max = " + data[0] + ", min = " + data[1] + ", deviceLevel = " + data[2] + ",actualMaxNit=" + data[3] + ",standardMaxNit=" + data[4]);
        }
        this.mNormalizedMaxBrightness = data[0];
        this.mNormalizedMinBrightness = data[1];
        this.mDeviceBrightnessLevel = data[2];
        this.mDeviceActualBrightnessNit = data[3];
        this.mDeviceStandardBrightnessNit = data[4];
        this.mDefaultNormalizedMaxBrightness = this.mNormalizedMaxBrightness;
        this.mDefaultNormalizedMinBrightness = this.mNormalizedMinBrightness;
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:26:0x008b, code lost:
        r6 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x008c, code lost:
        $closeResource(r5, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x008f, code lost:
        throw r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0092, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0093, code lost:
        $closeResource(r4, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0096, code lost:
        throw r5;
     */
    private boolean getBrightnessRangeFromMaxBrightness() {
        File file = new File(MAX_BRIGHTNESS_NODE);
        if (!file.exists()) {
            if (DEBUG) {
                SlogEx.w(TAG, "getBrightnessRangeFromMaxBrightness MAX_BRIGHTNESS_NODE:/sys/class/leds/lcd_backlight0/max_brightness isn't exist");
            }
            return false;
        }
        try {
            FileInputStream stream = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(stream, "UTF-8"));
            String text = reader.readLine();
            if (text != null) {
                if (!text.isEmpty()) {
                    this.mNormalizedMaxBrightness = Integer.parseInt(text);
                    this.mNormalizedMinBrightness = (this.mNormalizedMaxBrightness * 4) / 255;
                    this.mDefaultNormalizedMaxBrightness = this.mNormalizedMaxBrightness;
                    this.mDefaultNormalizedMinBrightness = this.mNormalizedMinBrightness;
                    if (DEBUG) {
                        SlogEx.i(TAG, "getBrightnessRangeFromMaxBrightness success! min = " + this.mNormalizedMinBrightness + ", max = " + this.mNormalizedMaxBrightness);
                    }
                    $closeResource(null, reader);
                    $closeResource(null, stream);
                    return true;
                }
            }
            SlogEx.e(TAG, "getBrightnessRangeFromMaxBrightness error! file is empty");
            $closeResource(null, reader);
            $closeResource(null, stream);
            return false;
        } catch (FileNotFoundException e) {
            SlogEx.e(TAG, "getBrightnessRangeFromMaxBrightness error!");
            return false;
        } catch (IOException e2) {
            SlogEx.e(TAG, "getBrightnessRangeFromMaxBrightness error! IOException");
            return false;
        } catch (NumberFormatException e3) {
            SlogEx.e(TAG, "getBrightnessRangeFromMaxBrightness error! NumberFormatException");
            return false;
        }
    }

    private void checkNormalizedBrightnessRange() {
        int i;
        int i2 = this.mNormalizedMinBrightness;
        if (i2 < 0 || i2 >= (i = this.mNormalizedMaxBrightness) || i > 10000) {
            this.mNormalizedMinBrightness = 4;
            this.mNormalizedMaxBrightness = 255;
            SlogEx.e(TAG, "checkNormalizedBrightnessRange failed! load default brightness range: min = " + this.mNormalizedMinBrightness + ", max = " + this.mNormalizedMaxBrightness);
            return;
        }
        SlogEx.i(TAG, "checkNormalizedBrightnessRange success! range: min = " + this.mNormalizedMinBrightness + ", max = " + this.mNormalizedMaxBrightness);
    }

    private void getNormalizedBrightnessRangeFromKernel() {
        if (getBrightnessRangeFromPanelInfo()) {
            checkNormalizedBrightnessRange();
            return;
        }
        if (DEBUG) {
            SlogEx.w(TAG, "getBrightnessRangeFromPanelInfo failed");
        }
        if (getBrightnessRangeFromMaxBrightness()) {
            checkNormalizedBrightnessRange();
            return;
        }
        if (DEBUG) {
            SlogEx.w(TAG, "getBrightnessRangeFromMaxBrightness failed");
        }
        checkNormalizedBrightnessRange();
    }

    private int getXccMapIntoRealBacklight(int level) {
        if (!this.mSupportXccLoaded) {
            this.mSupportXcc = this.mDisplayEngineManager.getSupported(16) == 1;
            this.mSupportXccLoaded = true;
            SlogEx.i(TAG, "mSupportXcc = " + this.mSupportXcc);
        }
        if (!this.mSupportXcc || this.mLightsBypass) {
            return convertPrecisionHighToLow(level, false);
        }
        PersistableBundle bundle = new PersistableBundle();
        bundle.putInt("MinBrightness", this.mNormalizedMinBrightness);
        bundle.putInt("MaxBrightness", this.mNormalizedMaxBrightness);
        bundle.putInt("brightnesslevel", level);
        int brightnessValue = this.mDisplayEngineManager.setData(6, bundle);
        if (brightnessValue <= 0) {
            return convertPrecisionHighToLow(level, false);
        }
        return brightnessValue;
    }

    private int mapIntoRealBacklightLevelIfNeedXnit(int level) {
        this.mBrightnessLevel = level;
        initInwardFoldDeviceParameters();
        initBrightnessMapping();
        int brightnessValue = getXccMapIntoRealBacklight(level);
        boolean z = false;
        if (!this.mSupportAmoledLoaded) {
            this.mSupportAmoled = this.mDisplayEngineManager.getSupported(25) == 1;
            this.mSupportAmoledLoaded = true;
            SlogEx.i(TAG, "mSupportAmoled = " + this.mSupportAmoled);
        }
        if (!this.mSupportGammaFixLoaded) {
            byte[] status = new byte[1];
            if (this.mDisplayEngineManager.getEffect(7, 0, status, 1) == 0) {
                this.mSupportGammaFix = status[0] == 1;
                SlogEx.i(TAG, "[effect] getEffect(DE_FEATURE_GAMMA):" + this.mSupportGammaFix);
            }
            this.mSupportGammaFixLoaded = true;
            SlogEx.i(TAG, "mSupportGammaFix = " + this.mSupportGammaFix);
        }
        if (!this.mSupportRgLedLoaded) {
            if (this.mDisplayEngineManager.getSupported(19) == 1) {
                z = true;
            }
            this.mSupportRgLed = z;
            this.mSupportRgLedLoaded = true;
            SlogEx.i(TAG, "mSupportRgLed = " + this.mSupportRgLed);
        }
        if (!this.mLightsBypass && (this.mSupportAmoled || this.mSupportGammaFix || this.mSupportRgLed)) {
            int brightnessHighPrecision = level;
            if (this.mNeedBrightnessMappingEnable) {
                brightnessHighPrecision = this.mHwNormalizedBrightnessMapping.getMappingBrightnessHighPrecision(level, this.mCurrentDisplayMode);
            }
            this.mDisplayEngineManager.setScene(26, (brightnessHighPrecision << 16) | brightnessValue);
        }
        return brightnessValue;
    }

    /* access modifiers changed from: protected */
    public int mapIntoRealBacklightLevel(int level) {
        this.mCurrentBrightnessLevelForHighPrecision = level;
        BackLightLevelLogPrinter backLightLevelLogPrinter = this.mBackLightLevelPrinter;
        if (backLightLevelLogPrinter != null) {
            backLightLevelLogPrinter.printLevel(level);
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
        return mapIntoRealBacklightLevelIfNeedXnit(level);
    }

    private static class BackLightLevelLogPrinter {
        private String mLogTag = null;
        private int mPrintedLevel = 0;
        private float mThresholdPercent = 0.1f;

        BackLightLevelLogPrinter(String logTag, float thresholdPercent) {
            this.mThresholdPercent = thresholdPercent;
            this.mLogTag = logTag;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void printLevel(int level) {
            if (HwLightsService.DEBUG) {
                if ((level != 0 || this.mPrintedLevel == 0) && (level == 0 || this.mPrintedLevel != 0)) {
                    int threshold = (int) (((float) this.mPrintedLevel) * this.mThresholdPercent);
                    int threshold2 = 2;
                    if (threshold >= 2) {
                        threshold2 = threshold;
                    }
                    int delta = level - this.mPrintedLevel;
                    if ((delta < 0 ? -delta : delta) > threshold2) {
                        SlogEx.i(HwLightsService.TAG, this.mLogTag + " = " + level);
                        this.mPrintedLevel = level;
                        return;
                    }
                    return;
                }
                SlogEx.i(HwLightsService.TAG, this.mLogTag + " = " + level);
                this.mPrintedLevel = level;
            }
        }
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

    public void setMirrorLinkBrightnessStatusInternal(boolean status) {
        SlogEx.i(TAG, "setMirrorLinkBrightnessStatus status is " + status);
        sInMirrorLinkBrightnessMode = status;
    }

    /* access modifiers changed from: protected */
    public boolean shouldIgnoreSetBrightness(int brightness, int brightnessMode) {
        if (sInMirrorLinkBrightnessMode) {
            return true;
        }
        if (HwPartCommInterfaceWraper.isHwFastShutdownEnable()) {
            if (brightness <= 0) {
                return false;
            }
            SlogEx.i(TAG, "Ignore brightness " + brightness + " during fast shutdown");
            return true;
        } else if (!sHasShutDown) {
            return false;
        } else {
            SlogEx.i(TAG, "Ignore brightness " + brightness + " during shutdown");
            return true;
        }
    }

    public boolean setHwBrightnessDataImpl(String name, Bundle data, int[] result) {
        HwBrightnessProcessorEx processor = this.mHwBrightnessProcessors.get(name);
        if (processor != null) {
            return processor.setData(data, result);
        }
        return false;
    }

    public boolean getHwBrightnessDataImpl(String name, Bundle data, int[] result) {
        HwBrightnessProcessorEx processor = this.mHwBrightnessProcessors.get(name);
        if (processor != null) {
            return processor.getData(data, result);
        }
        return false;
    }

    private void loadHwBrightnessProcessors() {
        this.mHwBrightnessProcessors.put("ManufactureBrightness", new ManufactureBrightnessProcessor());
        this.mHwBrightnessProcessors.put("CurrentBrightness", new CurrentBrightnessProcessor());
    }

    /* access modifiers changed from: private */
    public final class ManufactureBrightnessProcessor extends HwBrightnessProcessorEx {
        ManufactureBrightnessProcessor() {
        }

        public boolean setData(Bundle data, int[] retValue) {
            if (retValue == null || retValue.length <= 0) {
                return false;
            }
            if (data == null) {
                SlogEx.w(HwLightsService.TAG, "setData data == null!");
                HwLightsService.this.setManufactureBrightness(-1, -1, -1, -1);
                retValue[0] = -1;
                return true;
            }
            retValue[0] = HwLightsService.this.setManufactureBrightness(data.getInt("ManufactureProcess", -1), data.getInt("Scene", -1), data.getInt("Level", -1), data.getInt("AnimationTime", -1));
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int setManufactureBrightness(int manufactureProcess, int scene, int level, int animationTime) {
        boolean isOverrideLight;
        int devicePrecisionLevel;
        int highPrecisionLevel;
        if (DEBUG) {
            SlogEx.i(TAG, "ManufactureBrightness: manufactureProcess=" + manufactureProcess + ",scene=" + scene + ",level=" + level + ",animationTime=" + animationTime);
        }
        if (manufactureProcess < 0 || scene < 0 || level < 0 || animationTime < 0) {
            highPrecisionLevel = this.mBrightnessLevel;
            devicePrecisionLevel = mapIntoRealBacklightLevelIfNeedXnit(highPrecisionLevel);
            if (this.mNeedBrightnessMappingEnable) {
                highPrecisionLevel = this.mHwNormalizedBrightnessMapping.getMappingBrightnessHighPrecision(highPrecisionLevel, this.mCurrentDisplayMode);
            }
            isOverrideLight = false;
        } else if (level == 0) {
            highPrecisionLevel = 0;
            devicePrecisionLevel = 0;
            isOverrideLight = true;
        } else {
            int brightness = level;
            if (level < 4) {
                brightness = 4;
            }
            if (level > 255) {
                brightness = 255;
            }
            int highPrecisionLevel2 = convertPrecisionLowToHigh(brightness);
            boolean isFactoryMode = getManufactureBrightnessHbmMode(scene) == ManufactureBrightnessMode.OFF;
            int devicePrecisionLevel2 = convertPrecisionHighToLow(highPrecisionLevel2, isFactoryMode);
            if (!isFactoryMode && this.mNeedBrightnessMappingEnable) {
                highPrecisionLevel2 = this.mHwNormalizedBrightnessMapping.getMappingBrightnessHighPrecision(highPrecisionLevel2, this.mCurrentDisplayMode);
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
            SlogEx.i(TAG, "ManufactureBrightness: setLightsBrightnessOverride isBypass = " + isBypass + ", devicePrecisionBrightness =" + devicePrecisionBrightness);
        }
        this.mLightsBypass = isBypass;
        synchronized (this) {
            setLightNative(LightsManagerEx.LIGHT_ID_BACKLIGHT_10000, devicePrecisionBrightness, 0, 0, 0, 0);
        }
    }

    private int convertPrecisionHighToLow(int highPrecisionBrightness, boolean isManufactureMode) {
        HwNormalizedBrightnessMapping hwNormalizedBrightnessMapping;
        int brightnessValue = -1;
        if (this.mNeedBrightnessMappingEnable && (hwNormalizedBrightnessMapping = this.mHwNormalizedBrightnessMapping) != null) {
            brightnessValue = isManufactureMode ? hwNormalizedBrightnessMapping.getMappingBrightnessForManufacture(highPrecisionBrightness, this.mCurrentDisplayMode) : hwNormalizedBrightnessMapping.getMappingBrightness(highPrecisionBrightness, this.mCurrentDisplayMode);
        }
        if (brightnessValue >= 0) {
            return brightnessValue;
        }
        int i = this.mNormalizedMinBrightness;
        return i + (((highPrecisionBrightness - 156) * (this.mNormalizedMaxBrightness - i)) / 9844);
    }

    private int convertPrecisionLowToHigh(int lowPrecisionBrightness) {
        return (lowPrecisionBrightness * AwarenessResult.Code.SUCCESS) / 255;
    }

    private ManufactureBrightnessMode getManufactureBrightnessHbmMode(int scene) {
        if (scene == -1) {
            return ManufactureBrightnessMode.DEFAULT;
        }
        if (scene == 0) {
            return ManufactureBrightnessMode.OFF;
        }
        if (scene == 1) {
            return ManufactureBrightnessMode.ON;
        }
        if (scene == 2) {
            return ManufactureBrightnessMode.ON;
        }
        SlogEx.w(TAG, "ManufactureBrightness: Unsupported scene, using default param!");
        return ManufactureBrightnessMode.DEFAULT;
    }

    private ManufactureBrightnessMode getOledDimmingMode(int scene) {
        if (scene == -1) {
            return ManufactureBrightnessMode.DEFAULT;
        }
        if (scene == 0) {
            return ManufactureBrightnessMode.OFF;
        }
        if (scene == 1) {
            return ManufactureBrightnessMode.OFF;
        }
        if (scene == 2) {
            return ManufactureBrightnessMode.ON;
        }
        SlogEx.w(TAG, "ManufactureBrightness: Unsupported scene, using default param!");
        return ManufactureBrightnessMode.DEFAULT;
    }

    private void setManufactureBrightnessToDisplayEngine(int manufactureProcess, int scene, int brightness) {
        int[] param = {this.mNormalizedMaxBrightness, brightness, getManufactureBrightnessHbmMode(scene).getValue(), getOledDimmingMode(scene).getValue()};
        PersistableBundle bundle = new PersistableBundle();
        bundle.putIntArray("Buffer", param);
        bundle.putInt("BufferLength", param.length * 4);
        this.mDisplayEngineManager.setData(11, bundle);
    }

    /* access modifiers changed from: private */
    public enum ManufactureBrightnessMode {
        DEFAULT(-1),
        OFF(0),
        ON(1);
        
        private final int mValue;

        private ManufactureBrightnessMode(int value) {
            this.mValue = value;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private int getValue() {
            return this.mValue;
        }
    }

    /* access modifiers changed from: private */
    public final class CurrentBrightnessProcessor extends HwBrightnessProcessorEx {
        CurrentBrightnessProcessor() {
        }

        public boolean getData(Bundle data, int[] retValue) {
            data.putInt("Brightness", HwLightsService.this.getCurrentBrightess());
            return true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int getCurrentBrightess() {
        int currentBrightness = 0;
        int i = this.mCurrentBrightnessLevelForHighPrecision;
        if (i == 0) {
            return 0;
        }
        if (i > 0) {
            currentBrightness = (int) (((((float) i) * 255.0f) / 10000.0f) + 0.5f);
        }
        if (currentBrightness < 4) {
            currentBrightness = 4;
        }
        if (currentBrightness > 255) {
            currentBrightness = 255;
        }
        SlogEx.i(TAG, "mCurrentlForHighPrecision=" + this.mCurrentBrightnessLevelForHighPrecision + ",currentBrightness=" + currentBrightness);
        return currentBrightness;
    }

    private void initInwardFoldDeviceParameters() {
        if (HwFoldScreenState.isInwardFoldDevice() && !this.mIsDisplayModeListenerEnabled) {
            this.mIsDisplayModeListenerEnabled = true;
            if (this.mHwNormalizedBrightnessMapping != null) {
                updateScreenMinMaxBrightnessFromPanelInfo();
                this.mHwNormalizedBrightnessMapping.initInwardFoldScreenMinMaxBrightness(this.mInwardNormalizedMinBrightness, this.mInwardNormalizedMaxBrightness, this.mOutwardNormalizedMinBrightness, this.mOutwardNormalizedMaxBrightness);
                HwFoldScreenManagerEx.registerFoldDisplayMode(this.mFoldDisplayModeListener);
                this.mCurrentDisplayMode = HwFoldScreenManagerEx.getDisplayMode();
                SlogEx.i(TAG, "registerFoldDisplayMode,mCurrentDisplayMode=" + this.mCurrentDisplayMode);
            }
        }
    }

    private void initBrightnessMapping() {
        if (!this.mHwNormalizedBrightnessMappingEnableLoaded) {
            this.mNeedBrightnessMappingEnable = this.mHwNormalizedBrightnessMapping.isNeedBrightnessMappingEnable();
            if (HwFoldScreenState.isInwardFoldDevice() && (this.mInwardNormalizedMinBrightness <= 0 || this.mInwardNormalizedMaxBrightness <= 0 || this.mOutwardNormalizedMinBrightness <= 0 || this.mOutwardNormalizedMaxBrightness <= 0)) {
                int i = this.mDefaultNormalizedMinBrightness;
                this.mInwardNormalizedMinBrightness = i;
                int i2 = this.mDefaultNormalizedMaxBrightness;
                this.mInwardNormalizedMaxBrightness = i2;
                this.mOutwardNormalizedMinBrightness = i;
                this.mOutwardNormalizedMaxBrightness = i2;
                SlogEx.i(TAG, "initDefaultBrightnessMapping min=" + this.mDefaultNormalizedMinBrightness + ",max=" + this.mDefaultNormalizedMaxBrightness);
            }
            this.mHwNormalizedBrightnessMappingEnableLoaded = true;
        }
    }

    private void updateScreenMinMaxBrightnessFromPanelInfo() {
        IBinder binder = ServiceManagerEx.getService("DisplayEngineExService");
        if (binder == null) {
            SlogEx.w(TAG, "updateScreenMinMaxBrightnessFromPanelInfo() binder is null!");
            return;
        }
        IDisplayEngineServiceEx mService = IDisplayEngineServiceEx.Stub.asInterface(binder);
        if (mService == null) {
            SlogEx.w(TAG, "updateScreenMinMaxBrightnessFromPanelInfo() mService is null!");
            return;
        }
        Bundle data = new Bundle();
        try {
            int ret = mService.getEffectEx(14, 13, data);
            if (ret != 0) {
                SlogEx.e(TAG, "updateScreenMinMaxBrightnessFromPanelInfo() getEffect failed! ret=" + ret);
            } else if (HwFoldScreenState.isInwardFoldDevice()) {
                this.mInwardNormalizedMinBrightness = data.getInt("FullMinBacklight");
                this.mInwardNormalizedMaxBrightness = data.getInt("FullMaxBacklight");
                this.mOutwardNormalizedMinBrightness = data.getInt("MainMinBacklight");
                this.mOutwardNormalizedMaxBrightness = data.getInt("MainMaxBacklight");
                SlogEx.i(TAG, "InwardMin=" + this.mInwardNormalizedMinBrightness + ",InwardMax=" + this.mInwardNormalizedMaxBrightness + ",OutwardMin=" + this.mOutwardNormalizedMinBrightness + ",OutwardMax=" + this.mOutwardNormalizedMaxBrightness);
            }
        } catch (RemoteException e) {
            SlogEx.e(TAG, "updateScreenMinMaxBrightnessFromPanelInfo() RemoteException ");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateMinMaxBrightnessForDisplayModeChange(int minBrightness, int maxBrightness) {
        if (DEBUG) {
            SlogEx.i(TAG, "updateMinMaxBrightnessForDisplayModeChange, minBrightness=" + minBrightness + ",maxBrightness=" + maxBrightness);
        }
        if (minBrightness > 0 && maxBrightness > 0) {
            if (maxBrightness != this.mNormalizedMaxBrightness || minBrightness != this.mNormalizedMinBrightness) {
                this.mNormalizedMinBrightness = minBrightness;
                this.mNormalizedMaxBrightness = maxBrightness;
                SlogEx.i(TAG, "updateMinMaxBrightnessForDisplayModeChange,mNormalizedMinBrightness=" + this.mNormalizedMinBrightness + ",mNormalizedMaxBrightness=" + this.mNormalizedMaxBrightness + ",mCurrentDisplayMode=" + this.mCurrentDisplayMode);
            }
        }
    }
}
