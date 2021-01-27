package com.android.server.rms.iaware.cpu;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.rms.iaware.AwareLog;
import com.huawei.android.pgmng.plug.PowerKit;
import java.util.concurrent.atomic.AtomicBoolean;

public class CpuAppRecogMngProxy {
    private static final int CONNECT_PG_DELAYED = 5000;
    private static final int CYCLE_MAX_NUM = 6;
    private static final int MSG_PG_CONNECT = 1;
    private static final String TAG = "CpuAppRecogMngProxy";
    private Context mContext;
    private CpuAppRecogMngProxyHandler mCpuAppRecogMngProxyHandler;
    private int mCycleNum = 0;
    private PowerKit mPgSdk = null;
    private AtomicBoolean mRegistered = new AtomicBoolean(false);
    private PowerKit.Sink mSink = null;

    public CpuAppRecogMngProxy(Context context) {
        this.mContext = context;
        this.mCpuAppRecogMngProxyHandler = new CpuAppRecogMngProxyHandler();
        getPgSdk();
    }

    /* access modifiers changed from: private */
    public class CpuAppRecogMngProxyHandler extends Handler {
        private CpuAppRecogMngProxyHandler() {
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what != 1) {
                AwareLog.w(CpuAppRecogMngProxy.TAG, "msg.what = " + msg.what + " is Invalid !");
                return;
            }
            CpuAppRecogMngProxy.this.getPgSdk();
        }
    }

    private void callPgRegisterListener() {
        PowerKit.Sink sink;
        if (!this.mRegistered.get()) {
            PowerKit powerKit = this.mPgSdk;
            if (powerKit == null || (sink = this.mSink) == null) {
                AwareLog.e(TAG, "callPgRegisterListener mPgSdk == null");
                return;
            }
            try {
                powerKit.enableStateEvent(sink, 10011);
                this.mPgSdk.enableStateEvent(this.mSink, 10002);
                this.mPgSdk.enableStateEvent(this.mSink, 10015);
                this.mPgSdk.enableStateEvent(this.mSink, 10016);
                this.mRegistered.set(true);
            } catch (RemoteException e) {
                this.mPgSdk = null;
                this.mCycleNum = 0;
                AwareLog.e(TAG, "mPgSdk registerSink && enableStateEvent happend RemoteException!");
            }
        }
    }

    private void callPgUnregisterListener() {
        if (this.mPgSdk != null && this.mRegistered.get()) {
            try {
                this.mPgSdk.disableStateEvent(this.mSink, 10011);
                this.mPgSdk.disableStateEvent(this.mSink, 10002);
                this.mPgSdk.disableStateEvent(this.mSink, 10015);
                this.mPgSdk.disableStateEvent(this.mSink, 10016);
                this.mRegistered.set(false);
            } catch (RemoteException e) {
                AwareLog.e(TAG, "callPgUnregisterListener happend RemoteException!");
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void getPgSdk() {
        int i;
        if (this.mPgSdk == null) {
            this.mPgSdk = PowerKit.getInstance();
            if (this.mPgSdk != null || (i = this.mCycleNum) >= 6) {
                callPgRegisterListener();
                return;
            }
            this.mCycleNum = i + 1;
            this.mCpuAppRecogMngProxyHandler.removeMessages(1);
            this.mCpuAppRecogMngProxyHandler.sendEmptyMessageDelayed(1, 5000);
        }
    }

    public boolean isGameType(int stateType) {
        if (stateType == 10011 || stateType == 10002) {
            return true;
        }
        return false;
    }

    public boolean isVideoType(int stateType) {
        if (stateType == 10015 || stateType == 10016) {
            return true;
        }
        return false;
    }

    public void register(PowerKit.Sink sink) {
        if (this.mSink == null && sink != null) {
            this.mSink = sink;
            callPgRegisterListener();
        }
    }

    public void unregister(PowerKit.Sink sink) {
        if (this.mSink == sink && sink != null) {
            callPgUnregisterListener();
            this.mSink = null;
        }
    }
}
