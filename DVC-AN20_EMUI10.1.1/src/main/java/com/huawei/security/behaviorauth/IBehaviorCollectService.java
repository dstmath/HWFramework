package com.huawei.security.behaviorauth;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IBehaviorCollectService extends IInterface {
    float getBotDetectResult(String str) throws RemoteException;

    int initBotDetect(String str) throws RemoteException;

    int releaseBotDetect(String str) throws RemoteException;

    public static class Default implements IBehaviorCollectService {
        @Override // com.huawei.security.behaviorauth.IBehaviorCollectService
        public int initBotDetect(String pkgName) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.security.behaviorauth.IBehaviorCollectService
        public float getBotDetectResult(String pkgName) throws RemoteException {
            return 0.0f;
        }

        @Override // com.huawei.security.behaviorauth.IBehaviorCollectService
        public int releaseBotDetect(String pkgName) throws RemoteException {
            return 0;
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IBehaviorCollectService {
        private static final String DESCRIPTOR = "com.huawei.security.behaviorauth.IBehaviorCollectService";
        static final int TRANSACTION_getBotDetectResult = 2;
        static final int TRANSACTION_initBotDetect = 1;
        static final int TRANSACTION_releaseBotDetect = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IBehaviorCollectService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IBehaviorCollectService)) {
                return new Proxy(obj);
            }
            return (IBehaviorCollectService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                int _result = initBotDetect(data.readString());
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                float _result2 = getBotDetectResult(data.readString());
                reply.writeNoException();
                reply.writeFloat(_result2);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                int _result3 = releaseBotDetect(data.readString());
                reply.writeNoException();
                reply.writeInt(_result3);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IBehaviorCollectService {
            public static IBehaviorCollectService sDefaultImpl;
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

            @Override // com.huawei.security.behaviorauth.IBehaviorCollectService
            public int initBotDetect(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().initBotDetect(pkgName);
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

            @Override // com.huawei.security.behaviorauth.IBehaviorCollectService
            public float getBotDetectResult(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getBotDetectResult(pkgName);
                    }
                    _reply.readException();
                    float _result = _reply.readFloat();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.security.behaviorauth.IBehaviorCollectService
            public int releaseBotDetect(String pkgName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().releaseBotDetect(pkgName);
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

        public static boolean setDefaultImpl(IBehaviorCollectService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IBehaviorCollectService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
