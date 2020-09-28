package android.app.role;

import android.app.role.IOnRoleHoldersChangedListener;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteCallback;
import android.os.RemoteException;
import android.telephony.IFinancialSmsCallback;
import java.util.List;

public interface IRoleManager extends IInterface {
    void addOnRoleHoldersChangedListenerAsUser(IOnRoleHoldersChangedListener iOnRoleHoldersChangedListener, int i) throws RemoteException;

    void addRoleHolderAsUser(String str, String str2, int i, int i2, RemoteCallback remoteCallback) throws RemoteException;

    boolean addRoleHolderFromController(String str, String str2) throws RemoteException;

    void clearRoleHoldersAsUser(String str, int i, int i2, RemoteCallback remoteCallback) throws RemoteException;

    String getDefaultSmsPackage(int i) throws RemoteException;

    List<String> getHeldRolesFromController(String str) throws RemoteException;

    List<String> getRoleHoldersAsUser(String str, int i) throws RemoteException;

    void getSmsMessagesForFinancialApp(String str, Bundle bundle, IFinancialSmsCallback iFinancialSmsCallback) throws RemoteException;

    boolean isRoleAvailable(String str) throws RemoteException;

    boolean isRoleHeld(String str, String str2) throws RemoteException;

    void removeOnRoleHoldersChangedListenerAsUser(IOnRoleHoldersChangedListener iOnRoleHoldersChangedListener, int i) throws RemoteException;

    void removeRoleHolderAsUser(String str, String str2, int i, int i2, RemoteCallback remoteCallback) throws RemoteException;

    boolean removeRoleHolderFromController(String str, String str2) throws RemoteException;

    void setRoleNamesFromController(List<String> list) throws RemoteException;

    public static class Default implements IRoleManager {
        @Override // android.app.role.IRoleManager
        public boolean isRoleAvailable(String roleName) throws RemoteException {
            return false;
        }

        @Override // android.app.role.IRoleManager
        public boolean isRoleHeld(String roleName, String packageName) throws RemoteException {
            return false;
        }

        @Override // android.app.role.IRoleManager
        public List<String> getRoleHoldersAsUser(String roleName, int userId) throws RemoteException {
            return null;
        }

        @Override // android.app.role.IRoleManager
        public void addRoleHolderAsUser(String roleName, String packageName, int flags, int userId, RemoteCallback callback) throws RemoteException {
        }

        @Override // android.app.role.IRoleManager
        public void removeRoleHolderAsUser(String roleName, String packageName, int flags, int userId, RemoteCallback callback) throws RemoteException {
        }

        @Override // android.app.role.IRoleManager
        public void clearRoleHoldersAsUser(String roleName, int flags, int userId, RemoteCallback callback) throws RemoteException {
        }

        @Override // android.app.role.IRoleManager
        public void addOnRoleHoldersChangedListenerAsUser(IOnRoleHoldersChangedListener listener, int userId) throws RemoteException {
        }

        @Override // android.app.role.IRoleManager
        public void removeOnRoleHoldersChangedListenerAsUser(IOnRoleHoldersChangedListener listener, int userId) throws RemoteException {
        }

        @Override // android.app.role.IRoleManager
        public void setRoleNamesFromController(List<String> list) throws RemoteException {
        }

        @Override // android.app.role.IRoleManager
        public boolean addRoleHolderFromController(String roleName, String packageName) throws RemoteException {
            return false;
        }

        @Override // android.app.role.IRoleManager
        public boolean removeRoleHolderFromController(String roleName, String packageName) throws RemoteException {
            return false;
        }

        @Override // android.app.role.IRoleManager
        public List<String> getHeldRolesFromController(String packageName) throws RemoteException {
            return null;
        }

        @Override // android.app.role.IRoleManager
        public String getDefaultSmsPackage(int userId) throws RemoteException {
            return null;
        }

