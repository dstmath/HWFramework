package java.util.stream;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.EnumSet;
import java.util.IntSummaryStatistics;
import java.util.Iterator;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass1;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass10;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass11;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass12;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass13;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass14;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass15;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass16;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass17;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass18;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass19;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass2;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass20;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass21;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass22;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass23;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass24;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass25;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass26;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass27;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass28;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass29;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass3;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass30;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass31;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass32;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass33;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass34;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass35;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass36;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass37;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass38;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass39;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass4;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass40;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass41;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass42;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass43;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass44;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass45;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass46;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass47;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass48;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass49;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass5;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass50;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass51;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass52;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass53;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass54;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass55;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass56;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass57;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass58;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass59;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass6;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass60;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass61;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass62;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass63;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass64;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass65;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass66;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass67;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass68;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass69;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass7;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass70;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass71;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass72;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass75;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass76;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass77;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass78;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass79;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass8;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass80;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass81;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass82;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass83;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass84;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass85;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass86;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass87;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass88;
import java.util.stream.-$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0.AnonymousClass9;
import java.util.stream.Collector.Characteristics;

public final class Collectors {
    static final Set<Characteristics> CH_CONCURRENT_ID = Collections.unmodifiableSet(EnumSet.of(Characteristics.CONCURRENT, Characteristics.UNORDERED, Characteristics.IDENTITY_FINISH));
    static final Set<Characteristics> CH_CONCURRENT_NOID = Collections.unmodifiableSet(EnumSet.of(Characteristics.CONCURRENT, Characteristics.UNORDERED));
    static final Set<Characteristics> CH_ID = Collections.unmodifiableSet(EnumSet.of(Characteristics.IDENTITY_FINISH));
    static final Set<Characteristics> CH_NOID = Collections.emptySet();
    static final Set<Characteristics> CH_UNORDERED_ID = Collections.unmodifiableSet(EnumSet.of(Characteristics.UNORDERED, Characteristics.IDENTITY_FINISH));

    static class CollectorImpl<T, A, R> implements Collector<T, A, R> {
        private final BiConsumer<A, T> accumulator;
        private final Set<Characteristics> characteristics;
        private final BinaryOperator<A> combiner;
        private final Function<A, R> finisher;
        private final Supplier<A> supplier;

        CollectorImpl(Supplier<A> supplier, BiConsumer<A, T> accumulator, BinaryOperator<A> combiner, Function<A, R> finisher, Set<Characteristics> characteristics) {
            this.supplier = supplier;
            this.accumulator = accumulator;
            this.combiner = combiner;
            this.finisher = finisher;
            this.characteristics = characteristics;
        }

        CollectorImpl(Supplier<A> supplier, BiConsumer<A, T> accumulator, BinaryOperator<A> combiner, Set<Characteristics> characteristics) {
            this(supplier, accumulator, combiner, Collectors.castingIdentity(), characteristics);
        }

        public BiConsumer<A, T> accumulator() {
            return this.accumulator;
        }

        public Supplier<A> supplier() {
            return this.supplier;
        }

        public BinaryOperator<A> combiner() {
            return this.combiner;
        }

        public Function<A, R> finisher() {
            return this.finisher;
        }

        public Set<Characteristics> characteristics() {
            return this.characteristics;
        }
    }

    private static final class Partition<T> extends AbstractMap<Boolean, T> implements Map<Boolean, T> {
        final T forFalse;
        final T forTrue;

        Partition(T forTrue, T forFalse) {
            this.forTrue = forTrue;
            this.forFalse = forFalse;
        }

        public Set<Entry<Boolean, T>> entrySet() {
            return new AbstractSet<Entry<Boolean, T>>() {
                public Iterator<Entry<Boolean, T>> iterator() {
                    Entry<Boolean, T> falseEntry = new SimpleImmutableEntry(Boolean.valueOf(false), Partition.this.forFalse);
                    Entry<Boolean, T> trueEntry = new SimpleImmutableEntry(Boolean.valueOf(true), Partition.this.forTrue);
                    return Arrays.asList(falseEntry, trueEntry).iterator();
                }

                public int size() {
                    return 2;
                }
            };
        }
    }

    private Collectors() {
    }

    static /* synthetic */ Object lambda$-java_util_stream_Collectors_5845(Object u, Object obj) {
        throw new IllegalStateException(String.format("Duplicate key %s", u));
    }

