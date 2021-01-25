package com.android.server.wm;

import android.app.ActivityManager;
import android.app.IApplicationThread;
import android.app.ProfilerInfo;
import android.content.ComponentName;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.service.voice.IVoiceInteractionSession;
import android.util.SparseIntArray;
import android.util.proto.ProtoOutputStream;
import com.android.internal.app.IVoiceInteractor;
import com.android.server.am.PendingIntentRecord;
import com.android.server.am.UserState;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Set;

public abstract class ActivityTaskManagerInternal {
    public static final int APP_TRANSITION_RECENTS_ANIM = 5;
    public static final int APP_TRANSITION_SNAPSHOT = 4;
    public static final int APP_TRANSITION_SPLASH_SCREEN = 1;
    public static final int APP_TRANSITION_TIMEOUT = 3;
    public static final int APP_TRANSITION_WINDOWS_DRAWN = 2;
    public static final String ASSIST_ACTIVITY_ID = "activityId";
    public static final String ASSIST_KEY_CONTENT = "content";
    public static final String ASSIST_KEY_DATA = "data";
    public static final String ASSIST_KEY_RECEIVER_EXTRAS = "receiverExtras";
    public static final String ASSIST_KEY_STRUCTURE = "structure";
    public static final String ASSIST_TASK_ID = "taskId";

    public interface ScreenObserver {
        void onAwakeStateChanged(boolean z);

        void onKeyguardStateChanged(boolean z);
    }

    public static abstract class SleepToken {
        public abstract void release();
    }

    public abstract SleepToken acquireSleepToken(String str, int i);

    public abstract boolean attachApplication(WindowProcessController windowProcessController) throws RemoteException;

    public abstract boolean canGcNow();

    public abstract boolean canShowErrorDialogs();

    public abstract void cancelRecentsAnimation(boolean z);

    public abstract void cleanupDisabledPackageComponents(String str, Set<String> set, int i, boolean z);

    public abstract void cleanupRecentTasksForUser(int i);

    public abstract void clearHeavyWeightProcessIfEquals(WindowProcessController windowProcessController);

    public abstract void clearLockedTasks(String str);

    public abstract void clearPendingResultForActivity(IBinder iBinder, WeakReference<PendingIntentRecord> weakReference);

    public abstract void clearSavedANRState();

    public abstract void closeSystemDialogs(String str);

    public abstract CompatibilityInfo compatibilityInfoForPackage(ApplicationInfo applicationInfo);

    public abstract void dismissSplitScreenMode(boolean z);