        @Override // android.app.role.IRoleManager
        public void getSmsMessagesForFinancialApp(String callingPkg, Bundle params, IFinancialSmsCallback callback) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IRoleManager {
        private static final String DESCRIPTOR = "android.app.role.IRoleManager";
        static final int TRANSACTION_addOnRoleHoldersChangedListenerAsUser = 7;
        static final int TRANSACTION_addRoleHolderAsUser = 4;
        static final int TRANSACTION_addRoleHolderFromController = 10;
        static final int TRANSACTION_clearRoleHoldersAsUser = 6;
        static final int TRANSACTION_getDefaultSmsPackage = 13;
        static final int TRANSACTION_getHeldRolesFromController = 12;
        static final int TRANSACTION_getRoleHoldersAsUser = 3;
        static final int TRANSACTION_getSmsMessagesForFinancialApp = 14;
        static final int TRANSACTION_isRoleAvailable = 1;
        static final int TRANSACTION_isRoleHeld = 2;
        static final int TRANSACTION_removeOnRoleHoldersChangedListenerAsUser = 8;
        static final int TRANSACTION_removeRoleHolderAsUser = 5;
        static final int TRANSACTION_removeRoleHolderFromController = 11;
        static final int TRANSACTION_setRoleNamesFromController = 9;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IRoleManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IRoleManager)) {
                return new Proxy(obj);
            }
            return (IRoleManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "isRoleAvailable";
                case 2:
                    return "isRoleHeld";
                case 3:
                    return "getRoleHoldersAsUser";
                case 4:
                    return "addRoleHolderAsUser";
                case 5:
                    return "removeRoleHolderAsUser";
                case 6:
                    return "clearRoleHoldersAsUser";
                case 7:
                    return "addOnRoleHoldersChangedListenerAsUser";
                case 8:
                    return "removeOnRoleHoldersChangedListenerAsUser";
                case 9:
                    return "setRoleNamesFromController";
                case 10:
                    return "addRoleHolderFromController";
                case 11:
                    return "removeRoleHolderFromController";
                case 12:
                    return "getHeldRolesFromController";
                case 13:
                    return "getDefaultSmsPackage";
                case 14:
                    return "getSmsMessagesForFinancialApp";
                default:
                    return null;
            }
        }

        @Override // android.os.Binder
        public String getTransactionName(int transactionCode) {
            return getDefaultTransactionName(transactionCode);
        }

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            RemoteCallback _arg4;
            RemoteCallback _arg42;
            RemoteCallback _arg3;
            Bundle _arg1;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isRoleAvailable = isRoleAvailable(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isRoleAvailable ? 1 : 0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isRoleHeld = isRoleHeld(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(isRoleHeld ? 1 : 0);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result = getRoleHoldersAsUser(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeStringList(_result);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg0 = data.readString();
                        String _arg12 = data.readString();
                        int _arg2 = data.readInt();
                        int _arg32 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg4 = RemoteCallback.CREATOR.createFromParcel(data);
                        } else {
                            _arg4 = null;
                        }
                        addRoleHolderAsUser(_arg0, _arg12, _arg2, _arg32, _arg4);
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        String _arg13 = data.readString();
                        int _arg22 = data.readInt();
                        int _arg33 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg42 = RemoteCallback.CREATOR.createFromParcel(data);
                        } else {
                            _arg42 = null;
                        }
                        removeRoleHolderAsUser(_arg02, _arg13, _arg22, _arg33, _arg42);
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg03 = data.readString();
                        int _arg14 = data.readInt();
                        int _arg23 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg3 = RemoteCallback.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        clearRoleHoldersAsUser(_arg03, _arg14, _arg23, _arg3);
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        addOnRoleHoldersChangedListenerAsUser(IOnRoleHoldersChangedListener.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        removeOnRoleHoldersChangedListenerAsUser(IOnRoleHoldersChangedListener.Stub.asInterface(data.readStrongBinder()), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        setRoleNamesFromController(data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        boolean addRoleHolderFromController = addRoleHolderFromController(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(addRoleHolderFromController ? 1 : 0);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        boolean removeRoleHolderFromController = removeRoleHolderFromController(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(removeRoleHolderFromController ? 1 : 0);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result2 = getHeldRolesFromController(data.readString());
                        reply.writeNoException();
                        reply.writeStringList(_result2);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        String _result3 = getDefaultSmsPackage(data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result3);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg04 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        getSmsMessagesForFinancialApp(_arg04, _arg1, IFinancialSmsCallback.Stub.asInterface(data.readStrongBinder()));
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
        public static class Proxy implements IRoleManager {
            public static IRoleManager sDefaultImpl;
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

            @Override // android.app.role.IRoleManager
            public boolean isRoleAvailable(String roleName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(roleName);
                    boolean _result = false;
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isRoleAvailable(roleName);
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

            @Override // android.app.role.IRoleManager
            public boolean isRoleHeld(String roleName, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(roleName);
                    _data.writeString(packageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isRoleHeld(roleName, packageName);
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

            @Override // android.app.role.IRoleManager
            public List<String> getRoleHoldersAsUser(String roleName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(roleName);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRoleHoldersAsUser(roleName, userId);
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.role.IRoleManager
            public void addRoleHolderAsUser(String roleName, String packageName, int flags, int userId, RemoteCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(roleName);
                    _data.writeString(packageName);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    if (callback != null) {
                        _data.writeInt(1);
                        callback.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addRoleHolderAsUser(roleName, packageName, flags, userId, callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.role.IRoleManager
            public void removeRoleHolderAsUser(String roleName, String packageName, int flags, int userId, RemoteCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(roleName);
                    _data.writeString(packageName);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    if (callback != null) {
                        _data.writeInt(1);
                        callback.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeRoleHolderAsUser(roleName, packageName, flags, userId, callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.role.IRoleManager
            public void clearRoleHoldersAsUser(String roleName, int flags, int userId, RemoteCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(roleName);
                    _data.writeInt(flags);
                    _data.writeInt(userId);
                    if (callback != null) {
                        _data.writeInt(1);
                        callback.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().clearRoleHoldersAsUser(roleName, flags, userId, callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.role.IRoleManager
            public void addOnRoleHoldersChangedListenerAsUser(IOnRoleHoldersChangedListener listener, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addOnRoleHoldersChangedListenerAsUser(listener, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.role.IRoleManager
            public void removeOnRoleHoldersChangedListenerAsUser(IOnRoleHoldersChangedListener listener, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(listener != null ? listener.asBinder() : null);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeOnRoleHoldersChangedListenerAsUser(listener, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.role.IRoleManager
            public void setRoleNamesFromController(List<String> roleNames) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(roleNames);
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setRoleNamesFromController(roleNames);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.role.IRoleManager
            public boolean addRoleHolderFromController(String roleName, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(roleName);
                    _data.writeString(packageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().addRoleHolderFromController(roleName, packageName);
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

            @Override // android.app.role.IRoleManager
            public boolean removeRoleHolderFromController(String roleName, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(roleName);
                    _data.writeString(packageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().removeRoleHolderFromController(roleName, packageName);
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

            @Override // android.app.role.IRoleManager
            public List<String> getHeldRolesFromController(String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHeldRolesFromController(packageName);
                    }
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.role.IRoleManager
            public String getDefaultSmsPackage(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDefaultSmsPackage(userId);
                    }
                    _reply.readException();
                    String _result = _reply.readString();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.app.role.IRoleManager
            public void getSmsMessagesForFinancialApp(String callingPkg, Bundle params, IFinancialSmsCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(callingPkg);
                    if (params != null) {
                        _data.writeInt(1);
                        params.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(14, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().getSmsMessagesForFinancialApp(callingPkg, params, callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IRoleManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IRoleManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
