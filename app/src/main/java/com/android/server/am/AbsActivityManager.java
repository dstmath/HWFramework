package com.android.server.am;

import android.app.ActivityManagerNative;
import android.app.AppGlobals;
import android.app.IApplicationThread;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Configuration;
import android.os.RemoteException;
import com.android.server.AlarmManagerService;
import com.android.server.util.AbsUserBehaviourRecord;
import java.util.HashSet;
import java.util.List;

public abstract class AbsActivityManager extends ActivityManagerNative {

    static final class AppDiedInfo {
        public int callerPid;
        public boolean isClone;
        public String processName;
        public String reason;
        public int userId;

        public AppDiedInfo(int userId, String processName, boolean isClone, int callerPid, String reason) {
            this.userId = userId;
            this.processName = processName;
            this.isClone = isClone;
            this.callerPid = callerPid;
            this.reason = reason;
        }

        public String toString() {
            return "AppDiedInfo [userId=" + this.userId + ", processName= " + this.processName + ", isClone=" + this.isClone + ", callerPid=" + this.callerPid + ", reason=" + this.reason + "]";
        }
    }

    public boolean handleANRFilterFIFO(int uid, int cmd) {
        return false;
    }

    public void smartTrimAddProcessRelation_HwSysM(ContentProviderConnection conn) {
    }

    public void smartTrimAddProcessRelation_HwSysM(AppBindRecord server, AppBindRecord client) {
    }

    public void addCallerToIntent(Intent intent, IApplicationThread caller) {
    }

    protected void startPushService() {
    }

    protected void hwTrimApk() {
    }

    protected void smartTrimAddProcessRelation(String clientProc, int clientCurAdj, HashSet<String> hashSet, String serverProc, int serverCurAdj, HashSet<String> hashSet2) {
    }

    public Configuration getCurNaviConfiguration() {
        return null;
    }

    protected void setFocusedActivityLockedForNavi(ActivityRecord r) {
    }

    public String topAppName() {
        return null;
    }

    public void showUninstallLauncherDialog(String pkgName) {
    }

    public void updateCpusetSwitch() {
    }

    public boolean setCurProcessGroup(ProcessRecord app, int schedGroup) {
        return false;
    }

    public void setWhiteListProcessGroup(ProcessRecord app, ProcessRecord TOP_APP, boolean bConnectTopApp) {
    }

    public void notifyAppEventToIaware(int duration, String packageName) {
    }

    public long proxyBroadcast(List<String> list, boolean proxy) {
        return -1;
    }

    public long proxyBroadcastByPid(List<Integer> list, boolean proxy) {
        return -1;
    }

    public void setProxyBCActions(List<String> list) {
    }

    public void setActionExcludePkg(String action, String pkg) {
    }

    public void proxyBCConfig(int type, String key, List<String> list) {
    }

    public void checkIfScreenStatusRequestAndSendBroadcast() {
    }

    public void startupFilterReceiverList(Intent intent, List<ResolveInfo> list) {
    }

    public boolean shouldPreventStartService(ServiceInfo servInfo, int callerPid, int callerUid) {
        return false;
    }

    public boolean shouldPreventStartService(ServiceInfo sInfo, int callerUid, int callerPid, String callerPackage, int userId) {
        return false;
    }

    public boolean shouldPreventStartActivity(ActivityInfo aInfo, int callerUid, int callerPid, String callerPackage, int userId) {
        return false;
    }

    public boolean shouldPreventActivity(Intent intent, ActivityInfo aInfo, ActivityRecord record) {
        return false;
    }

    public boolean shouldPreventStartProvider(ProviderInfo cpi, int callerUid, int callerPid, String callerPackage, int userId) {
        return false;
    }

    public boolean shouldPreventSendBroadcast(Intent intent, String receiver, int callerUid, int callerPid, String callerPackage, int userId) {
        return false;
    }

    protected void checkToCloseTrustSpace(ActivityRecord next) {
    }

    protected void checkToStartTrustSpace(int userId) {
    }

    public boolean shouldPreventRestartService(String pkgName) {
        return false;
    }

    public boolean shouldPreventStartProvider(ProviderInfo cpi, int callerPid, int callerUid) {
        return false;
    }

    protected boolean customActivityStarting(Intent intent, String packageName) {
        return false;
    }

    protected boolean customActivityResuming(String packageName) {
        return false;
    }

    protected BroadcastQueue[] initialBroadcastQueue() {
        return new BroadcastQueue[2];
    }

    protected void setThirdPartyAppBroadcastQueue(BroadcastQueue[] broadcastQueues) {
    }

