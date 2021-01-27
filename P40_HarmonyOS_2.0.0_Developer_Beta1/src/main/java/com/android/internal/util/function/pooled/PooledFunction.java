package com.android.internal.util.function.pooled;

import java.util.function.Function;

public interface PooledFunction<A, R> extends PooledLambda, Function<A, R> {
    @Override // com.android.internal.util.function.pooled.PooledPredicate
    PooledConsumer<A> asConsumer();

    @Override // com.android.internal.util.function.pooled.PooledLambda, com.android.internal.util.function.pooled.PooledConsumer, com.android.internal.util.function.pooled.PooledPredicate, com.android.internal.util.function.pooled.PooledSupplier, com.android.internal.util.function.pooled.PooledRunnable, com.android.internal.util.function.pooled.PooledSupplier.OfInt, com.android.internal.util.function.pooled.PooledSupplier.OfLong, com.android.internal.util.function.pooled.PooledSupplier.OfDouble
    PooledFunction<A, R> recycleOnUse();
}
