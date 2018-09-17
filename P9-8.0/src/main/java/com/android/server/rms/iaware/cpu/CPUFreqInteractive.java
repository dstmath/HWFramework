package com.android.server.rms.iaware.cpu;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.rms.iaware.AwareLog;
import com.huawei.displayengine.IDisplayEngineService;
import com.huawei.pgmng.plug.PGSdk.Sink;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

class CPUFreqInteractive {
    private static final int CHANGE_FREQUENCY_DELAYED = 3000;
    private static final int FULLSCREEN_DELAY_TIME = 5000;
    private static final int MSG_FULLSCREEN_ENTER = 1;
    private static final int MSG_FULLSCREEN_EXIT = 2;
    private static final int NON_SAVE_POWER_MODE = 1;
    private static final String TAG = "CPUFreqInteractive";
    private static AtomicBoolean mIsFeatureEnable = new AtomicBoolean(false);
    private CPUAppRecogMngProxy mCPUAppRecogMngProxy;
    private CPUFeature mCPUFeatureInstance;
    private AtomicInteger mCurrPid = new AtomicInteger(0);
    private Sink mFreqInteractiveSink;
    private FreqInteractiveHandler mHandler = new FreqInteractiveHandler(this, null);
    private int mPowerMode = 1;

    private class FreqInteractiveHandler extends Handler {
        /* synthetic */ FreqInteractiveHandler(CPUFreqInteractive this$0, FreqInteractiveHandler -this1) {
            this();
        }

