package com.huawei.systemmanager.appcontrol.iaware.appmng;

import android.app.mtm.iaware.appmng.AppCleanParam;
import android.app.mtm.iaware.appmng.IAppCleanCallback;
import android.os.RemoteException;

public class IAppCleanCallbackEx {
    private IAppCleanCallback mIAppCleanCallback = new IAppCleanCallback.Stub() {
        /* class com.huawei.systemmanager.appcontrol.iaware.appmng.IAppCleanCallbackEx.AnonymousClass1 */

        public void onCleanFinish(AppCleanParam appCleanParam) throws RemoteException {
            IAppCleanCallbackEx.this.onCleanFinish(new AppCleanParamEx(appCleanParam));
        }
    };

    public void onCleanFinish(AppCleanParamEx appCleanParam) throws RemoteException {
    }

    public IAppCleanCallback getIAppCleanCallback() {
        return this.mIAppCleanCallback;
    }
}
