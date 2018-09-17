package android.media;

import android.media.VolumeShaper.Configuration;
import android.media.VolumeShaper.Operation;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IPlayer extends IInterface {

    public static abstract class Stub extends Binder implements IPlayer {
        private static final String DESCRIPTOR = "android.media.IPlayer";
        static final int TRANSACTION_applyVolumeShaper = 7;
        static final int TRANSACTION_pause = 2;
        static final int TRANSACTION_setPan = 5;
        static final int TRANSACTION_setStartDelayMs = 6;
        static final int TRANSACTION_setVolume = 4;
        static final int TRANSACTION_start = 1;
        static final int TRANSACTION_stop = 3;

        private static class Proxy implements IPlayer {
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

            public void start() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(1, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void pause() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(2, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void stop() throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(3, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setVolume(float vol) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeFloat(vol);
                    this.mRemote.transact(4, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setPan(float pan) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeFloat(pan);
                    this.mRemote.transact(5, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void setStartDelayMs(int delayMs) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(delayMs);
                    this.mRemote.transact(6, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }

            public void applyVolumeShaper(Configuration configuration, Operation operation) throws RemoteException {
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
                    this.mRemote.transact(7, _data, null, 1);
                } finally {
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
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
                    Configuration _arg0;
                    Operation _arg1;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg0 = (Configuration) Configuration.CREATOR.createFromParcel(data);
                    } else {
                        _arg0 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg1 = (Operation) Operation.CREATOR.createFromParcel(data);
                    } else {
                        _arg1 = null;
                    }
                    applyVolumeShaper(_arg0, _arg1);
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    void applyVolumeShaper(Configuration configuration, Operation operation) throws RemoteException;

    void pause() throws RemoteException;

    void setPan(float f) throws RemoteException;

    void setStartDelayMs(int i) throws RemoteException;

    void setVolume(float f) throws RemoteException;

    void start() throws RemoteException;

    void stop() throws RemoteException;
}
