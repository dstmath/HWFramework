package com.huawei.android.app;

import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.huawei.android.app.IHwDAMonitorCallback;
import java.util.List;
import java.util.Map;

public interface IHwActivityManager extends IInterface {
    boolean canPickColor(String str) throws RemoteException;

    boolean cleanPackageRes(List<String> list, Map map, int i, boolean z, boolean z2, boolean z3) throws RemoteException;

    boolean cleanProcessResourceFast(String str, int i, IBinder iBinder, boolean z, boolean z2) throws RemoteException;

    void forceStopPackages(List<String> list, int i) throws RemoteException;

    List<String> getPidWithUiFromUid(int i) throws RemoteException;

    boolean getProcessRecordFromMTM(ProcessInfo processInfo) throws RemoteException;

    boolean handleANRFilterFIFO(int i, int i2) throws RemoteException;

    void handleShowAppEyeAnrUi(int i, int i2, String str, String str2) throws RemoteException;

    boolean isProcessExistLocked(String str, int i) throws RemoteException;

    boolean killNativeProcessRecordFast(String str, int i, int i2, boolean z, boolean z2, String str2) throws RemoteException;

    boolean killProcessRecordFast(String str, int i, int i2, boolean z, boolean z2, String str2, boolean z3) throws RemoteException;

    boolean killProcessRecordFromIAware(ProcessInfo processInfo, boolean z, boolean z2, String str, boolean z3) throws RemoteException;

    boolean killProcessRecordFromIAwareNative(ProcessInfo processInfo, boolean z, boolean z2, String str) throws RemoteException;

    boolean killProcessRecordFromMTM(ProcessInfo processInfo, boolean z, String str) throws RemoteException;

    int preloadAppForLauncher(String str, int i, int i2) throws RemoteException;

    int preloadApplication(String str, int i) throws RemoteException;

    void registerDAMonitorCallback(IHwDAMonitorCallback iHwDAMonitorCallback) throws RemoteException;

    void registerServiceHooker(IBinder iBinder, Intent intent) throws RemoteException;

    void removePackageAlarm(String str, List<String> list, int i) throws RemoteException;

    void reportAssocDisable() throws RemoteException;

    void reportProcessDied(int i) throws RemoteException;

    void reportScreenRecord(int i, int i2, int i3) throws RemoteException;

    void requestProcessGroupChange(int i, int i2, int i3, int i4) throws RemoteException;

    void setAndRestoreMaxAdjIfNeed(List<String> list) throws RemoteException;

    void setCpusetSwitch(boolean z, int i) throws RemoteException;

    void setProcessRecForPid(int i) throws RemoteException;

    void unregisterServiceHooker(IBinder iBinder) throws RemoteException;

