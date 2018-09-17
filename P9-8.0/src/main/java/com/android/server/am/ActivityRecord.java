package com.android.server.am;

import android.app.ActivityManager;
import android.app.ActivityManager.StackId;
import android.app.ActivityManager.TaskDescription;
import android.app.ActivityOptions;
import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.PictureInPictureParams.Builder;
import android.app.ResultInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageParser;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.GraphicBuffer;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Debug;
import android.os.IBinder;
import android.os.IRemoteCallback;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.Trace;
import android.os.UserHandle;
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
import android.view.AppTransitionAnimationSpec;
import android.view.IAppTransitionAnimationSpecsFuture;
import android.view.IApplicationToken.Stub;
import com.android.internal.R;
import com.android.internal.app.ResolverActivity;
import com.android.internal.content.ReferrerIntent;
import com.android.internal.util.XmlUtils;
import com.android.server.AttributeCache;
import com.android.server.AttributeCache.Entry;
import com.android.server.HwServiceFactory;
import com.android.server.am.ActivityStackSupervisor.ActivityContainer;
import com.android.server.os.HwBootFail;
import com.android.server.wm.AppWindowContainerController;
import com.android.server.wm.AppWindowContainerListener;
import com.android.server.wm.TaskWindowContainerController;
import com.android.server.wm.WindowManagerService;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class ActivityRecord extends AbsActivityRecord implements AppWindowContainerListener {
    private static final /* synthetic */ int[] -com-android-server-am-ActivityStack$ActivityStateSwitchesValues = null;
    static final String ACTIVITY_ICON_SUFFIX = "_activity_icon_";
    static final int APPLICATION_ACTIVITY_TYPE = 0;
    static final int ASSISTANT_ACTIVITY_TYPE = 3;
    private static final String ATTR_COMPONENTSPECIFIED = "component_specified";
    private static final String ATTR_ID = "id";
    private static final String ATTR_LAUNCHEDFROMPACKAGE = "launched_from_package";
    private static final String ATTR_LAUNCHEDFROMUID = "launched_from_uid";
    private static final String ATTR_RESOLVEDTYPE = "resolved_type";
    private static final String ATTR_USERID = "user_id";
    static final int HOME_ACTIVITY_TYPE = 1;
    static final int RECENTS_ACTIVITY_TYPE = 2;
    private static final String RECENTS_PACKAGE_NAME = "com.android.systemui.recents";
    private static final boolean SHOW_ACTIVITY_START_TIME = true;
    static final int STARTING_WINDOW_NOT_SHOWN = 0;
    static final int STARTING_WINDOW_REMOVED = 2;
    static final int STARTING_WINDOW_SHOWN = 1;
    private static final String TAG = "ActivityManager";
    private static final String TAG_CONFIGURATION = (TAG + ActivityManagerDebugConfig.POSTFIX_CONFIGURATION);
    private static final String TAG_INTENT = "intent";
    private static final String TAG_PERSISTABLEBUNDLE = "persistable_bundle";
    private static final String TAG_SAVED_STATE = (TAG + ActivityManagerDebugConfig.POSTFIX_SAVED_STATE);
    private static final String TAG_SCREENSHOTS = (TAG + ActivityManagerDebugConfig.POSTFIX_SCREENSHOTS);
    protected static final String TAG_STATES = (TAG + ActivityManagerDebugConfig.POSTFIX_STATES);
    private static final String TAG_SWITCH = (TAG + ActivityManagerDebugConfig.POSTFIX_SWITCH);
    private static final String TAG_THUMBNAILS = (TAG + ActivityManagerDebugConfig.POSTFIX_THUMBNAILS);
    private static final String TAG_VISIBILITY = (TAG + ActivityManagerDebugConfig.POSTFIX_VISIBILITY);
    ProcessRecord app;
    final ApplicationInfo appInfo;
    AppTimeTracker appTimeTracker;
    final Stub appToken;
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
    private int logo;
    int mActivityType;
    private final Rect mBounds = new Rect();
    ArrayList<ActivityContainer> mChildContainers = new ArrayList();
    private boolean mDeferHidingClient;
    private int[] mHorizontalSizeConfigurations;
    ActivityContainer mInitialActivityContainer;
    private MergedConfiguration mLastReportedConfiguration;
    private int mLastReportedDisplayId;
    private boolean mLastReportedMultiWindowMode;
    private boolean mLastReportedPictureInPictureMode;
    boolean mLaunchTaskBehind;
    int mRotationAnimationHint = -1;
    private int[] mSmallestSizeConfigurations;
    final ActivityStackSupervisor mStackSupervisor;
    int mStartingWindowState = 0;
    boolean mTaskOverlay = false;
    private final Rect mTmpBounds = new Rect();
    private final Configuration mTmpConfig = new Configuration();
    boolean mUpdateTaskThumbnailWhenHidden;
    private int[] mVerticalSizeConfigurations;
    AppWindowContainerController mWindowContainerController;
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
    PictureInPictureParams pictureInPictureArgs = new Builder().build();
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
    ActivityState state;
    final boolean stateNotNeeded;
    boolean stopped;
    String stringName;
    boolean supportsPictureInPictureWhilePausing;
    TaskRecord task;
    final String taskAffinity;
    TaskDescription taskDescription;
    private int theme;
    public boolean translucent;
    UriPermissionOwner uriPermissions;
    final int userId;
    boolean visible;
    boolean visibleIgnoringKeyguard;
    IVoiceInteractionSession voiceSession;
    private int windowFlags;

    static class Token extends Stub {
        private final WeakReference<ActivityRecord> weakActivity;

        Token(ActivityRecord activity) {
            this.weakActivity = new WeakReference(activity);
        }

        private static ActivityRecord tokenToActivityRecordLocked(Token token) {
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
    }

    private static /* synthetic */ int[] -getcom-android-server-am-ActivityStack$ActivityStateSwitchesValues() {
        if (-com-android-server-am-ActivityStack$ActivityStateSwitchesValues != null) {
            return -com-android-server-am-ActivityStack$ActivityStateSwitchesValues;
        }
        int[] iArr = new int[ActivityState.values().length];
        try {
            iArr[ActivityState.DESTROYED.ordinal()] = 5;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ActivityState.DESTROYING.ordinal()] = 6;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ActivityState.FINISHING.ordinal()] = 7;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ActivityState.INITIALIZING.ordinal()] = 8;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[ActivityState.PAUSED.ordinal()] = 1;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[ActivityState.PAUSING.ordinal()] = 2;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[ActivityState.RESUMED.ordinal()] = 3;
        } catch (NoSuchFieldError e7) {
        }
        try {
            iArr[ActivityState.STOPPED.ordinal()] = 9;
        } catch (NoSuchFieldError e8) {
        }
        try {
            iArr[ActivityState.STOPPING.ordinal()] = 4;
        } catch (NoSuchFieldError e9) {
        }
        -com-android-server-am-ActivityStack$ActivityStateSwitchesValues = iArr;
        return iArr;
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

    void dump(PrintWriter pw, String prefix) {
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
        }
        pw.print(prefix);
        pw.print("stateNotNeeded=");
        pw.print(this.stateNotNeeded);
        pw.print(" componentSpecified=");
        pw.print(this.componentSpecified);
        pw.print(" mActivityType=");
        pw.println(this.mActivityType);
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
        if (!this.mBounds.isEmpty()) {
            pw.println(prefix + "mBounds=" + this.mBounds);
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
        if (this.taskDescription != null) {
            String iconFilename = this.taskDescription.getIconFilename();
            if (!(iconFilename == null && this.taskDescription.getLabel() == null && this.taskDescription.getPrimaryColor() == 0)) {
                pw.print(prefix);
                pw.print("taskDescription:");
                pw.print(" iconFilename=");
                pw.print(this.taskDescription.getIconFilename());
                pw.print(" label=\"");
                pw.print(this.taskDescription.getLabel());
                pw.print("\"");
                pw.print(" primaryColor=");
                pw.println(Integer.toHexString(this.taskDescription.getPrimaryColor()));
                pw.print(prefix + " backgroundColor=");
                pw.println(Integer.toHexString(this.taskDescription.getBackgroundColor()));
                pw.print(prefix + " statusBarColor=");
                pw.println(Integer.toHexString(this.taskDescription.getStatusBarColor()));
                pw.print(prefix + " navigationBarColor=");
                pw.println(Integer.toHexString(this.taskDescription.getNavigationBarColor()));
            }
            if (iconFilename == null && this.taskDescription.getIcon() != null) {
                pw.print(prefix);
                pw.println("taskDescription contains Bitmap");
            }
        }
        if (this.results != null) {
            pw.print(prefix);
            pw.print("results=");
            pw.println(this.results);
        }
        if (this.pendingResults != null && this.pendingResults.size() > 0) {
            pw.print(prefix);
            pw.println("Pending Results:");
            for (WeakReference<PendingIntentRecord> wpir : this.pendingResults) {
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
                Intent intent = (Intent) this.newIntents.get(i);
                pw.print(prefix);
                pw.print("  - ");
                if (intent == null) {
                    pw.println("null");
                } else {
                    pw.println(intent.toShortString(true, true, false, true));
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
        pw.print(this.state);
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
        pw.println(activityTypeToString(this.mActivityType));
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
                pw.println(prefix + "supportsPictureInPictureWhilePausing: " + this.supportsPictureInPictureWhilePausing);
            }
            if (this.info.maxAspectRatio != 0.0f) {
                pw.println(prefix + "maxAspectRatio=" + this.info.maxAspectRatio);
            }
        }
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

    void setSizeConfigurations(int[] horizontalSizeConfiguration, int[] verticalSizeConfigurations, int[] smallestSizeConfigurations) {
        this.mHorizontalSizeConfigurations = horizontalSizeConfiguration;
        this.mVerticalSizeConfigurations = verticalSizeConfigurations;
        this.mSmallestSizeConfigurations = smallestSizeConfigurations;
    }

    private void scheduleActivityMovedToDisplay(int displayId, Configuration config) {
        if (this.app == null || this.app.thread == null) {
            if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                Slog.w(TAG, "Can't report activity moved to display - client not running, activityRecord=" + this + ", displayId=" + displayId);
            }
            return;
        }
        try {
            if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                Slog.v(TAG, "Reporting activity moved to display, activityRecord=" + this + ", displayId=" + displayId + ", config=" + config);
            }
            this.app.thread.scheduleActivityMovedToDisplay(this.appToken, displayId, new Configuration(config));
        } catch (RemoteException e) {
        }
    }

    private void scheduleConfigurationChanged(Configuration config) {
        if (this.app == null || this.app.thread == null) {
            if (ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                Slog.w(TAG, "Can't report activity configuration update - client not running, activityRecord=" + this);
            }
            return;
        }
        try {
            if (ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                Slog.v(TAG, "Sending new config to " + this + ", config: " + config);
            }
            this.app.thread.scheduleActivityConfigurationChanged(this.appToken, new Configuration(config));
        } catch (RemoteException e) {
        }
    }

    void updateMultiWindowMode() {
        if (this.task != null && this.task.getStack() != null && this.app != null && this.app.thread != null) {
            boolean inMultiWindowMode = this.task.mFullscreen ^ 1;
            if (inMultiWindowMode != this.mLastReportedMultiWindowMode) {
                this.mLastReportedMultiWindowMode = inMultiWindowMode;
                scheduleMultiWindowModeChanged(getConfiguration());
            }
        }
    }

    void scheduleMultiWindowModeChanged(Configuration overrideConfig) {
        try {
            this.app.thread.scheduleMultiWindowModeChanged(this.appToken, this.mLastReportedMultiWindowMode, overrideConfig);
        } catch (Exception e) {
        }
    }

    void updatePictureInPictureMode(Rect targetStackBounds) {
        if (this.task != null && this.task.getStack() != null && this.app != null && this.app.thread != null) {
            boolean inPictureInPictureMode = this.task.getStackId() == 4 ? targetStackBounds != null : false;
            if (inPictureInPictureMode != this.mLastReportedPictureInPictureMode) {
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
            this.app.thread.schedulePictureInPictureModeChanged(this.appToken, this.mLastReportedPictureInPictureMode, overrideConfig);
        } catch (Exception e) {
        }
    }

    boolean isFreeform() {
        return this.task != null && this.task.getStackId() == 2;
    }

    protected int getChildCount() {
        return 0;
    }

    protected ConfigurationContainer getChildAt(int index) {
        return null;
    }

    protected ConfigurationContainer getParent() {
        return getTask();
    }

    TaskRecord getTask() {
        return this.task;
    }

    void setTask(TaskRecord task) {
        setTask(task, false);
    }

    void setTask(TaskRecord task, boolean reparenting) {
        if (task == null || task != getTask()) {
            ActivityStack stack = getStack();
            if (!(reparenting || stack == null || (task != null && stack == task.getStack()))) {
                stack.onActivityRemovedFromStack(this);
            }
            this.task = task;
            if (!reparenting) {
                onParentChanged();
            }
        }
    }

    public static ActivityRecord forToken(IBinder token) {
        return forTokenLocked(token);
    }

    static ActivityRecord forTokenLocked(IBinder token) {
        try {
            return Token.tokenToActivityRecordLocked((Token) token);
        } catch (ClassCastException e) {
            Slog.w(TAG, "Bad activity token: " + token, e);
            return null;
        }
    }

    boolean isResolverActivity() {
        return ResolverActivity.class.getName().equals(this.realActivity.getClassName());
    }

    public ActivityRecord(ActivityManagerService _service, ProcessRecord _caller, int _launchedFromPid, int _launchedFromUid, String _launchedFromPackage, Intent _intent, String _resolvedType, ActivityInfo aInfo, Configuration _configuration, ActivityRecord _resultTo, String _resultWho, int _reqCode, boolean _componentSpecified, boolean _rootVoiceInteraction, ActivityStackSupervisor supervisor, ActivityContainer container, ActivityOptions options, ActivityRecord sourceRecord) {
        this.service = _service;
        this.appToken = new Token(this);
        this.info = aInfo;
        this.launchedFromPid = _launchedFromPid;
        this.launchedFromUid = _launchedFromUid;
        this.launchedFromPackage = _launchedFromPackage;
        this.userId = UserHandle.getUserId(aInfo.applicationInfo.uid);
        this.intent = _intent;
        this.shortComponentName = _intent.getComponent().flattenToShortString();
        this.resolvedType = _resolvedType;
        this.componentSpecified = _componentSpecified;
        this.rootVoiceInteraction = _rootVoiceInteraction;
        this.mLastReportedConfiguration = new MergedConfiguration(_configuration);
        this.resultTo = _resultTo;
        this.resultWho = _resultWho;
        this.requestCode = _reqCode;
        this.state = ActivityState.INITIALIZING;
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
        this.mInitialActivityContainer = container;
        this.mRotationAnimationHint = aInfo.rotationAnimation;
        if (options != null) {
            this.pendingOptions = options;
            this.mLaunchTaskBehind = this.pendingOptions.getLaunchTaskBehind();
            int rotationAnimation = this.pendingOptions.getRotationAnimationHint();
            if (rotationAnimation >= 0) {
                this.mRotationAnimationHint = rotationAnimation;
            }
            PendingIntent usageReport = this.pendingOptions.getUsageTimeReport();
            if (usageReport != null) {
                this.appTimeTracker = new AppTimeTracker(usageReport);
            }
        }
        this.haveState = true;
        initSplitMode(_intent);
        if (aInfo.targetActivity == null || (aInfo.targetActivity.equals(_intent.getComponent().getClassName()) && (aInfo.launchMode == 0 || aInfo.launchMode == 1))) {
            this.realActivity = _intent.getComponent();
        } else {
            this.realActivity = new ComponentName(aInfo.packageName, aInfo.targetActivity);
        }
        if (isSplitMode()) {
            if (aInfo.taskAffinity == null || aInfo.taskAffinity.equals(aInfo.processName)) {
                this.taskAffinity = _caller.processName;
            } else {
                this.taskAffinity = aInfo.taskAffinity;
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
            ApplicationInfo app = aInfo.applicationInfo;
            this.nonLocalizedLabel = app.nonLocalizedLabel;
            this.labelRes = app.labelRes;
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
        if ((aInfo.flags & 1) == 0 || _caller == null || !(aInfo.applicationInfo.uid == 1000 || aInfo.applicationInfo.uid == _caller.info.uid)) {
            this.processName = aInfo.processName;
        } else {
            this.processName = _caller.processName;
        }
        if ((aInfo.flags & 32) != 0) {
            this.intent.addFlags(8388608);
        }
        this.packageName = aInfo.applicationInfo.packageName;
        this.launchMode = aInfo.launchMode;
        Entry ent = AttributeCache.instance().get(this.packageName, this.realTheme, R.styleable.Window, this.userId);
        boolean z = ent != null ? !ent.array.getBoolean(5, false) ? !ent.array.hasValue(5) ? ent.array.getBoolean(25, false) : false : true : false;
        this.translucent = z;
        z = (ent == null || (ActivityInfo.isTranslucentOrFloating(ent.array) ^ 1) == 0) ? isForceRotationMode(this.packageName, _intent) : true;
        this.fullscreen = z;
        if (isSplitMode()) {
            if (this.fullscreen) {
                this.fullscreen = false;
            } else {
                _intent.addHwFlags(8);
            }
        }
        this.noDisplay = ent != null ? ent.array.getBoolean(10, false) : false;
        setActivityType(_componentSpecified, _launchedFromUid, _intent, options, sourceRecord);
        this.immersive = (aInfo.flags & 2048) != 0;
        this.requestedVrComponent = aInfo.requestedVrComponent == null ? null : ComponentName.unflattenFromString(aInfo.requestedVrComponent);
    }

    AppWindowContainerController getWindowContainerController() {
        return this.mWindowContainerController;
    }

    void createWindowContainer(boolean naviBarHide) {
        if (this.mWindowContainerController != null) {
            throw new IllegalArgumentException("Window container=" + this.mWindowContainerController + " already created for r=" + this);
        }
        this.inHistory = true;
        TaskWindowContainerController taskController = this.task.getWindowContainerController();
        this.task.updateOverrideConfigurationFromLaunchBounds();
        updateOverrideConfiguration();
        this.mWindowContainerController = new AppWindowContainerController(taskController, this.appToken, this, HwBootFail.STAGE_BOOT_SUCCESS, this.info.screenOrientation, this.fullscreen, (this.info.flags & 1024) != 0, this.info.configChanges, this.task.voiceSession != null, this.mLaunchTaskBehind, isAlwaysFocusable(), this.appInfo.targetSdkVersion, this.mRotationAnimationHint, ActivityManagerService.getInputDispatchingTimeoutLocked(this) * 1000000, getOverrideConfiguration(), this.mBounds, naviBarHide, this.info.hwNotchSupport);
        this.task.addActivityToTop(this);
        this.mLastReportedMultiWindowMode = this.task.mFullscreen ^ 1;
        this.mLastReportedPictureInPictureMode = this.task.getStackId() == 4;
        onOverrideConfigurationSent();
    }

    void removeWindowContainer() {
        resumeKeyDispatchingLocked();
        this.mWindowContainerController.removeContainer(getDisplayId());
        this.mWindowContainerController = null;
    }

    void reparent(TaskRecord newTask, int position, String reason) {
        TaskRecord prevTask = this.task;
        if (prevTask == newTask) {
            throw new IllegalArgumentException(reason + ": task=" + newTask + " is already the parent of r=" + this);
        } else if (prevTask == null || newTask == null || prevTask.getStack() == newTask.getStack()) {
            if (this.mWindowContainerController != null) {
                this.mWindowContainerController.reparent(newTask.getWindowContainerController(), position);
            }
            prevTask.removeActivity(this, true);
            newTask.addActivityAtIndex(position, this);
        } else {
            throw new IllegalArgumentException(reason + ": task=" + newTask + " is in a different stack (" + newTask.getStackId() + ") than the parent of" + " r=" + this + " (" + prevTask.getStackId() + ")");
        }
    }

    private boolean isHomeIntent(Intent intent) {
        if ("android.intent.action.MAIN".equals(intent.getAction()) && intent.hasCategory("android.intent.category.HOME") && intent.getCategories().size() == 1 && intent.getData() == null) {
            return intent.getType() == null;
        } else {
            return false;
        }
    }

    static boolean isMainIntent(Intent intent) {
        if ("android.intent.action.MAIN".equals(intent.getAction()) && intent.hasCategory("android.intent.category.LAUNCHER") && intent.getCategories().size() == 1 && intent.getData() == null) {
            return intent.getType() == null;
        } else {
            return false;
        }
    }

    private boolean canLaunchHomeActivity(int uid, ActivityRecord sourceRecord) {
        boolean z = false;
        if (uid == Process.myUid() || uid == 0) {
            return true;
        }
        if (sourceRecord != null) {
            z = sourceRecord.isResolverActivity();
        }
        return z;
    }

    private boolean canLaunchAssistActivity(String packageName) {
        if (this.service.mAssistUtils == null) {
            return false;
        }
        ComponentName assistComponent = this.service.mAssistUtils.getActiveServiceComponentName();
        if (assistComponent != null) {
            return assistComponent.getPackageName().equals(packageName);
        }
        return false;
    }

    private void setActivityType(boolean componentSpecified, int launchedFromUid, Intent intent, ActivityOptions options, ActivityRecord sourceRecord) {
        if ((!componentSpecified || canLaunchHomeActivity(launchedFromUid, sourceRecord)) && isHomeIntent(intent) && (isResolverActivity() ^ 1) != 0) {
            this.mActivityType = 1;
            if (this.info.resizeMode == 4 || this.info.resizeMode == 1) {
                this.info.resizeMode = 0;
            }
        } else if (this.realActivity.getClassName().contains(RECENTS_PACKAGE_NAME)) {
            this.mActivityType = 2;
        } else if (options != null && options.getLaunchStackId() == 6 && canLaunchAssistActivity(this.launchedFromPackage)) {
            this.mActivityType = 3;
        } else {
            this.mActivityType = 0;
        }
    }

    void setTaskToAffiliateWith(TaskRecord taskToAffiliateWith) {
        if (this.launchMode != 3 && this.launchMode != 2) {
            this.task.setTaskToAffiliateWith(taskToAffiliateWith);
        }
    }

    <T extends ActivityStack> T getStack() {
        return this.task != null ? this.task.getStack() : null;
    }

    int getStackId() {
        return getStack() != null ? getStack().mStackId : -1;
    }

    boolean changeWindowTranslucency(boolean toOpaque) {
        if (this.fullscreen == toOpaque) {
            return false;
        }
        TaskRecord taskRecord = this.task;
        taskRecord.numFullscreen = (toOpaque ? 1 : -1) + taskRecord.numFullscreen;
        this.fullscreen = toOpaque;
        return true;
    }

    void takeFromHistory() {
        if (this.inHistory) {
            this.inHistory = false;
            if (!(this.task == null || (this.finishing ^ 1) == 0)) {
                this.task = null;
            }
            clearOptionsLocked();
        }
    }

    boolean isInHistory() {
        return this.inHistory;
    }

    boolean isInStackLocked() {
        ActivityStack stack = getStack();
        if (stack == null || stack.isInStackLocked(this) == null) {
            return false;
        }
        return true;
    }

    boolean isHomeActivity() {
        return this.mActivityType == 1;
    }

    boolean isRecentsActivity() {
        return this.mActivityType == 2;
    }

    boolean isAssistantActivity() {
        return this.mActivityType == 3;
    }

    boolean isApplicationActivity() {
        return this.mActivityType == 0;
    }

    boolean isPersistable() {
        boolean z = true;
        if (this.info.persistableMode != 0 && this.info.persistableMode != 2) {
            return false;
        }
        if (!(this.intent == null || (this.intent.getFlags() & 8388608) == 0)) {
            z = false;
        }
        return z;
    }

    boolean isFocusable() {
        return !StackId.canReceiveKeys(this.task.getStackId()) ? isAlwaysFocusable() : true;
    }

    boolean isResizeable() {
        return !ActivityInfo.isResizeableMode(this.info.resizeMode) ? this.info.supportsPictureInPicture() : true;
    }

    boolean isNonResizableOrForcedResizable() {
        if (this.info.resizeMode != 2) {
            return this.info.resizeMode != 1;
        } else {
            return false;
        }
    }

    boolean supportsPictureInPicture() {
        if (!this.service.mSupportsPictureInPicture || (isHomeActivity() ^ 1) == 0) {
            return false;
        }
        return this.info.supportsPictureInPicture();
    }

    boolean supportsSplitScreen() {
        return this.service.mSupportsSplitScreenMultiWindow ? supportsResizeableMultiWindow() : false;
    }

    boolean supportsFreeform() {
        return this.service.mSupportsFreeformWindowManagement ? supportsResizeableMultiWindow() : false;
    }

    private boolean supportsResizeableMultiWindow() {
        if (!this.service.mSupportsMultiWindow || (isHomeActivity() ^ 1) == 0) {
            return false;
        }
        if (ActivityInfo.isResizeableMode(this.info.resizeMode)) {
            return true;
        }
        return this.service.mForceResizableActivities;
    }

    boolean canBeLaunchedOnDisplay(int displayId) {
        return this.service.mStackSupervisor.canPlaceEntityOnDisplay(displayId, supportsResizeableMultiWindow());
    }

    boolean checkEnterPictureInPictureState(String caller, boolean noThrow, boolean beforeStopping) {
        boolean z = false;
        if (!supportsPictureInPicture() || !checkEnterPictureInPictureAppOpsState() || this.service.shouldDisableNonVrUiLocked()) {
            return false;
        }
        boolean isKeyguardLocked = this.service.isKeyguardLocked();
        boolean isCurrentAppLocked = this.mStackSupervisor.getLockTaskModeState() != 0;
        boolean hasPinnedStack = this.mStackSupervisor.getStack(4) != null;
        int isNotLockedOrOnKeyguard = !isKeyguardLocked ? isCurrentAppLocked ^ 1 : 0;
        if (beforeStopping && hasPinnedStack) {
            return false;
        }
        switch (-getcom-android-server-am-ActivityStack$ActivityStateSwitchesValues()[this.state.ordinal()]) {
            case 1:
            case 2:
                if (!(isNotLockedOrOnKeyguard == 0 || (hasPinnedStack ^ 1) == 0)) {
                    z = this.supportsPictureInPictureWhilePausing;
                }
                return z;
            case 3:
                if (!isCurrentAppLocked) {
                    z = !this.supportsPictureInPictureWhilePausing ? beforeStopping ^ 1 : true;
                }
                return z;
            case 4:
                if (this.supportsPictureInPictureWhilePausing) {
                    if (isNotLockedOrOnKeyguard != 0) {
                        z = hasPinnedStack ^ 1;
                    }
                    return z;
                }
                break;
        }
        if (noThrow) {
            return false;
        }
        throw new IllegalStateException(caller + ": Current activity is not visible (state=" + this.state.name() + ") " + "r=" + this);
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

    boolean isAlwaysFocusable() {
        return (this.info.flags & DumpState.DUMP_DOMAIN_PREFERRED) != 0;
    }

    boolean hasShowWhenLockedWindows() {
        return this.service.mWindowManager.containsShowWhenLockedWindow(this.appToken);
    }

    boolean hasDismissKeyguardWindows() {
        return this.service.mWindowManager.containsDismissKeyguardWindow(this.appToken);
    }

    void makeFinishingLocked() {
        if (!this.finishing) {
            ActivityStack stack = getStack();
            if (stack != null && this == stack.getVisibleBehindActivity()) {
                this.mStackSupervisor.requestVisibleBehindLocked(this, false);
            }
            this.finishing = true;
            if (this.stopped) {
                clearOptionsLocked();
            }
            if (this.service != null) {
                this.service.mTaskChangeNotificationController.notifyTaskStackChanged();
            }
        }
    }

    UriPermissionOwner getUriPermissionsLocked() {
        if (this.uriPermissions == null) {
            this.uriPermissions = new UriPermissionOwner(this.service, this);
        }
        return this.uriPermissions;
    }

    void addResultLocked(ActivityRecord from, String resultWho, int requestCode, int resultCode, Intent resultData) {
        ActivityResult r = new ActivityResult(from, resultWho, requestCode, resultCode, resultData);
        if (this.results == null) {
            this.results = new ArrayList();
        }
        this.results.add(r);
    }

    void removeResultsLocked(ActivityRecord from, String resultWho, int requestCode) {
        if (this.results != null) {
            for (int i = this.results.size() - 1; i >= 0; i--) {
                ActivityResult r = (ActivityResult) this.results.get(i);
                if (r.mFrom == from) {
                    if (r.mResultWho == null) {
                        if (resultWho != null) {
                        }
                    } else if (!r.mResultWho.equals(resultWho)) {
                    }
                    if (r.mRequestCode == requestCode) {
                        this.results.remove(i);
                    }
                }
            }
        }
    }

    private void addNewIntentLocked(ReferrerIntent intent) {
        if (this.newIntents == null) {
            this.newIntents = new ArrayList();
        }
        this.newIntents.add(intent);
    }

    final void deliverNewIntentLocked(int callingUid, Intent intent, String referrer) {
        this.service.grantUriPermissionFromIntentLocked(callingUid, this.packageName, intent, getUriPermissionsLocked(), this.userId);
        ReferrerIntent rintent = new ReferrerIntent(intent, referrer);
        boolean unsent = true;
        ActivityStack stack = getStack();
        boolean isTopActivityInStack = stack != null && stack.topRunningActivityLocked() == this;
        boolean isTopActivityWhileSleeping = this.service.isSleepingLocked() ? isTopActivityInStack : false;
        if (!((this.state != ActivityState.RESUMED && this.state != ActivityState.PAUSED && !isTopActivityWhileSleeping) || this.app == null || this.app.thread == null)) {
            try {
                ArrayList<ReferrerIntent> ar = new ArrayList(1);
                ar.add(rintent);
                this.app.thread.scheduleNewIntent(ar, this.appToken, this.state == ActivityState.PAUSED);
                unsent = false;
            } catch (RemoteException e) {
                Slog.w(TAG, "Exception thrown sending new intent to " + this, e);
            } catch (NullPointerException e2) {
                Slog.w(TAG, "Exception thrown sending new intent to " + this, e2);
            }
        }
        if (unsent) {
            addNewIntentLocked(rintent);
        }
    }

    void updateOptionsLocked(ActivityOptions options) {
        if (options != null) {
            if (this.pendingOptions != null) {
                this.pendingOptions.abort();
            }
            this.pendingOptions = options;
        }
    }

    void applyOptionsLocked() {
        if (this.pendingOptions != null && this.pendingOptions.getAnimationType() != 5) {
            int animationType = this.pendingOptions.getAnimationType();
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
                        }
                        boolean z;
                        WindowManagerService windowManagerService = this.service.mWindowManager;
                        GraphicBuffer thumbnail = this.pendingOptions.getThumbnail();
                        int startX = this.pendingOptions.getStartX();
                        int startY = this.pendingOptions.getStartY();
                        int width = this.pendingOptions.getWidth();
                        int height = this.pendingOptions.getHeight();
                        IRemoteCallback onAnimationStartListener = this.pendingOptions.getOnAnimationStartListener();
                        if (animationType == 8) {
                            z = true;
                        } else {
                            z = false;
                        }
                        windowManagerService.overridePendingAppTransitionAspectScaledThumb(thumbnail, startX, startY, width, height, onAnimationStartListener, z);
                        if (this.intent.getSourceBounds() == null) {
                            this.intent.setSourceBounds(new Rect(this.pendingOptions.getStartX(), this.pendingOptions.getStartY(), this.pendingOptions.getStartX() + this.pendingOptions.getWidth(), this.pendingOptions.getStartY() + this.pendingOptions.getHeight()));
                            break;
                        }
                    }
                    this.service.mWindowManager.overridePendingAppTransitionMultiThumbFuture(specsFuture, this.pendingOptions.getOnAnimationStartListener(), animationType == 8);
                    break;
                    break;
                case 11:
                    this.service.mWindowManager.overridePendingAppTransitionClipReveal(this.pendingOptions.getStartX(), this.pendingOptions.getStartY(), this.pendingOptions.getWidth(), this.pendingOptions.getHeight());
                    if (this.intent.getSourceBounds() == null) {
                        this.intent.setSourceBounds(new Rect(this.pendingOptions.getStartX(), this.pendingOptions.getStartY(), this.pendingOptions.getStartX() + this.pendingOptions.getWidth(), this.pendingOptions.getStartY() + this.pendingOptions.getHeight()));
                        break;
                    }
                    break;
                default:
                    Slog.e(TAG, "applyOptionsLocked: Unknown animationType=" + animationType);
                    break;
            }
            this.pendingOptions = null;
        }
    }

    ActivityOptions getOptionsForTargetActivityLocked() {
        return this.pendingOptions != null ? this.pendingOptions.forTargetActivity() : null;
    }

    void clearOptionsLocked() {
        if (this.pendingOptions != null) {
            this.pendingOptions.abort();
            this.pendingOptions = null;
        }
    }

    ActivityOptions takeOptionsLocked() {
        ActivityOptions opts = this.pendingOptions;
        this.pendingOptions = null;
        return opts;
    }

    void removeUriPermissionsLocked() {
        if (this.uriPermissions != null) {
            this.uriPermissions.removeUriPermissionsLocked();
            this.uriPermissions = null;
        }
    }

    void pauseKeyDispatchingLocked() {
        if (!this.keysPaused) {
            this.keysPaused = true;
            if (this.mWindowContainerController != null) {
                this.mWindowContainerController.pauseKeyDispatching();
            }
        }
    }

    void resumeKeyDispatchingLocked() {
        if (this.keysPaused) {
            this.keysPaused = false;
            if (this.mWindowContainerController != null) {
                this.mWindowContainerController.resumeKeyDispatching();
            }
        }
    }

    void updateThumbnailLocked(Bitmap newThumbnail, CharSequence description) {
        if (newThumbnail != null) {
            if (ActivityManagerDebugConfig.DEBUG_THUMBNAILS) {
                Slog.i(TAG_THUMBNAILS, "Setting thumbnail of " + this + " to " + newThumbnail);
            }
            if (this.task.setLastThumbnailLocked(newThumbnail) && isPersistable()) {
                this.service.notifyTaskPersisterLocked(this.task, false);
            }
        }
        this.task.lastDescription = description;
    }

    final Bitmap screenshotActivityLocked() {
        if (ActivityManagerDebugConfig.DEBUG_SCREENSHOTS) {
            Slog.d(TAG_SCREENSHOTS, "screenshotActivityLocked: " + this);
        }
        if (ActivityManager.ENABLE_TASK_SNAPSHOTS) {
            if (ActivityManagerDebugConfig.DEBUG_SCREENSHOTS) {
                Slog.d(TAG_SCREENSHOTS, "\tSnapshots are enabled, abort taking screenshot");
            }
            return null;
        } else if (this.noDisplay) {
            if (ActivityManagerDebugConfig.DEBUG_SCREENSHOTS) {
                Slog.d(TAG_SCREENSHOTS, "\tNo display");
            }
            return null;
        } else {
            ActivityStack stack = getStack();
            if (stack.isHomeOrRecentsStack()) {
                if (ActivityManagerDebugConfig.DEBUG_SCREENSHOTS) {
                    Slog.d(TAG_SCREENSHOTS, stack.getStackId() == 0 ? "\tHome stack" : "\tRecents stack");
                }
                return null;
            }
            int w = this.service.mThumbnailWidth;
            int h = this.service.mThumbnailHeight;
            if (w <= 0) {
                Slog.e(TAG, "\tInvalid thumbnail dimensions: " + w + "x" + h);
                return null;
            } else if (stack.mStackId == 3 && this.mStackSupervisor.mIsDockMinimized) {
                if (ActivityManagerDebugConfig.DEBUG_SCREENSHOTS) {
                    Slog.e(TAG, "\tIn minimized docked stack");
                }
                return null;
            } else {
                if (ActivityManagerDebugConfig.DEBUG_SCREENSHOTS) {
                    Slog.d(TAG_SCREENSHOTS, "\tTaking screenshot");
                }
                return this.mWindowContainerController.screenshotApplications(getDisplayId(), -1, -1, this.service.mFullscreenThumbnailScale);
            }
        }
    }

    void setDeferHidingClient(boolean deferHidingClient) {
        if (this.mDeferHidingClient != deferHidingClient) {
            this.mDeferHidingClient = deferHidingClient;
            if (!(this.mDeferHidingClient || (this.visible ^ 1) == 0)) {
                setVisibility(false);
            }
        }
    }

    void setVisibility(boolean visible) {
        if (this.mWindowContainerController != null) {
            this.mWindowContainerController.setVisibility(visible, this.mDeferHidingClient);
            this.mStackSupervisor.mActivityMetricsLogger.notifyVisibilityChanged(this, visible);
        }
    }

    void setVisible(boolean newVisible) {
        boolean z;
        this.visible = newVisible;
        if (this.visible) {
            z = false;
        } else {
            z = this.mDeferHidingClient;
        }
        this.mDeferHidingClient = z;
        if (!this.visible && this.mUpdateTaskThumbnailWhenHidden) {
            updateThumbnailLocked(screenshotActivityLocked(), null);
            this.mUpdateTaskThumbnailWhenHidden = false;
        }
        setVisibility(this.visible);
        ArrayList<ActivityContainer> containers = this.mChildContainers;
        for (int containerNdx = containers.size() - 1; containerNdx >= 0; containerNdx--) {
            ((ActivityContainer) containers.get(containerNdx)).setVisible(this.visible);
        }
        this.mStackSupervisor.mAppVisibilitiesChangedSinceLastPause = true;
    }

    void notifyAppResumed(boolean wasStopped) {
        this.mWindowContainerController.notifyAppResumed(wasStopped);
    }

    void notifyUnknownVisibilityLaunched() {
        this.mWindowContainerController.notifyUnknownVisibilityLaunched();
    }

    boolean shouldBeVisibleIgnoringKeyguard(boolean behindTranslucentActivity, boolean stackVisibleBehind, ActivityRecord visibleBehind, boolean behindFullscreenActivity) {
        if (!okToShowLocked()) {
            return false;
        }
        boolean activityVisibleBehind = (behindTranslucentActivity || stackVisibleBehind) && visibleBehind == this;
        boolean isVisible = (!behindFullscreenActivity || this.mLaunchTaskBehind) ? true : activityVisibleBehind;
        if (this.service.mSupportsLeanbackOnly && isVisible && isRecentsActivity()) {
            isVisible = this.mStackSupervisor.getStack(3) == null ? this.mStackSupervisor.isFocusedStack(getStack()) : true;
        }
        return isVisible;
    }

    void makeVisibleIfNeeded(ActivityRecord starting) {
        if (this.state == ActivityState.RESUMED || this == starting) {
            if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.d(TAG_VISIBILITY, "Not making visible, r=" + this + " state=" + this.state + " starting=" + starting);
            }
            return;
        }
        Slog.v(TAG_VISIBILITY, "Making visible and scheduling visibility: " + this);
        ActivityStack stack = getStack();
        try {
            if (stack.mTranslucentActivityWaiting != null) {
                updateOptionsLocked(this.returningOptions);
                stack.mUndrawnActivitiesBelowTopTranslucent.add(this);
            }
            setVisible(true);
            this.sleeping = false;
            this.app.pendingUiClean = true;
            this.app.thread.scheduleWindowVisibility(this.appToken, true);
            this.mStackSupervisor.mStoppingActivities.remove(this);
            this.mStackSupervisor.mGoingToSleepActivities.remove(this);
        } catch (Exception e) {
            Slog.w(TAG, "Exception thrown making visibile: " + this.intent.getComponent(), e);
        }
        handleAlreadyVisible();
    }

    boolean handleAlreadyVisible() {
        stopFreezingScreenLocked(false);
        try {
            if (this.returningOptions != null) {
                this.app.thread.scheduleOnNewActivityOptions(this.appToken, this.returningOptions.toBundle());
            }
        } catch (RemoteException e) {
        }
        if (this.state == ActivityState.RESUMED) {
            return true;
        }
        return false;
    }

    static void activityResumedLocked(IBinder token) {
        ActivityRecord r = forTokenLocked(token);
        if (ActivityManagerDebugConfig.DEBUG_SAVED_STATE) {
            Slog.i(TAG_STATES, "Resumed activity; dropping state of: " + r);
        }
        if (r != null) {
            r.icicle = null;
            r.haveState = false;
        }
    }

    void completeResumeLocked() {
        boolean wasVisible = this.visible;
        this.visible = true;
        if (!wasVisible) {
            this.mStackSupervisor.mAppVisibilitiesChangedSinceLastPause = true;
        }
        this.idle = false;
        this.results = null;
        this.newIntents = null;
        this.stopped = false;
        if (isHomeActivity()) {
            ProcessRecord app = ((ActivityRecord) this.task.mActivities.get(0)).app;
            if (!(app == null || app == this.service.mHomeProcess)) {
                this.service.mHomeProcess = app;
                this.service.reportHomeProcess(this.service.mHomeProcess);
            }
        }
        if (this.nowVisible) {
            this.mStackSupervisor.reportActivityVisibleLocked(this);
        }
        this.mStackSupervisor.scheduleIdleTimeoutLocked(this);
        this.mStackSupervisor.reportResumedActivityLocked(this);
        this.service.setFocusedActivityLockedForNavi(this);
        resumeKeyDispatchingLocked();
        ActivityStack stack = getStack();
        stack.mNoAnimActivities.clear();
        stack.setSoundEffectState(false, this.packageName, true, null);
        if (this.app != null) {
            this.cpuTimeAtResume = this.service.mProcessCpuTracker.getCpuTimeForPid(this.app.pid);
        } else {
            this.cpuTimeAtResume = 0;
        }
        this.returningOptions = null;
        if (stack.getVisibleBehindActivity() == this) {
            stack.setVisibleBehindActivity(null);
        }
        this.mStackSupervisor.checkReadyForSleepLocked();
        Flog.i(101, "completedResumed: " + this + ", launchTrack: " + this.mStackSupervisor.mActivityLaunchTrack);
        this.mStackSupervisor.mActivityLaunchTrack = "";
    }

    final void activityStoppedLocked(Bundle newIcicle, PersistableBundle newPersistentState, CharSequence description) {
        ActivityStack stack = getStack();
        if (this.state != ActivityState.STOPPING) {
            Slog.i(TAG, "Activity reported stop, but no longer stopping: " + this);
            stack.mHandler.removeMessages(104, this);
            return;
        }
        if (newPersistentState != null) {
            this.persistentState = newPersistentState;
            this.service.notifyTaskPersisterLocked(this.task, false);
        }
        if (ActivityManagerDebugConfig.DEBUG_SAVED_STATE) {
            Slog.i(TAG_SAVED_STATE, "Saving icicle of " + this + ": " + this.icicle);
        }
        if (newIcicle != null) {
            this.icicle = newIcicle;
            this.haveState = true;
            this.launchCount = 0;
            updateThumbnailLocked(null, description);
        }
        if (!this.stopped) {
            if (ActivityManagerDebugConfig.DEBUG_STATES) {
                Slog.v(TAG_STATES, "Moving to STOPPED: " + this + " (stop complete)");
            }
            stack.mHandler.removeMessages(104, this);
            this.stopped = true;
            this.state = ActivityState.STOPPED;
            this.mWindowContainerController.notifyAppStopped();
            if (stack.getVisibleBehindActivity() == this) {
                this.mStackSupervisor.requestVisibleBehindLocked(this, false);
            }
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

    void startLaunchTickingLocked() {
        if (!ActivityManagerService.IS_USER_BUILD && this.launchTickTime == 0) {
            this.launchTickTime = SystemClock.uptimeMillis();
            continueLaunchTickingLocked();
        }
    }

    boolean continueLaunchTickingLocked() {
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

    void finishLaunchTickingLocked() {
        this.launchTickTime = 0;
        ActivityStack stack = getStack();
        if (stack != null) {
            stack.mHandler.removeMessages(103);
        }
    }

    public boolean mayFreezeScreenLocked(ProcessRecord app) {
        return (app == null || (app.crashing ^ 1) == 0) ? false : app.notResponding ^ 1;
    }

    public void startFreezingScreenLocked(ProcessRecord app, int configChanges) {
        if (mayFreezeScreenLocked(app) && this.mWindowContainerController != null) {
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

    public void reportFullyDrawnLocked() {
        long curTime = SystemClock.uptimeMillis();
        if (this.displayStartTime == 0 && this.task != null && this.task.isLaunching) {
            this.displayStartTime = curTime;
        }
        if (this.displayStartTime != 0) {
            reportLaunchTimeLocked(curTime);
        } else {
            Jlog.warmLaunchingAppEnd(this.shortComponentName);
        }
        ActivityStack stack = getStack();
        if (!(this.fullyDrawnStartTime == 0 || stack == null)) {
            long thisTime = curTime - this.fullyDrawnStartTime;
            long totalTime = stack.mFullyDrawnStartTime != 0 ? curTime - stack.mFullyDrawnStartTime : thisTime;
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
            Log.i(TAG, sb.toString());
            int i = (totalTime > 0 ? 1 : (totalTime == 0 ? 0 : -1));
            stack.mFullyDrawnStartTime = 0;
        }
        this.fullyDrawnStartTime = 0;
    }

    private void reportLaunchTimeLocked(long curTime) {
        ActivityStack stack = getStack();
        if (stack != null) {
            long thisTime = curTime - this.displayStartTime;
            long totalTime = stack.mLaunchStartTime != 0 ? curTime - stack.mLaunchStartTime : thisTime;
            Trace.asyncTraceEnd(64, "launching: " + this.packageName, 0);
            EventLog.writeEvent(EventLogTags.AM_ACTIVITY_LAUNCH_TIME, new Object[]{Integer.valueOf(this.userId), Integer.valueOf(System.identityHashCode(this)), this.shortComponentName, Long.valueOf(thisTime), Long.valueOf(totalTime)});
            if (stack.mshortComponentName.equals(this.shortComponentName)) {
                Jlog.d(44, this.shortComponentName, (int) thisTime, "");
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
            Log.i(TAG, sb.toString());
            this.mStackSupervisor.reportActivityLaunchedLocked(false, this, thisTime, totalTime);
            int i = (totalTime > 0 ? 1 : (totalTime == 0 ? 0 : -1));
            this.displayStartTime = 0;
            this.task.isLaunching = false;
            stack.mLaunchStartTime = 0;
        }
    }

    public void onStartingWindowDrawn(long timestamp) {
        synchronized (this.service) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                this.mStackSupervisor.mActivityMetricsLogger.notifyStartingWindowDrawn(getStackId(), timestamp);
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void onWindowsDrawn(long timestamp) {
        synchronized (this.service) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                this.mStackSupervisor.mActivityMetricsLogger.notifyWindowsDrawn(getStackId(), timestamp);
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
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public void onWindowsVisible() {
        synchronized (this.service) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                this.mStackSupervisor.reportActivityVisibleLocked(this);
                if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                    Log.v(TAG_SWITCH, "windowsVisibleLocked(): " + this);
                }
                if (!this.nowVisible) {
                    this.nowVisible = true;
                    this.lastVisibleTime = SystemClock.uptimeMillis();
                    if (this.idle || this.mStackSupervisor.isStoppingNoHistoryActivity()) {
                        int size = this.mStackSupervisor.mActivitiesWaitingForVisibleActivity.size();
                        if (size > 0) {
                            for (int i = 0; i < size; i++) {
                                ActivityRecord r = (ActivityRecord) this.mStackSupervisor.mActivitiesWaitingForVisibleActivity.get(i);
                                if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                                    Log.v(TAG_SWITCH, "Was waiting for visible: " + r);
                                }
                            }
                            this.mStackSupervisor.mActivitiesWaitingForVisibleActivity.clear();
                            this.mStackSupervisor.scheduleIdleLocked();
                        }
                    } else {
                        this.mStackSupervisor.processStoppingActivitiesLocked(null, false, true);
                    }
                    this.service.scheduleAppGcsLocked();
                }
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
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
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    public boolean keyDispatchingTimedOut(String reason, int windowPid) {
        ActivityRecord anrActivity;
        ProcessRecord anrApp;
        boolean windowFromSameProcessAsActivity;
        boolean z = false;
        synchronized (this.service) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                anrActivity = getWaitingHistoryRecordLocked();
                anrApp = this.app;
                windowFromSameProcessAsActivity = this.app == null || this.app.pid == windowPid || windowPid == -1;
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
        if (windowFromSameProcessAsActivity) {
            return this.service.inputDispatchingTimedOut(anrApp, anrActivity, this, false, reason);
        }
        if (this.service.inputDispatchingTimedOut(windowPid, false, reason) < 0) {
            z = true;
        }
        return z;
    }

    private ActivityRecord getWaitingHistoryRecordLocked() {
        if (this.mStackSupervisor.mActivitiesWaitingForVisibleActivity.contains(this) || this.stopped) {
            ActivityStack stack = this.mStackSupervisor.getFocusedStack();
            ActivityRecord r = stack.mResumedActivity;
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
        if ((this.info.flags & 1024) != 0) {
            return true;
        }
        if (this.mStackSupervisor.isCurrentProfileLocked(this.userId)) {
            return this.service.mUserController.isUserStoppingOrShuttingDownLocked(this.userId) ^ 1;
        }
        return false;
    }

    public boolean isInterestingToUserLocked() {
        if (this.visible || this.nowVisible || this.state == ActivityState.PAUSING || this.state == ActivityState.RESUMED) {
            return true;
        }
        return false;
    }

    void setSleeping(boolean _sleeping) {
        setSleeping(_sleeping, false);
    }

    void setSleeping(boolean _sleeping, boolean force) {
        if (!((!force && this.sleeping == _sleeping) || this.app == null || this.app.thread == null)) {
            try {
                this.app.thread.scheduleSleeping(this.appToken, _sleeping);
                if (_sleeping && (this.mStackSupervisor.mGoingToSleepActivities.contains(this) ^ 1) != 0) {
                    this.mStackSupervisor.mGoingToSleepActivities.add(this);
                }
                this.sleeping = _sleeping;
            } catch (RemoteException e) {
                Slog.w(TAG, "Exception thrown when sleeping: " + this.intent.getComponent(), e);
            }
        }
    }

    static int getTaskForActivityLocked(IBinder token, boolean onlyRoot) {
        ActivityRecord r = forTokenLocked(token);
        if (r == null) {
            return -1;
        }
        TaskRecord task = r.task;
        int activityNdx = task.mActivities.indexOf(r);
        if (activityNdx < 0 || (onlyRoot && activityNdx > task.findEffectiveRootIndex())) {
            return -1;
        }
        return task.taskId;
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

    protected int getDisplayId() {
        ActivityStack stack = getStack();
        if (stack == null) {
            return -1;
        }
        return stack.mDisplayId;
    }

    /* JADX WARNING: Missing block: B:13:0x0020, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    final boolean isDestroyable() {
        if (this.finishing || this.app == null || this.state == ActivityState.DESTROYING || this.state == ActivityState.DESTROYED) {
            return false;
        }
        ActivityStack stack = getStack();
        if (stack == null || this == stack.mResumedActivity || this == stack.mPausingActivity || (this.haveState ^ 1) != 0 || (this.stopped ^ 1) != 0 || this.visible) {
            return false;
        }
        return true;
    }

    private static String createImageFilename(long createTime, int taskId) {
        return String.valueOf(taskId) + ACTIVITY_ICON_SUFFIX + createTime + ".png";
    }

    void setTaskDescription(TaskDescription _taskDescription) {
        if (_taskDescription.getIconFilename() == null) {
            Bitmap icon = _taskDescription.getIcon();
            if (icon != null) {
                String iconFilePath = new File(TaskPersister.getUserImagesDir(this.task.userId), createImageFilename(this.createTime, this.task.taskId)).getAbsolutePath();
                this.service.mRecentTasks.saveImage(icon, iconFilePath);
                _taskDescription.setIconFilename(iconFilePath);
            }
        }
        this.taskDescription = _taskDescription;
    }

    void setVoiceSessionLocked(IVoiceInteractionSession session) {
        this.voiceSession = session;
        this.pendingVoiceInteractionStart = false;
    }

    void clearVoiceSessionLocked() {
        this.voiceSession = null;
        this.pendingVoiceInteractionStart = false;
    }

    void showStartingWindow(ActivityRecord prev, boolean newTask, boolean taskSwitch) {
        showStartingWindow(prev, newTask, taskSwitch, false);
    }

    void showStartingWindow(ActivityRecord prev, boolean newTask, boolean taskSwitch, boolean fromRecents) {
        if (this.mWindowContainerController != null && !this.mTaskOverlay) {
            CompatibilityInfo compatInfo = this.service.compatibilityInfoForPackageLocked(this.info.applicationInfo);
            AppWindowContainerController appWindowContainerController = this.mWindowContainerController;
            String str = this.packageName;
            int i = this.theme;
            CharSequence charSequence = this.nonLocalizedLabel;
            int i2 = this.labelRes;
            int i3 = this.icon;
            int i4 = this.logo;
            int i5 = this.windowFlags;
            IBinder iBinder = prev != null ? prev.appToken : null;
            boolean isProcessRunning = isProcessRunning();
            boolean allowTaskSnapshot = allowTaskSnapshot();
            boolean z = this.state.ordinal() >= ActivityState.RESUMED.ordinal() && this.state.ordinal() <= ActivityState.STOPPED.ordinal();
            if (appWindowContainerController.addStartingWindow(str, i, compatInfo, charSequence, i2, i3, i4, i5, iBinder, newTask, taskSwitch, isProcessRunning, allowTaskSnapshot, z, fromRecents)) {
                this.mStartingWindowState = 1;
            }
        }
    }

    void removeOrphanedStartingWindow(boolean behindFullscreenActivity) {
        if (this.mStartingWindowState == 1 && behindFullscreenActivity) {
            if (ActivityManagerDebugConfig.DEBUG_VISIBILITY) {
                Slog.w(TAG_VISIBILITY, "Found orphaned starting window " + this);
            }
            this.mStartingWindowState = 2;
            this.mWindowContainerController.removeStartingWindow();
        }
    }

    int getRequestedOrientation() {
        return this.mWindowContainerController.getOrientation();
    }

    void setRequestedOrientation(int requestedOrientation) {
        if (ActivityInfo.isFixedOrientation(requestedOrientation) && (this.fullscreen ^ 1) != 0 && this.appInfo.targetSdkVersion > 26) {
            throw new IllegalStateException("Only fullscreen activities can request orientation");
        } else if (this.mWindowContainerController != null) {
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

    void setDisablePreviewScreenshots(boolean disable) {
        if (this.mWindowContainerController != null) {
            this.mWindowContainerController.setDisablePreviewScreenshots(disable);
        }
    }

    void setLastReportedGlobalConfiguration(Configuration config) {
        this.mLastReportedConfiguration.setGlobalConfiguration(config);
    }

    void setLastReportedConfiguration(MergedConfiguration config) {
        this.mLastReportedConfiguration.setTo(config);
    }

    void onOverrideConfigurationSent() {
        this.mLastReportedConfiguration.setOverrideConfiguration(getMergedOverrideConfiguration());
    }

    void onOverrideConfigurationChanged(Configuration newConfig) {
        Configuration currentConfig = getOverrideConfiguration();
        int appBoundsChanged;
        if (currentConfig.appBounds == null || newConfig == null) {
            appBoundsChanged = 0;
        } else {
            appBoundsChanged = currentConfig.appBounds.equals(newConfig.appBounds) ^ 1;
        }
        if (!currentConfig.equals(newConfig) || (appBoundsChanged ^ 1) == 0) {
            super.onOverrideConfigurationChanged(newConfig);
            if (this.mWindowContainerController != null) {
                this.mWindowContainerController.onOverrideConfigurationChanged(newConfig, this.mBounds);
                onOverrideConfigurationSent();
            }
        }
    }

    private void updateOverrideConfiguration() {
        this.mTmpConfig.unset();
        computeBounds(this.mTmpBounds);
        int isInMWPortraitWhiteList = 0;
        if (!(this.task == null || (this.task.mFullscreen ^ 1) == 0)) {
            int isValidExtDisplayId;
            if (HwPCUtils.isPcCastModeInServer()) {
                isValidExtDisplayId = HwPCUtils.isValidExtDisplayId(getDisplayId());
            } else {
                isValidExtDisplayId = 0;
            }
            if ((isValidExtDisplayId ^ 1) != 0) {
                ActivityRecord topActivity = this.task.getTopActivity();
                if (topActivity != null) {
                    isInMWPortraitWhiteList = this.service.getPackageManagerInternalLocked().isInMWPortraitWhiteList(topActivity.packageName);
                }
            }
        }
        if (!this.mTmpBounds.equals(this.mBounds) || (isInMWPortraitWhiteList ^ 1) == 0) {
            this.mBounds.set(this.mTmpBounds);
            if (!(this.mBounds.isEmpty() && isInMWPortraitWhiteList == 0)) {
                this.task.computeOverrideConfiguration(this.mTmpConfig, this.mBounds, null, false, false);
            }
            if (this.mBounds.isEmpty()) {
                this.mTmpConfig.nonFullScreen = 0;
            } else {
                this.mTmpConfig.nonFullScreen = 1;
            }
            onOverrideConfigurationChanged(this.mTmpConfig);
            return;
        }
        ActivityStack stack = getStack();
        if (this.mBounds.isEmpty() && this.task != null && stack != null && (this.task.mFullscreen ^ 1) == 0) {
            ActivityRecord top = this.mStackSupervisor.topRunningActivityLocked();
            Configuration parentConfig = getParent().getConfiguration();
            if (top != this || isConfigurationCompatible(parentConfig)) {
                onOverrideConfigurationChanged(this.mTmpConfig);
            } else if (isConfigurationCompatible(this.mLastReportedConfiguration.getMergedConfiguration())) {
                onOverrideConfigurationChanged(this.mLastReportedConfiguration.getMergedConfiguration());
            }
        }
    }

    boolean isConfigurationCompatible(Configuration config) {
        int orientation = this.mWindowContainerController != null ? this.mWindowContainerController.getOrientation() : this.info.screenOrientation;
        if (!ActivityInfo.isFixedOrientationPortrait(orientation) || config.orientation == 1) {
            return !ActivityInfo.isFixedOrientationLandscape(orientation) || config.orientation == 2;
        } else {
            return false;
        }
    }

    protected void computeBounds(Rect outBounds) {
        outBounds.setEmpty();
        float maxAspectRatio = this.info.maxAspectRatio;
        float userMaxAspectRatio = 0.0f;
        if (!(this.service == null || (TextUtils.isEmpty(this.packageName) ^ 1) == 0)) {
            userMaxAspectRatio = this.service.getPackageManagerInternalLocked().getUserMaxAspectRatio(this.packageName);
        }
        if (userMaxAspectRatio != 0.0f) {
            if (userMaxAspectRatio >= PackageParser.getScreenAspectRatio() || ((double) this.info.originMaxAspectRatio) <= 1.0d) {
                maxAspectRatio = userMaxAspectRatio;
            } else {
                maxAspectRatio = this.info.originMaxAspectRatio;
            }
        }
        ActivityStack stack = getStack();
        if (this.task != null && stack != null && (this.task.mFullscreen ^ 1) == 0 && maxAspectRatio != 0.0f) {
            Configuration configuration = getParent().getConfiguration();
            int containingAppWidth = configuration.appBounds.width();
            int containingAppHeight = configuration.appBounds.height();
            int maxActivityWidth = containingAppWidth;
            int maxActivityHeight = containingAppHeight;
            if (containingAppWidth < containingAppHeight) {
                maxActivityHeight = (int) ((((float) containingAppWidth) * maxAspectRatio) + 0.5f);
            } else {
                maxActivityWidth = (int) ((((float) containingAppHeight) * maxAspectRatio) + 0.5f);
            }
            if (containingAppWidth > maxActivityWidth || containingAppHeight > maxActivityHeight) {
                outBounds.set(0, 0, maxActivityWidth, maxActivityHeight);
            } else {
                outBounds.set(this.mBounds);
            }
        }
    }

    boolean ensureActivityConfigurationLocked(int globalChanges, boolean preserveWindow) {
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
        } else if ((this.state == ActivityState.STOPPING || this.state == ActivityState.STOPPED) && (isSplitBaseActivity() ^ 1) != 0) {
            if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                Slog.v(TAG_CONFIGURATION, "Skipping config check stopped or stopping: " + this);
            }
            return true;
        } else if (stack.shouldBeVisible(null) == 0) {
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
            if (!getConfiguration().equals(this.mTmpConfig) || (this.forceNewConfig ^ 1) == 0 || (displayChanged ^ 1) == 0) {
                int changes = getConfigurationChanges(this.mTmpConfig);
                Configuration newMergedOverrideConfig = getMergedOverrideConfiguration();
                this.mLastReportedConfiguration.setConfiguration(this.service.getGlobalConfiguration(), newMergedOverrideConfig);
                if (changes != 0 || (this.forceNewConfig ^ 1) == 0) {
                    if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                        Slog.v(TAG_CONFIGURATION, "Configuration changes for " + this + ", allChanges=" + Configuration.configurationDiffToString(changes));
                    }
                    if (this.app == null || this.app.thread == null) {
                        if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                            Slog.v(TAG_CONFIGURATION, "Configuration doesn't matter not running " + this);
                        }
                        stopFreezingScreenLocked(false);
                        this.forceNewConfig = false;
                        return true;
                    }
                    if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                        Slog.v(TAG_CONFIGURATION, "Checking to restart " + this.info.name + ": changed=0x" + Integer.toHexString(changes) + ", handles=0x" + Integer.toHexString(this.info.getRealConfigChanged()) + ", mLastReportedConfiguration=" + this.mLastReportedConfiguration);
                    }
                    if (shouldRelaunchLocked(changes, this.mTmpConfig) || this.forceNewConfig) {
                        this.configChangeFlags |= changes;
                        startFreezingScreenLocked(this.app, globalChanges);
                        this.forceNewConfig = false;
                        preserveWindow &= isResizeOnlyChange(changes);
                        if (this.app == null || this.app.thread == null) {
                            if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                                Slog.v(TAG_CONFIGURATION, "Config is destroying non-running " + this);
                            }
                            stack.destroyActivityLocked(this, true, "config");
                        } else if (this.state == ActivityState.PAUSING) {
                            if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                                Slog.v(TAG_CONFIGURATION, "Config is skipping already pausing " + this);
                            }
                            this.deferRelaunchUntilPaused = true;
                            this.preserveWindowOnDeferredRelaunch = preserveWindow;
                            return true;
                        } else if (this.state == ActivityState.RESUMED) {
                            Slog.v(TAG_CONFIGURATION, "Config is relaunching resumed " + this);
                            if (ActivityManagerDebugConfig.DEBUG_STATES && (this.visible ^ 1) != 0) {
                                Slog.v(TAG_STATES, "Config is relaunching resumed invisible activity " + this + " called by " + Debug.getCallers(4));
                            }
                            relaunchActivityLocked(true, preserveWindow);
                        } else {
                            Slog.v(TAG_CONFIGURATION, "Config is relaunching non-resumed " + this);
                            relaunchActivityLocked(false, preserveWindow);
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
                if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                    Slog.v(TAG_CONFIGURATION, "Configuration no differences in " + this);
                }
                if (displayChanged) {
                    scheduleActivityMovedToDisplay(newDisplayId, newMergedOverrideConfig);
                } else {
                    scheduleConfigurationChanged(newMergedOverrideConfig);
                }
                return true;
            }
            if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                Slog.v(TAG_CONFIGURATION, "Configuration & display unchanged in " + this);
            }
            return true;
        }
    }

    private boolean shouldRelaunchLocked(int changes, Configuration changesConfig) {
        int configChanged = overrideRealConfigChanged(this.info);
        boolean onlyVrUiModeChanged = onlyVrUiModeChanged(changes, changesConfig);
        if (this.appInfo.targetSdkVersion < 26 && this.requestedVrComponent != null && onlyVrUiModeChanged) {
            configChanged |= 512;
        }
        if (((~configChanged) & changes) != 0) {
            return true;
        }
        return false;
    }

    private boolean onlyVrUiModeChanged(int changes, Configuration lastReportedConfig) {
        Configuration currentConfig = getConfiguration();
        if (changes != 512 || isInVrUiMode(currentConfig) == isInVrUiMode(lastReportedConfig)) {
            return false;
        }
        return true;
    }

    protected int getConfigurationChanges(Configuration lastReportedConfig) {
        Configuration currentConfig = getConfiguration();
        int changes = lastReportedConfig.diff(currentConfig);
        if ((changes & 1024) != 0) {
            boolean crosses;
            if (crossesHorizontalSizeThreshold(lastReportedConfig.screenWidthDp, currentConfig.screenWidthDp)) {
                crosses = true;
            } else {
                crosses = crossesVerticalSizeThreshold(lastReportedConfig.screenHeightDp, currentConfig.screenHeightDp);
            }
            if (!crosses) {
                changes &= -1025;
            }
        }
        if ((changes & 2048) == 0 || crossesSmallestSizeThreshold(lastReportedConfig.smallestScreenWidthDp, currentConfig.smallestScreenWidthDp)) {
            return changes;
        }
        return changes & -2049;
    }

    private static boolean isResizeOnlyChange(int change) {
        return (change & -3457) == 0;
    }

    void relaunchActivityLocked(boolean andResume, boolean preserveWindow) {
        if (this.service.mSuppressResizeConfigChanges && preserveWindow) {
            this.configChangeFlags = 0;
            return;
        }
        int i;
        List pendingResults = null;
        List pendingNewIntents = null;
        if (andResume) {
            pendingResults = this.results;
            pendingNewIntents = this.newIntents;
        }
        if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
            Slog.v(TAG_SWITCH, "Relaunching: " + this + " with results=" + pendingResults + " newIntents=" + pendingNewIntents + " andResume=" + andResume + " preserveWindow=" + preserveWindow);
        }
        if (andResume) {
            i = EventLogTags.AM_RELAUNCH_RESUME_ACTIVITY;
        } else {
            i = EventLogTags.AM_RELAUNCH_ACTIVITY;
        }
        EventLog.writeEvent(i, new Object[]{Integer.valueOf(this.userId), Integer.valueOf(System.identityHashCode(this)), Integer.valueOf(this.task.taskId), this.shortComponentName});
        startFreezingScreenLocked(this.app, 0);
        this.mStackSupervisor.removeChildActivityContainers(this);
        try {
            if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_STATES) {
                Slog.i(TAG_SWITCH, "Moving to " + (andResume ? "RESUMED" : "PAUSED") + " Relaunching " + this + " callers=" + Debug.getCallers(6));
            }
            this.forceNewConfig = false;
            this.mStackSupervisor.activityRelaunchingLocked(this);
            this.app.thread.scheduleRelaunchActivity(this.appToken, pendingResults, pendingNewIntents, this.configChangeFlags, andResume ^ 1, new Configuration(this.service.getGlobalConfiguration()), new Configuration(getMergedOverrideConfiguration()), preserveWindow);
        } catch (RemoteException e) {
            if (ActivityManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_STATES) {
                Slog.i(TAG_SWITCH, "Relaunch failed", e);
            }
        }
        if (andResume) {
            if (ActivityManagerDebugConfig.DEBUG_STATES) {
                Slog.d(TAG_STATES, "Resumed after relaunch " + this);
            }
            this.results = null;
            this.newIntents = null;
            this.service.showUnsupportedZoomDialogIfNeededLocked(this);
            this.service.showAskCompatModeDialogLocked(this);
        } else {
            this.service.mHandler.removeMessages(101, this);
            this.state = ActivityState.PAUSED;
            if (this.stopped) {
                getStack().addToStopping(this, true, false);
            }
        }
        this.configChangeFlags = 0;
        this.deferRelaunchUntilPaused = false;
        this.preserveWindowOnDeferredRelaunch = false;
    }

    private boolean isProcessRunning() {
        ProcessRecord proc = this.app;
        if (proc == null) {
            proc = (ProcessRecord) this.service.mProcessNames.get(this.processName, this.info.applicationInfo.uid);
        }
        if (proc == null || proc.thread == null) {
            return false;
        }
        return true;
    }

    private boolean allowTaskSnapshot() {
        if (this.newIntents == null) {
            return true;
        }
        for (int i = this.newIntents.size() - 1; i >= 0; i--) {
            Intent intent = (Intent) this.newIntents.get(i);
            if (intent != null && (isMainIntent(intent) ^ 1) != 0) {
                return false;
            }
        }
        return true;
    }

    boolean isNoHistory() {
        return ((this.intent.getFlags() & 1073741824) == 0 && (this.info.flags & 128) == 0) ? false : true;
    }

    void saveToXml(XmlSerializer out) throws IOException, XmlPullParserException {
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

    /* JADX WARNING: Removed duplicated region for block: B:43:0x0134  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x011a  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static ActivityRecord restoreFromXml(XmlPullParser in, ActivityStackSupervisor stackSupervisor) throws IOException, XmlPullParserException {
        Intent intent = null;
        PersistableBundle persistentState = null;
        int launchedFromUid = 0;
        String launchedFromPackage = null;
        String resolvedType = null;
        boolean componentSpecified = false;
        int userId = 0;
        long createTime = -1;
        int outerDepth = in.getDepth();
        TaskDescription taskDescription = new TaskDescription();
        for (int attrNdx = in.getAttributeCount() - 1; attrNdx >= 0; attrNdx--) {
            String attrName = in.getAttributeName(attrNdx);
            String attrValue = in.getAttributeValue(attrNdx);
            if (ATTR_ID.equals(attrName)) {
                createTime = Long.parseLong(attrValue);
            } else if (ATTR_LAUNCHEDFROMUID.equals(attrName)) {
                launchedFromUid = Integer.parseInt(attrValue);
            } else if (ATTR_LAUNCHEDFROMPACKAGE.equals(attrName)) {
                launchedFromPackage = attrValue;
            } else if (ATTR_RESOLVEDTYPE.equals(attrName)) {
                resolvedType = attrValue;
            } else if (ATTR_COMPONENTSPECIFIED.equals(attrName)) {
                componentSpecified = Boolean.parseBoolean(attrValue);
            } else if (ATTR_USERID.equals(attrName)) {
                userId = Integer.parseInt(attrValue);
            } else {
                if (attrName.startsWith("task_description_")) {
                    taskDescription.restoreFromXml(attrName, attrValue);
                } else {
                    Log.d(TAG, "Unknown ActivityRecord attribute=" + attrName);
                }
            }
        }
        while (true) {
            int event = in.next();
            if (event == 1 || (event == 3 && in.getDepth() < outerDepth)) {
                if (intent != null) {
                    throw new XmlPullParserException("restoreActivity error intent=" + intent);
                }
                ActivityManagerService service = stackSupervisor.mService;
                ActivityInfo aInfo = stackSupervisor.resolveActivity(intent, resolvedType, 0, null, userId);
                if (aInfo == null) {
                    throw new XmlPullParserException("restoreActivity resolver error. Intent=" + intent + " resolvedType=" + resolvedType);
                }
                ActivityRecord r = HwServiceFactory.createActivityRecord(service, null, 0, launchedFromUid, launchedFromPackage, intent, resolvedType, aInfo, service.getConfiguration(), null, null, 0, componentSpecified, false, stackSupervisor, null, null, null);
                r.persistentState = persistentState;
                r.taskDescription = taskDescription;
                r.createTime = createTime;
                return r;
            } else if (event == 2) {
                String name = in.getName();
                if ("intent".equals(name)) {
                    intent = Intent.restoreFromXml(in);
                } else if (TAG_PERSISTABLEBUNDLE.equals(name)) {
                    persistentState = PersistableBundle.restoreFromXml(in);
                } else {
                    Slog.w(TAG, "restoreActivity: unexpected name=" + name);
                    XmlUtils.skipCurrentTag(in);
                }
            }
        }
        if (intent != null) {
        }
    }

    private static String activityTypeToString(int type) {
        switch (type) {
            case 0:
                return "APPLICATION_ACTIVITY_TYPE";
            case 1:
                return "HOME_ACTIVITY_TYPE";
            case 2:
                return "RECENTS_ACTIVITY_TYPE";
            case 3:
                return "ASSISTANT_ACTIVITY_TYPE";
            default:
                return Integer.toString(type);
        }
    }

    private static boolean isInVrUiMode(Configuration config) {
        return (config.uiMode & 15) == 7;
    }

    int getUid() {
        return this.info.applicationInfo.uid;
    }

    public String toString() {
        if (this.stringName != null) {
            return this.stringName + " t" + (this.task == null ? -1 : this.task.taskId) + (this.finishing ? " f}" : "}");
        }
        StringBuilder sb = new StringBuilder(128);
        sb.append("ActivityRecord{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(" u");
        sb.append(this.userId);
        sb.append(' ');
        sb.append(this.intent.getComponent().flattenToShortString());
        this.stringName = sb.toString();
        return toString();
    }
}
