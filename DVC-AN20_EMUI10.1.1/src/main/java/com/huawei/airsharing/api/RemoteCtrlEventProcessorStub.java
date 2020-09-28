package com.huawei.airsharing.api;

import android.os.RemoteException;
import com.huawei.airsharing.api.IRemoteCtrlEventProcessor;

public class RemoteCtrlEventProcessorStub extends IRemoteCtrlEventProcessor.Stub {
    @Override // com.huawei.airsharing.api.IRemoteCtrlEventProcessor
    public int process(int eventType, int len, byte[] data) throws RemoteException {
        return 0;
    }
}
