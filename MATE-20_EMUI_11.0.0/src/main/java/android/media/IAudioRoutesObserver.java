package android.media;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IAudioRoutesObserver extends IInterface {
    void dispatchAudioRoutesChanged(AudioRoutesInfo audioRoutesInfo) throws RemoteException;

    public static class Default implements IAudioRoutesObserver {
        @Override // android.media.IAudioRoutesObserver
        public void dispatchAudioRoutesChanged(AudioRoutesInfo newRoutes) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IAudioRoutesObserver {
        private static final String DESCRIPTOR = "android.media.IAudioRoutesObserver";
        static final int TRANSACTION_dispatchAudioRoutesChanged = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAudioRoutesObserver asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAudioRoutesObserver)) {
                return new Proxy(obj);
            }
            return (IAudioRoutesObserver) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode != 1) {
                return null;
            }
            return "dispatchAudioRoutesChanged";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            AudioRoutesInfo _arg0;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = AudioRoutesInfo.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                dispatchAudioRoutesChanged(_arg0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IAudioRoutesObserver {
            public static IAudioRoutesObserver sDefaultImpl;
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

            @Override // android.media.IAudioRoutesObserver
            public void dispatchAudioRoutesChanged(AudioRoutesInfo newRoutes) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (newRoutes != null) {
                        _data.writeInt(1);
                        newRoutes.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().dispatchAudioRoutesChanged(newRoutes);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IAudioRoutesObserver impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IAudioRoutesObserver getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
