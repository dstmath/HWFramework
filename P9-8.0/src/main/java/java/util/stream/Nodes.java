package java.util.stream;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.CountedCompleter;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.IntFunction;
import java.util.function.LongConsumer;
import java.util.function.LongFunction;
import java.util.stream.-$Lambda$aRB2Ve9yTNaLYumrLKKo_GKLlhc.AnonymousClass7;
import java.util.stream.Node.Builder;
import java.util.stream.Node.OfDouble;
import java.util.stream.Node.OfInt;
import java.util.stream.Node.OfLong;

final class Nodes {
    private static final /* synthetic */ int[] -java-util-stream-StreamShapeSwitchesValues = null;
    static final String BAD_SIZE = "Stream size exceeds max array size";
    private static final double[] EMPTY_DOUBLE_ARRAY = new double[0];
    private static final OfDouble EMPTY_DOUBLE_NODE = new OfDouble();
    private static final int[] EMPTY_INT_ARRAY = new int[0];
    private static final OfInt EMPTY_INT_NODE = new OfInt();
    private static final long[] EMPTY_LONG_ARRAY = new long[0];
    private static final OfLong EMPTY_LONG_NODE = new OfLong();
    private static final Node EMPTY_NODE = new OfRef();
    static final long MAX_ARRAY_SIZE = 2147483639;

    private static abstract class AbstractConcNode<T, T_NODE extends Node<T>> implements Node<T> {
        protected final T_NODE left;
        protected final T_NODE right;
        private final long size;

        AbstractConcNode(T_NODE left, T_NODE right) {
            this.left = left;
            this.right = right;
            this.size = left.count() + right.count();
        }

        public int getChildCount() {
            return 2;
        }

        public T_NODE getChild(int i) {
            if (i == 0) {
                return this.left;
            }
            if (i == 1) {
                return this.right;
            }
            throw new IndexOutOfBoundsException();
        }

        public long count() {
            return this.size;
        }
    }

    private static class ArrayNode<T> implements Node<T> {
        final T[] array;
        int curSize;

        ArrayNode(long size, IntFunction<T[]> generator) {
            if (size >= Nodes.MAX_ARRAY_SIZE) {
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            }
            this.array = (Object[]) generator.apply((int) size);
            this.curSize = 0;
        }

        ArrayNode(T[] array) {
            this.array = array;
            this.curSize = array.length;
        }

        public Spliterator<T> spliterator() {
            return Arrays.spliterator(this.array, 0, this.curSize);
        }

        public void copyInto(T[] dest, int destOffset) {
            System.arraycopy(this.array, 0, (Object) dest, destOffset, this.curSize);
        }

        public T[] asArray(IntFunction<T[]> intFunction) {
            if (this.array.length == this.curSize) {
                return this.array;
            }
            throw new IllegalStateException();
        }

        public long count() {
            return (long) this.curSize;
        }

        public void forEach(Consumer<? super T> consumer) {
            for (int i = 0; i < this.curSize; i++) {
                consumer.-java_util_stream_StreamSpliterators$WrappingSpliterator-mthref-1(this.array[i]);
            }
        }

        public String toString() {
            return String.format("ArrayNode[%d][%s]", Integer.valueOf(this.array.length - this.curSize), Arrays.toString(this.array));
        }
    }

    private static final class CollectionNode<T> implements Node<T> {
        private final Collection<T> c;

        CollectionNode(Collection<T> c) {
            this.c = c;
        }

        public Spliterator<T> spliterator() {
            return this.c.stream().spliterator();
        }

        public void copyInto(T[] array, int offset) {
            for (T t : this.c) {
                int offset2 = offset + 1;
                array[offset] = t;
                offset = offset2;
            }
        }

        public T[] asArray(IntFunction<T[]> generator) {
            return this.c.toArray((Object[]) generator.apply(this.c.size()));
        }

        public long count() {
            return (long) this.c.size();
        }

        public void forEach(Consumer<? super T> consumer) {
            this.c.forEach(consumer);
        }

        public String toString() {
            return String.format("CollectionNode[%d][%s]", Integer.valueOf(this.c.size()), this.c);
        }
    }

    private static class CollectorTask<P_IN, P_OUT, T_NODE extends Node<P_OUT>, T_BUILDER extends Builder<P_OUT>> extends AbstractTask<P_IN, P_OUT, T_NODE, CollectorTask<P_IN, P_OUT, T_NODE, T_BUILDER>> {
        protected final LongFunction<T_BUILDER> builderFactory;
        protected final BinaryOperator<T_NODE> concFactory;
        protected final PipelineHelper<P_OUT> helper;

        private static final class OfDouble<P_IN> extends CollectorTask<P_IN, Double, java.util.stream.Node.OfDouble, java.util.stream.Node.Builder.OfDouble> {
            OfDouble(PipelineHelper<Double> helper, Spliterator<P_IN> spliterator) {
                super(helper, spliterator, new LongFunction() {
                    public final Object apply(long j) {
                        return $m$0(j);
                    }
                }, new -$Lambda$aRB2Ve9yTNaLYumrLKKo_GKLlhc());
            }
        }

        private static final class OfInt<P_IN> extends CollectorTask<P_IN, Integer, java.util.stream.Node.OfInt, java.util.stream.Node.Builder.OfInt> {
            OfInt(PipelineHelper<Integer> helper, Spliterator<P_IN> spliterator) {
                super(helper, spliterator, new LongFunction() {
                    public final Object apply(long j) {
                        return $m$0(j);
                    }
                }, new BinaryOperator() {
                    public final Object apply(Object obj, Object obj2) {
                        return $m$0(obj, obj2);
                    }
                });
            }
        }

        private static final class OfLong<P_IN> extends CollectorTask<P_IN, Long, java.util.stream.Node.OfLong, java.util.stream.Node.Builder.OfLong> {
            OfLong(PipelineHelper<Long> helper, Spliterator<P_IN> spliterator) {
                super(helper, spliterator, new LongFunction() {
                    public final Object apply(long j) {
                        return $m$0(j);
                    }
                }, new BinaryOperator() {
                    public final Object apply(Object obj, Object obj2) {
                        return $m$0(obj, obj2);
                    }
                });
            }
        }

        private static final class OfRef<P_IN, P_OUT> extends CollectorTask<P_IN, P_OUT, Node<P_OUT>, Builder<P_OUT>> {
            OfRef(PipelineHelper<P_OUT> helper, IntFunction<P_OUT[]> generator, Spliterator<P_IN> spliterator) {
                super(helper, spliterator, new AnonymousClass7(generator), new BinaryOperator() {
                    public final Object apply(Object obj, Object obj2) {
                        return $m$0(obj, obj2);
                    }
                });
            }
        }

        CollectorTask(PipelineHelper<P_OUT> helper, Spliterator<P_IN> spliterator, LongFunction<T_BUILDER> builderFactory, BinaryOperator<T_NODE> concFactory) {
            super((PipelineHelper) helper, (Spliterator) spliterator);
            this.helper = helper;
            this.builderFactory = builderFactory;
            this.concFactory = concFactory;
        }

        CollectorTask(CollectorTask<P_IN, P_OUT, T_NODE, T_BUILDER> parent, Spliterator<P_IN> spliterator) {
            super((AbstractTask) parent, (Spliterator) spliterator);
            this.helper = parent.helper;
            this.builderFactory = parent.builderFactory;
            this.concFactory = parent.concFactory;
        }

        protected CollectorTask<P_IN, P_OUT, T_NODE, T_BUILDER> makeChild(Spliterator<P_IN> spliterator) {
            return new CollectorTask(this, spliterator);
        }

