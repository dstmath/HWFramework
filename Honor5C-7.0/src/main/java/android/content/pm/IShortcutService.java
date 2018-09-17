package android.content.pm;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import java.util.List;

public interface IShortcutService extends IInterface {

    public static abstract class Stub extends Binder implements IShortcutService {
        private static final String DESCRIPTOR = "android.content.pm.IShortcutService";
        static final int TRANSACTION_addDynamicShortcuts = 3;
        static final int TRANSACTION_applyRestore = 15;
        static final int TRANSACTION_getBackupPayload = 14;
        static final int TRANSACTION_getDynamicShortcuts = 2;
        static final int TRANSACTION_getIconMaxDimensions = 11;
        static final int TRANSACTION_getMaxDynamicShortcutCount = 8;
        static final int TRANSACTION_getPinnedShortcuts = 6;
        static final int TRANSACTION_getRateLimitResetTime = 10;
        static final int TRANSACTION_getRemainingCallCount = 9;
        static final int TRANSACTION_onApplicationActive = 13;
        static final int TRANSACTION_removeAllDynamicShortcuts = 5;
        static final int TRANSACTION_removeDynamicShortcuts = 4;
        static final int TRANSACTION_resetThrottling = 12;
        static final int TRANSACTION_setDynamicShortcuts = 1;
        static final int TRANSACTION_updateShortcuts = 7;

        private static class Proxy implements IShortcutService {
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

