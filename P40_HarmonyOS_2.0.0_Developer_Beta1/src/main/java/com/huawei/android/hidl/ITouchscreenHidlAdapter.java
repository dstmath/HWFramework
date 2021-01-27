package com.huawei.android.hidl;

import android.os.RemoteException;
import android.util.Log;
import android.util.Slog;
import com.huawei.android.os.HwBinderEx;
import java.util.ArrayList;
import vendor.huawei.hardware.tp.V1_0.ITPCallback;
import vendor.huawei.hardware.tp.V1_0.ITouchscreen;

public class ITouchscreenHidlAdapter {
    private static final boolean IS_DEBUG_ON = Log.HWINFO;
    private static final String TAG = "ITouchscreenHidlAdapter";
    private static ITouchscreen sProxy = null;
    private String mNewStatus;

    public static ITouchscreenHidlAdapter getService() throws RemoteException {
        sProxy = ITouchscreen.getService();
        if (sProxy != null) {
            return new ITouchscreenHidlAdapter();
        }
        return null;
    }

    public void linkToDeath(HwBinderEx.DeathRecipientEx recipient, int cookie) throws RemoteException {
        sProxy.linkToDeath(recipient.getDeathRecipient(), (long) cookie);
    }

    public boolean hwTsSetEasyWeakupGestureReportEnable(boolean isEnabled) throws RemoteException {
        return sProxy.hwTsSetEasyWeakupGestureReportEnable(isEnabled);
    }

    public boolean hwTsSetEasyWeakupGesture(int status) throws RemoteException {
        return sProxy.hwTsSetEasyWeakupGesture(status);
    }

    public void hwTsGetEasyWeakupGuestureData(HwTsGetEasyWeakupGuestureDataCallbackAdapter callbackAdapter) throws RemoteException {
        sProxy.hwTsGetEasyWeakupGuestureData(callbackAdapter.getCallback());
    }

    public class HwTsGetEasyWeakupGuestureDataCallbackAdapter {
        private ITouchscreen.hwTsGetEasyWeakupGuestureDataCallback sCallback = new ITouchscreen.hwTsGetEasyWeakupGuestureDataCallback() {
            /* class com.huawei.android.hidl.ITouchscreenHidlAdapter.HwTsGetEasyWeakupGuestureDataCallbackAdapter.AnonymousClass1 */

            @Override // vendor.huawei.hardware.tp.V1_0.ITouchscreen.hwTsGetEasyWeakupGuestureDataCallback
            public void onValues(boolean isRet, ArrayList<Integer> gestureDataList) {
                HwTsGetEasyWeakupGuestureDataCallbackAdapter.this.onValues(isRet, gestureDataList);
            }
        };

        public HwTsGetEasyWeakupGuestureDataCallbackAdapter() {
        }

        public void onValues(boolean isRet, ArrayList<Integer> arrayList) {
        }

        public ITouchscreen.hwTsGetEasyWeakupGuestureDataCallback getCallback() {
            return this.sCallback;
        }
    }

    public int hwTsSetCallback(ITPCallbackHidlAdapter callbackAdapter) throws RemoteException {
        ITPCallback callback = null;
        if (callbackAdapter != null) {
            callback = callbackAdapter.getITPCallback();
        }
        return sProxy.hwTsSetCallback(callback);
    }

    public String hwTsRunCommand(String command, String parameter) throws RemoteException {
        sProxy.hwTsRunCommand(command, parameter, new ITouchscreen.hwTsRunCommandCallback(command, parameter) {
            /* class com.huawei.android.hidl.$$Lambda$ITouchscreenHidlAdapter$2J0OfGcq6DPaO4R2BiMaEG8BXVI */
            private final /* synthetic */ String f$1;
            private final /* synthetic */ String f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // vendor.huawei.hardware.tp.V1_0.ITouchscreen.hwTsRunCommandCallback
            public final void onValues(int i, String str) {
                ITouchscreenHidlAdapter.this.lambda$hwTsRunCommand$0$ITouchscreenHidlAdapter(this.f$1, this.f$2, i, str);
            }
        });
        return this.mNewStatus;
    }

    public /* synthetic */ void lambda$hwTsRunCommand$0$ITouchscreenHidlAdapter(String command, String parameter, int ret, String status) {
        if (IS_DEBUG_ON) {
            Slog.i(TAG, "runHwTHPCommand command : " + command + ",parameter : " + parameter + ", ret = " + ret + ", status = " + status);
        }
        this.mNewStatus = status;
    }

    public int hwSetFeatureConfig(int feature, String config) throws RemoteException {
        return sProxy.hwSetFeatureConfig(feature, config);
    }
}
