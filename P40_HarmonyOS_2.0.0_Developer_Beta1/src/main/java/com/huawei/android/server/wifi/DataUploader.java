package com.huawei.android.server.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.util.Log;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DataUploader {
    public static final int BASE = 3;
    private static final boolean IS_DEBUG = ((Log.HWLog || (Log.HWModuleLog && Log.isLoggable(TAG, 3))) ? true : IS_DEBUG);
    private static final Object LOCK = new Object();
    public static final String NEED_TO_UPLOAD = "surfing_upload_date";
    private static final long ONE_DAY = 86400000;
    private static final int PER_MINUTE = 60;
    private static final int PER_SECOND = 1000;
    public static final String SURFING_TIME = "wifi_surfing_time";
    private static final String TAG = "DataUploader";
    public static final int WIFI_CONNECTION_ACTION = 6;
    public static final int WIFI_DISCONNECT = 5;
    public static final int WIFI_OPERATION_INFO = 4;
    public static final int WIFI_SURFING = 3;
    private static volatile DataUploader sDataUploader = null;
    private Context mContext = null;
    private ScheduledExecutorService mExecutor = Executors.newScheduledThreadPool(1);
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        /* class com.huawei.android.server.wifi.DataUploader.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
                if (DataUploader.IS_DEBUG) {
                    Log.d(DataUploader.TAG, "receive BOOT_COMPLETED action and begin upload data and set up timer");
                }
                DataUploader.this.uploadData();
                DataUploader.this.executePerDay();
            }
        }
    };
    private ReportTool mReportTool = null;
    private UploadThread mUploadThread = new UploadThread();

    private DataUploader() {
    }

    public static DataUploader getInstance() {
        if (sDataUploader == null) {
            synchronized (LOCK) {
                if (sDataUploader == null) {
                    sDataUploader = new DataUploader();
                }
            }
        }
        return sDataUploader;
    }

    public boolean e(int eventID, String eventMsg) {
        if (IS_DEBUG) {
            Log.d(TAG, "eventID=" + eventID + " eventMsg=" + eventMsg);
        }
        ReportTool reportTool = this.mReportTool;
        if (reportTool != null) {
            return reportTool.report(eventID, eventMsg);
        }
        return IS_DEBUG;
    }

    public void setContext(Context context) {
        if (this.mContext == null) {
            this.mContext = context;
            this.mReportTool = ReportTool.getInstance(this.mContext);
            this.mContext.registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.BOOT_COMPLETED"));
        }
    }

    /* access modifiers changed from: package-private */
    public Context getContext() {
        return this.mContext;
    }

    public void saveEachTime(long timeBefore, long timeAfter) {
        if (timeBefore != -1 && timeAfter - timeBefore > 0) {
            long tempTime = Settings.Secure.getLong(this.mContext.getContentResolver(), SURFING_TIME, 0);
            Settings.Secure.putLong(this.mContext.getContentResolver(), SURFING_TIME, (timeAfter - timeBefore) + tempTime);
            if (Settings.Secure.getLong(this.mContext.getContentResolver(), NEED_TO_UPLOAD, -1) == -1) {
                Settings.Secure.putLong(this.mContext.getContentResolver(), NEED_TO_UPLOAD, timeAfter);
            }
            if (IS_DEBUG) {
                Log.d(TAG, "new Surfing_time = " + ((timeAfter - timeBefore) + tempTime));
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void uploadData() {
        if (new Date().getDay() != new Date(Settings.Secure.getLong(this.mContext.getContentResolver(), NEED_TO_UPLOAD, -1)).getDay()) {
            long time = Settings.Secure.getLong(this.mContext.getContentResolver(), SURFING_TIME, -1);
            if (time != -1) {
                e(3, "" + ((time / 1000) / 60));
                Settings.Secure.putLong(this.mContext.getContentResolver(), NEED_TO_UPLOAD, -1);
                Settings.Secure.putLong(this.mContext.getContentResolver(), SURFING_TIME, -1);
                if (IS_DEBUG) {
                    Log.d(TAG, "3:" + ((time / 1000) / 60));
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void executePerDay() {
        long initDelay = getTimeMillis("24:00:00") - System.currentTimeMillis();
        this.mExecutor.scheduleAtFixedRate(this.mUploadThread, initDelay > 0 ? initDelay : ONE_DAY + initDelay, ONE_DAY, TimeUnit.SECONDS);
    }

    private static long getTimeMillis(String time) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
            DateFormat dayFormat = new SimpleDateFormat("yy-MM-dd");
            return dateFormat.parse(dayFormat.format(new Date()) + " " + time).getTime();
        } catch (ParseException e) {
            Log.e(TAG, "ParseException occure");
            return 0;
        }
    }

    /* access modifiers changed from: private */
    public class UploadThread implements Runnable {
        private UploadThread() {
        }

        @Override // java.lang.Runnable
        public void run() {
            DataUploader.this.uploadData();
        }
    }
}
