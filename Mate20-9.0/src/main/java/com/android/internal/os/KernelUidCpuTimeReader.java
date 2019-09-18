package com.android.internal.os;

import android.os.StrictMode;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Slog;
import android.util.SparseLongArray;
import android.util.TimeUtils;
import com.android.internal.content.NativeLibraryHelper;
import com.android.internal.os.KernelUidCpuTimeReaderBase;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class KernelUidCpuTimeReader extends KernelUidCpuTimeReaderBase<Callback> {
    private static final String TAG = KernelUidCpuTimeReader.class.getSimpleName();
    private static final String sProcFile = "/proc/uid_cputime/show_uid_stat";
    private static final String sRemoveUidProcFile = "/proc/uid_cputime/remove_uid_range";
    private SparseLongArray mLastSystemTimeUs = new SparseLongArray();
    private long mLastTimeReadUs = 0;
    private SparseLongArray mLastUserTimeUs = new SparseLongArray();

    public interface Callback extends KernelUidCpuTimeReaderBase.Callback {
        void onUidCpuTime(int i, long j, long j2);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0060, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0061, code lost:
        r4 = r3;
        r3 = r7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0065, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0066, code lost:
        r7 = r0;
        r4 = r3;
        r35 = r8;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:127:0x01e1 A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0065 A[ExcHandler: Throwable (r0v38 'th' java.lang.Throwable A[CUSTOM_DECLARE]), Splitter:B:12:0x0054] */
    /* JADX WARNING: Removed duplicated region for block: B:30:0x0099 A[SYNTHETIC, Splitter:B:30:0x0099] */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00a9  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00b4 A[SYNTHETIC, Splitter:B:38:0x00b4] */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x01bc A[Catch:{ Throwable -> 0x01f2, all -> 0x01eb }] */
    /* JADX WARNING: Removed duplicated region for block: B:73:0x01d6 A[Catch:{ Throwable -> 0x01f2, all -> 0x01eb }] */
    public void readDeltaImpl(Callback callback) {
        long nowUs;
        Throwable th;
        String line;
        Throwable th2;
        long nowUs2;
        long systemTimeUs;
        long powerMaUs;
        long nowUs3;
        TextUtils.SimpleStringSplitter splitter;
        boolean notifyCallback;
        long nowUs4;
        boolean notifyCallback2;
        int oldMask = StrictMode.allowThreadDiskReadsMask();
        long j = 1000;
        long nowUs5 = SystemClock.elapsedRealtime() * 1000;
        Throwable th3 = null;
        String line2 = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(sProcFile));
            try {
                TextUtils.SimpleStringSplitter splitter2 = new TextUtils.SimpleStringSplitter(' ');
                while (true) {
                    String readLine = reader.readLine();
                    line2 = readLine;
                    if (readLine != null) {
                        try {
                            splitter2.setString(line2);
                            String uidStr = splitter2.next();
                            int uid = Integer.parseInt(uidStr.substring(0, uidStr.length() - 1), 10);
                            long userTimeUs = Long.parseLong(splitter2.next(), 10);
                            long systemTimeUs2 = 0;
                            if (splitter2.hasNext()) {
                                try {
                                    systemTimeUs2 = Long.parseLong(splitter2.next(), 10);
                                } catch (Throwable th4) {
                                    th = th4;
                                    nowUs = nowUs5;
                                    th = null;
                                    try {
                                        $closeResource(th, reader);
                                        throw th;
                                    } catch (IOException e) {
                                        e = e;
                                        Slog.e(TAG, "Failed to read uid_cputime: " + e.getMessage());
                                        StrictMode.setThreadPolicyMask(oldMask);
                                        this.mLastTimeReadUs = nowUs;
                                    } catch (NumberFormatException e2) {
                                        e = e2;
                                        Slog.e(TAG, "read uid_cputime has NumberFormatException, line:" + line2);
                                        Slog.e(TAG, "Failed to read uid_cputime", e);
                                        StrictMode.setThreadPolicyMask(oldMask);
                                        this.mLastTimeReadUs = nowUs;
                                    } catch (StringIndexOutOfBoundsException e3) {
                                        e = e3;
                                        Slog.e(TAG, "read uid_cputime has StringIndexOutOfBoundsException, line:" + line2);
                                        Slog.e(TAG, "Failed to read uid_cputime", e);
                                        StrictMode.setThreadPolicyMask(oldMask);
                                        this.mLastTimeReadUs = nowUs;
                                    }
                                }
                            } else {
                                Slog.w(TAG, "Read uid_cputime has system time format exception when split line:" + line2);
                                int uIdIndex = this.mLastUserTimeUs.indexOfKey(uid);
                                if (uIdIndex >= 0) {
                                    systemTimeUs = this.mLastSystemTimeUs.valueAt(uIdIndex);
                                    if (splitter2.hasNext() == 0) {
                                        powerMaUs = Long.parseLong(splitter2.next(), 10) / j;
                                    } else {
                                        powerMaUs = 0;
                                    }
                                    long j2 = powerMaUs;
                                    long userTimeDeltaUs = userTimeUs;
                                    long systemTimeDeltaUs = systemTimeUs;
                                    if (callback != null) {
                                        splitter = splitter2;
                                        nowUs3 = nowUs5;
                                        notifyCallback = false;
                                        line = line2;
                                        nowUs4 = userTimeUs;
                                    } else if (this.mLastTimeReadUs != 0) {
                                        int index = this.mLastUserTimeUs.indexOfKey(uid);
                                        if (index >= 0) {
                                            userTimeDeltaUs -= this.mLastUserTimeUs.valueAt(index);
                                            systemTimeDeltaUs -= this.mLastSystemTimeUs.valueAt(index);
                                            long timeDiffUs = nowUs5 - this.mLastTimeReadUs;
                                            if (userTimeDeltaUs >= 0) {
                                                if (systemTimeDeltaUs >= 0) {
                                                    splitter = splitter2;
                                                    nowUs3 = nowUs5;
                                                    int i = index;
                                                    line = line2;
                                                    nowUs4 = userTimeUs;
                                                    j = 1000;
                                                }
                                            }
                                            splitter = splitter2;
                                            StringBuilder sb = new StringBuilder("Malformed cpu data for UID=");
                                            sb.append(uid);
                                            sb.append("!\n");
                                            sb.append("Time between reads: ");
                                            nowUs3 = nowUs5;
                                            try {
                                                TimeUtils.formatDuration(timeDiffUs / 1000, sb);
                                                sb.append("\n");
                                                sb.append("Previous times: u=");
                                                TimeUtils.formatDuration(this.mLastUserTimeUs.valueAt(index) / 1000, sb);
                                                sb.append(" s=");
                                                TimeUtils.formatDuration(this.mLastSystemTimeUs.valueAt(index) / 1000, sb);
                                                sb.append("\nCurrent times: u=");
                                                int i2 = index;
                                                long j3 = timeDiffUs;
                                                nowUs4 = userTimeUs;
                                                TimeUtils.formatDuration(nowUs4 / 1000, sb);
                                                sb.append(" s=");
                                                line = line2;
                                            } catch (Throwable th5) {
                                                th = th5;
                                                String str = line2;
                                                nowUs = nowUs3;
                                                th = null;
                                                $closeResource(th, reader);
                                                throw th;
                                            }
                                            try {
                                                TimeUtils.formatDuration(systemTimeUs / 1000, sb);
                                                sb.append("\nDelta: u=");
                                                TimeUtils.formatDuration(userTimeDeltaUs / 1000, sb);
                                                sb.append(" s=");
                                                j = 1000;
                                                TimeUtils.formatDuration(systemTimeDeltaUs / 1000, sb);
                                                Slog.e(TAG, sb.toString());
                                                userTimeDeltaUs = 0;
                                                systemTimeDeltaUs = 0;
                                            } catch (Throwable th6) {
                                                th = th6;
                                                nowUs = nowUs3;
                                                line2 = line;
                                                th = null;
                                                $closeResource(th, reader);
                                                throw th;
                                            }
                                        } else {
                                            splitter = splitter2;
                                            nowUs3 = nowUs5;
                                            int i3 = index;
                                            line = line2;
                                            nowUs4 = userTimeUs;
                                            j = 1000;
                                        }
                                        if (userTimeDeltaUs == 0) {
                                            if (systemTimeDeltaUs == 0) {
                                                notifyCallback2 = false;
                                                notifyCallback = notifyCallback2;
                                            }
                                        }
                                        notifyCallback2 = true;
                                        notifyCallback = notifyCallback2;
                                    } else {
                                        splitter = splitter2;
                                        nowUs3 = nowUs5;
                                        notifyCallback = false;
                                        line = line2;
                                        nowUs4 = userTimeUs;
                                        j = 1000;
                                    }
                                    long userTimeDeltaUs2 = userTimeDeltaUs;
                                    long systemTimeDeltaUs2 = systemTimeDeltaUs;
                                    this.mLastUserTimeUs.put(uid, nowUs4);
                                    this.mLastSystemTimeUs.put(uid, systemTimeUs);
                                    if (!notifyCallback) {
                                        callback.onUidCpuTime(uid, userTimeDeltaUs2, systemTimeDeltaUs2);
                                    }
                                    splitter2 = splitter;
                                    nowUs5 = nowUs3;
                                    String str2 = line;
                                    th3 = null;
                                }
                            }
                            systemTimeUs = systemTimeUs2;
                            if (splitter2.hasNext() == 0) {
                            }
                            long j22 = powerMaUs;
                            long userTimeDeltaUs3 = userTimeUs;
                            long systemTimeDeltaUs3 = systemTimeUs;
                            if (callback != null) {
                            }
                            long userTimeDeltaUs22 = userTimeDeltaUs3;
                            long systemTimeDeltaUs22 = systemTimeDeltaUs3;
                            this.mLastUserTimeUs.put(uid, nowUs4);
                            this.mLastSystemTimeUs.put(uid, systemTimeUs);
                            if (!notifyCallback) {
                            }
                            splitter2 = splitter;
                            nowUs5 = nowUs3;
                            String str22 = line;
                            th3 = null;
                        } catch (Throwable th7) {
                            th = th7;
                            String str3 = line2;
                            nowUs = nowUs5;
                            th = null;
                            $closeResource(th, reader);
                            throw th;
                        }
                    } else {
                        long nowUs6 = nowUs5;
                        String line3 = line2;
                        try {
                            $closeResource(null, reader);
                            StrictMode.setThreadPolicyMask(oldMask);
                            this.mLastTimeReadUs = nowUs6;
                            String str4 = line3;
                            return;
                        } catch (IOException e4) {
                            e = e4;
                            nowUs = nowUs6;
                            String str5 = line3;
                            Slog.e(TAG, "Failed to read uid_cputime: " + e.getMessage());
                            StrictMode.setThreadPolicyMask(oldMask);
                            this.mLastTimeReadUs = nowUs;
                        } catch (NumberFormatException e5) {
                            e = e5;
                            nowUs = nowUs6;
                            line2 = line3;
                            Slog.e(TAG, "read uid_cputime has NumberFormatException, line:" + line2);
                            Slog.e(TAG, "Failed to read uid_cputime", e);
                            StrictMode.setThreadPolicyMask(oldMask);
                            this.mLastTimeReadUs = nowUs;
                        } catch (StringIndexOutOfBoundsException e6) {
                            e = e6;
                            nowUs = nowUs6;
                            line2 = line3;
                            Slog.e(TAG, "read uid_cputime has StringIndexOutOfBoundsException, line:" + line2);
                            Slog.e(TAG, "Failed to read uid_cputime", e);
                            StrictMode.setThreadPolicyMask(oldMask);
                            this.mLastTimeReadUs = nowUs;
                        } catch (Throwable th8) {
                            th = th8;
                            nowUs = nowUs6;
                            String str6 = line3;
                            StrictMode.setThreadPolicyMask(oldMask);
                            this.mLastTimeReadUs = nowUs;
                            throw th;
                        }
                    }
                }
            } catch (Throwable th9) {
                th = th9;
                nowUs = nowUs5;
                th = null;
                $closeResource(th, reader);
                throw th;
            }
        } catch (IOException e7) {
            e = e7;
            nowUs = nowUs5;
            Slog.e(TAG, "Failed to read uid_cputime: " + e.getMessage());
            StrictMode.setThreadPolicyMask(oldMask);
            this.mLastTimeReadUs = nowUs;
        } catch (NumberFormatException e8) {
            e = e8;
            nowUs = nowUs5;
            Slog.e(TAG, "read uid_cputime has NumberFormatException, line:" + line2);
            Slog.e(TAG, "Failed to read uid_cputime", e);
            StrictMode.setThreadPolicyMask(oldMask);
            this.mLastTimeReadUs = nowUs;
        } catch (StringIndexOutOfBoundsException e9) {
            e = e9;
            nowUs = nowUs5;
            Slog.e(TAG, "read uid_cputime has StringIndexOutOfBoundsException, line:" + line2);
            Slog.e(TAG, "Failed to read uid_cputime", e);
            StrictMode.setThreadPolicyMask(oldMask);
            this.mLastTimeReadUs = nowUs;
        } catch (Throwable th10) {
            th = th10;
            StrictMode.setThreadPolicyMask(oldMask);
            this.mLastTimeReadUs = nowUs;
            throw th;
        }
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    public void readAbsolute(Callback callback) {
        BufferedReader reader;
        int oldMask = StrictMode.allowThreadDiskReadsMask();
        try {
            reader = new BufferedReader(new FileReader(sProcFile));
            TextUtils.SimpleStringSplitter splitter = new TextUtils.SimpleStringSplitter(' ');
            while (true) {
                String readLine = reader.readLine();
                String line = readLine;
                if (readLine == null) {
                    break;
                }
                splitter.setString(line);
                String uidStr = splitter.next();
                callback.onUidCpuTime(Integer.parseInt(uidStr.substring(0, uidStr.length() - 1), 10), Long.parseLong(splitter.next(), 10), Long.parseLong(splitter.next(), 10));
            }
            $closeResource(null, reader);
        } catch (IOException e) {
            try {
                String str = TAG;
                Slog.e(str, "Failed to read uid_cputime: " + e.getMessage());
            } catch (Throwable th) {
                StrictMode.setThreadPolicyMask(oldMask);
                throw th;
            }
        } catch (Throwable th2) {
            $closeResource(r2, reader);
            throw th2;
        }
        StrictMode.setThreadPolicyMask(oldMask);
    }

    public void removeUid(int uid) {
        int index = this.mLastSystemTimeUs.indexOfKey(uid);
        if (index >= 0) {
            this.mLastSystemTimeUs.removeAt(index);
            this.mLastUserTimeUs.removeAt(index);
        }
        removeUidsFromKernelModule(uid, uid);
    }

    public void removeUidsInRange(int startUid, int endUid) {
        if (endUid >= startUid) {
            this.mLastSystemTimeUs.put(startUid, 0);
            this.mLastUserTimeUs.put(startUid, 0);
            this.mLastSystemTimeUs.put(endUid, 0);
            this.mLastUserTimeUs.put(endUid, 0);
            int startIndex = this.mLastSystemTimeUs.indexOfKey(startUid);
            int endIndex = this.mLastSystemTimeUs.indexOfKey(endUid);
            this.mLastSystemTimeUs.removeAtRange(startIndex, (endIndex - startIndex) + 1);
            this.mLastUserTimeUs.removeAtRange(startIndex, (endIndex - startIndex) + 1);
            removeUidsFromKernelModule(startUid, endUid);
        }
    }

    private void removeUidsFromKernelModule(int startUid, int endUid) {
        FileWriter writer;
        String str = TAG;
        Slog.d(str, "Removing uids " + startUid + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + endUid);
        int oldMask = StrictMode.allowThreadDiskWritesMask();
        try {
            writer = new FileWriter(sRemoveUidProcFile);
            writer.write(startUid + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + endUid);
            writer.flush();
            $closeResource(null, writer);
        } catch (IOException e) {
            try {
                String str2 = TAG;
                Slog.e(str2, "failed to remove uids " + startUid + " - " + endUid + " from uid_cputime module", e);
            } catch (Throwable th) {
                StrictMode.setThreadPolicyMask(oldMask);
                throw th;
            }
        } catch (Throwable th2) {
            $closeResource(r2, writer);
            throw th2;
        }
        StrictMode.setThreadPolicyMask(oldMask);
    }
}
