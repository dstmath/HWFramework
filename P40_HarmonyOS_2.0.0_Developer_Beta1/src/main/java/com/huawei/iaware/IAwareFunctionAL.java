package com.huawei.iaware;

import android.app.ActivityManager;
import android.app.mtm.IMultiTaskManagerService;
import android.aps.IApsManager;
import android.common.HwFrameworkFactory;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.iawareperf.UniPerf;
import android.media.AudioSystem;
import android.net.Uri;
import android.net.booster.HwCommBoosterServiceManagerEx;
import android.net.wifi.HwInnerWifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IPowerManager;
import android.os.IRemoteCallback;
import android.os.Looper;
import android.os.Message;
import android.os.Parcel;
import android.os.PersistableBundle;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.rms.HwSysResManager;
import android.rms.iaware.AwareConstant;
import android.rms.iaware.AwareLog;
import android.rms.iaware.CollectData;
import android.rms.iaware.DataContract;
import android.rms.iaware.DumpData;
import android.rms.iaware.StatisticsData;
import android.rms.iaware.memrepair.MemRepairPkgInfo;
import android.security.NetworkSecurityPolicy;
import android.system.ErrnoException;
import android.system.Int32Ref;
import android.system.Os;
import android.util.ERecovery;
import android.util.ERecoveryEvent;
import android.util.HwPCUtils;
import android.util.Jlog;
import android.util.MutableInt;
import com.android.internal.os.BackgroundThread;
import com.android.internal.os.HwBootAnimationOeminfo;
import com.android.internal.telephony.IHwTelephony;
import com.android.internal.util.MemInfoReader;
import com.huawei.android.app.ActivityManagerEx;
import com.huawei.android.app.HwActivityManager;
import com.huawei.android.fsm.HwFoldScreenManagerEx;
import com.huawei.android.net.wifi.WifiManagerEx;
import com.huawei.android.pgmng.plug.PowerKit;
import com.huawei.android.util.NoExtAPIException;
import com.huawei.displayengine.IDisplayEngineServiceEx;
import com.huawei.hsm.permission.StubController;
import com.huawei.permission.IHoldService;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import vendor.huawei.hardware.hwsched.V1_0.ISched;
import vendor.huawei.hardware.iawareperfpolicy.V1_0.IPerfPolicy;

public class IAwareFunctionAL {
    private static final int INVALID_DATA = -1;
    private static final long INVALID_LONG = 0;
    private static final String TAG = "IAwareFunctionAL";

    public static boolean getBooleanProp(String key, boolean def) {
        return SystemProperties.getBoolean(key, def);
    }

    public static int getIntProp(String key, int def) {
        return SystemProperties.getInt(key, def);
    }

    public static void setProp(String key, String val) {
        try {
            SystemProperties.set(key, val);
        } catch (IllegalArgumentException e) {
            AwareLog.e(TAG, e.getMessage());
        }
    }

    public static String getProp(String key, String def) {
        return SystemProperties.get(key, def);
    }

    public static void enableFeatureRDA(int featureId) {
        HwSysResManager.getInstance().enableFeature(featureId);
    }

    public static void disableFeatureRDA(int featureId) {
        HwSysResManager.getInstance().disableFeature(featureId);
    }

    public static int getDumpDataRDA(int time, List<DumpData> dumpData) {
        return HwSysResManager.getInstance().getDumpData(time, dumpData);
    }

    public static int getStatisticsDataRDA(List<StatisticsData> statisticsData) {
        return HwSysResManager.getInstance().getStatisticsData(statisticsData);
    }

    public static void initRDA(Bundle bundle) {
        HwSysResManager.getInstance().init(bundle);
    }

    public static void configUpdateRDA() {
        HwSysResManager.getInstance().configUpdate();
    }

    public static void custConfigUpdateRDA() {
        HwSysResManager.getInstance().custConfigUpdate();
    }

    public static void requestAppClean(List<String> pkgNameList, int[] userIdArray, int level, String reason, int source) {
        if (pkgNameList != null && userIdArray != null) {
            HwSysResManager.getInstance().requestAppClean(pkgNameList, userIdArray, level, reason, source);
        }
    }

    public static String saveBigDataRDA(int featureId, boolean clear) {
        return HwSysResManager.getInstance().saveBigData(featureId, clear);
    }