            public boolean setDynamicShortcuts(String packageName, ParceledListSlice shortcutInfoList, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (shortcutInfoList != null) {
                        _data.writeInt(Stub.TRANSACTION_setDynamicShortcuts);
                        shortcutInfoList.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_setDynamicShortcuts, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getDynamicShortcuts(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParceledListSlice parceledListSlice;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_getDynamicShortcuts, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        parceledListSlice = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        parceledListSlice = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return parceledListSlice;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean addDynamicShortcuts(String packageName, ParceledListSlice shortcutInfoList, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (shortcutInfoList != null) {
                        _data.writeInt(Stub.TRANSACTION_setDynamicShortcuts);
                        shortcutInfoList.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_addDynamicShortcuts, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeDynamicShortcuts(String packageName, List shortcutIds, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeList(shortcutIds);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_removeDynamicShortcuts, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removeAllDynamicShortcuts(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_removeAllDynamicShortcuts, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ParceledListSlice getPinnedShortcuts(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    ParceledListSlice parceledListSlice;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_getPinnedShortcuts, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        parceledListSlice = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(_reply);
                    } else {
                        parceledListSlice = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return parceledListSlice;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean updateShortcuts(String packageName, ParceledListSlice shortcuts, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (shortcuts != null) {
                        _data.writeInt(Stub.TRANSACTION_setDynamicShortcuts);
                        shortcuts.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_updateShortcuts, _data, _reply, 0);
                    _reply.readException();
                    boolean _result = _reply.readInt() != 0;
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getMaxDynamicShortcutCount(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_getMaxDynamicShortcutCount, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getRemainingCallCount(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_getRemainingCallCount, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getRateLimitResetTime(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_getRateLimitResetTime, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getIconMaxDimensions(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_getIconMaxDimensions, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void resetThrottling() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_resetThrottling, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void onApplicationActive(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(Stub.TRANSACTION_onApplicationActive, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public byte[] getBackupPayload(int user) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(user);
                    this.mRemote.transact(Stub.TRANSACTION_getBackupPayload, _data, _reply, 0);
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void applyRestore(byte[] payload, int user) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(payload);
                    _data.writeInt(user);
                    this.mRemote.transact(Stub.TRANSACTION_applyRestore, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IShortcutService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IShortcutService)) {
                return new Proxy(obj);
            }
            return (IShortcutService) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            String _arg0;
            ParceledListSlice parceledListSlice;
            boolean _result;
            ParceledListSlice _result2;
            int _result3;
            switch (code) {
                case TRANSACTION_setDynamicShortcuts /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        parceledListSlice = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(data);
                    } else {
                        parceledListSlice = null;
                    }
                    _result = setDynamicShortcuts(_arg0, parceledListSlice, data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_setDynamicShortcuts : 0);
                    return true;
                case TRANSACTION_getDynamicShortcuts /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getDynamicShortcuts(data.readString(), data.readInt());
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(TRANSACTION_setDynamicShortcuts);
                        _result2.writeToParcel(reply, TRANSACTION_setDynamicShortcuts);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_addDynamicShortcuts /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        parceledListSlice = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(data);
                    } else {
                        parceledListSlice = null;
                    }
                    _result = addDynamicShortcuts(_arg0, parceledListSlice, data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_setDynamicShortcuts : 0);
                    return true;
                case TRANSACTION_removeDynamicShortcuts /*4*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeDynamicShortcuts(data.readString(), data.readArrayList(getClass().getClassLoader()), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_removeAllDynamicShortcuts /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    removeAllDynamicShortcuts(data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getPinnedShortcuts /*6*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = getPinnedShortcuts(data.readString(), data.readInt());
                    reply.writeNoException();
                    if (_result2 != null) {
                        reply.writeInt(TRANSACTION_setDynamicShortcuts);
                        _result2.writeToParcel(reply, TRANSACTION_setDynamicShortcuts);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_updateShortcuts /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readString();
                    if (data.readInt() != 0) {
                        parceledListSlice = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(data);
                    } else {
                        parceledListSlice = null;
                    }
                    _result = updateShortcuts(_arg0, parceledListSlice, data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result ? TRANSACTION_setDynamicShortcuts : 0);
                    return true;
                case TRANSACTION_getMaxDynamicShortcutCount /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getMaxDynamicShortcutCount(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case TRANSACTION_getRemainingCallCount /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getRemainingCallCount(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case TRANSACTION_getRateLimitResetTime /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    long _result4 = getRateLimitResetTime(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeLong(_result4);
                    return true;
                case TRANSACTION_getIconMaxDimensions /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getIconMaxDimensions(data.readString(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case TRANSACTION_resetThrottling /*12*/:
                    data.enforceInterface(DESCRIPTOR);
                    resetThrottling();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_onApplicationActive /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    onApplicationActive(data.readString(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getBackupPayload /*14*/:
                    data.enforceInterface(DESCRIPTOR);
                    byte[] _result5 = getBackupPayload(data.readInt());
                    reply.writeNoException();
                    reply.writeByteArray(_result5);
                    return true;
                case TRANSACTION_applyRestore /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    applyRestore(data.createByteArray(), data.readInt());
                    reply.writeNoException();
                    return true;
                case IBinder.INTERFACE_TRANSACTION /*1598968902*/:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    boolean addDynamicShortcuts(String str, ParceledListSlice parceledListSlice, int i) throws RemoteException;

    void applyRestore(byte[] bArr, int i) throws RemoteException;

    byte[] getBackupPayload(int i) throws RemoteException;

    ParceledListSlice getDynamicShortcuts(String str, int i) throws RemoteException;

    int getIconMaxDimensions(String str, int i) throws RemoteException;

    int getMaxDynamicShortcutCount(String str, int i) throws RemoteException;

    ParceledListSlice getPinnedShortcuts(String str, int i) throws RemoteException;

    long getRateLimitResetTime(String str, int i) throws RemoteException;

    int getRemainingCallCount(String str, int i) throws RemoteException;

    void onApplicationActive(String str, int i) throws RemoteException;

    void removeAllDynamicShortcuts(String str, int i) throws RemoteException;

    void removeDynamicShortcuts(String str, List list, int i) throws RemoteException;

    void resetThrottling() throws RemoteException;

    boolean setDynamicShortcuts(String str, ParceledListSlice parceledListSlice, int i) throws RemoteException;

    boolean updateShortcuts(String str, ParceledListSlice parceledListSlice, int i) throws RemoteException;
}
