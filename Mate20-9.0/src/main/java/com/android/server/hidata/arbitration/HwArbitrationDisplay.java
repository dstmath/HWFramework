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
import com.android.server.rms.iaware.memory.utils.MemoryConstant;

public class HwArbitrationDisplay {
    private static final String ACTION_WIFI_PLUS_ACTIVITY = "android.settings.WIFI_PLUS_SETTINGS";
    private static final int HiData_High_NOTIFICATION_ID = 288223;
    private static final int HiData_Low_NOTIFICATION_ID = 288224;
    private static final String NotificationHighChannelTAG = "hidata_brain_high_tag";
    private static final String NotificationLowChannelTAG = "hidata_brain_low_tag";
    /* access modifiers changed from: private */
    public static final String TAG = (HwArbitrationDEFS.BASE_TAG + HwArbitrationDisplay.class.getSimpleName());
    private static HwArbitrationDisplay instance;
    private Context mContext;
    private Handler mHBDHandler;
    /* access modifiers changed from: private */
    public boolean mIsSmartMpStart = false;
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
        HwArbitrationCommonUtils.logD(TAG, "init HwArbitrationDisplay");
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
    public void resetState() {
        HwArbitrationCommonUtils.logD(TAG, "resetState Notification State");
        this.noNotify25MB = true;
        this.noNotify50MB = true;
        this.noSetLowChanel = true;
        this.noSetHighChanel = true;
        this.mRO_StartRxBytes = 0;
        this.mRO_StartTxBytes = 0;
        this.startMonitorDataFlow = true;
    }

    public void requestDataMonitor(boolean enable, int id) {
        String str = TAG;
        HwArbitrationCommonUtils.logD(str, "enable = " + enable + ", id = " + id);
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
        HwArbitrationCommonUtils.logD(TAG, "startSmpMonitor");
        try {
            if (isSmartMpEnable() && !this.mIsSmartMpStart) {
                this.mIsSmartMpStart = true;
                requestDataMonitor(true, 2);
                this.mHBDHandler.sendEmptyMessage(HwArbitrationDEFS.MSG_Display_Start_Monitor_SmartMP);
            }
        } catch (Exception e) {
            HwArbitrationCommonUtils.logE(TAG, "startSmpMonitor error");
        }
    }

    private boolean isSmartMpEnable() {
        return EmcomManager.getInstance().isSmartMpEnable();
    }

    /* access modifiers changed from: private */
    public void smartMpMonitor() {
        try {
            if (!isSmartMpEnable()) {
                this.mHBDHandler.sendEmptyMessage(HwArbitrationDEFS.MSG_Display_Stop_Monitor_SmartMP);
            }
        } catch (Exception e) {
            HwArbitrationCommonUtils.logE(TAG, "smartMpMonitor error");
        }
    }

    /* access modifiers changed from: private */
    public void periodCheckHighDataFlow() {
        if (this.startMonitorDataFlow) {
            HwArbitrationCommonUtils.logD(TAG, "start Hidata_Notification monitor");
            this.mRO_StartRxBytes = TrafficStats.getMobileRxBytes();
            this.mRO_StartTxBytes = TrafficStats.getMobileTxBytes();
            if (this.mRO_StartTxBytes > 0 && this.mRO_StartRxBytes > 0) {
                this.startMonitorDataFlow = false;
            }
            String str = TAG;
            HwArbitrationCommonUtils.logD(str, "init mRO_StartTxBytes = " + this.mRO_StartTxBytes + ", mRO_StartRxBytes = " + this.mRO_StartRxBytes);
            return;
        }
        long rxBytes = TrafficStats.getMobileRxBytes();
        long txBytes = TrafficStats.getMobileTxBytes();
        if (rxBytes <= 0 || txBytes <= 0) {
            String str2 = TAG;
            HwArbitrationCommonUtils.logD(str2, "read rx tx error, rx=" + rxBytes + ", tx=" + txBytes);
            return;
        }
        String str3 = TAG;
        HwArbitrationCommonUtils.logD(str3, "rxBytes: " + rxBytes + ", txBytes: " + txBytes + ", mRO_StartRxBytes: " + this.mRO_StartRxBytes + ", mRO_StartTxBytes: " + this.mRO_StartTxBytes + ", CostBytes: " + ((((rxBytes - this.mRO_StartRxBytes) + txBytes) - this.mRO_StartTxBytes) / MemoryConstant.MB_SIZE) + "MB");
        if (rxBytes >= this.mRO_StartRxBytes && txBytes >= this.mRO_StartTxBytes) {
            long totalCostMB = ((rxBytes - this.mRO_StartRxBytes) + (txBytes - this.mRO_StartTxBytes)) / MemoryConstant.MB_SIZE;
            if (totalCostMB > 50 && this.noNotify50MB) {
                this.noNotify25MB = false;
                this.noNotify50MB = false;
                HwArbitrationCommonUtils.logD(TAG, "show Hidata_Notification: 50MB");
                showHiDataHighNotification(totalCostMB);
            } else if (totalCostMB > 25 && this.noNotify25MB) {
                this.noNotify25MB = false;
                HwArbitrationCommonUtils.logD(TAG, "show Hidata_Notification: 25MB");
                showHiDataHighNotification(totalCostMB);
            } else if (totalCostMB > 50) {
                String str4 = TAG;
                HwArbitrationCommonUtils.logD(str4, "show Hidata_Notification: " + totalCostMB + "MB");
                getInstance(this.mContext).showHiDataLowNotification(totalCostMB);
            }
        }
    }

