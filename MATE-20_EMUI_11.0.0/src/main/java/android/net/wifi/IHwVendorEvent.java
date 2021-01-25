package android.net.wifi;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.server.wifi.wificond.NativeMssResult;

public interface IHwVendorEvent extends IInterface {
    void OnMssSyncReport(NativeMssResult nativeMssResult) throws RemoteException;

    void OnTasRssiReport(int i, int i2, int[] iArr) throws RemoteException;

    public static class Default implements IHwVendorEvent {
        @Override // android.net.wifi.IHwVendorEvent
        public void OnMssSyncReport(NativeMssResult mssresult) throws RemoteException {
        }

        @Override // android.net.wifi.IHwVendorEvent
        public void OnTasRssiReport(int index, int rssi, int[] rsv) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwVendorEvent {
        private static final String DESCRIPTOR = "android.net.wifi.IHwVendorEvent";
        static final int TRANSACTION_OnMssSyncReport = 1;
        static final int TRANSACTION_OnTasRssiReport = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwVendorEvent asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwVendorEvent)) {
                return new Proxy(obj);
            }
            return (IHwVendorEvent) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            NativeMssResult _arg0;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = NativeMssResult.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                OnMssSyncReport(_arg0);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                OnTasRssiReport(data.readInt(), data.readInt(), data.createIntArray());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHwVendorEvent {
            public static IHwVendorEvent sDefaultImpl;
            private IBinder mRemote;

            Proxy(IBinder remote) {
                this.mRemote = remote;
            }

            @Override // android.os.IInterface
            public IBinder asBinder() {
                return this.mRemote;
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }

            @Override // android.net.wifi.IHwVendorEvent
            public void OnMssSyncReport(NativeMssResult mssresult) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (mssresult != null) {
                        _data.writeInt(1);
                        mssresult.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().OnMssSyncReport(mssresult);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IHwVendorEvent
            public void OnTasRssiReport(int index, int rssi, int[] rsv) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(index);
                    _data.writeInt(rssi);
                    _data.writeIntArray(rsv);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().OnTasRssiReport(index, rssi, rsv);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwVendorEvent impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwVendorEvent getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
