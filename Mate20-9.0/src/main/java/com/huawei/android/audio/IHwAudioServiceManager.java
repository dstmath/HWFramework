package com.huawei.android.audio;

import android.media.IAudioModeDispatcher;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwAudioServiceManager extends IInterface {

    public static abstract class Stub extends Binder implements IHwAudioServiceManager {
        private static final String DESCRIPTOR = "com.huawei.android.audio.IHwAudioServiceManager";
        static final int TRANSACTION_checkMicMute = 3;
        static final int TRANSACTION_checkRecordActive = 2;
        static final int TRANSACTION_getRecordConcurrentType = 5;
        static final int TRANSACTION_registerAudioModeCallback = 6;
        static final int TRANSACTION_sendRecordStateChangedIntent = 4;
        static final int TRANSACTION_setSoundEffectState = 1;
        static final int TRANSACTION_unregisterAudioModeCallback = 7;

        private static class Proxy implements IHwAudioServiceManager {
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

            public int setSoundEffectState(boolean restore, String packageName, boolean isOnTop, String reserved) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(restore);
                    _data.writeString(packageName);
                    _data.writeInt(isOnTop);
                    _data.writeString(reserved);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean checkRecordActive() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void checkMicMute() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void sendRecordStateChangedIntent(String sender, int state, int pid, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(sender);
                    _data.writeInt(state);
                    _data.writeInt(pid);
                    _data.writeString(packageName);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getRecordConcurrentType(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerAudioModeCallback(IAudioModeDispatcher pcdb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(pcdb != null ? pcdb.asBinder() : null);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterAudioModeCallback(IAudioModeDispatcher pcdb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(pcdb != null ? pcdb.asBinder() : null);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwAudioServiceManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwAudioServiceManager)) {
                return new Proxy(obj);
            }
            return (IHwAudioServiceManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _arg2 = false;
                        boolean _arg0 = data.readInt() != 0;
                        String _arg1 = data.readString();
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        int _result = setSoundEffectState(_arg0, _arg1, _arg2, data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        boolean _result2 = checkRecordActive();
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        checkMicMute();
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        sendRecordStateChangedIntent(data.readString(), data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = getRecordConcurrentType(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        registerAudioModeCallback(IAudioModeDispatcher.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterAudioModeCallback(IAudioModeDispatcher.Stub.asInterface(data.readStrongBinder()));
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
    }

    void checkMicMute() throws RemoteException;

    boolean checkRecordActive() throws RemoteException;

    int getRecordConcurrentType(String str) throws RemoteException;

    void registerAudioModeCallback(IAudioModeDispatcher iAudioModeDispatcher) throws RemoteException;

    void sendRecordStateChangedIntent(String str, int i, int i2, String str2) throws RemoteException;

    int setSoundEffectState(boolean z, String str, boolean z2, String str2) throws RemoteException;

    void unregisterAudioModeCallback(IAudioModeDispatcher iAudioModeDispatcher) throws RemoteException;
}