    private static <T> BinaryOperator<T> throwingMerger() {
        return new AnonymousClass19();
    }

    private static <I, R> Function<I, R> castingIdentity() {
        return new AnonymousClass26();
    }

    static /* synthetic */ Object lambda$-java_util_stream_Collectors_6048(Object i) {
        return i;
    }

    public static <T, C extends Collection<T>> Collector<T, ?, C> toCollection(Supplier<C> collectionFactory) {
        return new CollectorImpl(collectionFactory, new AnonymousClass3(), new AnonymousClass20(), CH_ID);
    }

    public static <T> Collector<T, ?, List<T>> toList() {
        return new CollectorImpl(new AnonymousClass51(), new AnonymousClass4(), new AnonymousClass21(), CH_ID);
    }

    public static <T> Collector<T, ?, Set<T>> toSet() {
        return new CollectorImpl(new AnonymousClass54(), new AnonymousClass5(), new AnonymousClass22(), CH_UNORDERED_ID);
    }

    public static Collector<CharSequence, ?, String> joining() {
        return new CollectorImpl(new AnonymousClass42(), new -$Lambda$qTstLJg88fs2C3g6LH-R51vCVP0(), new AnonymousClass10(), new AnonymousClass28(), CH_NOID);
    }

    public static Collector<CharSequence, ?, String> joining(CharSequence delimiter) {
        return joining(delimiter, "", "");
    }

    public static Collector<CharSequence, ?, String> joining(CharSequence delimiter, CharSequence prefix, CharSequence suffix) {
        return new CollectorImpl(new AnonymousClass88(delimiter, prefix, suffix), new AnonymousClass1(), new AnonymousClass11(), new AnonymousClass29(), CH_NOID);
    }

    private static <K, V, M extends Map<K, V>> BinaryOperator<M> mapMerger(BinaryOperator<V> mergeFunction) {
        return new AnonymousClass67(mergeFunction);
    }

    static /* synthetic */ Map lambda$-java_util_stream_Collectors_13304(BinaryOperator mergeFunction, Map m1, Map m2) {
        for (Entry<K, V> e : m2.entrySet()) {
            m1.merge(e.getKey(), e.getValue(), mergeFunction);
        }
        return m1;
    }

    public static <T, U, A, R> Collector<T, ?, R> mapping(Function<? super T, ? extends U> mapper, Collector<? super U, A, R> downstream) {
        return new CollectorImpl(downstream.supplier(), new AnonymousClass80(downstream.accumulator(), mapper), downstream.combiner(), downstream.finisher(), downstream.characteristics());
    }

    public static <T, A, R, RR> Collector<T, A, RR> collectingAndThen(Collector<T, A, R> downstream, Function<R, RR> finisher) {
        Set<Characteristics> characteristics;
        Collection characteristics2 = downstream.characteristics();
        if (characteristics2.contains(Characteristics.IDENTITY_FINISH)) {
            if (characteristics2.size() == 1) {
                characteristics2 = CH_NOID;
            } else {
                characteristics2 = EnumSet.copyOf(characteristics2);
                characteristics2.remove(Characteristics.IDENTITY_FINISH);
                characteristics2 = Collections.unmodifiableSet(characteristics2);
            }
        }
        return new CollectorImpl(downstream.supplier(), downstream.accumulator(), downstream.combiner(), downstream.finisher().andThen(finisher), characteristics2);
    }

    public static <T> Collector<T, ?, Long> counting() {
        return reducing(Long.valueOf(0), new AnonymousClass27(), new AnonymousClass9());
    }

    public static <T> Collector<T, ?, Optional<T>> minBy(Comparator<? super T> comparator) {
        return reducing(BinaryOperator.minBy(comparator));
    }

    public static <T> Collector<T, ?, Optional<T>> maxBy(Comparator<? super T> comparator) {
        return reducing(BinaryOperator.maxBy(comparator));
    }

    public static <T> Collector<T, ?, Integer> summingInt(ToIntFunction<? super T> mapper) {
        return new CollectorImpl(new AnonymousClass47(), new AnonymousClass63(mapper), new AnonymousClass17(), new AnonymousClass34(), CH_NOID);
    }

    public static <T> Collector<T, ?, Long> summingLong(ToLongFunction<? super T> mapper) {
        return new CollectorImpl(new AnonymousClass48(), new AnonymousClass64(mapper), new AnonymousClass18(), new AnonymousClass35(), CH_NOID);
    }

