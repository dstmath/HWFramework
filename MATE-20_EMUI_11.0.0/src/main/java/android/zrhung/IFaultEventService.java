package android.zrhung;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.zrhung.IFaultEventCallback;
import java.util.List;

public interface IFaultEventService extends IInterface {
    void callBack(String str, String str2, List<String> list) throws RemoteException;

    boolean registerCallback(String str, IFaultEventCallback iFaultEventCallback, int i) throws RemoteException;

    void unRegisterCallback(String str) throws RemoteException;

    public static class Default implements IFaultEventService {
        @Override // android.zrhung.IFaultEventService
        public boolean registerCallback(String packageName, IFaultEventCallback callBack, int flag) throws RemoteException {
            return false;
        }

        @Override // android.zrhung.IFaultEventService
        public void unRegisterCallback(String packageName) throws RemoteException {
        }

        @Override // android.zrhung.IFaultEventService
        public void callBack(String packageName, String tag, List<String> list) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IFaultEventService {
        private static final String DESCRIPTOR = "android.zrhung.IFaultEventService";
        static final int TRANSACTION_callBack = 3;
        static final int TRANSACTION_registerCallback = 1;
        static final int TRANSACTION_unRegisterCallback = 2;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IFaultEventService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IFaultEventService)) {
                return new Proxy(obj);
            }
            return (IFaultEventService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "registerCallback";
            }
            if (transactionCode == 2) {
                return "unRegisterCallback";
            }
            if (transactionCode != 3) {
                return null;
            }
            return "callBack";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                boolean registerCallback = registerCallback(data.readString(), IFaultEventCallback.Stub.asInterface(data.readStrongBinder()), data.readInt());
                reply.writeNoException();
                reply.writeInt(registerCallback ? 1 : 0);
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                unRegisterCallback(data.readString());
                reply.writeNoException();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                callBack(data.readString(), data.readString(), data.createStringArrayList());
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IFaultEventService {
            public static IFaultEventService sDefaultImpl;
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

            @Override // android.zrhung.IFaultEventService
            public boolean registerCallback(String packageName, IFaultEventCallback callBack, int flag) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeStrongBinder(callBack != null ? callBack.asBinder() : null);
                    _data.writeInt(flag);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerCallback(packageName, callBack, flag);
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

            @Override // android.zrhung.IFaultEventService
            public void unRegisterCallback(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unRegisterCallback(packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.zrhung.IFaultEventService
            public void callBack(String packageName, String tag, List<String> faultInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(tag);
                    _data.writeStringList(faultInfo);
                    if (this.mRemote.transact(3, _data, null, 1) || Stub.getDefaultImpl() == null) {
                        _data.recycle();
                    } else {
                        Stub.getDefaultImpl().callBack(packageName, tag, faultInfo);
                    }
                } finally {
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IFaultEventService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IFaultEventService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
