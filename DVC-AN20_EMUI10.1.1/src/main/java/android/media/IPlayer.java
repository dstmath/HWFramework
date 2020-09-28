package android.media;

import android.media.VolumeShaper;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.provider.Telephony;

public interface IPlayer extends IInterface {
    void applyVolumeShaper(VolumeShaper.Configuration configuration, VolumeShaper.Operation operation) throws RemoteException;

    void pause() throws RemoteException;

    void setPan(float f) throws RemoteException;

    void setStartDelayMs(int i) throws RemoteException;

    void setVolume(float f) throws RemoteException;

    void start() throws RemoteException;

    void stop() throws RemoteException;

    public static class Default implements IPlayer {
        @Override // android.media.IPlayer
        public void start() throws RemoteException {
        }

        @Override // android.media.IPlayer
        public void pause() throws RemoteException {
        }

        @Override // android.media.IPlayer
        public void stop() throws RemoteException {
        }

        @Override // android.media.IPlayer
        public void setVolume(float vol) throws RemoteException {
        }

        @Override // android.media.IPlayer
        public void setPan(float pan) throws RemoteException {
        }

        @Override // android.media.IPlayer
        public void setStartDelayMs(int delayMs) throws RemoteException {
        }

        @Override // android.media.IPlayer
        public void applyVolumeShaper(VolumeShaper.Configuration configuration, VolumeShaper.Operation operation) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IPlayer {
        private static final String DESCRIPTOR = "android.media.IPlayer";
        static final int TRANSACTION_applyVolumeShaper = 7;
        static final int TRANSACTION_pause = 2;
        static final int TRANSACTION_setPan = 5;
        static final int TRANSACTION_setStartDelayMs = 6;
        static final int TRANSACTION_setVolume = 4;
        static final int TRANSACTION_start = 1;
        static final int TRANSACTION_stop = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IPlayer asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IPlayer)) {
                return new Proxy(obj);
            }
            return (IPlayer) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return Telephony.BaseMmsColumns.START;
                case 2:
                    return "pause";
                case 3:
                    return "stop";
                case 4:
                    return "setVolume";
                case 5:
                    return "setPan";
                case 6:
                    return "setStartDelayMs";
                case 7:
                    return "applyVolumeShaper";
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
            VolumeShaper.Configuration _arg0;
            VolumeShaper.Operation _arg1;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        start();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        pause();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        stop();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        setVolume(data.readFloat());
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        setPan(data.readFloat());
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        setStartDelayMs(data.readInt());
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = VolumeShaper.Configuration.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg1 = VolumeShaper.Operation.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        applyVolumeShaper(_arg0, _arg1);
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
        public static class Proxy implements IPlayer {
            public static IPlayer sDefaultImpl;
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

            @Override // android.media.IPlayer
            public void start() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(1, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().start();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.IPlayer
            public void pause() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().pause();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.IPlayer
            public void stop() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().stop();
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.IPlayer
            public void setVolume(float vol) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeFloat(vol);
                    if (this.mRemote.transact(4, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setVolume(vol);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.IPlayer
            public void setPan(float pan) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeFloat(pan);
                    if (this.mRemote.transact(5, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setPan(pan);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.IPlayer
            public void setStartDelayMs(int delayMs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(delayMs);
                    if (this.mRemote.transact(6, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().setStartDelayMs(delayMs);
                    }
                } finally {
                    _data.recycle();
                }
            }

            @Override // android.media.IPlayer
            public void applyVolumeShaper(VolumeShaper.Configuration configuration, VolumeShaper.Operation operation) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (configuration != null) {
                        _data.writeInt(1);
                        configuration.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (operation != null) {
                        _data.writeInt(1);
                        operation.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(7, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().applyVolumeShaper(configuration, operation);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IPlayer impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IPlayer getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