        protected T_NODE doLeaf() {
            return ((Builder) this.helper.wrapAndCopyInto((Builder) this.builderFactory.apply(this.helper.exactOutputSizeIfKnown(this.spliterator)), this.spliterator)).build();
        }

        public void onCompletion(CountedCompleter<?> caller) {
            if (!isLeaf()) {
                setLocalResult((Node) this.concFactory.apply((Node) ((CollectorTask) this.leftChild).getLocalResult(), (Node) ((CollectorTask) this.rightChild).getLocalResult()));
            }
            super.onCompletion(caller);
        }
    }

    static final class ConcNode<T> extends AbstractConcNode<T, Node<T>> implements Node<T> {

        private static abstract class OfPrimitive<E, T_CONS, T_ARR, T_SPLITR extends java.util.Spliterator.OfPrimitive<E, T_CONS, T_SPLITR>, T_NODE extends java.util.stream.Node.OfPrimitive<E, T_CONS, T_ARR, T_SPLITR, T_NODE>> extends AbstractConcNode<E, T_NODE> implements java.util.stream.Node.OfPrimitive<E, T_CONS, T_ARR, T_SPLITR, T_NODE> {
            public /* bridge */ /* synthetic */ java.util.stream.Node.OfPrimitive getChild(int i) {
                return (java.util.stream.Node.OfPrimitive) getChild(i);
            }

            OfPrimitive(T_NODE left, T_NODE right) {
                super(left, right);
            }

            public void forEach(T_CONS consumer) {
                ((java.util.stream.Node.OfPrimitive) this.left).forEach(consumer);
                ((java.util.stream.Node.OfPrimitive) this.right).forEach(consumer);
            }

            public void copyInto(T_ARR array, int offset) {
                ((java.util.stream.Node.OfPrimitive) this.left).copyInto(array, offset);
                ((java.util.stream.Node.OfPrimitive) this.right).copyInto(array, ((int) ((java.util.stream.Node.OfPrimitive) this.left).count()) + offset);
            }

            public T_ARR asPrimitiveArray() {
                long size = count();
                if (size >= Nodes.MAX_ARRAY_SIZE) {
                    throw new IllegalArgumentException(Nodes.BAD_SIZE);
                }
                T_ARR array = newArray((int) size);
                copyInto(array, 0);
                return array;
            }

            public String toString() {
                if (count() < 32) {
                    return String.format("%s[%s.%s]", getClass().getName(), this.left, this.right);
                }
                return String.format("%s[size=%d]", getClass().getName(), Long.valueOf(count()));
            }
        }

        static final class OfDouble extends OfPrimitive<Double, DoubleConsumer, double[], java.util.Spliterator.OfDouble, java.util.stream.Node.OfDouble> implements java.util.stream.Node.OfDouble {
            OfDouble(java.util.stream.Node.OfDouble left, java.util.stream.Node.OfDouble right) {
                super(left, right);
            }

            public java.util.Spliterator.OfDouble spliterator() {
                return new OfDouble(this);
            }
        }

        static final class OfInt extends OfPrimitive<Integer, IntConsumer, int[], java.util.Spliterator.OfInt, java.util.stream.Node.OfInt> implements java.util.stream.Node.OfInt {
            OfInt(java.util.stream.Node.OfInt left, java.util.stream.Node.OfInt right) {
                super(left, right);
            }

            public java.util.Spliterator.OfInt spliterator() {
                return new OfInt(this);
            }
        }

        static final class OfLong extends OfPrimitive<Long, LongConsumer, long[], java.util.Spliterator.OfLong, java.util.stream.Node.OfLong> implements java.util.stream.Node.OfLong {
            OfLong(java.util.stream.Node.OfLong left, java.util.stream.Node.OfLong right) {
                super(left, right);
            }

            public java.util.Spliterator.OfLong spliterator() {
                return new OfLong(this);
            }
        }

        ConcNode(Node<T> left, Node<T> right) {
            super(left, right);
        }

        public Spliterator<T> spliterator() {
            return new OfRef(this);
        }

        public void copyInto(T[] array, int offset) {
            Objects.requireNonNull(array);
            this.left.copyInto(array, offset);
            this.right.copyInto(array, ((int) this.left.count()) + offset);
        }

        public T[] asArray(IntFunction<T[]> generator) {
            long size = count();
            if (size >= Nodes.MAX_ARRAY_SIZE) {
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            }
            Object[] array = (Object[]) generator.apply((int) size);
            copyInto(array, 0);
            return array;
        }

        public void forEach(Consumer<? super T> consumer) {
            this.left.forEach(consumer);
            this.right.forEach(consumer);
        }

        public Node<T> truncate(long from, long to, IntFunction<T[]> generator) {
            if (from == 0 && to == count()) {
                return this;
            }
            long leftCount = this.left.count();
            if (from >= leftCount) {
                return this.right.truncate(from - leftCount, to - leftCount, generator);
            }
            if (to <= leftCount) {
                return this.left.truncate(from, to, generator);
            }
            return Nodes.conc(getShape(), this.left.truncate(from, leftCount, generator), this.right.truncate(0, to - leftCount, generator));
        }

        public String toString() {
            if (count() < 32) {
                return String.format("ConcNode[%s.%s]", this.left, this.right);
            }
            return String.format("ConcNode[size=%d]", Long.valueOf(count()));
        }
    }

    private static class DoubleArrayNode implements OfDouble {
        final double[] array;
        int curSize;

        DoubleArrayNode(long size) {
            if (size >= Nodes.MAX_ARRAY_SIZE) {
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            }
            this.array = new double[((int) size)];
            this.curSize = 0;
        }

        DoubleArrayNode(double[] array) {
            this.array = array;
            this.curSize = array.length;
        }

        public Spliterator.OfDouble spliterator() {
            return Arrays.spliterator(this.array, 0, this.curSize);
        }

        public double[] asPrimitiveArray() {
            if (this.array.length == this.curSize) {
                return this.array;
            }
            return Arrays.copyOf(this.array, this.curSize);
        }

        public void copyInto(double[] dest, int destOffset) {
            System.arraycopy(this.array, 0, dest, destOffset, this.curSize);
        }

        public long count() {
            return (long) this.curSize;
        }

        public void forEach(DoubleConsumer consumer) {
            for (int i = 0; i < this.curSize; i++) {
                consumer.-java_util_stream_StreamSpliterators$DoubleWrappingSpliterator-mthref-1(this.array[i]);
            }
        }

        public String toString() {
            return String.format("DoubleArrayNode[%d][%s]", Integer.valueOf(this.array.length - this.curSize), Arrays.toString(this.array));
        }
    }

    private static final class DoubleFixedNodeBuilder extends DoubleArrayNode implements Builder.OfDouble {
        static final /* synthetic */ boolean -assertionsDisabled = (DoubleFixedNodeBuilder.class.desiredAssertionStatus() ^ 1);

        DoubleFixedNodeBuilder(long size) {
            super(size);
            if (!-assertionsDisabled && size >= Nodes.MAX_ARRAY_SIZE) {
                throw new AssertionError();
            }
        }

        public OfDouble build() {
            if (this.curSize >= this.array.length) {
                return this;
            }
            throw new IllegalStateException(String.format("Current size %d is less than fixed size %d", Integer.valueOf(this.curSize), Integer.valueOf(this.array.length)));
        }

        public void begin(long size) {
            if (size != ((long) this.array.length)) {
                throw new IllegalStateException(String.format("Begin size %d is not equal to fixed size %d", Long.valueOf(size), Integer.valueOf(this.array.length)));
            } else {
                this.curSize = 0;
            }
        }

