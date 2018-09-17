package com.android.server;

import android.app.Notification;
import android.app.Notification.Action;
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
import com.android.server.jankshield.TableJankEvent;
import com.android.server.lights.Light;
import com.android.server.lights.LightsManager;
import com.android.server.wifipro.WifiProCommonUtils;
import huawei.cust.HwCustUtils;
import java.io.File;

public final class HwBatteryService extends BatteryService {
    private static final String ACTION_CALL_BNT_CLICKED = "huawei.intent.action.CALL_BNT_CLICKED";
    private static final String ACTION_QUICK_CHARGE = "huawei.intent.action.BATTERY_QUICK_CHARGE";
    private static final String BATTERY_OVP_STATUS_ERROR = "1";
    private static final String BATTERY_OVP_STATUS_NORMAL = "0";
    private static final String FACTORY_VERSION = "factory";
    private static final String HUAWEI_CHINA_CUSTOMER_SERVICE_HOTLINE_NUMBER = "4008308300";
    public static final int LOW_BATTERY_SHUTDOWN_LEVEL = 2;
    public static final int LOW_BATTERY_WARNING_LEVEL = 4;
    private static final int MAX_BATTERY_PROP_REGISTER_TIMES = 5;
    private static final int MSG_BATTERY_OVP_ERROR = 2;
    private static final int MSG_BATTERY_PROP_REGISTER = 1;
    private static final int MSG_UPDATE_QUICK_CHARGE_STATE = 3;
    private static final String OVP_ERR_NOTIFICATION_ID = "n_id";
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
    private static final boolean isChinaRegion;
    private AudioAttributes mAudioAttributes;
    private final UEventObserver mBatteryFcpObserver;
    private String mBatteryOvpStatus;
    private int mBatteryPropRegisterTryTimes;
    private final Context mContext;
    private HwCustBatteryService mCust;
    private int mFlashingARGB;
    private final HwBatteryHandler mHwBatteryHandler;
    private final HandlerThread mHwBatteryThread;
    private HwLed mHwLed;
    private boolean mIsNotificationExisting;
    private int mNotificationLedOff;
    private int mNotificationLedOn;
    private NotificationManager mNotificationManager;
    private String mQuickChargeStatus;
    private BroadcastReceiver mReceiver;
    private Ringtone mRingRingtone;
    private Uri mUri;

    private class HealthdDeathRecipient implements DeathRecipient {
        private IBinder mCb;

        HealthdDeathRecipient(IBinder cb) {
            if (cb != null) {
                try {
                    Slog.i(HwBatteryService.TAG, "linkToDeath Healthd.");
                    cb.linkToDeath(this, HwBatteryService.PLUGGED_NONE);
                } catch (RemoteException e) {
                    Slog.w(HwBatteryService.TAG, "HealthdDeathRecipient() could not link to " + cb + " binder death");
                }
            }
            this.mCb = cb;
        }

        public void binderDied() {
            Slog.w(HwBatteryService.TAG, "Healthd died.");
            if (this.mCb != null) {
                this.mCb.unlinkToDeath(this, HwBatteryService.PLUGGED_NONE);
            }
            HwBatteryService.this.mBatteryPropRegisterTryTimes = HwBatteryService.PLUGGED_NONE;
            HwBatteryService.this.mHwBatteryHandler.sendEmptyMessageDelayed(HwBatteryService.MSG_BATTERY_PROP_REGISTER, TableJankEvent.recMAXCOUNT);
        }
    }

