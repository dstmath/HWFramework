package ohos.nfc.cardemulation;

import ohos.bundle.ElementName;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;

public interface ICardEmulation extends IRemoteBroker {
    AidGroup getAids(int i, ElementName elementName, String str) throws RemoteException;

    String getNfcInfo(String str) throws RemoteException;

    boolean isDefaultForAid(int i, ElementName elementName, String str) throws RemoteException;

    boolean isDefaultForType(int i, ElementName elementName, String str) throws RemoteException;

    boolean isListenModeEnabled() throws RemoteException;

    boolean isSupported(int i) throws RemoteException;

    boolean registerAids(int i, ElementName elementName, AidGroup aidGroup) throws RemoteException;

    boolean registerForegroundPreferred(ElementName elementName) throws RemoteException;

    boolean removeAids(int i, ElementName elementName, String str) throws RemoteException;

    void setListenMode(int i) throws RemoteException;

    int setRfConfig(String str, String str2) throws RemoteException;

    boolean unregisterForegroundPreferred() throws RemoteException;
}
