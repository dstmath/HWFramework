package com.huawei.server.rme.hyperhold;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.Trace;
import android.util.Slog;
import com.android.internal.os.BackgroundThread;
import com.android.server.wifipro.WifiProCommonDefs;
import com.huawei.hiai.awareness.AwarenessInnerConstants;

public final class BufferProc {
    private static final int ACTIVITY_START = 12;
    private static final int ACTIVITY_START_FINISH = 13;
    private static final int APP_IDLE_STATE = 0;
    private static final int APP_LAUNCHING_STATE = 1;
    private static final int APP_START = 2;
    private static final int APP_START_FINISH = 3;
    private static final int FUTILE_KILL = 14;
    private static final int GO_LAUNCHER = 4;
    private static final int GO_LAUNCHER_FINISH = 5;
    private static final int IDLE_INSCREASE = 11;
    private static final int KILL_APP_STATE = 3;
    private static final int KILL_START = 9;
    private static final int KILL_STOP = 10;
    private static final int NEED_TO_KILL = 1;
    private static final int RECOVER_ZSWAPD = 15;
    private static final String TAG = "SWAP_BufferProc";
    private static final int ZSWAPD_FAIL = 8;
    private static final int ZSWAPD_RUNNINT_STATE = 2;
    private static final int ZSWAPD_START = 6;
    private static final int ZSWAPD_STOP = 7;
    private static int appStartDelay = 2;
    private static volatile BufferProc bufferProc;
    private static int bufferScreenOffChange;
    private static int curReclaimFailCount = 0;
    private static int curState = -1;
    private static int defaultSize;
    private static int futileCloseDelay = WifiProCommonDefs.QUERY_TIMEOUT_MS;
    private static Handler handler = null;
    private static int highBufferStep = 50;
    private static int highSize;
    private static boolean isZswapdWorking = false;
    private static KernelInterface kernelInterface;
    private static long lastCallKillTime = -1000;
    private static long lastReclaimFailTime = -1;
    private static int lowBufferStep = 50;
    private static int lowSize;
    private static int maxTargetBuffer = 1900;
    private static int minTargetBuffer = 950;
    private static ParaConfig paraConfig;
    private static int raiseBufferMax = 1000;
    private static int raiseBufferStep = 10;
    private static int raiseBufferTimeWidth = 10;
    private static int reclaimDelay = 2;
    private static int reclaimFailCount = 5;
    private static int reclaimFailWindow = 5;
    private static int swapReserve;
    private static int targetBuffer = 1000;
    private static int upBufferStep = 30;

    private BufferProc() {
    }

    public static BufferProc getInstance() {
        if (bufferProc == null) {
            synchronized (BufferProc.class) {
                if (bufferProc == null) {
                    bufferProc = new BufferProc();
                }
            }
        }
        return bufferProc;
    }

    public void init() {
        Slog.i(TAG, "BufferProc init");
        paraConfig = ParaConfig.getInstance();
        kernelInterface = KernelInterface.getInstance();
        defaultSize = paraConfig.getBufferSizeParam().getDefaultSize();
        targetBuffer = defaultSize;
        lowSize = paraConfig.getBufferSizeParam().getLowSize();
        highSize = paraConfig.getBufferSizeParam().getHighSize();
        int i = defaultSize;
        lowBufferStep = i - lowSize;
        highBufferStep = highSize - i;
        upBufferStep = paraConfig.getBufferSizeParam().getUpperSize() - defaultSize;
        swapReserve = paraConfig.getBufferSizeParam().getSwapReserve();
        minTargetBuffer = paraConfig.getBufferSizeParam().getMinBuffer();
        maxTargetBuffer = paraConfig.getBufferSizeParam().getMaxBuffer();
        appStartDelay = paraConfig.getBufferSizeParam().getAppStartDelay();
        reclaimDelay = paraConfig.getBufferSizeParam().getReclaimDelay();
        reclaimFailWindow = paraConfig.getBufferSizeParam().getReclaimFailWindow();
        reclaimFailCount = paraConfig.getBufferSizeParam().getReclaimFailCount();
        raiseBufferTimeWidth = paraConfig.getBufferSizeParam().getRaiseBufferTimeWidth();
        raiseBufferMax = paraConfig.getBufferSizeParam().getRaiseBufferMax();
        raiseBufferStep = paraConfig.getBufferSizeParam().getRaiseBufferStep();
        bufferScreenOffChange = paraConfig.getKillParamOpt().getBufferScreenOffChange();
        futileCloseDelay = paraConfig.getKillParamOpt().getFutileCloseDelay();
        initHandler();
        Message msg = handler.obtainMessage();
        msg.what = 11;
        handler.sendMessageDelayed(msg, (long) (raiseBufferTimeWidth * 10000));
        enableZswapd();
        Slog.i(TAG, "Buffer Proc init succ: Target:" + targetBuffer + " lowStep:" + lowBufferStep + " highStep" + highBufferStep + " upperStep:" + upBufferStep);
    }

