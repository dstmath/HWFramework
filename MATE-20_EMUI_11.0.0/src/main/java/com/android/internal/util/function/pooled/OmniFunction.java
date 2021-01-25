package com.android.internal.util.function.pooled;

import com.android.internal.util.FunctionalUtils;
import com.android.internal.util.function.HeptConsumer;
import com.android.internal.util.function.HeptFunction;
import com.android.internal.util.function.HexConsumer;
import com.android.internal.util.function.HexFunction;
import com.android.internal.util.function.NonaConsumer;
import com.android.internal.util.function.NonaFunction;
import com.android.internal.util.function.OctConsumer;
import com.android.internal.util.function.OctFunction;
import com.android.internal.util.function.QuadConsumer;
import com.android.internal.util.function.QuadFunction;
import com.android.internal.util.function.QuintConsumer;
import com.android.internal.util.function.QuintFunction;
import com.android.internal.util.function.TriConsumer;
import com.android.internal.util.function.TriFunction;
import com.android.internal.util.function.pooled.PooledSupplier;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;

abstract class OmniFunction<A, B, C, D, E, F, G, H, I, R> implements PooledFunction<A, R>, BiFunction<A, B, R>, TriFunction<A, B, C, R>, QuadFunction<A, B, C, D, R>, QuintFunction<A, B, C, D, E, R>, HexFunction<A, B, C, D, E, F, R>, HeptFunction<A, B, C, D, E, F, G, R>, OctFunction<A, B, C, D, E, F, G, H, R>, NonaFunction<A, B, C, D, E, F, G, H, I, R>, PooledConsumer<A>, BiConsumer<A, B>, TriConsumer<A, B, C>, QuadConsumer<A, B, C, D>, QuintConsumer<A, B, C, D, E>, HexConsumer<A, B, C, D, E, F>, HeptConsumer<A, B, C, D, E, F, G>, OctConsumer<A, B, C, D, E, F, G, H>, NonaConsumer<A, B, C, D, E, F, G, H, I>, PooledPredicate<A>, BiPredicate<A, B>, PooledSupplier<R>, PooledRunnable, FunctionalUtils.ThrowingRunnable, FunctionalUtils.ThrowingSupplier<R>, PooledSupplier.OfInt, PooledSupplier.OfLong, PooledSupplier.OfDouble {
    @Override // java.util.function.Function, java.util.function.BiFunction
    public abstract <V> OmniFunction<A, B, C, D, E, F, G, H, I, V> andThen(Function<? super R, ? extends V> function);

    /* access modifiers changed from: package-private */
    public abstract R invoke(A a, B b, C c, D d, E e, F f, G g, H h, I i);

    @Override // java.util.function.Predicate, java.util.function.BiPredicate
    public abstract OmniFunction<A, B, C, D, E, F, G, H, I, R> negate();

    @Override // com.android.internal.util.function.pooled.PooledFunction, com.android.internal.util.function.pooled.PooledLambda, com.android.internal.util.function.pooled.PooledConsumer, com.android.internal.util.function.pooled.PooledPredicate, com.android.internal.util.function.pooled.PooledSupplier, com.android.internal.util.function.pooled.PooledRunnable, com.android.internal.util.function.pooled.PooledSupplier.OfInt, com.android.internal.util.function.pooled.PooledSupplier.OfLong, com.android.internal.util.function.pooled.PooledSupplier.OfDouble
    public abstract OmniFunction<A, B, C, D, E, F, G, H, I, R> recycleOnUse();

    OmniFunction() {
    }

    @Override // java.util.function.BiFunction
    public R apply(A o, B o2) {
        return invoke(o, o2, null, null, null, null, null, null, null);
    }

    @Override // java.util.function.Function
    public R apply(A o) {
        return invoke(o, null, null, null, null, null, null, null, null);
    }

    @Override // java.util.function.BiConsumer
    public void accept(A o, B o2) {
        invoke(o, o2, null, null, null, null, null, null, null);
    }

    @Override // java.util.function.Consumer
    public void accept(A o) {
        invoke(o, null, null, null, null, null, null, null, null);
    }

