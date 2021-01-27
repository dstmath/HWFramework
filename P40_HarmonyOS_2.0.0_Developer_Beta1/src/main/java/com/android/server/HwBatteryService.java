package com.android.server;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.drawable.Icon;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Flog;
import com.android.server.gesture.GestureNavConst;
import com.android.server.lights.LightEx;
import com.android.server.lights.LightsManagerEx;
import com.android.server.power.HwAutoPowerOffController;
import com.huawei.android.app.NotificationManagerExt;
import com.huawei.android.app.PendingIntentEx;
import com.huawei.android.content.ContextEx;
import com.huawei.android.hidl.HealthInfoAdapter;
import com.huawei.android.os.FileUtilsEx;
import com.huawei.android.os.PowerManagerEx;
import com.huawei.android.os.ServiceManagerEx;
import com.huawei.android.os.SystemPropertiesEx;
import com.huawei.android.os.UEventObserverExt;
import com.huawei.android.os.UserHandleEx;
import com.huawei.android.util.SlogEx;
import com.huawei.hwpartbasicplatformservices.BuildConfig;
import com.huawei.server.SystemServiceEx;
import com.huawei.server.notification.BatteryPreheatNotification;
import com.huawei.utils.HwPartResourceUtils;
import huawei.cust.HwCustUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

