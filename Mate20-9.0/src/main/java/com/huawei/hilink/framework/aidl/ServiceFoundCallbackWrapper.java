package com.huawei.hilink.framework.aidl;

import android.os.RemoteException;
import com.huawei.hilink.framework.aidl.IServiceFoundCallback;

public abstract class ServiceFoundCallbackWrapper extends IServiceFoundCallback.Stub {
    public static final int ERRORCODE_MAX_REQUEST_NUM_REACHED = 9;
    public static final int ERRORCODE_NO_NETWORK = 3;
    public static final int ERRORCODE_RUNTIME = 4;
    public static final int ERRORCODE_TIMEOUT = 1;

    public abstract void onFoundError(int i) throws RemoteException;

    public abstract void onFoundService(ServiceRecord serviceRecord) throws RemoteException;
}
