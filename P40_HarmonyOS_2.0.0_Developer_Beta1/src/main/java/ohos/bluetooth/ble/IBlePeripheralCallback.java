package ohos.bluetooth.ble;

import java.util.List;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;

public interface IBlePeripheralCallback extends IRemoteBroker {
    void onCharacteristicRead(String str, int i, int i2, byte[] bArr) throws RemoteException;

    void onCharacteristicWrite(String str, int i, int i2) throws RemoteException;

    void onClientConnectionState(int i, int i2, boolean z, String str) throws RemoteException;

    void onClientRegistered(int i, int i2) throws RemoteException;

    void onConfigureMTU(String str, int i, int i2) throws RemoteException;

    void onDescriptorRead(String str, int i, int i2, byte[] bArr) throws RemoteException;

    void onDescriptorWrite(String str, int i, int i2) throws RemoteException;

    void onNotify(String str, int i, byte[] bArr) throws RemoteException;

    void onReadRemoteRssi(String str, int i, int i2) throws RemoteException;

    void onSearchComplete(String str, List<GattService> list, int i) throws RemoteException;
}
