package com.huawei.android.app;

import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

public interface IHwDAMonitorCallback extends IInterface {
    void addPssToMap(String str, String str2, int i, int i2, int i3, long j, long j2, long j3, boolean z) throws RemoteException;

    int getActivityImportCount() throws RemoteException;

    int getCPUConfigGroupBG() throws RemoteException;

    int getFirstDevSchedEventId() throws RemoteException;

    String getRecentTask() throws RemoteException;

    int isCPUConfigWhiteList(String str) throws RemoteException;

    boolean isExcludedInBGCheck(String str, String str2) throws RemoteException;

    boolean isFastKillSwitch(String str, int i) throws RemoteException;

    boolean isResourceNeeded(String str) throws RemoteException;

    int killProcessGroupForQuickKill(int i, int i2) throws RemoteException;

    void noteActivityDisplayedStart(String str, int i, int i2) throws RemoteException;

    void noteProcessStart(String str, String str2, int i, int i2, boolean z, String str3, String str4) throws RemoteException;

    void notifyActivityState(String str) throws RemoteException;

    void notifyProcessDied(int i, int i2) throws RemoteException;

    void notifyProcessGroupChange(int i, int i2) throws RemoteException;

    void notifyProcessGroupChangeCpu(int i, int i2, int i3, int i4) throws RemoteException;

    void notifyProcessStatusChange(String str, String str2, String str3, int i, int i2) throws RemoteException;

    void notifyProcessWillDie(boolean z, boolean z2, boolean z3, String str, int i, int i2) throws RemoteException;

    void onPointerEvent(int i) throws RemoteException;

    void onWakefulnessChanged(int i) throws RemoteException;

    void reportAppDiedMsg(int i, String str, String str2) throws RemoteException;

    void reportCamera(int i, int i2) throws RemoteException;

    void reportData(String str, long j, Bundle bundle) throws RemoteException;

    void reportScreenRecord(int i, int i2, int i3) throws RemoteException;

    int resetAppMngOomAdj(int i, String str) throws RemoteException;

    void setVipThread(int i, int i2, int i3, boolean z, boolean z2) throws RemoteException;

    public static class Default implements IHwDAMonitorCallback {
        @Override // com.huawei.android.app.IHwDAMonitorCallback
        public int getActivityImportCount() throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.app.IHwDAMonitorCallback
        public String getRecentTask() throws RemoteException {
            return null;
        }

        @Override // com.huawei.android.app.IHwDAMonitorCallback
        public int isCPUConfigWhiteList(String processName) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.app.IHwDAMonitorCallback
        public int getCPUConfigGroupBG() throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.app.IHwDAMonitorCallback
        public int getFirstDevSchedEventId() throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.app.IHwDAMonitorCallback
        public void notifyActivityState(String activityInfo) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwDAMonitorCallback
        public void reportScreenRecord(int uid, int pid, int status) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwDAMonitorCallback
        public void reportCamera(int uid, int status) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwDAMonitorCallback
        public void notifyProcessGroupChangeCpu(int pid, int uid, int renderThreadTid, int grp) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwDAMonitorCallback
        public void setVipThread(int uid, int pid, int renderThreadTid, boolean isSet, boolean isSetGroup) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwDAMonitorCallback
        public void onPointerEvent(int action) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwDAMonitorCallback
        public void addPssToMap(String packageName, String procName, int uid, int pid, int procState, long pss, long uss, long swapPss, boolean test) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwDAMonitorCallback
        public void reportAppDiedMsg(int userId, String processName, String reason) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwDAMonitorCallback
        public int killProcessGroupForQuickKill(int uid, int pid) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.app.IHwDAMonitorCallback
        public void noteProcessStart(String packageName, String processName, int pid, int uid, boolean started, String launcherMode, String reason) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwDAMonitorCallback
        public void onWakefulnessChanged(int wakefulness) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwDAMonitorCallback
        public void notifyProcessGroupChange(int pid, int uid) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwDAMonitorCallback
        public void notifyProcessStatusChange(String pkg, String process, String hostingType, int pid, int uid) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwDAMonitorCallback
        public void notifyProcessWillDie(boolean byForceStop, boolean crashed, boolean byAnr, String packageName, int pid, int uid) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwDAMonitorCallback
        public void notifyProcessDied(int pid, int uid) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwDAMonitorCallback
        public int resetAppMngOomAdj(int maxAdj, String packageName) throws RemoteException {
            return 0;
        }

        @Override // com.huawei.android.app.IHwDAMonitorCallback
        public boolean isResourceNeeded(String resourceid) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwDAMonitorCallback
        public void reportData(String resourceid, long timestamp, Bundle args) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwDAMonitorCallback
        public boolean isExcludedInBGCheck(String pkg, String action) throws RemoteException {
            return false;
        }

