package com.huawei.android.app;

import android.app.IHwDockCallBack;
import android.os.RemoteException;

public class IHwDockCallBackEx {
    public static final int REGISTER_DOCK_CALLBACK = 0;
    public static final int UNREGISTER_DOCK_CALLBACK = 1;
    private IHwDockCallBack mHwDockCallback = new IHwDockCallBack.Stub() {
        /* class com.huawei.android.app.IHwDockCallBackEx.AnonymousClass1 */

        public void connect(int navID) throws RemoteException {
            IHwDockCallBackEx.this.connect(navID);
        }

        public void dismiss() throws RemoteException {
            IHwDockCallBackEx.this.dismiss();
        }

        public boolean isEditState() throws RemoteException {
            return IHwDockCallBackEx.this.isEditState();
        }

        public void dismissWithAnimation() throws RemoteException {
            IHwDockCallBackEx.this.dismissWithAnimation();
        }
    };

    public void connect(int navId) {
    }

    public void dismiss() {
    }

    public boolean isEditState() {
        return false;
    }

    public void dismissWithAnimation() {
    }

    public IHwDockCallBack getHwDockCallBack() {
        return this.mHwDockCallback;
    }
}
