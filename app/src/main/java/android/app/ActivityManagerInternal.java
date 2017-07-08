package android.app;

import android.content.ComponentName;
import android.content.IIntentSender;
import android.os.IBinder;
import android.rog.AppRogInfo;
import android.rog.IHwRogListener;
import android.service.voice.IVoiceInteractionSession;
import com.android.internal.app.IVoiceInteractor;
import java.util.List;

public abstract class ActivityManagerInternal {
    public static final int APP_TRANSITION_SAVED_SURFACE = 0;
    public static final int APP_TRANSITION_STARTING_WINDOW = 1;
    public static final int APP_TRANSITION_TIMEOUT = 3;
    public static final int APP_TRANSITION_WINDOWS_DRAWN = 2;

    public static abstract class SleepToken {
        public abstract void release();
    }

    public abstract SleepToken acquireSleepToken(String str);

    public abstract String checkContentProviderAccess(String str, int i);

    public abstract ComponentName getHomeActivityForUser(int i);

    public abstract int getPackageScreenCompatMode(String str);

    public abstract List<IBinder> getTopVisibleActivities();

    public abstract void killForegroundAppsForUser(int i);

    public abstract void notifyAppTransitionCancelled();

    public abstract void notifyAppTransitionFinished();

    public abstract void notifyAppTransitionStarting(int i);

    public abstract void notifyDockedStackMinimizedChanged(boolean z);

    public abstract void notifyRogInfoUpdated(IHwRogListener iHwRogListener, AppRogInfo appRogInfo);

    public abstract void notifyRogSwitchStateChanged(IHwRogListener iHwRogListener, boolean z, AppRogInfo appRogInfo);

    public abstract void notifyStartingWindowDrawn();

    public abstract void onLocalVoiceInteractionStarted(IBinder iBinder, IVoiceInteractionSession iVoiceInteractionSession, IVoiceInteractor iVoiceInteractor);

    public abstract void onUserRemoved(int i);

    public abstract void onWakefulnessChanged(int i);

    public abstract void setPendingIntentWhitelistDuration(IIntentSender iIntentSender, long j);

    public abstract int startIsolatedProcess(String str, String[] strArr, String str2, String str3, int i, Runnable runnable);
}
