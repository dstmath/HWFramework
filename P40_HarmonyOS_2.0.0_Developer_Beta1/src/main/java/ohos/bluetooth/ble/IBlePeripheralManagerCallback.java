package ohos.bluetooth.ble;

import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;

public interface IBlePeripheralManagerCallback extends IRemoteBroker {
    void characteristicReadRequestEvent(String str, int i, int i2, boolean z, int i3) throws RemoteException;

    void characteristicWriteRequestEvent(String str, int i, int i2, int i3, boolean z, boolean z2, int i4, byte[] bArr) throws RemoteException;

    void connectionStateUpdateEvent(String str, int i, int i2, int i3, int i4) throws RemoteException;

    void descriptorReadRequestEvent(String str, int i, int i2, boolean z, int i3) throws RemoteException;

    void descriptorWriteRequestEvent(String str, int i, int i2, int i3, boolean z, boolean z2, int i4, byte[] bArr) throws RemoteException;

    void executeWriteEvent(String str, int i, boolean z) throws RemoteException;

    void mtuUpdateEvent(String str, int i) throws RemoteException;

    void notificationSentEvent(String str, int i) throws RemoteException;

    void serverRegisteredEvent(int i, int i2) throws RemoteException;

    void serviceAddedEvent(int i, GattService gattService) throws RemoteException;
}
