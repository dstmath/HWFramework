package com.huawei.trustcircle;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.trustcircle.IEnhancedCircleListener;

public interface ITrustCircleService extends IInterface {
    Bundle call(Bundle bundle) throws RemoteException;

    void callAsync(Bundle bundle, IEnhancedCircleListener iEnhancedCircleListener) throws RemoteException;

    void createOrJoinCircle(Bundle bundle, IEnhancedCircleListener iEnhancedCircleListener) throws RemoteException;

    int getLockPatternStatus(String str) throws RemoteException;

    void remoteAuth(Bundle bundle, IEnhancedCircleListener iEnhancedCircleListener) throws RemoteException;

    void requestStatus(Bundle bundle, IEnhancedCircleListener iEnhancedCircleListener) throws RemoteException;

    Bundle secureDerive(int i, byte[] bArr, String str) throws RemoteException;

    Bundle unwrapData(Bundle bundle, String str) throws RemoteException;

    Bundle wrapData(byte[] bArr, String str) throws RemoteException;

    public static class Default implements ITrustCircleService {
        @Override // com.huawei.trustcircle.ITrustCircleService
        public void requestStatus(Bundle reqParams, IEnhancedCircleListener listener) throws RemoteException {
        }

        @Override // com.huawei.trustcircle.ITrustCircleService
        public void createOrJoinCircle(Bundle reqParams, IEnhancedCircleListener listener) throws RemoteException {
        }

        @Override // com.huawei.trustcircle.ITrustCircleService
        public Bundle wrapData(byte[] plainText, String packageName) throws RemoteException {
            return null;
        }

        @Override // com.huawei.trustcircle.ITrustCircleService
        public Bundle unwrapData(Bundle wrappedData, String packageName) throws RemoteException {
            return null;
        }

        @Override // com.huawei.trustcircle.ITrustCircleService
        public Bundle secureDerive(int keyType, byte[] deriveFactor, String packageName) throws RemoteException {
            return null;
        }

        @Override // com.huawei.trustcircle.ITrustCircleService
        public void remoteAuth(Bundle reqParams, IEnhancedCircleListener listener) throws RemoteException {
        }

        @Override // com.huawei.trustcircle.ITrustCircleService
        public int getLockPatternStatus(String accountId) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.trustcircle.ITrustCircleService
        public Bundle call(Bundle reqParams) throws RemoteException {
            return null;
        }

        @Override // com.huawei.trustcircle.ITrustCircleService
        public void callAsync(Bundle reqParams, IEnhancedCircleListener listener) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ITrustCircleService {
        private static final String DESCRIPTOR = "com.huawei.trustcircle.ITrustCircleService";
        static final int TRANSACTION_call = 8;
        static final int TRANSACTION_callAsync = 9;
        static final int TRANSACTION_createOrJoinCircle = 2;
        static final int TRANSACTION_getLockPatternStatus = 7;
        static final int TRANSACTION_remoteAuth = 6;
        static final int TRANSACTION_requestStatus = 1;
        static final int TRANSACTION_secureDerive = 5;
        static final int TRANSACTION_unwrapData = 4;
        static final int TRANSACTION_wrapData = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ITrustCircleService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ITrustCircleService)) {
                return new Proxy(obj);
            }
            return (ITrustCircleService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg0;
            Bundle _arg02;
            Bundle _arg03;
            Bundle _arg04;
            Bundle _arg05;
            Bundle _arg06;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        requestStatus(_arg0, IEnhancedCircleListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        createOrJoinCircle(_arg02, IEnhancedCircleListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        Bundle _result = wrapData(data.createByteArray(), data.readString());
                        reply.writeNoException();
                        if (_result != null) {
                            reply.writeInt(1);
                            _result.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        Bundle _result2 = unwrapData(_arg03, data.readString());
                        reply.writeNoException();
                        if (_result2 != null) {
                            reply.writeInt(1);
                            _result2.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        Bundle _result3 = secureDerive(data.readInt(), data.createByteArray(), data.readString());
                        reply.writeNoException();
                        if (_result3 != null) {
                            reply.writeInt(1);
                            _result3.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        remoteAuth(_arg04, IEnhancedCircleListener.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = getLockPatternStatus(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg05 = null;
                        }
                        Bundle _result5 = call(_arg05);
                        reply.writeNoException();
                        if (_result5 != null) {
                            reply.writeInt(1);
                            _result5.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg06 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg06 = null;
                        }
                        callAsync(_arg06, IEnhancedCircleListener.Stub.asInterface(data.readStrongBinder()));
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
        public static class Proxy implements ITrustCircleService {
            public static ITrustCircleService sDefaultImpl;
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

            @Override // com.huawei.trustcircle.ITrustCircleService
            public void requestStatus(Bundle reqParams, IEnhancedCircleListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (reqParams != null) {
                        _data.writeInt(1);
                        reqParams.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().requestStatus(reqParams, listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.trustcircle.ITrustCircleService
            public void createOrJoinCircle(Bundle reqParams, IEnhancedCircleListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (reqParams != null) {
                        _data.writeInt(1);
                        reqParams.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().createOrJoinCircle(reqParams, listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.trustcircle.ITrustCircleService
            public Bundle wrapData(byte[] plainText, String packageName) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(plainText);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().wrapData(plainText, packageName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.trustcircle.ITrustCircleService
            public Bundle unwrapData(Bundle wrappedData, String packageName) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (wrappedData != null) {
                        _data.writeInt(1);
                        wrappedData.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unwrapData(wrappedData, packageName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.trustcircle.ITrustCircleService
            public Bundle secureDerive(int keyType, byte[] deriveFactor, String packageName) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(keyType);
                    _data.writeByteArray(deriveFactor);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().secureDerive(keyType, deriveFactor, packageName);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.trustcircle.ITrustCircleService
            public void remoteAuth(Bundle reqParams, IEnhancedCircleListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (reqParams != null) {
                        _data.writeInt(1);
                        reqParams.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().remoteAuth(reqParams, listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.trustcircle.ITrustCircleService
            public int getLockPatternStatus(String accountId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(accountId);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLockPatternStatus(accountId);
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

            @Override // com.huawei.trustcircle.ITrustCircleService
            public Bundle call(Bundle reqParams) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (reqParams != null) {
                        _data.writeInt(1);
                        reqParams.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().call(reqParams);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.trustcircle.ITrustCircleService
            public void callAsync(Bundle reqParams, IEnhancedCircleListener listener) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (reqParams != null) {
                        _data.writeInt(1);
                        reqParams.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().callAsync(reqParams, listener);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ITrustCircleService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ITrustCircleService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
