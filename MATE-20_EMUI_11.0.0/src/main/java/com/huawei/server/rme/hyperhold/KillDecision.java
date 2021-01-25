package com.huawei.server.rme.hyperhold;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.util.Slog;
import com.android.internal.os.BackgroundThread;
import com.android.server.hidata.wavemapping.cons.Constant;
import com.huawei.server.HwPartIawareUtil;
import com.huawei.server.rme.hyperhold.SceneConst;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public final class KillDecision {
    private static final int KILL_CHECK = 1;
    private static final int LEVEL_THREE = 2;
    private static final int LEVEL_TWO = 1;
    private static final int QUICK_KILL = 3;
    private static final String TAG = "SWAP_KillDecision";
    private static final int ZSWAPD_UNMET_RESET = 2;
    private static final int ZSWAP_KILL = 4;
    private static Handler handler = null;
    private static volatile KillDecision killDecision = null;
    private DefaultAdvancedKiller advancedKiller = null;
    private BufferProc bufferProc;
    private volatile Context context;
    private int futileKillCnt = 0;
    private int futileKillThreshold = 3;
    private int futileKillTimeThreshold = Constant.SEND_STALL_MSG_DELAY;
    private KernelInterface kernelInterface = null;
    private ExecutorService killExecutorService = null;
    private volatile String killParamRef = "buffer size";
    private volatile int killThreshold = 400;
    private long lastFutileKillTime = -1;
    private int levelThreeThreshold;
    private int levelTwoThreshold;
    private int normalBuffer = 400;
    private volatile int quickKillBuffer = 400;
    private AtomicInteger zswapdUnmetNum = new AtomicInteger(0);
    private int zswapdWindow;

    private KillDecision() {
    }

    public static KillDecision getInstance() {
        if (killDecision == null) {
            synchronized (KillDecision.class) {
                if (killDecision == null) {
                    killDecision = new KillDecision();
                }
            }
        }
        return killDecision;
    }

    public void init(Context context2) {
        this.context = context2;
        this.advancedKiller = HwPartIawareUtil.getAdvancedKiller(context2);
        this.kernelInterface = KernelInterface.getInstance();
        this.bufferProc = BufferProc.getInstance();
        this.killExecutorService = Executors.newSingleThreadExecutor();
        this.killParamRef = ParaConfig.getInstance().getKillParam().getKillParamRef();
        this.killThreshold = ParaConfig.getInstance().getKillParam().getKillThreshold();
        this.quickKillBuffer = ParaConfig.getInstance().getKillParam().getBigKillMem();
        this.normalBuffer = ParaConfig.getInstance().getBufferSizeParam().getMinBuffer();
        Slog.i(TAG, "NormalBuffer is:" + this.normalBuffer);
        this.zswapdWindow = ParaConfig.getInstance().getZswapdPress().getWindow();
        Looper looper = HyperHoldServiceThread.getInstance().getLooper();
        if (looper == null) {
            looper = BackgroundThread.get().getLooper();
            Slog.e(TAG, "HyperHold Service Thread get failed, use background looper instead");
        }
        this.levelTwoThreshold = ParaConfig.getInstance().getKillParamOpt().getLevelTwo();
        this.levelThreeThreshold = ParaConfig.getInstance().getKillParamOpt().getLevelThree();
        this.futileKillThreshold = ParaConfig.getInstance().getKillParamOpt().getFutileKillThreshold();
        this.futileKillTimeThreshold = ParaConfig.getInstance().getKillParamOpt().getFutileKillTimeThreshold();
        handler = new Handler(looper) {
            /* class com.huawei.server.rme.hyperhold.KillDecision.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                if (msg.what == 2) {
                    KillDecision.this.resetUnmetTime();
                } else if (msg.obj instanceof SceneConst.KillCheckScene) {
                    SceneConst.KillCheckScene tempKillCheckScene = (SceneConst.KillCheckScene) msg.obj;
                    tempKillCheckScene.addCheckKillTime();
                    if (tempKillCheckScene.getCheckKillTime() < 3) {
                        KillDecision.this.killApplicationWithNewThread(tempKillCheckScene.getLevel(), tempKillCheckScene.getCheckKillTime());
                    }
                } else {
                    Slog.e(KillDecision.TAG, "handleMessage:error message");
                }
            }
        };
        resetUnmetTime();
    }

    public void updateModel(String appName) {
        DefaultAdvancedKiller defaultAdvancedKiller = this.advancedKiller;
        if (defaultAdvancedKiller != null) {
            defaultAdvancedKiller.updateModel(appName);
        } else {
            Slog.e(TAG, "Try to update model before init of advancedKiller");
        }
    }

    public void serializeModel() {
        DefaultAdvancedKiller defaultAdvancedKiller = this.advancedKiller;
        if (defaultAdvancedKiller != null) {
            defaultAdvancedKiller.serializeModel();
        } else {
            Slog.e(TAG, "Try to serialize model before init of advancedKiller");
        }
    }

    public void tryToQuickKill(int tempQuickKillThreshold) {
        if (this.kernelInterface != null && this.killParamRef != null) {
            OtherKillExecuteThread otherKillExecuteThread = new OtherKillExecuteThread(this, 3);
            otherKillExecuteThread.setTempQuickKillThreshold(tempQuickKillThreshold);
            this.killExecutorService.execute(otherKillExecuteThread);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doQuickKill(int tempQuickKillThreshold) {
        int freeBuffer = this.kernelInterface.getZswapdParamByName(this.killParamRef);
        int tempKillThreshold = this.quickKillBuffer;
        if (tempQuickKillThreshold > 0) {
            tempKillThreshold = tempQuickKillThreshold;
        }
        Slog.i(TAG, "BigMemory kill check. now free buffer is:" + freeBuffer + " quickKillBuffer:" + tempKillThreshold);
        if (freeBuffer < tempKillThreshold) {
            killByAk((long) (tempKillThreshold - freeBuffer), true);
        }
    }

    public void killApplicationWithNewThread() {
        Slog.i(TAG, "Begin call killApplicationWithThread");
    }

    public void killApplicationWithNewThread(int level, int checkTime) {
        this.killExecutorService.execute(new KillExecuteThread(level, checkTime));
    }

    public void setQuickKillBuffer(int quick) {
        this.quickKillBuffer = quick;
    }

    public void killApplicationWithZswapdPresure(boolean isFull) {
        this.killExecutorService.execute(new OtherKillExecuteThread(4, isFull));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doKillApplicationWithZswapdPresure(boolean isFull) {
        long reqMem;
        int freeBuffer = this.kernelInterface.getZswapdParamByName(this.killParamRef);
        if (isFull) {
            reqMem = (long) (BufferProc.getInstance().getTargetBuffer() - freeBuffer);
        } else {
            reqMem = (long) ((SceneProcessing.getInstance().isInDeepSleep() ? BufferProc.getInstance().getTargetBuffer() : this.normalBuffer) - freeBuffer);
        }
        if (reqMem > 0) {
            Slog.i(TAG, "Begin kill Application IN killApplicationWithZswapdPresure. reqMem:" + reqMem + " isFull:" + isFull);
            if (killByAk(reqMem, false) == 0 && !isFull) {
                checkFutileKillFromZswapd();
            }
        }
    }

    public void killOneApp() {
        Slog.i(TAG, "Begin KillOneApp.");
        killByAk(1, false);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int killByAk(long reqMem, boolean isQuick) {
        Bundle bundle = new Bundle();
        bundle.putLong("reqMem", reqMem);
        if (isQuick) {
            bundle.putBoolean("quickKill", true);
            bundle.putString("reason", "quick kill");
            bundle.putInt("curUid", SceneProcessing.getInstance().getCurrentAppUid());
        }
        int killResult = -1;
        if (this.advancedKiller != null) {
            BufferProc.getInstance().notifyCurrentEvent("beforeKill");
            killResult = this.advancedKiller.execute(bundle);
            Slog.i(TAG, "killResult:" + killResult);
            this.advancedKiller.setInterrupt(false);
            BufferProc.getInstance().notifyCurrentEvent("afterKill");
        } else {
            Slog.e(TAG, "AK is null. Try to execute model before init of AK");
        }
        if (killResult == 0) {
            incFutileKillCnt();
        } else {
            resetFutileKillCnt();
        }
        return killResult;
    }

    public int addZswapdUnmetNum() {
        return this.zswapdUnmetNum.incrementAndGet();
    }

    /* access modifiers changed from: private */
    public class KillExecuteThread implements Runnable {
        int checkTime;
        int level;

        KillExecuteThread(int level2, int checkTime2) {
            this.level = level2;
            this.checkTime = checkTime2;
        }

        @Override // java.lang.Runnable
        public void run() {
            int tid = Process.myTid();
            int beforePriority = Process.getThreadPriority(tid);
            Process.setThreadPriority(tid, -20);
            int reqMem = KillDecision.this.needKillByBuffer(this.level);
            if (reqMem <= 0) {
                Message msg = KillDecision.handler.obtainMessage();
                msg.obj = new SceneConst.KillCheckScene(this.checkTime, this.level);
                KillDecision.handler.sendMessageDelayed(msg, 100);
                Process.setThreadPriority(tid, beforePriority);
                return;
            }
            Slog.i(KillDecision.TAG, "Begin kill applications.");
            if (this.level == 2) {
                KillDecision.this.killByAk((long) reqMem, true);
            } else {
                KillDecision.this.killByAk((long) reqMem, false);
            }
            Process.setThreadPriority(tid, beforePriority);
        }
    }

    /* access modifiers changed from: private */
    public class OtherKillExecuteThread implements Runnable {
        boolean isFull;
        int tempQuickKillThreshold;
        int type;

        OtherKillExecuteThread(int type2, boolean isFull2) {
            this.isFull = false;
            this.tempQuickKillThreshold = -1;
            this.type = type2;
            this.isFull = isFull2;
        }

        OtherKillExecuteThread(KillDecision killDecision, int type2) {
            this(type2, false);
        }

        @Override // java.lang.Runnable
        public void run() {
            int tid = Process.myTid();
            int beforePriority = Process.getThreadPriority(tid);
            Process.setThreadPriority(tid, -20);
            int i = this.type;
            if (i == 3) {
                KillDecision.this.doQuickKill(this.tempQuickKillThreshold);
            } else if (i == 4) {
                KillDecision.this.doKillApplicationWithZswapdPresure(this.isFull);
            } else {
                Slog.e(KillDecision.TAG, "OtherKillExecuteThread type error");
            }
            Process.setThreadPriority(tid, beforePriority);
        }

        public void setTempQuickKillThreshold(int tempQuickKillThreshold2) {
            this.tempQuickKillThreshold = tempQuickKillThreshold2;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private int needKillByBuffer(int level) {
        int freeBuffer = this.kernelInterface.getZswapdParamByName(this.killParamRef);
        int tempKillThreshold = this.killThreshold;
        boolean isInDeepSleep = SceneProcessing.getInstance().isInDeepSleep();
        int targetBuffer = BufferProc.getInstance().getTargetBuffer();
        if (level == 1) {
            tempKillThreshold = isInDeepSleep ? targetBuffer : this.levelTwoThreshold;
            Slog.i(TAG, "high level check - level two. now free buffer is:" + freeBuffer + " KillThreshold:" + tempKillThreshold + "isInDeepSleep:" + isInDeepSleep);
        } else if (level == 2) {
            tempKillThreshold = isInDeepSleep ? targetBuffer : this.levelThreeThreshold;
            Slog.i(TAG, "high level check - level three. now free buffer is:" + freeBuffer + " KillThreshold:" + tempKillThreshold + "isInDeepSleep:" + isInDeepSleep);
        } else {
            Slog.i(TAG, "Kill check. now free buffer is:" + freeBuffer + " KillThreshold:" + this.killThreshold);
        }
        return tempKillThreshold - freeBuffer;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void resetUnmetTime() {
        this.zswapdUnmetNum.getAndSet(0);
        if (this.zswapdWindow == -1) {
            Slog.i(TAG, "ResetUnmetTime close");
            return;
        }
        Message msg = handler.obtainMessage();
        msg.what = 2;
        handler.sendMessageDelayed(msg, (long) (this.zswapdWindow * 1000));
    }

    private void incFutileKillCnt() {
        long curTime = SystemClock.uptimeMillis();
        long j = this.lastFutileKillTime;
        if (j != -1) {
            int i = this.futileKillTimeThreshold;
            if (curTime - j <= ((long) i)) {
                if (curTime - j <= ((long) i)) {
                    this.futileKillCnt++;
                    Slog.i(TAG, "incFutileKillCnt, now futileKillCnt:" + this.futileKillCnt);
                    this.lastFutileKillTime = curTime;
                    return;
                }
                Slog.i(TAG, "incFutileKillCnt wrongly called");
                return;
            }
        }
        this.lastFutileKillTime = curTime;
        this.futileKillCnt = 1;
        Slog.i(TAG, "incFutileKillCnt set futileKillCnt:" + this.futileKillCnt);
    }

    private void checkFutileKillFromZswapd() {
        if (this.futileKillCnt >= this.futileKillThreshold) {
            Slog.i(TAG, "found futile kill from zswapd faild, calling bufferProc");
            this.bufferProc.notifyCurrentEvent("futileKill");
            resetFutileKillCnt();
        }
    }

    private void resetFutileKillCnt() {
        this.futileKillCnt = 0;
        this.lastFutileKillTime = -1;
    }
}
