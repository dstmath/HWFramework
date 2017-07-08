package android.bluetooth;

import android.bluetooth.IBluetoothGattCallback.Stub;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.ScanResult;
import android.os.RemoteException;
import java.util.List;

public class BluetoothGattCallbackWrapper extends Stub {
    public void onClientRegistered(int status, int clientIf) throws RemoteException {
    }

    public void onClientConnectionState(int status, int clientIf, boolean connected, String address) throws RemoteException {
    }

    public void onScanResult(ScanResult scanResult) throws RemoteException {
    }

    public void onBatchScanResults(List<ScanResult> list) throws RemoteException {
    }

    public void onSearchComplete(String address, List<BluetoothGattService> list, int status) throws RemoteException {
    }

    public void onCharacteristicRead(String address, int status, int handle, byte[] value) throws RemoteException {
    }

    public void onCharacteristicWrite(String address, int status, int handle) throws RemoteException {
    }

    public void onExecuteWrite(String address, int status) throws RemoteException {
    }

    public void onDescriptorRead(String address, int status, int handle, byte[] value) throws RemoteException {
    }

    public void onDescriptorWrite(String address, int status, int handle) throws RemoteException {
    }

    public void onNotify(String address, int handle, byte[] value) throws RemoteException {
    }

    public void onReadRemoteRssi(String address, int rssi, int status) throws RemoteException {
    }

    public void onMultiAdvertiseCallback(int status, boolean isStart, AdvertiseSettings advertiseSettings) throws RemoteException {
    }

    public void onConfigureMTU(String address, int mtu, int status) throws RemoteException {
    }

    public void onFoundOrLost(boolean onFound, ScanResult scanResult) throws RemoteException {
    }

    public void onScanManagerErrorCallback(int errorCode) throws RemoteException {
    }
}
