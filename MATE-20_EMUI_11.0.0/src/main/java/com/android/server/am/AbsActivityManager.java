package com.android.server.am;

import android.app.IActivityManager;
import android.content.Intent;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Configuration;
import android.os.IBinder;
import com.android.server.wm.ActivityRecord;
import com.huawei.android.app.IGameObserver;
import java.util.HashSet;
import java.util.List;

public abstract class AbsActivityManager extends IActivityManager.Stub {
    /* access modifiers changed from: protected */
    public void hwTrimApk() {
    }

    /* access modifiers changed from: protected */
    public void smartTrimAddProcessRelation(String clientProc, int clientCurAdj, HashSet<String> hashSet, String serverProc, int serverCurAdj, HashSet<String> hashSet2) {
    }

    public Configuration getCurNaviConfiguration() {
        return null;
    }

    public long proxyBroadcast(List<String> list, boolean proxy) {
        return -1;
    }

    public long proxyBroadcastByPid(List<Integer> list, boolean proxy) {
        return -1;
    }

    public void setProxyBroadcastActions(List<String> list) {
    }

    public void setActionExcludePkg(String action, String pkg) {
    }

    public void proxyBroadcastConfig(int type, String key, List<String> list) {
    }

    public void checkIfScreenStatusRequestAndSendBroadcast() {
    }

    public boolean shouldPreventStartService(ServiceInfo sInfo, int callerUid, int callerPid, String callerPackage, int userId) {
        return false;
    }

    public boolean shouldPreventStartProvider(ProviderInfo cpi, int callerUid, int callerPid, String callerPackage, int userId) {
        return false;
    }

    public boolean shouldPreventSendBroadcast(Intent intent, String receiver, int callerUid, int callerPid, String callerPackage, int userId) {
        return false;
    }

    /* access modifiers changed from: protected */
    public void checkToCloseTrustSpace(ActivityRecord next) {
    }

    /* access modifiers changed from: protected */
    public void checkToStartTrustSpace(int userId) {
    }

    public boolean shouldPreventRestartService(ServiceInfo sInfo, boolean realStart) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean customActivityStarting(Intent intent, String packageName) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean customActivityResuming(String packageName) {
        return false;
    }

    /* access modifiers changed from: protected */
    public BroadcastQueue[] initialBroadcastQueue() {
        return new BroadcastQueue[2];
    }

    /* access modifiers changed from: protected */
    public void setThirdPartyAppBroadcastQueue(BroadcastQueue[] broadcastQueues) {
    }

    /* access modifiers changed from: protected */
    public boolean isThirdPartyAppBroadcastQueue(ProcessRecord callerApp) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isThirdPartyAppPendingBroadcastProcessLocked(int pid) {
        return false;
    }

    /* access modifiers changed from: protected */
    public BroadcastQueue thirdPartyAppBroadcastQueueForIntent(Intent intent) {
        return null;
    }

    /* access modifiers changed from: protected */
    public void setKeyAppBroadcastQueue(BroadcastQueue[] broadcastQueues) {
    }

    /* access modifiers changed from: protected */
    public boolean isKeyAppBroadcastQueue(int type, String name) {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean isKeyAppPendingBroadcastProcessLocked(int pid) {
        return false;
    }

    /* access modifiers changed from: protected */
    public BroadcastQueue keyAppBroadcastQueueForIntent(Intent intent) {
        return null;
    }

    public boolean getIawareResourceFeature(int type) {
        return false;
    }

    public boolean isKeyApp(int type, int value, String key) {
        return false;
    }

    /* access modifiers changed from: protected */
    public void initBroadcastResourceLocked() {
    }

    /* access modifiers changed from: protected */
    public void checkBroadcastRecordSpeed(int callingUid, String callerPackage, ProcessRecord callerApp) {
    }

    /* access modifiers changed from: protected */
    public void clearBroadcastResource(ProcessRecord app) {
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

    /* access modifiers changed from: protected */
    public void notifyProcessDied(int pid, int uid) {
    }

    /* access modifiers changed from: protected */
    public void noteProcessStop(String packageName, String processName, int pid, int uid, String exitMode, String reason) {
    }

    public void dispatchActivityResumed(IBinder token) {
    }

    public void dispatchActivityPaused(IBinder token) {
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

    public boolean isGameKeyControlOn() {
        return false;
    }

    public boolean isGameGestureDisabled() {
        return false;
    }

    /* access modifiers changed from: protected */
    public BroadcastQueue getProcessBroadcastQueue(ProcessRecord callerApp, String callerPackage, Intent intent) {
        return null;
    }
}
