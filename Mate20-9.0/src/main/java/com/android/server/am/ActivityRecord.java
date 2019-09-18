package com.android.server.am;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.IApplicationThread;
import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.ResultInfo;
import android.app.WindowConfiguration;
import android.app.servertransaction.ActivityConfigurationChangeItem;
import android.app.servertransaction.ActivityLifecycleItem;
import android.app.servertransaction.ActivityRelaunchItem;
import android.app.servertransaction.ClientTransaction;
import android.app.servertransaction.ClientTransactionItem;
import android.app.servertransaction.MoveToDisplayItem;
import android.app.servertransaction.MultiWindowModeChangeItem;
import android.app.servertransaction.NewIntentItem;
import android.app.servertransaction.PauseActivityItem;
import android.app.servertransaction.PipModeChangeItem;
import android.app.servertransaction.ResumeActivityItem;
import android.app.servertransaction.WindowVisibilityItem;
import android.common.HwFrameworkFactory;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.GraphicBuffer;
import android.graphics.Rect;
import android.hardware.display.HwFoldScreenState;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.IBinder;
import android.os.IRemoteCallback;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.service.voice.IVoiceInteractionSession;
import android.text.TextUtils;
import android.util.EventLog;
import android.util.Flog;
import android.util.HwPCUtils;
import android.util.Jlog;
import android.util.Log;
import android.util.MergedConfiguration;
import android.util.Slog;
import android.util.TimeUtils;
import android.util.proto.ProtoOutputStream;
import android.view.AppTransitionAnimationSpec;
import android.view.IAppTransitionAnimationSpecsFuture;
import android.view.IApplicationToken;
import android.view.RemoteAnimationDefinition;
import android.vrsystem.IVRSystemServiceManager;
import com.android.internal.R;
import com.android.internal.app.ResolverActivity;
import com.android.internal.content.ReferrerIntent;
import com.android.internal.util.XmlUtils;
import com.android.server.AttributeCache;
import com.android.server.HwServiceFactory;
import com.android.server.LocalServices;
import com.android.server.am.ActivityStack;
import com.android.server.am.LaunchTimeTracker;
import com.android.server.os.HwBootFail;
import com.android.server.pm.DumpState;
import com.android.server.pm.PackageManagerService;
import com.android.server.wm.AppWindowContainerController;
import com.android.server.wm.AppWindowContainerListener;
import com.android.server.wm.ConfigurationContainer;
import com.android.server.wm.TaskWindowContainerController;
import com.android.server.wm.WindowManagerService;
import com.huawei.android.audio.HwAudioServiceManager;
import com.huawei.android.fsm.HwFoldScreenManagerInternal;
import huawei.android.hwutil.HwFullScreenDisplay;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class ActivityRecord extends AbsActivityRecord implements AppWindowContainerListener {
    static final String ACTIVITY_ICON_SUFFIX = "_activity_icon_";
    private static final String ATTR_COMPONENTSPECIFIED = "component_specified";
    private static final String ATTR_ID = "id";
    private static final String ATTR_LAUNCHEDFROMPACKAGE = "launched_from_package";
    private static final String ATTR_LAUNCHEDFROMUID = "launched_from_uid";
    private static final String ATTR_RESOLVEDTYPE = "resolved_type";
    private static final String ATTR_USERID = "user_id";
    private static final boolean DEBUG_FULL = SystemProperties.getBoolean("ro.config.display_mode_full", true);
    private static final String LEGACY_RECENTS_PACKAGE_NAME = "com.android.systemui.recents";
    private static final String LEGACY_RECENTS_PACKAGE_NAME_LAUNCHER = "com.huawei.android.launcher.quickstep";
    private static final boolean SHOW_ACTIVITY_START_TIME = true;
    static final int STARTING_WINDOW_NOT_SHOWN = 0;
    static final int STARTING_WINDOW_REMOVED = 2;
    static final int STARTING_WINDOW_SHOWN = 1;
    private static final String TAG = "ActivityManager";
    private static final String TAG_CONFIGURATION = (ActivityManagerService.TAG + ActivityManagerDebugConfig.POSTFIX_CONFIGURATION);
    private static final String TAG_INTENT = "intent";
    private static final String TAG_PERSISTABLEBUNDLE = "persistable_bundle";
    private static final String TAG_SAVED_STATE = "ActivityManager";
    private static final String TAG_STATES = "ActivityManager";
    private static final String TAG_SWITCH = "ActivityManager";
    private static final String TAG_VISIBILITY = (ActivityManagerService.TAG + ActivityManagerDebugConfig.POSTFIX_VISIBILITY);
    private static final int UNIPERF_BOOST_OFF = 4;
    public static float mDeviceMaxRatio = -1.0f;
    ProcessRecord app;
    ApplicationInfo appInfo;
    AppTimeTracker appTimeTracker;
    final IApplicationToken.Stub appToken;
    CompatibilityInfo compat;
    private final boolean componentSpecified;
    int configChangeFlags;
    HashSet<ConnectionRecord> connections;
    long cpuTimeAtResume;
    private long createTime = System.currentTimeMillis();
    boolean deferRelaunchUntilPaused;
    boolean delayedResume;
    long displayStartTime;
    boolean finishing;
    boolean forceNewConfig;
    boolean frontOfTask;
    boolean frozenBeforeDestroy;
    boolean fullscreen;
    long fullyDrawnStartTime;
    boolean hasBeenLaunched;
    final boolean hasWallpaper;
    boolean haveState;
    Bundle icicle;
    private int icon;
    boolean idle;
    boolean immersive;
    private boolean inHistory;
    public final ActivityInfo info;
    final Intent intent;
    private boolean keysPaused;
    private int labelRes;
    long lastLaunchTime;
    long lastVisibleTime;
    int launchCount;
    boolean launchFailed;
    int launchMode;
    long launchTickTime;
    final String launchedFromPackage;
    final int launchedFromPid;
    final int launchedFromUid;
    int lockTaskLaunchMode;
    private int logo;
    boolean mClientVisibilityDeferred;
    private boolean mDeferHidingClient;
    private int[] mHorizontalSizeConfigurations;
    private boolean mIsFloating;
    private boolean mIsTransluent;
    private MergedConfiguration mLastReportedConfiguration;
    private int mLastReportedDisplayId;
    private boolean mLastReportedMultiWindowMode;
    private boolean mLastReportedPictureInPictureMode;
    boolean mLaunchTaskBehind;
    int mRotationAnimationHint = -1;
    private boolean mShowWhenLocked;
    boolean mSkipMultiWindowChanged = false;
    private int[] mSmallestSizeConfigurations;
    final ActivityStackSupervisor mStackSupervisor;
    int mStartingWindowState = 0;
    private ActivityStack.ActivityState mState;
    boolean mTaskOverlay = false;
    private final Rect mTmpBounds = new Rect();
    private final Configuration mTmpConfig = new Configuration();
    private boolean mTurnScreenOn;
    private int[] mVerticalSizeConfigurations;
    private IVRSystemServiceManager mVrMananger;
    AppWindowContainerController mWindowContainerController;
    public float maxAspectRatio = 0.0f;
    public float minAspectRatio = 0.0f;
    ArrayList<ReferrerIntent> newIntents;
    final boolean noDisplay;
    private CharSequence nonLocalizedLabel;
    boolean nowVisible;
    final String packageName;
    long pauseTime;
    ActivityOptions pendingOptions;
    HashSet<WeakReference<PendingIntentRecord>> pendingResults;
    boolean pendingVoiceInteractionStart;
    PersistableBundle persistentState;
    PictureInPictureParams pictureInPictureArgs = new PictureInPictureParams.Builder().build();
    boolean preserveWindowOnDeferredRelaunch;
    public final String processName;
    public final ComponentName realActivity;
    private int realTheme;
    final int requestCode;
    ComponentName requestedVrComponent;
    final String resolvedType;
    ActivityRecord resultTo;
    final String resultWho;
    ArrayList<ResultInfo> results;
    ActivityOptions returningOptions;
    final boolean rootVoiceInteraction;
    final ActivityManagerService service;
    final String shortComponentName;
    boolean sleeping;
    private long startTime;
    final boolean stateNotNeeded;
    boolean stopped;
    String stringName;
    boolean supportsEnterPipOnTaskSwitch;
    TaskRecord task;
    final String taskAffinity;
    ActivityManager.TaskDescription taskDescription;
    private int theme;
    UriPermissionOwner uriPermissions;
    final int userId;
    boolean visible;
    boolean visibleIgnoringKeyguard;
    IVoiceInteractionSession voiceSession;
    private int windowFlags;

    static class Token extends IApplicationToken.Stub {
        private final String name;
        private final WeakReference<ActivityRecord> weakActivity;

        Token(ActivityRecord activity, Intent intent) {
            this.weakActivity = new WeakReference<>(activity);
            this.name = intent.getComponent().flattenToShortString();
        }

        /* access modifiers changed from: private */
        public static ActivityRecord tokenToActivityRecordLocked(Token token) {
            if (token == null) {
                return null;
            }
            ActivityRecord r = (ActivityRecord) token.weakActivity.get();
            if (r == null || r.getStack() == null) {
                return null;
            }
            return r;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("Token{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(' ');
            sb.append(this.weakActivity.get());
            sb.append('}');
            return sb.toString();
        }

        public String getName() {
            return this.name;
        }
    }

    private static String startingWindowStateToString(int state) {
        switch (state) {
            case 0:
                return "STARTING_WINDOW_NOT_SHOWN";
            case 1:
                return "STARTING_WINDOW_SHOWN";
            case 2:
                return "STARTING_WINDOW_REMOVED";
            default:
                return "unknown state=" + state;
        }
    }

    /* access modifiers changed from: package-private */
    public void dump(PrintWriter pw, String prefix) {
        String str;
        long now = SystemClock.uptimeMillis();
        pw.print(prefix);
        pw.print("packageName=");
        pw.print(this.packageName);
        pw.print(" processName=");
        pw.println(this.processName);
        pw.print(prefix);
        pw.print("launchedFromUid=");
        pw.print(this.launchedFromUid);
        pw.print(" launchedFromPackage=");
        pw.print(this.launchedFromPackage);
        pw.print(" userId=");
        pw.println(this.userId);
        pw.print(prefix);
        pw.print("app=");
        pw.println(this.app);
        pw.print(prefix);
        pw.println(this.intent.toInsecureStringWithClip());
        pw.print(prefix);
        pw.print("frontOfTask=");
        pw.print(this.frontOfTask);
        pw.print(" task=");
        pw.println(this.task);
        pw.print(prefix);
        pw.print("taskAffinity=");
        pw.println(this.taskAffinity);
        pw.print(prefix);
        pw.print("realActivity=");
        pw.println(this.realActivity.flattenToShortString());
        if (this.appInfo != null) {
            pw.print(prefix);
            pw.print("baseDir=");
            pw.println(this.appInfo.sourceDir);
            if (!Objects.equals(this.appInfo.sourceDir, this.appInfo.publicSourceDir)) {
                pw.print(prefix);
                pw.print("resDir=");
                pw.println(this.appInfo.publicSourceDir);
            }
            pw.print(prefix);
            pw.print("dataDir=");
            pw.println(this.appInfo.dataDir);
            if (this.appInfo.splitSourceDirs != null) {
                pw.print(prefix);
                pw.print("splitDir=");
                pw.println(Arrays.toString(this.appInfo.splitSourceDirs));
            }
            if (HwFoldScreenState.isFoldScreenDevice()) {
                boolean canChangeMinAspect = this.appInfo.canChangeAspectRatio("minAspectRatio");
                if (canChangeMinAspect) {
                    pw.print(prefix);
                    pw.print("canChangeMinAspect=");
                    pw.println(canChangeMinAspect);
                }
                if (this.minAspectRatio != 0.0f) {
                    pw.print(prefix);
                    pw.print("minAspectRatio=");
                    pw.println(this.minAspectRatio);
                }
            }
        }
        pw.print(prefix);
        pw.print("stateNotNeeded=");
        pw.print(this.stateNotNeeded);
        pw.print(" componentSpecified=");
        pw.print(this.componentSpecified);
        pw.print(" mActivityType=");
        pw.println(WindowConfiguration.activityTypeToString(getActivityType()));
        if (this.rootVoiceInteraction) {
            pw.print(prefix);
            pw.print("rootVoiceInteraction=");
            pw.println(this.rootVoiceInteraction);
        }
        pw.print(prefix);
        pw.print("compat=");
        pw.print(this.compat);
        pw.print(" labelRes=0x");
        pw.print(Integer.toHexString(this.labelRes));
        pw.print(" icon=0x");
        pw.print(Integer.toHexString(this.icon));
        pw.print(" theme=0x");
        pw.println(Integer.toHexString(this.theme));
        pw.println(prefix + "mLastReportedConfigurations:");
        this.mLastReportedConfiguration.dump(pw, prefix + " ");
        pw.print(prefix);
        pw.print("CurrentConfiguration=");
        pw.println(getConfiguration());
        if (!getOverrideConfiguration().equals(Configuration.EMPTY)) {
            pw.println(prefix + "OverrideConfiguration=" + getOverrideConfiguration());
        }
        if (!matchParentBounds()) {
            pw.println(prefix + "bounds=" + getBounds());
        }
        if (!(this.resultTo == null && this.resultWho == null)) {
            pw.print(prefix);
            pw.print("resultTo=");
            pw.print(this.resultTo);
            pw.print(" resultWho=");
            pw.print(this.resultWho);
            pw.print(" resultCode=");
            pw.println(this.requestCode);
        }
        if (!(this.taskDescription == null || (this.taskDescription.getIconFilename() == null && this.taskDescription.getLabel() == null && this.taskDescription.getPrimaryColor() == 0))) {
            pw.print(prefix);
            pw.print("taskDescription:");
            pw.print(" label=\"");
            pw.print(this.taskDescription.getLabel());
            pw.print("\"");
            pw.print(" icon=");
            if (this.taskDescription.getInMemoryIcon() != null) {
                str = this.taskDescription.getInMemoryIcon().getByteCount() + " bytes";
            } else {
                str = "null";
            }
            pw.print(str);
            pw.print(" iconResource=");
            pw.print(this.taskDescription.getIconResource());
            pw.print(" iconFilename=");
            pw.print(this.taskDescription.getIconFilename());
            pw.print(" primaryColor=");
            pw.println(Integer.toHexString(this.taskDescription.getPrimaryColor()));
            pw.print(prefix + " backgroundColor=");
            pw.println(Integer.toHexString(this.taskDescription.getBackgroundColor()));
            pw.print(prefix + " statusBarColor=");
            pw.println(Integer.toHexString(this.taskDescription.getStatusBarColor()));
            pw.print(prefix + " navigationBarColor=");
            pw.println(Integer.toHexString(this.taskDescription.getNavigationBarColor()));
        }
        if (this.results != null) {
            pw.print(prefix);
            pw.print("results=");
            pw.println(this.results);
        }
        if (this.pendingResults != null && this.pendingResults.size() > 0) {
            pw.print(prefix);
            pw.println("Pending Results:");
            Iterator<WeakReference<PendingIntentRecord>> it = this.pendingResults.iterator();
            while (it.hasNext()) {
                WeakReference<PendingIntentRecord> wpir = it.next();
                PendingIntentRecord pir = wpir != null ? (PendingIntentRecord) wpir.get() : null;
                pw.print(prefix);
                pw.print("  - ");
                if (pir == null) {
                    pw.println("null");
                } else {
                    pw.println(pir);
                    pir.dump(pw, prefix + "    ");
                }
            }
        }
        if (this.newIntents != null && this.newIntents.size() > 0) {
            pw.print(prefix);
            pw.println("Pending New Intents:");
            for (int i = 0; i < this.newIntents.size(); i++) {
                Intent intent2 = this.newIntents.get(i);
                pw.print(prefix);
                pw.print("  - ");
                if (intent2 == null) {
                    pw.println("null");
                } else {
                    pw.println(intent2.toShortString(true, true, false, true));
                }
            }
        }
        if (this.pendingOptions != null) {
            pw.print(prefix);
            pw.print("pendingOptions=");
            pw.println(this.pendingOptions);
        }
        if (this.appTimeTracker != null) {
            this.appTimeTracker.dumpWithHeader(pw, prefix, false);
        }
        if (this.uriPermissions != null) {
            this.uriPermissions.dump(pw, prefix);
        }
        pw.print(prefix);
        pw.print("launchFailed=");
        pw.print(this.launchFailed);
        pw.print(" launchCount=");
        pw.print(this.launchCount);
        pw.print(" lastLaunchTime=");
        if (this.lastLaunchTime == 0) {
            pw.print("0");
        } else {
            TimeUtils.formatDuration(this.lastLaunchTime, now, pw);
        }
        pw.println();
        pw.print(prefix);
        pw.print("haveState=");
        pw.print(this.haveState);
        pw.print(" icicle=");
        pw.println(this.icicle);
        pw.print(prefix);
        pw.print("state=");
        pw.print(this.mState);
        pw.print(" stopped=");
        pw.print(this.stopped);
        pw.print(" delayedResume=");
        pw.print(this.delayedResume);
        pw.print(" finishing=");
        pw.println(this.finishing);
        pw.print(prefix);
        pw.print("keysPaused=");
        pw.print(this.keysPaused);
        pw.print(" inHistory=");
        pw.print(this.inHistory);
        pw.print(" visible=");
        pw.print(this.visible);
        pw.print(" sleeping=");
        pw.print(this.sleeping);
        pw.print(" idle=");
        pw.print(this.idle);
        pw.print(" mStartingWindowState=");
        pw.println(startingWindowStateToString(this.mStartingWindowState));
        pw.print(prefix);
        pw.print("fullscreen=");
        pw.print(this.fullscreen);
        pw.print(" noDisplay=");
        pw.print(this.noDisplay);
        pw.print(" immersive=");
        pw.print(this.immersive);
        pw.print(" launchMode=");
        pw.println(this.launchMode);
        pw.print(prefix);
        pw.print("frozenBeforeDestroy=");
        pw.print(this.frozenBeforeDestroy);
        pw.print(" forceNewConfig=");
        pw.println(this.forceNewConfig);
        pw.print(prefix);
        pw.print("mActivityType=");
        pw.println(WindowConfiguration.activityTypeToString(getActivityType()));
        if (this.requestedVrComponent != null) {
            pw.print(prefix);
            pw.print("requestedVrComponent=");
            pw.println(this.requestedVrComponent);
        }
        if (!(this.displayStartTime == 0 && this.startTime == 0)) {
            pw.print(prefix);
            pw.print("displayStartTime=");
            if (this.displayStartTime == 0) {
                pw.print("0");
            } else {
                TimeUtils.formatDuration(this.displayStartTime, now, pw);
            }
            pw.print(" startTime=");
            if (this.startTime == 0) {
                pw.print("0");
            } else {
                TimeUtils.formatDuration(this.startTime, now, pw);
            }
            pw.println();
        }
        boolean waitingVisible = this.mStackSupervisor.mActivitiesWaitingForVisibleActivity.contains(this);
        if (this.lastVisibleTime != 0 || waitingVisible || this.nowVisible) {
            pw.print(prefix);
            pw.print("waitingVisible=");
            pw.print(waitingVisible);
            pw.print(" nowVisible=");
            pw.print(this.nowVisible);
            pw.print(" lastVisibleTime=");
            if (this.lastVisibleTime == 0) {
                pw.print("0");
            } else {
                TimeUtils.formatDuration(this.lastVisibleTime, now, pw);
            }
            pw.println();
        }
        if (this.mDeferHidingClient) {
            pw.println(prefix + "mDeferHidingClient=" + this.mDeferHidingClient);
        }
        if (this.deferRelaunchUntilPaused || this.configChangeFlags != 0) {
            pw.print(prefix);
            pw.print("deferRelaunchUntilPaused=");
            pw.print(this.deferRelaunchUntilPaused);
            pw.print(" configChangeFlags=");
            pw.println(Integer.toHexString(this.configChangeFlags));
        }
        if (this.connections != null) {
            pw.print(prefix);
            pw.print("connections=");
            pw.println(this.connections);
        }
        if (this.info != null) {
            pw.println(prefix + "resizeMode=" + ActivityInfo.resizeModeToString(this.info.resizeMode));
            pw.println(prefix + "mLastReportedMultiWindowMode=" + this.mLastReportedMultiWindowMode + " mLastReportedPictureInPictureMode=" + this.mLastReportedPictureInPictureMode);
            if (this.info.supportsPictureInPicture()) {
                pw.println(prefix + "supportsPictureInPicture=" + this.info.supportsPictureInPicture());
                pw.println(prefix + "supportsEnterPipOnTaskSwitch: " + this.supportsEnterPipOnTaskSwitch);
            }
            if (this.info.maxAspectRatio != 0.0f) {
                pw.println(prefix + "maxAspectRatio=" + this.info.maxAspectRatio);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void updateApplicationInfo(ApplicationInfo aInfo) {
        this.appInfo = aInfo;
        this.info.applicationInfo = aInfo;
    }

    private boolean crossesHorizontalSizeThreshold(int firstDp, int secondDp) {
        return crossesSizeThreshold(this.mHorizontalSizeConfigurations, firstDp, secondDp);
    }

    private boolean crossesVerticalSizeThreshold(int firstDp, int secondDp) {
        return crossesSizeThreshold(this.mVerticalSizeConfigurations, firstDp, secondDp);
    }

    private boolean crossesSmallestSizeThreshold(int firstDp, int secondDp) {
        return crossesSizeThreshold(this.mSmallestSizeConfigurations, firstDp, secondDp);
    }

    private static boolean crossesSizeThreshold(int[] thresholds, int firstDp, int secondDp) {
        if (thresholds == null) {
            return false;
        }
        for (int i = thresholds.length - 1; i >= 0; i--) {
            int threshold = thresholds[i];
            if ((firstDp < threshold && secondDp >= threshold) || (firstDp >= threshold && secondDp < threshold)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public void setSizeConfigurations(int[] horizontalSizeConfiguration, int[] verticalSizeConfigurations, int[] smallestSizeConfigurations) {
        this.mHorizontalSizeConfigurations = horizontalSizeConfiguration;
        this.mVerticalSizeConfigurations = verticalSizeConfigurations;
        this.mSmallestSizeConfigurations = smallestSizeConfigurations;
    }

    private void scheduleActivityMovedToDisplay(int displayId, Configuration config) {
        if (this.app == null || this.app.thread == null) {
            if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                Slog.w(ActivityManagerService.TAG, "Can't report activity moved to display - client not running, activityRecord=" + this + ", displayId=" + displayId);
            }
            return;
        }
        try {
            if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                Slog.v(ActivityManagerService.TAG, "Reporting activity moved to display, activityRecord=" + this + ", displayId=" + displayId + ", config=" + config);
            }
            this.service.getLifecycleManager().scheduleTransaction(this.app.thread, (IBinder) this.appToken, (ClientTransactionItem) MoveToDisplayItem.obtain(displayId, config));
        } catch (RemoteException e) {
        }
    }

    private void scheduleConfigurationChanged(Configuration config) {
        if (this.app == null || this.app.thread == null) {
            if (ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                Slog.w(ActivityManagerService.TAG, "Can't report activity configuration update - client not running, activityRecord=" + this);
            }
            return;
        }
        try {
            if (ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                Slog.v(ActivityManagerService.TAG, "Sending new config to " + this + ", config: " + config);
            }
            this.service.getLifecycleManager().scheduleTransaction(this.app.thread, (IBinder) this.appToken, (ClientTransactionItem) ActivityConfigurationChangeItem.obtain(config));
        } catch (RemoteException e) {
        }
    }

    /* access modifiers changed from: package-private */
    public void updateMultiWindowMode() {
        if (this.task != null && this.task.getStack() != null && this.app != null && this.app.thread != null) {
            if (this.mSkipMultiWindowChanged && this.task.getStack().inFreeformWindowingMode()) {
                this.mSkipMultiWindowChanged = false;
            } else if (!this.task.getStack().deferScheduleMultiWindowModeChanged()) {
                boolean inMultiWindowMode = inMultiWindowMode();
                if (inMultiWindowMode != this.mLastReportedMultiWindowMode) {
                    this.mLastReportedMultiWindowMode = inMultiWindowMode;
                    scheduleMultiWindowModeChanged(getConfiguration());
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void scheduleMultiWindowModeChanged(Configuration overrideConfig) {
        try {
            this.service.getLifecycleManager().scheduleTransaction(this.app.thread, (IBinder) this.appToken, (ClientTransactionItem) MultiWindowModeChangeItem.obtain(this.mLastReportedMultiWindowMode, overrideConfig));
        } catch (Exception e) {
        }
    }

    /* access modifiers changed from: package-private */
    public void updatePictureInPictureMode(Rect targetStackBounds, boolean forceUpdate) {
        if (this.task != null && this.task.getStack() != null && this.app != null && this.app.thread != null) {
            boolean inPictureInPictureMode = inPinnedWindowingMode() && targetStackBounds != null;
            if (inPictureInPictureMode != this.mLastReportedPictureInPictureMode || forceUpdate) {
                this.mLastReportedPictureInPictureMode = inPictureInPictureMode;
                this.mLastReportedMultiWindowMode = inPictureInPictureMode;
                Configuration newConfig = this.task.computeNewOverrideConfigurationForBounds(targetStackBounds, null);
                schedulePictureInPictureModeChanged(newConfig);
                scheduleMultiWindowModeChanged(newConfig);
            }
        }
    }

    private void schedulePictureInPictureModeChanged(Configuration overrideConfig) {
        try {
            this.service.getLifecycleManager().scheduleTransaction(this.app.thread, (IBinder) this.appToken, (ClientTransactionItem) PipModeChangeItem.obtain(this.mLastReportedPictureInPictureMode, overrideConfig));
        } catch (Exception e) {
        }
    }

    /* access modifiers changed from: protected */
    public int getChildCount() {
        return 0;
    }

    /* access modifiers changed from: protected */
    public ConfigurationContainer getChildAt(int index) {
        return null;
    }

    /* access modifiers changed from: protected */
    public ConfigurationContainer getParent() {
        return getTask();
    }

    /* access modifiers changed from: package-private */
    public TaskRecord getTask() {
        return this.task;
    }

    /* access modifiers changed from: package-private */
    public void setTask(TaskRecord task2) {
        setTask(task2, false);
    }

    /* access modifiers changed from: package-private */
    public void setTask(TaskRecord task2, boolean reparenting) {
        if (task2 == null || task2 != getTask()) {
            ActivityStack oldStack = getStack();
            ActivityStack newStack = task2 != null ? task2.getStack() : null;
            if (oldStack != newStack) {
                if (!reparenting && oldStack != null) {
                    oldStack.onActivityRemovedFromStack(this);
                }
                if (newStack != null) {
                    newStack.onActivityAddedToStack(this);
                }
            }
            this.task = task2;
            if (!reparenting) {
                onParentChanged();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setWillCloseOrEnterPip(boolean willCloseOrEnterPip) {
        getWindowContainerController().setWillCloseOrEnterPip(willCloseOrEnterPip);
    }

    public static ActivityRecord forToken(IBinder token) {
        return forTokenLocked(token);
    }

    static ActivityRecord forTokenLocked(IBinder token) {
        try {
            return Token.tokenToActivityRecordLocked((Token) token);
        } catch (ClassCastException e) {
            Slog.w(ActivityManagerService.TAG, "Bad activity token: " + token, e);
            return null;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isResolverActivity() {
        return ResolverActivity.class.getName().equals(this.realActivity.getClassName());
    }

    /* access modifiers changed from: package-private */
    public boolean isResolverOrChildActivity() {
        if ("com.huawei.android.internal.app".equals(this.packageName)) {
            return true;
        }
        if (!PackageManagerService.PLATFORM_PACKAGE_NAME.equals(this.packageName)) {
            return false;
        }
        try {
            return ResolverActivity.class.isAssignableFrom(Object.class.getClassLoader().loadClass(this.realActivity.getClassName()));
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public ActivityRecord(ActivityManagerService _service, ProcessRecord _caller, int _launchedFromPid, int _launchedFromUid, String _launchedFromPackage, Intent _intent, String _resolvedType, ActivityInfo aInfo, Configuration _configuration, ActivityRecord _resultTo, String _resultWho, int _reqCode, boolean _componentSpecified, boolean _rootVoiceInteraction, ActivityStackSupervisor supervisor, ActivityOptions options, ActivityRecord sourceRecord) {
        ProcessRecord processRecord = _caller;
        Intent intent2 = _intent;
        ActivityInfo activityInfo = aInfo;
        ActivityOptions activityOptions = options;
        this.service = _service;
        this.appToken = new Token(this, intent2);
        this.info = activityInfo;
        this.launchedFromPid = _launchedFromPid;
        int i = _launchedFromUid;
        this.launchedFromUid = i;
        this.launchedFromPackage = _launchedFromPackage;
        this.userId = UserHandle.getUserId(activityInfo.applicationInfo.uid);
        this.intent = intent2;
        this.shortComponentName = _intent.getComponent().flattenToShortString();
        this.resolvedType = _resolvedType;
        boolean z = _componentSpecified;
        this.componentSpecified = z;
        this.rootVoiceInteraction = _rootVoiceInteraction;
        this.mLastReportedConfiguration = new MergedConfiguration(_configuration);
        this.resultTo = _resultTo;
        this.resultWho = _resultWho;
        this.requestCode = _reqCode;
        setState(ActivityStack.ActivityState.INITIALIZING, "ActivityRecord ctor");
        this.frontOfTask = false;
        this.launchFailed = false;
        this.stopped = false;
        this.delayedResume = false;
        this.finishing = false;
        this.deferRelaunchUntilPaused = false;
        this.keysPaused = false;
        this.inHistory = false;
        this.visible = false;
        this.nowVisible = false;
        this.idle = false;
        this.hasBeenLaunched = false;
        this.mStackSupervisor = supervisor;
        this.haveState = true;
        initSplitMode(intent2);
        this.mVrMananger = HwFrameworkFactory.getVRSystemServiceManager();
        if (activityInfo.targetActivity == null || (activityInfo.targetActivity.equals(_intent.getComponent().getClassName()) && (activityInfo.launchMode == 0 || activityInfo.launchMode == 1))) {
            this.realActivity = _intent.getComponent();
        } else {
            this.realActivity = new ComponentName(activityInfo.packageName, activityInfo.targetActivity);
        }
        if (isSplitMode()) {
            if (activityInfo.taskAffinity == null || activityInfo.taskAffinity.equals(activityInfo.processName)) {
                this.taskAffinity = processRecord.processName;
            } else {
                this.taskAffinity = activityInfo.taskAffinity;
            }
            if (activityInfo.launchMode == 2) {
                activityInfo.launchMode = 0;
            }
        } else {
            this.taskAffinity = activityInfo.taskAffinity;
        }
        this.stateNotNeeded = (activityInfo.flags & 16) != 0;
        this.appInfo = activityInfo.applicationInfo;
        this.nonLocalizedLabel = activityInfo.nonLocalizedLabel;
        this.labelRes = activityInfo.labelRes;
        if (this.nonLocalizedLabel == null && this.labelRes == 0) {
            ApplicationInfo app2 = activityInfo.applicationInfo;
            this.nonLocalizedLabel = app2.nonLocalizedLabel;
            this.labelRes = app2.labelRes;
        }
        this.icon = aInfo.getIconResource();
        this.logo = aInfo.getLogoResource();
        this.theme = aInfo.getThemeResource();
        this.realTheme = this.theme;
        if (this.realTheme == 0) {
            this.realTheme = activityInfo.applicationInfo.targetSdkVersion < 11 ? 16973829 : 16973931;
        }
        if ((activityInfo.flags & 512) != 0) {
            this.windowFlags |= DumpState.DUMP_SERVICE_PERMISSIONS;
        }
        if ((activityInfo.flags & 1) == 0 || processRecord == null || !(activityInfo.applicationInfo.uid == 1000 || activityInfo.applicationInfo.uid == processRecord.info.uid)) {
            this.processName = activityInfo.processName;
        } else {
            this.processName = processRecord.processName;
        }
        if ((activityInfo.flags & 32) != 0) {
            this.intent.addFlags(DumpState.DUMP_VOLUMES);
        }
        this.packageName = activityInfo.applicationInfo.packageName;
        this.launchMode = activityInfo.launchMode;
        AttributeCache.Entry ent = AttributeCache.instance().get(this.packageName, this.realTheme, R.styleable.Window, this.userId);
        if (ent != null) {
            this.fullscreen = !ActivityInfo.isTranslucentOrFloating(ent.array) || isForceRotationMode(this.packageName, intent2);
            this.hasWallpaper = ent.array.getBoolean(14, false);
            this.noDisplay = ent.array.getBoolean(10, false);
            this.mIsFloating = ent.array.getBoolean(4, false);
            this.mIsTransluent = ent.array.getBoolean(5, false);
        } else {
            this.hasWallpaper = false;
            this.noDisplay = false;
        }
        if (isSplitMode()) {
            if (!this.fullscreen) {
                intent2.addHwFlags(8);
            } else {
                this.fullscreen = false;
            }
        }
        AttributeCache.Entry entry = ent;
        setActivityType(z, i, intent2, activityOptions, sourceRecord);
        this.immersive = (activityInfo.flags & 2048) != 0;
        this.requestedVrComponent = activityInfo.requestedVrComponent == null ? null : ComponentName.unflattenFromString(activityInfo.requestedVrComponent);
        this.mShowWhenLocked = (activityInfo.flags & DumpState.DUMP_VOLUMES) != 0;
        this.mTurnScreenOn = (activityInfo.flags & DumpState.DUMP_SERVICE_PERMISSIONS) != 0;
        this.mRotationAnimationHint = activityInfo.rotationAnimation;
        this.lockTaskLaunchMode = activityInfo.lockTaskLaunchMode;
        if (this.appInfo.isPrivilegedApp() && (this.lockTaskLaunchMode == 2 || this.lockTaskLaunchMode == 1)) {
            this.lockTaskLaunchMode = 0;
        }
        if (activityOptions != null) {
            this.pendingOptions = activityOptions;
            this.mLaunchTaskBehind = options.getLaunchTaskBehind();
            int rotationAnimation = this.pendingOptions.getRotationAnimationHint();
            if (rotationAnimation >= 0) {
                this.mRotationAnimationHint = rotationAnimation;
            }
            PendingIntent usageReport = this.pendingOptions.getUsageTimeReport();
            if (usageReport != null) {
                this.appTimeTracker = new AppTimeTracker(usageReport);
            }
            if (this.pendingOptions.getLockTaskMode() && this.lockTaskLaunchMode == 0) {
                this.lockTaskLaunchMode = 3;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setProcess(ProcessRecord proc) {
        this.app = proc;
        if ((this.task != null ? this.task.getRootActivity() : null) == this) {
            this.task.setRootProcess(proc);
        }
    }

    /* access modifiers changed from: package-private */
    public AppWindowContainerController getWindowContainerController() {
        return this.mWindowContainerController;
    }

    /* access modifiers changed from: package-private */
    public void createWindowContainer(boolean naviBarHide) {
        if (this.mWindowContainerController == null) {
            this.inHistory = true;
            TaskWindowContainerController taskController = this.task.getWindowContainerController();
            this.task.updateOverrideConfigurationFromLaunchBounds();
            updateOverrideConfiguration();
            AppWindowContainerController appWindowContainerController = r0;
            AppWindowContainerController appWindowContainerController2 = new AppWindowContainerController(taskController, this.appToken, this, HwBootFail.STAGE_BOOT_SUCCESS, this.info.screenOrientation, this.fullscreen, (this.info.flags & 1024) != 0, this.info.configChanges, this.task.voiceSession != null, this.mLaunchTaskBehind, isAlwaysFocusable(), this.appInfo.targetSdkVersion, this.mRotationAnimationHint, 1000000 * ActivityManagerService.getInputDispatchingTimeoutLocked(this), naviBarHide, this.info);
            this.mWindowContainerController = appWindowContainerController;
            this.task.addActivityToTop(this);
            this.mLastReportedMultiWindowMode = inMultiWindowMode();
            this.mLastReportedPictureInPictureMode = inPinnedWindowingMode();
            return;
        }
        throw new IllegalArgumentException("Window container=" + this.mWindowContainerController + " already created for r=" + this);
    }

    /* access modifiers changed from: package-private */
    public void removeWindowContainer() {
        if (this.mWindowContainerController != null) {
            resumeKeyDispatchingLocked();
            this.mWindowContainerController.removeContainer(getDisplayId());
            this.mWindowContainerController = null;
        }
    }

    /* access modifiers changed from: package-private */
    public void reparent(TaskRecord newTask, int position, String reason) {
        TaskRecord prevTask = this.task;
        if (prevTask == newTask) {
            throw new IllegalArgumentException(reason + ": task=" + newTask + " is already the parent of r=" + this);
        } else if (prevTask == null || newTask == null || prevTask.getStack() == newTask.getStack()) {
            if (this.mWindowContainerController != null) {
                this.mWindowContainerController.reparent(newTask.getWindowContainerController(), position);
            }
            ActivityStack prevStack = prevTask.getStack();
            if (prevStack != newTask.getStack()) {
                prevStack.onActivityRemovedFromStack(this);
            }
            prevTask.removeActivity(this, true);
            newTask.addActivityAtIndex(position, this);
        } else {
            throw new IllegalArgumentException(reason + ": task=" + newTask + " is in a different stack (" + newTask.getStackId() + ") than the parent of r=" + this + " (" + prevTask.getStackId() + ")");
        }
    }

    private boolean isHomeIntent(Intent intent2) {
        if ("android.intent.action.MAIN".equals(intent2.getAction()) && intent2.hasCategory("android.intent.category.HOME") && intent2.getCategories().size() == 1 && intent2.getData() == null && intent2.getType() == null) {
            return true;
        }
        return false;
    }

    static boolean isMainIntent(Intent intent2) {
        if ("android.intent.action.MAIN".equals(intent2.getAction()) && intent2.hasCategory("android.intent.category.LAUNCHER") && intent2.getCategories().size() == 1 && intent2.getData() == null && intent2.getType() == null) {
            return true;
        }
        return false;
    }

    private boolean canLaunchHomeActivity(int uid, ActivityRecord sourceRecord) {
        boolean z = true;
        if (uid == Process.myUid() || uid == 0) {
            return true;
        }
        RecentTasks recentTasks = this.mStackSupervisor.mService.getRecentTasks();
        if (recentTasks != null && recentTasks.isCallerRecents(uid)) {
            return true;
        }
        if (sourceRecord == null || !sourceRecord.isResolverActivity()) {
            z = false;
        }
        return z;
    }

    private boolean canLaunchAssistActivity(String packageName2) {
        ComponentName assistComponent = this.service.mActiveVoiceInteractionServiceComponent;
        if (assistComponent != null) {
            return assistComponent.getPackageName().equals(packageName2);
        }
        return false;
    }

    private void setActivityType(boolean componentSpecified2, int launchedFromUid2, Intent intent2, ActivityOptions options, ActivityRecord sourceRecord) {
        int activityType = 0;
        if ((!componentSpecified2 || canLaunchHomeActivity(launchedFromUid2, sourceRecord)) && isHomeIntent(intent2) && !isResolverActivity()) {
            activityType = 2;
            if (this.info.resizeMode == 4 || this.info.resizeMode == 1) {
                this.info.resizeMode = 0;
            }
        } else if (this.realActivity.getClassName().contains(LEGACY_RECENTS_PACKAGE_NAME) || this.service.getRecentTasks().isRecentsComponent(this.realActivity, this.appInfo.uid) || this.realActivity.getClassName().contains(LEGACY_RECENTS_PACKAGE_NAME_LAUNCHER)) {
            activityType = 3;
        } else if (options != null && options.getLaunchActivityType() == 4 && canLaunchAssistActivity(this.launchedFromPackage)) {
            activityType = 4;
        }
        setActivityType(activityType);
    }

    /* access modifiers changed from: package-private */
    public void setTaskToAffiliateWith(TaskRecord taskToAffiliateWith) {
        if (this.launchMode != 3 && this.launchMode != 2) {
            this.task.setTaskToAffiliateWith(taskToAffiliateWith);
        }
    }

    /* access modifiers changed from: package-private */
    public <T extends ActivityStack> T getStack() {
        if (this.task != null) {
            return this.task.getStack();
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public int getStackId() {
        if (getStack() != null) {
            return getStack().mStackId;
        }
        return -1;
    }

    /* access modifiers changed from: package-private */
    public ActivityDisplay getDisplay() {
        ActivityStack stack = getStack();
        if (stack != null) {
            return stack.getDisplay();
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public boolean changeWindowTranslucency(boolean toOpaque) {
        if (this.fullscreen == toOpaque) {
            return false;
        }
        this.task.numFullscreen += toOpaque ? 1 : -1;
        this.fullscreen = toOpaque;
        return true;
    }

    /* access modifiers changed from: package-private */
    public void takeFromHistory() {
        if (this.inHistory) {
            this.inHistory = false;
            if (this.task != null && !this.finishing) {
                this.task = null;
            }
            clearOptionsLocked();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isInHistory() {
        return this.inHistory;
    }

    /* access modifiers changed from: package-private */
    public boolean isInStackLocked() {
        ActivityStack stack = getStack();
        return (stack == null || stack.isInStackLocked(this) == null) ? false : true;
    }

    /* access modifiers changed from: package-private */
    public boolean isPersistable() {
        return (this.info.persistableMode == 0 || this.info.persistableMode == 2) && (this.intent == null || (this.intent.getFlags() & DumpState.DUMP_VOLUMES) == 0);
    }

    /* access modifiers changed from: package-private */
    public boolean isFocusable() {
        return this.mStackSupervisor.isFocusable(this, isAlwaysFocusable());
    }

    /* access modifiers changed from: package-private */
    public boolean isResizeable() {
        return ActivityInfo.isResizeableMode(this.info.resizeMode) || this.info.supportsPictureInPicture();
    }

    /* access modifiers changed from: package-private */
    public boolean isNonResizableOrForcedResizable() {
        return (this.info.resizeMode == 2 || this.info.resizeMode == 1) ? false : true;
    }

    /* access modifiers changed from: package-private */
    public boolean supportsPictureInPicture() {
        return this.service.mSupportsPictureInPicture && isActivityTypeStandardOrUndefined() && this.info.supportsPictureInPicture();
    }

    public boolean supportsSplitScreenWindowingMode() {
        return super.supportsSplitScreenWindowingMode() && this.service.mSupportsSplitScreenMultiWindow && supportsResizeableMultiWindow();
    }

    /* access modifiers changed from: package-private */
    public boolean supportsFreeform() {
        return this.service.mSupportsFreeformWindowManagement && supportsResizeableMultiWindow();
    }

    private boolean supportsResizeableMultiWindow() {
        return this.service.mSupportsMultiWindow && !isActivityTypeHome() && (ActivityInfo.isResizeableMode(this.info.resizeMode) || this.service.mForceResizableActivities);
    }

    /* access modifiers changed from: package-private */
    public boolean canBeLaunchedOnDisplay(int displayId) {
        TaskRecord task2 = getTask();
        boolean resizeable = task2 != null ? task2.isResizeable() : supportsResizeableMultiWindow();
        if (!resizeable) {
            resizeable = (HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(displayId)) || (this.mVrMananger.isVRDeviceConnected() && this.mVrMananger.isValidVRDisplayId(displayId));
        }
        return this.service.mStackSupervisor.canPlaceEntityOnDisplay(displayId, resizeable, this.launchedFromPid, this.launchedFromUid, this.info);
    }

    /* access modifiers changed from: package-private */
    public boolean checkEnterPictureInPictureState(String caller, boolean beforeStopping) {
        boolean z = false;
        if (!supportsPictureInPicture() || !checkEnterPictureInPictureAppOpsState() || this.service.shouldDisableNonVrUiLocked()) {
            return false;
        }
        boolean isKeyguardLocked = this.service.isKeyguardLocked();
        boolean isCurrentAppLocked = this.service.getLockTaskModeState() != 0;
        ActivityDisplay display = getDisplay();
        boolean hasPinnedStack = display != null && display.hasPinnedStack();
        boolean isNotLockedOrOnKeyguard = !isKeyguardLocked && !isCurrentAppLocked;
        if (beforeStopping && hasPinnedStack) {
            return false;
        }
        switch (this.mState) {
            case RESUMED:
                if (!isCurrentAppLocked && (this.supportsEnterPipOnTaskSwitch || !beforeStopping)) {
                    z = true;
                }
                return z;
            case PAUSING:
            case PAUSED:
                if (isNotLockedOrOnKeyguard && !hasPinnedStack && this.supportsEnterPipOnTaskSwitch) {
                    z = true;
                }
                return z;
            case STOPPING:
                if (this.supportsEnterPipOnTaskSwitch) {
                    if (isNotLockedOrOnKeyguard && !hasPinnedStack) {
                        z = true;
                    }
                    return z;
                }
                break;
        }
        return false;
    }

    private boolean checkEnterPictureInPictureAppOpsState() {
        boolean z = false;
        try {
            if (this.service.getAppOpsService().checkOperation(67, this.appInfo.uid, this.packageName) == 0) {
                z = true;
            }
            return z;
        } catch (RemoteException e) {
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isAlwaysFocusable() {
        return (this.info.flags & 262144) != 0;
    }

    /* access modifiers changed from: package-private */
    public boolean hasDismissKeyguardWindows() {
        return this.service.mWindowManager.containsDismissKeyguardWindow(this.appToken);
    }

    /* access modifiers changed from: package-private */
    public void makeFinishingLocked() {
        if (!this.finishing) {
            this.finishing = true;
            if (this.stopped) {
                clearOptionsLocked();
            }
            if (this.service != null) {
                this.service.mTaskChangeNotificationController.notifyTaskStackChanged();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public UriPermissionOwner getUriPermissionsLocked() {
        if (this.uriPermissions == null) {
            this.uriPermissions = new UriPermissionOwner(this.service, this);
        }
        return this.uriPermissions;
    }

    /* access modifiers changed from: package-private */
    public void addResultLocked(ActivityRecord from, String resultWho2, int requestCode2, int resultCode, Intent resultData) {
        ActivityResult r = new ActivityResult(from, resultWho2, requestCode2, resultCode, resultData);
        if (this.results == null) {
            this.results = new ArrayList<>();
        }
        this.results.add(r);
    }

    /* access modifiers changed from: package-private */
    public void removeResultsLocked(ActivityRecord from, String resultWho2, int requestCode2) {
        if (this.results != null) {
            for (int i = this.results.size() - 1; i >= 0; i--) {
                ActivityResult r = this.results.get(i);
                if (r.mFrom == from) {
                    if (r.mResultWho == null) {
                        if (resultWho2 != null) {
                        }
                    } else if (!r.mResultWho.equals(resultWho2)) {
                    }
                    if (r.mRequestCode == requestCode2) {
                        this.results.remove(i);
                    }
                }
            }
        }
    }

    private void addNewIntentLocked(ReferrerIntent intent2) {
        if (this.newIntents == null) {
            this.newIntents = new ArrayList<>();
        }
        this.newIntents.add(intent2);
    }

    /* access modifiers changed from: package-private */
    public final boolean isSleeping() {
        ActivityStack stack = getStack();
        return stack != null ? stack.shouldSleepActivities() : this.service.isSleepingLocked();
    }

    /* access modifiers changed from: package-private */
    public final void deliverNewIntentLocked(int callingUid, Intent intent2, String referrer) {
        this.service.grantUriPermissionFromIntentLocked(callingUid, this.packageName, intent2, getUriPermissionsLocked(), this.userId);
        ReferrerIntent rintent = new ReferrerIntent(intent2, referrer);
        boolean unsent = true;
        boolean z = false;
        boolean isTopActivityWhileSleeping = isTopRunningActivity() && isSleeping();
        if (!((this.mState != ActivityStack.ActivityState.RESUMED && this.mState != ActivityStack.ActivityState.PAUSED && !isTopActivityWhileSleeping) || this.app == null || this.app.thread == null)) {
            try {
                ArrayList<ReferrerIntent> ar = new ArrayList<>(1);
                ar.add(rintent);
                ClientLifecycleManager lifecycleManager = this.service.getLifecycleManager();
                IApplicationThread iApplicationThread = this.app.thread;
                IApplicationToken.Stub stub = this.appToken;
                if (this.mState == ActivityStack.ActivityState.PAUSED) {
                    z = true;
                }
                lifecycleManager.scheduleTransaction(iApplicationThread, (IBinder) stub, (ClientTransactionItem) NewIntentItem.obtain(ar, z));
                unsent = false;
            } catch (RemoteException e) {
                Slog.w(ActivityManagerService.TAG, "Exception thrown sending new intent to " + this, e);
            } catch (NullPointerException e2) {
                Slog.w(ActivityManagerService.TAG, "Exception thrown sending new intent to " + this, e2);
            }
        }
        if (unsent) {
            addNewIntentLocked(rintent);
        }
    }

    /* access modifiers changed from: package-private */
    public void updateOptionsLocked(ActivityOptions options) {
        if (options != null) {
            if (this.pendingOptions != null) {
                this.pendingOptions.abort();
            }
            this.pendingOptions = options;
        }
    }

    /* access modifiers changed from: package-private */
    public void applyOptionsLocked() {
        if (this.pendingOptions != null && this.pendingOptions.getAnimationType() != 5) {
            int animationType = this.pendingOptions.getAnimationType();
            boolean z = true;
            switch (animationType) {
                case 1:
                    this.service.mWindowManager.overridePendingAppTransition(this.pendingOptions.getPackageName(), this.pendingOptions.getCustomEnterResId(), this.pendingOptions.getCustomExitResId(), this.pendingOptions.getOnAnimationStartListener());
                    break;
                case 2:
                    this.service.mWindowManager.overridePendingAppTransitionScaleUp(this.pendingOptions.getStartX(), this.pendingOptions.getStartY(), this.pendingOptions.getWidth(), this.pendingOptions.getHeight());
                    if (this.intent.getSourceBounds() == null) {
                        this.intent.setSourceBounds(new Rect(this.pendingOptions.getStartX(), this.pendingOptions.getStartY(), this.pendingOptions.getStartX() + this.pendingOptions.getWidth(), this.pendingOptions.getStartY() + this.pendingOptions.getHeight()));
                        break;
                    }
                    break;
                case 3:
                case 4:
                    boolean scaleUp = animationType == 3;
                    GraphicBuffer buffer = this.pendingOptions.getThumbnail();
                    this.service.mWindowManager.overridePendingAppTransitionThumb(buffer, this.pendingOptions.getStartX(), this.pendingOptions.getStartY(), this.pendingOptions.getOnAnimationStartListener(), scaleUp);
                    if (this.intent.getSourceBounds() == null && buffer != null) {
                        this.intent.setSourceBounds(new Rect(this.pendingOptions.getStartX(), this.pendingOptions.getStartY(), this.pendingOptions.getStartX() + buffer.getWidth(), this.pendingOptions.getStartY() + buffer.getHeight()));
                        break;
                    }
                case 8:
                case 9:
                    AppTransitionAnimationSpec[] specs = this.pendingOptions.getAnimSpecs();
                    IAppTransitionAnimationSpecsFuture specsFuture = this.pendingOptions.getSpecsFuture();
                    if (specsFuture == null) {
                        if (animationType == 9 && specs != null) {
                            this.service.mWindowManager.overridePendingAppTransitionMultiThumb(specs, this.pendingOptions.getOnAnimationStartListener(), this.pendingOptions.getAnimationFinishedListener(), false);
                            break;
                        } else {
                            this.service.mWindowManager.overridePendingAppTransitionAspectScaledThumb(this.pendingOptions.getThumbnail(), this.pendingOptions.getStartX(), this.pendingOptions.getStartY(), this.pendingOptions.getWidth(), this.pendingOptions.getHeight(), this.pendingOptions.getOnAnimationStartListener(), animationType == 8);
                            if (this.intent.getSourceBounds() == null) {
                                this.intent.setSourceBounds(new Rect(this.pendingOptions.getStartX(), this.pendingOptions.getStartY(), this.pendingOptions.getStartX() + this.pendingOptions.getWidth(), this.pendingOptions.getStartY() + this.pendingOptions.getHeight()));
                                break;
                            }
                        }
                    } else {
                        WindowManagerService windowManagerService = this.service.mWindowManager;
                        IRemoteCallback onAnimationStartListener = this.pendingOptions.getOnAnimationStartListener();
                        if (animationType != 8) {
                            z = false;
                        }
                        windowManagerService.overridePendingAppTransitionMultiThumbFuture(specsFuture, onAnimationStartListener, z);
                        break;
                    }
                    break;
                case 11:
                    this.service.mWindowManager.overridePendingAppTransitionClipReveal(this.pendingOptions.getStartX(), this.pendingOptions.getStartY(), this.pendingOptions.getWidth(), this.pendingOptions.getHeight());
                    if (this.intent.getSourceBounds() == null) {
                        this.intent.setSourceBounds(new Rect(this.pendingOptions.getStartX(), this.pendingOptions.getStartY(), this.pendingOptions.getStartX() + this.pendingOptions.getWidth(), this.pendingOptions.getStartY() + this.pendingOptions.getHeight()));
                        break;
                    }
                    break;
                case 12:
                    this.service.mWindowManager.overridePendingAppTransitionStartCrossProfileApps();
                    break;
                case 13:
                    this.service.mWindowManager.overridePendingAppTransitionRemote(this.pendingOptions.getRemoteAnimationAdapter());
                    break;
                default:
                    Slog.e(ActivityManagerService.TAG, "applyOptionsLocked: Unknown animationType=" + animationType);
                    break;
            }
            if (this.task == null) {
                clearOptionsLocked(false);
            } else {
                this.task.clearAllPendingOptions();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public ActivityOptions getOptionsForTargetActivityLocked() {
        if (this.pendingOptions != null) {
            return this.pendingOptions.forTargetActivity();
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void clearOptionsLocked() {
        clearOptionsLocked(true);
    }

    /* access modifiers changed from: package-private */
    public void clearOptionsLocked(boolean withAbort) {
        if (withAbort && this.pendingOptions != null) {
            this.pendingOptions.abort();
        }
        this.pendingOptions = null;
    }

    /* access modifiers changed from: package-private */
    public ActivityOptions takeOptionsLocked() {
        ActivityOptions opts = this.pendingOptions;
        this.pendingOptions = null;
        return opts;
    }

    /* access modifiers changed from: package-private */
    public void removeUriPermissionsLocked() {
        if (this.uriPermissions != null) {
            this.uriPermissions.removeUriPermissionsLocked();
            this.uriPermissions = null;
        }
    }

    /* access modifiers changed from: package-private */
    public void pauseKeyDispatchingLocked() {
        if (!this.keysPaused) {
            this.keysPaused = true;
            if (this.mWindowContainerController != null) {
                this.mWindowContainerController.pauseKeyDispatching();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void resumeKeyDispatchingLocked() {
        if (this.keysPaused) {
            this.keysPaused = false;
            if (this.mWindowContainerController != null) {
                this.mWindowContainerController.resumeKeyDispatching();
            }
        }
    }

    private void updateTaskDescription(CharSequence description) {
        this.task.lastDescription = description;
    }

    /* access modifiers changed from: package-private */
    public void setDeferHidingClient(boolean deferHidingClient) {
        if (this.mDeferHidingClient != deferHidingClient) {
            this.mDeferHidingClient = deferHidingClient;
            if (!this.mDeferHidingClient && !this.visible) {
                setVisibility(false);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setVisibility(boolean visible2) {
        if (this.mWindowContainerController != null) {
            this.mWindowContainerController.setVisibility(visible2, this.mDeferHidingClient);
            this.mStackSupervisor.getActivityMetricsLogger().notifyVisibilityChanged(this);
        }
    }

    /* access modifiers changed from: package-private */
    public void setVisible(boolean newVisible) {
        this.visible = newVisible;
        this.mDeferHidingClient = !this.visible && this.mDeferHidingClient;
        setVisibility(this.visible);
        this.mStackSupervisor.mAppVisibilitiesChangedSinceLastPause = true;
    }

    /* access modifiers changed from: package-private */
    public void setState(ActivityStack.ActivityState state, String reason) {
        if (ActivityManagerDebugConfig.DEBUG_STATES) {
            Slog.v(ActivityManagerService.TAG, "State movement: " + this + " from:" + getState() + " to:" + state + " reason:" + reason);
        }
        if (state == this.mState) {
            if (ActivityManagerDebugConfig.DEBUG_STATES) {
                Slog.v(ActivityManagerService.TAG, "State unchanged from:" + state);
            }
            return;
        }
        this.mState = state;
        TaskRecord parent = getTask();
        if (parent != null) {
            parent.onActivityStateChanged(this, state, reason);
        }
        if (state == ActivityStack.ActivityState.STOPPING && !isSleeping()) {
            this.mWindowContainerController.notifyAppStopping();
        }
    }

    /* access modifiers changed from: package-private */
    public ActivityStack.ActivityState getState() {
        return this.mState;
    }

    /* access modifiers changed from: package-private */
    public boolean isState(ActivityStack.ActivityState state) {
        return state == this.mState;
    }

    /* access modifiers changed from: package-private */
    public boolean isState(ActivityStack.ActivityState state1, ActivityStack.ActivityState state2) {
        return state1 == this.mState || state2 == this.mState;
    }

    /* access modifiers changed from: package-private */
    public boolean isState(ActivityStack.ActivityState state1, ActivityStack.ActivityState state2, ActivityStack.ActivityState state3) {
        return state1 == this.mState || state2 == this.mState || state3 == this.mState;
    }

    /* access modifiers changed from: package-private */
    public boolean isState(ActivityStack.ActivityState state1, ActivityStack.ActivityState state2, ActivityStack.ActivityState state3, ActivityStack.ActivityState state4) {
        return state1 == this.mState || state2 == this.mState || state3 == this.mState || state4 == this.mState;
    }

    /* access modifiers changed from: package-private */
    public void notifyAppResumed(boolean wasStopped) {
        this.mWindowContainerController.notifyAppResumed(wasStopped);
    }

    /* access modifiers changed from: package-private */
    public void notifyUnknownVisibilityLaunched() {
        if (!this.noDisplay) {
            this.mWindowContainerController.notifyUnknownVisibilityLaunched();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean shouldBeVisibleIgnoringKeyguard(boolean behindFullscreenActivity) {
        boolean z = false;
        if (!okToShowLocked()) {
            return false;
        }
        if (!behindFullscreenActivity || this.mLaunchTaskBehind) {
            z = true;
        }
        return z;
    }

    /* access modifiers changed from: package-private */
    public void makeVisibleIfNeeded(ActivityRecord starting, boolean reportToClient) {
        if (this.mState == ActivityStack.ActivityState.RESUMED || this == starting) {
            if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                String str = TAG_VISIBILITY;
                Slog.d(str, "Not making visible, r=" + this + " state=" + this.mState + " starting=" + starting);
            }
            return;
        }
        String str2 = TAG_VISIBILITY;
        Slog.v(str2, "Making visible and scheduling visibility: " + this);
        ActivityStack stack = getStack();
        try {
            if (stack.mTranslucentActivityWaiting != null) {
                updateOptionsLocked(this.returningOptions);
                stack.mUndrawnActivitiesBelowTopTranslucent.add(this);
            }
            setVisible(true);
            this.sleeping = false;
            this.app.pendingUiClean = true;
            if (reportToClient) {
                makeClientVisible();
            } else {
                this.mClientVisibilityDeferred = true;
            }
            this.mStackSupervisor.mStoppingActivities.remove(this);
            this.mStackSupervisor.mGoingToSleepActivities.remove(this);
        } catch (Exception e) {
            Slog.w(ActivityManagerService.TAG, "Exception thrown making visible: " + this.intent.getComponent(), e);
        }
        handleAlreadyVisible();
    }

    /* access modifiers changed from: package-private */
    public void makeClientVisible() {
        this.mClientVisibilityDeferred = false;
        try {
            this.service.getLifecycleManager().scheduleTransaction(this.app.thread, (IBinder) this.appToken, (ClientTransactionItem) WindowVisibilityItem.obtain(true));
            if (shouldPauseWhenBecomingVisible()) {
                setState(ActivityStack.ActivityState.PAUSING, "makeVisibleIfNeeded");
                this.service.getLifecycleManager().scheduleTransaction(this.app.thread, (IBinder) this.appToken, (ActivityLifecycleItem) PauseActivityItem.obtain(this.finishing, false, this.configChangeFlags, false));
            }
        } catch (Exception e) {
            Slog.w(ActivityManagerService.TAG, "Exception thrown sending visibility update: " + this.intent.getComponent(), e);
        }
    }

    private boolean shouldPauseWhenBecomingVisible() {
        if (!isState(ActivityStack.ActivityState.STOPPED, ActivityStack.ActivityState.STOPPING) || getStack().mTranslucentActivityWaiting != null || this.mStackSupervisor.getResumedActivityLocked() == this) {
            return false;
        }
        int positionInTask = this.task.mActivities.indexOf(this);
        if (positionInTask == -1) {
            throw new IllegalStateException("Activity not found in its task");
        } else if (positionInTask == this.task.mActivities.size() - 1) {
            return true;
        } else {
            if (!this.task.mActivities.get(positionInTask + 1).finishing || this.results != null) {
                return false;
            }
            return true;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean handleAlreadyVisible() {
        stopFreezingScreenLocked(false);
        try {
            if (this.returningOptions != null) {
                this.app.thread.scheduleOnNewActivityOptions(this.appToken, this.returningOptions.toBundle());
            }
        } catch (RemoteException e) {
        }
        if (this.mState == ActivityStack.ActivityState.RESUMED) {
            return true;
        }
        return false;
    }

    static void activityResumedLocked(IBinder token) {
        ActivityRecord r = forTokenLocked(token);
        if (ActivityManagerDebugConfig.DEBUG_SAVED_STATE) {
            Slog.i(ActivityManagerService.TAG, "Resumed activity; dropping state of: " + r);
        }
        if (r != null) {
            r.icicle = null;
            r.haveState = false;
        }
    }

    /* access modifiers changed from: package-private */
    public void completeResumeLocked() {
        boolean wasVisible = this.visible;
        setVisible(true);
        if (!wasVisible) {
            this.mStackSupervisor.mAppVisibilitiesChangedSinceLastPause = true;
        }
        this.idle = false;
        this.results = null;
        this.newIntents = null;
        this.stopped = false;
        if (isActivityTypeHome()) {
            ProcessRecord app2 = this.task.mActivities.get(0).app;
            if (!(app2 == null || app2 == this.service.mHomeProcess)) {
                this.service.mHomeProcess = app2;
                this.service.mHwAMSEx.reportHomeProcess(this.service.mHomeProcess);
            }
        }
        if (this.nowVisible) {
            this.mStackSupervisor.reportActivityVisibleLocked(this);
        }
        this.mStackSupervisor.scheduleIdleTimeoutLocked(this);
        this.mStackSupervisor.reportResumedActivityLocked(this);
        resumeKeyDispatchingLocked();
        ActivityStack stack = getStack();
        this.mStackSupervisor.mNoAnimActivities.clear();
        HwAudioServiceManager.setSoundEffectState(false, this.packageName, true, null);
        if (this.app != null) {
            this.cpuTimeAtResume = this.service.mProcessCpuTracker.getCpuTimeForPid(this.app.pid);
        } else {
            this.cpuTimeAtResume = 0;
        }
        this.returningOptions = null;
        if (canTurnScreenOn()) {
            this.mStackSupervisor.wakeUp("turnScreenOnFlag");
        } else {
            stack.checkReadyForSleep();
        }
        Flog.i(101, "Complete resume: " + this + ", launchTrack: " + this.mStackSupervisor.mActivityLaunchTrack);
        this.mStackSupervisor.mActivityLaunchTrack = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
    }

    /* access modifiers changed from: package-private */
    public final void activityStoppedLocked(Bundle newIcicle, PersistableBundle newPersistentState, CharSequence description) {
        ActivityStack stack = getStack();
        if (this.mState != ActivityStack.ActivityState.STOPPING) {
            Slog.i(ActivityManagerService.TAG, "Activity reported stop, but no longer stopping: " + this);
            stack.mHandler.removeMessages(104, this);
            return;
        }
        if (newPersistentState != null) {
            this.persistentState = newPersistentState;
            this.service.notifyTaskPersisterLocked(this.task, false);
        }
        if (ActivityManagerDebugConfig.DEBUG_SAVED_STATE) {
            Slog.i(ActivityManagerService.TAG, "Saving icicle of " + this + ": " + this.icicle);
        }
        if (newIcicle != null) {
            this.icicle = newIcicle;
            this.haveState = true;
            this.launchCount = 0;
            updateTaskDescription(description);
        }
        if (!this.stopped) {
            if (ActivityManagerDebugConfig.DEBUG_STATES) {
                Slog.v(ActivityManagerService.TAG, "Moving to STOPPED: " + this + " (stop complete)");
            }
            stack.mHandler.removeMessages(104, this);
            this.stopped = true;
            setState(ActivityStack.ActivityState.STOPPED, "activityStoppedLocked");
            this.mWindowContainerController.notifyAppStopped();
            if (this.finishing) {
                clearOptionsLocked();
            } else if (this.deferRelaunchUntilPaused) {
                stack.destroyActivityLocked(this, true, "stop-config");
                this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
            } else {
                this.mStackSupervisor.updatePreviousProcessLocked(this);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void startLaunchTickingLocked() {
        if (!Build.IS_USER && this.launchTickTime == 0) {
            this.launchTickTime = SystemClock.uptimeMillis();
            continueLaunchTickingLocked();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean continueLaunchTickingLocked() {
        if (this.launchTickTime == 0) {
            return false;
        }
        ActivityStack stack = getStack();
        if (stack == null) {
            return false;
        }
        Message msg = stack.mHandler.obtainMessage(103, this);
        stack.mHandler.removeMessages(103);
        stack.mHandler.sendMessageDelayed(msg, 500);
        return true;
    }

    /* access modifiers changed from: package-private */
    public void finishLaunchTickingLocked() {
        this.launchTickTime = 0;
        ActivityStack stack = getStack();
        if (stack != null) {
            stack.mHandler.removeMessages(103);
        }
    }

    public boolean mayFreezeScreenLocked(ProcessRecord app2) {
        return app2 != null && !app2.crashing && !app2.notResponding;
    }

    public void startFreezingScreenLocked(ProcessRecord app2, int configChanges) {
        if (mayFreezeScreenLocked(app2) && this.mWindowContainerController != null) {
            this.mWindowContainerController.startFreezingScreen(configChanges);
        }
    }

    public void stopFreezingScreenLocked(boolean force) {
        if (force || this.frozenBeforeDestroy) {
            this.frozenBeforeDestroy = false;
            if (this.mWindowContainerController != null) {
                this.mWindowContainerController.stopFreezingScreen(force);
            }
        }
    }

    public void reportFullyDrawnLocked(boolean restoredFromBundle) {
        long curTime = SystemClock.uptimeMillis();
        if (this.displayStartTime != 0) {
            reportLaunchTimeLocked(curTime);
        } else {
            Jlog.warmLaunchingAppEnd(this.shortComponentName);
        }
        LaunchTimeTracker.Entry entry = this.mStackSupervisor.getLaunchTimeTracker().getEntry(getWindowingMode());
        if (!(this.fullyDrawnStartTime == 0 || entry == null)) {
            long thisTime = curTime - this.fullyDrawnStartTime;
            long totalTime = entry.mFullyDrawnStartTime != 0 ? curTime - entry.mFullyDrawnStartTime : thisTime;
            Trace.asyncTraceEnd(64, "drawing", 0);
            EventLog.writeEvent(EventLogTags.AM_ACTIVITY_FULLY_DRAWN_TIME, new Object[]{Integer.valueOf(this.userId), Integer.valueOf(System.identityHashCode(this)), this.shortComponentName, Long.valueOf(thisTime), Long.valueOf(totalTime)});
            StringBuilder sb = this.service.mStringBuilder;
            sb.setLength(0);
            sb.append("Fully drawn ");
            sb.append(this.shortComponentName);
            sb.append(": ");
            TimeUtils.formatDuration(thisTime, sb);
            if (thisTime != totalTime) {
                sb.append(" (total ");
                TimeUtils.formatDuration(totalTime, sb);
                sb.append(")");
            }
            Log.i(ActivityManagerService.TAG, sb.toString());
            entry.mFullyDrawnStartTime = 0;
        }
        this.mStackSupervisor.getActivityMetricsLogger().logAppTransitionReportedDrawn(this, restoredFromBundle);
        this.fullyDrawnStartTime = 0;
    }

    public boolean isFloating() {
        return this.mIsFloating;
    }

    public boolean isTransluent() {
        return this.mIsTransluent;
    }

    private void reportLaunchTimeLocked(long curTime) {
        LaunchTimeTracker.Entry entry = this.mStackSupervisor.getLaunchTimeTracker().getEntry(getWindowingMode());
        if (entry != null) {
            long thisTime = curTime - this.displayStartTime;
            long totalTime = entry.mLaunchStartTime != 0 ? curTime - entry.mLaunchStartTime : thisTime;
            Trace.asyncTraceEnd(64, "launching: " + this.packageName, 0);
            EventLog.writeEvent(EventLogTags.AM_ACTIVITY_LAUNCH_TIME, new Object[]{Integer.valueOf(this.userId), Integer.valueOf(System.identityHashCode(this)), this.shortComponentName, Long.valueOf(thisTime), Long.valueOf(totalTime)});
            if (getStack() != null && getStack().mshortComponentName.equals(this.shortComponentName)) {
                if (this.app != null) {
                    Jlog.d(44, this.shortComponentName, (int) thisTime, "#PID:<" + String.valueOf(this.app.pid) + ">");
                } else {
                    Jlog.d(44, this.shortComponentName, (int) thisTime, "#PID:<0>");
                }
            }
            StringBuilder sb = this.service.mStringBuilder;
            sb.setLength(0);
            sb.append("Displayed ");
            sb.append(this.shortComponentName);
            sb.append(": ");
            TimeUtils.formatDuration(thisTime, sb);
            if (thisTime != totalTime) {
                sb.append(" (total ");
                TimeUtils.formatDuration(totalTime, sb);
                sb.append(")");
            }
            Log.i(ActivityManagerService.TAG, sb.toString());
            long j = totalTime;
            this.mStackSupervisor.reportActivityLaunchedLocked(false, this, thisTime, totalTime);
            this.displayStartTime = 0;
            this.service.mDAProxy.notifyAppEventToIaware(4, this.shortComponentName);
            entry.mLaunchStartTime = 0;
        }
    }

    public void onStartingWindowDrawn(long timestamp) {
        synchronized (this.service) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                this.mStackSupervisor.getActivityMetricsLogger().notifyStartingWindowDrawn(getWindowingMode(), timestamp);
            } catch (Throwable th) {
                while (true) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        ActivityManagerService.resetPriorityAfterLockedSection();
    }

    public void onWindowsDrawn(long timestamp) {
        synchronized (this.service) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                this.mStackSupervisor.getActivityMetricsLogger().notifyWindowsDrawn(getWindowingMode(), timestamp);
                if (this.displayStartTime != 0) {
                    reportLaunchTimeLocked(timestamp);
                } else {
                    Jlog.warmLaunchingAppEnd(this.shortComponentName);
                }
                this.mStackSupervisor.sendWaitingVisibleReportLocked(this);
                this.startTime = 0;
                finishLaunchTickingLocked();
                if (this.task != null) {
                    this.task.hasBeenVisible = true;
                }
            } catch (Throwable th) {
                while (true) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        ActivityManagerService.resetPriorityAfterLockedSection();
    }

    public void onWindowsVisible() {
        synchronized (this.service) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                this.mStackSupervisor.reportActivityVisibleLocked(this);
                if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                    Log.v(ActivityManagerService.TAG, "windowsVisibleLocked(): " + this);
                }
                if (!this.nowVisible) {
                    this.nowVisible = true;
                    this.lastVisibleTime = SystemClock.uptimeMillis();
                    int i = 0;
                    if (!this.idle) {
                        if (!this.mStackSupervisor.isStoppingNoHistoryActivity()) {
                            this.mStackSupervisor.processStoppingActivitiesLocked(null, false, true);
                            this.service.scheduleAppGcsLocked();
                        }
                    }
                    int size = this.mStackSupervisor.mActivitiesWaitingForVisibleActivity.size();
                    if (size > 0) {
                        while (true) {
                            int i2 = i;
                            if (i2 >= size) {
                                break;
                            }
                            ActivityRecord r = this.mStackSupervisor.mActivitiesWaitingForVisibleActivity.get(i2);
                            if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                                Log.v(ActivityManagerService.TAG, "Was waiting for visible: " + r);
                            }
                            i = i2 + 1;
                        }
                        this.mStackSupervisor.mActivitiesWaitingForVisibleActivity.clear();
                        this.mStackSupervisor.scheduleIdleLocked();
                    }
                    this.service.scheduleAppGcsLocked();
                }
            } catch (Throwable th) {
                while (true) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        ActivityManagerService.resetPriorityAfterLockedSection();
    }

    public void onWindowsGone() {
        synchronized (this.service) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                if (ActivityManagerService.isInCallActivity(this)) {
                    Flog.i(101, "Incall is gone");
                    Jlog.d(131, "JLID_PHONE_INCALLUI_CLOSE_END");
                }
                Flog.i(101, "windowsGone(): " + this);
                this.nowVisible = false;
            } catch (Throwable th) {
                while (true) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        ActivityManagerService.resetPriorityAfterLockedSection();
    }

    public boolean keyDispatchingTimedOut(String reason, int windowPid) {
        ActivityRecord anrActivity;
        ProcessRecord anrApp;
        boolean z;
        boolean windowFromSameProcessAsActivity;
        synchronized (this.service) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                anrActivity = getWaitingHistoryRecordLocked();
                anrApp = this.app;
                z = true;
                if (!(this.app == null || this.app.pid == windowPid)) {
                    if (windowPid != -1) {
                        windowFromSameProcessAsActivity = false;
                    }
                }
                windowFromSameProcessAsActivity = true;
            } catch (Throwable th) {
                while (true) {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
        }
        ActivityManagerService.resetPriorityAfterLockedSection();
        if (windowFromSameProcessAsActivity) {
            return this.service.inputDispatchingTimedOut(anrApp, anrActivity, this, false, reason);
        }
        if (this.service.inputDispatchingTimedOut(windowPid, false, reason) >= 0) {
            z = false;
        }
        return z;
    }

    private ActivityRecord getWaitingHistoryRecordLocked() {
        if (this.mStackSupervisor.mActivitiesWaitingForVisibleActivity.contains(this) || this.stopped) {
            ActivityStack stack = this.mStackSupervisor.getFocusedStack();
            ActivityRecord r = stack.getResumedActivity();
            if (r == null) {
                r = stack.mPausingActivity;
            }
            if (r != null) {
                return r;
            }
        }
        return this;
    }

    public boolean okToShowLocked() {
        boolean z = false;
        if (!StorageManager.isUserKeyUnlocked(this.userId) && !this.info.applicationInfo.isEncryptionAware()) {
            return false;
        }
        if ((this.info.flags & 1024) != 0 || (this.mStackSupervisor.isCurrentProfileLocked(this.userId) && this.service.mUserController.isUserRunning(this.userId, 0))) {
            z = true;
        }
        return z;
    }

    public boolean isInterestingToUserLocked() {
        return this.visible || this.nowVisible || this.mState == ActivityStack.ActivityState.PAUSING || this.mState == ActivityStack.ActivityState.RESUMED;
    }

    /* access modifiers changed from: package-private */
    public void setSleeping(boolean _sleeping) {
        setSleeping(_sleeping, false);
    }

    /* access modifiers changed from: package-private */
    public void setSleeping(boolean _sleeping, boolean force) {
        if (!((!force && this.sleeping == _sleeping) || this.app == null || this.app.thread == null)) {
            try {
                this.app.thread.scheduleSleeping(this.appToken, _sleeping);
                if (_sleeping && !this.mStackSupervisor.mGoingToSleepActivities.contains(this)) {
                    this.mStackSupervisor.mGoingToSleepActivities.add(this);
                }
                this.sleeping = _sleeping;
            } catch (RemoteException e) {
                Slog.w(ActivityManagerService.TAG, "Exception thrown when sleeping: " + this.intent.getComponent(), e);
            }
        }
    }

    static int getTaskForActivityLocked(IBinder token, boolean onlyRoot) {
        ActivityRecord r = forTokenLocked(token);
        if (r == null) {
            return -1;
        }
        TaskRecord task2 = r.task;
        int activityNdx = task2.mActivities.indexOf(r);
        if (activityNdx < 0 || (onlyRoot && activityNdx > task2.findEffectiveRootIndex())) {
            return -1;
        }
        return task2.taskId;
    }

    static ActivityRecord isInStackLocked(IBinder token) {
        ActivityRecord r = forTokenLocked(token);
        if (r != null) {
            return r.getStack().isInStackLocked(r);
        }
        return null;
    }

    static ActivityStack getStackLocked(IBinder token) {
        ActivityRecord r = isInStackLocked(token);
        if (r != null) {
            return r.getStack();
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public int getDisplayId() {
        ActivityStack stack = getStack();
        if (stack == null) {
            return -1;
        }
        return stack.mDisplayId;
    }

    /* access modifiers changed from: package-private */
    public final boolean isDestroyable() {
        if (this.finishing || this.app == null) {
            return false;
        }
        ActivityStack stack = getStack();
        if (stack == null || this == stack.getResumedActivity() || this == stack.mPausingActivity || !this.haveState || !this.stopped || this.visible) {
            return false;
        }
        return true;
    }

    private static String createImageFilename(long createTime2, int taskId) {
        return String.valueOf(taskId) + ACTIVITY_ICON_SUFFIX + createTime2 + ".png";
    }

    /* access modifiers changed from: package-private */
    public void setTaskDescription(ActivityManager.TaskDescription _taskDescription) {
        if (_taskDescription.getIconFilename() == null) {
            Bitmap icon2 = _taskDescription.getIcon();
            Bitmap icon3 = icon2;
            if (icon2 != null) {
                String iconFilePath = new File(TaskPersister.getUserImagesDir(this.task.userId), createImageFilename(this.createTime, this.task.taskId)).getAbsolutePath();
                this.service.getRecentTasks().saveImage(icon3, iconFilePath);
                _taskDescription.setIconFilename(iconFilePath);
            }
        }
        this.taskDescription = _taskDescription;
    }

    /* access modifiers changed from: package-private */
    public void setVoiceSessionLocked(IVoiceInteractionSession session) {
        this.voiceSession = session;
        this.pendingVoiceInteractionStart = false;
    }

    /* access modifiers changed from: package-private */
    public void clearVoiceSessionLocked() {
        this.voiceSession = null;
        this.pendingVoiceInteractionStart = false;
    }

    /* access modifiers changed from: package-private */
    public void showStartingWindow(ActivityRecord prev, boolean newTask, boolean taskSwitch) {
        showStartingWindow(prev, newTask, taskSwitch, false);
    }

    /* access modifiers changed from: package-private */
    public void showStartingWindow(ActivityRecord prev, boolean newTask, boolean taskSwitch, boolean fromRecents) {
        ActivityRecord activityRecord = prev;
        if (this.mWindowContainerController != null && !this.mTaskOverlay) {
            if (this.mWindowContainerController.addStartingWindow(this.packageName, this.theme, this.service.compatibilityInfoForPackageLocked(this.info.applicationInfo), this.nonLocalizedLabel, this.labelRes, this.icon, this.logo, this.windowFlags, activityRecord != null ? activityRecord.appToken : null, newTask, taskSwitch, isProcessRunning(), allowTaskSnapshot(), this.mState.ordinal() >= ActivityStack.ActivityState.RESUMED.ordinal() && this.mState.ordinal() <= ActivityStack.ActivityState.STOPPED.ordinal(), fromRecents)) {
                this.mStartingWindowState = 1;
                this.service.mHwAMSEx.dispatchActivityLifeState(this, "showStartingWindow");
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void removeOrphanedStartingWindow(boolean behindFullscreenActivity) {
        if (this.mStartingWindowState == 1 && behindFullscreenActivity) {
            if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                String str = TAG_VISIBILITY;
                Slog.w(str, "Found orphaned starting window " + this);
            }
            this.mStartingWindowState = 2;
            this.mWindowContainerController.removeStartingWindow();
        }
    }

    /* access modifiers changed from: package-private */
    public int getRequestedOrientation() {
        if (this.mWindowContainerController == null) {
            return -1;
        }
        return this.mWindowContainerController.getOrientation();
    }

    /* access modifiers changed from: package-private */
    public void setRequestedOrientation(int requestedOrientation) {
        if (this.mWindowContainerController != null) {
            int displayId = getDisplayId();
            Configuration config = this.mWindowContainerController.setOrientation(requestedOrientation, displayId, this.mStackSupervisor.getDisplayOverrideConfiguration(displayId), mayFreezeScreenLocked(this.app));
            if (config != null) {
                this.frozenBeforeDestroy = true;
                if (!this.service.updateDisplayOverrideConfigurationLocked(config, this, false, displayId)) {
                    this.mStackSupervisor.resumeFocusedStackTopActivityLocked();
                }
            }
            this.service.mTaskChangeNotificationController.notifyActivityRequestedOrientationChanged(this.task.taskId, requestedOrientation);
        }
    }

    /* access modifiers changed from: package-private */
    public void setDisablePreviewScreenshots(boolean disable) {
        if (this.mWindowContainerController != null) {
            this.mWindowContainerController.setDisablePreviewScreenshots(disable);
        }
    }

    /* access modifiers changed from: package-private */
    public void setLastReportedGlobalConfiguration(Configuration config) {
        this.mLastReportedConfiguration.setGlobalConfiguration(config);
    }

    /* access modifiers changed from: package-private */
    public void setLastReportedConfiguration(MergedConfiguration config) {
        setLastReportedConfiguration(config.getGlobalConfiguration(), config.getOverrideConfiguration());
    }

    private void setLastReportedConfiguration(Configuration global, Configuration override) {
        this.mLastReportedConfiguration.setConfiguration(global, override);
    }

    private void updateOverrideConfiguration() {
        this.mTmpConfig.unset();
        computeBounds(this.mTmpBounds);
        boolean isInMWPortraitWhiteList = false;
        if (this.task != null && this.task.inMultiWindowMode() && (!HwPCUtils.isPcCastModeInServer() || !HwPCUtils.isValidExtDisplayId(getDisplayId()))) {
            ActivityRecord topActivity = this.task.getTopActivity();
            if (topActivity != null) {
                isInMWPortraitWhiteList = this.service.getPackageManagerInternalLocked().isInMWPortraitWhiteList(topActivity.packageName);
            }
        }
        if (!this.mTmpBounds.equals(getOverrideBounds()) || isInMWPortraitWhiteList) {
            setBounds(this.mTmpBounds);
            Rect updatedBounds = getOverrideBounds();
            if (!matchParentBounds() || isInMWPortraitWhiteList) {
                this.task.computeOverrideConfiguration(this.mTmpConfig, updatedBounds, null, false, false);
            }
            if (!this.mTmpBounds.isEmpty()) {
                this.mTmpConfig.nonFullScreen = 1;
            } else {
                this.mTmpConfig.nonFullScreen = 0;
            }
            onOverrideConfigurationChanged(this.mTmpConfig);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isConfigurationCompatible(Configuration config) {
        int orientation = this.mWindowContainerController != null ? this.mWindowContainerController.getOrientation() : this.info.screenOrientation;
        if (!ActivityInfo.isFixedOrientationPortrait(orientation) || config.orientation == 1) {
            return !ActivityInfo.isFixedOrientationLandscape(orientation) || config.orientation == 2;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void computeBounds(Rect outBounds) {
        outBounds.setEmpty();
        this.maxAspectRatio = this.info.maxAspectRatio;
        if (mDeviceMaxRatio < 0.0f) {
            mDeviceMaxRatio = this.service.mWindowManager.getDeviceMaxRatio();
        }
        float userMaxAspectRatio = 0.0f;
        if (mDeviceMaxRatio > 0.0f && this.service != null && !TextUtils.isEmpty(this.packageName)) {
            userMaxAspectRatio = this.service.getPackageManagerInternalLocked().getUserMaxAspectRatio(this.packageName);
        }
        if (userMaxAspectRatio != 0.0f) {
            if (userMaxAspectRatio >= mDeviceMaxRatio || ((double) this.info.originMaxAspectRatio) <= 1.0d) {
                this.maxAspectRatio = userMaxAspectRatio;
            } else {
                this.maxAspectRatio = this.info.originMaxAspectRatio;
            }
        }
        int displayMode = 0;
        if (HwFoldScreenState.isFoldScreenDevice()) {
            displayMode = ((HwFoldScreenManagerInternal) LocalServices.getService(HwFoldScreenManagerInternal.class)).getDisplayMode();
            if (displayMode == 1) {
                if (DEBUG_FULL || this.info.resizeMode == 2 || this.info.resizeMode == 1) {
                    this.minAspectRatio = 0.0f;
                } else {
                    float userSetMinAspect = this.appInfo.canChangeAspectRatio("minAspectRatio") ? this.service.getPackageManagerInternalLocked().getUserAspectRatio(this.packageName, "minAspectRatio") : 0.0f;
                    this.minAspectRatio = userSetMinAspect != 0.0f ? userSetMinAspect : this.info.applicationInfo.minAspectRatio;
                    if (((double) Math.abs(this.minAspectRatio - HwFoldScreenState.getScreenFoldFullRatio())) < 1.0E-8d) {
                        this.minAspectRatio = 0.0f;
                    }
                }
            }
        }
        ActivityStack stack = getStack();
        if (this.task != null && stack != null && !this.task.inMultiWindowMode() && ((this.maxAspectRatio != 0.0f || this.minAspectRatio != 0.0f) && !isInVrUiMode(getConfiguration()))) {
            Rect appBounds = getParent().getWindowConfiguration().getAppBounds();
            if (!HwFoldScreenState.isFoldScreenDevice() || displayMode != 1) {
                int containingAppWidth = appBounds.width();
                int containingAppHeight = appBounds.height();
                int maxActivityWidth = containingAppWidth;
                int maxActivityHeight = containingAppHeight;
                if (containingAppWidth < containingAppHeight) {
                    maxActivityHeight = (int) ((((float) maxActivityWidth) * this.maxAspectRatio) + 0.5f);
                } else {
                    maxActivityWidth = (int) ((((float) maxActivityHeight) * this.maxAspectRatio) + 0.5f);
                }
                if (HwFullScreenDisplay.getDeviceMaxRatio() > this.maxAspectRatio || containingAppWidth > maxActivityWidth || containingAppHeight > maxActivityHeight) {
                    outBounds.set(0, 0, appBounds.left + maxActivityWidth, appBounds.top + maxActivityHeight);
                    if (this.service.mWindowManager.getNavBarPosition() == 1) {
                        outBounds.left = appBounds.right - maxActivityWidth;
                        outBounds.right = appBounds.right;
                    }
                    this.service.mWindowManager.getAppDisplayRect(this.maxAspectRatio, outBounds, appBounds.left);
                    return;
                }
                Rect rect = getOverrideBounds();
                if (HwFoldScreenState.isFoldScreenDevice()) {
                    if (rect.isEmpty() || rect.equals(appBounds)) {
                        outBounds.set(rect);
                    } else {
                        outBounds.set(appBounds);
                    }
                    return;
                }
                outBounds.set(rect);
                return;
            }
            HwFoldScreenState.getAppFolderDisplayRect(displayMode, this.minAspectRatio, outBounds, appBounds, this.service.mWindowManager.getDefaultDisplayContentLocked().getRotation());
        }
    }

    /* access modifiers changed from: package-private */
    public boolean ensureActivityConfiguration(int globalChanges, boolean preserveWindow) {
        return ensureActivityConfiguration(globalChanges, preserveWindow, false);
    }

    /* access modifiers changed from: package-private */
    public boolean ensureActivityConfiguration(int globalChanges, boolean preserveWindow, boolean ignoreStopState) {
        ActivityStack stack = getStack();
        if (stack.mConfigWillChange) {
            if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                Slog.v(TAG_CONFIGURATION, "Skipping config check (will change): " + this);
            }
            return true;
        } else if (this.finishing) {
            if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                Slog.v(TAG_CONFIGURATION, "Configuration doesn't matter in finishing " + this);
            }
            stopFreezingScreenLocked(false);
            return true;
        } else if (!ignoreStopState && !isSplitBaseActivity() && (this.mState == ActivityStack.ActivityState.STOPPING || this.mState == ActivityStack.ActivityState.STOPPED)) {
            if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                Slog.v(TAG_CONFIGURATION, "Skipping config check stopped or stopping: " + this);
            }
            return true;
        } else if (!stack.shouldBeVisible(null)) {
            if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                Slog.v(TAG_CONFIGURATION, "Skipping config check invisible stack: " + this);
            }
            return true;
        } else {
            if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                Slog.v(TAG_CONFIGURATION, "Ensuring correct configuration: " + this);
            }
            int newDisplayId = getDisplayId();
            boolean displayChanged = this.mLastReportedDisplayId != newDisplayId;
            if (displayChanged) {
                this.mLastReportedDisplayId = newDisplayId;
            }
            updateOverrideConfiguration();
            this.mTmpConfig.setTo(this.mLastReportedConfiguration.getMergedConfiguration());
            if (!getConfiguration().equals(this.mTmpConfig) || this.forceNewConfig || displayChanged) {
                int changes = getConfigurationChanges(this.mTmpConfig);
                Configuration newMergedOverrideConfig = getMergedOverrideConfiguration();
                setLastReportedConfiguration(this.service.getGlobalConfiguration(), newMergedOverrideConfig);
                if (this.mState == ActivityStack.ActivityState.INITIALIZING) {
                    if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                        Slog.v(TAG_CONFIGURATION, "Skipping config check for initializing activity: " + this);
                    }
                    return true;
                } else if (changes == 0 && !this.forceNewConfig) {
                    if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                        Slog.v(TAG_CONFIGURATION, "Configuration no differences in " + this);
                    }
                    if (displayChanged) {
                        scheduleActivityMovedToDisplay(newDisplayId, newMergedOverrideConfig);
                    } else {
                        scheduleConfigurationChanged(newMergedOverrideConfig);
                    }
                    return true;
                } else if (this.app == null || this.app.thread == null) {
                    if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                        Slog.v(TAG_CONFIGURATION, "Configuration doesn't matter not running " + this);
                    }
                    stopFreezingScreenLocked(false);
                    this.forceNewConfig = false;
                    return true;
                } else {
                    if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                        Slog.v(TAG_CONFIGURATION, "Checking to restart " + this.info.name + ": changed=0x" + Integer.toHexString(changes) + ", handles=0x" + Integer.toHexString(this.info.getRealConfigChanged()) + ", mLastReportedConfiguration=" + this.mLastReportedConfiguration + ", forceNewConfig:" + this.forceNewConfig);
                    }
                    if (shouldRelaunchLocked(changes, this.mTmpConfig) || this.forceNewConfig) {
                        this.configChangeFlags |= changes;
                        startFreezingScreenLocked(this.app, globalChanges);
                        this.forceNewConfig = false;
                        boolean preserveWindow2 = preserveWindow & isResizeOnlyChange(changes);
                        if (this.app == null || this.app.thread == null) {
                            if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                                Slog.v(TAG_CONFIGURATION, "Config is destroying non-running " + this);
                            }
                            stack.destroyActivityLocked(this, true, "config");
                        } else if (this.mState == ActivityStack.ActivityState.PAUSING) {
                            if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                                Slog.v(TAG_CONFIGURATION, "Config is skipping already pausing " + this);
                            }
                            this.deferRelaunchUntilPaused = true;
                            this.preserveWindowOnDeferredRelaunch = preserveWindow2;
                            return true;
                        } else if (this.mState == ActivityStack.ActivityState.RESUMED) {
                            if (!ActivityManagerDebugConfig.DEBUG_STATES || this.visible) {
                                Slog.v(TAG_CONFIGURATION, "Config is relaunching resumed " + this + ", changes=0x" + Integer.toHexString(changes));
                            } else {
                                Slog.v(ActivityManagerService.TAG, "Config is relaunching resumed invisible activity " + this + " called by " + Debug.getCallers(4));
                            }
                            relaunchActivityLocked(true, preserveWindow2);
                        } else {
                            Slog.v(TAG_CONFIGURATION, "Config is relaunching non-resumed " + this + ", changes=0x" + Integer.toHexString(changes));
                            relaunchActivityLocked(false, preserveWindow2);
                        }
                        return false;
                    }
                    if (displayChanged) {
                        scheduleActivityMovedToDisplay(newDisplayId, newMergedOverrideConfig);
                    } else {
                        scheduleConfigurationChanged(newMergedOverrideConfig);
                    }
                    stopFreezingScreenLocked(false);
                    return true;
                }
            } else {
                if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                    Slog.v(TAG_CONFIGURATION, "Configuration & display unchanged in " + this);
                }
                return true;
            }
        }
    }

    private boolean shouldRelaunchLocked(int changes, Configuration changesConfig) {
        int configChanged = overrideRealConfigChanged(this.info);
        boolean onlyVrUiModeChanged = onlyVrUiModeChanged(changes, changesConfig);
        if (this.appInfo.targetSdkVersion < 26 && this.requestedVrComponent != null && onlyVrUiModeChanged) {
            configChanged |= 512;
        }
        if (ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
            String str = TAG_CONFIGURATION;
            Slog.v(str, "shouldRelaunchLocked configChanged=0x" + Integer.toHexString(configChanged));
        }
        return ((~configChanged) & changes) != 0;
    }

    private boolean onlyVrUiModeChanged(int changes, Configuration lastReportedConfig) {
        return changes == 512 && isInVrUiMode(getConfiguration()) != isInVrUiMode(lastReportedConfig);
    }

    /* access modifiers changed from: protected */
    public int getConfigurationChanges(Configuration lastReportedConfig) {
        Configuration currentConfig = getConfiguration();
        int changes = lastReportedConfig.diff(currentConfig);
        if ((changes & 1024) != 0) {
            if (!(crossesHorizontalSizeThreshold(lastReportedConfig.screenWidthDp, currentConfig.screenWidthDp) || crossesVerticalSizeThreshold(lastReportedConfig.screenHeightDp, currentConfig.screenHeightDp))) {
                changes &= -1025;
            }
        }
        if ((changes != false && true) && !crossesSmallestSizeThreshold(lastReportedConfig.smallestScreenWidthDp, currentConfig.smallestScreenWidthDp)) {
            changes &= -2049;
        }
        if ((536870912 & changes) != 0) {
            return changes & -536870913;
        }
        return changes;
    }

    private static boolean isResizeOnlyChange(int change) {
        return (change & -3457) == 0;
    }

    /* access modifiers changed from: package-private */
    public void relaunchActivityLocked(boolean andResume, boolean preserveWindow) {
        int i;
        ActivityLifecycleItem lifecycleItem;
        if (!this.service.mSuppressResizeConfigChanges || !preserveWindow) {
            List<ResultInfo> pendingResults2 = null;
            List<ReferrerIntent> pendingNewIntents = null;
            if (andResume) {
                pendingResults2 = this.results;
                pendingNewIntents = this.newIntents;
            }
            if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                Slog.v(ActivityManagerService.TAG, "Relaunching: " + this + " with results=" + pendingResults2 + " newIntents=" + pendingNewIntents + " andResume=" + andResume + " preserveWindow=" + preserveWindow);
            }
            if (andResume) {
                i = EventLogTags.AM_RELAUNCH_RESUME_ACTIVITY;
            } else {
                i = EventLogTags.AM_RELAUNCH_ACTIVITY;
            }
            EventLog.writeEvent(i, new Object[]{Integer.valueOf(this.userId), Integer.valueOf(System.identityHashCode(this)), Integer.valueOf(this.task.taskId), this.shortComponentName});
            startFreezingScreenLocked(this.app, 0);
            try {
                if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_STATES) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Moving to ");
                    sb.append(andResume ? "RESUMED" : "PAUSED");
                    sb.append(" Relaunching ");
                    sb.append(this);
                    sb.append(" callers=");
                    sb.append(Debug.getCallers(6));
                    Slog.i(ActivityManagerService.TAG, sb.toString());
                }
                this.forceNewConfig = false;
                this.mStackSupervisor.activityRelaunchingLocked(this);
                ClientTransactionItem callbackItem = ActivityRelaunchItem.obtain(pendingResults2, pendingNewIntents, this.configChangeFlags, new MergedConfiguration(this.service.getGlobalConfiguration(), getMergedOverrideConfiguration()), preserveWindow);
                if (andResume) {
                    lifecycleItem = ResumeActivityItem.obtain(this.service.isNextTransitionForward());
                } else {
                    lifecycleItem = PauseActivityItem.obtain();
                }
                ClientTransaction transaction = ClientTransaction.obtain(this.app.thread, this.appToken);
                transaction.addCallback(callbackItem);
                transaction.setLifecycleStateRequest(lifecycleItem);
                this.service.getLifecycleManager().scheduleTransaction(transaction);
            } catch (RemoteException e) {
                if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_STATES) {
                    Slog.i(ActivityManagerService.TAG, "Relaunch failed", e);
                }
            }
            if (andResume) {
                if (ActivityManagerDebugConfig.DEBUG_STATES) {
                    Slog.d(ActivityManagerService.TAG, "Resumed after relaunch " + this);
                }
                this.results = null;
                this.newIntents = null;
                this.service.getAppWarningsLocked().onResumeActivity(this);
                this.service.showAskCompatModeDialogLocked(this);
            } else {
                this.service.mHandler.removeMessages(101, this);
                setState(ActivityStack.ActivityState.PAUSED, "relaunchActivityLocked");
            }
            this.configChangeFlags = 0;
            this.deferRelaunchUntilPaused = false;
            this.preserveWindowOnDeferredRelaunch = false;
            return;
        }
        this.configChangeFlags = 0;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v5, resolved type: java.lang.Object} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v2, resolved type: com.android.server.am.ProcessRecord} */
    /* JADX WARNING: Multi-variable type inference failed */
    private boolean isProcessRunning() {
        ProcessRecord proc = this.app;
        if (proc == null) {
            proc = this.service.mProcessNames.get(this.processName, this.info.applicationInfo.uid);
        }
        return (proc == null || proc.thread == null) ? false : true;
    }

    private boolean allowTaskSnapshot() {
        if (this.newIntents == null) {
            return true;
        }
        for (int i = this.newIntents.size() - 1; i >= 0; i--) {
            Intent intent2 = this.newIntents.get(i);
            if (intent2 != null && !isMainIntent(intent2)) {
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean isNoHistory() {
        return ((this.intent.getFlags() & 1073741824) == 0 && (this.info.flags & 128) == 0) ? false : true;
    }

    /* access modifiers changed from: package-private */
    public void saveToXml(XmlSerializer out) throws IOException, XmlPullParserException {
        out.attribute(null, ATTR_ID, String.valueOf(this.createTime));
        out.attribute(null, ATTR_LAUNCHEDFROMUID, String.valueOf(this.launchedFromUid));
        if (this.launchedFromPackage != null) {
            out.attribute(null, ATTR_LAUNCHEDFROMPACKAGE, this.launchedFromPackage);
        }
        if (this.resolvedType != null) {
            out.attribute(null, ATTR_RESOLVEDTYPE, this.resolvedType);
        }
        out.attribute(null, ATTR_COMPONENTSPECIFIED, String.valueOf(this.componentSpecified));
        out.attribute(null, ATTR_USERID, String.valueOf(this.userId));
        if (this.taskDescription != null) {
            this.taskDescription.saveToXml(out);
        }
        out.startTag(null, "intent");
        this.intent.saveToXml(out);
        out.endTag(null, "intent");
        if (isPersistable() && this.persistentState != null) {
            out.startTag(null, TAG_PERSISTABLEBUNDLE);
            this.persistentState.saveToXml(out);
            out.endTag(null, TAG_PERSISTABLEBUNDLE);
        }
    }

    static ActivityRecord restoreFromXml(XmlPullParser in, ActivityStackSupervisor stackSupervisor) throws IOException, XmlPullParserException {
        int outerDepth;
        PersistableBundle persistentState2;
        Intent intent2;
        XmlPullParser xmlPullParser = in;
        Intent intent3 = null;
        PersistableBundle persistentState3 = null;
        int launchedFromUid2 = 0;
        String launchedFromPackage2 = null;
        String resolvedType2 = null;
        boolean componentSpecified2 = false;
        int userId2 = 0;
        long createTime2 = -1;
        int outerDepth2 = in.getDepth();
        ActivityManager.TaskDescription taskDescription2 = new ActivityManager.TaskDescription();
        int attrNdx = in.getAttributeCount() - 1;
        while (attrNdx >= 0) {
            String attrName = xmlPullParser.getAttributeName(attrNdx);
            String attrValue = xmlPullParser.getAttributeValue(attrNdx);
            if (ATTR_ID.equals(attrName)) {
                createTime2 = Long.parseLong(attrValue);
            } else if (ATTR_LAUNCHEDFROMUID.equals(attrName)) {
                launchedFromUid2 = Integer.parseInt(attrValue);
            } else if (ATTR_LAUNCHEDFROMPACKAGE.equals(attrName)) {
                launchedFromPackage2 = attrValue;
            } else if (ATTR_RESOLVEDTYPE.equals(attrName)) {
                resolvedType2 = attrValue;
            } else if (ATTR_COMPONENTSPECIFIED.equals(attrName)) {
                componentSpecified2 = Boolean.parseBoolean(attrValue);
            } else if (ATTR_USERID.equals(attrName)) {
                userId2 = Integer.parseInt(attrValue);
            } else if (attrName.startsWith("task_description_")) {
                taskDescription2.restoreFromXml(attrName, attrValue);
            } else {
                intent2 = intent3;
                StringBuilder sb = new StringBuilder();
                persistentState2 = persistentState3;
                sb.append("Unknown ActivityRecord attribute=");
                sb.append(attrName);
                Log.d(ActivityManagerService.TAG, sb.toString());
                attrNdx--;
                intent3 = intent2;
                persistentState3 = persistentState2;
            }
            intent2 = intent3;
            persistentState2 = persistentState3;
            attrNdx--;
            intent3 = intent2;
            persistentState3 = persistentState2;
        }
        PersistableBundle persistableBundle = persistentState3;
        while (true) {
            int next = in.next();
            int event = next;
            if (next != 1) {
                if (event == 3 && in.getDepth() < outerDepth2) {
                    int i = outerDepth2;
                    break;
                } else if (event == 2) {
                    String name = in.getName();
                    if ("intent".equals(name)) {
                        intent3 = Intent.restoreFromXml(in);
                    } else if (TAG_PERSISTABLEBUNDLE.equals(name)) {
                        persistentState3 = PersistableBundle.restoreFromXml(in);
                    } else {
                        StringBuilder sb2 = new StringBuilder();
                        outerDepth = outerDepth2;
                        sb2.append("restoreActivity: unexpected name=");
                        sb2.append(name);
                        Slog.w(ActivityManagerService.TAG, sb2.toString());
                        XmlUtils.skipCurrentTag(in);
                        outerDepth2 = outerDepth;
                    }
                    outerDepth = outerDepth2;
                    outerDepth2 = outerDepth;
                }
            } else {
                break;
            }
        }
        if (intent3 != null) {
            ActivityStackSupervisor activityStackSupervisor = stackSupervisor;
            ActivityManagerService service2 = activityStackSupervisor.mService;
            ActivityInfo aInfo = activityStackSupervisor.resolveActivity(intent3, resolvedType2, 0, null, userId2, Binder.getCallingUid());
            if (aInfo != null) {
                ActivityRecord r = HwServiceFactory.createActivityRecord(service2, null, 0, launchedFromUid2, launchedFromPackage2, intent3, resolvedType2, aInfo, service2.getConfiguration(), null, null, 0, componentSpecified2, false, activityStackSupervisor, null, null);
                r.persistentState = persistentState3;
                r.taskDescription = taskDescription2;
                r.createTime = createTime2;
                return r;
            }
            throw new XmlPullParserException("restoreActivity resolver error. Intent=" + intent3 + " resolvedType=" + resolvedType2);
        }
        ActivityStackSupervisor activityStackSupervisor2 = stackSupervisor;
        throw new XmlPullParserException("restoreActivity error intent=" + intent3);
    }

    private static boolean isInVrUiMode(Configuration config) {
        return (config.uiMode & 15) == 7;
    }

    /* access modifiers changed from: package-private */
    public int getUid() {
        return this.info.applicationInfo.uid;
    }

    /* access modifiers changed from: package-private */
    public void setShowWhenLocked(boolean showWhenLocked) {
        this.mShowWhenLocked = showWhenLocked;
        if (this.mWindowContainerController != null) {
            this.mWindowContainerController.setShowWhenLocked(this.mShowWhenLocked);
        }
        this.mStackSupervisor.ensureActivitiesVisibleLocked(null, 0, false);
    }

    /* access modifiers changed from: package-private */
    public boolean canShowWhenLocked() {
        return !inPinnedWindowingMode() && (this.mShowWhenLocked || this.service.mWindowManager.containsShowWhenLockedWindow(this.appToken));
    }

    /* access modifiers changed from: package-private */
    public void setTurnScreenOn(boolean turnScreenOn) {
        this.mTurnScreenOn = turnScreenOn;
    }

    /* access modifiers changed from: package-private */
    public boolean canTurnScreenOn() {
        ActivityStack stack = getStack();
        if (!this.mTurnScreenOn || stack == null || !stack.checkKeyguardVisibility(this, true, true)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean getTurnScreenOnFlag() {
        return this.mTurnScreenOn;
    }

    /* access modifiers changed from: package-private */
    public boolean isTopRunningActivity() {
        return this.mStackSupervisor.topRunningActivityLocked() == this;
    }

    /* access modifiers changed from: package-private */
    public void registerRemoteAnimations(RemoteAnimationDefinition definition) {
        this.mWindowContainerController.registerRemoteAnimations(definition);
    }

    public String toString() {
        if (this.stringName != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(this.stringName);
            sb.append(" t");
            sb.append(this.task == null ? -1 : this.task.taskId);
            sb.append(this.finishing ? " f}" : "}");
            return sb.toString();
        }
        StringBuilder sb2 = new StringBuilder(128);
        sb2.append("ActivityRecord{");
        sb2.append(Integer.toHexString(System.identityHashCode(this)));
        sb2.append(" u");
        sb2.append(this.userId);
        sb2.append(' ');
        sb2.append(this.intent.getComponent().flattenToShortString());
        this.stringName = sb2.toString();
        return toString();
    }

    /* access modifiers changed from: package-private */
    public void writeIdentifierToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        proto.write(1120986464257L, System.identityHashCode(this));
        proto.write(1120986464258L, this.userId);
        proto.write(1138166333443L, this.intent.getComponent().flattenToShortString());
        proto.end(token);
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        super.writeToProto(proto, 1146756268033L, false);
        writeIdentifierToProto(proto, 1146756268034L);
        proto.write(1138166333443L, this.mState.toString());
        proto.write(1133871366148L, this.visible);
        proto.write(1133871366149L, this.frontOfTask);
        if (this.app != null) {
            proto.write(1120986464262L, this.app.pid);
        }
        proto.write(1133871366151L, !this.fullscreen);
        proto.end(token);
    }
}
