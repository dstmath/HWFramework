package java.util.stream;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.function.UnaryOperator;

public interface Stream<T> extends BaseStream<T, Stream<T>> {

    public interface Builder<T> extends Consumer<T> {
        void accept(T t);

        Stream<T> build();

        Builder<T> add(T t) {
            accept(t);
            return this;
        }
    }

    boolean allMatch(Predicate<? super T> predicate);

    boolean anyMatch(Predicate<? super T> predicate);

    <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> biConsumer, BiConsumer<R, R> biConsumer2);

    <R, A> R collect(Collector<? super T, A, R> collector);

    long count();

    Stream<T> distinct();

    Stream<T> filter(Predicate<? super T> predicate);

    Optional<T> findAny();

    Optional<T> findFirst();

    <R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> function);

    DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> function);

    IntStream flatMapToInt(Function<? super T, ? extends IntStream> function);

    LongStream flatMapToLong(Function<? super T, ? extends LongStream> function);

    void forEach(Consumer<? super T> consumer);

    void forEachOrdered(Consumer<? super T> consumer);

    Stream<T> limit(long j);

    <R> Stream<R> map(Function<? super T, ? extends R> function);

    DoubleStream mapToDouble(ToDoubleFunction<? super T> toDoubleFunction);

    IntStream mapToInt(ToIntFunction<? super T> toIntFunction);

    LongStream mapToLong(ToLongFunction<? super T> toLongFunction);

    Optional<T> max(Comparator<? super T> comparator);

    Optional<T> min(Comparator<? super T> comparator);

    boolean noneMatch(Predicate<? super T> predicate);

    Stream<T> peek(Consumer<? super T> consumer);

    <U> U reduce(U u, BiFunction<U, ? super T, U> biFunction, BinaryOperator<U> binaryOperator);

    T reduce(T t, BinaryOperator<T> binaryOperator);

    Optional<T> reduce(BinaryOperator<T> binaryOperator);

    Stream<T> skip(long j);

    Stream<T> sorted();

    Stream<T> sorted(Comparator<? super T> comparator);

    Object[] toArray();

    <A> A[] toArray(IntFunction<A[]> intFunction);

    static <T> Builder<T> builder() {
        return new StreamBuilderImpl();
    }

    static <T> Stream<T> empty() {
        return StreamSupport.stream(Spliterators.emptySpliterator(), false);
    }

    static <T> Stream<T> of(T t) {
        return StreamSupport.stream(new StreamBuilderImpl(t), false);
    }

    @SafeVarargs
    static <T> Stream<T> of(T... values) {
        return Arrays.stream((Object[]) values);
    }

    static <T> Stream<T> iterate(final T seed, final UnaryOperator<T> f) {
        Objects.requireNonNull(f);
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new Iterator<T>() {
            T t = Streams.NONE;

            public boolean hasNext() {
                return true;
            }

            public T next() {
                T apply = this.t == Streams.NONE ? seed : f.lambda$-java_util_stream_Collectors_49854(this.t);
                this.t = apply;
                return apply;
            }
        }, 1040), false);
    }

    static <T> Stream<T> generate(Supplier<T> s) {
        Objects.requireNonNull(s);
        return StreamSupport.stream(new OfRef(Long.MAX_VALUE, s), false);
    }

    static <T> Stream<T> concat(Stream<? extends T> a, Stream<? extends T> b) {
        Objects.requireNonNull(a);
        Objects.requireNonNull(b);
        return (Stream) StreamSupport.stream(new OfRef(a.spliterator(), b.spliterator()), !a.isParallel() ? b.isParallel() : true).onClose(Streams.composedClose(a, b));
    }
}
