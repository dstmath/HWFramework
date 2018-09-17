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
import android.rms.config.ResourceConfig;
import android.rms.iaware.CollectData;
import android.rms.iaware.DumpData;
import android.rms.iaware.IReportDataCallback;
import android.rms.iaware.RPolicyData;
import android.rms.iaware.StatisticsData;
import java.util.ArrayList;
import java.util.List;

public interface IHwSysResManager extends IInterface {

    public static abstract class Stub extends Binder implements IHwSysResManager {
        private static final String DESCRIPTOR = "android.rms.IHwSysResManager";
        static final int TRANSACTION_acquireSysRes = 4;
        static final int TRANSACTION_clearResourceStatus = 3;
        static final int TRANSACTION_configUpdate = 11;
        static final int TRANSACTION_disableFeature = 10;
        static final int TRANSACTION_dispatch = 6;
        static final int TRANSACTION_dispatchRPolicy = 14;
        static final int TRANSACTION_enableFeature = 9;
        static final int TRANSACTION_getDumpData = 15;
        static final int TRANSACTION_getIAwareProtectList = 26;
        static final int TRANSACTION_getLongTimeRunningApps = 27;
        static final int TRANSACTION_getMemAvaliable = 32;
        static final int TRANSACTION_getMostFrequentUsedApps = 28;
        static final int TRANSACTION_getPid = 23;
        static final int TRANSACTION_getPss = 24;
        static final int TRANSACTION_getResourceConfig = 1;
        static final int TRANSACTION_getStatisticsData = 16;
        static final int TRANSACTION_getWhiteList = 21;
        static final int TRANSACTION_init = 12;
        static final int TRANSACTION_isEnableFakeForegroundControl = 20;
        static final int TRANSACTION_isFakeForegroundProcess = 19;
        static final int TRANSACTION_isResourceNeeded = 13;
        static final int TRANSACTION_noteProcessStart = 31;
        static final int TRANSACTION_notifyResourceStatus = 5;
        static final int TRANSACTION_recordResourceOverloadStatus = 2;
        static final int TRANSACTION_registerProcessStateChangeObserver = 29;
        static final int TRANSACTION_registerResourceUpdateCallback = 22;
        static final int TRANSACTION_reportData = 7;
        static final int TRANSACTION_reportDataWithCallback = 8;
        static final int TRANSACTION_saveBigData = 17;
        static final int TRANSACTION_triggerUpdateWhiteList = 25;
        static final int TRANSACTION_unRegisterProcessStateChangeObserver = 30;
        static final int TRANSACTION_updateFakeForegroundList = 18;

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
                    this.mRemote.transact(Stub.TRANSACTION_getResourceConfig, _data, _reply, 0);
                    _reply.readException();
                    ResourceConfig[] _result = (ResourceConfig[]) _reply.createTypedArray(ResourceConfig.CREATOR);
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            public void recordResourceOverloadStatus(int uid, String pkg, int resourceType, int speedOverloadNum, int speedOverLoadPeroid, int countOverLoadNum) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeString(pkg);
                    _data.writeInt(resourceType);
                    _data.writeInt(speedOverloadNum);
                    _data.writeInt(speedOverLoadPeroid);
                    _data.writeInt(countOverLoadNum);
                    this.mRemote.transact(Stub.TRANSACTION_recordResourceOverloadStatus, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_clearResourceStatus, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_getResourceConfig);
                        uri.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (observer != null) {
                        iBinder = observer.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    if (args != null) {
                        _data.writeInt(Stub.TRANSACTION_getResourceConfig);
                        args.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_acquireSysRes, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_getResourceConfig);
                        bd.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_notifyResourceStatus, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_getResourceConfig);
                        policy.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_dispatch, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_getResourceConfig);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_reportData, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_getResourceConfig);
                        data.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (callback != null) {
                        iBinder = callback.asBinder();
                    }
                    _data.writeStrongBinder(iBinder);
                    this.mRemote.transact(Stub.TRANSACTION_reportDataWithCallback, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_enableFeature, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_disableFeature, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_configUpdate, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_getResourceConfig);
                        args.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_init, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_isResourceNeeded, _data, _reply, 0);
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
                        _data.writeInt(Stub.TRANSACTION_getResourceConfig);
                        policy.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    this.mRemote.transact(Stub.TRANSACTION_dispatchRPolicy, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getDumpData, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getStatisticsData, _data, _reply, 0);
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
                        i = Stub.TRANSACTION_getResourceConfig;
                    }
                    _data.writeInt(i);
                    this.mRemote.transact(Stub.TRANSACTION_saveBigData, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_updateFakeForegroundList, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_isFakeForegroundProcess, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_isEnableFakeForegroundControl, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getWhiteList, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_registerResourceUpdateCallback, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getPid, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getPss, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_triggerUpdateWhiteList, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getIAwareProtectList, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getLongTimeRunningApps, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getMostFrequentUsedApps, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_registerProcessStateChangeObserver, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_unRegisterProcessStateChangeObserver, _data, _reply, 0);
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
                        i = Stub.TRANSACTION_getResourceConfig;
                    }
                    _data.writeInt(i);
                    _data.writeString(launcherMode);
                    _data.writeString(reason);
                    this.mRemote.transact(Stub.TRANSACTION_noteProcessStart, _data, _reply, 0);
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
                    this.mRemote.transact(Stub.TRANSACTION_getMemAvaliable, _data, _reply, 0);
                    _reply.readException();
                    long _result = _reply.readLong();
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
            Bundle bundle;
            int _result;
            CollectData collectData;
            boolean _result2;
            String _result3;
            long _result4;
            List<String> _result5;
            switch (code) {
                case TRANSACTION_getResourceConfig /*1*/:
                    data.enforceInterface(DESCRIPTOR);
                    ResourceConfig[] _result6 = getResourceConfig(data.readInt());
                    reply.writeNoException();
                    reply.writeTypedArray(_result6, TRANSACTION_getResourceConfig);
                    return true;
                case TRANSACTION_recordResourceOverloadStatus /*2*/:
                    data.enforceInterface(DESCRIPTOR);
                    recordResourceOverloadStatus(data.readInt(), data.readString(), data.readInt(), data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_clearResourceStatus /*3*/:
                    data.enforceInterface(DESCRIPTOR);
                    clearResourceStatus(data.readInt(), data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_acquireSysRes /*4*/:
                    Uri uri;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        uri = (Uri) Uri.CREATOR.createFromParcel(data);
                    } else {
                        uri = null;
                    }
                    IContentObserver _arg2 = android.database.IContentObserver.Stub.asInterface(data.readStrongBinder());
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    _result = acquireSysRes(_arg0, uri, _arg2, bundle);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_notifyResourceStatus /*5*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    String _arg1 = data.readString();
                    int _arg22 = data.readInt();
                    if (data.readInt() != 0) {
                        bundle = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle = null;
                    }
                    notifyResourceStatus(_arg0, _arg1, _arg22, bundle);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_dispatch /*6*/:
                    MultiTaskPolicy multiTaskPolicy;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    if (data.readInt() != 0) {
                        multiTaskPolicy = (MultiTaskPolicy) MultiTaskPolicy.CREATOR.createFromParcel(data);
                    } else {
                        multiTaskPolicy = null;
                    }
                    dispatch(_arg0, multiTaskPolicy);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_reportData /*7*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        collectData = (CollectData) CollectData.CREATOR.createFromParcel(data);
                    } else {
                        collectData = null;
                    }
                    reportData(collectData);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_reportDataWithCallback /*8*/:
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        collectData = (CollectData) CollectData.CREATOR.createFromParcel(data);
                    } else {
                        collectData = null;
                    }
                    reportDataWithCallback(collectData, android.rms.iaware.IReportDataCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    return true;
                case TRANSACTION_enableFeature /*9*/:
                    data.enforceInterface(DESCRIPTOR);
                    enableFeature(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_disableFeature /*10*/:
                    data.enforceInterface(DESCRIPTOR);
                    disableFeature(data.readInt());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_configUpdate /*11*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = configUpdate();
                    reply.writeNoException();
                    reply.writeInt(_result2 ? TRANSACTION_getResourceConfig : 0);
                    return true;
                case TRANSACTION_init /*12*/:
                    Bundle bundle2;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        bundle2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                    } else {
                        bundle2 = null;
                    }
                    init(bundle2);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isResourceNeeded /*13*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = isResourceNeeded(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? TRANSACTION_getResourceConfig : 0);
                    return true;
                case TRANSACTION_dispatchRPolicy /*14*/:
                    RPolicyData rPolicyData;
                    data.enforceInterface(DESCRIPTOR);
                    if (data.readInt() != 0) {
                        rPolicyData = (RPolicyData) RPolicyData.CREATOR.createFromParcel(data);
                    } else {
                        rPolicyData = null;
                    }
                    dispatchRPolicy(rPolicyData);
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getDumpData /*15*/:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    List<DumpData> _arg12 = new ArrayList();
                    _result = getDumpData(_arg0, _arg12);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    reply.writeTypedList(_arg12);
                    return true;
                case TRANSACTION_getStatisticsData /*16*/:
                    data.enforceInterface(DESCRIPTOR);
                    List<StatisticsData> _arg02 = new ArrayList();
                    _result = getStatisticsData(_arg02);
                    reply.writeNoException();
                    reply.writeInt(_result);
                    reply.writeTypedList(_arg02);
                    return true;
                case TRANSACTION_saveBigData /*17*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = saveBigData(data.readInt(), data.readInt() != 0);
                    reply.writeNoException();
                    reply.writeString(_result3);
                    return true;
                case TRANSACTION_updateFakeForegroundList /*18*/:
                    data.enforceInterface(DESCRIPTOR);
                    updateFakeForegroundList(data.createStringArrayList());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_isFakeForegroundProcess /*19*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = isFakeForegroundProcess(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? TRANSACTION_getResourceConfig : 0);
                    return true;
                case TRANSACTION_isEnableFakeForegroundControl /*20*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = isEnableFakeForegroundControl();
                    reply.writeNoException();
                    reply.writeInt(_result2 ? TRANSACTION_getResourceConfig : 0);
                    return true;
                case TRANSACTION_getWhiteList /*21*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getWhiteList(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeString(_result3);
                    return true;
                case TRANSACTION_registerResourceUpdateCallback /*22*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = registerResourceUpdateCallback(android.rms.IUpdateWhiteListCallback.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result2 ? TRANSACTION_getResourceConfig : 0);
                    return true;
                case TRANSACTION_getPid /*23*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getPid(data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result);
                    return true;
                case TRANSACTION_getPss /*24*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getPss(data.readInt());
                    reply.writeNoException();
                    reply.writeLong(_result4);
                    return true;
                case TRANSACTION_triggerUpdateWhiteList /*25*/:
                    data.enforceInterface(DESCRIPTOR);
                    triggerUpdateWhiteList();
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getIAwareProtectList /*26*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result5 = getIAwareProtectList(data.readInt());
                    reply.writeNoException();
                    reply.writeStringList(_result5);
                    return true;
                case TRANSACTION_getLongTimeRunningApps /*27*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result5 = getLongTimeRunningApps();
                    reply.writeNoException();
                    reply.writeStringList(_result5);
                    return true;
                case TRANSACTION_getMostFrequentUsedApps /*28*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result5 = getMostFrequentUsedApps(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeStringList(_result5);
                    return true;
                case TRANSACTION_registerProcessStateChangeObserver /*29*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = registerProcessStateChangeObserver(android.rms.IProcessStateChangeObserver.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result2 ? TRANSACTION_getResourceConfig : 0);
                    return true;
                case TRANSACTION_unRegisterProcessStateChangeObserver /*30*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = unRegisterProcessStateChangeObserver(android.rms.IProcessStateChangeObserver.Stub.asInterface(data.readStrongBinder()));
                    reply.writeNoException();
                    reply.writeInt(_result2 ? TRANSACTION_getResourceConfig : 0);
                    return true;
                case TRANSACTION_noteProcessStart /*31*/:
                    data.enforceInterface(DESCRIPTOR);
                    noteProcessStart(data.readString(), data.readString(), data.readInt(), data.readInt(), data.readInt() != 0, data.readString(), data.readString());
                    reply.writeNoException();
                    return true;
                case TRANSACTION_getMemAvaliable /*32*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getMemAvaliable();
                    reply.writeNoException();
                    reply.writeLong(_result4);
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

    void disableFeature(int i) throws RemoteException;

    void dispatch(int i, MultiTaskPolicy multiTaskPolicy) throws RemoteException;

    void dispatchRPolicy(RPolicyData rPolicyData) throws RemoteException;

    void enableFeature(int i) throws RemoteException;

    int getDumpData(int i, List<DumpData> list) throws RemoteException;

    List<String> getIAwareProtectList(int i) throws RemoteException;

    List<String> getLongTimeRunningApps() throws RemoteException;

    long getMemAvaliable() throws RemoteException;

    List<String> getMostFrequentUsedApps(int i, int i2) throws RemoteException;

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

    void recordResourceOverloadStatus(int i, String str, int i2, int i3, int i4, int i5) throws RemoteException;

    boolean registerProcessStateChangeObserver(IProcessStateChangeObserver iProcessStateChangeObserver) throws RemoteException;

    boolean registerResourceUpdateCallback(IUpdateWhiteListCallback iUpdateWhiteListCallback) throws RemoteException;

    void reportData(CollectData collectData) throws RemoteException;

    void reportDataWithCallback(CollectData collectData, IReportDataCallback iReportDataCallback) throws RemoteException;

    String saveBigData(int i, boolean z) throws RemoteException;

    void triggerUpdateWhiteList() throws RemoteException;

    boolean unRegisterProcessStateChangeObserver(IProcessStateChangeObserver iProcessStateChangeObserver) throws RemoteException;

    void updateFakeForegroundList(List<String> list) throws RemoteException;
}
