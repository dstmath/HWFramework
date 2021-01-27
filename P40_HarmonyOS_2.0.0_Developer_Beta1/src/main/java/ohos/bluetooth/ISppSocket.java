package ohos.bluetooth;

import java.io.FileDescriptor;
import java.util.Optional;
import java.util.UUID;
import ohos.rpc.IRemoteBroker;
import ohos.rpc.RemoteException;

public interface ISppSocket extends IRemoteBroker {
    Optional<FileDescriptor> sppConnectSocket(BluetoothRemoteDevice bluetoothRemoteDevice, int i, UUID uuid, int i2, int i3) throws RemoteException;

    Optional<FileDescriptor> sppCreateSocketServer(String str, int i, UUID uuid, int i2, int i3) throws RemoteException;
}
