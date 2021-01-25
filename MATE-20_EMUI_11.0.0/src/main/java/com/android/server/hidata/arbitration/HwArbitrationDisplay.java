package com.android.server.hidata.arbitration;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.emcom.EmcomManager;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.os.UserHandle;
import android.provider.Settings;
import android.widget.Toast;

public class HwArbitrationDisplay {
    private static final String ACTION_WIFI_PLUS_ACTIVITY = "android.settings.WIFI_PLUS_SETTINGS";
    private static final int HiData_High_NOTIFICATION_ID = 288223;
    private static final int HiData_Low_NOTIFICATION_ID = 288224;
    private static final String NotificationHighChannelTAG = "hidata_brain_high_tag";
    private static final String NotificationLowChannelTAG = "hidata_brain_low_tag";
    private static final String TAG = (HwArbitrationDEFS.BASE_TAG + HwArbitrationDisplay.class.getSimpleName());
    private static HwArbitrationDisplay instance;
    private Context mContext;
    private int mDataMonitorUid = 0;
    private Handler mHBDHandler;
    private boolean mIsSmartMpStart = false;
    private NotificationManager mNotificationManager;
    private long mRO_StartRxBytes;
    private long mRO_StartTxBytes;
    private boolean mplinkEnableState = false;
    private boolean noNotify25MB;
    private boolean noNotify50MB;
    private boolean noSetHighChanel;
    private boolean noSetLowChanel;
    private boolean smartmpEnableState = false;
    private boolean startMonitorDataFlow;

    private HwArbitrationDisplay(Context mContext2) {
        HwArbitrationCommonUtils.logD(TAG, false, "init HwArbitrationDisplay", new Object[0]);
        this.mContext = mContext2;
        resetState();
        initHBDHandler();
        this.mNotificationManager = (NotificationManager) mContext2.getSystemService("notification");
    }

