package com.android.server.am;

import android.app.IActivityManager.Stub;
import android.app.IApplicationThread;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.os.IBinder;
import com.android.server.AlarmManagerService;
import com.android.server.util.AbsUserBehaviourRecord;
import com.huawei.android.app.IGameObserver;
import java.util.HashSet;
import java.util.List;

public abstract class AbsActivityManager extends Stub {

    static final class AppDiedInfo {
        public int callerPid;
        public String processName;
        public String reason;
        public int userId;

        public AppDiedInfo(int userId, String processName, int callerPid, String reason) {
            this.userId = userId;
            this.processName = processName;
            this.callerPid = callerPid;
            this.reason = reason;
        }

        public String toString() {
            return "AppDiedInfo [userId=" + this.userId + ", processName= " + this.processName + ", callerPid=" + this.callerPid + ", reason=" + this.reason + "]";
        }
    }

    static final class AppInfo {
        public String activityName;
        public String packageName;
        public int pid;
        public String processName;
        public int uid;

        public AppInfo(String packageName, String processName, String activityName, int pid, int uid) {
            this.packageName = packageName;
            this.processName = processName;
            this.activityName = activityName;
            this.pid = pid;
            this.uid = uid;
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

    public boolean shouldPreventSendReceiver(Intent intent, ResolveInfo resolveInfo, int callerPid, int callerUid, ProcessRecord targetApp, ProcessRecord callerApp) {
        return false;
    }

    public boolean shouldPreventStartService(ServiceInfo servInfo, int callerPid, int callerUid, ProcessRecord callerApp, boolean servExist, Intent service) {
        return false;
    }

    public void setServiceFlagLocked(int servFlag) {
    }

    public boolean shouldPreventStartService(ServiceInfo sInfo, int callerUid, int callerPid, String callerPackage, int userId) {
        return false;
    }

    public boolean shouldPreventStartActivity(ActivityInfo aInfo, int callerUid, int callerPid, String callerPackage, int userId) {
        return false;
    }

    public boolean shouldPreventActivity(Intent intent, ActivityInfo aInfo, ActivityRecord record, int callerPid, int callerUid, ProcessRecord callerApp) {
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

    public boolean shouldPreventRestartService(ServiceInfo sInfo, boolean realStart) {
        return false;
    }

    public void recognizeFakeActivity(String compName, int pid, int uid) {
    }

    public boolean shouldPreventStartProvider(ProviderInfo cpi, int callerPid, int callerUid, ProcessRecord callerApp) {
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

    protected void forceGCAfterRebooting() {
        throw new RuntimeException("should call subclass HwActivityManagerService.forceGC.AfterRebooting");
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

    protected void notifyProcessGroupChange(int pid, int uid) {
    }

    protected void notifyProcessGroupChange(int pid, int uid, int grp) {
    }

    protected void notifyProcessDied(int pid, int uid) {
    }

    protected void reportServiceRelationIAware(int relationType, ServiceRecord r, ProcessRecord caller) {
    }

    protected void reportPreviousInfo(int relationType, ProcessRecord r) {
    }

    protected void reportServiceRelationIAware(int relationType, ContentProviderRecord r, ProcessRecord caller) {
    }

    protected void reportHomeProcess(ProcessRecord homeProcess) {
    }

    protected void noteActivityStart(AppInfo appInfo, boolean started) {
    }

    protected void noteProcessStart(String packageName, String processName, int pid, int uid, boolean started, String launcherMode, String reason) {
    }

    protected void noteProcessStop(String packageName, String processName, int pid, int uid, String exitMode, String reason) {
    }

    public void setAlarmManagerExt(AlarmManagerService service) {
    }

    protected void cleanupAlarmLockedExt(ProcessRecord process) {
    }

    protected void forceValidateHomeButton(int userId) {
    }

    protected boolean isStartLauncherActivity(Intent intent, int userId) {
        return false;
    }

    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
    }

    public boolean isLimitedPackageBroadcast(Intent intent) {
        return false;
    }

    void reportAppDiedMsg(AppDiedInfo appDiedInfo) {
    }

    public void setExitPosition(int startX, int startY, int width, int height) {
    }

    protected void checkAndPrintTestModeLog(List list, String intentAction, String callingMethod, String desciption) {
    }

    public void dispatchActivityResumed(IBinder token) {
    }

    public void dispatchActivityPaused(IBinder token) {
    }

    public boolean isHiddenSpaceSwitch(UserInfo first, UserInfo second) {
        return false;
    }

    public void cleanAppForHiddenSpace() {
    }

    protected int[] handleGidsForUser(int[] gids, int userId) {
        return gids;
    }

    protected int handleUserForClone(String name, int userId) {
        return userId;
    }

    void setThreadSchedPolicy(int oldSchedGroup, ProcessRecord app) {
    }

    void setVipThread(ProcessRecord proc) {
    }

    protected void notifyProcessWillDie(boolean byForceStop, boolean crashed, boolean byAnr, String packageName, int pid, int uid) {
    }

    public boolean addGameSpacePackageList(List<String> list) {
        return false;
    }

    public boolean delGameSpacePackageList(List<String> list) {
        return false;
    }

    public boolean isInGameSpace(String packageName) {
        return false;
    }

    public List<String> getGameList() {
        return null;
    }

    public void registerGameObserver(IGameObserver observer) {
    }

    public void unregisterGameObserver(IGameObserver observer) {
    }

    public boolean isGameDndOn() {
        return false;
    }

    public boolean isGameKeyControlOn() {
        return false;
    }

    public boolean isGameGestureDisabled() {
        return false;
    }
}
