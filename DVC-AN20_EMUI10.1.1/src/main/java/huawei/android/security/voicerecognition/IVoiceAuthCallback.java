package huawei.android.security.voicerecognition;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IVoiceAuthCallback extends IInterface {
    void onHeadsetStatusChange(int i) throws RemoteException;

    void onReceiveAuthVoice(int i, int i2) throws RemoteException;

    void onReceiveUnAuthVoice(int i, int i2) throws RemoteException;

    public static class Default implements IVoiceAuthCallback {
        @Override // huawei.android.security.voicerecognition.IVoiceAuthCallback
        public void onReceiveAuthVoice(int userId, int type) throws RemoteException {
        }

        @Override // huawei.android.security.voicerecognition.IVoiceAuthCallback
        public void onReceiveUnAuthVoice(int userId, int type) throws RemoteException {
        }

        @Override // huawei.android.security.voicerecognition.IVoiceAuthCallback
        public void onHeadsetStatusChange(int status) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IVoiceAuthCallback {
        private static final String DESCRIPTOR = "huawei.android.security.voicerecognition.IVoiceAuthCallback";
        static final int TRANSACTION_onHeadsetStatusChange = 3;
        static final int TRANSACTION_onReceiveAuthVoice = 1;
        static final int TRANSACTION_onReceiveUnAuthVoice = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IVoiceAuthCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IVoiceAuthCallback)) {
                return new Proxy(obj);
            }
            return (IVoiceAuthCallback) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onReceiveAuthVoice(data.readInt(), data.readInt());
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onReceiveUnAuthVoice(data.readInt(), data.readInt());
                reply.writeNoException();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                onHeadsetStatusChange(data.readInt());
                reply.writeNoException();
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IVoiceAuthCallback {
            public static IVoiceAuthCallback sDefaultImpl;
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

            @Override // huawei.android.security.voicerecognition.IVoiceAuthCallback
            public void onReceiveAuthVoice(int userId, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(type);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onReceiveAuthVoice(userId, type);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.voicerecognition.IVoiceAuthCallback
            public void onReceiveUnAuthVoice(int userId, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(type);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onReceiveUnAuthVoice(userId, type);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.voicerecognition.IVoiceAuthCallback
            public void onHeadsetStatusChange(int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(status);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onHeadsetStatusChange(status);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IVoiceAuthCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IVoiceAuthCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
