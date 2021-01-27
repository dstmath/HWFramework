package com.huawei.servicehost.d3d;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IIPRequest4DestoryPipeline extends IInterface {

    public static class Default implements IIPRequest4DestoryPipeline {
        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IIPRequest4DestoryPipeline {
        private static final String DESCRIPTOR = "com.huawei.servicehost.d3d.IIPRequest4DestoryPipeline";

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IIPRequest4DestoryPipeline asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IIPRequest4DestoryPipeline)) {
                return new Proxy(obj);
            }
            return (IIPRequest4DestoryPipeline) iin;
        }

        @Override // android.os.IInterface
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

        private static class Proxy implements IIPRequest4DestoryPipeline {
            public static IIPRequest4DestoryPipeline sDefaultImpl;
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
        }

        public static boolean setDefaultImpl(IIPRequest4DestoryPipeline impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IIPRequest4DestoryPipeline getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
