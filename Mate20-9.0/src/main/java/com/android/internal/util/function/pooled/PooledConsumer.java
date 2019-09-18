package com.android.internal.util.function.pooled;

import java.util.function.Consumer;

public interface PooledConsumer<T> extends PooledLambda, Consumer<T> {
    PooledConsumer<T> recycleOnUse();
}