        public void accept(double i) {
            if (this.curSize < this.array.length) {
                double[] dArr = this.array;
                int i2 = this.curSize;
                this.curSize = i2 + 1;
                dArr[i2] = i;
                return;
            }
            throw new IllegalStateException(String.format("Accept exceeded fixed size of %d", Integer.valueOf(this.array.length)));
        }

        public void end() {
            if (this.curSize < this.array.length) {
                throw new IllegalStateException(String.format("End size %d is less than fixed size %d", Integer.valueOf(this.curSize), Integer.valueOf(this.array.length)));
            }
        }

        public String toString() {
            return String.format("DoubleFixedNodeBuilder[%d][%s]", Integer.valueOf(this.array.length - this.curSize), Arrays.toString(this.array));
        }
    }

    private static final class DoubleSpinedNodeBuilder extends SpinedBuffer.OfDouble implements OfDouble, Builder.OfDouble {
        static final /* synthetic */ boolean -assertionsDisabled = (DoubleSpinedNodeBuilder.class.desiredAssertionStatus() ^ 1);
        private boolean building = false;

        DoubleSpinedNodeBuilder() {
        }

        public Spliterator.OfDouble spliterator() {
            if (-assertionsDisabled || !this.building) {
                return super.spliterator();
            }
            throw new AssertionError((Object) "during building");
        }

        public void forEach(DoubleConsumer consumer) {
            if (-assertionsDisabled || !this.building) {
                super.forEach((Object) consumer);
                return;
            }
            throw new AssertionError((Object) "during building");
        }

        public void begin(long size) {
            if (-assertionsDisabled || !this.building) {
                this.building = true;
                clear();
                ensureCapacity(size);
                return;
            }
            throw new AssertionError((Object) "was already building");
        }

        public void accept(double i) {
            if (-assertionsDisabled || this.building) {
                super.-java_util_stream_StreamSpliterators$DoubleWrappingSpliterator-mthref-0(i);
                return;
            }
            throw new AssertionError((Object) "not building");
        }

        public void end() {
            if (-assertionsDisabled || this.building) {
                this.building = false;
                return;
            }
            throw new AssertionError((Object) "was not building");
        }

        public void copyInto(double[] array, int offset) {
            if (-assertionsDisabled || !this.building) {
                super.copyInto(array, offset);
                return;
            }
            throw new AssertionError((Object) "during building");
        }

        public double[] asPrimitiveArray() {
            if (-assertionsDisabled || !this.building) {
                return (double[]) super.asPrimitiveArray();
            }
            throw new AssertionError((Object) "during building");
        }

        public OfDouble build() {
            if (-assertionsDisabled || !this.building) {
                return this;
            }
            throw new AssertionError((Object) "during building");
        }
    }

    private static abstract class EmptyNode<T, T_ARR, T_CONS> implements Node<T> {

        private static final class OfDouble extends EmptyNode<Double, double[], DoubleConsumer> implements java.util.stream.Node.OfDouble {
            OfDouble() {
            }

            public java.util.Spliterator.OfDouble spliterator() {
                return Spliterators.emptyDoubleSpliterator();
            }

            public double[] asPrimitiveArray() {
                return Nodes.EMPTY_DOUBLE_ARRAY;
            }
        }

        private static final class OfInt extends EmptyNode<Integer, int[], IntConsumer> implements java.util.stream.Node.OfInt {
            OfInt() {
            }

            public java.util.Spliterator.OfInt spliterator() {
                return Spliterators.emptyIntSpliterator();
            }

            public int[] asPrimitiveArray() {
                return Nodes.EMPTY_INT_ARRAY;
            }
        }

        private static final class OfLong extends EmptyNode<Long, long[], LongConsumer> implements java.util.stream.Node.OfLong {
            OfLong() {
            }

            public java.util.Spliterator.OfLong spliterator() {
                return Spliterators.emptyLongSpliterator();
            }

            public long[] asPrimitiveArray() {
                return Nodes.EMPTY_LONG_ARRAY;
            }
        }

        private static class OfRef<T> extends EmptyNode<T, T[], Consumer<? super T>> {
            /* synthetic */ OfRef(OfRef -this0) {
                this();
            }

            private OfRef() {
            }

            public Spliterator<T> spliterator() {
                return Spliterators.emptySpliterator();
            }
        }

        EmptyNode() {
        }

        public T[] asArray(IntFunction<T[]> generator) {
            return (Object[]) generator.apply(0);
        }

        public void copyInto(T_ARR t_arr, int offset) {
        }

        public long count() {
            return 0;
        }

        public void forEach(T_CONS t_cons) {
        }
    }

    private static final class FixedNodeBuilder<T> extends ArrayNode<T> implements Builder<T> {
        static final /* synthetic */ boolean -assertionsDisabled = (FixedNodeBuilder.class.desiredAssertionStatus() ^ 1);

        FixedNodeBuilder(long size, IntFunction<T[]> generator) {
            super(size, generator);
            if (!-assertionsDisabled && size >= Nodes.MAX_ARRAY_SIZE) {
                throw new AssertionError();
            }
        }

        public Node<T> build() {
            if (this.curSize >= this.array.length) {
                return this;
            }
            throw new IllegalStateException(String.format("Current size %d is less than fixed size %d", Integer.valueOf(this.curSize), Integer.valueOf(this.array.length)));
        }

        public void begin(long size) {
            if (size != ((long) this.array.length)) {
                throw new IllegalStateException(String.format("Begin size %d is not equal to fixed size %d", Long.valueOf(size), Integer.valueOf(this.array.length)));
            } else {
                this.curSize = 0;
            }
        }

        public void accept(T t) {
            if (this.curSize < this.array.length) {
                Object[] objArr = this.array;
                int i = this.curSize;
                this.curSize = i + 1;
                objArr[i] = t;
                return;
            }
            throw new IllegalStateException(String.format("Accept exceeded fixed size of %d", Integer.valueOf(this.array.length)));
        }

        public void end() {
            if (this.curSize < this.array.length) {
                throw new IllegalStateException(String.format("End size %d is less than fixed size %d", Integer.valueOf(this.curSize), Integer.valueOf(this.array.length)));
            }
        }

        public String toString() {
            return String.format("FixedNodeBuilder[%d][%s]", Integer.valueOf(this.array.length - this.curSize), Arrays.toString(this.array));
        }
    }

    private static class IntArrayNode implements OfInt {
        final int[] array;
        int curSize;

        IntArrayNode(long size) {
            if (size >= Nodes.MAX_ARRAY_SIZE) {
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            }
            this.array = new int[((int) size)];
            this.curSize = 0;
        }

        IntArrayNode(int[] array) {
            this.array = array;
            this.curSize = array.length;
        }

        public Spliterator.OfInt spliterator() {
            return Arrays.spliterator(this.array, 0, this.curSize);
        }

        public int[] asPrimitiveArray() {
            if (this.array.length == this.curSize) {
                return this.array;
            }
            return Arrays.copyOf(this.array, this.curSize);
        }

        public void copyInto(int[] dest, int destOffset) {
            System.arraycopy(this.array, 0, dest, destOffset, this.curSize);
        }

        public long count() {
            return (long) this.curSize;
        }

        public void forEach(IntConsumer consumer) {
            for (int i = 0; i < this.curSize; i++) {
                consumer.-java_util_stream_StreamSpliterators$IntWrappingSpliterator-mthref-1(this.array[i]);
            }
        }

        public String toString() {
            return String.format("IntArrayNode[%d][%s]", Integer.valueOf(this.array.length - this.curSize), Arrays.toString(this.array));
        }
    }

    private static final class IntFixedNodeBuilder extends IntArrayNode implements Builder.OfInt {
        static final /* synthetic */ boolean -assertionsDisabled = (IntFixedNodeBuilder.class.desiredAssertionStatus() ^ 1);

