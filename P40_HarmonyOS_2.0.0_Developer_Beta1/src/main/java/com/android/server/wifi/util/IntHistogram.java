package com.android.server.wifi.util;

import android.util.SparseIntArray;
import com.android.server.wifi.ScoringParams;
import com.android.server.wifi.hwUtil.WifiCommonUtils;
import com.android.server.wifi.nano.WifiMetricsProto;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Iterator;

public class IntHistogram implements Iterable<Bucket> {
    private final int[] mBucketBoundaries;
    private SparseIntArray mBuckets;

    public interface ProtobufConverter<T> {
        T convert(int i, int i2, int i3);
    }

    public static class Bucket {
        public int count;
        public int end;
        public int start;

        public Bucket(int start2, int end2, int count2) {
            this.start = start2;
            this.end = end2;
            this.count = count2;
        }
    }

    public IntHistogram(int[] bucketBoundaries) {
        if (bucketBoundaries == null || bucketBoundaries.length == 0) {
            throw new IllegalArgumentException("bucketBoundaries must be non-null and non-empty!");
        }
        for (int i = 0; i < bucketBoundaries.length - 1; i++) {
            int cur = bucketBoundaries[i];
            int next = bucketBoundaries[i + 1];
            if (cur >= next) {
                throw new IllegalArgumentException(String.format("bucketBoundaries values must be strictly monotonically increasing, but value %d at index %d is greater or equal to value %d at index %d!", Integer.valueOf(cur), Integer.valueOf(i), Integer.valueOf(next), Integer.valueOf(i + 1)));
            }
        }
        this.mBucketBoundaries = (int[]) bucketBoundaries.clone();
        this.mBuckets = new SparseIntArray();
    }

    public void clear() {
        this.mBuckets.clear();
    }

    public int numNonEmptyBuckets() {
        return this.mBuckets.size();
    }

    public int numTotalBuckets() {
        return this.mBucketBoundaries.length + 1;
    }

    public Bucket getBucketByIndex(int bucketIndex) {
        int bucketKey = this.mBuckets.keyAt(bucketIndex);
        int start = bucketKey == 0 ? WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK : this.mBucketBoundaries[bucketKey - 1];
        int[] iArr = this.mBucketBoundaries;
        return new Bucket(start, bucketKey == iArr.length ? ScoringParams.Values.MAX_EXPID : iArr[bucketKey], this.mBuckets.valueAt(bucketIndex));
    }

    public void increment(int value) {
        add(value, 1);
    }

    public void add(int value, int count) {
        int bucketKey = getBucketKey(value);
        this.mBuckets.put(bucketKey, this.mBuckets.get(bucketKey) + count);
    }

    private int getBucketKey(int value) {
        return Math.abs(Arrays.binarySearch(this.mBucketBoundaries, value) + 1);
    }

    @Override // java.lang.Object
    public String toString() {
        if (this.mBuckets.size() <= 0) {
            return "{}";
        }
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        for (int bucketIndex = 0; bucketIndex < this.mBuckets.size(); bucketIndex++) {
            if (bucketIndex > 0) {
                sb.append(", ");
            }
            int bucketKey = this.mBuckets.keyAt(bucketIndex);
            sb.append('[');
            if (bucketKey == 0) {
                sb.append("Integer.MIN_VALUE");
            } else {
                sb.append(this.mBucketBoundaries[bucketKey - 1]);
            }
            sb.append(',');
            int[] iArr = this.mBucketBoundaries;
            if (bucketKey == iArr.length) {
                sb.append("Integer.MAX_VALUE]");
            } else {
                sb.append(iArr[bucketKey]);
                sb.append(')');
            }
            sb.append('=');
            sb.append(this.mBuckets.valueAt(bucketIndex));
        }
        sb.append('}');
        return sb.toString();
    }

    @Override // java.lang.Iterable
    public Iterator<Bucket> iterator() {
        return new Iterator<Bucket>() {
            /* class com.android.server.wifi.util.IntHistogram.AnonymousClass1 */
            private int mBucketIndex = 0;

            @Override // java.util.Iterator
            public boolean hasNext() {
                return this.mBucketIndex < IntHistogram.this.mBuckets.size();
            }

            @Override // java.util.Iterator
            public Bucket next() {
                Bucket bucket = IntHistogram.this.getBucketByIndex(this.mBucketIndex);
                this.mBucketIndex++;
                return bucket;
            }
        };
    }

    public <T> T[] toProto(Class<T> protoClass, ProtobufConverter<T> converter) {
        T[] output = (T[]) ((Object[]) Array.newInstance((Class<?>) protoClass, this.mBuckets.size()));
        int i = 0;
        Iterator<Bucket> it = iterator();
        while (it.hasNext()) {
            Bucket bucket = it.next();
            output[i] = converter.convert(bucket.start, bucket.end, bucket.count);
            i++;
        }
        return output;
    }

    public WifiMetricsProto.HistogramBucketInt32[] toProto() {
        return (WifiMetricsProto.HistogramBucketInt32[]) toProto(WifiMetricsProto.HistogramBucketInt32.class, $$Lambda$IntHistogram$KipvBry86qMmCc412Y4yMch_ek.INSTANCE);
    }

    static /* synthetic */ WifiMetricsProto.HistogramBucketInt32 lambda$toProto$0(int start, int end, int count) {
        WifiMetricsProto.HistogramBucketInt32 hb = new WifiMetricsProto.HistogramBucketInt32();
        hb.start = start;
        hb.end = end;
        hb.count = count;
        return hb;
    }
}
