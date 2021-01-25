package android.media;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IVolumeChangeDispatcher extends IInterface {
    void dispatchVolumeChange(int i, int i2, String str, int i3) throws RemoteException;

    public static class Default implements IVolumeChangeDispatcher {
        @Override // android.media.IVolumeChangeDispatcher
        public void dispatchVolumeChange(int device, int stream, String caller, int volume) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IVolumeChangeDispatcher {
        private static final String DESCRIPTOR = "android.media.IVolumeChangeDispatcher";
        static final int TRANSACTION_dispatchVolumeChange = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IVolumeChangeDispatcher asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IVolumeChangeDispatcher)) {
                return new Proxy(obj);
            }
            return (IVolumeChangeDispatcher) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode != 1) {
                return null;
            }
            return "dispatchVolumeChange";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                dispatchVolumeChange(data.readInt(), data.readInt(), data.readString(), data.readInt());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IVolumeChangeDispatcher {
            public static IVolumeChangeDispatcher sDefaultImpl;
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

            @Override // android.media.IVolumeChangeDispatcher
            public void dispatchVolumeChange(int device, int stream, String caller, int volume) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(device);
                    _data.writeInt(stream);
                    _data.writeString(caller);
                    _data.writeInt(volume);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().dispatchVolumeChange(device, stream, caller, volume);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IVolumeChangeDispatcher impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IVolumeChangeDispatcher getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