        IntFixedNodeBuilder(long size) {
            super(size);
            if (!-assertionsDisabled && size >= Nodes.MAX_ARRAY_SIZE) {
                throw new AssertionError();
            }
        }

        public OfInt build() {
            if (this.curSize >= this.array.length) {
                return this;
            }
            throw new IllegalStateException(String.format("Current size %d is less than fixed size %d", Integer.valueOf(this.curSize), Integer.valueOf(this.array.length)));
        }

        public void begin(long size) {
            if (size != ((long) this.array.length)) {
                throw new IllegalStateException(String.format("Begin size %d is not equal to fixed size %d", Long.valueOf(size), Integer.valueOf(this.array.length)));
            } else {
                this.curSize = 0;
            }
        }

        public void accept(int i) {
            if (this.curSize < this.array.length) {
                int[] iArr = this.array;
                int i2 = this.curSize;
                this.curSize = i2 + 1;
                iArr[i2] = i;
                return;
            }
            throw new IllegalStateException(String.format("Accept exceeded fixed size of %d", Integer.valueOf(this.array.length)));
        }

        public void end() {
            if (this.curSize < this.array.length) {
                throw new IllegalStateException(String.format("End size %d is less than fixed size %d", Integer.valueOf(this.curSize), Integer.valueOf(this.array.length)));
            }
        }

        public String toString() {
            return String.format("IntFixedNodeBuilder[%d][%s]", Integer.valueOf(this.array.length - this.curSize), Arrays.toString(this.array));
        }
    }

    private static final class IntSpinedNodeBuilder extends SpinedBuffer.OfInt implements OfInt, Builder.OfInt {
        static final /* synthetic */ boolean -assertionsDisabled = (IntSpinedNodeBuilder.class.desiredAssertionStatus() ^ 1);
        private boolean building = false;

        IntSpinedNodeBuilder() {
        }

        public Spliterator.OfInt spliterator() {
            if (-assertionsDisabled || !this.building) {
                return super.spliterator();
            }
            throw new AssertionError((Object) "during building");
        }

        public void forEach(IntConsumer consumer) {
            if (-assertionsDisabled || !this.building) {
                super.forEach((Object) consumer);
                return;
            }
            throw new AssertionError((Object) "during building");
        }

        public void begin(long size) {
            if (-assertionsDisabled || !this.building) {
                this.building = true;
                clear();
                ensureCapacity(size);
                return;
            }
            throw new AssertionError((Object) "was already building");
        }

        public void accept(int i) {
            if (-assertionsDisabled || this.building) {
                super.-java_util_stream_StreamSpliterators$IntWrappingSpliterator-mthref-0(i);
                return;
            }
            throw new AssertionError((Object) "not building");
        }

        public void end() {
            if (-assertionsDisabled || this.building) {
                this.building = false;
                return;
            }
            throw new AssertionError((Object) "was not building");
        }

        public void copyInto(int[] array, int offset) throws IndexOutOfBoundsException {
            if (-assertionsDisabled || !this.building) {
                super.copyInto(array, offset);
                return;
            }
            throw new AssertionError((Object) "during building");
        }

        public int[] asPrimitiveArray() {
            if (-assertionsDisabled || !this.building) {
                return (int[]) super.asPrimitiveArray();
            }
            throw new AssertionError((Object) "during building");
        }

        public OfInt build() {
            if (-assertionsDisabled || !this.building) {
                return this;
            }
            throw new AssertionError((Object) "during building");
        }
    }

    private static abstract class InternalNodeSpliterator<T, S extends Spliterator<T>, N extends Node<T>> implements Spliterator<T> {
        int curChildIndex;
        N curNode;
        S lastNodeSpliterator;
        S tryAdvanceSpliterator;
        Deque<N> tryAdvanceStack;

        private static abstract class OfPrimitive<T, T_CONS, T_ARR, T_SPLITR extends java.util.Spliterator.OfPrimitive<T, T_CONS, T_SPLITR>, N extends java.util.stream.Node.OfPrimitive<T, T_CONS, T_ARR, T_SPLITR, N>> extends InternalNodeSpliterator<T, T_SPLITR, N> implements java.util.Spliterator.OfPrimitive<T, T_CONS, T_SPLITR> {
            public /* bridge */ /* synthetic */ java.util.Spliterator.OfPrimitive trySplit() {
                return (java.util.Spliterator.OfPrimitive) trySplit();
            }

            OfPrimitive(N cur) {
                super(cur);
            }

            public boolean tryAdvance(T_CONS consumer) {
                if (!initTryAdvance()) {
                    return false;
                }
                boolean hasNext = ((java.util.Spliterator.OfPrimitive) this.tryAdvanceSpliterator).tryAdvance(consumer);
                if (!hasNext) {
                    if (this.lastNodeSpliterator == null) {
                        java.util.stream.Node.OfPrimitive leaf = (java.util.stream.Node.OfPrimitive) findNextLeafNode(this.tryAdvanceStack);
                        if (leaf != null) {
                            this.tryAdvanceSpliterator = leaf.spliterator();
                            return ((java.util.Spliterator.OfPrimitive) this.tryAdvanceSpliterator).tryAdvance(consumer);
                        }
                    }
                    this.curNode = null;
                }
                return hasNext;
            }

            public void forEachRemaining(T_CONS consumer) {
                if (this.curNode != null) {
                    if (this.tryAdvanceSpliterator != null) {
                        while (tryAdvance(consumer)) {
                        }
                    } else if (this.lastNodeSpliterator == null) {
                        Deque<N> stack = initStack();
                        while (true) {
                            java.util.stream.Node.OfPrimitive leaf = (java.util.stream.Node.OfPrimitive) findNextLeafNode(stack);
                            if (leaf == null) {
                                break;
                            }
                            leaf.forEach(consumer);
                        }
                        this.curNode = null;
                    } else {
                        ((java.util.Spliterator.OfPrimitive) this.lastNodeSpliterator).forEachRemaining(consumer);
                    }
                }
            }
        }

        private static final class OfDouble extends OfPrimitive<Double, DoubleConsumer, double[], java.util.Spliterator.OfDouble, java.util.stream.Node.OfDouble> implements java.util.Spliterator.OfDouble {
            public /* bridge */ /* synthetic */ java.util.Spliterator.OfDouble trySplit() {
                return (java.util.Spliterator.OfDouble) trySplit();
            }

            OfDouble(java.util.stream.Node.OfDouble cur) {
                super(cur);
            }
        }

        private static final class OfInt extends OfPrimitive<Integer, IntConsumer, int[], java.util.Spliterator.OfInt, java.util.stream.Node.OfInt> implements java.util.Spliterator.OfInt {
            public /* bridge */ /* synthetic */ java.util.Spliterator.OfInt trySplit() {
                return (java.util.Spliterator.OfInt) trySplit();
            }

            OfInt(java.util.stream.Node.OfInt cur) {
                super(cur);
            }
        }

        private static final class OfLong extends OfPrimitive<Long, LongConsumer, long[], java.util.Spliterator.OfLong, java.util.stream.Node.OfLong> implements java.util.Spliterator.OfLong {
            public /* bridge */ /* synthetic */ java.util.Spliterator.OfLong trySplit() {
                return (java.util.Spliterator.OfLong) trySplit();
            }

            OfLong(java.util.stream.Node.OfLong cur) {
                super(cur);
            }
        }

        private static final class OfRef<T> extends InternalNodeSpliterator<T, Spliterator<T>, Node<T>> {
            OfRef(Node<T> curNode) {
                super(curNode);
            }

