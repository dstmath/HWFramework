package java.util.stream;

import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterator.OfDouble;
import java.util.Spliterator.OfInt;
import java.util.Spliterator.OfLong;
import java.util.function.Supplier;
import java.util.stream.ReferencePipeline.Head;

public final class StreamSupport {
    private StreamSupport() {
    }

    public static <T> Stream<T> stream(Spliterator<T> spliterator, boolean parallel) {
        Objects.requireNonNull(spliterator);
        return new Head((Spliterator) spliterator, StreamOpFlag.fromCharacteristics((Spliterator) spliterator), parallel);
    }

    public static <T> Stream<T> stream(Supplier<? extends Spliterator<T>> supplier, int characteristics, boolean parallel) {
        Objects.requireNonNull(supplier);
        return new Head((Supplier) supplier, StreamOpFlag.fromCharacteristics(characteristics), parallel);
    }

    public static IntStream intStream(OfInt spliterator, boolean parallel) {
        return new IntPipeline.Head((Spliterator) spliterator, StreamOpFlag.fromCharacteristics((Spliterator) spliterator), parallel);
    }

    public static IntStream intStream(Supplier<? extends OfInt> supplier, int characteristics, boolean parallel) {
        return new IntPipeline.Head((Supplier) supplier, StreamOpFlag.fromCharacteristics(characteristics), parallel);
    }

    public static LongStream longStream(OfLong spliterator, boolean parallel) {
        return new LongPipeline.Head((Spliterator) spliterator, StreamOpFlag.fromCharacteristics((Spliterator) spliterator), parallel);
    }

    public static LongStream longStream(Supplier<? extends OfLong> supplier, int characteristics, boolean parallel) {
        return new LongPipeline.Head((Supplier) supplier, StreamOpFlag.fromCharacteristics(characteristics), parallel);
    }

    public static DoubleStream doubleStream(OfDouble spliterator, boolean parallel) {
        return new DoublePipeline.Head((Spliterator) spliterator, StreamOpFlag.fromCharacteristics((Spliterator) spliterator), parallel);
    }

    public static DoubleStream doubleStream(Supplier<? extends OfDouble> supplier, int characteristics, boolean parallel) {
        return new DoublePipeline.Head((Supplier) supplier, StreamOpFlag.fromCharacteristics(characteristics), parallel);
    }
}
