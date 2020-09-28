package com.nxp.nfc;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface INxpNfcAccessExtras extends IInterface {
    boolean checkChannelAdminAccess(String str) throws RemoteException;

    public static class Default implements INxpNfcAccessExtras {
        @Override // com.nxp.nfc.INxpNfcAccessExtras
        public boolean checkChannelAdminAccess(String pkg) throws RemoteException {
            return false;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements INxpNfcAccessExtras {
        private static final String DESCRIPTOR = "com.nxp.nfc.INxpNfcAccessExtras";
        static final int TRANSACTION_checkChannelAdminAccess = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INxpNfcAccessExtras asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INxpNfcAccessExtras)) {
                return new Proxy(obj);
            }
            return (INxpNfcAccessExtras) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                boolean checkChannelAdminAccess = checkChannelAdminAccess(data.readString());
                reply.writeNoException();
                reply.writeInt(checkChannelAdminAccess ? 1 : 0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements INxpNfcAccessExtras {
            public static INxpNfcAccessExtras sDefaultImpl;
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

            @Override // com.nxp.nfc.INxpNfcAccessExtras
            public boolean checkChannelAdminAccess(String pkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().checkChannelAdminAccess(pkg);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(INxpNfcAccessExtras impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static INxpNfcAccessExtras getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
