package com.android.server.rms.iaware.cpu;

import android.content.Context;
import android.rms.iaware.AwareLog;
import com.android.server.rms.iaware.sysload.SysLoadManager;
import com.huawei.pgmng.plug.PGSdk;

class CPUFreqInteractive {
    private static final String TAG = "CPUFreqInteractive";
    /* access modifiers changed from: private */
    public CPUAppRecogMngProxy mCPUAppRecogMngProxy;
    private PGSdk.Sink mFreqInteractiveSink = new FreqInteractiveSink();

    private class FreqInteractiveSink implements PGSdk.Sink {
        private FreqInteractiveSink() {
        }

        public void onStateChanged(int stateType, int eventType, int pid, String pkg, int uid) {
            AwareLog.d(CPUFreqInteractive.TAG, "onStateChanged stateType = " + stateType + " eventType = " + eventType + " pid = " + pid + " pkg = " + pkg);
            if (CPUFreqInteractive.this.mCPUAppRecogMngProxy.isGameType(stateType)) {
                if (eventType == 1) {
                    CpuThreadBoost.getInstance().resetBoostCpus();
                    CPUGameScene.getInstance().enterGameSceneMsg();
                    SysLoadManager.getInstance().enterGameSceneMsg();
                } else if (eventType == 2) {
                    CpuThreadBoost.getInstance().setBoostCpus();
                    CPUGameScene.getInstance().exitGameSceneMsg();
                    SysLoadManager.getInstance().exitGameSceneMsg();
                }
            } else if (!CPUFreqInteractive.this.mCPUAppRecogMngProxy.isVideoType(stateType)) {
            } else {
                if (stateType == 10015) {
                    CpuThreadBoost.getInstance().resetBoostCpus();
                } else if (stateType == 10016) {
                    CpuThreadBoost.getInstance().setBoostCpus();
                }
            }
        }
    }

    public CPUFreqInteractive(Context context) {
        this.mCPUAppRecogMngProxy = new CPUAppRecogMngProxy(context);
    }

    public void startGameStateMoniter() {
        this.mCPUAppRecogMngProxy.register(this.mFreqInteractiveSink);
    }

    public void stopGameStateMoniter() {
        this.mCPUAppRecogMngProxy.unregister(this.mFreqInteractiveSink);
    }
}
