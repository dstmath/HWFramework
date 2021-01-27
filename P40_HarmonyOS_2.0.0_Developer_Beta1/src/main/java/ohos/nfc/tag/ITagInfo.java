package ohos.nfc.tag;

import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;

public interface ITagInfo extends IRemoteBroker {
    boolean canSetReadOnly(int i) throws RemoteException;

    boolean connectTag(int i, int i2) throws RemoteException;

    int getMaxSendLength(int i) throws RemoteException;

    int getSendDataTimeout(int i) throws RemoteException;

    boolean isNdefTag(int i) throws RemoteException;

    boolean isTagConnected(int i) throws RemoteException;

    NdefMessage ndefRead(int i) throws RemoteException;

    int ndefSetReadOnly(int i) throws RemoteException;

    int ndefWrite(int i, NdefMessage ndefMessage) throws RemoteException;

    boolean reconnectTag(int i) throws RemoteException;

    void resetSendDataTimeout() throws RemoteException;

    byte[] sendData(int i, byte[] bArr) throws RemoteException;

    boolean setSendDataTimeout(int i, int i2) throws RemoteException;
}
