package android.emcom;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IOneHopAuthReqCallback extends IInterface {
    void onAuthResult(boolean z) throws RemoteException;

    public static class Default implements IOneHopAuthReqCallback {
        @Override // android.emcom.IOneHopAuthReqCallback
        public void onAuthResult(boolean result) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IOneHopAuthReqCallback {
        private static final String DESCRIPTOR = "android.emcom.IOneHopAuthReqCallback";
        static final int TRANSACTION_onAuthResult = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IOneHopAuthReqCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IOneHopAuthReqCallback)) {
                return new Proxy(obj);
            }
            return (IOneHopAuthReqCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onAuthResult(data.readInt() != 0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IOneHopAuthReqCallback {
            public static IOneHopAuthReqCallback sDefaultImpl;
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

            @Override // android.emcom.IOneHopAuthReqCallback
            public void onAuthResult(boolean result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(result ? 1 : 0);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().onAuthResult(result);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IOneHopAuthReqCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IOneHopAuthReqCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
