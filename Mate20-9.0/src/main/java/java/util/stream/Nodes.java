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
import java.util.stream.Node;
import java.util.stream.Sink;
import java.util.stream.SpinedBuffer;

final class Nodes {
    static final String BAD_SIZE = "Stream size exceeds max array size";
    /* access modifiers changed from: private */
    public static final double[] EMPTY_DOUBLE_ARRAY = new double[0];
    private static final Node.OfDouble EMPTY_DOUBLE_NODE = new EmptyNode.OfDouble();
    /* access modifiers changed from: private */
    public static final int[] EMPTY_INT_ARRAY = new int[0];
    private static final Node.OfInt EMPTY_INT_NODE = new EmptyNode.OfInt();
    /* access modifiers changed from: private */
    public static final long[] EMPTY_LONG_ARRAY = new long[0];
    private static final Node.OfLong EMPTY_LONG_NODE = new EmptyNode.OfLong();
    private static final Node EMPTY_NODE = new EmptyNode.OfRef();
    static final long MAX_ARRAY_SIZE = 2147483639;

    private static abstract class AbstractConcNode<T, T_NODE extends Node<T>> implements Node<T> {
        protected final T_NODE left;
        protected final T_NODE right;
        private final long size;

        AbstractConcNode(T_NODE left2, T_NODE right2) {
            this.left = left2;
            this.right = right2;
            this.size = left2.count() + right2.count();
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
            if (size < Nodes.MAX_ARRAY_SIZE) {
                this.array = (Object[]) generator.apply((int) size);
                this.curSize = 0;
                return;
            }
            throw new IllegalArgumentException(Nodes.BAD_SIZE);
        }

        ArrayNode(T[] array2) {
            this.array = array2;
            this.curSize = array2.length;
        }

        public Spliterator<T> spliterator() {
            return Arrays.spliterator(this.array, 0, this.curSize);
        }

        public void copyInto(T[] dest, int destOffset) {
            System.arraycopy((Object) this.array, 0, (Object) dest, destOffset, this.curSize);
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
                consumer.accept(this.array[i]);
            }
        }