    public static String fetchBigDataByVersionRDA(int iVer, int fId, boolean beta, boolean clear) {
        return HwSysResManager.getInstance().fetchBigDataByVersion(iVer, fId, beta, clear);
    }

    public static String fetchBigDataByVersionRda(int awareVersion, int featureId, boolean beta, boolean clear) {
        return HwSysResManager.getInstance().fetchBigDataByVersion(awareVersion, featureId, beta, clear);
    }

    public static String fetchDFTDataByVersionRDA(int iVer, int fId, boolean beta, boolean clear, boolean betaEncode) {
        return null;
    }

    public static void fetchDftDataByVersionRda(Bundle args) {
        HwSysResManager.getInstance().fetchDftDataByVersion(args);
    }

    public static void reportDataRDA(CollectData data) {
        HwSysResManager.getInstance().reportData(data);
    }

    public static long getMemAvaliableRDA() {
        return HwSysResManager.getInstance().getMemAvaliable();
    }

    public static List<String> getMostFrequentUsedAppsRDA(int number, int minCount) {
        return HwSysResManager.getInstance().getMostFrequentUsedApps(number, minCount);
    }

    public static void triggerUpdateWhiteListRDA() {
        HwSysResManager.getInstance().triggerUpdateWhiteList();
    }

    public static List<String> getLongTimeRunningAppsRDA() {
        return HwSysResManager.getInstance().getLongTimeRunningApps();
    }

    public static List<MemRepairPkgInfo> getMemRepairProcGroupRDA(int scene) {
        return HwSysResManager.getInstance().getMemRepairProcGroup(scene);
    }

    public static int getPidRDA(String procName) {
        return HwSysResManager.getInstance().getPid(procName);
    }

    public static void reportAppTypeRDA(String pkgName, int appType, boolean status, int attr) {
        HwSysResManager.getInstance().reportAppType(pkgName, appType, status, attr);
    }

    public static void registerSceneCallbackRDA(IBinder token, int scenes) {
        HwSysResManager.getInstance().registerSceneCallback(token, scenes);
    }

    public static void reportHabitDataRDA(Bundle bundle) {
        HwSysResManager.getInstance().reportHabitData(bundle);
    }

    public static void updateFakeForegroundListRDA(List<String> list) {
        HwSysResManager.getInstance().updateFakeForegroundList(list);
    }

    public static File getCfgFileHwPolicy(String fileName, int type) {
        return HwCfgFilePolicy.getCfgFile(fileName, type);
    }

    public static List<File> getCfgFileListHwPolicy(String fileName, int type) {
        return HwCfgFilePolicy.getCfgFileList(fileName, type);
    }

    public static int getFeatureTypeIAware(int featureId) {
        return AwareConstant.FeatureType.getFeatureType(featureId).getValue();
    }

    public static int valuesFeatureIAware(int featureId) {
        return AwareConstant.FeatureType.getFeatureId(featureId);
    }

    public static String nameFeatureIAware(int featureId) {
        return AwareConstant.FeatureType.getFeatureType(featureId).name();
    }

    public static boolean readProcFile(String file, int[] format, String[] outStrings, long[] outLongs, float[] outFloats) {
        return Process.readProcFile(file, format, outStrings, outLongs, outFloats);
    }

    public static boolean isThreadInProcess(int tidInt, int pid) {
        return Process.isThreadInProcess(tidInt, pid);
    }

    public static boolean isInGameSpaceAME(String packageName) {
        return ActivityManagerEx.isInGameSpace(packageName);
    }

    public static String getPackageNameForPidAME(int pid) {
        try {
            return getPackageNameForPid(pid);
        } catch (NoExtAPIException e) {
            return null;
        }
    }

    public static String getPackageNameForPid(int pid) {
        String res = null;
        try {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("android.app.IActivityManager");
            data.writeInt(pid);
            ActivityManager.getService().asBinder().transact(504, data, reply, 0);
            reply.readException();
            res = reply.readString();
            data.recycle();
            reply.recycle();
            return res;
        } catch (RemoteException e) {
            AwareLog.w(TAG, "getPackageNameForPid faild");
            return res;
        }
    }

    public static int uniPerfEvent(int cmdId, String pkgName, int... payload) {
        return UniPerf.getInstance().uniPerfEvent(cmdId, pkgName, payload);
    }