    public void disableZswapd() {
        setBuffer(0, 0, 0, 0);
        isZswapdWorking = false;
        curState = 0;
        removeAllMsg();
    }

    public void enableZswapd() {
        setBuffer(defaultSize, lowSize, highSize, swapReserve);
        isZswapdWorking = true;
        targetBuffer = defaultSize;
    }

    public int getTargetLowBuffer() {
        return targetBuffer - lowBufferStep;
    }

    public int getTargetBuffer() {
        return targetBuffer;
    }

    private void removeAllMsg() {
        Handler handler2 = handler;
        if (handler2 != null) {
            handler2.removeMessages(3);
            handler.removeMessages(5);
            handler.removeMessages(7);
            handler.removeMessages(10);
            handler.removeMessages(11);
            handler.removeMessages(14);
            handler.removeMessages(15);
        }
    }

    private Looper getHyperHoldLooper() {
        Looper looper = HyperHoldServiceThread.getInstance().getLooper();
        if (looper != null) {
            return looper;
        }
        Looper looper2 = BackgroundThread.get().getLooper();
        Slog.e(TAG, "HyperHold Service Thread get failed, use background looper instead");
        return looper2;
    }

    private void initHandler() {
        handler = new Handler(getHyperHoldLooper()) {
            /* class com.huawei.server.rme.hyperhold.BufferProc.AnonymousClass1 */

            @Override // android.os.Handler
            public void handleMessage(Message msg) {
                if (!BufferProc.isZswapdWorking) {
                    int unused = BufferProc.curState = 0;
                    return;
                }
                switch (msg.what) {
                    case 1:
                        BufferProc.this.handleCallKill();
                        return;
                    case 2:
                        BufferProc.this.handleAppStart();
                        return;
                    case 3:
                        BufferProc.this.handleAppStartFinish();
                        return;
                    case 4:
                        BufferProc.this.handleGoLauncher();
                        return;
                    case 5:
                        BufferProc.this.handleGoLauncherFinish();
                        return;
                    case 6:
                        BufferProc.this.handleZswapdStart();
                        return;
                    case 7:
                        BufferProc.this.handleZswapdStop();
                        return;
                    case 8:
                        BufferProc.this.handleZswapdFail();
                        return;
                    case 9:
                        BufferProc.this.handleKillStart();
                        return;
                    case 10:
                        BufferProc.this.handleKillStop();
                        return;
                    case 11:
                        BufferProc.this.handleIdleIncrease();
                        return;
                    case 12:
                        BufferProc.this.handleActivityStart(msg.arg1);
                        return;
                    case 13:
                        BufferProc.this.handleActivityStartFinish();
                        return;
                    case 14:
                        BufferProc.this.handleFutileKill();
                        return;
                    case 15:
                        BufferProc.this.handleRecoverZswapd();
                        return;
                    default:
                        Slog.e(BufferProc.TAG, "invalid msg: " + msg.what);
                        return;
                }
            }
        };
    }

    private void closeZswapd() {
        Slog.i(TAG, "closeZswapd now.");
        setBuffer(0, 0, 0, 0);
    }