            public boolean tryAdvance(Consumer<? super T> consumer) {
                if (!initTryAdvance()) {
                    return false;
                }
                boolean hasNext = this.tryAdvanceSpliterator.tryAdvance(consumer);
                if (!hasNext) {
                    if (this.lastNodeSpliterator == null) {
                        Node<T> leaf = findNextLeafNode(this.tryAdvanceStack);
                        if (leaf != null) {
                            this.tryAdvanceSpliterator = leaf.spliterator();
                            return this.tryAdvanceSpliterator.tryAdvance(consumer);
                        }
                    }
                    this.curNode = null;
                }
                return hasNext;
            }

            public void forEachRemaining(Consumer<? super T> consumer) {
                if (this.curNode != null) {
                    if (this.tryAdvanceSpliterator != null) {
                        while (tryAdvance(consumer)) {
                        }
                    } else if (this.lastNodeSpliterator == null) {
                        Deque<Node<T>> stack = initStack();
                        while (true) {
                            Node<T> leaf = findNextLeafNode(stack);
                            if (leaf == null) {
                                break;
                            }
                            leaf.forEach(consumer);
                        }
                        this.curNode = null;
                    } else {
                        this.lastNodeSpliterator.forEachRemaining(consumer);
                    }
                }
            }
        }

        InternalNodeSpliterator(N curNode) {
            this.curNode = curNode;
        }

        protected final Deque<N> initStack() {
            Deque<N> stack = new ArrayDeque(8);
            for (int i = this.curNode.getChildCount() - 1; i >= this.curChildIndex; i--) {
                stack.addFirst(this.curNode.getChild(i));
            }
            return stack;
        }

        protected final N findNextLeafNode(Deque<N> stack) {
            while (true) {
                Node n = (Node) stack.pollFirst();
                if (n == null) {
                    return null;
                }
                if (n.getChildCount() != 0) {
                    for (int i = n.getChildCount() - 1; i >= 0; i--) {
                        stack.addFirst(n.getChild(i));
                    }
                } else if (n.count() > 0) {
                    return n;
                }
            }
        }

        protected final boolean initTryAdvance() {
            if (this.curNode == null) {
                return false;
            }
            if (this.tryAdvanceSpliterator == null) {
                if (this.lastNodeSpliterator == null) {
                    this.tryAdvanceStack = initStack();
                    N leaf = findNextLeafNode(this.tryAdvanceStack);
                    if (leaf != null) {
                        this.tryAdvanceSpliterator = leaf.spliterator();
                    } else {
                        this.curNode = null;
                        return false;
                    }
                }
                this.tryAdvanceSpliterator = this.lastNodeSpliterator;
            }
            return true;
        }

        public final S trySplit() {
            if (this.curNode == null || this.tryAdvanceSpliterator != null) {
                return null;
            }
            if (this.lastNodeSpliterator != null) {
                return this.lastNodeSpliterator.trySplit();
            }
            Node node;
            int i;
            if (this.curChildIndex < this.curNode.getChildCount() - 1) {
                node = this.curNode;
                i = this.curChildIndex;
                this.curChildIndex = i + 1;
                return node.getChild(i).spliterator();
            }
            this.curNode = this.curNode.getChild(this.curChildIndex);
            if (this.curNode.getChildCount() == 0) {
                this.lastNodeSpliterator = this.curNode.spliterator();
                return this.lastNodeSpliterator.trySplit();
            }
            this.curChildIndex = 0;
            node = this.curNode;
            i = this.curChildIndex;
            this.curChildIndex = i + 1;
            return node.getChild(i).spliterator();
        }

        public final long estimateSize() {
            if (this.curNode == null) {
                return 0;
            }
            if (this.lastNodeSpliterator != null) {
                return this.lastNodeSpliterator.estimateSize();
            }
            long size = 0;
            for (int i = this.curChildIndex; i < this.curNode.getChildCount(); i++) {
                size += this.curNode.getChild(i).count();
            }
            return size;
        }

        public final int characteristics() {
            return 64;
        }
    }

    private static class LongArrayNode implements OfLong {
        final long[] array;
        int curSize;

        LongArrayNode(long size) {
            if (size >= Nodes.MAX_ARRAY_SIZE) {
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            }
            this.array = new long[((int) size)];
            this.curSize = 0;
        }

        LongArrayNode(long[] array) {
            this.array = array;
            this.curSize = array.length;
        }

        public Spliterator.OfLong spliterator() {
            return Arrays.spliterator(this.array, 0, this.curSize);
        }

        public long[] asPrimitiveArray() {
            if (this.array.length == this.curSize) {
                return this.array;
            }
            return Arrays.copyOf(this.array, this.curSize);
        }

        public void copyInto(long[] dest, int destOffset) {
            System.arraycopy(this.array, 0, dest, destOffset, this.curSize);
        }

        public long count() {
            return (long) this.curSize;
        }

        public void forEach(LongConsumer consumer) {
            for (int i = 0; i < this.curSize; i++) {
                consumer.-java_util_stream_StreamSpliterators$LongWrappingSpliterator-mthref-1(this.array[i]);
            }
        }

        public String toString() {
            return String.format("LongArrayNode[%d][%s]", Integer.valueOf(this.array.length - this.curSize), Arrays.toString(this.array));
        }
    }

    private static final class LongFixedNodeBuilder extends LongArrayNode implements Builder.OfLong {
        static final /* synthetic */ boolean -assertionsDisabled = (LongFixedNodeBuilder.class.desiredAssertionStatus() ^ 1);

        LongFixedNodeBuilder(long size) {
            super(size);
            if (!-assertionsDisabled && size >= Nodes.MAX_ARRAY_SIZE) {
                throw new AssertionError();
            }
        }

        public OfLong build() {
            if (this.curSize >= this.array.length) {
                return this;
            }
            throw new IllegalStateException(String.format("Current size %d is less than fixed size %d", Integer.valueOf(this.curSize), Integer.valueOf(this.array.length)));
        }

        public void begin(long size) {
            if (size != ((long) this.array.length)) {
                throw new IllegalStateException(String.format("Begin size %d is not equal to fixed size %d", Long.valueOf(size), Integer.valueOf(this.array.length)));
            } else {
                this.curSize = 0;
            }
        }

        public void accept(long i) {
            if (this.curSize < this.array.length) {
                long[] jArr = this.array;
                int i2 = this.curSize;
                this.curSize = i2 + 1;
                jArr[i2] = i;
                return;
            }
            throw new IllegalStateException(String.format("Accept exceeded fixed size of %d", Integer.valueOf(this.array.length)));
        }

        public void end() {
            if (this.curSize < this.array.length) {
                throw new IllegalStateException(String.format("End size %d is less than fixed size %d", Integer.valueOf(this.curSize), Integer.valueOf(this.array.length)));
            }
        }

        public String toString() {
            return String.format("LongFixedNodeBuilder[%d][%s]", Integer.valueOf(this.array.length - this.curSize), Arrays.toString(this.array));
        }
    }

    private static final class LongSpinedNodeBuilder extends SpinedBuffer.OfLong implements OfLong, Builder.OfLong {
        static final /* synthetic */ boolean -assertionsDisabled = (LongSpinedNodeBuilder.class.desiredAssertionStatus() ^ 1);
        private boolean building = false;

        LongSpinedNodeBuilder() {
        }

        public Spliterator.OfLong spliterator() {
            if (-assertionsDisabled || !this.building) {
                return super.spliterator();
            }
            throw new AssertionError((Object) "during building");
        }

        public void forEach(LongConsumer consumer) {
            if (-assertionsDisabled || !this.building) {
                super.forEach((Object) consumer);
                return;
            }
            throw new AssertionError((Object) "during building");
        }

        public void begin(long size) {
            if (-assertionsDisabled || !this.building) {
                this.building = true;
                clear();
                ensureCapacity(size);
                return;
            }
            throw new AssertionError((Object) "was already building");
        }

