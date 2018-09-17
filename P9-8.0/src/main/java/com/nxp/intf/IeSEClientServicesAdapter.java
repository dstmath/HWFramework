package com.nxp.intf;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IeSEClientServicesAdapter extends IInterface {

    public static abstract class Stub extends Binder implements IeSEClientServicesAdapter {
        private static final String DESCRIPTOR = "com.nxp.intf.IeSEClientServicesAdapter";
        static final int TRANSACTION_getJcopService = 3;
        static final int TRANSACTION_getLoaderService = 2;
        static final int TRANSACTION_getNxpExtrasService = 1;

        private static class Proxy implements IeSEClientServicesAdapter {
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

            public INxpExtrasService getNxpExtrasService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    INxpExtrasService _result = com.nxp.intf.INxpExtrasService.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ILoaderService getLoaderService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    ILoaderService _result = com.nxp.intf.ILoaderService.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public IJcopService getJcopService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    IJcopService _result = com.nxp.intf.IJcopService.Stub.asInterface(_reply.readStrongBinder());
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IeSEClientServicesAdapter asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IeSEClientServicesAdapter)) {
                return new Proxy(obj);
            }
            return (IeSEClientServicesAdapter) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            IBinder iBinder = null;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    INxpExtrasService _result = getNxpExtrasService();
                    reply.writeNoException();
                    if (_result != null) {
                        iBinder = _result.asBinder();
                    }
                    reply.writeStrongBinder(iBinder);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    ILoaderService _result2 = getLoaderService();
                    reply.writeNoException();
                    if (_result2 != null) {
                        iBinder = _result2.asBinder();
                    }
                    reply.writeStrongBinder(iBinder);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    IJcopService _result3 = getJcopService();
                    reply.writeNoException();
                    if (_result3 != null) {
                        iBinder = _result3.asBinder();
                    }
                    reply.writeStrongBinder(iBinder);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    IJcopService getJcopService() throws RemoteException;

    ILoaderService getLoaderService() throws RemoteException;

    INxpExtrasService getNxpExtrasService() throws RemoteException;
}
