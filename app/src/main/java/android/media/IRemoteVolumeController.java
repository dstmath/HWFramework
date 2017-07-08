package android.media;

import android.media.session.ISessionController;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IRemoteVolumeController extends IInterface {

    public static abstract class Stub extends Binder implements IRemoteVolumeController {
        private static final String DESCRIPTOR = "android.media.IRemoteVolumeController";
        static final int TRANSACTION_remoteVolumeChanged = 1;
        static final int TRANSACTION_updateRemoteController = 2;

        private static class Proxy implements IRemoteVolumeController {
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

            public void remoteVolumeChanged(ISessionController session, int flags) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (session != null) {
                        iBinder = session.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    _data.writeInt(flags);
                    this.mRemote.transact(Stub.TRANSACTION_remoteVolumeChanged, _data, null, Stub.TRANSACTION_remoteVolumeChanged);
                } finally {
                    _data.recycle();
                }
            }

            public void updateRemoteController(ISessionController session) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (session != null) {
                        iBinder = session.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_updateRemoteController, _data, null, Stub.TRANSACTION_remoteVolumeChanged);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IRemoteVolumeController asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IRemoteVolumeController)) {
                return new Proxy(obj);
            }
            return (IRemoteVolumeController) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_remoteVolumeChanged /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    remoteVolumeChanged(android.media.session.ISessionController.Stub.asInterface(data.readStrongBinder()), data.readInt());
                    return true;
                case TRANSACTION_updateRemoteController /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    updateRemoteController(android.media.session.ISessionController.Stub.asInterface(data.readStrongBinder()));
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void remoteVolumeChanged(ISessionController iSessionController, int i) throws RemoteException;

    void updateRemoteController(ISessionController iSessionController) throws RemoteException;
}
