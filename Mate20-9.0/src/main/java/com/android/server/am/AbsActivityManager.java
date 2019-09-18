package com.android.server.am;

import android.app.IActivityManager;
import android.app.IApplicationThread;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.res.Configuration;
import com.huawei.android.app.IGameObserver;
import java.util.List;

public abstract class AbsActivityManager extends IActivityManager.Stub {
    public void addCallerToIntent(Intent intent, IApplicationThread caller) {
    }

    public Configuration getCurNaviConfiguration() {
        return null;
    }

    public String topAppName() {
        return null;
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

    public boolean shouldPreventRestartService(ServiceInfo sInfo, boolean realStart) {
        return false;
    }

    public boolean shouldPreventStartProvider(ProviderInfo cpi, int callerPid, int callerUid, ProcessRecord callerApp) {
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
    public void forceValidateHomeButton(int userId) {
    }

    /* access modifiers changed from: protected */
    public boolean isStartLauncherActivity(Intent intent, int userId) {
        return false;
    }

    public void setExitPosition(int startX, int startY, int width, int height) {
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
