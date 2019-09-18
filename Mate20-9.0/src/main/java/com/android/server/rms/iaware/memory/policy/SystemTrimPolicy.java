package com.android.server.rms.iaware.memory.policy;

import android.os.Debug;
import android.rms.iaware.AwareLog;
import android.util.ArrayMap;
import com.android.server.mtm.iaware.appmng.AwareProcessInfo;
import com.android.server.mtm.taskstatus.ProcessInfo;
import com.android.server.rms.memrepair.ProcStateStatisData;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class SystemTrimPolicy {
    private static final String TAG = "AwareMem_SysTrim";
    private static Object lock = new Object();
    private static final AtomicBoolean mIsFeatureEnable = new AtomicBoolean(false);
    private static SystemTrimPolicy mSystemTrimPolicy = null;
    private Map<String, Long> mProcThreshold = new ArrayMap();

    private SystemTrimPolicy() {
    }

    public static SystemTrimPolicy getInstance() {
        SystemTrimPolicy systemTrimPolicy;
        synchronized (lock) {
            if (mSystemTrimPolicy == null) {
                mSystemTrimPolicy = new SystemTrimPolicy();
            }
            systemTrimPolicy = mSystemTrimPolicy;
        }
        return systemTrimPolicy;
    }

    public void enable() {
        if (mIsFeatureEnable.get()) {
            AwareLog.d(TAG, "SystemTrimPolicy has already enable!");
        } else {
            mIsFeatureEnable.set(true);
        }
    }

    public void disable() {
        if (!mIsFeatureEnable.get()) {
            AwareLog.d(TAG, "SystemTrimPolicy has already disable!");
        } else {
            mIsFeatureEnable.set(false);
        }
    }

    public void updateProcThreshold(String packageName, long threshold) {
        AwareLog.d(TAG, "set sys app for trim, name=" + packageName + ", threshold=" + threshold);
        synchronized (this.mProcThreshold) {
            this.mProcThreshold.put(packageName, Long.valueOf(threshold));
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:13:0x002d, code lost:
        if (r14 == null) goto L_0x0108;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0033, code lost:
        if (r14.isEmpty() == false) goto L_0x0037;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0037, code lost:
        r0 = new android.util.ArraySet<>();
        r1 = new android.util.ArraySet<>();
        r2 = new android.util.ArrayMap<>();
        r3 = r14.iterator();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x004e, code lost:
        if (r3.hasNext() == false) goto L_0x0107;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0050, code lost:
        r4 = r3.next();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0057, code lost:
        if (r4 == null) goto L_0x004a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x005b, code lost:
        if (r4.mProcInfo == null) goto L_0x004a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0061, code lost:
        if (r4.mProcInfo.mPackageName == null) goto L_0x004a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x006b, code lost:
        if (r4.mProcInfo.mPackageName.isEmpty() == false) goto L_0x006e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x006e, code lost:
        r5 = (java.lang.String) r4.mProcInfo.mPackageName.get(0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x007e, code lost:
        if (android.text.TextUtils.isEmpty(r5) == false) goto L_0x0081;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0081, code lost:
        r8 = r13.mProcThreshold;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0085, code lost:
        monitor-enter(r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x008c, code lost:
        if (r13.mProcThreshold.containsKey(r5) != false) goto L_0x0090;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x008e, code lost:
        monitor-exit(r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0090, code lost:
        r6 = r13.mProcThreshold.get(r5).longValue();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x009d, code lost:
        monitor-exit(r8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00a2, code lost:
        if (r1.contains(r5) == false) goto L_0x00c1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00a4, code lost:
        r0.add(r4.mProcInfo);
        android.rms.iaware.AwareLog.d(TAG, "the proc which need to trim is in the list, pkg=" + r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00c1, code lost:
        r8 = getPss(r4.mProcInfo);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00cb, code lost:
        if (r2.containsKey(r5) == false) goto L_0x00d8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00cd, code lost:
        r8 = r8 + r2.get(r5).longValue();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00d8, code lost:
        r2.put(r5, java.lang.Long.valueOf(r8));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x00e1, code lost:
        if (r8 <= r6) goto L_0x004a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00e3, code lost:
        r1.add(r5);
        r0.add(r4.mProcInfo);
        android.rms.iaware.AwareLog.d(TAG, "the proc need to trim, pkg=" + r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x0107, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x0108, code lost:
        android.rms.iaware.AwareLog.w(TAG, "no proc need to trim");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x010f, code lost:
        return null;
     */
    public Set<ProcessInfo> getProcNeedTrim(List<AwareProcessInfo> procsGroups) {
        AwareLog.d(TAG, "SystemTrimPolicy.getProcNeedTrim enter");
        if (!mIsFeatureEnable.get()) {
            AwareLog.d(TAG, "SystemTrimPolicy has already disable!");
            return null;
        }
        synchronized (this.mProcThreshold) {
            if (this.mProcThreshold.isEmpty()) {
                AwareLog.w(TAG, "no app proc need to trim");
                return null;
            }
        }
    }

    private long getPss(ProcessInfo procInfo) {
        if (procInfo == null) {
            return 0;
        }
        long pss = 0;
        int pid = procInfo.mPid;
        int uid = procInfo.mUid;
        if (checkPidValid(pid, uid)) {
            pss = ProcStateStatisData.getInstance().getProcPss(uid, pid);
            if (pss <= 0) {
                pss = Debug.getPss(pid, null, null);
            }
        }
        AwareLog.d(TAG, "the proc , pid=" + pid + ", uid=" + uid + ", pss=" + pss);
        return pss;
    }

    private boolean checkPidValid(int pid, int uid) {
        if (!new File("/acct/uid_" + uid + "/pid_" + pid).exists()) {
            try {
                int realUid = ((Integer) Files.getAttribute(Paths.get("/proc/" + pid + "/status/", new String[0]), "unix:uid", new LinkOption[0])).intValue();
                if (realUid != uid) {
                    AwareLog.w(TAG, "read uid " + realUid + " of " + pid + " is not match");
                    return false;
                }
            } catch (IOException e) {
                AwareLog.w(TAG, "read status of " + pid + " failed");
                return false;
            } catch (UnsupportedOperationException e2) {
                AwareLog.w(TAG, "read status of " + pid + " failed");
                return false;
            } catch (IllegalArgumentException e3) {
                AwareLog.w(TAG, "read status of " + pid + " failed");
                return false;
            } catch (SecurityException e4) {
                AwareLog.w(TAG, "read status of " + pid + " failed");
                return false;
            } catch (Exception e5) {
                AwareLog.w(TAG, "read status of " + pid + " failed");
                return false;
            }
        }
        return true;
    }
}