    private final class HwBatteryHandler extends Handler {
        public HwBatteryHandler(Looper looper) {
            super(looper);
            try {
                updateQuickChargeState(HwBatteryService.this.getQuickChargeBroadcastStatus(FileUtils.readTextFile(new File(HwBatteryService.this.getQuickChargeStatePath()), HwBatteryService.PLUGGED_NONE, null).trim(), FileUtils.readTextFile(new File(HwBatteryService.this.getDCQuickChargeStatePath()), HwBatteryService.PLUGGED_NONE, null).trim()));
                HwBatteryService.this.handleBatteryOvpStatus(FileUtils.readTextFile(new File(HwBatteryService.this.getBatteryOvpStatePath()), HwBatteryService.PLUGGED_NONE, null).trim());
            } catch (Exception e) {
                Slog.e(HwBatteryService.TAG, "Error get initialized state.", e);
            }
            HwBatteryService.this.mBatteryFcpObserver.startObserving("SUBSYSTEM=power_supply");
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HwBatteryService.MSG_BATTERY_PROP_REGISTER /*1*/:
                    IBinder binder = ServiceManager.checkService("batteryproperties");
                    if (binder == null || !binder.isBinderAlive()) {
                        HwBatteryService hwBatteryService = HwBatteryService.this;
                        hwBatteryService.mBatteryPropRegisterTryTimes = hwBatteryService.mBatteryPropRegisterTryTimes + HwBatteryService.MSG_BATTERY_PROP_REGISTER;
                        if (HwBatteryService.this.mBatteryPropRegisterTryTimes < HwBatteryService.MAX_BATTERY_PROP_REGISTER_TIMES) {
                            Slog.i(HwBatteryService.TAG, "Try to get batteryproperties service again.");
                            HwBatteryService.this.mHwBatteryHandler.sendEmptyMessageDelayed(HwBatteryService.MSG_BATTERY_PROP_REGISTER, TableJankEvent.recMAXCOUNT);
                            return;
                        }
                        Slog.e(HwBatteryService.TAG, "There is no connection between batteryservice and batteryproperties.");
                        return;
                    }
                    HwBatteryService.this.mBatteryPropRegisterTryTimes = HwBatteryService.PLUGGED_NONE;
                    HealthdDeathRecipient healthdDeathRecipient = new HealthdDeathRecipient(binder);
                    try {
                        Stub.asInterface(binder).registerListener(new BatteryListener(HwBatteryService.this));
                    } catch (RemoteException e) {
                    }
                case HwBatteryService.MSG_BATTERY_OVP_ERROR /*2*/:
                    HwBatteryService.this.sendBatteryOvpErrorNotification();
                case HwBatteryService.MSG_UPDATE_QUICK_CHARGE_STATE /*3*/:
                    String status = msg.obj;
                    if (!HwBatteryService.this.mQuickChargeStatus.equals(status)) {
                        HwBatteryService.this.mQuickChargeStatus = status;
                        sendQuickChargeBroadcast();
                    }
                default:
            }
        }

        public void updateQuickChargeState(String state) {
            removeMessages(HwBatteryService.MSG_UPDATE_QUICK_CHARGE_STATE);
            Message msg = Message.obtain(this, HwBatteryService.MSG_UPDATE_QUICK_CHARGE_STATE);
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
            this.mBatteryLight = lights.getLight(HwBatteryService.MSG_UPDATE_QUICK_CHARGE_STATE);
            this.mBatteryLowARGB = context.getResources().getInteger(17694811);
            this.mBatteryMediumARGB = context.getResources().getInteger(17694812);
            this.mBatteryFullARGB = context.getResources().getInteger(17694813);
            this.mBatteryLedOn = context.getResources().getInteger(17694814);
            this.mBatteryLedOff = context.getResources().getInteger(17694815);
        }

