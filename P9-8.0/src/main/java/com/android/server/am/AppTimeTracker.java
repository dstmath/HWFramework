package com.android.server.am;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.ArrayMap;
import android.util.MutableLong;
import android.util.TimeUtils;
import java.io.PrintWriter;

public class AppTimeTracker {
    private final ArrayMap<String, MutableLong> mPackageTimes = new ArrayMap();
    private final PendingIntent mReceiver;
    private String mStartedPackage;
    private MutableLong mStartedPackageTime;
    private long mStartedTime;
    private long mTotalTime;

    public AppTimeTracker(PendingIntent receiver) {
        this.mReceiver = receiver;
    }

    public void start(String packageName) {
        long now = SystemClock.elapsedRealtime();
        if (this.mStartedTime == 0) {
            this.mStartedTime = now;
        }
        if (!packageName.equals(this.mStartedPackage)) {
            if (this.mStartedPackageTime != null) {
                long elapsedTime = now - this.mStartedTime;
                MutableLong mutableLong = this.mStartedPackageTime;
                mutableLong.value += elapsedTime;
                this.mTotalTime += elapsedTime;
            }
            this.mStartedPackage = packageName;
            this.mStartedPackageTime = (MutableLong) this.mPackageTimes.get(packageName);
            if (this.mStartedPackageTime == null) {
                this.mStartedPackageTime = new MutableLong(0);
                this.mPackageTimes.put(packageName, this.mStartedPackageTime);
            }
        }
    }

    public void stop() {
        if (this.mStartedTime != 0) {
            long elapsedTime = SystemClock.elapsedRealtime() - this.mStartedTime;
            this.mTotalTime += elapsedTime;
            if (this.mStartedPackageTime != null) {
                MutableLong mutableLong = this.mStartedPackageTime;
                mutableLong.value += elapsedTime;
            }
            this.mStartedPackage = null;
            this.mStartedPackageTime = null;
        }
    }

    public void deliverResult(Context context) {
        stop();
        Bundle extras = new Bundle();
        extras.putLong("android.activity.usage_time", this.mTotalTime);
        Bundle pkgs = new Bundle();
        for (int i = this.mPackageTimes.size() - 1; i >= 0; i--) {
            pkgs.putLong((String) this.mPackageTimes.keyAt(i), ((MutableLong) this.mPackageTimes.valueAt(i)).value);
        }
        extras.putBundle("android.usage_time_packages", pkgs);
        Intent fillinIntent = new Intent();
        fillinIntent.putExtras(extras);
        try {
            this.mReceiver.send(context, 0, fillinIntent);
        } catch (CanceledException e) {
        }
    }

    public void dumpWithHeader(PrintWriter pw, String prefix, boolean details) {
        pw.print(prefix);
        pw.print("AppTimeTracker #");
        pw.print(Integer.toHexString(System.identityHashCode(this)));
        pw.println(":");
        dump(pw, prefix + "  ", details);
    }

    public void dump(PrintWriter pw, String prefix, boolean details) {
        pw.print(prefix);
        pw.print("mReceiver=");
        pw.println(this.mReceiver);
        pw.print(prefix);
        pw.print("mTotalTime=");
        TimeUtils.formatDuration(this.mTotalTime, pw);
        pw.println();
        for (int i = 0; i < this.mPackageTimes.size(); i++) {
            pw.print(prefix);
            pw.print("mPackageTime:");
            pw.print((String) this.mPackageTimes.keyAt(i));
            pw.print("=");
            TimeUtils.formatDuration(((MutableLong) this.mPackageTimes.valueAt(i)).value, pw);
            pw.println();
        }
        if (details && this.mStartedTime != 0) {
            pw.print(prefix);
            pw.print("mStartedTime=");
            TimeUtils.formatDuration(SystemClock.elapsedRealtime(), this.mStartedTime, pw);
            pw.println();
            pw.print(prefix);
            pw.print("mStartedPackage=");
            pw.println(this.mStartedPackage);
        }
    }
}
