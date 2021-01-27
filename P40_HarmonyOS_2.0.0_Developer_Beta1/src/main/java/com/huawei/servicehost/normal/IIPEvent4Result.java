package com.huawei.servicehost.normal;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IIPEvent4Result extends IInterface {

    public static class Default implements IIPEvent4Result {
        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IIPEvent4Result {
        private static final String DESCRIPTOR = "com.huawei.servicehost.normal.IIPEvent4Result";

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IIPEvent4Result asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IIPEvent4Result)) {
                return new Proxy(obj);
            }
            return (IIPEvent4Result) iin;
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

        private static class Proxy implements IIPEvent4Result {
            public static IIPEvent4Result sDefaultImpl;
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

        public static boolean setDefaultImpl(IIPEvent4Result impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IIPEvent4Result getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
