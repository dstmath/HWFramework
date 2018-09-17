package com.android.server.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Slog;
import com.android.server.display.HwEyeProtectionDividedTimeControl;
import com.android.server.rms.algorithm.AwareUserHabit;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HwUserBehaviourRecord extends AbsUserBehaviourRecord {
    private static final boolean DEBUG = false;
    public static final String TAG = "HwUserBehaviourRecord";
    private static final String TAG_LOCAL = "HwUserBehaviourRecord";
    private int APPENTEREVENTID = 1;
    private int APPEXITEVENTID = 2;
    private final AsyUploadLooperThread AsyUploadThread = new AsyUploadLooperThread();
    private ArrayList<Message> mMessageCache = new ArrayList();
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
                Slog.i("HwUserBehaviourRecord", "intent: action=ACTION_BOOT_COMPLETED---");
                HwUserBehaviourRecord.this.timerUpload();
            }
        }
    };
    private Map<String, AppInfo> map;
    private ReportTools reportTools;

    class AsyUploadLooperThread extends Thread {
        public static final int UPLOAD_APP_ENTER_RECORD = 0;
        public static final int UPLOAD_APP_EXIT_RECORD = 1;
        public static final int UPLOAD_TIMER_UPLOAD = 2;
        public Handler mHandler;

        AsyUploadLooperThread() {
        }

        public void run() {
            Looper.prepare();
            this.mHandler = new Handler() {
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case 0:
                            HwUserBehaviourRecord.this.appEnterRecordInnerImpl(msg.getData().getString(AwareUserHabit.USERHABIT_PACKAGE_NAME));
                            return;
                        case 1:
                            HwUserBehaviourRecord.this.appExitRecordInnerImpl(msg.getData().getString(AwareUserHabit.USERHABIT_PACKAGE_NAME), msg.getData().getString("back_reson"));
                            return;
                        case 2:
                            HwUserBehaviourRecord.this.timerUploadInnerImpl();
                            return;
                        default:
                            return;
                    }
                }
            };
            Looper.loop();
        }
    }

    class UploadThread implements Runnable {
        UploadThread() {
        }

        public void run() {
            HwUserBehaviourRecord.this.timerUpload();
        }
    }

    public HwUserBehaviourRecord(Context context) {
        super(context);
        this.reportTools = ReportTools.getInstance(context);
        this.map = new HashMap();
        context.registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.BOOT_COMPLETED"), "com.android.permission.HWUserBehaviourRecord", null);
        executePerDay();
    }

    private void startAsyUploadThread() {
        if (!this.AsyUploadThread.isAlive()) {
            this.AsyUploadThread.setName("AsyUploadThread");
            this.AsyUploadThread.start();
        }
    }

    private void appEnterRecordInnerImpl(String packageName) {
        AppInfo appInfo;
        if (this.map.containsKey(packageName)) {
            appInfo = (AppInfo) this.map.get(packageName);
            if (appInfo != null) {
                appInfo.setCount(appInfo.getCount() + 1);
                return;
            }
            return;
        }
        appInfo = new AppInfo(packageName);
        appInfo.setCount(1);
        this.map.put(packageName, appInfo);
    }

    public void appEnterRecord(String packageName) {
        startAsyUploadThread();
        Message msg;
        Bundle bundle;
        if (this.AsyUploadThread.mHandler != null) {
            if (!this.mMessageCache.isEmpty()) {
                int cacheSize = this.mMessageCache.size();
                for (int i = 0; i < cacheSize; i++) {
                    this.AsyUploadThread.mHandler.sendMessage((Message) this.mMessageCache.get(i));
                }
                this.mMessageCache.clear();
            }
            msg = new Message();
            msg.what = 0;
            bundle = new Bundle();
            bundle.putString(AwareUserHabit.USERHABIT_PACKAGE_NAME, packageName);
            msg.setData(bundle);
            this.AsyUploadThread.mHandler.sendMessage(msg);
            return;
        }
        msg = new Message();
        msg.what = 0;
        bundle = new Bundle();
        bundle.putString(AwareUserHabit.USERHABIT_PACKAGE_NAME, packageName);
        msg.setData(bundle);
        this.mMessageCache.add(msg);
    }

    private void appExitRecordInnerImpl(String packageName, String backreson) {
        this.reportTools.report(this.APPEXITEVENTID, AppInfo.exitReson(packageName, backreson));
    }

    public void appExitRecord(String packageName, String backreson) {
        startAsyUploadThread();
        Message msg;
        Bundle bundle;
        if (this.AsyUploadThread.mHandler != null) {
            msg = new Message();
            msg.what = 1;
            bundle = new Bundle();
            bundle.putString(AwareUserHabit.USERHABIT_PACKAGE_NAME, packageName);
            bundle.putString("back_reson", backreson);
            msg.setData(bundle);
            this.AsyUploadThread.mHandler.sendMessage(msg);
            return;
        }
        msg = new Message();
        msg.what = 1;
        bundle = new Bundle();
        bundle.putString(AwareUserHabit.USERHABIT_PACKAGE_NAME, packageName);
        bundle.putString("back_reson", backreson);
        msg.setData(bundle);
        this.mMessageCache.add(msg);
    }

    private void timerUploadInnerImpl() {
        if (!this.map.isEmpty()) {
            for (Entry<String, AppInfo> next : this.map.entrySet()) {
                this.reportTools.report(this.APPENTEREVENTID, ((AppInfo) next.getValue()).toString());
            }
        }
        this.map.clear();
    }

    private void timerUpload() {
        startAsyUploadThread();
        if (this.AsyUploadThread.mHandler != null) {
            this.AsyUploadThread.mHandler.sendEmptyMessage(2);
            return;
        }
        Message msg = new Message();
        msg.what = 2;
        this.mMessageCache.add(msg);
    }

    void executePerDay() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        long initDelay = getTimeMillis("24:00:00") - System.currentTimeMillis();
        if (initDelay <= 0) {
            initDelay += HwEyeProtectionDividedTimeControl.DAY_IN_MIllIS;
        }
        executor.scheduleAtFixedRate(new UploadThread(), initDelay, HwEyeProtectionDividedTimeControl.DAY_IN_MIllIS, TimeUnit.MILLISECONDS);
    }

    private long getTimeMillis(String time) {
        try {
            return new SimpleDateFormat("yy-MM-dd HH:mm:ss").parse(new SimpleDateFormat("yy-MM-dd").format(new Date()) + " " + time).getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
