package com.huawei.servicehost.normal;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IIPRequest4CancelCapture extends IInterface {

    public static class Default implements IIPRequest4CancelCapture {
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IIPRequest4CancelCapture {
        private static final String DESCRIPTOR = "com.huawei.servicehost.normal.IIPRequest4CancelCapture";

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IIPRequest4CancelCapture asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IIPRequest4CancelCapture)) {
                return new Proxy(obj);
            }
            return (IIPRequest4CancelCapture) iin;
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

        private static class Proxy implements IIPRequest4CancelCapture {
            public static IIPRequest4CancelCapture sDefaultImpl;
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

        public static boolean setDefaultImpl(IIPRequest4CancelCapture impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IIPRequest4CancelCapture getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
