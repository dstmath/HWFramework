package com.android.internal.util.function.pooled;

import java.util.function.Consumer;

public interface PooledConsumer<T> extends PooledLambda, Consumer<T> {
    @Override // com.android.internal.util.function.pooled.PooledPredicate, com.android.internal.util.function.pooled.PooledSupplier, com.android.internal.util.function.pooled.PooledRunnable, com.android.internal.util.function.pooled.PooledSupplier.OfInt, com.android.internal.util.function.pooled.PooledSupplier.OfLong, com.android.internal.util.function.pooled.PooledSupplier.OfDouble
    PooledConsumer<T> recycleOnUse();
}
