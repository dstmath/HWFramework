package android.net.wifi;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IApLinkedEvent extends IInterface {

    public static abstract class Stub extends Binder implements IApLinkedEvent {
        private static final String DESCRIPTOR = "android.net.wifi.IApLinkedEvent";
        static final int TRANSACTION_OnApLinkedStaJoin = 1;
        static final int TRANSACTION_OnApLinkedStaLeave = 2;

        private static class Proxy implements IApLinkedEvent {
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

            public void OnApLinkedStaJoin(String macStr) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(macStr);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void OnApLinkedStaLeave(String macStr) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(macStr);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IApLinkedEvent asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IApLinkedEvent)) {
                return new Proxy(obj);
            }
            return (IApLinkedEvent) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    OnApLinkedStaJoin(data.readString());
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    OnApLinkedStaLeave(data.readString());
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void OnApLinkedStaJoin(String str) throws RemoteException;

    void OnApLinkedStaLeave(String str) throws RemoteException;
}