        public void newUpdateLightsLocked() {
            int level = HwBatteryService.this.getBatteryProps().batteryLevel;
            int status = HwBatteryService.this.getBatteryProps().batteryStatus;
            Flog.i(1100, "updateLightsLocked --> level:" + level + ", status:" + status);
            if (HwBatteryService.this.mIsNotificationExisting) {
                if (level < HwBatteryService.this.getLowBatteryWarningLevel()) {
                    if (status == HwBatteryService.MSG_BATTERY_OVP_ERROR) {
                        if (HwBatteryService.this.mFlashingARGB != this.mBatteryLowARGB) {
                            this.mBatteryLight.turnOff();
                            Flog.i(1100, "updateLightsLocked --> level:" + HwBatteryService.this.getBatteryProps().batteryLevel + "-- Flashing red");
                            this.mBatteryLight.setFlashing(this.mBatteryLowARGB, HwBatteryService.MSG_BATTERY_PROP_REGISTER, HwBatteryService.this.mNotificationLedOn, HwBatteryService.this.mNotificationLedOff);
                            HwBatteryService.this.mFlashingARGB = this.mBatteryLowARGB;
                        }
                    } else if (level <= HwBatteryService.LOW_BATTERY_WARNING_LEVEL && level > HwBatteryService.MSG_BATTERY_OVP_ERROR) {
                        Flog.i(1100, "updateLightsLocked --> level:" + HwBatteryService.this.getBatteryProps().batteryLevel + "-- Solid red");
                        this.mBatteryLight.setColor(this.mBatteryLowARGB);
                        HwBatteryService.this.mFlashingARGB = HwBatteryService.PLUGGED_NONE;
                    } else if (level > HwBatteryService.MSG_BATTERY_OVP_ERROR) {
                        Flog.i(1100, "updateLightsLocked --> mBatteryLight.turnOff");
                        this.mBatteryLight.turnOff();
                        HwBatteryService.this.mFlashingARGB = HwBatteryService.PLUGGED_NONE;
                    } else if (HwBatteryService.this.mFlashingARGB != HwBatteryService.SHUTDOWN_LEVEL_FLASHINGARGB) {
                        this.mBatteryLight.turnOff();
                        Flog.i(1100, "updateLightsLocked --> level:" + HwBatteryService.this.getBatteryProps().batteryLevel + "--Flashing red SHUTDOWN_LEVEL");
                        this.mBatteryLight.setFlashing(this.mBatteryLowARGB, HwBatteryService.MSG_BATTERY_PROP_REGISTER, this.mBatteryLedOn, this.mBatteryLedOff);
                        HwBatteryService.this.mFlashingARGB = HwBatteryService.SHUTDOWN_LEVEL_FLASHINGARGB;
                    }
                } else if (status != HwBatteryService.MSG_BATTERY_OVP_ERROR && status != HwBatteryService.MAX_BATTERY_PROP_REGISTER_TIMES) {
                    Flog.i(1100, "updateLightsLocked --> mBatteryLight.turnOff");
                    this.mBatteryLight.turnOff();
                    HwBatteryService.this.mFlashingARGB = HwBatteryService.PLUGGED_NONE;
                } else if (status == HwBatteryService.MAX_BATTERY_PROP_REGISTER_TIMES || level >= 90) {
                    if (HwBatteryService.this.mFlashingARGB != this.mBatteryFullARGB) {
                        this.mBatteryLight.turnOff();
                        Flog.i(1100, "updateLightsLocked --> level:" + HwBatteryService.this.getBatteryProps().batteryLevel + "-- Flashing green");
                        this.mBatteryLight.setFlashing(this.mBatteryFullARGB, HwBatteryService.MSG_BATTERY_PROP_REGISTER, HwBatteryService.this.mNotificationLedOn, HwBatteryService.this.mNotificationLedOff);
                        HwBatteryService.this.mFlashingARGB = this.mBatteryFullARGB;
                    }
                } else if (HwBatteryService.this.mFlashingARGB != this.mBatteryMediumARGB) {
                    this.mBatteryLight.turnOff();
                    Flog.i(1100, "updateLightsLocked --> level:" + HwBatteryService.this.getBatteryProps().batteryLevel + "-- Flashing mBatteryMediumARGB");
                    this.mBatteryLight.setFlashing(this.mBatteryMediumARGB, HwBatteryService.MSG_BATTERY_PROP_REGISTER, HwBatteryService.this.mNotificationLedOn, HwBatteryService.this.mNotificationLedOff);
                    HwBatteryService.this.mFlashingARGB = this.mBatteryMediumARGB;
                }
            } else if (level < HwBatteryService.this.getLowBatteryWarningLevel()) {
                if (status == HwBatteryService.MSG_BATTERY_OVP_ERROR) {
                    Flog.i(1100, "updateLightsLocked --> level:" + HwBatteryService.this.getBatteryProps().batteryLevel + "-- Solid red");
                    this.mBatteryLight.setColor(this.mBatteryLowARGB);
                    HwBatteryService.this.mFlashingARGB = HwBatteryService.PLUGGED_NONE;
                } else if (level <= HwBatteryService.LOW_BATTERY_WARNING_LEVEL && level > HwBatteryService.MSG_BATTERY_OVP_ERROR) {
                    Flog.i(1100, "updateLightsLocked --> level:" + HwBatteryService.this.getBatteryProps().batteryLevel + "-- Solid red");
                    this.mBatteryLight.setColor(this.mBatteryLowARGB);
                    HwBatteryService.this.mFlashingARGB = HwBatteryService.PLUGGED_NONE;
                } else if (level > HwBatteryService.MSG_BATTERY_OVP_ERROR) {
                    Flog.i(1100, "updateLightsLocked --> mBatteryLight.turnOff");
                    this.mBatteryLight.turnOff();
                    HwBatteryService.this.mFlashingARGB = HwBatteryService.PLUGGED_NONE;
                } else if (HwBatteryService.this.mFlashingARGB != HwBatteryService.SHUTDOWN_LEVEL_FLASHINGARGB) {
                    this.mBatteryLight.turnOff();
                    Flog.i(1100, "updateLightsLocked --> level:" + HwBatteryService.this.getBatteryProps().batteryLevel + "-- Flashing red SHUTDOWN_LEVEL");
                    this.mBatteryLight.setFlashing(this.mBatteryLowARGB, HwBatteryService.MSG_BATTERY_PROP_REGISTER, this.mBatteryLedOn, this.mBatteryLedOff);
                    HwBatteryService.this.mFlashingARGB = HwBatteryService.SHUTDOWN_LEVEL_FLASHINGARGB;
                }
            } else if (status != HwBatteryService.MSG_BATTERY_OVP_ERROR && status != HwBatteryService.MAX_BATTERY_PROP_REGISTER_TIMES) {
                Flog.i(1100, "updateLightsLocked --> mBatteryLight.turnOff");
                this.mBatteryLight.turnOff();
                HwBatteryService.this.mFlashingARGB = HwBatteryService.PLUGGED_NONE;
            } else if (status == HwBatteryService.MAX_BATTERY_PROP_REGISTER_TIMES || level >= 90) {
                Flog.i(1100, "updateLightsLocked --> level:" + HwBatteryService.this.getBatteryProps().batteryLevel + "-- Solid green");
                this.mBatteryLight.setColor(this.mBatteryFullARGB);
                HwBatteryService.this.mFlashingARGB = HwBatteryService.PLUGGED_NONE;
            } else {
                Flog.i(1100, "updateLightsLocked --> level:" + HwBatteryService.this.getBatteryProps().batteryLevel + " -- Solid mBatteryMediumARGB");
                this.mBatteryLight.setColor(this.mBatteryMediumARGB);
                HwBatteryService.this.mFlashingARGB = HwBatteryService.PLUGGED_NONE;
            }
        }
    }

    static {
        isChinaRegion = SystemProperties.get("ro.config.hw_optb", QUICK_CHARGE_STATUS_NORMAL).equals("156");
    }

    public HwBatteryService(Context context) {
        super(context);
        this.mBatteryOvpStatus = QUICK_CHARGE_STATUS_NORMAL;
        this.mQuickChargeStatus = QUICK_CHARGE_STATUS_NORMAL;
        this.mCust = (HwCustBatteryService) HwCustUtils.createObj(HwCustBatteryService.class, new Object[PLUGGED_NONE]);
        this.mBatteryPropRegisterTryTimes = PLUGGED_NONE;
        this.mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (context == null || intent == null) {
                    Slog.i(HwBatteryService.TAG, "context or intent is null!");
                    return;
                }
                Slog.i(HwBatteryService.TAG, "intent = " + intent);
                if (HwBatteryService.ACTION_CALL_BNT_CLICKED.equals(intent.getAction())) {
                    HwBatteryService.this.closeSystemDialogs(context);
                    int notificationID = intent.getIntExtra(HwBatteryService.OVP_ERR_NOTIFICATION_ID, HwBatteryService.PLUGGED_NONE);
                    if (HwBatteryService.this.mNotificationManager != null) {
                        HwBatteryService.this.mNotificationManager.cancelAsUser(null, notificationID, UserHandle.CURRENT);
                    }
                    Intent callIntent = new Intent("android.intent.action.CALL");
                    if (HwBatteryService.isChinaRegion) {
                        callIntent.setData(Uri.parse("tel:4008308300"));
                    }
                    callIntent.setFlags(276824064);
                    context.startActivityAsUser(callIntent, UserHandle.CURRENT);
                }
            }
        };
        this.mBatteryFcpObserver = new UEventObserver() {
            public void onUEvent(UEvent event) {
                String fcpStatus = event.get("POWER_SUPPLY_FCP_STATUS", HwBatteryService.QUICK_CHARGE_STATUS_NORMAL);
                String scpStatus = event.get("POWER_SUPPLY_SCP_STATUS", HwBatteryService.QUICK_CHARGE_STATUS_NORMAL);
                String ovpStatus = event.get("POWER_SUPPLY_BAT_OVP", HwBatteryService.QUICK_CHARGE_STATUS_NORMAL);
                Slog.d(HwBatteryService.TAG, "onUEvent fcpStatus = " + fcpStatus + ",scpStatus =" + scpStatus + ",ovpStatus=" + ovpStatus);
                HwBatteryService.this.mHwBatteryHandler.updateQuickChargeState(HwBatteryService.this.getQuickChargeBroadcastStatus(fcpStatus, scpStatus));
                HwBatteryService.this.handleBatteryOvpStatus(ovpStatus);
            }
        };
        this.mContext = context;
        initAndRegisterReceiver();
        this.mHwBatteryThread = new HandlerThread(TAG);
        this.mHwBatteryThread.start();
        this.mHwBatteryHandler = new HwBatteryHandler(this.mHwBatteryThread.getLooper());
        this.mHwLed = new HwLed(context, (LightsManager) getLocalService(LightsManager.class));
        this.mAudioAttributes = new Builder().setUsage(13).setContentType(LOW_BATTERY_WARNING_LEVEL).build();
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

    private void initAndRegisterReceiver() {
        this.mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_CALL_BNT_CLICKED);
        this.mContext.registerReceiver(this.mReceiver, filter);
    }

    private void closeSystemDialogs(Context context) {
        context.sendBroadcastAsUser(new Intent("android.intent.action.CLOSE_SYSTEM_DIALOGS"), UserHandle.ALL);
    }

    private void sendBatteryOvpErrorNotification() {
        Slog.i(TAG, "sendBatteryOvpErrorNotification");
        String title = this.mContext.getResources().getString(33685898);
        CharSequence message = this.mContext.getResources().getString(33685899);
        Intent dialIntent = new Intent("android.intent.action.DIAL");
        if (isChinaRegion) {
            dialIntent.setData(Uri.parse("tel:4008308300"));
        }
        PendingIntent pi = PendingIntent.getActivityAsUser(this.mContext, PLUGGED_NONE, dialIntent, PLUGGED_NONE, null, UserHandle.CURRENT);
        Intent intent = new Intent(ACTION_CALL_BNT_CLICKED);
        intent.putExtra(OVP_ERR_NOTIFICATION_ID, 33685898);
        intent.setPackage(this.mContext.getPackageName());
        PendingIntent actionClickPI = PendingIntent.getBroadcast(this.mContext, PLUGGED_NONE, intent, PLUGGED_NONE);
        Notification.Builder build = new Notification.Builder(this.mContext).setSmallIcon(33751168).setOngoing(true).setContentTitle(title).setContentText(message).setContentIntent(pi).setTicker(title).setVibrate(new long[PLUGGED_NONE]).setPriority(MSG_BATTERY_OVP_ERROR).setWhen(System.currentTimeMillis()).setShowWhen(true).setVisibility(MSG_BATTERY_PROP_REGISTER).setAutoCancel(true);
        if (isChinaRegion) {
            build.addAction(new Action.Builder(null, this.mContext.getString(33685900), actionClickPI).build());
        }
        Notification notification = build.build();
        if (this.mNotificationManager != null) {
            this.mNotificationManager.notifyAsUser(null, 33685898, notification, UserHandle.ALL);
        }
    }

    private void handleBatteryOvpStatus(String status) {
        if (!this.mBatteryOvpStatus.equals(status) && QUICK_CHARGE_STATUS_WORKING.equals(status)) {
            Slog.i(TAG, "battery ovp error occur, send notification before.");
            this.mBatteryOvpStatus = status;
            if (this.mHwBatteryHandler.hasMessages(MSG_BATTERY_OVP_ERROR)) {
                this.mHwBatteryHandler.removeMessages(MSG_BATTERY_OVP_ERROR);
            }
            this.mHwBatteryHandler.sendMessage(Message.obtain(this.mHwBatteryHandler, MSG_BATTERY_OVP_ERROR, status));
        }
    }

    private String getQuickChargeBroadcastStatus(String fcpStatus, String scpStatus) {
        String status = QUICK_CHARGE_STATUS_NORMAL;
        if (fcpStatus.equals(QUICK_CHARGE_STATUS_WORKING) && scpStatus.equals(QUICK_CHARGE_STATUS_WORKING)) {
            return QUICK_CHARGE_SCP_STATUS;
        }
        if (fcpStatus.equals(QUICK_CHARGE_STATUS_WORKING) && scpStatus.equals(QUICK_CHARGE_STATUS_NORMAL)) {
            return QUICK_CHARGE_STATUS_WORKING;
        }
        if (fcpStatus.equals(QUICK_CHARGE_STATUS_NORMAL) && scpStatus.equals(QUICK_CHARGE_STATUS_WORKING)) {
            return QUICK_CHARGE_SCP_STATUS;
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
            if (this.mCust == null || !this.mCust.mutePowerConnectedTone()) {
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
        String[] cols = new String[MSG_BATTERY_PROP_REGISTER];
        cols[PLUGGED_NONE] = "_id";
        StringBuilder where = new StringBuilder("_data like '%");
        where.append(fileName).append("'");
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
            plugType = MSG_BATTERY_PROP_REGISTER;
        } else if (newProps.chargerUsbOnline) {
            plugType = MSG_BATTERY_OVP_ERROR;
        } else if (newProps.chargerWirelessOnline) {
            plugType = LOW_BATTERY_WARNING_LEVEL;
        } else {
            plugType = PLUGGED_NONE;
        }
        if (!(plugType == oldPlugType && oldProps.batteryLevel == newProps.batteryLevel)) {
            Flog.i(WifiProCommonUtils.RESP_CODE_REDIRECTED_HOST_CHANGED, "update battery new values: chargerAcOnline=" + newProps.chargerAcOnline + ", chargerUsbOnline=" + newProps.chargerUsbOnline + ", batteryStatus=" + newProps.batteryStatus + ", batteryHealth=" + newProps.batteryHealth + ", batteryPresent=" + newProps.batteryPresent + ", batteryLevel=" + newProps.batteryLevel + ", batteryTechnology=" + newProps.batteryTechnology + ", batteryVoltage=" + newProps.batteryVoltage + ", batteryTemperature=" + newProps.batteryTemperature + ", mUpdatesStopped=" + updatesStopped);
        }
    }
}
