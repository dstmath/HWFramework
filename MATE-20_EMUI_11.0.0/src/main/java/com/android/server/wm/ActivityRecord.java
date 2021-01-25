package com.android.server.wm;

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
import android.app.servertransaction.StopActivityItem;
import android.app.servertransaction.TopResumedActivityChangeItem;
import android.app.servertransaction.WindowVisibilityItem;
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
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.service.voice.IVoiceInteractionSession;
import android.util.CoordinationModeUtils;
import android.util.EventLog;
import android.util.Flog;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.MergedConfiguration;
import android.util.Slog;
import android.util.TimeUtils;
import android.util.proto.ProtoOutputStream;
import android.view.AppTransitionAnimationSpec;
import android.view.IAppTransitionAnimationSpecsFuture;
import android.view.IApplicationToken;
import android.view.RemoteAnimationDefinition;
import com.android.internal.R;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.app.ResolverActivity;
import com.android.internal.content.ReferrerIntent;
import com.android.internal.util.XmlUtils;
import com.android.server.AttributeCache;
import com.android.server.HwServiceFactory;
import com.android.server.am.AppTimeTracker;
import com.android.server.am.PendingIntentRecord;
import com.android.server.uri.UriPermissionOwner;
import com.android.server.wm.ActivityMetricsLogger;
import com.android.server.wm.ActivityStack;
import com.android.server.wm.WindowManagerService;
import com.huawei.android.app.HwActivityTaskManager;
import com.huawei.android.content.pm.HwPackageManager;
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

public class ActivityRecord extends AbsActivityRecord {
    static final String ACTIVITY_ICON_SUFFIX = "_activity_icon_";
    private static final String ATTR_COMPONENTSPECIFIED = "component_specified";
    private static final String ATTR_ID = "id";
    private static final String ATTR_LAUNCHEDFROMPACKAGE = "launched_from_package";
    private static final String ATTR_LAUNCHEDFROMUID = "launched_from_uid";
    private static final String ATTR_RESOLVEDTYPE = "resolved_type";
    private static final String ATTR_USERID = "user_id";
    private static final int CONFIG_RELAUNCH_DISABLE = 7;
    private static final int CONFIG_RELAUNCH_SWITCH_ON = 5;
    private static final boolean IS_FORCE_RELAUNCH_WHEN_APP_BOUNDS_CHANGE = SystemProperties.getBoolean("persist.debug.relaunch_multiwindow", false);
    private static final String LEGACY_RECENTS_PACKAGE_NAME = "com.android.systemui.recents";
    private static final String LEGACY_RECENTS_PACKAGE_NAME_LAUNCHER = "com.huawei.android.launcher.quickstep";
    private static final boolean SHOW_ACTIVITY_START_TIME = true;
    static final int STARTING_WINDOW_NOT_SHOWN = 0;
    static final int STARTING_WINDOW_REMOVED = 2;
    static final int STARTING_WINDOW_SHOWN = 1;
    private static final String TAG = "ActivityTaskManager";
    private static final String TAG_CONFIGURATION = ("ActivityTaskManager" + ActivityTaskManagerDebugConfig.POSTFIX_CONFIGURATION);
    private static final String TAG_FOCUS = "ActivityTaskManager";
    private static final String TAG_INTENT = "intent";
    private static final String TAG_PERSISTABLEBUNDLE = "persistable_bundle";
    private static final String TAG_SAVED_STATE = "ActivityTaskManager";
    private static final String TAG_STATES = "ActivityTaskManager";
    private static final String TAG_SWITCH = "ActivityTaskManager";
    private static final String TAG_VISIBILITY = ("ActivityTaskManager" + ActivityTaskManagerDebugConfig.POSTFIX_VISIBILITY);
    private static final int TYPE_RELAUNCH_ATTRIBUTE = 4;
    private static final int TYPE_RELAUNCH_SWITCH = 3;
    private static final int UNIPERF_BOOST_OFF = 4;
    WindowProcessController app;
    ApplicationInfo appInfo;
    AppTimeTracker appTimeTracker;
    final IApplicationToken.Stub appToken;
    final Binder assistToken = new Binder();
    CompatibilityInfo compat;
    private final boolean componentSpecified;
    int configChangeFlags;
    long cpuTimeAtResume;
    private long createTime = System.currentTimeMillis();
    boolean deferRelaunchUntilPaused;
    boolean delayedResume;
    boolean finishing;
    boolean forceNewConfig;
    boolean frontOfTask;
    boolean frozenBeforeDestroy;
    boolean fullscreen;
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
    long launchTimeRecord;
    final String launchedFromPackage;
    final int launchedFromPid;
    final int launchedFromUid;
    int lockTaskLaunchMode;
    private int logo;
    final ComponentName mActivityComponent;
    AppWindowToken mAppWindowToken;
    final ActivityTaskManagerService mAtmService;
    boolean mClientVisibilityDeferred;
    private CompatDisplayInsets mCompatDisplayInsets;
    private int mConfigurationSeq;
    private boolean mDeferHidingClient;
    private boolean mDisplayModeChanged = false;
    boolean mDrawn;
    @VisibleForTesting
    int mHandoverLaunchDisplayId = -1;
    private int[] mHorizontalSizeConfigurations;
    private boolean mInheritShownWhenLocked;
    private boolean mIsChangeToDefaultDisplay = false;
    private boolean mIsFloating;
    private boolean mIsIgnoreColorMode = false;
    public boolean mIsMwNewTask = false;
    private boolean mIsTransluent;
    private MergedConfiguration mLastReportedConfiguration;
    private int mLastReportedDisplayId;
    private int mLastReportedDisplayMode = 0;
    private boolean mLastReportedMultiWindowMode;
    private boolean mLastReportedPictureInPictureMode;
    boolean mLaunchTaskBehind;
    private int mOriginalDisplayMode = 0;
    int mRelaunchReason = 0;
    final RootActivityContainer mRootActivityContainer;
    int mRotationAnimationHint = -1;
    ActivityServiceConnectionsHolder mServiceConnectionsHolder;
    protected boolean mShowWhenLocked;
    private int[] mSmallestSizeConfigurations;
    final ActivityStackSupervisor mStackSupervisor;
    int mStartingWindowState = 0;
    private ActivityStack.ActivityState mState;
    boolean mTaskOverlay = false;
    private final Rect mTmpBounds = new Rect();
    private final Configuration mTmpConfig = new Configuration();
    private boolean mTurnScreenOn;
    final int mUserId;
    private int[] mVerticalSizeConfigurations;
    public float maxAspectRatio = 0.0f;
    public float minAspectRatio = 0.0f;
    ArrayList<ReferrerIntent> newIntents;
    @VisibleForTesting
    boolean noDisplay;
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
    final String processName;
    private int realTheme;
    final int requestCode;
    ComponentName requestedVrComponent;
    final String resolvedType;
    ActivityRecord resultTo;
    final String resultWho;
    ArrayList<ResultInfo> results;
    ActivityOptions returningOptions;
    final boolean rootVoiceInteraction;
    final String shortComponentName;
    boolean sleeping;
    final boolean stateNotNeeded;
    boolean stopped;
    String stringName;
    boolean supportsEnterPipOnTaskSwitch;
    TaskRecord task;
    final String taskAffinity;
    ActivityManager.TaskDescription taskDescription;
    private int theme;
    long topResumedStateLossTime;
    UriPermissionOwner uriPermissions;
    boolean visible;
    boolean visibleIgnoringKeyguard;
    IVoiceInteractionSession voiceSession;
    private int windowFlags;

    private static String startingWindowStateToString(int state) {
        if (state == 0) {
            return "STARTING_WINDOW_NOT_SHOWN";
        }
        if (state == 1) {
            return "STARTING_WINDOW_SHOWN";
        }
        if (state == 2) {
            return "STARTING_WINDOW_REMOVED";
        }
        return "unknown state=" + state;
    }