        public void accept(long i) {
            if (-assertionsDisabled || this.building) {
                super.-java_util_stream_StreamSpliterators$LongWrappingSpliterator-mthref-0(i);
                return;
            }
            throw new AssertionError((Object) "not building");
        }

        public void end() {
            if (-assertionsDisabled || this.building) {
                this.building = false;
                return;
            }
            throw new AssertionError((Object) "was not building");
        }

        public void copyInto(long[] array, int offset) {
            if (-assertionsDisabled || !this.building) {
                super.copyInto(array, offset);
                return;
            }
            throw new AssertionError((Object) "during building");
        }

        public long[] asPrimitiveArray() {
            if (-assertionsDisabled || !this.building) {
                return (long[]) super.asPrimitiveArray();
            }
            throw new AssertionError((Object) "during building");
        }

        public OfLong build() {
            if (-assertionsDisabled || !this.building) {
                return this;
            }
            throw new AssertionError((Object) "during building");
        }
    }

    private static abstract class SizedCollectorTask<P_IN, P_OUT, T_SINK extends Sink<P_OUT>, K extends SizedCollectorTask<P_IN, P_OUT, T_SINK, K>> extends CountedCompleter<Void> implements Sink<P_OUT> {
        static final /* synthetic */ boolean -assertionsDisabled = (SizedCollectorTask.class.desiredAssertionStatus() ^ 1);
        protected int fence;
        protected final PipelineHelper<P_OUT> helper;
        protected int index;
        protected long length;
        protected long offset;
        protected final Spliterator<P_IN> spliterator;
        protected final long targetSize;

        static final class OfDouble<P_IN> extends SizedCollectorTask<P_IN, Double, java.util.stream.Sink.OfDouble, OfDouble<P_IN>> implements java.util.stream.Sink.OfDouble {
            private final double[] array;

            OfDouble(Spliterator<P_IN> spliterator, PipelineHelper<Double> helper, double[] array) {
                super(spliterator, helper, array.length);
                this.array = array;
            }

            OfDouble(OfDouble<P_IN> parent, Spliterator<P_IN> spliterator, long offset, long length) {
                super(parent, spliterator, offset, length, parent.array.length);
                this.array = parent.array;
            }

            OfDouble<P_IN> makeChild(Spliterator<P_IN> spliterator, long offset, long size) {
                return new OfDouble(this, spliterator, offset, size);
            }

            public void accept(double value) {
                if (this.index >= this.fence) {
                    throw new IndexOutOfBoundsException(Integer.toString(this.index));
                }
                double[] dArr = this.array;
                int i = this.index;
                this.index = i + 1;
                dArr[i] = value;
            }
        }

        static final class OfInt<P_IN> extends SizedCollectorTask<P_IN, Integer, java.util.stream.Sink.OfInt, OfInt<P_IN>> implements java.util.stream.Sink.OfInt {
            private final int[] array;

            OfInt(Spliterator<P_IN> spliterator, PipelineHelper<Integer> helper, int[] array) {
                super(spliterator, helper, array.length);
                this.array = array;
            }

            OfInt(OfInt<P_IN> parent, Spliterator<P_IN> spliterator, long offset, long length) {
                super(parent, spliterator, offset, length, parent.array.length);
                this.array = parent.array;
            }

            OfInt<P_IN> makeChild(Spliterator<P_IN> spliterator, long offset, long size) {
                return new OfInt(this, spliterator, offset, size);
            }

            public void accept(int value) {
                if (this.index >= this.fence) {
                    throw new IndexOutOfBoundsException(Integer.toString(this.index));
                }
                int[] iArr = this.array;
                int i = this.index;
                this.index = i + 1;
                iArr[i] = value;
            }
        }

        static final class OfLong<P_IN> extends SizedCollectorTask<P_IN, Long, java.util.stream.Sink.OfLong, OfLong<P_IN>> implements java.util.stream.Sink.OfLong {
            private final long[] array;

            OfLong(Spliterator<P_IN> spliterator, PipelineHelper<Long> helper, long[] array) {
                super(spliterator, helper, array.length);
                this.array = array;
            }

            OfLong(OfLong<P_IN> parent, Spliterator<P_IN> spliterator, long offset, long length) {
                super(parent, spliterator, offset, length, parent.array.length);
                this.array = parent.array;
            }

            OfLong<P_IN> makeChild(Spliterator<P_IN> spliterator, long offset, long size) {
                return new OfLong(this, spliterator, offset, size);
            }

            public void accept(long value) {
                if (this.index >= this.fence) {
                    throw new IndexOutOfBoundsException(Integer.toString(this.index));
                }
                long[] jArr = this.array;
                int i = this.index;
                this.index = i + 1;
                jArr[i] = value;
            }
        }

        static final class OfRef<P_IN, P_OUT> extends SizedCollectorTask<P_IN, P_OUT, Sink<P_OUT>, OfRef<P_IN, P_OUT>> implements Sink<P_OUT> {
            private final P_OUT[] array;

            OfRef(Spliterator<P_IN> spliterator, PipelineHelper<P_OUT> helper, P_OUT[] array) {
                super(spliterator, helper, array.length);
                this.array = array;
            }

            OfRef(OfRef<P_IN, P_OUT> parent, Spliterator<P_IN> spliterator, long offset, long length) {
                super(parent, spliterator, offset, length, parent.array.length);
                this.array = parent.array;
            }

            OfRef<P_IN, P_OUT> makeChild(Spliterator<P_IN> spliterator, long offset, long size) {
                return new OfRef(this, spliterator, offset, size);
            }

            public void accept(P_OUT value) {
                if (this.index >= this.fence) {
                    throw new IndexOutOfBoundsException(Integer.toString(this.index));
                }
                Object[] objArr = this.array;
                int i = this.index;
                this.index = i + 1;
                objArr[i] = value;
            }
        }

        abstract K makeChild(Spliterator<P_IN> spliterator, long j, long j2);

        SizedCollectorTask(Spliterator<P_IN> spliterator, PipelineHelper<P_OUT> helper, int arrayLength) {
            if (-assertionsDisabled || spliterator.hasCharacteristics(16384)) {
                this.spliterator = spliterator;
                this.helper = helper;
                this.targetSize = AbstractTask.suggestTargetSize(spliterator.estimateSize());
                this.offset = 0;
                this.length = (long) arrayLength;
                return;
            }
            throw new AssertionError();
        }

        SizedCollectorTask(K parent, Spliterator<P_IN> spliterator, long offset, long length, int arrayLength) {
            super(parent);
            if (-assertionsDisabled || spliterator.hasCharacteristics(16384)) {
                this.spliterator = spliterator;
                this.helper = parent.helper;
                this.targetSize = parent.targetSize;
                this.offset = offset;
                this.length = length;
                if (offset < 0 || length < 0 || (offset + length) - 1 >= ((long) arrayLength)) {
                    throw new IllegalArgumentException(String.format("offset and length interval [%d, %d + %d) is not within array size interval [0, %d)", Long.valueOf(offset), Long.valueOf(offset), Long.valueOf(length), Integer.valueOf(arrayLength)));
                }
                return;
            }
            throw new AssertionError();
        }

        public void compute() {
            T_SINK task = this;
            Spliterator<P_IN> rightSplit = this.spliterator;
            while (rightSplit.estimateSize() > task.targetSize) {
                Spliterator<P_IN> leftSplit = rightSplit.trySplit();
                if (leftSplit == null) {
                    break;
                }
                task.setPendingCount(1);
                long leftSplitSize = leftSplit.estimateSize();
                task.makeChild(leftSplit, task.offset, leftSplitSize).fork();
                task = task.makeChild(rightSplit, task.offset + leftSplitSize, task.length - leftSplitSize);
            }
            if (-assertionsDisabled || task.offset + task.length < Nodes.MAX_ARRAY_SIZE) {
                task.helper.wrapAndCopyInto(task, rightSplit);
                task.propagateCompletion();
                return;
            }
            throw new AssertionError();
        }

