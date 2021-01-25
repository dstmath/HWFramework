package com.android.server.rms.iaware.cpu;

import android.content.Context;
import android.rms.iaware.AwareLog;
import com.android.server.rms.iaware.sysload.SysLoadManager;
import com.huawei.android.pgmng.plug.PowerKit;

public class CpuFreqInteractive {
    private static final String TAG = "CpuFreqInteractive";
    private CpuAppRecogMngProxy mCpuAppRecogMngProxy;
    private PowerKit.Sink mFreqInteractiveSink = new FreqInteractiveSink();

    public CpuFreqInteractive(Context context) {
        this.mCpuAppRecogMngProxy = new CpuAppRecogMngProxy(context);
    }

    public void startGameStateMoniter() {
        this.mCpuAppRecogMngProxy.register(this.mFreqInteractiveSink);
    }

    public void stopGameStateMoniter() {
        this.mCpuAppRecogMngProxy.unregister(this.mFreqInteractiveSink);
    }

    private class FreqInteractiveSink implements PowerKit.Sink {
        private FreqInteractiveSink() {
        }

        public void onStateChanged(int stateType, int eventType, int pid, String pkg, int uid) {
            AwareLog.d(CpuFreqInteractive.TAG, "onStateChanged stateType = " + stateType + " eventType = " + eventType + " pid = " + pid + " pkg = " + pkg);
            if (CpuFreqInteractive.this.mCpuAppRecogMngProxy.isGameType(stateType)) {
                if (eventType == 1) {
                    CpuThreadBoost.getInstance().resetBoostCpus();
                    CpuGameScene.getInstance().enterGameSceneMsg();
                    SysLoadManager.getInstance().enterGameSceneMsg();
                }
                if (eventType == 2) {
                    CpuThreadBoost.getInstance().setBoostCpus();
                    CpuGameScene.getInstance().exitGameSceneMsg();
                    SysLoadManager.getInstance().exitGameSceneMsg();
                }
            } else if (CpuFreqInteractive.this.mCpuAppRecogMngProxy.isVideoType(stateType)) {
                if (stateType == 10015) {
                    CpuThreadBoost.getInstance().resetBoostCpus();
                }
                if (stateType == 10016) {
                    CpuThreadBoost.getInstance().setBoostCpus();
                }
            }
        }
    }
}