        private FreqInteractiveHandler() {
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    CPUFreqInteractive.this.doFullscreenEnter();
                    return;
                case 2:
                    CPUFreqInteractive.this.doFullscreenExit();
                    return;
                case CPUFeature.MSG_SET_FREQUENCY /*112*/:
                    CPUFreqInteractive.this.setFrequency();
                    return;
                case CPUFeature.MSG_RESET_FREQUENCY /*113*/:
                    CPUFreqInteractive.this.resetFrequency();
                    return;
                default:
                    AwareLog.w(CPUFreqInteractive.TAG, "handleMessage default msg what = " + msg.what);
                    return;
            }
        }
    }

    private class FreqInteractiveSink implements Sink {
        /* synthetic */ FreqInteractiveSink(CPUFreqInteractive this$0, FreqInteractiveSink -this1) {
            this();
        }

        private FreqInteractiveSink() {
        }

        public void onStateChanged(int stateType, int eventType, int pid, String pkg, int uid) {
            AwareLog.d(CPUFreqInteractive.TAG, "onStateChanged stateType = " + stateType + " eventType = " + eventType + " pid = " + pid + " pkg = " + pkg);
            CPUFreqInteractive.this.removeFreqMessages();
            if (CPUFreqInteractive.this.mCPUAppRecogMngProxy.isGameType(stateType)) {
                if (eventType == 1) {
                    CpuThreadBoost.getInstance().resetBoostCpus();
                    CPUGameScene.getInstance().enterGameSceneMsg();
                    CPUGesturePointerBoost.getInstance().enterGameScene();
                } else if (eventType == 2) {
                    CpuThreadBoost.getInstance().setBoostCpus();
                    CPUGameScene.getInstance().exitGameSceneMsg();
                    CPUGesturePointerBoost.getInstance().exitGameScene();
                }
            } else if (!CPUFreqInteractive.this.mCPUAppRecogMngProxy.isVideoType(stateType)) {
            } else {
                if (stateType == IDisplayEngineService.DE_ACTION_PG_VIDEO_START) {
                    CPUFreqInteractive.this.resetFreqMsg();
                    CpuThreadBoost.getInstance().resetBoostCpus();
                } else if (stateType == IDisplayEngineService.DE_ACTION_PG_VIDEO_END) {
                    CPUFreqInteractive.this.setFreqMsg();
                    CpuThreadBoost.getInstance().setBoostCpus();
                }
            }
        }
    }

    public CPUFreqInteractive(CPUFeature feature, Context context) {
        this.mCPUFeatureInstance = feature;
        this.mCPUAppRecogMngProxy = new CPUAppRecogMngProxy(context);
        this.mFreqInteractiveSink = new FreqInteractiveSink(this, null);
    }

    private boolean isBootCompleted() {
        return "1".equals(SystemProperties.get("sys.boot_completed", "0"));
    }

    public void enable() {
        if (mIsFeatureEnable.get()) {
            AwareLog.e(TAG, "CPUFreqInteractive has already enable!");
            return;
        }
        mIsFeatureEnable.set(true);
        if (CPUPowerMode.isPerformanceMode()) {
            if (isBootCompleted()) {
                this.mHandler.sendEmptyMessageDelayed(CPUFeature.MSG_SET_FREQUENCY, 0);
            } else {
                this.mHandler.sendEmptyMessageDelayed(CPUFeature.MSG_SET_FREQUENCY, 3000);
            }
        }
    }

    public void startGameStateMoniter() {
        this.mCPUAppRecogMngProxy.register(this.mFreqInteractiveSink);
    }

    public void stopGameStateMoniter() {
        this.mCPUAppRecogMngProxy.unregister(this.mFreqInteractiveSink);
    }

    public void disable() {
        if (mIsFeatureEnable.get()) {
            mIsFeatureEnable.set(false);
            this.mHandler.removeMessages(CPUFeature.MSG_SET_FREQUENCY);
            if (!CPUPowerMode.getInstance().isSuperPowerSave()) {
                resetFrequency();
            }
            return;
        }
        AwareLog.e(TAG, "CPUFreqInteractive has already disable!");
    }

    private void removeFreqMessages() {
        this.mHandler.removeMessages(CPUFeature.MSG_SET_FREQUENCY);
        this.mHandler.removeMessages(CPUFeature.MSG_RESET_FREQUENCY);
    }

    private void setFrequency() {
        long time = System.currentTimeMillis();
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(CPUFeature.MSG_SET_FREQUENCY);
        int resCode = this.mCPUFeatureInstance.sendPacket(buffer);
        if (resCode != 1) {
            AwareLog.e(TAG, "setFrequency sendPacket failed, send error code:" + resCode);
        }
        CpuDumpRadar.getInstance().insertDumpInfo(time, "setFrequency()", "set cpu frequency", CpuDumpRadar.STATISTICS_CHG_FREQ_POLICY);
    }

    private void resetFrequency() {
        long time = System.currentTimeMillis();
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(CPUFeature.MSG_RESET_FREQUENCY);
        buffer.putInt(this.mPowerMode);
        int resCode = this.mCPUFeatureInstance.sendPacket(buffer);
        if (resCode != 1) {
            AwareLog.e(TAG, "resetFrequency sendPacket failed, send error code:" + resCode);
        }
        CpuDumpRadar.getInstance().insertDumpInfo(time, "resetFrequency()", "reset cpu frequency", CpuDumpRadar.STATISTICS_RESET_FREQ_POLICY);
    }

    private static boolean isSatisfied() {
        return mIsFeatureEnable.get() ? CPUPowerMode.isPerformanceMode() : false;
    }

    public void setFreqMsg() {
        if (isSatisfied()) {
            this.mHandler.sendEmptyMessage(CPUFeature.MSG_SET_FREQUENCY);
        }
    }

    public void resetFreqMsg() {
        if (isSatisfied()) {
            this.mHandler.sendEmptyMessage(CPUFeature.MSG_RESET_FREQUENCY);
        }
    }

    public void notifyToChangeFreq(int msg, int delayTime, int powerMode) {
        if (mIsFeatureEnable.get()) {
            removeFreqMessages();
            this.mHandler.sendEmptyMessageDelayed(msg, (long) delayTime);
            this.mPowerMode = powerMode;
        }
    }

    public void doDied(int pid) {
        if (isSatisfied() && this.mCurrPid.get() == pid) {
            AwareLog.d(TAG, "doDied pid = " + pid);
            this.mHandler.removeMessages(1);
            this.mHandler.sendEmptyMessage(2);
            this.mCurrPid.set(0);
        }
    }

    public void fullscreenChange(int pid, boolean isFullScreen) {
        AwareLog.d(TAG, "fullscreenChange pid = " + pid + " isFullScreen = " + isFullScreen);
        if (isSatisfied()) {
            this.mHandler.removeMessages(1);
            this.mHandler.removeMessages(2);
            if (isFullScreen) {
                this.mHandler.sendEmptyMessageDelayed(1, 5000);
                this.mCurrPid.set(pid);
            } else {
                this.mHandler.sendEmptyMessage(2);
                this.mCurrPid.set(0);
            }
        }
    }

    private void doFullscreenEnter() {
        if (CPUGameScene.getInstance().isGameScene()) {
            AwareLog.d(TAG, "doFullscreenEnter is game scene!");
            return;
        }
        AwareLog.d(TAG, "doFullscreenEnter ok!");
        resetFrequency();
    }

    private void doFullscreenExit() {
        if (CPUGameScene.getInstance().isGameScene()) {
            AwareLog.d(TAG, "doFullscreenExit is game scene!");
            return;
        }
        AwareLog.d(TAG, "doFullscreenExit ok!");
        setFrequency();
    }
}
