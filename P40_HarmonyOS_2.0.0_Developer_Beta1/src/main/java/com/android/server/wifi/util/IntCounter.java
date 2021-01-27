package com.android.server.wifi.util;

import android.util.SparseIntArray;
import com.android.server.wifi.ScoringParams;
import com.android.server.wifi.hwUtil.WifiCommonUtils;
import com.android.server.wifi.nano.WifiMetricsProto;
import java.lang.reflect.Array;
import java.util.Iterator;

public class IntCounter extends SparseIntArray implements Iterable<KeyCount> {
    public final int keyLowerBound;
    public final int keyUpperBound;

    public interface ProtobufConverter<T> {
        T convert(int i, int i2);
    }

    public static class KeyCount {
        public int count;
        public int key;

        public KeyCount(int key2, int count2) {
            this.key = key2;
            this.count = count2;
        }
    }

    public IntCounter() {
        this(WifiCommonUtils.WIFI_MODE_BIT_ACTION_FRWK, ScoringParams.Values.MAX_EXPID);
    }

    public IntCounter(int keyLowerBound2, int keyUpperBound2) {
        this.keyLowerBound = keyLowerBound2;
        this.keyUpperBound = keyUpperBound2;
    }

    public void increment(int key) {
        add(key, 1);
    }

    public void add(int key, int count) {
        int key2 = Math.max(this.keyLowerBound, Math.min(key, this.keyUpperBound));
        put(key2, get(key2) + count);
    }

    @Override // java.lang.Iterable
    public Iterator<KeyCount> iterator() {
        return new Iterator<KeyCount>() {
            /* class com.android.server.wifi.util.IntCounter.AnonymousClass1 */
            private int mIndex = 0;

            @Override // java.util.Iterator
            public boolean hasNext() {
                return this.mIndex < IntCounter.this.size();
            }

            @Override // java.util.Iterator
            public KeyCount next() {
                KeyCount kc = new KeyCount(IntCounter.this.keyAt(this.mIndex), IntCounter.this.valueAt(this.mIndex));
                this.mIndex++;
                return kc;
            }
        };
    }

    public <T> T[] toProto(Class<T> protoClass, ProtobufConverter<T> converter) {
        T[] output = (T[]) ((Object[]) Array.newInstance((Class<?>) protoClass, size()));
        int i = 0;
        Iterator<KeyCount> it = iterator();
        while (it.hasNext()) {
            KeyCount kc = it.next();
            output[i] = converter.convert(kc.key, kc.count);
            i++;
        }
        return output;
    }

    public WifiMetricsProto.Int32Count[] toProto() {
        return (WifiMetricsProto.Int32Count[]) toProto(WifiMetricsProto.Int32Count.class, $$Lambda$IntCounter$Vt2HJ0mJPxz65XXZd8VF8OJUwS8.INSTANCE);
    }

    static /* synthetic */ WifiMetricsProto.Int32Count lambda$toProto$0(int key, int count) {
        WifiMetricsProto.Int32Count entry = new WifiMetricsProto.Int32Count();
        entry.key = key;
        entry.count = count;
        return entry;
    }
}