    public static synchronized HwArbitrationDisplay getInstance(Context context) {
        HwArbitrationDisplay hwArbitrationDisplay;
        synchronized (HwArbitrationDisplay.class) {
            if (instance == null) {
                instance = new HwArbitrationDisplay(context);
            }
            hwArbitrationDisplay = instance;
        }
        return hwArbitrationDisplay;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetState() {
        HwArbitrationCommonUtils.logD(TAG, false, "resetState Notification State", new Object[0]);
        this.noNotify25MB = true;
        this.noNotify50MB = true;
        this.noSetLowChanel = true;
        this.noSetHighChanel = true;
        this.mRO_StartRxBytes = 0;
        this.mRO_StartTxBytes = 0;
        this.startMonitorDataFlow = true;
        this.mDataMonitorUid = 0;
    }

    public void requestDataMonitor(boolean enable, int id) {
        HwArbitrationCommonUtils.logD(TAG, false, "enable = %{public}s, id = %{public}d", String.valueOf(enable), Integer.valueOf(id));
        if (enable && !this.mplinkEnableState && !this.smartmpEnableState) {
            this.mHBDHandler.sendEmptyMessage(HwArbitrationDEFS.MSG_Display_Start_Monitor_Network);
        }
        if (1 == id) {
            this.mplinkEnableState = enable;
        }
        if (2 == id) {
            this.smartmpEnableState = enable;
        }
        if (!enable && !this.mplinkEnableState && !this.smartmpEnableState) {
            this.mHBDHandler.sendEmptyMessage(HwArbitrationDEFS.MSG_Display_stop_Monotor_network);
        }
    }

    public void startSmpMonitor() {
        HwArbitrationCommonUtils.logD(TAG, false, "startSmpMonitor", new Object[0]);
        try {
            if (isSmartMpEnable() && !this.mIsSmartMpStart) {
                this.mIsSmartMpStart = true;
                requestDataMonitor(true, 2);
                this.mHBDHandler.sendEmptyMessage(HwArbitrationDEFS.MSG_Display_Start_Monitor_SmartMP);
            }
        } catch (Exception e) {
            HwArbitrationCommonUtils.logE(TAG, false, "startSmpMonitor error", new Object[0]);
        }
    }

    private boolean isSmartMpEnable() {
        return EmcomManager.getInstance().isSmartMpEnable();
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void smartMpMonitor() {
        try {
            if (!isSmartMpEnable()) {
                this.mHBDHandler.sendEmptyMessage(HwArbitrationDEFS.MSG_Display_Stop_Monitor_SmartMP);
            }
        } catch (Exception e) {
            HwArbitrationCommonUtils.logE(TAG, false, "smartMpMonitor error", new Object[0]);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void periodCheckHighDataFlow() {
        if (this.startMonitorDataFlow) {
            HwArbitrationCommonUtils.logD(TAG, false, "start Hidata_Notification monitor", new Object[0]);
            this.mRO_StartRxBytes = TrafficStats.getMobileRxBytes();
            this.mRO_StartTxBytes = TrafficStats.getMobileTxBytes();
            if (this.mRO_StartTxBytes > 0 && this.mRO_StartRxBytes > 0) {
                this.startMonitorDataFlow = false;
            }
            HwArbitrationCommonUtils.logD(TAG, false, "init mRO_StartTxBytes = %{public}s, mRO_StartRxBytes = %{public}s", String.valueOf(this.mRO_StartTxBytes), String.valueOf(this.mRO_StartRxBytes));
            return;
        }
        long rxBytes = TrafficStats.getMobileRxBytes();
        long txBytes = TrafficStats.getMobileTxBytes();
        if (rxBytes <= 0 || txBytes <= 0) {
            HwArbitrationCommonUtils.logD(TAG, false, "read rx tx error, rx=%{public}s, tx=%{public}s", String.valueOf(rxBytes), String.valueOf(txBytes));
            return;
        }
        HwArbitrationCommonUtils.logD(TAG, false, "rxBytes: %{public}s, txBytes: %{public}s, mRO_StartRxBytes: %{public}s, mRO_StartTxBytes: %{public}s, CostBytes: %{public}s MB", String.valueOf(rxBytes), String.valueOf(txBytes), String.valueOf(this.mRO_StartRxBytes), String.valueOf(this.mRO_StartTxBytes), String.valueOf((((rxBytes - this.mRO_StartRxBytes) + txBytes) - this.mRO_StartTxBytes) / 1048576));
        long j = this.mRO_StartRxBytes;
        if (rxBytes >= j) {
            long j2 = this.mRO_StartTxBytes;
            if (txBytes >= j2) {
                long totalCostMB = ((rxBytes - j) + (txBytes - j2)) / 1048576;
                if (totalCostMB > 50 && this.noNotify50MB) {
                    this.noNotify25MB = false;
                    this.noNotify50MB = false;
                    HwArbitrationCommonUtils.logD(TAG, false, "show Hidata_Notification: 50MB", new Object[0]);
                    showHiDataHighNotification(totalCostMB);
                } else if (totalCostMB > 25 && this.noNotify25MB) {
                    this.noNotify25MB = false;
                    HwArbitrationCommonUtils.logD(TAG, false, "show Hidata_Notification: 25MB", new Object[0]);
                    showHiDataHighNotification(totalCostMB);
                } else if (totalCostMB > 50) {
                    HwArbitrationCommonUtils.logD(TAG, false, "show Hidata_Notification: %{public}s MB", String.valueOf(totalCostMB));
                    getInstance(this.mContext).showHiDataLowNotification(totalCostMB);
                }
            }
        }
    }

    private Notification getNotification(long mobileDateSize, boolean isHigh) {
        Notification.Builder b;
        HwArbitrationCommonUtils.logD(TAG, false, "showHiDataNotification: %{public}s MB", String.valueOf(mobileDateSize));
        String titleSrc = this.mContext.getResources().getString(33685755);
        String content = this.mContext.getResources().getString(33685753);
        String title = titleSrc.replace("%d", "" + mobileDateSize);
        String suitch = this.mContext.getResources().getString(33685752);
        PendingIntent roveInPendingIntent = PendingIntent.getBroadcastAsUser(this.mContext, 0, new Intent(HwArbitrationDEFS.ACTION_HiData_DATA_ROVE_IN).setPackage(this.mContext.getPackageName()), 268435456, UserHandle.ALL);
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this.mContext, 1, new Intent(ACTION_WIFI_PLUS_ACTIVITY), 268435456);
        Notification.Action action = new Notification.Action.Builder(33752008, suitch, roveInPendingIntent).build();
        if (isHigh) {
            b = new Notification.Builder(this.mContext, NotificationHighChannelTAG);
        } else {
            b = new Notification.Builder(this.mContext, NotificationLowChannelTAG);
        }
        b.setContentIntent(activityPendingIntent);
        b.setAutoCancel(true);
        b.setUsesChronometer(true);
        b.setContentTitle(title);
        b.setContentText(content);
        b.setVisibility(1);
        b.setTicker("");
        b.setShowWhen(true);
        b.setUsesChronometer(false);
        b.setSmallIcon(33752008);
        b.addAction(action);
        b.setOngoing(true);
        return b.build();
    }

    private void showHiDataHighNotification(long mobileDateSize) {
        if (this.mNotificationManager == null) {
            HwArbitrationCommonUtils.logE(TAG, false, "High NotificationManager is null!", new Object[0]);
        } else if (HwArbitrationFunction.isAppLinkTurboEnabled(this.mContext, this.mDataMonitorUid)) {
            HwArbitrationCommonUtils.logD(TAG, false, "link turbo is enabled, not show the high notification", new Object[0]);
        } else {
            if (this.noSetHighChanel) {
                this.noSetHighChanel = false;
                this.mNotificationManager.deleteNotificationChannel(NotificationLowChannelTAG);
                NotificationChannel highNotificationChannel = new NotificationChannel(NotificationHighChannelTAG, this.mContext.getResources().getString(33685748), 4);
                highNotificationChannel.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, Notification.AUDIO_ATTRIBUTES_DEFAULT);
                highNotificationChannel.enableLights(true);
                highNotificationChannel.enableVibration(true);
                this.mNotificationManager.createNotificationChannel(highNotificationChannel);
            }
            this.mNotificationManager.notify(HiData_High_NOTIFICATION_ID, getNotification(mobileDateSize, true));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cancelHiDataHighNotification() {
        if (this.mNotificationManager == null) {
            HwArbitrationCommonUtils.logE(TAG, false, "mNotificationManager is null!", new Object[0]);
            return;
        }
        HwArbitrationCommonUtils.logD(TAG, false, "cancel the High notification!", new Object[0]);
        this.mNotificationManager.cancel(HiData_High_NOTIFICATION_ID);
    }

    private void showHiDataLowNotification(long mobileDateSize) {
        if (this.mNotificationManager == null) {
            HwArbitrationCommonUtils.logE(TAG, false, "High NotificationManager is null!", new Object[0]);
        } else if (HwArbitrationFunction.isAppLinkTurboEnabled(this.mContext, this.mDataMonitorUid)) {
            HwArbitrationCommonUtils.logD(TAG, false, "link turbo is enabled, not show the low notification", new Object[0]);
        } else {
            if (this.noSetLowChanel) {
                this.noSetLowChanel = false;
                cancelHiDataHighNotification();
                this.mNotificationManager.deleteNotificationChannel(NotificationHighChannelTAG);
                this.mNotificationManager.createNotificationChannel(new NotificationChannel(NotificationLowChannelTAG, this.mContext.getResources().getString(33685748), 2));
            }
            this.mNotificationManager.notify(HiData_Low_NOTIFICATION_ID, getNotification(mobileDateSize, false));
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void cancelHiDataLowNotification() {
        if (this.mNotificationManager == null) {
            HwArbitrationCommonUtils.logE(TAG, false, "mNotificationManager is null!", new Object[0]);
            return;
        }
        HwArbitrationCommonUtils.logD(TAG, false, "cancel the Low notification!", new Object[0]);
        this.mNotificationManager.cancel(HiData_Low_NOTIFICATION_ID);
    }

    private void initHBDHandler() {
        HandlerThread handlerThread = new HandlerThread("hidata_brain_display_handler");
        handlerThread.start();
        this.mHBDHandler = new Handler(handlerThread.getLooper()) {
            /* class com.android.server.hidata.arbitration.HwArbitrationDisplay.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case HwArbitrationDEFS.MSG_Display_Start_Monitor_Network /* 3002 */:
                        HwArbitrationCommonUtils.logD(HwArbitrationDisplay.TAG, false, "MSG_Display_Start_Monitor_Network", new Object[0]);
                        HwArbitrationDisplay.this.periodCheckHighDataFlow();
                        sendEmptyMessageDelayed(HwArbitrationDEFS.MSG_Display_Start_Monitor_Network, 3000);
                        return;
                    case HwArbitrationDEFS.MSG_Display_stop_Monotor_network /* 3003 */:
                        HwArbitrationCommonUtils.logD(HwArbitrationDisplay.TAG, false, "MSG_Display_stop_Monotor_network", new Object[0]);
                        removeMessages(HwArbitrationDEFS.MSG_Display_Start_Monitor_Network);
                        HwArbitrationDisplay.this.cancelHiDataHighNotification();
                        HwArbitrationDisplay.this.cancelHiDataLowNotification();
                        HwArbitrationDisplay.this.resetState();
                        return;
                    case HwArbitrationDEFS.MSG_Display_Start_Monitor_SmartMP /* 3004 */:
                        HwArbitrationCommonUtils.logD(HwArbitrationDisplay.TAG, false, "MSG_Display_Start_Monitor_SmartMP", new Object[0]);
                        HwArbitrationDisplay.this.smartMpMonitor();
                        sendEmptyMessageDelayed(HwArbitrationDEFS.MSG_Display_Start_Monitor_SmartMP, 3000);
                        return;
                    case HwArbitrationDEFS.MSG_Display_Stop_Monitor_SmartMP /* 3005 */:
                        HwArbitrationCommonUtils.logD(HwArbitrationDisplay.TAG, false, "MSG_Display_Stop_Monitor_SmartMP", new Object[0]);
                        HwArbitrationDisplay.this.mIsSmartMpStart = false;
                        HwArbitrationDisplay.this.requestDataMonitor(false, 2);
                        removeMessages(HwArbitrationDEFS.MSG_Display_Start_Monitor_SmartMP);
                        return;
                    default:
                        return;
                }
            }
        };
    }

    public static void setToast(Context mContext2, String info) {
        HwArbitrationCommonUtils.logD(TAG, false, "show HiData_Toast", new Object[0]);
        Toast.makeText(mContext2, info, 1).show();
    }

    public void setDataMonitorUid(int dataMonitorUid) {
        this.mDataMonitorUid = dataMonitorUid;
    }
}