    private Notification getNotification(long mobileDateSize, boolean isHigh) {
        Notification.Builder b;
        long j = mobileDateSize;
        String str = TAG;
        HwArbitrationCommonUtils.logD(str, "showHiDataNotification: " + j + "MB");
        String titleSrc = this.mContext.getResources().getString(33686117);
        String content = this.mContext.getResources().getString(33686116);
        String title = titleSrc.replace("%d", "" + j);
        String suitch = this.mContext.getResources().getString(33686115);
        PendingIntent roveInPendingIntent = PendingIntent.getBroadcastAsUser(this.mContext, 0, new Intent(HwArbitrationDEFS.ACTION_HiData_DATA_ROVE_IN).setPackage(this.mContext.getPackageName()), 268435456, UserHandle.ALL);
        PendingIntent activityPendingIntent = PendingIntent.getActivity(this.mContext, 1, new Intent(ACTION_WIFI_PLUS_ACTIVITY), 268435456);
        Notification.Action action = new Notification.Action.Builder(33752033, suitch, roveInPendingIntent).build();
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
        b.setSmallIcon(33752033);
        b.addAction(action);
        b.setOngoing(true);
        return b.build();
    }

    private void showHiDataHighNotification(long mobileDateSize) {
        if (this.mNotificationManager == null) {
            HwArbitrationCommonUtils.logE(TAG, "High NotificationManager is null!");
            return;
        }
        if (this.noSetHighChanel) {
            this.noSetHighChanel = false;
            this.mNotificationManager.deleteNotificationChannel(NotificationLowChannelTAG);
            NotificationChannel highNotificationChannel = new NotificationChannel(NotificationHighChannelTAG, this.mContext.getResources().getString(33686112), 4);
            highNotificationChannel.setSound(Settings.System.DEFAULT_NOTIFICATION_URI, Notification.AUDIO_ATTRIBUTES_DEFAULT);
            highNotificationChannel.enableLights(true);
            highNotificationChannel.enableVibration(true);
            this.mNotificationManager.createNotificationChannel(highNotificationChannel);
        }
        this.mNotificationManager.notify(HiData_High_NOTIFICATION_ID, getNotification(mobileDateSize, true));
    }

    /* access modifiers changed from: private */
    public void cancelHiDataHighNotification() {
        if (this.mNotificationManager == null) {
            HwArbitrationCommonUtils.logE(TAG, "mNotificationManager is null!");
            return;
        }
        HwArbitrationCommonUtils.logD(TAG, "cancel the High notification!");
        this.mNotificationManager.cancel(HiData_High_NOTIFICATION_ID);
    }

    private void showHiDataLowNotification(long mobileDateSize) {
        if (this.mNotificationManager == null) {
            HwArbitrationCommonUtils.logE(TAG, "High NotificationManager is null!");
            return;
        }
        if (this.noSetLowChanel) {
            this.noSetLowChanel = false;
            cancelHiDataHighNotification();
            this.mNotificationManager.deleteNotificationChannel(NotificationHighChannelTAG);
            this.mNotificationManager.createNotificationChannel(new NotificationChannel(NotificationLowChannelTAG, this.mContext.getResources().getString(33686112), 2));
        }
        this.mNotificationManager.notify(HiData_Low_NOTIFICATION_ID, getNotification(mobileDateSize, false));
    }

    /* access modifiers changed from: private */
    public void cancelHiDataLowNotification() {
        if (this.mNotificationManager == null) {
            HwArbitrationCommonUtils.logE(TAG, "mNotificationManager is null!");
            return;
        }
        HwArbitrationCommonUtils.logD(TAG, "cancel the Low notification!");
        this.mNotificationManager.cancel(HiData_Low_NOTIFICATION_ID);
    }

    private void initHBDHandler() {
        HandlerThread handlerThread = new HandlerThread("hidata_brain_display_handler");
        handlerThread.start();
        this.mHBDHandler = new Handler(handlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case HwArbitrationDEFS.MSG_Display_Start_Monitor_Network:
                        HwArbitrationCommonUtils.logD(HwArbitrationDisplay.TAG, "MSG_Display_Start_Monitor_Network");
                        HwArbitrationDisplay.this.periodCheckHighDataFlow();
                        sendEmptyMessageDelayed(HwArbitrationDEFS.MSG_Display_Start_Monitor_Network, 3000);
                        return;
                    case HwArbitrationDEFS.MSG_Display_stop_Monotor_network:
                        HwArbitrationCommonUtils.logD(HwArbitrationDisplay.TAG, "MSG_Display_stop_Monotor_network");
                        removeMessages(HwArbitrationDEFS.MSG_Display_Start_Monitor_Network);
                        HwArbitrationDisplay.this.cancelHiDataHighNotification();
                        HwArbitrationDisplay.this.cancelHiDataLowNotification();
                        HwArbitrationDisplay.this.resetState();
                        return;
                    case HwArbitrationDEFS.MSG_Display_Start_Monitor_SmartMP:
                        HwArbitrationCommonUtils.logD(HwArbitrationDisplay.TAG, "MSG_Display_Start_Monitor_SmartMP");
                        HwArbitrationDisplay.this.smartMpMonitor();
                        sendEmptyMessageDelayed(HwArbitrationDEFS.MSG_Display_Start_Monitor_SmartMP, 3000);
                        return;
                    case HwArbitrationDEFS.MSG_Display_Stop_Monitor_SmartMP:
                        HwArbitrationCommonUtils.logD(HwArbitrationDisplay.TAG, "MSG_Display_Stop_Monitor_SmartMP");
                        boolean unused = HwArbitrationDisplay.this.mIsSmartMpStart = false;
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
        HwArbitrationCommonUtils.logD(TAG, "show HiData_Toast");
        Toast.makeText(mContext2, info, 1).show();
    }
}
