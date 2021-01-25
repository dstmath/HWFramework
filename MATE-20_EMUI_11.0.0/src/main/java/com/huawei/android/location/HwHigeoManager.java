package com.huawei.android.location;

import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import com.huawei.android.location.IHwHigeoFunc;
import com.huawei.android.os.ServiceManagerEx;

public class HwHigeoManager {
    private static final int SEND_FAIL = 0;
    private static final int SEND_PASS = 1;
    private static final String TAG = "HwHigeoManager";
    private static HwHigeoManager sInstance = new HwHigeoManager();
    private HwHigeoCallback mHwHigeoCallback;

    private HwHigeoManager() {
    }

    public static HwHigeoManager getDefault() {
        return sInstance;
    }

    public boolean sendMmData(int type, Bundle bundle) {
        try {
            return getIHwHigeoFunc().sendMmData(type, bundle);
        } catch (RemoteException e) {
            Log.e(TAG, "sendMmData RemoteException, type = " + type);
            return false;
        }
    }

    public boolean sendHigeoData(int type, Bundle bundle) {
        try {
            boolean result = getIHwHigeoFunc().sendHigeoData(type, bundle);
            Log.e(TAG, "sendHigeoData final type = " + type + ", result = " + result);
            return result;
        } catch (RemoteException e) {
            Log.e(TAG, "sendHigeoData RemoteException, type = " + type);
            return false;
        }
    }

    public boolean sendCellBatchingData(int type, Bundle bundle) {
        try {
            return getIHwHigeoFunc().sendCellBatchingData(type, bundle);
        } catch (RemoteException e) {
            Log.e(TAG, "sendCellBatchingData RemoteException, type = " + type);
            return false;
        }
    }

    public int sendWifiFenceData(int type, Bundle bundle) {
        try {
            return getIHwHigeoFunc().sendWifiFenceData(type, bundle);
        } catch (RemoteException e) {
            Log.e(TAG, "sendWifiFenceData RemoteException, type = " + type);
            return 0;
        }
    }

    public boolean sendCellFenceData(int type, Bundle bundle) {
        try {
            return getIHwHigeoFunc().sendCellFenceData(type, bundle);
        } catch (RemoteException e) {
            Log.e(TAG, "sendCellFenceData RemoteException, type = " + type);
            return false;
        }
    }

    public int sendGeoFenceData(int type, Bundle bundle) {
        try {
            return getIHwHigeoFunc().sendGeoFenceData(type, bundle);
        } catch (RemoteException e) {
            Log.e(TAG, "sendGeoFenceData RemoteException, type = " + type);
            return 0;
        }
    }

    public boolean registerHigeoCallback(HwHigeoCallbackInterface higeoCallback) {
        boolean result;
        if (this.mHwHigeoCallback == null) {
            this.mHwHigeoCallback = new HwHigeoCallback();
        }
        this.mHwHigeoCallback.setHwHigeoCallback(higeoCallback);
        try {
            result = getIHwHigeoFunc().registerHigeoCallback(this.mHwHigeoCallback.mCallbackStub);
        } catch (RemoteException e) {
            Log.e(TAG, "registerHigeoCallback RemoteException");
            result = false;
        }
        Log.d(TAG, "registerHigeoCallback final result = " + result);
        return result;
    }

    private IHwHigeoFunc getIHwHigeoFunc() throws RemoteException {
        IHwHigeoFunc higeoFunc = IHwHigeoFunc.Stub.asInterface(ServiceManagerEx.getService("higeo_service"));
        if (higeoFunc != null) {
            return higeoFunc;
        }
        throw new RemoteException("getIHwHigeoFunc return null");
    }

    private static class HwHigeoCallback extends HigeoCallback {
        private HwHigeoCallbackInterface mHwHigeoCallback;

        private HwHigeoCallback() {
            this.mHwHigeoCallback = null;
        }

        public void setHwHigeoCallback(HwHigeoCallbackInterface callback) {
            this.mHwHigeoCallback = callback;
        }

        @Override // com.huawei.android.location.HigeoCallback
        public void onMmDataRequest(int type, Bundle bundle) {
            if (this.mHwHigeoCallback != null) {
                this.mHwHigeoCallback.onMmDataRequest(type, bundle);
            }
        }

        @Override // com.huawei.android.location.HigeoCallback
        public void onHigeoEventCallback(int type, Bundle bundle) {
            if (this.mHwHigeoCallback != null) {
                this.mHwHigeoCallback.onHigeoEventCallback(type, bundle);
            }
        }

        @Override // com.huawei.android.location.HigeoCallback
        public void onCellBatchingCallback(int type, Bundle bundle) {
            if (this.mHwHigeoCallback != null) {
                this.mHwHigeoCallback.onCellBatchingCallback(type, bundle);
            }
        }

        @Override // com.huawei.android.location.HigeoCallback
        public void onWifiFenceCallback(int type, Bundle bundle) {
            if (this.mHwHigeoCallback != null) {
                this.mHwHigeoCallback.onWifiFenceCallback(type, bundle);
            }
        }

        @Override // com.huawei.android.location.HigeoCallback
        public void onCellFenceCallback(int type, Bundle bundle) {
            if (this.mHwHigeoCallback != null) {
                this.mHwHigeoCallback.onCellFenceCallback(type, bundle);
            }
        }

        @Override // com.huawei.android.location.HigeoCallback
        public void onGeoFenceCallback(int type, Bundle bundle) {
            if (this.mHwHigeoCallback != null) {
                this.mHwHigeoCallback.onGeoFenceCallback(type, bundle);
            }
        }
    }
}