    public static int uniPerfSetConfig(int clientId, int[] tags, int[] values) {
        return UniPerf.getInstance().uniPerfSetConfig(clientId, tags, values);
    }

    public static int uniPerfGetConfig(int[] tags, int[] values) {
        return UniPerf.getInstance().uniPerfGetConfig(tags, values);
    }

    public static void ioctlIntOs(FileDescriptor fd, int cmd, MutableInt arg) throws IOException {
        if (arg != null) {
            Int32Ref pending = new Int32Ref(0);
            try {
                Os.ioctlInt(fd, cmd, pending);
                arg.value = pending.value;
            } catch (ErrnoException e) {
                throw e.rethrowAsIOException();
            }
        }
    }

    public static boolean updateAppRunningStatusWME(int uid, int type, int status, int scene, int reserved) {
        return WifiManagerEx.updateAppRunningStatus(uid, type, status, scene, reserved);
    }

    public static boolean updateAppExperienceStatusWME(int uid, int experience, long rtt, int reserved) {
        return WifiManagerEx.updateAppExperienceStatus(uid, experience, rtt, reserved);
    }

    public static int reportBoosterParaBSME(String pkgName, int dataType, Bundle data) {
        return HwCommBoosterServiceManagerEx.reportBoosterPara(pkgName, dataType, data);
    }

    public static Looper getBackgroundThreadLooper() {
        return BackgroundThread.get().getLooper();
    }

    public static PackageInfo getPackageInfoPM(PackageManager pm, String name, int uid) throws PackageManager.NameNotFoundException {
        if (pm == null) {
            return null;
        }
        return pm.getPackageInfoAsUser(name, 2, uid);
    }

    public static String[] getPackagesForUidPM(PackageManager pm, int uid) {
        if (pm == null) {
            return null;
        }
        return pm.getPackagesForUid(uid);
    }

    public static ApplicationInfo getApplicationInfoPM(PackageManager pm, String packageName, int flags, int userId) throws PackageManager.NameNotFoundException {
        if (pm == null) {
            return null;
        }
        return pm.getApplicationInfoAsUser(packageName, flags, userId);
    }

    public static List<PackageInfo> getInstalledPackagesAsUserPM(PackageManager pManager, int flags, int userId) {
        if (pManager == null) {
            return null;
        }
        return pManager.getInstalledPackagesAsUser(flags, userId);
    }

    public static PackageInfo getPackageInfoAsUserPM(PackageManager pManager, String packageName, int flags, int userId) throws PackageManager.NameNotFoundException {
        if (pManager == null) {
            return null;
        }
        return pManager.getPackageInfoAsUser(packageName, flags, userId);
    }

    public static int getPackageUidAsUserPM(PackageManager pManager, String packageName, int userId) throws PackageManager.NameNotFoundException {
        if (pManager == null) {
            return 0;
        }
        return pManager.getPackageUidAsUser(packageName, userId);
    }

    public static int getCurrentUserAM() {
        return ActivityManager.getCurrentUser();
    }

    public static void forceStopPackageAM(ActivityManager am, String pkg) {
        if (am != null) {
            am.forceStopPackage(pkg);
        }
    }

    public static int getUserInfoProfileGroupIdUI(UserManager userManager, int userId) {
        UserInfo userInfo;
        if (userManager == null || (userInfo = userManager.getUserInfo(userId)) == null) {
            return -1;
        }
        return userInfo.profileGroupId;
    }

    public static List<Integer> getUserIdList(UserManager um) {
        List<UserInfo> userInfoList;
        if (um == null || (userInfoList = um.getUsers()) == null) {
            return null;
        }
        List<Integer> list = new ArrayList<>();
        int size = userInfoList.size();
        for (int i = 0; i < size; i++) {
            UserInfo info = userInfoList.get(i);
            if (info != null) {
                list.add(Integer.valueOf(info.id));
            }
        }
        return list;
    }

    public static Object getUserInfoUM(UserManager um, int userHandle) {
        if (um == null) {
            return null;
        }
        return um.getUserInfo(userHandle);
    }

    public static List<Object> getUsersUM(UserManager um) {
        if (um == null) {
            return null;
        }
        return new ArrayList(um.getUsers());
    }

    public static int getUserIdUH(int uid) {
        return UserHandle.getUserId(uid);
    }

