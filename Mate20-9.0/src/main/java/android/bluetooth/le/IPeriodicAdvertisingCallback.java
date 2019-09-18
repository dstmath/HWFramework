package android.bluetooth.le;

import android.bluetooth.BluetoothDevice;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IPeriodicAdvertisingCallback extends IInterface {

    public static abstract class Stub extends Binder implements IPeriodicAdvertisingCallback {
        private static final String DESCRIPTOR = "android.bluetooth.le.IPeriodicAdvertisingCallback";
        static final int TRANSACTION_onPeriodicAdvertisingReport = 2;
        static final int TRANSACTION_onSyncEstablished = 1;
        static final int TRANSACTION_onSyncLost = 3;

        private static class Proxy implements IPeriodicAdvertisingCallback {
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

            public void onSyncEstablished(int syncHandle, BluetoothDevice device, int advertisingSid, int skip, int timeout, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(syncHandle);
                    if (device != null) {
                        _data.writeInt(1);
                        device.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(advertisingSid);
                    _data.writeInt(skip);
                    _data.writeInt(timeout);
                    _data.writeInt(status);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onPeriodicAdvertisingReport(PeriodicAdvertisingReport report) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (report != null) {
                        _data.writeInt(1);
                        report.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onSyncLost(int syncHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(syncHandle);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IPeriodicAdvertisingCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPeriodicAdvertisingCallback)) {
                return new Proxy(obj);
            }
            return (IPeriodicAdvertisingCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v2, resolved type: android.bluetooth.le.PeriodicAdvertisingReport} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v3, resolved type: android.bluetooth.le.PeriodicAdvertisingReport} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r2v0, resolved type: android.bluetooth.BluetoothDevice} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v8, resolved type: android.bluetooth.le.PeriodicAdvertisingReport} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v14, resolved type: android.bluetooth.le.PeriodicAdvertisingReport} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v15, resolved type: android.bluetooth.le.PeriodicAdvertisingReport} */
        /* JADX WARNING: type inference failed for: r0v7, types: [android.bluetooth.BluetoothDevice] */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = code;
            Parcel parcel = data;
            if (i != 1598968902) {
                PeriodicAdvertisingReport _arg0 = null;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg0 = BluetoothDevice.CREATOR.createFromParcel(parcel);
                        }
                        BluetoothDevice _arg1 = _arg0;
                        onSyncEstablished(_arg02, _arg1, data.readInt(), data.readInt(), data.readInt(), data.readInt());
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = PeriodicAdvertisingReport.CREATOR.createFromParcel(parcel);
                        }
                        onPeriodicAdvertisingReport(_arg0);
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        onSyncLost(data.readInt());
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    void onPeriodicAdvertisingReport(PeriodicAdvertisingReport periodicAdvertisingReport) throws RemoteException;

    void onSyncEstablished(int i, BluetoothDevice bluetoothDevice, int i2, int i3, int i4, int i5) throws RemoteException;

    void onSyncLost(int i) throws RemoteException;
}
