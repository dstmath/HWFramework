package java.util.stream;

import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.PrimitiveIterator.OfDouble;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleConsumer;
import java.util.function.DoubleFunction;
import java.util.function.DoublePredicate;
import java.util.function.DoubleSupplier;
import java.util.function.DoubleToIntFunction;
import java.util.function.DoubleToLongFunction;
import java.util.function.DoubleUnaryOperator;
import java.util.function.ObjDoubleConsumer;
import java.util.function.Supplier;

public interface DoubleStream extends BaseStream<Double, DoubleStream> {

    public interface Builder extends DoubleConsumer {
        void accept(double d);

        DoubleStream build();

        Builder add(double t) {
            accept(t);
            return this;
        }
    }

    boolean allMatch(DoublePredicate doublePredicate);

    boolean anyMatch(DoublePredicate doublePredicate);

    OptionalDouble average();

    Stream<Double> boxed();

    <R> R collect(Supplier<R> supplier, ObjDoubleConsumer<R> objDoubleConsumer, BiConsumer<R, R> biConsumer);

    long count();

    DoubleStream distinct();

    DoubleStream filter(DoublePredicate doublePredicate);

    OptionalDouble findAny();

    OptionalDouble findFirst();

    DoubleStream flatMap(DoubleFunction<? extends DoubleStream> doubleFunction);

    void forEach(DoubleConsumer doubleConsumer);

    void forEachOrdered(DoubleConsumer doubleConsumer);

    OfDouble iterator();

    DoubleStream limit(long j);

    DoubleStream map(DoubleUnaryOperator doubleUnaryOperator);

    IntStream mapToInt(DoubleToIntFunction doubleToIntFunction);

    LongStream mapToLong(DoubleToLongFunction doubleToLongFunction);

    <U> Stream<U> mapToObj(DoubleFunction<? extends U> doubleFunction);

    OptionalDouble max();

    OptionalDouble min();

    boolean noneMatch(DoublePredicate doublePredicate);

    DoubleStream parallel();

    DoubleStream peek(DoubleConsumer doubleConsumer);

    double reduce(double d, DoubleBinaryOperator doubleBinaryOperator);

    OptionalDouble reduce(DoubleBinaryOperator doubleBinaryOperator);

    DoubleStream sequential();

    DoubleStream skip(long j);

    DoubleStream sorted();

    Spliterator.OfDouble spliterator();

    double sum();

    DoubleSummaryStatistics summaryStatistics();

    double[] toArray();

    static Builder builder() {
        return new DoubleStreamBuilderImpl();
    }

    static DoubleStream empty() {
        return StreamSupport.doubleStream(Spliterators.emptyDoubleSpliterator(), false);
    }

    static DoubleStream of(double t) {
        return StreamSupport.doubleStream(new DoubleStreamBuilderImpl(t), false);
    }

    static DoubleStream of(double... values) {
        return Arrays.stream(values);
    }

    static DoubleStream iterate(final double seed, final DoubleUnaryOperator f) {
        Objects.requireNonNull(f);
        return StreamSupport.doubleStream(Spliterators.spliteratorUnknownSize(new OfDouble() {
            double t = seed;

            public boolean hasNext() {
                return true;
            }

            public double nextDouble() {
                double v = this.t;
                this.t = f.applyAsDouble(this.t);
                return v;
            }
        }, 1296), false);
    }

    static DoubleStream generate(DoubleSupplier s) {
        Objects.requireNonNull(s);
        return StreamSupport.doubleStream(new OfDouble(Long.MAX_VALUE, s), false);
    }

    static DoubleStream concat(DoubleStream a, DoubleStream b) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);
        return (DoubleStream) StreamSupport.doubleStream(new OfDouble(a.spliterator(), b.spliterator()), !a.isParallel() ? b.isParallel() : true).onClose(Streams.composedClose(a, b));
    }
}