    @Override // java.lang.Runnable, com.android.internal.util.FunctionalUtils.ThrowingRunnable
    public void run() {
        invoke(null, null, null, null, null, null, null, null, null);
    }

    @Override // java.util.function.Supplier
    public R get() {
        return invoke(null, null, null, null, null, null, null, null, null);
    }

    @Override // java.util.function.BiPredicate
    public boolean test(A o, B o2) {
        return invoke(o, o2, null, null, null, null, null, null, null).booleanValue();
    }

    @Override // java.util.function.Predicate
    public boolean test(A o) {
        return invoke(o, null, null, null, null, null, null, null, null).booleanValue();
    }

    @Override // com.android.internal.util.function.pooled.PooledSupplier
    public PooledRunnable asRunnable() {
        return this;
    }

    @Override // com.android.internal.util.function.pooled.PooledFunction, com.android.internal.util.function.pooled.PooledPredicate
    public PooledConsumer<A> asConsumer() {
        return this;
    }

    @Override // com.android.internal.util.function.TriFunction
    public R apply(A a, B b, C c) {
        return invoke(a, b, c, null, null, null, null, null, null);
    }

    @Override // com.android.internal.util.function.TriConsumer
    public void accept(A a, B b, C c) {
        invoke(a, b, c, null, null, null, null, null, null);
    }

    @Override // com.android.internal.util.function.QuadFunction
    public R apply(A a, B b, C c, D d) {
        return invoke(a, b, c, d, null, null, null, null, null);
    }

    @Override // com.android.internal.util.function.QuintFunction
    public R apply(A a, B b, C c, D d, E e) {
        return invoke(a, b, c, d, e, null, null, null, null);
    }

    @Override // com.android.internal.util.function.HexFunction
    public R apply(A a, B b, C c, D d, E e, F f) {
        return invoke(a, b, c, d, e, f, null, null, null);
    }

    @Override // com.android.internal.util.function.HeptFunction
    public R apply(A a, B b, C c, D d, E e, F f, G g) {
        return invoke(a, b, c, d, e, f, g, null, null);
    }

    @Override // com.android.internal.util.function.OctFunction
    public R apply(A a, B b, C c, D d, E e, F f, G g, H h) {
        return invoke(a, b, c, d, e, f, g, h, null);
    }

    @Override // com.android.internal.util.function.NonaFunction
    public R apply(A a, B b, C c, D d, E e, F f, G g, H h, I i) {
        return invoke(a, b, c, d, e, f, g, h, i);
    }

    @Override // com.android.internal.util.function.QuadConsumer
    public void accept(A a, B b, C c, D d) {
        invoke(a, b, c, d, null, null, null, null, null);
    }

    @Override // com.android.internal.util.function.QuintConsumer
    public void accept(A a, B b, C c, D d, E e) {
        invoke(a, b, c, d, e, null, null, null, null);
    }

    @Override // com.android.internal.util.function.HexConsumer
    public void accept(A a, B b, C c, D d, E e, F f) {
        invoke(a, b, c, d, e, f, null, null, null);
    }

    @Override // com.android.internal.util.function.HeptConsumer
    public void accept(A a, B b, C c, D d, E e, F f, G g) {
        invoke(a, b, c, d, e, f, g, null, null);
    }

    @Override // com.android.internal.util.function.OctConsumer
    public void accept(A a, B b, C c, D d, E e, F f, G g, H h) {
        invoke(a, b, c, d, e, f, g, h, null);
    }

    @Override // com.android.internal.util.function.NonaConsumer
    public void accept(A a, B b, C c, D d, E e, F f, G g, H h, I i) {
        invoke(a, b, c, d, e, f, g, h, i);
    }

    @Override // com.android.internal.util.FunctionalUtils.ThrowingRunnable
    public void runOrThrow() throws Exception {
        run();
    }

    @Override // com.android.internal.util.FunctionalUtils.ThrowingSupplier
    public R getOrThrow() throws Exception {
        return get();
    }
}
