package com.android.server;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.hardware.health.V1_0.HealthInfo;
import android.media.AudioAttributes;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.FileUtils;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UEventObserver;
import android.os.UserHandle;
import android.provider.MediaStore;
import android.util.Flog;
import android.util.Slog;
import com.android.server.lights.Light;
import com.android.server.lights.LightsManager;
import com.android.server.power.HwAutoPowerOffController;
import com.android.server.wifipro.WifiProCommonUtils;
import com.android.systemui.shared.recents.hwutil.HwRecentsTaskUtils;
import com.huawei.msdp.devicestatus.DeviceStatusConstant;
import huawei.cust.HwCustUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public final class HwBatteryService extends BatteryService {
    private static final String ACTION_BATTERY_ISCD_ERROR = "huawei.intent.action.BATTERY_ISCD_ERROR";
    private static final String ACTION_CALL_BNT_CLICKED = "huawei.intent.action.CALL_BNT_CLICKED";
    private static final String ACTION_QUICK_CHARGE = "huawei.intent.action.BATTERY_QUICK_CHARGE";
    private static final String ACTION_WIRELESS_TX_CHARGE_ERROR = "huawei.intent.action.WIRELESS_TX_CHARGE_ERROR";
    private static final String ACTION_WIRELESS_TX_STATUS_CHANGE = "huawei.intent.action.WIRELESS_TX_STATUS_CHANGE";
    private static final String BATTERY_ERR_NOTIFICATION_ID = "n_id";
    private static final String BATTERY_ISCD_STATUS_ERROR = "1";
    private static final String BATTERY_ISCD_STATUS_NORMAL = "0";
    private static final String BATTERY_OVP_STATUS_ERROR = "1";
    private static final String BATTERY_OVP_STATUS_NORMAL = "0";
    private static final String FACTORY_VERSION = "factory";
    private static final String HISI_PLATFORM_SUPPLY_NAME = "Battery";
    private static final String HUAWEI_CHINA_CUSTOMER_SERVICE_HOTLINE_NUMBER = "4008308300";
    public static final int LOW_BATTERY_SHUTDOWN_LEVEL = 2;
    public static final int LOW_BATTERY_WARNING_LEVEL = 4;
    private static final int MAX_BATTERY_PROP_REGISTER_TIMES = 5;
    private static final int MSG_BATTERY_ISCD_ERROR = 6;
    private static final int MSG_BATTERY_OVP_ERROR = 2;
    private static final int MSG_BATTERY_PROP_REGISTER = 1;
    private static final int MSG_UPDATE_QUICK_CHARGE_STATE = 3;
    private static final String MTK_PLATFORM_SUPPLY_NAME = "battery";
    private static final String OVP_ERR_CHANNEL_ID = "battery_error_c_id";
    private static final String PERMISSION_TX_CHARGE_ERROR = "com.huawei.batteryservice.permission.WIRELESS_TX_CHARGE_ERROR";
    private static final String PERMISSION_TX_STATUS_CHANGE = "com.huawei.batteryservice.permission.WIRELESS_TX_STATUS_CHANGE";
    private static final int PLUGGED_NONE = 0;
    private static final String PPWER_CONNECTED_RINGTONE = "PowerConnected.ogg";
    private static final String QCOM_PLATFORM_SUPPLY_NAME = "usb";
    private static final String QUICK_CHARGE_FCP_STATUS = "1";
    private static final String QUICK_CHARGE_NODE_NOT_EXIST = "0";
    private static final String QUICK_CHARGE_NONE_STATUS = "0";
    private static final String QUICK_CHARGE_SCP_STATUS = "2";
    private static final String QUICK_CHARGE_STATUS_NORMAL = "0";
    private static final String QUICK_CHARGE_STATUS_WORKING = "1";
    private static final String QUICK_CHARGE_WIRELESS_STATUS = "3";
    private static final String RUN_MODE_PROPERTY = "ro.runmode";
    private static final int SHUTDOWN_LEVEL_FLASHINGARGB = -1;
    private static final String TAG = "HwBatteryService";
    private static final String WIRELESS_TX_DIR = "sys/class/hw_power/charger/wireless_tx";
    private static final int WIRELESS_TX_END = 0;
    private static final int WIRELESS_TX_FLAG_CLOSE = 2;
    private static final int WIRELESS_TX_FLAG_OPEN = 1;
    private static final int WIRELESS_TX_FLAG_UNKNOWN = 0;
    private static final String WIRELESS_TX_LOW_BATTERY = "20%";
    private static final String WIRELESS_TX_OPEN = "sys/class/hw_power/charger/wireless_tx/tx_open";
    private static final int WIRELESS_TX_START = 1;
    private static final String WIRELESS_TX_STATUS = "sys/class/hw_power/charger/wireless_tx/tx_status";
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
    /* access modifiers changed from: private */
    public static final boolean isChinaRegion = SystemProperties.get("ro.config.hw_optb", "0").equals("156");
    /* access modifiers changed from: private */
    public static final boolean isLedCloseByCamera = SystemProperties.getBoolean("ro.config.led_close_by_camera", false);
    /* access modifiers changed from: private */
    public static final boolean isScreenOnTurnOffLed = SystemProperties.getBoolean("ro.config.screenon_turnoff_led", false);
    /* access modifiers changed from: private */
    public static final boolean isTwoColorLight = SystemProperties.getBoolean("ro.config.hw_two_color_light", false);
    /* access modifiers changed from: private */
    public static final boolean mIsIscdCheck = SystemProperties.getBoolean("ro.config.check_battery_error", true);
    private AudioAttributes mAudioAttributes;
    /* access modifiers changed from: private */
    public final UEventObserver mBatteryFcpObserver = new UEventObserver() {
        public void onUEvent(UEventObserver.UEvent event) {
            String scpStatus = event.get("POWER_SUPPLY_SCP_STATUS", "0");
            String ovpStatus = event.get("POWER_SUPPLY_BAT_OVP", "0");
            String supplyName = event.get("POWER_SUPPLY_NAME");
            if (HwBatteryService.HISI_PLATFORM_SUPPLY_NAME.equals(supplyName) || HwBatteryService.QCOM_PLATFORM_SUPPLY_NAME.equals(supplyName) || HwBatteryService.MTK_PLATFORM_SUPPLY_NAME.equals(supplyName)) {
                String fcpStatus = event.get("POWER_SUPPLY_FCP_STATUS", "0");
                HwBatteryService.this.mHwBatteryHandler.updateQuickChargeState(HwBatteryService.this.getQuickChargeBroadcastStatus(fcpStatus, scpStatus));
                Slog.d(HwBatteryService.TAG, "onUEvent fcpStatus = " + fcpStatus);
            }
            Slog.d(HwBatteryService.TAG, "onUEvent scpStatus = " + scpStatus + " ,ovpStatus = " + ovpStatus);
            HwBatteryService.this.handleBatteryOvpStatus(ovpStatus);
            HwBatteryService.this.handleWirelessTxStatus();
        }
    };
    /* access modifiers changed from: private */
    public final UEventObserver mBatteryIscdObserver = new UEventObserver() {
        public void onUEvent(UEventObserver.UEvent event) {
            Slog.d(HwBatteryService.TAG, "onUEvent battery iscd error");
            String unused = HwBatteryService.this.mBatteryIscdStatus = "1";
            HwBatteryService.this.handleBatteryIscdStatus();
        }
    };
    /* access modifiers changed from: private */
    public String mBatteryIscdStatus = "0";
    private String mBatteryOvpStatus = "0";
    /* access modifiers changed from: private */
    public int mBatteryPropRegisterTryTimes = 0;
    /* access modifiers changed from: private */
    public final Context mContext;
    private HwCustBatteryService mCust = ((HwCustBatteryService) HwCustUtils.createObj(HwCustBatteryService.class, new Object[0]));
    /* access modifiers changed from: private */
    public boolean mFlagScreenOn = true;
    /* access modifiers changed from: private */
    public int mFlashingARGB;
    /* access modifiers changed from: private */
    public boolean mFrontCameraOpening = false;
    private HwAutoPowerOffController mHwAutoPowerOffController;
    /* access modifiers changed from: private */
    public final HwBatteryHandler mHwBatteryHandler;
    private final HandlerThread mHwBatteryThread;
    /* access modifiers changed from: private */
    public HwLed mHwLed;
    /* access modifiers changed from: private */
    public boolean mIsBootFinish = false;
    /* access modifiers changed from: private */
    public boolean mIsNotificationExisting;
    private int mLastWirelessTxStatus = 0;
    private final Object mLock = new Object();
    private boolean mLowBattery = false;
    /* access modifiers changed from: private */
    public int mNotificationLedOff;
    /* access modifiers changed from: private */
    public int mNotificationLedOn;
    /* access modifiers changed from: private */
    public NotificationManager mNotificationManager;
    /* access modifiers changed from: private */
    public String mQuickChargeStatus = "0";
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (context == null || intent == null) {
                Slog.i(HwBatteryService.TAG, "context or intent is null!");
                return;
            }
            Slog.i(HwBatteryService.TAG, "intent = " + intent);
            if (HwBatteryService.ACTION_CALL_BNT_CLICKED.equals(intent.getAction())) {
                HwBatteryService.this.closeSystemDialogs(context);
                int notificationID = intent.getIntExtra(HwBatteryService.BATTERY_ERR_NOTIFICATION_ID, 0);
                if (HwBatteryService.this.mNotificationManager != null) {
                    HwBatteryService.this.mNotificationManager.cancelAsUser(null, notificationID, UserHandle.CURRENT);
                }
                Intent callIntent = new Intent("android.intent.action.CALL");
                if (HwBatteryService.isChinaRegion) {
                    callIntent.setData(Uri.parse("tel:4008308300"));
                }
                callIntent.setFlags(276824064);
                context.startActivityAsUser(callIntent, UserHandle.CURRENT);
            } else if ("android.intent.action.LOCKED_BOOT_COMPLETED".equals(intent.getAction())) {
                boolean unused = HwBatteryService.this.mIsBootFinish = true;
                if (HwBatteryService.mIsIscdCheck && !HwBatteryService.this.mBatteryIscdStatus.equals("0")) {
                    HwBatteryService.this.handleBatteryIscdStatus();
                }
            } else if ("android.intent.action.SCREEN_ON".equals(intent.getAction())) {
                boolean unused2 = HwBatteryService.this.mFlagScreenOn = true;
                if (HwBatteryService.isScreenOnTurnOffLed) {
                    HwBatteryService.this.mHwLed.newUpdateLightsLocked();
                }
            } else if ("android.intent.action.SCREEN_OFF".equals(intent.getAction())) {
                boolean unused3 = HwBatteryService.this.mFlagScreenOn = false;
                if (HwBatteryService.isScreenOnTurnOffLed) {
                    HwBatteryService.this.mHwLed.newUpdateLightsLocked();
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public Ringtone mRingRingtone;
    /* access modifiers changed from: private */
    public Uri mUri;
    private int mWirelessTxFlag = 0;

    private class HealthdDeathRecipient implements IBinder.DeathRecipient {
        private IBinder mCb;

        HealthdDeathRecipient(IBinder cb) {
            if (cb != null) {
                try {
                    Slog.i(HwBatteryService.TAG, "linkToDeath Healthd.");
                    cb.linkToDeath(this, 0);
                } catch (RemoteException e) {
                    Slog.w(HwBatteryService.TAG, "HealthdDeathRecipient() could not link to " + cb + " binder death");
                }
            }
            this.mCb = cb;
        }

        public void binderDied() {
            Slog.w(HwBatteryService.TAG, "Healthd died.");
            if (this.mCb != null) {
                this.mCb.unlinkToDeath(this, 0);
            }
            int unused = HwBatteryService.this.mBatteryPropRegisterTryTimes = 0;
            HwBatteryService.this.mHwBatteryHandler.sendEmptyMessageDelayed(1, 2000);
        }
    }

    private final class HwBatteryHandler extends Handler {
        public HwBatteryHandler(Looper looper) {
            super(looper);
            if (HwBatteryService.mIsIscdCheck) {
                HwBatteryService.this.mBatteryIscdObserver.startObserving("BATTERY_EVENT=FATAL_ISC");
            }
            try {
                updateQuickChargeState(HwBatteryService.this.getQuickChargeBroadcastStatus(FileUtils.readTextFile(new File(HwBatteryService.this.getQuickChargeStatePath()), 0, null).trim(), FileUtils.readTextFile(new File(HwBatteryService.this.getDCQuickChargeStatePath()), 0, null).trim()));
                HwBatteryService.this.handleBatteryOvpStatus(FileUtils.readTextFile(new File(HwBatteryService.this.getBatteryOvpStatePath()), 0, null).trim());
                if (HwBatteryService.mIsIscdCheck) {
                    String iscdStatus = FileUtils.readTextFile(new File(HwBatteryService.this.getBatteryIscdStatePath()), 0, null).trim();
                    Slog.i(HwBatteryService.TAG, "iscdStatus: " + iscdStatus);
                }
            } catch (Exception e) {
                Slog.e(HwBatteryService.TAG, "Error get initialized state.", e);
            }
            HwBatteryService.this.mBatteryFcpObserver.startObserving("SUBSYSTEM=power_supply");
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i != 6) {
                switch (i) {
                    case 1:
                        IBinder binder = ServiceManager.checkService("batteryproperties");
                        if (binder == null || !binder.isBinderAlive()) {
                            int unused = HwBatteryService.this.mBatteryPropRegisterTryTimes = HwBatteryService.this.mBatteryPropRegisterTryTimes + 1;
                            if (HwBatteryService.this.mBatteryPropRegisterTryTimes < 5) {
                                Slog.i(HwBatteryService.TAG, "Try to get batteryproperties service again.");
                                HwBatteryService.this.mHwBatteryHandler.sendEmptyMessageDelayed(1, 2000);
                                return;
                            }
                            Slog.e(HwBatteryService.TAG, "There is no connection between batteryservice and batteryproperties.");
                            return;
                        }
                        int unused2 = HwBatteryService.this.mBatteryPropRegisterTryTimes = 0;
                        new HealthdDeathRecipient(binder);
                        HwBatteryService.this.registerHealthCallback();
                        return;
                    case 2:
                        HwBatteryService.this.sendBatteryErrorNotification(33685926, 33685927, 33751168);
                        return;
                    case 3:
                        String status = (String) msg.obj;
                        if (!HwBatteryService.this.mQuickChargeStatus.equals(status)) {
                            String unused3 = HwBatteryService.this.mQuickChargeStatus = status;
                            sendQuickChargeBroadcast();
                            return;
                        }
                        return;
                    default:
                        return;
                }
            } else {
                HwBatteryService.this.sendBatteryErrorNotification(33685916, 33685915, 33751739);
                HwBatteryService.this.sendBatteryIsdcErrorBroadcast();
            }
        }

        public void updateQuickChargeState(String state) {
            removeMessages(3);
            Message msg = Message.obtain(this, 3);
            msg.obj = state;
            sendMessage(msg);
        }

        private void sendQuickChargeBroadcast() {
            Intent quickChargeIntent = new Intent(HwBatteryService.ACTION_QUICK_CHARGE);
            quickChargeIntent.addFlags(1073741824);
            quickChargeIntent.addFlags(536870912);
            quickChargeIntent.putExtra("quick_charge_status", HwBatteryService.this.mQuickChargeStatus);
            Slog.i(HwBatteryService.TAG, "Stick broadcast intent: " + quickChargeIntent + " mQuickChargeStatus:" + HwBatteryService.this.mQuickChargeStatus);
            HwBatteryService.this.mContext.sendStickyBroadcastAsUser(quickChargeIntent, UserHandle.ALL);
        }
    }

    private final class HwLed {
        private final int mBatteryFullARGB;
        private final int mBatteryLedOff;
        private final int mBatteryLedOn;
        private final Light mBatteryLight;
        private final int mBatteryLowARGB;
        private final int mBatteryMediumARGB;

        public HwLed(Context context, LightsManager lights) {
            this.mBatteryLight = lights.getLight(3);
            this.mBatteryLowARGB = context.getResources().getInteger(17694840);
            if (HwBatteryService.isTwoColorLight) {
                this.mBatteryMediumARGB = context.getResources().getInteger(17694840);
            } else {
                this.mBatteryMediumARGB = context.getResources().getInteger(17694841);
            }
            this.mBatteryFullARGB = context.getResources().getInteger(17694837);
            this.mBatteryLedOn = context.getResources().getInteger(17694839);
            this.mBatteryLedOff = context.getResources().getInteger(17694838);
        }

        private void removeBatteryMediumLights(int level, int status) {
            Flog.i(DeviceStatusConstant.TYPE_HW_STEP_COUNTER, "removeBatteryMediumLights --> level:" + level + ", status:" + status);
            if (HwBatteryService.this.mIsNotificationExisting) {
                if (level <= 4) {
                    if (HwBatteryService.this.mFlashingARGB != this.mBatteryLowARGB) {
                        this.mBatteryLight.turnOff();
                        Flog.i(DeviceStatusConstant.TYPE_HW_STEP_COUNTER, "updateLightsLocked --> level:" + HwBatteryService.this.getHealthInfo().batteryLevel + "-- Flashing red");
                        this.mBatteryLight.setFlashing(this.mBatteryLowARGB, 1, HwBatteryService.this.mNotificationLedOn, HwBatteryService.this.mNotificationLedOff);
                        int unused = HwBatteryService.this.mFlashingARGB = this.mBatteryLowARGB;
                    }
                } else if (status != 2 && status != 5) {
                    Flog.i(DeviceStatusConstant.TYPE_HW_STEP_COUNTER, "updateLightsLocked --> mBatteryLight.turnOff");
                    this.mBatteryLight.turnOff();
                    int unused2 = HwBatteryService.this.mFlashingARGB = 0;
                } else if (HwBatteryService.this.mFlagScreenOn) {
                    Flog.i(DeviceStatusConstant.TYPE_HW_STEP_COUNTER, "updateLightsLocked --> mBatteryLight.turnOff");
                    this.mBatteryLight.turnOff();
                    int unused3 = HwBatteryService.this.mFlashingARGB = 0;
                } else if (status == 5 || level >= 90) {
                    if (HwBatteryService.this.mFlashingARGB != this.mBatteryFullARGB) {
                        this.mBatteryLight.turnOff();
                        Flog.i(DeviceStatusConstant.TYPE_HW_STEP_COUNTER, "updateLightsLocked --> level:" + HwBatteryService.this.getHealthInfo().batteryLevel + "-- Flashing green");
                        this.mBatteryLight.setFlashing(this.mBatteryFullARGB, 1, HwBatteryService.this.mNotificationLedOn, HwBatteryService.this.mNotificationLedOff);
                        int unused4 = HwBatteryService.this.mFlashingARGB = this.mBatteryFullARGB;
                    }
                } else if (HwBatteryService.this.mFlashingARGB != this.mBatteryLowARGB) {
                    this.mBatteryLight.turnOff();
                    Flog.i(DeviceStatusConstant.TYPE_HW_STEP_COUNTER, "updateLightsLocked --> level:" + HwBatteryService.this.getHealthInfo().batteryLevel + "-- Flashing red");
                    this.mBatteryLight.setFlashing(this.mBatteryLowARGB, 1, HwBatteryService.this.mNotificationLedOn, HwBatteryService.this.mNotificationLedOff);
                    int unused5 = HwBatteryService.this.mFlashingARGB = this.mBatteryLowARGB;
                }
            } else if (level <= 4) {
                if (status == 2) {
                    Flog.i(DeviceStatusConstant.TYPE_HW_STEP_COUNTER, "updateLightsLocked --> level:" + HwBatteryService.this.getHealthInfo().batteryLevel + "-- Solid red");
                    this.mBatteryLight.setColor(this.mBatteryLowARGB);
                    int unused6 = HwBatteryService.this.mFlashingARGB = 0;
                } else if (level <= 4 && level > 2) {
                    Flog.i(DeviceStatusConstant.TYPE_HW_STEP_COUNTER, "updateLightsLocked --> level:" + HwBatteryService.this.getHealthInfo().batteryLevel + " -- Solid red");
                    this.mBatteryLight.setColor(this.mBatteryLowARGB);
                    int unused7 = HwBatteryService.this.mFlashingARGB = 0;
                } else if (level <= 2 && HwBatteryService.this.mFlashingARGB != -1) {
                    this.mBatteryLight.turnOff();
                    Flog.i(DeviceStatusConstant.TYPE_HW_STEP_COUNTER, "updateLightsLocked --> level:" + HwBatteryService.this.getHealthInfo().batteryLevel + "--Flashing red SHUTDOWN_LEVEL");
                    this.mBatteryLight.setFlashing(this.mBatteryLowARGB, 1, this.mBatteryLedOn, this.mBatteryLedOff);
                    int unused8 = HwBatteryService.this.mFlashingARGB = -1;
                }
            } else if (status != 2 && status != 5) {
                Flog.i(DeviceStatusConstant.TYPE_HW_STEP_COUNTER, "updateLightsLocked --> mBatteryLight.turnOff");
                this.mBatteryLight.turnOff();
                int unused9 = HwBatteryService.this.mFlashingARGB = 0;
            } else if (HwBatteryService.this.mFlagScreenOn) {
                Flog.i(DeviceStatusConstant.TYPE_HW_STEP_COUNTER, "updateLightsLocked --> mBatteryLight.turnOff");
                this.mBatteryLight.turnOff();
                int unused10 = HwBatteryService.this.mFlashingARGB = 0;
            } else if (status == 5 || level >= 90) {
                Flog.i(DeviceStatusConstant.TYPE_HW_STEP_COUNTER, "updateLightsLocked --> level:" + HwBatteryService.this.getHealthInfo().batteryLevel + "-- Solid green");
                this.mBatteryLight.setColor(this.mBatteryFullARGB);
                int unused11 = HwBatteryService.this.mFlashingARGB = 0;
            } else {
                Flog.i(DeviceStatusConstant.TYPE_HW_STEP_COUNTER, "updateLightsLocked --> level:" + HwBatteryService.this.getHealthInfo().batteryLevel + " -- Solid red");
                this.mBatteryLight.setColor(this.mBatteryLowARGB);
                int unused12 = HwBatteryService.this.mFlashingARGB = 0;
            }
        }

        public void newUpdateLightsLocked() {
            int level = HwBatteryService.this.getHealthInfo().batteryLevel;
            int status = HwBatteryService.this.getHealthInfo().batteryStatus;
            Flog.i(DeviceStatusConstant.TYPE_HW_STEP_COUNTER, "updateLightsLocked --> level:" + level + ", status:" + status + " mFrontCameraOpening " + HwBatteryService.this.mFrontCameraOpening);
            if (HwBatteryService.isLedCloseByCamera && ((status == 2 || status == 5) && HwBatteryService.this.mFrontCameraOpening)) {
                Flog.i(DeviceStatusConstant.TYPE_HW_STEP_COUNTER, "updateLightsLocked --> mBatteryLight.turnOff front camera open");
                this.mBatteryLight.turnOff();
                int unused = HwBatteryService.this.mFlashingARGB = 0;
            } else if (HwBatteryService.isScreenOnTurnOffLed) {
                removeBatteryMediumLights(level, status);
            } else if (HwBatteryService.this.mIsNotificationExisting) {
                if (level < HwBatteryService.this.getLowBatteryWarningLevel()) {
                    if (status == 2) {
                        if (HwBatteryService.this.mFlashingARGB != this.mBatteryLowARGB) {
                            this.mBatteryLight.turnOff();
                            Flog.i(DeviceStatusConstant.TYPE_HW_STEP_COUNTER, "updateLightsLocked --> level:" + HwBatteryService.this.getHealthInfo().batteryLevel + "-- Flashing red");
                            this.mBatteryLight.setFlashing(this.mBatteryLowARGB, 1, HwBatteryService.this.mNotificationLedOn, HwBatteryService.this.mNotificationLedOff);
                            int unused2 = HwBatteryService.this.mFlashingARGB = this.mBatteryLowARGB;
                        }
                    } else if (level > 4) {
                        Flog.i(DeviceStatusConstant.TYPE_HW_STEP_COUNTER, "updateLightsLocked --> mBatteryLight.turnOff");
                        this.mBatteryLight.turnOff();
                        int unused3 = HwBatteryService.this.mFlashingARGB = 0;
                    } else if (HwBatteryService.this.mFlashingARGB != this.mBatteryLowARGB) {
                        this.mBatteryLight.turnOff();
                        Flog.i(DeviceStatusConstant.TYPE_HW_STEP_COUNTER, "updateLightsLocked --> level:" + HwBatteryService.this.getHealthInfo().batteryLevel + "-- Flashing red");
                        this.mBatteryLight.setFlashing(this.mBatteryLowARGB, 1, HwBatteryService.this.mNotificationLedOn, HwBatteryService.this.mNotificationLedOff);
                        int unused4 = HwBatteryService.this.mFlashingARGB = this.mBatteryLowARGB;
                    }
                } else if (status != 2 && status != 5) {
                    Flog.i(DeviceStatusConstant.TYPE_HW_STEP_COUNTER, "updateLightsLocked --> mBatteryLight.turnOff");
                    this.mBatteryLight.turnOff();
                    int unused5 = HwBatteryService.this.mFlashingARGB = 0;
                } else if (status == 5 || level >= 90) {
                    if (HwBatteryService.this.mFlashingARGB != this.mBatteryFullARGB) {
                        this.mBatteryLight.turnOff();
                        Flog.i(DeviceStatusConstant.TYPE_HW_STEP_COUNTER, "updateLightsLocked --> level:" + HwBatteryService.this.getHealthInfo().batteryLevel + "-- Flashing green");
                        this.mBatteryLight.setFlashing(this.mBatteryFullARGB, 1, HwBatteryService.this.mNotificationLedOn, HwBatteryService.this.mNotificationLedOff);
                        int unused6 = HwBatteryService.this.mFlashingARGB = this.mBatteryFullARGB;
                    }
                } else if (HwBatteryService.this.mFlashingARGB != this.mBatteryMediumARGB) {
                    this.mBatteryLight.turnOff();
                    Flog.i(DeviceStatusConstant.TYPE_HW_STEP_COUNTER, "updateLightsLocked --> level:" + HwBatteryService.this.getHealthInfo().batteryLevel + "-- Flashing mBatteryMediumARGB");
                    this.mBatteryLight.setFlashing(this.mBatteryMediumARGB, 1, HwBatteryService.this.mNotificationLedOn, HwBatteryService.this.mNotificationLedOff);
                    int unused7 = HwBatteryService.this.mFlashingARGB = this.mBatteryMediumARGB;
                }
            } else if (level < HwBatteryService.this.getLowBatteryWarningLevel()) {
                if (status == 2) {
                    Flog.i(DeviceStatusConstant.TYPE_HW_STEP_COUNTER, "updateLightsLocked --> level:" + HwBatteryService.this.getHealthInfo().batteryLevel + "-- Solid red");
                    this.mBatteryLight.setColor(this.mBatteryLowARGB);
                    int unused8 = HwBatteryService.this.mFlashingARGB = 0;
                } else if (level <= 4 && level > 2) {
                    Flog.i(DeviceStatusConstant.TYPE_HW_STEP_COUNTER, "updateLightsLocked --> level:" + HwBatteryService.this.getHealthInfo().batteryLevel + "-- Solid red");
                    this.mBatteryLight.setColor(this.mBatteryLowARGB);
                    int unused9 = HwBatteryService.this.mFlashingARGB = 0;
                } else if (level > 2) {
                    Flog.i(DeviceStatusConstant.TYPE_HW_STEP_COUNTER, "updateLightsLocked --> mBatteryLight.turnOff");
                    this.mBatteryLight.turnOff();
                    int unused10 = HwBatteryService.this.mFlashingARGB = 0;
                } else if (HwBatteryService.this.mFlashingARGB != -1) {
                    this.mBatteryLight.turnOff();
                    Flog.i(DeviceStatusConstant.TYPE_HW_STEP_COUNTER, "updateLightsLocked --> level:" + HwBatteryService.this.getHealthInfo().batteryLevel + "-- Flashing red SHUTDOWN_LEVEL");
                    this.mBatteryLight.setFlashing(this.mBatteryLowARGB, 1, this.mBatteryLedOn, this.mBatteryLedOff);
                    int unused11 = HwBatteryService.this.mFlashingARGB = -1;
                }
            } else if (status != 2 && status != 5) {
                Flog.i(DeviceStatusConstant.TYPE_HW_STEP_COUNTER, "updateLightsLocked --> mBatteryLight.turnOff");
                this.mBatteryLight.turnOff();
                int unused12 = HwBatteryService.this.mFlashingARGB = 0;
            } else if (status == 5 || level >= 90) {
                Flog.i(DeviceStatusConstant.TYPE_HW_STEP_COUNTER, "updateLightsLocked --> level:" + HwBatteryService.this.getHealthInfo().batteryLevel + "-- Solid green");
                this.mBatteryLight.setColor(this.mBatteryFullARGB);
                int unused13 = HwBatteryService.this.mFlashingARGB = 0;
            } else {
                Flog.i(DeviceStatusConstant.TYPE_HW_STEP_COUNTER, "updateLightsLocked --> level:" + HwBatteryService.this.getHealthInfo().batteryLevel + " -- Solid mBatteryMediumARGB");
                this.mBatteryLight.setColor(this.mBatteryMediumARGB);
                int unused14 = HwBatteryService.this.mFlashingARGB = 0;
            }
        }
    }

    public HwBatteryService(Context context) {
        super(context);
        this.mContext = context;
        initAndRegisterReceiver();
        this.mHwBatteryThread = new HandlerThread(TAG);
        this.mHwBatteryThread.start();
        this.mHwBatteryHandler = new HwBatteryHandler(this.mHwBatteryThread.getLooper());
        this.mHwLed = new HwLed(context, (LightsManager) getLocalService(LightsManager.class));
        this.mAudioAttributes = new AudioAttributes.Builder().setUsage(13).setContentType(4).build();
        if (IS_AUTO_POWEROFF_ON) {
            this.mHwAutoPowerOffController = new HwAutoPowerOffController(context);
        }
    }

    private boolean writeFile(String path, String data) {
        String str;
        StringBuilder sb;
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(path);
            fos.write(data.getBytes());
            try {
                fos.close();
                return true;
            } catch (IOException e) {
                e2 = e;
                str = TAG;
                sb = new StringBuilder();
            }
            sb.append("closeFile ");
            sb.append(path);
            Slog.e(str, sb.toString(), e2);
            return false;
        } catch (IOException e1) {
            Slog.e(TAG, "writeFile " + path, e1);
            if (fos == null) {
                return false;
            }
            try {
                fos.close();
                return false;
            } catch (IOException e2) {
                e2 = e2;
                str = TAG;
                sb = new StringBuilder();
            }
        } catch (Throwable th) {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e22) {
                    Slog.e(TAG, "closeFile " + path, e22);
                }
            }
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x009d, code lost:
        return 0;
     */
    public int alterWirelessTxSwitchInternal(int status) {
        if (status == 1 || status == 0) {
            Slog.i(TAG, "alterWirelessTxStatus status : " + status);
            synchronized (this.mLock) {
                this.mWirelessTxFlag = 0;
                this.mLastWirelessTxStatus = 0;
                if (!writeFile(WIRELESS_TX_OPEN, String.valueOf(status))) {
                    Slog.e(TAG, "writeFile error sys/class/hw_power/charger/wireless_tx/tx_open");
                    return -1;
                }
                try {
                    this.mLock.wait(100);
                } catch (InterruptedException ie) {
                    Slog.e(TAG, "Error occurs when sleep", ie);
                }
                int wirelessTxStatus = getWirelessTxStatus(WIRELESS_TX_STATUS);
                if (!allowWirelessTxSwitch(status, wirelessTxStatus)) {
                    Slog.i(TAG, "not allowed to open tx wireless : " + wirelessTxStatus);
                    writeFile(WIRELESS_TX_OPEN, String.valueOf(0));
                    return wirelessTxStatus;
                } else if (status == 1) {
                    this.mWirelessTxFlag = 1;
                } else {
                    sendWirelessTxStatusChangeBroadcast(0);
                    this.mWirelessTxFlag = 2;
                }
            }
        } else {
            Slog.w(TAG, "alterWirelessTxStatus status error : " + status);
            return -1;
        }
    }

    /* access modifiers changed from: protected */
    public int getWirelessTxSwitchInternal() {
        return getWirelessTxStatus(WIRELESS_TX_OPEN);
    }

    /* access modifiers changed from: protected */
    public boolean supportWirelessTxChargeInternal() {
        File wirelessTxDir = new File(WIRELESS_TX_DIR);
        Slog.i(TAG, "exists : " + wirelessTxDir.exists());
        return wirelessTxDir.exists() && wirelessTxDir.isDirectory();
    }

    private int getWirelessTxStatus(String wirelessTxPath) {
        int result = 0;
        try {
            result = Integer.parseInt(FileUtils.readTextFile(new File(wirelessTxPath), 0, null).trim());
        } catch (IOException ioe) {
            Slog.e(TAG, "Error occurs when read " + wirelessTxPath, ioe);
        } catch (NumberFormatException e) {
            Slog.e(TAG, "Error occurs when translate status : " + null);
        }
        Slog.i(TAG, "getWirelessTxStatus , " + wirelessTxPath + " : " + result);
        return result;
    }

    private boolean isWirelessTxNormal(int status) {
        switch (status) {
            case 2:
            case 3:
                return true;
            default:
                return false;
        }
    }

    private boolean isWirelessTxError(int status) {
        if (status != 4) {
            switch (status) {
                case 16:
                case 17:
                    break;
                default:
                    switch (status) {
                        case 19:
                        case 20:
                        case 21:
                        case 22:
                        case 23:
                            break;
                        default:
                            return false;
                    }
            }
        }
        return true;
    }

    private boolean isWirelessTxDisconnect(int status) {
        return status == 18 || status == 1;
    }

    private int getWirelessTxErrorRes(int status) {
        this.mLowBattery = false;
        if (status == 4) {
            return 33686261;
        }
        if (status != 23) {
            switch (status) {
                case 19:
                    return 33686263;
                case 20:
                    return 33686265;
                case 21:
                    return 33686264;
                default:
                    return -1;
            }
        } else {
            this.mLowBattery = true;
            return 33686262;
        }
    }

    private boolean allowWirelessTxSwitch(int switchStatus, int wirelessTxStatus) {
        if (switchStatus == 0) {
            return true;
        }
        boolean result = true;
        switch (wirelessTxStatus) {
            case 20:
            case 21:
            case 22:
            case 23:
                result = false;
                break;
        }
        return result;
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0059, code lost:
        if (isWirelessTxNormal(r0) == false) goto L_0x005f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x005b, code lost:
        sendWirelessTxStatusChangeBroadcast(1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0063, code lost:
        if (isWirelessTxError(r0) == false) goto L_0x006c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0065, code lost:
        sendWirelessTxChargeErrorBroadcast();
        sendWirelessTxErrorNotification(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0070, code lost:
        if (isWirelessTxDisconnect(r0) == false) goto L_0x0076;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0072, code lost:
        sendWirelessTxStatusChangeBroadcast(0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0076, code lost:
        return;
     */
    public void handleWirelessTxStatus() {
        Slog.i(TAG, "handleWirelessTxStatus");
        synchronized (this.mLock) {
            if (this.mWirelessTxFlag != 1) {
                Slog.i(TAG, "switch not open, return, mWirelessTxFlag : " + this.mWirelessTxFlag);
                return;
            }
            int wirelessTxStatus = getWirelessTxStatus(WIRELESS_TX_STATUS);
            if (wirelessTxStatus == this.mLastWirelessTxStatus) {
                Slog.i(TAG, "wireless_tx_status not changed, return, mLastWirelessTxStatus : " + this.mLastWirelessTxStatus);
                return;
            }
            this.mLastWirelessTxStatus = wirelessTxStatus;
        }
    }

    private void sendWirelessTxStatusChangeBroadcast(int status) {
        Intent intent = new Intent(ACTION_WIRELESS_TX_STATUS_CHANGE);
        intent.addFlags(1073741824);
        intent.addFlags(536870912);
        intent.putExtra("status", status);
        Slog.i(TAG, "sendWirelessTxStatusChangeBroadcast status : " + status);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, PERMISSION_TX_STATUS_CHANGE);
    }

    private void sendWirelessTxChargeErrorBroadcast() {
        Intent intent = new Intent(ACTION_WIRELESS_TX_CHARGE_ERROR);
        intent.addFlags(1073741824);
        intent.addFlags(536870912);
        Slog.i(TAG, "sendWirelessTxChargeErrorBroadcast");
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL, PERMISSION_TX_CHARGE_ERROR);
    }

    private void sendWirelessTxErrorNotification(int status) {
        int messageId = getWirelessTxErrorRes(status);
        if (messageId != -1) {
            Slog.i(TAG, "sendWirelessTxErrorNotification");
            String message = this.mContext.getResources().getString(messageId);
            if (this.mLowBattery) {
                message = String.format(message, new Object[]{WIRELESS_TX_LOW_BATTERY});
            }
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(HwRecentsTaskUtils.PKG_SYS_MANAGER, "com.huawei.systemmanager.power.ui.HwPowerManagerActivity"));
            PendingIntent pi = PendingIntent.getActivityAsUser(this.mContext, 0, intent, 0, null, UserHandle.CURRENT);
            makeNotificationChannel(message);
            Notification notification = new Notification.Builder(this.mContext, OVP_ERR_CHANNEL_ID).setSmallIcon(33752146).setContentText(message).setContentIntent(pi).setVibrate(new long[0]).setPriority(2).setWhen(System.currentTimeMillis()).setShowWhen(true).setVisibility(1).setStyle(new Notification.BigTextStyle().bigText(message)).setAutoCancel(true).build();
            if (this.mNotificationManager != null) {
                this.mNotificationManager.notifyAsUser(null, messageId, notification, UserHandle.ALL);
            }
        }
    }

    public void onStart() {
        HwBatteryService.super.onStart();
        new HealthdDeathRecipient(ServiceManager.getService("batteryproperties"));
    }

    /* access modifiers changed from: private */
    public String getQuickChargeStatePath() {
        return "/sys/class/power_supply/Battery/fcp_status";
    }

    /* access modifiers changed from: private */
    public String getDCQuickChargeStatePath() {
        return "/sys/class/power_supply/Battery/scp_status";
    }

    /* access modifiers changed from: private */
    public String getBatteryOvpStatePath() {
        return "/sys/class/power_supply/Battery/bat_ovp";
    }

    /* access modifiers changed from: private */
    public String getBatteryIscdStatePath() {
        return "/sys/class/hw_power/battery/isc";
    }

    private void initAndRegisterReceiver() {
        this.mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_CALL_BNT_CLICKED);
        filter.addAction("android.intent.action.LOCKED_BOOT_COMPLETED");
        filter.addAction("android.intent.action.SCREEN_ON");
        filter.addAction("android.intent.action.SCREEN_OFF");
        this.mContext.registerReceiver(this.mReceiver, filter, "android.permission.DEVICE_POWER", null);
    }

    /* access modifiers changed from: private */
    public void closeSystemDialogs(Context context) {
        context.sendBroadcastAsUser(new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS"), UserHandle.ALL);
    }

    private void makeNotificationChannel(String name) {
        if (this.mNotificationManager != null) {
            this.mNotificationManager.createNotificationChannel(new NotificationChannel(OVP_ERR_CHANNEL_ID, name, 4));
        }
    }

    /* access modifiers changed from: private */
    public void sendBatteryErrorNotification(int titleId, int messageId, int iconId) {
        Slog.i(TAG, "sendBatteryErrorNotification");
        String title = this.mContext.getResources().getString(titleId);
        CharSequence message = this.mContext.getResources().getString(messageId);
        Intent dialIntent = new Intent("android.intent.action.DIAL");
        if (isChinaRegion) {
            dialIntent.setData(Uri.parse("tel:4008308300"));
        }
        PendingIntent pi = PendingIntent.getActivityAsUser(this.mContext, 0, dialIntent, 0, null, UserHandle.CURRENT);
        Intent intent = new Intent(ACTION_CALL_BNT_CLICKED);
        intent.putExtra(BATTERY_ERR_NOTIFICATION_ID, titleId);
        intent.setPackage(this.mContext.getPackageName());
        PendingIntent actionClickPI = PendingIntent.getBroadcast(this.mContext, 0, intent, 0);
        makeNotificationChannel(title);
        Notification.Builder build = new Notification.Builder(this.mContext, OVP_ERR_CHANNEL_ID).setSmallIcon(iconId).setOngoing(true).setContentTitle(title).setContentText(message).setContentIntent(pi).setTicker(title).setVibrate(new long[0]).setPriority(2).setWhen(System.currentTimeMillis()).setShowWhen(true).setVisibility(1).setAutoCancel(true);
        if (isChinaRegion) {
            build.addAction(new Notification.Action.Builder(null, this.mContext.getString(33685928), actionClickPI).build());
        }
        Notification notification = build.build();
        if (this.mNotificationManager != null) {
            this.mNotificationManager.notifyAsUser(null, titleId, notification, UserHandle.ALL);
        }
    }

    /* access modifiers changed from: private */
    public void handleBatteryOvpStatus(String status) {
        if (!this.mBatteryOvpStatus.equals(status) && "1".equals(status)) {
            Slog.i(TAG, "battery ovp error occur, send notification before.");
            this.mBatteryOvpStatus = status;
            if (this.mHwBatteryHandler.hasMessages(2)) {
                this.mHwBatteryHandler.removeMessages(2);
            }
            this.mHwBatteryHandler.sendMessage(Message.obtain(this.mHwBatteryHandler, 2, status));
        }
    }

    /* access modifiers changed from: private */
    public void handleBatteryIscdStatus() {
        if (this.mIsBootFinish) {
            if (this.mHwBatteryHandler.hasMessages(6)) {
                this.mHwBatteryHandler.removeMessages(6);
            }
            this.mHwBatteryHandler.sendMessage(Message.obtain(this.mHwBatteryHandler, 6));
        }
    }

    /* access modifiers changed from: private */
    public String getQuickChargeBroadcastStatus(String fcpStatus, String scpStatus) {
        String status = "0";
        if (isWirelessCharge()) {
            if (fcpStatus.equals("1")) {
                status = "3";
            }
        } else if (fcpStatus.equals("1") && scpStatus.equals("1")) {
            status = "2";
        } else if (fcpStatus.equals("1") && scpStatus.equals("0")) {
            status = "1";
        } else if (fcpStatus.equals("0") && scpStatus.equals("1")) {
            status = "2";
        }
        Slog.i(TAG, "quick charge status : " + status);
        return status;
    }

    /* access modifiers changed from: protected */
    public void updateLight() {
        this.mHwLed.newUpdateLightsLocked();
    }

    /* access modifiers changed from: protected */
    public void updateLight(boolean enable, int ledOnMS, int ledOffMS) {
        if (this.mIsNotificationExisting != enable || this.mNotificationLedOn != ledOnMS || this.mNotificationLedOff != ledOffMS) {
            Flog.i(DeviceStatusConstant.TYPE_HW_STEP_COUNTER, " updateLight --> mIsNotificationExisting : " + enable + " ledOnMS : " + ledOnMS + " ledOffMS : " + ledOffMS);
            this.mIsNotificationExisting = enable;
            this.mNotificationLedOn = ledOnMS;
            this.mNotificationLedOff = ledOffMS;
            this.mHwLed.newUpdateLightsLocked();
        }
    }

    /* access modifiers changed from: protected */
    public void cameraUpdateLight(boolean enable) {
        Slog.d(TAG, "cameraUpdateLight enable " + enable);
        if (this.mFrontCameraOpening != enable) {
            this.mFrontCameraOpening = enable;
            this.mHwLed.newUpdateLightsLocked();
        }
    }

    /* access modifiers changed from: protected */
    public void playRing() {
        if (FACTORY_VERSION.equalsIgnoreCase(SystemProperties.get(RUN_MODE_PROPERTY, "unknown"))) {
            return;
        }
        if (this.mCust == null || !this.mCust.mutePowerConnectedTone()) {
            this.mHwBatteryHandler.post(new Runnable() {
                public void run() {
                    boolean isWireless = true;
                    if (HwBatteryService.this.mPlugType != 1) {
                        isWireless = false;
                    }
                    Uri unused = HwBatteryService.this.mUri = HwBatteryService.queryRingMusicUri(HwBatteryService.this.mContext, isWireless ? HwBatteryService.WIRELSSS_CONNECTED_RINGTONE : HwBatteryService.PPWER_CONNECTED_RINGTONE);
                    Ringtone unused2 = HwBatteryService.this.mRingRingtone = HwBatteryService.this.playRing(HwBatteryService.this.mUri, HwBatteryService.this.mRingRingtone);
                }
            });
        }
    }

    /* access modifiers changed from: protected */
    public void stopRing() {
        if (!FACTORY_VERSION.equalsIgnoreCase(SystemProperties.get(RUN_MODE_PROPERTY, "unknown"))) {
            this.mHwBatteryHandler.post(new Runnable() {
                public void run() {
                    HwBatteryService.this.stopRing(HwBatteryService.this.mRingRingtone);
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public Ringtone playRing(Uri uri, Ringtone ringtone) {
        Ringtone ringtone2 = RingtoneManager.getRingtone(this.mContext, uri);
        if (ringtone2 != null) {
            ringtone2.setAudioAttributes(this.mAudioAttributes);
            ringtone2.play();
        }
        return ringtone2;
    }

    /* access modifiers changed from: private */
    public void stopRing(Ringtone ringtone) {
        if (ringtone != null) {
            ringtone.stop();
        }
    }

    /* access modifiers changed from: private */
    public static Uri queryRingMusicUri(Context context, String fileName) {
        return queryRingMusicUri(context.getContentResolver(), fileName);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0047, code lost:
        if (r9 != null) goto L_0x0049;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0049, code lost:
        r9.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0055, code lost:
        if (r9 == null) goto L_0x0058;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0058, code lost:
        return null;
     */
    private static Uri queryRingMusicUri(ContentResolver resolver, String fileName) {
        if (fileName == null) {
            return null;
        }
        Uri uri = MediaStore.Audio.Media.INTERNAL_CONTENT_URI;
        Cursor cur = null;
        try {
            cur = resolver.query(uri, new String[]{"_id"}, "_data like '%" + fileName + "'", null, null);
            if (cur != null && cur.moveToFirst()) {
                Uri withAppendedId = ContentUris.withAppendedId(uri, (long) cur.getInt(cur.getColumnIndex("_id")));
                if (cur != null) {
                    cur.close();
                }
                return withAppendedId;
            }
        } catch (Exception e) {
        } catch (Throwable th) {
            if (cur != null) {
                cur.close();
            }
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public void printBatteryLog(HealthInfo oldInfo, HealthInfo newInfo, int oldPlugType, boolean updatesStopped) {
        int plugType;
        if (oldInfo == null || newInfo == null) {
            Flog.i(WifiProCommonUtils.RESP_CODE_REDIRECTED_HOST_CHANGED, "mBatteryProps or new battery values is null");
            return;
        }
        if (newInfo.chargerAcOnline) {
            plugType = 1;
        } else if (newInfo.chargerUsbOnline != 0) {
            plugType = 2;
        } else if (newInfo.chargerWirelessOnline) {
            plugType = 4;
        } else {
            plugType = 0;
        }
        if (!(plugType == oldPlugType && oldInfo.batteryLevel == newInfo.batteryLevel)) {
            Flog.i(WifiProCommonUtils.RESP_CODE_REDIRECTED_HOST_CHANGED, "update battery new values: chargerAcOnline=" + newInfo.chargerAcOnline + ", chargerUsbOnline=" + newInfo.chargerUsbOnline + ", batteryStatus=" + newInfo.batteryStatus + ", batteryHealth=" + newInfo.batteryHealth + ", batteryPresent=" + newInfo.batteryPresent + ", batteryLevel=" + newInfo.batteryLevel + ", batteryTechnology=" + newInfo.batteryTechnology + ", batteryVoltage=" + newInfo.batteryVoltage + ", batteryTemperature=" + newInfo.batteryTemperature + ", mUpdatesStopped=" + updatesStopped);
        }
    }

    /* access modifiers changed from: private */
    public void sendBatteryIsdcErrorBroadcast() {
        Intent iscdErrorIntent = new Intent(ACTION_BATTERY_ISCD_ERROR);
        iscdErrorIntent.addFlags(1073741824);
        iscdErrorIntent.addFlags(536870912);
        Slog.i(TAG, "Stick broadcast intent: " + iscdErrorIntent);
        this.mContext.sendStickyBroadcastAsUser(iscdErrorIntent, UserHandle.ALL);
    }

    /* access modifiers changed from: protected */
    public void startAutoPowerOff() {
        if (this.mHwAutoPowerOffController != null) {
            Slog.d(TAG, "startAutoPowerOff()");
            this.mHwAutoPowerOffController.startAutoPowerOff();
        }
    }

    /* access modifiers changed from: protected */
    public void stopAutoPowerOff() {
        if (this.mHwAutoPowerOffController != null) {
            Slog.d(TAG, "stopAutoPowerOff");
            this.mHwAutoPowerOffController.stopAutoPowerOff();
        }
    }
}
