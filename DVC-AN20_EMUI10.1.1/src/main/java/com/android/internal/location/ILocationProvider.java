package com.android.internal.location;

import android.annotation.UnsupportedAppUsage;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.WorkSource;
import com.android.internal.location.ILocationProviderManager;

public interface ILocationProvider extends IInterface {
    @UnsupportedAppUsage
    int getStatus(Bundle bundle) throws RemoteException;

    @UnsupportedAppUsage
    long getStatusUpdateTime() throws RemoteException;

    void sendExtraCommand(String str, Bundle bundle) throws RemoteException;

    void setLocationProviderManager(ILocationProviderManager iLocationProviderManager) throws RemoteException;

    void setRequest(ProviderRequest providerRequest, WorkSource workSource) throws RemoteException;

    public static class Default implements ILocationProvider {
        @Override // com.android.internal.location.ILocationProvider
        public void setLocationProviderManager(ILocationProviderManager manager) throws RemoteException {
        }

        @Override // com.android.internal.location.ILocationProvider
        public void setRequest(ProviderRequest request, WorkSource ws) throws RemoteException {
        }

        @Override // com.android.internal.location.ILocationProvider
        public void sendExtraCommand(String command, Bundle extras) throws RemoteException {
        }

        @Override // com.android.internal.location.ILocationProvider
        public int getStatus(Bundle extras) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.location.ILocationProvider
        public long getStatusUpdateTime() throws RemoteException {
            return 0;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ILocationProvider {
        private static final String DESCRIPTOR = "com.android.internal.location.ILocationProvider";
        static final int TRANSACTION_getStatus = 4;
        static final int TRANSACTION_getStatusUpdateTime = 5;
        static final int TRANSACTION_sendExtraCommand = 3;
        static final int TRANSACTION_setLocationProviderManager = 1;
        static final int TRANSACTION_setRequest = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ILocationProvider asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ILocationProvider)) {
                return new Proxy(obj);
            }
            return (ILocationProvider) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "setLocationProviderManager";
            }
            if (transactionCode == 2) {
                return "setRequest";
            }
            if (transactionCode == 3) {
                return "sendExtraCommand";
            }
            if (transactionCode == 4) {
                return "getStatus";
            }
            if (transactionCode != 5) {
                return null;
            }
            return "getStatusUpdateTime";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            ProviderRequest _arg0;
            WorkSource _arg1;
            Bundle _arg12;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                setLocationProviderManager(ILocationProviderManager.Stub.asInterface(data.readStrongBinder()));
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = ProviderRequest.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                if (data.readInt() != 0) {
                    _arg1 = WorkSource.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                setRequest(_arg0, _arg1);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                String _arg02 = data.readString();
                if (data.readInt() != 0) {
                    _arg12 = Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg12 = null;
                }
                sendExtraCommand(_arg02, _arg12);
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                Bundle _arg03 = new Bundle();
                int _result = getStatus(_arg03);
                reply.writeNoException();
                reply.writeInt(_result);
                reply.writeInt(1);
                _arg03.writeToParcel(reply, 1);
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                long _result2 = getStatusUpdateTime();
                reply.writeNoException();
                reply.writeLong(_result2);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements ILocationProvider {
            public static ILocationProvider sDefaultImpl;
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

            @Override // com.android.internal.location.ILocationProvider
            public void setLocationProviderManager(ILocationProviderManager manager) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(manager != null ? manager.asBinder() : null);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setLocationProviderManager(manager);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.location.ILocationProvider
            public void setRequest(ProviderRequest request, WorkSource ws) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (request != null) {
                        _data.writeInt(1);
                        request.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (ws != null) {
                        _data.writeInt(1);
                        ws.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setRequest(request, ws);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.location.ILocationProvider
            public void sendExtraCommand(String command, Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(command);
                    if (extras != null) {
                        _data.writeInt(1);
                        extras.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().sendExtraCommand(command, extras);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // com.android.internal.location.ILocationProvider
            public int getStatus(Bundle extras) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getStatus(extras);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        extras.readFromParcel(_reply);
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.location.ILocationProvider
            public long getStatusUpdateTime() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getStatusUpdateTime();
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ILocationProvider impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ILocationProvider getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
