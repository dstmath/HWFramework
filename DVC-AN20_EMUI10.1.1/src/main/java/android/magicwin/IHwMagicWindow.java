package android.magicwin;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;
import java.util.Map;

public interface IHwMagicWindow extends IInterface {
    Map getHwMagicWinEnabledApps() throws RemoteException;

    void invokeAsync(String str, String str2, String str3, Bundle bundle, IBinder iBinder) throws RemoteException;

    Bundle invokeSync(String str, String str2, String str3, Bundle bundle) throws RemoteException;

    Bundle performHwMagicWindowPolicy(int i, List list) throws RemoteException;

    boolean setHwMagicWinEnabled(String str, boolean z) throws RemoteException;

    public static class Default implements IHwMagicWindow {
        @Override // android.magicwin.IHwMagicWindow
        public Bundle invokeSync(String packageName, String method, String params, Bundle objects) throws RemoteException {
            return null;
        }

        @Override // android.magicwin.IHwMagicWindow
        public void invokeAsync(String packageName, String method, String params, Bundle objects, IBinder callback) throws RemoteException {
        }

        @Override // android.magicwin.IHwMagicWindow
        public Bundle performHwMagicWindowPolicy(int policy, List params) throws RemoteException {
            return null;
        }

        @Override // android.magicwin.IHwMagicWindow
        public Map getHwMagicWinEnabledApps() throws RemoteException {
            return null;
        }

        @Override // android.magicwin.IHwMagicWindow
        public boolean setHwMagicWinEnabled(String pkg, boolean enabled) throws RemoteException {
            return false;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwMagicWindow {
        private static final String DESCRIPTOR = "android.magicwin.IHwMagicWindow";
        static final int TRANSACTION_getHwMagicWinEnabledApps = 4;
        static final int TRANSACTION_invokeAsync = 2;
        static final int TRANSACTION_invokeSync = 1;
        static final int TRANSACTION_performHwMagicWindowPolicy = 3;
        static final int TRANSACTION_setHwMagicWinEnabled = 5;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwMagicWindow asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwMagicWindow)) {
                return new Proxy(obj);
            }
            return (IHwMagicWindow) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            if (transactionCode == 1) {
                return "invokeSync";
            }
            if (transactionCode == 2) {
                return "invokeAsync";
            }
            if (transactionCode == 3) {
                return "performHwMagicWindowPolicy";
            }
            if (transactionCode == 4) {
                return "getHwMagicWinEnabledApps";
            }
            if (transactionCode != 5) {
                return null;
            }
            return "setHwMagicWinEnabled";
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg3;
            Bundle _arg32;
            boolean _arg1 = false;
            if (code == 1) {
                data.enforceInterface(DESCRIPTOR);
                String _arg0 = data.readString();
                String _arg12 = data.readString();
                String _arg2 = data.readString();
                if (data.readInt() != 0) {
                    _arg3 = Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg3 = null;
                }
                Bundle _result = invokeSync(_arg0, _arg12, _arg2, _arg3);
                reply.writeNoException();
                if (_result != null) {
                    reply.writeInt(1);
                    _result.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                return true;
            } else if (code == 2) {
                data.enforceInterface(DESCRIPTOR);
                String _arg02 = data.readString();
                String _arg13 = data.readString();
                String _arg22 = data.readString();
                if (data.readInt() != 0) {
                    _arg32 = Bundle.CREATOR.createFromParcel(data);
                } else {
                    _arg32 = null;
                }
                invokeAsync(_arg02, _arg13, _arg22, _arg32, data.readStrongBinder());
                reply.writeNoException();
                return true;
            } else if (code == 3) {
                data.enforceInterface(DESCRIPTOR);
                Bundle _result2 = performHwMagicWindowPolicy(data.readInt(), data.readArrayList(getClass().getClassLoader()));
                reply.writeNoException();
                if (_result2 != null) {
                    reply.writeInt(1);
                    _result2.writeToParcel(reply, 1);
                } else {
                    reply.writeInt(0);
                }
                return true;
            } else if (code == 4) {
                data.enforceInterface(DESCRIPTOR);
                Map _result3 = getHwMagicWinEnabledApps();
                reply.writeNoException();
                reply.writeMap(_result3);
                return true;
            } else if (code == 5) {
                data.enforceInterface(DESCRIPTOR);
                String _arg03 = data.readString();
                if (data.readInt() != 0) {
                    _arg1 = true;
                }
                boolean hwMagicWinEnabled = setHwMagicWinEnabled(_arg03, _arg1);
                reply.writeNoException();
                reply.writeInt(hwMagicWinEnabled ? 1 : 0);
                return true;
            } else if (code != 1598968902) {
                return super.onTransact(code, data, reply, flags);
            } else {
                reply.writeString(DESCRIPTOR);
                return true;
            }
        }

        /* access modifiers changed from: private */
        public static class Proxy implements IHwMagicWindow {
            public static IHwMagicWindow sDefaultImpl;
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

            @Override // android.magicwin.IHwMagicWindow
            public Bundle invokeSync(String packageName, String method, String params, Bundle objects) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(method);
                    _data.writeString(params);
                    if (objects != null) {
                        _data.writeInt(1);
                        objects.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().invokeSync(packageName, method, params, objects);
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

            @Override // android.magicwin.IHwMagicWindow
            public void invokeAsync(String packageName, String method, String params, Bundle objects, IBinder callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(method);
                    _data.writeString(params);
                    if (objects != null) {
                        _data.writeInt(1);
                        objects.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().invokeAsync(packageName, method, params, objects, callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.magicwin.IHwMagicWindow
            public Bundle performHwMagicWindowPolicy(int policy, List params) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(policy);
                    _data.writeList(params);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().performHwMagicWindowPolicy(policy, params);
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

            @Override // android.magicwin.IHwMagicWindow
            public Map getHwMagicWinEnabledApps() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHwMagicWinEnabledApps();
                    }
                    _reply.readException();
                    Map _result = _reply.readHashMap(getClass().getClassLoader());
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.magicwin.IHwMagicWindow
            public boolean setHwMagicWinEnabled(String pkg, boolean enabled) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    boolean _result = true;
                    _data.writeInt(enabled ? 1 : 0);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setHwMagicWinEnabled(pkg, enabled);
                    }
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
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

        public static boolean setDefaultImpl(IHwMagicWindow impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwMagicWindow getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
