package com.android.internal.util.function.pooled;

import com.android.internal.util.FunctionalUtils;

public interface PooledRunnable extends PooledLambda, Runnable, FunctionalUtils.ThrowingRunnable {
    @Override // com.android.internal.util.function.pooled.PooledLambda, com.android.internal.util.function.pooled.PooledConsumer, com.android.internal.util.function.pooled.PooledPredicate, com.android.internal.util.function.pooled.PooledSupplier, com.android.internal.util.function.pooled.PooledRunnable, com.android.internal.util.function.pooled.PooledSupplier.OfInt, com.android.internal.util.function.pooled.PooledSupplier.OfLong, com.android.internal.util.function.pooled.PooledSupplier.OfDouble
    PooledRunnable recycleOnUse();
}
