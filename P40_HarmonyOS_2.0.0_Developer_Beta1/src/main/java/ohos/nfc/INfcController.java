package ohos.nfc;

import java.util.List;
import ohos.event.intentagent.IntentAgent;
import ohos.interwork.utils.PacMapEx;
import ohos.nfc.NfcController;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;

public interface INfcController extends IRemoteBroker {
    int getNfcState() throws RemoteException;

    boolean isNfcAvailable() throws RemoteException;

    void registerForegroundDispatch(IntentAgent intentAgent, List<String> list, ProfileParcel profileParcel) throws RemoteException;

    int setNfcEnabled(boolean z) throws RemoteException;

    void setReaderMode(IRemoteObject iRemoteObject, NfcController.ReaderModeCallback readerModeCallback, int i, PacMapEx pacMapEx) throws RemoteException;
}
