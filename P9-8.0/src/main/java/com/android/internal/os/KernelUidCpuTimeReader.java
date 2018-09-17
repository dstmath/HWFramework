package com.android.internal.os;

import android.os.SystemClock;
import android.text.TextUtils.SimpleStringSplitter;
import android.util.Slog;
import android.util.SparseLongArray;
import android.util.TimeUtils;
import com.android.internal.content.NativeLibraryHelper;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class KernelUidCpuTimeReader {
    private static final String TAG = "KernelUidCpuTimeReader";
    private static final String sProcFile = "/proc/uid_cputime/show_uid_stat";
    private static final String sRemoveUidProcFile = "/proc/uid_cputime/remove_uid_range";
    private SparseLongArray mLastSystemTimeUs = new SparseLongArray();
    private long mLastTimeReadUs = 0;
    private SparseLongArray mLastUserTimeUs = new SparseLongArray();

    public interface Callback {
        void onUidCpuTime(int i, long j, long j2);
    }

    /* JADX WARNING: Removed duplicated region for block: B:37:0x01be A:{SYNTHETIC, Splitter: B:37:0x01be} */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x0283 A:{SYNTHETIC, Splitter: B:70:0x0283} */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x01c3 A:{SYNTHETIC, Splitter: B:40:0x01c3} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void readDelta(Callback callback) {
        NumberFormatException e;
        StringIndexOutOfBoundsException e2;
        Throwable th;
        long nowUs = SystemClock.elapsedRealtime() * 1000;
        String line = null;
        Throwable th2 = null;
        BufferedReader reader = null;
        try {
            IOException e3;
            BufferedReader bufferedReader = new BufferedReader(new FileReader(sProcFile));
            try {
                SimpleStringSplitter simpleStringSplitter = new SimpleStringSplitter(' ');
                while (true) {
                    line = bufferedReader.readLine();
                    if (line == null) {
                        break;
                    }
                    simpleStringSplitter.setString(line);
                    String uidStr = simpleStringSplitter.next();
                    int uid = Integer.parseInt(uidStr.substring(0, uidStr.length() - 1), 10);
                    long userTimeUs = Long.parseLong(simpleStringSplitter.next(), 10);
                    long systemTimeUs = 0;
                    if (simpleStringSplitter.hasNext()) {
                        systemTimeUs = Long.parseLong(simpleStringSplitter.next(), 10);
                    } else {
                        Slog.w(TAG, "Read uid_cputime has system time format exception when split line:" + line);
                        int uIdIndex = this.mLastUserTimeUs.indexOfKey(uid);
                        if (uIdIndex >= 0) {
                            systemTimeUs = this.mLastSystemTimeUs.valueAt(uIdIndex);
                        }
                    }
                    if (simpleStringSplitter.hasNext()) {
                        long powerMaUs = Long.parseLong(simpleStringSplitter.next(), 10) / 1000;
                    }
                    if (!(callback == null || this.mLastTimeReadUs == 0)) {
                        long userTimeDeltaUs = userTimeUs;
                        long systemTimeDeltaUs = systemTimeUs;
                        int index = this.mLastUserTimeUs.indexOfKey(uid);
                        if (index >= 0) {
                            userTimeDeltaUs = userTimeUs - this.mLastUserTimeUs.valueAt(index);
                            systemTimeDeltaUs -= this.mLastSystemTimeUs.valueAt(index);
                            long timeDiffUs = nowUs - this.mLastTimeReadUs;
                            if (userTimeDeltaUs < 0 || systemTimeDeltaUs < 0) {
                                StringBuilder stringBuilder = new StringBuilder("Malformed cpu data for UID=");
                                stringBuilder.append(uid).append("!\n");
                                stringBuilder.append("Time between reads: ");
                                TimeUtils.formatDuration(timeDiffUs / 1000, stringBuilder);
                                stringBuilder.append("\n");
                                stringBuilder.append("Previous times: u=");
                                TimeUtils.formatDuration(this.mLastUserTimeUs.valueAt(index) / 1000, stringBuilder);
                                stringBuilder.append(" s=");
                                TimeUtils.formatDuration(this.mLastSystemTimeUs.valueAt(index) / 1000, stringBuilder);
                                stringBuilder.append("\nCurrent times: u=");
                                TimeUtils.formatDuration(userTimeUs / 1000, stringBuilder);
                                stringBuilder.append(" s=");
                                TimeUtils.formatDuration(systemTimeUs / 1000, stringBuilder);
                                stringBuilder.append("\nDelta: u=");
                                TimeUtils.formatDuration(userTimeDeltaUs / 1000, stringBuilder);
                                stringBuilder.append(" s=");
                                TimeUtils.formatDuration(systemTimeDeltaUs / 1000, stringBuilder);
                                Slog.e(TAG, stringBuilder.toString());
                                userTimeDeltaUs = 0;
                                systemTimeDeltaUs = 0;
                            }
                        }
                        if (!(userTimeDeltaUs == 0 && systemTimeDeltaUs == 0)) {
                            callback.onUidCpuTime(uid, userTimeDeltaUs, systemTimeDeltaUs);
                        }
                    }
                    this.mLastUserTimeUs.put(uid, userTimeUs);
                    this.mLastSystemTimeUs.put(uid, systemTimeUs);
                }
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 != null) {
                    try {
                        throw th2;
                    } catch (IOException e4) {
                        e3 = e4;
                        reader = bufferedReader;
                    } catch (NumberFormatException e5) {
                        e = e5;
                        reader = bufferedReader;
                        Slog.e(TAG, "read uid_cputime has NumberFormatException, line:" + line);
                        Slog.e(TAG, "Failed to read uid_cputime", e);
                        this.mLastTimeReadUs = nowUs;
                    } catch (StringIndexOutOfBoundsException e6) {
                        e2 = e6;
                        reader = bufferedReader;
                        Slog.e(TAG, "read uid_cputime has StringIndexOutOfBoundsException, line:" + line);
                        Slog.e(TAG, "Failed to read uid_cputime", e2);
                        this.mLastTimeReadUs = nowUs;
                    }
                }
                reader = bufferedReader;
                this.mLastTimeReadUs = nowUs;
            } catch (Throwable th4) {
                th = th4;
                reader = bufferedReader;
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (Throwable th5) {
                        if (th2 == null) {
                            th2 = th5;
                        } else if (th2 != th5) {
                            th2.addSuppressed(th5);
                        }
                    }
                }
                if (th2 == null) {
                    try {
                        throw th2;
                    } catch (IOException e7) {
                        e3 = e7;
                    } catch (NumberFormatException e8) {
                        e = e8;
                        Slog.e(TAG, "read uid_cputime has NumberFormatException, line:" + line);
                        Slog.e(TAG, "Failed to read uid_cputime", e);
                        this.mLastTimeReadUs = nowUs;
                    } catch (StringIndexOutOfBoundsException e9) {
                        e2 = e9;
                        Slog.e(TAG, "read uid_cputime has StringIndexOutOfBoundsException, line:" + line);
                        Slog.e(TAG, "Failed to read uid_cputime", e2);
                        this.mLastTimeReadUs = nowUs;
                    }
                }
                throw th;
            }
            Slog.e(TAG, "Failed to read uid_cputime: " + e3.getMessage());
            this.mLastTimeReadUs = nowUs;
        } catch (Throwable th6) {
            th = th6;
            if (reader != null) {
            }
            if (th2 == null) {
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x0062 A:{SYNTHETIC, Splitter: B:25:0x0062} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0075 A:{Catch:{ IOException -> 0x0068 }} */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0067 A:{SYNTHETIC, Splitter: B:28:0x0067} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void removeUid(int uid) {
        IOException e;
        Throwable th;
        Throwable th2 = null;
        int index = this.mLastUserTimeUs.indexOfKey(uid);
        if (index >= 0) {
            this.mLastUserTimeUs.removeAt(index);
            this.mLastSystemTimeUs.removeAt(index);
        }
        FileWriter writer = null;
        try {
            FileWriter writer2 = new FileWriter(sRemoveUidProcFile);
            try {
                writer2.write(Integer.toString(uid) + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + Integer.toString(uid));
                writer2.flush();
                if (writer2 != null) {
                    try {
                        writer2.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 != null) {
                    try {
                        throw th2;
                    } catch (IOException e2) {
                        e = e2;
                        writer = writer2;
                    }
                }
            } catch (Throwable th4) {
                th = th4;
                writer = writer2;
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (Throwable th5) {
                        if (th2 == null) {
                            th2 = th5;
                        } else if (th2 != th5) {
                            th2.addSuppressed(th5);
                        }
                    }
                }
                if (th2 == null) {
                    try {
                        throw th2;
                    } catch (IOException e3) {
                        e = e3;
                        Slog.e(TAG, "failed to remove uid from uid_cputime module", e);
                        return;
                    }
                }
                throw th;
            }
        } catch (Throwable th6) {
            th = th6;
            if (writer != null) {
            }
            if (th2 == null) {
            }
        }
    }
}
