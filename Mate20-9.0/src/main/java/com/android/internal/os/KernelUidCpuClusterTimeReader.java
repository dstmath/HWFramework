package com.android.internal.os;

import android.util.Slog;
import android.util.SparseArray;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.os.KernelUidCpuClusterTimeReader;
import com.android.internal.os.KernelUidCpuTimeReaderBase;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.function.Consumer;

public class KernelUidCpuClusterTimeReader extends KernelUidCpuTimeReaderBase<Callback> {
    private static final String TAG = KernelUidCpuClusterTimeReader.class.getSimpleName();
    private double[] mCurTime;
    private long[] mCurTimeRounded;
    private long[] mDeltaTime;
    private SparseArray<double[]> mLastUidPolicyTimeMs;
    private int mNumClusters;
    private int mNumCores;
    private int[] mNumCoresOnCluster;
    private final KernelCpuProcReader mProcReader;

    public interface Callback extends KernelUidCpuTimeReaderBase.Callback {
        void onUidCpuPolicyTime(int i, long[] jArr);
    }

    public KernelUidCpuClusterTimeReader() {
        this.mLastUidPolicyTimeMs = new SparseArray<>();
        this.mNumClusters = -1;
        this.mProcReader = KernelCpuProcReader.getClusterTimeReaderInstance();
    }

    @VisibleForTesting
    public KernelUidCpuClusterTimeReader(KernelCpuProcReader procReader) {
        this.mLastUidPolicyTimeMs = new SparseArray<>();
        this.mNumClusters = -1;
        this.mProcReader = procReader;
    }

    /* access modifiers changed from: protected */
    public void readDeltaImpl(Callback cb) {
        readImpl(new Consumer(cb) {
            private final /* synthetic */ KernelUidCpuClusterTimeReader.Callback f$1;

            {
                this.f$1 = r2;
            }

            public final void accept(Object obj) {
                KernelUidCpuClusterTimeReader.lambda$readDeltaImpl$0(KernelUidCpuClusterTimeReader.this, this.f$1, (IntBuffer) obj);
            }
        });
    }

    public static /* synthetic */ void lambda$readDeltaImpl$0(KernelUidCpuClusterTimeReader kernelUidCpuClusterTimeReader, Callback cb, IntBuffer buf) {
        int uid = buf.get();
        double[] lastTimes = kernelUidCpuClusterTimeReader.mLastUidPolicyTimeMs.get(uid);
        if (lastTimes == null) {
            lastTimes = new double[kernelUidCpuClusterTimeReader.mNumClusters];
            kernelUidCpuClusterTimeReader.mLastUidPolicyTimeMs.put(uid, lastTimes);
        }
        if (kernelUidCpuClusterTimeReader.sumClusterTime(buf, kernelUidCpuClusterTimeReader.mCurTime)) {
            boolean notify = false;
            boolean valid = true;
            for (int i = 0; i < kernelUidCpuClusterTimeReader.mNumClusters; i++) {
                kernelUidCpuClusterTimeReader.mDeltaTime[i] = (long) (kernelUidCpuClusterTimeReader.mCurTime[i] - lastTimes[i]);
                if (kernelUidCpuClusterTimeReader.mDeltaTime[i] < 0) {
                    Slog.e(TAG, "Negative delta from cluster time proc: " + kernelUidCpuClusterTimeReader.mDeltaTime[i]);
                    valid = false;
                }
                notify |= kernelUidCpuClusterTimeReader.mDeltaTime[i] > 0;
            }
            if (notify && valid) {
                System.arraycopy(kernelUidCpuClusterTimeReader.mCurTime, 0, lastTimes, 0, kernelUidCpuClusterTimeReader.mNumClusters);
                if (cb != null) {
                    cb.onUidCpuPolicyTime(uid, kernelUidCpuClusterTimeReader.mDeltaTime);
                }
            }
        }
    }

    public void readAbsolute(Callback callback) {
        readImpl(new Consumer(callback) {
            private final /* synthetic */ KernelUidCpuClusterTimeReader.Callback f$1;

            {
                this.f$1 = r2;
            }

            public final void accept(Object obj) {
                KernelUidCpuClusterTimeReader.lambda$readAbsolute$1(KernelUidCpuClusterTimeReader.this, this.f$1, (IntBuffer) obj);
            }
        });
    }