    protected boolean isThirdPartyAppBroadcastQueue(ProcessRecord callerApp) {
        return false;
    }

    protected boolean isThirdPartyAppFGBroadcastQueue(BroadcastQueue queue) {
        return false;
    }

    protected boolean isThirdPartyAppPendingBroadcastProcessLocked(int pid) {
        return false;
    }

    protected BroadcastQueue thirdPartyAppBroadcastQueueForIntent(Intent intent) {
        return null;
    }

    protected void setKeyAppBroadcastQueue(BroadcastQueue[] broadcastQueues) {
    }

    protected boolean isKeyAppBroadcastQueue(int type, String name) {
        return false;
    }

    protected boolean isKeyAppFGBroadcastQueue(BroadcastQueue queue) {
        return false;
    }

    protected boolean isKeyAppPendingBroadcastProcessLocked(int pid) {
        return false;
    }

    protected BroadcastQueue keyAppBroadcastQueueForIntent(Intent intent) {
        return null;
    }

    public boolean getIawareResourceFeature(int type) {
        return false;
    }

    public boolean isKeyApp(int type, int value, String key) {
        return false;
    }

    protected void initBroadcastResourceLocked() {
    }

    protected void checkBroadcastRecordSpeed(int callingUid, String callerPackage, ProcessRecord callerApp) {
    }

    protected void clearBroadcastResource(ProcessRecord app) {
    }

    public AbsUserBehaviourRecord getRecordCust() {
        return null;
    }

    public void initAppAndAppServiceResourceLocked() {
    }

    public boolean isAcquireAppServiceResourceLocked(ServiceRecord sr, ProcessRecord app) {
        return true;
    }

    public boolean isAcquireAppResourceLocked(ProcessRecord app) {
        return true;
    }

    public void clearAppAndAppServiceResource(ProcessRecord app) {
    }

    public void checkOrderedBroadcastTimeoutLocked(String actionOrPkg, int timeCost, boolean isInToOut) {
    }

    public void updateSRMSStatisticsData(int subTypeCode) {
    }

    protected int setSoundEffectState(boolean restore, String packageName, boolean isOnTop, String reserved) {
        return 0;
    }

    protected void registerCtrlSocketForMm(String processname, int pid) {
    }

    protected void unregisterCtrlSocketForMm(String processname) {
    }

    protected void notifyProcessGroupChange(int pid, int uid) {
    }

    protected void notifyProcessGroupChange(int pid, int uid, int grp) {
    }

    public boolean bootSceneStart(int sceneId, long maxTime) {
        return true;
    }

    public boolean bootSceneEnd(int sceneId) {
        return true;
    }

    protected void reportServiceRelationIAware(int relationType, ServiceRecord r, ProcessRecord caller) {
    }

    protected void reportPreviousInfo(int relationType, ProcessRecord r) {
    }

    protected void reportServiceRelationIAware(int relationType, ContentProviderRecord r, ProcessRecord caller) {
    }

    protected void reportHomeProcess(ProcessRecord homeProcess) {
    }

    protected void noteActivityStart(String packageName, String processName, int pid, int uid, boolean started) {
    }

    protected void noteProcessStart(String packageName, String processName, int pid, int uid, boolean started, String launcherMode, String reason) {
    }

    protected void noteProcessStop(String packageName, String processName, int pid, int uid, String exitMode, String reason) {
    }

    void reportActivityStartFinished() {
    }

    public void setAlarmManagerExt(AlarmManagerService service) {
    }

    protected void cleanupAlarmLockedExt(ProcessRecord process) {
    }

    protected void forceValidateHomeButton() {
    }

    protected boolean isStartLauncherActivity(Intent intent) {
        return false;
    }

    public boolean isClonedProcess(int pid) {
        return false;
    }

    public boolean isPackageCloned(String packageName, int userId) {
        return false;
    }

    protected List<ResolveInfo> queryIntentReceivers(ProcessRecord callerApp, Intent intent, String resolvedType, int flags, int userId) throws RemoteException {
        return AppGlobals.getPackageManager().queryIntentReceivers(intent, resolvedType, flags, userId).getList();
    }

    protected void filterRegisterReceiversForEuid(List<BroadcastFilter> list, ProcessRecord callerApp) {
    }

    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
    }

    public boolean isLimitedPackageBroadcast(Intent intent) {
        return false;
    }

    void reportAppDiedMsg(AppDiedInfo appDiedInfo) {
    }

    boolean shouldNotKillProcWhenRemoveTask(String pkg) {
        return false;
    }
}
