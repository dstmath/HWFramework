package com.android.server.rms;

import android.app.job.JobInfo.Builder;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.util.LocalLog;
import android.util.Log;
import com.android.server.rms.utils.Interrupt;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class CompactJobService extends JobService {
    private static final int COMPACT_PERIOD_INTERVAL = 0;
    private static final int FAILED_PERIOD_INTERVAL = 0;
    private static final int JOB_ID = 1684366962;
    static final String TAG = "CompactJobService";
    private static ComponentName sCompactServiceName;
    private static final ArrayList<IDefraggler> sDefragglers = null;
    private static LocalLog sHistory;
    private static AtomicInteger sTimes;
    private final Interrupt mInterrupt;

    /* renamed from: com.android.server.rms.CompactJobService.1 */
    class AnonymousClass1 extends Thread {
        final /* synthetic */ JobParameters val$jobParams;

        AnonymousClass1(String $anonymous0, JobParameters val$jobParams) {
            this.val$jobParams = val$jobParams;
            super($anonymous0);
        }

        public void run() {
            int -get0;
            boolean bFinished = true;
            for (IDefraggler defraggler : CompactJobService.sDefragglers) {
                if (CompactJobService.this.mInterrupt.checkInterruptAndReset()) {
                    bFinished = false;
                    break;
                }
                defraggler.compact("background compact", null);
            }
            CompactJobService.this.mInterrupt.reset();
            CompactJobService.sTimes.getAndIncrement();
            CompactJobService.sHistory.log("do compact " + bFinished + " times = " + CompactJobService.sTimes.get());
            CompactJobService.this.jobFinished(this.val$jobParams, false);
            Context context = CompactJobService.this;
            if (bFinished) {
                -get0 = CompactJobService.COMPACT_PERIOD_INTERVAL;
            } else {
                -get0 = CompactJobService.FAILED_PERIOD_INTERVAL;
            }
            CompactJobService.delay_schedule(context, (long) -get0);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.CompactJobService.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rms.CompactJobService.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.CompactJobService.<clinit>():void");
    }

    public CompactJobService() {
        this.mInterrupt = new Interrupt();
    }

    public static void schedule(Context context) {
        ((JobScheduler) context.getSystemService("jobscheduler")).schedule(new Builder(JOB_ID, sCompactServiceName).setRequiresDeviceIdle(true).setRequiresCharging(true).build());
    }

    private static void delay_schedule(Context context, long delay) {
        ((JobScheduler) context.getSystemService("jobscheduler")).schedule(new Builder(JOB_ID, sCompactServiceName).setRequiresDeviceIdle(true).setRequiresCharging(true).setMinimumLatency(delay).build());
    }

    public boolean onStartJob(JobParameters params) {
        Log.w(TAG, "onIdleStart");
        if (sDefragglers.size() <= 0) {
            return false;
        }
        JobParameters jobParams = params;
        new AnonymousClass1("CompactJobService_Handler", params).start();
        return true;
    }

    public boolean onStopJob(JobParameters params) {
        Log.w(TAG, "onIdleStop");
        this.mInterrupt.trigger();
        for (IDefraggler defraggler : sDefragglers) {
            defraggler.interrupt();
        }
        return false;
    }

    protected static void dumpLog(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("CompactJobService dump");
        sHistory.dump(fd, pw, args);
    }

    public static void addDefragglers(IDefraggler defraggler) {
        sDefragglers.add(defraggler);
    }
}