    public static boolean isGuestUI(Object obj) {
        if (obj == null || !(obj instanceof UserInfo)) {
            return false;
        }
        return ((UserInfo) obj).isGuest();
    }

    public static boolean isClonedProfileUI(Object obj) {
        if (obj == null || !(obj instanceof UserInfo)) {
            return false;
        }
        return ((UserInfo) obj).isClonedProfile();
    }

    public static int getIdFromUI(Object obj) {
        if (obj == null || !(obj instanceof UserInfo)) {
            return -1;
        }
        return ((UserInfo) obj).id;
    }

    public static int getNumStreamTypesAS() {
        return AudioSystem.getNumStreamTypes();
    }

    public static boolean isStreamActiveAS(int stream, int inPastMs) {
        return AudioSystem.isStreamActive(stream, inPastMs);
    }

    public static boolean isStreamActiveRemotelyAS(int stream, int inPastMs) {
        return AudioSystem.isStreamActiveRemotely(stream, inPastMs);
    }

    public static int jLogD(int id, String arg1, int arg2, String msg) {
        return Jlog.d(id, arg1, arg2, msg);
    }

    public static int jLogD(int id, String msg) {
        return Jlog.d(id, msg);
    }

    public static Object getApsManagerHFF() {
        return HwFrameworkFactory.getApsManager();
    }

    public static int setFpsAPS(Object obj, String pkgName, int fps) {
        if (obj == null || !(obj instanceof IApsManager)) {
            return -1;
        }
        return ((IApsManager) obj).setFps(pkgName, fps);
    }

    public static int getFpsAPS(Object obj, String pkgName) {
        if (obj == null || !(obj instanceof IApsManager)) {
            return -1;
        }
        return ((IApsManager) obj).getFps(pkgName);
    }

    public static int setDynamicFpsAPS(Object obj, String pkgName, int fps) {
        if (obj == null || !(obj instanceof IApsManager)) {
            return -1;
        }
        return ((IApsManager) obj).setDynamicFps(pkgName, fps);
    }

    public static int getDynamicFpsAPS(Object obj, String pkgName) {
        if (obj == null || !(obj instanceof IApsManager)) {
            return -1;
        }
        return ((IApsManager) obj).getDynamicFps(pkgName);
    }

    public static int setResolutionAPS(Object obj, String packageName, float Ratio, boolean switchable) {
        if (obj == null || !(obj instanceof IApsManager)) {
            return -1;
        }
        return ((IApsManager) obj).setResolution(packageName, Ratio, false);
    }

    public static Object asInterfaceIDESEx(IBinder displayBinder) {
        return IDisplayEngineServiceEx.Stub.asInterface(displayBinder);
    }

    public static int setDataIDESEx(Object obj, int type, PersistableBundle data) throws RemoteException {
        if (obj == null || !(obj instanceof IDisplayEngineServiceEx)) {
            return -1;
        }
        return ((IDisplayEngineServiceEx) obj).setData(type, data);
    }

    public static Object getInstancePG() {
        return PowerKit.getInstance();
    }

    public static int getThermalInfoPG(Object obj, Context context, int type) throws RemoteException {
        if (obj == null || !(obj instanceof PowerKit)) {
            return -1;
        }
        return ((PowerKit) obj).getThermalInfo(context, type);
    }

    public static String getTopFrontAppPG(Object obj, Context context) throws RemoteException {
        if (obj == null || !(obj instanceof PowerKit)) {
            return null;
        }
        return ((PowerKit) obj).getTopFrontApp(context);
    }

    public static int getPkgTypePG(Object obj, Context context, String pkg) throws RemoteException {
        if (obj == null || !(obj instanceof PowerKit)) {
            return -1;
        }
        return ((PowerKit) obj).getPkgType(context, pkg);
    }

    public static Object getIHoldServiceSC() {
        return StubController.getHoldService();
    }

    public static Bundle callHsmServiceSC(Object object, String method, Bundle params) throws RemoteException {
        if (object == null || !(object instanceof IHoldService)) {
            return null;
        }
        return ((IHoldService) object).callHsmService(method, params);
    }

    public static void registerReceiverAsUserContext(Context context, BroadcastReceiver receiver, int id, IntentFilter filter, String permission, Handler handler) {
        if (context != null && id != -559038737) {
            context.registerReceiverAsUser(receiver, UserHandle.of(id), filter, permission, handler);
        }
    }