        public void begin(long size) {
            if (size > this.length) {
                throw new IllegalStateException("size passed to Sink.begin exceeds array length");
            }
            this.index = (int) this.offset;
            this.fence = this.index + ((int) this.length);
        }
    }

    private static final class SpinedNodeBuilder<T> extends SpinedBuffer<T> implements Node<T>, Builder<T> {
        static final /* synthetic */ boolean -assertionsDisabled = (SpinedNodeBuilder.class.desiredAssertionStatus() ^ 1);
        private boolean building = false;

        SpinedNodeBuilder() {
        }

        public Spliterator<T> spliterator() {
            if (-assertionsDisabled || !this.building) {
                return super.spliterator();
            }
            throw new AssertionError((Object) "during building");
        }

        public void forEach(Consumer<? super T> consumer) {
            if (-assertionsDisabled || !this.building) {
                super.forEach(consumer);
                return;
            }
            throw new AssertionError((Object) "during building");
        }

        public void begin(long size) {
            if (-assertionsDisabled || !this.building) {
                this.building = true;
                clear();
                ensureCapacity(size);
                return;
            }
            throw new AssertionError((Object) "was already building");
        }

        public void accept(T t) {
            if (-assertionsDisabled || this.building) {
                super.-java_util_stream_StreamSpliterators$WrappingSpliterator-mthref-0(t);
                return;
            }
            throw new AssertionError((Object) "not building");
        }

        public void end() {
            if (-assertionsDisabled || this.building) {
                this.building = false;
                return;
            }
            throw new AssertionError((Object) "was not building");
        }

        public void copyInto(T[] array, int offset) {
            if (-assertionsDisabled || !this.building) {
                super.copyInto(array, offset);
                return;
            }
            throw new AssertionError((Object) "during building");
        }

        public T[] asArray(IntFunction<T[]> arrayFactory) {
            if (-assertionsDisabled || !this.building) {
                return super.asArray(arrayFactory);
            }
            throw new AssertionError((Object) "during building");
        }

        public Node<T> build() {
            if (-assertionsDisabled || !this.building) {
                return this;
            }
            throw new AssertionError((Object) "during building");
        }
    }

    private static abstract class ToArrayTask<T, T_NODE extends Node<T>, K extends ToArrayTask<T, T_NODE, K>> extends CountedCompleter<Void> {
        protected final T_NODE node;
        protected final int offset;

        private static class OfPrimitive<T, T_CONS, T_ARR, T_SPLITR extends java.util.Spliterator.OfPrimitive<T, T_CONS, T_SPLITR>, T_NODE extends java.util.stream.Node.OfPrimitive<T, T_CONS, T_ARR, T_SPLITR, T_NODE>> extends ToArrayTask<T, T_NODE, OfPrimitive<T, T_CONS, T_ARR, T_SPLITR, T_NODE>> {
            private final T_ARR array;

            /* synthetic */ OfPrimitive(java.util.stream.Node.OfPrimitive node, Object array, int offset, OfPrimitive -this3) {
                this(node, array, offset);
            }

            private OfPrimitive(T_NODE node, T_ARR array, int offset) {
                super(node, offset);
                this.array = array;
            }

            private OfPrimitive(OfPrimitive<T, T_CONS, T_ARR, T_SPLITR, T_NODE> parent, T_NODE node, int offset) {
                super(parent, node, offset);
                this.array = parent.array;
            }

            OfPrimitive<T, T_CONS, T_ARR, T_SPLITR, T_NODE> makeChild(int childIndex, int offset) {
                return new OfPrimitive(this, ((java.util.stream.Node.OfPrimitive) this.node).getChild(childIndex), offset);
            }

            void copyNodeToArray() {
                ((java.util.stream.Node.OfPrimitive) this.node).copyInto(this.array, this.offset);
            }
        }

        private static final class OfDouble extends OfPrimitive<Double, DoubleConsumer, double[], java.util.Spliterator.OfDouble, java.util.stream.Node.OfDouble> {
            /* synthetic */ OfDouble(java.util.stream.Node.OfDouble node, double[] array, int offset, OfDouble -this3) {
                this(node, array, offset);
            }

            private OfDouble(java.util.stream.Node.OfDouble node, double[] array, int offset) {
                super(node, array, offset, null);
            }
        }

        private static final class OfInt extends OfPrimitive<Integer, IntConsumer, int[], java.util.Spliterator.OfInt, java.util.stream.Node.OfInt> {
            /* synthetic */ OfInt(java.util.stream.Node.OfInt node, int[] array, int offset, OfInt -this3) {
                this(node, array, offset);
            }

            private OfInt(java.util.stream.Node.OfInt node, int[] array, int offset) {
                super(node, array, offset, null);
            }
        }

        private static final class OfLong extends OfPrimitive<Long, LongConsumer, long[], java.util.Spliterator.OfLong, java.util.stream.Node.OfLong> {
            /* synthetic */ OfLong(java.util.stream.Node.OfLong node, long[] array, int offset, OfLong -this3) {
                this(node, array, offset);
            }

            private OfLong(java.util.stream.Node.OfLong node, long[] array, int offset) {
                super(node, array, offset, null);
            }
        }

        private static final class OfRef<T> extends ToArrayTask<T, Node<T>, OfRef<T>> {
            private final T[] array;

            /* synthetic */ OfRef(Node node, Object[] array, int offset, OfRef -this3) {
                this(node, array, offset);
            }

            private OfRef(Node<T> node, T[] array, int offset) {
                super(node, offset);
                this.array = array;
            }

            private OfRef(OfRef<T> parent, Node<T> node, int offset) {
                super(parent, node, offset);
                this.array = parent.array;
            }

            OfRef<T> makeChild(int childIndex, int offset) {
                return new OfRef(this, this.node.getChild(childIndex), offset);
            }

            void copyNodeToArray() {
                this.node.copyInto(this.array, this.offset);
            }
        }

        abstract void copyNodeToArray();

        abstract K makeChild(int i, int i2);

        ToArrayTask(T_NODE node, int offset) {
            this.node = node;
            this.offset = offset;
        }

        ToArrayTask(K parent, T_NODE node, int offset) {
            super(parent);
            this.node = node;
            this.offset = offset;
        }

        public void compute() {
            ToArrayTask<T, T_NODE, K> task = this;
            while (task.node.getChildCount() != 0) {
                task.setPendingCount(task.node.getChildCount() - 1);
                int size = 0;
                int i = 0;
                while (i < task.node.getChildCount() - 1) {
                    K leftTask = task.makeChild(i, task.offset + size);
                    size = (int) (((long) size) + leftTask.node.count());
                    leftTask.fork();
                    i++;
                }
                task = task.makeChild(i, task.offset + size);
            }
            task.copyNodeToArray();
            task.propagateCompletion();
        }
    }

