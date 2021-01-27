package com.android.internal.app;

import android.annotation.UnsupportedAppUsage;
import android.app.AppOpsManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteCallback;
import android.os.RemoteException;
import com.android.internal.app.IAppOpsActiveCallback;
import com.android.internal.app.IAppOpsCallback;
import com.android.internal.app.IAppOpsNotedCallback;
import java.util.List;

public interface IAppOpsService extends IInterface {
    void addHistoricalOps(AppOpsManager.HistoricalOps historicalOps) throws RemoteException;

    int checkAudioOperation(int i, int i2, int i3, String str) throws RemoteException;

    int checkOperation(int i, int i2, String str) throws RemoteException;

    int checkOperationRaw(int i, int i2, String str) throws RemoteException;

    int checkPackage(int i, String str) throws RemoteException;

    void clearHistory() throws RemoteException;

    @UnsupportedAppUsage
    void finishOperation(IBinder iBinder, int i, int i2, String str) throws RemoteException;

    void getHistoricalOps(int i, String str, List<String> list, long j, long j2, int i2, RemoteCallback remoteCallback) throws RemoteException;

    void getHistoricalOpsFromDiskRaw(int i, String str, List<String> list, long j, long j2, int i2, RemoteCallback remoteCallback) throws RemoteException;

    @UnsupportedAppUsage
    List<AppOpsManager.PackageOps> getOpsForPackage(int i, String str, int[] iArr) throws RemoteException;

    @UnsupportedAppUsage
    List<AppOpsManager.PackageOps> getPackagesForOps(int[] iArr) throws RemoteException;

    IBinder getToken(IBinder iBinder) throws RemoteException;

    List<AppOpsManager.PackageOps> getUidOps(int i, int[] iArr) throws RemoteException;

    boolean isOperationActive(int i, int i2, String str) throws RemoteException;

    int noteOperation(int i, int i2, String str) throws RemoteException;

    int noteProxyOperation(int i, int i2, String str, int i3, String str2) throws RemoteException;

    void offsetHistory(long j) throws RemoteException;

    int permissionToOpCode(String str) throws RemoteException;

    void reloadNonHistoricalState() throws RemoteException;

    void removeUser(int i) throws RemoteException;

    @UnsupportedAppUsage
    void resetAllModes(int i, String str) throws RemoteException;

    void resetHistoryParameters() throws RemoteException;

    void setAudioRestriction(int i, int i2, int i3, int i4, String[] strArr) throws RemoteException;

    void setHistoryParameters(int i, long j, int i2) throws RemoteException;

    @UnsupportedAppUsage
    void setMode(int i, int i2, String str, int i3) throws RemoteException;

    void setUidMode(int i, int i2, int i3) throws RemoteException;

    void setUserRestriction(int i, boolean z, IBinder iBinder, int i2, String[] strArr) throws RemoteException;

    void setUserRestrictions(Bundle bundle, IBinder iBinder, int i) throws RemoteException;

    int startOperation(IBinder iBinder, int i, int i2, String str, boolean z) throws RemoteException;

    void startWatchingActive(int[] iArr, IAppOpsActiveCallback iAppOpsActiveCallback) throws RemoteException;

    void startWatchingMode(int i, String str, IAppOpsCallback iAppOpsCallback) throws RemoteException;

    void startWatchingModeWithFlags(int i, String str, int i2, IAppOpsCallback iAppOpsCallback) throws RemoteException;

    void startWatchingNoted(int[] iArr, IAppOpsNotedCallback iAppOpsNotedCallback) throws RemoteException;

    void stopWatchingActive(IAppOpsActiveCallback iAppOpsActiveCallback) throws RemoteException;

    void stopWatchingMode(IAppOpsCallback iAppOpsCallback) throws RemoteException;

    void stopWatchingNoted(IAppOpsNotedCallback iAppOpsNotedCallback) throws RemoteException;

    public static class Default implements IAppOpsService {
        @Override // com.android.internal.app.IAppOpsService
        public int checkOperation(int code, int uid, String packageName) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.app.IAppOpsService
        public int noteOperation(int code, int uid, String packageName) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.app.IAppOpsService
        public int startOperation(IBinder token, int code, int uid, String packageName, boolean startIfModeDefault) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.app.IAppOpsService
        public void finishOperation(IBinder token, int code, int uid, String packageName) throws RemoteException {
        }

        @Override // com.android.internal.app.IAppOpsService
        public void startWatchingMode(int op, String packageName, IAppOpsCallback callback) throws RemoteException {
        }