    public static void sendBroadcastAsUserContext(Context context, Intent intent, int id, String permission) {
        if (context != null && id != -559038737) {
            context.sendBroadcastAsUser(intent, UserHandle.of(id), permission);
        }
    }

    public static Object getMTMServices() {
        return IMultiTaskManagerService.Stub.asInterface(ServiceManager.getService("multi_task"));
    }

    public static List<String> retrieveAppStartupPackagesMTM(Object mtmServices, List<String> pkgList, int[] policy, int[] modifier, int[] show) throws RemoteException {
        if (mtmServices == null || !(mtmServices instanceof IMultiTaskManagerService)) {
            return null;
        }
        return ((IMultiTaskManagerService) mtmServices).retrieveAppStartupPackages(pkgList, policy, modifier, show);
    }

    public static void registerContentObserverAsUser(Object contentResolver, Uri uri, boolean notifyForDescendants, ContentObserver cob, int uid) throws RuntimeException {
        if (contentResolver != null && (contentResolver instanceof ContentResolver)) {
            ((ContentResolver) contentResolver).registerContentObserver(uri, notifyForDescendants, cob, uid);
        }
    }

    public static void sendResultUserSwitch(Object reply, Bundle data) throws RemoteException {
        if (reply != null && (reply instanceof IRemoteCallback)) {
            ((IRemoteCallback) reply).sendResult(data);
        }
    }

    public static void addServiceSM(String name, IBinder service) {
        ServiceManager.addService(name, service);
    }

    public static IBinder getServiceSM(String name) {
        return ServiceManager.getService(name);
    }

    public static Object readMemInfo() {
        MemInfoReader reader = new MemInfoReader();
        reader.readMemInfo();
        return reader;
    }

    public static long getTotalSize(Object reader) {
        if (reader == null || !(reader instanceof MemInfoReader)) {
            return INVALID_LONG;
        }
        return ((MemInfoReader) reader).getTotalSize();
    }

    public static long getCachedSizeKb(Object reader) {
        if (reader == null || !(reader instanceof MemInfoReader)) {
            return INVALID_LONG;
        }
        return ((MemInfoReader) reader).getCachedSizeKb();
    }

    public static long getFreeSizeKb(Object reader) {
        if (reader == null || !(reader instanceof MemInfoReader)) {
            return INVALID_LONG;
        }
        return ((MemInfoReader) reader).getFreeSizeKb();
    }

    public static CollectData buildRequestMemData(int event, int memKB, int pid, int uid) {
        DataContract.Apps.Builder builder = DataContract.Apps.builder();
        builder.addEvent(event);
        builder.addRequestMemApp(pid, uid, memKB);
        return builder.build();
    }

    public static Object getISchedService(String str) throws RemoteException {
        if (str != null) {
            return ISched.getService(str);
        }
        return null;
    }

    public static void sched_msg(Object object, int status, String msgInfo, int len) throws RemoteException {
        if (object != null && (object instanceof ISched)) {
            ((ISched) object).sched_msg(status, msgInfo, len);
        }
    }

    public static String getSchedData(Object object, String keys) throws RemoteException {
        if (keys == null) {
            AwareLog.e(TAG, "getSchedData input keys null!");
            return null;
        } else if (object == null || !(object instanceof ISched)) {
            return null;
        } else {
            return ((ISched) object).get_sched_data(keys);
        }
    }

    public static int preloadApplication(String pkg, int userId) {
        if (pkg != null) {
            return HwActivityManager.preloadApplication(pkg, userId);
        }
        AwareLog.e(TAG, "pkg null");
        return -1;
    }

    public static int getIntForUser(ContentResolver cr, String name, int userHandle) {
        if (cr == null) {
            AwareLog.e(TAG, "contentResolver is null");
            return -1;
        }
        try {
            int ret = Settings.System.getIntForUser(cr, name, userHandle);
            AwareLog.i(TAG, "getIntForUser : " + ret);
            return ret;
        } catch (Settings.SettingNotFoundException e) {
            AwareLog.e(TAG, "unable to get settings");
            return -1;
        }
    }

    public static Object getPerfPolicyService(String str) throws RemoteException {
        if (str != null) {
            return IPerfPolicy.getService(str);
        }
        return null;
    }

