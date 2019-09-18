package java.util.stream;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.LongConsumer;
import java.util.stream.Sink;

public interface Node<T> {

    public interface Builder<T> extends Sink<T> {

        public interface OfDouble extends Builder<Double>, Sink.OfDouble {
            OfDouble build();
        }

        public interface OfInt extends Builder<Integer>, Sink.OfInt {
            OfInt build();
        }

        public interface OfLong extends Builder<Long>, Sink.OfLong {
            OfLong build();
        }

        Node<T> build();
    }

    public interface OfDouble extends OfPrimitive<Double, DoubleConsumer, double[], Spliterator.OfDouble, OfDouble> {
        void forEach(Consumer<? super Double> consumer) {
            if (consumer instanceof DoubleConsumer) {
                forEach((DoubleConsumer) consumer);
                return;
            }
            if (Tripwire.ENABLED) {
                Tripwire.trip(getClass(), "{0} calling Node.OfLong.forEachRemaining(Consumer)");
            }
            ((Spliterator.OfDouble) spliterator()).forEachRemaining(consumer);
        }

        void copyInto(Double[] boxed, int offset) {
            if (Tripwire.ENABLED) {
                Tripwire.trip(getClass(), "{0} calling Node.OfDouble.copyInto(Double[], int)");
            }
            double[] array = (double[]) asPrimitiveArray();
            for (int i = 0; i < array.length; i++) {
                boxed[offset + i] = Double.valueOf(array[i]);
            }
        }

        OfDouble truncate(long from, long to, IntFunction<Double[]> intFunction) {
            if (from == 0 && to == count()) {
                return this;
            }
            long size = to - from;
            Spliterator.OfDouble spliterator = (Spliterator.OfDouble) spliterator();
            Builder.OfDouble nodeBuilder = Nodes.doubleBuilder(size);
            nodeBuilder.begin(size);
            for (int i = 0; ((long) i) < from && spliterator.tryAdvance((DoubleConsumer) $$Lambda$Node$OfDouble$5XMtiGLC0wkQzF2OIEVEnEBoYWM.INSTANCE); i++) {
            }
            for (int i2 = 0; ((long) i2) < size && spliterator.tryAdvance((DoubleConsumer) nodeBuilder); i2++) {
            }
            nodeBuilder.end();
            return nodeBuilder.build();
        }

        static /* synthetic */ void lambda$truncate$0(double e) {
        }

        double[] newArray(int count) {
            return new double[count];
        }

        StreamShape getShape() {
            return StreamShape.DOUBLE_VALUE;
        }
    }

    public interface OfInt extends OfPrimitive<Integer, IntConsumer, int[], Spliterator.OfInt, OfInt> {
        void forEach(Consumer<? super Integer> consumer) {
            if (consumer instanceof IntConsumer) {
                forEach((IntConsumer) consumer);
                return;
            }
            if (Tripwire.ENABLED) {
                Tripwire.trip(getClass(), "{0} calling Node.OfInt.forEachRemaining(Consumer)");
            }
            ((Spliterator.OfInt) spliterator()).forEachRemaining(consumer);
        }

        void copyInto(Integer[] boxed, int offset) {
            if (Tripwire.ENABLED) {
                Tripwire.trip(getClass(), "{0} calling Node.OfInt.copyInto(Integer[], int)");
            }
            int[] array = (int[]) asPrimitiveArray();
            for (int i = 0; i < array.length; i++) {
                boxed[offset + i] = Integer.valueOf(array[i]);
            }
        }

        OfInt truncate(long from, long to, IntFunction<Integer[]> intFunction) {
            if (from == 0 && to == count()) {
                return this;
            }
            long size = to - from;
            Spliterator.OfInt spliterator = (Spliterator.OfInt) spliterator();
            Builder.OfInt nodeBuilder = Nodes.intBuilder(size);
            nodeBuilder.begin(size);
            for (int i = 0; ((long) i) < from && spliterator.tryAdvance((IntConsumer) $$Lambda$Node$OfInt$SR5qcq7S0oCtehCDXAgbRdnvBbw.INSTANCE); i++) {
            }
            for (int i2 = 0; ((long) i2) < size && spliterator.tryAdvance((IntConsumer) nodeBuilder); i2++) {
            }
            nodeBuilder.end();
            return nodeBuilder.build();
        }

        static /* synthetic */ void lambda$truncate$0(int e) {
        }

        int[] newArray(int count) {
            return new int[count];
        }

