package com.android.server.am;

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
import android.view.RemoteAnimationAdapter;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.HwServiceFactory;
import com.android.server.am.ActivityStackSupervisor;
import com.android.server.am.ActivityStarter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class ActivityStartController {
    private static final int DO_PENDING_ACTIVITY_LAUNCHES_MSG = 1;
    private static final String TAG = "ActivityManager";
    String mCurActivityPkName;
    private final ActivityStarter.Factory mFactory;
    private final Handler mHandler;
    private ActivityRecord mLastHomeActivityStartRecord;
    private int mLastHomeActivityStartResult;
    private ActivityStarter mLastStarter;
    private final ArrayList<ActivityStackSupervisor.PendingActivityLaunch> mPendingActivityLaunches;
    private final PendingRemoteAnimationRegistry mPendingRemoteAnimationRegistry;
    /* access modifiers changed from: private */
    public final ActivityManagerService mService;
    private final ActivityStackSupervisor mSupervisor;
    private ActivityRecord[] tmpOutRecord;

    private final class StartHandler extends Handler {
        public StartHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                synchronized (ActivityStartController.this.mService) {
                    try {
                        ActivityManagerService.boostPriorityForLockedSection();
                        ActivityStartController.this.doPendingActivityLaunches(true);
                    } catch (Throwable th) {
                        while (true) {
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            throw th;
                        }
                    }
                }
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    ActivityStartController(ActivityManagerService service) {
        this(service, service.mStackSupervisor, new ActivityStarter.DefaultFactory(service, service.mStackSupervisor, HwServiceFactory.createActivityStartInterceptor(service, service.mStackSupervisor)));
    }

    @VisibleForTesting
    ActivityStartController(ActivityManagerService service, ActivityStackSupervisor supervisor, ActivityStarter.Factory factory) {
        this.tmpOutRecord = new ActivityRecord[1];
        this.mPendingActivityLaunches = new ArrayList<>();
        this.mCurActivityPkName = BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS;
        this.mService = service;
        this.mSupervisor = supervisor;
        this.mHandler = new StartHandler(this.mService.mHandlerThread.getLooper());
        this.mFactory = factory;
        this.mFactory.setController(this);
        this.mPendingRemoteAnimationRegistry = new PendingRemoteAnimationRegistry(service, service.mHandler);
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
        if (this.mLastStarter != null) {
            this.mLastStarter.postStartActivityProcessing(r, result, targetStack);
        }
    }

    /* access modifiers changed from: package-private */
    public void startHomeActivity(Intent intent, ActivityInfo aInfo, String reason) {
        this.mSupervisor.moveHomeStackTaskToTop(reason);
        this.mLastHomeActivityStartResult = obtainStarter(intent, "startHomeActivity: " + reason).setOutActivity(this.tmpOutRecord).setCallingUid(0).setActivityInfo(aInfo).execute();
        this.mLastHomeActivityStartRecord = this.tmpOutRecord[0];
        if (this.mSupervisor.inResumeTopActivity) {
            this.mSupervisor.scheduleResumeTopActivities();
        }
    }

    /* access modifiers changed from: package-private */
    public void startSetupActivity() {
        String vers;
        if (!this.mService.getCheckedForSetup()) {
            ContentResolver resolver = this.mService.mContext.getContentResolver();
            if (!(this.mService.mFactoryTest == 1 || Settings.Global.getInt(resolver, "device_provisioned", 0) == 0)) {
                this.mService.setCheckedForSetup(true);
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
            return this.mService.mUserController.handleIncomingUser(realCallingPid, realCallingUid, targetUserId, false, 2, reason, null);
        }
        this.mService.mUserController.ensureNotSpecialUser(targetUserId);
        return targetUserId;
    }

    /* access modifiers changed from: package-private */
    public final int startActivityInPackage(int uid, int realCallingPid, int realCallingUid, String callingPackage, Intent intent, String resolvedType, IBinder resultTo, String resultWho, int requestCode, int startFlags, SafeActivityOptions options, int userId, TaskRecord inTask, String reason, boolean validateIncomingUser) {
        return obtainStarter(intent, reason).setCallingUid(uid).setRealCallingPid(realCallingPid).setRealCallingUid(realCallingUid).setCallingPackage(callingPackage).setResolvedType(resolvedType).setResultTo(resultTo).setResultWho(resultWho).setRequestCode(requestCode).setStartFlags(startFlags).setActivityOptions(options).setMayWait(checkTargetUser(userId, validateIncomingUser, realCallingPid, realCallingUid, reason)).setInTask(inTask).execute();
    }

    /* access modifiers changed from: package-private */
    public final int startActivitiesInPackage(int uid, String callingPackage, Intent[] intents, String[] resolvedTypes, IBinder resultTo, SafeActivityOptions options, int userId, boolean validateIncomingUser) {
        return startActivities(null, uid, callingPackage, intents, resolvedTypes, resultTo, options, checkTargetUser(userId, validateIncomingUser, Binder.getCallingPid(), Binder.getCallingUid(), "startActivityInPackage"), "startActivityInPackage");
    }

    /* access modifiers changed from: package-private */
    public int startActivities(IApplicationThread caller, int callingUid, String callingPackage, Intent[] intents, String[] resolvedTypes, IBinder resultTo, SafeActivityOptions options, int userId, String reason) {
        int callingPid;
        int callingUid2;
        long origId;
        long origId2;
        long origId3;
        long origId4;
        long origId5;
        ActivityRecord[] outActivity;
        long origId6;
        IBinder iBinder;
        ActivityStartController activityStartController = this;
        IApplicationThread iApplicationThread = caller;
        Intent[] intentArr = intents;
        String[] strArr = resolvedTypes;
        if (intentArr == null) {
            String str = reason;
            throw new NullPointerException("intents is null");
        } else if (strArr == null) {
            String str2 = reason;
            throw new NullPointerException("resolvedTypes is null");
        } else if (intentArr.length == strArr.length) {
            int realCallingPid = Binder.getCallingPid();
            int realCallingUid = Binder.getCallingUid();
            if (callingUid >= 0) {
                callingPid = -1;
                callingUid2 = callingUid;
            } else if (iApplicationThread == null) {
                callingPid = realCallingPid;
                callingUid2 = realCallingUid;
            } else {
                callingUid2 = -1;
                callingPid = -1;
            }
            long origId7 = Binder.clearCallingIdentity();
            try {
                synchronized (activityStartController.mService) {
                    try {
                        ActivityManagerService.boostPriorityForLockedSection();
                        boolean z = true;
                        ActivityRecord[] outActivity2 = new ActivityRecord[1];
                        IBinder resultTo2 = resultTo;
                        int i = 0;
                        while (i < intentArr.length) {
                            try {
                                Intent intent = intentArr[i];
                                if (intent == null) {
                                    origId5 = origId7;
                                    outActivity = outActivity2;
                                    String str3 = reason;
                                } else {
                                    if (intent != null) {
                                        try {
                                            if (!intent.hasFileDescriptors()) {
                                                origId6 = origId7;
                                            } else {
                                                origId6 = origId7;
                                                try {
                                                    throw new IllegalArgumentException("File descriptors passed in Intent");
                                                } catch (Throwable th) {
                                                    th = th;
                                                    String str4 = reason;
                                                    origId4 = origId6;
                                                    origId2 = origId4;
                                                    origId2 = origId4;
                                                    origId3 = origId4;
                                                    ActivityManagerService.resetPriorityAfterLockedSection();
                                                    throw th;
                                                }
                                            }
                                        } catch (Throwable th2) {
                                            th = th2;
                                            String str5 = reason;
                                            origId4 = origId7;
                                            origId2 = origId4;
                                            origId2 = origId4;
                                            origId3 = origId4;
                                            ActivityManagerService.resetPriorityAfterLockedSection();
                                            throw th;
                                        }
                                    } else {
                                        origId6 = origId7;
                                    }
                                    origId2 = iApplicationThread;
                                    boolean componentSpecified = intent.getComponent() != null ? z : false;
                                    Intent intent2 = new Intent(intent);
                                    activityStartController.mService.addCallerToIntent(intent2, iApplicationThread);
                                    ActivityRecord[] outActivity3 = outActivity2;
                                    ActivityInfo aInfo = activityStartController.mService.getActivityInfoForUser(activityStartController.mSupervisor.resolveActivity(intent2, strArr[i], 0, null, userId, ActivityStarter.computeResolveFilterUid(callingUid2, realCallingUid, -10000)), userId);
                                    if (aInfo != null) {
                                        if ((aInfo.applicationInfo.privateFlags & 2) != 0) {
                                            throw new IllegalArgumentException("FLAG_CANT_SAVE_STATE not supported here");
                                        }
                                    }
                                    try {
                                        boolean top = i == intentArr.length - 1;
                                        try {
                                            origId2 = iApplicationThread;
                                            Intent intent3 = intent2;
                                            SafeActivityOptions checkedOptions = top ? options : null;
                                            SafeActivityOptions safeActivityOptions = checkedOptions;
                                            outActivity = outActivity3;
                                            int res = activityStartController.obtainStarter(intent2, reason).setCaller(iApplicationThread).setResolvedType(strArr[i]).setActivityInfo(aInfo).setResultTo(resultTo2).setRequestCode(-1).setCallingPid(callingPid).setCallingUid(callingUid2).setCallingPackage(callingPackage).setRealCallingPid(realCallingPid).setRealCallingUid(realCallingUid).setActivityOptions(checkedOptions).setComponentSpecified(componentSpecified).setOutActivity(outActivity).setAllowPendingRemoteAnimationRegistryLookup(top).execute();
                                            if (res < 0) {
                                                ActivityManagerService.resetPriorityAfterLockedSection();
                                                Binder.restoreCallingIdentity(origId6);
                                                return res;
                                            }
                                            long origId8 = origId6;
                                            origId2 = origId8;
                                            if (outActivity[0] != null) {
                                                int i2 = res;
                                                iBinder = outActivity[0].appToken;
                                            } else {
                                                iBinder = null;
                                            }
                                            resultTo2 = iBinder;
                                            origId5 = origId8;
                                        } catch (Throwable th3) {
                                            th = th3;
                                            origId4 = origId2;
                                            origId2 = origId4;
                                            origId2 = origId4;
                                            origId3 = origId4;
                                            ActivityManagerService.resetPriorityAfterLockedSection();
                                            throw th;
                                        }
                                    } catch (Throwable th4) {
                                        th = th4;
                                        String str6 = reason;
                                        origId4 = origId6;
                                        origId2 = origId4;
                                        origId2 = origId4;
                                        origId3 = origId4;
                                        ActivityManagerService.resetPriorityAfterLockedSection();
                                        throw th;
                                    }
                                }
                                i++;
                                outActivity2 = outActivity;
                                origId7 = origId5;
                                activityStartController = this;
                                iApplicationThread = caller;
                                intentArr = intents;
                                z = true;
                            } catch (Throwable th5) {
                                th = th5;
                                String str7 = reason;
                                origId4 = origId7;
                                origId2 = origId4;
                                origId2 = origId4;
                                origId3 = origId4;
                                ActivityManagerService.resetPriorityAfterLockedSection();
                                throw th;
                            }
                        }
                        String str8 = reason;
                        long origId9 = origId7;
                        try {
                            origId3 = origId9;
                            ActivityManagerService.resetPriorityAfterLockedSection();
                            origId3 = origId9;
                            Binder.restoreCallingIdentity(origId9);
                            return 0;
                        } catch (Throwable th6) {
                            th = th6;
                            origId = origId3;
                            Binder.restoreCallingIdentity(origId);
                            throw th;
                        }
                    } catch (Throwable th7) {
                        th = th7;
                        String str9 = reason;
                        origId4 = origId7;
                        IBinder iBinder2 = resultTo;
                        origId2 = origId4;
                        origId2 = origId4;
                        origId3 = origId4;
                        ActivityManagerService.resetPriorityAfterLockedSection();
                        throw th;
                    }
                }
            } catch (Throwable th8) {
                th = th8;
                String str10 = reason;
                origId = origId7;
                IBinder iBinder3 = resultTo;
                Binder.restoreCallingIdentity(origId);
                throw th;
            }
        } else {
            String str11 = reason;
            throw new IllegalArgumentException("intents are length different than resolvedTypes");
        }
    }

    /* access modifiers changed from: package-private */
    public void schedulePendingActivityLaunches(long delayMs) {
        this.mHandler.removeMessages(1);
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1), delayMs);
    }

    /* access modifiers changed from: package-private */
    public void doPendingActivityLaunches(boolean doResume) {
        while (!this.mPendingActivityLaunches.isEmpty()) {
            boolean z = false;
            ActivityStackSupervisor.PendingActivityLaunch pal = this.mPendingActivityLaunches.remove(0);
            if (doResume && this.mPendingActivityLaunches.isEmpty()) {
                z = true;
            }
            boolean resume = z;
            try {
                obtainStarter(null, "pendingActivityLaunch").startResolvedActivity(pal.r, pal.sourceRecord, null, null, pal.startFlags, resume, null, null, null);
            } catch (Exception e) {
                Slog.e("ActivityManager", "Exception during pending activity launch pal=" + pal, e);
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
        pw.print(prefix);
        pw.print("mLastHomeActivityStartResult=");
        pw.println(this.mLastHomeActivityStartResult);
        if (this.mLastHomeActivityStartRecord != null) {
            pw.print(prefix);
            pw.println("mLastHomeActivityStartRecord:");
            this.mLastHomeActivityStartRecord.dump(pw, prefix + "  ");
        }
        boolean dump = false;
        boolean dumpPackagePresent = dumpPackage != null;
        if (this.mLastStarter != null) {
            if (!dumpPackagePresent || this.mLastStarter.relatedToPackage(dumpPackage) || (this.mLastHomeActivityStartRecord != null && dumpPackage.equals(this.mLastHomeActivityStartRecord.packageName))) {
                dump = true;
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
}
