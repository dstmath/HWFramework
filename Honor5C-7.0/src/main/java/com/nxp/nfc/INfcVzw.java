package com.nxp.nfc;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.vzw.nfc.RouteEntry;

public interface INfcVzw extends IInterface {

    public static abstract class Stub extends Binder implements INfcVzw {
        private static final String DESCRIPTOR = "com.nxp.nfc.INfcVzw";
        static final int TRANSACTION_setScreenOffCondition = 2;
        static final int TRANSACTION_setVzwAidList = 1;

        private static class Proxy implements INfcVzw {
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

            public boolean setVzwAidList(RouteEntry[] entries) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeTypedArray(entries, 0);
                    this.mRemote.transact(Stub.TRANSACTION_setVzwAidList, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setScreenOffCondition(boolean enable) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (enable) {
                        i = Stub.TRANSACTION_setVzwAidList;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_setScreenOffCondition, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

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

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = 0;
            switch (code) {
                case TRANSACTION_setVzwAidList /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    boolean _result = setVzwAidList((RouteEntry[]) data.createTypedArray(RouteEntry.CREATOR));
                    reply.writeNoException();
                    if (_result) {
                        i = TRANSACTION_setVzwAidList;
                    }
                    reply.writeInt(i);
                    return true;
                case TRANSACTION_setScreenOffCondition /*2*/:
                    boolean _arg0;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = true;
                    } else {
                        _arg0 = false;
                    }
                    setScreenOffCondition(_arg0);
                    reply.writeNoException();
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void setScreenOffCondition(boolean z) throws RemoteException;

    boolean setVzwAidList(RouteEntry[] routeEntryArr) throws RemoteException;
}
