package com.huawei.remotepassword.auth;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.remotepassword.auth.IRemotePasswordInputer;

public interface IRemotePassword extends IInterface {
    int deinitCoAuth() throws RemoteException;

    int deregisterPassword(int i, byte[] bArr, byte[] bArr2) throws RemoteException;

    int getRemoteServerKey(int i, byte[] bArr, Bundle bundle) throws RemoteException;

    int initCoAuth() throws RemoteException;

    int isPasswordRegistered(int i, byte[] bArr) throws RemoteException;

    int onLocalSetProperty(byte[] bArr, byte[] bArr2) throws RemoteException;

    void postEdit(int i) throws RemoteException;

    long preEdit(int i) throws RemoteException;

    int registerPassword(int i, byte[] bArr, byte[] bArr2, Bundle bundle) throws RemoteException;

    void registerPasswordInputer(IRemotePasswordInputer iRemotePasswordInputer) throws RemoteException;

    void registerPasswordInputerFor(IRemotePasswordInputer iRemotePasswordInputer, String str) throws RemoteException;

    int verifyRemotePassword(int i, byte[] bArr, Bundle bundle, Bundle bundle2, Bundle bundle3) throws RemoteException;

    public static abstract class Stub extends Binder implements IRemotePassword {
        private static final String DESCRIPTOR = "com.huawei.remotepassword.auth.IRemotePassword";
        static final int TRANSACTION_deinitCoAuth = 6;
        static final int TRANSACTION_deregisterPassword = 4;
        static final int TRANSACTION_getRemoteServerKey = 8;
        static final int TRANSACTION_initCoAuth = 5;
        static final int TRANSACTION_isPasswordRegistered = 10;
        static final int TRANSACTION_onLocalSetProperty = 12;
        static final int TRANSACTION_postEdit = 2;
        static final int TRANSACTION_preEdit = 1;
        static final int TRANSACTION_registerPassword = 3;
        static final int TRANSACTION_registerPasswordInputer = 7;
        static final int TRANSACTION_registerPasswordInputerFor = 11;
        static final int TRANSACTION_verifyRemotePassword = 9;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IRemotePassword asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IRemotePassword)) {
                return new Proxy(obj);
            }
            return (IRemotePassword) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg3;
            Bundle _arg2;
            Bundle _arg32;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        long _result = preEdit(data.readInt());
                        reply.writeNoException();
                        reply.writeLong(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        postEdit(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg0 = data.readInt();
                        byte[] _arg1 = data.createByteArray();
                        byte[] _arg22 = data.createByteArray();
                        if (data.readInt() != 0) {
                            _arg3 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        int _result2 = registerPassword(_arg0, _arg1, _arg22, _arg3);
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = deregisterPassword(data.readInt(), data.createByteArray(), data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = initCoAuth();
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = deinitCoAuth();
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        registerPasswordInputer(IRemotePasswordInputer.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        byte[] _arg12 = data.createByteArray();
                        Bundle _arg23 = new Bundle();
                        int _result6 = getRemoteServerKey(_arg02, _arg12, _arg23);
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        reply.writeInt(1);
                        _arg23.writeToParcel(reply, 1);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        byte[] _arg13 = data.createByteArray();
                        if (data.readInt() != 0) {
                            _arg2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg32 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg32 = null;
                        }
                        Bundle _arg4 = new Bundle();
                        int _result7 = verifyRemotePassword(_arg03, _arg13, _arg2, _arg32, _arg4);
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        reply.writeInt(1);
                        _arg4.writeToParcel(reply, 1);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = isPasswordRegistered(data.readInt(), data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        registerPasswordInputerFor(IRemotePasswordInputer.Stub.asInterface(data.readStrongBinder()), data.readString());
                        reply.writeNoException();
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        int _result9 = onLocalSetProperty(data.createByteArray(), data.createByteArray());
                        reply.writeNoException();
                        reply.writeInt(_result9);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        private static class Proxy implements IRemotePassword {
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

            @Override // com.huawei.remotepassword.auth.IRemotePassword
            public long preEdit(int businessType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(businessType);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.remotepassword.auth.IRemotePassword
            public void postEdit(int businessType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(businessType);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.remotepassword.auth.IRemotePassword
            public int registerPassword(int businessType, byte[] userName, byte[] authtoken, Bundle params) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(businessType);
                    _data.writeByteArray(userName);
                    _data.writeByteArray(authtoken);
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.remotepassword.auth.IRemotePassword
            public int deregisterPassword(int businessType, byte[] userName, byte[] authtoken) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(businessType);
                    _data.writeByteArray(userName);
                    _data.writeByteArray(authtoken);
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.remotepassword.auth.IRemotePassword
            public int initCoAuth() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.remotepassword.auth.IRemotePassword
            public int deinitCoAuth() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.remotepassword.auth.IRemotePassword
            public void registerPasswordInputer(IRemotePasswordInputer inputer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(inputer != null ? inputer.asBinder() : null);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.remotepassword.auth.IRemotePassword
            public int getRemoteServerKey(int businessType, byte[] userName, Bundle response) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(businessType);
                    _data.writeByteArray(userName);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        response.readFromParcel(_reply);
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.remotepassword.auth.IRemotePassword
            public int verifyRemotePassword(int businessType, byte[] userName, Bundle info, Bundle params, Bundle dict) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(businessType);
                    _data.writeByteArray(userName);
                    if (info != null) {
                        _data.writeInt(1);
                        info.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    if (_reply.readInt() != 0) {
                        dict.readFromParcel(_reply);
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.remotepassword.auth.IRemotePassword
            public int isPasswordRegistered(int businessType, byte[] userName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(businessType);
                    _data.writeByteArray(userName);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.remotepassword.auth.IRemotePassword
            public void registerPasswordInputerFor(IRemotePasswordInputer inputer, String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(inputer != null ? inputer.asBinder() : null);
                    _data.writeString(pkgName);
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.remotepassword.auth.IRemotePassword
            public int onLocalSetProperty(byte[] property, byte[] value) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(property);
                    _data.writeByteArray(value);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }
    }
}
