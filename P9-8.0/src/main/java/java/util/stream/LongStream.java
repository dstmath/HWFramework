package java.util.stream;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.LongSummaryStatistics;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalLong;
import java.util.PrimitiveIterator.OfLong;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.LongBinaryOperator;
import java.util.function.LongConsumer;
import java.util.function.LongFunction;
import java.util.function.LongPredicate;
import java.util.function.LongSupplier;
import java.util.function.LongToDoubleFunction;
import java.util.function.LongToIntFunction;
import java.util.function.LongUnaryOperator;
import java.util.function.ObjLongConsumer;
import java.util.function.Supplier;

public interface LongStream extends BaseStream<Long, LongStream> {

    public interface Builder extends LongConsumer {
        void accept(long j);

        LongStream build();

        Builder add(long t) {
            accept(t);
            return this;
        }
    }

    boolean allMatch(LongPredicate longPredicate);

    boolean anyMatch(LongPredicate longPredicate);

    DoubleStream asDoubleStream();

    OptionalDouble average();

    Stream<Long> boxed();

    <R> R collect(Supplier<R> supplier, ObjLongConsumer<R> objLongConsumer, BiConsumer<R, R> biConsumer);

    long count();

    LongStream distinct();

    LongStream filter(LongPredicate longPredicate);

    OptionalLong findAny();

    OptionalLong findFirst();

    LongStream flatMap(LongFunction<? extends LongStream> longFunction);

    void forEach(LongConsumer longConsumer);

    void forEachOrdered(LongConsumer longConsumer);

    OfLong iterator();

    LongStream limit(long j);

    LongStream map(LongUnaryOperator longUnaryOperator);

    DoubleStream mapToDouble(LongToDoubleFunction longToDoubleFunction);

    IntStream mapToInt(LongToIntFunction longToIntFunction);

    <U> Stream<U> mapToObj(LongFunction<? extends U> longFunction);

    OptionalLong max();

    OptionalLong min();

    boolean noneMatch(LongPredicate longPredicate);

    LongStream parallel();

    LongStream peek(LongConsumer longConsumer);

    long reduce(long j, LongBinaryOperator longBinaryOperator);

    OptionalLong reduce(LongBinaryOperator longBinaryOperator);

    LongStream sequential();

    LongStream skip(long j);

    LongStream sorted();

    Spliterator.OfLong spliterator();

    long sum();

    LongSummaryStatistics summaryStatistics();

    long[] toArray();

    static Builder builder() {
        return new LongStreamBuilderImpl();
    }

    static LongStream empty() {
        return StreamSupport.longStream(Spliterators.emptyLongSpliterator(), false);
    }

    static LongStream of(long t) {
        return StreamSupport.longStream(new LongStreamBuilderImpl(t), false);
    }

    static LongStream of(long... values) {
        return Arrays.stream(values);
    }

    static LongStream iterate(final long seed, final LongUnaryOperator f) {
        Objects.requireNonNull(f);
        return StreamSupport.longStream(Spliterators.spliteratorUnknownSize(new OfLong() {
            long t = seed;

            public boolean hasNext() {
                return true;
            }

            public long nextLong() {
                long v = this.t;
                this.t = f.applyAsLong(this.t);
                return v;
            }
        }, 1296), false);
    }

    static LongStream generate(LongSupplier s) {
        Objects.requireNonNull(s);
        return StreamSupport.longStream(new OfLong(Long.MAX_VALUE, s), false);
    }

    static LongStream range(long startInclusive, long endExclusive) {
        if (startInclusive >= endExclusive) {
            return empty();
        }
        if (endExclusive - startInclusive >= 0) {
            return StreamSupport.longStream(new RangeLongSpliterator(startInclusive, endExclusive, false), false);
        }
        long m = (BigInteger.valueOf(endExclusive).subtract(BigInteger.valueOf(startInclusive)).divide(BigInteger.valueOf(2)).longValue() + startInclusive) + 1;
        return concat(range(startInclusive, m), range(m, endExclusive));
    }

    static LongStream rangeClosed(long startInclusive, long endInclusive) {
        if (startInclusive > endInclusive) {
            return empty();
        }
        if ((endInclusive - startInclusive) + 1 > 0) {
            return StreamSupport.longStream(new RangeLongSpliterator(startInclusive, endInclusive, true), false);
        }
        long m = (BigInteger.valueOf(endInclusive).subtract(BigInteger.valueOf(startInclusive)).divide(BigInteger.valueOf(2)).longValue() + startInclusive) + 1;
        return concat(range(startInclusive, m), rangeClosed(m, endInclusive));
    }

    static LongStream concat(LongStream a, LongStream b) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);
        return (LongStream) StreamSupport.longStream(new OfLong(a.spliterator(), b.spliterator()), !a.isParallel() ? b.isParallel() : true).onClose(Streams.composedClose(a, b));
    }
}
