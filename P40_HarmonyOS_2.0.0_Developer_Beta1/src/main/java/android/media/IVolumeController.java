package android.media;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IVolumeController extends IInterface {
    void dismiss() throws RemoteException;

    void displaySafeVolumeWarning(int i) throws RemoteException;

    void masterMuteChanged(int i) throws RemoteException;

    void setA11yMode(int i) throws RemoteException;

    void setLayoutDirection(int i) throws RemoteException;

    void volumeChanged(int i, int i2) throws RemoteException;

    public static class Default implements IVolumeController {
        @Override // android.media.IVolumeController
        public void displaySafeVolumeWarning(int flags) throws RemoteException {
        }

        @Override // android.media.IVolumeController
        public void volumeChanged(int streamType, int flags) throws RemoteException {
        }

        @Override // android.media.IVolumeController
        public void masterMuteChanged(int flags) throws RemoteException {
        }

        @Override // android.media.IVolumeController
        public void setLayoutDirection(int layoutDirection) throws RemoteException {
        }

        @Override // android.media.IVolumeController
        public void dismiss() throws RemoteException {
        }

        @Override // android.media.IVolumeController
        public void setA11yMode(int mode) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IVolumeController {
        private static final String DESCRIPTOR = "android.media.IVolumeController";
        static final int TRANSACTION_dismiss = 5;
        static final int TRANSACTION_displaySafeVolumeWarning = 1;
        static final int TRANSACTION_masterMuteChanged = 3;
        static final int TRANSACTION_setA11yMode = 6;
        static final int TRANSACTION_setLayoutDirection = 4;
        static final int TRANSACTION_volumeChanged = 2;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "displaySafeVolumeWarning";
                case 2:
                    return "volumeChanged";
                case 3:
                    return "masterMuteChanged";
                case 4:
                    return "setLayoutDirection";
                case 5:
                    return "dismiss";
                case 6:
                    return "setA11yMode";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        displaySafeVolumeWarning(data.readInt());
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        volumeChanged(data.readInt(), data.readInt());
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        masterMuteChanged(data.readInt());
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        setLayoutDirection(data.readInt());
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        dismiss();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        setA11yMode(data.readInt());
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IVolumeController {
            public static IVolumeController sDefaultImpl;
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

            @Override // android.media.IVolumeController
            public void displaySafeVolumeWarning(int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flags);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().displaySafeVolumeWarning(flags);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.IVolumeController
            public void volumeChanged(int streamType, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(streamType);
                    _data.writeInt(flags);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().volumeChanged(streamType, flags);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.IVolumeController
            public void masterMuteChanged(int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(flags);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().masterMuteChanged(flags);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.IVolumeController
            public void setLayoutDirection(int layoutDirection) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(layoutDirection);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setLayoutDirection(layoutDirection);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.IVolumeController
            public void dismiss() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().dismiss();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.IVolumeController
            public void setA11yMode(int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mode);
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setA11yMode(mode);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IVolumeController impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IVolumeController getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
