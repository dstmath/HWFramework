package com.huawei.android.app;

import android.app.IHwActivityNotifier;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Rect;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.IMWThirdpartyCallback;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.huawei.android.app.IHwDAMonitorCallback;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface IHwActivityManager extends IInterface {

    public static abstract class Stub extends Binder implements IHwActivityManager {
        private static final String DESCRIPTOR = "com.huawei.android.app.IHwActivityManager";
        static final int TRANSACTION_canPickColor = 31;
        static final int TRANSACTION_cleanPackageRes = 4;
        static final int TRANSACTION_cleanProcessResourceFast = 32;
        static final int TRANSACTION_dismissSplitScreenToFocusedStack = 38;
        static final int TRANSACTION_enterCoordinationMode = 39;
        static final int TRANSACTION_exitCoordinationMode = 40;
        static final int TRANSACTION_getActivityWindowMode = 37;
        static final int TRANSACTION_getCaptionState = 36;
        static final int TRANSACTION_getLastResumedActivity = 25;
        static final int TRANSACTION_getPCTopTaskBounds = 29;
        static final int TRANSACTION_getPidWithUiFromUid = 14;
        static final int TRANSACTION_getProcessRecordFromMTM = 21;
        static final int TRANSACTION_getTopTaskIdInDisplay = 27;
        static final int TRANSACTION_handleANRFilterFIFO = 11;
        static final int TRANSACTION_handleShowAppEyeAnrUi = 12;
        static final int TRANSACTION_isFreeFormVisible = 8;
        static final int TRANSACTION_isInMultiWindowMode = 30;
        static final int TRANSACTION_isProcessExistLocked = 19;
        static final int TRANSACTION_isProcessExistPidsSelfLocked = 26;
        static final int TRANSACTION_isTaskSupportResize = 28;
        static final int TRANSACTION_isTaskVisible = 13;
        static final int TRANSACTION_killNativeProcessRecordFast = 34;
        static final int TRANSACTION_killProcessRecordFast = 33;
        static final int TRANSACTION_killProcessRecordFromIAware = 16;
        static final int TRANSACTION_killProcessRecordFromIAwareNative = 17;
        static final int TRANSACTION_killProcessRecordFromMTM = 18;
        static final int TRANSACTION_preloadApplication = 15;
        static final int TRANSACTION_registerDAMonitorCallback = 1;
        static final int TRANSACTION_registerHwActivityNotifier = 9;
        static final int TRANSACTION_registerThirdPartyCallBack = 5;
        static final int TRANSACTION_removePackageAlarm = 20;
        static final int TRANSACTION_reportAssocDisable = 24;
        static final int TRANSACTION_reportProcessDied = 23;
        static final int TRANSACTION_reportScreenRecord = 7;
        static final int TRANSACTION_setAndRestoreMaxAdjIfNeed = 22;
        static final int TRANSACTION_setCpusetSwitch = 2;
        static final int TRANSACTION_setWarmColdSwitch = 3;
        static final int TRANSACTION_unregisterHwActivityNotifier = 10;
        static final int TRANSACTION_unregisterThirdPartyCallBack = 6;
        static final int TRANSACTION_updateFreeFormOutLine = 35;

        private static class Proxy implements IHwActivityManager {
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

            public void registerDAMonitorCallback(IHwDAMonitorCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setCpusetSwitch(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable);
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setWarmColdSwitch(boolean enable) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(enable);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean cleanPackageRes(List<String> packageList, Map alarmTags, int targetUid, boolean cleanAlarm, boolean isNative, boolean hasPerceptAlarm) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(packageList);
                    _data.writeMap(alarmTags);
                    _data.writeInt(targetUid);
                    _data.writeInt(cleanAlarm);
                    _data.writeInt(isNative);
                    _data.writeInt(hasPerceptAlarm);
                    boolean _result = false;
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean registerThirdPartyCallBack(IMWThirdpartyCallback aCallBackHandler) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(aCallBackHandler != null ? aCallBackHandler.asBinder() : null);
                    boolean _result = false;
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean unregisterThirdPartyCallBack(IMWThirdpartyCallback aCallBackHandler) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(aCallBackHandler != null ? aCallBackHandler.asBinder() : null);
                    boolean _result = false;
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void reportScreenRecord(int uid, int pid, int status) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(pid);
                    _data.writeInt(status);
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isFreeFormVisible() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerHwActivityNotifier(IHwActivityNotifier notifier, String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(notifier != null ? notifier.asBinder() : null);
                    _data.writeString(reason);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterHwActivityNotifier(IHwActivityNotifier notifier) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(notifier != null ? notifier.asBinder() : null);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean handleANRFilterFIFO(int uid, int cmd) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(cmd);
                    boolean _result = false;
                    this.mRemote.transact(11, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void handleShowAppEyeAnrUi(int pid, int uid, String processName, String packageName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeString(processName);
                    _data.writeString(packageName);
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isTaskVisible(int id) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(id);
                    boolean _result = false;
                    this.mRemote.transact(13, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getPidWithUiFromUid(int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int preloadApplication(String packageName, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

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
                    _data.writeInt(restartservice);
                    _data.writeInt(isAsynchronous);
                    _data.writeString(reason);
                    _data.writeInt(checkAdj);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

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
                    _data.writeInt(restartservice);
                    _data.writeInt(isAsynchronous);
                    _data.writeString(reason);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

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
                    _data.writeInt(restartservice);
                    _data.writeString(reason);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isProcessExistLocked(String processName, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(processName);
                    _data.writeInt(uid);
                    boolean _result = false;
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void removePackageAlarm(String pkg, List<String> tags, int targetUid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeStringList(tags);
                    _data.writeInt(targetUid);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

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
                    this.mRemote.transact(21, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void setAndRestoreMaxAdjIfNeed(List<String> adjCustPkg) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(adjCustPkg);
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void reportProcessDied(int pid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void reportAssocDisable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public ActivityInfo getLastResumedActivity() throws RemoteException {
                ActivityInfo _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (ActivityInfo) ActivityInfo.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isProcessExistPidsSelfLocked(String processName, int uid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(processName);
                    _data.writeInt(uid);
                    boolean _result = false;
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getTopTaskIdInDisplay(int displayId, String pkgName, boolean invisibleAlso) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    _data.writeString(pkgName);
                    _data.writeInt(invisibleAlso);
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isTaskSupportResize(int taskId, boolean isFullscreen, boolean isMaximized) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(taskId);
                    _data.writeInt(isFullscreen);
                    _data.writeInt(isMaximized);
                    boolean _result = false;
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Rect getPCTopTaskBounds(int displayId) throws RemoteException {
                Rect _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(displayId);
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Rect) Rect.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isInMultiWindowMode() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean canPickColor(String activity) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(activity);
                    boolean _result = false;
                    this.mRemote.transact(31, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean cleanProcessResourceFast(String processName, int pid, IBinder thread, boolean restartService, boolean isNative) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(processName);
                    _data.writeInt(pid);
                    _data.writeStrongBinder(thread);
                    _data.writeInt(restartService);
                    _data.writeInt(isNative);
                    boolean _result = false;
                    this.mRemote.transact(32, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean killProcessRecordFast(String processName, int pid, int uid, boolean restartservice, boolean isAsynchronous, String reason, boolean needCheckAdj) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(processName);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeInt(restartservice);
                    _data.writeInt(isAsynchronous);
                    _data.writeString(reason);
                    _data.writeInt(needCheckAdj);
                    boolean _result = false;
                    this.mRemote.transact(33, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean killNativeProcessRecordFast(String processName, int pid, int uid, boolean restartservice, boolean isAsynchronous, String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(processName);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeInt(restartservice);
                    _data.writeInt(isAsynchronous);
                    _data.writeString(reason);
                    boolean _result = false;
                    this.mRemote.transact(34, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateFreeFormOutLine(int state) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(state);
                    this.mRemote.transact(35, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getCaptionState(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(36, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getActivityWindowMode(IBinder token) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(token);
                    this.mRemote.transact(37, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void dismissSplitScreenToFocusedStack() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(38, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean enterCoordinationMode(Intent intent) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = true;
                    if (intent != null) {
                        _data.writeInt(1);
                        intent.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(39, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() == 0) {
                        _result = false;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean exitCoordinationMode(boolean toTop) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(toTop);
                    boolean _result = false;
                    this.mRemote.transact(40, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = true;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }
        }

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

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v38, resolved type: com.android.server.mtm.taskstatus.ProcessInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v42, resolved type: com.android.server.mtm.taskstatus.ProcessInfo} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v48, resolved type: com.android.server.mtm.taskstatus.ProcessInfo} */
        /* JADX WARNING: type inference failed for: r0v1 */
        /* JADX WARNING: type inference failed for: r0v29 */
        /* JADX WARNING: type inference failed for: r0v77, types: [android.content.Intent] */
        /* JADX WARNING: type inference failed for: r0v84 */
        /* JADX WARNING: type inference failed for: r0v85 */
        /* JADX WARNING: type inference failed for: r0v86 */
        /* JADX WARNING: type inference failed for: r0v87 */
        /* JADX WARNING: type inference failed for: r0v88 */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int i = code;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            if (i != 1598968902) {
                ? _arg0 = 0;
                boolean _arg5 = false;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        registerDAMonitorCallback(IHwDAMonitorCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        setCpusetSwitch(_arg5);
                        reply.writeNoException();
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        setWarmColdSwitch(_arg5);
                        reply.writeNoException();
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        ArrayList<String> createStringArrayList = data.createStringArrayList();
                        HashMap readHashMap = parcel.readHashMap(getClass().getClassLoader());
                        int _arg2 = data.readInt();
                        boolean _arg3 = data.readInt() != 0;
                        boolean _arg4 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        boolean _result = cleanPackageRes(createStringArrayList, readHashMap, _arg2, _arg3, _arg4, _arg5);
                        reply.writeNoException();
                        parcel2.writeInt(_result);
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result2 = registerThirdPartyCallBack(IMWThirdpartyCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result2);
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result3 = unregisterThirdPartyCallBack(IMWThirdpartyCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result3);
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        reportScreenRecord(data.readInt(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result4 = isFreeFormVisible();
                        reply.writeNoException();
                        parcel2.writeInt(_result4);
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        registerHwActivityNotifier(IHwActivityNotifier.Stub.asInterface(data.readStrongBinder()), data.readString());
                        reply.writeNoException();
                        return true;
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        unregisterHwActivityNotifier(IHwActivityNotifier.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 11:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result5 = handleANRFilterFIFO(data.readInt(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result5);
                        return true;
                    case 12:
                        parcel.enforceInterface(DESCRIPTOR);
                        handleShowAppEyeAnrUi(data.readInt(), data.readInt(), data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 13:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result6 = isTaskVisible(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result6);
                        return true;
                    case 14:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<String> _result7 = getPidWithUiFromUid(data.readInt());
                        reply.writeNoException();
                        parcel2.writeStringList(_result7);
                        return true;
                    case 15:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result8 = preloadApplication(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result8);
                        return true;
                    case 16:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ProcessInfo.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result9 = killProcessRecordFromIAware(_arg0, data.readInt() != 0, data.readInt() != 0, data.readString(), data.readInt() != 0);
                        reply.writeNoException();
                        parcel2.writeInt(_result9);
                        return true;
                    case 17:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ProcessInfo.CREATOR.createFromParcel(parcel);
                        }
                        boolean _arg1 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        boolean _result10 = killProcessRecordFromIAwareNative(_arg0, _arg1, _arg5, data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result10);
                        return true;
                    case 18:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ProcessInfo.CREATOR.createFromParcel(parcel);
                        }
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        boolean _result11 = killProcessRecordFromMTM(_arg0, _arg5, data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result11);
                        return true;
                    case 19:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result12 = isProcessExistLocked(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result12);
                        return true;
                    case 20:
                        parcel.enforceInterface(DESCRIPTOR);
                        removePackageAlarm(data.readString(), data.createStringArrayList(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 21:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = ProcessInfo.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result13 = getProcessRecordFromMTM(_arg0);
                        reply.writeNoException();
                        parcel2.writeInt(_result13);
                        return true;
                    case 22:
                        parcel.enforceInterface(DESCRIPTOR);
                        setAndRestoreMaxAdjIfNeed(data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 23:
                        parcel.enforceInterface(DESCRIPTOR);
                        reportProcessDied(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 24:
                        parcel.enforceInterface(DESCRIPTOR);
                        reportAssocDisable();
                        reply.writeNoException();
                        return true;
                    case 25:
                        parcel.enforceInterface(DESCRIPTOR);
                        ActivityInfo _result14 = getLastResumedActivity();
                        reply.writeNoException();
                        if (_result14 != null) {
                            parcel2.writeInt(1);
                            _result14.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 26:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result15 = isProcessExistPidsSelfLocked(data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result15);
                        return true;
                    case 27:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        String _arg12 = data.readString();
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        int _result16 = getTopTaskIdInDisplay(_arg02, _arg12, _arg5);
                        reply.writeNoException();
                        parcel2.writeInt(_result16);
                        return true;
                    case 28:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        boolean _arg13 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        boolean _result17 = isTaskSupportResize(_arg03, _arg13, _arg5);
                        reply.writeNoException();
                        parcel2.writeInt(_result17);
                        return true;
                    case 29:
                        parcel.enforceInterface(DESCRIPTOR);
                        Rect _result18 = getPCTopTaskBounds(data.readInt());
                        reply.writeNoException();
                        if (_result18 != null) {
                            parcel2.writeInt(1);
                            _result18.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 30:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result19 = isInMultiWindowMode();
                        reply.writeNoException();
                        parcel2.writeInt(_result19);
                        return true;
                    case 31:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result20 = canPickColor(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result20);
                        return true;
                    case 32:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result21 = cleanProcessResourceFast(data.readString(), data.readInt(), data.readStrongBinder(), data.readInt() != 0, data.readInt() != 0);
                        reply.writeNoException();
                        parcel2.writeInt(_result21);
                        return true;
                    case 33:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result22 = killProcessRecordFast(data.readString(), data.readInt(), data.readInt(), data.readInt() != 0, data.readInt() != 0, data.readString(), data.readInt() != 0);
                        reply.writeNoException();
                        parcel2.writeInt(_result22);
                        return true;
                    case 34:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result23 = killNativeProcessRecordFast(data.readString(), data.readInt(), data.readInt(), data.readInt() != 0, data.readInt() != 0, data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result23);
                        return true;
                    case 35:
                        parcel.enforceInterface(DESCRIPTOR);
                        updateFreeFormOutLine(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 36:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result24 = getCaptionState(data.readStrongBinder());
                        reply.writeNoException();
                        parcel2.writeInt(_result24);
                        return true;
                    case 37:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result25 = getActivityWindowMode(data.readStrongBinder());
                        reply.writeNoException();
                        parcel2.writeInt(_result25);
                        return true;
                    case 38:
                        parcel.enforceInterface(DESCRIPTOR);
                        dismissSplitScreenToFocusedStack();
                        reply.writeNoException();
                        return true;
                    case 39:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (Intent) Intent.CREATOR.createFromParcel(parcel);
                        }
                        boolean _result26 = enterCoordinationMode(_arg0);
                        reply.writeNoException();
                        parcel2.writeInt(_result26);
                        return true;
                    case 40:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg5 = true;
                        }
                        boolean _result27 = exitCoordinationMode(_arg5);
                        reply.writeNoException();
                        parcel2.writeInt(_result27);
                        return true;
                    default:
                        return super.onTransact(code, data, reply, flags);
                }
            } else {
                parcel2.writeString(DESCRIPTOR);
                return true;
            }
        }
    }

    boolean canPickColor(String str) throws RemoteException;

    boolean cleanPackageRes(List<String> list, Map map, int i, boolean z, boolean z2, boolean z3) throws RemoteException;

    boolean cleanProcessResourceFast(String str, int i, IBinder iBinder, boolean z, boolean z2) throws RemoteException;

    void dismissSplitScreenToFocusedStack() throws RemoteException;

    boolean enterCoordinationMode(Intent intent) throws RemoteException;

    boolean exitCoordinationMode(boolean z) throws RemoteException;

    int getActivityWindowMode(IBinder iBinder) throws RemoteException;

    int getCaptionState(IBinder iBinder) throws RemoteException;

    ActivityInfo getLastResumedActivity() throws RemoteException;

    Rect getPCTopTaskBounds(int i) throws RemoteException;

    List<String> getPidWithUiFromUid(int i) throws RemoteException;

    boolean getProcessRecordFromMTM(ProcessInfo processInfo) throws RemoteException;

    int getTopTaskIdInDisplay(int i, String str, boolean z) throws RemoteException;

    boolean handleANRFilterFIFO(int i, int i2) throws RemoteException;

    void handleShowAppEyeAnrUi(int i, int i2, String str, String str2) throws RemoteException;

    boolean isFreeFormVisible() throws RemoteException;

    boolean isInMultiWindowMode() throws RemoteException;

    boolean isProcessExistLocked(String str, int i) throws RemoteException;

    boolean isProcessExistPidsSelfLocked(String str, int i) throws RemoteException;

    boolean isTaskSupportResize(int i, boolean z, boolean z2) throws RemoteException;

    boolean isTaskVisible(int i) throws RemoteException;

    boolean killNativeProcessRecordFast(String str, int i, int i2, boolean z, boolean z2, String str2) throws RemoteException;

    boolean killProcessRecordFast(String str, int i, int i2, boolean z, boolean z2, String str2, boolean z3) throws RemoteException;

    boolean killProcessRecordFromIAware(ProcessInfo processInfo, boolean z, boolean z2, String str, boolean z3) throws RemoteException;

    boolean killProcessRecordFromIAwareNative(ProcessInfo processInfo, boolean z, boolean z2, String str) throws RemoteException;

    boolean killProcessRecordFromMTM(ProcessInfo processInfo, boolean z, String str) throws RemoteException;

    int preloadApplication(String str, int i) throws RemoteException;

    void registerDAMonitorCallback(IHwDAMonitorCallback iHwDAMonitorCallback) throws RemoteException;

    void registerHwActivityNotifier(IHwActivityNotifier iHwActivityNotifier, String str) throws RemoteException;

    boolean registerThirdPartyCallBack(IMWThirdpartyCallback iMWThirdpartyCallback) throws RemoteException;

    void removePackageAlarm(String str, List<String> list, int i) throws RemoteException;

    void reportAssocDisable() throws RemoteException;

    void reportProcessDied(int i) throws RemoteException;

    void reportScreenRecord(int i, int i2, int i3) throws RemoteException;

    void setAndRestoreMaxAdjIfNeed(List<String> list) throws RemoteException;

    void setCpusetSwitch(boolean z) throws RemoteException;

    void setWarmColdSwitch(boolean z) throws RemoteException;

    void unregisterHwActivityNotifier(IHwActivityNotifier iHwActivityNotifier) throws RemoteException;

    boolean unregisterThirdPartyCallBack(IMWThirdpartyCallback iMWThirdpartyCallback) throws RemoteException;

    void updateFreeFormOutLine(int i) throws RemoteException;
}
