package ohos.nfc;

import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;

public interface INfcController extends IRemoteBroker {
    int getNfcState() throws RemoteException;

    boolean isNfcAvailable() throws RemoteException;

    int setNfcEnabled(boolean z) throws RemoteException;
}
