package com.huawei.android.app;

import android.app.IApplicationThread;
import android.app.IServiceConnection;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IServiceHooker extends IInterface {
    void bindService(IApplicationThread iApplicationThread, IBinder iBinder, Intent intent, String str, IServiceConnection iServiceConnection, int i, String str2, String str3, int i2) throws RemoteException;

    void unbindService(IServiceConnection iServiceConnection) throws RemoteException;

    public static class Default implements IServiceHooker {
        @Override // com.huawei.android.app.IServiceHooker
        public void bindService(IApplicationThread caller, IBinder token, Intent service, String resolvedType, IServiceConnection connection, int flags, String instanceName, String callingPackage, int userId) throws RemoteException {
        }

        @Override // com.huawei.android.app.IServiceHooker
        public void unbindService(IServiceConnection connection) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IServiceHooker {
        private static final String DESCRIPTOR = "com.huawei.android.app.IServiceHooker";
        static final int TRANSACTION_bindService = 1;
        static final int TRANSACTION_unbindService = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IServiceHooker asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IServiceHooker)) {
                return new Proxy(obj);
            }
            return (IServiceHooker) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "bindService";
            }
            if (transactionCode != 2) {
                return null;
            }
            return "unbindService";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Intent _arg2;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                IApplicationThread _arg0 = IApplicationThread.Stub.asInterface(data.readStrongBinder());
                IBinder _arg1 = data.readStrongBinder();
                if (data.readInt() != 0) {
                    _arg2 = Intent.CREATOR.createFromParcel(data);
                } else {
                    _arg2 = null;
                }
                bindService(_arg0, _arg1, _arg2, data.readString(), IServiceConnection.Stub.asInterface(data.readStrongBinder()), data.readInt(), data.readString(), data.readString(), data.readInt());
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                unbindService(IServiceConnection.Stub.asInterface(data.readStrongBinder()));
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IServiceHooker {
            public static IServiceHooker sDefaultImpl;
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

            @Override // com.huawei.android.app.IServiceHooker
            public void bindService(IApplicationThread caller, IBinder token, Intent service, String resolvedType, IServiceConnection connection, int flags, String instanceName, String callingPackage, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(caller != null ? caller.asBinder() : null);
                    try {
                        _data.writeStrongBinder(token);
                        if (service != null) {
                            _data.writeInt(1);
                            service.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        try {
                            _data.writeString(resolvedType);
                            _data.writeStrongBinder(connection != null ? connection.asBinder() : null);
                        } catch (Throwable th) {
                            th = th;
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeInt(flags);
                            _data.writeString(instanceName);
                            _data.writeString(callingPackage);
                            _data.writeInt(userId);
                            if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().bindService(caller, token, service, resolvedType, connection, flags, instanceName, callingPackage, userId);
                            _data.recycle();
                        } catch (Throwable th2) {
                            th = th2;
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.huawei.android.app.IServiceHooker
            public void unbindService(IServiceConnection connection) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(connection != null ? connection.asBinder() : null);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().unbindService(connection);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IServiceHooker impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IServiceHooker getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
