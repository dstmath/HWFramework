package com.huawei.security.deviceauth;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface ICallbackMethods extends IInterface {
    void onOperationFinished(String str, int i, int i2) throws RemoteException;

    void onOperationFinishedWithData(String str, int i, int i2, byte[] bArr) throws RemoteException;

    boolean onPassthroughDataGenerated(String str, byte[] bArr) throws RemoteException;

    ConfirmParams onReceiveRequest(String str, int i) throws RemoteException;

    void onSessionKeyReturned(String str, byte[] bArr) throws RemoteException;

    public static class Default implements ICallbackMethods {
        @Override // com.huawei.security.deviceauth.ICallbackMethods
        public void onOperationFinished(String sessionId, int operationCode, int result) throws RemoteException {
        }

        @Override // com.huawei.security.deviceauth.ICallbackMethods
        public void onOperationFinishedWithData(String sessionId, int operationCode, int result, byte[] returnData) throws RemoteException {
        }

        @Override // com.huawei.security.deviceauth.ICallbackMethods
        public boolean onPassthroughDataGenerated(String targetSessionId, byte[] passthroughData) throws RemoteException {
            return false;
        }

        @Override // com.huawei.security.deviceauth.ICallbackMethods
        public ConfirmParams onReceiveRequest(String sessionId, int operationCode) throws RemoteException {
            return null;
        }

        @Override // com.huawei.security.deviceauth.ICallbackMethods
        public void onSessionKeyReturned(String sessionId, byte[] sessionKey) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements ICallbackMethods {
        private static final String DESCRIPTOR = "com.huawei.security.deviceauth.ICallbackMethods";
        static final int TRANSACTION_onOperationFinished = 1;
        static final int TRANSACTION_onOperationFinishedWithData = 2;
        static final int TRANSACTION_onPassthroughDataGenerated = 3;
        static final int TRANSACTION_onReceiveRequest = 4;
        static final int TRANSACTION_onSessionKeyReturned = 5;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static ICallbackMethods asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof ICallbackMethods)) {
                return new Proxy(obj);
            }
            return (ICallbackMethods) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                onOperationFinished(data.readString(), data.readInt(), data.readInt());
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                onOperationFinishedWithData(data.readString(), data.readInt(), data.readInt(), data.createByteArray());
                reply.writeNoException();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                boolean onPassthroughDataGenerated = onPassthroughDataGenerated(data.readString(), data.createByteArray());
                reply.writeNoException();
                reply.writeInt(onPassthroughDataGenerated ? 1 : 0);
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                ConfirmParams _result = onReceiveRequest(data.readString(), data.readInt());
                reply.writeNoException();
                if (_result != null) {
                    reply.writeInt(1);
                    _result.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                onSessionKeyReturned(data.readString(), data.createByteArray());
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
        public static class Proxy implements ICallbackMethods {
            public static ICallbackMethods sDefaultImpl;
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

            @Override // com.huawei.security.deviceauth.ICallbackMethods
            public void onOperationFinished(String sessionId, int operationCode, int result) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(sessionId);
                    _data.writeInt(operationCode);
                    _data.writeInt(result);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onOperationFinished(sessionId, operationCode, result);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.ICallbackMethods
            public void onOperationFinishedWithData(String sessionId, int operationCode, int result, byte[] returnData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(sessionId);
                    _data.writeInt(operationCode);
                    _data.writeInt(result);
                    _data.writeByteArray(returnData);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onOperationFinishedWithData(sessionId, operationCode, result, returnData);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.ICallbackMethods
            public boolean onPassthroughDataGenerated(String targetSessionId, byte[] passthroughData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(targetSessionId);
                    _data.writeByteArray(passthroughData);
                    boolean _result = false;
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().onPassthroughDataGenerated(targetSessionId, passthroughData);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.deviceauth.ICallbackMethods
            public ConfirmParams onReceiveRequest(String sessionId, int operationCode) throws RemoteException {
                ConfirmParams _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(sessionId);
                    _data.writeInt(operationCode);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().onReceiveRequest(sessionId, operationCode);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ConfirmParams) ConfirmParams.CREATOR.createFromParcel(_reply);
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

            @Override // com.huawei.security.deviceauth.ICallbackMethods
            public void onSessionKeyReturned(String sessionId, byte[] sessionKey) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(sessionId);
                    _data.writeByteArray(sessionKey);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onSessionKeyReturned(sessionId, sessionKey);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(ICallbackMethods impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static ICallbackMethods getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
