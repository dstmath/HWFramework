package java.util.stream;

import java.util.Arrays;
import java.util.IntSummaryStatistics;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.PrimitiveIterator.OfInt;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.IntBinaryOperator;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.IntSupplier;
import java.util.function.IntToDoubleFunction;
import java.util.function.IntToLongFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.ObjIntConsumer;
import java.util.function.Supplier;

public interface IntStream extends BaseStream<Integer, IntStream> {

    public interface Builder extends IntConsumer {
        void accept(int i);

        IntStream build();

        Builder add(int t) {
            accept(t);
            return this;
        }
    }

    boolean allMatch(IntPredicate intPredicate);

    boolean anyMatch(IntPredicate intPredicate);

    DoubleStream asDoubleStream();

    LongStream asLongStream();

    OptionalDouble average();

    Stream<Integer> boxed();

    <R> R collect(Supplier<R> supplier, ObjIntConsumer<R> objIntConsumer, BiConsumer<R, R> biConsumer);

    long count();

    IntStream distinct();

    IntStream filter(IntPredicate intPredicate);

    OptionalInt findAny();

    OptionalInt findFirst();

    IntStream flatMap(IntFunction<? extends IntStream> intFunction);

    void forEach(IntConsumer intConsumer);

    void forEachOrdered(IntConsumer intConsumer);

    OfInt iterator();

    IntStream limit(long j);

    IntStream map(IntUnaryOperator intUnaryOperator);

    DoubleStream mapToDouble(IntToDoubleFunction intToDoubleFunction);

    LongStream mapToLong(IntToLongFunction intToLongFunction);

    <U> Stream<U> mapToObj(IntFunction<? extends U> intFunction);

    OptionalInt max();

    OptionalInt min();

    boolean noneMatch(IntPredicate intPredicate);

    IntStream parallel();

    IntStream peek(IntConsumer intConsumer);

    int reduce(int i, IntBinaryOperator intBinaryOperator);

    OptionalInt reduce(IntBinaryOperator intBinaryOperator);

    IntStream sequential();

    IntStream skip(long j);

    IntStream sorted();

    Spliterator.OfInt spliterator();

    int sum();

    IntSummaryStatistics summaryStatistics();

    int[] toArray();

    static Builder builder() {
        return new IntStreamBuilderImpl();
    }

    static IntStream empty() {
        return StreamSupport.intStream(Spliterators.emptyIntSpliterator(), false);
    }

    static IntStream of(int t) {
        return StreamSupport.intStream(new IntStreamBuilderImpl(t), false);
    }

    static IntStream of(int... values) {
        return Arrays.stream(values);
    }

    static IntStream iterate(final int seed, final IntUnaryOperator f) {
        Objects.requireNonNull(f);
        return StreamSupport.intStream(Spliterators.spliteratorUnknownSize(new OfInt() {
            int t = seed;

            public boolean hasNext() {
                return true;
            }

            public int nextInt() {
                int v = this.t;
                this.t = f.applyAsInt(this.t);
                return v;
            }
        }, 1296), false);
    }

    static IntStream generate(IntSupplier s) {
        Objects.requireNonNull(s);
        return StreamSupport.intStream(new OfInt(Long.MAX_VALUE, s), false);
    }

    static IntStream range(int startInclusive, int endExclusive) {
        if (startInclusive >= endExclusive) {
            return empty();
        }
        return StreamSupport.intStream(new RangeIntSpliterator(startInclusive, endExclusive, false), false);
    }

    static IntStream rangeClosed(int startInclusive, int endInclusive) {
        if (startInclusive > endInclusive) {
            return empty();
        }
        return StreamSupport.intStream(new RangeIntSpliterator(startInclusive, endInclusive, true), false);
    }

    static IntStream concat(IntStream a, IntStream b) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);
        return (IntStream) StreamSupport.intStream(new OfInt(a.spliterator(), b.spliterator()), !a.isParallel() ? b.isParallel() : true).onClose(Streams.composedClose(a, b));
    }
}
