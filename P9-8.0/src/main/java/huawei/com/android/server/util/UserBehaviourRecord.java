package huawei.com.android.server.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UserBehaviourRecord {
    private static UserBehaviourRecord behaviourRecord;
    private int APPENTEREVENTID = 1;
    private int APPEXITEVENTID = 2;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
                UserBehaviourRecord.this.timerUpload();
            }
        }
    };
    private Map<String, AppInfo> map;
    private ReportTool reportTool;

    class UploadThread implements Runnable {
        UploadThread() {
        }

        public void run() {
            UserBehaviourRecord.this.timerUpload();
        }
    }

    public static UserBehaviourRecord getInstance(Context context) {
        if (behaviourRecord == null) {
            behaviourRecord = new UserBehaviourRecord(context);
        }
        return behaviourRecord;
    }

    private UserBehaviourRecord(Context context) {
        this.reportTool = ReportTool.getInstance(context);
        this.map = new HashMap();
        context.registerReceiver(this.mReceiver, new IntentFilter("android.intent.action.BOOT_COMPLETED"));
        executePerDay();
    }

    public void appEnterRecord(String packageName) {
        synchronized (this) {
            AppInfo appInfo;
            if (this.map.containsKey(packageName)) {
                appInfo = (AppInfo) this.map.get(packageName);
                if (appInfo != null) {
                    appInfo.setCount(appInfo.getCount() + 1);
                }
            } else {
                appInfo = new AppInfo(packageName);
                appInfo.setCount(1);
                this.map.put(packageName, appInfo);
            }
        }
    }

    public void appExitRecord(String packageName, String backreson) {
        this.reportTool.report(this.APPEXITEVENTID, AppInfo.exitReson(packageName, backreson));
    }

    private void timerUpload() {
        synchronized (this) {
            if (!this.map.isEmpty()) {
                for (Entry<String, AppInfo> next : this.map.entrySet()) {
                    this.reportTool.report(this.APPENTEREVENTID, ((AppInfo) next.getValue()).toString());
                }
            }
        }
        this.map.clear();
    }

    void executePerDay() {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        long initDelay = getTimeMillis("24:00:00") - System.currentTimeMillis();
        if (initDelay <= 0) {
            initDelay += 86400000;
        }
        executor.scheduleAtFixedRate(new UploadThread(), initDelay, 86400000, TimeUnit.MILLISECONDS);
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
