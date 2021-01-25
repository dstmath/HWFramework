package com.huawei.server.rme.hyperhold;

import android.os.Process;
import android.util.Slog;
import com.huawei.server.rme.hyperhold.ParaConfig;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class KernelEventReceiver {
    private static final int INVALID = -1;
    private static final String TAG = "SWAP_KernelEventReceiver";
    private static final int ZSWAPD_MONITOR_DELAY = 5;
    private static final int ZSWAPD_RUNNING_STAT_OFF = 0;
    private static final int ZSWAPD_RUNNING_STAT_ON = 1;
    private static final int ZSWAPD_START = 0;
    private static final int ZSWAPD_UNMET = 2;
    private static final int ZSWAPD_UNRECYCLE = 1;
    private static volatile KernelEventReceiver kernelEventReceiver = null;
    private int highPressure = -1;
    private KillDecision killDecision;
    private int zswapdRunning = 0;

    private KernelEventReceiver() {
    }

    public static KernelEventReceiver getInstance() {
        if (kernelEventReceiver == null) {
            synchronized (KernelEventReceiver.class) {
                if (kernelEventReceiver == null) {
                    kernelEventReceiver = new KernelEventReceiver();
                }
            }
        }
        return kernelEventReceiver;
    }

    public void init() {
        this.killDecision = KillDecision.getInstance();
        this.highPressure = ParaConfig.getInstance().getZswapdPress().getHighPressure();
        ParaConfig.PsiEventParam eventParam = ParaConfig.getInstance().getPsiEventParam();
        Slog.i(TAG, "KernelEventReceiver init, get eventParam" + eventParam.getThreshold() + " " + eventParam.getWindow());
        Executors.newSingleThreadExecutor().execute(new PsiMemMonitor(eventParam.getThreshold(), eventParam.getWindow()));
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(new ZswapdMonitor(), 5, 5, TimeUnit.SECONDS);
        Executors.newSingleThreadExecutor().execute(new ZswapdJniMonitor("handleZswapd", 0));
        Executors.newSingleThreadExecutor().execute(new ZswapdJniMonitor("handleZswapdUnrecycle", 1));
        Executors.newSingleThreadExecutor().execute(new ZswapdJniMonitor("handleZswapdUnmet", 2));
        Slog.i(TAG, "ZswapdUnmetMonitor:Init");
    }

    /* access modifiers changed from: private */
    public class ZswapdJniMonitor implements Runnable {
        String callBackMethod = "";
        int level;

        ZswapdJniMonitor(String callBackMethod2, int level2) {
            this.callBackMethod = callBackMethod2;
            this.level = level2;
        }

        @Override // java.lang.Runnable
        public void run() {
            Process.setThreadPriority(Process.myTid(), -20);
            Slog.i(KernelEventReceiver.TAG, "ZswapdZswapdJniMonitor:" + this.callBackMethod + " " + this.level);
            JniCommunication.triggerEvent("/dev/memcg/memory.zswapd_pressure", this.callBackMethod, this.level);
        }
    }

    /* access modifiers changed from: private */
    public class ZswapdMonitor implements Runnable {
        ZswapdMonitor() {
        }

        @Override // java.lang.Runnable
        public void run() {
            if (KernelEventReceiver.this.zswapdRunning == 1) {
                KernelEventReceiver.this.zswapdRunning = 0;
                AppModel.getInstance().zswapdFirstRunning();
            }
        }
    }

    public void handleZswapd() {
        BufferProc.getInstance().notifyCurrentEvent("zswapdStart");
        if (this.zswapdRunning == 0) {
            Slog.i(TAG, "receive zswapd!");
            this.zswapdRunning = 1;
        }
    }

    public void handleZswapdUnmet(boolean isFull) {
        BufferProc.getInstance().notifyCurrentEvent("zswapdFail");
        KillDecision killDecision2 = this.killDecision;
        if (killDecision2 == null) {
            return;
        }
        if (isFull) {
            killDecision2.killApplicationWithZswapdPresure(isFull);
        } else if (this.highPressure == -1) {
            Slog.i(TAG, "Zswapd pressure check close.");
        } else if (killDecision2.addZswapdUnmetNum() > this.highPressure) {
            this.killDecision.killApplicationWithZswapdPresure(isFull);
        }
    }

    public void handleZswapdUnmet() {
        Slog.i(TAG, "Zswapd pressure unmet without param.");
    }

    /* access modifiers changed from: private */
    public static class PsiMemMonitor implements Runnable {
        int threshold = 70;
        int window = 1000;

        PsiMemMonitor(int threshold2, int window2) {
            this.threshold = threshold2;
            this.window = window2;
        }

        @Override // java.lang.Runnable
        public void run() {
            Process.setThreadPriority(Process.myTid(), -20);
            Slog.i(KernelEventReceiver.TAG, "PsiMemMonitor run, threshold:" + this.threshold + " window:" + this.window);
            JniCommunication.triggerPsiEvent(this.threshold, this.window);
        }
    }
}
