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
    private SparseLongArray mLastPowerMaUs;
    private SparseLongArray mLastSystemTimeUs;
    private long mLastTimeReadUs;
    private SparseLongArray mLastUserTimeUs;

    public interface Callback {
        void onUidCpuTime(int i, long j, long j2, long j3);
    }

    public KernelUidCpuTimeReader() {
        this.mLastUserTimeUs = new SparseLongArray();
        this.mLastSystemTimeUs = new SparseLongArray();
        this.mLastPowerMaUs = new SparseLongArray();
        this.mLastTimeReadUs = 0;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void readDelta(Callback callback) {
        IOException e;
        NumberFormatException e2;
        StringIndexOutOfBoundsException e3;
        Throwable th;
        long nowUs = SystemClock.elapsedRealtime() * 1000;
        String line = null;
        Throwable th2 = null;
        BufferedReader bufferedReader = null;
        try {
            BufferedReader bufferedReader2 = new BufferedReader(new FileReader(sProcFile));
            try {
                SimpleStringSplitter simpleStringSplitter = new SimpleStringSplitter(' ');
                while (true) {
                    line = bufferedReader2.readLine();
                    if (line == null) {
                        break;
                    }
                    long powerMaUs;
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
                        powerMaUs = Long.parseLong(simpleStringSplitter.next(), 10) / 1000;
                    } else {
                        powerMaUs = 0;
                    }
                    if (callback != null) {
                        if (this.mLastTimeReadUs != 0) {
                            long userTimeDeltaUs = userTimeUs;
                            long systemTimeDeltaUs = systemTimeUs;
                            long powerDeltaMaUs = powerMaUs;
                            int index = this.mLastUserTimeUs.indexOfKey(uid);
                            if (index >= 0) {
                                userTimeDeltaUs = userTimeUs - this.mLastUserTimeUs.valueAt(index);
                                systemTimeDeltaUs -= this.mLastSystemTimeUs.valueAt(index);
                                powerDeltaMaUs -= this.mLastPowerMaUs.valueAt(index);
                                long timeDiffUs = nowUs - this.mLastTimeReadUs;
                                if (userTimeDeltaUs >= 0 && systemTimeDeltaUs >= 0) {
                                    if (powerDeltaMaUs < 0) {
                                    }
                                }
                                StringBuilder stringBuilder = new StringBuilder("Malformed cpu data for UID=");
                                stringBuilder.append(uid).append("!\n");
                                stringBuilder.append("Time between reads: ");
                                TimeUtils.formatDuration(timeDiffUs / 1000, stringBuilder);
                                stringBuilder.append("\n");
                                stringBuilder.append("Previous times: u=");
                                TimeUtils.formatDuration(this.mLastUserTimeUs.valueAt(index) / 1000, stringBuilder);
                                stringBuilder.append(" s=");
                                TimeUtils.formatDuration(this.mLastSystemTimeUs.valueAt(index) / 1000, stringBuilder);
                                stringBuilder = stringBuilder;
                                stringBuilder.append(" p=").append(this.mLastPowerMaUs.valueAt(index) / 1000);
                                stringBuilder.append("mAms\n");
                                stringBuilder.append("Current times: u=");
                                TimeUtils.formatDuration(userTimeUs / 1000, stringBuilder);
                                stringBuilder.append(" s=");
                                TimeUtils.formatDuration(systemTimeUs / 1000, stringBuilder);
                                stringBuilder = stringBuilder;
                                stringBuilder.append(" p=").append(powerMaUs / 1000);
                                stringBuilder.append("mAms\n");
                                stringBuilder.append("Delta: u=");
                                TimeUtils.formatDuration(userTimeDeltaUs / 1000, stringBuilder);
                                stringBuilder.append(" s=");
                                TimeUtils.formatDuration(systemTimeDeltaUs / 1000, stringBuilder);
                                stringBuilder = stringBuilder;
                                String str = "mAms";
                                stringBuilder.append(" p=").append(powerDeltaMaUs / 1000).append(r34);
                                Slog.wtf(TAG, stringBuilder.toString());
                                userTimeDeltaUs = 0;
                                systemTimeDeltaUs = 0;
                                powerDeltaMaUs = 0;
                            }
                            if (userTimeDeltaUs == 0 && systemTimeDeltaUs == 0) {
                                if (powerDeltaMaUs != 0) {
                                }
                            }
                            callback.onUidCpuTime(uid, userTimeDeltaUs, systemTimeDeltaUs, powerDeltaMaUs);
                        }
                    }
                    this.mLastUserTimeUs.put(uid, userTimeUs);
                    this.mLastSystemTimeUs.put(uid, systemTimeUs);
                    this.mLastPowerMaUs.put(uid, powerMaUs);
                }
                if (bufferedReader2 != null) {
                    try {
                        bufferedReader2.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 != null) {
                    try {
                        throw th2;
                    } catch (IOException e4) {
                        e = e4;
                        bufferedReader = bufferedReader2;
                    } catch (NumberFormatException e5) {
                        e2 = e5;
                        bufferedReader = bufferedReader2;
                        Slog.e(TAG, "read uid_cputime has NumberFormatException, line:" + line);
                        Slog.e(TAG, "Failed to read uid_cputime", e2);
                        this.mLastTimeReadUs = nowUs;
                    } catch (StringIndexOutOfBoundsException e6) {
                        e3 = e6;
                        bufferedReader = bufferedReader2;
                        Slog.e(TAG, "read uid_cputime has StringIndexOutOfBoundsException, line:" + line);
                        Slog.e(TAG, "Failed to read uid_cputime", e3);
                        this.mLastTimeReadUs = nowUs;
                    }
                }
                bufferedReader = bufferedReader2;
                this.mLastTimeReadUs = nowUs;
            } catch (Throwable th4) {
                th = th4;
                bufferedReader = bufferedReader2;
            }
        } catch (Throwable th5) {
            th = th5;
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (Throwable th6) {
                    if (th2 == null) {
                        th2 = th6;
                    } else if (th2 != th6) {
                        th2.addSuppressed(th6);
                    }
                }
            }
            if (th2 != null) {
                try {
                    throw th2;
                } catch (IOException e7) {
                    e = e7;
                } catch (NumberFormatException e8) {
                    e2 = e8;
                    Slog.e(TAG, "read uid_cputime has NumberFormatException, line:" + line);
                    Slog.e(TAG, "Failed to read uid_cputime", e2);
                    this.mLastTimeReadUs = nowUs;
                } catch (StringIndexOutOfBoundsException e9) {
                    e3 = e9;
                    Slog.e(TAG, "read uid_cputime has StringIndexOutOfBoundsException, line:" + line);
                    Slog.e(TAG, "Failed to read uid_cputime", e3);
                    this.mLastTimeReadUs = nowUs;
                }
            }
            throw th;
        }
    }

    public void removeUid(int uid) {
        IOException e;
        Throwable th;
        Throwable th2 = null;
        int index = this.mLastUserTimeUs.indexOfKey(uid);
        if (index >= 0) {
            this.mLastUserTimeUs.removeAt(index);
            this.mLastSystemTimeUs.removeAt(index);
            this.mLastPowerMaUs.removeAt(index);
        }
        FileWriter fileWriter = null;
        try {
            FileWriter writer = new FileWriter(sRemoveUidProcFile);
            try {
                writer.write(Integer.toString(uid) + NativeLibraryHelper.CLEAR_ABI_OVERRIDE + Integer.toString(uid));
                writer.flush();
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (Throwable th3) {
                        th2 = th3;
                    }
                }
                if (th2 != null) {
                    try {
                        throw th2;
                    } catch (IOException e2) {
                        e = e2;
                        fileWriter = writer;
                    }
                }
            } catch (Throwable th4) {
                th = th4;
                fileWriter = writer;
                if (fileWriter != null) {
                    try {
                        fileWriter.close();
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
            if (fileWriter != null) {
                fileWriter.close();
            }
            if (th2 == null) {
                throw th;
            }
            throw th2;
        }
    }
}