        @Override // com.android.internal.app.IAppOpsService
        public void stopWatchingMode(IAppOpsCallback callback) throws RemoteException {
        }

        @Override // com.android.internal.app.IAppOpsService
        public IBinder getToken(IBinder clientToken) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.app.IAppOpsService
        public int permissionToOpCode(String permission) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.app.IAppOpsService
        public int checkAudioOperation(int code, int usage, int uid, String packageName) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.app.IAppOpsService
        public int noteProxyOperation(int code, int proxyUid, String proxyPackageName, int callingUid, String callingPackageName) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.app.IAppOpsService
        public int checkPackage(int uid, String packageName) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.app.IAppOpsService
        public List<AppOpsManager.PackageOps> getPackagesForOps(int[] ops) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.app.IAppOpsService
        public List<AppOpsManager.PackageOps> getOpsForPackage(int uid, String packageName, int[] ops) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.app.IAppOpsService
        public void getHistoricalOps(int uid, String packageName, List<String> list, long beginTimeMillis, long endTimeMillis, int flags, RemoteCallback callback) throws RemoteException {
        }

        @Override // com.android.internal.app.IAppOpsService
        public void getHistoricalOpsFromDiskRaw(int uid, String packageName, List<String> list, long beginTimeMillis, long endTimeMillis, int flags, RemoteCallback callback) throws RemoteException {
        }

        @Override // com.android.internal.app.IAppOpsService
        public void offsetHistory(long duration) throws RemoteException {
        }

        @Override // com.android.internal.app.IAppOpsService
        public void setHistoryParameters(int mode, long baseSnapshotInterval, int compressionStep) throws RemoteException {
        }

        @Override // com.android.internal.app.IAppOpsService
        public void addHistoricalOps(AppOpsManager.HistoricalOps ops) throws RemoteException {
        }

        @Override // com.android.internal.app.IAppOpsService
        public void resetHistoryParameters() throws RemoteException {
        }

        @Override // com.android.internal.app.IAppOpsService
        public void clearHistory() throws RemoteException {
        }

        @Override // com.android.internal.app.IAppOpsService
        public List<AppOpsManager.PackageOps> getUidOps(int uid, int[] ops) throws RemoteException {
            return null;
        }

        @Override // com.android.internal.app.IAppOpsService
        public void setUidMode(int code, int uid, int mode) throws RemoteException {
        }

        @Override // com.android.internal.app.IAppOpsService
        public void setMode(int code, int uid, String packageName, int mode) throws RemoteException {
        }

        @Override // com.android.internal.app.IAppOpsService
        public void resetAllModes(int reqUserId, String reqPackageName) throws RemoteException {
        }

        @Override // com.android.internal.app.IAppOpsService
        public void setAudioRestriction(int code, int usage, int uid, int mode, String[] exceptionPackages) throws RemoteException {
        }

        @Override // com.android.internal.app.IAppOpsService
        public void setUserRestrictions(Bundle restrictions, IBinder token, int userHandle) throws RemoteException {
        }

        @Override // com.android.internal.app.IAppOpsService
        public void setUserRestriction(int code, boolean restricted, IBinder token, int userHandle, String[] exceptionPackages) throws RemoteException {
        }

        @Override // com.android.internal.app.IAppOpsService
        public void removeUser(int userHandle) throws RemoteException {
        }

        @Override // com.android.internal.app.IAppOpsService
        public void startWatchingActive(int[] ops, IAppOpsActiveCallback callback) throws RemoteException {
        }

        @Override // com.android.internal.app.IAppOpsService
        public void stopWatchingActive(IAppOpsActiveCallback callback) throws RemoteException {
        }

        @Override // com.android.internal.app.IAppOpsService
        public boolean isOperationActive(int code, int uid, String packageName) throws RemoteException {
            return false;
        }

        @Override // com.android.internal.app.IAppOpsService
        public void startWatchingModeWithFlags(int op, String packageName, int flags, IAppOpsCallback callback) throws RemoteException {
        }

        @Override // com.android.internal.app.IAppOpsService
        public void startWatchingNoted(int[] ops, IAppOpsNotedCallback callback) throws RemoteException {
        }

        @Override // com.android.internal.app.IAppOpsService
        public void stopWatchingNoted(IAppOpsNotedCallback callback) throws RemoteException {
        }

        @Override // com.android.internal.app.IAppOpsService
        public int checkOperationRaw(int code, int uid, String packageName) throws RemoteException {
            return 0;
        }

