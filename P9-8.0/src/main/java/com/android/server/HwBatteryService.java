package com.android.server;

import android.app.Notification;
import android.app.Notification.Action;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.AudioAttributes;
import android.media.AudioAttributes.Builder;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.BatteryProperties;
import android.os.FileUtils;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBatteryPropertiesRegistrar.Stub;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UEventObserver;
import android.os.UEventObserver.UEvent;
import android.os.UserHandle;
import android.provider.MediaStore.Audio.Media;
import android.util.Flog;
import android.util.Slog;
import com.android.server.BatteryService.BatteryListener;
import com.android.server.lights.Light;
import com.android.server.lights.LightsManager;
import com.android.server.wifipro.WifiProCommonUtils;
import huawei.cust.HwCustUtils;
import java.io.File;

public final class HwBatteryService extends BatteryService {
    private static final String ACTION_BATTERY_ISCD_ERROR = "huawei.intent.action.BATTERY_ISCD_ERROR";
    private static final String ACTION_CALL_BNT_CLICKED = "huawei.intent.action.CALL_BNT_CLICKED";
    private static final String ACTION_QUICK_CHARGE = "huawei.intent.action.BATTERY_QUICK_CHARGE";
    private static final String BATTERY_ERR_NOTIFICATION_ID = "n_id";
    private static final String BATTERY_ISCD_STATUS_ERROR = "1";
    private static final String BATTERY_ISCD_STATUS_NORMAL = "0";
    private static final String BATTERY_OVP_STATUS_ERROR = "1";
    private static final String BATTERY_OVP_STATUS_NORMAL = "0";
    private static final String FACTORY_VERSION = "factory";
    private static final String HUAWEI_CHINA_CUSTOMER_SERVICE_HOTLINE_NUMBER = "4008308300";
    public static final int LOW_BATTERY_SHUTDOWN_LEVEL = 2;
    public static final int LOW_BATTERY_WARNING_LEVEL = 4;
    private static final int MAX_BATTERY_PROP_REGISTER_TIMES = 5;
    private static final int MSG_BATTERY_ISCD_ERROR = 6;
    private static final int MSG_BATTERY_OVP_ERROR = 2;
    private static final int MSG_BATTERY_PROP_REGISTER = 1;
    private static final int MSG_UPDATE_QUICK_CHARGE_STATE = 3;
    private static final String OVP_ERR_CHANNEL_ID = "battery_error_c_id";
    private static final int PLUGGED_NONE = 0;
    private static final String PPWER_CONNECTED_RINGTONE = "PowerConnected.ogg";
    private static final String QUICK_CHARGE_FCP_STATUS = "1";
    private static final String QUICK_CHARGE_NODE_NOT_EXIST = "0";
    private static final String QUICK_CHARGE_NONE_STATUS = "0";
    private static final String QUICK_CHARGE_SCP_STATUS = "2";
    private static final String QUICK_CHARGE_STATUS_NORMAL = "0";
    private static final String QUICK_CHARGE_STATUS_WORKING = "1";
    private static final String RUN_MODE_PROPERTY = "ro.runmode";
    private static final int SHUTDOWN_LEVEL_FLASHINGARGB = -1;
    private static final String TAG = "HwBatteryService";
    private static final boolean isChinaRegion = SystemProperties.get("ro.config.hw_optb", "0").equals("156");
    private static final boolean isIscdCheck = SystemProperties.getBoolean("ro.config.check_battery_error", false);
    private static final boolean isTwoColorLight = SystemProperties.getBoolean("ro.config.hw_two_color_light", false);
    private AudioAttributes mAudioAttributes;
    private final UEventObserver mBatteryFcpObserver = new UEventObserver() {
        public void onUEvent(UEvent event) {
            String fcpStatus = event.get("POWER_SUPPLY_FCP_STATUS", "0");
            String scpStatus = event.get("POWER_SUPPLY_SCP_STATUS", "0");
            String ovpStatus = event.get("POWER_SUPPLY_BAT_OVP", "0");
            Slog.d(HwBatteryService.TAG, "onUEvent fcpStatus = " + fcpStatus + ",scpStatus =" + scpStatus + ",ovpStatus=" + ovpStatus);
            HwBatteryService.this.mHwBatteryHandler.updateQuickChargeState(HwBatteryService.this.getQuickChargeBroadcastStatus(fcpStatus, scpStatus));
            HwBatteryService.this.handleBatteryOvpStatus(ovpStatus);
        }
    };
    private final UEventObserver mBatteryIscdObserver = new UEventObserver() {
        public void onUEvent(UEvent event) {
            Slog.d(HwBatteryService.TAG, "onUEvent battery iscd error");
            HwBatteryService.this.mBatteryIscdStatus = "1";
            HwBatteryService.this.handleBatteryIscdStatus();
        }
    };
    private String mBatteryIscdStatus = "0";
    private String mBatteryOvpStatus = "0";
    private int mBatteryPropRegisterTryTimes = 0;
    private final Context mContext;
    private HwCustBatteryService mCust = ((HwCustBatteryService) HwCustUtils.createObj(HwCustBatteryService.class, new Object[0]));
    private int mFlashingARGB;
    private final HwBatteryHandler mHwBatteryHandler;
    private final HandlerThread mHwBatteryThread;
    private HwLed mHwLed;
    private boolean mIsBootFinish = false;
    private boolean mIsNotificationExisting;
    private int mNotificationLedOff;
    private int mNotificationLedOn;
    private NotificationManager mNotificationManager;
    private String mQuickChargeStatus = "0";
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
                HwBatteryService.this.mIsBootFinish = true;
                if (HwBatteryService.isIscdCheck && (HwBatteryService.this.mBatteryIscdStatus.equals("0") ^ 1) != 0) {
                    HwBatteryService.this.handleBatteryIscdStatus();
                }
            }
        }
    };
    private Ringtone mRingRingtone;
    private Uri mUri;

    private class HealthdDeathRecipient implements DeathRecipient {
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
            HwBatteryService.this.mBatteryPropRegisterTryTimes = 0;
            HwBatteryService.this.mHwBatteryHandler.sendEmptyMessageDelayed(1, 2000);
        }
    }

    private final class HwBatteryHandler extends Handler {
        public HwBatteryHandler(Looper looper) {
            super(looper);
            if (HwBatteryService.isIscdCheck) {
                HwBatteryService.this.mBatteryIscdObserver.startObserving("BATTERY_EVENT=FATAL_ISC");
            }
            try {
                updateQuickChargeState(HwBatteryService.this.getQuickChargeBroadcastStatus(FileUtils.readTextFile(new File(HwBatteryService.this.getQuickChargeStatePath()), 0, null).trim(), FileUtils.readTextFile(new File(HwBatteryService.this.getDCQuickChargeStatePath()), 0, null).trim()));
                HwBatteryService.this.handleBatteryOvpStatus(FileUtils.readTextFile(new File(HwBatteryService.this.getBatteryOvpStatePath()), 0, null).trim());
                if (HwBatteryService.isIscdCheck) {
                    Slog.i(HwBatteryService.TAG, "iscdStatus: " + FileUtils.readTextFile(new File(HwBatteryService.this.getBatteryIscdStatePath()), 0, null).trim());
                }
            } catch (Exception e) {
                Slog.e(HwBatteryService.TAG, "Error get initialized state.", e);
            }
            HwBatteryService.this.mBatteryFcpObserver.startObserving("SUBSYSTEM=power_supply");
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    IBinder binder = ServiceManager.checkService("batteryproperties");
                    if (binder == null || !binder.isBinderAlive()) {
                        HwBatteryService hwBatteryService = HwBatteryService.this;
                        hwBatteryService.mBatteryPropRegisterTryTimes = hwBatteryService.mBatteryPropRegisterTryTimes + 1;
                        if (HwBatteryService.this.mBatteryPropRegisterTryTimes < 5) {
                            Slog.i(HwBatteryService.TAG, "Try to get batteryproperties service again.");
                            HwBatteryService.this.mHwBatteryHandler.sendEmptyMessageDelayed(1, 2000);
                            return;
                        }
                        Slog.e(HwBatteryService.TAG, "There is no connection between batteryservice and batteryproperties.");
                        return;
                    }
                    HwBatteryService.this.mBatteryPropRegisterTryTimes = 0;
                    HealthdDeathRecipient healthdDeathRecipient = new HealthdDeathRecipient(binder);
                    try {
                        Stub.asInterface(binder).registerListener(new BatteryListener(HwBatteryService.this));
                        return;
                    } catch (RemoteException e) {
                        return;
                    }
                case 2:
                    HwBatteryService.this.sendBatteryErrorNotification(33685926, 33685927, 33751168);
                    return;
                case 3:
                    String status = msg.obj;
                    if (!HwBatteryService.this.mQuickChargeStatus.equals(status)) {
                        HwBatteryService.this.mQuickChargeStatus = status;
                        sendQuickChargeBroadcast();
                        return;
                    }
                    return;
                case 6:
                    HwBatteryService.this.sendBatteryErrorNotification(33685528, 33685527, 33751135);
                    HwBatteryService.this.sendBatteryIsdcErrorBroadcast();
                    return;
                default:
                    return;
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
            this.mBatteryLowARGB = context.getResources().getInteger(17694829);
            if (HwBatteryService.isTwoColorLight) {
                this.mBatteryMediumARGB = context.getResources().getInteger(17694829);
            } else {
                this.mBatteryMediumARGB = context.getResources().getInteger(17694830);
            }
            this.mBatteryFullARGB = context.getResources().getInteger(17694826);
            this.mBatteryLedOn = context.getResources().getInteger(17694828);
            this.mBatteryLedOff = context.getResources().getInteger(17694827);
        }

        public void newUpdateLightsLocked() {
            int level = HwBatteryService.this.getBatteryProps().batteryLevel;
            int status = HwBatteryService.this.getBatteryProps().batteryStatus;
            Flog.i(1100, "updateLightsLocked --> level:" + level + ", status:" + status);
            if (HwBatteryService.this.mIsNotificationExisting) {
                if (level < HwBatteryService.this.getLowBatteryWarningLevel()) {
                    if (status == 2) {
                        if (HwBatteryService.this.mFlashingARGB != this.mBatteryLowARGB) {
                            this.mBatteryLight.turnOff();
                            Flog.i(1100, "updateLightsLocked --> level:" + HwBatteryService.this.getBatteryProps().batteryLevel + "-- Flashing red");
                            this.mBatteryLight.setFlashing(this.mBatteryLowARGB, 1, HwBatteryService.this.mNotificationLedOn, HwBatteryService.this.mNotificationLedOff);
                            HwBatteryService.this.mFlashingARGB = this.mBatteryLowARGB;
                        }
                    } else if (level <= 4 && level > 2) {
                        Flog.i(1100, "updateLightsLocked --> level:" + HwBatteryService.this.getBatteryProps().batteryLevel + "-- Solid red");
                        this.mBatteryLight.setColor(this.mBatteryLowARGB);
                        HwBatteryService.this.mFlashingARGB = 0;
                    } else if (level > 2) {
                        Flog.i(1100, "updateLightsLocked --> mBatteryLight.turnOff");
                        this.mBatteryLight.turnOff();
                        HwBatteryService.this.mFlashingARGB = 0;
                    } else if (HwBatteryService.this.mFlashingARGB != -1) {
                        this.mBatteryLight.turnOff();
                        Flog.i(1100, "updateLightsLocked --> level:" + HwBatteryService.this.getBatteryProps().batteryLevel + "--Flashing red SHUTDOWN_LEVEL");
                        this.mBatteryLight.setFlashing(this.mBatteryLowARGB, 1, this.mBatteryLedOn, this.mBatteryLedOff);
                        HwBatteryService.this.mFlashingARGB = -1;
                    }
                } else if (status != 2 && status != 5) {
                    Flog.i(1100, "updateLightsLocked --> mBatteryLight.turnOff");
                    this.mBatteryLight.turnOff();
                    HwBatteryService.this.mFlashingARGB = 0;
                } else if (status == 5 || level >= 90) {
                    if (HwBatteryService.this.mFlashingARGB != this.mBatteryFullARGB) {
                        this.mBatteryLight.turnOff();
                        Flog.i(1100, "updateLightsLocked --> level:" + HwBatteryService.this.getBatteryProps().batteryLevel + "-- Flashing green");
                        this.mBatteryLight.setFlashing(this.mBatteryFullARGB, 1, HwBatteryService.this.mNotificationLedOn, HwBatteryService.this.mNotificationLedOff);
                        HwBatteryService.this.mFlashingARGB = this.mBatteryFullARGB;
                    }
                } else if (HwBatteryService.this.mFlashingARGB != this.mBatteryMediumARGB) {
                    this.mBatteryLight.turnOff();
                    Flog.i(1100, "updateLightsLocked --> level:" + HwBatteryService.this.getBatteryProps().batteryLevel + "-- Flashing mBatteryMediumARGB");
                    this.mBatteryLight.setFlashing(this.mBatteryMediumARGB, 1, HwBatteryService.this.mNotificationLedOn, HwBatteryService.this.mNotificationLedOff);
                    HwBatteryService.this.mFlashingARGB = this.mBatteryMediumARGB;
                }
            } else if (level < HwBatteryService.this.getLowBatteryWarningLevel()) {
                if (status == 2) {
                    Flog.i(1100, "updateLightsLocked --> level:" + HwBatteryService.this.getBatteryProps().batteryLevel + "-- Solid red");
                    this.mBatteryLight.setColor(this.mBatteryLowARGB);
                    HwBatteryService.this.mFlashingARGB = 0;
                } else if (level <= 4 && level > 2) {
                    Flog.i(1100, "updateLightsLocked --> level:" + HwBatteryService.this.getBatteryProps().batteryLevel + "-- Solid red");
                    this.mBatteryLight.setColor(this.mBatteryLowARGB);
                    HwBatteryService.this.mFlashingARGB = 0;
                } else if (level > 2) {
                    Flog.i(1100, "updateLightsLocked --> mBatteryLight.turnOff");
                    this.mBatteryLight.turnOff();
                    HwBatteryService.this.mFlashingARGB = 0;
                } else if (HwBatteryService.this.mFlashingARGB != -1) {
                    this.mBatteryLight.turnOff();
                    Flog.i(1100, "updateLightsLocked --> level:" + HwBatteryService.this.getBatteryProps().batteryLevel + "-- Flashing red SHUTDOWN_LEVEL");
                    this.mBatteryLight.setFlashing(this.mBatteryLowARGB, 1, this.mBatteryLedOn, this.mBatteryLedOff);
                    HwBatteryService.this.mFlashingARGB = -1;
                }
            } else if (status != 2 && status != 5) {
                Flog.i(1100, "updateLightsLocked --> mBatteryLight.turnOff");
                this.mBatteryLight.turnOff();
                HwBatteryService.this.mFlashingARGB = 0;
            } else if (status == 5 || level >= 90) {
                Flog.i(1100, "updateLightsLocked --> level:" + HwBatteryService.this.getBatteryProps().batteryLevel + "-- Solid green");
                this.mBatteryLight.setColor(this.mBatteryFullARGB);
                HwBatteryService.this.mFlashingARGB = 0;
            } else {
                Flog.i(1100, "updateLightsLocked --> level:" + HwBatteryService.this.getBatteryProps().batteryLevel + " -- Solid mBatteryMediumARGB");
                this.mBatteryLight.setColor(this.mBatteryMediumARGB);
                HwBatteryService.this.mFlashingARGB = 0;
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
        this.mAudioAttributes = new Builder().setUsage(13).setContentType(4).build();
    }

    public void onStart() {
        super.onStart();
        HealthdDeathRecipient healthdDeathRecipient = new HealthdDeathRecipient(ServiceManager.getService("batteryproperties"));
    }

    private String getQuickChargeStatePath() {
        return "/sys/class/power_supply/Battery/fcp_status";
    }

    private String getDCQuickChargeStatePath() {
        return "/sys/class/power_supply/Battery/scp_status";
    }

    private String getBatteryOvpStatePath() {
        return "/sys/class/power_supply/Battery/bat_ovp";
    }

    private String getBatteryIscdStatePath() {
        return "/sys/class/hw_power/battery/isc";
    }

    private void initAndRegisterReceiver() {
        this.mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_CALL_BNT_CLICKED);
        filter.addAction("android.intent.action.LOCKED_BOOT_COMPLETED");
        this.mContext.registerReceiver(this.mReceiver, filter, "android.permission.DEVICE_POWER", null);
    }

    private void closeSystemDialogs(Context context) {
        context.sendBroadcastAsUser(new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS"), UserHandle.ALL);
    }

    private void makeNotificationChannel(String name) {
        if (this.mNotificationManager != null) {
            this.mNotificationManager.createNotificationChannel(new NotificationChannel(OVP_ERR_CHANNEL_ID, name, 4));
        }
    }

    private void sendBatteryErrorNotification(int titleId, int messageId, int iconId) {
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
            build.addAction(new Action.Builder(null, this.mContext.getString(33685928), actionClickPI).build());
        }
        Notification notification = build.build();
        if (this.mNotificationManager != null) {
            this.mNotificationManager.notifyAsUser(null, titleId, notification, UserHandle.ALL);
        }
    }

    private void handleBatteryOvpStatus(String status) {
        if (!this.mBatteryOvpStatus.equals(status) && "1".equals(status)) {
            Slog.i(TAG, "battery ovp error occur, send notification before.");
            this.mBatteryOvpStatus = status;
            if (this.mHwBatteryHandler.hasMessages(2)) {
                this.mHwBatteryHandler.removeMessages(2);
            }
            this.mHwBatteryHandler.sendMessage(Message.obtain(this.mHwBatteryHandler, 2, status));
        }
    }

    private void handleBatteryIscdStatus() {
        if (this.mIsBootFinish) {
            if (this.mHwBatteryHandler.hasMessages(6)) {
                this.mHwBatteryHandler.removeMessages(6);
            }
            this.mHwBatteryHandler.sendMessage(Message.obtain(this.mHwBatteryHandler, 6));
        }
    }

    private String getQuickChargeBroadcastStatus(String fcpStatus, String scpStatus) {
        String status = "0";
        if (fcpStatus.equals("1") && scpStatus.equals("1")) {
            return "2";
        }
        if (fcpStatus.equals("1") && scpStatus.equals("0")) {
            return "1";
        }
        if (fcpStatus.equals("0") && scpStatus.equals("1")) {
            return "2";
        }
        return status;
    }

    protected void updateLight() {
        this.mHwLed.newUpdateLightsLocked();
    }

    protected void updateLight(boolean enable, int ledOnMS, int ledOffMS) {
        this.mIsNotificationExisting = enable;
        if (enable) {
            this.mNotificationLedOn = ledOnMS;
            this.mNotificationLedOff = ledOffMS;
        }
        Flog.i(1100, "updateLight --> mIsNotificationExisting: " + enable + " ledOnMS: " + ledOnMS + " ledOffMS :" + ledOffMS);
        this.mHwLed.newUpdateLightsLocked();
    }

    protected void playRing() {
        if (!FACTORY_VERSION.equalsIgnoreCase(SystemProperties.get(RUN_MODE_PROPERTY, "unknown"))) {
            if (((this.mCust != null ? this.mCust.mutePowerConnectedTone() : 0) ^ 1) != 0) {
                this.mHwBatteryHandler.post(new Runnable() {
                    public void run() {
                        HwBatteryService.this.mUri = HwBatteryService.queryRingMusicUri(HwBatteryService.this.mContext, HwBatteryService.PPWER_CONNECTED_RINGTONE);
                        HwBatteryService.this.mRingRingtone = HwBatteryService.this.playRing(HwBatteryService.this.mUri, HwBatteryService.this.mRingRingtone);
                    }
                });
            }
        }
    }

    protected void stopRing() {
        if (!FACTORY_VERSION.equalsIgnoreCase(SystemProperties.get(RUN_MODE_PROPERTY, "unknown"))) {
            this.mHwBatteryHandler.post(new Runnable() {
                public void run() {
                    HwBatteryService.this.stopRing(HwBatteryService.this.mRingRingtone);
                }
            });
        }
    }

    private Ringtone playRing(Uri uri, Ringtone ringtone) {
        ringtone = RingtoneManager.getRingtone(this.mContext, uri);
        if (ringtone != null) {
            ringtone.setAudioAttributes(this.mAudioAttributes);
            ringtone.play();
        }
        return ringtone;
    }

    private void stopRing(Ringtone ringtone) {
        if (ringtone != null) {
            ringtone.stop();
        }
    }

    private static Uri queryRingMusicUri(Context context, String fileName) {
        return queryRingMusicUri(context.getContentResolver(), fileName);
    }

    private static Uri queryRingMusicUri(ContentResolver resolver, String fileName) {
        if (fileName == null) {
            return null;
        }
        Uri uri = Media.INTERNAL_CONTENT_URI;
        String[] cols = new String[]{"_id"};
        StringBuilder where = new StringBuilder("_data like '%");
        where.append(fileName);
        where.append("'");
        Cursor cursor = null;
        try {
            cursor = resolver.query(uri, cols, where.toString(), null, null);
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
            Uri withAppendedId = ContentUris.withAppendedId(uri, (long) cursor.getInt(cursor.getColumnIndex("_id")));
            if (cursor != null) {
                cursor.close();
            }
            return withAppendedId;
        } catch (Exception e) {
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    protected void printBatteryLog(BatteryProperties oldProps, BatteryProperties newProps, int oldPlugType, boolean updatesStopped) {
        if (oldProps == null || newProps == null) {
            Flog.i(WifiProCommonUtils.RESP_CODE_REDIRECTED_HOST_CHANGED, "mBatteryProps or new battery values is null");
            return;
        }
        int plugType;
        if (newProps.chargerAcOnline) {
            plugType = 1;
        } else if (newProps.chargerUsbOnline) {
            plugType = 2;
        } else if (newProps.chargerWirelessOnline) {
            plugType = 4;
        } else {
            plugType = 0;
        }
        if (!(plugType == oldPlugType && oldProps.batteryLevel == newProps.batteryLevel)) {
            Flog.i(WifiProCommonUtils.RESP_CODE_REDIRECTED_HOST_CHANGED, "update battery new values: chargerAcOnline=" + newProps.chargerAcOnline + ", chargerUsbOnline=" + newProps.chargerUsbOnline + ", batteryStatus=" + newProps.batteryStatus + ", batteryHealth=" + newProps.batteryHealth + ", batteryPresent=" + newProps.batteryPresent + ", batteryLevel=" + newProps.batteryLevel + ", batteryTechnology=" + newProps.batteryTechnology + ", batteryVoltage=" + newProps.batteryVoltage + ", batteryTemperature=" + newProps.batteryTemperature + ", mUpdatesStopped=" + updatesStopped);
        }
    }

    private void sendBatteryIsdcErrorBroadcast() {
        Intent iscdErrorIntent = new Intent(ACTION_BATTERY_ISCD_ERROR);
        iscdErrorIntent.addFlags(1073741824);
        iscdErrorIntent.addFlags(536870912);
        iscdErrorIntent.setPackage("com.android.systemui");
        Slog.i(TAG, "Stick broadcast intent: " + iscdErrorIntent);
        this.mContext.sendStickyBroadcastAsUser(iscdErrorIntent, UserHandle.ALL);
    }
}