    public static <T> Collector<T, ?, Double> summingDouble(ToDoubleFunction<? super T> mapper) {
        return new CollectorImpl(new AnonymousClass46(), new AnonymousClass62(mapper), new AnonymousClass16(), new AnonymousClass33(), CH_NOID);
    }

    static /* synthetic */ void lambda$-java_util_stream_Collectors_22066(ToDoubleFunction mapper, double[] a, Object t) {
        sumWithCompensation(a, mapper.applyAsDouble(t));
        a[2] = a[2] + mapper.applyAsDouble(t);
    }

    static /* synthetic */ double[] lambda$-java_util_stream_Collectors_22206(double[] a, double[] b) {
        sumWithCompensation(a, b[0]);
        a[2] = a[2] + b[2];
        return sumWithCompensation(a, b[1]);
    }

    static double[] sumWithCompensation(double[] intermediateSum, double value) {
        double tmp = value - intermediateSum[1];
        double sum = intermediateSum[0];
        double velvel = sum + tmp;
        intermediateSum[1] = (velvel - sum) - tmp;
        intermediateSum[0] = velvel;
        return intermediateSum;
    }

    static double computeFinalSum(double[] summands) {
        double tmp = summands[0] + summands[1];
        double simpleSum = summands[summands.length - 1];
        if (Double.isNaN(tmp) && Double.isInfinite(simpleSum)) {
            return simpleSum;
        }
        return tmp;
    }

    public static <T> Collector<T, ?, Double> averagingInt(ToIntFunction<? super T> mapper) {
        return new CollectorImpl(new AnonymousClass37(), new AnonymousClass56(mapper), new AnonymousClass7(), new AnonymousClass24(), CH_NOID);
    }

    static /* synthetic */ void lambda$-java_util_stream_Collectors_24417(ToIntFunction mapper, long[] a, Object t) {
        a[0] = a[0] + ((long) mapper.applyAsInt(t));
        a[1] = a[1] + 1;
    }

    static /* synthetic */ long[] lambda$-java_util_stream_Collectors_24486(long[] a, long[] b) {
        a[0] = a[0] + b[0];
        a[1] = a[1] + b[1];
        return a;
    }

    static /* synthetic */ Double lambda$-java_util_stream_Collectors_24555(long[] a) {
        return Double.valueOf(a[1] == 0 ? 0.0d : ((double) a[0]) / ((double) a[1]));
    }

    public static <T> Collector<T, ?, Double> averagingLong(ToLongFunction<? super T> mapper) {
        return new CollectorImpl(new AnonymousClass38(), new AnonymousClass57(mapper), new AnonymousClass8(), new AnonymousClass25(), CH_NOID);
    }

    static /* synthetic */ void lambda$-java_util_stream_Collectors_25213(ToLongFunction mapper, long[] a, Object t) {
        a[0] = a[0] + mapper.applyAsLong(t);
        a[1] = a[1] + 1;
    }

    static /* synthetic */ long[] lambda$-java_util_stream_Collectors_25283(long[] a, long[] b) {
        a[0] = a[0] + b[0];
        a[1] = a[1] + b[1];
        return a;
    }

    static /* synthetic */ Double lambda$-java_util_stream_Collectors_25352(long[] a) {
        return Double.valueOf(a[1] == 0 ? 0.0d : ((double) a[0]) / ((double) a[1]));
    }

    public static <T> Collector<T, ?, Double> averagingDouble(ToDoubleFunction<? super T> mapper) {
        return new CollectorImpl(new AnonymousClass36(), new AnonymousClass55(mapper), new AnonymousClass6(), new AnonymousClass23(), CH_NOID);
    }

    static /* synthetic */ void lambda$-java_util_stream_Collectors_27066(ToDoubleFunction mapper, double[] a, Object t) {
        sumWithCompensation(a, mapper.applyAsDouble(t));
        a[2] = a[2] + 1.0d;
        a[3] = a[3] + mapper.applyAsDouble(t);
    }

    static /* synthetic */ double[] lambda$-java_util_stream_Collectors_27185(double[] a, double[] b) {
        sumWithCompensation(a, b[0]);
        sumWithCompensation(a, b[1]);
        a[2] = a[2] + b[2];
        a[3] = a[3] + b[3];
        return a;
    }

    static /* synthetic */ Double lambda$-java_util_stream_Collectors_27314(double[] a) {
        double d = 0.0d;
        if (a[2] != 0.0d) {
            d = computeFinalSum(a) / a[2];
        }
        return Double.valueOf(d);
    }

    public static <T> Collector<T, ?, T> reducing(T identity, BinaryOperator<T> op) {
        return new CollectorImpl(boxSupplier(identity), new AnonymousClass58(op), new AnonymousClass69(op), new AnonymousClass30(), CH_NOID);
    }