    public static int updatePerfConfig(Object perfPolicy, String str, int len, int status) throws RemoteException {
        if (perfPolicy == null || !(perfPolicy instanceof IPerfPolicy)) {
            return -1;
        }
        return ((IPerfPolicy) perfPolicy).updatePerfConfig(str, len, status);
    }

    public static void setCleartextTrafficPermitted(boolean permitted) {
        NetworkSecurityPolicy.getInstance().setCleartextTrafficPermitted(permitted);
    }

    public static Object asInterfaceIPWM(IBinder pwmBinder) {
        return IPowerManager.Stub.asInterface(pwmBinder);
    }

    public static void setLcdDisplay(Object obj, int value) {
        if (obj != null && (obj instanceof IPowerManager)) {
            try {
                ((IPowerManager) obj).setMaxBrightnessFromThermal(value);
            } catch (RemoteException e) {
                AwareLog.e(TAG, "RemoteException setMaxBrightnessFromThermal");
            }
        }
    }

    public static void setLcdNitData(Object obj, String str, Bundle data) {
        if (obj != null && (obj instanceof IPowerManager)) {
            try {
                ((IPowerManager) obj).hwBrightnessSetData(str, data);
            } catch (RemoteException e) {
                AwareLog.e(TAG, "RemoteException setLcdNitData");
            }
        }
    }

    public static int setShutdownFlag(int flag) {
        try {
            return HwBootAnimationOeminfo.setBootChargeShutFlag(flag);
        } catch (Exception e) {
            AwareLog.e(TAG, "Exception setBootChargeShutFlag");
            return -1;
        }
    }

    public static boolean limitSpeed(int mode, int reserve1, int reserve2) {
        HwInnerWifiManager manager = HwFrameworkFactory.getHwInnerWifiManager();
        if (manager != null) {
            return manager.updateLimitSpeedStatus(mode, reserve1, reserve2);
        }
        return false;
    }

    public static Object getTelephonyService() {
        return IHwTelephony.Stub.asInterface(ServiceManager.getService("phone_huawei"));
    }

    public static boolean setTemperatureControlToModem(Object obj, int level, int type) {
        if (obj != null) {
            try {
                if (obj instanceof IHwTelephony) {
                    return ((IHwTelephony) obj).setTemperatureControlToModem(level, type, ((IHwTelephony) obj).getDefault4GSlotId(), (Message) null);
                }
            } catch (RemoteException e) {
                AwareLog.e(TAG, "RemoteException " + e.getMessage());
                return false;
            } catch (NoSuchMethodError e2) {
                AwareLog.e(TAG, "NoSuchMethodError: " + e2.getMessage());
                return false;
            }
        }
        AwareLog.e(TAG, "invalid IHwTelephony service.");
        return false;
    }

    public static boolean isPCMode() {
        return HwPCUtils.isPcCastMode();
    }

    public static void eRecoveryReportERec(long eRecoveryId, long faultId) {
        ERecoveryEvent event = new ERecoveryEvent();
        event.setERecoveryID(eRecoveryId);
        event.setFaultID(faultId);
        event.setState(3);
        event.setResult((long) INVALID_LONG);
        ERecovery.eRecoveryReport(event);
    }

    public static void reportTopADataRDA(Bundle bdl) {
        HwSysResManager.getInstance().reportTopAData(bdl);
    }

    public static void reportCloudUpdateRDA(Bundle bundle) {
        HwSysResManager.getInstance().reportCloudUpdate(bundle);
    }

    public static boolean isFoldable() {
        return HwFoldScreenManagerEx.isFoldable();
    }

    public static boolean isWearable(Context context) {
        PackageManager pManager;
        if (context == null || (pManager = context.getPackageManager()) == null) {
            return false;
        }
        return pManager.hasSystemFeature("android.hardware.type.watch");
    }

    public static int getFoldableState() {
        try {
            return HwFoldScreenManagerEx.getFoldableState();
        } catch (RuntimeException e) {
            AwareLog.e(TAG, "RuntimeException getFoldableState: " + e.getMessage());
            return -1;
        }
    }

    public static void reportSceneInfosRDA(Bundle bdl) {
        HwSysResManager.getInstance().reportSceneInfos(bdl);
    }

    public static List<String> getCtsPkgs() {
        return HwSysResManager.getInstance().getCtsPkgs();
    }
}
