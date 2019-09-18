package java.util.stream;

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Supplier;
import java.util.stream.DoublePipeline;
import java.util.stream.IntPipeline;
import java.util.stream.LongPipeline;
import java.util.stream.ReferencePipeline;

public final class StreamSupport {
    private StreamSupport() {
    }

    public static <T> Stream<T> stream(Spliterator<T> spliterator, boolean parallel) {
        Objects.requireNonNull(spliterator);
        return new ReferencePipeline.Head((Spliterator<?>) spliterator, StreamOpFlag.fromCharacteristics((Spliterator<?>) spliterator), parallel);
    }

    public static <T> Stream<T> stream(Supplier<? extends Spliterator<T>> supplier, int characteristics, boolean parallel) {
        Objects.requireNonNull(supplier);
        return new ReferencePipeline.Head((Supplier<? extends Spliterator<?>>) supplier, StreamOpFlag.fromCharacteristics(characteristics), parallel);
    }

    public static IntStream intStream(Spliterator.OfInt spliterator, boolean parallel) {
        return new IntPipeline.Head((Spliterator<Integer>) spliterator, StreamOpFlag.fromCharacteristics((Spliterator<?>) spliterator), parallel);
    }

    public static IntStream intStream(Supplier<? extends Spliterator.OfInt> supplier, int characteristics, boolean parallel) {
        return new IntPipeline.Head((Supplier<? extends Spliterator<Integer>>) supplier, StreamOpFlag.fromCharacteristics(characteristics), parallel);
    }

    public static LongStream longStream(Spliterator.OfLong spliterator, boolean parallel) {
        return new LongPipeline.Head((Spliterator<Long>) spliterator, StreamOpFlag.fromCharacteristics((Spliterator<?>) spliterator), parallel);
    }

    public static LongStream longStream(Supplier<? extends Spliterator.OfLong> supplier, int characteristics, boolean parallel) {
        return new LongPipeline.Head((Supplier<? extends Spliterator<Long>>) supplier, StreamOpFlag.fromCharacteristics(characteristics), parallel);
    }

    public static DoubleStream doubleStream(Spliterator.OfDouble spliterator, boolean parallel) {
        return new DoublePipeline.Head((Spliterator<Double>) spliterator, StreamOpFlag.fromCharacteristics((Spliterator<?>) spliterator), parallel);
    }

    public static DoubleStream doubleStream(Supplier<? extends Spliterator.OfDouble> supplier, int characteristics, boolean parallel) {
        return new DoublePipeline.Head((Supplier<? extends Spliterator<Double>>) supplier, StreamOpFlag.fromCharacteristics(characteristics), parallel);
    }
}