public final class HwBatteryService extends BatteryServiceEx {
    private static final String ACTION_BATTERY_ISCD_ERROR = "huawei.intent.action.BATTERY_ISCD_ERROR";
    private static final String ACTION_CALL_BNT_CLICKED = "huawei.intent.action.CALL_BNT_CLICKED";
    private static final String ACTION_PERI_STATE_CHANGED = "com.huawei.nearby.peripheral.action.STATE_CHANGED";
    private static final String ACTION_QUICK_CHARGE = "huawei.intent.action.BATTERY_QUICK_CHARGE";
    private static final String ACTION_SMART_NOTIFY_FAULT = "huawei.intent.action.SMART_NOTIFY_FAULT";
    private static final String ACTION_VIEW_CHARGE_LINE = "huawei.intent.action.chargeline";
    private static final String ACTION_WIRELESS_CHARGE_POSITION = "huawei.intent.action.WIRELESS_CHARGE_POSITION";
    private static final String ACTION_WIRELESS_TX_CHARGE_ERROR = "huawei.intent.action.WIRELESS_TX_CHARGE_ERROR";
    private static final String ACTION_WIRELESS_TX_STATUS_CHANGE = "huawei.intent.action.WIRELESS_TX_STATUS_CHANGE";
    private static final String BATTERY_ERR_NOTIFICATION_ID = "n_id";
    private static final String BATTERY_ISCD_STATUS_ERROR = "1";
    private static final String BATTERY_ISCD_STATUS_NORMAL = "0";
    private static final String BATTERY_OVP_STATUS_ERROR = "1";
    private static final String BATTERY_OVP_STATUS_NORMAL = "0";
    private static final int CHARGE_TIME_BIT = 24;
    private static final int CHARGE_TIME_MASK = 16777215;
    private static final int CHARGE_TIME_MAX_HOURS = 43200;
    private static final int CHARGE_TIME_VERIFY = 85;
    private static final String DEFAULT_CHARGE_TIME_REAMINING = "-1";
    private static final String DEFAULT_LANGUAGE_CH = "zh_CN_#Hans";
    private static final String FACTORY_VERSION = "factory";
    private static final String HISI_PLATFORM_SUPPLY_NAME = "Battery";
    private static final String HUAWEI_CHINA_CUSTOMER_SERVICE_HOTLINE_NUMBER = "950800";
    private static final boolean IS_CHINA_REGION = SystemPropertiesEx.get("ro.config.hw_optb", "0").equals("156");
    private static final boolean IS_HWBATTERY_THREAD_DISABLED = SystemPropertiesEx.getBoolean("ro.config.hwbatterythread.disable", false);
    private static final boolean IS_ISCD_CHECK = SystemPropertiesEx.getBoolean("ro.config.check_battery_error", true);
    private static final boolean IS_LED_CLOSE_BY_CAMERA = SystemPropertiesEx.getBoolean("ro.config.led_close_by_camera", false);
    private static final boolean IS_SCREEN_ON_TURN_OFF_LED = SystemPropertiesEx.getBoolean("ro.config.screenon_turnoff_led", false);
    private static final boolean IS_TWO_COLOR_LIGHT = SystemPropertiesEx.getBoolean("ro.config.hw_two_color_light", false);
    private static final String LIGHT_STRAP_CASE_MODEL_ID_KEY = "RXID";
    private static final String LIGHT_STRAP_CASE_STATUS_KEY = "LIGHTSTRAPCASE";
    private static final String LIGHT_STRAP_CASE_STATUS_ON = "ON";
    private static final int LOW_BATTERY_SHUTDOWN_LEVEL = 2;
    private static final int LOW_BATTERY_WARNING_LEVEL = 4;
    private static final int MAX_BATTERY_PROP_REGISTER_TIMES = 5;
    private static final int MSG_BATTERY_ISCD_ERROR = 6;
    private static final int MSG_BATTERY_OVP_ERROR = 2;
    private static final int MSG_BATTERY_PROP_REGISTER = 1;
    private static final int MSG_CHECK_NON_STANDARD_CABLE = 10;
    private static final int MSG_RING_LIGHT_CASE = 8;
    private static final int MSG_UPDATE_QUICK_CHARGE_STATE = 3;
    private static final int MSG_WIRELWSS_CHARGE_NON_STANDARD = 9;
    private static final int MSG_WIRELWSS_CHARGE_POSITION = 7;
    private static final String MTK_PLATFORM_SUPPLY_NAME = "battery";
    private static final String NEARBY_PERMISSION = "com.huawei.permission.NEARBY";
    private static final String NON_STANDARD_CHARGE_LINE_NOTIFICATION_ID = "non_standard_charge_id";
    private static final int NO_HW_CHARGE_TIME_COMPUTE = -1;
    private static final String OVP_ERR_CHANNEL_ID = "battery_error_c_id";
    private static final String PERMISSION_HW_BATTERY_CHANGE = "com.huawei.permission.BATTERY_CHARGE";
    private static final String PERMISSION_TX_CHARGE_ERROR = "com.huawei.batteryservice.permission.WIRELESS_TX_CHARGE_ERROR";
    private static final String PERMISSION_TX_STATUS_CHANGE = "com.huawei.batteryservice.permission.WIRELESS_TX_STATUS_CHANGE";
    private static final int PLUGGED_NONE = 0;
    private static final String PPWER_CONNECTED_RINGTONE = "PowerConnected.ogg";
    private static final String QCOM_PLATFORM_SUPPLY_NAME = "usb";
    private static final String QUICK_CHARGE_STATUS_PATH = "sys/class/hw_power/power_ui/icon_type";
    private static final String RUN_MODE_PROPERTY = "ro.runmode";
    private static final int SHUTDOWN_LEVEL_FLASHINGARGB = -1;
    private static final String SMART_FAULT_CLASS_NAME = "com.huawei.hwdetectrepair.smartnotify.eventlistener.InstantMessageReceiver";
    private static final String SMART_FAULT_PACKAGE_NAME = "com.huawei.hwdetectrepair";
    private static final String SMART_FAULT_PERMISSION = "huawei.permission.SMART_NOTIFY_FAULT";
    private static final String SUPER_CHARGE_LINE_NODE = "sys/class/hw_power/power_ui/cable_type";
    private static final String TAG = "HwBatteryService";
    private static final int VALID_FLASH_RING_RX_ID = 7;
    private static final String WIRELESS_TX_DIR = "sys/class/hw_power/charger/wireless_tx";
    private static final int WIRELESS_TX_END = 0;
    private static final int WIRELESS_TX_ERROR = -1;
    private static final int WIRELESS_TX_FLAG_CLOSE = 2;
    private static final int WIRELESS_TX_FLAG_OPEN = 1;
    private static final int WIRELESS_TX_FLAG_UNKNOWN = 0;
    private static final String WIRELESS_TX_LOW_BATTERY = "20%";
    private static final String WIRELESS_TX_OPEN = "sys/class/hw_power/charger/wireless_tx/tx_open";
    private static final int WIRELESS_TX_START = 1;
    private static final String WIRELESS_TX_STATUS = "sys/class/hw_power/charger/wireless_tx/tx_status";
    private static final int WIRELESS_TX_STATUS_WAIT_TIME = 100;
    private static final int WIRELESS_TX_SWITCH_CLOSE = 0;
    private static final int WIRELESS_TX_SWITCH_OPEN = 1;
    private static final String WIRELSSS_CONNECTED_RINGTONE = "WirelessPowerConnected.ogg";
    private static final int WL_TX_STATUS_CHARGE_DONE = 4;
    private static final int WL_TX_STATUS_DEFAULT = 0;
    private static final int WL_TX_STATUS_FAULT_BASE = 16;
    private static final int WL_TX_STATUS_IN_CHARGING = 3;
    private static final int WL_TX_STATUS_IN_WL_CHARGING = 22;
    private static final int WL_TX_STATUS_PING = 1;
    private static final int WL_TX_STATUS_PING_SUCC = 2;
    private static final int WL_TX_STATUS_PING_TIMEOUT = 19;
    private static final int WL_TX_STATUS_RX_DISCONNECT = 18;
    private static final int WL_TX_STATUS_SOC_ERROR = 23;
    private static final int WL_TX_STATUS_TBATT_HIGH = 21;
    private static final int WL_TX_STATUS_TBATT_LOW = 20;
    private static final int WL_TX_STATUS_TX_CLOSE = 17;
    private AudioAttributes mAudioAttributes;
    private final UEventObserverExt mBatteryFcpObserver = new UEventObserverExt() {
        /* class com.android.server.HwBatteryService.AnonymousClass1 */

        public void onUEvent(UEventObserverExt.UEvent event) {
            HwBatteryService.this.handlerQuickChargeStatus();
            HwBatteryService.this.handleBatteryOvpStatus(event.get("POWER_SUPPLY_BAT_OVP", "0"));
            HwBatteryService.this.handleWirelessTxStatus();
            HwBatteryService.this.handerChargeTimeRemaining(event.get("POWER_SUPPLY_CHARGE_TIME_REMAINING", HwBatteryService.DEFAULT_CHARGE_TIME_REAMINING));
            HwBatteryService.this.handleWirelessChangerPosition();
        }
    };
    private final UEventObserverExt mBatteryIscdObserver = new UEventObserverExt() {
        /* class com.android.server.HwBatteryService.AnonymousClass2 */

        public void onUEvent(UEventObserverExt.UEvent event) {
            SlogEx.d(HwBatteryService.TAG, "onUEvent battery iscd error");
            HwBatteryService.this.mBatteryIscdStatus = "1";
            HwBatteryService.this.handleBatteryIscdStatus();
        }
    };
    private String mBatteryIscdStatus = "0";
    private String mBatteryOvpStatus = "0";
    private BatteryPreheatNotification mBatteryPreheatNotification;
    private int mBatteryPropRegisterTryTimes = 0;
    private final Context mContext;
    private HwCustBatteryService mCust = ((HwCustBatteryService) HwCustUtils.createObj(HwCustBatteryService.class, new Object[0]));
    private String mFaultDescripion = "0";
    private String mFautSuggestion = "0";
    private int mFlashingARGB;
    private HwAutoPowerOffController mHwAutoPowerOffController;
    private HwBatteryHandler mHwBatteryHandler;
    private HandlerThread mHwBatteryThread;
    private HwLed mHwLed;
    private boolean mIsBootFinish = false;
    private boolean mIsFlagScreenOn = true;
    private boolean mIsFrontCameraOpening = false;
    private boolean mIsHwChargeTimeValid = false;
    private boolean mIsLowBattery = false;
    private boolean mIsNonStandardChargeLine = false;
    private boolean mIsNotificationExisting;
    private boolean mIsRingLightCasePluged = false;
    private boolean mIsSystemReady = false;
    private boolean mIsWirelessChargeCancel = false;
    private boolean mIsWirelessChargeNoStandard = false;
    private int mLastWirelessTxStatus = 0;
    private final Object mLock = new Object();
    private int mNotificationLedOff;
    private int mNotificationLedOn;
    private NotificationManager mNotificationManager;
    private PowerManager mPowerManager;
    private final UEventObserverExt mPowerSupplyObserver = new UEventObserverExt() {
        /* class com.android.server.HwBatteryService.AnonymousClass3 */

        public void onUEvent(UEventObserverExt.UEvent event) {
            SlogEx.d(HwBatteryService.TAG, "mPowerSupplyObserver,event:" + event);
            if (event == null) {
                SlogEx.e(HwBatteryService.TAG, "mPowerSupplyObserver,event is null!");
                return;
            }
            if (HwBatteryService.this.mBatteryPreheatNotification == null) {
                HwBatteryService hwBatteryService = HwBatteryService.this;
                hwBatteryService.mBatteryPreheatNotification = new BatteryPreheatNotification(hwBatteryService.mContext);
            }
            if ("1".equals(event.get("UI_HEATING_STATUS"))) {
                HwBatteryService.this.mBatteryPreheatNotification.notification();
            } else if ("0".equals(event.get("UI_HEATING_STATUS"))) {
                HwBatteryService.this.mBatteryPreheatNotification.cancelNotification();
            } else {
                SlogEx.d(HwBatteryService.TAG, "mPowerSupplyObserver,UI_HEATING_STATUS is not 1 neither 0");
            }
            String cableType = event.get("UI_CABLE_TYPE");
            if (cableType != null) {
                SlogEx.i(HwBatteryService.TAG, "mBatteryFcpObserver UI_CABLE_TYPE= " + cableType);
            }
            if ("1".equals(cableType)) {
                HwBatteryService.this.mIsNonStandardChargeLine = true;
                HwBatteryService.this.handleNonStandardChargeLine();
            }
        }
    };
    private String mQuickChargeStatus = "0";
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.android.server.HwBatteryService.AnonymousClass6 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if (context == null || intent == null) {
                SlogEx.i(HwBatteryService.TAG, "context or intent is null!");
                return;
            }
            SlogEx.i(HwBatteryService.TAG, "intent = " + intent);
            if (HwBatteryService.ACTION_CALL_BNT_CLICKED.equals(intent.getAction())) {
                HwBatteryService.this.closeSystemDialogs(context);
                int notificationID = intent.getIntExtra(HwBatteryService.BATTERY_ERR_NOTIFICATION_ID, 0);
                if (HwBatteryService.this.mNotificationManager != null) {
                    NotificationManagerExt.cancelAsUser(HwBatteryService.this.mNotificationManager, (String) null, notificationID, UserHandleEx.CURRENT);
                }
                Intent callIntent = new Intent("android.intent.action.CALL");
                if (HwBatteryService.IS_CHINA_REGION) {
                    callIntent.setData(Uri.parse("tel:950800"));
                }
                callIntent.setFlags(276824064);
                ContextEx.startActivityAsUser(context, callIntent, (Bundle) null, UserHandleEx.CURRENT);
            } else if ("android.intent.action.LOCKED_BOOT_COMPLETED".equals(intent.getAction())) {
                HwBatteryService.this.mIsBootFinish = true;
                if (HwBatteryService.IS_ISCD_CHECK && !HwBatteryService.this.mBatteryIscdStatus.equals("0")) {
                    HwBatteryService.this.handleBatteryIscdStatus();
                }
            } else if ("android.intent.action.SCREEN_ON".equals(intent.getAction())) {
                HwBatteryService.this.mIsFlagScreenOn = true;
                if (HwBatteryService.IS_SCREEN_ON_TURN_OFF_LED) {
                    HwBatteryService.this.mHwLed.newUpdateLightsLocked();
                }
            } else if ("android.intent.action.SCREEN_OFF".equals(intent.getAction())) {
                HwBatteryService.this.mIsFlagScreenOn = false;
                if (HwBatteryService.IS_SCREEN_ON_TURN_OFF_LED) {
                    HwBatteryService.this.mHwLed.newUpdateLightsLocked();
                }
            } else if (HwBatteryService.ACTION_VIEW_CHARGE_LINE.equals(intent.getAction())) {
                HwBatteryService.this.closeSystemDialogs(context);
                HwBatteryService.this.cancelChargeLineNotification(HwPartResourceUtils.getResourceId("non_standard_charge_line_title"));
                Intent webIntent = new Intent("android.intent.action.VIEW");
                webIntent.setData(Uri.parse(HwBatteryService.this.getPlayCardHtmlAddress()));
                webIntent.setFlags(276824576);
                try {
                    ContextEx.startActivityAsUser(context, webIntent, (Bundle) null, UserHandleEx.CURRENT);
                } catch (ActivityNotFoundException e) {
                    SlogEx.e(HwBatteryService.TAG, "start activity fail");
                }
            }
        }
    };
    private final UEventObserverExt mRingLightObserver = new UEventObserverExt() {
        /* class com.android.server.HwBatteryService.AnonymousClass4 */

        public void onUEvent(UEventObserverExt.UEvent event) {
            String status = event.get(HwBatteryService.LIGHT_STRAP_CASE_STATUS_KEY);
            SlogEx.i(HwBatteryService.TAG, "Ring Light case uevent, status = " + status);
            if (HwBatteryService.this.mHwBatteryHandler == null) {
                SlogEx.i(HwBatteryService.TAG, "mHwBatteryHandler is null");
            } else if (HwBatteryService.LIGHT_STRAP_CASE_STATUS_ON.equals(status) && !HwBatteryService.this.mIsRingLightCasePluged) {
                HwBatteryService.this.mIsRingLightCasePluged = true;
                HwBatteryService.this.mHwBatteryHandler.updateRingLightCaseState(HwBatteryService.LIGHT_STRAP_CASE_STATUS_ON);
                HwBatteryService.this.checkLightStrapCaseModelId(event.get(HwBatteryService.LIGHT_STRAP_CASE_MODEL_ID_KEY));
            } else if (!HwBatteryService.LIGHT_STRAP_CASE_STATUS_ON.equals(status) && HwBatteryService.this.mIsRingLightCasePluged) {
                HwBatteryService.this.mIsRingLightCasePluged = false;
                HwBatteryService.this.mHwBatteryHandler.updateRingLightCaseState("OFF");
            }
        }
    };
    private Ringtone mRingRingtone;
    private Uri mUri;
    private final UEventObserverExt mWirelessChargeNonStandardObserver = new UEventObserverExt() {
        /* class com.android.server.HwBatteryService.AnonymousClass5 */

        public void onUEvent(UEventObserverExt.UEvent event) {
            if (event == null) {
                SlogEx.e(HwBatteryService.TAG, "wireless charge non standard UI_ACC_DET_STATUS event is null!");
                return;
            }
            HwBatteryService.this.mIsWirelessChargeNoStandard = true;
            HwBatteryService.this.mIsWirelessChargeCancel = false;
            String state = event.get("UI_ACC_DET_STATUS");
            SlogEx.i(HwBatteryService.TAG, "wireless charge non standard UI_ACC_DET_STATUS, state: " + state);
            HwBatteryService.this.switchStatusToFaultId(state);
            if (HwBatteryService.this.mHwBatteryHandler != null) {
                HwBatteryService.this.mHwBatteryHandler.updateWirelessChargeNonStandardState();
            }
        }
    };
    private int mWirelessTxFlag = 0;

    static /* synthetic */ int access$1708(HwBatteryService x0) {
        int i = x0.mBatteryPropRegisterTryTimes;
        x0.mBatteryPropRegisterTryTimes = i + 1;
        return i;
    }

    public HwBatteryService(Context context) {
        super(context);
        this.mContext = context;
        initAndRegisterReceiver();
        if (IS_HWBATTERY_THREAD_DISABLED) {
            SlogEx.w(TAG, "HwBatteryService thread is disabled.");
        } else {
            this.mHwBatteryThread = new HandlerThread(TAG);
            this.mHwBatteryThread.start();
            this.mHwBatteryHandler = new HwBatteryHandler(this.mHwBatteryThread.getLooper());
        }
        this.mHwLed = new HwLed(context, new LightsManagerEx());
        this.mAudioAttributes = new AudioAttributes.Builder().setUsage(13).setContentType(4).build();
        if (this.mCust.isAutoPowerOffOn()) {
            this.mHwAutoPowerOffController = new HwAutoPowerOffController(context);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void switchStatusToFaultId(String state) {
        if (state == null) {
            SlogEx.e(TAG, "wireless charge non standard state is null, return!");
            return;
        }
        char c = 65535;
        switch (state.hashCode()) {
            case 48:
                if (state.equals("0")) {
                    c = 0;
                    break;
                }
                break;
            case 49:
                if (state.equals("1")) {
                    c = 1;
                    break;
                }
                break;
            case 50:
                if (state.equals("2")) {
                    c = 2;
                    break;
                }
                break;
            case 51:
                if (state.equals("3")) {
                    c = 3;
                    break;
                }
                break;
            case 52:
                if (state.equals("4")) {
                    c = 4;
                    break;
                }
                break;
            case 53:
                if (state.equals("5")) {
                    c = 5;
                    break;
                }
                break;
        }
        if (c == 0) {
            this.mIsWirelessChargeCancel = true;
        } else if (c == 1) {
            this.mFaultDescripion = "820001110";
            this.mFautSuggestion = "520001110";
        } else if (c == 2) {
            this.mFaultDescripion = "820001111";
            this.mFautSuggestion = "520001111";
        } else if (c == 3) {
            this.mFaultDescripion = "820001112";
            this.mFautSuggestion = "520001112";
        } else if (c == 4) {
            this.mFaultDescripion = "820001113";
            this.mFautSuggestion = "520001113";
        } else if (c == 5) {
            this.mFaultDescripion = "820001114";
            this.mFautSuggestion = "520001114";
        }
    }

    private boolean writeFile(String path, String data) {
        StringBuilder sb;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            fos.write(data.getBytes("UTF-8"));
            try {
                fos.close();
                return true;
            } catch (IOException e) {
                sb = new StringBuilder();
            }
            sb.append("closeFile ");
            sb.append(path);
            SlogEx.e(TAG, sb.toString());
            return false;
        } catch (IOException e2) {
            SlogEx.w(TAG, "writeFile " + path);
            if (fos == null) {
                return false;
            }
            try {
                fos.close();
                return false;
            } catch (IOException e3) {
                sb = new StringBuilder();
            }
        } catch (Throwable th) {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e4) {
                    SlogEx.e(TAG, "closeFile " + path);
                }
            }
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public int alterWirelessTxSwitchInternal(int status) {
        SlogEx.i(TAG, "alterWirelessTxStatus status : " + status);
        synchronized (this.mLock) {
            this.mWirelessTxFlag = 0;
            this.mLastWirelessTxStatus = 0;
            if (!writeFile(WIRELESS_TX_OPEN, String.valueOf(status))) {
                SlogEx.e(TAG, "writeFile error sys/class/hw_power/charger/wireless_tx/tx_open");
                return -1;
            }
            try {
                this.mLock.wait(100);
            } catch (InterruptedException e) {
                SlogEx.w(TAG, "Error occurs when sleep");
            }
            int wirelessTxStatus = getWirelessTxStatus(WIRELESS_TX_STATUS);
            if (!allowWirelessTxSwitch(status % 2, wirelessTxStatus)) {
                SlogEx.i(TAG, "not allowed to open tx wireless : " + wirelessTxStatus);
                writeFile(WIRELESS_TX_OPEN, String.valueOf(0));
                return wirelessTxStatus;
            }
            if (status % 2 == 1) {
                this.mWirelessTxFlag = 1;
            } else {
                sendWirelessTxStatusChangeBroadcast(0);
                this.mWirelessTxFlag = 2;
            }
            return 0;
        }
    }

    /* access modifiers changed from: protected */
    public int getWirelessTxSwitchInternal() {
        return getWirelessTxStatus(WIRELESS_TX_OPEN);
    }

    /* access modifiers changed from: protected */
    public boolean supportWirelessTxChargeInternal() {
        File wirelessTxDir = new File(WIRELESS_TX_DIR);
        SlogEx.i(TAG, "exists : " + wirelessTxDir.exists());
        return wirelessTxDir.exists() && wirelessTxDir.isDirectory();
    }

    private int getWirelessTxStatus(String wirelessTxPath) {
        int result = 0;
        String txStatus = null;
        try {
            txStatus = FileUtilsEx.readTextFile(new File(wirelessTxPath), 0, (String) null).trim();
            result = Integer.parseInt(txStatus);
        } catch (IOException e) {
            SlogEx.w(TAG, "Error occurs when read: " + wirelessTxPath);
        } catch (NumberFormatException e2) {
            SlogEx.e(TAG, "Error occurs when translate status : " + txStatus);
        }
        SlogEx.i(TAG, "getWirelessTxStatus , " + wirelessTxPath + " : " + result);
        return result;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void checkLightStrapCaseModelId(String modelIdStr) {
        int modelId = 0;
        try {
            modelId = Integer.parseInt(modelIdStr);
        } catch (NumberFormatException e) {
            SlogEx.e(TAG, "light strap case rx id number format wrong! rxIdStr = " + modelIdStr);
        }
        if (modelId != 7) {
            SlogEx.w(TAG, "light strap case on, but rxId is = " + modelId);
        }
    }

    private boolean isWirelessTxNormal(int status) {
        if (status == 2 || status == 3) {
            return true;
        }
        return false;
    }

    private boolean isWirelessTxError(int status) {
        if (!(status == 4 || status == WL_TX_STATUS_FAULT_BASE || status == WL_TX_STATUS_TX_CLOSE)) {
            switch (status) {
                case WL_TX_STATUS_PING_TIMEOUT /* 19 */:
                case 20:
                case WL_TX_STATUS_TBATT_HIGH /* 21 */:
                case WL_TX_STATUS_IN_WL_CHARGING /* 22 */:
                case WL_TX_STATUS_SOC_ERROR /* 23 */:
                    break;
                default:
                    return false;
            }
        }
        return true;
    }

    private boolean isWirelessTxDisconnect(int status) {
        return status == WL_TX_STATUS_RX_DISCONNECT || status == 1;
    }

    private int getWirelessTxErrorRes(int status) {
        this.mIsLowBattery = false;
        if (status == 4) {
            return HwPartResourceUtils.getResourceId("wireless_tx_error_full_device");
        }
        if (status != WL_TX_STATUS_SOC_ERROR) {
            switch (status) {
                case WL_TX_STATUS_PING_TIMEOUT /* 19 */:
                    return HwPartResourceUtils.getResourceId("wireless_tx_error_no_device");
                case 20:
                    return HwPartResourceUtils.getResourceId("wireless_tx_error_temperature_low");
                case WL_TX_STATUS_TBATT_HIGH /* 21 */:
                    return HwPartResourceUtils.getResourceId("wireless_tx_error_temperature_high");
                default:
                    return -1;
            }
        } else {
            int res = HwPartResourceUtils.getResourceId("wireless_tx_error_low_battery");
            this.mIsLowBattery = true;
            return res;
        }
    }

    private boolean allowWirelessTxSwitch(int switchStatus, int wirelessTxStatus) {
        if (switchStatus == 0) {
            return true;
        }
        switch (wirelessTxStatus) {
            case 20:
            case WL_TX_STATUS_TBATT_HIGH /* 21 */:
            case WL_TX_STATUS_IN_WL_CHARGING /* 22 */:
            case WL_TX_STATUS_SOC_ERROR /* 23 */:
                return false;
            default:
                return true;
        }
    }

    private String getWirelessChangerPositionPath() {
        return "/sys/class/hw_power/power_ui/wl_off_pos";
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleWirelessChangerPosition() {
        int offPositon = 0;
        try {
            offPositon = Integer.parseInt(FileUtilsEx.readTextFile(new File(getWirelessChangerPositionPath()), 0, (String) null).trim());
        } catch (IOException e) {
            SlogEx.w(TAG, "Error occurs when read");
        } catch (NumberFormatException e2) {
            SlogEx.e(TAG, "Error occurs when translate status : ");
        }
        if (offPositon == 1) {
            this.mHwBatteryHandler.removeMessages(7);
            this.mHwBatteryHandler.sendMessage(Message.obtain(this.mHwBatteryHandler, 7));
        }
    }

    private boolean isTablet() {
        return GestureNavConst.DEVICE_TYPE_TABLET.equals(SystemPropertiesEx.get("ro.build.characteristics", BuildConfig.FLAVOR));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleWirelessTxStatus() {
        int wirelessTxStatus;
        SlogEx.i(TAG, "handleWirelessTxStatus");
        synchronized (this.mLock) {
            if (this.mWirelessTxFlag != 1) {
                SlogEx.i(TAG, "switch not open, return, mWirelessTxFlag : " + this.mWirelessTxFlag);
                return;
            }
            wirelessTxStatus = getWirelessTxStatus(WIRELESS_TX_STATUS);
            if (wirelessTxStatus == this.mLastWirelessTxStatus) {
                SlogEx.i(TAG, "wireless_tx_status not changed, return, mLastWirelessTxStatus : " + this.mLastWirelessTxStatus);
                return;
            }
            this.mLastWirelessTxStatus = wirelessTxStatus;
        }
        if (isWirelessTxNormal(wirelessTxStatus)) {
            sendWirelessTxStatusChangeBroadcast(1);
        } else if (isWirelessTxError(wirelessTxStatus)) {
            sendWirelessTxChargeErrorBroadcast();
            sendWirelessTxErrorNotification(wirelessTxStatus);
        } else if (isWirelessTxDisconnect(wirelessTxStatus)) {
            sendWirelessTxStatusChangeBroadcast(0);
        } else {
            SlogEx.i(TAG, "default charger status don't exists.");
        }
    }

    private void sendWirelessTxStatusChangeBroadcast(int status) {
        Intent intent = new Intent(ACTION_WIRELESS_TX_STATUS_CHANGE);
        intent.addFlags(1073741824);
        intent.addFlags(536870912);
        intent.putExtra("status", status);
        SlogEx.i(TAG, "sendWirelessTxStatusChangeBroadcast status : " + status);
        this.mContext.sendBroadcastAsUser(intent, UserHandleEx.ALL, PERMISSION_TX_STATUS_CHANGE);
    }

    private void sendWirelessTxChargeErrorBroadcast() {
        Intent intent = new Intent(ACTION_WIRELESS_TX_CHARGE_ERROR);
        intent.addFlags(1073741824);
        intent.addFlags(536870912);
        SlogEx.i(TAG, "sendWirelessTxChargeErrorBroadcast");
        this.mContext.sendBroadcastAsUser(intent, UserHandleEx.ALL, PERMISSION_TX_CHARGE_ERROR);
    }

    private void sendWirelessTxErrorNotification(int status) {
        int messageId = getWirelessTxErrorRes(status);
        if (messageId != -1) {
            SlogEx.i(TAG, "sendWirelessTxErrorNotification");
            int iconId = HwPartResourceUtils.getResourceId("wireless_tx_status_error");
            String message = this.mContext.getResources().getString(messageId);
            if (this.mIsLowBattery) {
                message = String.format(Locale.ROOT, message, WIRELESS_TX_LOW_BATTERY);
            }
            Intent intent = new Intent();
            intent.setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.power.ui.HwPowerManagerActivity"));
            PendingIntent pi = PendingIntentEx.getActivityAsUser(this.mContext, 0, intent, 0, (Bundle) null, UserHandleEx.CURRENT);
            makeNotificationChannel(message);
            Notification notification = new Notification.Builder(this.mContext, OVP_ERR_CHANNEL_ID).setSmallIcon(iconId).setContentText(message).setContentIntent(pi).setVibrate(new long[0]).setPriority(2).setWhen(System.currentTimeMillis()).setShowWhen(true).setVisibility(1).setStyle(new Notification.BigTextStyle().bigText(message)).setAutoCancel(true).build();
            NotificationManager notificationManager = this.mNotificationManager;
            if (notificationManager != null) {
                NotificationManagerExt.notifyAsUser(notificationManager, (String) null, messageId, notification, UserHandleEx.ALL);
            }
        }
    }

    public void onStart() {
        new HealthdDeathRecipient(ServiceManagerEx.getService("batteryproperties"));
    }

    /* access modifiers changed from: private */
    public class HealthdDeathRecipient implements IBinder.DeathRecipient {
        private IBinder mCb;

        HealthdDeathRecipient(IBinder cb) {
            if (cb != null) {
                try {
                    SlogEx.i(HwBatteryService.TAG, "linkToDeath Healthd.");
                    cb.linkToDeath(this, 0);
                } catch (RemoteException e) {
                    SlogEx.w(HwBatteryService.TAG, "HealthdDeathRecipient() could not link to " + cb + " binder death");
                }
            }
            this.mCb = cb;
        }

        @Override // android.os.IBinder.DeathRecipient
        public void binderDied() {
            SlogEx.w(HwBatteryService.TAG, "Healthd died.");
            IBinder iBinder = this.mCb;
            if (iBinder != null) {
                iBinder.unlinkToDeath(this, 0);
            }
            HwBatteryService.this.mBatteryPropRegisterTryTimes = 0;
            if (HwBatteryService.this.mHwBatteryHandler == null) {
                SlogEx.w(HwBatteryService.TAG, "mHwBatteryHandler is null.");
            } else {
                HwBatteryService.this.mHwBatteryHandler.sendEmptyMessageDelayed(1, 2000);
            }
        }
    }

    /* access modifiers changed from: private */
    public final class HwBatteryHandler extends Handler {
        public HwBatteryHandler(Looper looper) {
            super(looper);
            if (HwBatteryService.IS_ISCD_CHECK) {
                HwBatteryService.this.mBatteryIscdObserver.startObserving("BATTERY_EVENT=FATAL_ISC");
            }
            try {
                if (HwBatteryService.IS_ISCD_CHECK) {
                    String iscdStatus = FileUtilsEx.readTextFile(new File(HwBatteryService.this.getBatteryIscdStatePath()), 0, (String) null).trim();
                    SlogEx.i(HwBatteryService.TAG, "iscdStatus: " + iscdStatus);
                }
            } catch (IOException e) {
                SlogEx.e(HwBatteryService.TAG, "Error get initialized state.");
            }
            HwBatteryService.this.mBatteryFcpObserver.startObserving("SUBSYSTEM=power_supply");
            HwBatteryService.this.mPowerSupplyObserver.startObserving("SUBSYSTEM=hw_power");
            HwBatteryService.this.mRingLightObserver.startObserving("LIGHTSTRAPCASE=");
            HwBatteryService.this.mWirelessChargeNonStandardObserver.startObserving("UI_ACC_DET_STATUS=");
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    handleBatteryPropertiesRegister();
                    return;
                case 2:
                    HwBatteryService.this.sendBatteryErrorNotification(33685926, 33685927, 33751168);
                    return;
                case 3:
                    if (msg.obj instanceof String) {
                        sendQuickChargeBroadcast((String) msg.obj);
                        return;
                    }
                    return;
                case 4:
                case 5:
                default:
                    return;
                case 6:
                    HwBatteryService.this.sendBatteryErrorNotification(HwPartResourceUtils.getResourceId("battery_iscd_error_notification_title"), HwPartResourceUtils.getResourceId("battery_iscd_error_notification_msg"), HwPartResourceUtils.getResourceId("battery_iscd_error_notification_icon"));
                    HwBatteryService.this.sendBatteryIsdcErrorBroadcast();
                    return;
                case 7:
                    sendWirelessChargePositionBroadcast();
                    if (HwBatteryService.this.mPowerManager == null) {
                        HwBatteryService hwBatteryService = HwBatteryService.this;
                        hwBatteryService.mPowerManager = (PowerManager) hwBatteryService.mContext.getSystemService("power");
                    }
                    PowerManagerEx.wakeUp(HwBatteryService.this.mPowerManager, SystemClock.uptimeMillis(), 3, "wirelessDock.wakeUp");
                    return;
                case 8:
                    if (msg.obj instanceof String) {
                        sendRingLightCaseBroadcast((String) msg.obj);
                        return;
                    }
                    return;
                case 9:
                    sendWirelessChargeNonStandardBroadcast();
                    return;
                case 10:
                    HwBatteryService.this.sendNonStandardChargeLineNotification();
                    return;
            }
        }

        public void updateQuickChargeState(String state) {
            if (state == null) {
                SlogEx.i(HwBatteryService.TAG, "Quick Charge State is null, return!");
                return;
            }
            removeMessages(3);
            Message msg = Message.obtain(this, 3);
            msg.obj = state;
            sendMessage(msg);
        }

        public void updateRingLightCaseState(String state) {
            if (state == null) {
                SlogEx.i(HwBatteryService.TAG, "Quick Charge State is null, return!");
            } else if (!HwBatteryService.this.mIsSystemReady) {
                SlogEx.i(HwBatteryService.TAG, "boot not completed, do not send smart-notify broadcast");
            } else {
                removeMessages(8);
                Message msg = Message.obtain(this, 8);
                msg.obj = state;
                sendMessage(msg);
            }
        }

        public void updateWirelessChargeNonStandardState() {
            if (!HwBatteryService.this.mIsSystemReady) {
                SlogEx.e(HwBatteryService.TAG, "boot not completed, do not send smart-notify broadcast");
                return;
            }
            removeMessages(9);
            sendMessage(Message.obtain(this, 9));
        }

        private void handleBatteryPropertiesRegister() {
            IBinder binder = ServiceManagerEx.checkService("batteryproperties");
            if (binder == null || !binder.isBinderAlive()) {
                HwBatteryService.access$1708(HwBatteryService.this);
                if (HwBatteryService.this.mBatteryPropRegisterTryTimes < 5) {
                    SlogEx.i(HwBatteryService.TAG, "Try to get batteryproperties service again.");
                    HwBatteryService.this.mHwBatteryHandler.sendEmptyMessageDelayed(1, 2000);
                    return;
                }
                SlogEx.e(HwBatteryService.TAG, "There is no connection between batteryservice and batteryproperties.");
                return;
            }
            HwBatteryService.this.mBatteryPropRegisterTryTimes = 0;
            new HealthdDeathRecipient(binder);
            HwBatteryService.this.registerHealthCallback();
        }

        private void sendWirelessChargePositionBroadcast() {
            SlogEx.i(HwBatteryService.TAG, "Send Wireless Charge Position Broadcast!");
            Intent positionIntent = new Intent(HwBatteryService.ACTION_WIRELESS_CHARGE_POSITION);
            positionIntent.addFlags(1073741824);
            positionIntent.addFlags(536870912);
            HwBatteryService.this.mContext.sendBroadcastAsUser(positionIntent, UserHandleEx.ALL, HwBatteryService.PERMISSION_HW_BATTERY_CHANGE);
        }

        private void sendQuickChargeBroadcast(String state) {
            Intent quickChargeIntent = new Intent(HwBatteryService.ACTION_QUICK_CHARGE);
            quickChargeIntent.addFlags(1073741824);
            quickChargeIntent.addFlags(536870912);
            quickChargeIntent.putExtra("quick_charge_status", state);
            SlogEx.i(HwBatteryService.TAG, "Stick broadcast intent: " + quickChargeIntent + " mQuickChargeStatus:" + state);
            HwBatteryService.this.mContext.sendStickyBroadcastAsUser(quickChargeIntent, UserHandleEx.ALL);
        }

        private void sendRingLightCaseBroadcast(String state) {
            boolean equals = HwBatteryService.LIGHT_STRAP_CASE_STATUS_ON.equals(state);
            Bundle bundle = new Bundle();
            bundle.putInt("peripheral.state", equals ? 1 : 0);
            bundle.putString("message.source", "lightcase.request");
            bundle.putByteArray("message.content", new byte[]{40, 1});
            Intent intent = new Intent(HwBatteryService.ACTION_PERI_STATE_CHANGED);
            intent.putExtra("message.bundle", bundle);
            intent.addFlags(HwBatteryService.WL_TX_STATUS_FAULT_BASE);
            HwBatteryService.this.mContext.sendBroadcastAsUser(intent, UserHandleEx.ALL, HwBatteryService.NEARBY_PERMISSION);
        }

        private void sendWirelessChargeNonStandardBroadcast() {
            if ("0".equals(HwBatteryService.this.mFaultDescripion) || "0".equals(HwBatteryService.this.mFautSuggestion)) {
                SlogEx.w(HwBatteryService.TAG, "FaultDescripion or FautSuggestion is null.");
                return;
            }
            Intent intent = new Intent(HwBatteryService.ACTION_SMART_NOTIFY_FAULT);
            intent.setClassName(HwBatteryService.SMART_FAULT_PACKAGE_NAME, HwBatteryService.SMART_FAULT_CLASS_NAME);
            intent.putExtra("FAULT_DESCRIPTION", HwBatteryService.this.mFaultDescripion);
            intent.putExtra("FAULT_SUGGESTION", HwBatteryService.this.mFautSuggestion);
            if (HwBatteryService.this.mIsWirelessChargeCancel) {
                SlogEx.i(HwBatteryService.TAG, "send cancel wireless charge non standard broadcast intent extra.");
                intent.putExtra("FAULT_STAT", 0);
            }
            SlogEx.i(HwBatteryService.TAG, "send wireless charge non standard broadcast intent: " + intent + " mFaultDescripion= " + HwBatteryService.this.mFaultDescripion + " mFautSuggestion= " + HwBatteryService.this.mFautSuggestion);
            HwBatteryService.this.mContext.sendBroadcastAsUser(intent, UserHandleEx.ALL, HwBatteryService.SMART_FAULT_PERMISSION);
        }
    }

    private String getBatteryOvpStatePath() {
        return "/sys/class/power_supply/Battery/bat_ovp";
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getBatteryIscdStatePath() {
        return "/sys/class/hw_power/battery/isc";
    }

    private void initAndRegisterReceiver() {
        this.mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_CALL_BNT_CLICKED);
        filter.addAction("android.intent.action.LOCKED_BOOT_COMPLETED");
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction(ACTION_VIEW_CHARGE_LINE);
        this.mContext.registerReceiver(this.mReceiver, filter, "android.permission.DEVICE_POWER", null);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void closeSystemDialogs(Context context) {
        context.sendBroadcastAsUser(new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS"), UserHandleEx.ALL);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String getPlayCardHtmlAddress() {
        return this.mContext.getResources().getString(HwPartResourceUtils.getResourceId("non_standard_charging_line_profile_url"));
    }

    private void makeNotificationChannel(String name) {
        if (this.mNotificationManager != null) {
            this.mNotificationManager.createNotificationChannel(new NotificationChannel(OVP_ERR_CHANNEL_ID, name, 4));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendBatteryErrorNotification(int titleId, int messageId, int iconId) {
        SlogEx.i(TAG, "sendBatteryErrorNotification");
        String title = this.mContext.getResources().getString(titleId);
        Intent dialIntent = new Intent("android.intent.action.DIAL");
        if (IS_CHINA_REGION) {
            dialIntent.setData(Uri.parse("tel:950800"));
        }
        PendingIntent pi = PendingIntentEx.getActivityAsUser(this.mContext, 0, dialIntent, 0, (Bundle) null, UserHandleEx.CURRENT);
        Intent intent = new Intent(ACTION_CALL_BNT_CLICKED);
        intent.putExtra(BATTERY_ERR_NOTIFICATION_ID, titleId);
        intent.setPackage(this.mContext.getPackageName());
        PendingIntent actionClickPI = PendingIntent.getBroadcast(this.mContext, 0, intent, 0);
        makeNotificationChannel(title);
        Notification.Builder build = new Notification.Builder(this.mContext, OVP_ERR_CHANNEL_ID).setSmallIcon(iconId).setOngoing(true).setContentTitle(title).setContentText(this.mContext.getResources().getString(messageId)).setContentIntent(pi).setTicker(title).setVibrate(new long[0]).setPriority(2).setWhen(System.currentTimeMillis()).setShowWhen(true).setVisibility(1).setAutoCancel(true);
        if (IS_CHINA_REGION) {
            build.addAction(new Notification.Action.Builder((Icon) null, this.mContext.getString(33685928), actionClickPI).build());
        }
        Notification notification = build.build();
        NotificationManager notificationManager = this.mNotificationManager;
        if (notificationManager != null) {
            NotificationManagerExt.notifyAsUser(notificationManager, (String) null, titleId, notification, UserHandleEx.ALL);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleBatteryOvpStatus(String status) {
        if (!this.mBatteryOvpStatus.equals(status) && "1".equals(status)) {
            SlogEx.i(TAG, "battery ovp error occur, send notification before.");
            this.mBatteryOvpStatus = status;
            HwBatteryHandler hwBatteryHandler = this.mHwBatteryHandler;
            if (hwBatteryHandler != null) {
                if (hwBatteryHandler.hasMessages(2)) {
                    this.mHwBatteryHandler.removeMessages(2);
                }
                this.mHwBatteryHandler.sendMessage(Message.obtain(this.mHwBatteryHandler, 2, status));
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleBatteryIscdStatus() {
        HwBatteryHandler hwBatteryHandler;
        if (this.mIsBootFinish && (hwBatteryHandler = this.mHwBatteryHandler) != null) {
            if (hwBatteryHandler.hasMessages(6)) {
                this.mHwBatteryHandler.removeMessages(6);
            }
            this.mHwBatteryHandler.sendMessage(Message.obtain(this.mHwBatteryHandler, 6));
        }
    }

    public void onBootPhase(int phase) {
        if (phase == SystemServiceEx.PHASE_BOOT_COMPLETED) {
            SlogEx.i(TAG, "system boot completed");
            this.mIsSystemReady = true;
            handlerQuickChargeStatus();
            HwBatteryHandler hwBatteryHandler = this.mHwBatteryHandler;
            if (hwBatteryHandler != null) {
                hwBatteryHandler.updateRingLightCaseState(this.mIsRingLightCasePluged ? LIGHT_STRAP_CASE_STATUS_ON : "OFF");
            }
            HwBatteryHandler hwBatteryHandler2 = this.mHwBatteryHandler;
            if (hwBatteryHandler2 != null && this.mIsWirelessChargeNoStandard) {
                hwBatteryHandler2.updateWirelessChargeNonStandardState();
            }
            if (this.mIsNonStandardChargeLine) {
                handleNonStandardChargeLine();
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleNonStandardChargeLine() {
        SlogEx.i(TAG, "handle non_standard charge line.");
        HwBatteryHandler hwBatteryHandler = this.mHwBatteryHandler;
        if (hwBatteryHandler != null) {
            if (hwBatteryHandler.hasMessages(10)) {
                this.mHwBatteryHandler.removeMessages(10);
            }
            this.mHwBatteryHandler.sendMessage(Message.obtain(this.mHwBatteryHandler, 10));
        }
    }

    /* access modifiers changed from: protected */
    public void cancelChargeLineNotification(int titleId) {
        SlogEx.d(TAG, "charge line pull out, try to cancel non_standard charge line notification");
        if (!this.mIsSystemReady) {
            SlogEx.e(TAG, "System boot is not ready, can not cancel notification");
            return;
        }
        NotificationManager notificationManager = this.mNotificationManager;
        if (notificationManager != null) {
            NotificationManagerExt.cancelAsUser(notificationManager, (String) null, titleId, UserHandleEx.CURRENT);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendNonStandardChargeLineNotification() {
        SlogEx.i(TAG, "start to send non_stantard charge line notification");
        if (!this.mIsSystemReady) {
            SlogEx.e(TAG, "System boot is not ready, can not send notification");
        } else if (this.mNotificationManager == null) {
            SlogEx.e(TAG, "mNotificationManager is null");
        } else {
            this.mNotificationManager.createNotificationChannel(new NotificationChannel(NON_STANDARD_CHARGE_LINE_NOTIFICATION_ID, this.mContext.getResources().getString(HwPartResourceUtils.getResourceId("battery_charge_notification_channel_name")), 3));
            PendingIntent viewPendingIntent = PendingIntent.getBroadcast(this.mContext, 0, new Intent(ACTION_VIEW_CHARGE_LINE), 0);
            int iconId = HwPartResourceUtils.getResourceId("ic_battery_non_standard_charge_line");
            int titleId = HwPartResourceUtils.getResourceId("non_standard_charge_line_title");
            String message = this.mContext.getResources().getString(HwPartResourceUtils.getResourceId("non_standard_charge_line_message"));
            String viewButtonMessage = this.mContext.getResources().getString(HwPartResourceUtils.getResourceId("non_standard_charge_line_button_text"));
            Notification.Builder builder = new Notification.Builder(this.mContext, NON_STANDARD_CHARGE_LINE_NOTIFICATION_ID).setSmallIcon(iconId).setContentText(message).setWhen(System.currentTimeMillis()).setShowWhen(true).setPriority(0).setVisibility(-1).setStyle(new Notification.BigTextStyle().bigText(message)).setAutoCancel(true);
            if (IS_CHINA_REGION && Locale.getDefault().toString().equals(DEFAULT_LANGUAGE_CH)) {
                builder.setContentIntent(viewPendingIntent).addAction(new Notification.Action.Builder((Icon) null, viewButtonMessage, viewPendingIntent).build());
            }
            NotificationManagerExt.notifyAsUser(this.mNotificationManager, (String) null, titleId, builder.build(), UserHandleEx.ALL);
        }
    }

    /* access modifiers changed from: private */
    public final class HwLed {
        private final int mBatteryFullARGB;
        private final int mBatteryLedOff;
        private final int mBatteryLedOn;
        private final LightEx mBatteryLight;
        private final int mBatteryLowARGB;
        private final int mBatteryMediumARGB;

        public HwLed(Context context, LightsManagerEx lights) {
            this.mBatteryLight = lights.getLight(LightsManagerEx.LIGHT_ID_BATTERY);
            this.mBatteryLowARGB = context.getResources().getInteger(HwPartResourceUtils.getResourceId("config_notificationsBatteryLowARGB"));
            if (HwBatteryService.IS_TWO_COLOR_LIGHT) {
                this.mBatteryMediumARGB = context.getResources().getInteger(HwPartResourceUtils.getResourceId("config_notificationsBatteryLowARGB"));
            } else {
                this.mBatteryMediumARGB = context.getResources().getInteger(HwPartResourceUtils.getResourceId("config_notificationsBatteryMediumARGB"));
            }
            this.mBatteryFullARGB = context.getResources().getInteger(HwPartResourceUtils.getResourceId("config_notificationsBatteryFullARGB"));
            this.mBatteryLedOn = context.getResources().getInteger(HwPartResourceUtils.getResourceId("config_notificationsBatteryLedOn"));
            this.mBatteryLedOff = context.getResources().getInteger(HwPartResourceUtils.getResourceId("config_notificationsBatteryLedOff"));
        }

        private void updatelightFlashingImpl(int flashRGB, int color, int level, boolean isNotifyExist, String lightColor) {
            if (HwBatteryService.this.mFlashingARGB == flashRGB) {
                Flog.i(1100, "current flashRGB no change, return.");
                return;
            }
            this.mBatteryLight.turnOff();
            Flog.i(1100, "updatelightFlashingImpl, level:" + level + ", Flashing: " + lightColor);
            if (isNotifyExist) {
                this.mBatteryLight.setFlashing(color, LightEx.LIGHT_FLASH_TIMED, HwBatteryService.this.mNotificationLedOn, HwBatteryService.this.mNotificationLedOff);
            } else {
                this.mBatteryLight.setFlashing(color, LightEx.LIGHT_FLASH_TIMED, this.mBatteryLedOn, this.mBatteryLedOff);
            }
            HwBatteryService.this.mFlashingARGB = flashRGB;
        }

        private void setLightColorImpl(int color, int level, String colorChar) {
            Flog.i(1100, "setLightColorImpl, level:" + level + ", Solid:" + colorChar);
            this.mBatteryLight.setColor(color);
            HwBatteryService.this.mFlashingARGB = 0;
        }

        private void updateLightNotificationExisted(int level, int status, boolean isSupportMedium) {
            if (level <= 4) {
                int i = this.mBatteryLowARGB;
                updatelightFlashingImpl(i, i, level, true, "red");
            } else if (status == 5 && (!HwBatteryService.this.mIsFlagScreenOn || isSupportMedium)) {
                int i2 = this.mBatteryFullARGB;
                updatelightFlashingImpl(i2, i2, level, true, "green");
            } else if (status != 2 || (HwBatteryService.this.mIsFlagScreenOn && !isSupportMedium)) {
                Flog.i(1100, "updateLightNotificationyExisted, mBatteryLight.turnOff");
                this.mBatteryLight.turnOff();
                HwBatteryService.this.mFlashingARGB = 0;
            } else if (level >= 90) {
                int i3 = this.mBatteryFullARGB;
                updatelightFlashingImpl(i3, i3, level, true, "green");
            } else if (!isSupportMedium || level < HwBatteryService.this.getLowBatteryWarningLevel()) {
                int i4 = this.mBatteryLowARGB;
                updatelightFlashingImpl(i4, i4, level, true, "red");
            } else {
                int i5 = this.mBatteryMediumARGB;
                updatelightFlashingImpl(i5, i5, level, true, "medium");
            }
        }

        private void updateLightNotificationNoExisted(int level, int status, boolean isSupportMedium) {
            if (level <= 4) {
                if (status == 2 || level > 2) {
                    setLightColorImpl(this.mBatteryLowARGB, level, "red");
                } else {
                    updatelightFlashingImpl(-1, this.mBatteryLowARGB, level, false, "red shutdown");
                }
            } else if (status == 5 && (!HwBatteryService.this.mIsFlagScreenOn || isSupportMedium)) {
                setLightColorImpl(this.mBatteryFullARGB, level, "Solid green");
            } else if (status != 2 || (HwBatteryService.this.mIsFlagScreenOn && !isSupportMedium)) {
                Flog.i(1100, "updateLightNotificationyNoExisted, mBatteryLight.turnOff");
                this.mBatteryLight.turnOff();
                HwBatteryService.this.mFlashingARGB = 0;
            } else if (level >= 90) {
                setLightColorImpl(this.mBatteryFullARGB, level, "Solid green");
            } else if (!isSupportMedium || level < HwBatteryService.this.getLowBatteryWarningLevel()) {
                setLightColorImpl(this.mBatteryLowARGB, level, "Solid red");
            } else {
                setLightColorImpl(this.mBatteryMediumARGB, level, "Solid medium");
            }
        }

        private void updateFlashColorLights(int level, int status, boolean isSupportMedium) {
            Flog.i(1100, "updateFlashColorLights, level:" + level + ", isMsgNotifyExist:" + HwBatteryService.this.mIsNotificationExisting + ", status:" + status + ", isSupportMedium:" + isSupportMedium);
            if (HwBatteryService.this.mIsNotificationExisting) {
                updateLightNotificationExisted(level, status, isSupportMedium);
            } else {
                updateLightNotificationNoExisted(level, status, isSupportMedium);
            }
        }

        public void newUpdateLightsLocked() {
            int level = HwBatteryService.this.getHealthInfoBatteryLevel();
            int status = HwBatteryService.this.getHealthInfoBatteryStatus();
            if (HwBatteryService.IS_LED_CLOSE_BY_CAMERA && HwBatteryService.this.mIsFrontCameraOpening && (status == 2 || status == 5)) {
                Flog.i(1100, "newUpdateLightsLocked, light turnOff when front camera open");
                this.mBatteryLight.turnOff();
                HwBatteryService.this.mFlashingARGB = 0;
            } else if (HwBatteryService.IS_SCREEN_ON_TURN_OFF_LED) {
                updateFlashColorLights(level, status, false);
            } else {
                updateFlashColorLights(level, status, true);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void updateLight() {
        this.mHwLed.newUpdateLightsLocked();
    }

    /* access modifiers changed from: protected */
    public void updateLight(boolean isEnable, int ledOnMS, int ledOffMS) {
        if (this.mIsNotificationExisting != isEnable || this.mNotificationLedOn != ledOnMS || this.mNotificationLedOff != ledOffMS) {
            Flog.i(1100, " updateLight --> mIsNotificationExisting : " + isEnable + " ledOnMS : " + ledOnMS + " ledOffMS : " + ledOffMS);
            this.mIsNotificationExisting = isEnable;
            this.mNotificationLedOn = ledOnMS;
            this.mNotificationLedOff = ledOffMS;
            this.mHwLed.newUpdateLightsLocked();
        }
    }

    /* access modifiers changed from: protected */
    public void cameraUpdateLight(boolean isEnable) {
        SlogEx.d(TAG, "cameraUpdateLight enable " + isEnable);
        if (this.mIsFrontCameraOpening != isEnable) {
            this.mIsFrontCameraOpening = isEnable;
            this.mHwLed.newUpdateLightsLocked();
        }
    }

    /* access modifiers changed from: protected */
    public void playRing() {
        HwBatteryHandler hwBatteryHandler;
        if (!FACTORY_VERSION.equalsIgnoreCase(SystemPropertiesEx.get(RUN_MODE_PROPERTY, "unknown"))) {
            HwCustBatteryService hwCustBatteryService = this.mCust;
            if ((hwCustBatteryService == null || !hwCustBatteryService.mutePowerConnectedTone()) && (hwBatteryHandler = this.mHwBatteryHandler) != null) {
                hwBatteryHandler.post(new Runnable() {
                    /* class com.android.server.HwBatteryService.AnonymousClass7 */

                    @Override // java.lang.Runnable
                    public void run() {
                        boolean isWireless = true;
                        if (HwBatteryService.this.getPlugType() != 1) {
                            isWireless = false;
                        }
                        String fileName = isWireless ? HwBatteryService.WIRELSSS_CONNECTED_RINGTONE : HwBatteryService.PPWER_CONNECTED_RINGTONE;
                        HwBatteryService hwBatteryService = HwBatteryService.this;
                        hwBatteryService.mUri = HwBatteryService.queryRingMusicUri(hwBatteryService.mContext, fileName);
                        HwBatteryService hwBatteryService2 = HwBatteryService.this;
                        hwBatteryService2.mRingRingtone = hwBatteryService2.playRing(hwBatteryService2.mUri, HwBatteryService.this.mRingRingtone);
                    }
                });
            }
        }
    }

    /* access modifiers changed from: protected */
    public void stopRing() {
        HwBatteryHandler hwBatteryHandler;
        if (!FACTORY_VERSION.equalsIgnoreCase(SystemPropertiesEx.get(RUN_MODE_PROPERTY, "unknown")) && (hwBatteryHandler = this.mHwBatteryHandler) != null) {
            hwBatteryHandler.post(new Runnable() {
                /* class com.android.server.HwBatteryService.AnonymousClass8 */

                @Override // java.lang.Runnable
                public void run() {
                    HwBatteryService hwBatteryService = HwBatteryService.this;
                    hwBatteryService.stopRing(hwBatteryService.mRingRingtone);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Ringtone playRing(Uri uri, Ringtone ringtone) {
        if (uri == null) {
            return null;
        }
        Ringtone ringtone2 = RingtoneManager.getRingtone(this.mContext, uri);
        if (ringtone2 != null) {
            ringtone2.setAudioAttributes(this.mAudioAttributes);
            ringtone2.play();
        }
        return ringtone2;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void stopRing(Ringtone ringtone) {
        if (ringtone != null) {
            ringtone.stop();
        }
    }

    /* access modifiers changed from: private */
    public static Uri queryRingMusicUri(Context context, String fileName) {
        return queryRingMusicUri(context.getContentResolver(), fileName);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0044, code lost:
        if (r10 != null) goto L_0x0046;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0046, code lost:
        r10.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0055, code lost:
        if (0 == 0) goto L_0x0058;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0058, code lost:
        return null;
     */
    private static Uri queryRingMusicUri(ContentResolver resolver, String fileName) {
        if (fileName == null) {
            return null;
        }
        Uri uri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
        String[] cols = {"_id"};
        Cursor cur = null;
        try {
            cur = resolver.query(uri, cols, "_data like '%" + fileName + "'", null, null);
            if (cur != null && cur.moveToFirst()) {
                Uri withAppendedId = ContentUris.withAppendedId(uri, (long) cur.getInt(cur.getColumnIndex("_id")));
                cur.close();
                return withAppendedId;
            }
        } catch (Exception e) {
            Flog.i(605, "queryRingMusicUri Uri error!");
        } catch (Throwable th) {
            if (0 != 0) {
                cur.close();
            }
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public void printBatteryLog(HealthInfoAdapter oldInfo, HealthInfoAdapter newInfo, int oldPlugType, boolean isUpdatesStopped) {
        int plugType;
        if (oldInfo == null || newInfo == null) {
            Flog.i(605, "mBatteryProps or new battery values is null");
            return;
        }
        if (newInfo.getChargerAcOnline()) {
            plugType = 1;
        } else if (newInfo.getChargerUsbOnline()) {
            plugType = 2;
        } else if (newInfo.getChargerWirelessOnline()) {
            plugType = 4;
        } else {
            plugType = 0;
        }
        if (plugType != oldPlugType || oldInfo.getBatteryLevel() != newInfo.getBatteryLevel()) {
            Flog.i(605, "update battery new values: chargerAcOnline=" + newInfo.getChargerAcOnline() + ", chargerUsbOnline=" + newInfo.getChargerUsbOnline() + ", batteryStatus=" + newInfo.getBatteryStatus() + ", batteryHealth=" + newInfo.getBatteryHealth() + ", batteryPresent=" + newInfo.getBatteryPresent() + ", batteryLevel=" + newInfo.getBatteryLevel() + ", batteryTechnology=" + newInfo.getBatteryTechnology() + ", batteryVoltage=" + newInfo.getBatteryVoltage() + ", batteryTemperature=" + newInfo.getBatteryTemperature() + ", mUpdatesStopped=" + isUpdatesStopped);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendBatteryIsdcErrorBroadcast() {
        Intent iscdErrorIntent = new Intent(ACTION_BATTERY_ISCD_ERROR);
        iscdErrorIntent.addFlags(1073741824);
        iscdErrorIntent.addFlags(536870912);
        SlogEx.i(TAG, "Stick broadcast intent: " + iscdErrorIntent);
        this.mContext.sendStickyBroadcastAsUser(iscdErrorIntent, UserHandleEx.ALL);
    }

    /* access modifiers changed from: protected */
    public void startAutoPowerOff() {
        if (this.mHwAutoPowerOffController != null) {
            SlogEx.d(TAG, "startAutoPowerOff()");
            this.mHwAutoPowerOffController.startAutoPowerOff();
        }
    }

    /* access modifiers changed from: protected */
    public void stopAutoPowerOff() {
        if (this.mHwAutoPowerOffController != null) {
            SlogEx.d(TAG, "stopAutoPowerOff");
            this.mHwAutoPowerOffController.stopAutoPowerOff();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handerChargeTimeRemaining(String time) {
        int tempTime;
        int tempTime2;
        boolean tempTimeValid = false;
        if (getPlugType() != 0) {
            try {
                tempTime = Integer.parseInt(time);
            } catch (NumberFormatException e) {
                tempTime = -1;
                SlogEx.w(TAG, "Not int number, Invalid value: -1");
            }
            if (checkChanrgeTimeValue(tempTime)) {
                tempTime2 = tempTime & CHARGE_TIME_MASK;
                tempTimeValid = true;
            } else {
                tempTime2 = -1;
            }
            if (this.mIsHwChargeTimeValid != tempTimeValid) {
                this.mIsHwChargeTimeValid = tempTimeValid;
                String timeValid = tempTimeValid ? "1" : "0";
                SystemPropertiesEx.set("persist.sys.hwChargeTime", timeValid);
                SlogEx.d(TAG, "SystemPropertiesEx.set " + timeValid);
            }
            SlogEx.d(TAG, "setHwChargeTimeRemaining, time = " + tempTime2 + " String time: " + time);
            setHwChargeTimeRemaining(tempTime2);
        }
    }

    private boolean checkChanrgeTimeValue(int time) {
        if ((time >> CHARGE_TIME_BIT) != CHARGE_TIME_VERIFY) {
            SlogEx.w(TAG, "need return. Invalid value: " + time);
            return false;
        } else if ((CHARGE_TIME_MASK & time) <= CHARGE_TIME_MAX_HOURS) {
            return true;
        } else {
            SlogEx.w(TAG, "Need return. bigger then 12 hours, Invalid value: " + time);
            return false;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlerQuickChargeStatus() {
        try {
            String state = FileUtilsEx.readTextFile(new File(QUICK_CHARGE_STATUS_PATH), 0, (String) null).trim();
            SlogEx.i(TAG, "power_ui state= " + state + " mQuickChargeStatus=" + this.mQuickChargeStatus);
            if (!this.mIsSystemReady) {
                SlogEx.i(TAG, "boot not completed, do not send smart-notify broadcast");
            } else if (state != null && !this.mQuickChargeStatus.equals(state) && this.mHwBatteryHandler != null) {
                this.mQuickChargeStatus = state;
                this.mHwBatteryHandler.updateQuickChargeState(state);
            }
        } catch (IOException e) {
            SlogEx.e(TAG, "read UI_ICON_TYPE error, return");
        }
    }
}
