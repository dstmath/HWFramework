package com.android.server.power;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.BatteryManagerInternal;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.SystemProperties;
import android.os.UEventObserver;
import android.os.UEventObserver.UEvent;
import android.provider.Settings.System;
import android.service.dreams.DreamManagerInternal;
import android.util.Log;
import com.android.server.dreams.HwCustDreamManagerServiceImpl;

public class HwCustPowerManagerServiceImpl extends HwCustPowerManagerService {
    static final int C2DM_ACQUIRE_EVENT = 1;
    static final int C2DM_TIMEOUT_EVENT = 2;
    public static final String CHARGING_ALBUM_COMPONENTS = "charging_album_components";
    public static final String CHARGING_ALBUM_ENABLED = "charging_album_enabled";
    public static final String CHARGING_ALBUM_MODE = "charging_album_mode";
    public static final int CHARGING_ALBUM_MODE_BASE = 0;
    public static final int CHARGING_ALBUM_MODE_USB = 1;
    public static final int CHARGING_ALBUM_OFF = 0;
    public static final int CHARGING_ALBUM_ON = 1;
    public static final String CHARGING_ALBUM_PATH = "charging_album_path";
    public static final int CHARGING_ALBUM_PATH_DEFAULT = 1;
    public static final int CHARGING_ALBUM_PATH_GALLARY = 0;
    private static final int CHARING_MODE_BASE = 0;
    private static final int CHARING_MODE_USB = 1;
    private static final String CHARING_UEVENT_BASE = "custom_acc5_mode";
    private static final String CHARING_UEVENT_CONNECTED = "pedestal_attach";
    private static final String CHARING_UEVENT_DISCONNECETED = "pedestal_detach";
    private static final String CHARING_UEVENT_ITEM = "USB_CUSTOM_ACC5";
    protected static final boolean HWDBG;
    protected static final boolean HWFLOW;
    protected static final boolean HWLOGW_E = true;
    private static final String TAG = "HwCustPowerManager";
    private static final String TAG_FLOW = "HwCustPowerManager_FLOW";
    private static final String TAG_INIT = "HwCustPowerManager_INIT";
    private int C2DM_DELAY_TIMEOUT;
    private BatteryManagerInternal mBatteryManagerInternal;
    private HandlerThread mC2DMHandlerThread;
    private C2DMHelperHandler mC2DMHelperHandler;
    private WakeLock mC2DMWakeLock;
    private boolean mChargingAlbumEnabled;
    private int mChargingMode;
    private boolean mCharingBase = false;
    private UEventObserver mCharingObserver = new UEventObserver() {
        public void onUEvent(UEvent event) {
            String state = event.get(HwCustPowerManagerServiceImpl.CHARING_UEVENT_ITEM);
            if (HwCustPowerManagerServiceImpl.HWDBG) {
                Log.d(HwCustPowerManagerServiceImpl.TAG, "mCharingObserver:state=" + state);
            }
            if (HwCustPowerManagerServiceImpl.CHARING_UEVENT_CONNECTED.equals(state)) {
                HwCustPowerManagerServiceImpl.this.mCharingBase = HwCustPowerManagerServiceImpl.HWLOGW_E;
            } else if (HwCustPowerManagerServiceImpl.CHARING_UEVENT_DISCONNECETED.equals(state)) {
                HwCustPowerManagerServiceImpl.this.mCharingBase = false;
            }
        }
    };
    private Context mContext;
    private DreamManagerInternal mDreamManager;
    private boolean mEnableC2DMDelay = SystemProperties.getBoolean("ro.config.enable_c2dm_delay", false);
    private ContentObserver mSettingsObserver;
    private boolean startDreamFromUser = false;

