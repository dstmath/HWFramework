package android.rms;

import android.app.mtm.MultiTaskPolicy;
import android.database.IContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.rms.IProcessStateChangeObserver;
import android.rms.IUpdateWhiteListCallback;
import android.rms.config.ResourceConfig;
import android.rms.iaware.CollectData;
import android.rms.iaware.DumpData;
import android.rms.iaware.IDeviceSettingCallback;
import android.rms.iaware.IReportDataCallback;
import android.rms.iaware.NetLocationStrategy;
import android.rms.iaware.RPolicyData;
import android.rms.iaware.StatisticsData;
import android.rms.iaware.memrepair.MemRepairPkgInfo;
import java.util.ArrayList;
import java.util.List;

public interface IHwSysResManager extends IInterface {

    public static abstract class Stub extends Binder implements IHwSysResManager {
        private static final String DESCRIPTOR = "android.rms.IHwSysResManager";
        static final int TRANSACTION_acquireSysRes = 4;
        static final int TRANSACTION_clearResourceStatus = 3;
        static final int TRANSACTION_configUpdate = 11;
        static final int TRANSACTION_custConfigUpdate = 41;
        static final int TRANSACTION_disableFeature = 10;
        static final int TRANSACTION_dispatch = 6;
        static final int TRANSACTION_dispatchRPolicy = 14;
        static final int TRANSACTION_enableFeature = 9;
        static final int TRANSACTION_fetchBigDataByVersion = 18;
        static final int TRANSACTION_fetchDFTDataByVersion = 19;
        static final int TRANSACTION_getDumpData = 15;
        static final int TRANSACTION_getFrequentIM = 38;
        static final int TRANSACTION_getHabitTopN = 46;
        static final int TRANSACTION_getIAwareProtectList = 28;
        static final int TRANSACTION_getLongTimeRunningApps = 29;
        static final int TRANSACTION_getMemAvaliable = 34;
        static final int TRANSACTION_getMemRepairProcGroup = 37;
        static final int TRANSACTION_getMostFrequentUsedApps = 30;
        static final int TRANSACTION_getNetLocationStrategy = 43;
        static final int TRANSACTION_getPid = 25;
        static final int TRANSACTION_getPss = 26;
        static final int TRANSACTION_getResourceConfig = 1;
        static final int TRANSACTION_getStatisticsData = 16;
        static final int TRANSACTION_getTypeTopN = 47;
        static final int TRANSACTION_getWhiteList = 23;
        static final int TRANSACTION_init = 12;
        static final int TRANSACTION_isEnableFakeForegroundControl = 22;
        static final int TRANSACTION_isFakeForegroundProcess = 21;
        static final int TRANSACTION_isResourceNeeded = 13;
        static final int TRANSACTION_isScene = 45;
        static final int TRANSACTION_isVisibleWindow = 39;
        static final int TRANSACTION_noteProcessStart = 33;
        static final int TRANSACTION_notifyResourceStatus = 5;
        static final int TRANSACTION_recordResourceOverloadStatus = 2;
        static final int TRANSACTION_registerDevModeMethod = 48;
        static final int TRANSACTION_registerProcessStateChangeObserver = 31;
        static final int TRANSACTION_registerResourceUpdateCallback = 24;
        static final int TRANSACTION_registerSceneCallback = 44;
        static final int TRANSACTION_reportAppType = 35;
        static final int TRANSACTION_reportData = 7;
        static final int TRANSACTION_reportDataWithCallback = 8;
        static final int TRANSACTION_reportHabitData = 36;
        static final int TRANSACTION_reportSysWakeUp = 40;
        static final int TRANSACTION_requestAppClean = 42;
        static final int TRANSACTION_saveBigData = 17;
        static final int TRANSACTION_triggerUpdateWhiteList = 27;
        static final int TRANSACTION_unRegisterProcessStateChangeObserver = 32;
        static final int TRANSACTION_unregisterDevModeMethod = 49;
        static final int TRANSACTION_updateFakeForegroundList = 20;

        private static class Proxy implements IHwSysResManager {
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

