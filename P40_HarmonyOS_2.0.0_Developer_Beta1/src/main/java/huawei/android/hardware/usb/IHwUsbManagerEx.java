package huawei.android.hardware.usb;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwUsbManagerEx extends IInterface {
    void allowUsbHDB(boolean z, String str) throws RemoteException;

    void clearUsbHDBKeys() throws RemoteException;

    void denyUsbHDB() throws RemoteException;

    void setHdbEnabled(boolean z) throws RemoteException;

    public static class Default implements IHwUsbManagerEx {
        @Override // huawei.android.hardware.usb.IHwUsbManagerEx
        public void allowUsbHDB(boolean alwaysAllow, String publicKey) throws RemoteException {
        }

        @Override // huawei.android.hardware.usb.IHwUsbManagerEx
        public void denyUsbHDB() throws RemoteException {
        }

        @Override // huawei.android.hardware.usb.IHwUsbManagerEx
        public void clearUsbHDBKeys() throws RemoteException {
        }

        @Override // huawei.android.hardware.usb.IHwUsbManagerEx
        public void setHdbEnabled(boolean enabled) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwUsbManagerEx {
        private static final String DESCRIPTOR = "huawei.android.hardware.usb.IHwUsbManagerEx";
        static final int TRANSACTION_allowUsbHDB = 1;
        static final int TRANSACTION_clearUsbHDBKeys = 3;
        static final int TRANSACTION_denyUsbHDB = 2;
        static final int TRANSACTION_setHdbEnabled = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwUsbManagerEx asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwUsbManagerEx)) {
                return new Proxy(obj);
            }
            return (IHwUsbManagerEx) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            boolean _arg0 = false;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = true;
                }
                allowUsbHDB(_arg0, data.readString());
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                denyUsbHDB();
                reply.writeNoException();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                clearUsbHDBKeys();
                reply.writeNoException();
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = true;
                }
                setHdbEnabled(_arg0);
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHwUsbManagerEx {
            public static IHwUsbManagerEx sDefaultImpl;
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

            @Override // huawei.android.hardware.usb.IHwUsbManagerEx
            public void allowUsbHDB(boolean alwaysAllow, String publicKey) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(alwaysAllow ? 1 : 0);
                    _data.writeString(publicKey);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().allowUsbHDB(alwaysAllow, publicKey);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.hardware.usb.IHwUsbManagerEx
            public void denyUsbHDB() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().denyUsbHDB();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.hardware.usb.IHwUsbManagerEx
            public void clearUsbHDBKeys() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().clearUsbHDBKeys();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.hardware.usb.IHwUsbManagerEx
            public void setHdbEnabled(boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enabled ? 1 : 0);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setHdbEnabled(enabled);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwUsbManagerEx impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwUsbManagerEx getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
