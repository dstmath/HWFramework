package com.android.server.backup;

import android.app.job.JobInfo.Builder;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;

public class FullBackupJob extends JobService {
    private static final boolean DEBUG = true;
    private static final int JOB_ID = 20536;
    private static final String TAG = "FullBackupJob";
    private static ComponentName sIdleService;
    JobParameters mParams;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.backup.FullBackupJob.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.backup.FullBackupJob.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.backup.FullBackupJob.<clinit>():void");
    }

    public static void schedule(Context ctx, long minDelay) {
        JobScheduler js = (JobScheduler) ctx.getSystemService("jobscheduler");
        Builder builder = new Builder(JOB_ID, sIdleService).setRequiresDeviceIdle(DEBUG).setRequiredNetworkType(2).setRequiresCharging(DEBUG);
        if (minDelay > 0) {
            builder.setMinimumLatency(minDelay);
        }
        js.schedule(builder.build());
    }

    public void finishBackupPass() {
        if (this.mParams != null) {
            jobFinished(this.mParams, false);
            this.mParams = null;
        }
    }

    public boolean onStartJob(JobParameters params) {
        this.mParams = params;
        return BackupManagerService.getInstance().beginFullBackup(this);
    }

    public boolean onStopJob(JobParameters params) {
        if (this.mParams != null) {
            this.mParams = null;
            BackupManagerService.getInstance().endFullBackup();
        }
        return false;
    }
}
