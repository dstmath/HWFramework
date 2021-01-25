package com.android.server.wm;

import android.app.ActivityOptions;
import android.app.IApplicationThread;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import android.view.RemoteAnimationAdapter;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ArrayUtils;
import com.android.server.HwServiceFactory;
import com.android.server.am.PendingIntentRecord;
import com.android.server.wm.ActivityStackSupervisor;
import com.android.server.wm.ActivityStarter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ActivityStartController {
    private static final int DO_PENDING_ACTIVITY_LAUNCHES_MSG = 1;
    private static final String TAG = "ActivityTaskManager";
    boolean mCheckedForSetup;
    String mCurActivityPkName;
    private final ActivityStarter.Factory mFactory;
    private final Handler mHandler;
    private ActivityRecord mLastHomeActivityStartRecord;
    private int mLastHomeActivityStartResult;
    private ActivityStarter mLastStarter;
    private final ArrayList<ActivityStackSupervisor.PendingActivityLaunch> mPendingActivityLaunches;
    private final PendingRemoteAnimationRegistry mPendingRemoteAnimationRegistry;
    private final ActivityTaskManagerService mService;
    private final ActivityStackSupervisor mSupervisor;
    private ActivityRecord[] tmpOutRecord;

    private final class StartHandler extends Handler {
        public StartHandler(Looper looper) {
            super(looper, null, true);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                synchronized (ActivityStartController.this.mService.mGlobalLock) {
                    try {
                        WindowManagerService.boostPriorityForLockedSection();
                        ActivityStartController.this.doPendingActivityLaunches(true);
                    } finally {
                        WindowManagerService.resetPriorityAfterLockedSection();
                    }
                }
            }
        }
    }

    ActivityStartController(ActivityTaskManagerService service) {
        this(service, service.mStackSupervisor, new ActivityStarter.DefaultFactory(service, service.mStackSupervisor, HwServiceFactory.createActivityStartInterceptor(service, service.mStackSupervisor)));
    }

    @VisibleForTesting
    ActivityStartController(ActivityTaskManagerService service, ActivityStackSupervisor supervisor, ActivityStarter.Factory factory) {
        this.tmpOutRecord = new ActivityRecord[1];
        this.mPendingActivityLaunches = new ArrayList<>();
        this.mCheckedForSetup = false;
        this.mCurActivityPkName = "";
        this.mService = service;
        this.mSupervisor = supervisor;
        this.mHandler = new StartHandler(this.mService.mH.getLooper());
        this.mFactory = factory;
        this.mFactory.setController(this);
        this.mPendingRemoteAnimationRegistry = new PendingRemoteAnimationRegistry(service, service.mH);
    }

    /* access modifiers changed from: package-private */
    public ActivityStarter obtainStarter(Intent intent, String reason) {
        return this.mFactory.obtain().setIntent(intent).setReason(reason);
    }

    /* access modifiers changed from: package-private */
    public void onExecutionComplete(ActivityStarter starter) {
        if (this.mLastStarter == null) {
            this.mLastStarter = this.mFactory.obtain();
        }
        this.mLastStarter.set(starter);
        this.mFactory.recycle(starter);
    }

    /* access modifiers changed from: package-private */
    public void postStartActivityProcessingForLastStarter(ActivityRecord r, int result, ActivityStack targetStack) {
        ActivityStarter activityStarter = this.mLastStarter;
        if (activityStarter != null) {
            activityStarter.postStartActivityProcessing(r, result, targetStack);
        }
    }

    /* access modifiers changed from: package-private */
    public void startHomeActivity(Intent intent, ActivityInfo aInfo, String reason, int displayId) {
        ActivityOptions options = ActivityOptions.makeBasic();
        options.setLaunchWindowingMode(1);
        if (!ActivityRecord.isResolverActivity(aInfo.name)) {
            options.setLaunchActivityType(2);
        }
        options.setLaunchDisplayId(displayId);
        this.mLastHomeActivityStartResult = obtainStarter(intent, "startHomeActivity: " + reason).setOutActivity(this.tmpOutRecord).setCallingUid(0).setActivityInfo(aInfo).setActivityOptions(options.toBundle()).execute();
        this.mLastHomeActivityStartRecord = this.tmpOutRecord[0];
        ActivityDisplay display = this.mService.mRootActivityContainer.getActivityDisplay(displayId);
        ActivityStack homeStack = display != null ? display.getHomeStack() : null;
        if (homeStack != null && homeStack.mInResumeTopActivity) {
            this.mSupervisor.scheduleResumeTopActivities();
        }
    }

    /* access modifiers changed from: package-private */
    public void startSetupActivity() {
        String vers;
        if (!this.mCheckedForSetup) {
            ContentResolver resolver = this.mService.mContext.getContentResolver();
            if (this.mService.mFactoryTest != 1 && Settings.Global.getInt(resolver, "device_provisioned", 0) != 0) {
                this.mCheckedForSetup = true;
                Intent intent = new Intent("android.intent.action.UPGRADE_SETUP");
                List<ResolveInfo> ris = this.mService.mContext.getPackageManager().queryIntentActivities(intent, 1049728);
                if (!ris.isEmpty()) {
                    ResolveInfo ri = ris.get(0);
                    if (ri.activityInfo.metaData != null) {
                        vers = ri.activityInfo.metaData.getString("android.SETUP_VERSION");
                    } else {
                        vers = null;
                    }
                    if (vers == null && ri.activityInfo.applicationInfo.metaData != null) {
                        vers = ri.activityInfo.applicationInfo.metaData.getString("android.SETUP_VERSION");
                    }
                    String lastVers = Settings.Secure.getString(resolver, "last_setup_shown");
                    if (vers != null && !vers.equals(lastVers)) {
                        intent.setFlags(268435456);
                        intent.setComponent(new ComponentName(ri.activityInfo.packageName, ri.activityInfo.name));
                        obtainStarter(intent, "startSetupActivity").setCallingUid(0).setActivityInfo(ri.activityInfo).execute();
                    }
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public int checkTargetUser(int targetUserId, boolean validateIncomingUser, int realCallingPid, int realCallingUid, String reason) {
        if (validateIncomingUser) {
            return this.mService.handleIncomingUser(realCallingPid, realCallingUid, targetUserId, reason);
        }
        this.mService.mAmInternal.ensureNotSpecialUser(targetUserId);
        return targetUserId;
    }

    /* access modifiers changed from: package-private */
    public final int startActivityInPackage(int uid, int realCallingPid, int realCallingUid, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int startFlags, SafeActivityOptions options, int userId, TaskRecord inTask, String reason, boolean validateIncomingUser, PendingIntentRecord originatingPendingIntent, boolean allowBackgroundActivityStart) {
        return obtainStarter(intent, reason).setCallingUid(uid).setRealCallingPid(realCallingPid).setRealCallingUid(realCallingUid).setCallingPackage(callingPackage).setResolvedType(resolvedType).setResultTo(resultTo).setResultWho(resultWho).setRequestCode(requestCode).setStartFlags(startFlags).setActivityOptions(options).setMayWait(checkTargetUser(userId, validateIncomingUser, realCallingPid, realCallingUid, reason)).setInTask(inTask).setOriginatingPendingIntent(originatingPendingIntent).setAllowBackgroundActivityStart(allowBackgroundActivityStart).execute();
    }

    /* access modifiers changed from: package-private */
    public final int startActivitiesInPackage(int uid, String callingPackage, Intent[] intents, String[] resolvedTypes, IBinder resultTo, SafeActivityOptions options, int userId, boolean validateIncomingUser, PendingIntentRecord originatingPendingIntent, boolean allowBackgroundActivityStart) {
        return startActivitiesInPackage(uid, 0, -1, callingPackage, intents, resolvedTypes, resultTo, options, userId, validateIncomingUser, originatingPendingIntent, allowBackgroundActivityStart);
    }

    /* access modifiers changed from: package-private */
    public final int startActivitiesInPackage(int uid, int realCallingPid, int realCallingUid, String callingPackage, Intent[] intents, String[] resolvedTypes, IBinder resultTo, SafeActivityOptions options, int userId, boolean validateIncomingUser, PendingIntentRecord originatingPendingIntent, boolean allowBackgroundActivityStart) {
        return startActivities(null, uid, realCallingPid, realCallingUid, callingPackage, intents, resolvedTypes, resultTo, options, checkTargetUser(userId, validateIncomingUser, Binder.getCallingPid(), Binder.getCallingUid(), "startActivityInPackage"), "startActivityInPackage", originatingPendingIntent, allowBackgroundActivityStart);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:100:0x01a9, code lost:
        return 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:101:0x01aa, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:102:0x01ab, code lost:
        monitor-exit(r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:103:0x01ac, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:104:0x01af, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:105:0x01b0, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:106:0x01b2, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:74:0x0148, code lost:
        r19 = r10;
        r0 = new com.android.server.wm.ActivityRecord[1];
        r13 = r1.mService.mGlobalLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x015c, code lost:
        monitor-enter(r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:?, code lost:
        com.android.server.wm.WindowManagerService.boostPriorityForLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:78:0x0160, code lost:
        r14 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:81:0x0163, code lost:
        if (r14 >= r12.length) goto L_0x01a0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x0165, code lost:
        r0 = r12[r14].setOutActivity(r0).execute();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:83:0x016f, code lost:
        if (r0 >= 0) goto L_0x018c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x0171, code lost:
        r15 = r14 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x0174, code lost:
        if (r15 >= r12.length) goto L_0x0184;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x0176, code lost:
        r1.mFactory.recycle(r12[r15]);
        r15 = r15 + 1;
        r1 = r26;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:88:0x0184, code lost:
        monitor-exit(r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:89:0x0185, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
        android.os.Binder.restoreCallingIdentity(r19);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:0x018b, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:92:0x018f, code lost:
        if (r0[0] == null) goto L_0x0196;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:93:0x0191, code lost:
        r1 = r0[0].appToken;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:94:0x0196, code lost:
        r1 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:95:0x0198, code lost:
        r14 = r14 + 1;
        r1 = r26;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:96:0x01a0, code lost:
        monitor-exit(r13);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:98:?, code lost:
        com.android.server.wm.WindowManagerService.resetPriorityAfterLockedSection();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:99:0x01a4, code lost:
        android.os.Binder.restoreCallingIdentity(r19);
     */
    public int startActivities(IApplicationThread caller, int callingUid, int incomingRealCallingPid, int incomingRealCallingUid, String callingPackage, Intent[] intents, String[] resolvedTypes, IBinder resultTo, SafeActivityOptions options, int userId, String reason, PendingIntentRecord originatingPendingIntent, boolean allowBackgroundActivityStart) {
        int realCallingPid;
        int realCallingUid;
        int callingUid2;
        int callingUid3;
        long origId;
        Throwable th;
        ActivityStartController activityStartController = this;
        IApplicationThread iApplicationThread = caller;
        if (intents == null) {
            throw new NullPointerException("intents is null");
        } else if (resolvedTypes == null) {
            throw new NullPointerException("resolvedTypes is null");
        } else if (intents.length == resolvedTypes.length) {
            if (incomingRealCallingPid != 0) {
                realCallingPid = incomingRealCallingPid;
            } else {
                realCallingPid = Binder.getCallingPid();
            }
            if (incomingRealCallingUid != -1) {
                realCallingUid = incomingRealCallingUid;
            } else {
                realCallingUid = Binder.getCallingUid();
            }
            if (callingUid >= 0) {
                callingUid3 = -1;
                callingUid2 = callingUid;
            } else if (iApplicationThread == null) {
                callingUid3 = realCallingPid;
                callingUid2 = realCallingUid;
            } else {
                callingUid2 = -1;
                callingUid3 = -1;
            }
            long origId2 = Binder.clearCallingIdentity();
            try {
                Intent[] intents2 = (Intent[]) ArrayUtils.filterNotNull(intents, $$Lambda$ActivityStartController$6bTAPCVeDq_D4Y53Y5WNfMK4xBE.INSTANCE);
                try {
                    ActivityStarter[] starters = new ActivityStarter[intents2.length];
                    int i = 0;
                    while (true) {
                        SafeActivityOptions checkedOptions = null;
                        if (i >= intents2.length) {
                            break;
                        }
                        try {
                            Intent intent = intents2[i];
                            if (!intent.hasFileDescriptors()) {
                                Intent intent2 = new Intent(intent);
                                intent2.addHwFlags(524288);
                                ActivityInfo aInfo = activityStartController.mService.mAmInternal.getActivityInfoForUser(activityStartController.mSupervisor.resolveActivity(intent2, resolvedTypes[i], 0, null, userId, ActivityStarter.computeResolveFilterUid(callingUid2, realCallingUid, -10000)), userId);
                                if (aInfo != null) {
                                    try {
                                        if ((aInfo.applicationInfo.privateFlags & 2) != 0) {
                                            throw new IllegalArgumentException("FLAG_CANT_SAVE_STATE not supported here");
                                        }
                                    } catch (Throwable th2) {
                                        th = th2;
                                        origId = origId2;
                                        Binder.restoreCallingIdentity(origId);
                                        throw th;
                                    }
                                }
                                boolean top = i == intents2.length - 1;
                                if (top) {
                                    checkedOptions = options;
                                }
                                origId = origId2;
                                try {
                                    try {
                                    } catch (Throwable th3) {
                                        th = th3;
                                        Binder.restoreCallingIdentity(origId);
                                        throw th;
                                    }
                                } catch (Throwable th4) {
                                    th = th4;
                                    Binder.restoreCallingIdentity(origId);
                                    throw th;
                                }
                                try {
                                    starters[i] = activityStartController.obtainStarter(intent2, reason).setCaller(iApplicationThread).setResolvedType(resolvedTypes[i]).setActivityInfo(aInfo).setResultTo(resultTo).setRequestCode(-1).setCallingPid(callingUid3).setCallingUid(callingUid2).setCallingPackage(callingPackage).setRealCallingPid(realCallingPid).setRealCallingUid(realCallingUid).setActivityOptions(checkedOptions).setComponentSpecified(intent2.getComponent() != null).setAllowPendingRemoteAnimationRegistryLookup(top).setOriginatingPendingIntent(originatingPendingIntent).setAllowBackgroundActivityStart(allowBackgroundActivityStart);
                                    i++;
                                    iApplicationThread = caller;
                                    intents2 = intents2;
                                    origId2 = origId;
                                } catch (Throwable th5) {
                                    th = th5;
                                    Binder.restoreCallingIdentity(origId);
                                    throw th;
                                }
                            } else {
                                throw new IllegalArgumentException("File descriptors passed in Intent");
                            }
                        } catch (Throwable th6) {
                            th = th6;
                            origId = origId2;
                            Binder.restoreCallingIdentity(origId);
                            throw th;
                        }
                    }
                } catch (Throwable th7) {
                    th = th7;
                    origId = origId2;
                    Binder.restoreCallingIdentity(origId);
                    throw th;
                }
            } catch (Throwable th8) {
                th = th8;
                origId = origId2;
                Binder.restoreCallingIdentity(origId);
                throw th;
            }
        } else {
            throw new IllegalArgumentException("intents are length different than resolvedTypes");
        }
    }

    static /* synthetic */ Intent[] lambda$startActivities$0(int x$0) {
        return new Intent[x$0];
    }

    /* access modifiers changed from: package-private */
    public void schedulePendingActivityLaunches(long delayMs) {
        this.mHandler.removeMessages(1);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1), delayMs);
    }

    /* access modifiers changed from: package-private */
    public void doPendingActivityLaunches(boolean doResume) {
        while (!this.mPendingActivityLaunches.isEmpty()) {
            boolean resume = false;
            ActivityStackSupervisor.PendingActivityLaunch pal = this.mPendingActivityLaunches.remove(0);
            if (doResume && this.mPendingActivityLaunches.isEmpty()) {
                resume = true;
            }
            try {
                obtainStarter(null, "pendingActivityLaunch").startResolvedActivity(pal.r, pal.sourceRecord, null, null, pal.startFlags, resume, pal.r.pendingOptions, null);
            } catch (Exception e) {
                Slog.e(TAG, "Exception during pending activity launch pal=" + pal, e);
                pal.sendErrorResult(e.getMessage());
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void addPendingActivityLaunch(ActivityStackSupervisor.PendingActivityLaunch launch) {
        this.mPendingActivityLaunches.add(launch);
    }

    /* access modifiers changed from: package-private */
    public boolean clearPendingActivityLaunches(String packageName) {
        int pendingLaunches = this.mPendingActivityLaunches.size();
        for (int palNdx = pendingLaunches - 1; palNdx >= 0; palNdx--) {
            ActivityRecord r = this.mPendingActivityLaunches.get(palNdx).r;
            if (r != null && r.packageName.equals(packageName)) {
                this.mPendingActivityLaunches.remove(palNdx);
            }
        }
        return this.mPendingActivityLaunches.size() < pendingLaunches;
    }

    /* access modifiers changed from: package-private */
    public void registerRemoteAnimationForNextActivityStart(String packageName, RemoteAnimationAdapter adapter) {
        this.mPendingRemoteAnimationRegistry.addPendingAnimation(packageName, adapter);
    }

    /* access modifiers changed from: package-private */
    public PendingRemoteAnimationRegistry getPendingRemoteAnimationRegistry() {
        return this.mPendingRemoteAnimationRegistry;
    }

    /* access modifiers changed from: package-private */
    public void dump(PrintWriter pw, String prefix, String dumpPackage) {
        ActivityRecord activityRecord;
        pw.print(prefix);
        pw.print("mLastHomeActivityStartResult=");
        pw.println(this.mLastHomeActivityStartResult);
        if (this.mLastHomeActivityStartRecord != null) {
            pw.print(prefix);
            pw.println("mLastHomeActivityStartRecord:");
            this.mLastHomeActivityStartRecord.dump(pw, prefix + "  ");
        }
        boolean dump = true;
        boolean dumpPackagePresent = dumpPackage != null;
        ActivityStarter activityStarter = this.mLastStarter;
        if (activityStarter != null) {
            if (dumpPackagePresent && !activityStarter.relatedToPackage(dumpPackage) && ((activityRecord = this.mLastHomeActivityStartRecord) == null || !dumpPackage.equals(activityRecord.packageName))) {
                dump = false;
            }
            if (dump) {
                pw.print(prefix);
                this.mLastStarter.dump(pw, prefix + "  ");
                if (dumpPackagePresent) {
                    return;
                }
            }
        }
        if (dumpPackagePresent) {
            pw.print(prefix);
            pw.println("(nothing)");
        }
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        Iterator<ActivityStackSupervisor.PendingActivityLaunch> it = this.mPendingActivityLaunches.iterator();
        while (it.hasNext()) {
            it.next().r.writeIdentifierToProto(proto, fieldId);
        }
    }
}
