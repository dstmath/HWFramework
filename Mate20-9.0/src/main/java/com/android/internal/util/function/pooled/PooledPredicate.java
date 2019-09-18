package com.android.internal.util.function.pooled;

import java.util.function.Predicate;

public interface PooledPredicate<T> extends PooledLambda, Predicate<T> {
    PooledConsumer<T> asConsumer();

    PooledPredicate<T> recycleOnUse();
}
