package com.huawei.android.hardware.mtkfmradio;

import android.content.Context;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.hardware.fmradio.IBaseFmRxEvCallbacksAdaptor;
import com.huawei.android.hardware.fmradio.IHwFmService;
import com.huawei.android.hardware.fmradio.common.BaseFmConfig;
import com.huawei.android.hardware.fmradio.common.FmUtils;
import com.huawei.android.os.ServiceManagerEx;
import java.util.ArrayList;
import java.util.Iterator;

/* access modifiers changed from: package-private */
public class HwFmManager {
    private static final String TAG = "HwFmManager";
    private final ArrayList<FmRxEvCallbacksAdaptorDelegate> mDelegates = new ArrayList<>();
    private final IHwFmService mHwFmService;
    private final IBinder mICallback = new Binder();
    private final Object mLock = new Object();
    private final Looper mLooper;

    /* access modifiers changed from: private */
    public static class FmRxEvCallbacksAdaptorDelegate extends IBaseFmRxEvCallbacksAdaptor.Stub implements Handler.Callback {
        private static final int MSG_RDS_AF_LIST_CHANGED = 9;
        private static final int MSG_RDS_AVAL_CHANGED = 6;
        private static final int MSG_RDS_PS_CHANGED = 7;
        private static final int MSG_RDS_RT_CHANGED = 8;
        private static final int MSG_RDS_STATE_CHANGED = 2;
        private static final int MSG_SEARCH_CANCELLED = 4;
        private static final int MSG_SEARCH_COMPLETED = 5;
        private static final int MSG_SEARCH_IN_PROGRESS = 3;
        private static final int MSG_TUNE_STATE_CHANGED = 1;
        final MtkFmRxEvCallbacksAdaptor mCallback;
        final Handler mHandler;

        public FmRxEvCallbacksAdaptorDelegate(MtkFmRxEvCallbacksAdaptor callback, Looper looper) {
            this.mCallback = callback;
            this.mHandler = new Handler(looper, this);
        }

        @Override // android.os.Handler.Callback
        public boolean handleMessage(Message msg) {
            SomeArgs args = (SomeArgs) msg.obj;
            switch (msg.what) {
                case 1:
                    this.mCallback.FmRxEvRadioTuneStatus(((Integer) args.arg1).intValue());
                    recycleSomeArgs(args);
                    return true;
                case 2:
                    this.mCallback.FmRxEvRdsLockStatus(((Boolean) args.arg1).booleanValue());
                    recycleSomeArgs(args);
                    return true;
                case 3:
                    this.mCallback.FmRxEvSearchInProgress();
                    recycleSomeArgs(args);
                    return true;
                case 4:
                    this.mCallback.FmRxEvSearchCancelled();
                    recycleSomeArgs(args);
                    return true;
                case 5:
                    this.mCallback.FmRxEvSearchComplete(((Integer) args.arg1).intValue());
                    recycleSomeArgs(args);
                    return true;
                case 6:
                    this.mCallback.FmRxEvRdsInfo(((Integer) args.arg1).intValue());
                    recycleSomeArgs(args);
                    return true;
                case 7:
                    this.mCallback.FmRxEvRdsPsInfo();
                    recycleSomeArgs(args);
                    return true;
                case 8:
                    this.mCallback.FmRxEvRdsRtInfo();
                    recycleSomeArgs(args);
                    return true;
                case 9:
                    this.mCallback.FmRxEvRdsAfInfo();
                    recycleSomeArgs(args);
                    return true;
                default:
                    recycleSomeArgs(args);
                    return false;
            }
        }

        private void recycleSomeArgs(SomeArgs someArgs) {
            if (someArgs != null) {
                try {
                    someArgs.recycle();
                } catch (IllegalStateException e) {
                    Log.e(HwFmManager.TAG, "recycleSomeArgs(): fail.");
                }
            }
        }

        @Override // com.huawei.android.hardware.fmradio.IBaseFmRxEvCallbacksAdaptor
        public void FmRxEvRadioTuneStatus(int freq) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = Integer.valueOf(freq);
            this.mHandler.obtainMessage(1, args).sendToTarget();
        }