        StreamShape getShape() {
            return StreamShape.INT_VALUE;
        }
    }

    public interface OfLong extends OfPrimitive<Long, LongConsumer, long[], Spliterator.OfLong, OfLong> {
        void forEach(Consumer<? super Long> consumer) {
            if (consumer instanceof LongConsumer) {
                forEach((LongConsumer) consumer);
                return;
            }
            if (Tripwire.ENABLED) {
                Tripwire.trip(getClass(), "{0} calling Node.OfLong.forEachRemaining(Consumer)");
            }
            ((Spliterator.OfLong) spliterator()).forEachRemaining(consumer);
        }

        void copyInto(Long[] boxed, int offset) {
            if (Tripwire.ENABLED) {
                Tripwire.trip(getClass(), "{0} calling Node.OfInt.copyInto(Long[], int)");
            }
            long[] array = (long[]) asPrimitiveArray();
            for (int i = 0; i < array.length; i++) {
                boxed[offset + i] = Long.valueOf(array[i]);
            }
        }

        OfLong truncate(long from, long to, IntFunction<Long[]> intFunction) {
            if (from == 0 && to == count()) {
                return this;
            }
            long size = to - from;
            Spliterator.OfLong spliterator = (Spliterator.OfLong) spliterator();
            Builder.OfLong nodeBuilder = Nodes.longBuilder(size);
            nodeBuilder.begin(size);
            for (int i = 0; ((long) i) < from && spliterator.tryAdvance((LongConsumer) $$Lambda$Node$OfLong$bPdsg_NFhPinja_QQPm0P0wq9s.INSTANCE); i++) {
            }
            for (int i2 = 0; ((long) i2) < size && spliterator.tryAdvance((LongConsumer) nodeBuilder); i2++) {
            }
            nodeBuilder.end();
            return nodeBuilder.build();
        }

        static /* synthetic */ void lambda$truncate$0(long e) {
        }

        long[] newArray(int count) {
            return new long[count];
        }

        StreamShape getShape() {
            return StreamShape.LONG_VALUE;
        }
    }

    public interface OfPrimitive<T, T_CONS, T_ARR, T_SPLITR extends Spliterator.OfPrimitive<T, T_CONS, T_SPLITR>, T_NODE extends OfPrimitive<T, T_CONS, T_ARR, T_SPLITR, T_NODE>> extends Node<T> {
        T_ARR asPrimitiveArray();

        void copyInto(T_ARR t_arr, int i);

        void forEach(T_CONS t_cons);

        T_ARR newArray(int i);

        T_SPLITR spliterator();

        T_NODE truncate(long j, long j2, IntFunction<T[]> intFunction);

        T_NODE getChild(int i) {
            throw new IndexOutOfBoundsException();
        }

        T[] asArray(IntFunction<T[]> generator) {
            if (Tripwire.ENABLED) {
                Tripwire.trip(getClass(), "{0} calling Node.OfPrimitive.asArray");
            }
            if (count() < 2147483639) {
                T[] boxed = (Object[]) generator.apply((int) count());
                copyInto(boxed, 0);
                return boxed;
            }
            throw new IllegalArgumentException("Stream size exceeds max array size");
        }
    }

    T[] asArray(IntFunction<T[]> intFunction);

    void copyInto(T[] tArr, int i);

    long count();

    void forEach(Consumer<? super T> consumer);

    Spliterator<T> spliterator();

    int getChildCount() {
        return 0;
    }

    Node<T> getChild(int i) {
        throw new IndexOutOfBoundsException();
    }

    Node<T> truncate(long from, long to, IntFunction<T[]> generator) {
        if (from == 0 && to == count()) {
            return this;
        }
        Spliterator<T> spliterator = spliterator();
        long size = to - from;
        Builder<T> nodeBuilder = Nodes.builder(size, generator);
        nodeBuilder.begin(size);
        for (int i = 0; ((long) i) < from && spliterator.tryAdvance($$Lambda$Node$fa69PlTVbbnR3yr46T9Wo0_bIhg.INSTANCE); i++) {
        }
        for (int i2 = 0; ((long) i2) < size && spliterator.tryAdvance(nodeBuilder); i2++) {
        }
        nodeBuilder.end();
        return nodeBuilder.build();
    }

    static /* synthetic */ void lambda$truncate$0(Object e) {
    }

    StreamShape getShape() {
        return StreamShape.REFERENCE;
    }
}
