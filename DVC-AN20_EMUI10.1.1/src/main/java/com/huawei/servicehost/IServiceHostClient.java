package com.huawei.servicehost;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IServiceHostClient extends IInterface {

    public static class Default implements IServiceHostClient {
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IServiceHostClient {
        private static final String DESCRIPTOR = "com.huawei.servicehost.IServiceHostClient";

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IServiceHostClient asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IServiceHostClient)) {
                return new Proxy(obj);
            }
            return (IServiceHostClient) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            }
            reply.writeString(DESCRIPTOR);
            return true;
        }

        private static class Proxy implements IServiceHostClient {
            public static IServiceHostClient sDefaultImpl;
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
        }

        public static boolean setDefaultImpl(IServiceHostClient impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IServiceHostClient getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