        @Override // com.huawei.android.hardware.fmradio.IBaseFmRxEvCallbacksAdaptor
        public void FmRxEvRdsLockStatus(boolean avail) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = Boolean.valueOf(avail);
            this.mHandler.obtainMessage(2, args).sendToTarget();
        }

        @Override // com.huawei.android.hardware.fmradio.IBaseFmRxEvCallbacksAdaptor
        public void FmRxEvSearchInProgress() {
            this.mHandler.obtainMessage(3, SomeArgs.obtain()).sendToTarget();
        }

        @Override // com.huawei.android.hardware.fmradio.IBaseFmRxEvCallbacksAdaptor
        public void FmRxEvSearchCancelled() {
            this.mHandler.obtainMessage(4, SomeArgs.obtain()).sendToTarget();
        }

        @Override // com.huawei.android.hardware.fmradio.IBaseFmRxEvCallbacksAdaptor
        public void FmRxEvSearchComplete(int freq) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = Integer.valueOf(freq);
            this.mHandler.obtainMessage(5, args).sendToTarget();
        }

        @Override // com.huawei.android.hardware.fmradio.IBaseFmRxEvCallbacksAdaptor
        public void FmRxEvRdsInfo(int event) {
            SomeArgs args = SomeArgs.obtain();
            args.arg1 = Integer.valueOf(event);
            this.mHandler.obtainMessage(6, args).sendToTarget();
        }

        @Override // com.huawei.android.hardware.fmradio.IBaseFmRxEvCallbacksAdaptor
        public void FmRxEvRdsPsInfo() {
            this.mHandler.obtainMessage(7, SomeArgs.obtain()).sendToTarget();
        }

        @Override // com.huawei.android.hardware.fmradio.IBaseFmRxEvCallbacksAdaptor
        public void FmRxEvRdsRtInfo() {
            this.mHandler.obtainMessage(8, SomeArgs.obtain()).sendToTarget();
        }

        @Override // com.huawei.android.hardware.fmradio.IBaseFmRxEvCallbacksAdaptor
        public void FmRxEvRdsAfInfo() {
            this.mHandler.obtainMessage(9, SomeArgs.obtain()).sendToTarget();
        }
    }

    public void registerListener(MtkFmRxEvCallbacksAdaptor listener) {
        synchronized (this.mDelegates) {
            FmRxEvCallbacksAdaptorDelegate delegate = new FmRxEvCallbacksAdaptorDelegate(listener, this.mLooper);
            try {
                this.mHwFmService.registerListener(delegate);
            } catch (RemoteException e) {
                FmUtils.rethrowFromSystemServer(e);
            }
            this.mDelegates.add(delegate);
        }
    }

    public void unregisterListener(MtkFmRxEvCallbacksAdaptor listener) {
        synchronized (this.mDelegates) {
            Iterator<FmRxEvCallbacksAdaptorDelegate> i = this.mDelegates.iterator();
            while (i.hasNext()) {
                FmRxEvCallbacksAdaptorDelegate delegate = i.next();
                if (delegate.mCallback == listener) {
                    try {
                        this.mHwFmService.unregisterListener(delegate);
                    } catch (RemoteException e) {
                        FmUtils.rethrowFromSystemServer(e);
                    }
                    i.remove();
                }
            }
        }
    }

    public HwFmManager(Context context, Looper looper) {
        Log.d(TAG, TAG);
        this.mLooper = looper;
        this.mHwFmService = IHwFmService.Stub.asInterface(ServiceManagerEx.getService("hwfm_service"));
        Log.d(TAG, "mHwFmService" + this.mHwFmService);
    }

    public boolean enable(BaseFmConfig configSettings) {
        boolean status;
        Log.d(TAG, "enable");
        try {
            synchronized (this.mLock) {
                status = this.mHwFmService.enable((MtkFmConfig) configSettings, this.mICallback);
            }
            return status;
        } catch (RemoteException e) {
            throw FmUtils.rethrowFromSystemServer(e);
        }
    }

    public boolean disable() {
        boolean status;
        Log.d(TAG, "disable");
        try {
            synchronized (this.mLock) {
                status = this.mHwFmService.disable();
                Log.d(TAG, "disable:" + status);
            }
            return status;
        } catch (RemoteException e) {
            throw FmUtils.rethrowFromSystemServer(e);
        }
    }

    public boolean cancelSearch() {
        boolean status;
        Log.d(TAG, "cancelSearch");
        try {
            synchronized (this.mLock) {
                status = this.mHwFmService.mtkCancelSearch();
                Log.d(TAG, "status :" + status);
            }
            return status;
        } catch (RemoteException e) {
            throw FmUtils.rethrowFromSystemServer(e);
        }
    }

    public boolean searchStations(int mode, int dwellPeriod, int direction, int pty, int pi) {
        boolean status;
        Log.d(TAG, "searchStations");
        try {
            synchronized (this.mLock) {
                status = this.mHwFmService.searchStations(mode, dwellPeriod, direction, pty, pi);
                Log.d(TAG, "status :" + status);
            }
            return status;
        } catch (RemoteException e) {
            throw FmUtils.rethrowFromSystemServer(e);
        }
    }

    public boolean searchStations(int mode, int dwellPeriod, int direction) {
        boolean status;
        Log.d(TAG, "searchStations");
        try {
            synchronized (this.mLock) {
                status = this.mHwFmService.searchStations(mode, dwellPeriod, direction, 0, 0);
                Log.d(TAG, "status :" + status);
            }
            return status;
        } catch (RemoteException e) {
            throw FmUtils.rethrowFromSystemServer(e);
        }
    }

    public boolean setStation(int frequencyKHz) {
        boolean status;
        Log.d(TAG, "setStation :frequencyKHz:" + frequencyKHz);
        try {
            synchronized (this.mLock) {
                status = this.mHwFmService.setStation(frequencyKHz);
            }
            return status;
        } catch (RemoteException e) {
            throw FmUtils.rethrowFromSystemServer(e);
        }
    }

    public boolean setRdsOnOff(int onOff) {
        Log.d(TAG, "setRdsOnOff :onOff:" + onOff);
        try {
            boolean re = this.mHwFmService.setRdsOnOff(onOff);
            Log.d(TAG, "setRdsOnOff :re:" + re);
            return re;
        } catch (RemoteException e) {
            throw FmUtils.rethrowFromSystemServer(e);
        }
    }

    public boolean setLowPwrMode(boolean lpmode) {
        Log.d(TAG, "setLowPwrMode :lpmode:" + lpmode);
        try {
            boolean re = this.mHwFmService.setLowPwrMode(lpmode);
            Log.d(TAG, "setLowPwrMode :re:" + re);
            return re;
        } catch (RemoteException e) {
            throw FmUtils.rethrowFromSystemServer(e);
        }
    }

    public String getPrgmServices() {
        String psString;
        Log.d(TAG, "getPrgmServices");
        try {
            synchronized (this.mLock) {
                psString = this.mHwFmService.getPrgmServices();
            }
            return psString;
        } catch (RemoteException e) {
            throw FmUtils.rethrowFromSystemServer(e);
        }
    }

    public int getRdsStatus() {
        int rdsEvent;
        Log.d(TAG, "getRdsStatus");
        try {
            synchronized (this.mLock) {
                rdsEvent = this.mHwFmService.getRdsStatus();
            }
            return rdsEvent;
        } catch (RemoteException e) {
            throw FmUtils.rethrowFromSystemServer(e);
        }
    }

    public int getRssi() {
        int rssi;
        Log.d(TAG, "getRssi");
        try {
            synchronized (this.mLock) {
                rssi = this.mHwFmService.getRssi();
            }
            return rssi;
        } catch (RemoteException e) {
            throw FmUtils.rethrowFromSystemServer(e);
        }
    }

    public int[] getAfInfo() {
        int[] afInfo;
        Log.d(TAG, "getAfInfo");
        try {
            synchronized (this.mLock) {
                afInfo = this.mHwFmService.getAfInfo();
            }
            return afInfo;
        } catch (RemoteException e) {
            throw FmUtils.rethrowFromSystemServer(e);
        }
    }

    public boolean registerRdsGroupProcessing(int fmGrpsToProc) {
        Log.d(TAG, "registerRdsGroupProcessing :fmGrpsToProc:" + fmGrpsToProc);
        try {
            boolean re = this.mHwFmService.registerRdsGroupProcessing(fmGrpsToProc);
            Log.d(TAG, "registerRdsGroupProcessing :re:" + re);
            return re;
        } catch (RemoteException e) {
            throw FmUtils.rethrowFromSystemServer(e);
        }
    }

    public String getRadioText() {
        String rtString;
        Log.d(TAG, "getRadioText");
        try {
            synchronized (this.mLock) {
                rtString = this.mHwFmService.getRadioText();
            }
            return rtString;
        } catch (RemoteException e) {
            throw FmUtils.rethrowFromSystemServer(e);
        }
    }

    public int getPrgmType() {
        int pTy;
        Log.d(TAG, "getPrgmType");
        try {
            synchronized (this.mLock) {
                pTy = this.mHwFmService.getPrgmType();
            }
            return pTy;
        } catch (RemoteException e) {
            throw FmUtils.rethrowFromSystemServer(e);
        }
    }

    public int getPrgmId() {
        int pI;
        Log.d(TAG, "getPrgmId");
        try {
            synchronized (this.mLock) {
                pI = this.mHwFmService.getPrgmId();
            }
            return pI;
        } catch (RemoteException e) {
            throw FmUtils.rethrowFromSystemServer(e);
        }
    }
}