    private static <T> Supplier<T[]> boxSupplier(T identity) {
        return new AnonymousClass77(identity);
    }

    public static <T> Collector<T, ?, Optional<T>> reducing(BinaryOperator<T> op) {
        return new CollectorImpl(new AnonymousClass79(op), new AnonymousClass2(), new AnonymousClass12(), new AnonymousClass32(), CH_NOID);
    }

    static /* synthetic */ AnonymousClass1OptionalBox lambda$-java_util_stream_Collectors_30747(AnonymousClass1OptionalBox a, AnonymousClass1OptionalBox b) {
        if (b.present) {
            a.-java_util_stream_Collectors-mthref-13(b.value);
        }
        return a;
    }

    public static <T, U> Collector<T, ?, U> reducing(U identity, Function<? super T, ? extends U> mapper, BinaryOperator<U> op) {
        return new CollectorImpl(boxSupplier(identity), new AnonymousClass82(op, mapper), new AnonymousClass70(op), new AnonymousClass31(), CH_NOID);
    }

    public static <T, K> Collector<T, ?, Map<K, List<T>>> groupingBy(Function<? super T, ? extends K> classifier) {
        return groupingBy(classifier, toList());
    }

    public static <T, K, A, D> Collector<T, ?, Map<K, D>> groupingBy(Function<? super T, ? extends K> classifier, Collector<? super T, A, D> downstream) {
        return groupingBy(classifier, new AnonymousClass39(), downstream);
    }

    public static <T, K, D, A, M extends Map<K, D>> Collector<T, ?, M> groupingBy(Function<? super T, ? extends K> classifier, Supplier<M> mapFactory, Collector<? super T, A, D> downstream) {
        BiConsumer<Map<K, A>, T> accumulator = new AnonymousClass83(classifier, downstream.supplier(), downstream.accumulator());
        BinaryOperator<Map<K, A>> merger = mapMerger(downstream.combiner());
        Supplier<Map<K, A>> mangledFactory = mapFactory;
        if (downstream.characteristics().contains(Characteristics.IDENTITY_FINISH)) {
            return new CollectorImpl(mapFactory, accumulator, merger, CH_ID);
        }
        return new CollectorImpl(mapFactory, accumulator, merger, new AnonymousClass71(downstream.finisher()), CH_NOID);
    }

    static /* synthetic */ Map lambda$-java_util_stream_Collectors_41242(Function downstreamFinisher, Map intermediate) {
        intermediate.replaceAll(new AnonymousClass65(downstreamFinisher));
        M castResult = intermediate;
        return intermediate;
    }

    public static <T, K> Collector<T, ?, ConcurrentMap<K, List<T>>> groupingByConcurrent(Function<? super T, ? extends K> classifier) {
        return groupingByConcurrent(classifier, new AnonymousClass40(), toList());
    }

    public static <T, K, A, D> Collector<T, ?, ConcurrentMap<K, D>> groupingByConcurrent(Function<? super T, ? extends K> classifier, Collector<? super T, A, D> downstream) {
        return groupingByConcurrent(classifier, new AnonymousClass41(), downstream);
    }

    public static <T, K, A, D, M extends ConcurrentMap<K, D>> Collector<T, ?, M> groupingByConcurrent(Function<? super T, ? extends K> classifier, Supplier<M> mapFactory, Collector<? super T, A, D> downstream) {
        BiConsumer<ConcurrentMap<K, A>, T> accumulator;
        Supplier<A> downstreamSupplier = downstream.supplier();
        BiConsumer<A, ? super T> downstreamAccumulator = downstream.accumulator();
        BinaryOperator<ConcurrentMap<K, A>> merger = mapMerger(downstream.combiner());
        Supplier<ConcurrentMap<K, A>> mangledFactory = mapFactory;
        if (downstream.characteristics().contains(Characteristics.CONCURRENT)) {
            accumulator = new AnonymousClass84(classifier, downstreamSupplier, downstreamAccumulator);
        } else {
            accumulator = new AnonymousClass85(classifier, downstreamSupplier, downstreamAccumulator);
        }
        if (downstream.characteristics().contains(Characteristics.IDENTITY_FINISH)) {
            return new CollectorImpl(mapFactory, accumulator, merger, CH_CONCURRENT_ID);
        }
        return new CollectorImpl(mapFactory, accumulator, merger, new AnonymousClass72(downstream.finisher()), CH_CONCURRENT_NOID);
    }

