package huawei.android.security;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ILifeCycleCallback extends IInterface {
    void onFinalLoginResult(int i) throws RemoteException;

    void onFinalRegisterResult(int i) throws RemoteException;

    void onLoginResponse(int i, int i2, String str) throws RemoteException;

    void onLogoutResult(int i) throws RemoteException;

    void onRegisterResponse(int i, int i2, int i3, String str, String str2, String str3) throws RemoteException;

    void onUnregisterResult(int i) throws RemoteException;

    void onUpdateResponse(int i, int i2, String str) throws RemoteException;

    public static class Default implements ILifeCycleCallback {
        @Override // huawei.android.security.ILifeCycleCallback
        public void onRegisterResponse(int errorCode, int globalKeyID, int authKeyAlgoType, String regAuthKeyData, String regAuthKeyDataSign, String clientChallenge) throws RemoteException {
        }

        @Override // huawei.android.security.ILifeCycleCallback
        public void onFinalRegisterResult(int errorCode) throws RemoteException {
        }

        @Override // huawei.android.security.ILifeCycleCallback
        public void onLoginResponse(int errorCode, int indexVersion, String clientChallenge) throws RemoteException {
        }

        @Override // huawei.android.security.ILifeCycleCallback
        public void onUpdateResponse(int errorCode, int indexVersion, String clientChallenge) throws RemoteException {
        }

        @Override // huawei.android.security.ILifeCycleCallback
        public void onFinalLoginResult(int errorCode) throws RemoteException {
        }

        @Override // huawei.android.security.ILifeCycleCallback
        public void onLogoutResult(int errorCode) throws RemoteException {
        }

        @Override // huawei.android.security.ILifeCycleCallback
        public void onUnregisterResult(int errorCode) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ILifeCycleCallback {
        private static final String DESCRIPTOR = "huawei.android.security.ILifeCycleCallback";
        static final int TRANSACTION_onFinalLoginResult = 5;
        static final int TRANSACTION_onFinalRegisterResult = 2;
        static final int TRANSACTION_onLoginResponse = 3;
        static final int TRANSACTION_onLogoutResult = 6;
        static final int TRANSACTION_onRegisterResponse = 1;
        static final int TRANSACTION_onUnregisterResult = 7;
        static final int TRANSACTION_onUpdateResponse = 4;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ILifeCycleCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ILifeCycleCallback)) {
                return new Proxy(obj);
            }
            return (ILifeCycleCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        onRegisterResponse(data.readInt(), data.readInt(), data.readInt(), data.readString(), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        onFinalRegisterResult(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        onLoginResponse(data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        onUpdateResponse(data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        onFinalLoginResult(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        onLogoutResult(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        onUnregisterResult(data.readInt());
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
        public static class Proxy implements ILifeCycleCallback {
            public static ILifeCycleCallback sDefaultImpl;
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

            @Override // huawei.android.security.ILifeCycleCallback
            public void onRegisterResponse(int errorCode, int globalKeyID, int authKeyAlgoType, String regAuthKeyData, String regAuthKeyDataSign, String clientChallenge) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(errorCode);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(globalKeyID);
                        try {
                            _data.writeInt(authKeyAlgoType);
                            try {
                                _data.writeString(regAuthKeyData);
                            } catch (Throwable th3) {
                                th = th3;
                                _reply.recycle();
                                _data.recycle();
                                throw th;
                            }
                        } catch (Throwable th4) {
                            th = th4;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(regAuthKeyDataSign);
                        try {
                            _data.writeString(clientChallenge);
                            if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().onRegisterResponse(errorCode, globalKeyID, authKeyAlgoType, regAuthKeyData, regAuthKeyDataSign, clientChallenge);
                            _reply.recycle();
                            _data.recycle();
                        } catch (Throwable th6) {
                            th = th6;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th7) {
                        th = th7;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th8) {
                    th = th8;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // huawei.android.security.ILifeCycleCallback
            public void onFinalRegisterResult(int errorCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(errorCode);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onFinalRegisterResult(errorCode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.ILifeCycleCallback
            public void onLoginResponse(int errorCode, int indexVersion, String clientChallenge) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(errorCode);
                    _data.writeInt(indexVersion);
                    _data.writeString(clientChallenge);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onLoginResponse(errorCode, indexVersion, clientChallenge);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.ILifeCycleCallback
            public void onUpdateResponse(int errorCode, int indexVersion, String clientChallenge) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(errorCode);
                    _data.writeInt(indexVersion);
                    _data.writeString(clientChallenge);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onUpdateResponse(errorCode, indexVersion, clientChallenge);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.ILifeCycleCallback
            public void onFinalLoginResult(int errorCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(errorCode);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onFinalLoginResult(errorCode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.ILifeCycleCallback
            public void onLogoutResult(int errorCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(errorCode);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onLogoutResult(errorCode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // huawei.android.security.ILifeCycleCallback
            public void onUnregisterResult(int errorCode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(errorCode);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onUnregisterResult(errorCode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ILifeCycleCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ILifeCycleCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
