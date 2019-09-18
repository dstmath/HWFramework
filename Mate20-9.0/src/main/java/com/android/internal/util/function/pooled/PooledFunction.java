package com.android.internal.util.function.pooled;

import java.util.function.Function;

public interface PooledFunction<A, R> extends PooledLambda, Function<A, R> {
    PooledConsumer<A> asConsumer();

    PooledFunction<A, R> recycleOnUse();
}