            public ResourceConfig[] getResourceConfig(int resourceType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(resourceType);
                    this.mRemote.transact(1, _data, _reply, 0);
                    _reply.readException();
                    return (ResourceConfig[]) _reply.createTypedArray(ResourceConfig.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void recordResourceOverloadStatus(int uid, String pkg, int resourceType, int overloadNum, int speedOverLoadPeroid, int totalNum, Bundle args) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeString(pkg);
                    _data.writeInt(resourceType);
                    _data.writeInt(overloadNum);
                    _data.writeInt(speedOverLoadPeroid);
                    _data.writeInt(totalNum);
                    if (args != null) {
                        _data.writeInt(1);
                        args.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(2, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void clearResourceStatus(int uid, int resourceType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(resourceType);
                    this.mRemote.transact(3, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int acquireSysRes(int resourceType, Uri uri, IContentObserver observer, Bundle args) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(resourceType);
                    if (uri != null) {
                        _data.writeInt(1);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    if (args != null) {
                        _data.writeInt(1);
                        args.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void notifyResourceStatus(int resourceType, String resourceName, int resourceStatus, Bundle bd) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(resourceType);
                    _data.writeString(resourceName);
                    _data.writeInt(resourceStatus);
                    if (bd != null) {
                        _data.writeInt(1);
                        bd.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(5, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void dispatch(int resourceType, MultiTaskPolicy policy) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(resourceType);
                    if (policy != null) {
                        _data.writeInt(1);
                        policy.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(6, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void reportData(CollectData data) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(7, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void reportDataWithCallback(CollectData data, IReportDataCallback callback) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (data != null) {
                        _data.writeInt(1);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    this.mRemote.transact(8, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void enableFeature(int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    this.mRemote.transact(9, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void disableFeature(int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    this.mRemote.transact(10, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean configUpdate() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
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

            public void init(Bundle args) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (args != null) {
                        _data.writeInt(1);
                        args.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(12, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isResourceNeeded(int resourceid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(resourceid);
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

            public void dispatchRPolicy(RPolicyData policy) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (policy != null) {
                        _data.writeInt(1);
                        policy.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(14, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getDumpData(int time, List<DumpData> dumpData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(time);
                    this.mRemote.transact(15, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readTypedList(dumpData, DumpData.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public int getStatisticsData(List<StatisticsData> statisticsData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(16, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readTypedList(statisticsData, StatisticsData.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String saveBigData(int featureId, boolean clear) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(featureId);
                    _data.writeInt(clear);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String fetchBigDataByVersion(int iVer, int fId, boolean beta, boolean clear) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(iVer);
                    _data.writeInt(fId);
                    _data.writeInt(beta);
                    _data.writeInt(clear);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String fetchDFTDataByVersion(int iVer, int fId, boolean beta, boolean clear, boolean betaEncode) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(iVer);
                    _data.writeInt(fId);
                    _data.writeInt(beta);
                    _data.writeInt(clear);
                    _data.writeInt(betaEncode);
                    this.mRemote.transact(19, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void updateFakeForegroundList(List<String> processList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(processList);
                    this.mRemote.transact(20, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isFakeForegroundProcess(String process) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(process);
                    boolean _result = false;
                    this.mRemote.transact(21, _data, _reply, 0);
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

            public boolean isEnableFakeForegroundControl() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(22, _data, _reply, 0);
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

            public String getWhiteList(int resourceType, int whiteListType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(resourceType);
                    _data.writeInt(whiteListType);
                    this.mRemote.transact(23, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readString();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean registerResourceUpdateCallback(IUpdateWhiteListCallback cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    boolean _result = false;
                    this.mRemote.transact(24, _data, _reply, 0);
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

            public int getPid(String procName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(procName);
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readInt();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getPss(int pid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    this.mRemote.transact(26, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void triggerUpdateWhiteList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getIAwareProtectList(int num) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(num);
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getLongTimeRunningApps() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getMostFrequentUsedApps(int n, int minCount) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(n);
                    _data.writeInt(minCount);
                    this.mRemote.transact(30, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean registerProcessStateChangeObserver(IProcessStateChangeObserver observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
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

            public boolean unRegisterProcessStateChangeObserver(IProcessStateChangeObserver observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
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

            public void noteProcessStart(String packageName, String processName, int pid, int uid, boolean started, String launcherMode, String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(processName);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    _data.writeInt(started);
                    _data.writeString(launcherMode);
                    _data.writeString(reason);
                    this.mRemote.transact(33, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public long getMemAvaliable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(34, _data, _reply, 0);
                    _reply.readException();
                    return _reply.readLong();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void reportAppType(String pkgName, int appType, boolean status, int attr) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeInt(appType);
                    _data.writeInt(status);
                    _data.writeInt(attr);
                    this.mRemote.transact(35, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void reportHabitData(Bundle habitData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (habitData != null) {
                        _data.writeInt(1);
                        habitData.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(36, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<MemRepairPkgInfo> getMemRepairProcGroup(int sceneType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sceneType);
                    this.mRemote.transact(37, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createTypedArrayList(MemRepairPkgInfo.CREATOR);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public List<String> getFrequentIM(int count) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(count);
                    this.mRemote.transact(38, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean isVisibleWindow(int userid, String pkg, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userid);
                    _data.writeString(pkg);
                    _data.writeInt(type);
                    boolean _result = false;
                    this.mRemote.transact(39, _data, _reply, 0);
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

            public void reportSysWakeUp(String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(reason);
                    this.mRemote.transact(40, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean custConfigUpdate() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    this.mRemote.transact(41, _data, _reply, 0);
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

            public void requestAppClean(List<String> pkgNameList, int[] userIdArray, int level, String reason, int source) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(pkgNameList);
                    _data.writeIntArray(userIdArray);
                    _data.writeInt(level);
                    _data.writeString(reason);
                    _data.writeInt(source);
                    this.mRemote.transact(42, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public NetLocationStrategy getNetLocationStrategy(String pkgName, int uid, int type) throws RemoteException {
                NetLocationStrategy _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeInt(uid);
                    _data.writeInt(type);
                    this.mRemote.transact(43, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (NetLocationStrategy) NetLocationStrategy.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean registerSceneCallback(IBinder callback, int scenes) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback);
                    _data.writeInt(scenes);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_registerSceneCallback, _data, _reply, 0);
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

            public boolean isScene(int scene) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(scene);
                    boolean _result = false;
                    this.mRemote.transact(Stub.TRANSACTION_isScene, _data, _reply, 0);
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

            public List<String> getHabitTopN(int n) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(n);
                    this.mRemote.transact(Stub.TRANSACTION_getHabitTopN, _data, _reply, 0);
                    _reply.readException();
                    return _reply.createStringArrayList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public Bundle getTypeTopN(int[] appTypes) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(appTypes);
                    this.mRemote.transact(47, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void registerDevModeMethod(int deviceId, IDeviceSettingCallback callback, Bundle reserve) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(deviceId);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (reserve != null) {
                        _data.writeInt(1);
                        reserve.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(48, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void unregisterDevModeMethod(int deviceId, IDeviceSettingCallback callback, Bundle reserve) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(deviceId);
                    _data.writeStrongBinder(callback != null ? callback.asBinder() : null);
                    if (reserve != null) {
                        _data.writeInt(1);
                        reserve.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_unregisterDevModeMethod, _data, _reply, 0);
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

        public static IHwSysResManager asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IHwSysResManager)) {
                return new Proxy(obj);
            }
            return (IHwSysResManager) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v15, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v17, resolved type: android.rms.iaware.CollectData} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v19, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v22, resolved type: android.rms.iaware.CollectData} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v26, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v25, resolved type: android.rms.iaware.CollectData} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v31, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v29, resolved type: android.rms.iaware.CollectData} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v62, resolved type: java.lang.Object} */
        /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v52, resolved type: android.rms.iaware.CollectData} */
        /* JADX WARNING: Multi-variable type inference failed */
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg6;
            Uri _arg1;
            int i = code;
            Parcel parcel = data;
            Parcel parcel2 = reply;
            if (i != 1598968902) {
                boolean _arg2 = false;
                CollectData _arg0 = null;
                switch (i) {
                    case 1:
                        parcel.enforceInterface(DESCRIPTOR);
                        ResourceConfig[] _result = getResourceConfig(data.readInt());
                        reply.writeNoException();
                        parcel2.writeTypedArray(_result, 1);
                        return true;
                    case 2:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg02 = data.readInt();
                        String _arg12 = data.readString();
                        int _arg22 = data.readInt();
                        int _arg3 = data.readInt();
                        int _arg4 = data.readInt();
                        int _arg5 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg6 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg6 = null;
                        }
                        recordResourceOverloadStatus(_arg02, _arg12, _arg22, _arg3, _arg4, _arg5, _arg6);
                        reply.writeNoException();
                        return true;
                    case 3:
                        parcel.enforceInterface(DESCRIPTOR);
                        clearResourceStatus(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 4:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg03 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = (Uri) Uri.CREATOR.createFromParcel(parcel);
                        } else {
                            _arg1 = null;
                        }
                        IContentObserver _arg23 = IContentObserver.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg0 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        int _result2 = acquireSysRes(_arg03, _arg1, _arg23, _arg0);
                        reply.writeNoException();
                        parcel2.writeInt(_result2);
                        return true;
                    case 5:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg04 = data.readInt();
                        String _arg13 = data.readString();
                        int _arg24 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg0 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        notifyResourceStatus(_arg04, _arg13, _arg24, _arg0);
                        reply.writeNoException();
                        return true;
                    case 6:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg05 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg0 = (MultiTaskPolicy) MultiTaskPolicy.CREATOR.createFromParcel(parcel);
                        }
                        dispatch(_arg05, _arg0);
                        reply.writeNoException();
                        return true;
                    case 7:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = CollectData.CREATOR.createFromParcel(parcel);
                        }
                        reportData(_arg0);
                        reply.writeNoException();
                        return true;
                    case 8:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = CollectData.CREATOR.createFromParcel(parcel);
                        }
                        reportDataWithCallback(_arg0, IReportDataCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        return true;
                    case 9:
                        parcel.enforceInterface(DESCRIPTOR);
                        enableFeature(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 10:
                        parcel.enforceInterface(DESCRIPTOR);
                        disableFeature(data.readInt());
                        reply.writeNoException();
                        return true;
                    case 11:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result3 = configUpdate();
                        reply.writeNoException();
                        parcel2.writeInt(_result3);
                        return true;
                    case 12:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        init(_arg0);
                        reply.writeNoException();
                        return true;
                    case 13:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result4 = isResourceNeeded(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result4);
                        return true;
                    case 14:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (RPolicyData) RPolicyData.CREATOR.createFromParcel(parcel);
                        }
                        dispatchRPolicy(_arg0);
                        reply.writeNoException();
                        return true;
                    case 15:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg06 = data.readInt();
                        List<DumpData> _arg14 = new ArrayList<>();
                        int _result5 = getDumpData(_arg06, _arg14);
                        reply.writeNoException();
                        parcel2.writeInt(_result5);
                        parcel2.writeTypedList(_arg14);
                        return true;
                    case 16:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<StatisticsData> _arg07 = new ArrayList<>();
                        int _result6 = getStatisticsData(_arg07);
                        reply.writeNoException();
                        parcel2.writeInt(_result6);
                        parcel2.writeTypedList(_arg07);
                        return true;
                    case 17:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg08 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        String _result7 = saveBigData(_arg08, _arg2);
                        reply.writeNoException();
                        parcel2.writeString(_result7);
                        return true;
                    case 18:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg09 = data.readInt();
                        int _arg15 = data.readInt();
                        boolean _arg25 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        String _result8 = fetchBigDataByVersion(_arg09, _arg15, _arg25, _arg2);
                        reply.writeNoException();
                        parcel2.writeString(_result8);
                        return true;
                    case 19:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result9 = fetchDFTDataByVersion(data.readInt(), data.readInt(), data.readInt() != 0, data.readInt() != 0, data.readInt() != 0);
                        reply.writeNoException();
                        parcel2.writeString(_result9);
                        return true;
                    case 20:
                        parcel.enforceInterface(DESCRIPTOR);
                        updateFakeForegroundList(data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 21:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result10 = isFakeForegroundProcess(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result10);
                        return true;
                    case 22:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result11 = isEnableFakeForegroundControl();
                        reply.writeNoException();
                        parcel2.writeInt(_result11);
                        return true;
                    case 23:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _result12 = getWhiteList(data.readInt(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeString(_result12);
                        return true;
                    case 24:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result13 = registerResourceUpdateCallback(IUpdateWhiteListCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result13);
                        return true;
                    case 25:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _result14 = getPid(data.readString());
                        reply.writeNoException();
                        parcel2.writeInt(_result14);
                        return true;
                    case 26:
                        parcel.enforceInterface(DESCRIPTOR);
                        long _result15 = getPss(data.readInt());
                        reply.writeNoException();
                        parcel2.writeLong(_result15);
                        return true;
                    case 27:
                        parcel.enforceInterface(DESCRIPTOR);
                        triggerUpdateWhiteList();
                        reply.writeNoException();
                        return true;
                    case 28:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<String> _result16 = getIAwareProtectList(data.readInt());
                        reply.writeNoException();
                        parcel2.writeStringList(_result16);
                        return true;
                    case 29:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<String> _result17 = getLongTimeRunningApps();
                        reply.writeNoException();
                        parcel2.writeStringList(_result17);
                        return true;
                    case 30:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<String> _result18 = getMostFrequentUsedApps(data.readInt(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeStringList(_result18);
                        return true;
                    case 31:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result19 = registerProcessStateChangeObserver(IProcessStateChangeObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result19);
                        return true;
                    case 32:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result20 = unRegisterProcessStateChangeObserver(IProcessStateChangeObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        parcel2.writeInt(_result20);
                        return true;
                    case 33:
                        parcel.enforceInterface(DESCRIPTOR);
                        noteProcessStart(data.readString(), data.readString(), data.readInt(), data.readInt(), data.readInt() != 0, data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 34:
                        parcel.enforceInterface(DESCRIPTOR);
                        long _result21 = getMemAvaliable();
                        reply.writeNoException();
                        parcel2.writeLong(_result21);
                        return true;
                    case 35:
                        parcel.enforceInterface(DESCRIPTOR);
                        String _arg010 = data.readString();
                        int _arg16 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg2 = true;
                        }
                        reportAppType(_arg010, _arg16, _arg2, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 36:
                        parcel.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        reportHabitData(_arg0);
                        reply.writeNoException();
                        return true;
                    case 37:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<MemRepairPkgInfo> _result22 = getMemRepairProcGroup(data.readInt());
                        reply.writeNoException();
                        parcel2.writeTypedList(_result22);
                        return true;
                    case 38:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<String> _result23 = getFrequentIM(data.readInt());
                        reply.writeNoException();
                        parcel2.writeStringList(_result23);
                        return true;
                    case 39:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result24 = isVisibleWindow(data.readInt(), data.readString(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result24);
                        return true;
                    case 40:
                        parcel.enforceInterface(DESCRIPTOR);
                        reportSysWakeUp(data.readString());
                        reply.writeNoException();
                        return true;
                    case 41:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result25 = custConfigUpdate();
                        reply.writeNoException();
                        parcel2.writeInt(_result25);
                        return true;
                    case 42:
                        parcel.enforceInterface(DESCRIPTOR);
                        requestAppClean(data.createStringArrayList(), data.createIntArray(), data.readInt(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 43:
                        parcel.enforceInterface(DESCRIPTOR);
                        NetLocationStrategy _result26 = getNetLocationStrategy(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result26 != null) {
                            parcel2.writeInt(1);
                            _result26.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case TRANSACTION_registerSceneCallback /*44*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result27 = registerSceneCallback(data.readStrongBinder(), data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result27);
                        return true;
                    case TRANSACTION_isScene /*45*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        boolean _result28 = isScene(data.readInt());
                        reply.writeNoException();
                        parcel2.writeInt(_result28);
                        return true;
                    case TRANSACTION_getHabitTopN /*46*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        List<String> _result29 = getHabitTopN(data.readInt());
                        reply.writeNoException();
                        parcel2.writeStringList(_result29);
                        return true;
                    case 47:
                        parcel.enforceInterface(DESCRIPTOR);
                        Bundle _result30 = getTypeTopN(data.createIntArray());
                        reply.writeNoException();
                        if (_result30 != null) {
                            parcel2.writeInt(1);
                            _result30.writeToParcel(parcel2, 1);
                        } else {
                            parcel2.writeInt(0);
                        }
                        return true;
                    case 48:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg011 = data.readInt();
                        IDeviceSettingCallback _arg17 = IDeviceSettingCallback.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg0 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        registerDevModeMethod(_arg011, _arg17, _arg0);
                        reply.writeNoException();
                        return true;
                    case TRANSACTION_unregisterDevModeMethod /*49*/:
                        parcel.enforceInterface(DESCRIPTOR);
                        int _arg012 = data.readInt();
                        IDeviceSettingCallback _arg18 = IDeviceSettingCallback.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg0 = (Bundle) Bundle.CREATOR.createFromParcel(parcel);
                        }
                        unregisterDevModeMethod(_arg012, _arg18, _arg0);
                        reply.writeNoException();
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

    int acquireSysRes(int i, Uri uri, IContentObserver iContentObserver, Bundle bundle) throws RemoteException;

    void clearResourceStatus(int i, int i2) throws RemoteException;

    boolean configUpdate() throws RemoteException;

    boolean custConfigUpdate() throws RemoteException;

    void disableFeature(int i) throws RemoteException;

    void dispatch(int i, MultiTaskPolicy multiTaskPolicy) throws RemoteException;

    void dispatchRPolicy(RPolicyData rPolicyData) throws RemoteException;

    void enableFeature(int i) throws RemoteException;

    String fetchBigDataByVersion(int i, int i2, boolean z, boolean z2) throws RemoteException;

    String fetchDFTDataByVersion(int i, int i2, boolean z, boolean z2, boolean z3) throws RemoteException;

    int getDumpData(int i, List<DumpData> list) throws RemoteException;

    List<String> getFrequentIM(int i) throws RemoteException;

    List<String> getHabitTopN(int i) throws RemoteException;

    List<String> getIAwareProtectList(int i) throws RemoteException;

    List<String> getLongTimeRunningApps() throws RemoteException;

    long getMemAvaliable() throws RemoteException;

    List<MemRepairPkgInfo> getMemRepairProcGroup(int i) throws RemoteException;

    List<String> getMostFrequentUsedApps(int i, int i2) throws RemoteException;

    NetLocationStrategy getNetLocationStrategy(String str, int i, int i2) throws RemoteException;

    int getPid(String str) throws RemoteException;

    long getPss(int i) throws RemoteException;

    ResourceConfig[] getResourceConfig(int i) throws RemoteException;

    int getStatisticsData(List<StatisticsData> list) throws RemoteException;

    Bundle getTypeTopN(int[] iArr) throws RemoteException;

    String getWhiteList(int i, int i2) throws RemoteException;

    void init(Bundle bundle) throws RemoteException;

    boolean isEnableFakeForegroundControl() throws RemoteException;

    boolean isFakeForegroundProcess(String str) throws RemoteException;

    boolean isResourceNeeded(int i) throws RemoteException;

    boolean isScene(int i) throws RemoteException;

    boolean isVisibleWindow(int i, String str, int i2) throws RemoteException;

    void noteProcessStart(String str, String str2, int i, int i2, boolean z, String str3, String str4) throws RemoteException;

    void notifyResourceStatus(int i, String str, int i2, Bundle bundle) throws RemoteException;

    void recordResourceOverloadStatus(int i, String str, int i2, int i3, int i4, int i5, Bundle bundle) throws RemoteException;

    void registerDevModeMethod(int i, IDeviceSettingCallback iDeviceSettingCallback, Bundle bundle) throws RemoteException;

    boolean registerProcessStateChangeObserver(IProcessStateChangeObserver iProcessStateChangeObserver) throws RemoteException;

    boolean registerResourceUpdateCallback(IUpdateWhiteListCallback iUpdateWhiteListCallback) throws RemoteException;

    boolean registerSceneCallback(IBinder iBinder, int i) throws RemoteException;

    void reportAppType(String str, int i, boolean z, int i2) throws RemoteException;

    void reportData(CollectData collectData) throws RemoteException;

    void reportDataWithCallback(CollectData collectData, IReportDataCallback iReportDataCallback) throws RemoteException;

    void reportHabitData(Bundle bundle) throws RemoteException;

    void reportSysWakeUp(String str) throws RemoteException;

    void requestAppClean(List<String> list, int[] iArr, int i, String str, int i2) throws RemoteException;

    String saveBigData(int i, boolean z) throws RemoteException;

    void triggerUpdateWhiteList() throws RemoteException;

    boolean unRegisterProcessStateChangeObserver(IProcessStateChangeObserver iProcessStateChangeObserver) throws RemoteException;

    void unregisterDevModeMethod(int i, IDeviceSettingCallback iDeviceSettingCallback, Bundle bundle) throws RemoteException;

    void updateFakeForegroundList(List<String> list) throws RemoteException;
}
