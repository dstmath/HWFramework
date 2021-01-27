package android.net.booster;

import android.net.booster.IHwCommBoosterCallback;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwCommBoosterService extends IInterface {
    Bundle getBoosterPara(String str, int i, Bundle bundle) throws RemoteException;

    int registerCallBack(String str, IHwCommBoosterCallback iHwCommBoosterCallback) throws RemoteException;

    int reportBoosterPara(String str, int i, Bundle bundle) throws RemoteException;

    int unRegisterCallBack(String str, IHwCommBoosterCallback iHwCommBoosterCallback) throws RemoteException;

    public static class Default implements IHwCommBoosterService {
        @Override // android.net.booster.IHwCommBoosterService
        public int registerCallBack(String pkgName, IHwCommBoosterCallback cb) throws RemoteException {
            return 0;
        }

        @Override // android.net.booster.IHwCommBoosterService
        public int unRegisterCallBack(String pkgName, IHwCommBoosterCallback cb) throws RemoteException {
            return 0;
        }

        @Override // android.net.booster.IHwCommBoosterService
        public int reportBoosterPara(String pkgName, int dataType, Bundle data) throws RemoteException {
            return 0;
        }

        @Override // android.net.booster.IHwCommBoosterService
        public Bundle getBoosterPara(String pkgName, int dataType, Bundle data) throws RemoteException {
            return null;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwCommBoosterService {
        private static final String DESCRIPTOR = "android.net.booster.IHwCommBoosterService";
        static final int TRANSACTION_getBoosterPara = 4;
        static final int TRANSACTION_registerCallBack = 1;
        static final int TRANSACTION_reportBoosterPara = 3;
        static final int TRANSACTION_unRegisterCallBack = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwCommBoosterService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwCommBoosterService)) {
                return new Proxy(obj);
            }
            return (IHwCommBoosterService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "registerCallBack";
            }
            if (transactionCode == 2) {
                return "unRegisterCallBack";
            }
            if (transactionCode == 3) {
                return "reportBoosterPara";
            }
            if (transactionCode != 4) {
                return null;
            }
            return "getBoosterPara";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg2;
            Bundle _arg22;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                int _result = registerCallBack(data.readString(), IHwCommBoosterCallback.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                reply.writeInt(_result);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                int _result2 = unRegisterCallBack(data.readString(), IHwCommBoosterCallback.Stub.asInterface(data.readStrongBinder()));
                reply.writeNoException();
                reply.writeInt(_result2);
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                String _arg0 = data.readString();
                int _arg1 = data.readInt();
                if (data.readInt() != 0) {
                    _arg2 = Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg2 = null;
                }
                int _result3 = reportBoosterPara(_arg0, _arg1, _arg2);
                reply.writeNoException();
                reply.writeInt(_result3);
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                String _arg02 = data.readString();
                int _arg12 = data.readInt();
                if (data.readInt() != 0) {
                    _arg22 = Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg22 = null;
                }
                Bundle _result4 = getBoosterPara(_arg02, _arg12, _arg22);
                reply.writeNoException();
                if (_result4 != null) {
                    reply.writeInt(1);
                    _result4.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHwCommBoosterService {
            public static IHwCommBoosterService sDefaultImpl;
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

            @Override // android.net.booster.IHwCommBoosterService
            public int registerCallBack(String pkgName, IHwCommBoosterCallback cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerCallBack(pkgName, cb);
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

            @Override // android.net.booster.IHwCommBoosterService
            public int unRegisterCallBack(String pkgName, IHwCommBoosterCallback cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unRegisterCallBack(pkgName, cb);
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

            @Override // android.net.booster.IHwCommBoosterService
            public int reportBoosterPara(String pkgName, int dataType, Bundle data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeInt(dataType);
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().reportBoosterPara(pkgName, dataType, data);
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

            @Override // android.net.booster.IHwCommBoosterService
            public Bundle getBoosterPara(String pkgName, int dataType, Bundle data) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeInt(dataType);
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getBoosterPara(pkgName, dataType, data);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Bundle.CREATOR.createFromParcel(_reply);
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
        }

        public static boolean setDefaultImpl(IHwCommBoosterService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwCommBoosterService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
