package com.huawei.airsharing.api;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IAidlMediaPlayerListener extends IInterface {
    void onBufferedPositionChanged(int i) throws RemoteException;

    void onError(NotificationInfo notificationInfo) throws RemoteException;

    void onMediaItemChanged(NotificationInfo notificationInfo) throws RemoteException;

    void onPositionChanged(int i) throws RemoteException;

    void onRateChanged(float f) throws RemoteException;

    void onRepeatModeChanged(String str) throws RemoteException;

    void onStateChanged(NotificationInfo notificationInfo) throws RemoteException;

    void onVolumeChanged(int i) throws RemoteException;

    void onVolumeMutedChanged(boolean z) throws RemoteException;

    public static abstract class Stub extends Binder implements IAidlMediaPlayerListener {
        private static final String DESCRIPTOR = "com.huawei.airsharing.api.IAidlMediaPlayerListener";
        static final int TRANSACTION_onBufferedPositionChanged = 8;
        static final int TRANSACTION_onError = 3;
        static final int TRANSACTION_onMediaItemChanged = 2;
        static final int TRANSACTION_onPositionChanged = 5;
        static final int TRANSACTION_onRateChanged = 6;
        static final int TRANSACTION_onRepeatModeChanged = 7;
        static final int TRANSACTION_onStateChanged = 1;
        static final int TRANSACTION_onVolumeChanged = 4;
        static final int TRANSACTION_onVolumeMutedChanged = 9;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAidlMediaPlayerListener asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAidlMediaPlayerListener)) {
                return new Proxy(obj);
            }
            return (IAidlMediaPlayerListener) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            NotificationInfo _arg0;
            NotificationInfo _arg02;
            NotificationInfo _arg03;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = NotificationInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        onStateChanged(_arg0);
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = NotificationInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        onMediaItemChanged(_arg02);
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = NotificationInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        onError(_arg03);
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        onVolumeChanged(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        onPositionChanged(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        onRateChanged(data.readFloat());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        onRepeatModeChanged(data.readString());
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        onBufferedPositionChanged(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        onVolumeMutedChanged(data.readInt() != 0);
                        reply.writeNoException();
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IAidlMediaPlayerListener {
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

            @Override // com.huawei.airsharing.api.IAidlMediaPlayerListener
            public void onStateChanged(NotificationInfo info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.api.IAidlMediaPlayerListener
            public void onMediaItemChanged(NotificationInfo info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.api.IAidlMediaPlayerListener
            public void onError(NotificationInfo info) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.api.IAidlMediaPlayerListener
            public void onVolumeChanged(int currVolume) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(currVolume);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.api.IAidlMediaPlayerListener
            public void onPositionChanged(int currPosition) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(currPosition);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.api.IAidlMediaPlayerListener
            public void onRateChanged(float currRate) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeFloat(currRate);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.api.IAidlMediaPlayerListener
            public void onRepeatModeChanged(String repeatMode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(repeatMode);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.api.IAidlMediaPlayerListener
            public void onBufferedPositionChanged(int bufferedPosition) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(bufferedPosition);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.airsharing.api.IAidlMediaPlayerListener
            public void onVolumeMutedChanged(boolean isMute) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(isMute ? 1 : 0);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
