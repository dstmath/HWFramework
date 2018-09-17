package com.android.server.rms.iaware.cpu;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.rms.iaware.AwareLog;
import com.android.server.PPPOEStateMachine;
import com.huawei.pgmng.plug.PGSdk.Sink;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

class CPUFreqInteractive {
    private static final String ACTION_ENTER_SUPER_SAVE_MODE = "huawei.intent.action.HWSYSTEMMANAGER_CHANGE_POWERMODE";
    private static final String ACTION_POWER_MODE_CHANGE = "huawei.intent.action.POWER_MODE_CHANGED_ACTION";
    private static final String ACTION_QUIT_SUPER_SAVE_MODE = "huawei.intent.action.HWSYSTEMMANAGER_SHUTDOWN_LIMIT_POWERMODE";
    private static final int CHANGE_FREQUENCY_DELAYED = 3000;
    private static final int NON_SAVE_POWER_MODE = 1;
    private static final String TAG = "CPUFreqInteractive";
    private static AtomicBoolean mIsFeatureEnable;
    private static AtomicBoolean sIsSpecialScene;
    private CPUAppRecogMngProxy mCPUAppRecogMngProxy;
    private CPUFeature mCPUFeatureInstance;
    private Sink mFreqInteractiveSink;
    private FreqInteractiveHandler mHandler;
    private int mPowerMode;

    private class FreqInteractiveHandler extends Handler {
        private FreqInteractiveHandler() {
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case CPUFeature.MSG_SET_FREQUENCY /*112*/:
                    CPUFreqInteractive.this.setFrequency();
                case CPUFeature.MSG_RESET_FREQUENCY /*113*/:
                    CPUFreqInteractive.this.resetFrequency();
                default:
                    AwareLog.w(CPUFreqInteractive.TAG, "handleMessage default msg what = " + msg.what);
            }
        }
    }

    private class FreqInteractiveSink implements Sink {
        private FreqInteractiveSink() {
        }

        public void onStateChanged(int stateType, int eventType, int pid, String pkg, int uid) {
            AwareLog.d(CPUFreqInteractive.TAG, "onStateChanged stateType = " + stateType + " eventType = " + eventType + " pid = " + pid + " pkg = " + pkg);
            CPUFreqInteractive.this.removeFreqMessages();
            if (CPUFreqInteractive.this.mCPUAppRecogMngProxy.isGameType(stateType)) {
                if (eventType == CPUFreqInteractive.NON_SAVE_POWER_MODE) {
                    CPUFreqInteractive.sIsSpecialScene.set(true);
                    CPUFreqInteractive.this.resetFreqMsg();
                } else if (eventType == 2) {
                    CPUFreqInteractive.sIsSpecialScene.set(false);
                    CPUFreqInteractive.this.setFreqMsg();
                }
            } else if (!CPUFreqInteractive.this.mCPUAppRecogMngProxy.isVideoType(stateType)) {
            } else {
                if (stateType == 10015) {
                    CPUFreqInteractive.sIsSpecialScene.set(true);
                    CPUFreqInteractive.this.resetFreqMsg();
                } else if (stateType == 10016) {
                    CPUFreqInteractive.sIsSpecialScene.set(false);
                    CPUFreqInteractive.this.setFreqMsg();
                }
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.rms.iaware.cpu.CPUFreqInteractive.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.rms.iaware.cpu.CPUFreqInteractive.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.rms.iaware.cpu.CPUFreqInteractive.<clinit>():void");
    }

    public CPUFreqInteractive(CPUFeature feature, Context context) {
        this.mPowerMode = NON_SAVE_POWER_MODE;
        this.mHandler = new FreqInteractiveHandler();
        this.mCPUFeatureInstance = feature;
        this.mCPUAppRecogMngProxy = new CPUAppRecogMngProxy(context);
        this.mFreqInteractiveSink = new FreqInteractiveSink();
    }

    private boolean isBootCompleted() {
        return PPPOEStateMachine.PHASE_INITIALIZE.equals(SystemProperties.get("sys.boot_completed", PPPOEStateMachine.PHASE_DEAD));
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
        if (resCode != NON_SAVE_POWER_MODE) {
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
        if (resCode != NON_SAVE_POWER_MODE) {
            AwareLog.e(TAG, "resetFrequency sendPacket failed, send error code:" + resCode);
        }
        CpuDumpRadar.getInstance().insertDumpInfo(time, "resetFrequency()", "reset cpu frequency", CpuDumpRadar.STATISTICS_RESET_FREQ_POLICY);
    }

    private static boolean isSatisfied() {
        return mIsFeatureEnable.get() ? CPUPowerMode.isPerformanceMode() : false;
    }

    public static boolean isFGSpecialScene() {
        return sIsSpecialScene.get();
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
}
