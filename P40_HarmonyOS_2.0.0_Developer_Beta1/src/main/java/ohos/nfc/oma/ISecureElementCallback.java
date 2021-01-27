package ohos.nfc.oma;

import ohos.rpc.IRemoteBroker;

public interface ISecureElementCallback extends IRemoteBroker {
    void onServiceConnected();

    void onServiceDisconnected();
}
