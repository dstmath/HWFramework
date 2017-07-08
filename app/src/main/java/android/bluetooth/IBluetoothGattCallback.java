package android.bluetooth;

import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.ScanResult;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IBluetoothGattCallback extends IInterface {

    public static abstract class Stub extends Binder implements IBluetoothGattCallback {
        private static final String DESCRIPTOR = "android.bluetooth.IBluetoothGattCallback";
        static final int TRANSACTION_onBatchScanResults = 4;
        static final int TRANSACTION_onCharacteristicRead = 6;
        static final int TRANSACTION_onCharacteristicWrite = 7;
        static final int TRANSACTION_onClientConnectionState = 2;
        static final int TRANSACTION_onClientRegistered = 1;
        static final int TRANSACTION_onConfigureMTU = 15;
        static final int TRANSACTION_onDescriptorRead = 9;
        static final int TRANSACTION_onDescriptorWrite = 10;
        static final int TRANSACTION_onExecuteWrite = 8;
        static final int TRANSACTION_onFoundOrLost = 16;
        static final int TRANSACTION_onMultiAdvertiseCallback = 13;
        static final int TRANSACTION_onNotify = 11;
        static final int TRANSACTION_onReadRemoteRssi = 12;
        static final int TRANSACTION_onScanManagerErrorCallback = 14;
        static final int TRANSACTION_onScanResult = 3;
        static final int TRANSACTION_onSearchComplete = 5;

        private static class Proxy implements IBluetoothGattCallback {
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            public void onClientRegistered(int status, int clientIf) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    _data.writeInt(clientIf);
                    this.mRemote.transact(Stub.TRANSACTION_onClientRegistered, _data, null, Stub.TRANSACTION_onClientRegistered);
                } finally {
                    _data.recycle();
                }
            }

            public void onClientConnectionState(int status, int clientIf, boolean connected, String address) throws RemoteException {
                int i = Stub.TRANSACTION_onClientRegistered;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    _data.writeInt(clientIf);
                    if (!connected) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    _data.writeString(address);
                    this.mRemote.transact(Stub.TRANSACTION_onClientConnectionState, _data, null, Stub.TRANSACTION_onClientRegistered);
                } finally {
                    _data.recycle();
                }
            }

            public void onScanResult(ScanResult scanResult) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (scanResult != null) {
                        _data.writeInt(Stub.TRANSACTION_onClientRegistered);
                        scanResult.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onScanResult, _data, null, Stub.TRANSACTION_onClientRegistered);
                } finally {
                    _data.recycle();
                }
            }

            public void onBatchScanResults(List<ScanResult> batchResults) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(batchResults);
                    this.mRemote.transact(Stub.TRANSACTION_onBatchScanResults, _data, null, Stub.TRANSACTION_onClientRegistered);
                } finally {
                    _data.recycle();
                }
            }

            public void onSearchComplete(String address, List<BluetoothGattService> services, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeTypedList(services);
                    _data.writeInt(status);
                    this.mRemote.transact(Stub.TRANSACTION_onSearchComplete, _data, null, Stub.TRANSACTION_onClientRegistered);
                } finally {
                    _data.recycle();
                }
            }

            public void onCharacteristicRead(String address, int status, int handle, byte[] value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeInt(status);
                    _data.writeInt(handle);
                    _data.writeByteArray(value);
                    this.mRemote.transact(Stub.TRANSACTION_onCharacteristicRead, _data, null, Stub.TRANSACTION_onClientRegistered);
                } finally {
                    _data.recycle();
                }
            }

            public void onCharacteristicWrite(String address, int status, int handle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeInt(status);
                    _data.writeInt(handle);
                    this.mRemote.transact(Stub.TRANSACTION_onCharacteristicWrite, _data, null, Stub.TRANSACTION_onClientRegistered);
                } finally {
                    _data.recycle();
                }
            }

            public void onExecuteWrite(String address, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeInt(status);
                    this.mRemote.transact(Stub.TRANSACTION_onExecuteWrite, _data, null, Stub.TRANSACTION_onClientRegistered);
                } finally {
                    _data.recycle();
                }
            }

            public void onDescriptorRead(String address, int status, int handle, byte[] value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeInt(status);
                    _data.writeInt(handle);
                    _data.writeByteArray(value);
                    this.mRemote.transact(Stub.TRANSACTION_onDescriptorRead, _data, null, Stub.TRANSACTION_onClientRegistered);
                } finally {
                    _data.recycle();
                }
            }

            public void onDescriptorWrite(String address, int status, int handle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeInt(status);
                    _data.writeInt(handle);
                    this.mRemote.transact(Stub.TRANSACTION_onDescriptorWrite, _data, null, Stub.TRANSACTION_onClientRegistered);
                } finally {
                    _data.recycle();
                }
            }

            public void onNotify(String address, int handle, byte[] value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeInt(handle);
                    _data.writeByteArray(value);
                    this.mRemote.transact(Stub.TRANSACTION_onNotify, _data, null, Stub.TRANSACTION_onClientRegistered);
                } finally {
                    _data.recycle();
                }
            }

            public void onReadRemoteRssi(String address, int rssi, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeInt(rssi);
                    _data.writeInt(status);
                    this.mRemote.transact(Stub.TRANSACTION_onReadRemoteRssi, _data, null, Stub.TRANSACTION_onClientRegistered);
                } finally {
                    _data.recycle();
                }
            }

            public void onMultiAdvertiseCallback(int status, boolean isStart, AdvertiseSettings advertiseSettings) throws RemoteException {
                int i = Stub.TRANSACTION_onClientRegistered;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    if (!isStart) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (advertiseSettings != null) {
                        _data.writeInt(Stub.TRANSACTION_onClientRegistered);
                        advertiseSettings.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onMultiAdvertiseCallback, _data, null, Stub.TRANSACTION_onClientRegistered);
                } finally {
                    _data.recycle();
                }
            }

            public void onScanManagerErrorCallback(int errorCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(errorCode);
                    this.mRemote.transact(Stub.TRANSACTION_onScanManagerErrorCallback, _data, null, Stub.TRANSACTION_onClientRegistered);
                } finally {
                    _data.recycle();
                }
            }

            public void onConfigureMTU(String address, int mtu, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(address);
                    _data.writeInt(mtu);
                    _data.writeInt(status);
                    this.mRemote.transact(Stub.TRANSACTION_onConfigureMTU, _data, null, Stub.TRANSACTION_onClientRegistered);
                } finally {
                    _data.recycle();
                }
            }

            public void onFoundOrLost(boolean onFound, ScanResult scanResult) throws RemoteException {
                int i = Stub.TRANSACTION_onClientRegistered;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!onFound) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (scanResult != null) {
                        _data.writeInt(Stub.TRANSACTION_onClientRegistered);
                        scanResult.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_onFoundOrLost, _data, null, Stub.TRANSACTION_onClientRegistered);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IBluetoothGattCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IBluetoothGattCallback)) {
                return new Proxy(obj);
            }
            return (IBluetoothGattCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_onClientRegistered /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    onClientRegistered(data.readInt(), data.readInt());
                    return true;
                case TRANSACTION_onClientConnectionState /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    onClientConnectionState(data.readInt(), data.readInt(), data.readInt() != 0, data.readString());
                    return true;
                case TRANSACTION_onScanResult /*3*/:
                    ScanResult scanResult;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        scanResult = (ScanResult) ScanResult.CREATOR.createFromParcel(data);
                    } else {
                        scanResult = null;
                    }
                    onScanResult(scanResult);
                    return true;
                case TRANSACTION_onBatchScanResults /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    onBatchScanResults(data.createTypedArrayList(ScanResult.CREATOR));
                    return true;
                case TRANSACTION_onSearchComplete /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    onSearchComplete(data.readString(), data.createTypedArrayList(BluetoothGattService.CREATOR), data.readInt());
                    return true;
                case TRANSACTION_onCharacteristicRead /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    onCharacteristicRead(data.readString(), data.readInt(), data.readInt(), data.createByteArray());
                    return true;
                case TRANSACTION_onCharacteristicWrite /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    onCharacteristicWrite(data.readString(), data.readInt(), data.readInt());
                    return true;
                case TRANSACTION_onExecuteWrite /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    onExecuteWrite(data.readString(), data.readInt());
                    return true;
                case TRANSACTION_onDescriptorRead /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    onDescriptorRead(data.readString(), data.readInt(), data.readInt(), data.createByteArray());
                    return true;
                case TRANSACTION_onDescriptorWrite /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    onDescriptorWrite(data.readString(), data.readInt(), data.readInt());
                    return true;
                case TRANSACTION_onNotify /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    onNotify(data.readString(), data.readInt(), data.createByteArray());
                    return true;
                case TRANSACTION_onReadRemoteRssi /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    onReadRemoteRssi(data.readString(), data.readInt(), data.readInt());
                    return true;
                case TRANSACTION_onMultiAdvertiseCallback /*13*/:
                    AdvertiseSettings advertiseSettings;
                    data.enforceInterface(DESCRIPTOR);
                    int _arg0 = data.readInt();
                    boolean _arg1 = data.readInt() != 0;
                    if (data.readInt() != 0) {
                        advertiseSettings = (AdvertiseSettings) AdvertiseSettings.CREATOR.createFromParcel(data);
                    } else {
                        advertiseSettings = null;
                    }
                    onMultiAdvertiseCallback(_arg0, _arg1, advertiseSettings);
                    return true;
                case TRANSACTION_onScanManagerErrorCallback /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    onScanManagerErrorCallback(data.readInt());
                    return true;
                case TRANSACTION_onConfigureMTU /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    onConfigureMTU(data.readString(), data.readInt(), data.readInt());
                    return true;
                case TRANSACTION_onFoundOrLost /*16*/:
                    ScanResult scanResult2;
                    data.enforceInterface(DESCRIPTOR);
                    boolean _arg02 = data.readInt() != 0;
                    if (data.readInt() != 0) {
                        scanResult2 = (ScanResult) ScanResult.CREATOR.createFromParcel(data);
                    } else {
                        scanResult2 = null;
                    }
                    onFoundOrLost(_arg02, scanResult2);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onBatchScanResults(List<ScanResult> list) throws RemoteException;

    void onCharacteristicRead(String str, int i, int i2, byte[] bArr) throws RemoteException;

    void onCharacteristicWrite(String str, int i, int i2) throws RemoteException;

    void onClientConnectionState(int i, int i2, boolean z, String str) throws RemoteException;

    void onClientRegistered(int i, int i2) throws RemoteException;

    void onConfigureMTU(String str, int i, int i2) throws RemoteException;

    void onDescriptorRead(String str, int i, int i2, byte[] bArr) throws RemoteException;

    void onDescriptorWrite(String str, int i, int i2) throws RemoteException;

    void onExecuteWrite(String str, int i) throws RemoteException;

    void onFoundOrLost(boolean z, ScanResult scanResult) throws RemoteException;

    void onMultiAdvertiseCallback(int i, boolean z, AdvertiseSettings advertiseSettings) throws RemoteException;

    void onNotify(String str, int i, byte[] bArr) throws RemoteException;

    void onReadRemoteRssi(String str, int i, int i2) throws RemoteException;

    void onScanManagerErrorCallback(int i) throws RemoteException;

    void onScanResult(ScanResult scanResult) throws RemoteException;

    void onSearchComplete(String str, List<BluetoothGattService> list, int i) throws RemoteException;
}
