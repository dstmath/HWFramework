package android.media;

import android.media.session.MediaSession;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IRemoteVolumeController extends IInterface {
    void remoteVolumeChanged(MediaSession.Token token, int i) throws RemoteException;

    void updateRemoteController(MediaSession.Token token) throws RemoteException;

    public static class Default implements IRemoteVolumeController {
        @Override // android.media.IRemoteVolumeController
        public void remoteVolumeChanged(MediaSession.Token sessionToken, int flags) throws RemoteException {
        }

        @Override // android.media.IRemoteVolumeController
        public void updateRemoteController(MediaSession.Token sessionToken) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IRemoteVolumeController {
        private static final String DESCRIPTOR = "android.media.IRemoteVolumeController";
        static final int TRANSACTION_remoteVolumeChanged = 1;
        static final int TRANSACTION_updateRemoteController = 2;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "remoteVolumeChanged";
            }
            if (transactionCode != 2) {
                return null;
            }
            return "updateRemoteController";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            MediaSession.Token _arg0;
            MediaSession.Token _arg02;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg0 = MediaSession.Token.CREATOR.createFromParcel(data);
                } else {
                    _arg0 = null;
                }
                remoteVolumeChanged(_arg0, data.readInt());
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                if (data.readInt() != 0) {
                    _arg02 = MediaSession.Token.CREATOR.createFromParcel(data);
                } else {
                    _arg02 = null;
                }
                updateRemoteController(_arg02);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IRemoteVolumeController {
            public static IRemoteVolumeController sDefaultImpl;
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

            @Override // android.media.IRemoteVolumeController
            public void remoteVolumeChanged(MediaSession.Token sessionToken, int flags) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (sessionToken != null) {
                        _data.writeInt(1);
                        sessionToken.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(flags);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().remoteVolumeChanged(sessionToken, flags);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.IRemoteVolumeController
            public void updateRemoteController(MediaSession.Token sessionToken) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (sessionToken != null) {
                        _data.writeInt(1);
                        sessionToken.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().updateRemoteController(sessionToken);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IRemoteVolumeController impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IRemoteVolumeController getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
