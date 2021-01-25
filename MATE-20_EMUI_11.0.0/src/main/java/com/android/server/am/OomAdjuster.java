package com.android.server.am;

import android.app.ActivityManager;
import android.hardware.biometrics.face.V1_0.FaceAcquiredInfo;
import android.net.INetd;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManagerInternal;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.Trace;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.HwLog;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.BatteryService;
import com.android.server.LocalServices;
import com.android.server.ServiceThread;
import com.android.server.SystemService;
import com.android.server.am.ActivityManagerService;
import com.android.server.connectivity.NetworkAgentInfo;
import com.android.server.pm.DumpState;
import com.android.server.usage.AppStandbyController;
import com.android.server.wm.ActivityServiceConnectionsHolder;
import com.android.server.wm.ActivityTaskManagerDebugConfig;
import com.android.server.wm.WindowProcessController;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

public final class OomAdjuster {
    static final int MSG_REQ_PROCESS_GRP_CHANGE = 10;
    static final String OOM_ADJ_REASON_ACTIVITY = "updateOomAdj_activityChange";
    static final String OOM_ADJ_REASON_BIND_SERVICE = "updateOomAdj_bindService";
    static final String OOM_ADJ_REASON_FINISH_RECEIVER = "updateOomAdj_finishReceiver";
    static final String OOM_ADJ_REASON_GET_PROVIDER = "updateOomAdj_getProvider";
    static final String OOM_ADJ_REASON_METHOD = "updateOomAdj";
    static final String OOM_ADJ_REASON_NONE = "updateOomAdj_meh";
    static final String OOM_ADJ_REASON_PROCESS_BEGIN = "updateOomAdj_processBegin";
    static final String OOM_ADJ_REASON_PROCESS_END = "updateOomAdj_processEnd";
    static final String OOM_ADJ_REASON_REMOVE_PROVIDER = "updateOomAdj_removeProvider";
    static final String OOM_ADJ_REASON_START_RECEIVER = "updateOomAdj_startReceiver";
    static final String OOM_ADJ_REASON_START_SERVICE = "updateOomAdj_startService";
    static final String OOM_ADJ_REASON_UI_VISIBILITY = "updateOomAdj_uiVisibility";
    static final String OOM_ADJ_REASON_UNBIND_SERVICE = "updateOomAdj_unbindService";
    static final String OOM_ADJ_REASON_WHITELIST = "updateOomAdj_whitelistChange";
    private static final String TAG = "OomAdjuster";
    ActiveUids mActiveUids;
    int mAdjSeq = 0;
    AppCompactor mAppCompact;
    ActivityManagerConstants mConstants;
    PowerManagerInternal mLocalPowerManager;
    int mNewNumAServiceProcs = 0;
    int mNewNumServiceProcs = 0;
    int mNumCachedHiddenProcs = 0;
    int mNumNonCachedProcs = 0;
    int mNumServiceProcs = 0;
    private final Handler mProcessGroupHandler;
    private final ProcessList mProcessList;
    private final ActivityManagerService mService;
    private final ArraySet<BroadcastQueue> mTmpBroadcastQueue = new ArraySet<>();
    private final ComputeOomAdjWindowCallback mTmpComputeOomAdjWindowCallback = new ComputeOomAdjWindowCallback();
    final long[] mTmpLong = new long[3];

    OomAdjuster(ActivityManagerService service, ProcessList processList, ActiveUids activeUids) {
        this.mService = service;
        this.mProcessList = processList;
        this.mActiveUids = activeUids;
        this.mLocalPowerManager = (PowerManagerInternal) LocalServices.getService(PowerManagerInternal.class);
        this.mConstants = this.mService.mConstants;
        this.mAppCompact = new AppCompactor(this.mService);
        ServiceThread adjusterThread = new ServiceThread(TAG, -10, false);
        adjusterThread.start();
        Process.setThreadGroupAndCpuset(adjusterThread.getThreadId(), 5);
        this.mProcessGroupHandler = new Handler(adjusterThread.getLooper(), new Handler.Callback() {
            /* class com.android.server.am.$$Lambda$OomAdjuster$TycrMfpYu_LfNv_I2DM8ANoONEE */

            @Override // android.os.Handler.Callback
            public final boolean handleMessage(Message message) {
                return OomAdjuster.this.lambda$new$0$OomAdjuster(message);
            }
        });
    }

    public /* synthetic */ boolean lambda$new$0$OomAdjuster(Message msg) {
        Bundle data;
        int curSchedGroup;
        if (isGroupRequestMsg(msg)) {
            return true;
        }
        Trace.traceBegin(64, "setProcessGroup");
        int pid = msg.arg1;
        int group = msg.arg2;
        try {
            Process.setProcessGroup(pid, group);
        } catch (Exception e) {
            if (ActivityManagerDebugConfig.DEBUG_ALL) {
                Slog.w(TAG, "Failed setting process group of " + pid + " to " + group, e);
            }
        } catch (Throwable th) {
            Trace.traceEnd(64);
            throw th;
        }
        Trace.traceEnd(64);
        if (!(!this.mService.mCpusetSwitch || (data = msg.getData()) == null || msg.obj == null || !(msg.obj instanceof ProcessRecord) || (curSchedGroup = data.getInt("curSchedGroup", -10000)) == -10000)) {
            ProcessRecord app = (ProcessRecord) msg.obj;
            this.mService.mDAProxy.notifyProcessGroupChangeCpu(app.pid, app.uid, app.renderThreadTid, curSchedGroup);
        }
        return true;
    }