    public static /* synthetic */ void lambda$readAbsolute$1(KernelUidCpuClusterTimeReader kernelUidCpuClusterTimeReader, Callback callback, IntBuffer buf) {
        int uid = buf.get();
        if (kernelUidCpuClusterTimeReader.sumClusterTime(buf, kernelUidCpuClusterTimeReader.mCurTime)) {
            for (int i = 0; i < kernelUidCpuClusterTimeReader.mNumClusters; i++) {
                kernelUidCpuClusterTimeReader.mCurTimeRounded[i] = (long) kernelUidCpuClusterTimeReader.mCurTime[i];
            }
            callback.onUidCpuPolicyTime(uid, kernelUidCpuClusterTimeReader.mCurTimeRounded);
        }
    }

    private boolean sumClusterTime(IntBuffer buffer, double[] clusterTime) {
        boolean valid = true;
        for (int i = 0; i < this.mNumClusters; i++) {
            clusterTime[i] = 0.0d;
            for (int j = 1; j <= this.mNumCoresOnCluster[i]; j++) {
                int time = buffer.get();
                if (time < 0) {
                    String str = TAG;
                    Slog.e(str, "Negative time from cluster time proc: " + time);
                    valid = false;
                }
                clusterTime[i] = clusterTime[i] + ((((double) time) * 10.0d) / ((double) j));
            }
        }
        return valid;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00df, code lost:
        return;
     */
    private void readImpl(Consumer<IntBuffer> processUid) {
        synchronized (this.mProcReader) {
            ByteBuffer bytes = this.mProcReader.readBytes();
            if (bytes != null) {
                if (bytes.remaining() > 4) {
                    if ((bytes.remaining() & 3) != 0) {
                        String str = TAG;
                        Slog.wtf(str, "Cannot parse cluster time proc bytes to int: " + bytes.remaining());
                        return;
                    }
                    IntBuffer buf = bytes.asIntBuffer();
                    int numClusters = buf.get();
                    if (numClusters <= 0) {
                        String str2 = TAG;
                        Slog.wtf(str2, "Cluster time format error: " + numClusters);
                        return;
                    }
                    if (this.mNumClusters == -1) {
                        this.mNumClusters = numClusters;
                    }
                    if (buf.remaining() < numClusters) {
                        String str3 = TAG;
                        Slog.wtf(str3, "Too few data left in the buffer: " + buf.remaining());
                        return;
                    }
                    if (this.mNumCores > 0) {
                        buf.position(buf.position() + numClusters);
                    } else if (!readCoreInfo(buf, numClusters)) {
                        return;
                    }
                    if (buf.remaining() % (this.mNumCores + 1) != 0) {
                        String str4 = TAG;
                        Slog.wtf(str4, "Cluster time format error: " + buf.remaining() + " / " + (this.mNumCores + 1));
                        return;
                    }
                    int numUids = buf.remaining() / (this.mNumCores + 1);
                    for (int i = 0; i < numUids; i++) {
                        processUid.accept(buf);
                    }
                }
            }
        }
    }

    private boolean readCoreInfo(IntBuffer buf, int numClusters) {
        int[] numCoresOnCluster = new int[numClusters];
        int numCores = 0;
        for (int i = 0; i < numClusters; i++) {
            numCoresOnCluster[i] = buf.get();
            numCores += numCoresOnCluster[i];
        }
        if (numCores <= 0) {
            Slog.e(TAG, "Invalid # cores from cluster time proc file: " + numCores);
            return false;
        }
        this.mNumCores = numCores;
        this.mNumCoresOnCluster = numCoresOnCluster;
        this.mCurTime = new double[numClusters];
        this.mDeltaTime = new long[numClusters];
        this.mCurTimeRounded = new long[numClusters];
        return true;
    }

    public void removeUid(int uid) {
        this.mLastUidPolicyTimeMs.delete(uid);
    }

    public void removeUidsInRange(int startUid, int endUid) {
        this.mLastUidPolicyTimeMs.put(startUid, null);
        this.mLastUidPolicyTimeMs.put(endUid, null);
        int firstIndex = this.mLastUidPolicyTimeMs.indexOfKey(startUid);
        this.mLastUidPolicyTimeMs.removeAtRange(firstIndex, (this.mLastUidPolicyTimeMs.indexOfKey(endUid) - firstIndex) + 1);
    }
}
