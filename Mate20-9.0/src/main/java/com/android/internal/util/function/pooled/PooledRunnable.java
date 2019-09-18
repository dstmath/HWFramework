package com.android.internal.util.function.pooled;

import com.android.internal.util.FunctionalUtils;

public interface PooledRunnable extends PooledLambda, Runnable, FunctionalUtils.ThrowingRunnable {
    PooledRunnable recycleOnUse();
}
