package com.android.internal.util;

import android.util.Log;
import java.util.Arrays;

public class ExponentiallyBucketedHistogram {
    private final int[] mData;

    public ExponentiallyBucketedHistogram(int numBuckets) {
        this.mData = new int[Preconditions.checkArgumentInRange(numBuckets, 1, 31, "numBuckets")];
    }

    public void add(int value) {
        int[] iArr;
        if (value <= 0) {
            iArr = this.mData;
            iArr[0] = iArr[0] + 1;
            return;
        }
        iArr = this.mData;
        int min = Math.min(this.mData.length - 1, 32 - Integer.numberOfLeadingZeros(value));
        iArr[min] = iArr[min] + 1;
    }

    public void reset() {
        Arrays.fill(this.mData, 0);
    }

    public void log(String tag, CharSequence prefix) {
        StringBuilder builder = new StringBuilder(prefix);
        builder.append('[');
        for (int i = 0; i < this.mData.length; i++) {
            if (i != 0) {
                builder.append(", ");
            }
            if (i < this.mData.length - 1) {
                builder.append("<");
                builder.append(1 << i);
            } else {
                builder.append(">=");
                builder.append(1 << (i - 1));
            }
            builder.append(": ");
            builder.append(this.mData[i]);
        }
        builder.append("]");
        Log.d(tag, builder.toString());
    }
}
