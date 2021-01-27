package ohos.dcall;

import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;

public interface ICallStateObserver extends IRemoteBroker {
    void onCallStateUpdated(int i, String str) throws RemoteException;

    void onCfuIndicatorUpdated(boolean z) throws RemoteException;

    void onVoiceMailMsgIndicatorUpdated(boolean z) throws RemoteException;

    void setReadCallLogPermission(boolean z);
}
