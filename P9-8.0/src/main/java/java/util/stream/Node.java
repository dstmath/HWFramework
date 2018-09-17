package java.util.stream;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.LongConsumer;

public interface Node<T> {

    public interface Builder<T> extends Sink<T> {

        public interface OfDouble extends Builder<Double>, java.util.stream.Sink.OfDouble {
            OfDouble build();
        }

        public interface OfInt extends Builder<Integer>, java.util.stream.Sink.OfInt {
            OfInt build();
        }

        public interface OfLong extends Builder<Long>, java.util.stream.Sink.OfLong {
            OfLong build();
        }

        Node<T> build();
    }

    public interface OfPrimitive<T, T_CONS, T_ARR, T_SPLITR extends java.util.Spliterator.OfPrimitive<T, T_CONS, T_SPLITR>, T_NODE extends OfPrimitive<T, T_CONS, T_ARR, T_SPLITR, T_NODE>> extends Node<T> {
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
            if (count() >= 2147483639) {
                throw new IllegalArgumentException("Stream size exceeds max array size");
            }
            Object[] boxed = (Object[]) generator.apply((int) count());
            copyInto(boxed, 0);
            return boxed;
        }
    }

    public interface OfDouble extends OfPrimitive<Double, DoubleConsumer, double[], java.util.Spliterator.OfDouble, OfDouble> {
        static /* synthetic */ void lambda$-java_util_stream_Node$OfDouble_19427(double e) {
        }

        void forEach(Consumer<? super Double> consumer) {
            if (consumer instanceof DoubleConsumer) {
                forEach((DoubleConsumer) consumer);
                return;
            }
            if (Tripwire.ENABLED) {
                Tripwire.trip(getClass(), "{0} calling Node.OfLong.forEachRemaining(Consumer)");
            }
            ((java.util.Spliterator.OfDouble) spliterator()).forEachRemaining((Consumer) consumer);
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
            int i;
            long size = to - from;
            java.util.Spliterator.OfDouble spliterator = (java.util.Spliterator.OfDouble) spliterator();
            DoubleConsumer nodeBuilder = Nodes.doubleBuilder(size);
            nodeBuilder.begin(size);
            for (i = 0; ((long) i) < from && spliterator.tryAdvance(new DoubleConsumer() {
                public final void accept(double d) {
                    $m$0(d);
                }
            }); i++) {
            }
            for (i = 0; ((long) i) < size && spliterator.tryAdvance(nodeBuilder); i++) {
            }
            nodeBuilder.end();
            return nodeBuilder.build();
        }

        double[] newArray(int count) {
            return new double[count];
        }

        StreamShape getShape() {
            return StreamShape.DOUBLE_VALUE;
        }
    }

    public interface OfInt extends OfPrimitive<Integer, IntConsumer, int[], java.util.Spliterator.OfInt, OfInt> {
        static /* synthetic */ void lambda$-java_util_stream_Node$OfInt_13922(int e) {
        }

        void forEach(Consumer<? super Integer> consumer) {
            if (consumer instanceof IntConsumer) {
                forEach((IntConsumer) consumer);
                return;
            }
            if (Tripwire.ENABLED) {
                Tripwire.trip(getClass(), "{0} calling Node.OfInt.forEachRemaining(Consumer)");
            }
            ((java.util.Spliterator.OfInt) spliterator()).forEachRemaining((Consumer) consumer);
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
            int i;
            long size = to - from;
            java.util.Spliterator.OfInt spliterator = (java.util.Spliterator.OfInt) spliterator();
            IntConsumer nodeBuilder = Nodes.intBuilder(size);
            nodeBuilder.begin(size);
            for (i = 0; ((long) i) < from && spliterator.tryAdvance(new IntConsumer() {
                public final void accept(int i) {
                    $m$0(i);
                }
            }); i++) {
            }
            for (i = 0; ((long) i) < size && spliterator.tryAdvance(nodeBuilder); i++) {
            }
            nodeBuilder.end();
            return nodeBuilder.build();
        }

        int[] newArray(int count) {
            return new int[count];
        }

        StreamShape getShape() {
            return StreamShape.INT_VALUE;
        }
    }

    public interface OfLong extends OfPrimitive<Long, LongConsumer, long[], java.util.Spliterator.OfLong, OfLong> {
        static /* synthetic */ void lambda$-java_util_stream_Node$OfLong_16640(long e) {
        }

        void forEach(Consumer<? super Long> consumer) {
            if (consumer instanceof LongConsumer) {
                forEach((LongConsumer) consumer);
                return;
            }
            if (Tripwire.ENABLED) {
                Tripwire.trip(getClass(), "{0} calling Node.OfLong.forEachRemaining(Consumer)");
            }
            ((java.util.Spliterator.OfLong) spliterator()).forEachRemaining((Consumer) consumer);
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
            int i;
            long size = to - from;
            java.util.Spliterator.OfLong spliterator = (java.util.Spliterator.OfLong) spliterator();
            LongConsumer nodeBuilder = Nodes.longBuilder(size);
            nodeBuilder.begin(size);
            for (i = 0; ((long) i) < from && spliterator.tryAdvance(new LongConsumer() {
                public final void accept(long j) {
                    $m$0(j);
                }
            }); i++) {
            }
            for (i = 0; ((long) i) < size && spliterator.tryAdvance(nodeBuilder); i++) {
            }
            nodeBuilder.end();
            return nodeBuilder.build();
        }

        long[] newArray(int count) {
            return new long[count];
        }

        StreamShape getShape() {
            return StreamShape.LONG_VALUE;
        }
    }

    static /* synthetic */ void lambda$-java_util_stream_Node_5216(Object obj) {
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
        int i;
        Spliterator<T> spliterator = spliterator();
        long size = to - from;
        Builder<T> nodeBuilder = Nodes.builder(size, generator);
        nodeBuilder.begin(size);
        for (i = 0; ((long) i) < from && spliterator.tryAdvance(new -$Lambda$gxn6Hor6yuk2I7Qlz5sIlMEFl7s()); i++) {
        }
        for (i = 0; ((long) i) < size && spliterator.tryAdvance(nodeBuilder); i++) {
        }
        nodeBuilder.end();
        return nodeBuilder.build();
    }

    StreamShape getShape() {
        return StreamShape.REFERENCE;
    }
}
