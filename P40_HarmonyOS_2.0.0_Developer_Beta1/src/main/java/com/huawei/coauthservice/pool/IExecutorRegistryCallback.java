package com.huawei.coauthservice.pool;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.coauthservice.pool.IExecutorRegistry;

public interface IExecutorRegistryCallback extends IInterface {
    void executorSecureRegistryCallback(IExecutorRegistry iExecutorRegistry) throws RemoteException;

    public static abstract class Stub extends Binder implements IExecutorRegistryCallback {
        private static final String DESCRIPTOR = "com.huawei.coauthservice.pool.IExecutorRegistryCallback";
        static final int TRANSACTION_executorSecureRegistryCallback = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IExecutorRegistryCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IExecutorRegistryCallback)) {
                return new Proxy(obj);
            }
            return (IExecutorRegistryCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                executorSecureRegistryCallback(IExecutorRegistry.Stub.asInterface(data.readStrongBinder()));
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IExecutorRegistryCallback {
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

            @Override // com.huawei.coauthservice.pool.IExecutorRegistryCallback
            public void executorSecureRegistryCallback(IExecutorRegistry registry) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(registry != null ? registry.asBinder() : null);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }
    }
}
