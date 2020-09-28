package com.huawei.android.bluetooth;

import android.bluetooth.le.ScanResult;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ILeRangingCallback extends IInterface {
    void onRangeResult(LeRangingResult leRangingResult, ScanResult scanResult) throws RemoteException;

    public static class Default implements ILeRangingCallback {
        @Override // com.huawei.android.bluetooth.ILeRangingCallback
        public void onRangeResult(LeRangingResult rangingResult, ScanResult scanResult) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ILeRangingCallback {
        private static final String DESCRIPTOR = "com.huawei.android.bluetooth.ILeRangingCallback";
        static final int TRANSACTION_onRangeResult = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ILeRangingCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ILeRangingCallback)) {
                return new Proxy(obj);
            }
            return (ILeRangingCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            LeRangingResult _arg0;
            ScanResult _arg1;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = (LeRangingResult) LeRangingResult.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                if (data.readInt() != 0) {
                    _arg1 = (ScanResult) ScanResult.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                onRangeResult(_arg0, _arg1);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ILeRangingCallback {
            public static ILeRangingCallback sDefaultImpl;
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

            @Override // com.huawei.android.bluetooth.ILeRangingCallback
            public void onRangeResult(LeRangingResult rangingResult, ScanResult scanResult) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (rangingResult != null) {
                        _data.writeInt(1);
                        rangingResult.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (scanResult != null) {
                        _data.writeInt(1);
                        scanResult.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onRangeResult(rangingResult, scanResult);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ILeRangingCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ILeRangingCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
