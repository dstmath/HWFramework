package com.android.server.backup;

import android.app.job.JobInfo.Builder;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.RemoteException;
import android.util.Slog;
import java.util.Random;

public class KeyValueBackupJob extends JobService {
    static final long BATCH_INTERVAL = 14400000;
    private static final int FUZZ_MILLIS = 600000;
    private static final int JOB_ID = 20537;
    private static final long MAX_DEFERRAL = 86400000;
    private static final String TAG = "KeyValueBackupJob";
    private static ComponentName sKeyValueJobService;
    private static long sNextScheduled;
    private static boolean sScheduled;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.backup.KeyValueBackupJob.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.backup.KeyValueBackupJob.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.backup.KeyValueBackupJob.<clinit>():void");
    }

    public static void schedule(Context ctx) {
        schedule(ctx, 0);
    }

    public static void schedule(Context ctx, long delay) {
        synchronized (KeyValueBackupJob.class) {
            if (!sScheduled) {
                if (delay <= 0) {
                    delay = BATCH_INTERVAL + ((long) new Random().nextInt(FUZZ_MILLIS));
                }
                if (BackupManagerService.DEBUG_SCHEDULING) {
                    Slog.v(TAG, "Scheduling k/v pass in " + ((delay / 1000) / 60) + " minutes");
                }
                ((JobScheduler) ctx.getSystemService("jobscheduler")).schedule(new Builder(JOB_ID, sKeyValueJobService).setMinimumLatency(delay).setRequiredNetworkType(1).setRequiresCharging(true).setOverrideDeadline(MAX_DEFERRAL).build());
                sNextScheduled = System.currentTimeMillis() + delay;
                sScheduled = true;
            }
        }
    }

    public static void cancel(Context ctx) {
        synchronized (KeyValueBackupJob.class) {
            ((JobScheduler) ctx.getSystemService("jobscheduler")).cancel(JOB_ID);
            sNextScheduled = 0;
            sScheduled = false;
        }
    }

    public static long nextScheduled() {
        long j;
        synchronized (KeyValueBackupJob.class) {
            j = sNextScheduled;
        }
        return j;
    }

    public boolean onStartJob(JobParameters params) {
        synchronized (KeyValueBackupJob.class) {
            sNextScheduled = 0;
            sScheduled = false;
        }
        try {
            BackupManagerService.getInstance().backupNow();
        } catch (RemoteException e) {
        }
        return false;
    }

    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
