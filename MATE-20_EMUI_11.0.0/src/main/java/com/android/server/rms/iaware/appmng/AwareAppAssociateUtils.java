package com.android.server.rms.iaware.appmng;

import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.ActivityInfoEx;
import android.os.Bundle;
import android.os.SystemClock;
import android.rms.iaware.AwareLog;
import android.util.ArraySet;
import android.util.SparseArray;
import com.android.internal.app.ProcessMap;
import com.android.server.mtm.utils.SparseSet;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.rms.iaware.qos.AwareBinderSchedManager;
import com.huawei.android.app.HwActivityTaskManager;
import com.huawei.android.os.UserHandleEx;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Optional;

public class AwareAppAssociateUtils {
    private static final String TAG = "RMS.AwareAppAssociate";

    protected static boolean isAppLock(int uid) {
        ActivityInfo activityInfo;
        ComponentName componentName;
        if (UserHandleEx.getAppId(uid) != 1000 || (activityInfo = HwActivityTaskManager.getLastResumedActivity()) == null || (componentName = ActivityInfoEx.getComponentName(activityInfo)) == null) {
            return false;
        }
        return AwareIntelligentRecg.getInstance().isAppLockClassName(componentName.getClassName());
    }

    protected static boolean checkProcessRelationParams(int callerUid, int targetUid, int callerPid, int type) {
        if (!checkType(type)) {
            return false;
        }
        if (callerUid == targetUid) {
            if (AwareAppAssociate.isDebugEnabled()) {
                AwareLog.i(TAG, typeToString(type) + " in the same UID.Pass.");
            }
            return false;
        } else if (callerPid > 0 && callerUid > 0 && targetUid > 0) {
            return true;
        } else {
            if (AwareAppAssociate.isDebugEnabled()) {
                AwareLog.i(TAG, typeToString(type) + " with wrong pid or uid");
            }
            return false;
        }
    }

    protected static boolean isInRestriction(int oldMode, int newMode) {
        return (oldMode == 0 || oldMode == 3) && newMode == 1;
    }

    protected static void getAssocProviderLocked(SparseArray<AssocPidRecord> assocRecordMap, int pid, SparseSet assocProvider) {
        long curElapse = SystemClock.elapsedRealtime();
        AssocPidRecord record = assocRecordMap.get(pid);
        if (record != null) {
            int np = record.assocProvider.getMap().size();
            for (int i = 0; i < np; i++) {
                SparseArray<AssocBaseRecord> brs = (SparseArray) record.assocProvider.getMap().valueAt(i);
                int nb = brs.size();
                for (int j = 0; j < nb; j++) {
                    AssocBaseRecord br = brs.valueAt(j);
                    int targetPid = br.pid;
                    if (targetPid != 0 && br.isStrong && curElapse - br.miniTime < 120000) {
                        assocProvider.add(targetPid);
                    }
                }
            }
        }
    }

    protected static void getStrongAssocClientLocked(AssocPidRecord record, int clientPid, int pid, SparseSet strong) {
        int np = record.assocBindService.getMap().size();
        for (int i = 0; i < np; i++) {
            SparseArray<AssocBaseRecord> brs = (SparseArray) record.assocBindService.getMap().valueAt(i);
            int nb = brs.size();
            for (int j = 0; j < nb; j++) {
                AssocBaseRecord br = brs.valueAt(j);
                if (br != null && br.pid == pid) {
                    strong.add(clientPid);
                }
            }
        }
    }