        @Override // com.huawei.android.app.IHwDAMonitorCallback
        public void noteActivityDisplayedStart(String componentName, int uid, int pid) throws RemoteException {
        }

        @Override // com.huawei.android.app.IHwDAMonitorCallback
        public boolean isFastKillSwitch(String processName, int uid) throws RemoteException {
            return false;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return null;
        }
    }

    public static abstract class Stub extends Binder implements IHwDAMonitorCallback {
        private static final String DESCRIPTOR = "com.huawei.android.app.IHwDAMonitorCallback";
        static final int TRANSACTION_addPssToMap = 12;
        static final int TRANSACTION_getActivityImportCount = 1;
        static final int TRANSACTION_getCPUConfigGroupBG = 4;
        static final int TRANSACTION_getFirstDevSchedEventId = 5;
        static final int TRANSACTION_getRecentTask = 2;
        static final int TRANSACTION_isCPUConfigWhiteList = 3;
        static final int TRANSACTION_isExcludedInBGCheck = 24;
        static final int TRANSACTION_isFastKillSwitch = 26;
        static final int TRANSACTION_isResourceNeeded = 22;
        static final int TRANSACTION_killProcessGroupForQuickKill = 14;
        static final int TRANSACTION_noteActivityDisplayedStart = 25;
        static final int TRANSACTION_noteProcessStart = 15;
        static final int TRANSACTION_notifyActivityState = 6;
        static final int TRANSACTION_notifyProcessDied = 20;
        static final int TRANSACTION_notifyProcessGroupChange = 17;
        static final int TRANSACTION_notifyProcessGroupChangeCpu = 9;
        static final int TRANSACTION_notifyProcessStatusChange = 18;
        static final int TRANSACTION_notifyProcessWillDie = 19;
        static final int TRANSACTION_onPointerEvent = 11;
        static final int TRANSACTION_onWakefulnessChanged = 16;
        static final int TRANSACTION_reportAppDiedMsg = 13;
        static final int TRANSACTION_reportCamera = 8;
        static final int TRANSACTION_reportData = 23;
        static final int TRANSACTION_reportScreenRecord = 7;
        static final int TRANSACTION_resetAppMngOomAdj = 21;
        static final int TRANSACTION_setVipThread = 10;

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IHwDAMonitorCallback asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwDAMonitorCallback)) {
                return new Proxy(obj);
            }
            return (IHwDAMonitorCallback) iin;
        }

        @Override // android.os.IInterface
        public IBinder asBinder() {
            return this;
        }

        public static String getDefaultTransactionName(int transactionCode) {
            switch (transactionCode) {
                case 1:
                    return "getActivityImportCount";
                case 2:
                    return "getRecentTask";
                case 3:
                    return "isCPUConfigWhiteList";
                case 4:
                    return "getCPUConfigGroupBG";
                case 5:
                    return "getFirstDevSchedEventId";
                case 6:
                    return "notifyActivityState";
                case 7:
                    return "reportScreenRecord";
                case 8:
                    return "reportCamera";
                case 9:
                    return "notifyProcessGroupChangeCpu";
                case 10:
                    return "setVipThread";
                case 11:
                    return "onPointerEvent";
                case 12:
                    return "addPssToMap";
                case 13:
                    return "reportAppDiedMsg";
                case 14:
                    return "killProcessGroupForQuickKill";
                case 15:
                    return "noteProcessStart";
                case 16:
                    return "onWakefulnessChanged";
                case 17:
                    return "notifyProcessGroupChange";
                case 18:
                    return "notifyProcessStatusChange";
                case 19:
                    return "notifyProcessWillDie";
                case 20:
                    return "notifyProcessDied";
                case 21:
                    return "resetAppMngOomAdj";
                case 22:
                    return "isResourceNeeded";
                case 23:
                    return "reportData";
                case 24:
                    return "isExcludedInBGCheck";
                case 25:
                    return "noteActivityDisplayedStart";
                case 26:
                    return "isFastKillSwitch";
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
            Bundle _arg2;
            if (code != 1598968902) {
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        int _result = getActivityImportCount();
                        reply.writeNoException();
                        reply.writeInt(_result);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        String _result2 = getRecentTask();
                        reply.writeNoException();
                        reply.writeString(_result2);
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        int _result3 = isCPUConfigWhiteList(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _result4 = getCPUConfigGroupBG();
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _result5 = getFirstDevSchedEventId();
                        reply.writeNoException();
                        reply.writeInt(_result5);
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        notifyActivityState(data.readString());
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        reportScreenRecord(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        reportCamera(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 9:
                        data.enforceInterface(DESCRIPTOR);
                        notifyProcessGroupChangeCpu(data.readInt(), data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 10:
                        data.enforceInterface(DESCRIPTOR);
                        setVipThread(data.readInt(), data.readInt(), data.readInt(), data.readInt() != 0, data.readInt() != 0);
                        reply.writeNoException();
                        return true;
                    case 11:
                        data.enforceInterface(DESCRIPTOR);
                        onPointerEvent(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 12:
                        data.enforceInterface(DESCRIPTOR);
                        addPssToMap(data.readString(), data.readString(), data.readInt(), data.readInt(), data.readInt(), data.readLong(), data.readLong(), data.readLong(), data.readInt() != 0);
                        reply.writeNoException();
                        return true;
                    case 13:
                        data.enforceInterface(DESCRIPTOR);
                        reportAppDiedMsg(data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        int _result6 = killProcessGroupForQuickKill(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(_result6);
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        noteProcessStart(data.readString(), data.readString(), data.readInt(), data.readInt(), data.readInt() != 0, data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        onWakefulnessChanged(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        notifyProcessGroupChange(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        notifyProcessStatusChange(data.readString(), data.readString(), data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        notifyProcessWillDie(data.readInt() != 0, data.readInt() != 0, data.readInt() != 0, data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        notifyProcessDied(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        int _result7 = resetAppMngOomAdj(data.readInt(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result7);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isResourceNeeded = isResourceNeeded(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isResourceNeeded ? 1 : 0);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg0 = data.readString();
                        long _arg1 = data.readLong();
                        if (data.readInt() != 0) {
                            _arg2 = Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        reportData(_arg0, _arg1, _arg2);
                        reply.writeNoException();
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isExcludedInBGCheck = isExcludedInBGCheck(data.readString(), data.readString());
                        reply.writeNoException();
                        reply.writeInt(isExcludedInBGCheck ? 1 : 0);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        noteActivityDisplayedStart(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isFastKillSwitch = isFastKillSwitch(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isFastKillSwitch ? 1 : 0);
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
        public static class Proxy implements IHwDAMonitorCallback {
            public static IHwDAMonitorCallback sDefaultImpl;
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

            @Override // com.huawei.android.app.IHwDAMonitorCallback
            public int getActivityImportCount() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getActivityImportCount();
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

            @Override // com.huawei.android.app.IHwDAMonitorCallback
            public String getRecentTask() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(2, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getRecentTask();
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

            @Override // com.huawei.android.app.IHwDAMonitorCallback
            public int isCPUConfigWhiteList(String processName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(processName);
                    if (!this.mRemote.transact(3, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isCPUConfigWhiteList(processName);
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

            @Override // com.huawei.android.app.IHwDAMonitorCallback
            public int getCPUConfigGroupBG() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getCPUConfigGroupBG();
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

            @Override // com.huawei.android.app.IHwDAMonitorCallback
            public int getFirstDevSchedEventId() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(5, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFirstDevSchedEventId();
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

            @Override // com.huawei.android.app.IHwDAMonitorCallback
            public void notifyActivityState(String activityInfo) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(activityInfo);
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyActivityState(activityInfo);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwDAMonitorCallback
            public void reportScreenRecord(int uid, int pid, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(pid);
                    _data.writeInt(status);
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
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

            @Override // com.huawei.android.app.IHwDAMonitorCallback
            public void reportCamera(int uid, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(status);
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().reportCamera(uid, status);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwDAMonitorCallback
            public void notifyProcessGroupChangeCpu(int pid, int uid, int renderThreadTid, int grp) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeInt(renderThreadTid);
                    _data.writeInt(grp);
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyProcessGroupChangeCpu(pid, uid, renderThreadTid, grp);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwDAMonitorCallback
            public void setVipThread(int uid, int pid, int renderThreadTid, boolean isSet, boolean isSetGroup) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(pid);
                    _data.writeInt(renderThreadTid);
                    int i = 1;
                    _data.writeInt(isSet ? 1 : 0);
                    if (!isSetGroup) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (this.mRemote.transact(10, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().setVipThread(uid, pid, renderThreadTid, isSet, isSetGroup);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwDAMonitorCallback
            public void onPointerEvent(int action) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(action);
                    if (this.mRemote.transact(11, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onPointerEvent(action);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwDAMonitorCallback
            public void addPssToMap(String packageName, String procName, int uid, int pid, int procState, long pss, long uss, long swapPss, boolean test) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(procName);
                    _data.writeInt(uid);
                    _data.writeInt(pid);
                    _data.writeInt(procState);
                    _data.writeLong(pss);
                    _data.writeLong(uss);
                    _data.writeLong(swapPss);
                    _data.writeInt(test ? 1 : 0);
                    if (this.mRemote.transact(12, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().addPssToMap(packageName, procName, uid, pid, procState, pss, uss, swapPss, test);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwDAMonitorCallback
            public void reportAppDiedMsg(int userId, String processName, String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userId);
                    _data.writeString(processName);
                    _data.writeString(reason);
                    if (this.mRemote.transact(13, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().reportAppDiedMsg(userId, processName, reason);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwDAMonitorCallback
            public int killProcessGroupForQuickKill(int uid, int pid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(pid);
                    if (!this.mRemote.transact(14, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().killProcessGroupForQuickKill(uid, pid);
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

            @Override // com.huawei.android.app.IHwDAMonitorCallback
            public void noteProcessStart(String packageName, String processName, int pid, int uid, boolean started, String launcherMode, String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeString(packageName);
                    } catch (Throwable th) {
                        th = th;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeString(processName);
                    } catch (Throwable th2) {
                        th = th2;
                        _reply.recycle();
                        _data.recycle();
                        throw th;
                    }
                    try {
                        _data.writeInt(pid);
                        try {
                            _data.writeInt(uid);
                            _data.writeInt(started ? 1 : 0);
                        } catch (Throwable th3) {
                            th = th3;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeString(launcherMode);
                            _data.writeString(reason);
                            if (this.mRemote.transact(15, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                _reply.readException();
                                _reply.recycle();
                                _data.recycle();
                                return;
                            }
                            Stub.getDefaultImpl().noteProcessStart(packageName, processName, pid, uid, started, launcherMode, reason);
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

            @Override // com.huawei.android.app.IHwDAMonitorCallback
            public void onWakefulnessChanged(int wakefulness) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(wakefulness);
                    if (this.mRemote.transact(16, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().onWakefulnessChanged(wakefulness);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwDAMonitorCallback
            public void notifyProcessGroupChange(int pid, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    if (this.mRemote.transact(17, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyProcessGroupChange(pid, uid);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwDAMonitorCallback
            public void notifyProcessStatusChange(String pkg, String process, String hostingType, int pid, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeString(process);
                    _data.writeString(hostingType);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    if (this.mRemote.transact(18, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyProcessStatusChange(pkg, process, hostingType, pid, uid);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwDAMonitorCallback
            public void notifyProcessWillDie(boolean byForceStop, boolean crashed, boolean byAnr, String packageName, int pid, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    int i = 1;
                    _data.writeInt(byForceStop ? 1 : 0);
                    _data.writeInt(crashed ? 1 : 0);
                    if (!byAnr) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    try {
                        _data.writeString(packageName);
                        try {
                            _data.writeInt(pid);
                        } catch (Throwable th) {
                            th = th;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeInt(uid);
                            try {
                                if (this.mRemote.transact(19, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                                    _reply.readException();
                                    _reply.recycle();
                                    _data.recycle();
                                    return;
                                }
                                Stub.getDefaultImpl().notifyProcessWillDie(byForceStop, crashed, byAnr, packageName, pid, uid);
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
                } catch (Throwable th5) {
                    th = th5;
                    _reply.recycle();
                    _data.recycle();
                    throw th;
                }
            }

            @Override // com.huawei.android.app.IHwDAMonitorCallback
            public void notifyProcessDied(int pid, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    if (this.mRemote.transact(20, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyProcessDied(pid, uid);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwDAMonitorCallback
            public int resetAppMngOomAdj(int maxAdj, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(maxAdj);
                    _data.writeString(packageName);
                    if (!this.mRemote.transact(21, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().resetAppMngOomAdj(maxAdj, packageName);
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

            @Override // com.huawei.android.app.IHwDAMonitorCallback
            public boolean isResourceNeeded(String resourceid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(resourceid);
                    boolean _result = false;
                    if (!this.mRemote.transact(22, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isResourceNeeded(resourceid);
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

            @Override // com.huawei.android.app.IHwDAMonitorCallback
            public void reportData(String resourceid, long timestamp, Bundle args) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(resourceid);
                    _data.writeLong(timestamp);
                    if (args != null) {
                        _data.writeInt(1);
                        args.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(23, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().reportData(resourceid, timestamp, args);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwDAMonitorCallback
            public boolean isExcludedInBGCheck(String pkg, String action) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeString(action);
                    boolean _result = false;
                    if (!this.mRemote.transact(24, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isExcludedInBGCheck(pkg, action);
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

            @Override // com.huawei.android.app.IHwDAMonitorCallback
            public void noteActivityDisplayedStart(String componentName, int uid, int pid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(componentName);
                    _data.writeInt(uid);
                    _data.writeInt(pid);
                    if (this.mRemote.transact(25, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().noteActivityDisplayedStart(componentName, uid, pid);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // com.huawei.android.app.IHwDAMonitorCallback
            public boolean isFastKillSwitch(String processName, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(processName);
                    _data.writeInt(uid);
                    boolean _result = false;
                    if (!this.mRemote.transact(26, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isFastKillSwitch(processName, uid);
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
        }

        public static boolean setDefaultImpl(IHwDAMonitorCallback impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwDAMonitorCallback getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
