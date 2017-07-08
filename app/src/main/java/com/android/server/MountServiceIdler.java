package com.android.server;

import android.app.ActivityManagerNative;
import android.app.job.JobInfo.Builder;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.RemoteException;
import android.util.Slog;
import java.util.Calendar;

public class MountServiceIdler extends JobService {
    private static int MOUNT_JOB_ID = 0;
    private static final String TAG = "MountServiceIdler";
    private static ComponentName sIdleService;
    private Runnable mFinishCallback;
    private JobParameters mJobParams;
    private boolean mStarted;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.MountServiceIdler.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.MountServiceIdler.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.MountServiceIdler.<clinit>():void");
    }

    public MountServiceIdler() {
        this.mFinishCallback = new Runnable() {
            public void run() {
                Slog.i(MountServiceIdler.TAG, "Got mount service completion callback");
                synchronized (MountServiceIdler.this.mFinishCallback) {
                    if (MountServiceIdler.this.mStarted) {
                        MountServiceIdler.this.jobFinished(MountServiceIdler.this.mJobParams, false);
                        MountServiceIdler.this.mStarted = false;
                    }
                }
                MountServiceIdler.scheduleIdlePass(MountServiceIdler.this);
            }
        };
    }

    public boolean onStartJob(JobParameters params) {
        try {
            ActivityManagerNative.getDefault().performIdleMaintenance();
        } catch (RemoteException e) {
        }
        this.mJobParams = params;
        MountService ms = MountService.sSelf;
        if (ms != null) {
            synchronized (this.mFinishCallback) {
                this.mStarted = true;
            }
            ms.runIdleMaintenance(this.mFinishCallback);
        }
        if (ms != null) {
            return true;
        }
        return false;
    }

    public boolean onStopJob(JobParameters params) {
        synchronized (this.mFinishCallback) {
            this.mStarted = false;
        }
        return false;
    }

    public static void scheduleIdlePass(Context context) {
        JobScheduler tm = (JobScheduler) context.getSystemService("jobscheduler");
        long timeToMidnight = tomorrowMidnight().getTimeInMillis() - System.currentTimeMillis();
        Builder builder = new Builder(MOUNT_JOB_ID, sIdleService);
        builder.setRequiresDeviceIdle(true);
        builder.setRequiresCharging(true);
        builder.setMinimumLatency(timeToMidnight);
        if (tm != null) {
            tm.schedule(builder.build());
        }
    }

    private static Calendar tomorrowMidnight() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(11, 3);
        calendar.set(12, 0);
        calendar.set(13, 0);
        calendar.set(14, 0);
        calendar.add(5, 1);
        return calendar;
    }
}