    static /* synthetic */ void lambda$-java_util_stream_Collectors_49016(Function classifier, Supplier downstreamSupplier, BiConsumer downstreamAccumulator, ConcurrentMap m, Object t) {
        A resultContainer = m.computeIfAbsent(Objects.requireNonNull(classifier.lambda$-java_util_stream_Collectors_49854(t), "element cannot be mapped to a null key"), new AnonymousClass75(downstreamSupplier));
        synchronized (resultContainer) {
            downstreamAccumulator.lambda$-java_util_stream_ReferencePipeline_19478(resultContainer, t);
        }
    }

    static /* synthetic */ ConcurrentMap lambda$-java_util_stream_Collectors_49796(Function downstreamFinisher, ConcurrentMap intermediate) {
        intermediate.replaceAll(new AnonymousClass66(downstreamFinisher));
        M castResult = intermediate;
        return intermediate;
    }

    public static <T> Collector<T, ?, Map<Boolean, List<T>>> partitioningBy(Predicate<? super T> predicate) {
        return partitioningBy(predicate, toList());
    }

    public static <T, D, A> Collector<T, ?, Map<Boolean, D>> partitioningBy(Predicate<? super T> predicate, Collector<? super T, A, D> downstream) {
        BiConsumer<Partition<A>, T> accumulator = new AnonymousClass81(downstream.accumulator(), predicate);
        BinaryOperator<Partition<A>> merger = new AnonymousClass68(downstream.combiner());
        Supplier<Partition<A>> supplier = new AnonymousClass78(downstream);
        if (downstream.characteristics().contains(Characteristics.IDENTITY_FINISH)) {
            return new CollectorImpl(supplier, accumulator, merger, CH_ID);
        }
        return new CollectorImpl(supplier, accumulator, merger, new AnonymousClass76(downstream), CH_NOID);
    }

    static /* synthetic */ void lambda$-java_util_stream_Collectors_52253(BiConsumer downstreamAccumulator, Predicate predicate, Partition result, Object t) {
        downstreamAccumulator.lambda$-java_util_stream_ReferencePipeline_19478(predicate.test(t) ? result.forTrue : result.forFalse, t);
    }

    public static <T, K, U> Collector<T, ?, Map<K, U>> toMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper) {
        return toMap(keyMapper, valueMapper, throwingMerger(), new AnonymousClass52());
    }

    public static <T, K, U> Collector<T, ?, Map<K, U>> toMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper, BinaryOperator<U> mergeFunction) {
        return toMap(keyMapper, valueMapper, mergeFunction, new AnonymousClass53());
    }

    public static <T, K, U, M extends Map<K, U>> Collector<T, ?, M> toMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper, BinaryOperator<U> mergeFunction, Supplier<M> mapSupplier) {
        return new CollectorImpl(mapSupplier, new AnonymousClass87(keyMapper, valueMapper, mergeFunction), mapMerger(mergeFunction), CH_ID);
    }

    public static <T, K, U> Collector<T, ?, ConcurrentMap<K, U>> toConcurrentMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper) {
        return toConcurrentMap(keyMapper, valueMapper, throwingMerger(), new AnonymousClass49());
    }

    public static <T, K, U> Collector<T, ?, ConcurrentMap<K, U>> toConcurrentMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper, BinaryOperator<U> mergeFunction) {
        return toConcurrentMap(keyMapper, valueMapper, mergeFunction, new AnonymousClass50());
    }

    public static <T, K, U, M extends ConcurrentMap<K, U>> Collector<T, ?, M> toConcurrentMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper, BinaryOperator<U> mergeFunction, Supplier<M> mapSupplier) {
        return new CollectorImpl(mapSupplier, new AnonymousClass86(keyMapper, valueMapper, mergeFunction), mapMerger(mergeFunction), CH_CONCURRENT_ID);
    }

    public static <T> Collector<T, ?, IntSummaryStatistics> summarizingInt(ToIntFunction<? super T> mapper) {
        return new CollectorImpl(new AnonymousClass44(), new AnonymousClass60(mapper), new AnonymousClass14(), CH_ID);
    }

    public static <T> Collector<T, ?, LongSummaryStatistics> summarizingLong(ToLongFunction<? super T> mapper) {
        return new CollectorImpl(new AnonymousClass45(), new AnonymousClass61(mapper), new AnonymousClass15(), CH_ID);
    }

    public static <T> Collector<T, ?, DoubleSummaryStatistics> summarizingDouble(ToDoubleFunction<? super T> mapper) {
        return new CollectorImpl(new AnonymousClass43(), new AnonymousClass59(mapper), new AnonymousClass13(), CH_ID);
    }
}