        public String toString() {
            return String.format("ArrayNode[%d][%s]", Integer.valueOf(this.array.length - this.curSize), Arrays.toString((Object[]) this.array));
        }
    }

    private static final class CollectionNode<T> implements Node<T> {
        private final Collection<T> c;

        CollectionNode(Collection<T> c2) {
            this.c = c2;
        }

        public Spliterator<T> spliterator() {
            return this.c.stream().spliterator();
        }

        public void copyInto(T[] array, int offset) {
            for (T t : this.c) {
                array[offset] = t;
                offset++;
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

    private static class CollectorTask<P_IN, P_OUT, T_NODE extends Node<P_OUT>, T_BUILDER extends Node.Builder<P_OUT>> extends AbstractTask<P_IN, P_OUT, T_NODE, CollectorTask<P_IN, P_OUT, T_NODE, T_BUILDER>> {
        protected final LongFunction<T_BUILDER> builderFactory;
        protected final BinaryOperator<T_NODE> concFactory;
        protected final PipelineHelper<P_OUT> helper;

        private static final class OfDouble<P_IN> extends CollectorTask<P_IN, Double, Node.OfDouble, Node.Builder.OfDouble> {
            OfDouble(PipelineHelper<Double> helper, Spliterator<P_IN> spliterator) {
                super(helper, spliterator, $$Lambda$LfPL0444L8HcP6gPtdKqQiCTSfM.INSTANCE, $$Lambda$KTexUmxMdHIv08L4oU8j9HXK_go.INSTANCE);
            }
        }

        private static final class OfInt<P_IN> extends CollectorTask<P_IN, Integer, Node.OfInt, Node.Builder.OfInt> {
            OfInt(PipelineHelper<Integer> helper, Spliterator<P_IN> spliterator) {
                super(helper, spliterator, $$Lambda$B6rBjxAejI5kqKK9J3AHwY_L9ag.INSTANCE, $$Lambda$O4iFzVwtlyKFZkWcnfXHIHbxaTY.INSTANCE);
            }
        }

        private static final class OfLong<P_IN> extends CollectorTask<P_IN, Long, Node.OfLong, Node.Builder.OfLong> {
            OfLong(PipelineHelper<Long> helper, Spliterator<P_IN> spliterator) {
                super(helper, spliterator, $$Lambda$8ABiL5PN53c8rr14_yI2_4o5Zlo.INSTANCE, $$Lambda$eeRvX3cGN3C3qCAoKtOxCHIW8Lo.INSTANCE);
            }
        }

        private static final class OfRef<P_IN, P_OUT> extends CollectorTask<P_IN, P_OUT, Node<P_OUT>, Node.Builder<P_OUT>> {
            OfRef(PipelineHelper<P_OUT> helper, IntFunction<P_OUT[]> generator, Spliterator<P_IN> spliterator) {
                super(helper, spliterator, 
                /*  JADX ERROR: Method code generation error
                    jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0007: CONSTRUCTOR  (r3v0 'helper' java.util.stream.PipelineHelper<P_OUT>), (r5v0 'spliterator' java.util.Spliterator<P_IN>), (wrap: java.util.stream.-$$Lambda$Nodes$CollectorTask$OfRef$Zd2fdoB-mZW0DbPHybIpYjf-Pyo
                      0x0002: CONSTRUCTOR  (r0v0 java.util.stream.-$$Lambda$Nodes$CollectorTask$OfRef$Zd2fdoB-mZW0DbPHybIpYjf-Pyo) = (r4v0 'generator' java.util.function.IntFunction<P_OUT[]>) java.util.stream.-$$Lambda$Nodes$CollectorTask$OfRef$Zd2fdoB-mZW0DbPHybIpYjf-Pyo.<init>(java.util.function.IntFunction):void CONSTRUCTOR), (wrap: java.util.stream.-$$Lambda$Mo9-ryI3XUGyoHfpnRL3BoFhaqY
                      0x0005: SGET  (r1v0 java.util.stream.-$$Lambda$Mo9-ryI3XUGyoHfpnRL3BoFhaqY) =  java.util.stream.-$$Lambda$Mo9-ryI3XUGyoHfpnRL3BoFhaqY.INSTANCE java.util.stream.-$$Lambda$Mo9-ryI3XUGyoHfpnRL3BoFhaqY) java.util.stream.Nodes.CollectorTask.<init>(java.util.stream.PipelineHelper, java.util.Spliterator, java.util.function.LongFunction, java.util.function.BinaryOperator):void SUPER in method: java.util.stream.Nodes.CollectorTask.OfRef.<init>(java.util.stream.PipelineHelper, java.util.function.IntFunction, java.util.Spliterator):void, dex: boot_classes.dex
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:210)
                    	at jadx.core.codegen.RegionGen.makeSimpleBlock(RegionGen.java:109)
                    	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:55)
                    	at jadx.core.codegen.RegionGen.makeSimpleRegion(RegionGen.java:92)
                    	at jadx.core.codegen.RegionGen.makeRegion(RegionGen.java:58)
                    	at jadx.core.codegen.MethodGen.addRegionInsns(MethodGen.java:211)
                    	at jadx.core.codegen.MethodGen.addInstructions(MethodGen.java:204)
                    	at jadx.core.codegen.ClassGen.addMethod(ClassGen.java:317)
                    	at jadx.core.codegen.ClassGen.addMethods(ClassGen.java:263)
                    	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:226)
                    	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
                    	at jadx.core.codegen.ClassGen.addInnerClasses(ClassGen.java:238)
                    	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:225)
                    	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
                    	at jadx.core.codegen.ClassGen.addInnerClasses(ClassGen.java:238)
                    	at jadx.core.codegen.ClassGen.addClassBody(ClassGen.java:225)
                    	at jadx.core.codegen.ClassGen.addClassCode(ClassGen.java:111)
                    	at jadx.core.codegen.ClassGen.makeClass(ClassGen.java:77)
                    	at jadx.core.codegen.CodeGen.wrapCodeGen(CodeGen.java:44)
                    	at jadx.core.codegen.CodeGen.generateJavaCode(CodeGen.java:33)
                    	at jadx.core.codegen.CodeGen.generate(CodeGen.java:21)
                    	at jadx.core.ProcessClass.generateCode(ProcessClass.java:61)
                    	at jadx.core.dex.nodes.ClassNode.decompile(ClassNode.java:273)
                    Caused by: jadx.core.utils.exceptions.CodegenException: Error generate insn: 0x0002: CONSTRUCTOR  (r0v0 java.util.stream.-$$Lambda$Nodes$CollectorTask$OfRef$Zd2fdoB-mZW0DbPHybIpYjf-Pyo) = (r4v0 'generator' java.util.function.IntFunction<P_OUT[]>) java.util.stream.-$$Lambda$Nodes$CollectorTask$OfRef$Zd2fdoB-mZW0DbPHybIpYjf-Pyo.<init>(java.util.function.IntFunction):void CONSTRUCTOR in method: java.util.stream.Nodes.CollectorTask.OfRef.<init>(java.util.stream.PipelineHelper, java.util.function.IntFunction, java.util.Spliterator):void, dex: boot_classes.dex
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:245)
                    	at jadx.core.codegen.InsnGen.addArg(InsnGen.java:108)
                    	at jadx.core.codegen.InsnGen.generateMethodArguments(InsnGen.java:776)
                    	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:629)
                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:239)
                    	... 23 more
                    Caused by: jadx.core.utils.exceptions.JadxRuntimeException: Expected class to be processed at this point, class: java.util.stream.-$$Lambda$Nodes$CollectorTask$OfRef$Zd2fdoB-mZW0DbPHybIpYjf-Pyo, state: NOT_LOADED
                    	at jadx.core.dex.nodes.ClassNode.ensureProcessed(ClassNode.java:260)
                    	at jadx.core.codegen.InsnGen.makeConstructor(InsnGen.java:595)
                    	at jadx.core.codegen.InsnGen.makeInsnBody(InsnGen.java:353)
                    	at jadx.core.codegen.InsnGen.makeInsn(InsnGen.java:220)
                    	... 28 more
                    */
                /*
                    this = this;
                    java.util.stream.-$$Lambda$Nodes$CollectorTask$OfRef$Zd2fdoB-mZW0DbPHybIpYjf-Pyo r0 = new java.util.stream.-$$Lambda$Nodes$CollectorTask$OfRef$Zd2fdoB-mZW0DbPHybIpYjf-Pyo
                    r0.<init>(r4)
                    java.util.stream.-$$Lambda$Mo9-ryI3XUGyoHfpnRL3BoFhaqY r1 = java.util.stream.$$Lambda$Mo9ryI3XUGyoHfpnRL3BoFhaqY.INSTANCE
                    r2.<init>(r3, r5, r0, r1)
                    return
                */
                throw new UnsupportedOperationException("Method not decompiled: java.util.stream.Nodes.CollectorTask.OfRef.<init>(java.util.stream.PipelineHelper, java.util.function.IntFunction, java.util.Spliterator):void");
            }
        }

        CollectorTask(PipelineHelper<P_OUT> helper2, Spliterator<P_IN> spliterator, LongFunction<T_BUILDER> builderFactory2, BinaryOperator<T_NODE> concFactory2) {
            super(helper2, spliterator);
            this.helper = helper2;
            this.builderFactory = builderFactory2;
            this.concFactory = concFactory2;
        }

        CollectorTask(CollectorTask<P_IN, P_OUT, T_NODE, T_BUILDER> parent, Spliterator<P_IN> spliterator) {
            super(parent, spliterator);
            this.helper = parent.helper;
            this.builderFactory = parent.builderFactory;
            this.concFactory = parent.concFactory;
        }

        /* access modifiers changed from: protected */
        public CollectorTask<P_IN, P_OUT, T_NODE, T_BUILDER> makeChild(Spliterator<P_IN> spliterator) {
            return new CollectorTask<>(this, spliterator);
        }

        /* access modifiers changed from: protected */
        public T_NODE doLeaf() {
            return ((Node.Builder) this.helper.wrapAndCopyInto((Node.Builder) this.builderFactory.apply(this.helper.exactOutputSizeIfKnown(this.spliterator)), this.spliterator)).build();
        }

        public void onCompletion(CountedCompleter<?> caller) {
            if (!isLeaf()) {
                setLocalResult((Node) this.concFactory.apply((Node) ((CollectorTask) this.leftChild).getLocalResult(), (Node) ((CollectorTask) this.rightChild).getLocalResult()));
            }
            super.onCompletion(caller);
        }
    }

    static final class ConcNode<T> extends AbstractConcNode<T, Node<T>> implements Node<T> {

        static final class OfDouble extends OfPrimitive<Double, DoubleConsumer, double[], Spliterator.OfDouble, Node.OfDouble> implements Node.OfDouble {
            OfDouble(Node.OfDouble left, Node.OfDouble right) {
                super(left, right);
            }

            public Spliterator.OfDouble spliterator() {
                return new InternalNodeSpliterator.OfDouble(this);
            }
        }

        static final class OfInt extends OfPrimitive<Integer, IntConsumer, int[], Spliterator.OfInt, Node.OfInt> implements Node.OfInt {
            OfInt(Node.OfInt left, Node.OfInt right) {
                super(left, right);
            }

            public Spliterator.OfInt spliterator() {
                return new InternalNodeSpliterator.OfInt(this);
            }
        }

        static final class OfLong extends OfPrimitive<Long, LongConsumer, long[], Spliterator.OfLong, Node.OfLong> implements Node.OfLong {
            OfLong(Node.OfLong left, Node.OfLong right) {
                super(left, right);
            }

            public Spliterator.OfLong spliterator() {
                return new InternalNodeSpliterator.OfLong(this);
            }
        }

        private static abstract class OfPrimitive<E, T_CONS, T_ARR, T_SPLITR extends Spliterator.OfPrimitive<E, T_CONS, T_SPLITR>, T_NODE extends Node.OfPrimitive<E, T_CONS, T_ARR, T_SPLITR, T_NODE>> extends AbstractConcNode<E, T_NODE> implements Node.OfPrimitive<E, T_CONS, T_ARR, T_SPLITR, T_NODE> {
            public /* bridge */ /* synthetic */ Node.OfPrimitive getChild(int i) {
                return (Node.OfPrimitive) super.getChild(i);
            }

            OfPrimitive(T_NODE left, T_NODE right) {
                super(left, right);
            }

            public void forEach(T_CONS consumer) {
                ((Node.OfPrimitive) this.left).forEach(consumer);
                ((Node.OfPrimitive) this.right).forEach(consumer);
            }

            public void copyInto(T_ARR array, int offset) {
                ((Node.OfPrimitive) this.left).copyInto(array, offset);
                ((Node.OfPrimitive) this.right).copyInto(array, ((int) ((Node.OfPrimitive) this.left).count()) + offset);
            }

            public T_ARR asPrimitiveArray() {
                long size = count();
                if (size < Nodes.MAX_ARRAY_SIZE) {
                    T_ARR array = newArray((int) size);
                    copyInto(array, 0);
                    return array;
                }
                throw new IllegalArgumentException(Nodes.BAD_SIZE);
            }

            public String toString() {
                if (count() < 32) {
                    return String.format("%s[%s.%s]", getClass().getName(), this.left, this.right);
                }
                return String.format("%s[size=%d]", getClass().getName(), Long.valueOf(count()));
            }
        }

        ConcNode(Node<T> left, Node<T> right) {
            super(left, right);
        }

        public Spliterator<T> spliterator() {
            return new InternalNodeSpliterator.OfRef(this);
        }

        public void copyInto(T[] array, int offset) {
            Objects.requireNonNull(array);
            this.left.copyInto(array, offset);
            this.right.copyInto(array, ((int) this.left.count()) + offset);
        }

        public T[] asArray(IntFunction<T[]> generator) {
            long size = count();
            if (size < Nodes.MAX_ARRAY_SIZE) {
                T[] array = (Object[]) generator.apply((int) size);
                copyInto(array, 0);
                return array;
            }
            throw new IllegalArgumentException(Nodes.BAD_SIZE);
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

    private static class DoubleArrayNode implements Node.OfDouble {
        final double[] array;
        int curSize;

        DoubleArrayNode(long size) {
            if (size < Nodes.MAX_ARRAY_SIZE) {
                this.array = new double[((int) size)];
                this.curSize = 0;
                return;
            }
            throw new IllegalArgumentException(Nodes.BAD_SIZE);
        }

        DoubleArrayNode(double[] array2) {
            this.array = array2;
            this.curSize = array2.length;
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
            System.arraycopy((Object) this.array, 0, (Object) dest, destOffset, this.curSize);
        }

        public long count() {
            return (long) this.curSize;
        }

        public void forEach(DoubleConsumer consumer) {
            for (int i = 0; i < this.curSize; i++) {
                consumer.accept(this.array[i]);
            }
        }

        public String toString() {
            return String.format("DoubleArrayNode[%d][%s]", Integer.valueOf(this.array.length - this.curSize), Arrays.toString(this.array));
        }
    }

    private static final class DoubleFixedNodeBuilder extends DoubleArrayNode implements Node.Builder.OfDouble {
        static final /* synthetic */ boolean $assertionsDisabled = false;

        static {
            Class<Nodes> cls = Nodes.class;
        }

        DoubleFixedNodeBuilder(long size) {
            super(size);
        }

        public Node.OfDouble build() {
            if (this.curSize >= this.array.length) {
                return this;
            }
            throw new IllegalStateException(String.format("Current size %d is less than fixed size %d", Integer.valueOf(this.curSize), Integer.valueOf(this.array.length)));
        }

        public void begin(long size) {
            if (size == ((long) this.array.length)) {
                this.curSize = 0;
            } else {
                throw new IllegalStateException(String.format("Begin size %d is not equal to fixed size %d", Long.valueOf(size), Integer.valueOf(this.array.length)));
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

    private static final class DoubleSpinedNodeBuilder extends SpinedBuffer.OfDouble implements Node.OfDouble, Node.Builder.OfDouble {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private boolean building = false;

        static {
            Class<Nodes> cls = Nodes.class;
        }

        DoubleSpinedNodeBuilder() {
        }

        public Spliterator.OfDouble spliterator() {
            return super.spliterator();
        }

        public void forEach(DoubleConsumer consumer) {
            super.forEach(consumer);
        }

        public void begin(long size) {
            this.building = true;
            clear();
            ensureCapacity(size);
        }

        public void accept(double i) {
            super.accept(i);
        }

        public void end() {
            this.building = false;
        }

        public void copyInto(double[] array, int offset) {
            super.copyInto(array, offset);
        }

        public double[] asPrimitiveArray() {
            return (double[]) super.asPrimitiveArray();
        }

        public Node.OfDouble build() {
            return this;
        }
    }

    private static abstract class EmptyNode<T, T_ARR, T_CONS> implements Node<T> {

        private static final class OfDouble extends EmptyNode<Double, double[], DoubleConsumer> implements Node.OfDouble {
            OfDouble() {
            }

            public Spliterator.OfDouble spliterator() {
                return Spliterators.emptyDoubleSpliterator();
            }

            public double[] asPrimitiveArray() {
                return Nodes.EMPTY_DOUBLE_ARRAY;
            }
        }

        private static final class OfInt extends EmptyNode<Integer, int[], IntConsumer> implements Node.OfInt {
            OfInt() {
            }

            public Spliterator.OfInt spliterator() {
                return Spliterators.emptyIntSpliterator();
            }

            public int[] asPrimitiveArray() {
                return Nodes.EMPTY_INT_ARRAY;
            }
        }

        private static final class OfLong extends EmptyNode<Long, long[], LongConsumer> implements Node.OfLong {
            OfLong() {
            }

            public Spliterator.OfLong spliterator() {
                return Spliterators.emptyLongSpliterator();
            }

            public long[] asPrimitiveArray() {
                return Nodes.EMPTY_LONG_ARRAY;
            }
        }

        private static class OfRef<T> extends EmptyNode<T, T[], Consumer<? super T>> {
            public /* bridge */ /* synthetic */ void copyInto(Object[] objArr, int i) {
                super.copyInto(objArr, i);
            }

            public /* bridge */ /* synthetic */ void forEach(Consumer consumer) {
                super.forEach(consumer);
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

    private static final class FixedNodeBuilder<T> extends ArrayNode<T> implements Node.Builder<T> {
        static final /* synthetic */ boolean $assertionsDisabled = false;

        static {
            Class<Nodes> cls = Nodes.class;
        }

        FixedNodeBuilder(long size, IntFunction<T[]> generator) {
            super(size, generator);
        }

        public Node<T> build() {
            if (this.curSize >= this.array.length) {
                return this;
            }
            throw new IllegalStateException(String.format("Current size %d is less than fixed size %d", Integer.valueOf(this.curSize), Integer.valueOf(this.array.length)));
        }

        public void begin(long size) {
            if (size == ((long) this.array.length)) {
                this.curSize = 0;
            } else {
                throw new IllegalStateException(String.format("Begin size %d is not equal to fixed size %d", Long.valueOf(size), Integer.valueOf(this.array.length)));
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

    private static class IntArrayNode implements Node.OfInt {
        final int[] array;
        int curSize;

        IntArrayNode(long size) {
            if (size < Nodes.MAX_ARRAY_SIZE) {
                this.array = new int[((int) size)];
                this.curSize = 0;
                return;
            }
            throw new IllegalArgumentException(Nodes.BAD_SIZE);
        }

        IntArrayNode(int[] array2) {
            this.array = array2;
            this.curSize = array2.length;
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
            System.arraycopy((Object) this.array, 0, (Object) dest, destOffset, this.curSize);
        }

        public long count() {
            return (long) this.curSize;
        }

        public void forEach(IntConsumer consumer) {
            for (int i = 0; i < this.curSize; i++) {
                consumer.accept(this.array[i]);
            }
        }

        public String toString() {
            return String.format("IntArrayNode[%d][%s]", Integer.valueOf(this.array.length - this.curSize), Arrays.toString(this.array));
        }
    }

    private static final class IntFixedNodeBuilder extends IntArrayNode implements Node.Builder.OfInt {
        static final /* synthetic */ boolean $assertionsDisabled = false;

        static {
            Class<Nodes> cls = Nodes.class;
        }

        IntFixedNodeBuilder(long size) {
            super(size);
        }

        public Node.OfInt build() {
            if (this.curSize >= this.array.length) {
                return this;
            }
            throw new IllegalStateException(String.format("Current size %d is less than fixed size %d", Integer.valueOf(this.curSize), Integer.valueOf(this.array.length)));
        }

        public void begin(long size) {
            if (size == ((long) this.array.length)) {
                this.curSize = 0;
            } else {
                throw new IllegalStateException(String.format("Begin size %d is not equal to fixed size %d", Long.valueOf(size), Integer.valueOf(this.array.length)));
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

    private static final class IntSpinedNodeBuilder extends SpinedBuffer.OfInt implements Node.OfInt, Node.Builder.OfInt {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private boolean building = false;

        static {
            Class<Nodes> cls = Nodes.class;
        }

        IntSpinedNodeBuilder() {
        }

        public Spliterator.OfInt spliterator() {
            return super.spliterator();
        }

        public void forEach(IntConsumer consumer) {
            super.forEach(consumer);
        }

        public void begin(long size) {
            this.building = true;
            clear();
            ensureCapacity(size);
        }

        public void accept(int i) {
            super.accept(i);
        }

        public void end() {
            this.building = false;
        }

        public void copyInto(int[] array, int offset) throws IndexOutOfBoundsException {
            super.copyInto(array, offset);
        }

        public int[] asPrimitiveArray() {
            return (int[]) super.asPrimitiveArray();
        }

        public Node.OfInt build() {
            return this;
        }
    }

    private static abstract class InternalNodeSpliterator<T, S extends Spliterator<T>, N extends Node<T>> implements Spliterator<T> {
        int curChildIndex;
        N curNode;
        S lastNodeSpliterator;
        S tryAdvanceSpliterator;
        Deque<N> tryAdvanceStack;

        private static final class OfDouble extends OfPrimitive<Double, DoubleConsumer, double[], Spliterator.OfDouble, Node.OfDouble> implements Spliterator.OfDouble {
            public /* bridge */ /* synthetic */ void forEachRemaining(DoubleConsumer doubleConsumer) {
                super.forEachRemaining(doubleConsumer);
            }

            public /* bridge */ /* synthetic */ boolean tryAdvance(DoubleConsumer doubleConsumer) {
                return super.tryAdvance(doubleConsumer);
            }

            public /* bridge */ /* synthetic */ Spliterator.OfDouble trySplit() {
                return (Spliterator.OfDouble) super.trySplit();
            }

            OfDouble(Node.OfDouble cur) {
                super(cur);
            }
        }

        private static final class OfInt extends OfPrimitive<Integer, IntConsumer, int[], Spliterator.OfInt, Node.OfInt> implements Spliterator.OfInt {
            public /* bridge */ /* synthetic */ void forEachRemaining(IntConsumer intConsumer) {
                super.forEachRemaining(intConsumer);
            }

            public /* bridge */ /* synthetic */ boolean tryAdvance(IntConsumer intConsumer) {
                return super.tryAdvance(intConsumer);
            }

            public /* bridge */ /* synthetic */ Spliterator.OfInt trySplit() {
                return (Spliterator.OfInt) super.trySplit();
            }

            OfInt(Node.OfInt cur) {
                super(cur);
            }
        }

        private static final class OfLong extends OfPrimitive<Long, LongConsumer, long[], Spliterator.OfLong, Node.OfLong> implements Spliterator.OfLong {
            public /* bridge */ /* synthetic */ void forEachRemaining(LongConsumer longConsumer) {
                super.forEachRemaining(longConsumer);
            }

            public /* bridge */ /* synthetic */ boolean tryAdvance(LongConsumer longConsumer) {
                return super.tryAdvance(longConsumer);
            }

            public /* bridge */ /* synthetic */ Spliterator.OfLong trySplit() {
                return (Spliterator.OfLong) super.trySplit();
            }

            OfLong(Node.OfLong cur) {
                super(cur);
            }
        }

        private static abstract class OfPrimitive<T, T_CONS, T_ARR, T_SPLITR extends Spliterator.OfPrimitive<T, T_CONS, T_SPLITR>, N extends Node.OfPrimitive<T, T_CONS, T_ARR, T_SPLITR, N>> extends InternalNodeSpliterator<T, T_SPLITR, N> implements Spliterator.OfPrimitive<T, T_CONS, T_SPLITR> {
            public /* bridge */ /* synthetic */ Spliterator.OfPrimitive trySplit() {
                return (Spliterator.OfPrimitive) super.trySplit();
            }

            OfPrimitive(N cur) {
                super(cur);
            }

            public boolean tryAdvance(T_CONS consumer) {
                if (!initTryAdvance()) {
                    return false;
                }
                boolean hasNext = ((Spliterator.OfPrimitive) this.tryAdvanceSpliterator).tryAdvance(consumer);
                if (!hasNext) {
                    if (this.lastNodeSpliterator == null) {
                        N leaf = (Node.OfPrimitive) findNextLeafNode(this.tryAdvanceStack);
                        if (leaf != null) {
                            this.tryAdvanceSpliterator = leaf.spliterator();
                            return ((Spliterator.OfPrimitive) this.tryAdvanceSpliterator).tryAdvance(consumer);
                        }
                    }
                    this.curNode = null;
                }
                return hasNext;
            }

            public void forEachRemaining(T_CONS consumer) {
                if (this.curNode != null) {
                    if (this.tryAdvanceSpliterator != null) {
                        do {
                        } while (tryAdvance(consumer));
                    } else if (this.lastNodeSpliterator == null) {
                        Deque<N> stack = initStack();
                        while (true) {
                            N n = (Node.OfPrimitive) findNextLeafNode(stack);
                            N leaf = n;
                            if (n == null) {
                                break;
                            }
                            leaf.forEach(consumer);
                        }
                        this.curNode = null;
                    } else {
                        ((Spliterator.OfPrimitive) this.lastNodeSpliterator).forEachRemaining(consumer);
                    }
                }
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
                        do {
                        } while (tryAdvance(consumer));
                    } else if (this.lastNodeSpliterator == null) {
                        Deque<Node<T>> stack = initStack();
                        while (true) {
                            Node<T> findNextLeafNode = findNextLeafNode(stack);
                            Node<T> leaf = findNextLeafNode;
                            if (findNextLeafNode == null) {
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

        InternalNodeSpliterator(N curNode2) {
            this.curNode = curNode2;
        }

        /* access modifiers changed from: protected */
        public final Deque<N> initStack() {
            Deque<N> stack = new ArrayDeque<>(8);
            int i = this.curNode.getChildCount();
            while (true) {
                i--;
                if (i < this.curChildIndex) {
                    return stack;
                }
                stack.addFirst(this.curNode.getChild(i));
            }
        }

        /* access modifiers changed from: protected */
        public final N findNextLeafNode(Deque<N> stack) {
            while (true) {
                N n = (Node) stack.pollFirst();
                N n2 = n;
                if (n == null) {
                    return null;
                }
                if (n2.getChildCount() != 0) {
                    for (int i = n2.getChildCount() - 1; i >= 0; i--) {
                        stack.addFirst(n2.getChild(i));
                    }
                } else if (n2.count() > 0) {
                    return n2;
                }
            }
        }

        /* access modifiers changed from: protected */
        public final boolean initTryAdvance() {
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
                } else {
                    this.tryAdvanceSpliterator = this.lastNodeSpliterator;
                }
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
            if (this.curChildIndex < this.curNode.getChildCount() - 1) {
                N n = this.curNode;
                int i = this.curChildIndex;
                this.curChildIndex = i + 1;
                return n.getChild(i).spliterator();
            }
            this.curNode = this.curNode.getChild(this.curChildIndex);
            if (this.curNode.getChildCount() == 0) {
                this.lastNodeSpliterator = this.curNode.spliterator();
                return this.lastNodeSpliterator.trySplit();
            }
            this.curChildIndex = 0;
            N n2 = this.curNode;
            int i2 = this.curChildIndex;
            this.curChildIndex = i2 + 1;
            return n2.getChild(i2).spliterator();
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

    private static class LongArrayNode implements Node.OfLong {
        final long[] array;
        int curSize;

        LongArrayNode(long size) {
            if (size < Nodes.MAX_ARRAY_SIZE) {
                this.array = new long[((int) size)];
                this.curSize = 0;
                return;
            }
            throw new IllegalArgumentException(Nodes.BAD_SIZE);
        }

        LongArrayNode(long[] array2) {
            this.array = array2;
            this.curSize = array2.length;
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
            System.arraycopy((Object) this.array, 0, (Object) dest, destOffset, this.curSize);
        }

        public long count() {
            return (long) this.curSize;
        }

        public void forEach(LongConsumer consumer) {
            for (int i = 0; i < this.curSize; i++) {
                consumer.accept(this.array[i]);
            }
        }

        public String toString() {
            return String.format("LongArrayNode[%d][%s]", Integer.valueOf(this.array.length - this.curSize), Arrays.toString(this.array));
        }
    }

    private static final class LongFixedNodeBuilder extends LongArrayNode implements Node.Builder.OfLong {
        static final /* synthetic */ boolean $assertionsDisabled = false;

        static {
            Class<Nodes> cls = Nodes.class;
        }

        LongFixedNodeBuilder(long size) {
            super(size);
        }

        public Node.OfLong build() {
            if (this.curSize >= this.array.length) {
                return this;
            }
            throw new IllegalStateException(String.format("Current size %d is less than fixed size %d", Integer.valueOf(this.curSize), Integer.valueOf(this.array.length)));
        }

        public void begin(long size) {
            if (size == ((long) this.array.length)) {
                this.curSize = 0;
            } else {
                throw new IllegalStateException(String.format("Begin size %d is not equal to fixed size %d", Long.valueOf(size), Integer.valueOf(this.array.length)));
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

    private static final class LongSpinedNodeBuilder extends SpinedBuffer.OfLong implements Node.OfLong, Node.Builder.OfLong {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private boolean building = false;

        static {
            Class<Nodes> cls = Nodes.class;
        }

        LongSpinedNodeBuilder() {
        }

        public Spliterator.OfLong spliterator() {
            return super.spliterator();
        }

        public void forEach(LongConsumer consumer) {
            super.forEach(consumer);
        }

        public void begin(long size) {
            this.building = true;
            clear();
            ensureCapacity(size);
        }

        public void accept(long i) {
            super.accept(i);
        }

        public void end() {
            this.building = false;
        }

        public void copyInto(long[] array, int offset) {
            super.copyInto(array, offset);
        }

        public long[] asPrimitiveArray() {
            return (long[]) super.asPrimitiveArray();
        }

        public Node.OfLong build() {
            return this;
        }
    }

    private static abstract class SizedCollectorTask<P_IN, P_OUT, T_SINK extends Sink<P_OUT>, K extends SizedCollectorTask<P_IN, P_OUT, T_SINK, K>> extends CountedCompleter<Void> implements Sink<P_OUT> {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        protected int fence;
        protected final PipelineHelper<P_OUT> helper;
        protected int index;
        protected long length;
        protected long offset;
        protected final Spliterator<P_IN> spliterator;
        protected final long targetSize;

        static final class OfDouble<P_IN> extends SizedCollectorTask<P_IN, Double, Sink.OfDouble, OfDouble<P_IN>> implements Sink.OfDouble {
            private final double[] array;

            OfDouble(Spliterator<P_IN> spliterator, PipelineHelper<Double> helper, double[] array2) {
                super(spliterator, helper, array2.length);
                this.array = array2;
            }

            OfDouble(OfDouble<P_IN> parent, Spliterator<P_IN> spliterator, long offset, long length) {
                super(parent, spliterator, offset, length, parent.array.length);
                this.array = parent.array;
            }

            /* access modifiers changed from: package-private */
            public OfDouble<P_IN> makeChild(Spliterator<P_IN> spliterator, long offset, long size) {
                OfDouble ofDouble = new OfDouble(this, spliterator, offset, size);
                return ofDouble;
            }

            public void accept(double value) {
                if (this.index < this.fence) {
                    double[] dArr = this.array;
                    int i = this.index;
                    this.index = i + 1;
                    dArr[i] = value;
                    return;
                }
                throw new IndexOutOfBoundsException(Integer.toString(this.index));
            }
        }

        static final class OfInt<P_IN> extends SizedCollectorTask<P_IN, Integer, Sink.OfInt, OfInt<P_IN>> implements Sink.OfInt {
            private final int[] array;

            OfInt(Spliterator<P_IN> spliterator, PipelineHelper<Integer> helper, int[] array2) {
                super(spliterator, helper, array2.length);
                this.array = array2;
            }

            OfInt(OfInt<P_IN> parent, Spliterator<P_IN> spliterator, long offset, long length) {
                super(parent, spliterator, offset, length, parent.array.length);
                this.array = parent.array;
            }

            /* access modifiers changed from: package-private */
            public OfInt<P_IN> makeChild(Spliterator<P_IN> spliterator, long offset, long size) {
                OfInt ofInt = new OfInt(this, spliterator, offset, size);
                return ofInt;
            }

            public void accept(int value) {
                if (this.index < this.fence) {
                    int[] iArr = this.array;
                    int i = this.index;
                    this.index = i + 1;
                    iArr[i] = value;
                    return;
                }
                throw new IndexOutOfBoundsException(Integer.toString(this.index));
            }
        }

        static final class OfLong<P_IN> extends SizedCollectorTask<P_IN, Long, Sink.OfLong, OfLong<P_IN>> implements Sink.OfLong {
            private final long[] array;

            OfLong(Spliterator<P_IN> spliterator, PipelineHelper<Long> helper, long[] array2) {
                super(spliterator, helper, array2.length);
                this.array = array2;
            }

            OfLong(OfLong<P_IN> parent, Spliterator<P_IN> spliterator, long offset, long length) {
                super(parent, spliterator, offset, length, parent.array.length);
                this.array = parent.array;
            }

            /* access modifiers changed from: package-private */
            public OfLong<P_IN> makeChild(Spliterator<P_IN> spliterator, long offset, long size) {
                OfLong ofLong = new OfLong(this, spliterator, offset, size);
                return ofLong;
            }

            public void accept(long value) {
                if (this.index < this.fence) {
                    long[] jArr = this.array;
                    int i = this.index;
                    this.index = i + 1;
                    jArr[i] = value;
                    return;
                }
                throw new IndexOutOfBoundsException(Integer.toString(this.index));
            }
        }

        static final class OfRef<P_IN, P_OUT> extends SizedCollectorTask<P_IN, P_OUT, Sink<P_OUT>, OfRef<P_IN, P_OUT>> implements Sink<P_OUT> {
            private final P_OUT[] array;

            OfRef(Spliterator<P_IN> spliterator, PipelineHelper<P_OUT> helper, P_OUT[] array2) {
                super(spliterator, helper, array2.length);
                this.array = array2;
            }

            OfRef(OfRef<P_IN, P_OUT> parent, Spliterator<P_IN> spliterator, long offset, long length) {
                super(parent, spliterator, offset, length, parent.array.length);
                this.array = parent.array;
            }

            /* access modifiers changed from: package-private */
            public OfRef<P_IN, P_OUT> makeChild(Spliterator<P_IN> spliterator, long offset, long size) {
                OfRef ofRef = new OfRef(this, spliterator, offset, size);
                return ofRef;
            }

            public void accept(P_OUT value) {
                if (this.index < this.fence) {
                    P_OUT[] p_outArr = this.array;
                    int i = this.index;
                    this.index = i + 1;
                    p_outArr[i] = value;
                    return;
                }
                throw new IndexOutOfBoundsException(Integer.toString(this.index));
            }
        }

        /* access modifiers changed from: package-private */
        public abstract K makeChild(Spliterator<P_IN> spliterator2, long j, long j2);

        static {
            Class<Nodes> cls = Nodes.class;
        }

        SizedCollectorTask(Spliterator<P_IN> spliterator2, PipelineHelper<P_OUT> helper2, int arrayLength) {
            this.spliterator = spliterator2;
            this.helper = helper2;
            this.targetSize = AbstractTask.suggestTargetSize(spliterator2.estimateSize());
            this.offset = 0;
            this.length = (long) arrayLength;
        }

        SizedCollectorTask(K parent, Spliterator<P_IN> spliterator2, long offset2, long length2, int arrayLength) {
            super(parent);
            this.spliterator = spliterator2;
            this.helper = parent.helper;
            this.targetSize = parent.targetSize;
            this.offset = offset2;
            this.length = length2;
            if (offset2 < 0 || length2 < 0 || (offset2 + length2) - 1 >= ((long) arrayLength)) {
                throw new IllegalArgumentException(String.format("offset and length interval [%d, %d + %d) is not within array size interval [0, %d)", Long.valueOf(offset2), Long.valueOf(offset2), Long.valueOf(length2), Integer.valueOf(arrayLength)));
            }
        }

        public void compute() {
            SizedCollectorTask sizedCollectorTask = this;
            Spliterator<P_IN> rightSplit = this.spliterator;
            while (rightSplit.estimateSize() > sizedCollectorTask.targetSize) {
                Spliterator<P_IN> trySplit = rightSplit.trySplit();
                Spliterator<P_IN> leftSplit = trySplit;
                if (trySplit == null) {
                    break;
                }
                sizedCollectorTask.setPendingCount(1);
                long leftSplitSize = leftSplit.estimateSize();
                sizedCollectorTask.makeChild(leftSplit, sizedCollectorTask.offset, leftSplitSize).fork();
                sizedCollectorTask = sizedCollectorTask.makeChild(rightSplit, sizedCollectorTask.offset + leftSplitSize, sizedCollectorTask.length - leftSplitSize);
            }
            sizedCollectorTask.helper.wrapAndCopyInto(sizedCollectorTask, rightSplit);
            sizedCollectorTask.propagateCompletion();
        }

        public void begin(long size) {
            if (size <= this.length) {
                this.index = (int) this.offset;
                this.fence = this.index + ((int) this.length);
                return;
            }
            throw new IllegalStateException("size passed to Sink.begin exceeds array length");
        }
    }

    private static final class SpinedNodeBuilder<T> extends SpinedBuffer<T> implements Node<T>, Node.Builder<T> {
        static final /* synthetic */ boolean $assertionsDisabled = false;
        private boolean building = false;

        static {
            Class<Nodes> cls = Nodes.class;
        }

        SpinedNodeBuilder() {
        }

        public Spliterator<T> spliterator() {
            return super.spliterator();
        }

        public void forEach(Consumer<? super T> consumer) {
            super.forEach(consumer);
        }

        public void begin(long size) {
            this.building = true;
            clear();
            ensureCapacity(size);
        }

        public void accept(T t) {
            super.accept(t);
        }

        public void end() {
            this.building = false;
        }

        public void copyInto(T[] array, int offset) {
            super.copyInto(array, offset);
        }

        public T[] asArray(IntFunction<T[]> arrayFactory) {
            return super.asArray(arrayFactory);
        }

        public Node<T> build() {
            return this;
        }
    }

    private static abstract class ToArrayTask<T, T_NODE extends Node<T>, K extends ToArrayTask<T, T_NODE, K>> extends CountedCompleter<Void> {
        protected final T_NODE node;
        protected final int offset;

        private static final class OfDouble extends OfPrimitive<Double, DoubleConsumer, double[], Spliterator.OfDouble, Node.OfDouble> {
            private OfDouble(Node.OfDouble node, double[] array, int offset) {
                super(node, array, offset);
            }
        }

        private static final class OfInt extends OfPrimitive<Integer, IntConsumer, int[], Spliterator.OfInt, Node.OfInt> {
            private OfInt(Node.OfInt node, int[] array, int offset) {
                super(node, array, offset);
            }
        }

        private static final class OfLong extends OfPrimitive<Long, LongConsumer, long[], Spliterator.OfLong, Node.OfLong> {
            private OfLong(Node.OfLong node, long[] array, int offset) {
                super(node, array, offset);
            }
        }

        private static class OfPrimitive<T, T_CONS, T_ARR, T_SPLITR extends Spliterator.OfPrimitive<T, T_CONS, T_SPLITR>, T_NODE extends Node.OfPrimitive<T, T_CONS, T_ARR, T_SPLITR, T_NODE>> extends ToArrayTask<T, T_NODE, OfPrimitive<T, T_CONS, T_ARR, T_SPLITR, T_NODE>> {
            private final T_ARR array;

            private OfPrimitive(T_NODE node, T_ARR array2, int offset) {
                super(node, offset);
                this.array = array2;
            }

            private OfPrimitive(OfPrimitive<T, T_CONS, T_ARR, T_SPLITR, T_NODE> parent, T_NODE node, int offset) {
                super(parent, node, offset);
                this.array = parent.array;
            }

            /* access modifiers changed from: package-private */
            public OfPrimitive<T, T_CONS, T_ARR, T_SPLITR, T_NODE> makeChild(int childIndex, int offset) {
                return new OfPrimitive<>((OfPrimitive<T, T_CONS, T_ARR, T_SPLITR, T_NODE>) this, (T_NODE) ((Node.OfPrimitive) this.node).getChild(childIndex), offset);
            }

            /* access modifiers changed from: package-private */
            public void copyNodeToArray() {
                ((Node.OfPrimitive) this.node).copyInto(this.array, this.offset);
            }
        }

        private static final class OfRef<T> extends ToArrayTask<T, Node<T>, OfRef<T>> {
            private final T[] array;

            private OfRef(Node<T> node, T[] array2, int offset) {
                super(node, offset);
                this.array = array2;
            }

            private OfRef(OfRef<T> parent, Node<T> node, int offset) {
                super(parent, node, offset);
                this.array = parent.array;
            }

            /* access modifiers changed from: package-private */
            public OfRef<T> makeChild(int childIndex, int offset) {
                return new OfRef<>((OfRef<T>) this, (Node<T>) this.node.getChild(childIndex), offset);
            }

            /* access modifiers changed from: package-private */
            public void copyNodeToArray() {
                this.node.copyInto(this.array, this.offset);
            }
        }

        /* access modifiers changed from: package-private */
        public abstract void copyNodeToArray();

        /* access modifiers changed from: package-private */
        public abstract K makeChild(int i, int i2);

        ToArrayTask(T_NODE node2, int offset2) {
            this.node = node2;
            this.offset = offset2;
        }

        ToArrayTask(K parent, T_NODE node2, int offset2) {
            super(parent);
            this.node = node2;
            this.offset = offset2;
        }

        public void compute() {
            ToArrayTask toArrayTask = this;
            while (toArrayTask.node.getChildCount() != 0) {
                toArrayTask.setPendingCount(toArrayTask.node.getChildCount() - 1);
                int size = 0;
                int i = 0;
                while (i < toArrayTask.node.getChildCount() - 1) {
                    K leftTask = toArrayTask.makeChild(i, toArrayTask.offset + size);
                    size = (int) (((long) size) + leftTask.node.count());
                    leftTask.fork();
                    i++;
                }
                toArrayTask = toArrayTask.makeChild(i, toArrayTask.offset + size);
            }
            toArrayTask.copyNodeToArray();
            toArrayTask.propagateCompletion();
        }
    }

    private Nodes() {
        throw new Error("no instances");
    }

    static <T> Node<T> emptyNode(StreamShape shape) {
        switch (shape) {
            case REFERENCE:
                return EMPTY_NODE;
            case INT_VALUE:
                return EMPTY_INT_NODE;
            case LONG_VALUE:
                return EMPTY_LONG_NODE;
            case DOUBLE_VALUE:
                return EMPTY_DOUBLE_NODE;
            default:
                throw new IllegalStateException("Unknown shape " + shape);
        }
    }

    static <T> Node<T> conc(StreamShape shape, Node<T> left, Node<T> right) {
        switch (shape) {
            case REFERENCE:
                return new ConcNode(left, right);
            case INT_VALUE:
                return new ConcNode.OfInt((Node.OfInt) left, (Node.OfInt) right);
            case LONG_VALUE:
                return new ConcNode.OfLong((Node.OfLong) left, (Node.OfLong) right);
            case DOUBLE_VALUE:
                return new ConcNode.OfDouble((Node.OfDouble) left, (Node.OfDouble) right);
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

    /* access modifiers changed from: package-private */
    public static <T> Node.Builder<T> builder(long exactSizeIfKnown, IntFunction<T[]> generator) {
        if (exactSizeIfKnown < 0 || exactSizeIfKnown >= MAX_ARRAY_SIZE) {
            return builder();
        }
        return new FixedNodeBuilder(exactSizeIfKnown, generator);
    }

    static <T> Node.Builder<T> builder() {
        return new SpinedNodeBuilder();
    }

    static Node.OfInt node(int[] array) {
        return new IntArrayNode(array);
    }

    static Node.Builder.OfInt intBuilder(long exactSizeIfKnown) {
        if (exactSizeIfKnown < 0 || exactSizeIfKnown >= MAX_ARRAY_SIZE) {
            return intBuilder();
        }
        return new IntFixedNodeBuilder(exactSizeIfKnown);
    }

    static Node.Builder.OfInt intBuilder() {
        return new IntSpinedNodeBuilder();
    }

    static Node.OfLong node(long[] array) {
        return new LongArrayNode(array);
    }

    static Node.Builder.OfLong longBuilder(long exactSizeIfKnown) {
        if (exactSizeIfKnown < 0 || exactSizeIfKnown >= MAX_ARRAY_SIZE) {
            return longBuilder();
        }
        return new LongFixedNodeBuilder(exactSizeIfKnown);
    }

    static Node.Builder.OfLong longBuilder() {
        return new LongSpinedNodeBuilder();
    }

    static Node.OfDouble node(double[] array) {
        return new DoubleArrayNode(array);
    }

    static Node.Builder.OfDouble doubleBuilder(long exactSizeIfKnown) {
        if (exactSizeIfKnown < 0 || exactSizeIfKnown >= MAX_ARRAY_SIZE) {
            return doubleBuilder();
        }
        return new DoubleFixedNodeBuilder(exactSizeIfKnown);
    }

    static Node.Builder.OfDouble doubleBuilder() {
        return new DoubleSpinedNodeBuilder();
    }

    public static <P_IN, P_OUT> Node<P_OUT> collect(PipelineHelper<P_OUT> helper, Spliterator<P_IN> spliterator, boolean flattenTree, IntFunction<P_OUT[]> generator) {
        long size = helper.exactOutputSizeIfKnown(spliterator);
        if (size < 0 || !spliterator.hasCharacteristics(16384)) {
            Node<P_OUT> node = (Node) new CollectorTask.OfRef(helper, generator, spliterator).invoke();
            return flattenTree ? flatten(node, generator) : node;
        } else if (size < MAX_ARRAY_SIZE) {
            P_OUT[] array = (Object[]) generator.apply((int) size);
            new SizedCollectorTask.OfRef(spliterator, helper, array).invoke();
            return node((T[]) array);
        } else {
            throw new IllegalArgumentException(BAD_SIZE);
        }
    }

    public static <P_IN> Node.OfInt collectInt(PipelineHelper<Integer> helper, Spliterator<P_IN> spliterator, boolean flattenTree) {
        long size = helper.exactOutputSizeIfKnown(spliterator);
        if (size < 0 || !spliterator.hasCharacteristics(16384)) {
            Node.OfInt node = (Node.OfInt) new CollectorTask.OfInt(helper, spliterator).invoke();
            return flattenTree ? flattenInt(node) : node;
        } else if (size < MAX_ARRAY_SIZE) {
            int[] array = new int[((int) size)];
            new SizedCollectorTask.OfInt(spliterator, helper, array).invoke();
            return node(array);
        } else {
            throw new IllegalArgumentException(BAD_SIZE);
        }
    }

    public static <P_IN> Node.OfLong collectLong(PipelineHelper<Long> helper, Spliterator<P_IN> spliterator, boolean flattenTree) {
        long size = helper.exactOutputSizeIfKnown(spliterator);
        if (size < 0 || !spliterator.hasCharacteristics(16384)) {
            Node.OfLong node = (Node.OfLong) new CollectorTask.OfLong(helper, spliterator).invoke();
            return flattenTree ? flattenLong(node) : node;
        } else if (size < MAX_ARRAY_SIZE) {
            long[] array = new long[((int) size)];
            new SizedCollectorTask.OfLong(spliterator, helper, array).invoke();
            return node(array);
        } else {
            throw new IllegalArgumentException(BAD_SIZE);
        }
    }

    public static <P_IN> Node.OfDouble collectDouble(PipelineHelper<Double> helper, Spliterator<P_IN> spliterator, boolean flattenTree) {
        long size = helper.exactOutputSizeIfKnown(spliterator);
        if (size < 0 || !spliterator.hasCharacteristics(16384)) {
            Node.OfDouble node = (Node.OfDouble) new CollectorTask.OfDouble(helper, spliterator).invoke();
            return flattenTree ? flattenDouble(node) : node;
        } else if (size < MAX_ARRAY_SIZE) {
            double[] array = new double[((int) size)];
            new SizedCollectorTask.OfDouble(spliterator, helper, array).invoke();
            return node(array);
        } else {
            throw new IllegalArgumentException(BAD_SIZE);
        }
    }

    public static <T> Node<T> flatten(Node<T> node, IntFunction<T[]> generator) {
        if (node.getChildCount() <= 0) {
            return node;
        }
        long size = node.count();
        if (size < MAX_ARRAY_SIZE) {
            T[] array = (Object[]) generator.apply((int) size);
            new ToArrayTask.OfRef(node, array, 0).invoke();
            return node(array);
        }
        throw new IllegalArgumentException(BAD_SIZE);
    }

    public static Node.OfInt flattenInt(Node.OfInt node) {
        if (node.getChildCount() <= 0) {
            return node;
        }
        long size = node.count();
        if (size < MAX_ARRAY_SIZE) {
            int[] array = new int[((int) size)];
            new ToArrayTask.OfInt(node, array, 0).invoke();
            return node(array);
        }
        throw new IllegalArgumentException(BAD_SIZE);
    }

    public static Node.OfLong flattenLong(Node.OfLong node) {
        if (node.getChildCount() <= 0) {
            return node;
        }
        long size = node.count();
        if (size < MAX_ARRAY_SIZE) {
            long[] array = new long[((int) size)];
            new ToArrayTask.OfLong(node, array, 0).invoke();
            return node(array);
        }
        throw new IllegalArgumentException(BAD_SIZE);
    }

    public static Node.OfDouble flattenDouble(Node.OfDouble node) {
        if (node.getChildCount() <= 0) {
            return node;
        }
        long size = node.count();
        if (size < MAX_ARRAY_SIZE) {
            double[] array = new double[((int) size)];
            new ToArrayTask.OfDouble(node, array, 0).invoke();
            return node(array);
        }
        throw new IllegalArgumentException(BAD_SIZE);
    }
}
