package com.nxp.intf;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.nxp.intf.IJcopService;
import com.nxp.intf.ILoaderService;
import com.nxp.intf.INxpExtrasService;

public interface IeSEClientServicesAdapter extends IInterface {
    IJcopService getJcopService() throws RemoteException;

    ILoaderService getLoaderService() throws RemoteException;

    INxpExtrasService getNxpExtrasService() throws RemoteException;

    public static class Default implements IeSEClientServicesAdapter {
        @Override // com.nxp.intf.IeSEClientServicesAdapter
        public INxpExtrasService getNxpExtrasService() throws RemoteException {
            return null;
        }

        @Override // com.nxp.intf.IeSEClientServicesAdapter
        public ILoaderService getLoaderService() throws RemoteException {
            return null;
        }

        @Override // com.nxp.intf.IeSEClientServicesAdapter
        public IJcopService getJcopService() throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IeSEClientServicesAdapter {
        private static final String DESCRIPTOR = "com.nxp.intf.IeSEClientServicesAdapter";
        static final int TRANSACTION_getJcopService = 3;
        static final int TRANSACTION_getLoaderService = 2;
        static final int TRANSACTION_getNxpExtrasService = 1;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            IBinder iBinder = null;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                INxpExtrasService _result = getNxpExtrasService();
                reply.writeNoException();
                if (_result != null) {
                    iBinder = _result.asBinder();
                }
                reply.writeStrongBinder(iBinder);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                ILoaderService _result2 = getLoaderService();
                reply.writeNoException();
                if (_result2 != null) {
                    iBinder = _result2.asBinder();
                }
                reply.writeStrongBinder(iBinder);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                IJcopService _result3 = getJcopService();
                reply.writeNoException();
                if (_result3 != null) {
                    iBinder = _result3.asBinder();
                }
                reply.writeStrongBinder(iBinder);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IeSEClientServicesAdapter {
            public static IeSEClientServicesAdapter sDefaultImpl;
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

            @Override // com.nxp.intf.IeSEClientServicesAdapter
            public INxpExtrasService getNxpExtrasService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNxpExtrasService();
                    }
                    _reply.readException();
                    INxpExtrasService _result = INxpExtrasService.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.intf.IeSEClientServicesAdapter
            public ILoaderService getLoaderService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLoaderService();
                    }
                    _reply.readException();
                    ILoaderService _result = ILoaderService.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.nxp.intf.IeSEClientServicesAdapter
            public IJcopService getJcopService() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getJcopService();
                    }
                    _reply.readException();
                    IJcopService _result = IJcopService.Stub.asInterface(_reply.readStrongBinder());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IeSEClientServicesAdapter impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IeSEClientServicesAdapter getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
