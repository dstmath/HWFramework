package android.content.pm;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.text.TextUtils;
import java.util.List;

public interface IShortcutService extends IInterface {
    boolean addDynamicShortcuts(String str, ParceledListSlice parceledListSlice, int i) throws RemoteException;

    void applyRestore(byte[] bArr, int i) throws RemoteException;

    Intent createShortcutResultIntent(String str, ShortcutInfo shortcutInfo, int i) throws RemoteException;

    void disableShortcuts(String str, List list, CharSequence charSequence, int i, int i2) throws RemoteException;

    void enableShortcuts(String str, List list, int i) throws RemoteException;

    byte[] getBackupPayload(int i) throws RemoteException;

    ParceledListSlice getDynamicShortcuts(String str, int i) throws RemoteException;

    int getIconMaxDimensions(String str, int i) throws RemoteException;

    ParceledListSlice getManifestShortcuts(String str, int i) throws RemoteException;

    int getMaxShortcutCountPerActivity(String str, int i) throws RemoteException;

    ParceledListSlice getPinnedShortcuts(String str, int i) throws RemoteException;

    long getRateLimitResetTime(String str, int i) throws RemoteException;

    int getRemainingCallCount(String str, int i) throws RemoteException;

    ParceledListSlice getShareTargets(String str, IntentFilter intentFilter, int i) throws RemoteException;

    boolean hasShareTargets(String str, String str2, int i) throws RemoteException;

    boolean isRequestPinItemSupported(int i, int i2) throws RemoteException;

    void onApplicationActive(String str, int i) throws RemoteException;

    void removeAllDynamicShortcuts(String str, int i) throws RemoteException;

    void removeDynamicShortcuts(String str, List list, int i) throws RemoteException;

    void reportShortcutUsed(String str, String str2, int i) throws RemoteException;

    boolean requestPinShortcut(String str, ShortcutInfo shortcutInfo, IntentSender intentSender, int i) throws RemoteException;

    void resetThrottling() throws RemoteException;

    void restoreShortcuts(int i) throws RemoteException;

    boolean setDynamicShortcuts(String str, ParceledListSlice parceledListSlice, int i) throws RemoteException;

    boolean updateShortcuts(String str, ParceledListSlice parceledListSlice, int i) throws RemoteException;

