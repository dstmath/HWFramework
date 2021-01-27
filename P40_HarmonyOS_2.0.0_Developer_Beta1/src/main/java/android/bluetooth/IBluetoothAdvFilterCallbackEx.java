package android.bluetooth;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IBluetoothAdvFilterCallbackEx extends IInterface {

    public static class Default implements IBluetoothAdvFilterCallbackEx {
        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IBluetoothAdvFilterCallbackEx {
        private static final String DESCRIPTOR = "android.bluetooth.IBluetoothAdvFilterCallbackEx";

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IBluetoothAdvFilterCallbackEx asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IBluetoothAdvFilterCallbackEx)) {
                return new Proxy(obj);
            }
            return (IBluetoothAdvFilterCallbackEx) iin;
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

        private static class Proxy implements IBluetoothAdvFilterCallbackEx {
            public static IBluetoothAdvFilterCallbackEx sDefaultImpl;
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

        public static boolean setDefaultImpl(IBluetoothAdvFilterCallbackEx impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IBluetoothAdvFilterCallbackEx getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
