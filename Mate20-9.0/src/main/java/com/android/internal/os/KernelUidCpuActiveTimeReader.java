package com.android.internal.os;

import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.os.KernelUidCpuActiveTimeReader;
import com.android.internal.os.KernelUidCpuTimeReaderBase;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.function.Consumer;

public class KernelUidCpuActiveTimeReader extends KernelUidCpuTimeReaderBase<Callback> {
    private static final String TAG = KernelUidCpuActiveTimeReader.class.getSimpleName();
    private int mCores;
    private SparseArray<Double> mLastUidCpuActiveTimeMs;
    private final KernelCpuProcReader mProcReader;

    public interface Callback extends KernelUidCpuTimeReaderBase.Callback {
        void onUidCpuActiveTime(int i, long j);
    }

    public KernelUidCpuActiveTimeReader() {
        this.mLastUidCpuActiveTimeMs = new SparseArray<>();
        this.mProcReader = KernelCpuProcReader.getActiveTimeReaderInstance();
    }

    @VisibleForTesting
    public KernelUidCpuActiveTimeReader(KernelCpuProcReader procReader) {
        this.mLastUidCpuActiveTimeMs = new SparseArray<>();
        this.mProcReader = procReader;
    }

    /* access modifiers changed from: protected */
    public void readDeltaImpl(Callback callback) {
        readImpl(new Consumer(callback) {
            private final /* synthetic */ KernelUidCpuActiveTimeReader.Callback f$1;

            {
                this.f$1 = r2;
            }

            public final void accept(Object obj) {
                KernelUidCpuActiveTimeReader.lambda$readDeltaImpl$0(KernelUidCpuActiveTimeReader.this, this.f$1, (IntBuffer) obj);
            }
        });
    }

    public static /* synthetic */ void lambda$readDeltaImpl$0(KernelUidCpuActiveTimeReader kernelUidCpuActiveTimeReader, Callback callback, IntBuffer buf) {
        int uid = buf.get();
        double activeTime = kernelUidCpuActiveTimeReader.sumActiveTime(buf);
        if (activeTime > 0.0d) {
            double delta = activeTime - kernelUidCpuActiveTimeReader.mLastUidCpuActiveTimeMs.get(uid, Double.valueOf(0.0d)).doubleValue();
            if (delta > 0.0d) {
                kernelUidCpuActiveTimeReader.mLastUidCpuActiveTimeMs.put(uid, Double.valueOf(activeTime));
                if (callback != null) {
                    callback.onUidCpuActiveTime(uid, (long) delta);
                }
            } else if (delta < 0.0d) {
                String str = TAG;
                Slog.e(str, "Negative delta from active time proc: " + delta);
            }
        }
    }

    public void readAbsolute(Callback callback) {
        readImpl(new Consumer(callback) {
            private final /* synthetic */ KernelUidCpuActiveTimeReader.Callback f$1;

            {
                this.f$1 = r2;
            }

            public final void accept(Object obj) {
                KernelUidCpuActiveTimeReader.lambda$readAbsolute$1(KernelUidCpuActiveTimeReader.this, this.f$1, (IntBuffer) obj);
            }
        });
    }

    public static /* synthetic */ void lambda$readAbsolute$1(KernelUidCpuActiveTimeReader kernelUidCpuActiveTimeReader, Callback callback, IntBuffer buf) {
        int uid = buf.get();
        double activeTime = kernelUidCpuActiveTimeReader.sumActiveTime(buf);
        if (activeTime > 0.0d) {
            callback.onUidCpuActiveTime(uid, (long) activeTime);
        }
    }

    private double sumActiveTime(IntBuffer buffer) {
        double sum = 0.0d;
        boolean corrupted = false;
        for (int j = 1; j <= this.mCores; j++) {
            int time = buffer.get();
            if (time < 0) {
                String str = TAG;
                Slog.e(str, "Negative time from active time proc: " + time);
                corrupted = true;
            } else {
                sum += (((double) time) * 10.0d) / ((double) j);
            }
        }
        if (corrupted) {
            return -1.0d;
        }
        return sum;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00a7, code lost:
        return;
     */
    private void readImpl(Consumer<IntBuffer> processUid) {
        synchronized (this.mProcReader) {
            ByteBuffer bytes = this.mProcReader.readBytes();
            if (bytes != null) {
                if (bytes.remaining() > 4) {
                    if ((bytes.remaining() & 3) != 0) {
                        String str = TAG;
                        Slog.wtf(str, "Cannot parse active time proc bytes to int: " + bytes.remaining());
                        return;
                    }
                    IntBuffer buf = bytes.asIntBuffer();
                    int cores = buf.get();
                    if (this.mCores == 0 || cores == this.mCores) {
                        this.mCores = cores;
                        if (cores > 0) {
                            if (buf.remaining() % (cores + 1) == 0) {
                                int numUids = buf.remaining() / (cores + 1);
                                for (int i = 0; i < numUids; i++) {
                                    processUid.accept(buf);
                                }
                                return;
                            }
                        }
                        String str2 = TAG;
                        Slog.wtf(str2, "Cpu active time format error: " + buf.remaining() + " / " + (cores + 1));
                        return;
                    }
                    String str3 = TAG;
                    Slog.wtf(str3, "Cpu active time wrong # cores: " + cores);
                }
            }
        }
    }

    public void removeUid(int uid) {
        this.mLastUidCpuActiveTimeMs.delete(uid);
    }

    public void removeUidsInRange(int startUid, int endUid) {
        this.mLastUidCpuActiveTimeMs.put(startUid, null);
        this.mLastUidCpuActiveTimeMs.put(endUid, null);
        int firstIndex = this.mLastUidCpuActiveTimeMs.indexOfKey(startUid);
        this.mLastUidCpuActiveTimeMs.removeAtRange(firstIndex, (this.mLastUidCpuActiveTimeMs.indexOfKey(endUid) - firstIndex) + 1);
    }
}