    public void notifyQiyi() {
        Message msg = handler.obtainMessage();
        msg.what = 12;
        msg.arg1 = 3000;
        handler.sendMessage(msg);
    }

    public void notifyCurrentEvent(String eventName) {
        if (handler != null || !isZswapdWorking) {
            Message msg = handler.obtainMessage();
            char c = 65535;
            switch (eventName.hashCode()) {
                case -1694578437:
                    if (eventName.equals("futileKill")) {
                        c = 7;
                        break;
                    }
                    break;
                case -1047235949:
                    if (eventName.equals("activityStart")) {
                        c = 1;
                        break;
                    }
                    break;
                case -834471509:
                    if (eventName.equals("zswapdStart")) {
                        c = 3;
                        break;
                    }
                    break;
                case 518264701:
                    if (eventName.equals("beforeKill")) {
                        c = 5;
                        break;
                    }
                    break;
                case 526865589:
                    if (eventName.equals("zswapdFail")) {
                        c = 4;
                        break;
                    }
                    break;
                case 1018980186:
                    if (eventName.equals("afterKill")) {
                        c = 6;
                        break;
                    }
                    break;
                case 1127356584:
                    if (eventName.equals("goLauncher")) {
                        c = 2;
                        break;
                    }
                    break;
                case 1156744897:
                    if (eventName.equals("appStart")) {
                        c = 0;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    msg.what = 2;
                    break;
                case 1:
                    msg.arg1 = 500;
                    msg.what = 12;
                    break;
                case 2:
                    msg.what = 4;
                    break;
                case 3:
                    msg.what = 6;
                    break;
                case 4:
                    msg.what = 8;
                    break;
                case 5:
                    msg.what = 9;
                    break;
                case 6:
                    msg.what = 10;
                    break;
                case 7:
                    msg.what = 14;
                    break;
                default:
                    Slog.e(TAG, "invalid eventName: " + eventName);
                    return;
            }
            handler.sendMessage(msg);
            return;
        }
        Slog.e(TAG, "handler is null or zswapd is not init");
    }

    private void removeMessagesToStart() {
        handler.removeMessages(10);
        handler.removeMessages(7);
        handler.removeMessages(5);
        handler.removeMessages(3);
        handler.removeMessages(13);
        handler.removeMessages(15);
    }

    private void removeMessagesToKill() {
        handler.removeMessages(7);
        handler.removeMessages(15);
    }

    private void setBuffer(int mid, int low, int high, int swap) {
        KernelInterface.getInstance().setBuffer(mid, low, high, swap);
    }

    private int getCurBufferSize() {
        return kernelInterface.getZswapdParamByName("buffer size");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleCallKill() {
        Trace.traceBegin(8, "bufferLowToKill");
        long curTime = System.currentTimeMillis();
        long j = lastCallKillTime;
        if (curTime < j || curTime - j >= 1000) {
            lastCallKillTime = curTime;
            Slog.i(TAG, "buffer low too long, zswapd keep fail, call kill");
            KillDecision.getInstance().killApplicationWithZswapdPresure(false);
            Trace.traceEnd(8);
            return;
        }
        Slog.i(TAG, "buffer low too long, kill too frequently" + lastCallKillTime + AwarenessInnerConstants.DASH_KEY + curTime);
        Trace.traceEnd(8);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleAppStart() {
        Trace.traceBegin(8, "appStart:closeZswapd");
        removeMessagesToStart();
        curState = 1;
        Message msg = handler.obtainMessage();
        msg.what = 3;
        Slog.i(TAG, "handle app start, stop zswapd.");
        closeZswapd();
        handler.sendMessageDelayed(msg, (long) (appStartDelay * 1000));
        Trace.traceEnd(8);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleAppStartFinish() {
        if (curState == 1) {
            curReclaimFailCount = 0;
            lastReclaimFailTime = -1;
            handler.removeMessages(3);
            setTargetBuffer();
            curState = 0;
            Slog.i(TAG, "handle app start finish, reset failCount, targetBuffer:" + targetBuffer);
            return;
        }
        Slog.e(TAG, "invalid app start finish msg");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleActivityStart(int delay) {
        Trace.traceBegin(8, "appStart:closeZswapd");
        removeMessagesToStart();
        curState = 1;
        Message msg = handler.obtainMessage();
        msg.what = 13;
        Slog.i(TAG, "handle activity start, stop zswapd.");
        closeZswapd();
        handler.sendMessageDelayed(msg, (long) delay);
        Trace.traceEnd(8);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleActivityStartFinish() {
        if (curState == 1) {
            curReclaimFailCount = 0;
            lastReclaimFailTime = -1;
            handler.removeMessages(13);
            setTargetBuffer();
            curState = 0;
            Slog.i(TAG, "handle activity start finish, reset failCount, targetBuffer:" + targetBuffer);
            return;
        }
        Slog.e(TAG, "invalid activty start finish msg");
    }

    private void setTargetBuffer() {
        int curBuffer = getCurBufferSize();
        Slog.i(TAG, "current buffer: " + curBuffer);
        targetBuffer = Math.max(targetBuffer, curBuffer);
        targetBuffer = Math.min(maxTargetBuffer, targetBuffer);
        targetBuffer = Math.max(targetBuffer, minTargetBuffer);
        Trace.traceBegin(8, "set targerBuffer:" + targetBuffer);
        int i = targetBuffer;
        setBuffer(i, i - lowBufferStep, highBufferStep + i, swapReserve);
        Trace.traceEnd(8);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGoLauncher() {
        Trace.traceBegin(8, "goLauncher:closeZswapd");
        removeMessagesToStart();
        curState = 1;
        Message msg = handler.obtainMessage();
        msg.what = 5;
        Slog.i(TAG, "handle launcher start, close zswapd");
        closeZswapd();
        handler.sendMessageDelayed(msg, (long) (appStartDelay * 1000));
        Trace.traceEnd(8);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleGoLauncherFinish() {
        if (curState == 1) {
            curReclaimFailCount = 0;
            lastReclaimFailTime = -1;
            Trace.traceBegin(8, "goLauncherFinish:openZswapd:" + defaultSize);
            setBuffer(defaultSize, lowSize, highSize, swapReserve);
            targetBuffer = defaultSize;
            handler.removeMessages(5);
            curState = 0;
            Slog.i(TAG, "handle launcher finish, reset failCount, targetBuffer:" + defaultSize);
            Trace.traceEnd(8);
            return;
        }
        Slog.e(TAG, "invalid go launcher finish msg");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleZswapdStart() {
        Trace.traceBegin(8, "zswapd start");
        int curBuffer = getCurBufferSize();
        Slog.i(TAG, "handle zswapd start, current buffer: " + curBuffer);
        if (curState == 2) {
            handler.removeMessages(7);
            handleZswapdStop();
        }
        if (curState == 0) {
            curState = 2;
            Message msg = handler.obtainMessage();
            msg.what = 7;
            handler.sendMessageDelayed(msg, (long) (reclaimDelay * 1000));
        }
        Trace.traceEnd(8);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleZswapdStop() {
        if (curState == 2) {
            curReclaimFailCount = 0;
            int curBuffer = getCurBufferSize();
            int i = targetBuffer;
            if (curBuffer >= upBufferStep + i) {
                targetBuffer = Math.max(curBuffer, i);
                targetBuffer = Math.min(maxTargetBuffer, targetBuffer);
                targetBuffer = Math.max(targetBuffer, minTargetBuffer);
                Trace.traceBegin(8, "zswapd stop:" + targetBuffer);
                int i2 = targetBuffer;
                setBuffer(i2, i2 - lowBufferStep, highBufferStep + i2, swapReserve);
                Trace.traceEnd(8);
            }
            Slog.i(TAG, "handle zswapd stop, reset failCount, current buffer: " + curBuffer + ", targetBuffer: " + targetBuffer);
            curState = 0;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleZswapdFail() {
        if (curState == 2) {
            handler.removeMessages(7);
            curState = 0;
        }
        if (curState == 0) {
            curReclaimFailCount++;
            Slog.i(TAG, "zswapd fail, now Fail count:" + curReclaimFailCount);
            long curTime = SystemClock.uptimeMillis();
            if (lastReclaimFailTime == -1) {
                lastReclaimFailTime = curTime;
            } else if (reclaimFailCount <= curReclaimFailCount) {
                int curBuffer = getCurBufferSize();
                if (minTargetBuffer == targetBuffer) {
                    Message msg = handler.obtainMessage();
                    Slog.i(TAG, "zswapd keep fail, targetBuffer is min, call kill, curBuf: " + curBuffer);
                    msg.what = 1;
                    handler.sendMessage(msg);
                }
                targetBuffer = curBuffer;
                targetBuffer = Math.min(maxTargetBuffer, targetBuffer);
                targetBuffer = Math.max(targetBuffer, minTargetBuffer);
                Trace.traceBegin(8, "zswapd fail:" + targetBuffer);
                int i = targetBuffer;
                setBuffer(i, i - lowBufferStep, highBufferStep + i, swapReserve);
                Trace.traceEnd(8);
                Slog.i(TAG, "handle zswapd fail a lot, current buffer: " + curBuffer + ", targetBuffer: " + targetBuffer);
                lastReclaimFailTime = curTime;
                curReclaimFailCount = 0;
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleKillStart() {
        if (curState != 1) {
            Slog.i(TAG, "handle kill start, stop zswapd");
            removeMessagesToKill();
            curState = 3;
            closeZswapd();
            Message msg = handler.obtainMessage();
            msg.what = 10;
            handler.sendMessageDelayed(msg, 10000);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleKillStop() {
        if (curState == 3) {
            handler.removeMessages(10);
            int curBuffer = getCurBufferSize();
            targetBuffer = Math.max(curBuffer, targetBuffer);
            targetBuffer = Math.min(maxTargetBuffer, targetBuffer);
            targetBuffer = Math.max(targetBuffer, minTargetBuffer);
            Trace.traceBegin(8, "killFinish:" + targetBuffer);
            int i = targetBuffer;
            setBuffer(i, i - lowBufferStep, highBufferStep + i, swapReserve);
            Trace.traceEnd(8);
            Slog.i(TAG, "handle kill stop end, current buffer: " + curBuffer + ", targetBuffer: " + targetBuffer);
            curState = 0;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleIdleIncrease() {
        int i;
        int i2;
        if (curState == 0 && (i = targetBuffer) < (i2 = raiseBufferMax)) {
            targetBuffer = i + raiseBufferStep;
            targetBuffer = Math.min(targetBuffer, i2);
            Trace.traceBegin(8, "increaseBuffer:" + targetBuffer);
            int i3 = targetBuffer;
            setBuffer(i3, i3 - lowBufferStep, highBufferStep + i3, swapReserve);
            Trace.traceEnd(8);
        }
        Message msg = handler.obtainMessage();
        msg.what = 11;
        handler.sendMessageDelayed(msg, (long) (raiseBufferTimeWidth * 1000));
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleFutileKill() {
        handler.removeMessages(15);
        Slog.i(TAG, "handle futile kill, close zswapd");
        closeZswapd();
        Message msg = handler.obtainMessage();
        msg.what = 15;
        handler.sendMessageDelayed(msg, (long) futileCloseDelay);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handleRecoverZswapd() {
        handler.removeMessages(15);
        setTargetBuffer();
        Slog.i(TAG, "handle recover zswapd, set target to current");
    }

    public void notifyToSetBufferScreenClose() {
        KernelInterface kernelInterface2 = kernelInterface;
        int i = defaultSize;
        int i2 = bufferScreenOffChange;
        kernelInterface2.setBuffer(i + i2, lowSize + i2, highSize + i2, swapReserve);
        targetBuffer = defaultSize + bufferScreenOffChange;
    }
}