    public static class Default implements IShortcutService {
        @Override // android.content.pm.IShortcutService
        public boolean setDynamicShortcuts(String packageName, ParceledListSlice shortcutInfoList, int userId) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IShortcutService
        public ParceledListSlice getDynamicShortcuts(String packageName, int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IShortcutService
        public ParceledListSlice getManifestShortcuts(String packageName, int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IShortcutService
        public boolean addDynamicShortcuts(String packageName, ParceledListSlice shortcutInfoList, int userId) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IShortcutService
        public void removeDynamicShortcuts(String packageName, List shortcutIds, int userId) throws RemoteException {
        }

        @Override // android.content.pm.IShortcutService
        public void removeAllDynamicShortcuts(String packageName, int userId) throws RemoteException {
        }

        @Override // android.content.pm.IShortcutService
        public ParceledListSlice getPinnedShortcuts(String packageName, int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IShortcutService
        public boolean updateShortcuts(String packageName, ParceledListSlice shortcuts, int userId) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IShortcutService
        public boolean requestPinShortcut(String packageName, ShortcutInfo shortcut, IntentSender resultIntent, int userId) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IShortcutService
        public Intent createShortcutResultIntent(String packageName, ShortcutInfo shortcut, int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IShortcutService
        public void disableShortcuts(String packageName, List shortcutIds, CharSequence disabledMessage, int disabledMessageResId, int userId) throws RemoteException {
        }

        @Override // android.content.pm.IShortcutService
        public void enableShortcuts(String packageName, List shortcutIds, int userId) throws RemoteException {
        }

        @Override // android.content.pm.IShortcutService
        public int getMaxShortcutCountPerActivity(String packageName, int userId) throws RemoteException {
            return 0;
        }

        @Override // android.content.pm.IShortcutService
        public int getRemainingCallCount(String packageName, int userId) throws RemoteException {
            return 0;
        }

        @Override // android.content.pm.IShortcutService
        public long getRateLimitResetTime(String packageName, int userId) throws RemoteException {
            return 0;
        }

        @Override // android.content.pm.IShortcutService
        public int getIconMaxDimensions(String packageName, int userId) throws RemoteException {
            return 0;
        }

        @Override // android.content.pm.IShortcutService
        public void reportShortcutUsed(String packageName, String shortcutId, int userId) throws RemoteException {
        }

        @Override // android.content.pm.IShortcutService
        public void resetThrottling() throws RemoteException {
        }

        @Override // android.content.pm.IShortcutService
        public void onApplicationActive(String packageName, int userId) throws RemoteException {
        }

        @Override // android.content.pm.IShortcutService
        public byte[] getBackupPayload(int user) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IShortcutService
        public void applyRestore(byte[] payload, int user) throws RemoteException {
        }

        @Override // android.content.pm.IShortcutService
        public boolean isRequestPinItemSupported(int user, int requestType) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IShortcutService
        public ParceledListSlice getShareTargets(String packageName, IntentFilter filter, int userId) throws RemoteException {
            return null;
        }

        @Override // android.content.pm.IShortcutService
        public boolean hasShareTargets(String packageName, String packageToCheck, int userId) throws RemoteException {
            return false;
        }

        @Override // android.content.pm.IShortcutService
        public void restoreShortcuts(int userId) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IShortcutService {
        private static final String DESCRIPTOR = "android.content.pm.IShortcutService";
        static final int TRANSACTION_addDynamicShortcuts = 4;
        static final int TRANSACTION_applyRestore = 21;
        static final int TRANSACTION_createShortcutResultIntent = 10;
        static final int TRANSACTION_disableShortcuts = 11;
        static final int TRANSACTION_enableShortcuts = 12;
        static final int TRANSACTION_getBackupPayload = 20;
        static final int TRANSACTION_getDynamicShortcuts = 2;
        static final int TRANSACTION_getIconMaxDimensions = 16;
        static final int TRANSACTION_getManifestShortcuts = 3;
        static final int TRANSACTION_getMaxShortcutCountPerActivity = 13;
        static final int TRANSACTION_getPinnedShortcuts = 7;
        static final int TRANSACTION_getRateLimitResetTime = 15;
        static final int TRANSACTION_getRemainingCallCount = 14;
        static final int TRANSACTION_getShareTargets = 23;
        static final int TRANSACTION_hasShareTargets = 24;
        static final int TRANSACTION_isRequestPinItemSupported = 22;
        static final int TRANSACTION_onApplicationActive = 19;
        static final int TRANSACTION_removeAllDynamicShortcuts = 6;
        static final int TRANSACTION_removeDynamicShortcuts = 5;
        static final int TRANSACTION_reportShortcutUsed = 17;
        static final int TRANSACTION_requestPinShortcut = 9;
        static final int TRANSACTION_resetThrottling = 18;
        static final int TRANSACTION_restoreShortcuts = 25;
        static final int TRANSACTION_setDynamicShortcuts = 1;
        static final int TRANSACTION_updateShortcuts = 8;

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

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "setDynamicShortcuts";
                case 2:
                    return "getDynamicShortcuts";
                case 3:
                    return "getManifestShortcuts";
                case 4:
                    return "addDynamicShortcuts";
                case 5:
                    return "removeDynamicShortcuts";
                case 6:
                    return "removeAllDynamicShortcuts";
                case 7:
                    return "getPinnedShortcuts";
                case 8:
                    return "updateShortcuts";
                case 9:
                    return "requestPinShortcut";
                case 10:
                    return "createShortcutResultIntent";
                case 11:
                    return "disableShortcuts";
                case 12:
                    return "enableShortcuts";
                case 13:
                    return "getMaxShortcutCountPerActivity";
                case 14:
                    return "getRemainingCallCount";
                case 15:
                    return "getRateLimitResetTime";
                case 16:
                    return "getIconMaxDimensions";
                case 17:
                    return "reportShortcutUsed";
                case 18:
                    return "resetThrottling";
                case 19:
                    return "onApplicationActive";
                case 20:
                    return "getBackupPayload";
                case 21:
                    return "applyRestore";
                case 22:
                    return "isRequestPinItemSupported";
                case 23:
                    return "getShareTargets";
                case 24:
                    return "hasShareTargets";
                case 25:
                    return "restoreShortcuts";
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
            ParceledListSlice _arg1;
            ParceledListSlice _arg12;
            ParceledListSlice _arg13;
            ShortcutInfo _arg14;
            IntentSender _arg2;
            ShortcutInfo _arg15;
            CharSequence _arg22;
            IntentFilter _arg16;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg0 = data.readString();
                        if (data.readInt() != 0) {
                            _arg1 = ParceledListSlice.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        boolean dynamicShortcuts = setDynamicShortcuts(_arg0, _arg1, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(dynamicShortcuts ? 1 : 0);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result = getDynamicShortcuts(data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result != null) {
                            reply.writeInt(1);
                            _result.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result2 = getManifestShortcuts(data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result2 != null) {
                            reply.writeInt(1);
                            _result2.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg02 = data.readString();
                        if (data.readInt() != 0) {
                            _arg12 = ParceledListSlice.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        boolean addDynamicShortcuts = addDynamicShortcuts(_arg02, _arg12, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(addDynamicShortcuts ? 1 : 0);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        removeDynamicShortcuts(data.readString(), data.readArrayList(getClass().getClassLoader()), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        removeAllDynamicShortcuts(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        ParceledListSlice _result3 = getPinnedShortcuts(data.readString(), data.readInt());
                        reply.writeNoException();
                        if (_result3 != null) {
                            reply.writeInt(1);
                            _result3.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg03 = data.readString();
                        if (data.readInt() != 0) {
                            _arg13 = ParceledListSlice.CREATOR.createFromParcel(data);
                        } else {
                            _arg13 = null;
                        }
                        boolean updateShortcuts = updateShortcuts(_arg03, _arg13, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(updateShortcuts ? 1 : 0);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg04 = data.readString();
                        if (data.readInt() != 0) {
                            _arg14 = ShortcutInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg14 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg2 = IntentSender.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        boolean requestPinShortcut = requestPinShortcut(_arg04, _arg14, _arg2, data.readInt());
                        reply.writeNoException();
                        reply.writeInt(requestPinShortcut ? 1 : 0);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg05 = data.readString();
                        if (data.readInt() != 0) {
                            _arg15 = ShortcutInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg15 = null;
                        }
                        Intent _result4 = createShortcutResultIntent(_arg05, _arg15, data.readInt());
                        reply.writeNoException();
                        if (_result4 != null) {
                            reply.writeInt(1);
                            _result4.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg06 = data.readString();
                        List _arg17 = data.readArrayList(getClass().getClassLoader());
                        if (data.readInt() != 0) {
                            _arg22 = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(data);
                        } else {
                            _arg22 = null;
                        }
                        disableShortcuts(_arg06, _arg17, _arg22, data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        enableShortcuts(data.readString(), data.readArrayList(getClass().getClassLoader()), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = getMaxShortcutCountPerActivity(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        int _result6 = getRemainingCallCount(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        long _result7 = getRateLimitResetTime(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeLong(_result7);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = getIconMaxDimensions(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        reportShortcutUsed(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        resetThrottling();
                        reply.writeNoException();
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        onApplicationActive(data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        byte[] _result9 = getBackupPayload(data.readInt());
                        reply.writeNoException();
                        reply.writeByteArray(_result9);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        applyRestore(data.createByteArray(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isRequestPinItemSupported = isRequestPinItemSupported(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isRequestPinItemSupported ? 1 : 0);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg07 = data.readString();
                        if (data.readInt() != 0) {
                            _arg16 = IntentFilter.CREATOR.createFromParcel(data);
                        } else {
                            _arg16 = null;
                        }
                        ParceledListSlice _result10 = getShareTargets(_arg07, _arg16, data.readInt());
                        reply.writeNoException();
                        if (_result10 != null) {
                            reply.writeInt(1);
                            _result10.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        boolean hasShareTargets = hasShareTargets(data.readString(), data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(hasShareTargets ? 1 : 0);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        restoreShortcuts(data.readInt());
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
        public static class Proxy implements IShortcutService {
            public static IShortcutService sDefaultImpl;
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

            @Override // android.content.pm.IShortcutService
            public boolean setDynamicShortcuts(String packageName, ParceledListSlice shortcutInfoList, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = true;
                    if (shortcutInfoList != null) {
                        _data.writeInt(1);
                        shortcutInfoList.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().setDynamicShortcuts(packageName, shortcutInfoList, userId);
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

            @Override // android.content.pm.IShortcutService
            public ParceledListSlice getDynamicShortcuts(String packageName, int userId) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDynamicShortcuts(packageName, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParceledListSlice.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IShortcutService
            public ParceledListSlice getManifestShortcuts(String packageName, int userId) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getManifestShortcuts(packageName, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParceledListSlice.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IShortcutService
            public boolean addDynamicShortcuts(String packageName, ParceledListSlice shortcutInfoList, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = true;
                    if (shortcutInfoList != null) {
                        _data.writeInt(1);
                        shortcutInfoList.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().addDynamicShortcuts(packageName, shortcutInfoList, userId);
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

            @Override // android.content.pm.IShortcutService
            public void removeDynamicShortcuts(String packageName, List shortcutIds, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeList(shortcutIds);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeDynamicShortcuts(packageName, shortcutIds, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IShortcutService
            public void removeAllDynamicShortcuts(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeAllDynamicShortcuts(packageName, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IShortcutService
            public ParceledListSlice getPinnedShortcuts(String packageName, int userId) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPinnedShortcuts(packageName, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParceledListSlice.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IShortcutService
            public boolean updateShortcuts(String packageName, ParceledListSlice shortcuts, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = true;
                    if (shortcuts != null) {
                        _data.writeInt(1);
                        shortcuts.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().updateShortcuts(packageName, shortcuts, userId);
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

            @Override // android.content.pm.IShortcutService
            public boolean requestPinShortcut(String packageName, ShortcutInfo shortcut, IntentSender resultIntent, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    boolean _result = true;
                    if (shortcut != null) {
                        _data.writeInt(1);
                        shortcut.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (resultIntent != null) {
                        _data.writeInt(1);
                        resultIntent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().requestPinShortcut(packageName, shortcut, resultIntent, userId);
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

            @Override // android.content.pm.IShortcutService
            public Intent createShortcutResultIntent(String packageName, ShortcutInfo shortcut, int userId) throws RemoteException {
                Intent _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (shortcut != null) {
                        _data.writeInt(1);
                        shortcut.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().createShortcutResultIntent(packageName, shortcut, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = Intent.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IShortcutService
            public void disableShortcuts(String packageName, List shortcutIds, CharSequence disabledMessage, int disabledMessageResId, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeList(shortcutIds);
                    if (disabledMessage != null) {
                        _data.writeInt(1);
                        TextUtils.writeToParcel(disabledMessage, _data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(disabledMessageResId);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(11, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().disableShortcuts(packageName, shortcutIds, disabledMessage, disabledMessageResId, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IShortcutService
            public void enableShortcuts(String packageName, List shortcutIds, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeList(shortcutIds);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(12, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().enableShortcuts(packageName, shortcutIds, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IShortcutService
            public int getMaxShortcutCountPerActivity(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMaxShortcutCountPerActivity(packageName, userId);
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

            @Override // android.content.pm.IShortcutService
            public int getRemainingCallCount(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRemainingCallCount(packageName, userId);
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

            @Override // android.content.pm.IShortcutService
            public long getRateLimitResetTime(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRateLimitResetTime(packageName, userId);
                    }
                    _reply.readException();
                    long _result = _reply.readLong();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IShortcutService
            public int getIconMaxDimensions(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getIconMaxDimensions(packageName, userId);
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

            @Override // android.content.pm.IShortcutService
            public void reportShortcutUsed(String packageName, String shortcutId, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(shortcutId);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(17, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().reportShortcutUsed(packageName, shortcutId, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IShortcutService
            public void resetThrottling() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(18, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().resetThrottling();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IShortcutService
            public void onApplicationActive(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(19, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onApplicationActive(packageName, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IShortcutService
            public byte[] getBackupPayload(int user) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(user);
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getBackupPayload(user);
                    }
                    _reply.readException();
                    byte[] _result = _reply.createByteArray();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IShortcutService
            public void applyRestore(byte[] payload, int user) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeByteArray(payload);
                    _data.writeInt(user);
                    if (this.mRemote.transact(21, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().applyRestore(payload, user);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.content.pm.IShortcutService
            public boolean isRequestPinItemSupported(int user, int requestType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(user);
                    _data.writeInt(requestType);
                    boolean _result = false;
                    if (!this.mRemote.transact(22, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isRequestPinItemSupported(user, requestType);
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

            @Override // android.content.pm.IShortcutService
            public ParceledListSlice getShareTargets(String packageName, IntentFilter filter, int userId) throws RemoteException {
                ParceledListSlice _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    if (filter != null) {
                        _data.writeInt(1);
                        filter.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(23, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getShareTargets(packageName, filter, userId);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = ParceledListSlice.CREATOR.createFromParcel(_reply);
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

            @Override // android.content.pm.IShortcutService
            public boolean hasShareTargets(String packageName, String packageToCheck, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(packageToCheck);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(24, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().hasShareTargets(packageName, packageToCheck, userId);
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

            @Override // android.content.pm.IShortcutService
            public void restoreShortcuts(int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(25, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().restoreShortcuts(userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IShortcutService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IShortcutService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
