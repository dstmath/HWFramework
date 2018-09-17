package com.android.server.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings.Secure;
import android.util.Log;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DataUploader {
    public static final int BASE = 3;
    public static final String NEED_TO_UPLOAD = "surfing_upload_date";
    public static final String SURFING_TIME = "wifi_surfing_time";
    private static final String TAG = "DataUploader";
    private static DataUploader mDataUploader = null;
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private Context mContext = null;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
                Log.d(DataUploader.TAG, "receive BOOT_COMPLETED action and begin upload data and set up timer");
                DataUploader.this.uploadData();
                DataUploader.this.executePerDay();
            }
        }
    };
    private ReportTool rTool = null;
    private UploadThread uploadThread = new UploadThread(this, null);

    private class UploadThread implements Runnable {
        /* synthetic */ UploadThread(DataUploader this$0, UploadThread -this1) {
            this();
        }

        private UploadThread() {
        }

        public void run() {
            DataUploader.this.uploadData();
        }
    }

    private DataUploader() {
    }

    public static DataUploader getInstance() {
        if (mDataUploader == null) {
            mDataUploader = new DataUploader();
        }
        return mDataUploader;
    }

    public boolean e(int eventID, String eventMsg) {
        Log.d(TAG, "eventID=" + eventID + " eventMsg=" + eventMsg);
        if (this.rTool != null) {
            return this.rTool.report(eventID, eventMsg);
        }
        return false;
    }

    public void setContext(Context context) {
        if (this.mContext == null) {
            this.mContext = context;
            this.rTool = ReportTool.getInstance(this.mContext);
            this.mContext.registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.BOOT_COMPLETED"));
        }
    }

    Context getContext() {
        return this.mContext;
    }

    public void saveEachTime(long time1, long time2) {
        if (time1 != -1 && time2 - time1 > 0) {
            long tempTime = Secure.getLong(this.mContext.getContentResolver(), SURFING_TIME, 0);
            Secure.putLong(this.mContext.getContentResolver(), SURFING_TIME, (time2 - time1) + tempTime);
            if (Secure.getLong(this.mContext.getContentResolver(), NEED_TO_UPLOAD, -1) == -1) {
                Secure.putLong(this.mContext.getContentResolver(), NEED_TO_UPLOAD, time2);
            }
            Log.d(TAG, "new Surfing_time = " + ((time2 - time1) + tempTime));
        }
    }

    private final void uploadData() {
        if (new Date().getDay() != new Date(Secure.getLong(this.mContext.getContentResolver(), NEED_TO_UPLOAD, -1)).getDay()) {
            long time = Secure.getLong(this.mContext.getContentResolver(), SURFING_TIME, -1);
            if (time != -1) {
                e(51, "{Dur:" + ((time / 1000) / 60) + "}");
                Secure.putLong(this.mContext.getContentResolver(), NEED_TO_UPLOAD, -1);
                Secure.putLong(this.mContext.getContentResolver(), SURFING_TIME, -1);
                Log.d(TAG, "3:" + ((time / 1000) / 60));
            }
        }
    }

    private void executePerDay() {
        long initDelay = getTimeMillis("24:00:00") - System.currentTimeMillis();
        if (initDelay <= 0) {
            initDelay += 86400000;
        }
        this.executor.scheduleAtFixedRate(this.uploadThread, initDelay, 86400000, TimeUnit.SECONDS);
    }

    private static long getTimeMillis(String time) {
        try {
            return new SimpleDateFormat("yy-MM-dd HH:mm:ss").parse(new SimpleDateFormat("yy-MM-dd").format(new Date()) + " " + time).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
