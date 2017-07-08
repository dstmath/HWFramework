package com.android.server.am;

import android.app.ActivityManager.StackId;
import android.app.ActivityManager.TaskDescription;
import android.app.ActivityOptions;
import android.app.IApplicationThread;
import android.app.PendingIntent;
import android.app.ResultInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
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
import android.util.EventLog;
import android.util.Flog;
import android.util.Jlog;
import android.util.Log;
import android.util.Slog;
import android.util.TimeUtils;
import android.view.AppTransitionAnimationSpec;
import android.view.IApplicationToken.Stub;
import com.android.internal.R;
import com.android.internal.app.ResolverActivity;
import com.android.internal.content.ReferrerIntent;
import com.android.internal.util.XmlUtils;
import com.android.server.AttributeCache;
import com.android.server.AttributeCache.Entry;
import com.android.server.HwServiceFactory;
import com.android.server.am.ActivityStackSupervisor.ActivityContainer;
import com.android.server.wm.WindowManagerService;
import com.android.server.wm.WindowManagerService.H;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class ActivityRecord extends AbsActivityRecord {
    static final String ACTIVITY_ICON_SUFFIX = "_activity_icon_";
    static final int APPLICATION_ACTIVITY_TYPE = 0;
    private static final String ATTR_COMPONENTSPECIFIED = "component_specified";
    private static final String ATTR_ID = "id";
    private static final String ATTR_LAUNCHEDFROMPACKAGE = "launched_from_package";
    private static final String ATTR_LAUNCHEDFROMUID = "launched_from_uid";
    private static final String ATTR_RESOLVEDTYPE = "resolved_type";
    private static final String ATTR_USERID = "user_id";
    static final int HOME_ACTIVITY_TYPE = 1;
    static final int RECENTS_ACTIVITY_TYPE = 2;
    public static final String RECENTS_PACKAGE_NAME = "com.android.systemui.recent";
    private static final boolean SHOW_ACTIVITY_START_TIME = true;
    static final int STARTING_WINDOW_NOT_SHOWN = 0;
    static final int STARTING_WINDOW_REMOVED = 2;
    static final int STARTING_WINDOW_SHOWN = 1;
    private static final String TAG = null;
    private static final String TAG_INTENT = "intent";
    private static final String TAG_PERSISTABLEBUNDLE = "persistable_bundle";
    protected static final String TAG_STATES = null;
    private static final String TAG_SWITCH = null;
    private static final String TAG_THUMBNAILS = null;
    ProcessRecord app;
    final ApplicationInfo appInfo;
    AppTimeTracker appTimeTracker;
    final Stub appToken;
    CompatibilityInfo compat;
    final boolean componentSpecified;
    int configChangeFlags;
    Configuration configuration;
    HashSet<ConnectionRecord> connections;
    long cpuTimeAtResume;
    long createTime;
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
    int icon;
    boolean idle;
    boolean immersive;
    private boolean inHistory;
    public final ActivityInfo info;
    final Intent intent;
    boolean keysPaused;
    int labelRes;
    long lastLaunchTime;
    long lastVisibleTime;
    int launchCount;
    boolean launchFailed;
    int launchMode;
    long launchTickTime;
    final String launchedFromPackage;
    final int launchedFromUid;
    int logo;
    int mActivityType;
    ArrayList<ActivityContainer> mChildContainers;
    private int[] mHorizontalSizeConfigurations;
    ActivityContainer mInitialActivityContainer;
    boolean mLaunchTaskBehind;
    private int[] mSmallestSizeConfigurations;
    final ActivityStackSupervisor mStackSupervisor;
    int mStartingWindowState;
    boolean mTaskOverlay;
    boolean mUpdateTaskThumbnailWhenHidden;
    private int[] mVerticalSizeConfigurations;
    final int multiLaunchId;
    ArrayList<ReferrerIntent> newIntents;
    final boolean noDisplay;
    CharSequence nonLocalizedLabel;
    boolean nowVisible;
    final String packageName;
    long pauseTime;
    ActivityOptions pendingOptions;
    HashSet<WeakReference<PendingIntentRecord>> pendingResults;
    boolean pendingVoiceInteractionStart;
    PersistableBundle persistentState;
    boolean preserveWindowOnDeferredRelaunch;
    public final String processName;
    public final ComponentName realActivity;
    int realTheme;
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
    long startTime;
    ActivityState state;
    final boolean stateNotNeeded;
    boolean stopped;
    String stringName;
    TaskRecord task;
    final String taskAffinity;
    Configuration taskConfigOverride;
    TaskDescription taskDescription;
    int theme;
    public boolean translucent;
    UriPermissionOwner uriPermissions;
    final int userId;
    boolean visible;
    IVoiceInteractionSession voiceSession;
    int windowFlags;

    static class Token extends Stub {
        private final ActivityManagerService mService;
        private final WeakReference<ActivityRecord> weakActivity;

        Token(ActivityRecord activity, ActivityManagerService service) {
            this.weakActivity = new WeakReference(activity);
            this.mService = service;
        }

        public void windowsDrawn() {
            synchronized (this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    ActivityRecord r = tokenToActivityRecordLocked(this);
                    if (r != null) {
                        r.windowsDrawnLocked();
                    }
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        public void windowsVisible() {
            synchronized (this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    ActivityRecord r = tokenToActivityRecordLocked(this);
                    if (r != null) {
                        r.windowsVisibleLocked();
                    }
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        public void windowsGone() {
            synchronized (this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    ActivityRecord r = tokenToActivityRecordLocked(this);
                    if (r != null) {
                        if (ActivityManagerService.isInCallActivity(r)) {
                            Flog.i(H.KEYGUARD_DISMISS_DONE, "Incall is gone");
                            Jlog.d(131, "JLID_PHONE_INCALLUI_CLOSE_END");
                        }
                        Flog.i(H.KEYGUARD_DISMISS_DONE, "windowsGone(): " + r);
                        r.nowVisible = false;
                        return;
                    }
                    ActivityManagerService.resetPriorityAfterLockedSection();
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        public boolean keyDispatchingTimedOut(String reason) {
            synchronized (this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    ActivityRecord r = tokenToActivityRecordLocked(this);
                    if (r == null) {
                        return false;
                    }
                    ProcessRecord processRecord;
                    ActivityRecord anrActivity = r.getWaitingHistoryRecordLocked();
                    if (r != null) {
                        processRecord = r.app;
                    } else {
                        processRecord = null;
                    }
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    return this.mService.inputDispatchingTimedOut(processRecord, anrActivity, r, false, reason);
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        public long getKeyDispatchingTimeout() {
            synchronized (this.mService) {
                try {
                    ActivityManagerService.boostPriorityForLockedSection();
                    ActivityRecord r = tokenToActivityRecordLocked(this);
                    long j;
                    if (r == null) {
                        j = 0;
                        return j;
                    }
                    j = ActivityManagerService.getInputDispatchingTimeoutLocked(r.getWaitingHistoryRecordLocked());
                    ActivityManagerService.resetPriorityAfterLockedSection();
                    return j;
                } finally {
                    ActivityManagerService.resetPriorityAfterLockedSection();
                }
            }
        }

        private static final ActivityRecord tokenToActivityRecordLocked(Token token) {
            if (token == null) {
                return null;
            }
            ActivityRecord r = (ActivityRecord) token.weakActivity.get();
            if (r == null || r.task == null || r.task.stack == null) {
                return null;
            }
            return r;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(DumpState.DUMP_PACKAGES);
            sb.append("Token{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(' ');
            sb.append(this.weakActivity.get());
            sb.append('}');
            return sb.toString();
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.am.ActivityRecord.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.am.ActivityRecord.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.ActivityRecord.<clinit>():void");
    }

    private static String startingWindowStateToString(int state) {
        switch (state) {
            case STARTING_WINDOW_NOT_SHOWN /*0*/:
                return "STARTING_WINDOW_NOT_SHOWN";
            case STARTING_WINDOW_SHOWN /*1*/:
                return "STARTING_WINDOW_SHOWN";
            case STARTING_WINDOW_REMOVED /*2*/:
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
        pw.print(this.userId);
        pw.print(" multiLaunchId=");
        pw.println(this.multiLaunchId);
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
        pw.print(prefix);
        pw.print("config=");
        pw.println(this.configuration);
        pw.print(prefix);
        pw.print("taskConfigOverride=");
        pw.println(this.taskConfigOverride);
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
            if (iconFilename == null && this.taskDescription.getLabel() == null) {
                if (this.taskDescription.getPrimaryColor() != 0) {
                }
                if (iconFilename == null && this.taskDescription.getIcon() != null) {
                    pw.print(prefix);
                    pw.println("taskDescription contains Bitmap");
                }
            }
            pw.print(prefix);
            pw.print("taskDescription:");
            pw.print(" iconFilename=");
            pw.print(this.taskDescription.getIconFilename());
            pw.print(" label=\"");
            pw.print(this.taskDescription.getLabel());
            pw.print("\"");
            pw.print(" color=");
            pw.println(Integer.toHexString(this.taskDescription.getPrimaryColor()));
            pw.print(prefix);
            pw.println("taskDescription contains Bitmap");
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
                PendingIntentRecord pendingIntentRecord = wpir != null ? (PendingIntentRecord) wpir.get() : null;
                pw.print(prefix);
                pw.print("  - ");
                if (pendingIntentRecord == null) {
                    pw.println("null");
                } else {
                    pw.println(pendingIntentRecord);
                    pendingIntentRecord.dump(pw, prefix + "    ");
                }
            }
        }
        if (this.newIntents != null && this.newIntents.size() > 0) {
            pw.print(prefix);
            pw.println("Pending New Intents:");
            for (int i = STARTING_WINDOW_NOT_SHOWN; i < this.newIntents.size(); i += STARTING_WINDOW_SHOWN) {
                Intent intent = (Intent) this.newIntents.get(i);
                pw.print(prefix);
                pw.print("  - ");
                if (intent == null) {
                    pw.println("null");
                } else {
                    pw.println(intent.toShortString(SHOW_ACTIVITY_START_TIME, SHOW_ACTIVITY_START_TIME, false, SHOW_ACTIVITY_START_TIME));
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
        boolean waitingVisible = this.mStackSupervisor.mWaitingVisibleActivities.contains(this);
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
        }
    }

    public boolean crossesHorizontalSizeThreshold(int firstDp, int secondDp) {
        return crossesSizeThreshold(this.mHorizontalSizeConfigurations, firstDp, secondDp);
    }

    public boolean crossesVerticalSizeThreshold(int firstDp, int secondDp) {
        return crossesSizeThreshold(this.mVerticalSizeConfigurations, firstDp, secondDp);
    }

    public boolean crossesSmallestSizeThreshold(int firstDp, int secondDp) {
        return crossesSizeThreshold(this.mSmallestSizeConfigurations, firstDp, secondDp);
    }

    private static boolean crossesSizeThreshold(int[] thresholds, int firstDp, int secondDp) {
        if (thresholds == null) {
            return false;
        }
        for (int i = thresholds.length - 1; i >= 0; i--) {
            int threshold = thresholds[i];
            if ((firstDp < threshold && secondDp >= threshold) || (firstDp >= threshold && secondDp < threshold)) {
                return SHOW_ACTIVITY_START_TIME;
            }
        }
        return false;
    }

    public void setSizeConfigurations(int[] horizontalSizeConfiguration, int[] verticalSizeConfigurations, int[] smallestSizeConfigurations) {
        this.mHorizontalSizeConfigurations = horizontalSizeConfiguration;
        this.mVerticalSizeConfigurations = verticalSizeConfigurations;
        this.mSmallestSizeConfigurations = smallestSizeConfigurations;
    }

    void scheduleConfigurationChanged(Configuration config, boolean reportToActivity) {
        if (this.app != null && this.app.thread != null) {
            try {
                Configuration overrideConfig = new Configuration(config);
                overrideConfig.fontScale = this.service.mConfiguration.fontScale;
                if (ActivityManagerDebugConfig.DEBUG_CONFIGURATION) {
                    Slog.v(TAG, "Sending new config to " + this + " " + "reportToActivity=" + reportToActivity + " and config: " + overrideConfig);
                }
                this.app.thread.scheduleActivityConfigurationChanged(this.appToken, overrideConfig, reportToActivity);
            } catch (RemoteException e) {
            }
        }
    }

    void scheduleMultiWindowModeChanged() {
        if (this.task != null && this.task.stack != null && this.app != null && this.app.thread != null) {
            try {
                boolean z;
                IApplicationThread iApplicationThread = this.app.thread;
                IBinder iBinder = this.appToken;
                if (this.task.mFullscreen) {
                    z = false;
                } else {
                    z = SHOW_ACTIVITY_START_TIME;
                }
                iApplicationThread.scheduleMultiWindowModeChanged(iBinder, z);
            } catch (Exception e) {
            }
        }
    }

    void schedulePictureInPictureModeChanged() {
        if (this.task != null && this.task.stack != null && this.app != null && this.app.thread != null) {
            try {
                this.app.thread.schedulePictureInPictureModeChanged(this.appToken, this.task.stack.mStackId == 4 ? SHOW_ACTIVITY_START_TIME : false);
            } catch (Exception e) {
            }
        }
    }

    boolean isFreeform() {
        if (this.task == null || this.task.stack == null || this.task.stack.mStackId != STARTING_WINDOW_REMOVED) {
            return false;
        }
        return SHOW_ACTIVITY_START_TIME;
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

    public ActivityRecord(ActivityManagerService _service, ProcessRecord _caller, int _launchedFromUid, String _launchedFromPackage, Intent _intent, String _resolvedType, ActivityInfo aInfo, Configuration _configuration, ActivityRecord _resultTo, String _resultWho, int _reqCode, boolean _componentSpecified, boolean _rootVoiceInteraction, ActivityStackSupervisor supervisor, ActivityContainer container, ActivityOptions options, ActivityRecord sourceRecord) {
        this.createTime = System.currentTimeMillis();
        this.mChildContainers = new ArrayList();
        this.mStartingWindowState = STARTING_WINDOW_NOT_SHOWN;
        this.mTaskOverlay = false;
        this.service = _service;
        this.appToken = new Token(this, this.service);
        this.info = aInfo;
        this.launchedFromUid = _launchedFromUid;
        this.launchedFromPackage = _launchedFromPackage;
        this.userId = UserHandle.getUserId(aInfo.applicationInfo.uid);
        int i = ((_intent.getHwFlags() & STARTING_WINDOW_SHOWN) == 0 && this.info.applicationInfo.euid == 0) ? STARTING_WINDOW_NOT_SHOWN : STARTING_WINDOW_SHOWN;
        this.multiLaunchId = i;
        this.intent = _intent;
        this.shortComponentName = _intent.getComponent().flattenToShortString();
        this.resolvedType = _resolvedType;
        this.componentSpecified = _componentSpecified;
        this.rootVoiceInteraction = _rootVoiceInteraction;
        this.configuration = _configuration;
        this.taskConfigOverride = Configuration.EMPTY;
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
        if (options != null) {
            this.pendingOptions = options;
            this.mLaunchTaskBehind = this.pendingOptions.getLaunchTaskBehind();
            PendingIntent usageReport = this.pendingOptions.getUsageTimeReport();
            if (usageReport != null) {
                this.appTimeTracker = new AppTimeTracker(usageReport);
            }
        }
        this.haveState = SHOW_ACTIVITY_START_TIME;
        initSplitMode(_intent);
        if (aInfo != null) {
            if (aInfo.targetActivity == null || (aInfo.targetActivity.equals(_intent.getComponent().getClassName()) && (aInfo.launchMode == 0 || aInfo.launchMode == STARTING_WINDOW_SHOWN))) {
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
                aInfo.launchMode = STARTING_WINDOW_NOT_SHOWN;
            } else {
                this.taskAffinity = aInfo.taskAffinity;
            }
            this.stateNotNeeded = (aInfo.flags & 16) != 0 ? SHOW_ACTIVITY_START_TIME : false;
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
                if (aInfo.applicationInfo.targetSdkVersion < 11) {
                    i = 16973829;
                } else {
                    i = 16973931;
                }
                this.realTheme = i;
            }
            if ((aInfo.flags & DumpState.DUMP_MESSAGES) != 0) {
                this.windowFlags |= 16777216;
            }
            if ((aInfo.flags & STARTING_WINDOW_SHOWN) == 0 || _caller == null || !(aInfo.applicationInfo.uid == ProcessList.PSS_SAFE_TIME_FROM_STATE_CHANGE || aInfo.applicationInfo.uid == _caller.info.uid)) {
                this.processName = aInfo.processName;
            } else {
                this.processName = _caller.processName;
            }
            if (!(this.intent == null || (aInfo.flags & 32) == 0)) {
                this.intent.addFlags(8388608);
            }
            this.packageName = aInfo.applicationInfo.packageName;
            this.launchMode = aInfo.launchMode;
            Entry ent = AttributeCache.instance().get(this.packageName, this.realTheme, R.styleable.Window, this.userId);
            boolean z = ent != null ? !ent.array.getBoolean(5, false) ? !ent.array.hasValue(5) ? ent.array.getBoolean(25, false) : false : SHOW_ACTIVITY_START_TIME : false;
            this.translucent = z;
            z = (ent == null || ent.array.getBoolean(4, false)) ? false : this.translucent ? false : SHOW_ACTIVITY_START_TIME;
            this.fullscreen = z;
            if (isSplitMode()) {
                if (this.fullscreen) {
                    this.fullscreen = false;
                } else {
                    _intent.addHwFlags(8);
                }
            }
            this.noDisplay = ent != null ? ent.array.getBoolean(10, false) : false;
            setActivityType(_componentSpecified, _launchedFromUid, _intent, sourceRecord);
            this.immersive = (aInfo.flags & DumpState.DUMP_VERIFIERS) != 0 ? SHOW_ACTIVITY_START_TIME : false;
            this.requestedVrComponent = aInfo.requestedVrComponent == null ? null : ComponentName.unflattenFromString(aInfo.requestedVrComponent);
            return;
        }
        this.realActivity = null;
        this.taskAffinity = null;
        this.stateNotNeeded = false;
        this.appInfo = null;
        this.processName = null;
        this.packageName = null;
        this.fullscreen = SHOW_ACTIVITY_START_TIME;
        this.noDisplay = false;
        this.mActivityType = STARTING_WINDOW_NOT_SHOWN;
        this.immersive = false;
        this.requestedVrComponent = null;
    }

    private boolean isHomeIntent(Intent intent) {
        if ("android.intent.action.MAIN".equals(intent.getAction()) && intent.hasCategory("android.intent.category.HOME") && intent.getCategories().size() == STARTING_WINDOW_SHOWN && intent.getData() == null) {
            return intent.getType() == null ? SHOW_ACTIVITY_START_TIME : false;
        } else {
            return false;
        }
    }

    private boolean canLaunchHomeActivity(int uid, ActivityRecord sourceRecord) {
        boolean z = false;
        if (uid == Process.myUid() || uid == 0) {
            return SHOW_ACTIVITY_START_TIME;
        }
        if (sourceRecord != null) {
            z = sourceRecord.isResolverActivity();
        }
        return z;
    }

    private void setActivityType(boolean componentSpecified, int launchedFromUid, Intent intent, ActivityRecord sourceRecord) {
        if ((!componentSpecified || canLaunchHomeActivity(launchedFromUid, sourceRecord)) && isHomeIntent(intent) && !isResolverActivity()) {
            this.mActivityType = STARTING_WINDOW_SHOWN;
        } else if (this.realActivity.getClassName().contains(RECENTS_PACKAGE_NAME)) {
            this.mActivityType = STARTING_WINDOW_REMOVED;
        } else {
            this.mActivityType = STARTING_WINDOW_NOT_SHOWN;
        }
    }

    void setTask(TaskRecord newTask, TaskRecord taskToAffiliateWith) {
        if (!(this.task == null || !this.task.removeActivity(this) || this.task == newTask || this.task.stack == null)) {
            this.task.stack.removeTask(this.task, "setTask");
        }
        this.task = newTask;
        setTaskToAffiliateWith(taskToAffiliateWith);
    }

    void setTaskToAffiliateWith(TaskRecord taskToAffiliateWith) {
        if (taskToAffiliateWith != null && this.launchMode != 3 && this.launchMode != STARTING_WINDOW_REMOVED) {
            this.task.setTaskToAffiliateWith(taskToAffiliateWith);
        }
    }

    boolean changeWindowTranslucency(boolean toOpaque) {
        if (this.fullscreen == toOpaque) {
            return false;
        }
        TaskRecord taskRecord = this.task;
        taskRecord.numFullscreen = (toOpaque ? STARTING_WINDOW_SHOWN : -1) + taskRecord.numFullscreen;
        this.fullscreen = toOpaque;
        return SHOW_ACTIVITY_START_TIME;
    }

    void putInHistory() {
        if (!this.inHistory) {
            this.inHistory = SHOW_ACTIVITY_START_TIME;
        }
    }

    void takeFromHistory() {
        if (this.inHistory) {
            this.inHistory = false;
            if (!(this.task == null || this.finishing)) {
                this.task = null;
            }
            clearOptionsLocked();
        }
    }

    boolean isInHistory() {
        return this.inHistory;
    }

    boolean isInStackLocked() {
        return (this.task == null || this.task.stack == null || this.task.stack.isInStackLocked(this) == null) ? false : SHOW_ACTIVITY_START_TIME;
    }

    boolean isHomeActivity() {
        return this.mActivityType == STARTING_WINDOW_SHOWN ? SHOW_ACTIVITY_START_TIME : false;
    }

    boolean isRecentsActivity() {
        return this.mActivityType == STARTING_WINDOW_REMOVED ? SHOW_ACTIVITY_START_TIME : false;
    }

    boolean isApplicationActivity() {
        return this.mActivityType == 0 ? SHOW_ACTIVITY_START_TIME : false;
    }

    boolean isPersistable() {
        boolean z = SHOW_ACTIVITY_START_TIME;
        if (this.info.persistableMode != 0 && this.info.persistableMode != STARTING_WINDOW_REMOVED) {
            return false;
        }
        if (!(this.intent == null || (this.intent.getFlags() & 8388608) == 0)) {
            z = false;
        }
        return z;
    }

    boolean isFocusable() {
        return !StackId.canReceiveKeys(this.task.stack.mStackId) ? isAlwaysFocusable() : SHOW_ACTIVITY_START_TIME;
    }

    boolean isResizeable() {
        return !isHomeActivity() ? ActivityInfo.isResizeableMode(this.info.resizeMode) : false;
    }

    boolean isResizeableOrForced() {
        if (isHomeActivity()) {
            return false;
        }
        return !isResizeable() ? this.service.mForceResizableActivities : SHOW_ACTIVITY_START_TIME;
    }

    boolean isNonResizableOrForced() {
        if (isHomeActivity() || this.info.resizeMode == STARTING_WINDOW_REMOVED || this.info.resizeMode == 3) {
            return false;
        }
        return SHOW_ACTIVITY_START_TIME;
    }

    boolean supportsPictureInPicture() {
        return (isHomeActivity() || this.info.resizeMode != 3) ? false : SHOW_ACTIVITY_START_TIME;
    }

    boolean canGoInDockedStack() {
        if (isHomeActivity()) {
            return false;
        }
        return (isResizeableOrForced() || this.info.resizeMode == STARTING_WINDOW_SHOWN) ? SHOW_ACTIVITY_START_TIME : false;
    }

    boolean isAlwaysFocusable() {
        return (this.info.flags & DumpState.DUMP_DOMAIN_PREFERRED) != 0 ? SHOW_ACTIVITY_START_TIME : false;
    }

    void makeFinishingLocked() {
        if (!this.finishing) {
            if (!(this.task == null || this.task.stack == null || this != this.task.stack.getVisibleBehindActivity())) {
                this.mStackSupervisor.requestVisibleBehindLocked(this, false);
            }
            this.finishing = SHOW_ACTIVITY_START_TIME;
            if (this.stopped) {
                clearOptionsLocked();
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

    void addNewIntentLocked(ReferrerIntent intent) {
        if (this.newIntents == null) {
            this.newIntents = new ArrayList();
        }
        this.newIntents.add(intent);
    }

    final void deliverNewIntentLocked(int callingUid, Intent intent, String referrer) {
        this.service.grantUriPermissionFromIntentLocked(callingUid, this.packageName, intent, getUriPermissionsLocked(), this.userId);
        ReferrerIntent rintent = new ReferrerIntent(intent, referrer);
        boolean unsent = SHOW_ACTIVITY_START_TIME;
        if (!((this.state != ActivityState.RESUMED && (!this.service.isSleepingLocked() || this.task.stack == null || this.task.stack.topRunningActivityLocked() != this)) || this.app == null || this.app.thread == null)) {
            try {
                ArrayList<ReferrerIntent> ar = new ArrayList(STARTING_WINDOW_SHOWN);
                ar.add(rintent);
                this.app.thread.scheduleNewIntent(ar, this.appToken);
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
                case STARTING_WINDOW_SHOWN /*1*/:
                    this.service.mWindowManager.overridePendingAppTransition(this.pendingOptions.getPackageName(), this.pendingOptions.getCustomEnterResId(), this.pendingOptions.getCustomExitResId(), this.pendingOptions.getOnAnimationStartListener());
                    break;
                case STARTING_WINDOW_REMOVED /*2*/:
                    this.service.mWindowManager.overridePendingAppTransitionScaleUp(this.pendingOptions.getStartX(), this.pendingOptions.getStartY(), this.pendingOptions.getWidth(), this.pendingOptions.getHeight());
                    if (this.intent.getSourceBounds() == null) {
                        this.intent.setSourceBounds(new Rect(this.pendingOptions.getStartX(), this.pendingOptions.getStartY(), this.pendingOptions.getStartX() + this.pendingOptions.getWidth(), this.pendingOptions.getStartY() + this.pendingOptions.getHeight()));
                        break;
                    }
                    break;
                case H.REPORT_LOSING_FOCUS /*3*/:
                case H.DO_TRAVERSAL /*4*/:
                    this.service.mWindowManager.overridePendingAppTransitionThumb(this.pendingOptions.getThumbnail(), this.pendingOptions.getStartX(), this.pendingOptions.getStartY(), this.pendingOptions.getOnAnimationStartListener(), animationType == 3 ? SHOW_ACTIVITY_START_TIME : false);
                    if (this.intent.getSourceBounds() == null) {
                        this.intent.setSourceBounds(new Rect(this.pendingOptions.getStartX(), this.pendingOptions.getStartY(), this.pendingOptions.getStartX() + this.pendingOptions.getThumbnail().getWidth(), this.pendingOptions.getStartY() + this.pendingOptions.getThumbnail().getHeight()));
                        break;
                    }
                    break;
                case H.REPORT_APPLICATION_TOKEN_WINDOWS /*8*/:
                case H.REPORT_APPLICATION_TOKEN_DRAWN /*9*/:
                    AppTransitionAnimationSpec[] specs = this.pendingOptions.getAnimSpecs();
                    if (animationType == 9 && specs != null) {
                        this.service.mWindowManager.overridePendingAppTransitionMultiThumb(specs, this.pendingOptions.getOnAnimationStartListener(), this.pendingOptions.getAnimationFinishedListener(), false);
                        break;
                    }
                    boolean z;
                    WindowManagerService windowManagerService = this.service.mWindowManager;
                    Bitmap thumbnail = this.pendingOptions.getThumbnail();
                    int startX = this.pendingOptions.getStartX();
                    int startY = this.pendingOptions.getStartY();
                    int width = this.pendingOptions.getWidth();
                    int height = this.pendingOptions.getHeight();
                    IRemoteCallback onAnimationStartListener = this.pendingOptions.getOnAnimationStartListener();
                    if (animationType == 8) {
                        z = SHOW_ACTIVITY_START_TIME;
                    } else {
                        z = false;
                    }
                    windowManagerService.overridePendingAppTransitionAspectScaledThumb(thumbnail, startX, startY, width, height, onAnimationStartListener, z);
                    if (this.intent.getSourceBounds() == null) {
                        this.intent.setSourceBounds(new Rect(this.pendingOptions.getStartX(), this.pendingOptions.getStartY(), this.pendingOptions.getStartX() + this.pendingOptions.getWidth(), this.pendingOptions.getStartY() + this.pendingOptions.getHeight()));
                        break;
                    }
                    break;
                case H.WINDOW_FREEZE_TIMEOUT /*11*/:
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
            this.keysPaused = SHOW_ACTIVITY_START_TIME;
            this.service.mWindowManager.pauseKeyDispatching(this.appToken);
        }
    }

    void resumeKeyDispatchingLocked() {
        if (this.keysPaused) {
            this.keysPaused = false;
            this.service.mWindowManager.resumeKeyDispatching(this.appToken);
        }
    }

    void updateThumbnailLocked(Bitmap newThumbnail, CharSequence description) {
        if (newThumbnail != null) {
            if (ActivityManagerDebugConfig.DEBUG_THUMBNAILS) {
                Slog.i(TAG_THUMBNAILS, "Setting thumbnail of " + this + " to " + newThumbnail);
            }
            if (this.task.setLastThumbnailLocked(newThumbnail) && isPersistable()) {
                this.mStackSupervisor.mService.notifyTaskPersisterLocked(this.task, false);
            }
        }
        this.task.lastDescription = description;
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
        ActivityStack stack = this.task.stack;
        if (stack == null) {
            return false;
        }
        Message msg = stack.mHandler.obtainMessage(HdmiCecKeycode.CEC_KEYCODE_TUNE_FUNCTION, this);
        stack.mHandler.removeMessages(HdmiCecKeycode.CEC_KEYCODE_TUNE_FUNCTION);
        stack.mHandler.sendMessageDelayed(msg, 500);
        return SHOW_ACTIVITY_START_TIME;
    }

    void finishLaunchTickingLocked() {
        this.launchTickTime = 0;
        ActivityStack stack = this.task.stack;
        if (stack != null) {
            stack.mHandler.removeMessages(HdmiCecKeycode.CEC_KEYCODE_TUNE_FUNCTION);
        }
    }

    public boolean mayFreezeScreenLocked(ProcessRecord app) {
        return (app == null || app.crashing || app.notResponding) ? false : SHOW_ACTIVITY_START_TIME;
    }

    public void startFreezingScreenLocked(ProcessRecord app, int configChanges) {
        if (mayFreezeScreenLocked(app)) {
            this.service.mWindowManager.startAppFreezingScreen(this.appToken, configChanges);
        }
    }

    public void stopFreezingScreenLocked(boolean force) {
        if (force || this.frozenBeforeDestroy) {
            this.frozenBeforeDestroy = false;
            this.service.mWindowManager.stopAppFreezingScreen(this.appToken, force);
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
        ActivityStack stack = this.task.stack;
        if (!(this.fullyDrawnStartTime == 0 || stack == null)) {
            long thisTime = curTime - this.fullyDrawnStartTime;
            long totalTime = stack.mFullyDrawnStartTime != 0 ? curTime - stack.mFullyDrawnStartTime : thisTime;
            Trace.asyncTraceEnd(64, "drawing", STARTING_WINDOW_NOT_SHOWN);
            EventLog.writeEvent(EventLogTags.AM_ACTIVITY_FULLY_DRAWN_TIME, new Object[]{Integer.valueOf(this.userId), Integer.valueOf(System.identityHashCode(this)), this.shortComponentName, Long.valueOf(thisTime), Long.valueOf(totalTime)});
            StringBuilder sb = this.service.mStringBuilder;
            sb.setLength(STARTING_WINDOW_NOT_SHOWN);
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
            if (totalTime > 0) {
                stack.mFullyDrawnStartTime = 0;
            } else {
                stack.mFullyDrawnStartTime = 0;
            }
        }
        this.fullyDrawnStartTime = 0;
    }

    private void reportLaunchTimeLocked(long curTime) {
        ActivityStack stack = this.task.stack;
        if (stack != null) {
            long thisTime = curTime - this.displayStartTime;
            long totalTime = stack.mLaunchStartTime != 0 ? curTime - stack.mLaunchStartTime : thisTime;
            Trace.asyncTraceEnd(64, "launching: " + this.packageName, STARTING_WINDOW_NOT_SHOWN);
            EventLog.writeEvent(EventLogTags.AM_ACTIVITY_LAUNCH_TIME, new Object[]{Integer.valueOf(this.userId), Integer.valueOf(System.identityHashCode(this)), this.shortComponentName, Long.valueOf(thisTime), Long.valueOf(totalTime)});
            if (stack.mshortComponentName.equals(this.shortComponentName)) {
                Jlog.d(44, this.shortComponentName, (int) thisTime, "");
            }
            StringBuilder sb = this.service.mStringBuilder;
            sb.setLength(STARTING_WINDOW_NOT_SHOWN);
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
            if (totalTime > 0) {
                this.displayStartTime = 0;
                this.task.isLaunching = false;
                stack.mLaunchStartTime = 0;
            } else {
                this.displayStartTime = 0;
                this.task.isLaunching = false;
                stack.mLaunchStartTime = 0;
            }
        }
    }

    void windowsDrawnLocked() {
        this.mStackSupervisor.mActivityMetricsLogger.notifyWindowsDrawn();
        long curTime = SystemClock.uptimeMillis();
        if (this.displayStartTime == 0 && this.task != null && this.task.isLaunching) {
            this.displayStartTime = curTime;
        }
        if (this.displayStartTime != 0) {
            reportLaunchTimeLocked(curTime);
        } else {
            Jlog.warmLaunchingAppEnd(this.shortComponentName);
        }
        this.mStackSupervisor.sendWaitingVisibleReportLocked(this);
        this.startTime = 0;
        finishLaunchTickingLocked();
        if (this.task != null) {
            this.task.hasBeenVisible = SHOW_ACTIVITY_START_TIME;
        }
        this.service.reportActivityStartFinished();
    }

    void windowsVisibleLocked() {
        this.mStackSupervisor.reportActivityVisibleLocked(this);
        Flog.i(H.KEYGUARD_DISMISS_DONE, "windowsVisibleLocked(): " + this + " idle: " + this.idle);
        if (!this.nowVisible) {
            this.nowVisible = SHOW_ACTIVITY_START_TIME;
            this.lastVisibleTime = SystemClock.uptimeMillis();
            if (this.idle) {
                int size = this.mStackSupervisor.mWaitingVisibleActivities.size();
                if (size > 0) {
                    for (int i = STARTING_WINDOW_NOT_SHOWN; i < size; i += STARTING_WINDOW_SHOWN) {
                        ActivityRecord r = (ActivityRecord) this.mStackSupervisor.mWaitingVisibleActivities.get(i);
                        if (ActivityManagerDebugConfig.DEBUG_SWITCH) {
                            Log.v(TAG_SWITCH, "Was waiting for visible: " + r);
                        }
                    }
                    this.mStackSupervisor.mWaitingVisibleActivities.clear();
                    this.mStackSupervisor.scheduleIdleLocked();
                }
            } else {
                this.mStackSupervisor.processStoppingActivitiesLocked(false);
            }
            this.service.scheduleAppGcsLocked();
        }
    }

    ActivityRecord getWaitingHistoryRecordLocked() {
        if (this.mStackSupervisor.mWaitingVisibleActivities.contains(this) || this.stopped) {
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

    public boolean isInterestingToUserLocked() {
        if (this.visible || this.nowVisible || this.state == ActivityState.PAUSING || this.state == ActivityState.RESUMED) {
            return SHOW_ACTIVITY_START_TIME;
        }
        return false;
    }

    public void setSleeping(boolean _sleeping) {
        if (!(this.sleeping == _sleeping || this.app == null || this.app.thread == null)) {
            try {
                this.app.thread.scheduleSleeping(this.appToken, _sleeping);
                if (_sleeping && !this.mStackSupervisor.mGoingToSleepActivities.contains(this)) {
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
            return r.task.stack.isInStackLocked(r);
        }
        return null;
    }

    static ActivityStack getStackLocked(IBinder token) {
        ActivityRecord r = isInStackLocked(token);
        if (r != null) {
            return r.task.stack;
        }
        return null;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    final boolean isDestroyable() {
        if (this.finishing || this.app == null || this.state == ActivityState.DESTROYING || this.state == ActivityState.DESTROYED || this.task == null || this.task.stack == null || this == this.task.stack.mResumedActivity || this == this.task.stack.mPausingActivity || !this.haveState || !this.stopped || this.visible) {
            return false;
        }
        return SHOW_ACTIVITY_START_TIME;
    }

    private static String createImageFilename(long createTime, int taskId) {
        return String.valueOf(taskId) + ACTIVITY_ICON_SUFFIX + createTime + ".png";
    }

    void setTaskDescription(TaskDescription _taskDescription) {
        if (_taskDescription.getIconFilename() == null) {
            Bitmap icon = _taskDescription.getIcon();
            if (icon != null) {
                String iconFilePath = new File(TaskPersister.getUserImagesDir(this.userId), createImageFilename(this.createTime, this.task.taskId)).getAbsolutePath();
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

    void showStartingWindow(ActivityRecord prev, boolean createIfNeeded) {
        IBinder iBinder = null;
        CompatibilityInfo compatInfo = this.service.compatibilityInfoForPackageLocked(this.info.applicationInfo);
        WindowManagerService windowManagerService = this.service.mWindowManager;
        IBinder iBinder2 = this.appToken;
        String str = this.packageName;
        int i = this.theme;
        CharSequence charSequence = this.nonLocalizedLabel;
        int i2 = this.labelRes;
        int i3 = this.icon;
        int i4 = this.logo;
        int i5 = this.windowFlags;
        if (prev != null) {
            iBinder = prev.appToken;
        }
        if (windowManagerService.setAppStartingWindow(iBinder2, str, i, compatInfo, charSequence, i2, i3, i4, i5, iBinder, createIfNeeded)) {
            this.mStartingWindowState = STARTING_WINDOW_SHOWN;
        }
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
        out.startTag(null, TAG_INTENT);
        this.intent.saveToXml(out);
        out.endTag(null, TAG_INTENT);
        if (isPersistable() && this.persistentState != null) {
            out.startTag(null, TAG_PERSISTABLEBUNDLE);
            this.persistentState.saveToXml(out);
            out.endTag(null, TAG_PERSISTABLEBUNDLE);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static ActivityRecord restoreFromXml(XmlPullParser in, ActivityStackSupervisor stackSupervisor) throws IOException, XmlPullParserException {
        ActivityInfo aInfo;
        Intent intent = null;
        PersistableBundle persistableBundle = null;
        int launchedFromUid = STARTING_WINDOW_NOT_SHOWN;
        String launchedFromPackage = null;
        String resolvedType = null;
        boolean componentSpecified = false;
        int userId = STARTING_WINDOW_NOT_SHOWN;
        long createTime = -1;
        int outerDepth = in.getDepth();
        TaskDescription taskDescription = new TaskDescription();
        for (int attrNdx = in.getAttributeCount() - 1; attrNdx >= 0; attrNdx--) {
            String attrName = in.getAttributeName(attrNdx);
            String attrValue = in.getAttributeValue(attrNdx);
            if (ATTR_ID.equals(attrName)) {
                createTime = Long.valueOf(attrValue).longValue();
            } else if (ATTR_LAUNCHEDFROMUID.equals(attrName)) {
                launchedFromUid = Integer.parseInt(attrValue);
            } else if (ATTR_LAUNCHEDFROMPACKAGE.equals(attrName)) {
                launchedFromPackage = attrValue;
            } else if (ATTR_RESOLVEDTYPE.equals(attrName)) {
                resolvedType = attrValue;
            } else if (ATTR_COMPONENTSPECIFIED.equals(attrName)) {
                componentSpecified = Boolean.valueOf(attrValue).booleanValue();
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
            ActivityManagerService service;
            int event = in.next();
            if (event == STARTING_WINDOW_SHOWN || (event == 3 && in.getDepth() < outerDepth)) {
                if (intent != null) {
                    throw new XmlPullParserException("restoreActivity error intent=" + intent);
                }
                service = stackSupervisor.mService;
                aInfo = stackSupervisor.resolveActivity(intent, resolvedType, STARTING_WINDOW_NOT_SHOWN, null, userId);
                if (aInfo != null) {
                    throw new XmlPullParserException("restoreActivity resolver error. Intent=" + intent + " resolvedType=" + resolvedType);
                }
                ActivityRecord r = HwServiceFactory.createActivityRecord(service, null, launchedFromUid, launchedFromPackage, intent, resolvedType, aInfo, service.getConfiguration(), null, null, STARTING_WINDOW_NOT_SHOWN, componentSpecified, false, stackSupervisor, null, null, null);
                r.persistentState = persistableBundle;
                r.taskDescription = taskDescription;
                r.createTime = createTime;
                return r;
            } else if (event == STARTING_WINDOW_REMOVED) {
                String name = in.getName();
                if (TAG_INTENT.equals(name)) {
                    intent = Intent.restoreFromXml(in);
                } else if (TAG_PERSISTABLEBUNDLE.equals(name)) {
                    persistableBundle = PersistableBundle.restoreFromXml(in);
                } else {
                    Slog.w(TAG, "restoreActivity: unexpected name=" + name);
                    XmlUtils.skipCurrentTag(in);
                }
            }
        }
        if (intent != null) {
            service = stackSupervisor.mService;
            aInfo = stackSupervisor.resolveActivity(intent, resolvedType, STARTING_WINDOW_NOT_SHOWN, null, userId);
            if (aInfo != null) {
                ActivityRecord r2 = HwServiceFactory.createActivityRecord(service, null, launchedFromUid, launchedFromPackage, intent, resolvedType, aInfo, service.getConfiguration(), null, null, STARTING_WINDOW_NOT_SHOWN, componentSpecified, false, stackSupervisor, null, null, null);
                r2.persistentState = persistableBundle;
                r2.taskDescription = taskDescription;
                r2.createTime = createTime;
                return r2;
            }
            throw new XmlPullParserException("restoreActivity resolver error. Intent=" + intent + " resolvedType=" + resolvedType);
        }
        throw new XmlPullParserException("restoreActivity error intent=" + intent);
    }

    private static String activityTypeToString(int type) {
        switch (type) {
            case STARTING_WINDOW_NOT_SHOWN /*0*/:
                return "APPLICATION_ACTIVITY_TYPE";
            case STARTING_WINDOW_SHOWN /*1*/:
                return "HOME_ACTIVITY_TYPE";
            case STARTING_WINDOW_REMOVED /*2*/:
                return "RECENTS_ACTIVITY_TYPE";
            default:
                return Integer.toString(type);
        }
    }

    public String toString() {
        if (this.stringName != null) {
            return this.stringName + " t" + (this.task == null ? -1 : this.task.taskId) + (this.finishing ? " f}" : "}");
        }
        StringBuilder sb = new StringBuilder(DumpState.DUMP_PACKAGES);
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
