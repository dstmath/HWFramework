package android.media;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IVolumeController extends IInterface {

    public static abstract class Stub extends Binder implements IVolumeController {
        private static final String DESCRIPTOR = "android.media.IVolumeController";
        static final int TRANSACTION_dismiss = 5;
        static final int TRANSACTION_displaySafeVolumeWarning = 1;
        static final int TRANSACTION_masterMuteChanged = 3;
        static final int TRANSACTION_setLayoutDirection = 4;
        static final int TRANSACTION_volumeChanged = 2;

        private static class Proxy implements IVolumeController {
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

            public void displaySafeVolumeWarning(int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flags);
                    this.mRemote.transact(Stub.TRANSACTION_displaySafeVolumeWarning, _data, null, Stub.TRANSACTION_displaySafeVolumeWarning);
                } finally {
                    _data.recycle();
                }
            }

            public void volumeChanged(int streamType, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(streamType);
                    _data.writeInt(flags);
                    this.mRemote.transact(Stub.TRANSACTION_volumeChanged, _data, null, Stub.TRANSACTION_displaySafeVolumeWarning);
                } finally {
                    _data.recycle();
                }
            }

            public void masterMuteChanged(int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flags);
                    this.mRemote.transact(Stub.TRANSACTION_masterMuteChanged, _data, null, Stub.TRANSACTION_displaySafeVolumeWarning);
                } finally {
                    _data.recycle();
                }
            }

            public void setLayoutDirection(int layoutDirection) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(layoutDirection);
                    this.mRemote.transact(Stub.TRANSACTION_setLayoutDirection, _data, null, Stub.TRANSACTION_displaySafeVolumeWarning);
                } finally {
                    _data.recycle();
                }
            }

            public void dismiss() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_dismiss, _data, null, Stub.TRANSACTION_displaySafeVolumeWarning);
                } finally {
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IVolumeController asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IVolumeController)) {
                return new Proxy(obj);
            }
            return (IVolumeController) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            switch (code) {
                case TRANSACTION_displaySafeVolumeWarning /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    displaySafeVolumeWarning(data.readInt());
                    return true;
                case TRANSACTION_volumeChanged /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    volumeChanged(data.readInt(), data.readInt());
                    return true;
                case TRANSACTION_masterMuteChanged /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    masterMuteChanged(data.readInt());
                    return true;
                case TRANSACTION_setLayoutDirection /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    setLayoutDirection(data.readInt());
                    return true;
                case TRANSACTION_dismiss /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    dismiss();
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void dismiss() throws RemoteException;

    void displaySafeVolumeWarning(int i) throws RemoteException;

    void masterMuteChanged(int i) throws RemoteException;

    void setLayoutDirection(int i) throws RemoteException;

    void volumeChanged(int i, int i2) throws RemoteException;
}
