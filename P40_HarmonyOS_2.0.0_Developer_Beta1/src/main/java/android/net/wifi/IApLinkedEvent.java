package android.net.wifi;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IApLinkedEvent extends IInterface {
    void OnApLinkedStaJoin(String str) throws RemoteException;

    void OnApLinkedStaLeave(String str) throws RemoteException;

    public static class Default implements IApLinkedEvent {
        @Override // android.net.wifi.IApLinkedEvent
        public void OnApLinkedStaJoin(String macStr) throws RemoteException {
        }

        @Override // android.net.wifi.IApLinkedEvent
        public void OnApLinkedStaLeave(String macStr) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IApLinkedEvent {
        private static final String DESCRIPTOR = "android.net.wifi.IApLinkedEvent";
        static final int TRANSACTION_OnApLinkedStaJoin = 1;
        static final int TRANSACTION_OnApLinkedStaLeave = 2;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                OnApLinkedStaJoin(data.readString());
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                OnApLinkedStaLeave(data.readString());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IApLinkedEvent {
            public static IApLinkedEvent sDefaultImpl;
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

            @Override // android.net.wifi.IApLinkedEvent
            public void OnApLinkedStaJoin(String macStr) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(macStr);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().OnApLinkedStaJoin(macStr);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.net.wifi.IApLinkedEvent
            public void OnApLinkedStaLeave(String macStr) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(macStr);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().OnApLinkedStaLeave(macStr);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IApLinkedEvent impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IApLinkedEvent getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
