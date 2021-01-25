package com.huawei.android.os;

import android.os.IMWThirdpartyCallback;
import android.os.RemoteException;

public class IMWThirdpartyCallbackEx {
    private IMWThirdpartyCallback mCallback = new IMWThirdpartyCallback.Stub() {
        /* class com.huawei.android.os.IMWThirdpartyCallbackEx.AnonymousClass1 */

        public void onModeChanged(boolean status) throws RemoteException {
            IMWThirdpartyCallbackEx.this.onModeChanged(status);
        }

        public void onZoneChanged() throws RemoteException {
            IMWThirdpartyCallbackEx.this.onZoneChanged();
        }

        public void onSizeChanged() throws RemoteException {
            IMWThirdpartyCallbackEx.this.onSizeChanged();
        }
    };

    public void onModeChanged(boolean status) throws RemoteException {
    }

    public void onZoneChanged() throws RemoteException {
    }

    public void onSizeChanged() throws RemoteException {
    }

    public IMWThirdpartyCallback getCallback() {
        return this.mCallback;
    }
}
