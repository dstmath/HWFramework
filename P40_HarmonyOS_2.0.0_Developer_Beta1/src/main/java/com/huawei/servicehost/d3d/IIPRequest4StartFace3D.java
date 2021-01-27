package com.huawei.servicehost.d3d;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IIPRequest4StartFace3D extends IInterface {

    public static class Default implements IIPRequest4StartFace3D {
        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IIPRequest4StartFace3D {
        private static final String DESCRIPTOR = "com.huawei.servicehost.d3d.IIPRequest4StartFace3D";

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IIPRequest4StartFace3D asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IIPRequest4StartFace3D)) {
                return new Proxy(obj);
            }
            return (IIPRequest4StartFace3D) iin;
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

        private static class Proxy implements IIPRequest4StartFace3D {
            public static IIPRequest4StartFace3D sDefaultImpl;
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

        public static boolean setDefaultImpl(IIPRequest4StartFace3D impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IIPRequest4StartFace3D getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
