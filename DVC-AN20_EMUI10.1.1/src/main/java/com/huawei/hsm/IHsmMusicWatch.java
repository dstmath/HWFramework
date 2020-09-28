package com.huawei.hsm;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHsmMusicWatch extends IInterface {
    int onMusicPauseOrStop(int i, int i2) throws RemoteException;

    int onMusicPlaying(int i, int i2) throws RemoteException;

    public static class Default implements IHsmMusicWatch {
        @Override // com.huawei.hsm.IHsmMusicWatch
        public int onMusicPlaying(int uid, int pid) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.hsm.IHsmMusicWatch
        public int onMusicPauseOrStop(int uid, int pid) throws RemoteException {
            return 0;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHsmMusicWatch {
        private static final String DESCRIPTOR = "com.huawei.hsm.IHsmMusicWatch";
        static final int TRANSACTION_onMusicPauseOrStop = 2;
        static final int TRANSACTION_onMusicPlaying = 1;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHsmMusicWatch asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHsmMusicWatch)) {
                return new Proxy(obj);
            }
            return (IHsmMusicWatch) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                int _result = onMusicPlaying(data.readInt(), data.readInt());
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                int _result2 = onMusicPauseOrStop(data.readInt(), data.readInt());
                reply.writeNoException();
                reply.writeInt(_result2);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHsmMusicWatch {
            public static IHsmMusicWatch sDefaultImpl;
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

            @Override // com.huawei.hsm.IHsmMusicWatch
            public int onMusicPlaying(int uid, int pid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(pid);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().onMusicPlaying(uid, pid);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.hsm.IHsmMusicWatch
            public int onMusicPauseOrStop(int uid, int pid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(pid);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().onMusicPauseOrStop(uid, pid);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHsmMusicWatch impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHsmMusicWatch getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