    /* access modifiers changed from: package-private */
    public void dump(PrintWriter pw, String prefix) {
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
        pw.println(this.mUserId);
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
        pw.print("mActivityComponent=");
        pw.println(this.mActivityComponent.flattenToShortString());
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
        if (!getRequestedOverrideConfiguration().equals(Configuration.EMPTY)) {
            pw.println(prefix + "RequestedOverrideConfiguration=" + getRequestedOverrideConfiguration());
        }
        if (!getResolvedOverrideConfiguration().equals(getRequestedOverrideConfiguration())) {
            pw.println(prefix + "ResolvedOverrideConfiguration=" + getResolvedOverrideConfiguration());
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
        ActivityManager.TaskDescription taskDescription2 = this.taskDescription;
        if (!(taskDescription2 == null || (taskDescription2.getIconFilename() == null && this.taskDescription.getLabel() == null && this.taskDescription.getPrimaryColor() == 0))) {
            pw.print(prefix);
            pw.print("taskDescription:");
            pw.print(" label=\"");
            pw.print(this.taskDescription.getLabel());
            pw.print("\"");
            pw.print(" icon=");
            pw.print(this.taskDescription.getInMemoryIcon() != null ? this.taskDescription.getInMemoryIcon().getByteCount() + " bytes" : "null");
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
        HashSet<WeakReference<PendingIntentRecord>> hashSet = this.pendingResults;
        if (hashSet != null && hashSet.size() > 0) {
            pw.print(prefix);
            pw.println("Pending Results:");
            Iterator<WeakReference<PendingIntentRecord>> it = this.pendingResults.iterator();
            while (it.hasNext()) {
                WeakReference<PendingIntentRecord> wpir = it.next();
                PendingIntentRecord pir = wpir != null ? wpir.get() : null;
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
        ArrayList<ReferrerIntent> arrayList = this.newIntents;
        if (arrayList != null && arrayList.size() > 0) {
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
        AppTimeTracker appTimeTracker2 = this.appTimeTracker;
        if (appTimeTracker2 != null) {
            appTimeTracker2.dumpWithHeader(pw, prefix, false);
        }
        UriPermissionOwner uriPermissionOwner = this.uriPermissions;
        if (uriPermissionOwner != null) {
            uriPermissionOwner.dump(pw, prefix);
        }
        pw.print(prefix);
        pw.print("launchFailed=");
        pw.print(this.launchFailed);
        pw.print(" launchCount=");
        pw.print(this.launchCount);
        pw.print(" lastLaunchTime=");
        long j = this.lastLaunchTime;
        if (j == 0) {
            pw.print("0");
        } else {
            TimeUtils.formatDuration(j, now, pw);
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
        if (this.lastVisibleTime != 0 || this.nowVisible) {
            pw.print(prefix);
            pw.print(" nowVisible=");
            pw.print(this.nowVisible);
            pw.print(" lastVisibleTime=");
            long j2 = this.lastVisibleTime;
            if (j2 == 0) {
                pw.print("0");
            } else {
                TimeUtils.formatDuration(j2, now, pw);
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
        if (this.mServiceConnectionsHolder != null) {
            pw.print(prefix);
            pw.print("connections=");
            pw.println(this.mServiceConnectionsHolder);
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
            if (this.info.minAspectRatio != 0.0f) {
                pw.println(prefix + "minAspectRatio=" + this.info.minAspectRatio);
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
        if (attachedToProcess()) {
            try {
                if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH || ActivityTaskManagerDebugConfig.DEBUG_CONFIGURATION) {
                    Slog.v("ActivityTaskManager", "Reporting activity moved to display, activityRecord=" + this + ", displayId=" + displayId + ", config=" + config);
                }
                this.mAtmService.getLifecycleManager().scheduleTransaction(this.app.getThread(), (IBinder) this.appToken, (ClientTransactionItem) MoveToDisplayItem.obtain(displayId, config));
            } catch (RemoteException e) {
            }
        } else if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH || ActivityTaskManagerDebugConfig.DEBUG_CONFIGURATION) {
            Slog.w("ActivityTaskManager", "Can't report activity moved to display - client not running, activityRecord=" + this + ", displayId=" + displayId);
        }
    }

    private void scheduleConfigurationChanged(Configuration config) {
        if (attachedToProcess()) {
            try {
                if (ActivityTaskManagerDebugConfig.DEBUG_CONFIGURATION) {
                    Slog.v("ActivityTaskManager", "Sending new config to " + this + ", config: " + config);
                }
                this.mAtmService.getLifecycleManager().scheduleTransaction(this.app.getThread(), (IBinder) this.appToken, (ClientTransactionItem) ActivityConfigurationChangeItem.obtain(config));
            } catch (RemoteException e) {
            }
        } else if (ActivityTaskManagerDebugConfig.DEBUG_CONFIGURATION) {
            Slog.w("ActivityTaskManager", "Can't report activity configuration update - client not running, activityRecord=" + this);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean scheduleTopResumedActivityChanged(boolean onTop) {
        if (!attachedToProcess()) {
            if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                Slog.w("ActivityTaskManager", "Can't report activity position update - client not running, activityRecord=" + this);
            }
            return false;
        }
        try {
            if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                Slog.v("ActivityTaskManager", "Sending position change to " + this + ", onTop: " + onTop);
            }
            this.mAtmService.getLifecycleManager().scheduleTransaction(this.app.getThread(), (IBinder) this.appToken, (ClientTransactionItem) TopResumedActivityChangeItem.obtain(onTop));
            return true;
        } catch (RemoteException e) {
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    public void updateMultiWindowMode() {
        boolean inMultiWindowMode;
        TaskRecord taskRecord = this.task;
        if (taskRecord != null && taskRecord.getStack() != null && attachedToProcess() && !this.task.getStack().deferScheduleMultiWindowModeChanged() && (inMultiWindowMode = inMultiWindowMode()) != this.mLastReportedMultiWindowMode) {
            this.mLastReportedMultiWindowMode = inMultiWindowMode;
            scheduleMultiWindowModeChanged(getConfiguration());
        }
    }

    /* access modifiers changed from: package-private */
    public void scheduleMultiWindowModeChanged(Configuration overrideConfig) {
        try {
            this.mAtmService.getLifecycleManager().scheduleTransaction(this.app.getThread(), (IBinder) this.appToken, (ClientTransactionItem) MultiWindowModeChangeItem.obtain(this.mLastReportedMultiWindowMode, overrideConfig));
        } catch (Exception e) {
        }
    }

    /* access modifiers changed from: package-private */
    public void updatePictureInPictureMode(Rect targetStackBounds, boolean forceUpdate) {
        TaskRecord taskRecord = this.task;
        if (taskRecord != null && taskRecord.getStack() != null && attachedToProcess()) {
            boolean inPictureInPictureMode = inPinnedWindowingMode() && targetStackBounds != null;
            if (inPictureInPictureMode != this.mLastReportedPictureInPictureMode || forceUpdate) {
                this.mLastReportedPictureInPictureMode = inPictureInPictureMode;
                this.mLastReportedMultiWindowMode = inPictureInPictureMode;
                Configuration newConfig = new Configuration();
                if (targetStackBounds != null && !targetStackBounds.isEmpty()) {
                    newConfig.setTo(this.task.getRequestedOverrideConfiguration());
                    Rect outBounds = newConfig.windowConfiguration.getBounds();
                    this.task.adjustForMinimalTaskDimensions(outBounds, outBounds);
                    TaskRecord taskRecord2 = this.task;
                    taskRecord2.computeConfigResourceOverrides(newConfig, taskRecord2.getParent().getConfiguration());
                }
                schedulePictureInPictureModeChanged(newConfig);
                scheduleMultiWindowModeChanged(newConfig);
                this.mAtmService.mHwATMSEx.updatePictureInPictureMode(this, inPictureInPictureMode);
            }
        }
    }

    private void schedulePictureInPictureModeChanged(Configuration overrideConfig) {
        try {
            this.mAtmService.getLifecycleManager().scheduleTransaction(this.app.getThread(), (IBinder) this.appToken, (ClientTransactionItem) PipModeChangeItem.obtain(this.mLastReportedPictureInPictureMode, overrideConfig));
        } catch (Exception e) {
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.ConfigurationContainer
    public int getChildCount() {
        return 0;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.ConfigurationContainer
    public ConfigurationContainer getChildAt(int index) {
        return null;
    }

    /* access modifiers changed from: protected */
    @Override // com.android.server.wm.ConfigurationContainer
    public ConfigurationContainer getParent() {
        return getTaskRecord();
    }

    /* access modifiers changed from: package-private */
    public TaskRecord getTaskRecord() {
        return this.task;
    }

    /* access modifiers changed from: package-private */
    public void setTask(TaskRecord task2) {
        setTask(task2, false);
    }

    /* access modifiers changed from: package-private */
    public void setTask(TaskRecord task2, boolean reparenting) {
        if (task2 == null || task2 != getTaskRecord()) {
            ActivityStack oldStack = getActivityStack();
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
        AppWindowToken appWindowToken = this.mAppWindowToken;
        if (appWindowToken != null) {
            appWindowToken.setWillCloseOrEnterPip(willCloseOrEnterPip);
        }
    }

    /* access modifiers changed from: package-private */
    public static class Token extends IApplicationToken.Stub {
        private final String name;
        private final WeakReference<ActivityRecord> weakActivity;

        Token(ActivityRecord activity, Intent intent) {
            this.weakActivity = new WeakReference<>(activity);
            this.name = intent.getComponent().flattenToShortString();
        }

        /* access modifiers changed from: private */
        public static ActivityRecord tokenToActivityRecordLocked(Token token) {
            ActivityRecord r;
            if (token == null || (r = token.weakActivity.get()) == null || r.getActivityStack() == null) {
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

    public static ActivityRecord forToken(IBinder token) {
        return forTokenLocked(token);
    }

    static ActivityRecord forTokenLocked(IBinder token) {
        try {
            return Token.tokenToActivityRecordLocked((Token) token);
        } catch (ClassCastException e) {
            Slog.w("ActivityTaskManager", "Bad activity token: " + token, e);
            return null;
        }
    }

    static boolean isResolverActivity(String className) {
        return ResolverActivity.class.getName().equals(className);
    }

    /* access modifiers changed from: package-private */
    public boolean isResolverActivity() {
        return isResolverActivity(this.mActivityComponent.getClassName());
    }

    /* access modifiers changed from: package-private */
    public boolean isResolverOrChildActivity() {
        if ("com.huawei.android.internal.app".equals(this.packageName)) {
            return true;
        }
        if (!"android".equals(this.packageName)) {
            return false;
        }
        try {
            return ResolverActivity.class.isAssignableFrom(Object.class.getClassLoader().loadClass(this.mActivityComponent.getClassName()));
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v53, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r0v56, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r0v70, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r0v71, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r0v72, resolved type: boolean */
    /* JADX DEBUG: Multi-variable search result rejected for r0v73, resolved type: boolean */
    /* JADX WARN: Multi-variable type inference failed */
    public ActivityRecord(ActivityTaskManagerService _service, WindowProcessController _caller, int _launchedFromPid, int _launchedFromUid, String _launchedFromPackage, Intent _intent, String _resolvedType, ActivityInfo aInfo, Configuration _configuration, ActivityRecord _resultTo, String _resultWho, int _reqCode, boolean _componentSpecified, boolean _rootVoiceInteraction, ActivityStackSupervisor supervisor, ActivityOptions options, ActivityRecord sourceRecord) {
        boolean z;
        int i;
        int i2;
        this.mAtmService = _service;
        this.mRootActivityContainer = _service.mRootActivityContainer;
        this.appToken = new Token(this, _intent);
        this.info = aInfo;
        this.launchedFromPid = _launchedFromPid;
        this.launchedFromUid = _launchedFromUid;
        this.launchedFromPackage = _launchedFromPackage;
        this.mUserId = UserHandle.getUserId(aInfo.applicationInfo.uid);
        this.intent = _intent;
        this.shortComponentName = _intent.getComponent().flattenToShortString();
        this.resolvedType = _resolvedType;
        this.componentSpecified = _componentSpecified;
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
        this.mDrawn = false;
        this.idle = false;
        this.hasBeenLaunched = false;
        this.mStackSupervisor = supervisor;
        this.haveState = true;
        boolean splitMode = (_intent.getHwFlags() & 4) != 0 && (_intent.getHwFlags() & 8) == 0;
        if (aInfo.targetActivity == null || (aInfo.targetActivity.equals(_intent.getComponent().getClassName()) && (aInfo.launchMode == 0 || aInfo.launchMode == 1))) {
            this.mActivityComponent = _intent.getComponent();
        } else {
            this.mActivityComponent = new ComponentName(aInfo.packageName, aInfo.targetActivity);
        }
        if (splitMode) {
            if (_caller == null || (aInfo.taskAffinity != null && !aInfo.taskAffinity.equals(aInfo.processName))) {
                this.taskAffinity = aInfo.taskAffinity;
            } else {
                this.taskAffinity = _caller.mName;
            }
            if (aInfo.launchMode == 2) {
                aInfo.launchMode = 0;
            }
        } else {
            this.taskAffinity = aInfo.taskAffinity;
        }
        this.stateNotNeeded = (aInfo.flags & 16) != 0;
        this.appInfo = aInfo.applicationInfo;
        this.nonLocalizedLabel = aInfo.nonLocalizedLabel;
        this.labelRes = aInfo.labelRes;
        if (this.nonLocalizedLabel == null && this.labelRes == 0) {
            ApplicationInfo app2 = aInfo.applicationInfo;
            this.nonLocalizedLabel = app2.nonLocalizedLabel;
            this.labelRes = app2.labelRes;
        }
        this.icon = aInfo.getIconResource();
        this.logo = aInfo.getLogoResource();
        this.theme = aInfo.getThemeResource();
        this.realTheme = this.theme;
        if (this.realTheme == 0) {
            this.realTheme = aInfo.applicationInfo.targetSdkVersion < 11 ? 16973829 : 16973931;
        }
        if ((aInfo.flags & 512) != 0) {
            this.windowFlags |= 16777216;
        }
        if ((aInfo.flags & 1) == 0 || _caller == null || !(aInfo.applicationInfo.uid == 1000 || aInfo.applicationInfo.uid == _caller.mInfo.uid)) {
            this.processName = aInfo.processName;
        } else {
            this.processName = _caller.mName;
        }
        if ((aInfo.flags & 32) != 0) {
            this.intent.addFlags(8388608);
        }
        this.packageName = aInfo.applicationInfo.packageName;
        this.launchMode = aInfo.launchMode;
        AttributeCache.Entry ent = AttributeCache.instance().get(this.packageName, this.realTheme, R.styleable.Window, this.mUserId);
        if (ent != null) {
            i = 1;
            this.fullscreen = !ActivityInfo.isTranslucentOrFloating(ent.array);
            z = false;
            this.hasWallpaper = ent.array.getBoolean(14, false);
            this.noDisplay = ent.array.getBoolean(10, false);
            this.mIsFloating = ent.array.getBoolean(4, false);
            this.mIsTransluent = ent.array.getBoolean(5, false);
        } else {
            i = 1;
            z = false;
            this.hasWallpaper = false;
            this.noDisplay = false;
        }
        if (splitMode) {
            if (!this.fullscreen) {
                _intent.addHwFlags(8);
            } else {
                this.fullscreen = z;
            }
        }
        setActivityType(_componentSpecified, _launchedFromUid, _intent, options, sourceRecord);
        this.immersive = (aInfo.flags & 2048) != 0 ? i : false;
        this.requestedVrComponent = aInfo.requestedVrComponent == null ? null : ComponentName.unflattenFromString(aInfo.requestedVrComponent);
        this.mShowWhenLocked = (aInfo.flags & 8388608) != 0 ? i : false;
        this.mInheritShownWhenLocked = (aInfo.privateFlags & i) != 0 ? i : 0;
        this.mTurnScreenOn = (aInfo.flags & 16777216) != 0 ? i : 0;
        this.mRotationAnimationHint = aInfo.rotationAnimation;
        this.lockTaskLaunchMode = aInfo.lockTaskLaunchMode;
        if (this.appInfo.isPrivilegedApp() && ((i2 = this.lockTaskLaunchMode) == 2 || i2 == i)) {
            this.lockTaskLaunchMode = 0;
        }
        if (options != null) {
            this.pendingOptions = options;
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
            this.mHandoverLaunchDisplayId = options.getLaunchDisplayId();
        }
        if (HwFoldScreenState.isFoldScreenDevice()) {
            this.mLastReportedDisplayMode = this.mAtmService.mWindowManager.getFoldDisplayMode();
        }
    }

    /* access modifiers changed from: package-private */
    public void setProcess(WindowProcessController proc) {
        this.app = proc;
        TaskRecord taskRecord = this.task;
        if ((taskRecord != null ? taskRecord.getRootActivity() : null) == this) {
            this.task.setRootProcess(proc);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean hasProcess() {
        return this.app != null;
    }

    /* access modifiers changed from: package-private */
    public boolean attachedToProcess() {
        return hasProcess() && this.app.hasThread();
    }

    /* access modifiers changed from: package-private */
    public void createAppWindowToken(boolean naviBarHide) {
        ActivityRecord activityRecord;
        if (this.mAppWindowToken == null) {
            this.inHistory = true;
            this.task.updateOverrideConfigurationFromLaunchBounds();
            updateOverrideConfiguration();
            this.mAppWindowToken = this.mAtmService.mWindowManager.mRoot.getAppWindowToken(this.appToken.asBinder());
            if (this.mAppWindowToken != null) {
                Slog.w("ActivityTaskManager", "Attempted to add existing app token: " + this.appToken);
                activityRecord = this;
            } else {
                Task container = this.task.getTask();
                if (container != null) {
                    activityRecord = this;
                    activityRecord.mAppWindowToken = createAppWindow(this.mAtmService.mWindowManager, this.appToken, this.task.voiceSession != null, container.getDisplayContent(), ActivityTaskManagerService.getInputDispatchingTimeoutLocked(this) * 1000000, this.fullscreen, (this.info.flags & 1024) != 0, this.appInfo.targetSdkVersion, this.info.screenOrientation, this.mRotationAnimationHint, this.mLaunchTaskBehind, isAlwaysFocusable(), naviBarHide, this.info);
                    Slog.v("ActivityTaskManager", "addAppToken: " + activityRecord.mAppWindowToken + " task=" + container + " at 2147483647");
                    container.addChild(activityRecord.mAppWindowToken, Integer.MAX_VALUE);
                } else {
                    throw new IllegalArgumentException("createAppWindowToken: invalid task =" + this.task);
                }
            }
            activityRecord.task.addActivityToTop(activityRecord);
            activityRecord.mLastReportedMultiWindowMode = inMultiWindowMode();
            activityRecord.mLastReportedPictureInPictureMode = inPinnedWindowingMode();
            return;
        }
        throw new IllegalArgumentException("App Window Token=" + this.mAppWindowToken + " already created for r=" + this);
    }

    /* access modifiers changed from: package-private */
    public boolean addStartingWindow(String pkg, int theme2, CompatibilityInfo compatInfo, CharSequence nonLocalizedLabel2, int labelRes2, int icon2, int logo2, int windowFlags2, IBinder transferFrom, boolean newTask, boolean taskSwitch, boolean processRunning, boolean allowTaskSnapshot, boolean activityCreated, boolean fromRecents) {
        if (WindowManagerDebugConfig.DEBUG_STARTING_WINDOW) {
            Slog.v("ActivityTaskManager", "setAppStartingWindow: token=" + this.appToken + " pkg=" + pkg + " transferFrom=" + transferFrom + " newTask=" + newTask + " taskSwitch=" + taskSwitch + " processRunning=" + processRunning + " allowTaskSnapshot=" + allowTaskSnapshot);
        }
        AppWindowToken appWindowToken = this.mAppWindowToken;
        if (appWindowToken == null) {
            Slog.w("WindowManager", "Attempted to set icon of non-existing app token: " + this.appToken);
            return false;
        } else if (appWindowToken.getTask() != null) {
            return this.mAppWindowToken.addStartingWindow(pkg, theme2, compatInfo, nonLocalizedLabel2, labelRes2, icon2, logo2, windowFlags2, transferFrom, newTask, taskSwitch, processRunning, allowTaskSnapshot, activityCreated, fromRecents);
        } else {
            Slog.w("WindowManager", "Attempted to start a window to an app token not having attached to any task: " + this.appToken);
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public AppWindowToken createAppWindow(WindowManagerService service, IApplicationToken token, boolean voiceInteraction, DisplayContent dc, long inputDispatchingTimeoutNanos, boolean fullscreen2, boolean showForAllUsers, int targetSdk, int orientation, int rotationAnimationHint, boolean launchTaskBehind, boolean alwaysFocusable, boolean naviBarHide, ActivityInfo info2) {
        return new AppWindowToken(service, token, this.mActivityComponent, voiceInteraction, dc, inputDispatchingTimeoutNanos, fullscreen2, showForAllUsers, targetSdk, orientation, rotationAnimationHint, launchTaskBehind, alwaysFocusable, this, naviBarHide, info2);
    }

    /* access modifiers changed from: package-private */
    public void removeWindowContainer() {
        if (this.mAtmService.mWindowManager.mRoot != null) {
            DisplayContent dc = this.mAtmService.mWindowManager.mRoot.getDisplayContent(getDisplayId());
            if (dc == null) {
                Slog.w("ActivityTaskManager", "removeWindowContainer: Attempted to remove token: " + this.appToken + " from non-existing displayId=" + getDisplayId());
                return;
            }
            resumeKeyDispatchingLocked();
            dc.removeAppToken(this.appToken.asBinder());
        }
    }

    /* access modifiers changed from: package-private */
    public void reparent(TaskRecord newTask, int position, String reason) {
        if (this.mAppWindowToken == null) {
            Slog.w("ActivityTaskManager", "reparent: Attempted to reparent non-existing app token: " + this.appToken);
            return;
        }
        TaskRecord prevTask = this.task;
        if (prevTask == newTask) {
            throw new IllegalArgumentException(reason + ": task=" + newTask + " is already the parent of r=" + this);
        } else if (prevTask == null || newTask == null || prevTask.getStack() == newTask.getStack()) {
            this.mAppWindowToken.reparent(newTask.getTask(), position);
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
        return "android.intent.action.MAIN".equals(intent2.getAction()) && (intent2.hasCategory("android.intent.category.HOME") || intent2.hasCategory("android.intent.category.SECONDARY_HOME")) && intent2.getCategories().size() == 1 && intent2.getData() == null && intent2.getType() == null;
    }

    static boolean isMainIntent(Intent intent2) {
        if ("android.intent.action.MAIN".equals(intent2.getAction()) && intent2.hasCategory("android.intent.category.LAUNCHER") && intent2.getCategories().size() == 1 && intent2.getData() == null && intent2.getType() == null) {
            return true;
        }
        return false;
    }

    private boolean canLaunchHomeActivity(int uid, ActivityRecord sourceRecord) {
        if (uid == Process.myUid() || uid == 0) {
            return true;
        }
        RecentTasks recentTasks = this.mStackSupervisor.mService.getRecentTasks();
        if (recentTasks != null && recentTasks.isCallerRecents(uid)) {
            return true;
        }
        if (sourceRecord == null || !sourceRecord.isResolverActivity()) {
            return false;
        }
        return true;
    }

    private boolean canLaunchAssistActivity(String packageName2) {
        ComponentName assistComponent = this.mAtmService.mActiveVoiceInteractionServiceComponent;
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
        } else if (this.mActivityComponent.getClassName().contains(LEGACY_RECENTS_PACKAGE_NAME) || this.mAtmService.getRecentTasks().isRecentsComponent(this.mActivityComponent, this.appInfo.uid) || this.mActivityComponent.getClassName().contains(LEGACY_RECENTS_PACKAGE_NAME_LAUNCHER)) {
            activityType = 3;
        } else if (options != null && options.getLaunchActivityType() == 4 && canLaunchAssistActivity(this.launchedFromPackage)) {
            activityType = 4;
        }
        setActivityType(activityType);
    }

    /* access modifiers changed from: package-private */
    public void setTaskToAffiliateWith(TaskRecord taskToAffiliateWith) {
        int i = this.launchMode;
        if (i != 3 && i != 2) {
            this.task.setTaskToAffiliateWith(taskToAffiliateWith);
        }
    }

    public <T extends ActivityStack> T getActivityStack() {
        TaskRecord taskRecord = this.task;
        if (taskRecord != null) {
            return (T) taskRecord.getStack();
        }
        return null;
    }

    public int getStackId() {
        if (getActivityStack() != null) {
            return getActivityStack().mStackId;
        }
        return -1;
    }

    /* access modifiers changed from: package-private */
    public ActivityDisplay getDisplay() {
        ActivityStack stack = getActivityStack();
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

    public boolean isInStackLocked() {
        ActivityStack stack = getActivityStack();
        return (stack == null || stack.isInStackLocked(this) == null) ? false : true;
    }

    /* access modifiers changed from: package-private */
    public boolean isPersistable() {
        Intent intent2;
        return (this.info.persistableMode == 0 || this.info.persistableMode == 2) && ((intent2 = this.intent) == null || (intent2.getFlags() & 8388608) == 0);
    }

    /* access modifiers changed from: package-private */
    public boolean isFocusable() {
        return this.mRootActivityContainer.isFocusable(this, isAlwaysFocusable());
    }

    public boolean isResizeable() {
        return this.mAtmService.mHwATMSEx.isResizableApp(this.info.packageName, this.info.resizeMode) || this.info.supportsPictureInPicture();
    }

    /* access modifiers changed from: package-private */
    public boolean isResizeableOriginal() {
        return ActivityInfo.isResizeableMode(this.info.resizeMode) || this.info.supportsPictureInPicture();
    }

    /* access modifiers changed from: package-private */
    public boolean isNonResizableOrForcedResizable() {
        return (this.info.resizeMode == 2 || this.info.resizeMode == 1) ? false : true;
    }

    /* access modifiers changed from: package-private */
    public boolean supportsPictureInPicture() {
        return this.mAtmService.mSupportsPictureInPicture && isActivityTypeStandardOrUndefined() && this.info.supportsPictureInPicture();
    }

    @Override // com.android.server.wm.ConfigurationContainer
    public boolean supportsSplitScreenWindowingMode() {
        return super.supportsSplitScreenWindowingMode() && this.mAtmService.mSupportsSplitScreenMultiWindow && supportsResizeableMultiWindow() && this.info.packageName != null && !this.mAtmService.mHwATMSEx.isHwFreeFormOnlyApp(this.info.packageName);
    }

    /* access modifiers changed from: package-private */
    public boolean supportsFreeform() {
        return this.mAtmService.mSupportsFreeformWindowManagement && supportsResizeableMultiWindow();
    }

    private boolean supportsResizeableMultiWindow() {
        return this.mAtmService.mSupportsMultiWindow && !isActivityTypeHome() && (this.mAtmService.mHwATMSEx.isResizableApp(this.info.packageName, this.info.resizeMode) || this.mAtmService.mForceResizableActivities);
    }

    /* access modifiers changed from: package-private */
    public boolean canBeLaunchedOnDisplay(int displayId) {
        return this.mAtmService.mStackSupervisor.canPlaceEntityOnDisplay(displayId, this.launchedFromPid, this.launchedFromUid, this.info);
    }

    /* access modifiers changed from: package-private */
    public boolean checkEnterPictureInPictureState(String caller, boolean beforeStopping) {
        if (!supportsPictureInPicture() || !checkEnterPictureInPictureAppOpsState() || this.mAtmService.shouldDisableNonVrUiLocked()) {
            return false;
        }
        boolean isKeyguardLocked = this.mAtmService.isKeyguardLocked();
        boolean isCurrentAppLocked = this.mAtmService.getLockTaskModeState() != 0;
        ActivityDisplay display = getDisplay();
        boolean hasPinnedStack = display != null && display.hasPinnedStack();
        boolean isNotLockedOrOnKeyguard = !isKeyguardLocked && !isCurrentAppLocked;
        if (beforeStopping && hasPinnedStack) {
            return false;
        }
        int i = AnonymousClass1.$SwitchMap$com$android$server$wm$ActivityStack$ActivityState[this.mState.ordinal()];
        if (i != 1) {
            if (i == 2 || i == 3) {
                if (!isNotLockedOrOnKeyguard || hasPinnedStack || !this.supportsEnterPipOnTaskSwitch) {
                    return false;
                }
                return true;
            } else if (i == 4 && this.supportsEnterPipOnTaskSwitch && isNotLockedOrOnKeyguard && !hasPinnedStack) {
                return true;
            } else {
                return false;
            }
        } else if (isCurrentAppLocked) {
            return false;
        } else {
            if (this.supportsEnterPipOnTaskSwitch || !beforeStopping) {
                return true;
            }
            return false;
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.server.wm.ActivityRecord$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$server$wm$ActivityStack$ActivityState = new int[ActivityStack.ActivityState.values().length];

        static {
            try {
                $SwitchMap$com$android$server$wm$ActivityStack$ActivityState[ActivityStack.ActivityState.RESUMED.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$server$wm$ActivityStack$ActivityState[ActivityStack.ActivityState.PAUSING.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$server$wm$ActivityStack$ActivityState[ActivityStack.ActivityState.PAUSED.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
            try {
                $SwitchMap$com$android$server$wm$ActivityStack$ActivityState[ActivityStack.ActivityState.STOPPING.ordinal()] = 4;
            } catch (NoSuchFieldError e4) {
            }
        }
    }

    private boolean checkEnterPictureInPictureAppOpsState() {
        return this.mAtmService.getAppOpsService().checkOperation(67, this.appInfo.uid, this.packageName) == 0;
    }

    /* access modifiers changed from: package-private */
    public boolean isAlwaysFocusable() {
        return (this.info.flags & 262144) != 0;
    }

    /* access modifiers changed from: package-private */
    public boolean moveFocusableActivityToTop(String reason) {
        if (!isFocusable()) {
            if (ActivityTaskManagerDebugConfig.DEBUG_FOCUS) {
                Slog.d("ActivityTaskManager", "moveActivityStackToFront: unfocusable activity=" + this);
            }
            return false;
        }
        TaskRecord task2 = getTaskRecord();
        ActivityStack stack = getActivityStack();
        if (stack == null) {
            Slog.w("ActivityTaskManager", "moveActivityStackToFront: invalid task or stack: activity=" + this + " task=" + task2);
            return false;
        }
        if (this.mRootActivityContainer.getTopResumedActivity() == this) {
            if (ActivityTaskManagerDebugConfig.DEBUG_FOCUS) {
                Slog.d("ActivityTaskManager", "moveActivityStackToFront: already on top, activity=" + this);
            }
            if ((!HwPCUtils.isHiCarCastMode() || !HwPCUtils.isValidExtDisplayId(getDisplayId())) && !WindowConfiguration.isHwPCFreeFormWindowingMode(getWindowingMode())) {
                HwPCUtils.log("ActivityTaskManager", "moveActivityStackToFront dont return here");
                return false;
            }
        }
        if (ActivityTaskManagerDebugConfig.DEBUG_FOCUS) {
            Slog.d("ActivityTaskManager", "moveActivityStackToFront: activity=" + this);
        }
        stack.moveToFront(reason, task2);
        if (this.mRootActivityContainer.getTopResumedActivity() != this) {
            return true;
        }
        this.mAtmService.setResumedActivityUncheckLocked(this, reason);
        return true;
    }

    /* access modifiers changed from: package-private */
    public void makeFinishingLocked() {
        if (!this.finishing) {
            this.finishing = true;
            if (this.stopped) {
                clearOptionsLocked();
            }
            ActivityTaskManagerService activityTaskManagerService = this.mAtmService;
            if (activityTaskManagerService != null) {
                activityTaskManagerService.getTaskChangeNotificationController().notifyTaskStackChanged();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public UriPermissionOwner getUriPermissionsLocked() {
        if (this.uriPermissions == null) {
            this.uriPermissions = new UriPermissionOwner(this.mAtmService.mUgmInternal, this);
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
        ArrayList<ResultInfo> arrayList = this.results;
        if (arrayList != null) {
            for (int i = arrayList.size() - 1; i >= 0; i--) {
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
        ActivityStack stack = getActivityStack();
        return stack != null ? stack.shouldSleepActivities() : this.mAtmService.isSleepingLocked();
    }

    /* access modifiers changed from: package-private */
    public final void deliverNewIntentLocked(int callingUid, Intent intent2, String referrer) {
        this.mAtmService.mUgmInternal.grantUriPermissionFromIntent(callingUid, this.packageName, intent2, getUriPermissionsLocked(), this.mUserId);
        ReferrerIntent rintent = new ReferrerIntent(intent2, referrer);
        boolean unsent = true;
        boolean z = false;
        boolean isTopActivityWhileSleeping = isTopRunningActivity() && isSleeping();
        if ((this.mState == ActivityStack.ActivityState.RESUMED || this.mState == ActivityStack.ActivityState.PAUSED || isTopActivityWhileSleeping) && attachedToProcess()) {
            try {
                ArrayList<ReferrerIntent> ar = new ArrayList<>(1);
                ar.add(rintent);
                ClientLifecycleManager lifecycleManager = this.mAtmService.getLifecycleManager();
                IApplicationThread thread = this.app.getThread();
                IApplicationToken.Stub stub = this.appToken;
                if (this.mState == ActivityStack.ActivityState.RESUMED) {
                    z = true;
                }
                lifecycleManager.scheduleTransaction(thread, (IBinder) stub, (ClientTransactionItem) NewIntentItem.obtain(ar, z));
                unsent = false;
            } catch (RemoteException e) {
                Slog.w("ActivityTaskManager", "Exception thrown sending new intent to " + this, e);
            } catch (NullPointerException e2) {
                Slog.w("ActivityTaskManager", "Exception thrown sending new intent to " + this, e2);
            }
        }
        if (unsent) {
            addNewIntentLocked(rintent);
        }
    }

    /* access modifiers changed from: package-private */
    public void updateOptionsLocked(ActivityOptions options) {
        if (options != null) {
            if (ActivityTaskManagerDebugConfig.DEBUG_TRANSITION) {
                Slog.i("ActivityTaskManager", "Update options for " + this);
            }
            ActivityOptions activityOptions = this.pendingOptions;
            if (activityOptions != null) {
                activityOptions.abort();
            }
            this.pendingOptions = options;
        }
    }

    /* access modifiers changed from: package-private */
    public void applyOptionsLocked() {
        ActivityOptions activityOptions = this.pendingOptions;
        if (activityOptions != null && activityOptions.getAnimationType() != 5) {
            if (ActivityTaskManagerDebugConfig.DEBUG_TRANSITION) {
                Slog.i("ActivityTaskManager", "Applying options for " + this);
            }
            applyOptionsLocked(this.pendingOptions, this.intent);
            TaskRecord taskRecord = this.task;
            if (taskRecord == null) {
                clearOptionsLocked(false);
            } else {
                taskRecord.clearAllPendingOptions();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void applyOptionsLocked(ActivityOptions pendingOptions2, Intent intent2) {
        int animationType = pendingOptions2.getAnimationType();
        AppWindowToken appWindowToken = this.mAppWindowToken;
        if (appWindowToken == null) {
            Slog.e("WindowManager", "applyOptionsLocked: mAppWindowToken is null!");
            return;
        }
        DisplayContent displayContent = appWindowToken.getDisplayContent();
        boolean z = true;
        switch (animationType) {
            case 0:
                return;
            case 1:
                displayContent.mAppTransition.overridePendingAppTransition(pendingOptions2.getPackageName(), pendingOptions2.getCustomEnterResId(), pendingOptions2.getCustomExitResId(), pendingOptions2.getOnAnimationStartListener());
                return;
            case 2:
                displayContent.mAppTransition.overridePendingAppTransitionScaleUp(pendingOptions2.getStartX(), pendingOptions2.getStartY(), pendingOptions2.getWidth(), pendingOptions2.getHeight());
                if (intent2.getSourceBounds() == null) {
                    intent2.setSourceBounds(new Rect(pendingOptions2.getStartX(), pendingOptions2.getStartY(), pendingOptions2.getStartX() + pendingOptions2.getWidth(), pendingOptions2.getStartY() + pendingOptions2.getHeight()));
                    return;
                }
                return;
            case 3:
            case 4:
                boolean scaleUp = animationType == 3;
                GraphicBuffer buffer = pendingOptions2.getThumbnail();
                displayContent.mAppTransition.overridePendingAppTransitionThumb(buffer, pendingOptions2.getStartX(), pendingOptions2.getStartY(), pendingOptions2.getOnAnimationStartListener(), scaleUp);
                if (intent2.getSourceBounds() == null && buffer != null) {
                    intent2.setSourceBounds(new Rect(pendingOptions2.getStartX(), pendingOptions2.getStartY(), pendingOptions2.getStartX() + buffer.getWidth(), pendingOptions2.getStartY() + buffer.getHeight()));
                    return;
                }
                return;
            case 5:
            case 6:
            case CONFIG_RELAUNCH_DISABLE /* 7 */:
            case 10:
            default:
                Slog.e("WindowManager", "applyOptionsLocked: Unknown animationType=" + animationType);
                return;
            case 8:
            case 9:
                AppTransitionAnimationSpec[] specs = pendingOptions2.getAnimSpecs();
                IAppTransitionAnimationSpecsFuture specsFuture = pendingOptions2.getSpecsFuture();
                if (specsFuture != null) {
                    AppTransition appTransition = displayContent.mAppTransition;
                    IRemoteCallback onAnimationStartListener = pendingOptions2.getOnAnimationStartListener();
                    if (animationType != 8) {
                        z = false;
                    }
                    appTransition.overridePendingAppTransitionMultiThumbFuture(specsFuture, onAnimationStartListener, z);
                    return;
                } else if (animationType != 9 || specs == null) {
                    displayContent.mAppTransition.overridePendingAppTransitionAspectScaledThumb(pendingOptions2.getThumbnail(), pendingOptions2.getStartX(), pendingOptions2.getStartY(), pendingOptions2.getWidth(), pendingOptions2.getHeight(), pendingOptions2.getOnAnimationStartListener(), animationType == 8);
                    if (intent2.getSourceBounds() == null) {
                        intent2.setSourceBounds(new Rect(pendingOptions2.getStartX(), pendingOptions2.getStartY(), pendingOptions2.getStartX() + pendingOptions2.getWidth(), pendingOptions2.getStartY() + pendingOptions2.getHeight()));
                        return;
                    }
                    return;
                } else {
                    displayContent.mAppTransition.overridePendingAppTransitionMultiThumb(specs, pendingOptions2.getOnAnimationStartListener(), pendingOptions2.getAnimationFinishedListener(), false);
                    return;
                }
            case WindowManagerService.H.WINDOW_FREEZE_TIMEOUT /* 11 */:
                displayContent.mAppTransition.overridePendingAppTransitionClipReveal(pendingOptions2.getStartX(), pendingOptions2.getStartY(), pendingOptions2.getWidth(), pendingOptions2.getHeight());
                if (intent2.getSourceBounds() == null) {
                    intent2.setSourceBounds(new Rect(pendingOptions2.getStartX(), pendingOptions2.getStartY(), pendingOptions2.getStartX() + pendingOptions2.getWidth(), pendingOptions2.getStartY() + pendingOptions2.getHeight()));
                    return;
                }
                return;
            case 12:
                displayContent.mAppTransition.overridePendingAppTransitionStartCrossProfileApps();
                return;
            case 13:
                displayContent.mAppTransition.overridePendingAppTransitionRemote(pendingOptions2.getRemoteAnimationAdapter());
                return;
        }
    }

    /* access modifiers changed from: package-private */
    public ActivityOptions getOptionsForTargetActivityLocked() {
        ActivityOptions activityOptions = this.pendingOptions;
        if (activityOptions != null) {
            return activityOptions.forTargetActivity();
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void clearOptionsLocked() {
        clearOptionsLocked(true);
    }

    /* access modifiers changed from: package-private */
    public void clearOptionsLocked(boolean withAbort) {
        ActivityOptions activityOptions;
        if (withAbort && (activityOptions = this.pendingOptions) != null) {
            activityOptions.abort();
        }
        this.pendingOptions = null;
    }

    /* access modifiers changed from: package-private */
    public ActivityOptions takeOptionsLocked(boolean fromClient) {
        if (ActivityTaskManagerDebugConfig.DEBUG_TRANSITION) {
            Slog.i("ActivityTaskManager", "Taking options for " + this + " callers=" + Debug.getCallers(6));
        }
        ActivityOptions opts = this.pendingOptions;
        if (!fromClient || opts == null || opts.getRemoteAnimationAdapter() == null) {
            this.pendingOptions = null;
        }
        return opts;
    }

    /* access modifiers changed from: package-private */
    public void removeUriPermissionsLocked() {
        UriPermissionOwner uriPermissionOwner = this.uriPermissions;
        if (uriPermissionOwner != null) {
            uriPermissionOwner.removeUriPermissions();
            this.uriPermissions = null;
        }
    }

    /* access modifiers changed from: package-private */
    public void pauseKeyDispatchingLocked() {
        if (!this.keysPaused) {
            this.keysPaused = true;
            AppWindowToken appWindowToken = this.mAppWindowToken;
            if (appWindowToken != null && appWindowToken.getDisplayContent() != null) {
                this.mAppWindowToken.getDisplayContent().getInputMonitor().pauseDispatchingLw(this.mAppWindowToken);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void resumeKeyDispatchingLocked() {
        if (this.keysPaused) {
            this.keysPaused = false;
            AppWindowToken appWindowToken = this.mAppWindowToken;
            if (appWindowToken != null && appWindowToken.getDisplayContent() != null) {
                this.mAppWindowToken.getDisplayContent().getInputMonitor().resumeDispatchingLw(this.mAppWindowToken);
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
        AppWindowToken appWindowToken = this.mAppWindowToken;
        if (appWindowToken == null) {
            Slog.w("WindowManager", "Attempted to set visibility of non-existing app token: " + this.appToken);
            return;
        }
        appWindowToken.setVisibility(visible2, this.mDeferHidingClient);
        this.mStackSupervisor.getActivityMetricsLogger().notifyVisibilityChanged(this);
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
        if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
            Slog.v("ActivityTaskManager", "State movement: " + this + " from:" + getState() + " to:" + state + " reason:" + reason);
        }
        if (state != this.mState) {
            this.mState = state;
            TaskRecord parent = getTaskRecord();
            if (parent != null) {
                parent.onActivityStateChanged(this, state, reason);
            }
            if (state == ActivityStack.ActivityState.STOPPING && !isSleeping()) {
                AppWindowToken appWindowToken = this.mAppWindowToken;
                if (appWindowToken == null) {
                    Slog.w("WindowManager", "Attempted to notify stopping on non-existing app token: " + this.appToken);
                    return;
                }
                appWindowToken.detachChildren();
            }
            if (state == ActivityStack.ActivityState.RESUMED) {
                this.mAtmService.updateBatteryStats(this, true);
                this.mAtmService.updateActivityUsageStats(this, 1);
            } else if (state == ActivityStack.ActivityState.PAUSED) {
                this.mAtmService.updateBatteryStats(this, false);
                this.mAtmService.updateActivityUsageStats(this, 2);
            } else if (state == ActivityStack.ActivityState.STOPPED) {
                this.mAtmService.updateActivityUsageStats(this, 23);
            } else if (state == ActivityStack.ActivityState.DESTROYED) {
                this.mAtmService.updateActivityUsageStats(this, 24);
            }
        } else if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
            Slog.v("ActivityTaskManager", "State unchanged from:" + state);
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
        ActivityStack.ActivityState activityState = this.mState;
        return state1 == activityState || state2 == activityState;
    }

    /* access modifiers changed from: package-private */
    public boolean isState(ActivityStack.ActivityState state1, ActivityStack.ActivityState state2, ActivityStack.ActivityState state3) {
        ActivityStack.ActivityState activityState = this.mState;
        return state1 == activityState || state2 == activityState || state3 == activityState;
    }

    /* access modifiers changed from: package-private */
    public boolean isState(ActivityStack.ActivityState state1, ActivityStack.ActivityState state2, ActivityStack.ActivityState state3, ActivityStack.ActivityState state4) {
        ActivityStack.ActivityState activityState = this.mState;
        return state1 == activityState || state2 == activityState || state3 == activityState || state4 == activityState;
    }

    /* access modifiers changed from: package-private */
    public void notifyAppResumed(boolean wasStopped) {
        AppWindowToken appWindowToken = this.mAppWindowToken;
        if (appWindowToken == null) {
            Slog.w("WindowManager", "Attempted to notify resumed of non-existing app token: " + this.appToken);
            return;
        }
        appWindowToken.notifyAppResumed(wasStopped);
    }

    /* access modifiers changed from: package-private */
    public void notifyUnknownVisibilityLaunched() {
        AppWindowToken appWindowToken;
        if (!this.noDisplay && (appWindowToken = this.mAppWindowToken) != null) {
            appWindowToken.getDisplayContent().mUnknownAppVisibilityController.notifyLaunched(this.mAppWindowToken);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean shouldBeVisibleIgnoringKeyguard(boolean behindFullscreenActivity) {
        if (!okToShowLocked()) {
            return false;
        }
        if (!behindFullscreenActivity || this.mLaunchTaskBehind) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean shouldBeVisible(boolean behindFullscreenActivity) {
        this.visibleIgnoringKeyguard = shouldBeVisibleIgnoringKeyguard(behindFullscreenActivity);
        ActivityStack stack = getActivityStack();
        if (stack == null) {
            return false;
        }
        boolean isDisplaySleeping = getDisplay().isSleeping() && getDisplayId() != 0;
        if (this.mStackSupervisor.getKeyguardController().isOccludedShowWhenKeyguard(getDisplayId())) {
            isDisplaySleeping = getDisplay().isSleeping() && getDisplayId() == 0;
        }
        if (!stack.checkKeyguardVisibility(this, this.visibleIgnoringKeyguard, (this == stack.getTopActivity()) && (stack.isAttached() && stack.getDisplay().isTopNotPinnedStack(stack))) || isDisplaySleeping) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean shouldBeVisible() {
        ActivityStack stack = getActivityStack();
        if (stack == null) {
            return false;
        }
        return shouldBeVisible(!stack.shouldBeVisible(null));
    }

    /* access modifiers changed from: package-private */
    public void makeVisibleIfNeeded(ActivityRecord starting, boolean reportToClient) {
        if ((this.mState != ActivityStack.ActivityState.RESUMED && this != starting) || (isActivityTypeHome() && !this.visible && getDisplayId() == 0)) {
            String str = TAG_VISIBILITY;
            Slog.i(str, "Making visible and scheduling visibility: " + this);
            ActivityStack stack = getActivityStack();
            try {
                if (stack.mTranslucentActivityWaiting != null) {
                    updateOptionsLocked(this.returningOptions);
                    stack.mUndrawnActivitiesBelowTopTranslucent.add(this);
                }
                setVisible(true);
                this.sleeping = false;
                this.app.postPendingUiCleanMsg(true);
                if (reportToClient) {
                    makeClientVisible();
                } else {
                    this.mClientVisibilityDeferred = true;
                }
                this.mStackSupervisor.mStoppingActivities.remove(this);
                this.mStackSupervisor.mGoingToSleepActivities.remove(this);
            } catch (Exception e) {
                Slog.w("ActivityTaskManager", "Exception thrown making visible: " + this.intent.getComponent(), e);
            }
            handleAlreadyVisible();
        } else if (ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
            String str2 = TAG_VISIBILITY;
            Slog.d(str2, "Not making visible, r=" + this + " state=" + this.mState + " starting=" + starting);
        }
    }

    /* access modifiers changed from: package-private */
    public void makeClientVisible() {
        this.mClientVisibilityDeferred = false;
        try {
            this.mAtmService.getLifecycleManager().scheduleTransaction(this.app.getThread(), (IBinder) this.appToken, (ClientTransactionItem) WindowVisibilityItem.obtain(true));
            makeActiveIfNeeded(null);
            if (isState(ActivityStack.ActivityState.STOPPING, ActivityStack.ActivityState.STOPPED) && isFocusable()) {
                setState(ActivityStack.ActivityState.PAUSED, "makeClientVisible");
            }
        } catch (Exception e) {
            Slog.w("ActivityTaskManager", "Exception thrown sending visibility update: " + this.intent.getComponent(), e);
        }
    }

    /* access modifiers changed from: package-private */
    public boolean makeActiveIfNeeded(ActivityRecord activeActivity) {
        if (shouldResumeActivity(activeActivity)) {
            if (ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.v("TAG_VISIBILITY", "Resume visible activity, " + this);
            }
            return getActivityStack().resumeTopActivityUncheckedLocked(activeActivity, null);
        }
        if (shouldPauseActivity(activeActivity)) {
            if (ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.v("TAG_VISIBILITY", "Pause visible activity, " + this);
            }
            setState(ActivityStack.ActivityState.PAUSING, "makeVisibleIfNeeded");
            try {
                this.mAtmService.getLifecycleManager().scheduleTransaction(this.app.getThread(), (IBinder) this.appToken, (ActivityLifecycleItem) PauseActivityItem.obtain(this.finishing, false, this.configChangeFlags, false));
            } catch (Exception e) {
                Slog.w("ActivityTaskManager", "Exception thrown sending pause: " + this.intent.getComponent(), e);
            }
        }
        return false;
    }

    private boolean shouldPauseActivity(ActivityRecord activeActivity) {
        return shouldMakeActive(activeActivity) && !isFocusable() && !isState(ActivityStack.ActivityState.PAUSING, ActivityStack.ActivityState.PAUSED);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean shouldResumeActivity(ActivityRecord activeActivity) {
        return shouldMakeActive(activeActivity) && isFocusable() && !isState(ActivityStack.ActivityState.RESUMED) && getActivityStack().getVisibility(activeActivity) == 0;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean shouldMakeActive(ActivityRecord activeActivity) {
        if (!isState(ActivityStack.ActivityState.RESUMED, ActivityStack.ActivityState.PAUSED, ActivityStack.ActivityState.STOPPED, ActivityStack.ActivityState.STOPPING) || getActivityStack().mTranslucentActivityWaiting != null || this == activeActivity || !this.mStackSupervisor.readyToResume() || this.mLaunchTaskBehind) {
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
                this.app.getThread().scheduleOnNewActivityOptions(this.appToken, this.returningOptions.toBundle());
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
        if (ActivityTaskManagerDebugConfig.DEBUG_SAVED_STATE) {
            Slog.i("ActivityTaskManager", "Resumed activity; dropping state of: " + r);
        }
        if (r != null) {
            r.icicle = null;
            r.haveState = false;
            ActivityDisplay display = r.getDisplay();
            if (display != null) {
                display.handleActivitySizeCompatModeIfNeeded(r);
            }
            if (r.isActivityTypeHome()) {
                r.mAtmService.mHwATMSEx.stopInterceptionWhenBackHome();
                Slog.d("ActivityTaskManager", "Stop interception for recently back-home app when launcher resumed");
            }
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
            this.mStackSupervisor.updateHomeProcess(this.task.mActivities.get(0).app);
        }
        if (this.nowVisible) {
            this.mStackSupervisor.stopWaitingForActivityVisible(this);
        }
        this.mStackSupervisor.scheduleIdleTimeoutLocked(this);
        this.mStackSupervisor.reportResumedActivityLocked(this);
        resumeKeyDispatchingLocked();
        ActivityStack stack = getActivityStack();
        this.mStackSupervisor.mNoAnimActivities.clear();
        if (hasProcess()) {
            this.cpuTimeAtResume = this.app.getCpuTime();
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
        this.mStackSupervisor.mActivityLaunchTrack = "";
        this.mAtmService.mHwATMSEx.handleActivityResumedForPCCast(this);
    }

    /* access modifiers changed from: package-private */
    public final void activityStoppedLocked(Bundle newIcicle, PersistableBundle newPersistentState, CharSequence description) {
        ActivityStack stack = getActivityStack();
        boolean isStopping = this.mState == ActivityStack.ActivityState.STOPPING;
        if (isStopping || this.mState == ActivityStack.ActivityState.RESTARTING_PROCESS) {
            if (newPersistentState != null) {
                this.persistentState = newPersistentState;
                this.mAtmService.notifyTaskPersisterLocked(this.task, false);
            }
            if (ActivityTaskManagerDebugConfig.DEBUG_SAVED_STATE) {
                Slog.i("ActivityTaskManager", "Saving icicle of " + this + ": " + this.icicle);
            }
            if (newIcicle != null) {
                this.icicle = newIcicle;
                this.haveState = true;
                this.launchCount = 0;
                updateTaskDescription(description);
            }
            if (!this.stopped) {
                if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                    Slog.v("ActivityTaskManager", "Moving to STOPPED: " + this + " (stop complete)");
                }
                stack.mHandler.removeMessages(WindowManagerService.H.SET_FOCUSED_TASK, this);
                this.stopped = true;
                if (isStopping) {
                    setState(ActivityStack.ActivityState.STOPPED, "activityStoppedLocked");
                }
                AppWindowToken appWindowToken = this.mAppWindowToken;
                if (appWindowToken != null) {
                    appWindowToken.notifyAppStopped();
                }
                if (this.finishing) {
                    clearOptionsLocked();
                } else if (this.deferRelaunchUntilPaused) {
                    stack.destroyActivityLocked(this, true, "stop-config");
                    this.mRootActivityContainer.resumeFocusedStacksTopActivities();
                } else {
                    this.mRootActivityContainer.updatePreviousProcess(this);
                }
            }
        } else {
            Slog.i("ActivityTaskManager", "Activity reported stop, but no longer stopping: " + this);
            stack.mHandler.removeMessages(WindowManagerService.H.SET_FOCUSED_TASK, this);
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
        ActivityStack stack;
        if (this.launchTickTime == 0 || (stack = getActivityStack()) == null) {
            return false;
        }
        Message msg = stack.mHandler.obtainMessage(WindowManagerService.H.PC_FREEZE_TIMEOUT, this);
        stack.mHandler.removeMessages(WindowManagerService.H.PC_FREEZE_TIMEOUT);
        stack.mHandler.sendMessageDelayed(msg, 500);
        return true;
    }

    /* access modifiers changed from: package-private */
    public void finishLaunchTickingLocked() {
        this.launchTickTime = 0;
        ActivityStack stack = getActivityStack();
        if (stack != null) {
            stack.mHandler.removeMessages(WindowManagerService.H.PC_FREEZE_TIMEOUT);
        }
    }

    public boolean mayFreezeScreenLocked(WindowProcessController app2) {
        return hasProcess() && !app2.isCrashing() && !app2.isNotResponding();
    }

    public void startFreezingScreenLocked(WindowProcessController app2, int configChanges) {
        if (mayFreezeScreenLocked(app2)) {
            AppWindowToken appWindowToken = this.mAppWindowToken;
            if (appWindowToken == null) {
                Slog.w("WindowManager", "Attempted to freeze screen with non-existing app token: " + this.appToken);
            } else if ((-536870913 & configChanges) != 0 || !appWindowToken.okToDisplay()) {
                this.mAppWindowToken.startFreezingScreen();
            } else if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                Slog.v("WindowManager", "Skipping set freeze of " + this.appToken);
            }
        }
    }

    public void stopFreezingScreenLocked(boolean force) {
        if (force || this.frozenBeforeDestroy) {
            this.frozenBeforeDestroy = false;
            if (this.mAppWindowToken != null) {
                if (WindowManagerDebugConfig.DEBUG_ORIENTATION) {
                    Slog.v("WindowManager", "Clear freezing of " + this.appToken + ": hidden=" + this.mAppWindowToken.isHidden() + " freezing=" + this.mAppWindowToken.isFreezingScreen());
                }
                this.mAppWindowToken.stopFreezingScreen(true, force);
            }
        }
    }

    public void reportFullyDrawnLocked(boolean restoredFromBundle) {
        ActivityMetricsLogger.WindowingModeTransitionInfoSnapshot info2 = this.mStackSupervisor.getActivityMetricsLogger().logAppTransitionReportedDrawn(this, restoredFromBundle);
        if (info2 != null) {
            this.mStackSupervisor.reportActivityLaunchedLocked(false, this, (long) info2.windowsFullyDrawnDelayMs, info2.getLaunchState());
        }
        this.mAtmService.mAtmDAProxy.notifyAppEventToIaware(4, this.shortComponentName);
        if (this.app != null) {
            this.mAtmService.mAtmDAProxy.noteActivityDisplayed(this.shortComponentName, this.app.mUid, this.app.mPid, false);
        }
    }

    public boolean isFloating() {
        return this.mIsFloating;
    }

    public boolean isTransluent() {
        return this.mIsTransluent;
    }

    public void onStartingWindowDrawn(long timestamp) {
        synchronized (this.mAtmService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mStackSupervisor.getActivityMetricsLogger().notifyStartingWindowDrawn(getWindowingMode(), timestamp);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void onWindowsDrawn(boolean drawn, long timestamp) {
        synchronized (this.mAtmService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mDrawn = drawn;
                this.mStackSupervisor.getActivityMetricsLogger().setLaunchEndLog(this, timestamp);
                if (drawn) {
                    this.mAtmService.mAtmDAProxy.notifyAppEventToIaware(4, this.shortComponentName);
                    if (this.app != null) {
                        this.mAtmService.mAtmDAProxy.noteActivityDisplayed(this.shortComponentName, this.app.mUid, this.app.mPid, false);
                    }
                    ActivityMetricsLogger.WindowingModeTransitionInfoSnapshot info2 = this.mStackSupervisor.getActivityMetricsLogger().notifyWindowsDrawn(getWindowingMode(), timestamp);
                    int launchState = -1;
                    int windowsDrawnDelayMs = info2 != null ? info2.windowsDrawnDelayMs : -1;
                    if (info2 != null) {
                        launchState = info2.getLaunchState();
                    }
                    this.mStackSupervisor.reportActivityLaunchedLocked(false, this, (long) windowsDrawnDelayMs, launchState);
                    this.mStackSupervisor.stopWaitingForActivityVisible(this);
                    finishLaunchTickingLocked();
                    if (this.task != null) {
                        this.task.hasBeenVisible = true;
                    }
                    WindowManagerService.resetPriorityAfterLockedSection();
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void onWindowsVisible() {
        synchronized (this.mAtmService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                this.mStackSupervisor.stopWaitingForActivityVisible(this);
                if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH) {
                    Log.v("ActivityTaskManager", "windowsVisibleLocked(): " + this);
                }
                if (!this.nowVisible) {
                    this.nowVisible = true;
                    this.lastVisibleTime = SystemClock.uptimeMillis();
                    this.mAtmService.scheduleAppGcsLocked();
                    this.mAtmService.mHwATMSEx.reportAppWindowVisibleOrGone(this);
                }
                if (inHwSplitScreenSecondaryWindowingMode() && !getActivityStack().mTaskStack.isAdjustedForIme()) {
                    this.mAtmService.mHwATMSEx.setSplitBarVisibility(true);
                }
                this.mAtmService.mHwATMSEx.doReplaceSplitStack(null);
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void onWindowsGone() {
        synchronized (this.mAtmService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if ("com.android.incallui/.InCallActivity".equals(this.shortComponentName)) {
                    Flog.i(101, "Incall is gone");
                }
                Flog.i(101, "windowsGone(): " + this);
                boolean wasNowVisible = this.nowVisible;
                this.nowVisible = false;
                if (wasNowVisible) {
                    this.mAtmService.mHwATMSEx.reportAppWindowVisibleOrGone(this);
                }
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void onAnimationFinished() {
        if (this.mAtmService.mHwATMSEx.isVirtualDisplayId(getDisplayId(), "padCast") && !this.idle) {
            this.idle = true;
        }
        if (!this.mRootActivityContainer.allResumedActivitiesIdle() && !this.mStackSupervisor.isStoppingNoHistoryActivity()) {
            this.mStackSupervisor.processStoppingActivitiesLocked(null, false, true);
        } else if (this.mStackSupervisor.mStoppingActivities.contains(this) || isSplitMode()) {
            this.mStackSupervisor.scheduleIdleLocked();
        }
    }

    public boolean keyDispatchingTimedOut(String reason, int windowPid) {
        ActivityRecord anrActivity;
        WindowProcessController anrApp;
        boolean windowFromSameProcessAsActivity;
        synchronized (this.mAtmService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                anrActivity = getWaitingHistoryRecordLocked();
                anrApp = this.app;
                if (hasProcess() && this.app.getPid() != windowPid) {
                    if (windowPid != -1) {
                        windowFromSameProcessAsActivity = false;
                    }
                }
                windowFromSameProcessAsActivity = true;
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
        if (windowFromSameProcessAsActivity) {
            return this.mAtmService.mAmInternal.inputDispatchingTimedOut(anrApp.mOwner, anrActivity.shortComponentName, anrActivity.appInfo, this.shortComponentName, this.app, false, reason);
        }
        return this.mAtmService.mAmInternal.inputDispatchingTimedOut(windowPid, false, reason) < 0;
    }

    private ActivityRecord getWaitingHistoryRecordLocked() {
        if (this.stopped) {
            ActivityStack stack = this.mRootActivityContainer.getTopDisplayFocusedStack();
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
        if (!StorageManager.isUserKeyUnlocked(this.mUserId) && !this.info.applicationInfo.isEncryptionAware()) {
            return false;
        }
        if ((this.info.flags & 1024) != 0 || (this.mStackSupervisor.isCurrentProfileLocked(this.mUserId) && this.mAtmService.mAmInternal.isUserRunning(this.mUserId, 0))) {
            return true;
        }
        return false;
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
        if (!force && this.sleeping == _sleeping) {
            return;
        }
        if (!_sleeping && this.finishing && "com.huawei.camera/.controller.CollaborationActivity".equals(this.shortComponentName)) {
            Slog.i("ActivityTaskManager", "CollaborationActivity skip sleep");
        } else if (attachedToProcess()) {
            try {
                this.app.getThread().scheduleSleeping(this.appToken, _sleeping);
                if (_sleeping && !this.mStackSupervisor.mGoingToSleepActivities.contains(this)) {
                    this.mStackSupervisor.mGoingToSleepActivities.add(this);
                }
                this.sleeping = _sleeping;
            } catch (RemoteException e) {
                Slog.w("ActivityTaskManager", "Exception thrown when sleeping: " + this.intent.getComponent(), e);
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
            return r.getActivityStack().isInStackLocked(r);
        }
        return null;
    }

    public static ActivityStack getStackLocked(IBinder token) {
        ActivityRecord r = isInStackLocked(token);
        if (r != null) {
            return r.getActivityStack();
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public int getDisplayId() {
        ActivityStack stack = getActivityStack();
        if (stack == null) {
            return -1;
        }
        return stack.mDisplayId;
    }

    /* access modifiers changed from: package-private */
    public final boolean isDestroyable() {
        ActivityStack stack;
        if (this.finishing || !hasProcess() || (stack = getActivityStack()) == null || this == stack.getResumedActivity() || this == stack.mPausingActivity || !this.haveState || !this.stopped || this.visible) {
            return false;
        }
        return true;
    }

    private static String createImageFilename(long createTime2, int taskId) {
        return String.valueOf(taskId) + ACTIVITY_ICON_SUFFIX + createTime2 + ".png";
    }

    /* access modifiers changed from: package-private */
    public void setTaskDescription(ActivityManager.TaskDescription _taskDescription) {
        Bitmap icon2;
        if (_taskDescription.getIconFilename() == null && (icon2 = _taskDescription.getIcon()) != null) {
            String iconFilePath = new File(TaskPersister.getUserImagesDir(this.task.userId), createImageFilename(this.createTime, this.task.taskId)).getAbsolutePath();
            this.mAtmService.getRecentTasks().saveImage(icon2, iconFilePath);
            _taskDescription.setIconFilename(iconFilePath);
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
        if (this.mAppWindowToken != null) {
            TaskRecord taskRecord = this.task;
            if ((taskRecord == null || !taskRecord.inHwPCFreeformWindowingMode()) && !this.mTaskOverlay) {
                ActivityOptions activityOptions = this.pendingOptions;
                if (activityOptions == null || activityOptions.getAnimationType() != 5) {
                    if (addStartingWindow(this.packageName, this.theme, this.mAtmService.compatibilityInfoForPackageLocked(this.info.applicationInfo), this.nonLocalizedLabel, this.labelRes, this.icon, this.logo, this.windowFlags, prev != null ? prev.appToken : null, newTask, taskSwitch, isProcessRunning(), allowTaskSnapshot(), this.mState.ordinal() >= ActivityStack.ActivityState.RESUMED.ordinal() && this.mState.ordinal() <= ActivityStack.ActivityState.STOPPED.ordinal(), fromRecents)) {
                        this.mStartingWindowState = 1;
                        this.mAtmService.mHwATMSEx.dispatchActivityLifeState(this, "showStartingWindow");
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void removeOrphanedStartingWindow(boolean behindFullscreenActivity) {
        if (this.mStartingWindowState == 1 && behindFullscreenActivity) {
            if (ActivityTaskManagerDebugConfig.DEBUG_VISIBILITY) {
                String str = TAG_VISIBILITY;
                Slog.w(str, "Found orphaned starting window " + this);
            }
            this.mStartingWindowState = 2;
            this.mAppWindowToken.removeStartingWindow();
        }
    }

    /* access modifiers changed from: package-private */
    public void setRequestedOrientation(int requestedOrientation) {
        setOrientation(requestedOrientation, mayFreezeScreenLocked(this.app));
        this.mAtmService.getTaskChangeNotificationController().notifyActivityRequestedOrientationChanged(this.task.taskId, requestedOrientation);
    }

    private void setOrientation(int requestedOrientation, boolean freezeScreenIfNeeded) {
        IApplicationToken.Stub stub;
        if (this.mAppWindowToken == null) {
            Slog.w("WindowManager", "Attempted to set orientation of non-existing app token: " + this.appToken);
            return;
        }
        this.mAppWindowToken.setOrientation(requestedOrientation, (!freezeScreenIfNeeded || (stub = this.appToken) == null) ? null : stub.asBinder(), this);
        if (!getMergedOverrideConfiguration().equals(this.mLastReportedConfiguration.getMergedConfiguration())) {
            ensureActivityConfiguration(0, false);
        }
    }

    /* access modifiers changed from: package-private */
    public int getOrientation() {
        AppWindowToken appWindowToken = this.mAppWindowToken;
        if (appWindowToken == null) {
            return this.info.screenOrientation;
        }
        return appWindowToken.getOrientationIgnoreVisibility();
    }

    /* access modifiers changed from: package-private */
    public void setDisablePreviewScreenshots(boolean disable) {
        AppWindowToken appWindowToken = this.mAppWindowToken;
        if (appWindowToken == null) {
            Slog.w("WindowManager", "Attempted to set disable screenshots of non-existing app token: " + this.appToken);
            return;
        }
        appWindowToken.setDisablePreviewScreenshots(disable);
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

    /* access modifiers changed from: package-private */
    public int getRequestedConfigurationOrientation() {
        int screenOrientation = getOrientation();
        if (screenOrientation == 5) {
            ActivityDisplay display = getDisplay();
            if (display == null || display.mDisplayContent == null) {
                return 0;
            }
            return display.mDisplayContent.getNaturalOrientation();
        } else if (screenOrientation == 14) {
            return getConfiguration().orientation;
        } else {
            if (ActivityInfo.isFixedOrientationLandscape(screenOrientation)) {
                return 2;
            }
            if (ActivityInfo.isFixedOrientationPortrait(screenOrientation)) {
                return 1;
            }
            return 0;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean inSizeCompatMode() {
        if (!shouldUseSizeCompatMode()) {
            return false;
        }
        Configuration resolvedConfig = getResolvedOverrideConfiguration();
        Rect resolvedAppBounds = resolvedConfig.windowConfiguration.getAppBounds();
        if (resolvedAppBounds == null) {
            return false;
        }
        Configuration parentConfig = getParent().getConfiguration();
        if (parentConfig.densityDpi != resolvedConfig.densityDpi) {
            return true;
        }
        Rect parentAppBounds = parentConfig.windowConfiguration.getAppBounds();
        int appWidth = resolvedAppBounds.width();
        int appHeight = resolvedAppBounds.height();
        int parentAppWidth = parentAppBounds.width();
        int parentAppHeight = parentAppBounds.height();
        if (parentAppWidth == appWidth && parentAppHeight == appHeight) {
            return false;
        }
        if ((parentAppWidth > appWidth && parentAppHeight > appHeight) || parentAppWidth < appWidth || parentAppHeight < appHeight) {
            return true;
        }
        if (this.info.maxAspectRatio > 0.0f && (((float) Math.max(appWidth, appHeight)) + 0.5f) / ((float) Math.min(appWidth, appHeight)) >= this.info.maxAspectRatio) {
            return false;
        }
        if (this.info.minAspectRatio <= 0.0f || (((float) Math.max(parentAppWidth, parentAppHeight)) + 0.5f) / ((float) Math.min(parentAppWidth, parentAppHeight)) > this.info.minAspectRatio) {
            return true;
        }
        return false;
    }

    /* access modifiers changed from: package-private */
    public boolean shouldUseSizeCompatMode() {
        return !isResizeableOriginal() && (this.info.isFixedOrientation() || this.info.hasFixedAspectRatio()) && isActivityTypeStandard() && !this.mAtmService.mForceResizableActivities && !inCoordinationWindowingMode() && !inHwMagicWindowingMode() && !inHwMultiStackWindowingMode() && !this.mAtmService.mHwATMSEx.isNerverUseSizeCompateMode(this.packageName);
    }

    /* access modifiers changed from: protected */
    public void updateOverrideConfiguration() {
        ActivityRecord topActivity;
        Configuration overrideConfig = this.mTmpConfig;
        boolean isSkipUseCompatMode = isSkipUseCompatMode();
        if (isSkipUseCompatMode || !shouldUseSizeCompatMode() || ((HwPCUtils.isPcCastModeInServer() && HwPCUtils.isValidExtDisplayId(getDisplayId())) || (this.mAtmService.mVrMananger.isVRDeviceConnected() && this.mAtmService.mVrMananger.isValidVRDisplayId(getDisplayId())))) {
            computeBounds(this.mTmpBounds, getParent().getWindowConfiguration().getAppBounds());
            boolean isInMWPortraitWhiteList = false;
            TaskRecord taskRecord = this.task;
            if (!(taskRecord == null || !taskRecord.inMultiWindowMode() || (topActivity = this.task.getTopActivity()) == null)) {
                isInMWPortraitWhiteList = this.mAtmService.getPackageManagerInternalLocked().isInMWPortraitWhiteList(topActivity.packageName);
            }
            if (isSkipUseCompatMode || !this.mTmpBounds.equals(getRequestedOverrideBounds()) || isInMWPortraitWhiteList) {
                overrideConfig.unset();
                if (inHwMagicWindowingMode()) {
                    this.mCompatDisplayInsets = null;
                    overrideConfig.windowConfiguration.setBounds(getRequestedOverrideBounds());
                } else {
                    overrideConfig.windowConfiguration.setBounds(this.mTmpBounds);
                }
                if (!this.mTmpBounds.isEmpty()) {
                    overrideConfig.nonFullScreen = 1;
                } else {
                    overrideConfig.nonFullScreen = 0;
                }
            } else {
                return;
            }
        } else if (this.mCompatDisplayInsets == null) {
            Configuration parentConfig = getParent().getConfiguration();
            if (hasProcess() || isConfigurationCompatible(parentConfig)) {
                overrideConfig.unset();
                overrideConfig.colorMode = parentConfig.colorMode;
                overrideConfig.densityDpi = parentConfig.densityDpi;
                overrideConfig.screenLayout = parentConfig.screenLayout & 63;
                overrideConfig.smallestScreenWidthDp = parentConfig.smallestScreenWidthDp;
                ActivityDisplay display = getDisplay();
                if (!(display == null || display.mDisplayContent == null)) {
                    this.mCompatDisplayInsets = new CompatDisplayInsets(display.mDisplayContent);
                }
            } else {
                return;
            }
        } else {
            return;
        }
        onRequestedOverrideConfigurationChanged(overrideConfig);
    }

    private boolean isSkipUseCompatMode() {
        boolean z = true;
        if (this.mIsChangeToDefaultDisplay || this.mAtmService.mHwATMSEx.isVirtualDisplayId(getDisplayId(), "padCast") || inHwMultiStackWindowingMode() || (0.0f < this.info.maxAspectRatio && this.info.maxAspectRatio < this.mAtmService.mWindowManager.getDeviceMaxRatio())) {
            return true;
        }
        if (!HwFoldScreenState.isFoldScreenDevice()) {
            return false;
        }
        if (this.info.minAspectRatio > 0.0f || this.mAtmService.mHwATMSEx.isNerverUseSizeCompateMode(this.packageName) || this.mDisplayModeChanged) {
            return true;
        }
        int currentDisplayMode = this.mAtmService.mWindowManager.getFoldDisplayMode();
        if (this.mOriginalDisplayMode == 0) {
            this.mOriginalDisplayMode = currentDisplayMode;
        }
        if (this.mOriginalDisplayMode == currentDisplayMode) {
            z = false;
        }
        this.mDisplayModeChanged = z;
        if (this.mDisplayModeChanged) {
            this.mCompatDisplayInsets = null;
        }
        return this.mDisplayModeChanged;
    }

    public boolean isSystemUiFullScreenWindowShow() {
        float realMaxAspectRatio = this.mAtmService.mHwATMSEx.getAspectRatioWithUserSet(this.packageName, "maxAspectRatio", this.info);
        return this.mIsFloating || (0.0f < realMaxAspectRatio && realMaxAspectRatio < this.mAtmService.mWindowManager.getDeviceMaxRatio());
    }

    /* access modifiers changed from: package-private */
    @Override // com.android.server.wm.ConfigurationContainer
    public void resolveOverrideConfiguration(Configuration newParentConfiguration) {
        TaskRecord taskRecord;
        if (this.mCompatDisplayInsets == null || inCoordinationWindowingMode() || isSkipUseCompatMode()) {
            super.resolveOverrideConfiguration(newParentConfiguration);
            if (!matchParentBounds() && (taskRecord = this.task) != null) {
                taskRecord.computeConfigResourceOverrides(getResolvedOverrideConfiguration(), newParentConfiguration);
                if (HwActivityTaskManager.IS_HW_MULTIWINDOW_APPCOMPACT_SUPPORTED && !inMultiWindowMode()) {
                    int smallestScreenWidthDp = Math.min(getResolvedOverrideConfiguration().screenWidthDp, getResolvedOverrideConfiguration().screenHeightDp);
                    if (smallestScreenWidthDp != 0) {
                        getResolvedOverrideConfiguration().smallestScreenWidthDp = smallestScreenWidthDp;
                    }
                    if (this.mAtmService.mHwATMSEx.isVirtualDisplayId(getDisplayId(), "padCast")) {
                        if (getResolvedOverrideConfiguration().orientation == 1) {
                            getResolvedOverrideConfiguration().windowConfiguration.setRotation(0);
                        } else {
                            getResolvedOverrideConfiguration().windowConfiguration.setRotation(1);
                        }
                    }
                }
            }
        } else {
            resolveSizeCompatModeConfiguration(newParentConfiguration);
        }
        int i = this.mConfigurationSeq + 1;
        this.mConfigurationSeq = i;
        this.mConfigurationSeq = Math.max(i, 1);
        getResolvedOverrideConfiguration().seq = this.mConfigurationSeq;
    }

    /* JADX INFO: Multiple debug info for r8v2 int: [D('baseOrientation' int), D('containingOffsetX' int)] */
    private void resolveSizeCompatModeConfiguration(Configuration newParentConfiguration) {
        if (!HwPCUtils.isPcCastModeInServer() || !HwPCUtils.isValidExtDisplayId(getDisplayId())) {
            Configuration resolvedConfig = getResolvedOverrideConfiguration();
            Rect resolvedBounds = resolvedConfig.windowConfiguration.getBounds();
            int parentRotation = newParentConfiguration.windowConfiguration.getRotation();
            int parentOrientation = newParentConfiguration.orientation;
            int orientation = getConfiguration().orientation;
            if (orientation != parentOrientation && isConfigurationCompatible(newParentConfiguration)) {
                orientation = parentOrientation;
            } else if (resolvedBounds.isEmpty() || !isSkipUseCompatMode() || getWindowConfiguration().getRotation() != parentRotation) {
                int requestedOrientation = getRequestedConfigurationOrientation();
                if (requestedOrientation != 0) {
                    orientation = requestedOrientation;
                }
            } else {
                return;
            }
            super.resolveOverrideConfiguration(newParentConfiguration);
            boolean useParentOverrideBounds = false;
            Rect displayBounds = this.mTmpBounds;
            Rect containingAppBounds = new Rect();
            if (this.task.handlesOrientationChangeFromDescendant()) {
                this.mCompatDisplayInsets.getDisplayBoundsByOrientation(displayBounds, orientation);
            } else {
                int baseOrientation = this.task.getParent().getConfiguration().orientation;
                this.mCompatDisplayInsets.getDisplayBoundsByOrientation(displayBounds, baseOrientation);
                this.task.computeFullscreenBounds(containingAppBounds, this, displayBounds, baseOrientation);
                useParentOverrideBounds = !containingAppBounds.isEmpty();
            }
            int containingOffsetX = containingAppBounds.left;
            int containingOffsetY = containingAppBounds.top;
            if (!useParentOverrideBounds) {
                containingAppBounds.set(displayBounds);
            }
            if (parentRotation != -1) {
                TaskRecord.intersectWithInsetsIfFits(containingAppBounds, displayBounds, this.mCompatDisplayInsets.mNonDecorInsets[parentRotation]);
            }
            computeBounds(resolvedBounds, containingAppBounds);
            if (resolvedBounds.isEmpty()) {
                resolvedBounds.set(useParentOverrideBounds ? containingAppBounds : displayBounds);
            } else {
                resolvedBounds.left += containingOffsetX;
                resolvedBounds.top += containingOffsetY;
            }
            this.task.computeConfigResourceOverrides(resolvedConfig, newParentConfiguration, this.mCompatDisplayInsets);
            if (!HwFoldScreenState.isFoldScreenDevice()) {
                Rect resolvedAppBounds = resolvedConfig.windowConfiguration.getAppBounds();
                if (resolvedBounds.width() < newParentConfiguration.windowConfiguration.getAppBounds().width()) {
                    resolvedBounds.right -= resolvedAppBounds.left;
                }
            }
            if (resolvedConfig.screenWidthDp == resolvedConfig.screenHeightDp) {
                resolvedConfig.orientation = newParentConfiguration.orientation;
                return;
            }
            return;
        }
        Slog.i("ActivityTaskManager", "Won't adjust the bounds to match the current orientation in PC mode");
    }

    @Override // com.android.server.wm.ConfigurationContainer
    public void onConfigurationChanged(Configuration newParentConfig) {
        super.onConfigurationChanged(newParentConfig);
        if (getMergedOverrideConfiguration().seq != getResolvedOverrideConfiguration().seq) {
            onMergedOverrideConfigurationChanged();
        }
        AppWindowToken appWindowToken = this.mAppWindowToken;
        if (appWindowToken != null) {
            Configuration appWindowTokenRequestedOverrideConfig = appWindowToken.getRequestedOverrideConfiguration();
            if (appWindowTokenRequestedOverrideConfig.seq != getResolvedOverrideConfiguration().seq) {
                appWindowTokenRequestedOverrideConfig.seq = getResolvedOverrideConfiguration().seq;
                this.mAppWindowToken.onMergedOverrideConfigurationChanged();
            }
            ActivityDisplay display = getDisplay();
            if (display != null) {
                if (this.visible) {
                    display.handleActivitySizeCompatModeIfNeeded(this);
                } else if (shouldUseSizeCompatMode()) {
                    int displayChanges = display.getLastOverrideConfigurationChanges();
                    if (!(hasResizeChange(displayChanges) && (displayChanges & 536872064) != 536872064) && (displayChanges & 4096) == 0) {
                        return;
                    }
                    if (!HwFoldScreenState.isFoldScreenDevice()) {
                        restartProcessIfVisible();
                    } else {
                        this.mCompatDisplayInsets = null;
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isConfigurationCompatible(Configuration config) {
        int orientation = getOrientation();
        if (!ActivityInfo.isFixedOrientationPortrait(orientation) || config.orientation == 1) {
            return !ActivityInfo.isFixedOrientationLandscape(orientation) || config.orientation == 2;
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void computeBounds(Rect outBounds, Rect containingAppBounds) {
        TaskRecord taskRecord;
        boolean adjustWidth;
        outBounds.setEmpty();
        this.maxAspectRatio = this.mAtmService.mHwATMSEx.getAspectRatioWithUserSet(this.packageName, "maxAspectRatio", this.info);
        ActivityStack stack = getActivityStack();
        this.minAspectRatio = this.info.minAspectRatio;
        int displayMode = 0;
        if (HwFoldScreenState.isFoldScreenDevice() && (displayMode = this.mAtmService.mWindowManager.getFoldDisplayMode()) == 1) {
            float userSetMinAspect = this.mAtmService.getPackageManagerInternalLocked().getUserAspectRatio(this.packageName, "minAspectRatio");
            if (userSetMinAspect != 0.0f) {
                this.minAspectRatio = userSetMinAspect;
            }
            if (((double) Math.abs(this.minAspectRatio - HwFoldScreenState.getScreenFoldFullRatio())) < 1.0E-8d) {
                this.minAspectRatio = 0.0f;
            }
        }
        AppWindowToken appWindowToken = this.mAppWindowToken;
        if (appWindowToken != null) {
            appWindowToken.mIsNeedBackgroundSurface = false;
        }
        if (this.mIsChangeToDefaultDisplay) {
            this.mIsChangeToDefaultDisplay = false;
        } else if (this.mAtmService.mHwATMSEx.computeBounds(this, outBounds) || (taskRecord = this.task) == null || stack == null || taskRecord.inMultiWindowMode()) {
        } else {
            if (!(this.maxAspectRatio == 0.0f && this.minAspectRatio == 0.0f) && !isInVrUiMode(getConfiguration())) {
                int containingAppWidth = containingAppBounds.width();
                int containingAppHeight = containingAppBounds.height();
                float containingRatio = ((float) Math.max(containingAppWidth, containingAppHeight)) / ((float) Math.min(containingAppWidth, containingAppHeight));
                if (HwFoldScreenState.isFoldScreenDevice()) {
                    if (displayMode == 2) {
                        float deviceMaxRatio = this.mAtmService.mWindowManager.getDeviceMaxRatio();
                        containingRatio = deviceMaxRatio < containingRatio ? deviceMaxRatio : containingRatio;
                    }
                    if (displayMode == 1) {
                        float f = this.maxAspectRatio;
                        if (containingRatio <= f || f == 0.0f) {
                            HwFoldScreenState.getAppFolderDisplayRect(displayMode, this.minAspectRatio, outBounds, containingAppBounds, this.mAtmService.mWindowManager.getDefaultDisplayContentLocked().getRotation());
                            return;
                        }
                    }
                }
                int activityWidth = containingAppWidth;
                int activityHeight = containingAppHeight;
                float f2 = this.maxAspectRatio;
                if (containingRatio <= f2 || f2 == 0.0f) {
                    if (containingRatio < this.minAspectRatio) {
                        int requestedConfigurationOrientation = getRequestedConfigurationOrientation();
                        if (requestedConfigurationOrientation == 1) {
                            adjustWidth = true;
                        } else if (requestedConfigurationOrientation == 2) {
                            adjustWidth = false;
                        } else if (containingAppWidth < containingAppHeight) {
                            adjustWidth = true;
                        } else {
                            adjustWidth = false;
                        }
                        if (adjustWidth) {
                            activityWidth = (int) ((((float) activityHeight) / this.minAspectRatio) + 0.5f);
                        } else {
                            activityHeight = (int) ((((float) activityWidth) / this.minAspectRatio) + 0.5f);
                        }
                    }
                } else if (containingAppWidth < containingAppHeight) {
                    activityHeight = (int) ((((float) activityWidth) * f2) + 0.5f);
                } else {
                    activityWidth = (int) ((((float) activityHeight) * f2) + 0.5f);
                }
                if (containingAppWidth > activityWidth || containingAppHeight > activityHeight) {
                    outBounds.set(0, 0, containingAppBounds.left + activityWidth, containingAppBounds.top + activityHeight);
                    this.mAtmService.mWindowManager.getAppDisplayRect(this.maxAspectRatio, outBounds, containingAppBounds.left);
                    return;
                }
                Rect rect = getRequestedOverrideBounds();
                if (!HwFoldScreenState.isFoldScreenDevice()) {
                    outBounds.set(rect);
                } else if (rect.isEmpty() || rect.equals(containingAppBounds)) {
                    outBounds.set(rect);
                } else {
                    outBounds.set(containingAppBounds);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean shouldUpdateConfigForDisplayChanged() {
        return this.mLastReportedDisplayId != getDisplayId();
    }

    /* access modifiers changed from: package-private */
    public boolean ensureActivityConfiguration(int globalChanges, boolean preserveWindow) {
        return ensureActivityConfiguration(globalChanges, preserveWindow, false);
    }

    /* access modifiers changed from: package-private */
    public boolean ensureActivityConfiguration(int globalChanges, boolean preserveWindow, boolean ignoreStopState) {
        int i;
        ActivityStack stack = getActivityStack();
        if (stack == null) {
            Slog.e(TAG_CONFIGURATION, "Skipping config check stack is null : " + this);
            return true;
        } else if (HwFoldScreenState.isFoldScreenDevice() && CoordinationModeUtils.getInstance(this.mAtmService.mContext).isExitingCoordinationMode() && "com.huawei.camera/.controller.CollaborationActivity".equals(this.shortComponentName)) {
            return true;
        } else {
            if (stack.mConfigWillChange) {
                if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH || ActivityTaskManagerDebugConfig.DEBUG_CONFIGURATION) {
                    Slog.v(TAG_CONFIGURATION, "Skipping config check (will change): " + this);
                }
                return true;
            } else if (this.finishing) {
                if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH || ActivityTaskManagerDebugConfig.DEBUG_CONFIGURATION) {
                    Slog.v(TAG_CONFIGURATION, "Configuration doesn't matter in finishing " + this);
                }
                stopFreezingScreenLocked(false);
                return true;
            } else if (ignoreStopState || isSplitBaseActivity() || !(this.mState == ActivityStack.ActivityState.STOPPING || this.mState == ActivityStack.ActivityState.STOPPED)) {
                if (!shouldBeVisible()) {
                    if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH || ActivityTaskManagerDebugConfig.DEBUG_CONFIGURATION) {
                        Slog.v(TAG_CONFIGURATION, "Skipping config check invisible stack: " + this);
                    }
                    if (!this.mLastReportedConfiguration.getMergedConfiguration().windowConfiguration.inHwPCFreeFormWindowingMode()) {
                        return true;
                    }
                }
                if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH || ActivityTaskManagerDebugConfig.DEBUG_CONFIGURATION) {
                    Slog.v(TAG_CONFIGURATION, "Ensuring correct configuration: " + this);
                }
                int newDisplayId = getDisplayId();
                boolean displayChanged = this.mLastReportedDisplayId != newDisplayId;
                if (displayChanged) {
                    this.mIsChangeToDefaultDisplay = newDisplayId == 0;
                    if (this.mAtmService.mHwATMSEx.isVirtualDisplayId(newDisplayId, "padCast") || this.mIsChangeToDefaultDisplay) {
                        this.mIsIgnoreColorMode = true;
                    }
                    this.mLastReportedDisplayId = newDisplayId;
                }
                updateOverrideConfiguration();
                this.mTmpConfig.setTo(this.mLastReportedConfiguration.getMergedConfiguration());
                if (!getConfiguration().equals(this.mTmpConfig) || this.forceNewConfig || displayChanged) {
                    int changes = getConfigurationChanges(this.mTmpConfig);
                    Configuration newMergedOverrideConfig = getMergedOverrideConfiguration();
                    setLastReportedConfiguration(this.mAtmService.getGlobalConfiguration(), newMergedOverrideConfig);
                    if (this.mState == ActivityStack.ActivityState.INITIALIZING) {
                        if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH || ActivityTaskManagerDebugConfig.DEBUG_CONFIGURATION) {
                            Slog.v(TAG_CONFIGURATION, "Skipping config check for initializing activity: " + this);
                        }
                        return true;
                    } else if (changes == 0 && !this.forceNewConfig) {
                        if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH || ActivityTaskManagerDebugConfig.DEBUG_CONFIGURATION) {
                            Slog.v(TAG_CONFIGURATION, "Configuration no differences in " + this);
                        }
                        if (displayChanged) {
                            scheduleActivityMovedToDisplay(newDisplayId, newMergedOverrideConfig);
                        } else {
                            scheduleConfigurationChanged(newMergedOverrideConfig);
                        }
                        return true;
                    } else if (!attachedToProcess()) {
                        if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH || ActivityTaskManagerDebugConfig.DEBUG_CONFIGURATION) {
                            Slog.v(TAG_CONFIGURATION, "Configuration doesn't matter not running " + this);
                        }
                        stopFreezingScreenLocked(false);
                        this.forceNewConfig = false;
                        return true;
                    } else {
                        if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH || ActivityTaskManagerDebugConfig.DEBUG_CONFIGURATION) {
                            Slog.v(TAG_CONFIGURATION, "Checking to restart " + this.info.name + ": changed=0x" + Integer.toHexString(changes) + ", handles=0x" + Integer.toHexString(this.info.getRealConfigChanged()) + ", mLastReportedConfiguration=" + this.mLastReportedConfiguration + ", forceNewConfig:" + this.forceNewConfig);
                        }
                        if (HwActivityTaskManager.IS_HW_MULTIWINDOW_APPCOMPACT_SUPPORTED && !this.forceNewConfig && IS_FORCE_RELAUNCH_WHEN_APP_BOUNDS_CHANGE && !getConfiguration().windowConfiguration.getAppBounds().equals(this.mTmpConfig.windowConfiguration.getAppBounds()) && (HwActivityTaskManager.isAdjustConfig(getConfiguration()) || HwActivityTaskManager.isAdjustConfig(this.mTmpConfig) || displayChanged)) {
                            Slog.i(TAG_CONFIGURATION, "Force relaunch " + this);
                            this.forceNewConfig = true;
                        }
                        if (shouldRelaunchLocked(changes, this.mTmpConfig) || this.forceNewConfig) {
                            this.configChangeFlags |= changes;
                            startFreezingScreenLocked(this.app, globalChanges);
                            this.forceNewConfig = false;
                            boolean preserveWindow2 = preserveWindow & isResizeOnlyChange(changes);
                            if (hasResizeChange((~this.info.getRealConfigChanged()) & changes)) {
                                if (getTaskRecord().getTask().isDragResizing()) {
                                    i = 2;
                                } else {
                                    i = 1;
                                }
                                this.mRelaunchReason = i;
                            } else {
                                this.mRelaunchReason = 0;
                            }
                            if (!attachedToProcess()) {
                                if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH || ActivityTaskManagerDebugConfig.DEBUG_CONFIGURATION) {
                                    Slog.v(TAG_CONFIGURATION, "Config is destroying non-running " + this);
                                }
                                stack.destroyActivityLocked(this, true, "config");
                            } else if (this.mState == ActivityStack.ActivityState.PAUSING) {
                                if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH || ActivityTaskManagerDebugConfig.DEBUG_CONFIGURATION) {
                                    Slog.v(TAG_CONFIGURATION, "Config is skipping already pausing " + this);
                                }
                                this.deferRelaunchUntilPaused = true;
                                this.preserveWindowOnDeferredRelaunch = preserveWindow2;
                                return true;
                            } else if (this.mState == ActivityStack.ActivityState.RESUMED) {
                                if (!ActivityTaskManagerDebugConfig.DEBUG_STATES || this.visible) {
                                    Slog.i(TAG_CONFIGURATION, "Config is relaunching resumed " + this + ", changes=0x" + Integer.toHexString(changes));
                                } else {
                                    Slog.v("ActivityTaskManager", "Config is relaunching resumed invisible activity " + this + " called by " + Debug.getCallers(4));
                                }
                                relaunchActivityLocked(true, preserveWindow2);
                            } else if (this.mState != ActivityStack.ActivityState.PAUSED || !inHwMagicWindowingMode()) {
                                Slog.i(TAG_CONFIGURATION, "Config is relaunching non-resumed " + this + ", changes=0x" + Integer.toHexString(changes));
                                relaunchActivityLocked(false, preserveWindow2);
                            } else {
                                Slog.i(TAG_CONFIGURATION, "Config is relaunching paused for magicwindow " + this + ", changes=0x" + Integer.toHexString(changes));
                                relaunchActivityLocked(true, preserveWindow2);
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
                    if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH || ActivityTaskManagerDebugConfig.DEBUG_CONFIGURATION) {
                        Slog.v(TAG_CONFIGURATION, "Configuration & display unchanged in " + this);
                    }
                    return true;
                }
            } else {
                if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH || ActivityTaskManagerDebugConfig.DEBUG_CONFIGURATION) {
                    Slog.v(TAG_CONFIGURATION, "Skipping config check stopped or stopping: " + this);
                }
                return true;
            }
        }
    }

    private boolean shouldRelaunchLocked(int changes, Configuration changesConfig) {
        int configChanged = this.info.getRealConfigChanged();
        boolean onlyVrUiModeChanged = onlyVrUiModeChanged(changes, changesConfig);
        if (HwFoldScreenState.isFoldScreenDevice()) {
            int newDisplayMode = this.mAtmService.mWindowManager.getFoldDisplayMode();
            boolean isRelaunchDisable = HwPackageManager.getDisplayChangeAppRestartConfig(3, this.packageName) == 5 && HwPackageManager.getDisplayChangeAppRestartConfig(4, this.packageName) == CONFIG_RELAUNCH_DISABLE;
            boolean isDispalyModeChange = this.mLastReportedDisplayMode != newDisplayMode;
            this.mLastReportedDisplayMode = newDisplayMode;
            if (isRelaunchDisable && isDispalyModeChange && this.mState == ActivityStack.ActivityState.RESUMED) {
                Slog.i("ActivityTaskManager", "should not relaunch for config, pkg:" + this.packageName);
                return false;
            }
        }
        if (this.appInfo.targetSdkVersion < 26 && this.requestedVrComponent != null && onlyVrUiModeChanged) {
            configChanged |= 512;
        }
        if (this.mAtmService.mVrMananger.isVRDisplayConnected() && this.mAtmService.mVrMananger.isVirtualScreenMode()) {
            configChanged |= 8;
        }
        if (ActivityTaskManagerDebugConfig.DEBUG_CONFIGURATION) {
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
            if (!(crossesHorizontalSizeThreshold(lastReportedConfig.screenWidthDp, currentConfig.screenWidthDp) || crossesVerticalSizeThreshold(lastReportedConfig.screenHeightDp, currentConfig.screenHeightDp)) || currentConfig.windowConfiguration.inHwPCFreeFormWindowingMode() || lastReportedConfig.windowConfiguration.inHwPCFreeFormWindowingMode()) {
                changes &= -1025;
            }
        }
        if ((changes & 2048) != 0 && !crossesSmallestSizeThreshold(lastReportedConfig.smallestScreenWidthDp, currentConfig.smallestScreenWidthDp)) {
            changes &= -2049;
        }
        if ((536870912 & changes) != 0) {
            changes &= -536870913;
        }
        if (!this.mIsIgnoreColorMode || (changes & 16384) == 0) {
            return changes;
        }
        int changes2 = changes & -16385;
        this.mIsIgnoreColorMode = false;
        return changes2;
    }

    private static boolean isResizeOnlyChange(int change) {
        return (change & -3457) == 0;
    }

    private static boolean hasResizeChange(int change) {
        return (change & 3456) != 0;
    }

    /* access modifiers changed from: package-private */
    public void relaunchActivityLocked(boolean andResume, boolean preserveWindow) {
        int i;
        ActivityLifecycleItem lifecycleItem;
        if (!this.mAtmService.mSuppressResizeConfigChanges || !preserveWindow) {
            List<ResultInfo> pendingResults2 = null;
            List<ReferrerIntent> pendingNewIntents = null;
            if (andResume) {
                pendingResults2 = this.results;
                pendingNewIntents = this.newIntents;
            }
            if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH) {
                Slog.v("ActivityTaskManager", "Relaunching: " + this + " with results=" + pendingResults2 + " newIntents=" + pendingNewIntents + " andResume=" + andResume + " preserveWindow=" + preserveWindow);
            }
            if (andResume) {
                i = 30019;
            } else {
                i = 30020;
            }
            EventLog.writeEvent(i, Integer.valueOf(this.mUserId), Integer.valueOf(System.identityHashCode(this)), Integer.valueOf(this.task.taskId), this.shortComponentName);
            startFreezingScreenLocked(this.app, 0);
            try {
                if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH || ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Moving to ");
                    sb.append(andResume ? "RESUMED" : "PAUSED");
                    sb.append(" Relaunching ");
                    sb.append(this);
                    sb.append(" callers=");
                    sb.append(Debug.getCallers(6));
                    Slog.i("ActivityTaskManager", sb.toString());
                }
                this.forceNewConfig = false;
                this.mStackSupervisor.activityRelaunchingLocked(this);
                ClientTransactionItem callbackItem = ActivityRelaunchItem.obtain(pendingResults2, pendingNewIntents, this.configChangeFlags, new MergedConfiguration(this.mAtmService.getGlobalConfiguration(), getMergedOverrideConfiguration()), preserveWindow);
                if (andResume) {
                    lifecycleItem = ResumeActivityItem.obtain(getDisplay().mDisplayContent.isNextTransitionForward());
                } else {
                    lifecycleItem = PauseActivityItem.obtain();
                }
                ClientTransaction transaction = ClientTransaction.obtain(this.app.getThread(), this.appToken);
                transaction.addCallback(callbackItem);
                transaction.setLifecycleStateRequest(lifecycleItem);
                this.mAtmService.getLifecycleManager().scheduleTransaction(transaction);
            } catch (RemoteException e) {
                if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH || ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                    Slog.i("ActivityTaskManager", "Relaunch failed", e);
                }
            }
            if (andResume) {
                if (ActivityTaskManagerDebugConfig.DEBUG_STATES) {
                    Slog.d("ActivityTaskManager", "Resumed after relaunch " + this);
                }
                this.results = null;
                this.newIntents = null;
                this.mAtmService.getAppWarningsLocked().onResumeActivity(this);
            } else {
                ActivityStack stack = getActivityStack();
                if (stack != null) {
                    stack.mHandler.removeMessages(101, this);
                }
                setState(ActivityStack.ActivityState.PAUSED, "relaunchActivityLocked");
            }
            this.configChangeFlags = 0;
            this.deferRelaunchUntilPaused = false;
            this.preserveWindowOnDeferredRelaunch = false;
            return;
        }
        this.configChangeFlags = 0;
    }

    /* access modifiers changed from: package-private */
    public void restartProcessIfVisible() {
        Slog.i("ActivityTaskManager", "Request to restart process of " + this);
        getRequestedOverrideConfiguration().unset();
        getResolvedOverrideConfiguration().unset();
        this.mCompatDisplayInsets = null;
        if (this.visible) {
            updateOverrideConfiguration();
        }
        if (attachedToProcess()) {
            if (!this.visible || this.haveState) {
                this.mAtmService.mH.post(new Runnable() {
                    /* class com.android.server.wm.$$Lambda$ActivityRecord$rAbBM9IZ5lau2L_lVdzsqLwNpA */

                    @Override // java.lang.Runnable
                    public final void run() {
                        ActivityRecord.this.lambda$restartProcessIfVisible$0$ActivityRecord();
                    }
                });
                return;
            }
            setState(ActivityStack.ActivityState.RESTARTING_PROCESS, "restartActivityProcess");
            AppWindowToken appWindowToken = this.mAppWindowToken;
            if (appWindowToken != null) {
                appWindowToken.startFreezingScreen();
            }
            try {
                this.mAtmService.getLifecycleManager().scheduleTransaction(this.app.getThread(), (IBinder) this.appToken, (ActivityLifecycleItem) StopActivityItem.obtain(false, 0));
            } catch (RemoteException e) {
                Slog.w("ActivityTaskManager", "Exception thrown during restart " + this, e);
            }
            this.mStackSupervisor.scheduleRestartTimeout(this);
        }
    }

    public /* synthetic */ void lambda$restartProcessIfVisible$0$ActivityRecord() {
        synchronized (this.mAtmService.mGlobalLock) {
            try {
                WindowManagerService.boostPriorityForLockedSection();
                if (hasProcess()) {
                    if (this.app.getReportedProcState() > CONFIG_RELAUNCH_DISABLE) {
                        setState(ActivityStack.ActivityState.RESTARTING_PROCESS, "restartActivityProcess");
                        WindowProcessController wpc = this.app;
                        WindowManagerService.resetPriorityAfterLockedSection();
                        this.mAtmService.mAmInternal.killProcess(wpc.mName, wpc.mUid, "resetConfig");
                        return;
                    }
                }
                StringBuilder sb = new StringBuilder();
                sb.append("restartProcessIfVisible hasProcess:");
                sb.append(hasProcess());
                sb.append(", procState:");
                sb.append(this.app != null ? Integer.valueOf(this.app.getReportedProcState()) : "app is null");
                Slog.i("ActivityTaskManager", sb.toString());
            } finally {
                WindowManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    private boolean isProcessRunning() {
        WindowProcessController proc = this.app;
        if (proc == null) {
            proc = (WindowProcessController) this.mAtmService.mProcessNames.get(this.processName, this.info.applicationInfo.uid);
        }
        return proc != null && proc.hasThread();
    }

    private boolean allowTaskSnapshot() {
        ArrayList<ReferrerIntent> arrayList = this.newIntents;
        if (arrayList == null) {
            return true;
        }
        for (int i = arrayList.size() - 1; i >= 0; i--) {
            Intent intent2 = this.newIntents.get(i);
            if (!(intent2 == null || isMainIntent(intent2))) {
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
        String str = this.launchedFromPackage;
        if (str != null) {
            out.attribute(null, ATTR_LAUNCHEDFROMPACKAGE, str);
        }
        String str2 = this.resolvedType;
        if (str2 != null) {
            out.attribute(null, ATTR_RESOLVEDTYPE, str2);
        }
        out.attribute(null, ATTR_COMPONENTSPECIFIED, String.valueOf(this.componentSpecified));
        out.attribute(null, ATTR_USERID, String.valueOf(this.mUserId));
        ActivityManager.TaskDescription taskDescription2 = this.taskDescription;
        if (taskDescription2 != null) {
            taskDescription2.saveToXml(out);
        }
        out.startTag(null, TAG_INTENT);
        this.intent.saveToXml(out);
        out.endTag(null, TAG_INTENT);
        if (isPersistable() && this.persistentState != null) {
            out.startTag(null, TAG_PERSISTABLEBUNDLE);
            this.persistentState.saveToXml(out);
            out.endTag(null, TAG_PERSISTABLEBUNDLE);
        }
    }

    static ActivityRecord restoreFromXml(XmlPullParser in, ActivityStackSupervisor stackSupervisor) throws IOException, XmlPullParserException {
        Intent intent2;
        XmlPullParser xmlPullParser = in;
        Intent intent3 = null;
        PersistableBundle persistentState2 = null;
        int launchedFromUid2 = 0;
        String launchedFromPackage2 = null;
        String resolvedType2 = null;
        boolean componentSpecified2 = false;
        int userId = 0;
        long createTime2 = -1;
        int outerDepth = in.getDepth();
        ActivityManager.TaskDescription taskDescription2 = new ActivityManager.TaskDescription();
        int attrNdx = in.getAttributeCount() - 1;
        while (attrNdx >= 0) {
            String attrName = xmlPullParser.getAttributeName(attrNdx);
            String attrValue = xmlPullParser.getAttributeValue(attrNdx);
            if (ATTR_ID.equals(attrName)) {
                createTime2 = Long.parseLong(attrValue);
                intent2 = intent3;
            } else if (ATTR_LAUNCHEDFROMUID.equals(attrName)) {
                launchedFromUid2 = Integer.parseInt(attrValue);
                intent2 = intent3;
            } else if (ATTR_LAUNCHEDFROMPACKAGE.equals(attrName)) {
                launchedFromPackage2 = attrValue;
                intent2 = intent3;
            } else if (ATTR_RESOLVEDTYPE.equals(attrName)) {
                resolvedType2 = attrValue;
                intent2 = intent3;
            } else if (ATTR_COMPONENTSPECIFIED.equals(attrName)) {
                componentSpecified2 = Boolean.parseBoolean(attrValue);
                intent2 = intent3;
            } else if (ATTR_USERID.equals(attrName)) {
                userId = Integer.parseInt(attrValue);
                intent2 = intent3;
            } else if (attrName.startsWith("task_description_")) {
                taskDescription2.restoreFromXml(attrName, attrValue);
                intent2 = intent3;
            } else {
                StringBuilder sb = new StringBuilder();
                intent2 = intent3;
                sb.append("Unknown ActivityRecord attribute=");
                sb.append(attrName);
                Log.d("ActivityTaskManager", sb.toString());
            }
            attrNdx--;
            xmlPullParser = in;
            intent3 = intent2;
        }
        Intent intent4 = intent3;
        while (true) {
            int event = in.next();
            if (event == 1 || (event == 3 && in.getDepth() < outerDepth)) {
                break;
            } else if (event == 2) {
                String name = in.getName();
                if (TAG_INTENT.equals(name)) {
                    intent4 = Intent.restoreFromXml(in);
                } else if (TAG_PERSISTABLEBUNDLE.equals(name)) {
                    persistentState2 = PersistableBundle.restoreFromXml(in);
                } else {
                    Slog.w("ActivityTaskManager", "restoreActivity: unexpected name=" + name);
                    XmlUtils.skipCurrentTag(in);
                }
            }
        }
        if (intent4 != null) {
            ActivityTaskManagerService service = stackSupervisor.mService;
            ActivityInfo aInfo = stackSupervisor.resolveActivity(intent4, resolvedType2, 0, null, userId, Binder.getCallingUid());
            if (aInfo != null) {
                ActivityRecord r = HwServiceFactory.createActivityRecord(service, (WindowProcessController) null, 0, launchedFromUid2, launchedFromPackage2, intent4, resolvedType2, aInfo, service.getConfiguration(), (ActivityRecord) null, (String) null, 0, componentSpecified2, false, stackSupervisor, (ActivityOptions) null, (ActivityRecord) null);
                r.persistentState = persistentState2;
                r.taskDescription = taskDescription2;
                r.createTime = createTime2;
                return r;
            }
            throw new XmlPullParserException("restoreActivity resolver error. Intent=" + intent4 + " resolvedType=" + resolvedType2);
        }
        throw new XmlPullParserException("restoreActivity error intent=" + intent4);
    }

    private static boolean isInVrUiMode(Configuration config) {
        return (config.uiMode & 15) == CONFIG_RELAUNCH_DISABLE;
    }

    public int getUid() {
        return this.info.applicationInfo.uid;
    }

    /* access modifiers changed from: package-private */
    public void setShowWhenLocked(boolean showWhenLocked) {
        this.mShowWhenLocked = showWhenLocked;
        this.mRootActivityContainer.ensureActivitiesVisible(null, 0, false);
    }

    /* access modifiers changed from: package-private */
    public void setInheritShowWhenLocked(boolean inheritShowWhenLocked) {
        this.mInheritShownWhenLocked = inheritShowWhenLocked;
        this.mRootActivityContainer.ensureActivitiesVisible(null, 0, false);
    }

    /* access modifiers changed from: package-private */
    public boolean canShowWhenLocked() {
        AppWindowToken appWindowToken;
        if (!inPinnedWindowingMode() && !WindowConfiguration.isHwPCFreeFormWindowingMode(getWindowingMode()) && (this.mShowWhenLocked || ((appWindowToken = this.mAppWindowToken) != null && appWindowToken.containsShowWhenLockedWindow()))) {
            return true;
        }
        if (!this.mInheritShownWhenLocked) {
            return false;
        }
        ActivityRecord r = getActivityBelow();
        if (r != null && !r.inPinnedWindowingMode()) {
            if (r.mShowWhenLocked) {
                return true;
            }
            AppWindowToken appWindowToken2 = r.mAppWindowToken;
            if (appWindowToken2 != null && appWindowToken2.containsShowWhenLockedWindow()) {
                return true;
            }
        }
        return false;
    }

    private ActivityRecord getActivityBelow() {
        int pos = this.task.mActivities.indexOf(this);
        if (pos == -1) {
            throw new IllegalStateException("Activity not found in its task");
        } else if (pos == 0) {
            return null;
        } else {
            return this.task.getChildAt(pos - 1);
        }
    }

    /* access modifiers changed from: package-private */
    public void setTurnScreenOn(boolean turnScreenOn) {
        this.mTurnScreenOn = turnScreenOn;
    }

    /* access modifiers changed from: package-private */
    public boolean canTurnScreenOn() {
        ActivityStack stack = getActivityStack();
        if (!this.mTurnScreenOn || stack == null || !stack.checkKeyguardVisibility(this, true, true)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean canResumeByCompat() {
        WindowProcessController windowProcessController = this.app;
        return windowProcessController == null || windowProcessController.updateTopResumingActivityInProcessIfNeeded(this);
    }

    /* access modifiers changed from: package-private */
    public boolean getTurnScreenOnFlag() {
        return this.mTurnScreenOn;
    }

    /* access modifiers changed from: package-private */
    public boolean isTopRunningActivity() {
        return this.mRootActivityContainer.topRunningActivity() == this;
    }

    /* access modifiers changed from: package-private */
    public boolean isResumedActivityOnDisplay() {
        ActivityDisplay display = getDisplay();
        return display != null && this == display.getResumedActivity();
    }

    /* access modifiers changed from: package-private */
    public void registerRemoteAnimations(RemoteAnimationDefinition definition) {
        AppWindowToken appWindowToken = this.mAppWindowToken;
        if (appWindowToken == null) {
            Slog.w("WindowManager", "Attempted to register remote animations with non-existing app token: " + this.appToken);
            return;
        }
        appWindowToken.registerRemoteAnimations(definition);
    }

    public String toString() {
        if (this.stringName != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(this.stringName);
            sb.append(" t");
            TaskRecord taskRecord = this.task;
            sb.append(taskRecord == null ? -1 : taskRecord.taskId);
            sb.append(this.finishing ? " f}" : "}");
            return sb.toString();
        }
        StringBuilder sb2 = new StringBuilder(128);
        sb2.append("ActivityRecord{");
        sb2.append(Integer.toHexString(System.identityHashCode(this)));
        sb2.append(" u");
        sb2.append(this.mUserId);
        sb2.append(' ');
        sb2.append(this.intent.getComponent().flattenToShortString());
        this.stringName = sb2.toString();
        return toString();
    }

    /* access modifiers changed from: package-private */
    public void writeIdentifierToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        proto.write(1120986464257L, System.identityHashCode(this));
        proto.write(1120986464258L, this.mUserId);
        proto.write(1138166333443L, this.intent.getComponent().flattenToShortString());
        proto.end(token);
    }

    /* access modifiers changed from: package-private */
    public void writeToProto(ProtoOutputStream proto) {
        super.writeToProto(proto, 1146756268033L, 0);
        writeIdentifierToProto(proto, 1146756268034L);
        proto.write(1138166333443L, this.mState.toString());
        proto.write(1133871366148L, this.visible);
        proto.write(1133871366149L, this.frontOfTask);
        if (hasProcess()) {
            proto.write(1120986464262L, this.app.getPid());
        }
        proto.write(1133871366151L, !this.fullscreen);
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long token = proto.start(fieldId);
        writeToProto(proto);
        proto.end(token);
    }

    /* access modifiers changed from: package-private */
    public static class CompatDisplayInsets {
        final int mDisplayHeight;
        final int mDisplayWidth;
        final Rect[] mNonDecorInsets = new Rect[4];
        final Rect[] mStableInsets = new Rect[4];

        CompatDisplayInsets(DisplayContent display) {
            this.mDisplayWidth = display.mBaseDisplayWidth;
            this.mDisplayHeight = display.mBaseDisplayHeight;
            DisplayPolicy policy = display.getDisplayPolicy();
            for (int rotation = 0; rotation < 4; rotation++) {
                this.mNonDecorInsets[rotation] = new Rect();
                this.mStableInsets[rotation] = new Rect();
                boolean rotated = true;
                if (!(rotation == 1 || rotation == 3)) {
                    rotated = false;
                }
                policy.getNonDecorInsetsLw(rotation, rotated ? this.mDisplayHeight : this.mDisplayWidth, rotated ? this.mDisplayWidth : this.mDisplayHeight, display.calculateDisplayCutoutForRotation(rotation).getDisplayCutout(), this.mNonDecorInsets[rotation]);
                this.mStableInsets[rotation].set(this.mNonDecorInsets[rotation]);
                policy.convertNonDecorInsetsToStableInsets(this.mStableInsets[rotation], rotation);
            }
        }

        /* access modifiers changed from: package-private */
        public void getDisplayBoundsByRotation(Rect outBounds, int rotation) {
            boolean rotated = true;
            if (!(rotation == 1 || rotation == 3)) {
                rotated = false;
            }
            outBounds.set(0, 0, rotated ? this.mDisplayHeight : this.mDisplayWidth, rotated ? this.mDisplayWidth : this.mDisplayHeight);
        }

        /* access modifiers changed from: package-private */
        public void getDisplayBoundsByOrientation(Rect outBounds, int orientation) {
            int longSide = Math.max(this.mDisplayWidth, this.mDisplayHeight);
            int shortSide = Math.min(this.mDisplayWidth, this.mDisplayHeight);
            boolean isLandscape = orientation == 2;
            outBounds.set(0, 0, isLandscape ? longSide : shortSide, isLandscape ? shortSide : longSide);
        }
    }
}
