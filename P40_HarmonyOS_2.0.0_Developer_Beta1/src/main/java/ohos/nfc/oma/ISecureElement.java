package ohos.nfc.oma;

import java.util.Optional;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.IRemoteObject;
import ohos.rpc.RemoteException;

public interface ISecureElement extends IRemoteBroker {
    void bindSeService(SecureElementCallbackProxy secureElementCallbackProxy) throws RemoteException;

    void close(IRemoteObject iRemoteObject) throws RemoteException;

    void closeChannel(IRemoteObject iRemoteObject) throws RemoteException;

    void closeSeSessions(Reader reader) throws RemoteException;

    byte[] getATR(IRemoteObject iRemoteObject) throws RemoteException;

    Reader[] getReaders(SEService sEService) throws RemoteException;

    byte[] getSelectResponse(IRemoteObject iRemoteObject) throws RemoteException;

    boolean isChannelClosed(IRemoteObject iRemoteObject) throws RemoteException;

    boolean isSeServiceConnected() throws RemoteException;

    boolean isSecureElementPresent(String str) throws RemoteException;

    Optional<Channel> openBasicChannel(Session session, byte[] bArr) throws RemoteException;

    Optional<Channel> openLogicalChannel(Session session, byte[] bArr) throws RemoteException;

    Optional<Session> openSession(Reader reader) throws RemoteException;

    byte[] transmit(IRemoteObject iRemoteObject, byte[] bArr) throws RemoteException;
}