    public abstract void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr, int i, boolean z, boolean z2, String str2);

    public abstract boolean dumpActivity(FileDescriptor fileDescriptor, PrintWriter printWriter, String str, String[] strArr, int i, boolean z, boolean z2, boolean z3);

    public abstract void dumpForOom(PrintWriter printWriter);

    public abstract boolean dumpForProcesses(FileDescriptor fileDescriptor, PrintWriter printWriter, boolean z, String str, int i, boolean z2, boolean z3, int i2);

    public abstract void enableScreenAfterBoot(boolean z);

    public abstract void enforceCallerIsRecentsOrHasPermission(String str, String str2);

    public abstract void enterCoordinationMode();

    public abstract void exitCoordinationMode();

    public abstract boolean exitCoordinationMode(boolean z, boolean z2);

    public abstract void finishHeavyWeightApp();

    public abstract int finishTopCrashedActivities(WindowProcessController windowProcessController, String str);

    public abstract void flushRecentTasks();

    public abstract ComponentName getHomeActivityForUser(int i);

    public abstract Intent getHomeIntent();

    public abstract WindowProcessController getHomeProcess();

    public abstract IIntentSender getIntentSender(int i, String str, int i2, int i3, IBinder iBinder, String str2, int i4, Intent[] intentArr, String[] strArr, int i5, Bundle bundle);

    public abstract ActivityInfo getLastResumedActivity();

    public abstract ActivityMetricsLaunchObserverRegistry getLaunchObserverRegistry();

    public abstract WindowProcessController getPreviousProcess();

    public abstract ActivityServiceConnectionsHolder getServiceConnectionsHolder(IBinder iBinder);

    public abstract ActivityManager.TaskSnapshot getTaskSnapshotNoRestore(int i, boolean z);

    public abstract ActivityTokens getTopActivityForTask(int i);

    public abstract WindowProcessController getTopApp();

    public abstract int getTopProcessState();

    public abstract List<IBinder> getTopVisibleActivities();

    public abstract boolean handleAppCrashInActivityController(String str, int i, String str2, String str3, long j, String str4, Runnable runnable);

    public abstract void handleAppDied(WindowProcessController windowProcessController, boolean z, Runnable runnable);

    public abstract boolean isCallerRecents(int i);

    public abstract boolean isFactoryTestProcess(WindowProcessController windowProcessController);

    public abstract boolean isGetTasksAllowed(String str, int i, int i2);

    public abstract boolean isHeavyWeightProcess(WindowProcessController windowProcessController);

    public abstract boolean isRecentsComponentHomeActivity(int i);

    public abstract boolean isShuttingDown();

    public abstract boolean isSleeping();

    public abstract boolean isUidForeground(int i);

    public abstract void loadRecentTasksForUser(int i);

    public abstract void notifyActiveVoiceInteractionServiceChanged(ComponentName componentName);

    public abstract void notifyAppTransitionCancelled();

    public abstract void notifyAppTransitionFinished();

    public abstract void notifyAppTransitionStarting(SparseIntArray sparseIntArray, long j);

    public abstract void notifyDockedStackMinimizedChanged(boolean z);

    public abstract void notifyHoldScreenStateChange(String str, int i, int i2, int i3, String str2);

    public abstract void notifyKeyguardFlagsChanged(Runnable runnable, int i);

    public abstract void notifyKeyguardTrustedChanged();

    public abstract void notifyLockedProfile(int i, int i2);

    public abstract void onActiveUidsCleared();

    public abstract void onCleanUpApplicationRecord(WindowProcessController windowProcessController);

    public abstract boolean onForceStopPackage(String str, boolean z, boolean z2, int i);

    public abstract void onHandleAppCrash(WindowProcessController windowProcessController);

    public abstract void onImeWindowSetOnDisplay(int i, int i2);

    public abstract void onLocalVoiceInteractionStarted(IBinder iBinder, IVoiceInteractionSession iVoiceInteractionSession, IVoiceInteractor iVoiceInteractor);

    public abstract void onPackageAdded(String str, boolean z);

    public abstract void onPackageDataCleared(String str);

    public abstract void onPackageReplaced(ApplicationInfo applicationInfo);

    public abstract void onPackageUninstalled(String str);

    public abstract void onPackagesSuspendedChanged(String[] strArr, boolean z, int i);

    public abstract void onProcessAdded(WindowProcessController windowProcessController);

    public abstract void onProcessMapped(int i, WindowProcessController windowProcessController);

    public abstract void onProcessRemoved(String str, int i);

    public abstract void onProcessUnMapped(int i);

    public abstract void onUidActive(int i, int i2);

    public abstract void onUidAddedToPendingTempWhitelist(int i, String str);

    public abstract void onUidInactive(int i);

    public abstract void onUidProcStateChanged(int i, int i2);

    public abstract void onUidRemovedFromPendingTempWhitelist(int i);

    public abstract void onUserStopped(int i);

    public abstract void preBindApplication(WindowProcessController windowProcessController, Configuration configuration);

    public abstract void rankTaskLayersIfNeeded();

    public abstract void registerScreenObserver(ScreenObserver screenObserver);

    public abstract void removeRecentTasksByPackageName(String str, int i);

    public abstract void removeUser(int i);

    public abstract void resumeTopActivities(boolean z);

    public abstract void saveANRState(String str);

    public abstract void scheduleDestroyAllActivities(String str);

    public abstract void sendActivityResult(int i, IBinder iBinder, String str, int i2, int i3, Intent intent);

    public abstract void setAllowAppSwitches(String str, int i, int i2);

    public abstract void setCompanionAppPackages(int i, Set<String> set);

    public abstract void setDeviceOwnerUid(int i);

    public abstract void setExpandScreenTurningOn(boolean z);

    public abstract void setFocusedActivity(IBinder iBinder);

    public abstract void setProfileApp(String str);

    public abstract void setProfileProc(WindowProcessController windowProcessController);

    public abstract void setProfilerInfo(ProfilerInfo profilerInfo);

    public abstract void setVr2dDisplayId(int i);

    public abstract boolean showStrictModeViolationDialog();

    public abstract void showSystemReadyErrorDialogsIfNeeded();

    public abstract boolean shuttingDown(boolean z, int i);

    public abstract int startActivitiesAsPackage(String str, int i, Intent[] intentArr, Bundle bundle);

    public abstract int startActivitiesInPackage(int i, int i2, int i3, String str, Intent[] intentArr, String[] strArr, IBinder iBinder, SafeActivityOptions safeActivityOptions, int i4, boolean z, PendingIntentRecord pendingIntentRecord, boolean z2);

    public abstract int startActivityAsUser(IApplicationThread iApplicationThread, String str, Intent intent, Bundle bundle, int i);

    public abstract int startActivityInPackage(int i, int i2, int i3, String str, Intent intent, String str2, IBinder iBinder, String str3, int i4, int i5, SafeActivityOptions safeActivityOptions, int i6, TaskRecord taskRecord, String str4, boolean z, PendingIntentRecord pendingIntentRecord, boolean z2);

    public abstract void startConfirmDeviceCredentialIntent(Intent intent, Bundle bundle);

    public abstract boolean startHomeActivity(int i, String str);

    public abstract boolean startHomeOnAllDisplays(int i, String str);

    public abstract boolean startHomeOnDisplay(int i, String str, int i2, boolean z, boolean z2);

    public abstract boolean switchUser(int i, UserState userState);

    public abstract void updateTopComponentForFactoryTest();

    public abstract void updateUserConfiguration();

    public abstract void writeActivitiesToProto(ProtoOutputStream protoOutputStream);

    public abstract void writeProcessesToProto(ProtoOutputStream protoOutputStream, String str, int i, boolean z);

    public final class ActivityTokens {
        private final IBinder mActivityToken;
        private final IApplicationThread mAppThread;
        private final IBinder mAssistToken;

        public ActivityTokens(IBinder activityToken, IBinder assistToken, IApplicationThread appThread) {
            this.mActivityToken = activityToken;
            this.mAssistToken = assistToken;
            this.mAppThread = appThread;
        }

        public IBinder getActivityToken() {
            return this.mActivityToken;
        }

        public IBinder getAssistToken() {
            return this.mAssistToken;
        }

        public IApplicationThread getApplicationThread() {
            return this.mAppThread;
        }
    }
}
