package com.huawei.android.hardware.mtkfmradio;

import android.content.Context;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import com.huawei.android.hardware.fmradio.IBaseFmRxEvCallbacksAdaptor;
import com.huawei.android.hardware.fmradio.common.BaseFmConfig;
import com.huawei.android.hardware.fmradio.common.BaseHwFmService;
import com.huawei.android.util.SlogEx;

public class MtkFmService extends BaseHwFmService implements IFmHidlClientCallbacks {
    private static final String TAG = "MtkFmService";
    private final Callbacks mCallbacks;
    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        /* class com.huawei.android.hardware.mtkfmradio.MtkFmService.AnonymousClass1 */

        public void binderDied() {
            if (MtkFmService.this.mICallback != null) {
                MtkFmService.this.mICallback.unlinkToDeath(MtkFmService.this.mDeathRecipient, 0);
            }
            MtkFmService.this.disable();
            MtkFmService.this.mICallback = null;
            SlogEx.w(MtkFmService.TAG, "binderDied ,disable");
        }
    };
    private FmState mFmState;
    private int mGrpMask;
    private FmHidlClient mHidlClient;
    private IBinder mICallback;
    private final Object mLock = new Object();

    public MtkFmService(Context context) {
        Looper looper = FmFgThread.getLooper();
        this.mCallbacks = new Callbacks(looper == null ? Looper.myLooper() : looper);
        this.mFmState = new FmState();
        this.mHidlClient = new FmHidlClient(this, this.mFmState);
        SlogEx.d(TAG, "new HwFmService:mHidlClient " + this.mHidlClient);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int getRdsStatus() {
        int rdsEvent;
        SlogEx.d(TAG, "getRdsStatus");
        synchronized (this.mLock) {
            rdsEvent = this.mHidlClient.getRdsStatus();
        }
        return rdsEvent;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int getRssi() {
        int rssi;
        SlogEx.d(TAG, "getRssi");
        synchronized (this.mLock) {
            rssi = this.mHidlClient.getRssi();
        }
        return rssi;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int[] getAfInfo() {
        int[] afInfo;
        SlogEx.d(TAG, "getAfInfo");
        synchronized (this.mLock) {
            afInfo = this.mHidlClient.getAfInfo();
        }
        return afInfo;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public boolean registerRdsGroupProcessing(int fmGrpsToProc) {
        SlogEx.d(TAG, "registerRdsGroupProcessing fmGrpsToProc :" + fmGrpsToProc);
        synchronized (this.mLock) {
            int state = this.mFmState.getSyncFmPowerState();
            if (state != 2) {
                if (state != 1) {
                    boolean status = this.mHidlClient.setRdsOnOff(1);
                    if (status) {
                        this.mGrpMask = fmGrpsToProc;
                    }
                    return status;
                }
            }
            SlogEx.d(TAG, "registerRdsGroupProcessing: Device currently busy");
            return false;
        }
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public boolean enable(BaseFmConfig configSettings, IBinder callback) {
        boolean status;
        SlogEx.d(TAG, "enable");
        synchronized (this.mLock) {
            this.mICallback = callback;
            try {
                callback.linkToDeath(this.mDeathRecipient, 0);
            } catch (RemoteException e) {
                SlogEx.e(TAG, "can't link to death");
            }
            int state = this.mFmState.getSyncFmPowerState();
            SlogEx.d(TAG, "getSyncFmPowerState:" + state);
            this.mFmState.setFmPowerState(4);
            SlogEx.d(TAG, "enable: CURRENT-STATE : FMOff ---> NEW-STATE : FMRxStarting");
            status = this.mHidlClient.enable(configSettings);
            if (status) {
                this.mFmState.setFmPowerState(0);
            } else {
                SlogEx.e(TAG, "FM enable fail");
                this.mFmState.setFmPowerState(2);
            }
        }
        return status;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public boolean disable() {
        SlogEx.d(TAG, "disable");
        synchronized (this.mLock) {
            int state = this.mFmState.getSyncFmPowerState();
            SlogEx.d(TAG, "getSyncFmPowerState:" + state);
            if (state == 1) {
                SlogEx.d(TAG, "Cancel the search operation to disable the FM");
                this.mFmState.setSearchState(4);
                mtkCancelSearch();
            } else if (state == 2) {
                SlogEx.d(TAG, "FM is alreayd turned off");
            } else if (state == 4) {
                state = this.mFmState.getSyncFmPowerState();
                if (state == 4) {
                    SlogEx.e(TAG, "FM is in bad state");
                    return false;
                }
            } else if (state == 6) {
                SlogEx.e(TAG, "FM is in process to tuned off");
                return false;
            }
            this.mFmState.setFmPowerState(2);
            boolean status = this.mHidlClient.disable();
            SlogEx.d(TAG, "getSyncFmPowerState after disable:" + state);
            return status;
        }
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public boolean mtkCancelSearch() {
        SlogEx.d(TAG, "cancelSearch");
        boolean status = false;
        synchronized (this.mLock) {
            if (this.mFmState.getSyncFmPowerState() == 1) {
                this.mFmState.setSearchState(4);
                status = this.mHidlClient.cancelSearch();
                SlogEx.d(TAG, "status :" + status);
            } else {
                SlogEx.d(TAG, "No search opeartion");
            }
        }
        return status;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public boolean searchStations(int mode, int dwellPeriod, int direction, int pty, int pi) {
        boolean status;
        boolean status2;
        SlogEx.d(TAG, "searchStations");
        boolean status3 = true;
        synchronized (this.mLock) {
            try {
                int state = this.mFmState.getSyncFmPowerState();
                SlogEx.d(TAG, "searchStations :state:" + state);
                if (state != 1) {
                    if (state != 2) {
                        if (!(mode == 1 || mode == 0)) {
                            SlogEx.d(TAG, "invalid serach mode" + mode);
                            status3 = false;
                        }
                        if (dwellPeriod < 1 || dwellPeriod > 7) {
                            SlogEx.d(TAG, "invalid dwelling time" + dwellPeriod);
                            status3 = false;
                        }
                        if (direction == 1 || direction == 0) {
                            status = status3;
                        } else {
                            SlogEx.d(TAG, "invalid direction" + direction);
                            status = false;
                        }
                        if (status) {
                            if (mode == 1) {
                                try {
                                    this.mFmState.setSearchState(1);
                                } catch (Throwable th) {
                                    th = th;
                                    throw th;
                                }
                            } else if (mode == 0) {
                                this.mFmState.setSearchState(0);
                            }
                            status2 = this.mHidlClient.searchStations(mode, dwellPeriod, direction, pty, pi);
                            if (!status2) {
                                if (this.mFmState.getSyncFmPowerState() == 1) {
                                    this.mFmState.setSearchState(3);
                                }
                                status2 = false;
                            }
                        } else {
                            status2 = status;
                        }
                        return status2;
                    }
                }
                SlogEx.e(TAG, "FM is busy in another command");
                return false;
            } catch (Throwable th2) {
                th = th2;
                throw th;
            }
        }
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public boolean setStation(int frequencyKHz) {
        SlogEx.d(TAG, "setStation :" + frequencyKHz);
        synchronized (this.mLock) {
            if (this.mHidlClient.setFreq(frequencyKHz) != 0) {
                return false;
            }
            return true;
        }
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public boolean setRdsOnOff(int onOff) {
        boolean status;
        SlogEx.d(TAG, "setRdsOnOff onOff :" + onOff);
        synchronized (this.mLock) {
            status = this.mHidlClient.setRdsOnOff(onOff);
        }
        return status;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public boolean setLowPwrMode(boolean lpmode) {
        boolean status;
        SlogEx.d(TAG, "setLowPwrMode lpmode :" + lpmode);
        synchronized (this.mLock) {
            status = this.mHidlClient.setLowPwrMode(lpmode);
        }
        return status;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public String getPrgmServices() {
        String psString;
        SlogEx.d(TAG, "getPrgmServices");
        synchronized (this.mLock) {
            psString = this.mHidlClient.getPrgmServices();
        }
        return psString;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public String getRadioText() {
        String rtString;
        SlogEx.d(TAG, "getRadioText");
        synchronized (this.mLock) {
            rtString = this.mHidlClient.getRadioText();
        }
        return rtString;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int getPrgmType() {
        int pTy;
        SlogEx.d(TAG, "getPrgmType");
        synchronized (this.mLock) {
            pTy = this.mHidlClient.getPrgmType();
        }
        return pTy;
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public int getPrgmId() {
        int pI;
        SlogEx.d(TAG, "getPrgmId");
        synchronized (this.mLock) {
            pI = this.mHidlClient.getPrgmId();
        }
        return pI;
    }

    private static class Callbacks extends Handler {
        private static final int MSG_RDS_AF_LIST_CHANGED = 9;
        private static final int MSG_RDS_AVAL_CHANGED = 6;
        private static final int MSG_RDS_PS_CHANGED = 7;
        private static final int MSG_RDS_RT_CHANGED = 8;
        private static final int MSG_RDS_STATE_CHANGED = 2;
        private static final int MSG_SEARCH_CANCELLED = 4;
        private static final int MSG_SEARCH_COMPLETED = 5;
        private static final int MSG_SEARCH_IN_PROGRESS = 3;
        private static final int MSG_TUNE_STATE_CHANGED = 1;
        private final RemoteCallbackList<IBaseFmRxEvCallbacksAdaptor> mCallbacks = new RemoteCallbackList<>();

        public Callbacks(Looper looper) {
            super(looper);
        }

        public void register(IBaseFmRxEvCallbacksAdaptor callback) {
            this.mCallbacks.register(callback);
        }

        public void unregister(IBaseFmRxEvCallbacksAdaptor callback) {
            this.mCallbacks.unregister(callback);
        }

        public void handleMessage(Message msg) {
            SomeArgs args = (SomeArgs) msg.obj;
            int n = this.mCallbacks.beginBroadcast();
            for (int i = 0; i < n; i++) {
                try {
                    invokeCallback(this.mCallbacks.getBroadcastItem(i), msg.what, args);
                } catch (RemoteException e) {
                }
            }
            this.mCallbacks.finishBroadcast();
            try {
                args.recycle();
            } catch (IllegalStateException e2) {
                SlogEx.e(MtkFmService.TAG, "handleMessage(): SomeArgs recycle fail.");
            }
        }

        private void invokeCallback(IBaseFmRxEvCallbacksAdaptor callback, int what, SomeArgs args) throws RemoteException {
            switch (what) {
                case 1:
                    callback.FmRxEvRadioTuneStatus(((Integer) args.arg1).intValue());
                    return;
                case 2:
                    callback.FmRxEvRdsLockStatus(((Boolean) args.arg1).booleanValue());
                    return;
                case 3:
                    callback.FmRxEvSearchInProgress();
                    return;
                case 4:
                    callback.FmRxEvSearchCancelled();
                    return;
                case 5:
                    callback.FmRxEvSearchComplete(((Integer) args.arg1).intValue());
                    return;
                case 6:
                    callback.FmRxEvRdsInfo(((Integer) args.arg1).intValue());
                    return;
                case 7:
                    callback.FmRxEvRdsPsInfo();
                    return;
                case 8:
                    callback.FmRxEvRdsRtInfo();
                    return;
                case 9:
                    callback.FmRxEvRdsAfInfo();
                    return;
                default:
                    return;
            }
        }

        public void FmRxEvRadioTuneStatus(int freq) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = Integer.valueOf(freq);
            obtainMessage(1, args).sendToTarget();
        }

        public void FmRxEvRdsLockStatus(boolean avail) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = Boolean.valueOf(avail);
            obtainMessage(2, args).sendToTarget();
        }

        public void FmRxEvSearchInProgress() {
            obtainMessage(3, SomeArgs.obtain()).sendToTarget();
        }

        public void FmRxEvSearchCancelled() {
            obtainMessage(4, SomeArgs.obtain()).sendToTarget();
        }

        public void FmRxEvSearchComplete(int freq) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = Integer.valueOf(freq);
            obtainMessage(5, args).sendToTarget();
        }

        public void FmRxEvRdsInfo(int event) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = Integer.valueOf(event);
            obtainMessage(6, args).sendToTarget();
        }

        public void FmRxEvRdsPsInfo() {
            obtainMessage(7, SomeArgs.obtain()).sendToTarget();
        }

        public void FmRxEvRdsRtInfo() {
            obtainMessage(8, SomeArgs.obtain()).sendToTarget();
        }

        public void FmRxEvRdsAfInfo() {
            obtainMessage(9, SomeArgs.obtain()).sendToTarget();
        }
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public void registerListener(IBaseFmRxEvCallbacksAdaptor listener) {
        this.mCallbacks.register(listener);
    }

    @Override // com.huawei.android.hardware.fmradio.common.BaseHwFmService, com.huawei.android.hardware.fmradio.common.BaseHwFmServiceInterface
    public void unregisterListener(IBaseFmRxEvCallbacksAdaptor listener) {
        this.mCallbacks.unregister(listener);
    }

    @Override // com.huawei.android.hardware.mtkfmradio.IFmHidlClientCallbacks
    public void onRadioTunedStatus(int freq) {
        this.mCallbacks.FmRxEvRadioTuneStatus(freq);
    }

    @Override // com.huawei.android.hardware.mtkfmradio.IFmHidlClientCallbacks
    public void onRdsLockedStatus(boolean rdsAvail) {
        this.mCallbacks.FmRxEvRdsLockStatus(rdsAvail);
    }

    @Override // com.huawei.android.hardware.mtkfmradio.IFmHidlClientCallbacks
    public void onSearchInProgress() {
        this.mCallbacks.FmRxEvSearchInProgress();
    }

    @Override // com.huawei.android.hardware.mtkfmradio.IFmHidlClientCallbacks
    public void onSearchCancelled() {
        this.mCallbacks.FmRxEvSearchCancelled();
    }

    @Override // com.huawei.android.hardware.mtkfmradio.IFmHidlClientCallbacks
    public void onSearchComplete(int freq) {
        this.mCallbacks.FmRxEvSearchComplete(freq);
    }

    @Override // com.huawei.android.hardware.mtkfmradio.IFmHidlClientCallbacks
    public void onRdsInfo(int iRdsEvents) {
        SlogEx.d(TAG, "onRdsInfo iRdsEvents:" + iRdsEvents);
        if (8 == (iRdsEvents & 8)) {
            int i = this.mGrpMask;
            if ((i & 2) == 2 || (i & 16) == 16) {
                this.mCallbacks.FmRxEvRdsPsInfo();
            }
        }
        if (64 == (iRdsEvents & 64) && (this.mGrpMask & 1) == 1) {
            this.mCallbacks.FmRxEvRdsRtInfo();
        }
        if (128 == (iRdsEvents & 128) && (this.mGrpMask & 4) == 4) {
            this.mCallbacks.FmRxEvRdsAfInfo();
        }
        if (2 == (iRdsEvents & 2) || 4 == (iRdsEvents & 4)) {
            this.mCallbacks.FmRxEvRdsInfo(iRdsEvents);
        }
    }
}
