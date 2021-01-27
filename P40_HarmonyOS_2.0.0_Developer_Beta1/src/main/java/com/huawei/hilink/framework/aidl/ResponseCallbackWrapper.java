package com.huawei.hilink.framework.aidl;

import android.os.RemoteException;
import com.huawei.hilink.framework.aidl.IResponseCallback;

public class ResponseCallbackWrapper extends IResponseCallback.Stub {
    public static final int ERRORCODE_MAX_REQUEST_NUM_REACHED = 9;
    public static final int ERRORCODE_NO_NETWORK = 3;
    public static final int ERRORCODE_RUNTIME = 4;
    public static final int ERRORCODE_TIMEOUT = 1;

    @Override // com.huawei.hilink.framework.aidl.IResponseCallback
    public void onRecieveResponse(int requestID, String payload) throws RemoteException {
    }

    @Override // com.huawei.hilink.framework.aidl.IResponseCallback
    public void onRecieveError(int errorCode) throws RemoteException {
    }
}