        @Override // com.android.internal.app.IAppOpsService
        public void reloadNonHistoricalState() throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IAppOpsService {
        private static final String DESCRIPTOR = "com.android.internal.app.IAppOpsService";
        static final int TRANSACTION_addHistoricalOps = 18;
        static final int TRANSACTION_checkAudioOperation = 9;
        static final int TRANSACTION_checkOperation = 1;
        static final int TRANSACTION_checkOperationRaw = 35;
        static final int TRANSACTION_checkPackage = 11;
        static final int TRANSACTION_clearHistory = 20;
        static final int TRANSACTION_finishOperation = 4;
        static final int TRANSACTION_getHistoricalOps = 14;
        static final int TRANSACTION_getHistoricalOpsFromDiskRaw = 15;
        static final int TRANSACTION_getOpsForPackage = 13;
        static final int TRANSACTION_getPackagesForOps = 12;
        static final int TRANSACTION_getToken = 7;
        static final int TRANSACTION_getUidOps = 21;
        static final int TRANSACTION_isOperationActive = 31;
        static final int TRANSACTION_noteOperation = 2;
        static final int TRANSACTION_noteProxyOperation = 10;
        static final int TRANSACTION_offsetHistory = 16;
        static final int TRANSACTION_permissionToOpCode = 8;
        static final int TRANSACTION_reloadNonHistoricalState = 36;
        static final int TRANSACTION_removeUser = 28;
        static final int TRANSACTION_resetAllModes = 24;
        static final int TRANSACTION_resetHistoryParameters = 19;
        static final int TRANSACTION_setAudioRestriction = 25;
        static final int TRANSACTION_setHistoryParameters = 17;
        static final int TRANSACTION_setMode = 23;
        static final int TRANSACTION_setUidMode = 22;
        static final int TRANSACTION_setUserRestriction = 27;
        static final int TRANSACTION_setUserRestrictions = 26;
        static final int TRANSACTION_startOperation = 3;
        static final int TRANSACTION_startWatchingActive = 29;
        static final int TRANSACTION_startWatchingMode = 5;
        static final int TRANSACTION_startWatchingModeWithFlags = 32;
        static final int TRANSACTION_startWatchingNoted = 33;
        static final int TRANSACTION_stopWatchingActive = 30;
        static final int TRANSACTION_stopWatchingMode = 6;
        static final int TRANSACTION_stopWatchingNoted = 34;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IAppOpsService asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IAppOpsService)) {
                return new Proxy(obj);
            }
            return (IAppOpsService) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "checkOperation";
                case 2:
                    return "noteOperation";
                case 3:
                    return "startOperation";
                case 4:
                    return "finishOperation";
                case 5:
                    return "startWatchingMode";
                case 6:
                    return "stopWatchingMode";
                case 7:
                    return "getToken";
                case 8:
                    return "permissionToOpCode";
                case 9:
                    return "checkAudioOperation";
                case 10:
                    return "noteProxyOperation";
                case 11:
                    return "checkPackage";
                case 12:
                    return "getPackagesForOps";
                case 13:
                    return "getOpsForPackage";
                case 14:
                    return "getHistoricalOps";
                case 15:
                    return "getHistoricalOpsFromDiskRaw";
                case 16:
                    return "offsetHistory";
                case 17:
                    return "setHistoryParameters";
                case 18:
                    return "addHistoricalOps";
                case 19:
                    return "resetHistoryParameters";
                case 20:
                    return "clearHistory";
                case 21:
                    return "getUidOps";
                case 22:
                    return "setUidMode";
                case 23:
                    return "setMode";
                case 24:
                    return "resetAllModes";
                case 25:
                    return "setAudioRestriction";
                case 26:
                    return "setUserRestrictions";
                case 27:
                    return "setUserRestriction";
                case 28:
                    return "removeUser";
                case 29:
                    return "startWatchingActive";
                case 30:
                    return "stopWatchingActive";
                case 31:
                    return "isOperationActive";
                case 32:
                    return "startWatchingModeWithFlags";
                case 33:
                    return "startWatchingNoted";
                case 34:
                    return "stopWatchingNoted";
                case 35:
                    return "checkOperationRaw";
                case 36:
                    return "reloadNonHistoricalState";
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
            RemoteCallback _arg6;
            RemoteCallback _arg62;
            AppOpsManager.HistoricalOps _arg0;
            Bundle _arg02;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = checkOperation(data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = noteOperation(data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = startOperation(data.readStrongBinder(), data.readInt(), data.readInt(), data.readString(), data.readInt() != 0);
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        finishOperation(data.readStrongBinder(), data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        startWatchingMode(data.readInt(), data.readString(), IAppOpsCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        stopWatchingMode(IAppOpsCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _result4 = getToken(data.readStrongBinder());
                        reply.writeNoException();
                        reply.writeStrongBinder(_result4);
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = permissionToOpCode(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        int _result6 = checkAudioOperation(data.readInt(), data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _result7 = noteProxyOperation(data.readInt(), data.readInt(), data.readString(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = checkPackage(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        List<AppOpsManager.PackageOps> _result9 = getPackagesForOps(data.createIntArray());
                        reply.writeNoException();
                        reply.writeTypedList(_result9);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        List<AppOpsManager.PackageOps> _result10 = getOpsForPackage(data.readInt(), data.readString(), data.createIntArray());
                        reply.writeNoException();
                        reply.writeTypedList(_result10);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        String _arg1 = data.readString();
                        List<String> _arg2 = data.createStringArrayList();
                        long _arg3 = data.readLong();
                        long _arg4 = data.readLong();
                        int _arg5 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg6 = RemoteCallback.CREATOR.createFromParcel(data);
                        } else {
                            _arg6 = null;
                        }
                        getHistoricalOps(_arg03, _arg1, _arg2, _arg3, _arg4, _arg5, _arg6);
                        reply.writeNoException();
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg04 = data.readInt();
                        String _arg12 = data.readString();
                        List<String> _arg22 = data.createStringArrayList();
                        long _arg32 = data.readLong();
                        long _arg42 = data.readLong();
                        int _arg52 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg62 = RemoteCallback.CREATOR.createFromParcel(data);
                        } else {
                            _arg62 = null;
                        }
                        getHistoricalOpsFromDiskRaw(_arg04, _arg12, _arg22, _arg32, _arg42, _arg52, _arg62);
                        reply.writeNoException();
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        offsetHistory(data.readLong());
                        reply.writeNoException();
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        setHistoryParameters(data.readInt(), data.readLong(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = AppOpsManager.HistoricalOps.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        addHistoricalOps(_arg0);
                        reply.writeNoException();
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        resetHistoryParameters();
                        reply.writeNoException();
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        clearHistory();
                        reply.writeNoException();
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        List<AppOpsManager.PackageOps> _result11 = getUidOps(data.readInt(), data.createIntArray());
                        reply.writeNoException();
                        reply.writeTypedList(_result11);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        setUidMode(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        setMode(data.readInt(), data.readInt(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        resetAllModes(data.readInt(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        setAudioRestriction(data.readInt(), data.readInt(), data.readInt(), data.readInt(), data.createStringArray());
                        reply.writeNoException();
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        setUserRestrictions(_arg02, data.readStrongBinder(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        setUserRestriction(data.readInt(), data.readInt() != 0, data.readStrongBinder(), data.readInt(), data.createStringArray());
                        reply.writeNoException();
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        removeUser(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        startWatchingActive(data.createIntArray(), IAppOpsActiveCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        stopWatchingActive(IAppOpsActiveCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isOperationActive = isOperationActive(data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(isOperationActive ? 1 : 0);
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        startWatchingModeWithFlags(data.readInt(), data.readString(), data.readInt(), IAppOpsCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        startWatchingNoted(data.createIntArray(), IAppOpsNotedCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        stopWatchingNoted(IAppOpsNotedCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        int _result12 = checkOperationRaw(data.readInt(), data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result12);
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        reloadNonHistoricalState();
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
        public static class Proxy implements IAppOpsService {
            public static IAppOpsService sDefaultImpl;
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

            @Override // com.android.internal.app.IAppOpsService
            public int checkOperation(int code, int uid, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(code);
                    _data.writeInt(uid);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().checkOperation(code, uid, packageName);
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

            @Override // com.android.internal.app.IAppOpsService
            public int noteOperation(int code, int uid, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(code);
                    _data.writeInt(uid);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().noteOperation(code, uid, packageName);
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

            @Override // com.android.internal.app.IAppOpsService
            public int startOperation(IBinder token, int code, int uid, String packageName, boolean startIfModeDefault) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(code);
                    _data.writeInt(uid);
                    _data.writeString(packageName);
                    _data.writeInt(startIfModeDefault ? 1 : 0);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().startOperation(token, code, uid, packageName, startIfModeDefault);
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

            @Override // com.android.internal.app.IAppOpsService
            public void finishOperation(IBinder token, int code, int uid, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    _data.writeInt(code);
                    _data.writeInt(uid);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().finishOperation(token, code, uid, packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IAppOpsService
            public void startWatchingMode(int op, String packageName, IAppOpsCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(op);
                    _data.writeString(packageName);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().startWatchingMode(op, packageName, callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IAppOpsService
            public void stopWatchingMode(IAppOpsCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().stopWatchingMode(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IAppOpsService
            public IBinder getToken(IBinder clientToken) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(clientToken);
                    if (!this.mRemote.transact(7, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getToken(clientToken);
                    }
                    _reply.readException();
                    IBinder _result = _reply.readStrongBinder();
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IAppOpsService
            public int permissionToOpCode(String permission) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(permission);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().permissionToOpCode(permission);
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

            @Override // com.android.internal.app.IAppOpsService
            public int checkAudioOperation(int code, int usage, int uid, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(code);
                    _data.writeInt(usage);
                    _data.writeInt(uid);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().checkAudioOperation(code, usage, uid, packageName);
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

            @Override // com.android.internal.app.IAppOpsService
            public int noteProxyOperation(int code, int proxyUid, String proxyPackageName, int callingUid, String callingPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(code);
                    _data.writeInt(proxyUid);
                    _data.writeString(proxyPackageName);
                    _data.writeInt(callingUid);
                    _data.writeString(callingPackageName);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().noteProxyOperation(code, proxyUid, proxyPackageName, callingUid, callingPackageName);
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

            @Override // com.android.internal.app.IAppOpsService
            public int checkPackage(int uid, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().checkPackage(uid, packageName);
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

            @Override // com.android.internal.app.IAppOpsService
            public List<AppOpsManager.PackageOps> getPackagesForOps(int[] ops) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(ops);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPackagesForOps(ops);
                    }
                    _reply.readException();
                    List<AppOpsManager.PackageOps> _result = _reply.createTypedArrayList(AppOpsManager.PackageOps.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IAppOpsService
            public List<AppOpsManager.PackageOps> getOpsForPackage(int uid, String packageName, int[] ops) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeString(packageName);
                    _data.writeIntArray(ops);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getOpsForPackage(uid, packageName, ops);
                    }
                    _reply.readException();
                    List<AppOpsManager.PackageOps> _result = _reply.createTypedArrayList(AppOpsManager.PackageOps.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IAppOpsService
            public void getHistoricalOps(int uid, String packageName, List<String> ops, long beginTimeMillis, long endTimeMillis, int flags, RemoteCallback callback) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(uid);
                        try {
                            _data.writeString(packageName);
                            _data.writeStringList(ops);
                            _data.writeLong(beginTimeMillis);
                            _data.writeLong(endTimeMillis);
                            _data.writeInt(flags);
                            if (callback != null) {
                                _data.writeInt(1);
                                callback.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            if (this.mRemote.transact(14, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().getHistoricalOps(uid, packageName, ops, beginTimeMillis, endTimeMillis, flags, callback);
                            _reply.recycle();
                            _data.recycle();
                        } catch (Throwable th2) {
                            th = th2;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.android.internal.app.IAppOpsService
            public void getHistoricalOpsFromDiskRaw(int uid, String packageName, List<String> ops, long beginTimeMillis, long endTimeMillis, int flags, RemoteCallback callback) throws RemoteException {
                Throwable th;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(uid);
                        try {
                            _data.writeString(packageName);
                            _data.writeStringList(ops);
                            _data.writeLong(beginTimeMillis);
                            _data.writeLong(endTimeMillis);
                            _data.writeInt(flags);
                            if (callback != null) {
                                _data.writeInt(1);
                                callback.writeToParcel(_data, 0);
                            } else {
                                _data.writeInt(0);
                            }
                            if (this.mRemote.transact(15, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().getHistoricalOpsFromDiskRaw(uid, packageName, ops, beginTimeMillis, endTimeMillis, flags, callback);
                            _reply.recycle();
                            _data.recycle();
                        } catch (Throwable th2) {
                            th = th2;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                } catch (Throwable th4) {
                    th = th4;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.android.internal.app.IAppOpsService
            public void offsetHistory(long duration) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeLong(duration);
                    if (this.mRemote.transact(16, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().offsetHistory(duration);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IAppOpsService
            public void setHistoryParameters(int mode, long baseSnapshotInterval, int compressionStep) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(mode);
                    _data.writeLong(baseSnapshotInterval);
                    _data.writeInt(compressionStep);
                    if (this.mRemote.transact(17, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setHistoryParameters(mode, baseSnapshotInterval, compressionStep);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IAppOpsService
            public void addHistoricalOps(AppOpsManager.HistoricalOps ops) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (ops != null) {
                        _data.writeInt(1);
                        ops.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(18, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addHistoricalOps(ops);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IAppOpsService
            public void resetHistoryParameters() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(19, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().resetHistoryParameters();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IAppOpsService
            public void clearHistory() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(20, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().clearHistory();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IAppOpsService
            public List<AppOpsManager.PackageOps> getUidOps(int uid, int[] ops) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeIntArray(ops);
                    if (!this.mRemote.transact(21, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getUidOps(uid, ops);
                    }
                    _reply.readException();
                    List<AppOpsManager.PackageOps> _result = _reply.createTypedArrayList(AppOpsManager.PackageOps.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IAppOpsService
            public void setUidMode(int code, int uid, int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(code);
                    _data.writeInt(uid);
                    _data.writeInt(mode);
                    if (this.mRemote.transact(22, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setUidMode(code, uid, mode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IAppOpsService
            public void setMode(int code, int uid, String packageName, int mode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(code);
                    _data.writeInt(uid);
                    _data.writeString(packageName);
                    _data.writeInt(mode);
                    if (this.mRemote.transact(23, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setMode(code, uid, packageName, mode);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IAppOpsService
            public void resetAllModes(int reqUserId, String reqPackageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(reqUserId);
                    _data.writeString(reqPackageName);
                    if (this.mRemote.transact(24, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().resetAllModes(reqUserId, reqPackageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IAppOpsService
            public void setAudioRestriction(int code, int usage, int uid, int mode, String[] exceptionPackages) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(code);
                    _data.writeInt(usage);
                    _data.writeInt(uid);
                    _data.writeInt(mode);
                    _data.writeStringArray(exceptionPackages);
                    if (this.mRemote.transact(25, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAudioRestriction(code, usage, uid, mode, exceptionPackages);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IAppOpsService
            public void setUserRestrictions(Bundle restrictions, IBinder token, int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (restrictions != null) {
                        _data.writeInt(1);
                        restrictions.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(token);
                    _data.writeInt(userHandle);
                    if (this.mRemote.transact(26, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setUserRestrictions(restrictions, token, userHandle);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IAppOpsService
            public void setUserRestriction(int code, boolean restricted, IBinder token, int userHandle, String[] exceptionPackages) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(code);
                    _data.writeInt(restricted ? 1 : 0);
                    _data.writeStrongBinder(token);
                    _data.writeInt(userHandle);
                    _data.writeStringArray(exceptionPackages);
                    if (this.mRemote.transact(27, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setUserRestriction(code, restricted, token, userHandle, exceptionPackages);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IAppOpsService
            public void removeUser(int userHandle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userHandle);
                    if (this.mRemote.transact(28, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removeUser(userHandle);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IAppOpsService
            public void startWatchingActive(int[] ops, IAppOpsActiveCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(ops);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(29, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().startWatchingActive(ops, callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IAppOpsService
            public void stopWatchingActive(IAppOpsActiveCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(30, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().stopWatchingActive(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IAppOpsService
            public boolean isOperationActive(int code, int uid, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(code);
                    _data.writeInt(uid);
                    _data.writeString(packageName);
                    boolean _result = false;
                    if (!this.mRemote.transact(31, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isOperationActive(code, uid, packageName);
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

            @Override // com.android.internal.app.IAppOpsService
            public void startWatchingModeWithFlags(int op, String packageName, int flags, IAppOpsCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(op);
                    _data.writeString(packageName);
                    _data.writeInt(flags);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(32, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().startWatchingModeWithFlags(op, packageName, flags, callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IAppOpsService
            public void startWatchingNoted(int[] ops, IAppOpsNotedCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(ops);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(33, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().startWatchingNoted(ops, callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IAppOpsService
            public void stopWatchingNoted(IAppOpsNotedCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(34, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().stopWatchingNoted(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.android.internal.app.IAppOpsService
            public int checkOperationRaw(int code, int uid, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(code);
                    _data.writeInt(uid);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(35, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().checkOperationRaw(code, uid, packageName);
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

            @Override // com.android.internal.app.IAppOpsService
            public void reloadNonHistoricalState() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(36, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().reloadNonHistoricalState();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IAppOpsService impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IAppOpsService getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