    private static /* synthetic */ int[] -getjava-util-stream-StreamShapeSwitchesValues() {
        if (-java-util-stream-StreamShapeSwitchesValues != null) {
            return -java-util-stream-StreamShapeSwitchesValues;
        }
        int[] iArr = new int[StreamShape.values().length];
        try {
            iArr[StreamShape.DOUBLE_VALUE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[StreamShape.INT_VALUE.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[StreamShape.LONG_VALUE.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[StreamShape.REFERENCE.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        -java-util-stream-StreamShapeSwitchesValues = iArr;
        return iArr;
    }

    private Nodes() {
        throw new Error("no instances");
    }

    static <T> Node<T> emptyNode(StreamShape shape) {
        switch (-getjava-util-stream-StreamShapeSwitchesValues()[shape.ordinal()]) {
            case 1:
                return EMPTY_DOUBLE_NODE;
            case 2:
                return EMPTY_INT_NODE;
            case 3:
                return EMPTY_LONG_NODE;
            case 4:
                return EMPTY_NODE;
            default:
                throw new IllegalStateException("Unknown shape " + shape);
        }
    }

    static <T> Node<T> conc(StreamShape shape, Node<T> left, Node<T> right) {
        switch (-getjava-util-stream-StreamShapeSwitchesValues()[shape.ordinal()]) {
            case 1:
                return new OfDouble((OfDouble) left, (OfDouble) right);
            case 2:
                return new OfInt((OfInt) left, (OfInt) right);
            case 3:
                return new OfLong((OfLong) left, (OfLong) right);
            case 4:
                return new ConcNode(left, right);
            default:
                throw new IllegalStateException("Unknown shape " + shape);
        }
    }

    static <T> Node<T> node(T[] array) {
        return new ArrayNode(array);
    }

    static <T> Node<T> node(Collection<T> c) {
        return new CollectionNode(c);
    }

    static <T> Builder<T> builder(long exactSizeIfKnown, IntFunction<T[]> generator) {
        if (exactSizeIfKnown < 0 || exactSizeIfKnown >= MAX_ARRAY_SIZE) {
            return builder();
        }
        return new FixedNodeBuilder(exactSizeIfKnown, generator);
    }

    static <T> Builder<T> builder() {
        return new SpinedNodeBuilder();
    }

    static OfInt node(int[] array) {
        return new IntArrayNode(array);
    }

    static Builder.OfInt intBuilder(long exactSizeIfKnown) {
        if (exactSizeIfKnown < 0 || exactSizeIfKnown >= MAX_ARRAY_SIZE) {
            return intBuilder();
        }
        return new IntFixedNodeBuilder(exactSizeIfKnown);
    }

    static Builder.OfInt intBuilder() {
        return new IntSpinedNodeBuilder();
    }

    static OfLong node(long[] array) {
        return new LongArrayNode(array);
    }

    static Builder.OfLong longBuilder(long exactSizeIfKnown) {
        if (exactSizeIfKnown < 0 || exactSizeIfKnown >= MAX_ARRAY_SIZE) {
            return longBuilder();
        }
        return new LongFixedNodeBuilder(exactSizeIfKnown);
    }

    static Builder.OfLong longBuilder() {
        return new LongSpinedNodeBuilder();
    }

    static OfDouble node(double[] array) {
        return new DoubleArrayNode(array);
    }

    static Builder.OfDouble doubleBuilder(long exactSizeIfKnown) {
        if (exactSizeIfKnown < 0 || exactSizeIfKnown >= MAX_ARRAY_SIZE) {
            return doubleBuilder();
        }
        return new DoubleFixedNodeBuilder(exactSizeIfKnown);
    }

    static Builder.OfDouble doubleBuilder() {
        return new DoubleSpinedNodeBuilder();
    }

    public static <P_IN, P_OUT> Node<P_OUT> collect(PipelineHelper<P_OUT> helper, Spliterator<P_IN> spliterator, boolean flattenTree, IntFunction<P_OUT[]> generator) {
        long size = helper.exactOutputSizeIfKnown(spliterator);
        if (size < 0 || !spliterator.hasCharacteristics(16384)) {
            Node<P_OUT> node = (Node) new OfRef(helper, generator, spliterator).invoke();
            if (flattenTree) {
                node = flatten(node, generator);
            }
            return node;
        } else if (size >= MAX_ARRAY_SIZE) {
            throw new IllegalArgumentException(BAD_SIZE);
        } else {
            Object[] array = (Object[]) generator.apply((int) size);
            new OfRef(spliterator, helper, array).invoke();
            return node(array);
        }
    }

    public static <P_IN> OfInt collectInt(PipelineHelper<Integer> helper, Spliterator<P_IN> spliterator, boolean flattenTree) {
        long size = helper.exactOutputSizeIfKnown(spliterator);
        if (size < 0 || !spliterator.hasCharacteristics(16384)) {
            OfInt node = (OfInt) new OfInt(helper, spliterator).invoke();
            if (flattenTree) {
                node = flattenInt(node);
            }
            return node;
        } else if (size >= MAX_ARRAY_SIZE) {
            throw new IllegalArgumentException(BAD_SIZE);
        } else {
            int[] array = new int[((int) size)];
            new OfInt(spliterator, helper, array).invoke();
            return node(array);
        }
    }

    public static <P_IN> OfLong collectLong(PipelineHelper<Long> helper, Spliterator<P_IN> spliterator, boolean flattenTree) {
        long size = helper.exactOutputSizeIfKnown(spliterator);
        if (size < 0 || !spliterator.hasCharacteristics(16384)) {
            OfLong node = (OfLong) new OfLong(helper, spliterator).invoke();
            if (flattenTree) {
                node = flattenLong(node);
            }
            return node;
        } else if (size >= MAX_ARRAY_SIZE) {
            throw new IllegalArgumentException(BAD_SIZE);
        } else {
            long[] array = new long[((int) size)];
            new OfLong(spliterator, helper, array).invoke();
            return node(array);
        }
    }

    public static <P_IN> OfDouble collectDouble(PipelineHelper<Double> helper, Spliterator<P_IN> spliterator, boolean flattenTree) {
        long size = helper.exactOutputSizeIfKnown(spliterator);
        if (size < 0 || !spliterator.hasCharacteristics(16384)) {
            OfDouble node = (OfDouble) new OfDouble(helper, spliterator).invoke();
            if (flattenTree) {
                node = flattenDouble(node);
            }
            return node;
        } else if (size >= MAX_ARRAY_SIZE) {
            throw new IllegalArgumentException(BAD_SIZE);
        } else {
            double[] array = new double[((int) size)];
            new OfDouble(spliterator, helper, array).invoke();
            return node(array);
        }
    }

    public static <T> Node<T> flatten(Node<T> node, IntFunction<T[]> generator) {
        if (node.getChildCount() <= 0) {
            return node;
        }
        long size = node.count();
        if (size >= MAX_ARRAY_SIZE) {
            throw new IllegalArgumentException(BAD_SIZE);
        }
        Object[] array = (Object[]) generator.apply((int) size);
        new OfRef(node, array, 0, null).invoke();
        return node(array);
    }

    public static OfInt flattenInt(OfInt node) {
        if (node.getChildCount() <= 0) {
            return node;
        }
        long size = node.count();
        if (size >= MAX_ARRAY_SIZE) {
            throw new IllegalArgumentException(BAD_SIZE);
        }
        int[] array = new int[((int) size)];
        new OfInt(node, array, 0, null).invoke();
        return node(array);
    }

    public static OfLong flattenLong(OfLong node) {
        if (node.getChildCount() <= 0) {
            return node;
        }
        long size = node.count();
        if (size >= MAX_ARRAY_SIZE) {
            throw new IllegalArgumentException(BAD_SIZE);
        }
        long[] array = new long[((int) size)];
        new OfLong(node, array, 0, null).invoke();
        return node(array);
    }

    public static OfDouble flattenDouble(OfDouble node) {
        if (node.getChildCount() <= 0) {
            return node;
        }
        long size = node.count();
        if (size >= MAX_ARRAY_SIZE) {
            throw new IllegalArgumentException(BAD_SIZE);
        }
        double[] array = new double[((int) size)];
        new OfDouble(node, array, 0, null).invoke();
        return node(array);
    }
}
