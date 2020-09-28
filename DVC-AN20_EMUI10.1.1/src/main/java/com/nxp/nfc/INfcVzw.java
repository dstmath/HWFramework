package com.nxp.nfc;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.vzw.nfc.RouteEntry;

public interface INfcVzw extends IInterface {
    void setScreenOffCondition(boolean z) throws RemoteException;

    boolean setVzwAidList(RouteEntry[] routeEntryArr) throws RemoteException;

    public static class Default implements INfcVzw {
        @Override // com.nxp.nfc.INfcVzw
        public boolean setVzwAidList(RouteEntry[] entries) throws RemoteException {
            return false;
        }

        @Override // com.nxp.nfc.INfcVzw
        public void setScreenOffCondition(boolean enable) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements INfcVzw {
        private static final String DESCRIPTOR = "com.nxp.nfc.INfcVzw";
        static final int TRANSACTION_setScreenOffCondition = 2;
        static final int TRANSACTION_setVzwAidList = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static INfcVzw asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof INfcVzw)) {
                return new Proxy(obj);
            }
            return (INfcVzw) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                boolean vzwAidList = setVzwAidList((RouteEntry[]) data.createTypedArray(RouteEntry.CREATOR));
                reply.writeNoException();
                reply.writeInt(vzwAidList ? 1 : 0);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                setScreenOffCondition(data.readInt() != 0);
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
        public static class Proxy implements INfcVzw {
            public static INfcVzw sDefaultImpl;
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

            @Override // com.nxp.nfc.INfcVzw
            public boolean setVzwAidList(RouteEntry[] entries) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    _data.writeTypedArray(entries, 0);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setVzwAidList(entries);
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

            @Override // com.nxp.nfc.INfcVzw
            public void setScreenOffCondition(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable ? 1 : 0);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setScreenOffCondition(enable);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(INfcVzw impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static INfcVzw getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
