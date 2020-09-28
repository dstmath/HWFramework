package huawei.android.security.facerecognition;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IFaceRecognizeServiceReceiver extends IInterface {
    void onAuthenticationAcquired(int i) throws RemoteException;

    void onAuthenticationCancel() throws RemoteException;

    void onAuthenticationResult(int i, int i2) throws RemoteException;

    void onCallback(long j, int i, int i2, int i3) throws RemoteException;

    void onCallbackResult(int i, int i2, int i3, int i4) throws RemoteException;

    void onEnrollAcquired(int i, int i2) throws RemoteException;

    void onEnrollCancel() throws RemoteException;

    void onEnrollResult(int i, int i2, int i3) throws RemoteException;

    void onRemovedResult(int i, int i2) throws RemoteException;

    public static class Default implements IFaceRecognizeServiceReceiver {
        @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceReceiver
        public void onEnrollResult(int faceId, int userId, int errorCode) throws RemoteException {
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceReceiver
        public void onEnrollCancel() throws RemoteException {
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceReceiver
        public void onEnrollAcquired(int acquiredInfo, int process) throws RemoteException {
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceReceiver
        public void onAuthenticationResult(int userId, int errorCode) throws RemoteException {
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceReceiver
        public void onAuthenticationCancel() throws RemoteException {
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceReceiver
        public void onAuthenticationAcquired(int acquiredInfo) throws RemoteException {
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceReceiver
        public void onRemovedResult(int userId, int errorCode) throws RemoteException {
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceReceiver
        public void onCallback(long reqId, int type, int code, int errorCode) throws RemoteException {
        }

        @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceReceiver
        public void onCallbackResult(int reqId, int type, int code, int errorCode) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IFaceRecognizeServiceReceiver {
        private static final String DESCRIPTOR = "huawei.android.security.facerecognition.IFaceRecognizeServiceReceiver";
        static final int TRANSACTION_onAuthenticationAcquired = 6;
        static final int TRANSACTION_onAuthenticationCancel = 5;
        static final int TRANSACTION_onAuthenticationResult = 4;
        static final int TRANSACTION_onCallback = 8;
        static final int TRANSACTION_onCallbackResult = 9;
        static final int TRANSACTION_onEnrollAcquired = 3;
        static final int TRANSACTION_onEnrollCancel = 2;
        static final int TRANSACTION_onEnrollResult = 1;
        static final int TRANSACTION_onRemovedResult = 7;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IFaceRecognizeServiceReceiver asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IFaceRecognizeServiceReceiver)) {
                return new Proxy(obj);
            }
            return (IFaceRecognizeServiceReceiver) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        onEnrollResult(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        onEnrollCancel();
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        onEnrollAcquired(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        onAuthenticationResult(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        onAuthenticationCancel();
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        onAuthenticationAcquired(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        onRemovedResult(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        onCallback(data.readLong(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        onCallbackResult(data.readInt(), data.readInt(), data.readInt(), data.readInt());
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

        /* access modifiers changed from: private */
        public static class Proxy implements IFaceRecognizeServiceReceiver {
            public static IFaceRecognizeServiceReceiver sDefaultImpl;
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

            @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceReceiver
            public void onEnrollResult(int faceId, int userId, int errorCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(faceId);
                    _data.writeInt(userId);
                    _data.writeInt(errorCode);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onEnrollResult(faceId, userId, errorCode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceReceiver
            public void onEnrollCancel() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onEnrollCancel();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceReceiver
            public void onEnrollAcquired(int acquiredInfo, int process) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(acquiredInfo);
                    _data.writeInt(process);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onEnrollAcquired(acquiredInfo, process);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceReceiver
            public void onAuthenticationResult(int userId, int errorCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(errorCode);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onAuthenticationResult(userId, errorCode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceReceiver
            public void onAuthenticationCancel() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onAuthenticationCancel();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceReceiver
            public void onAuthenticationAcquired(int acquiredInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(acquiredInfo);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onAuthenticationAcquired(acquiredInfo);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceReceiver
            public void onRemovedResult(int userId, int errorCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeInt(errorCode);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onRemovedResult(userId, errorCode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceReceiver
            public void onCallback(long reqId, int type, int code, int errorCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(reqId);
                    _data.writeInt(type);
                    _data.writeInt(code);
                    _data.writeInt(errorCode);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onCallback(reqId, type, code, errorCode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.facerecognition.IFaceRecognizeServiceReceiver
            public void onCallbackResult(int reqId, int type, int code, int errorCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(reqId);
                    _data.writeInt(type);
                    _data.writeInt(code);
                    _data.writeInt(errorCode);
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onCallbackResult(reqId, type, code, errorCode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IFaceRecognizeServiceReceiver impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IFaceRecognizeServiceReceiver getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
