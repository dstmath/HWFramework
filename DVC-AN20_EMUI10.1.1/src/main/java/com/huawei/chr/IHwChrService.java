package com.huawei.chr;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.huawei.chr.IHwChrPsCallBack;

public interface IHwChrService extends IInterface {
    void registerCallBack(String str, IHwChrPsCallBack iHwChrPsCallBack) throws RemoteException;

    void reportCallException(String str, int i, int i2, String str2, long j) throws RemoteException;

    void reportPsExceptionMsg(int i, Bundle bundle) throws RemoteException;

    void unRegisterCallBack(String str, IHwChrPsCallBack iHwChrPsCallBack) throws RemoteException;

    public static class Default implements IHwChrService {
        @Override // com.huawei.chr.IHwChrService
        public void reportCallException(String appName, int subId, int callType, String params, long timestamp) throws RemoteException {
        }

        @Override // com.huawei.chr.IHwChrService
        public void registerCallBack(String notifyType, IHwChrPsCallBack callback) throws RemoteException {
        }

        @Override // com.huawei.chr.IHwChrService
        public void unRegisterCallBack(String notifyType, IHwChrPsCallBack callback) throws RemoteException {
        }

        @Override // com.huawei.chr.IHwChrService
        public void reportPsExceptionMsg(int msgType, Bundle datas) throws RemoteException {
        }

        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwChrService {
        private static final String DESCRIPTOR = "com.huawei.chr.IHwChrService";
        static final int TRANSACTION_registerCallBack = 2;
        static final int TRANSACTION_reportCallException = 1;
        static final int TRANSACTION_reportPsExceptionMsg = 4;
        static final int TRANSACTION_unRegisterCallBack = 3;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwChrService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwChrService)) {
                return new Proxy(obj);
            }
            return (IHwChrService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg1;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                reportCallException(data.readString(), data.readInt(), data.readInt(), data.readString(), data.readLong());
                reply.writeNoException();
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                registerCallBack(data.readString(), IHwChrPsCallBack.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                unRegisterCallBack(data.readString(), IHwChrPsCallBack.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                int _arg0 = data.readInt();
                if (data.readInt() != 0) {
                    _arg1 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg1 = null;
                }
                reportPsExceptionMsg(_arg0, _arg1);
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
        public static class Proxy implements IHwChrService {
            public static IHwChrService sDefaultImpl;
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

            @Override // com.huawei.chr.IHwChrService
            public void reportCallException(String appName, int subId, int callType, String params, long timestamp) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(appName);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(subId);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(callType);
                        try {
                            _data.writeString(params);
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeLong(timestamp);
                            if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().reportCallException(appName, subId, callType, params, timestamp);
                            _reply.recycle();
                            _data.recycle();
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
                } catch (Throwable th6) {
                    th = th6;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.huawei.chr.IHwChrService
            public void registerCallBack(String notifyType, IHwChrPsCallBack callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(notifyType);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerCallBack(notifyType, callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.chr.IHwChrService
            public void unRegisterCallBack(String notifyType, IHwChrPsCallBack callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(notifyType);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unRegisterCallBack(notifyType, callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.chr.IHwChrService
            public void reportPsExceptionMsg(int msgType, Bundle datas) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(msgType);
                    if (datas != null) {
                        _data.writeInt(1);
                        datas.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().reportPsExceptionMsg(msgType, datas);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwChrService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwChrService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
