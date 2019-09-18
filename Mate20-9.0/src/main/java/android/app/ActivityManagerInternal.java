package android.app;

import android.content.ComponentName;
import android.content.IIntentSender;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.service.voice.IVoiceInteractionSession;
import android.util.SparseIntArray;
import com.android.internal.app.IVoiceInteractor;
import java.util.List;

public abstract class ActivityManagerInternal {
    public static final int APP_TRANSITION_RECENTS_ANIM = 5;
    public static final int APP_TRANSITION_SNAPSHOT = 4;
    public static final int APP_TRANSITION_SPLASH_SCREEN = 1;
    public static final int APP_TRANSITION_TIMEOUT = 3;
    public static final int APP_TRANSITION_WINDOWS_DRAWN = 2;
    public static final String ASSIST_KEY_CONTENT = "content";
    public static final String ASSIST_KEY_DATA = "data";
    public static final String ASSIST_KEY_RECEIVER_EXTRAS = "receiverExtras";
    public static final String ASSIST_KEY_STRUCTURE = "structure";

    public interface ScreenObserver {
        void onAwakeStateChanged(boolean z);

        void onKeyguardStateChanged(boolean z);
    }

    public static abstract class SleepToken {
        public abstract void release();
    }

    public abstract SleepToken acquireSleepToken(String str, int i);

    public abstract boolean canStartMoreUsers();

    public abstract void cancelRecentsAnimation(boolean z);

    public abstract String checkContentProviderAccess(String str, int i);

    public abstract void clearSavedANRState();

    public abstract void enforceCallerIsRecentsOrHasPermission(String str, String str2);

    public abstract boolean exitCoordinationModeInner(boolean z, boolean z2);

    public abstract ComponentName getHomeActivityForUser(int i);

    public abstract ActivityInfo getLastResumedActivity();

    public abstract int getMaxRunningUsers();

    public abstract List<ProcessMemoryState> getMemoryStateForProcesses();

    public abstract List<IBinder> getTopVisibleActivities();

    public abstract int getUidProcessState(int i);

    public abstract void grantUriPermissionFromIntent(int i, String str, Intent intent, int i2);

    public abstract int handleUserForClone(String str, int i);

    public abstract boolean hasRunningActivity(int i, String str);

    public abstract boolean isCallerRecents(int i);

    public abstract boolean isRecentsComponentHomeActivity(int i);

    public abstract boolean isRuntimeRestarted();

    public abstract boolean isSystemReady();

    public abstract boolean isUidActive(int i);

    public abstract void killForegroundAppsForUser(int i);

    public abstract void notifyActiveVoiceInteractionServiceChanged(ComponentName componentName);

    public abstract void notifyAppTransitionCancelled();

    public abstract void notifyAppTransitionFinished();

    public abstract void notifyAppTransitionStarting(SparseIntArray sparseIntArray, long j);

    public abstract void notifyDockedStackMinimizedChanged(boolean z);

    public abstract void notifyKeyguardFlagsChanged(Runnable runnable);

    public abstract void notifyKeyguardTrustedChanged();

    public abstract void notifyNetworkPolicyRulesUpdated(int i, long j);

    public abstract void onLocalVoiceInteractionStarted(IBinder iBinder, IVoiceInteractionSession iVoiceInteractionSession, IVoiceInteractor iVoiceInteractor);

    public abstract void onUserRemoved(int i);

    public abstract void onWakefulnessChanged(int i);

    public abstract void registerScreenObserver(ScreenObserver screenObserver);

    public abstract void saveANRState(String str);

    public abstract void setAllowAppSwitches(String str, int i, int i2);

    public abstract void setDeviceIdleWhitelist(int[] iArr, int[] iArr2);

    public abstract void setFocusedActivity(IBinder iBinder);

    public abstract void setHasOverlayUi(int i, boolean z);

    public abstract void setPendingIntentWhitelistDuration(IIntentSender iIntentSender, IBinder iBinder, long j);

    public abstract void setRunningRemoteAnimation(int i, boolean z);

    public abstract void setSwitchingFromSystemUserMessage(String str);

    public abstract void setSwitchingToSystemUserMessage(String str);

    public abstract void setVr2dDisplayId(int i);

    public abstract int startActivitiesAsPackage(String str, int i, Intent[] intentArr, Bundle bundle);

    public abstract int startActivityAsUser(IApplicationThread iApplicationThread, String str, Intent intent, Bundle bundle, int i);

    public abstract boolean startIsolatedProcess(String str, String[] strArr, String str2, String str3, int i, Runnable runnable);

    public abstract void updateDeviceIdleTempWhitelist(int[] iArr, int i, boolean z);

    public abstract void updatePersistentConfigurationForUser(Configuration configuration, int i);
}