    protected static void getStrongAssocLocked(SparseArray<AssocPidRecord> assocRecordMap, int pid, SparseSet strong) {
        long curElapse = SystemClock.elapsedRealtime();
        AssocPidRecord record = assocRecordMap.get(pid);
        if (record != null) {
            int np = record.assocBindService.getMap().size();
            for (int i = 0; i < np; i++) {
                SparseArray<AssocBaseRecord> brs = (SparseArray) record.assocBindService.getMap().valueAt(i);
                int nb = brs.size();
                for (int j = 0; j < nb; j++) {
                    int targetPid = brs.valueAt(j).pid;
                    if (targetPid != 0) {
                        strong.add(targetPid);
                    }
                }
            }
            int np2 = record.assocProvider.getMap().size();
            for (int i2 = 0; i2 < np2; i2++) {
                SparseArray<AssocBaseRecord> brs2 = (SparseArray) record.assocProvider.getMap().valueAt(i2);
                int nb2 = brs2.size();
                for (int j2 = 0; j2 < nb2; j2++) {
                    AssocBaseRecord br = brs2.valueAt(j2);
                    int targetPid2 = br.pid;
                    if (targetPid2 != 0 && br.isStrong && curElapse - br.miniTime < 120000) {
                        strong.add(targetPid2);
                    }
                }
            }
        }
    }

    protected static void removeDiedRecordProc(int uid, int pid) {
        if (uid <= 0) {
            AwareLog.i(TAG, "removeDiedRecodrProc with wrong pid or uid");
        } else {
            AwareIntelligentRecg.getInstance().removeDiedScreenProc(uid, pid);
        }
    }

    protected static boolean checkType(int type) {
        if (type == 1 || type == 2 || type == 3) {
            return true;
        }
        return false;
    }

    protected static String typeToString(int type) {
        if (type == 1) {
            return "ADD_ASSOC_BINDSERVICE";
        }
        if (type == 2) {
            return "ADD_ASSOC_PROVIDER";
        }
        if (type == 3) {
            return "DEL_ASSOC_BINDSERVICE";
        }
        if (type == 4) {
            return "APP_ASSOC_PROCESSUPDATE";
        }
        return "[Error type]" + type;
    }

    /* access modifiers changed from: protected */
    public static final class AssocBaseRecord {
        public HashSet<String> components = new HashSet<>();
        public ArraySet<String> componentsJob = new ArraySet<>();
        public boolean isStrong = true;
        public long miniTime;
        public int pid;
        public ArraySet<String> pkgList = new ArraySet<>();
        public String processName;
        public int uid;

        public AssocBaseRecord(String name, int uid2, int pid2) {
            this.processName = name;
            this.uid = uid2;
            this.pid = pid2;
            this.miniTime = SystemClock.elapsedRealtime();
        }
    }

    /* access modifiers changed from: protected */
    public static final class AssocPidRecord {
        public final ProcessMap<AssocBaseRecord> assocBindService = new ProcessMap<>();
        public final ProcessMap<AssocBaseRecord> assocProvider = new ProcessMap<>();
        public int pid;
        public String processName;
        public int uid;

        public AssocPidRecord(int pid2, int uid2, String name) {
            this.pid = pid2;
            this.uid = uid2;
            this.processName = name;
        }

        public Optional<ProcessMap<AssocBaseRecord>> getMap(int type) {
            if (type != 1) {
                if (type == 2) {
                    return Optional.ofNullable(this.assocProvider);
                }
                if (type != 3) {
                    return Optional.empty();
                }
            }
            return Optional.ofNullable(this.assocBindService);
        }

        public boolean isEmpty() {
            return this.assocBindService.getMap().isEmpty() && this.assocProvider.getMap().isEmpty();
        }

        public int size() {
            return this.assocBindService.getMap().size() + this.assocProvider.getMap().size();
        }

