package com.android.internal.util.function.pooled;

import java.util.function.Predicate;

public interface PooledPredicate<T> extends PooledLambda, Predicate<T> {
    PooledConsumer<T> asConsumer();

    @Override // com.android.internal.util.function.pooled.PooledLambda, com.android.internal.util.function.pooled.PooledConsumer, com.android.internal.util.function.pooled.PooledPredicate, com.android.internal.util.function.pooled.PooledSupplier, com.android.internal.util.function.pooled.PooledRunnable, com.android.internal.util.function.pooled.PooledSupplier.OfInt, com.android.internal.util.function.pooled.PooledSupplier.OfLong, com.android.internal.util.function.pooled.PooledSupplier.OfDouble
    PooledPredicate<T> recycleOnUse();
}