    public static class Default implements IHwActivityManager {
        @Override // com.huawei.android.app.IHwActivityManager
        public void registerDAMonitorCallback(IHwDAMonitorCallback callback) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityManager
        public void setCpusetSwitch(boolean enable, int subSwitch) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityManager
        public boolean cleanPackageRes(List<String> list, Map alarmTags, int targetUid, boolean cleanAlarm, boolean isNative, boolean hasPerceptAlarm) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityManager
        public void reportScreenRecord(int uid, int pid, int status) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityManager
        public boolean handleANRFilterFIFO(int uid, int cmd) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityManager
        public void handleShowAppEyeAnrUi(int pid, int uid, String processName, String packageName) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityManager
        public void setProcessRecForPid(int pid) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityManager
        public List<String> getPidWithUiFromUid(int uid) throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.app.IHwActivityManager
        public int preloadApplication(String packageName, int userId) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.app.IHwActivityManager
        public int preloadAppForLauncher(String packageName, int userId, int preloadType) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.app.IHwActivityManager
        public boolean killProcessRecordFromIAware(ProcessInfo procInfo, boolean restartservice, boolean isAsynchronous, String reason, boolean checkAdj) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityManager
        public boolean killProcessRecordFromIAwareNative(ProcessInfo procInfo, boolean restartservice, boolean isAsynchronous, String reason) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityManager
        public boolean killProcessRecordFromMTM(ProcessInfo procInfo, boolean restartservice, String reason) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityManager
        public boolean isProcessExistLocked(String processName, int uid) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityManager
        public void removePackageAlarm(String pkg, List<String> list, int targetUid) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityManager
        public boolean getProcessRecordFromMTM(ProcessInfo procInfo) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityManager
        public void setAndRestoreMaxAdjIfNeed(List<String> list) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityManager
        public void reportProcessDied(int pid) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityManager
        public void reportAssocDisable() throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityManager
        public boolean canPickColor(String activity) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityManager
        public boolean cleanProcessResourceFast(String processName, int pid, IBinder thread, boolean restartService, boolean isNative) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityManager
        public boolean killProcessRecordFast(String processName, int pid, int uid, boolean restartservice, boolean isAsynchronous, String reason, boolean needCheckAdj) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityManager
        public boolean killNativeProcessRecordFast(String processName, int pid, int uid, boolean restartservice, boolean isAsynchronous, String reason) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwActivityManager
        public void registerServiceHooker(IBinder hooker, Intent filter) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityManager
        public void unregisterServiceHooker(IBinder hooker) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityManager
        public void forceStopPackages(List<String> list, int userId) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwActivityManager
        public void requestProcessGroupChange(int pid, int oldGroup, int newGroup, int isLimit) throws RemoteException {
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwActivityManager {
        private static final String DESCRIPTOR = "com.huawei.android.app.IHwActivityManager";
        static final int TRANSACTION_canPickColor = 20;
        static final int TRANSACTION_cleanPackageRes = 3;
        static final int TRANSACTION_cleanProcessResourceFast = 21;
        static final int TRANSACTION_forceStopPackages = 26;
        static final int TRANSACTION_getPidWithUiFromUid = 8;
        static final int TRANSACTION_getProcessRecordFromMTM = 16;
        static final int TRANSACTION_handleANRFilterFIFO = 5;
        static final int TRANSACTION_handleShowAppEyeAnrUi = 6;
        static final int TRANSACTION_isProcessExistLocked = 14;
        static final int TRANSACTION_killNativeProcessRecordFast = 23;
        static final int TRANSACTION_killProcessRecordFast = 22;
        static final int TRANSACTION_killProcessRecordFromIAware = 11;
        static final int TRANSACTION_killProcessRecordFromIAwareNative = 12;
        static final int TRANSACTION_killProcessRecordFromMTM = 13;
        static final int TRANSACTION_preloadAppForLauncher = 10;
        static final int TRANSACTION_preloadApplication = 9;
        static final int TRANSACTION_registerDAMonitorCallback = 1;
        static final int TRANSACTION_registerServiceHooker = 24;
        static final int TRANSACTION_removePackageAlarm = 15;
        static final int TRANSACTION_reportAssocDisable = 19;
        static final int TRANSACTION_reportProcessDied = 18;
        static final int TRANSACTION_reportScreenRecord = 4;
        static final int TRANSACTION_requestProcessGroupChange = 27;
        static final int TRANSACTION_setAndRestoreMaxAdjIfNeed = 17;
        static final int TRANSACTION_setCpusetSwitch = 2;
        static final int TRANSACTION_setProcessRecForPid = 7;
        static final int TRANSACTION_unregisterServiceHooker = 25;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwActivityManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwActivityManager)) {
                return new Proxy(obj);
            }
            return (IHwActivityManager) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "registerDAMonitorCallback";
                case 2:
                    return "setCpusetSwitch";
                case 3:
                    return "cleanPackageRes";
                case 4:
                    return "reportScreenRecord";
                case 5:
                    return "handleANRFilterFIFO";
                case 6:
                    return "handleShowAppEyeAnrUi";
                case 7:
                    return "setProcessRecForPid";
                case 8:
                    return "getPidWithUiFromUid";
                case 9:
                    return "preloadApplication";
                case 10:
                    return "preloadAppForLauncher";
                case 11:
                    return "killProcessRecordFromIAware";
                case 12:
                    return "killProcessRecordFromIAwareNative";
                case 13:
                    return "killProcessRecordFromMTM";
                case 14:
                    return "isProcessExistLocked";
                case 15:
                    return "removePackageAlarm";
                case 16:
                    return "getProcessRecordFromMTM";
                case 17:
                    return "setAndRestoreMaxAdjIfNeed";
                case 18:
                    return "reportProcessDied";
                case 19:
                    return "reportAssocDisable";
                case 20:
                    return "canPickColor";
                case 21:
                    return "cleanProcessResourceFast";
                case 22:
                    return "killProcessRecordFast";
                case 23:
                    return "killNativeProcessRecordFast";
                case 24:
                    return "registerServiceHooker";
                case 25:
                    return "unregisterServiceHooker";
                case 26:
                    return "forceStopPackages";
                case 27:
                    return "requestProcessGroupChange";
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
            ProcessInfo _arg0;
            ProcessInfo _arg02;
            ProcessInfo _arg03;
            ProcessInfo _arg04;
            Intent _arg1;
            if (code != 1598968902) {
                boolean _arg05 = false;
                boolean _arg12 = false;
                boolean _arg2 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        registerDAMonitorCallback(IHwDAMonitorCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = true;
                        }
                        setCpusetSwitch(_arg05, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        boolean cleanPackageRes = cleanPackageRes(data.createStringArrayList(), data.readHashMap(getClass().getClassLoader()), data.readInt(), data.readInt() != 0, data.readInt() != 0, data.readInt() != 0);
                        reply.writeNoException();
                        reply.writeInt(cleanPackageRes ? 1 : 0);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        reportScreenRecord(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        boolean handleANRFilterFIFO = handleANRFilterFIFO(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(handleANRFilterFIFO ? 1 : 0);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        handleShowAppEyeAnrUi(data.readInt(), data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        setProcessRecForPid(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result = getPidWithUiFromUid(data.readInt());
                        reply.writeNoException();
                        reply.writeStringList(_result);
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        int _result2 = preloadApplication(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = preloadAppForLauncher(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ProcessInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        boolean killProcessRecordFromIAware = killProcessRecordFromIAware(_arg0, data.readInt() != 0, data.readInt() != 0, data.readString(), data.readInt() != 0);
                        reply.writeNoException();
                        reply.writeInt(killProcessRecordFromIAware ? 1 : 0);
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = ProcessInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        boolean _arg13 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        boolean killProcessRecordFromIAwareNative = killProcessRecordFromIAwareNative(_arg02, _arg13, _arg2, data.readString());
                        reply.writeNoException();
                        reply.writeInt(killProcessRecordFromIAwareNative ? 1 : 0);
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg03 = ProcessInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg03 = null;
                        }
                        if (data.readInt() != 0) {
                            _arg12 = true;
                        }
                        boolean killProcessRecordFromMTM = killProcessRecordFromMTM(_arg03, _arg12, data.readString());
                        reply.writeNoException();
                        reply.writeInt(killProcessRecordFromMTM ? 1 : 0);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isProcessExistLocked = isProcessExistLocked(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isProcessExistLocked ? 1 : 0);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        removePackageAlarm(data.readString(), data.createStringArrayList(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = ProcessInfo.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        boolean processRecordFromMTM = getProcessRecordFromMTM(_arg04);
                        reply.writeNoException();
                        reply.writeInt(processRecordFromMTM ? 1 : 0);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        setAndRestoreMaxAdjIfNeed(data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        reportProcessDied(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        reportAssocDisable();
                        reply.writeNoException();
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        boolean canPickColor = canPickColor(data.readString());
                        reply.writeNoException();
                        reply.writeInt(canPickColor ? 1 : 0);
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        boolean cleanProcessResourceFast = cleanProcessResourceFast(data.readString(), data.readInt(), data.readStrongBinder(), data.readInt() != 0, data.readInt() != 0);
                        reply.writeNoException();
                        reply.writeInt(cleanProcessResourceFast ? 1 : 0);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        boolean killProcessRecordFast = killProcessRecordFast(data.readString(), data.readInt(), data.readInt(), data.readInt() != 0, data.readInt() != 0, data.readString(), data.readInt() != 0);
                        reply.writeNoException();
                        reply.writeInt(killProcessRecordFast ? 1 : 0);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        boolean killNativeProcessRecordFast = killNativeProcessRecordFast(data.readString(), data.readInt(), data.readInt(), data.readInt() != 0, data.readInt() != 0, data.readString());
                        reply.writeNoException();
                        reply.writeInt(killNativeProcessRecordFast ? 1 : 0);
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        IBinder _arg06 = data.readStrongBinder();
                        if (data.readInt() != 0) {
                            _arg1 = Intent.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        registerServiceHooker(_arg06, _arg1);
                        reply.writeNoException();
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        unregisterServiceHooker(data.readStrongBinder());
                        reply.writeNoException();
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        forceStopPackages(data.createStringArrayList(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        requestProcessGroupChange(data.readInt(), data.readInt(), data.readInt(), data.readInt());
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
        public static class Proxy implements IHwActivityManager {
            public static IHwActivityManager sDefaultImpl;
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

            @Override // com.huawei.android.app.IHwActivityManager
            public void registerDAMonitorCallback(IHwDAMonitorCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (this.mRemote.transact(1, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerDAMonitorCallback(callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityManager
            public void setCpusetSwitch(boolean enable, int subSwitch) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable ? 1 : 0);
                    _data.writeInt(subSwitch);
                    if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setCpusetSwitch(enable, subSwitch);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityManager
            public boolean cleanPackageRes(List<String> packageList, Map alarmTags, int targetUid, boolean cleanAlarm, boolean isNative, boolean hasPerceptAlarm) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeStringList(packageList);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeMap(alarmTags);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(targetUid);
                        boolean _result = true;
                        _data.writeInt(cleanAlarm ? 1 : 0);
                        _data.writeInt(isNative ? 1 : 0);
                        _data.writeInt(hasPerceptAlarm ? 1 : 0);
                        try {
                            if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                if (_reply.readInt() == 0) {
                                    _result = false;
                                }
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            boolean cleanPackageRes = Stub.getDefaultImpl().cleanPackageRes(packageList, alarmTags, targetUid, cleanAlarm, isNative, hasPerceptAlarm);
                            _reply.recycle();
                            _data.recycle();
                            return cleanPackageRes;
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
                } catch (Throwable th5) {
                    th = th5;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.huawei.android.app.IHwActivityManager
            public void reportScreenRecord(int uid, int pid, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(pid);
                    _data.writeInt(status);
                    if (this.mRemote.transact(4, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().reportScreenRecord(uid, pid, status);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityManager
            public boolean handleANRFilterFIFO(int uid, int cmd) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(cmd);
                    boolean _result = false;
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().handleANRFilterFIFO(uid, cmd);
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

            @Override // com.huawei.android.app.IHwActivityManager
            public void handleShowAppEyeAnrUi(int pid, int uid, String processName, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeString(processName);
                    _data.writeString(packageName);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().handleShowAppEyeAnrUi(pid, uid, processName, packageName);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityManager
            public void setProcessRecForPid(int pid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setProcessRecForPid(pid);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityManager
            public List<String> getPidWithUiFromUid(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    if (!this.mRemote.transact(8, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPidWithUiFromUid(uid);
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

            @Override // com.huawei.android.app.IHwActivityManager
            public int preloadApplication(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    if (!this.mRemote.transact(9, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().preloadApplication(packageName, userId);
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

            @Override // com.huawei.android.app.IHwActivityManager
            public int preloadAppForLauncher(String packageName, int userId, int preloadType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    _data.writeInt(preloadType);
                    if (!this.mRemote.transact(10, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().preloadAppForLauncher(packageName, userId, preloadType);
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

            @Override // com.huawei.android.app.IHwActivityManager
            public boolean killProcessRecordFromIAware(ProcessInfo procInfo, boolean restartservice, boolean isAsynchronous, String reason, boolean checkAdj) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (procInfo != null) {
                        _data.writeInt(1);
                        procInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(restartservice ? 1 : 0);
                    _data.writeInt(isAsynchronous ? 1 : 0);
                    try {
                        _data.writeString(reason);
                        _data.writeInt(checkAdj ? 1 : 0);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        if (this.mRemote.transact(11, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            if (_reply.readInt() == 0) {
                                _result = false;
                            }
                            _reply.recycle();
                            _data.recycle();
                            return _result;
                        }
                        boolean killProcessRecordFromIAware = Stub.getDefaultImpl().killProcessRecordFromIAware(procInfo, restartservice, isAsynchronous, reason, checkAdj);
                        _reply.recycle();
                        _data.recycle();
                        return killProcessRecordFromIAware;
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
            }

            @Override // com.huawei.android.app.IHwActivityManager
            public boolean killProcessRecordFromIAwareNative(ProcessInfo procInfo, boolean restartservice, boolean isAsynchronous, String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (procInfo != null) {
                        _data.writeInt(1);
                        procInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(restartservice ? 1 : 0);
                    _data.writeInt(isAsynchronous ? 1 : 0);
                    _data.writeString(reason);
                    if (!this.mRemote.transact(12, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().killProcessRecordFromIAwareNative(procInfo, restartservice, isAsynchronous, reason);
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

            @Override // com.huawei.android.app.IHwActivityManager
            public boolean killProcessRecordFromMTM(ProcessInfo procInfo, boolean restartservice, String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (procInfo != null) {
                        _data.writeInt(1);
                        procInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeInt(restartservice ? 1 : 0);
                    _data.writeString(reason);
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().killProcessRecordFromMTM(procInfo, restartservice, reason);
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

            @Override // com.huawei.android.app.IHwActivityManager
            public boolean isProcessExistLocked(String processName, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(processName);
                    _data.writeInt(uid);
                    boolean _result = false;
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isProcessExistLocked(processName, uid);
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

            @Override // com.huawei.android.app.IHwActivityManager
            public void removePackageAlarm(String pkg, List<String> tags, int targetUid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeStringList(tags);
                    _data.writeInt(targetUid);
                    if (this.mRemote.transact(15, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().removePackageAlarm(pkg, tags, targetUid);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityManager
            public boolean getProcessRecordFromMTM(ProcessInfo procInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (procInfo != null) {
                        _data.writeInt(1);
                        procInfo.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getProcessRecordFromMTM(procInfo);
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

            @Override // com.huawei.android.app.IHwActivityManager
            public void setAndRestoreMaxAdjIfNeed(List<String> adjCustPkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(adjCustPkg);
                    if (this.mRemote.transact(17, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setAndRestoreMaxAdjIfNeed(adjCustPkg);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityManager
            public void reportProcessDied(int pid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    if (this.mRemote.transact(18, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().reportProcessDied(pid);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityManager
            public void reportAssocDisable() throws RemoteException {
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
                    Stub.getDefaultImpl().reportAssocDisable();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityManager
            public boolean canPickColor(String activity) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(activity);
                    boolean _result = false;
                    if (!this.mRemote.transact(20, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().canPickColor(activity);
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

            @Override // com.huawei.android.app.IHwActivityManager
            public boolean cleanProcessResourceFast(String processName, int pid, IBinder thread, boolean restartService, boolean isNative) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(processName);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(pid);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeStrongBinder(thread);
                        boolean _result = true;
                        _data.writeInt(restartService ? 1 : 0);
                        _data.writeInt(isNative ? 1 : 0);
                        try {
                            if (this.mRemote.transact(21, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                if (_reply.readInt() == 0) {
                                    _result = false;
                                }
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            boolean cleanProcessResourceFast = Stub.getDefaultImpl().cleanProcessResourceFast(processName, pid, thread, restartService, isNative);
                            _reply.recycle();
                            _data.recycle();
                            return cleanProcessResourceFast;
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
                } catch (Throwable th5) {
                    th = th5;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.huawei.android.app.IHwActivityManager
            public boolean killProcessRecordFast(String processName, int pid, int uid, boolean restartservice, boolean isAsynchronous, String reason, boolean needCheckAdj) throws RemoteException {
                boolean _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(processName);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(pid);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(uid);
                        _result = true;
                        _data.writeInt(restartservice ? 1 : 0);
                        _data.writeInt(isAsynchronous ? 1 : 0);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(reason);
                        _data.writeInt(needCheckAdj ? 1 : 0);
                        try {
                            if (this.mRemote.transact(22, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                if (_reply.readInt() == 0) {
                                    _result = false;
                                }
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            boolean killProcessRecordFast = Stub.getDefaultImpl().killProcessRecordFast(processName, pid, uid, restartservice, isAsynchronous, reason, needCheckAdj);
                            _reply.recycle();
                            _data.recycle();
                            return killProcessRecordFast;
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

            @Override // com.huawei.android.app.IHwActivityManager
            public boolean killNativeProcessRecordFast(String processName, int pid, int uid, boolean restartservice, boolean isAsynchronous, String reason) throws RemoteException {
                boolean _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(processName);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(pid);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(uid);
                        _result = true;
                        _data.writeInt(restartservice ? 1 : 0);
                        _data.writeInt(isAsynchronous ? 1 : 0);
                    } catch (Throwable th3) {
                        th = th3;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(reason);
                        try {
                            if (this.mRemote.transact(23, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                if (_reply.readInt() == 0) {
                                    _result = false;
                                }
                                _reply.recycle();
                                _data.recycle();
                                return _result;
                            }
                            boolean killNativeProcessRecordFast = Stub.getDefaultImpl().killNativeProcessRecordFast(processName, pid, uid, restartservice, isAsynchronous, reason);
                            _reply.recycle();
                            _data.recycle();
                            return killNativeProcessRecordFast;
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

            @Override // com.huawei.android.app.IHwActivityManager
            public void registerServiceHooker(IBinder hooker, Intent filter) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(hooker);
                    if (filter != null) {
                        _data.writeInt(1);
                        filter.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(24, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerServiceHooker(hooker, filter);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityManager
            public void unregisterServiceHooker(IBinder hooker) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(hooker);
                    if (this.mRemote.transact(25, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterServiceHooker(hooker);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityManager
            public void forceStopPackages(List<String> packagesNames, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packagesNames);
                    _data.writeInt(userId);
                    if (this.mRemote.transact(26, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().forceStopPackages(packagesNames, userId);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwActivityManager
            public void requestProcessGroupChange(int pid, int oldGroup, int newGroup, int isLimit) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    _data.writeInt(oldGroup);
                    _data.writeInt(newGroup);
                    _data.writeInt(isLimit);
                    if (this.mRemote.transact(27, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().requestProcessGroupChange(pid, oldGroup, newGroup, isLimit);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

        public static boolean setDefaultImpl(IHwActivityManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwActivityManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
