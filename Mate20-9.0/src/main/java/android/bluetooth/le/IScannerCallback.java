package android.bluetooth.le;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IScannerCallback extends IInterface {

    public static abstract class Stub extends Binder implements IScannerCallback {
        private static final String DESCRIPTOR = "android.bluetooth.le.IScannerCallback";
        static final int TRANSACTION_onBatchScanResults = 3;
        static final int TRANSACTION_onFoundOrLost = 4;
        static final int TRANSACTION_onScanManagerErrorCallback = 5;
        static final int TRANSACTION_onScanResult = 2;
        static final int TRANSACTION_onScannerRegistered = 1;

        private static class Proxy implements IScannerCallback {
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

            public void onScannerRegistered(int status, int scannerId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    _data.writeInt(scannerId);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onScanResult(ScanResult scanResult) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (scanResult != null) {
                        _data.writeInt(1);
                        scanResult.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onBatchScanResults(List<ScanResult> batchResults) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedList(batchResults);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onFoundOrLost(boolean onFound, ScanResult scanResult) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(onFound);
                    if (scanResult != null) {
                        _data.writeInt(1);
                        scanResult.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void onScanManagerErrorCallback(int errorCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(errorCode);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IScannerCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IScannerCallback)) {
                return new Proxy(obj);
            }
            return (IScannerCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                ScanResult _arg1 = null;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        onScannerRegistered(data.readInt(), data.readInt());
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg1 = ScanResult.CREATOR.createFromParcel(data);
                        }
                        onScanResult(_arg1);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        onBatchScanResults(data.createTypedArrayList(ScanResult.CREATOR));
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _arg0 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg1 = ScanResult.CREATOR.createFromParcel(data);
                        }
                        onFoundOrLost(_arg0, _arg1);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        onScanManagerErrorCallback(data.readInt());
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

    void onBatchScanResults(List<ScanResult> list) throws RemoteException;

    void onFoundOrLost(boolean z, ScanResult scanResult) throws RemoteException;

    void onScanManagerErrorCallback(int i) throws RemoteException;

    void onScanResult(ScanResult scanResult) throws RemoteException;

    void onScannerRegistered(int i, int i2) throws RemoteException;
}
