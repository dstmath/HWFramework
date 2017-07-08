package android.media.tv;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ITvRemoteProvider extends IInterface {

    public static abstract class Stub extends Binder implements ITvRemoteProvider {
        private static final String DESCRIPTOR = "android.media.tv.ITvRemoteProvider";
        static final int TRANSACTION_onInputBridgeConnected = 2;
        static final int TRANSACTION_setRemoteServiceInputSink = 1;

        private static class Proxy implements ITvRemoteProvider {
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

            public void setRemoteServiceInputSink(ITvRemoteServiceInput tvServiceInput) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (tvServiceInput != null) {
                        iBinder = tvServiceInput.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_setRemoteServiceInputSink, _data, null, Stub.TRANSACTION_setRemoteServiceInputSink);
                } finally {
                    _data.recycle();
                }
            }

            public void onInputBridgeConnected(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(Stub.TRANSACTION_onInputBridgeConnected, _data, null, Stub.TRANSACTION_setRemoteServiceInputSink);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ITvRemoteProvider asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ITvRemoteProvider)) {
                return new Proxy(obj);
            }
            return (ITvRemoteProvider) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_setRemoteServiceInputSink /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    setRemoteServiceInputSink(android.media.tv.ITvRemoteServiceInput.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case TRANSACTION_onInputBridgeConnected /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    onInputBridgeConnected(data.readStrongBinder());
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void onInputBridgeConnected(IBinder iBinder) throws RemoteException;

    void setRemoteServiceInputSink(ITvRemoteServiceInput iTvRemoteServiceInput) throws RemoteException;
}
