package ohos.smartcomm;

import ohos.rpc.IRemoteBroker;

interface IZMultiPathCallback extends IRemoteBroker {
    public static final int TRANSACTION_ON_CALLBACK = 1;

    void onCallback(String str);
}
