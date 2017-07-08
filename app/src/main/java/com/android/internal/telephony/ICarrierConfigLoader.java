package com.android.internal.telephony;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.PersistableBundle;
import android.os.RemoteException;

public interface ICarrierConfigLoader extends IInterface {

    public static abstract class Stub extends Binder implements ICarrierConfigLoader {
        private static final String DESCRIPTOR = "com.android.internal.telephony.ICarrierConfigLoader";
        static final int TRANSACTION_getConfigForSubId = 1;
        static final int TRANSACTION_notifyConfigChangedForSubId = 2;
        static final int TRANSACTION_updateConfigForPhoneId = 3;

        private static class Proxy implements ICarrierConfigLoader {
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

            public PersistableBundle getConfigForSubId(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    PersistableBundle persistableBundle;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(Stub.TRANSACTION_getConfigForSubId, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        persistableBundle = (PersistableBundle) PersistableBundle.CREATOR.createFromParcel(_reply);
                    } else {
                        persistableBundle = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return persistableBundle;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyConfigChangedForSubId(int subId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(subId);
                    this.mRemote.transact(Stub.TRANSACTION_notifyConfigChangedForSubId, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateConfigForPhoneId(int phoneId, String simState) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(phoneId);
                    _data.writeString(simState);
                    this.mRemote.transact(Stub.TRANSACTION_updateConfigForPhoneId, _data, _reply, 0);
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

        public static ICarrierConfigLoader asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ICarrierConfigLoader)) {
                return new Proxy(obj);
            }
            return (ICarrierConfigLoader) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_getConfigForSubId /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    PersistableBundle _result = getConfigForSubId(data.readInt());
                    reply.writeNoException();
                    if (_result != null) {
                        reply.writeInt(TRANSACTION_getConfigForSubId);
                        _result.writeToParcel(reply, TRANSACTION_getConfigForSubId);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_notifyConfigChangedForSubId /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    notifyConfigChangedForSubId(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_updateConfigForPhoneId /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    updateConfigForPhoneId(data.readInt(), data.readString());
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

    PersistableBundle getConfigForSubId(int i) throws RemoteException;

    void notifyConfigChangedForSubId(int i) throws RemoteException;

    void updateConfigForPhoneId(int i, String str) throws RemoteException;
}