    public class C2DMHelperHandler extends Handler {
        public C2DMHelperHandler(Looper looper) {
            super(looper, null, HwCustPowerManagerServiceImpl.HWLOGW_E);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (HwCustPowerManagerServiceImpl.HWDBG) {
                        Log.d(HwCustPowerManagerServiceImpl.TAG, "handleMessage C2DM_ACQUIRE_EVENT, mC2DMWakeLock.acquire!");
                    }
                    if (!HwCustPowerManagerServiceImpl.this.mC2DMWakeLock.isHeld()) {
                        if (HwCustPowerManagerServiceImpl.HWDBG) {
                            Log.d(HwCustPowerManagerServiceImpl.TAG, "mC2DMWakeLock has not been hold!");
                        }
                        HwCustPowerManagerServiceImpl.this.mC2DMWakeLock.acquire();
                    }
                    removeMessages(HwCustPowerManagerServiceImpl.C2DM_TIMEOUT_EVENT);
                    sendMessageDelayed(obtainMessage(HwCustPowerManagerServiceImpl.C2DM_TIMEOUT_EVENT), (long) HwCustPowerManagerServiceImpl.this.C2DM_DELAY_TIMEOUT);
                    return;
                case HwCustPowerManagerServiceImpl.C2DM_TIMEOUT_EVENT /*2*/:
                    HwCustPowerManagerServiceImpl.this.mC2DMWakeLock.release();
                    if (HwCustPowerManagerServiceImpl.HWDBG) {
                        Log.d(HwCustPowerManagerServiceImpl.TAG, "handleMessage C2DM_TIMEOUT_EVENT, mC2DMWakeLock.release!");
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    static {
        boolean z;
        boolean z2 = HWLOGW_E;
        if (Log.HWLog) {
            z = HWLOGW_E;
        } else {
            z = Log.HWModuleLog ? Log.isLoggable(TAG, 3) : false;
        }
        HWDBG = z;
        if (!Log.HWINFO) {
            z2 = Log.HWModuleLog ? Log.isLoggable(TAG, 4) : false;
        }
        HWFLOW = z2;
    }

    public HwCustPowerManagerServiceImpl(Context context) {
        super(context);
        this.mContext = context;
        if (isChargingAlbumSupported()) {
            this.mCharingObserver.startObserving("DEVPATH=/devices/virtual/usbswitch/usbsw");
        }
    }

    public void systemReady(BatteryManagerInternal batterymanager, DreamManagerInternal dreammanager, ContentObserver observer) {
        if (isChargingAlbumSupported()) {
            this.mBatteryManagerInternal = batterymanager;
            this.mDreamManager = dreammanager;
            this.mSettingsObserver = observer;
            ContentResolver resolver = this.mContext.getContentResolver();
            resolver.registerContentObserver(System.getUriFor(CHARGING_ALBUM_ENABLED), false, this.mSettingsObserver, -1);
            resolver.registerContentObserver(System.getUriFor(CHARGING_ALBUM_MODE), false, this.mSettingsObserver, -1);
        }
    }

    public boolean readConfigurationLocked(boolean config) {
        boolean myconfig = isChargingAlbumSupported();
        if (myconfig) {
            return myconfig;
        }
        return super.readConfigurationLocked(config);
    }

    public void updateSettingsLocked() {
        boolean z = HWLOGW_E;
        if (isChargingAlbumSupported()) {
            ContentResolver resolver = this.mContext.getContentResolver();
            if (System.getInt(resolver, CHARGING_ALBUM_ENABLED, 1) == 0) {
                z = false;
            }
            this.mChargingAlbumEnabled = z;
            this.mChargingMode = System.getInt(resolver, CHARGING_ALBUM_MODE, 0);
        }
    }

    public boolean isStartDreamFromUser() {
        return isChargingAlbumSupported() ? this.startDreamFromUser : false;
    }

    public void setStartDreamFromUser(boolean flag) {
        if (isChargingAlbumSupported()) {
            this.startDreamFromUser = flag;
        }
    }

    public boolean isChargingAlbumSupported() {
        return HwCustDreamManagerServiceImpl.mChargingAlbumSupported;
    }

    public boolean isChargingAlbumEnabled() {
        if (!isChargingAlbumSupported()) {
            return super.isChargingAlbumEnabled();
        }
        if (HWDBG) {
            Log.d(TAG, "isChargingAlbumenable " + this.mChargingAlbumEnabled);
        }
        return this.mChargingAlbumEnabled;
    }

    public boolean canDreamLocked() {
        if (HWFLOW) {
            Log.i(TAG_FLOW, "canDreamLocked");
        }
        if (!isChargingAlbumSupported()) {
            return super.canDreamLocked();
        }
        if (this.mChargingMode != 0 || (this.mCharingBase ^ 1) == 0) {
            return false;
        }
        return HWLOGW_E;
    }

    public void handleDreamLocked() {
        if (HWFLOW) {
            Log.i(TAG_FLOW, "handleDreamLocked");
        }
        if (isChargingAlbumSupported() && this.mDreamManager != null && (this.mBatteryManagerInternal.isPowered(7) ^ 1) != 0) {
            this.mDreamManager.stopDream(HWLOGW_E);
        }
    }

    public boolean startDream(boolean bwakefullness) {
        if (HWFLOW) {
            Log.i(TAG_FLOW, "startdream");
        }
        if (!isChargingAlbumSupported()) {
            return super.startDream(bwakefullness);
        }
        if (this.mDreamManager == null || !this.mChargingAlbumEnabled || ((this.mChargingMode == 0 && !this.mCharingBase) || !this.mBatteryManagerInternal.isPowered(7))) {
            return false;
        }
        if (HWFLOW) {
            Log.i(TAG_FLOW, "start powermanagerservice dreamservice");
        }
        this.mDreamManager.startDream(bwakefullness);
        this.startDreamFromUser = HWLOGW_E;
        return HWLOGW_E;
    }

    public boolean stopDream() {
        if (HWFLOW) {
            Log.i(TAG_FLOW, "stopDream()");
        }
        if (!isChargingAlbumSupported()) {
            return super.stopDream();
        }
        if (this.mDreamManager == null) {
            return false;
        }
        this.mDreamManager.stopDream(HWLOGW_E);
        this.startDreamFromUser = false;
        return HWLOGW_E;
    }

    public void init(Context context) {
        if (context != null) {
            this.mC2DMHandlerThread = new HandlerThread("C2DMHandlerThread");
            this.mC2DMHandlerThread.start();
            this.mC2DMHelperHandler = new C2DMHelperHandler(this.mC2DMHandlerThread.getLooper());
            PowerManager pm = (PowerManager) context.getSystemService("power");
            if (pm != null) {
                this.mC2DMWakeLock = pm.newWakeLock(1, "C2DMHandlerThread");
            }
            this.C2DM_DELAY_TIMEOUT = SystemProperties.getInt("ro.config.c2dmdelay", 10000);
            if (HWDBG) {
                Log.d(TAG, "c2dm delay time " + this.C2DM_DELAY_TIMEOUT + "ms");
            }
        }
    }

    public boolean isDelayEnanbled() {
        return this.mEnableC2DMDelay;
    }

    public void checkDelay(String tagName) {
        if ("google_c2dm".compareToIgnoreCase(tagName) == 0) {
            if (this.mC2DMWakeLock == null) {
                Log.e(TAG, "mC2DMWakeLock = null, mC2DMWakeLock is null!");
            } else if (this.mC2DMHelperHandler == null) {
                Log.e(TAG, "mC2DMHelperHandler = null, init failed!");
            } else {
                this.mC2DMHelperHandler.sendMessage(this.mC2DMHelperHandler.obtainMessage(1));
            }
        }
    }
}