    private boolean isGroupRequestMsg(Message msg) {
        if (!this.mService.mCpusetSwitch || msg.what != 10) {
            return false;
        }
        Bundle data = msg.getData();
        if (data == null) {
            return true;
        }
        int pid = msg.arg1;
        int newGroup = msg.arg2;
        int oldGroup = data.getInt("oldGroup", -1);
        int isLimit = data.getInt("isLimit", 0);
        try {
            if (Process.getProcessGroup(pid) != oldGroup) {
                return true;
            }
            if (newGroup == 8 && isLimit == 1) {
                newGroup = 30;
            }
            Process.setProcessGroup(pid, newGroup);
            return true;
        } catch (Exception e) {
            if (ActivityManagerDebugConfig.DEBUG_ALL) {
                Slog.w(TAG, "Failed request process group of " + pid + " to " + newGroup, e);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void requestProcessGroupChange(int pid, int oldGroup, int newGroup, int isLimit) {
        if (pid >= 0) {
            if (ActivityManagerDebugConfig.DEBUG_ALL) {
                Slog.i(TAG, "Request process group of " + pid + " to " + newGroup);
            }
            Message msg = this.mProcessGroupHandler.obtainMessage(10, pid, newGroup);
            Bundle data = new Bundle();
            data.putInt("oldGroup", oldGroup);
            data.putInt("isLimit", isLimit);
            msg.setData(data);
            this.mProcessGroupHandler.sendMessage(msg);
        }
    }

    /* access modifiers changed from: package-private */
    public void initSettings() {
        this.mAppCompact.init();
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public boolean updateOomAdjLocked(ProcessRecord app, boolean oomAdjAll, String oomAdjReason) {
        if (app == null) {
            return false;
        }
        ProcessRecord TOP_APP = this.mService.getTopAppLocked();
        boolean wasCached = app.cached;
        this.mAdjSeq++;
        boolean success = updateOomAdjLocked(app, app.getCurRawAdj() >= 900 ? app.getCurRawAdj() : 1001, TOP_APP, false, SystemClock.uptimeMillis());
        if (oomAdjAll && (wasCached != app.cached || app.getCurRawAdj() == 1001)) {
            updateOomAdjLocked(oomAdjReason);
        }
        return success;
    }

    @GuardedBy({"mService"})
    private final boolean updateOomAdjLocked(ProcessRecord app, int cachedAdj, ProcessRecord TOP_APP, boolean doingAll, long now) {
        if (app.thread == null) {
            return false;
        }
        computeOomAdjLocked(app, cachedAdj, TOP_APP, doingAll, now, false);
        return applyOomAdjLocked(app, doingAll, now, SystemClock.elapsedRealtime());
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v75, resolved type: com.android.server.am.ProcessRecord */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX INFO: Multiple debug info for r2v7 int: [D('numCached' int), D('lastCachedGroupUid' int)] */
    /* JADX INFO: Multiple debug info for r5v6 int: [D('numCached' int), D('numTrimming' int)] */
    /* JADX INFO: Multiple debug info for r7v26 'lastCachedGroupImportance'  int: [D('lastCachedGroupImportance' int), D('cachedFactor' int)] */
    /* JADX INFO: Multiple debug info for r1v43 int: [D('retryCycles' boolean), D('stepEmpty' int)] */
    /* JADX INFO: Multiple debug info for r7v38 'cachedFactor'  int: [D('cachedFactor' int), D('emptyFactor' int)] */
    /* JADX WARN: Type inference failed for: r10v7 */
    /* JADX WARN: Type inference failed for: r10v8, types: [boolean, int] */
    /* JADX WARN: Type inference failed for: r10v21 */
    /* access modifiers changed from: package-private */
    /* JADX WARNING: Unknown variable types count: 1 */
    @GuardedBy({"mService"})
    public void updateOomAdjLocked(String oomAdjReason) {
        int emptyProcessLimit;
        int numTrimming;
        boolean z;
        int uidChange;
        int numEmpty;
        int numEmpty2;
        int emptyProcessLimit2;
        ProcessRecord app;
        int lastCachedGroup;
        int lastCachedGroupUid;
        ProcessRecord app2;
        int numEmpty3;
        int curCachedAdj;
        int lastCachedGroupImportance;
        int lastCachedGroup2;
        int lastCachedGroupUid2;
        int curEmptyAdj;
        int i;
        int cycleCount;
        int lastCachedGroupImportance2;
        int nextCachedAdj;
        int lastCachedGroupUid3;
        boolean z2;
        int emptyFactor;
        int cachedFactor;
        int emptyProcessLimit3;
        int cachedProcessLimit;
        long nowElapsed;
        int numEmptyProcs;
        int numEmptyProcs2;
        int emptyFactor2;
        int lastCachedGroupUid4;
        int lastCachedGroupImportance3;
        int lastCachedGroup3;
        int lastCachedGroupImportance4;
        int nextCachedAdj2;
        int lastCachedGroupImportance5;
        int cachedFactor2;
        int lastCachedGroupImportance6;
        boolean retryCycles;
        Trace.traceBegin(64, oomAdjReason);
        this.mService.mOomAdjProfiler.oomAdjStarted();
        ProcessRecord TOP_APP = this.mService.getTopAppLocked();
        long now = SystemClock.uptimeMillis();
        long nowElapsed2 = SystemClock.elapsedRealtime();
        long oldTime = now - ProcessList.MAX_EMPTY_TIME;
        int emptyFactor3 = this.mProcessList.getLruSizeLocked();
        for (int i2 = this.mActiveUids.size() - 1; i2 >= 0; i2--) {
            this.mActiveUids.valueAt(i2).reset();
        }
        if (this.mService.mAtmInternal != null) {
            this.mService.mAtmInternal.rankTaskLayersIfNeeded();
        }
        this.mAdjSeq++;
        this.mNewNumServiceProcs = 0;
        this.mNewNumAServiceProcs = 0;
        int emptyProcessLimit4 = this.mConstants.CUR_MAX_EMPTY_PROCESSES;
        int cachedProcessLimit2 = this.mConstants.CUR_MAX_CACHED_PROCESSES - emptyProcessLimit4;
        int numEmptyProcs3 = (emptyFactor3 - this.mNumNonCachedProcs) - this.mNumCachedHiddenProcs;
        int numEmptyProcs4 = numEmptyProcs3 > cachedProcessLimit2 ? cachedProcessLimit2 : numEmptyProcs3;
        int emptyFactor4 = ((numEmptyProcs4 + 10) - 1) / 10;
        if (emptyFactor4 < 1) {
            emptyFactor4 = 1;
        }
        int i3 = this.mNumCachedHiddenProcs;
        int cachedFactor3 = (i3 > 0 ? (i3 + 10) - 1 : 1) / 10;
        if (cachedFactor3 < 1) {
            cachedFactor3 = 1;
        }
        int stepCached = -1;
        int stepEmpty = -1;
        int numCachedExtraGroup = 0;
        this.mNumNonCachedProcs = 0;
        this.mNumCachedHiddenProcs = 0;
        int curCachedAdj2 = 900;
        int nextCachedAdj3 = 900 + 10;
        int curCachedImpAdj = 0;
        int nextEmptyAdj = 905 + 10;
        boolean retryCycles2 = false;
        int i4 = emptyFactor3 - 1;
        while (true) {
            emptyProcessLimit = emptyProcessLimit4;
            if (i4 < 0) {
                break;
            }
            ProcessRecord app3 = this.mProcessList.mLruProcesses.get(i4);
            app3.containsCycle = false;
            app3.setCurRawProcState(20);
            app3.setCurRawAdj(NetworkAgentInfo.EVENT_NETWORK_LINGER_COMPLETE);
            i4--;
            curCachedAdj2 = curCachedAdj2;
            emptyProcessLimit4 = emptyProcessLimit;
        }
        int i5 = emptyFactor3 - 1;
        int lastCachedGroup4 = 0;
        int lastCachedGroupImportance7 = 0;
        int lastCachedGroupUid5 = 0;
        int nextCachedAdj4 = nextCachedAdj3;
        int curEmptyAdj2 = 905;
        int nextEmptyAdj2 = nextEmptyAdj;
        int curCachedAdj3 = curCachedAdj2;
        while (i5 >= 0) {
            ProcessRecord app4 = this.mProcessList.mLruProcesses.get(i5);
            if (app4.killedByAm || app4.thread == null) {
                cachedProcessLimit = cachedProcessLimit2;
                cachedFactor = cachedFactor3;
                nowElapsed = nowElapsed2;
                numEmptyProcs = numEmptyProcs4;
                emptyProcessLimit3 = emptyProcessLimit;
                numEmptyProcs2 = emptyFactor3;
                emptyFactor2 = emptyFactor4;
                nextEmptyAdj2 = nextEmptyAdj2;
                curEmptyAdj2 = curEmptyAdj2;
                nextCachedAdj4 = nextCachedAdj4;
                curCachedAdj3 = curCachedAdj3;
                lastCachedGroup4 = lastCachedGroup4;
                lastCachedGroupUid5 = lastCachedGroupUid5;
                lastCachedGroupImportance7 = lastCachedGroupImportance7;
            } else {
                app4.procStateChanged = false;
                cachedProcessLimit = cachedProcessLimit2;
                emptyProcessLimit3 = emptyProcessLimit;
                cachedFactor = cachedFactor3;
                nowElapsed = nowElapsed2;
                numEmptyProcs = numEmptyProcs4;
                numEmptyProcs2 = emptyFactor3;
                computeOomAdjLocked(app4, NetworkAgentInfo.EVENT_NETWORK_LINGER_COMPLETE, TOP_APP, true, now, false);
                boolean retryCycles3 = retryCycles2 | app4.containsCycle;
                if (app4.curAdj >= 1001) {
                    switch (app4.getCurProcState()) {
                        case 17:
                        case 18:
                        case FaceAcquiredInfo.FACE_OBSCURED /* 19 */:
                            boolean inGroup = false;
                            if (app4.connectionGroup != 0) {
                                if (lastCachedGroupUid5 == app4.uid) {
                                    lastCachedGroup3 = lastCachedGroup4;
                                    if (lastCachedGroup3 == app4.connectionGroup) {
                                        lastCachedGroupUid4 = lastCachedGroupUid5;
                                        if (app4.connectionImportance > lastCachedGroupImportance7) {
                                            int lastCachedGroupImportance8 = app4.connectionImportance;
                                            nextCachedAdj2 = nextCachedAdj4;
                                            if (curCachedAdj3 >= nextCachedAdj2 || curCachedAdj3 >= 999) {
                                                lastCachedGroupImportance6 = lastCachedGroupImportance8;
                                                lastCachedGroupImportance4 = curCachedAdj3;
                                            } else {
                                                curCachedImpAdj++;
                                                lastCachedGroupImportance6 = lastCachedGroupImportance8;
                                                lastCachedGroupImportance4 = curCachedAdj3;
                                            }
                                        } else {
                                            lastCachedGroupImportance4 = curCachedAdj3;
                                            nextCachedAdj2 = nextCachedAdj4;
                                            lastCachedGroupImportance6 = lastCachedGroupImportance7;
                                        }
                                        inGroup = true;
                                        lastCachedGroupImportance3 = lastCachedGroupImportance6;
                                    } else {
                                        lastCachedGroupImportance4 = curCachedAdj3;
                                        nextCachedAdj2 = nextCachedAdj4;
                                    }
                                } else {
                                    lastCachedGroupImportance4 = curCachedAdj3;
                                    nextCachedAdj2 = nextCachedAdj4;
                                }
                                int lastCachedGroupUid6 = app4.uid;
                                lastCachedGroup3 = app4.connectionGroup;
                                lastCachedGroupImportance3 = app4.connectionImportance;
                                lastCachedGroupUid4 = lastCachedGroupUid6;
                            } else {
                                lastCachedGroupUid4 = lastCachedGroupUid5;
                                lastCachedGroup3 = lastCachedGroup4;
                                lastCachedGroupImportance3 = lastCachedGroupImportance7;
                                lastCachedGroupImportance4 = curCachedAdj3;
                                nextCachedAdj2 = nextCachedAdj4;
                            }
                            if (inGroup || lastCachedGroupImportance4 == nextCachedAdj2) {
                                lastCachedGroupImportance5 = lastCachedGroupImportance3;
                                cachedFactor2 = cachedFactor;
                                curCachedAdj3 = lastCachedGroupImportance4;
                            } else {
                                int stepCached2 = stepCached + 1;
                                curCachedImpAdj = 0;
                                lastCachedGroupImportance5 = lastCachedGroupImportance3;
                                cachedFactor2 = cachedFactor;
                                if (stepCached2 >= cachedFactor2) {
                                    stepCached = 0;
                                    curCachedAdj3 = nextCachedAdj2;
                                    int nextCachedAdj5 = nextCachedAdj2 + 10;
                                    nextCachedAdj2 = nextCachedAdj5 > 999 ? 999 : nextCachedAdj5;
                                } else {
                                    stepCached = stepCached2;
                                    curCachedAdj3 = lastCachedGroupImportance4;
                                }
                            }
                            app4.setCurRawAdj(curCachedAdj3 + curCachedImpAdj);
                            app4.curAdj = app4.modifyRawOomAdj(curCachedAdj3 + curCachedImpAdj);
                            boolean z3 = ActivityManagerDebugConfig.DEBUG_LRU;
                            retryCycles2 = retryCycles3;
                            nextCachedAdj4 = nextCachedAdj2;
                            lastCachedGroup4 = lastCachedGroup3;
                            cachedFactor = cachedFactor2;
                            lastCachedGroupUid5 = lastCachedGroupUid4;
                            lastCachedGroupImportance7 = lastCachedGroupImportance5;
                            emptyFactor2 = emptyFactor4;
                            continue;
                        default:
                            int curEmptyAdj3 = curEmptyAdj2;
                            int nextEmptyAdj3 = nextEmptyAdj2;
                            if (curEmptyAdj3 != nextEmptyAdj3) {
                                retryCycles = retryCycles3;
                                int stepEmpty2 = stepEmpty + 1;
                                cachedFactor = cachedFactor;
                                emptyFactor2 = emptyFactor4;
                                if (stepEmpty2 >= emptyFactor2) {
                                    stepEmpty = 0;
                                    int nextEmptyAdj4 = nextEmptyAdj3 + 10;
                                    if (nextEmptyAdj4 > 999) {
                                        curEmptyAdj3 = nextEmptyAdj3;
                                        nextEmptyAdj3 = 999;
                                    } else {
                                        nextEmptyAdj3 = nextEmptyAdj4;
                                        curEmptyAdj3 = nextEmptyAdj3;
                                    }
                                } else {
                                    stepEmpty = stepEmpty2;
                                }
                            } else {
                                retryCycles = retryCycles3;
                                cachedFactor = cachedFactor;
                                emptyFactor2 = emptyFactor4;
                            }
                            app4.setCurRawAdj(curEmptyAdj3);
                            app4.curAdj = app4.modifyRawOomAdj(curEmptyAdj3);
                            boolean z4 = ActivityManagerDebugConfig.DEBUG_LRU;
                            nextEmptyAdj2 = nextEmptyAdj3;
                            curEmptyAdj2 = curEmptyAdj3;
                            nextCachedAdj4 = nextCachedAdj4;
                            curCachedAdj3 = curCachedAdj3;
                            lastCachedGroup4 = lastCachedGroup4;
                            lastCachedGroupUid5 = lastCachedGroupUid5;
                            lastCachedGroupImportance7 = lastCachedGroupImportance7;
                            retryCycles2 = retryCycles;
                            continue;
                    }
                } else {
                    emptyFactor2 = emptyFactor4;
                    retryCycles2 = retryCycles3;
                }
            }
            i5--;
            emptyFactor4 = emptyFactor2;
            emptyFactor3 = numEmptyProcs2;
            numEmptyProcs4 = numEmptyProcs;
            nowElapsed2 = nowElapsed;
            cachedProcessLimit2 = cachedProcessLimit;
            emptyProcessLimit = emptyProcessLimit3;
            cachedFactor3 = cachedFactor;
        }
        int cachedProcessLimit3 = cachedProcessLimit2;
        int cachedFactor4 = cachedFactor3;
        long nowElapsed3 = nowElapsed2;
        int emptyProcessLimit5 = emptyProcessLimit;
        int curEmptyAdj4 = curEmptyAdj2;
        int nextEmptyAdj5 = nextEmptyAdj2;
        int emptyFactor5 = lastCachedGroupUid5;
        int lastCachedGroup5 = lastCachedGroup4;
        int cachedFactor5 = lastCachedGroupImportance7;
        int curCachedAdj4 = curCachedAdj3;
        int nextCachedAdj6 = nextCachedAdj4;
        int emptyFactor6 = emptyFactor4;
        int cycleCount2 = 0;
        while (retryCycles2 && cycleCount2 < 10) {
            int cycleCount3 = cycleCount2 + 1;
            boolean retryCycles4 = false;
            int i6 = 0;
            while (i6 < emptyFactor3) {
                ProcessRecord app5 = this.mProcessList.mLruProcesses.get(i6);
                if (app5.killedByAm || app5.thread == null) {
                    emptyFactor = emptyFactor6;
                } else {
                    emptyFactor = emptyFactor6;
                    if (app5.containsCycle) {
                        app5.adjSeq--;
                        app5.completedAdjSeq--;
                    }
                }
                i6++;
                retryCycles4 = retryCycles4;
                nextEmptyAdj5 = nextEmptyAdj5;
                emptyFactor6 = emptyFactor;
            }
            int emptyFactor7 = emptyFactor6;
            boolean z5 = true;
            int i7 = 0;
            retryCycles2 = retryCycles4;
            while (i7 < emptyFactor3) {
                ProcessRecord app6 = this.mProcessList.mLruProcesses.get(i7);
                if (app6.killedByAm || app6.thread == null || app6.containsCycle != z5) {
                    i = i7;
                    curEmptyAdj = curEmptyAdj4;
                    nextCachedAdj = nextCachedAdj6;
                    curCachedAdj = curCachedAdj4;
                    lastCachedGroup2 = lastCachedGroup5;
                    cycleCount = cycleCount3;
                    lastCachedGroupUid2 = emptyFactor5;
                    lastCachedGroupImportance = cachedFactor5;
                    lastCachedGroupUid3 = emptyFactor7;
                    lastCachedGroupImportance2 = cachedFactor4;
                    z2 = z5;
                } else {
                    i = i7;
                    curEmptyAdj = curEmptyAdj4;
                    nextCachedAdj = nextCachedAdj6;
                    curCachedAdj = curCachedAdj4;
                    lastCachedGroup2 = lastCachedGroup5;
                    cycleCount = cycleCount3;
                    lastCachedGroupUid2 = emptyFactor5;
                    lastCachedGroupImportance = cachedFactor5;
                    lastCachedGroupUid3 = emptyFactor7;
                    lastCachedGroupImportance2 = cachedFactor4;
                    z2 = z5;
                    if (computeOomAdjLocked(app6, app6.getCurRawAdj(), TOP_APP, true, now, true)) {
                        retryCycles2 = true;
                    }
                }
                i7 = i + 1;
                z5 = z2;
                emptyFactor7 = lastCachedGroupUid3;
                nextCachedAdj6 = nextCachedAdj;
                cachedFactor4 = lastCachedGroupImportance2;
                cycleCount3 = cycleCount;
                curEmptyAdj4 = curEmptyAdj;
                emptyFactor5 = lastCachedGroupUid2;
                lastCachedGroup5 = lastCachedGroup2;
                cachedFactor5 = lastCachedGroupImportance;
                curCachedAdj4 = curCachedAdj;
            }
            emptyFactor6 = emptyFactor7;
            nextEmptyAdj5 = nextEmptyAdj5;
            cycleCount2 = cycleCount3;
            emptyFactor5 = emptyFactor5;
            cachedFactor5 = cachedFactor5;
        }
        ?? r10 = 1;
        int i8 = emptyFactor3 - 1;
        int numCached = 0;
        int lastCachedGroup6 = 0;
        int numTrimming2 = 0;
        int cachedProcessLimit4 = 0;
        int lastCachedGroupUid7 = 0;
        while (i8 >= 0) {
            ProcessRecord app7 = this.mProcessList.mLruProcesses.get(i8);
            if (app7.killedByAm || app7.thread == null) {
                numEmpty = cachedProcessLimit4;
                numEmpty2 = cachedProcessLimit3;
                emptyProcessLimit2 = emptyProcessLimit5;
                numTrimming2 = numTrimming2;
                numCached = numCached;
                lastCachedGroupUid7 = lastCachedGroupUid7;
            } else {
                int lastCachedGroupUid8 = numCached;
                int lastCachedGroup7 = lastCachedGroup6;
                int numCached2 = numTrimming2;
                numEmpty = cachedProcessLimit4;
                applyOomAdjLocked(app7, true, now, nowElapsed3);
                int curProcState = app7.getCurProcState();
                if (curProcState == 17 || curProcState == 18) {
                    ProcessRecord app8 = app7;
                    emptyProcessLimit2 = emptyProcessLimit5;
                    int i9 = this.mNumCachedHiddenProcs;
                    int i10 = r10 == true ? 1 : 0;
                    int i11 = r10 == true ? 1 : 0;
                    int i12 = r10 == true ? 1 : 0;
                    this.mNumCachedHiddenProcs = i9 + i10;
                    int numCached3 = numCached2 + 1;
                    if (app8.connectionGroup != 0) {
                        if (lastCachedGroupUid8 == app8.info.uid) {
                            lastCachedGroup = lastCachedGroup7;
                            if (lastCachedGroup == app8.connectionGroup) {
                                numCachedExtraGroup++;
                                lastCachedGroupUid = lastCachedGroupUid8;
                            }
                        }
                        lastCachedGroupUid = app8.info.uid;
                        lastCachedGroup = app8.connectionGroup;
                    } else {
                        lastCachedGroup = 0;
                        lastCachedGroupUid = 0;
                    }
                    numEmpty2 = cachedProcessLimit3;
                    if (numCached3 - numCachedExtraGroup > numEmpty2) {
                        app8.kill("cached #" + numCached3, true);
                    }
                    lastCachedGroupUid8 = lastCachedGroupUid;
                    lastCachedGroup7 = lastCachedGroup;
                    numCached2 = numCached3;
                    app = app8;
                } else {
                    if (curProcState != 20) {
                        this.mNumNonCachedProcs += r10;
                        app2 = app7;
                    } else {
                        if (numEmpty > this.mConstants.CUR_TRIM_EMPTY_PROCESSES) {
                            app2 = app7;
                            if (app2.lastActivityTime < oldTime) {
                                StringBuilder sb = new StringBuilder();
                                sb.append("empty for ");
                                numEmpty = numEmpty;
                                sb.append(((ProcessList.MAX_EMPTY_TIME + oldTime) - app2.lastActivityTime) / 1000);
                                sb.append("s");
                                app2.kill(sb.toString(), r10);
                            } else {
                                numEmpty3 = numEmpty;
                            }
                        } else {
                            numEmpty3 = numEmpty;
                            app2 = app7;
                        }
                        int numEmpty4 = numEmpty3 + 1;
                        emptyProcessLimit2 = emptyProcessLimit5;
                        if (numEmpty4 > emptyProcessLimit2) {
                            app2.kill("empty #" + numEmpty4, r10);
                        }
                        numEmpty = numEmpty4;
                        numEmpty2 = cachedProcessLimit3;
                        app = app2;
                    }
                    numEmpty2 = cachedProcessLimit3;
                    emptyProcessLimit2 = emptyProcessLimit5;
                    app = app2;
                }
                if (!app.isolated || app.services.size() > 0 || app.isolatedEntryPoint != null) {
                    UidRecord uidRec = app.uidRecord;
                    if (uidRec != null) {
                        uidRec.ephemeral = app.info.isInstantApp();
                        if (uidRec.getCurProcState() > app.getCurProcState()) {
                            uidRec.setCurProcState(app.getCurProcState());
                        }
                        if (app.hasForegroundServices()) {
                            uidRec.foregroundServices = true;
                        }
                    }
                } else {
                    app.kill("isolated not needed", true);
                }
                if (app.getCurProcState() < 15 || app.killedByAm) {
                    lastCachedGroupUid7 = lastCachedGroupUid7;
                    numCached = lastCachedGroupUid8;
                    lastCachedGroup6 = lastCachedGroup7;
                    numTrimming2 = numCached2;
                } else {
                    lastCachedGroupUid7++;
                    numCached = lastCachedGroupUid8;
                    lastCachedGroup6 = lastCachedGroup7;
                    numTrimming2 = numCached2;
                }
            }
            i8--;
            emptyProcessLimit5 = emptyProcessLimit2;
            cachedProcessLimit3 = numEmpty2;
            cachedProcessLimit4 = numEmpty;
            r10 = 1;
        }
        int lastCachedGroupUid9 = numCached;
        int numCached4 = numTrimming2;
        int numTrimming3 = lastCachedGroupUid7;
        this.mService.incrementProcStateSeqAndNotifyAppsLocked();
        this.mNumServiceProcs = this.mNewNumServiceProcs;
        boolean allChanged = this.mService.updateLowMemStateLocked(numCached4, cachedProcessLimit4, numTrimming3);
        if (this.mService.mAlwaysFinishActivities) {
            this.mService.mAtmInternal.scheduleDestroyAllActivities("always-finish");
        }
        if (allChanged) {
            ActivityManagerService activityManagerService = this.mService;
            activityManagerService.requestPssAllProcsLocked(now, false, activityManagerService.mProcessStats.isMemFactorLowered());
        }
        ArrayList<UidRecord> becameIdle = null;
        PowerManagerInternal powerManagerInternal = this.mLocalPowerManager;
        if (powerManagerInternal != null) {
            powerManagerInternal.startUidChanges();
        }
        int i13 = this.mActiveUids.size() - 1;
        while (i13 >= 0) {
            UidRecord uidRec2 = this.mActiveUids.valueAt(i13);
            int uidChange2 = 0;
            if (uidRec2.getCurProcState() == 21) {
                numTrimming = numTrimming3;
            } else if (uidRec2.setProcState == uidRec2.getCurProcState() && uidRec2.setWhitelist == uidRec2.curWhitelist) {
                numTrimming = numTrimming3;
            } else {
                if (ActivityManagerDebugConfig.DEBUG_UID_OBSERVERS) {
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("Changes in ");
                    sb2.append(uidRec2);
                    sb2.append(": proc state from ");
                    sb2.append(uidRec2.setProcState);
                    sb2.append(" to ");
                    numTrimming = numTrimming3;
                    sb2.append(uidRec2.getCurProcState());
                    sb2.append(", whitelist from ");
                    sb2.append(uidRec2.setWhitelist);
                    sb2.append(" to ");
                    sb2.append(uidRec2.curWhitelist);
                    Slog.i(ActivityManagerService.TAG, sb2.toString());
                } else {
                    numTrimming = numTrimming3;
                }
                if (!ActivityManager.isProcStateBackground(uidRec2.getCurProcState()) || uidRec2.curWhitelist) {
                    if (uidRec2.idle) {
                        uidChange2 = 4;
                        EventLogTags.writeAmUidActive(uidRec2.uid);
                        z = false;
                        uidRec2.idle = false;
                    } else {
                        z = false;
                    }
                    uidRec2.lastBackgroundTime = 0;
                } else {
                    if (!ActivityManager.isProcStateBackground(uidRec2.setProcState) || uidRec2.setWhitelist) {
                        uidRec2.lastBackgroundTime = nowElapsed3;
                        if (!this.mService.mHandler.hasMessages(58)) {
                            nowElapsed3 = nowElapsed3;
                            this.mService.mHandler.sendEmptyMessageDelayed(58, this.mConstants.BACKGROUND_SETTLE_TIME);
                        } else {
                            nowElapsed3 = nowElapsed3;
                        }
                    }
                    if (!uidRec2.idle || uidRec2.setIdle) {
                        z = false;
                    } else {
                        uidChange2 = 2;
                        if (becameIdle == null) {
                            becameIdle = new ArrayList<>();
                        }
                        becameIdle.add(uidRec2);
                        z = false;
                    }
                }
                boolean wasCached = uidRec2.setProcState > 12 ? true : z;
                boolean isCached = uidRec2.getCurProcState() > 12 ? true : z;
                if (wasCached != isCached || uidRec2.setProcState == 21) {
                    uidChange = uidChange2 | (isCached ? 8 : 16);
                } else {
                    uidChange = uidChange2;
                }
                uidRec2.setProcState = uidRec2.getCurProcState();
                uidRec2.setWhitelist = uidRec2.curWhitelist;
                uidRec2.setIdle = uidRec2.idle;
                this.mService.mAtmInternal.onUidProcStateChanged(uidRec2.uid, uidRec2.setProcState);
                this.mService.enqueueUidChangeLocked(uidRec2, -1, uidChange);
                this.mService.noteUidProcessState(uidRec2.uid, uidRec2.getCurProcState());
                if (uidRec2.foregroundServices) {
                    this.mService.mServices.foregroundServiceProcStateChangedLocked(uidRec2);
                }
                becameIdle = becameIdle;
            }
            i13--;
            lastCachedGroupUid9 = lastCachedGroupUid9;
            numTrimming3 = numTrimming;
            lastCachedGroup6 = lastCachedGroup6;
            numCached4 = numCached4;
        }
        PowerManagerInternal powerManagerInternal2 = this.mLocalPowerManager;
        if (powerManagerInternal2 != null) {
            powerManagerInternal2.finishUidChanges();
        }
        if (becameIdle != null) {
            for (int i14 = becameIdle.size() - 1; i14 >= 0; i14--) {
                this.mService.mServices.stopInBackgroundLocked(becameIdle.get(i14).uid);
            }
        }
        if (this.mService.mProcessStats.shouldWriteNowLocked(now)) {
            ActivityManagerService.MainHandler mainHandler = this.mService.mHandler;
            ActivityManagerService activityManagerService2 = this.mService;
            mainHandler.post(new ActivityManagerService.ProcStatsRunnable(activityManagerService2, activityManagerService2.mProcessStats));
        }
        this.mService.mProcessStats.updateTrackingAssociationsLocked(this.mAdjSeq, now);
        if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ) {
            Slog.d(ActivityManagerService.TAG, "Did OOM ADJ in " + (SystemClock.uptimeMillis() - now) + "ms");
        }
        this.mService.mOomAdjProfiler.oomAdjEnded();
        Trace.traceEnd(64);
    }

    /* access modifiers changed from: private */
    public final class ComputeOomAdjWindowCallback implements WindowProcessController.ComputeOomAdjCallback {
        int adj;
        ProcessRecord app;
        int appUid;
        boolean foregroundActivities;
        int logUid;
        int procState;
        int processStateCurTop;
        int schedGroup;

        private ComputeOomAdjWindowCallback() {
        }

        /* access modifiers changed from: package-private */
        public void initialize(ProcessRecord app2, int adj2, boolean foregroundActivities2, int procState2, int schedGroup2, int appUid2, int logUid2, int processStateCurTop2) {
            this.app = app2;
            this.adj = adj2;
            this.foregroundActivities = foregroundActivities2;
            this.procState = procState2;
            this.schedGroup = schedGroup2;
            this.appUid = appUid2;
            this.logUid = logUid2;
            this.processStateCurTop = processStateCurTop2;
        }

        public void onVisibleActivity() {
            if (this.adj > 100) {
                this.adj = 100;
                this.app.adjType = "vis-activity";
                if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || this.logUid == this.appUid) {
                    OomAdjuster oomAdjuster = OomAdjuster.this;
                    oomAdjuster.reportOomAdjMessageLocked(ActivityManagerService.TAG, "Raise adj to vis-activity: " + this.app);
                }
            }
            int i = this.procState;
            int i2 = this.processStateCurTop;
            if (i > i2) {
                this.procState = i2;
                this.app.adjType = "vis-activity";
                if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || this.logUid == this.appUid) {
                    OomAdjuster oomAdjuster2 = OomAdjuster.this;
                    oomAdjuster2.reportOomAdjMessageLocked(ActivityManagerService.TAG, "Raise procstate to vis-activity (top): " + this.app);
                }
            }
            if (this.schedGroup < 2) {
                this.schedGroup = 2;
            }
            ProcessRecord processRecord = this.app;
            processRecord.cached = false;
            processRecord.empty = false;
            this.foregroundActivities = true;
        }

        public void onPausedActivity() {
            if (this.adj > 200) {
                this.adj = 200;
                this.app.adjType = "pause-activity";
                if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || this.logUid == this.appUid) {
                    OomAdjuster oomAdjuster = OomAdjuster.this;
                    oomAdjuster.reportOomAdjMessageLocked(ActivityManagerService.TAG, "Raise adj to pause-activity: " + this.app);
                }
            }
            int i = this.procState;
            int i2 = this.processStateCurTop;
            if (i > i2) {
                this.procState = i2;
                this.app.adjType = "pause-activity";
                if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || this.logUid == this.appUid) {
                    OomAdjuster oomAdjuster2 = OomAdjuster.this;
                    oomAdjuster2.reportOomAdjMessageLocked(ActivityManagerService.TAG, "Raise procstate to pause-activity (top): " + this.app);
                }
            }
            if (this.schedGroup < 2) {
                this.schedGroup = 2;
            }
            ProcessRecord processRecord = this.app;
            processRecord.cached = false;
            processRecord.empty = false;
            this.foregroundActivities = true;
        }

        public void onStoppingActivity(boolean finishing) {
            if (this.adj > 200) {
                this.adj = 200;
                this.app.adjType = "stop-activity";
                if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || this.logUid == this.appUid) {
                    OomAdjuster oomAdjuster = OomAdjuster.this;
                    oomAdjuster.reportOomAdjMessageLocked(ActivityManagerService.TAG, "Raise adj to stop-activity: " + this.app);
                }
            }
            if (!finishing && this.procState > 16) {
                this.procState = 16;
                this.app.adjType = "stop-activity";
                if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || this.logUid == this.appUid) {
                    OomAdjuster oomAdjuster2 = OomAdjuster.this;
                    oomAdjuster2.reportOomAdjMessageLocked(ActivityManagerService.TAG, "Raise procstate to stop-activity: " + this.app);
                }
            }
            ProcessRecord processRecord = this.app;
            processRecord.cached = false;
            processRecord.empty = false;
            this.foregroundActivities = true;
        }

        public void onOtherActivity() {
            if (this.procState > 17) {
                this.procState = 17;
                this.app.adjType = "cch-act";
                if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || this.logUid == this.appUid) {
                    OomAdjuster oomAdjuster = OomAdjuster.this;
                    oomAdjuster.reportOomAdjMessageLocked(ActivityManagerService.TAG, "Raise procstate to cached activity: " + this.app);
                }
            }
        }
    }

    /* JADX INFO: Multiple debug info for r10v13 com.android.server.am.ProcessRecord: [D('provi' int), D('client' com.android.server.am.ProcessRecord)] */
    /* JADX INFO: Multiple debug info for r35v9 'schedGroup'  int: [D('schedGroup' int), D('adj' int)] */
    /* JADX INFO: Multiple debug info for r11v25 'schedGroup'  int: [D('schedGroup' int), D('appUid' int)] */
    /* JADX WARNING: Code restructure failed: missing block: B:136:0x0347, code lost:
        if (r2 > 3) goto L_0x034b;
     */
    /* JADX WARNING: Removed duplicated region for block: B:135:0x0346  */
    /* JADX WARNING: Removed duplicated region for block: B:137:0x034a  */
    /* JADX WARNING: Removed duplicated region for block: B:140:0x0351  */
    /* JADX WARNING: Removed duplicated region for block: B:153:0x039a  */
    /* JADX WARNING: Removed duplicated region for block: B:185:0x0453  */
    /* JADX WARNING: Removed duplicated region for block: B:199:0x04a7  */
    /* JADX WARNING: Removed duplicated region for block: B:217:0x0507  */
    /* JADX WARNING: Removed duplicated region for block: B:222:0x052e  */
    /* JADX WARNING: Removed duplicated region for block: B:227:0x0551  */
    /* JADX WARNING: Removed duplicated region for block: B:228:0x0553  */
    /* JADX WARNING: Removed duplicated region for block: B:231:0x0560  */
    /* JADX WARNING: Removed duplicated region for block: B:232:0x0562  */
    /* JADX WARNING: Removed duplicated region for block: B:239:0x058b  */
    /* JADX WARNING: Removed duplicated region for block: B:252:0x05d2  */
    /* JADX WARNING: Removed duplicated region for block: B:259:0x0608  */
    /* JADX WARNING: Removed duplicated region for block: B:390:0x089c  */
    /* JADX WARNING: Removed duplicated region for block: B:426:0x090e  */
    /* JADX WARNING: Removed duplicated region for block: B:436:0x092b  */
    /* JADX WARNING: Removed duplicated region for block: B:441:0x0938  */
    /* JADX WARNING: Removed duplicated region for block: B:443:0x093f  */
    /* JADX WARNING: Removed duplicated region for block: B:447:0x094e  */
    /* JADX WARNING: Removed duplicated region for block: B:454:0x0962  */
    /* JADX WARNING: Removed duplicated region for block: B:459:0x09c4  */
    /* JADX WARNING: Removed duplicated region for block: B:489:0x0abb  */
    /* JADX WARNING: Removed duplicated region for block: B:579:0x0ced  */
    /* JADX WARNING: Removed duplicated region for block: B:596:0x0d54  */
    /* JADX WARNING: Removed duplicated region for block: B:599:0x0d59  */
    /* JADX WARNING: Removed duplicated region for block: B:607:0x0d74  */
    /* JADX WARNING: Removed duplicated region for block: B:627:0x0dbc  */
    /* JADX WARNING: Removed duplicated region for block: B:630:0x0dc4  */
    /* JADX WARNING: Removed duplicated region for block: B:634:0x0dd6  */
    /* JADX WARNING: Removed duplicated region for block: B:637:0x0ddc  */
    /* JADX WARNING: Removed duplicated region for block: B:641:0x0de7  */
    /* JADX WARNING: Removed duplicated region for block: B:644:0x0e06  */
    /* JADX WARNING: Removed duplicated region for block: B:647:0x0e11  */
    /* JADX WARNING: Removed duplicated region for block: B:650:0x0e1d  */
    /* JADX WARNING: Removed duplicated region for block: B:652:0x0a9f A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:668:0x0cdd A[SYNTHETIC] */
    private final boolean computeOomAdjLocked(ProcessRecord app, int cachedAdj, ProcessRecord TOP_APP, boolean doingAll, long now, boolean cycleReEval) {
        int schedGroup;
        int adj;
        int procState;
        int prevAppAdj;
        int prevProcState;
        String str;
        int logUid;
        int schedGroup2;
        boolean foregroundActivities;
        String str2;
        int logUid2;
        String str3;
        String str4;
        boolean foregroundActivities2;
        int schedGroup3;
        int adj2;
        int schedGroup4;
        int i;
        BackupRecord backupTarget;
        int procState2;
        int procState3;
        int appUid;
        boolean isConnectTopApp;
        WindowProcessController wpc;
        int schedGroup5;
        int schedGroup6;
        boolean isConnectTopApp2;
        String str5;
        String str6;
        int is;
        String str7;
        long j;
        int provi;
        boolean isConnectTopApp3;
        String str8;
        ProcessRecord processRecord;
        int adj3;
        ProcessRecord processRecord2;
        OomAdjuster oomAdjuster;
        int adj4;
        int adj5;
        boolean z;
        int i2;
        int schedGroup7;
        int schedGroup8;
        String str9;
        ContentProviderRecord cpr;
        int provi2;
        String str10;
        String str11;
        int procState4;
        ProcessRecord processRecord3;
        int adj6;
        int schedGroup9;
        String adjType;
        int i3;
        ProcessRecord processRecord4;
        ContentProviderRecord cpr2;
        String str12;
        int adj7;
        String adjType2;
        String str13;
        ContentProviderRecord cpr3;
        int adj8;
        int procState5;
        int schedGroup10;
        int schedGroup11;
        boolean isConnectTopApp4;
        WindowProcessController wpc2;
        int is2;
        String str14;
        int is3;
        int conni;
        String str15;
        String str16;
        int schedGroup12;
        String str17;
        ArrayList<ConnectionRecord> clist;
        int i4;
        ArrayMap<IBinder, ArrayList<ConnectionRecord>> serviceConnections;
        String str18;
        long j2;
        int schedGroup13;
        String str19;
        String str20;
        int adj9;
        ConnectionRecord cr;
        ConnectionRecord cr2;
        int adj10;
        String adjType3;
        ProcessRecord client;
        int adj11;
        int schedGroup14;
        String adjType4;
        int schedGroup15;
        int bestState;
        int newAdj;
        int i5;
        int procState6;
        int procState7;
        int schedGroup16;
        int schedGroup17;
        int procState8;
        int adj12;
        ProcessRecord processRecord5 = app;
        long j3 = now;
        if (this.mAdjSeq == processRecord5.adjSeq) {
            this.mService.mHwAMSEx.updateProcessRecordCurAdj(this.mAdjSeq, processRecord5);
            if (processRecord5.adjSeq == processRecord5.completedAdjSeq) {
                return false;
            }
            processRecord5.containsCycle = true;
            return false;
        } else if (processRecord5.thread == null) {
            processRecord5.adjSeq = this.mAdjSeq;
            processRecord5.setCurrentSchedulingGroup(0);
            processRecord5.setCurProcState(20);
            processRecord5.curAdj = 999;
            processRecord5.setCurRawAdj(999);
            processRecord5.completedAdjSeq = processRecord5.adjSeq;
            return false;
        } else {
            processRecord5.adjTypeCode = 0;
            processRecord5.adjSource = null;
            processRecord5.adjTarget = null;
            processRecord5.empty = false;
            processRecord5.cached = false;
            WindowProcessController wpc3 = app.getWindowProcessController();
            int appUid2 = processRecord5.info.uid;
            int logUid3 = this.mService.mCurOomAdjUid;
            int prevAppAdj2 = processRecord5.curAdj;
            int prevProcState2 = app.getCurProcState();
            if (processRecord5.maxAdj <= 0) {
                if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid3 == appUid2) {
                    this.mService.reportOomAdjMessageLocked(ActivityManagerService.TAG, "Making fixed: " + processRecord5);
                }
                processRecord5.adjType = "fixed";
                processRecord5.adjSeq = this.mAdjSeq;
                processRecord5.setCurRawAdj(processRecord5.maxAdj);
                processRecord5.setHasForegroundActivities(false);
                processRecord5.setCurrentSchedulingGroup(2);
                processRecord5.setCurProcState(0);
                processRecord5.systemNoUi = true;
                if (processRecord5 == TOP_APP) {
                    processRecord5.systemNoUi = false;
                    processRecord5.setCurrentSchedulingGroup(3);
                    processRecord5.adjType = "pers-top-activity";
                } else if (app.hasTopUi()) {
                    processRecord5.systemNoUi = false;
                    processRecord5.adjType = "pers-top-ui";
                } else if (wpc3.hasVisibleActivities()) {
                    processRecord5.systemNoUi = false;
                }
                if (!processRecord5.systemNoUi) {
                    if (this.mService.mWakefulness == 1) {
                        processRecord5.setCurProcState(1);
                        processRecord5.setCurrentSchedulingGroup(3);
                    } else {
                        processRecord5.setCurProcState(6);
                        processRecord5.setCurrentSchedulingGroup(1);
                    }
                }
                if (this.mService.mCpusetSwitch) {
                    this.mService.setWhiteListProcessGroup(processRecord5, TOP_APP, processRecord5 == TOP_APP);
                }
                processRecord5.setCurRawProcState(app.getCurProcState());
                processRecord5.curAdj = processRecord5.maxAdj;
                processRecord5.completedAdjSeq = processRecord5.adjSeq;
                if (processRecord5.curAdj < prevAppAdj2 || app.getCurProcState() < prevProcState2) {
                    return true;
                }
                return false;
            }
            processRecord5.systemNoUi = false;
            int PROCESS_STATE_CUR_TOP = this.mService.mAtmInternal.getTopProcessState();
            boolean foregroundActivities3 = processRecord5 == TOP_APP;
            this.mTmpBroadcastQueue.clear();
            if (PROCESS_STATE_CUR_TOP == 2 && processRecord5 == TOP_APP) {
                adj = 0;
                schedGroup16 = 3;
                processRecord5.adjType = "top-activity";
                procState7 = PROCESS_STATE_CUR_TOP;
                if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid3 == appUid2) {
                    reportOomAdjMessageLocked(ActivityManagerService.TAG, "Making top: " + processRecord5);
                }
            } else {
                if (processRecord5.runningRemoteAnimation) {
                    schedGroup17 = 3;
                    processRecord5.adjType = "running-remote-anim";
                    procState8 = PROCESS_STATE_CUR_TOP;
                    if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid3 == appUid2) {
                        StringBuilder sb = new StringBuilder();
                        adj12 = 100;
                        sb.append("Making running remote anim: ");
                        sb.append(processRecord5);
                        reportOomAdjMessageLocked(ActivityManagerService.TAG, sb.toString());
                    } else {
                        adj12 = 100;
                    }
                } else if (app.getActiveInstrumentation() != null) {
                    schedGroup17 = 2;
                    processRecord5.adjType = "instrumentation";
                    procState8 = 5;
                    if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid3 == appUid2) {
                        StringBuilder sb2 = new StringBuilder();
                        adj12 = 0;
                        sb2.append("Making instrumentation: ");
                        sb2.append(processRecord5);
                        reportOomAdjMessageLocked(ActivityManagerService.TAG, sb2.toString());
                    } else {
                        adj12 = 0;
                    }
                } else if (this.mService.isReceivingBroadcastLocked(processRecord5, this.mTmpBroadcastQueue)) {
                    if (this.mTmpBroadcastQueue.contains(this.mService.mFgBroadcastQueue) || ((this.mService.mFgThirdAppBroadcastQueue != null && this.mTmpBroadcastQueue.contains(this.mService.mFgThirdAppBroadcastQueue)) || (this.mService.mFgKeyAppBroadcastQueue != null && this.mTmpBroadcastQueue.contains(this.mService.mFgKeyAppBroadcastQueue)))) {
                        schedGroup17 = 2;
                    } else {
                        schedGroup17 = 0;
                    }
                    processRecord5.adjType = INetd.IF_FLAG_BROADCAST;
                    procState8 = 12;
                    if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid3 == appUid2) {
                        StringBuilder sb3 = new StringBuilder();
                        adj12 = 0;
                        sb3.append("Making broadcast: ");
                        sb3.append(processRecord5);
                        reportOomAdjMessageLocked(ActivityManagerService.TAG, sb3.toString());
                    } else {
                        adj12 = 0;
                    }
                } else if (processRecord5.executingServices.size() > 0) {
                    schedGroup17 = processRecord5.execServicesFg ? 2 : 0;
                    processRecord5.adjType = "exec-service";
                    procState8 = 11;
                    if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid3 == appUid2) {
                        StringBuilder sb4 = new StringBuilder();
                        adj12 = 0;
                        sb4.append("Making exec-service: ");
                        sb4.append(processRecord5);
                        reportOomAdjMessageLocked(ActivityManagerService.TAG, sb4.toString());
                    } else {
                        adj12 = 0;
                    }
                } else if (processRecord5 == TOP_APP) {
                    schedGroup17 = 0;
                    processRecord5.adjType = "top-sleeping";
                    procState8 = PROCESS_STATE_CUR_TOP;
                    if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid3 == appUid2) {
                        StringBuilder sb5 = new StringBuilder();
                        adj12 = 0;
                        sb5.append("Making top (sleeping): ");
                        sb5.append(processRecord5);
                        reportOomAdjMessageLocked(ActivityManagerService.TAG, sb5.toString());
                    } else {
                        adj12 = 0;
                    }
                } else {
                    adj = cachedAdj;
                    procState7 = 20;
                    processRecord5.cached = true;
                    processRecord5.empty = true;
                    processRecord5.adjType = "cch-empty";
                    if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid3 == appUid2) {
                        StringBuilder sb6 = new StringBuilder();
                        schedGroup16 = 0;
                        sb6.append("Making empty: ");
                        sb6.append(processRecord5);
                        reportOomAdjMessageLocked(ActivityManagerService.TAG, sb6.toString());
                    } else {
                        schedGroup16 = 0;
                    }
                }
                schedGroup = schedGroup17;
                procState = procState8;
                adj = adj12;
                if (!foregroundActivities3 || !wpc3.hasActivities()) {
                    str = ActivityManagerService.TAG;
                    prevProcState = prevProcState2;
                    prevAppAdj = prevAppAdj2;
                    logUid = logUid3;
                    foregroundActivities = foregroundActivities3;
                    schedGroup2 = schedGroup;
                } else {
                    ComputeOomAdjWindowCallback computeOomAdjWindowCallback = this.mTmpComputeOomAdjWindowCallback;
                    str = ActivityManagerService.TAG;
                    prevProcState = prevProcState2;
                    prevAppAdj = prevAppAdj2;
                    logUid = logUid3;
                    computeOomAdjWindowCallback.initialize(app, adj, foregroundActivities3, procState, schedGroup, appUid2, logUid, PROCESS_STATE_CUR_TOP);
                    int minLayer = wpc3.computeOomAdjFromActivities(99, this.mTmpComputeOomAdjWindowCallback);
                    adj = this.mTmpComputeOomAdjWindowCallback.adj;
                    foregroundActivities = this.mTmpComputeOomAdjWindowCallback.foregroundActivities;
                    procState = this.mTmpComputeOomAdjWindowCallback.procState;
                    schedGroup2 = this.mTmpComputeOomAdjWindowCallback.schedGroup;
                    if (adj == 100) {
                        adj += minLayer;
                    }
                }
                if (procState > 19 || !app.hasRecentTasks()) {
                    logUid2 = logUid;
                    str2 = str;
                } else {
                    procState = 19;
                    processRecord5.adjType = "cch-rec";
                    if (!ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON) {
                        logUid2 = logUid;
                        if (logUid2 != appUid2) {
                            str2 = str;
                        }
                    } else {
                        logUid2 = logUid;
                    }
                    str2 = str;
                    reportOomAdjMessageLocked(str2, "Raise procstate to cached recent: " + processRecord5);
                }
                str3 = ": ";
                str4 = "Raise to ";
                if (adj > 200) {
                }
                if (!app.hasForegroundServices()) {
                    adj = 200;
                    if (app.hasLocationForegroundServices()) {
                        procState = 3;
                        processRecord5.adjType = "fg-service-location";
                    } else {
                        procState = 5;
                        processRecord5.adjType = "fg-service";
                    }
                    processRecord5.cached = false;
                    if (!this.mService.mCpusetSwitch || app.hasOverlayUi()) {
                        schedGroup2 = 2;
                    }
                    if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid2 == appUid2) {
                        reportOomAdjMessageLocked(str2, str4 + processRecord5.adjType + str3 + processRecord5 + " ");
                    }
                } else if (app.hasOverlayUi()) {
                    adj = 200;
                    procState = 7;
                    processRecord5.cached = false;
                    processRecord5.adjType = "has-overlay-ui";
                    schedGroup2 = 2;
                    if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid2 == appUid2) {
                        reportOomAdjMessageLocked(str2, "Raise to overlay ui: " + processRecord5);
                    }
                }
                if (app.hasForegroundServices() || adj <= 50) {
                    adj2 = adj;
                    foregroundActivities2 = foregroundActivities;
                    schedGroup3 = schedGroup2;
                } else {
                    adj2 = adj;
                    schedGroup3 = schedGroup2;
                    foregroundActivities2 = foregroundActivities;
                    if (processRecord5.lastTopTime + this.mConstants.TOP_TO_FGS_GRACE_DURATION > j3 || processRecord5.setProcState <= 2) {
                        processRecord5.adjType = "fg-service-act";
                        if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid2 == appUid2) {
                            reportOomAdjMessageLocked(str2, "Raise to recent fg: " + processRecord5);
                        }
                        adj2 = 50;
                    }
                }
                if ((adj2 <= 200 || procState > 9) && processRecord5.forcingToImportant != null) {
                    adj2 = 200;
                    procState = 9;
                    processRecord5.cached = false;
                    processRecord5.adjType = "force-imp";
                    processRecord5.adjSource = processRecord5.forcingToImportant;
                    schedGroup4 = 2;
                    if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid2 == appUid2) {
                        reportOomAdjMessageLocked(str2, "Raise to force imp: " + processRecord5);
                    }
                } else {
                    schedGroup4 = schedGroup3;
                }
                if (this.mService.mAtmInternal.isHeavyWeightProcess(app.getWindowProcessController())) {
                    if (adj2 > 400) {
                        adj2 = 400;
                        schedGroup4 = 0;
                        processRecord5.cached = false;
                        processRecord5.adjType = "heavy";
                        if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid2 == appUid2) {
                            reportOomAdjMessageLocked(str2, "Raise adj to heavy: " + processRecord5);
                        }
                    }
                    if (procState > 14) {
                        procState = 14;
                        processRecord5.adjType = "heavy";
                        if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid2 == appUid2) {
                            reportOomAdjMessageLocked(str2, "Raise procstate to heavy: " + processRecord5);
                        }
                    }
                }
                if (wpc3.isHomeProcess()) {
                    if (adj2 > 600) {
                        adj2 = SystemService.PHASE_THIRD_PARTY_APPS_CAN_START;
                        schedGroup4 = 0;
                        processRecord5.cached = false;
                        processRecord5.adjType = "home";
                        if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid2 == appUid2) {
                            reportOomAdjMessageLocked(str2, "Raise adj to home: " + processRecord5);
                        }
                    }
                    if (procState > 15) {
                        procState = 15;
                        processRecord5.adjType = "home";
                        if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid2 == appUid2) {
                            reportOomAdjMessageLocked(str2, "Raise procstate to home: " + processRecord5);
                        }
                    }
                }
                if (wpc3.isPreviousProcess() && app.hasActivities()) {
                    if (adj2 > 700) {
                        adj2 = 700;
                        schedGroup4 = 0;
                        processRecord5.cached = false;
                        processRecord5.adjType = "previous";
                        if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid2 == appUid2) {
                            reportOomAdjMessageLocked(str2, "Raise adj to prev: " + processRecord5);
                        }
                    }
                    if (procState > 16) {
                        procState = 16;
                        processRecord5.adjType = "previous";
                        if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid2 == appUid2) {
                            reportOomAdjMessageLocked(str2, "Raise procstate to prev: " + processRecord5);
                        }
                    }
                }
                processRecord5.setCurRawAdj(cycleReEval ? adj2 : Math.min(adj2, app.getCurRawAdj()));
                if (cycleReEval) {
                    i = procState;
                } else {
                    i = Math.min(procState, app.getCurRawProcState());
                }
                processRecord5.setCurRawProcState(i);
                processRecord5.hasStartedServices = false;
                processRecord5.adjSeq = this.mAdjSeq;
                backupTarget = this.mService.mBackupTargets.get(processRecord5.userId);
                if (backupTarget != null && processRecord5 == backupTarget.app) {
                    if (adj2 > 300) {
                        if (ActivityManagerDebugConfig.DEBUG_BACKUP) {
                            Slog.v(str2, "oom BACKUP_APP_ADJ for " + processRecord5);
                        }
                        if (procState > 9) {
                            procState = 9;
                        }
                        processRecord5.adjType = BatteryService.HealthServiceWrapper.INSTANCE_HEALTHD;
                        if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid2 == appUid2) {
                            reportOomAdjMessageLocked(str2, "Raise adj to backup: " + processRecord5);
                        }
                        processRecord5.cached = false;
                        adj2 = 300;
                    }
                    if (procState > 10) {
                        procState = 10;
                        processRecord5.adjType = BatteryService.HealthServiceWrapper.INSTANCE_HEALTHD;
                        if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid2 == appUid2) {
                            reportOomAdjMessageLocked(str2, "Raise procstate to backup: " + processRecord5);
                        }
                    }
                }
                procState2 = procState;
                procState3 = schedGroup4;
                appUid = processRecord5.services.size() - 1;
                isConnectTopApp = false;
                while (true) {
                    if (appUid < 0) {
                        if (adj2 <= 0 && procState3 != 0 && procState2 <= 2) {
                            is = appUid2;
                            wpc = wpc3;
                            schedGroup5 = procState3;
                            schedGroup6 = procState2;
                            isConnectTopApp2 = isConnectTopApp;
                            str5 = str2;
                            str6 = str4;
                            str7 = str3;
                            j = j3;
                            break;
                        }
                        ServiceRecord s = processRecord5.services.valueAt(appUid);
                        int schedGroup18 = procState3;
                        if (s.startRequested) {
                            processRecord5.hasStartedServices = true;
                            if (procState2 > 11) {
                                processRecord5.adjType = "started-services";
                                if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid2 == appUid2) {
                                    StringBuilder sb7 = new StringBuilder();
                                    procState6 = 11;
                                    sb7.append("Raise procstate to started service: ");
                                    sb7.append(processRecord5);
                                    reportOomAdjMessageLocked(str2, sb7.toString());
                                } else {
                                    procState6 = 11;
                                }
                                procState2 = procState6;
                            }
                            if (!processRecord5.hasShownUi || wpc3.isHomeProcess()) {
                                wpc2 = wpc3;
                                isConnectTopApp4 = isConnectTopApp;
                                if (j3 < s.lastActivity + this.mConstants.MAX_SERVICE_INACTIVITY) {
                                    if (adj2 > 500) {
                                        processRecord5.adjType = "started-services";
                                        if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid2 == appUid2) {
                                            reportOomAdjMessageLocked(str2, "Raise adj to started service: " + processRecord5);
                                        }
                                        processRecord5.cached = false;
                                        adj2 = 500;
                                    }
                                }
                                if (adj2 > 500) {
                                    processRecord5.adjType = "cch-started-services";
                                }
                                procState2 = procState2;
                            } else {
                                if (adj2 > 500) {
                                    processRecord5.adjType = "cch-started-ui-services";
                                }
                                wpc2 = wpc3;
                                isConnectTopApp4 = isConnectTopApp;
                            }
                        } else {
                            wpc2 = wpc3;
                            isConnectTopApp4 = isConnectTopApp;
                        }
                        ArrayMap<IBinder, ArrayList<ConnectionRecord>> serviceConnections2 = s.getConnections();
                        int procState9 = serviceConnections2.size() - 1;
                        int clientProcState = adj2;
                        while (true) {
                            if (procState9 >= 0) {
                                if (clientProcState <= 0 && schedGroup18 != 0 && procState2 <= 2) {
                                    conni = procState2;
                                    is2 = appUid;
                                    str16 = str2;
                                    str14 = str4;
                                    is3 = appUid2;
                                    str15 = str3;
                                    break;
                                }
                                ArrayList<ConnectionRecord> clist2 = serviceConnections2.valueAt(procState9);
                                int appUid3 = schedGroup18;
                                int procState10 = procState2;
                                int procState11 = 0;
                                while (true) {
                                    if (procState11 >= clist2.size()) {
                                        break;
                                    }
                                    if (clientProcState <= 0 && appUid3 != 0) {
                                        if (procState10 <= 2) {
                                            break;
                                        }
                                    }
                                    ConnectionRecord cr3 = clist2.get(procState11);
                                    if (cr3.binding.client == processRecord5) {
                                        adj9 = clientProcState;
                                        clist = clist2;
                                        i4 = procState11;
                                        serviceConnections = serviceConnections2;
                                        schedGroup12 = appUid3;
                                        str20 = str3;
                                        schedGroup13 = appUid2;
                                    } else {
                                        boolean trackedProcState = false;
                                        if ((cr3.flags & 32) == 0) {
                                            ProcessRecord client2 = cr3.binding.client;
                                            if (this.mService.mCpusetSwitch) {
                                                processRecord5.setCurrentSchedulingGroup(appUid3);
                                            }
                                            adj9 = clientProcState;
                                            schedGroup12 = appUid3;
                                            schedGroup13 = appUid2;
                                            clist = clist2;
                                            str20 = str3;
                                            i4 = procState11;
                                            serviceConnections = serviceConnections2;
                                            computeOomAdjLocked(client2, client2.getCurRawAdj(), TOP_APP, doingAll, now, cycleReEval);
                                            if (!shouldSkipDueToCycle(app, client2, procState10, adj9, cycleReEval)) {
                                                int clientAdj = client2.getCurRawAdj();
                                                int clientProcState2 = client2.getCurRawProcState();
                                                if (clientProcState2 >= 17) {
                                                    clientProcState2 = 20;
                                                }
                                                String adjType5 = null;
                                                if ((cr3.flags & 16) != 0) {
                                                    processRecord5 = app;
                                                    if (!processRecord5.hasShownUi || wpc2.isHomeProcess()) {
                                                        adj10 = adj9;
                                                        cr2 = cr3;
                                                        j2 = now;
                                                        if (j2 >= s.lastActivity + this.mConstants.MAX_SERVICE_INACTIVITY) {
                                                            if (adj10 > clientAdj) {
                                                                adjType5 = "cch-bound-services";
                                                            }
                                                            clientAdj = adj10;
                                                        }
                                                    } else {
                                                        adj10 = adj9;
                                                        if (adj10 > clientAdj) {
                                                            adjType5 = "cch-bound-ui-services";
                                                        }
                                                        processRecord5.cached = false;
                                                        clientAdj = adj10;
                                                        clientProcState2 = procState10;
                                                        cr2 = cr3;
                                                        j2 = now;
                                                    }
                                                } else {
                                                    processRecord5 = app;
                                                    cr2 = cr3;
                                                    adj10 = adj9;
                                                    j2 = now;
                                                }
                                                if (adj10 <= clientAdj) {
                                                    client = client2;
                                                    cr = cr2;
                                                    adjType3 = adjType5;
                                                } else if (!processRecord5.hasShownUi || wpc2.isHomeProcess() || clientAdj <= 200) {
                                                    cr = cr2;
                                                    if ((cr.flags & 72) == 0) {
                                                        adjType3 = adjType5;
                                                        if ((cr.flags & 256) == 0 || clientAdj >= 200 || adj10 <= 250) {
                                                            if ((cr.flags & 1073741824) != 0) {
                                                                i5 = 200;
                                                                if (clientAdj < 200 && adj10 > 200) {
                                                                    newAdj = 200;
                                                                }
                                                            } else {
                                                                i5 = 200;
                                                            }
                                                            if (clientAdj >= i5) {
                                                                newAdj = clientAdj;
                                                            } else if (adj10 > 100) {
                                                                newAdj = Math.max(clientAdj, 100);
                                                            } else {
                                                                newAdj = adj10;
                                                            }
                                                        } else {
                                                            newAdj = 250;
                                                        }
                                                    } else if (clientAdj >= -700) {
                                                        newAdj = clientAdj;
                                                        adjType3 = adjType5;
                                                    } else {
                                                        newAdj = -700;
                                                        procState10 = 0;
                                                        adjType3 = adjType5;
                                                        cr.trackProcState(0, this.mAdjSeq, j2);
                                                        trackedProcState = true;
                                                        schedGroup12 = 2;
                                                    }
                                                    client = client2;
                                                    if (!client.cached) {
                                                        processRecord5.cached = false;
                                                    }
                                                    if (adj10 > newAdj) {
                                                        processRecord5.setCurRawAdj(newAdj);
                                                        adjType3 = "service";
                                                        adj10 = newAdj;
                                                        adj11 = schedGroup12;
                                                    } else {
                                                        adj11 = schedGroup12;
                                                    }
                                                    if ((cr.flags & 8388612) == 0) {
                                                        int curSchedGroup = client.getCurrentSchedulingGroup();
                                                        if (curSchedGroup > adj11) {
                                                            if ((cr.flags & 64) != 0) {
                                                                adj11 = curSchedGroup;
                                                            } else {
                                                                adj11 = 2;
                                                            }
                                                        }
                                                        if (client == TOP_APP) {
                                                            isConnectTopApp4 = true;
                                                        }
                                                        if (clientProcState2 < 2) {
                                                            if (cr.hasFlag(4096)) {
                                                                bestState = 3;
                                                            } else {
                                                                bestState = 6;
                                                            }
                                                            schedGroup15 = adj11;
                                                            if ((cr.flags & DumpState.DUMP_HANDLE) != 0) {
                                                                clientProcState2 = bestState;
                                                            } else if (this.mService.mWakefulness != 1 || (cr.flags & DumpState.DUMP_APEX) == 0) {
                                                                clientProcState2 = 7;
                                                            } else {
                                                                clientProcState2 = bestState;
                                                            }
                                                        } else {
                                                            schedGroup15 = adj11;
                                                            if (clientProcState2 == 2) {
                                                                if (cr.notHasFlag(4096)) {
                                                                    clientProcState2 = 4;
                                                                }
                                                            } else if (clientProcState2 <= 5 && cr.notHasFlag(4096)) {
                                                                clientProcState2 = 5;
                                                            }
                                                        }
                                                        adj11 = schedGroup15;
                                                    } else if ((cr.flags & DumpState.DUMP_VOLUMES) == 0) {
                                                        if (clientProcState2 < 9) {
                                                            clientProcState2 = 9;
                                                        }
                                                    } else if (clientProcState2 < 8) {
                                                        clientProcState2 = 8;
                                                    }
                                                    if (adj11 < 3 || (cr.flags & DumpState.DUMP_FROZEN) == 0) {
                                                        schedGroup14 = adj11;
                                                    } else {
                                                        schedGroup14 = 3;
                                                    }
                                                    if (!trackedProcState) {
                                                        cr.trackProcState(clientProcState2, this.mAdjSeq, j2);
                                                    }
                                                    if (procState10 > clientProcState2) {
                                                        procState10 = clientProcState2;
                                                        processRecord5.setCurRawProcState(procState10);
                                                        if (adjType3 == null) {
                                                            adjType4 = "service";
                                                        } else {
                                                            adjType4 = adjType3;
                                                        }
                                                    } else {
                                                        adjType4 = adjType3;
                                                    }
                                                    if (procState10 < 8 && (cr.flags & 536870912) != 0) {
                                                        processRecord5.setPendingUiClean(true);
                                                    }
                                                    if (adjType4 != null) {
                                                        processRecord5.adjType = adjType4;
                                                        processRecord5.adjTypeCode = 2;
                                                        processRecord5.adjSource = cr.binding.client;
                                                        processRecord5.adjSourceProcState = clientProcState2;
                                                        processRecord5.adjTarget = s.instanceName;
                                                        if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid2 == schedGroup13) {
                                                            StringBuilder sb8 = new StringBuilder();
                                                            sb8.append(str4);
                                                            sb8.append(adjType4);
                                                            str17 = str4;
                                                            str18 = str20;
                                                            sb8.append(str18);
                                                            sb8.append(processRecord5);
                                                            schedGroup12 = schedGroup14;
                                                            sb8.append(", due to ");
                                                            sb8.append(cr.binding.client);
                                                            sb8.append(" adj=");
                                                            sb8.append(adj10);
                                                            sb8.append(" procState=");
                                                            sb8.append(ProcessList.makeProcStateString(procState10));
                                                            String sb9 = sb8.toString();
                                                            str19 = str2;
                                                            reportOomAdjMessageLocked(str19, sb9);
                                                        } else {
                                                            schedGroup12 = schedGroup14;
                                                            str17 = str4;
                                                            str19 = str2;
                                                            str18 = str20;
                                                        }
                                                    } else {
                                                        schedGroup12 = schedGroup14;
                                                        str17 = str4;
                                                        str19 = str2;
                                                        str18 = str20;
                                                    }
                                                    clientProcState = adj10;
                                                } else if (adj10 >= 900) {
                                                    client = client2;
                                                    cr = cr2;
                                                    adjType3 = "cch-bound-ui-services";
                                                    adj11 = schedGroup12;
                                                    if ((cr.flags & 8388612) == 0) {
                                                    }
                                                    if (adj11 < 3) {
                                                    }
                                                    schedGroup14 = adj11;
                                                    if (!trackedProcState) {
                                                    }
                                                    if (procState10 > clientProcState2) {
                                                    }
                                                    processRecord5.setPendingUiClean(true);
                                                    if (adjType4 != null) {
                                                    }
                                                    clientProcState = adj10;
                                                } else {
                                                    client = client2;
                                                    cr = cr2;
                                                    adjType3 = adjType5;
                                                }
                                                adj11 = schedGroup12;
                                                if ((cr.flags & 8388612) == 0) {
                                                }
                                                if (adj11 < 3) {
                                                }
                                                schedGroup14 = adj11;
                                                if (!trackedProcState) {
                                                }
                                                if (procState10 > clientProcState2) {
                                                }
                                                processRecord5.setPendingUiClean(true);
                                                if (adjType4 != null) {
                                                }
                                                clientProcState = adj10;
                                            }
                                        } else {
                                            clist = clist2;
                                            i4 = procState11;
                                            serviceConnections = serviceConnections2;
                                            schedGroup12 = appUid3;
                                            schedGroup13 = appUid2;
                                            str19 = str2;
                                            str17 = str4;
                                            str18 = str3;
                                            cr = cr3;
                                            j2 = j3;
                                        }
                                        if ((cr.flags & DumpState.DUMP_HWFEATURES) != 0) {
                                            processRecord5.treatLikeActivity = true;
                                        }
                                        ActivityServiceConnectionsHolder a = cr.activity;
                                        if ((cr.flags & 128) != 0 && a != null && clientProcState > 0 && a.isActivityVisible()) {
                                            clientProcState = 0;
                                            processRecord5.setCurRawAdj(0);
                                            if ((cr.flags & 4) == 0) {
                                                if ((cr.flags & 64) != 0) {
                                                    schedGroup12 = 4;
                                                } else {
                                                    schedGroup12 = 2;
                                                }
                                            }
                                            processRecord5.cached = false;
                                            processRecord5.adjType = "service";
                                            processRecord5.adjTypeCode = 2;
                                            processRecord5.adjSource = a;
                                            processRecord5.adjSourceProcState = procState10;
                                            processRecord5.adjTarget = s.instanceName;
                                            if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid2 == schedGroup13) {
                                                reportOomAdjMessageLocked(str19, "Raise to service w/activity: " + processRecord5);
                                            }
                                        }
                                        j3 = j2;
                                        str3 = str18;
                                        serviceConnections2 = serviceConnections;
                                        clist2 = clist;
                                        str4 = str17;
                                        str2 = str19;
                                        procState11 = i4 + 1;
                                        appUid2 = schedGroup13;
                                        appUid3 = schedGroup12;
                                    }
                                    processRecord5 = app;
                                    j2 = now;
                                    str17 = str4;
                                    str19 = str2;
                                    clientProcState = adj9;
                                    str18 = str20;
                                    j3 = j2;
                                    str3 = str18;
                                    serviceConnections2 = serviceConnections;
                                    clist2 = clist;
                                    str4 = str17;
                                    str2 = str19;
                                    procState11 = i4 + 1;
                                    appUid2 = schedGroup13;
                                    appUid3 = schedGroup12;
                                }
                                clientProcState = clientProcState;
                                j3 = j3;
                                str3 = str3;
                                serviceConnections2 = serviceConnections2;
                                str4 = str4;
                                schedGroup18 = appUid3;
                                str2 = str2;
                                procState2 = procState10;
                                procState9--;
                                appUid2 = appUid2;
                                appUid = appUid;
                            } else {
                                is2 = appUid;
                                str14 = str4;
                                is3 = appUid2;
                                conni = procState2;
                                str15 = str3;
                                str16 = str2;
                                break;
                            }
                        }
                        j3 = j3;
                        str3 = str15;
                        backupTarget = backupTarget;
                        procState3 = schedGroup18;
                        isConnectTopApp = isConnectTopApp4;
                        str4 = str14;
                        str2 = str16;
                        procState2 = conni;
                        adj2 = clientProcState;
                        wpc3 = wpc2;
                        appUid = is2 - 1;
                        appUid2 = is3;
                    } else {
                        wpc = wpc3;
                        schedGroup5 = procState3;
                        schedGroup6 = procState2;
                        isConnectTopApp2 = isConnectTopApp;
                        str5 = str2;
                        str6 = str4;
                        is = appUid2;
                        str7 = str3;
                        j = j3;
                        break;
                    }
                }
                provi = processRecord5.pubProviders.size() - 1;
                isConnectTopApp3 = isConnectTopApp2;
                while (true) {
                    if (provi < 0) {
                        if (adj2 <= 0 && schedGroup5 != 0 && schedGroup6 <= 2) {
                            str8 = str5;
                            processRecord = processRecord5;
                            adj3 = adj2;
                            processRecord2 = TOP_APP;
                            break;
                        }
                        ContentProviderRecord cpr4 = processRecord5.pubProviders.valueAt(provi);
                        boolean z2 = true;
                        boolean isConnectTopApp5 = isConnectTopApp3;
                        int adj13 = adj2;
                        int adj14 = schedGroup5;
                        int adj15 = cpr4.connections.size() - 1;
                        while (true) {
                            if (adj15 < 0) {
                                schedGroup7 = adj14;
                                schedGroup8 = adj13;
                                str9 = str5;
                                cpr = cpr4;
                                provi2 = provi;
                                str10 = str7;
                                str11 = str6;
                                procState4 = schedGroup6;
                                processRecord3 = processRecord5;
                                break;
                            }
                            if (adj13 <= 0 && adj14 != 0) {
                                if (schedGroup6 <= 2) {
                                    schedGroup7 = adj14;
                                    schedGroup8 = adj13;
                                    str9 = str5;
                                    cpr = cpr4;
                                    provi2 = provi;
                                    str10 = str7;
                                    str11 = str6;
                                    procState4 = schedGroup6;
                                    processRecord3 = processRecord5;
                                    break;
                                }
                            }
                            ContentProviderConnection conn = cpr4.connections.get(adj15);
                            ProcessRecord client3 = conn.client;
                            if (client3 == processRecord5) {
                                procState5 = schedGroup6;
                                schedGroup9 = adj14;
                                adj8 = adj13;
                                str13 = str5;
                                cpr3 = cpr4;
                                adjType = str7;
                                i3 = adj15;
                                processRecord4 = processRecord5;
                            } else {
                                if (this.mService.mCpusetSwitch) {
                                    processRecord5.setCurrentSchedulingGroup(adj14);
                                }
                                adjType = str7;
                                i3 = adj15;
                                procState5 = schedGroup6;
                                schedGroup9 = adj14;
                                adj8 = adj13;
                                str13 = str5;
                                cpr3 = cpr4;
                                processRecord4 = processRecord5;
                                computeOomAdjLocked(client3, client3.getCurRawAdj(), TOP_APP, doingAll, now, cycleReEval);
                                if (!shouldSkipDueToCycle(app, client3, procState5, adj8, cycleReEval)) {
                                    int clientAdj2 = client3.getCurRawAdj();
                                    int clientProcState3 = client3.getCurRawProcState();
                                    if (clientProcState3 >= 17) {
                                        clientProcState3 = 20;
                                    }
                                    String adjType6 = null;
                                    adj7 = adj8;
                                    if (adj7 > clientAdj2) {
                                        if (processRecord4.hasShownUi && !wpc.isHomeProcess()) {
                                            if (clientAdj2 > 200) {
                                                adjType6 = "cch-ui-provider";
                                                processRecord4.cached &= client3.cached;
                                            }
                                        }
                                        adj7 = clientAdj2 > 0 ? clientAdj2 : 0;
                                        processRecord4.setCurRawAdj(adj7);
                                        adjType6 = "provider";
                                        processRecord4.cached &= client3.cached;
                                    }
                                    if (clientProcState3 <= 5) {
                                        if (adjType6 == null) {
                                            adjType6 = "provider";
                                        }
                                        if (clientProcState3 == 2) {
                                            clientProcState3 = 4;
                                        } else {
                                            clientProcState3 = 6;
                                        }
                                    }
                                    conn.trackProcState(clientProcState3, this.mAdjSeq, j);
                                    int procState12 = procState5;
                                    if (procState12 > clientProcState3) {
                                        procState12 = clientProcState3;
                                        processRecord4.setCurRawProcState(procState12);
                                    }
                                    if (client3.getCurrentSchedulingGroup() > schedGroup9) {
                                        schedGroup10 = 2;
                                    } else {
                                        schedGroup10 = schedGroup9;
                                    }
                                    if (client3 == TOP_APP) {
                                        isConnectTopApp5 = true;
                                    }
                                    if (adjType6 != null) {
                                        processRecord4.adjType = adjType6;
                                        processRecord4.adjTypeCode = 1;
                                        processRecord4.adjSource = client3;
                                        processRecord4.adjSourceProcState = clientProcState3;
                                        cpr2 = cpr3;
                                        processRecord4.adjTarget = cpr2.name;
                                        if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid2 == is) {
                                            StringBuilder sb10 = new StringBuilder();
                                            schedGroup11 = schedGroup10;
                                            str12 = str6;
                                            sb10.append(str12);
                                            sb10.append(adjType6);
                                            sb10.append(adjType);
                                            sb10.append(processRecord4);
                                            sb10.append(", due to ");
                                            sb10.append(client3);
                                            sb10.append(" adj=");
                                            sb10.append(adj7);
                                            sb10.append(" procState=");
                                            sb10.append(ProcessList.makeProcStateString(procState12));
                                            adjType2 = str13;
                                            reportOomAdjMessageLocked(adjType2, sb10.toString());
                                        } else {
                                            schedGroup11 = schedGroup10;
                                            str12 = str6;
                                            adjType2 = str13;
                                        }
                                    } else {
                                        schedGroup11 = schedGroup10;
                                        str12 = str6;
                                        cpr2 = cpr3;
                                        adjType2 = str13;
                                    }
                                    schedGroup6 = procState12;
                                    schedGroup9 = schedGroup11;
                                    adj15 = i3 - 1;
                                    str6 = str12;
                                    cpr4 = cpr2;
                                    processRecord5 = processRecord4;
                                    provi = provi;
                                    str7 = adjType;
                                    z2 = true;
                                    str5 = adjType2;
                                    adj13 = adj7;
                                    adj14 = schedGroup9;
                                }
                            }
                            schedGroup6 = procState5;
                            adj7 = adj8;
                            str12 = str6;
                            cpr2 = cpr3;
                            adjType2 = str13;
                            adj15 = i3 - 1;
                            str6 = str12;
                            cpr4 = cpr2;
                            processRecord5 = processRecord4;
                            provi = provi;
                            str7 = adjType;
                            z2 = true;
                            str5 = adjType2;
                            adj13 = adj7;
                            adj14 = schedGroup9;
                        }
                        if (cpr.hasExternalProcessHandles()) {
                            if (schedGroup8 > 0) {
                                adj6 = 0;
                                processRecord3.setCurRawAdj(0);
                                processRecord3.cached = false;
                                processRecord3.adjType = "ext-provider";
                                processRecord3.adjTarget = cpr.name;
                                if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid2 == is) {
                                    reportOomAdjMessageLocked(str9, "Raise adj to external provider: " + processRecord3);
                                }
                                schedGroup7 = 2;
                            } else {
                                adj6 = schedGroup8;
                            }
                            if (procState4 > 7) {
                                schedGroup6 = 7;
                                processRecord3.setCurRawProcState(7);
                                if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid2 == is) {
                                    reportOomAdjMessageLocked(str9, "Raise procstate to external provider: " + processRecord3);
                                }
                            } else {
                                schedGroup6 = procState4;
                            }
                        } else {
                            adj6 = schedGroup8;
                            schedGroup6 = procState4;
                        }
                        schedGroup5 = schedGroup7;
                        provi = provi2 - 1;
                        adj2 = adj6;
                        str5 = str9;
                        str6 = str11;
                        processRecord5 = processRecord3;
                        isConnectTopApp3 = isConnectTopApp5;
                        str7 = str10;
                    } else {
                        str8 = str5;
                        processRecord = processRecord5;
                        adj3 = adj2;
                        processRecord2 = TOP_APP;
                        break;
                    }
                }
                if (processRecord.lastProviderTime > 0) {
                    oomAdjuster = this;
                } else if (processRecord.lastProviderTime + this.mConstants.CONTENT_PROVIDER_RETAIN_TIME > j) {
                    if (adj3 > 700) {
                        adj3 = 700;
                        schedGroup5 = 0;
                        processRecord.cached = false;
                        processRecord.adjType = "recent-provider";
                        if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid2 == is) {
                            oomAdjuster = this;
                            oomAdjuster.reportOomAdjMessageLocked(str8, "Raise adj to recent provider: " + processRecord);
                        } else {
                            oomAdjuster = this;
                        }
                    } else {
                        oomAdjuster = this;
                    }
                    if (schedGroup6 > 16) {
                        schedGroup6 = 16;
                        processRecord.adjType = "recent-provider";
                        if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ_REASON || logUid2 == is) {
                            oomAdjuster.reportOomAdjMessageLocked(str8, "Raise procstate to recent provider: " + processRecord);
                        }
                    }
                } else {
                    oomAdjuster = this;
                }
                if (schedGroup6 >= 20) {
                    if (app.hasClientActivities()) {
                        schedGroup6 = 18;
                        processRecord.adjType = "cch-client-act";
                    } else if (processRecord.treatLikeActivity) {
                        schedGroup6 = 17;
                        processRecord.adjType = "cch-as-act";
                    }
                }
                if (adj3 != 500) {
                    if (doingAll) {
                        processRecord.serviceb = oomAdjuster.mNewNumAServiceProcs > oomAdjuster.mNumServiceProcs / 3;
                        oomAdjuster.mNewNumServiceProcs++;
                        if (!processRecord.serviceb) {
                            if (oomAdjuster.mService.mLastMemoryLevel <= 0) {
                                i2 = 1;
                            } else if (processRecord.lastPss >= oomAdjuster.mProcessList.getCachedRestoreThresholdKb()) {
                                processRecord.serviceHighRam = true;
                                processRecord.serviceb = true;
                            } else {
                                i2 = 1;
                            }
                            oomAdjuster.mNewNumAServiceProcs += i2;
                        } else {
                            processRecord.serviceHighRam = false;
                        }
                    }
                    if (processRecord.serviceb) {
                        adj3 = 800;
                    }
                }
                processRecord.setCurRawAdj(adj3);
                if (adj3 <= processRecord.maxAdj) {
                    int adj16 = processRecord.maxAdj;
                    if (processRecord.maxAdj <= 250) {
                        adj4 = adj16;
                        adj5 = 2;
                    } else {
                        adj4 = adj16;
                        adj5 = schedGroup5;
                    }
                } else {
                    adj4 = adj3;
                    adj5 = schedGroup5;
                }
                if (schedGroup6 < 6) {
                    z = true;
                    if (oomAdjuster.mService.mWakefulness != 1 && adj5 > 1) {
                        adj5 = 1;
                    }
                } else {
                    z = true;
                }
                processRecord.curAdj = processRecord.modifyRawOomAdj(adj4);
                processRecord.setCurrentSchedulingGroup(adj5);
                processRecord.setCurProcState(schedGroup6);
                processRecord.setCurRawProcState(schedGroup6);
                processRecord.setHasForegroundActivities(foregroundActivities2);
                processRecord.completedAdjSeq = oomAdjuster.mAdjSeq;
                if (oomAdjuster.mService.mCpusetSwitch) {
                    oomAdjuster.mService.setWhiteListProcessGroup(processRecord, processRecord2, isConnectTopApp3);
                }
                if (processRecord.curAdj < prevAppAdj) {
                    if (app.getCurProcState() >= prevProcState) {
                        return false;
                    }
                }
                return z;
            }
            procState = procState7;
            schedGroup = schedGroup16;
            if (!foregroundActivities3) {
            }
            str = ActivityManagerService.TAG;
            prevProcState = prevProcState2;
            prevAppAdj = prevAppAdj2;
            logUid = logUid3;
            foregroundActivities = foregroundActivities3;
            schedGroup2 = schedGroup;
            if (procState > 19) {
            }
            logUid2 = logUid;
            str2 = str;
            str3 = ": ";
            str4 = "Raise to ";
            if (adj > 200) {
            }
            if (!app.hasForegroundServices()) {
            }
            if (app.hasForegroundServices()) {
            }
            adj2 = adj;
            foregroundActivities2 = foregroundActivities;
            schedGroup3 = schedGroup2;
            if (adj2 <= 200) {
            }
            adj2 = 200;
            procState = 9;
            processRecord5.cached = false;
            processRecord5.adjType = "force-imp";
            processRecord5.adjSource = processRecord5.forcingToImportant;
            schedGroup4 = 2;
            reportOomAdjMessageLocked(str2, "Raise to force imp: " + processRecord5);
            if (this.mService.mAtmInternal.isHeavyWeightProcess(app.getWindowProcessController())) {
            }
            if (wpc3.isHomeProcess()) {
            }
            if (adj2 > 700) {
            }
            if (procState > 16) {
            }
            processRecord5.setCurRawAdj(cycleReEval ? adj2 : Math.min(adj2, app.getCurRawAdj()));
            if (cycleReEval) {
            }
            processRecord5.setCurRawProcState(i);
            processRecord5.hasStartedServices = false;
            processRecord5.adjSeq = this.mAdjSeq;
            backupTarget = this.mService.mBackupTargets.get(processRecord5.userId);
            if (adj2 > 300) {
            }
            if (procState > 10) {
            }
            procState2 = procState;
            procState3 = schedGroup4;
            appUid = processRecord5.services.size() - 1;
            isConnectTopApp = false;
            while (true) {
                if (appUid < 0) {
                }
                j3 = j3;
                str3 = str15;
                backupTarget = backupTarget;
                procState3 = schedGroup18;
                isConnectTopApp = isConnectTopApp4;
                str4 = str14;
                str2 = str16;
                procState2 = conni;
                adj2 = clientProcState;
                wpc3 = wpc2;
                appUid = is2 - 1;
                appUid2 = is3;
            }
            provi = processRecord5.pubProviders.size() - 1;
            isConnectTopApp3 = isConnectTopApp2;
            while (true) {
                if (provi < 0) {
                }
                schedGroup5 = schedGroup7;
                provi = provi2 - 1;
                adj2 = adj6;
                str5 = str9;
                str6 = str11;
                processRecord5 = processRecord3;
                isConnectTopApp3 = isConnectTopApp5;
                str7 = str10;
            }
            if (processRecord.lastProviderTime > 0) {
            }
            if (schedGroup6 >= 20) {
            }
            if (adj3 != 500) {
            }
            processRecord.setCurRawAdj(adj3);
            if (adj3 <= processRecord.maxAdj) {
            }
            if (schedGroup6 < 6) {
            }
            processRecord.curAdj = processRecord.modifyRawOomAdj(adj4);
            processRecord.setCurrentSchedulingGroup(adj5);
            processRecord.setCurProcState(schedGroup6);
            processRecord.setCurRawProcState(schedGroup6);
            processRecord.setHasForegroundActivities(foregroundActivities2);
            processRecord.completedAdjSeq = oomAdjuster.mAdjSeq;
            if (oomAdjuster.mService.mCpusetSwitch) {
            }
            if (processRecord.curAdj < prevAppAdj) {
            }
            return z;
        }
    }

    private boolean shouldSkipDueToCycle(ProcessRecord app, ProcessRecord client, int procState, int adj, boolean cycleReEval) {
        if (!client.containsCycle) {
            return false;
        }
        app.containsCycle = true;
        if (client.completedAdjSeq >= this.mAdjSeq) {
            return false;
        }
        if (!cycleReEval) {
            return true;
        }
        if (client.getCurRawProcState() < procState || client.getCurRawAdj() < adj) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public void reportOomAdjMessageLocked(String tag, String msg) {
        Slog.d(tag, msg);
        if (this.mService.mCurOomAdjObserver != null) {
            this.mService.mUiHandler.obtainMessage(70, msg).sendToTarget();
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:132:0x0297 A[Catch:{ all -> 0x0212 }] */
    /* JADX WARNING: Removed duplicated region for block: B:137:0x02cc  */
    /* JADX WARNING: Removed duplicated region for block: B:141:0x02e2  */
    /* JADX WARNING: Removed duplicated region for block: B:142:0x02ec  */
    /* JADX WARNING: Removed duplicated region for block: B:145:0x02f7  */
    /* JADX WARNING: Removed duplicated region for block: B:152:0x0313  */
    /* JADX WARNING: Removed duplicated region for block: B:167:0x039e  */
    /* JADX WARNING: Removed duplicated region for block: B:170:0x03c8  */
    /* JADX WARNING: Removed duplicated region for block: B:173:0x0409  */
    /* JADX WARNING: Removed duplicated region for block: B:198:0x0499  */
    /* JADX WARNING: Removed duplicated region for block: B:209:0x04c7  */
    /* JADX WARNING: Removed duplicated region for block: B:220:0x0585  */
    @GuardedBy({"mService"})
    private final boolean applyOomAdjLocked(ProcessRecord app, boolean doingAll, long now, long nowElapsed) {
        String str;
        String str2;
        boolean success;
        int changes;
        int changes2;
        String str3;
        String str4;
        String str5;
        int i;
        int changes3;
        int processGroup;
        Exception e;
        if (app.getCurRawAdj() != app.setRawAdj) {
            app.setRawAdj = app.getCurRawAdj();
        }
        if (this.mAppCompact.useCompaction() && this.mService.mBooted) {
            if (app.curAdj != app.setAdj) {
                if (app.setAdj <= 200 && (app.curAdj == 700 || app.curAdj == 600)) {
                    this.mAppCompact.compactAppSome(app);
                } else if ((app.setAdj < 900 || app.setAdj > 999) && app.curAdj >= 900 && app.curAdj <= 999) {
                    this.mAppCompact.compactAppFull(app);
                }
            } else if (this.mService.mWakefulness != 1 && app.setAdj < 0 && this.mAppCompact.shouldCompactPersistent(app, now)) {
                this.mAppCompact.compactAppPersistent(app);
            } else if (this.mService.mWakefulness != 1 && app.getCurProcState() == 6 && this.mAppCompact.shouldCompactBFGS(app, now)) {
                this.mAppCompact.compactAppBfgs(app);
            }
        }
        if (app.curAdj != app.setAdj) {
            ProcessList.setOomAdj(app.pid, app.uid, app.curAdj);
            if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_OOM_ADJ || this.mService.mCurOomAdjUid == app.info.uid) {
                reportOomAdjMessageLocked(ActivityManagerService.TAG, "Set " + app.pid + " " + app.processName + " adj " + app.curAdj + ": " + app.adjType);
            }
            app.setAdj = app.curAdj;
            app.verifiedAdj = -10000;
        }
        int curSchedGroup = app.getCurrentSchedulingGroup();
        if (app.setSchedGroup != curSchedGroup) {
            int oldSchedGroup = app.setSchedGroup;
            app.setSchedGroup = curSchedGroup;
            if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_OOM_ADJ || this.mService.mCurOomAdjUid == app.uid) {
                reportOomAdjMessageLocked(ActivityManagerService.TAG, "Setting sched group of " + app.processName + " to " + curSchedGroup + ": " + app.adjType);
            }
            if (app.waitingToKill == null || !app.curReceivers.isEmpty() || app.setSchedGroup != 0) {
                if (curSchedGroup == 0) {
                    processGroup = 0;
                } else if (curSchedGroup == 1) {
                    processGroup = 7;
                } else if (curSchedGroup == 3 || curSchedGroup == 4) {
                    processGroup = 5;
                } else {
                    processGroup = -1;
                }
                if (this.mService.mCpusetSwitch) {
                    success = true;
                    str2 = " to ";
                    Message msg = this.mProcessGroupHandler.obtainMessage(0, app.pid, processGroup, app);
                    Bundle data = new Bundle();
                    data.putInt("curSchedGroup", curSchedGroup);
                    msg.setData(data);
                    this.mProcessGroupHandler.sendMessage(msg);
                } else {
                    success = true;
                    str2 = " to ";
                    Handler handler = this.mProcessGroupHandler;
                    handler.sendMessage(handler.obtainMessage(0, app.pid, processGroup));
                }
                if (curSchedGroup != 3) {
                    str = ActivityManagerService.TAG;
                    if (oldSchedGroup == 3 && curSchedGroup != 3) {
                        app.getWindowProcessController().onTopProcChanged();
                        if (this.mService.mUseFifoUiScheduling) {
                            try {
                                try {
                                    Process.setThreadScheduler(app.pid, 0, 0);
                                    Process.setThreadPriority(app.pid, app.savedPriority);
                                    if (app.renderThreadTid != 0) {
                                        Process.setThreadScheduler(app.renderThreadTid, 0, 0);
                                        Process.setThreadPriority(app.renderThreadTid, -4);
                                    }
                                } catch (Exception e2) {
                                    e = e2;
                                    if (ActivityManagerDebugConfig.DEBUG_ALL) {
                                    }
                                    this.mService.mDAProxy.notifyProcessGroupChange(app.pid, app.uid);
                                    if (this.mService.mCpusetSwitch) {
                                    }
                                    if (app.repForegroundActivities == app.hasForegroundActivities()) {
                                    }
                                    if (app.getReportedProcState() != app.getCurProcState()) {
                                    }
                                    if (app.setProcState != 21) {
                                    }
                                    app.lastStateTime = now;
                                    app.nextPssTime = ProcessList.computeNextPssTime(app.getCurProcState(), app.procStateMemTracker, this.mService.mTestPssMode, this.mService.mAtmInternal.isSleeping(), now);
                                    if (ActivityManagerDebugConfig.DEBUG_PSS) {
                                    }
                                    if (app.setProcState == app.getCurProcState()) {
                                    }
                                    if (changes2 == 0) {
                                    }
                                    return success;
                                }
                            } catch (IllegalArgumentException e3) {
                                Slog.w(TAG, "Failed to set scheduling policy, thread does not exist:\n" + e3);
                            } catch (SecurityException e4) {
                                Slog.w(TAG, "Failed to set scheduling policy, not allowed:\n" + e4);
                            }
                        } else {
                            Process.setThreadPriority(app.pid, 0);
                            if (app.renderThreadTid != 0) {
                                Process.setThreadPriority(app.renderThreadTid, 0);
                            }
                        }
                    }
                } else if (oldSchedGroup != 3) {
                    try {
                        app.getWindowProcessController().onTopProcChanged();
                        if (this.mService.mUseFifoUiScheduling) {
                            app.savedPriority = Process.getThreadPriority(app.pid);
                            ActivityManagerService activityManagerService = this.mService;
                            ActivityManagerService.scheduleAsFifoPriority(app.pid, true);
                            if (app.renderThreadTid != 0) {
                                ActivityManagerService activityManagerService2 = this.mService;
                                int i2 = app.renderThreadTid;
                                str = ActivityManagerService.TAG;
                                try {
                                    ActivityManagerService.scheduleAsFifoPriority(i2, true);
                                    if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ) {
                                        Slog.d("UI_FIFO", "Set RenderThread (TID " + app.renderThreadTid + ") to FIFO");
                                    }
                                } catch (Exception e5) {
                                    e = e5;
                                    try {
                                        if (ActivityManagerDebugConfig.DEBUG_ALL) {
                                            Slog.w(TAG, "Failed setting thread priority of " + app.pid, e);
                                        }
                                        this.mService.mDAProxy.notifyProcessGroupChange(app.pid, app.uid);
                                        if (this.mService.mCpusetSwitch) {
                                        }
                                        if (app.repForegroundActivities == app.hasForegroundActivities()) {
                                        }
                                        if (app.getReportedProcState() != app.getCurProcState()) {
                                        }
                                        if (app.setProcState != 21) {
                                        }
                                        app.lastStateTime = now;
                                        app.nextPssTime = ProcessList.computeNextPssTime(app.getCurProcState(), app.procStateMemTracker, this.mService.mTestPssMode, this.mService.mAtmInternal.isSleeping(), now);
                                        if (ActivityManagerDebugConfig.DEBUG_PSS) {
                                        }
                                        if (app.setProcState == app.getCurProcState()) {
                                        }
                                        if (changes2 == 0) {
                                        }
                                        return success;
                                    } catch (Throwable th) {
                                        this.mService.mDAProxy.notifyProcessGroupChange(app.pid, app.uid);
                                        throw th;
                                    }
                                }
                            } else {
                                str = ActivityManagerService.TAG;
                                if (ActivityManagerDebugConfig.DEBUG_OOM_ADJ) {
                                    Slog.d("UI_FIFO", "Not setting RenderThread TID");
                                }
                            }
                        } else {
                            str = ActivityManagerService.TAG;
                            Process.setThreadPriority(app.pid, -10);
                            if (app.renderThreadTid != 0) {
                                try {
                                    Process.setThreadPriority(app.renderThreadTid, -10);
                                } catch (IllegalArgumentException e6) {
                                }
                            }
                        }
                    } catch (Exception e7) {
                        e = e7;
                        str = ActivityManagerService.TAG;
                        if (ActivityManagerDebugConfig.DEBUG_ALL) {
                        }
                        this.mService.mDAProxy.notifyProcessGroupChange(app.pid, app.uid);
                        if (this.mService.mCpusetSwitch) {
                        }
                        if (app.repForegroundActivities == app.hasForegroundActivities()) {
                        }
                        if (app.getReportedProcState() != app.getCurProcState()) {
                        }
                        if (app.setProcState != 21) {
                        }
                        app.lastStateTime = now;
                        app.nextPssTime = ProcessList.computeNextPssTime(app.getCurProcState(), app.procStateMemTracker, this.mService.mTestPssMode, this.mService.mAtmInternal.isSleeping(), now);
                        if (ActivityManagerDebugConfig.DEBUG_PSS) {
                        }
                        if (app.setProcState == app.getCurProcState()) {
                        }
                        if (changes2 == 0) {
                        }
                        return success;
                    }
                } else {
                    str = ActivityManagerService.TAG;
                }
                this.mService.mDAProxy.notifyProcessGroupChange(app.pid, app.uid);
                if (this.mService.mCpusetSwitch) {
                    this.mService.mHwAMSEx.setThreadSchedPolicy(oldSchedGroup, app);
                }
            } else {
                app.kill(app.waitingToKill, true);
                success = false;
                str2 = " to ";
                str = ActivityManagerService.TAG;
            }
        } else {
            success = true;
            str2 = " to ";
            str = ActivityManagerService.TAG;
        }
        if (app.repForegroundActivities == app.hasForegroundActivities()) {
            app.repForegroundActivities = app.hasForegroundActivities();
            changes = 0 | 1;
        } else {
            changes = 0;
        }
        if (app.getReportedProcState() != app.getCurProcState()) {
            app.setReportedProcState(app.getCurProcState());
            if (app.thread != null) {
                try {
                    app.thread.setProcessState(app.getReportedProcState());
                } catch (RemoteException e8) {
                }
            }
        }
        if (app.setProcState != 21) {
            str3 = " ";
            str4 = str2;
            changes2 = changes;
            str5 = str;
        } else if (ProcessList.procStatesDifferForMem(app.getCurProcState(), app.setProcState)) {
            str3 = " ";
            str4 = str2;
            changes2 = changes;
            str5 = str;
        } else {
            if (now <= app.nextPssTime) {
                if (now <= app.lastPssTime + AppStandbyController.SettingsObserver.DEFAULT_STRONG_USAGE_TIMEOUT) {
                    str3 = " ";
                    str4 = str2;
                    changes2 = changes;
                    str5 = str;
                } else if (now <= app.lastStateTime + ProcessList.minTimeFromStateChange(this.mService.mTestPssMode)) {
                    str3 = " ";
                    str4 = str2;
                    changes2 = changes;
                    str5 = str;
                }
                if (app.setProcState == app.getCurProcState()) {
                    if (ActivityTaskManagerDebugConfig.DEBUG_SWITCH || ActivityManagerDebugConfig.DEBUG_OOM_ADJ || this.mService.mCurOomAdjUid == app.uid) {
                        reportOomAdjMessageLocked(str5, "Proc state change of " + app.processName + str4 + ProcessList.makeProcStateString(app.getCurProcState()) + " (" + app.getCurProcState() + "): " + app.adjType);
                    }
                    boolean setImportant = app.setProcState < 11;
                    boolean curImportant = app.getCurProcState() < 11;
                    if (setImportant && !curImportant) {
                        app.setWhenUnimportant(now);
                        app.lastCpuTime = 0;
                    }
                    maybeUpdateUsageStatsLocked(app, nowElapsed);
                    maybeUpdateLastTopTime(app, now);
                    app.setProcState = app.getCurProcState();
                    if (app.setProcState >= 15) {
                        i = 0;
                        app.notCachedSinceIdle = false;
                    } else {
                        i = 0;
                    }
                    if (!doingAll) {
                        ActivityManagerService activityManagerService3 = this.mService;
                        activityManagerService3.setProcessTrackerStateLocked(app, activityManagerService3.mProcessStats.getMemFactorLocked(), now);
                    } else {
                        app.procStateChanged = true;
                    }
                } else {
                    i = 0;
                    if (app.reportedInteraction && nowElapsed - app.getInteractionEventTime() > this.mConstants.USAGE_STATS_INTERACTION_INTERVAL) {
                        maybeUpdateUsageStatsLocked(app, nowElapsed);
                    } else if (!app.reportedInteraction && nowElapsed - app.getFgInteractionTime() > this.mConstants.SERVICE_USAGE_INTERACTION_TIME) {
                        maybeUpdateUsageStatsLocked(app, nowElapsed);
                    }
                }
                if (changes2 == 0) {
                    if (ActivityManagerDebugConfig.DEBUG_PROCESS_OBSERVERS) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Changes in ");
                        sb.append(app);
                        sb.append(": ");
                        changes3 = changes2;
                        sb.append(changes3);
                        Slog.i(str5, sb.toString());
                    } else {
                        changes3 = changes2;
                    }
                    ActivityManagerService.ProcessChangeItem item = this.mService.enqueueProcessChangeItemLocked(app.pid, app.info.uid);
                    item.changes = changes3;
                    item.foregroundActivities = app.repForegroundActivities;
                    if (ActivityManagerDebugConfig.DEBUG_PROCESS_OBSERVERS) {
                        Slog.i(str5, "Item " + Integer.toHexString(System.identityHashCode(item)) + str3 + app.toShortString() + ": changes=" + item.changes + " foreground=" + item.foregroundActivities + " type=" + app.adjType + " source=" + app.adjSource + " target=" + app.adjTarget);
                    }
                    StringBuilder sb2 = new StringBuilder();
                    sb2.append("name=");
                    sb2.append(app.info.packageName);
                    sb2.append(" state=");
                    int i3 = 1;
                    if (!item.foregroundActivities) {
                        i3 = i;
                    }
                    sb2.append(i3);
                    HwLog.dubaie("DUBAI_TAG_ACTIVITY_STATE", sb2.toString());
                }
                return success;
            }
            if (this.mService.requestPssLocked(app, app.setProcState)) {
                str3 = " ";
                str4 = str2;
                changes2 = changes;
                str5 = str;
                app.nextPssTime = ProcessList.computeNextPssTime(app.getCurProcState(), app.procStateMemTracker, this.mService.mTestPssMode, this.mService.mAtmInternal.isSleeping(), now);
            } else {
                str3 = " ";
                str4 = str2;
                changes2 = changes;
                str5 = str;
            }
            if (app.setProcState == app.getCurProcState()) {
            }
            if (changes2 == 0) {
            }
            return success;
        }
        app.lastStateTime = now;
        app.nextPssTime = ProcessList.computeNextPssTime(app.getCurProcState(), app.procStateMemTracker, this.mService.mTestPssMode, this.mService.mAtmInternal.isSleeping(), now);
        if (ActivityManagerDebugConfig.DEBUG_PSS) {
            Slog.d(str5, "Process state change from " + ProcessList.makeProcStateString(app.setProcState) + str4 + ProcessList.makeProcStateString(app.getCurProcState()) + " next pss in " + (app.nextPssTime - now) + ": " + app);
        }
        if (app.setProcState == app.getCurProcState()) {
        }
        if (changes2 == 0) {
        }
        return success;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void maybeUpdateUsageStats(ProcessRecord app, long nowElapsed) {
        synchronized (this.mService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                maybeUpdateUsageStatsLocked(app, nowElapsed);
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    @GuardedBy({"mService"})
    private void maybeUpdateUsageStatsLocked(ProcessRecord app, long nowElapsed) {
        boolean isInteraction;
        if (ActivityManagerDebugConfig.DEBUG_USAGE_STATS) {
            Slog.d(TAG, "Checking proc [" + Arrays.toString(app.getPackageList()) + "] state changes: old = " + app.setProcState + ", new = " + app.getCurProcState());
        }
        if (this.mService.mUsageStatsService != null) {
            if (app.getCurProcState() <= 2 || app.getCurProcState() == 4) {
                isInteraction = true;
                app.setFgInteractionTime(0);
            } else {
                boolean z = false;
                if (app.getCurProcState() > 5) {
                    if (app.getCurProcState() <= 7) {
                        z = true;
                    }
                    isInteraction = z;
                    app.setFgInteractionTime(0);
                } else if (app.getFgInteractionTime() == 0) {
                    app.setFgInteractionTime(nowElapsed);
                    isInteraction = false;
                } else {
                    if (nowElapsed > app.getFgInteractionTime() + this.mConstants.SERVICE_USAGE_INTERACTION_TIME) {
                        z = true;
                    }
                    isInteraction = z;
                }
            }
            if (isInteraction && (!app.reportedInteraction || nowElapsed - app.getInteractionEventTime() > this.mConstants.USAGE_STATS_INTERACTION_INTERVAL)) {
                app.setInteractionEventTime(nowElapsed);
                String[] packages = app.getPackageList();
                if (packages != null) {
                    for (String str : packages) {
                        this.mService.mUsageStatsService.reportEvent(str, app.userId, 6);
                    }
                }
            }
            app.reportedInteraction = isInteraction;
            if (!isInteraction) {
                app.setInteractionEventTime(0);
            }
        }
    }

    private void maybeUpdateLastTopTime(ProcessRecord app, long nowUptime) {
        if (app.setProcState <= 2 && app.getCurProcState() > 2) {
            app.lastTopTime = nowUptime;
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public void idleUidsLocked() {
        int N = this.mActiveUids.size();
        if (N > 0) {
            long nowElapsed = SystemClock.elapsedRealtime();
            long maxBgTime = nowElapsed - this.mConstants.BACKGROUND_SETTLE_TIME;
            long nextTime = 0;
            PowerManagerInternal powerManagerInternal = this.mLocalPowerManager;
            if (powerManagerInternal != null) {
                powerManagerInternal.startUidChanges();
            }
            for (int i = N - 1; i >= 0; i--) {
                UidRecord uidRec = this.mActiveUids.valueAt(i);
                long bgTime = uidRec.lastBackgroundTime;
                if (bgTime > 0 && !uidRec.idle) {
                    if (bgTime <= maxBgTime) {
                        EventLogTags.writeAmUidIdle(uidRec.uid);
                        uidRec.idle = true;
                        uidRec.setIdle = true;
                        this.mService.doStopUidLocked(uidRec.uid, uidRec);
                    } else if (nextTime == 0 || nextTime > bgTime) {
                        nextTime = bgTime;
                    }
                }
            }
            PowerManagerInternal powerManagerInternal2 = this.mLocalPowerManager;
            if (powerManagerInternal2 != null) {
                powerManagerInternal2.finishUidChanges();
            }
            if (nextTime > 0) {
                this.mService.mHandler.removeMessages(58);
                this.mService.mHandler.sendEmptyMessageDelayed(58, (this.mConstants.BACKGROUND_SETTLE_TIME + nextTime) - nowElapsed);
            }
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public final void setAppIdTempWhitelistStateLocked(int appId, boolean onWhitelist) {
        boolean changed = false;
        for (int i = this.mActiveUids.size() - 1; i >= 0; i--) {
            UidRecord uidRec = this.mActiveUids.valueAt(i);
            if (UserHandle.getAppId(uidRec.uid) == appId && uidRec.curWhitelist != onWhitelist) {
                uidRec.curWhitelist = onWhitelist;
                changed = true;
            }
        }
        if (changed) {
            updateOomAdjLocked(OOM_ADJ_REASON_WHITELIST);
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public final void setUidTempWhitelistStateLocked(int uid, boolean onWhitelist) {
        UidRecord uidRec = this.mActiveUids.get(uid);
        if (uidRec != null && uidRec.curWhitelist != onWhitelist) {
            uidRec.curWhitelist = onWhitelist;
            updateOomAdjLocked(OOM_ADJ_REASON_WHITELIST);
        }
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public void dumpProcessListVariablesLocked(ProtoOutputStream proto) {
        proto.write(1120986464305L, this.mAdjSeq);
        proto.write(1120986464306L, this.mProcessList.mLruSeq);
        proto.write(1120986464307L, this.mNumNonCachedProcs);
        proto.write(1120986464309L, this.mNumServiceProcs);
        proto.write(1120986464310L, this.mNewNumServiceProcs);
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public void dumpSequenceNumbersLocked(PrintWriter pw) {
        pw.println("  mAdjSeq=" + this.mAdjSeq + " mLruSeq=" + this.mProcessList.mLruSeq);
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public void dumpProcCountsLocked(PrintWriter pw) {
        pw.println("  mNumNonCachedProcs=" + this.mNumNonCachedProcs + " (" + this.mProcessList.getLruSizeLocked() + " total) mNumCachedHiddenProcs=" + this.mNumCachedHiddenProcs + " mNumServiceProcs=" + this.mNumServiceProcs + " mNewNumServiceProcs=" + this.mNewNumServiceProcs);
    }

    /* access modifiers changed from: package-private */
    @GuardedBy({"mService"})
    public void dumpAppCompactorSettings(PrintWriter pw) {
        this.mAppCompact.dump(pw);
    }
}
