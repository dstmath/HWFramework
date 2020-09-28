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
    int acquireSysRes(int i, Uri uri, IContentObserver iContentObserver, Bundle bundle) throws RemoteException;

    void clearResourceStatus(int i, int i2) throws RemoteException;

    boolean configUpdate() throws RemoteException;

    boolean custConfigUpdate() throws RemoteException;

    void disableFeature(int i) throws RemoteException;

    void dispatch(int i, MultiTaskPolicy multiTaskPolicy) throws RemoteException;

    void dispatchRPolicy(RPolicyData rPolicyData) throws RemoteException;

    void enableFeature(int i) throws RemoteException;

    String fetchBigDataByVersion(int i, int i2, boolean z, boolean z2) throws RemoteException;

    void fetchDftDataByVersion(Bundle bundle) throws RemoteException;

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

    boolean isVisibleWindow(int i, String str, int i2) throws RemoteException;

    boolean isZApp(String str, int i) throws RemoteException;

    void noteProcessStart(String str, String str2, int i, int i2, boolean z, String str3, String str4) throws RemoteException;

    void notifyResourceStatus(int i, String str, int i2, Bundle bundle) throws RemoteException;

    boolean preloadAppForLauncher(String str, int i, int i2) throws RemoteException;

    void recordResourceOverloadStatus(int i, String str, int i2, int i3, int i4, int i5, Bundle bundle) throws RemoteException;

    void registerDevModeMethod(int i, IDeviceSettingCallback iDeviceSettingCallback, Bundle bundle) throws RemoteException;

    boolean registerProcessStateChangeObserver(IProcessStateChangeObserver iProcessStateChangeObserver) throws RemoteException;

    boolean registerResourceUpdateCallback(IUpdateWhiteListCallback iUpdateWhiteListCallback) throws RemoteException;

    boolean registerSceneCallback(IBinder iBinder, int i) throws RemoteException;

    void reportAppType(String str, int i, boolean z, int i2) throws RemoteException;

    void reportCloudUpdate(Bundle bundle) throws RemoteException;

    void reportData(CollectData collectData) throws RemoteException;

    void reportDataWithCallback(CollectData collectData, IReportDataCallback iReportDataCallback) throws RemoteException;

    void reportHabitData(Bundle bundle) throws RemoteException;

    void reportSceneInfos(Bundle bundle) throws RemoteException;

    void reportSysWakeUp(String str) throws RemoteException;

    void reportTopAData(Bundle bundle) throws RemoteException;

    void requestAppClean(List<String> list, int[] iArr, int i, String str, int i2) throws RemoteException;

    String saveBigData(int i, boolean z) throws RemoteException;

    void triggerUpdateWhiteList() throws RemoteException;

    boolean unRegisterProcessStateChangeObserver(IProcessStateChangeObserver iProcessStateChangeObserver) throws RemoteException;

    void unregisterDevModeMethod(int i, IDeviceSettingCallback iDeviceSettingCallback, Bundle bundle) throws RemoteException;

    void updateFakeForegroundList(List<String> list) throws RemoteException;

    public static class Default implements IHwSysResManager {
        @Override // android.rms.IHwSysResManager
        public ResourceConfig[] getResourceConfig(int resourceType) throws RemoteException {
            return null;
        }

        @Override // android.rms.IHwSysResManager
        public void recordResourceOverloadStatus(int uid, String pkg, int resourceType, int overloadNum, int speedOverLoadPeroid, int totalNum, Bundle args) throws RemoteException {
        }

        @Override // android.rms.IHwSysResManager
        public void clearResourceStatus(int uid, int resourceType) throws RemoteException {
        }

        @Override // android.rms.IHwSysResManager
        public int acquireSysRes(int resourceType, Uri uri, IContentObserver observer, Bundle args) throws RemoteException {
            return 0;
        }

        @Override // android.rms.IHwSysResManager
        public void notifyResourceStatus(int resourceType, String resourceName, int resourceStatus, Bundle bd) throws RemoteException {
        }

        @Override // android.rms.IHwSysResManager
        public void dispatch(int resourceType, MultiTaskPolicy policy) throws RemoteException {
        }

        @Override // android.rms.IHwSysResManager
        public void reportData(CollectData data) throws RemoteException {
        }

        @Override // android.rms.IHwSysResManager
        public void reportDataWithCallback(CollectData data, IReportDataCallback callback) throws RemoteException {
        }

        @Override // android.rms.IHwSysResManager
        public void enableFeature(int type) throws RemoteException {
        }

        @Override // android.rms.IHwSysResManager
        public void disableFeature(int type) throws RemoteException {
        }

        @Override // android.rms.IHwSysResManager
        public boolean configUpdate() throws RemoteException {
            return false;
        }

        @Override // android.rms.IHwSysResManager
        public void init(Bundle args) throws RemoteException {
        }

        @Override // android.rms.IHwSysResManager
        public boolean isResourceNeeded(int resourceid) throws RemoteException {
            return false;
        }

        @Override // android.rms.IHwSysResManager
        public void dispatchRPolicy(RPolicyData policy) throws RemoteException {
        }

        @Override // android.rms.IHwSysResManager
        public int getDumpData(int time, List<DumpData> list) throws RemoteException {
            return 0;
        }

        @Override // android.rms.IHwSysResManager
        public int getStatisticsData(List<StatisticsData> list) throws RemoteException {
            return 0;
        }

        @Override // android.rms.IHwSysResManager
        public String saveBigData(int featureId, boolean clear) throws RemoteException {
            return null;
        }

        @Override // android.rms.IHwSysResManager
        public String fetchBigDataByVersion(int iVer, int fId, boolean beta, boolean clear) throws RemoteException {
            return null;
        }

        @Override // android.rms.IHwSysResManager
        public void fetchDftDataByVersion(Bundle args) throws RemoteException {
        }

        @Override // android.rms.IHwSysResManager
        public void updateFakeForegroundList(List<String> list) throws RemoteException {
        }

        @Override // android.rms.IHwSysResManager
        public boolean isFakeForegroundProcess(String process) throws RemoteException {
            return false;
        }

        @Override // android.rms.IHwSysResManager
        public boolean isEnableFakeForegroundControl() throws RemoteException {
            return false;
        }

        @Override // android.rms.IHwSysResManager
        public String getWhiteList(int resourceType, int whiteListType) throws RemoteException {
            return null;
        }

        @Override // android.rms.IHwSysResManager
        public boolean registerResourceUpdateCallback(IUpdateWhiteListCallback cb) throws RemoteException {
            return false;
        }

        @Override // android.rms.IHwSysResManager
        public int getPid(String procName) throws RemoteException {
            return 0;
        }

        @Override // android.rms.IHwSysResManager
        public long getPss(int pid) throws RemoteException {
            return 0;
        }

        @Override // android.rms.IHwSysResManager
        public void triggerUpdateWhiteList() throws RemoteException {
        }

        @Override // android.rms.IHwSysResManager
        public List<String> getIAwareProtectList(int num) throws RemoteException {
            return null;
        }

        @Override // android.rms.IHwSysResManager
        public List<String> getLongTimeRunningApps() throws RemoteException {
            return null;
        }

        @Override // android.rms.IHwSysResManager
        public List<String> getMostFrequentUsedApps(int n, int minCount) throws RemoteException {
            return null;
        }

        @Override // android.rms.IHwSysResManager
        public boolean registerProcessStateChangeObserver(IProcessStateChangeObserver observer) throws RemoteException {
            return false;
        }

        @Override // android.rms.IHwSysResManager
        public boolean unRegisterProcessStateChangeObserver(IProcessStateChangeObserver observer) throws RemoteException {
            return false;
        }

        @Override // android.rms.IHwSysResManager
        public void noteProcessStart(String packageName, String processName, int pid, int uid, boolean started, String launcherMode, String reason) throws RemoteException {
        }

        @Override // android.rms.IHwSysResManager
        public long getMemAvaliable() throws RemoteException {
            return 0;
        }

        @Override // android.rms.IHwSysResManager
        public void reportAppType(String pkgName, int appType, boolean status, int attr) throws RemoteException {
        }

        @Override // android.rms.IHwSysResManager
        public void reportHabitData(Bundle habitData) throws RemoteException {
        }

        @Override // android.rms.IHwSysResManager
        public List<MemRepairPkgInfo> getMemRepairProcGroup(int sceneType) throws RemoteException {
            return null;
        }

        @Override // android.rms.IHwSysResManager
        public List<String> getFrequentIM(int count) throws RemoteException {
            return null;
        }

        @Override // android.rms.IHwSysResManager
        public boolean isVisibleWindow(int userid, String pkg, int type) throws RemoteException {
            return false;
        }

        @Override // android.rms.IHwSysResManager
        public void reportSysWakeUp(String reason) throws RemoteException {
        }

        @Override // android.rms.IHwSysResManager
        public boolean custConfigUpdate() throws RemoteException {
            return false;
        }

        @Override // android.rms.IHwSysResManager
        public void requestAppClean(List<String> list, int[] userIdArray, int level, String reason, int source) throws RemoteException {
        }

        @Override // android.rms.IHwSysResManager
        public NetLocationStrategy getNetLocationStrategy(String pkgName, int uid, int type) throws RemoteException {
            return null;
        }

        @Override // android.rms.IHwSysResManager
        public boolean registerSceneCallback(IBinder callback, int scenes) throws RemoteException {
            return false;
        }

        @Override // android.rms.IHwSysResManager
        public List<String> getHabitTopN(int n) throws RemoteException {
            return null;
        }

        @Override // android.rms.IHwSysResManager
        public Bundle getTypeTopN(int[] appTypes) throws RemoteException {
            return null;
        }

        @Override // android.rms.IHwSysResManager
        public void registerDevModeMethod(int deviceId, IDeviceSettingCallback callback, Bundle reserve) throws RemoteException {
        }

        @Override // android.rms.IHwSysResManager
        public void unregisterDevModeMethod(int deviceId, IDeviceSettingCallback callback, Bundle reserve) throws RemoteException {
        }

        @Override // android.rms.IHwSysResManager
        public void reportTopAData(Bundle bdl) throws RemoteException {
        }

        @Override // android.rms.IHwSysResManager
        public void reportCloudUpdate(Bundle bundle) throws RemoteException {
        }

        @Override // android.rms.IHwSysResManager
        public void reportSceneInfos(Bundle bdl) throws RemoteException {
        }

        @Override // android.rms.IHwSysResManager
        public boolean preloadAppForLauncher(String packageName, int userId, int preloadType) throws RemoteException {
            return false;
        }

        @Override // android.rms.IHwSysResManager
        public boolean isZApp(String pkg, int userId) throws RemoteException {
            return false;
        }

        public IBinder asBinder() {
            return null;
        }
    }

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
        static final int TRANSACTION_fetchDftDataByVersion = 19;
        static final int TRANSACTION_getDumpData = 15;
        static final int TRANSACTION_getFrequentIM = 38;
        static final int TRANSACTION_getHabitTopN = 45;
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
        static final int TRANSACTION_getTypeTopN = 46;
        static final int TRANSACTION_getWhiteList = 23;
        static final int TRANSACTION_init = 12;
        static final int TRANSACTION_isEnableFakeForegroundControl = 22;
        static final int TRANSACTION_isFakeForegroundProcess = 21;
        static final int TRANSACTION_isResourceNeeded = 13;
        static final int TRANSACTION_isVisibleWindow = 39;
        static final int TRANSACTION_isZApp = 53;
        static final int TRANSACTION_noteProcessStart = 33;
        static final int TRANSACTION_notifyResourceStatus = 5;
        static final int TRANSACTION_preloadAppForLauncher = 52;
        static final int TRANSACTION_recordResourceOverloadStatus = 2;
        static final int TRANSACTION_registerDevModeMethod = 47;
        static final int TRANSACTION_registerProcessStateChangeObserver = 31;
        static final int TRANSACTION_registerResourceUpdateCallback = 24;
        static final int TRANSACTION_registerSceneCallback = 44;
        static final int TRANSACTION_reportAppType = 35;
        static final int TRANSACTION_reportCloudUpdate = 50;
        static final int TRANSACTION_reportData = 7;
        static final int TRANSACTION_reportDataWithCallback = 8;
        static final int TRANSACTION_reportHabitData = 36;
        static final int TRANSACTION_reportSceneInfos = 51;
        static final int TRANSACTION_reportSysWakeUp = 40;
        static final int TRANSACTION_reportTopAData = 49;
        static final int TRANSACTION_requestAppClean = 42;
        static final int TRANSACTION_saveBigData = 17;
        static final int TRANSACTION_triggerUpdateWhiteList = 27;
        static final int TRANSACTION_unRegisterProcessStateChangeObserver = 32;
        static final int TRANSACTION_unregisterDevModeMethod = 48;
        static final int TRANSACTION_updateFakeForegroundList = 20;

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

        @Override // android.os.Binder
        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            Bundle _arg6;
            Uri _arg1;
            Bundle _arg3;
            Bundle _arg32;
            MultiTaskPolicy _arg12;
            CollectData _arg0;
            CollectData _arg02;
            Bundle _arg03;
            RPolicyData _arg04;
            Bundle _arg05;
            Bundle _arg06;
            Bundle _arg2;
            Bundle _arg22;
            Bundle _arg07;
            Bundle _arg08;
            Bundle _arg09;
            if (code != 1598968902) {
                boolean _arg13 = false;
                boolean _arg23 = false;
                boolean _arg33 = false;
                switch (code) {
                    case 1:
                        data.enforceInterface(DESCRIPTOR);
                        ResourceConfig[] _result = getResourceConfig(data.readInt());
                        reply.writeNoException();
                        reply.writeTypedArray(_result, 1);
                        return true;
                    case 2:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg010 = data.readInt();
                        String _arg14 = data.readString();
                        int _arg24 = data.readInt();
                        int _arg34 = data.readInt();
                        int _arg4 = data.readInt();
                        int _arg5 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg6 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg6 = null;
                        }
                        recordResourceOverloadStatus(_arg010, _arg14, _arg24, _arg34, _arg4, _arg5, _arg6);
                        reply.writeNoException();
                        return true;
                    case 3:
                        data.enforceInterface(DESCRIPTOR);
                        clearResourceStatus(data.readInt(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 4:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg011 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg1 = (Uri) Uri.CREATOR.createFromParcel(data);
                        } else {
                            _arg1 = null;
                        }
                        IContentObserver _arg25 = IContentObserver.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg3 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg3 = null;
                        }
                        int _result2 = acquireSysRes(_arg011, _arg1, _arg25, _arg3);
                        reply.writeNoException();
                        reply.writeInt(_result2);
                        return true;
                    case 5:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg012 = data.readInt();
                        String _arg15 = data.readString();
                        int _arg26 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg32 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg32 = null;
                        }
                        notifyResourceStatus(_arg012, _arg15, _arg26, _arg32);
                        reply.writeNoException();
                        return true;
                    case 6:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg013 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg12 = MultiTaskPolicy.CREATOR.createFromParcel(data);
                        } else {
                            _arg12 = null;
                        }
                        dispatch(_arg013, _arg12);
                        reply.writeNoException();
                        return true;
                    case 7:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg0 = CollectData.CREATOR.createFromParcel(data);
                        } else {
                            _arg0 = null;
                        }
                        reportData(_arg0);
                        reply.writeNoException();
                        return true;
                    case 8:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg02 = CollectData.CREATOR.createFromParcel(data);
                        } else {
                            _arg02 = null;
                        }
                        reportDataWithCallback(_arg02, IReportDataCallback.Stub.asInterface(data.readStrongBinder()));
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
                        boolean configUpdate = configUpdate();
                        reply.writeNoException();
                        reply.writeInt(configUpdate ? 1 : 0);
                        return true;
                    case 12:
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
                        boolean isResourceNeeded = isResourceNeeded(data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isResourceNeeded ? 1 : 0);
                        return true;
                    case 14:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg04 = RPolicyData.CREATOR.createFromParcel(data);
                        } else {
                            _arg04 = null;
                        }
                        dispatchRPolicy(_arg04);
                        reply.writeNoException();
                        return true;
                    case 15:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg014 = data.readInt();
                        ArrayList arrayList = new ArrayList();
                        int _result3 = getDumpData(_arg014, arrayList);
                        reply.writeNoException();
                        reply.writeInt(_result3);
                        reply.writeTypedList(arrayList);
                        return true;
                    case 16:
                        data.enforceInterface(DESCRIPTOR);
                        ArrayList arrayList2 = new ArrayList();
                        int _result4 = getStatisticsData(arrayList2);
                        reply.writeNoException();
                        reply.writeInt(_result4);
                        reply.writeTypedList(arrayList2);
                        return true;
                    case 17:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg015 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg13 = true;
                        }
                        String _result5 = saveBigData(_arg015, _arg13);
                        reply.writeNoException();
                        reply.writeString(_result5);
                        return true;
                    case 18:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg016 = data.readInt();
                        int _arg16 = data.readInt();
                        boolean _arg27 = data.readInt() != 0;
                        if (data.readInt() != 0) {
                            _arg33 = true;
                        }
                        String _result6 = fetchBigDataByVersion(_arg016, _arg16, _arg27, _arg33);
                        reply.writeNoException();
                        reply.writeString(_result6);
                        return true;
                    case 19:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg05 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg05 = null;
                        }
                        fetchDftDataByVersion(_arg05);
                        reply.writeNoException();
                        return true;
                    case 20:
                        data.enforceInterface(DESCRIPTOR);
                        updateFakeForegroundList(data.createStringArrayList());
                        reply.writeNoException();
                        return true;
                    case 21:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isFakeForegroundProcess = isFakeForegroundProcess(data.readString());
                        reply.writeNoException();
                        reply.writeInt(isFakeForegroundProcess ? 1 : 0);
                        return true;
                    case 22:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isEnableFakeForegroundControl = isEnableFakeForegroundControl();
                        reply.writeNoException();
                        reply.writeInt(isEnableFakeForegroundControl ? 1 : 0);
                        return true;
                    case 23:
                        data.enforceInterface(DESCRIPTOR);
                        String _result7 = getWhiteList(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeString(_result7);
                        return true;
                    case 24:
                        data.enforceInterface(DESCRIPTOR);
                        boolean registerResourceUpdateCallback = registerResourceUpdateCallback(IUpdateWhiteListCallback.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(registerResourceUpdateCallback ? 1 : 0);
                        return true;
                    case 25:
                        data.enforceInterface(DESCRIPTOR);
                        int _result8 = getPid(data.readString());
                        reply.writeNoException();
                        reply.writeInt(_result8);
                        return true;
                    case 26:
                        data.enforceInterface(DESCRIPTOR);
                        long _result9 = getPss(data.readInt());
                        reply.writeNoException();
                        reply.writeLong(_result9);
                        return true;
                    case 27:
                        data.enforceInterface(DESCRIPTOR);
                        triggerUpdateWhiteList();
                        reply.writeNoException();
                        return true;
                    case 28:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result10 = getIAwareProtectList(data.readInt());
                        reply.writeNoException();
                        reply.writeStringList(_result10);
                        return true;
                    case 29:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result11 = getLongTimeRunningApps();
                        reply.writeNoException();
                        reply.writeStringList(_result11);
                        return true;
                    case 30:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result12 = getMostFrequentUsedApps(data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeStringList(_result12);
                        return true;
                    case 31:
                        data.enforceInterface(DESCRIPTOR);
                        boolean registerProcessStateChangeObserver = registerProcessStateChangeObserver(IProcessStateChangeObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(registerProcessStateChangeObserver ? 1 : 0);
                        return true;
                    case 32:
                        data.enforceInterface(DESCRIPTOR);
                        boolean unRegisterProcessStateChangeObserver = unRegisterProcessStateChangeObserver(IProcessStateChangeObserver.Stub.asInterface(data.readStrongBinder()));
                        reply.writeNoException();
                        reply.writeInt(unRegisterProcessStateChangeObserver ? 1 : 0);
                        return true;
                    case 33:
                        data.enforceInterface(DESCRIPTOR);
                        noteProcessStart(data.readString(), data.readString(), data.readInt(), data.readInt(), data.readInt() != 0, data.readString(), data.readString());
                        reply.writeNoException();
                        return true;
                    case 34:
                        data.enforceInterface(DESCRIPTOR);
                        long _result13 = getMemAvaliable();
                        reply.writeNoException();
                        reply.writeLong(_result13);
                        return true;
                    case 35:
                        data.enforceInterface(DESCRIPTOR);
                        String _arg017 = data.readString();
                        int _arg17 = data.readInt();
                        if (data.readInt() != 0) {
                            _arg23 = true;
                        }
                        reportAppType(_arg017, _arg17, _arg23, data.readInt());
                        reply.writeNoException();
                        return true;
                    case 36:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg06 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg06 = null;
                        }
                        reportHabitData(_arg06);
                        reply.writeNoException();
                        return true;
                    case 37:
                        data.enforceInterface(DESCRIPTOR);
                        List<MemRepairPkgInfo> _result14 = getMemRepairProcGroup(data.readInt());
                        reply.writeNoException();
                        reply.writeTypedList(_result14);
                        return true;
                    case 38:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result15 = getFrequentIM(data.readInt());
                        reply.writeNoException();
                        reply.writeStringList(_result15);
                        return true;
                    case 39:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isVisibleWindow = isVisibleWindow(data.readInt(), data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isVisibleWindow ? 1 : 0);
                        return true;
                    case 40:
                        data.enforceInterface(DESCRIPTOR);
                        reportSysWakeUp(data.readString());
                        reply.writeNoException();
                        return true;
                    case 41:
                        data.enforceInterface(DESCRIPTOR);
                        boolean custConfigUpdate = custConfigUpdate();
                        reply.writeNoException();
                        reply.writeInt(custConfigUpdate ? 1 : 0);
                        return true;
                    case 42:
                        data.enforceInterface(DESCRIPTOR);
                        requestAppClean(data.createStringArrayList(), data.createIntArray(), data.readInt(), data.readString(), data.readInt());
                        reply.writeNoException();
                        return true;
                    case 43:
                        data.enforceInterface(DESCRIPTOR);
                        NetLocationStrategy _result16 = getNetLocationStrategy(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        if (_result16 != null) {
                            reply.writeInt(1);
                            _result16.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 44:
                        data.enforceInterface(DESCRIPTOR);
                        boolean registerSceneCallback = registerSceneCallback(data.readStrongBinder(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(registerSceneCallback ? 1 : 0);
                        return true;
                    case 45:
                        data.enforceInterface(DESCRIPTOR);
                        List<String> _result17 = getHabitTopN(data.readInt());
                        reply.writeNoException();
                        reply.writeStringList(_result17);
                        return true;
                    case 46:
                        data.enforceInterface(DESCRIPTOR);
                        Bundle _result18 = getTypeTopN(data.createIntArray());
                        reply.writeNoException();
                        if (_result18 != null) {
                            reply.writeInt(1);
                            _result18.writeToParcel(reply, 1);
                        } else {
                            reply.writeInt(0);
                        }
                        return true;
                    case 47:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg018 = data.readInt();
                        IDeviceSettingCallback _arg18 = IDeviceSettingCallback.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg2 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg2 = null;
                        }
                        registerDevModeMethod(_arg018, _arg18, _arg2);
                        reply.writeNoException();
                        return true;
                    case 48:
                        data.enforceInterface(DESCRIPTOR);
                        int _arg019 = data.readInt();
                        IDeviceSettingCallback _arg19 = IDeviceSettingCallback.Stub.asInterface(data.readStrongBinder());
                        if (data.readInt() != 0) {
                            _arg22 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg22 = null;
                        }
                        unregisterDevModeMethod(_arg019, _arg19, _arg22);
                        reply.writeNoException();
                        return true;
                    case 49:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg07 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg07 = null;
                        }
                        reportTopAData(_arg07);
                        reply.writeNoException();
                        return true;
                    case 50:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg08 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg08 = null;
                        }
                        reportCloudUpdate(_arg08);
                        reply.writeNoException();
                        return true;
                    case 51:
                        data.enforceInterface(DESCRIPTOR);
                        if (data.readInt() != 0) {
                            _arg09 = (Bundle) Bundle.CREATOR.createFromParcel(data);
                        } else {
                            _arg09 = null;
                        }
                        reportSceneInfos(_arg09);
                        reply.writeNoException();
                        return true;
                    case 52:
                        data.enforceInterface(DESCRIPTOR);
                        boolean preloadAppForLauncher = preloadAppForLauncher(data.readString(), data.readInt(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(preloadAppForLauncher ? 1 : 0);
                        return true;
                    case 53:
                        data.enforceInterface(DESCRIPTOR);
                        boolean isZApp = isZApp(data.readString(), data.readInt());
                        reply.writeNoException();
                        reply.writeInt(isZApp ? 1 : 0);
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
        public static class Proxy implements IHwSysResManager {
            public static IHwSysResManager sDefaultImpl;
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

            @Override // android.rms.IHwSysResManager
            public ResourceConfig[] getResourceConfig(int resourceType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(resourceType);
                    if (!this.mRemote.transact(1, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getResourceConfig(resourceType);
                    }
                    _reply.readException();
                    ResourceConfig[] _result = (ResourceConfig[]) _reply.createTypedArray(ResourceConfig.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.rms.IHwSysResManager
            public void recordResourceOverloadStatus(int uid, String pkg, int resourceType, int overloadNum, int speedOverLoadPeroid, int totalNum, Bundle args) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    try {
                        _data.writeInt(uid);
                        try {
                            _data.writeString(pkg);
                        } catch (Throwable th) {
                            th = th;
                            _reply.recycle();
                            _data.recycle();
                            throw th;
                        }
                        try {
                            _data.writeInt(resourceType);
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
                    try {
                        _data.writeInt(overloadNum);
                        _data.writeInt(speedOverLoadPeroid);
                        _data.writeInt(totalNum);
                        if (args != null) {
                            _data.writeInt(1);
                            args.writeToParcel(_data, 0);
                        } else {
                            _data.writeInt(0);
                        }
                        if (this.mRemote.transact(2, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                            _reply.readException();
                            _reply.recycle();
                            _data.recycle();
                            return;
                        }
                        Stub.getDefaultImpl().recordResourceOverloadStatus(uid, pkg, resourceType, overloadNum, speedOverLoadPeroid, totalNum, args);
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
            }

            @Override // android.rms.IHwSysResManager
            public void clearResourceStatus(int uid, int resourceType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(uid);
                    _data.writeInt(resourceType);
                    if (this.mRemote.transact(3, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().clearResourceStatus(uid, resourceType);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.rms.IHwSysResManager
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
                    if (!this.mRemote.transact(4, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().acquireSysRes(resourceType, uri, observer, args);
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

            @Override // android.rms.IHwSysResManager
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
                    if (this.mRemote.transact(5, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().notifyResourceStatus(resourceType, resourceName, resourceStatus, bd);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.rms.IHwSysResManager
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
                    if (this.mRemote.transact(6, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().dispatch(resourceType, policy);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.rms.IHwSysResManager
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
                    if (this.mRemote.transact(7, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().reportData(data);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.rms.IHwSysResManager
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
                    if (this.mRemote.transact(8, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().reportDataWithCallback(data, callback);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.rms.IHwSysResManager
            public void enableFeature(int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    if (this.mRemote.transact(9, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().enableFeature(type);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.rms.IHwSysResManager
            public void disableFeature(int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(type);
                    if (this.mRemote.transact(10, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().disableFeature(type);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.rms.IHwSysResManager
            public boolean configUpdate() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(11, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().configUpdate();
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

            @Override // android.rms.IHwSysResManager
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
                    if (this.mRemote.transact(12, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().init(args);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.rms.IHwSysResManager
            public boolean isResourceNeeded(int resourceid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(resourceid);
                    boolean _result = false;
                    if (!this.mRemote.transact(13, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
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

            @Override // android.rms.IHwSysResManager
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
                    if (this.mRemote.transact(14, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().dispatchRPolicy(policy);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.rms.IHwSysResManager
            public int getDumpData(int time, List<DumpData> dumpData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(time);
                    if (!this.mRemote.transact(15, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getDumpData(time, dumpData);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readTypedList(dumpData, DumpData.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.rms.IHwSysResManager
            public int getStatisticsData(List<StatisticsData> statisticsData) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(16, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getStatisticsData(statisticsData);
                    }
                    _reply.readException();
                    int _result = _reply.readInt();
                    _reply.readTypedList(statisticsData, StatisticsData.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.rms.IHwSysResManager
            public String saveBigData(int featureId, boolean clear) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(featureId);
                    _data.writeInt(clear ? 1 : 0);
                    if (!this.mRemote.transact(17, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().saveBigData(featureId, clear);
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

            @Override // android.rms.IHwSysResManager
            public String fetchBigDataByVersion(int iVer, int fId, boolean beta, boolean clear) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(iVer);
                    _data.writeInt(fId);
                    int i = 1;
                    _data.writeInt(beta ? 1 : 0);
                    if (!clear) {
                        i = 0;
                    }
                    _data.writeInt(i);
                    if (!this.mRemote.transact(18, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().fetchBigDataByVersion(iVer, fId, beta, clear);
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

            @Override // android.rms.IHwSysResManager
            public void fetchDftDataByVersion(Bundle args) throws RemoteException {
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
                    if (this.mRemote.transact(19, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().fetchDftDataByVersion(args);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.rms.IHwSysResManager
            public void updateFakeForegroundList(List<String> processList) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStringList(processList);
                    if (this.mRemote.transact(20, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().updateFakeForegroundList(processList);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.rms.IHwSysResManager
            public boolean isFakeForegroundProcess(String process) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(process);
                    boolean _result = false;
                    if (!this.mRemote.transact(21, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isFakeForegroundProcess(process);
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

            @Override // android.rms.IHwSysResManager
            public boolean isEnableFakeForegroundControl() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(22, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isEnableFakeForegroundControl();
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

            @Override // android.rms.IHwSysResManager
            public String getWhiteList(int resourceType, int whiteListType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(resourceType);
                    _data.writeInt(whiteListType);
                    if (!this.mRemote.transact(23, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getWhiteList(resourceType, whiteListType);
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

            @Override // android.rms.IHwSysResManager
            public boolean registerResourceUpdateCallback(IUpdateWhiteListCallback cb) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(cb != null ? cb.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(24, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerResourceUpdateCallback(cb);
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

            @Override // android.rms.IHwSysResManager
            public int getPid(String procName) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(procName);
                    if (!this.mRemote.transact(25, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPid(procName);
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

            @Override // android.rms.IHwSysResManager
            public long getPss(int pid) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(pid);
                    if (!this.mRemote.transact(26, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getPss(pid);
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

            @Override // android.rms.IHwSysResManager
            public void triggerUpdateWhiteList() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (this.mRemote.transact(27, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().triggerUpdateWhiteList();
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.rms.IHwSysResManager
            public List<String> getIAwareProtectList(int num) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(num);
                    if (!this.mRemote.transact(28, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getIAwareProtectList(num);
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

            @Override // android.rms.IHwSysResManager
            public List<String> getLongTimeRunningApps() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(29, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getLongTimeRunningApps();
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

            @Override // android.rms.IHwSysResManager
            public List<String> getMostFrequentUsedApps(int n, int minCount) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(n);
                    _data.writeInt(minCount);
                    if (!this.mRemote.transact(30, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMostFrequentUsedApps(n, minCount);
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

            @Override // android.rms.IHwSysResManager
            public boolean registerProcessStateChangeObserver(IProcessStateChangeObserver observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(31, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerProcessStateChangeObserver(observer);
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

            @Override // android.rms.IHwSysResManager
            public boolean unRegisterProcessStateChangeObserver(IProcessStateChangeObserver observer) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(observer != null ? observer.asBinder() : null);
                    boolean _result = false;
                    if (!this.mRemote.transact(32, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().unRegisterProcessStateChangeObserver(observer);
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

            @Override // android.rms.IHwSysResManager
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
                            if (this.mRemote.transact(33, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
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

            @Override // android.rms.IHwSysResManager
            public long getMemAvaliable() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (!this.mRemote.transact(34, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMemAvaliable();
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

            @Override // android.rms.IHwSysResManager
            public void reportAppType(String pkgName, int appType, boolean status, int attr) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeInt(appType);
                    _data.writeInt(status ? 1 : 0);
                    _data.writeInt(attr);
                    if (this.mRemote.transact(35, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().reportAppType(pkgName, appType, status, attr);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.rms.IHwSysResManager
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
                    if (this.mRemote.transact(36, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().reportHabitData(habitData);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.rms.IHwSysResManager
            public List<MemRepairPkgInfo> getMemRepairProcGroup(int sceneType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(sceneType);
                    if (!this.mRemote.transact(37, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getMemRepairProcGroup(sceneType);
                    }
                    _reply.readException();
                    List<MemRepairPkgInfo> _result = _reply.createTypedArrayList(MemRepairPkgInfo.CREATOR);
                    _reply.recycle();
                    _data.recycle();
                    return _result;
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.rms.IHwSysResManager
            public List<String> getFrequentIM(int count) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(count);
                    if (!this.mRemote.transact(38, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getFrequentIM(count);
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

            @Override // android.rms.IHwSysResManager
            public boolean isVisibleWindow(int userid, String pkg, int type) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(userid);
                    _data.writeString(pkg);
                    _data.writeInt(type);
                    boolean _result = false;
                    if (!this.mRemote.transact(39, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isVisibleWindow(userid, pkg, type);
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

            @Override // android.rms.IHwSysResManager
            public void reportSysWakeUp(String reason) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(reason);
                    if (this.mRemote.transact(40, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().reportSysWakeUp(reason);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.rms.IHwSysResManager
            public boolean custConfigUpdate() throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    boolean _result = false;
                    if (!this.mRemote.transact(41, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().custConfigUpdate();
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

            @Override // android.rms.IHwSysResManager
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
                    if (this.mRemote.transact(42, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().requestAppClean(pkgNameList, userIdArray, level, reason, source);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.rms.IHwSysResManager
            public NetLocationStrategy getNetLocationStrategy(String pkgName, int uid, int type) throws RemoteException {
                NetLocationStrategy _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkgName);
                    _data.writeInt(uid);
                    _data.writeInt(type);
                    if (!this.mRemote.transact(43, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getNetLocationStrategy(pkgName, uid, type);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (NetLocationStrategy) NetLocationStrategy.CREATOR.createFromParcel(_reply);
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

            @Override // android.rms.IHwSysResManager
            public boolean registerSceneCallback(IBinder callback, int scenes) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeStrongBinder(callback);
                    _data.writeInt(scenes);
                    boolean _result = false;
                    if (!this.mRemote.transact(44, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().registerSceneCallback(callback, scenes);
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

            @Override // android.rms.IHwSysResManager
            public List<String> getHabitTopN(int n) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeInt(n);
                    if (!this.mRemote.transact(45, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getHabitTopN(n);
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

            @Override // android.rms.IHwSysResManager
            public Bundle getTypeTopN(int[] appTypes) throws RemoteException {
                Bundle _result;
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeIntArray(appTypes);
                    if (!this.mRemote.transact(46, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().getTypeTopN(appTypes);
                    }
                    _reply.readException();
                    if (_reply.readInt() != 0) {
                        _result = (Bundle) Bundle.CREATOR.createFromParcel(_reply);
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

            @Override // android.rms.IHwSysResManager
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
                    if (this.mRemote.transact(47, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().registerDevModeMethod(deviceId, callback, reserve);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.rms.IHwSysResManager
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
                    if (this.mRemote.transact(48, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().unregisterDevModeMethod(deviceId, callback, reserve);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.rms.IHwSysResManager
            public void reportTopAData(Bundle bdl) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (bdl != null) {
                        _data.writeInt(1);
                        bdl.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(49, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().reportTopAData(bdl);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.rms.IHwSysResManager
            public void reportCloudUpdate(Bundle bundle) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (bundle != null) {
                        _data.writeInt(1);
                        bundle.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(50, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().reportCloudUpdate(bundle);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.rms.IHwSysResManager
            public void reportSceneInfos(Bundle bdl) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    if (bdl != null) {
                        _data.writeInt(1);
                        bdl.writeToParcel(_data, 0);
                    } else {
                        _data.writeInt(0);
                    }
                    if (this.mRemote.transact(51, _data, _reply, 0) || Stub.getDefaultImpl() == null) {
                        _reply.readException();
                        _reply.recycle();
                        _data.recycle();
                        return;
                    }
                    Stub.getDefaultImpl().reportSceneInfos(bdl);
                } finally {
                    _reply.recycle();
                    _data.recycle();
                }
            }

            @Override // android.rms.IHwSysResManager
            public boolean preloadAppForLauncher(String packageName, int userId, int preloadType) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(packageName);
                    _data.writeInt(userId);
                    _data.writeInt(preloadType);
                    boolean _result = false;
                    if (!this.mRemote.transact(52, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().preloadAppForLauncher(packageName, userId, preloadType);
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

            @Override // android.rms.IHwSysResManager
            public boolean isZApp(String pkg, int userId) throws RemoteException {
                Parcel _data = Parcel.obtain();
                Parcel _reply = Parcel.obtain();
                try {
                    _data.writeInterfaceToken(Stub.DESCRIPTOR);
                    _data.writeString(pkg);
                    _data.writeInt(userId);
                    boolean _result = false;
                    if (!this.mRemote.transact(53, _data, _reply, 0) && Stub.getDefaultImpl() != null) {
                        return Stub.getDefaultImpl().isZApp(pkg, userId);
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

        public static boolean setDefaultImpl(IHwSysResManager impl) {
            if (Proxy.sDefaultImpl != null || impl == null) {
                return false;
            }
            Proxy.sDefaultImpl = impl;
            return true;
        }

        public static IHwSysResManager getDefaultImpl() {
            return Proxy.sDefaultImpl;
        }
    }
}
