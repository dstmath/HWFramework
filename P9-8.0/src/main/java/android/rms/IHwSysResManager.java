package android.rms;

import android.app.mtm.MultiTaskPolicy;
import android.content.pm.ParceledListSlice;
import android.database.IContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.rms.config.ResourceConfig;
import android.rms.iaware.CollectData;
import android.rms.iaware.DumpData;
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
        static final int TRANSACTION_custConfigUpdate = 39;
        static final int TRANSACTION_disableFeature = 10;
        static final int TRANSACTION_dispatch = 6;
        static final int TRANSACTION_dispatchRPolicy = 14;
        static final int TRANSACTION_enableFeature = 9;
        static final int TRANSACTION_fetchBigDataByVersion = 18;
        static final int TRANSACTION_getDumpData = 15;
        static final int TRANSACTION_getFrequentIM = 37;
        static final int TRANSACTION_getIAwareProtectList = 27;
        static final int TRANSACTION_getLongTimeRunningApps = 28;
        static final int TRANSACTION_getMemAvaliable = 33;
        static final int TRANSACTION_getMemRepairProcGroup = 36;
        static final int TRANSACTION_getMostFrequentUsedApps = 29;
        static final int TRANSACTION_getNetLocationStrategy = 38;
        static final int TRANSACTION_getPid = 24;
        static final int TRANSACTION_getPss = 25;
        static final int TRANSACTION_getResourceConfig = 1;
        static final int TRANSACTION_getStatisticsData = 16;
        static final int TRANSACTION_getWhiteList = 22;
        static final int TRANSACTION_init = 12;
        static final int TRANSACTION_isEnableFakeForegroundControl = 21;
        static final int TRANSACTION_isFakeForegroundProcess = 20;
        static final int TRANSACTION_isResourceNeeded = 13;
        static final int TRANSACTION_noteProcessStart = 32;
        static final int TRANSACTION_notifyResourceStatus = 5;
        static final int TRANSACTION_recordResourceOverloadStatus = 2;
        static final int TRANSACTION_registerProcessStateChangeObserver = 30;
        static final int TRANSACTION_registerResourceUpdateCallback = 23;
        static final int TRANSACTION_reportAppType = 34;
        static final int TRANSACTION_reportData = 7;
        static final int TRANSACTION_reportDataWithCallback = 8;
        static final int TRANSACTION_reportHabitData = 35;
        static final int TRANSACTION_saveBigData = 17;
        static final int TRANSACTION_triggerUpdateWhiteList = 26;
        static final int TRANSACTION_unRegisterProcessStateChangeObserver = 31;
        static final int TRANSACTION_updateFakeForegroundList = 19;

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
                    ResourceConfig[] _result = (ResourceConfig[]) _reply.createTypedArray(ResourceConfig.CREATOR);
                    return _result;
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
                IBinder iBinder = null;
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
                    if (observer != null) {
                        iBinder = observer.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (args != null) {
                        _data.writeInt(1);
                        args.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(4, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                IBinder iBinder = null;
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
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
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
                    this.mRemote.transact(11, _data, _reply, 0);
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
                    this.mRemote.transact(13, _data, _reply, 0);
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
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(featureId);
                    if (clear) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(17, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public String fetchBigDataByVersion(int iVer, int fId, boolean beta, boolean clear) throws RemoteException {
                int i = 1;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    int i2;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(iVer);
                    _data.writeInt(fId);
                    if (beta) {
                        i2 = 1;
                    } else {
                        i2 = 0;
                    }
                    _data.writeInt(i2);
                    if (!clear) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(18, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
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
                    this.mRemote.transact(19, _data, _reply, 0);
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
                    this.mRemote.transact(20, _data, _reply, 0);
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

            public boolean isEnableFakeForegroundControl() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(21, _data, _reply, 0);
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

            public String getWhiteList(int resourceType, int whiteListType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(resourceType);
                    _data.writeInt(whiteListType);
                    this.mRemote.transact(22, _data, _reply, 0);
                    _reply.readException();
                    String _result = _reply.readString();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean registerResourceUpdateCallback(IUpdateWhiteListCallback cb) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (cb != null) {
                        iBinder = cb.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(23, _data, _reply, 0);
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

            public int getPid(String procName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(procName);
                    this.mRemote.transact(24, _data, _reply, 0);
                    _reply.readException();
                    int _result = _reply.readInt();
                    return _result;
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
                    this.mRemote.transact(25, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
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
                    this.mRemote.transact(26, _data, _reply, 0);
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
                    this.mRemote.transact(27, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
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
                    this.mRemote.transact(28, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
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
                    this.mRemote.transact(29, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean registerProcessStateChangeObserver(IProcessStateChangeObserver observer) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (observer != null) {
                        iBinder = observer.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(30, _data, _reply, 0);
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

            public boolean unRegisterProcessStateChangeObserver(IProcessStateChangeObserver observer) throws RemoteException {
                IBinder iBinder = null;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (observer != null) {
                        iBinder = observer.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(31, _data, _reply, 0);
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

            public void noteProcessStart(String packageName, String processName, int pid, int uid, boolean started, String launcherMode, String reason) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeString(processName);
                    _data.writeInt(pid);
                    _data.writeInt(uid);
                    if (started) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    _data.writeString(launcherMode);
                    _data.writeString(reason);
                    this.mRemote.transact(32, _data, _reply, 0);
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
                    this.mRemote.transact(33, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void reportAppType(String pkgName, int appType, boolean status, int attr) throws RemoteException {
                int i = 0;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeInt(appType);
                    if (status) {
                        i = 1;
                    }
                    _data.writeInt(i);
                    _data.writeInt(attr);
                    this.mRemote.transact(34, _data, _reply, 0);
                    _reply.readException();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void reportHabitData(ParceledListSlice habitData) throws RemoteException {
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
                    this.mRemote.transact(35, _data, _reply, 0);
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
                    this.mRemote.transact(36, _data, _reply, 0);
                    _reply.readException();
                    List<MemRepairPkgInfo> _result = _reply.createTypedArrayList(MemRepairPkgInfo.CREATOR);
                    return _result;
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
                    this.mRemote.transact(Stub.TRANSACTION_getFrequentIM, _data, _reply, 0);
                    _reply.readException();
                    List<String> _result = _reply.createStringArrayList();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public NetLocationStrategy getNetLocationStrategy(String pkgName, int uid, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    NetLocationStrategy _result;
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeInt(uid);
                    _data.writeInt(type);
                    this.mRemote.transact(Stub.TRANSACTION_getNetLocationStrategy, _data, _reply, 0);
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (NetLocationStrategy) NetLocationStrategy.CREATOR.createFromParcel(_reply);
                    } else {
                        _result = null;
                    }
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } catch (Throwable th) {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public boolean custConfigUpdate() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    this.mRemote.transact(Stub.TRANSACTION_custConfigUpdate, _data, _reply, 0);
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

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            int _arg0;
            String _arg1;
            int _arg2;
            Bundle _arg3;
            int _result;
            CollectData _arg02;
            boolean _result2;
            String _result3;
            long _result4;
            List<String> _result5;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    ResourceConfig[] _result6 = getResourceConfig(data.readInt());
                    reply.writeNoException();
                    reply.writeTypedArray(_result6, 1);
                    return true;
                case 2:
                    Bundle _arg6;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    _arg1 = data.readString();
                    _arg2 = data.readInt();
                    int _arg32 = data.readInt();
                    int _arg4 = data.readInt();
                    int _arg5 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg6 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg6 = null;
                    }
                    recordResourceOverloadStatus(_arg0, _arg1, _arg2, _arg32, _arg4, _arg5, _arg6);
                    reply.writeNoException();
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    clearResourceStatus(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case 4:
                    Uri _arg12;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg12 = (Uri) Uri.CREATOR.createFromParcel(data);
                    } else {
                        _arg12 = null;
                    }
                    IContentObserver _arg22 = android.database.IContentObserver.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        _arg3 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg3 = null;
                    }
                    _result = acquireSysRes(_arg0, _arg12, _arg22, _arg3);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    _arg1 = data.readString();
                    _arg2 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg3 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg3 = null;
                    }
                    notifyResourceStatus(_arg0, _arg1, _arg2, _arg3);
                    reply.writeNoException();
                    return true;
                case 6:
                    MultiTaskPolicy _arg13;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg13 = (MultiTaskPolicy) MultiTaskPolicy.CREATOR.createFromParcel(data);
                    } else {
                        _arg13 = null;
                    }
                    dispatch(_arg0, _arg13);
                    reply.writeNoException();
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg02 = (CollectData) CollectData.CREATOR.createFromParcel(data);
                    } else {
                        _arg02 = null;
                    }
                    reportData(_arg02);
                    reply.writeNoException();
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg02 = (CollectData) CollectData.CREATOR.createFromParcel(data);
                    } else {
                        _arg02 = null;
                    }
                    reportDataWithCallback(_arg02, android.rms.iaware.IReportDataCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    enableFeature(data.readInt());
                    reply.writeNoException();
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    disableFeature(data.readInt());
                    reply.writeNoException();
                    return true;
                case 11:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = configUpdate();
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 12:
                    Bundle _arg03;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg03 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        _arg03 = null;
                    }
                    init(_arg03);
                    reply.writeNoException();
                    return true;
                case 13:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = isResourceNeeded(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 14:
                    RPolicyData _arg04;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg04 = (RPolicyData) RPolicyData.CREATOR.createFromParcel(data);
                    } else {
                        _arg04 = null;
                    }
                    dispatchRPolicy(_arg04);
                    reply.writeNoException();
                    return true;
                case 15:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    List<DumpData> _arg14 = new ArrayList();
                    _result = getDumpData(_arg0, _arg14);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    reply.writeTypedList(_arg14);
                    return true;
                case 16:
                    data.enforceInterface(DESCRIPTOR);
                    List<StatisticsData> _arg05 = new ArrayList();
                    _result = getStatisticsData(_arg05);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    reply.writeTypedList(_arg05);
                    return true;
                case 17:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = saveBigData(data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeString(_result3);
                    return true;
                case 18:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = fetchBigDataByVersion(data.readInt(), data.readInt(), data.readInt() != 0, data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeString(_result3);
                    return true;
                case 19:
                    data.enforceInterface(DESCRIPTOR);
                    updateFakeForegroundList(data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case 20:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = isFakeForegroundProcess(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 21:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = isEnableFakeForegroundControl();
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 22:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getWhiteList(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeString(_result3);
                    return true;
                case 23:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = registerResourceUpdateCallback(android.rms.IUpdateWhiteListCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 24:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getPid(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case 25:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getPss(data.readInt());
                    reply.writeNoException();
                    reply.writeLong(_result4);
                    return true;
                case 26:
                    data.enforceInterface(DESCRIPTOR);
                    triggerUpdateWhiteList();
                    reply.writeNoException();
                    return true;
                case 27:
                    data.enforceInterface(DESCRIPTOR);
                    _result5 = getIAwareProtectList(data.readInt());
                    reply.writeNoException();
                    reply.writeStringList(_result5);
                    return true;
                case 28:
                    data.enforceInterface(DESCRIPTOR);
                    _result5 = getLongTimeRunningApps();
                    reply.writeNoException();
                    reply.writeStringList(_result5);
                    return true;
                case 29:
                    data.enforceInterface(DESCRIPTOR);
                    _result5 = getMostFrequentUsedApps(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeStringList(_result5);
                    return true;
                case 30:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = registerProcessStateChangeObserver(android.rms.IProcessStateChangeObserver.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 31:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = unRegisterProcessStateChangeObserver(android.rms.IProcessStateChangeObserver.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 32:
                    data.enforceInterface(DESCRIPTOR);
                    noteProcessStart(data.readString(), data.readString(), data.readInt(), data.readInt(), data.readInt() != 0, data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case 33:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getMemAvaliable();
                    reply.writeNoException();
                    reply.writeLong(_result4);
                    return true;
                case 34:
                    data.enforceInterface(DESCRIPTOR);
                    reportAppType(data.readString(), data.readInt(), data.readInt() != 0, data.readInt());
                    reply.writeNoException();
                    return true;
                case 35:
                    ParceledListSlice _arg06;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        _arg06 = (ParceledListSlice) ParceledListSlice.CREATOR.createFromParcel(data);
                    } else {
                        _arg06 = null;
                    }
                    reportHabitData(_arg06);
                    reply.writeNoException();
                    return true;
                case 36:
                    data.enforceInterface(DESCRIPTOR);
                    List<MemRepairPkgInfo> _result7 = getMemRepairProcGroup(data.readInt());
                    reply.writeNoException();
                    reply.writeTypedList(_result7);
                    return true;
                case TRANSACTION_getFrequentIM /*37*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result5 = getFrequentIM(data.readInt());
                    reply.writeNoException();
                    reply.writeStringList(_result5);
                    return true;
                case TRANSACTION_getNetLocationStrategy /*38*/:
                    data.enforceInterface(DESCRIPTOR);
                    NetLocationStrategy _result8 = getNetLocationStrategy(data.readString(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    if (_result8 != null) {
                        reply.writeInt(1);
                        _result8.writeToParcel(reply, 1);
                    } else {
                        reply.writeInt(0);
                    }
                    return true;
                case TRANSACTION_custConfigUpdate /*39*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = custConfigUpdate();
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
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

    int getDumpData(int i, List<DumpData> list) throws RemoteException;

    List<String> getFrequentIM(int i) throws RemoteException;

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

    String getWhiteList(int i, int i2) throws RemoteException;

    void init(Bundle bundle) throws RemoteException;

    boolean isEnableFakeForegroundControl() throws RemoteException;

    boolean isFakeForegroundProcess(String str) throws RemoteException;

    boolean isResourceNeeded(int i) throws RemoteException;

    void noteProcessStart(String str, String str2, int i, int i2, boolean z, String str3, String str4) throws RemoteException;

    void notifyResourceStatus(int i, String str, int i2, Bundle bundle) throws RemoteException;

    void recordResourceOverloadStatus(int i, String str, int i2, int i3, int i4, int i5, Bundle bundle) throws RemoteException;

    boolean registerProcessStateChangeObserver(IProcessStateChangeObserver iProcessStateChangeObserver) throws RemoteException;

    boolean registerResourceUpdateCallback(IUpdateWhiteListCallback iUpdateWhiteListCallback) throws RemoteException;

    void reportAppType(String str, int i, boolean z, int i2) throws RemoteException;

    void reportData(CollectData collectData) throws RemoteException;

    void reportDataWithCallback(CollectData collectData, IReportDataCallback iReportDataCallback) throws RemoteException;

    void reportHabitData(ParceledListSlice parceledListSlice) throws RemoteException;

    String saveBigData(int i, boolean z) throws RemoteException;

    void triggerUpdateWhiteList() throws RemoteException;

    boolean unRegisterProcessStateChangeObserver(IProcessStateChangeObserver iProcessStateChangeObserver) throws RemoteException;

    void updateFakeForegroundList(List<String> list) throws RemoteException;
}