        private void toStringInner(int np, boolean flag, StringBuilder sb) {
            for (int i = 0; i < np; i++) {
                SparseArray<AssocBaseRecord> brs = (SparseArray) this.assocBindService.getMap().valueAt(i);
                int nb = brs.size();
                for (int j = 0; j < nb; j++) {
                    AssocBaseRecord br = brs.valueAt(j);
                    if (flag) {
                        sb.append("    [BindService] depend on:");
                        sb.append(System.lineSeparator());
                        flag = false;
                    }
                    Iterator<String> it = br.components.iterator();
                    while (it.hasNext()) {
                        sb.append("        Pid:");
                        sb.append(br.pid);
                        sb.append(",Uid:");
                        sb.append(br.uid);
                        sb.append(",ProcessName:");
                        sb.append(br.processName);
                        sb.append(",Time:");
                        sb.append(SystemClock.elapsedRealtime() - br.miniTime);
                        sb.append(",Component:");
                        sb.append(it.next());
                        sb.append(System.lineSeparator());
                    }
                    Iterator<String> it2 = br.componentsJob.iterator();
                    while (it2.hasNext()) {
                        sb.append("        [jobService] Pid:");
                        sb.append(br.pid);
                        sb.append(",Uid:");
                        sb.append(br.uid);
                        sb.append(",ProcessName:");
                        sb.append(br.processName);
                        sb.append(",componentJob:");
                        sb.append(it2.next());
                        sb.append(System.lineSeparator());
                    }
                }
            }
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Pid:");
            sb.append(this.pid);
            sb.append(",Uid:");
            sb.append(this.uid);
            sb.append(",ProcessName:");
            sb.append(this.processName);
            sb.append(System.lineSeparator());
            String sameUid = AwareAppAssociate.getInstance().sameUid(this.pid).orElse(null);
            if (sameUid != null) {
                sb.append(sameUid);
            }
            toStringInner(this.assocBindService.getMap().size(), true, sb);
            int np = this.assocProvider.getMap().size();
            boolean flag = true;
            for (int i = 0; i < np; i++) {
                SparseArray<AssocBaseRecord> brs = (SparseArray) this.assocProvider.getMap().valueAt(i);
                int nb = brs.size();
                for (int j = 0; j < nb; j++) {
                    AssocBaseRecord br = brs.valueAt(j);
                    if (flag) {
                        sb.append("    [Provider] depend on:");
                        sb.append(System.lineSeparator());
                        flag = false;
                    }
                    Iterator<String> it = br.components.iterator();
                    while (it.hasNext()) {
                        String component = it.next();
                        if (SystemClock.elapsedRealtime() - br.miniTime >= 120000) {
                            sameUid = sameUid;
                            np = np;
                        } else {
                            sb.append("        Pid:");
                            sb.append(br.pid);
                            sb.append(",Uid:");
                            sb.append(br.uid);
                            sb.append(",ProcessName:");
                            sb.append(br.processName);
                            sb.append(",Time:");
                            sb.append(SystemClock.elapsedRealtime() - br.miniTime);
                            sb.append(",Component:");
                            sb.append(component);
                            sb.append(",Strong:");
                            sb.append(br.isStrong);
                            sb.append(System.lineSeparator());
                            sameUid = sameUid;
                            np = np;
                        }
                    }
                }
            }
            return sb.toString();
        }
    }

    protected static void report(int eventId, Bundle bundleArgs) {
        if (bundleArgs != null) {
            if (eventId == 4) {
                try {
                    int callerPid = bundleArgs.getInt("callPid");
                    int callerUid = bundleArgs.getInt("callUid");
                    String callerProcessName = bundleArgs.getString("callProcName");
                    AwareAppAssociate.getInstance().updateProcessRelation(callerPid, callerUid, callerProcessName, bundleArgs.getStringArrayList(MemoryConstant.MEM_PREREAD_ITEM_NAME));
                    AwareBinderSchedManager.getInstance().reportProcessStarted(callerPid, callerUid, callerProcessName);
                } catch (ArrayIndexOutOfBoundsException e) {
                    AwareLog.e(TAG, "getStringArrayList out of bounds exception!");
                }
            } else if (eventId == 33) {
                AwareIntelligentRecg.getInstance().reportGoogleConn(bundleArgs.getBoolean("gms_conn"));
            } else if (eventId == 25) {
                AwareIntelligentRecg.getInstance().addScreenRecord(bundleArgs.getInt("callUid"), bundleArgs.getInt("callPid"));
            } else if (eventId == 26) {
                AwareIntelligentRecg.getInstance().removeScreenRecord(bundleArgs.getInt("callUid"), bundleArgs.getInt("callPid"));
            } else if (eventId == 30) {
                AwareIntelligentRecg.getInstance().addCamera(bundleArgs.getInt("callUid"));
            } else if (eventId == 31) {
                AwareIntelligentRecg.getInstance().removeCamera(bundleArgs.getInt("callUid"));
            }
        }
    }
}
