package java.util.stream;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IntSummaryStatistics;
import java.util.Iterator;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector.Characteristics;

public final class Collectors {
    static final Set<Characteristics> CH_CONCURRENT_ID = null;
    static final Set<Characteristics> CH_CONCURRENT_NOID = null;
    static final Set<Characteristics> CH_ID = null;
    static final Set<Characteristics> CH_NOID = null;
    static final Set<Characteristics> CH_UNORDERED_ID = null;

    final /* synthetic */ class -java_util_function_BinaryOperator_mapMerger_java_util_function_BinaryOperator_mergeFunction_LambdaImpl0 implements BinaryOperator {
        private /* synthetic */ BinaryOperator val$mergeFunction;

        public /* synthetic */ -java_util_function_BinaryOperator_mapMerger_java_util_function_BinaryOperator_mergeFunction_LambdaImpl0(BinaryOperator binaryOperator) {
            this.val$mergeFunction = binaryOperator;
        }

        public Object apply(Object arg0, Object arg1) {
            return Collectors.-java_util_stream_Collectors_lambda$19(this.val$mergeFunction, (Map) arg0, (Map) arg1);
        }
    }

    final /* synthetic */ class -java_util_function_BinaryOperator_throwingMerger__LambdaImpl0 implements BinaryOperator {
        public Object apply(Object arg0, Object arg1) {
            return Collectors.-java_util_stream_Collectors_lambda$1(arg0, arg1);
        }
    }

    final /* synthetic */ class -java_util_function_Function_castingIdentity__LambdaImpl0 implements Function {
        public Object apply(Object arg0) {
            return Collectors.-java_util_stream_Collectors_lambda$2(arg0);
        }
    }

    final /* synthetic */ class -java_util_function_Supplier_boxSupplier_java_lang_Object_identity_LambdaImpl0 implements Supplier {
        private /* synthetic */ Object val$identity;

        public /* synthetic */ -java_util_function_Supplier_boxSupplier_java_lang_Object_identity_LambdaImpl0(Object obj) {
            this.val$identity = obj;
        }

        public Object get() {
            return new Object[]{this.val$identity};
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_averagingDouble_java_util_function_ToDoubleFunction_mapper_LambdaImpl0 implements Supplier {
        public Object get() {
            return new double[4];
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_averagingDouble_java_util_function_ToDoubleFunction_mapper_LambdaImpl1 implements BiConsumer {
        private /* synthetic */ ToDoubleFunction val$mapper;

        public /* synthetic */ -java_util_stream_Collector_averagingDouble_java_util_function_ToDoubleFunction_mapper_LambdaImpl1(ToDoubleFunction toDoubleFunction) {
            this.val$mapper = toDoubleFunction;
        }

        public void accept(Object arg0, Object arg1) {
            Collectors.-java_util_stream_Collectors_lambda$44(this.val$mapper, (double[]) arg0, arg1);
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_averagingDouble_java_util_function_ToDoubleFunction_mapper_LambdaImpl2 implements BinaryOperator {
        public Object apply(Object arg0, Object arg1) {
            return Collectors.-java_util_stream_Collectors_lambda$45((double[]) arg0, (double[]) arg1);
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_averagingDouble_java_util_function_ToDoubleFunction_mapper_LambdaImpl3 implements Function {
        public Object apply(Object arg0) {
            return Collectors.-java_util_stream_Collectors_lambda$46((double[]) arg0);
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_averagingInt_java_util_function_ToIntFunction_mapper_LambdaImpl0 implements Supplier {
        public Object get() {
            return new long[2];
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_averagingInt_java_util_function_ToIntFunction_mapper_LambdaImpl1 implements BiConsumer {
        private /* synthetic */ ToIntFunction val$mapper;

        public /* synthetic */ -java_util_stream_Collector_averagingInt_java_util_function_ToIntFunction_mapper_LambdaImpl1(ToIntFunction toIntFunction) {
            this.val$mapper = toIntFunction;
        }

        public void accept(Object arg0, Object arg1) {
            Collectors.-java_util_stream_Collectors_lambda$36(this.val$mapper, (long[]) arg0, arg1);
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_averagingInt_java_util_function_ToIntFunction_mapper_LambdaImpl2 implements BinaryOperator {
        public Object apply(Object arg0, Object arg1) {
            return Collectors.-java_util_stream_Collectors_lambda$37((long[]) arg0, (long[]) arg1);
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_averagingInt_java_util_function_ToIntFunction_mapper_LambdaImpl3 implements Function {
        public Object apply(Object arg0) {
            return Collectors.-java_util_stream_Collectors_lambda$38((long[]) arg0);
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_averagingLong_java_util_function_ToLongFunction_mapper_LambdaImpl0 implements Supplier {
        public Object get() {
            return new long[2];
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_averagingLong_java_util_function_ToLongFunction_mapper_LambdaImpl1 implements BiConsumer {
        private /* synthetic */ ToLongFunction val$mapper;

        public /* synthetic */ -java_util_stream_Collector_averagingLong_java_util_function_ToLongFunction_mapper_LambdaImpl1(ToLongFunction toLongFunction) {
            this.val$mapper = toLongFunction;
        }

        public void accept(Object arg0, Object arg1) {
            Collectors.-java_util_stream_Collectors_lambda$40(this.val$mapper, (long[]) arg0, arg1);
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_averagingLong_java_util_function_ToLongFunction_mapper_LambdaImpl2 implements BinaryOperator {
        public Object apply(Object arg0, Object arg1) {
            return Collectors.-java_util_stream_Collectors_lambda$41((long[]) arg0, (long[]) arg1);
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_averagingLong_java_util_function_ToLongFunction_mapper_LambdaImpl3 implements Function {
        public Object apply(Object arg0) {
            return Collectors.-java_util_stream_Collectors_lambda$42((long[]) arg0);
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_counting__LambdaImpl0 implements Function {
        public Object apply(Object arg0) {
            return Long.valueOf(1);
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_counting__LambdaImpl1 implements BinaryOperator {
        public Object apply(Object arg0, Object arg1) {
            return Long.valueOf(Long.sum(((Long) arg0).longValue(), ((Long) arg1).longValue()));
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_groupingByConcurrent_java_util_function_Function_classifier_LambdaImpl0 implements Supplier {
        public Object get() {
            return new ConcurrentHashMap();
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_groupingByConcurrent_java_util_function_Function_classifier_java_util_function_Supplier_mapFactory_java_util_stream_Collector_downstream_LambdaImpl0 implements BiConsumer {
        private /* synthetic */ Function val$classifier;
        private /* synthetic */ BiConsumer val$downstreamAccumulator;
        private /* synthetic */ Supplier val$downstreamSupplier;

        public /* synthetic */ -java_util_stream_Collector_groupingByConcurrent_java_util_function_Function_classifier_java_util_function_Supplier_mapFactory_java_util_stream_Collector_downstream_LambdaImpl0(Function function, Supplier supplier, BiConsumer biConsumer) {
            this.val$classifier = function;
            this.val$downstreamSupplier = supplier;
            this.val$downstreamAccumulator = biConsumer;
        }

        public void accept(Object arg0, Object arg1) {
            this.val$downstreamAccumulator.accept(((ConcurrentMap) arg0).computeIfAbsent(Objects.requireNonNull(this.val$classifier.apply(arg1), "element cannot be mapped to a null key"), new Collectors$-void_-java_util_stream_Collectors_lambda$65_java_util_function_Function_classifier_java_util_function_Supplier_downstreamSupplier_java_util_function_BiConsumer_downstreamAccumulator_java_util_concurrent_ConcurrentMap_m_java_lang_Object_t_LambdaImpl0(this.val$downstreamSupplier)), arg1);
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_groupingByConcurrent_java_util_function_Function_classifier_java_util_function_Supplier_mapFactory_java_util_stream_Collector_downstream_LambdaImpl1 implements BiConsumer {
        private /* synthetic */ Function val$classifier;
        private /* synthetic */ BiConsumer val$downstreamAccumulator;
        private /* synthetic */ Supplier val$downstreamSupplier;

        public /* synthetic */ -java_util_stream_Collector_groupingByConcurrent_java_util_function_Function_classifier_java_util_function_Supplier_mapFactory_java_util_stream_Collector_downstream_LambdaImpl1(Function function, Supplier supplier, BiConsumer biConsumer) {
            this.val$classifier = function;
            this.val$downstreamSupplier = supplier;
            this.val$downstreamAccumulator = biConsumer;
        }

        public void accept(Object arg0, Object arg1) {
            Collectors.-java_util_stream_Collectors_lambda$67(this.val$classifier, this.val$downstreamSupplier, this.val$downstreamAccumulator, (ConcurrentMap) arg0, arg1);
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_groupingByConcurrent_java_util_function_Function_classifier_java_util_function_Supplier_mapFactory_java_util_stream_Collector_downstream_LambdaImpl2 implements Function {
        private /* synthetic */ Function val$downstreamFinisher;

        public /* synthetic */ -java_util_stream_Collector_groupingByConcurrent_java_util_function_Function_classifier_java_util_function_Supplier_mapFactory_java_util_stream_Collector_downstream_LambdaImpl2(Function function) {
            this.val$downstreamFinisher = function;
        }

        public Object apply(Object arg0) {
            return Collectors.-java_util_stream_Collectors_lambda$69(this.val$downstreamFinisher, (ConcurrentMap) arg0);
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_groupingByConcurrent_java_util_function_Function_classifier_java_util_stream_Collector_downstream_LambdaImpl0 implements Supplier {
        public Object get() {
            return new ConcurrentHashMap();
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_groupingBy_java_util_function_Function_classifier_java_util_function_Supplier_mapFactory_java_util_stream_Collector_downstream_LambdaImpl0 implements BiConsumer {
        private /* synthetic */ Function val$classifier;
        private /* synthetic */ BiConsumer val$downstreamAccumulator;
        private /* synthetic */ Supplier val$downstreamSupplier;

        public /* synthetic */ -java_util_stream_Collector_groupingBy_java_util_function_Function_classifier_java_util_function_Supplier_mapFactory_java_util_stream_Collector_downstream_LambdaImpl0(Function function, Supplier supplier, BiConsumer biConsumer) {
            this.val$classifier = function;
            this.val$downstreamSupplier = supplier;
            this.val$downstreamAccumulator = biConsumer;
        }

        public void accept(Object arg0, Object arg1) {
            this.val$downstreamAccumulator.accept(((Map) arg0).computeIfAbsent(Objects.requireNonNull(this.val$classifier.apply(arg1), "element cannot be mapped to a null key"), new Collectors$-void_-java_util_stream_Collectors_lambda$59_java_util_function_Function_classifier_java_util_function_Supplier_downstreamSupplier_java_util_function_BiConsumer_downstreamAccumulator_java_util_Map_m_java_lang_Object_t_LambdaImpl0(this.val$downstreamSupplier)), arg1);
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_groupingBy_java_util_function_Function_classifier_java_util_function_Supplier_mapFactory_java_util_stream_Collector_downstream_LambdaImpl1 implements Function {
        private /* synthetic */ Function val$downstreamFinisher;

        public /* synthetic */ -java_util_stream_Collector_groupingBy_java_util_function_Function_classifier_java_util_function_Supplier_mapFactory_java_util_stream_Collector_downstream_LambdaImpl1(Function function) {
            this.val$downstreamFinisher = function;
        }

        public Object apply(Object arg0) {
            return Collectors.-java_util_stream_Collectors_lambda$61(this.val$downstreamFinisher, (Map) arg0);
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_groupingBy_java_util_function_Function_classifier_java_util_stream_Collector_downstream_LambdaImpl0 implements Supplier {
        public Object get() {
            return new HashMap();
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_joining__LambdaImpl0 implements Supplier {
        public Object get() {
            return new StringBuilder();
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_joining__LambdaImpl1 implements BiConsumer {
        public void accept(Object arg0, Object arg1) {
            ((StringBuilder) arg0).append((CharSequence) arg1);
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_joining__LambdaImpl2 implements BinaryOperator {
        public Object apply(Object arg0, Object arg1) {
            return ((StringBuilder) arg0).append((CharSequence) (StringBuilder) arg1);
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_joining__LambdaImpl3 implements Function {
        public Object apply(Object arg0) {
            return ((StringBuilder) arg0).toString();
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_joining_java_lang_CharSequence_delimiter_java_lang_CharSequence_prefix_java_lang_CharSequence_suffix_LambdaImpl0 implements Supplier {
        private /* synthetic */ CharSequence val$delimiter;
        private /* synthetic */ CharSequence val$prefix;
        private /* synthetic */ CharSequence val$suffix;

        public /* synthetic */ -java_util_stream_Collector_joining_java_lang_CharSequence_delimiter_java_lang_CharSequence_prefix_java_lang_CharSequence_suffix_LambdaImpl0(CharSequence charSequence, CharSequence charSequence2, CharSequence charSequence3) {
            this.val$delimiter = charSequence;
            this.val$prefix = charSequence2;
            this.val$suffix = charSequence3;
        }

        public Object get() {
            return new StringJoiner(this.val$delimiter, this.val$prefix, this.val$suffix);
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_joining_java_lang_CharSequence_delimiter_java_lang_CharSequence_prefix_java_lang_CharSequence_suffix_LambdaImpl1 implements BiConsumer {
        public void accept(Object arg0, Object arg1) {
            ((StringJoiner) arg0).add((CharSequence) arg1);
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_joining_java_lang_CharSequence_delimiter_java_lang_CharSequence_prefix_java_lang_CharSequence_suffix_LambdaImpl2 implements BinaryOperator {
        public Object apply(Object arg0, Object arg1) {
            return ((StringJoiner) arg0).merge((StringJoiner) arg1);
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_joining_java_lang_CharSequence_delimiter_java_lang_CharSequence_prefix_java_lang_CharSequence_suffix_LambdaImpl3 implements Function {
        public Object apply(Object arg0) {
            return ((StringJoiner) arg0).toString();
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_mapping_java_util_function_Function_mapper_java_util_stream_Collector_downstream_LambdaImpl0 implements BiConsumer {
        private /* synthetic */ BiConsumer val$downstreamAccumulator;
        private /* synthetic */ Function val$mapper;

        public /* synthetic */ -java_util_stream_Collector_mapping_java_util_function_Function_mapper_java_util_stream_Collector_downstream_LambdaImpl0(BiConsumer biConsumer, Function function) {
            this.val$downstreamAccumulator = biConsumer;
            this.val$mapper = function;
        }

        public void accept(Object arg0, Object arg1) {
            this.val$downstreamAccumulator.accept(arg0, this.val$mapper.apply(arg1));
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_partitioningBy_java_util_function_Predicate_predicate_java_util_stream_Collector_downstream_LambdaImpl0 implements BiConsumer {
        private /* synthetic */ BiConsumer val$downstreamAccumulator;
        private /* synthetic */ Predicate val$predicate;

        public /* synthetic */ -java_util_stream_Collector_partitioningBy_java_util_function_Predicate_predicate_java_util_stream_Collector_downstream_LambdaImpl0(BiConsumer biConsumer, Predicate predicate) {
            this.val$downstreamAccumulator = biConsumer;
            this.val$predicate = predicate;
        }

        public void accept(Object arg0, Object arg1) {
            Collectors.-java_util_stream_Collectors_lambda$71(this.val$downstreamAccumulator, this.val$predicate, (Partition) arg0, arg1);
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_partitioningBy_java_util_function_Predicate_predicate_java_util_stream_Collector_downstream_LambdaImpl1 implements BinaryOperator {
        private /* synthetic */ BinaryOperator val$op;

        public /* synthetic */ -java_util_stream_Collector_partitioningBy_java_util_function_Predicate_predicate_java_util_stream_Collector_downstream_LambdaImpl1(BinaryOperator binaryOperator) {
            this.val$op = binaryOperator;
        }

        public Object apply(Object arg0, Object arg1) {
            return new Partition(this.val$op.apply(((Partition) arg0).forTrue, ((Partition) arg1).forTrue), this.val$op.apply(((Partition) arg0).forFalse, ((Partition) arg1).forFalse));
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_partitioningBy_java_util_function_Predicate_predicate_java_util_stream_Collector_downstream_LambdaImpl2 implements Supplier {
        private /* synthetic */ Collector val$downstream;

        public /* synthetic */ -java_util_stream_Collector_partitioningBy_java_util_function_Predicate_predicate_java_util_stream_Collector_downstream_LambdaImpl2(Collector collector) {
            this.val$downstream = collector;
        }

        public Object get() {
            return new Partition(this.val$downstream.supplier().get(), this.val$downstream.supplier().get());
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_partitioningBy_java_util_function_Predicate_predicate_java_util_stream_Collector_downstream_LambdaImpl3 implements Function {
        private /* synthetic */ Collector val$downstream;

        public /* synthetic */ -java_util_stream_Collector_partitioningBy_java_util_function_Predicate_predicate_java_util_stream_Collector_downstream_LambdaImpl3(Collector collector) {
            this.val$downstream = collector;
        }

        public Object apply(Object arg0) {
            return new Partition(this.val$downstream.finisher().apply(((Partition) arg0).forTrue), this.val$downstream.finisher().apply(((Partition) arg0).forFalse));
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_reducing_java_lang_Object_identity_java_util_function_BinaryOperator_op_LambdaImpl0 implements BiConsumer {
        private /* synthetic */ BinaryOperator val$op;

        public /* synthetic */ -java_util_stream_Collector_reducing_java_lang_Object_identity_java_util_function_BinaryOperator_op_LambdaImpl0(BinaryOperator binaryOperator) {
            this.val$op = binaryOperator;
        }

        public void accept(Object arg0, Object arg1) {
            ((Object[]) arg0)[0] = this.val$op.apply(((Object[]) arg0)[0], arg1);
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_reducing_java_lang_Object_identity_java_util_function_BinaryOperator_op_LambdaImpl1 implements BinaryOperator {
        private /* synthetic */ BinaryOperator val$op;

        public /* synthetic */ -java_util_stream_Collector_reducing_java_lang_Object_identity_java_util_function_BinaryOperator_op_LambdaImpl1(BinaryOperator binaryOperator) {
            this.val$op = binaryOperator;
        }

        public Object apply(Object arg0, Object arg1) {
            return ((Object[]) arg0)[0] = this.val$op.apply(((Object[]) arg0)[0], ((Object[]) arg1)[0]);
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_reducing_java_lang_Object_identity_java_util_function_BinaryOperator_op_LambdaImpl2 implements Function {
        public Object apply(Object arg0) {
            return ((Object[]) arg0)[0];
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_reducing_java_lang_Object_identity_java_util_function_Function_mapper_java_util_function_BinaryOperator_op_LambdaImpl0 implements BiConsumer {
        private /* synthetic */ Function val$mapper;
        private /* synthetic */ BinaryOperator val$op;

        public /* synthetic */ -java_util_stream_Collector_reducing_java_lang_Object_identity_java_util_function_Function_mapper_java_util_function_BinaryOperator_op_LambdaImpl0(BinaryOperator binaryOperator, Function function) {
            this.val$op = binaryOperator;
            this.val$mapper = function;
        }

        public void accept(Object arg0, Object arg1) {
            ((Object[]) arg0)[0] = this.val$op.apply(((Object[]) arg0)[0], this.val$mapper.apply(arg1));
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_reducing_java_lang_Object_identity_java_util_function_Function_mapper_java_util_function_BinaryOperator_op_LambdaImpl1 implements BinaryOperator {
        private /* synthetic */ BinaryOperator val$op;

        public /* synthetic */ -java_util_stream_Collector_reducing_java_lang_Object_identity_java_util_function_Function_mapper_java_util_function_BinaryOperator_op_LambdaImpl1(BinaryOperator binaryOperator) {
            this.val$op = binaryOperator;
        }

        public Object apply(Object arg0, Object arg1) {
            return ((Object[]) arg0)[0] = this.val$op.apply(((Object[]) arg0)[0], ((Object[]) arg1)[0]);
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_reducing_java_lang_Object_identity_java_util_function_Function_mapper_java_util_function_BinaryOperator_op_LambdaImpl2 implements Function {
        public Object apply(Object arg0) {
            return ((Object[]) arg0)[0];
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_reducing_java_util_function_BinaryOperator_op_LambdaImpl0 implements Supplier {
        private /* synthetic */ BinaryOperator val$op;

        public /* synthetic */ -java_util_stream_Collector_reducing_java_util_function_BinaryOperator_op_LambdaImpl0(BinaryOperator binaryOperator) {
            this.val$op = binaryOperator;
        }

        public Object get() {
            return new AnonymousClass1OptionalBox(this.val$op);
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_reducing_java_util_function_BinaryOperator_op_LambdaImpl1 implements BiConsumer {
        public void accept(Object arg0, Object arg1) {
            ((AnonymousClass1OptionalBox) arg0).accept(arg1);
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_reducing_java_util_function_BinaryOperator_op_LambdaImpl2 implements BinaryOperator {
        public Object apply(Object arg0, Object arg1) {
            return Collectors.-java_util_stream_Collectors_lambda$53((AnonymousClass1OptionalBox) arg0, (AnonymousClass1OptionalBox) arg1);
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_reducing_java_util_function_BinaryOperator_op_LambdaImpl3 implements Function {
        public Object apply(Object arg0) {
            return Optional.ofNullable(((AnonymousClass1OptionalBox) arg0).value);
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_summarizingDouble_java_util_function_ToDoubleFunction_mapper_LambdaImpl0 implements Supplier {
        public Object get() {
            return new DoubleSummaryStatistics();
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_summarizingDouble_java_util_function_ToDoubleFunction_mapper_LambdaImpl1 implements BiConsumer {
        private /* synthetic */ ToDoubleFunction val$mapper;

        public /* synthetic */ -java_util_stream_Collector_summarizingDouble_java_util_function_ToDoubleFunction_mapper_LambdaImpl1(ToDoubleFunction toDoubleFunction) {
            this.val$mapper = toDoubleFunction;
        }

        public void accept(Object arg0, Object arg1) {
            ((DoubleSummaryStatistics) arg0).accept(this.val$mapper.applyAsDouble(arg1));
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_summarizingDouble_java_util_function_ToDoubleFunction_mapper_LambdaImpl2 implements BinaryOperator {
        public Object apply(Object arg0, Object arg1) {
            return ((DoubleSummaryStatistics) arg0).combine((DoubleSummaryStatistics) arg1);
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_summarizingInt_java_util_function_ToIntFunction_mapper_LambdaImpl0 implements Supplier {
        public Object get() {
            return new IntSummaryStatistics();
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_summarizingInt_java_util_function_ToIntFunction_mapper_LambdaImpl1 implements BiConsumer {
        private /* synthetic */ ToIntFunction val$mapper;

        public /* synthetic */ -java_util_stream_Collector_summarizingInt_java_util_function_ToIntFunction_mapper_LambdaImpl1(ToIntFunction toIntFunction) {
            this.val$mapper = toIntFunction;
        }

        public void accept(Object arg0, Object arg1) {
            ((IntSummaryStatistics) arg0).accept(this.val$mapper.applyAsInt(arg1));
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_summarizingInt_java_util_function_ToIntFunction_mapper_LambdaImpl2 implements BinaryOperator {
        public Object apply(Object arg0, Object arg1) {
            return ((IntSummaryStatistics) arg0).combine((IntSummaryStatistics) arg1);
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_summarizingLong_java_util_function_ToLongFunction_mapper_LambdaImpl0 implements Supplier {
        public Object get() {
            return new LongSummaryStatistics();
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_summarizingLong_java_util_function_ToLongFunction_mapper_LambdaImpl1 implements BiConsumer {
        private /* synthetic */ ToLongFunction val$mapper;

        public /* synthetic */ -java_util_stream_Collector_summarizingLong_java_util_function_ToLongFunction_mapper_LambdaImpl1(ToLongFunction toLongFunction) {
            this.val$mapper = toLongFunction;
        }

        public void accept(Object arg0, Object arg1) {
            ((LongSummaryStatistics) arg0).accept(this.val$mapper.applyAsLong(arg1));
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_summarizingLong_java_util_function_ToLongFunction_mapper_LambdaImpl2 implements BinaryOperator {
        public Object apply(Object arg0, Object arg1) {
            return ((LongSummaryStatistics) arg0).combine((LongSummaryStatistics) arg1);
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_summingDouble_java_util_function_ToDoubleFunction_mapper_LambdaImpl0 implements Supplier {
        public Object get() {
            return new double[3];
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_summingDouble_java_util_function_ToDoubleFunction_mapper_LambdaImpl1 implements BiConsumer {
        private /* synthetic */ ToDoubleFunction val$mapper;

        public /* synthetic */ -java_util_stream_Collector_summingDouble_java_util_function_ToDoubleFunction_mapper_LambdaImpl1(ToDoubleFunction toDoubleFunction) {
            this.val$mapper = toDoubleFunction;
        }

        public void accept(Object arg0, Object arg1) {
            Collectors.-java_util_stream_Collectors_lambda$32(this.val$mapper, (double[]) arg0, arg1);
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_summingDouble_java_util_function_ToDoubleFunction_mapper_LambdaImpl2 implements BinaryOperator {
        public Object apply(Object arg0, Object arg1) {
            return Collectors.-java_util_stream_Collectors_lambda$33((double[]) arg0, (double[]) arg1);
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_summingDouble_java_util_function_ToDoubleFunction_mapper_LambdaImpl3 implements Function {
        public Object apply(Object arg0) {
            return Double.valueOf(Collectors.computeFinalSum((double[]) arg0));
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_summingInt_java_util_function_ToIntFunction_mapper_LambdaImpl0 implements Supplier {
        public Object get() {
            return new int[1];
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_summingInt_java_util_function_ToIntFunction_mapper_LambdaImpl1 implements BiConsumer {
        private /* synthetic */ ToIntFunction val$mapper;

        public /* synthetic */ -java_util_stream_Collector_summingInt_java_util_function_ToIntFunction_mapper_LambdaImpl1(ToIntFunction toIntFunction) {
            this.val$mapper = toIntFunction;
        }

        public void accept(Object arg0, Object arg1) {
            ((int[]) arg0)[0] = ((int[]) arg0)[0] + this.val$mapper.applyAsInt(arg1);
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_summingInt_java_util_function_ToIntFunction_mapper_LambdaImpl2 implements BinaryOperator {
        public Object apply(Object arg0, Object arg1) {
            return ((int[]) arg0)[0] = ((int[]) arg0)[0] + ((int[]) arg1)[0];
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_summingInt_java_util_function_ToIntFunction_mapper_LambdaImpl3 implements Function {
        public Object apply(Object arg0) {
            return Integer.valueOf(((int[]) arg0)[0]);
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_summingLong_java_util_function_ToLongFunction_mapper_LambdaImpl0 implements Supplier {
        public Object get() {
            return new long[1];
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_summingLong_java_util_function_ToLongFunction_mapper_LambdaImpl1 implements BiConsumer {
        private /* synthetic */ ToLongFunction val$mapper;

        public /* synthetic */ -java_util_stream_Collector_summingLong_java_util_function_ToLongFunction_mapper_LambdaImpl1(ToLongFunction toLongFunction) {
            this.val$mapper = toLongFunction;
        }

        public void accept(Object arg0, Object arg1) {
            ((long[]) arg0)[0] = ((long[]) arg0)[0] + this.val$mapper.applyAsLong(arg1);
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_summingLong_java_util_function_ToLongFunction_mapper_LambdaImpl2 implements BinaryOperator {
        public Object apply(Object arg0, Object arg1) {
            return ((long[]) arg0)[0] = ((long[]) arg0)[0] + ((long[]) arg1)[0];
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_summingLong_java_util_function_ToLongFunction_mapper_LambdaImpl3 implements Function {
        public Object apply(Object arg0) {
            return Long.valueOf(((long[]) arg0)[0]);
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_toCollection_java_util_function_Supplier_collectionFactory_LambdaImpl0 implements BiConsumer {
        public void accept(Object arg0, Object arg1) {
            ((Collection) arg0).add(arg1);
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_toCollection_java_util_function_Supplier_collectionFactory_LambdaImpl1 implements BinaryOperator {
        public Object apply(Object arg0, Object arg1) {
            return ((Collection) arg0).addAll((Collection) arg1);
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_toConcurrentMap_java_util_function_Function_keyMapper_java_util_function_Function_valueMapper_LambdaImpl0 implements Supplier {
        public Object get() {
            return new ConcurrentHashMap();
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_toConcurrentMap_java_util_function_Function_keyMapper_java_util_function_Function_valueMapper_java_util_function_BinaryOperator_mergeFunction_LambdaImpl0 implements Supplier {
        public Object get() {
            return new ConcurrentHashMap();
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_toConcurrentMap_java_util_function_Function_keyMapper_java_util_function_Function_valueMapper_java_util_function_BinaryOperator_mergeFunction_java_util_function_Supplier_mapSupplier_LambdaImpl0 implements BiConsumer {
        private /* synthetic */ Function val$keyMapper;
        private /* synthetic */ BinaryOperator val$mergeFunction;
        private /* synthetic */ Function val$valueMapper;

        public /* synthetic */ -java_util_stream_Collector_toConcurrentMap_java_util_function_Function_keyMapper_java_util_function_Function_valueMapper_java_util_function_BinaryOperator_mergeFunction_java_util_function_Supplier_mapSupplier_LambdaImpl0(Function function, Function function2, BinaryOperator binaryOperator) {
            this.val$keyMapper = function;
            this.val$valueMapper = function2;
            this.val$mergeFunction = binaryOperator;
        }

        public void accept(Object arg0, Object arg1) {
            ((ConcurrentMap) arg0).merge(this.val$keyMapper.apply(arg1), this.val$valueMapper.apply(arg1), this.val$mergeFunction);
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_toList__LambdaImpl0 implements Supplier {
        public Object get() {
            return new ArrayList();
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_toList__LambdaImpl1 implements BiConsumer {
        public void accept(Object arg0, Object arg1) {
            ((List) arg0).add(arg1);
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_toList__LambdaImpl2 implements BinaryOperator {
        public Object apply(Object arg0, Object arg1) {
            return ((List) arg0).addAll((List) arg1);
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_toMap_java_util_function_Function_keyMapper_java_util_function_Function_valueMapper_LambdaImpl0 implements Supplier {
        public Object get() {
            return new HashMap();
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_toMap_java_util_function_Function_keyMapper_java_util_function_Function_valueMapper_java_util_function_BinaryOperator_mergeFunction_LambdaImpl0 implements Supplier {
        public Object get() {
            return new HashMap();
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_toMap_java_util_function_Function_keyMapper_java_util_function_Function_valueMapper_java_util_function_BinaryOperator_mergeFunction_java_util_function_Supplier_mapSupplier_LambdaImpl0 implements BiConsumer {
        private /* synthetic */ Function val$keyMapper;
        private /* synthetic */ BinaryOperator val$mergeFunction;
        private /* synthetic */ Function val$valueMapper;

        public /* synthetic */ -java_util_stream_Collector_toMap_java_util_function_Function_keyMapper_java_util_function_Function_valueMapper_java_util_function_BinaryOperator_mergeFunction_java_util_function_Supplier_mapSupplier_LambdaImpl0(Function function, Function function2, BinaryOperator binaryOperator) {
            this.val$keyMapper = function;
            this.val$valueMapper = function2;
            this.val$mergeFunction = binaryOperator;
        }

        public void accept(Object arg0, Object arg1) {
            ((Map) arg0).merge(this.val$keyMapper.apply(arg1), this.val$valueMapper.apply(arg1), this.val$mergeFunction);
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_toSet__LambdaImpl0 implements Supplier {
        public Object get() {
            return new HashSet();
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_toSet__LambdaImpl1 implements BiConsumer {
        public void accept(Object arg0, Object arg1) {
            ((Set) arg0).add(arg1);
        }
    }

    final /* synthetic */ class -java_util_stream_Collector_toSet__LambdaImpl2 implements BinaryOperator {
        public Object apply(Object arg0, Object arg1) {
            return ((Set) arg0).addAll((Set) arg1);
        }
    }

    /* renamed from: java.util.stream.Collectors.1OptionalBox */
    class AnonymousClass1OptionalBox implements Consumer<T> {
        boolean present;
        final /* synthetic */ BinaryOperator val$op;
        T value;

        AnonymousClass1OptionalBox(BinaryOperator val$op) {
            this.val$op = val$op;
            this.value = null;
            this.present = false;
        }

        public void accept(T t) {
            if (this.present) {
                this.value = this.val$op.apply(this.value, t);
                return;
            }
            this.value = t;
            this.present = true;
        }
    }

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

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.stream.Collectors.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.stream.Collectors.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.stream.Collectors.<clinit>():void");
    }

    private Collectors() {
    }

    static /* synthetic */ Object -java_util_stream_Collectors_lambda$1(Object u, Object v) {
        throw new IllegalStateException(String.format("Duplicate key %s", u));
    }

    private static <T> BinaryOperator<T> throwingMerger() {
        return new -java_util_function_BinaryOperator_throwingMerger__LambdaImpl0();
    }

    static /* synthetic */ Object -java_util_stream_Collectors_lambda$2(Object i) {
        return i;
    }

    private static <I, R> Function<I, R> castingIdentity() {
        return new -java_util_function_Function_castingIdentity__LambdaImpl0();
    }

    public static <T, C extends Collection<T>> Collector<T, ?, C> toCollection(Supplier<C> collectionFactory) {
        return new CollectorImpl(collectionFactory, new -java_util_stream_Collector_toCollection_java_util_function_Supplier_collectionFactory_LambdaImpl0(), new -java_util_stream_Collector_toCollection_java_util_function_Supplier_collectionFactory_LambdaImpl1(), CH_ID);
    }

    public static <T> Collector<T, ?, List<T>> toList() {
        return new CollectorImpl(new -java_util_stream_Collector_toList__LambdaImpl0(), new -java_util_stream_Collector_toList__LambdaImpl1(), new -java_util_stream_Collector_toList__LambdaImpl2(), CH_ID);
    }

    public static <T> Collector<T, ?, Set<T>> toSet() {
        return new CollectorImpl(new -java_util_stream_Collector_toSet__LambdaImpl0(), new -java_util_stream_Collector_toSet__LambdaImpl1(), new -java_util_stream_Collector_toSet__LambdaImpl2(), CH_UNORDERED_ID);
    }

    public static Collector<CharSequence, ?, String> joining() {
        return new CollectorImpl(new -java_util_stream_Collector_joining__LambdaImpl0(), new -java_util_stream_Collector_joining__LambdaImpl1(), new -java_util_stream_Collector_joining__LambdaImpl2(), new -java_util_stream_Collector_joining__LambdaImpl3(), CH_NOID);
    }

    public static Collector<CharSequence, ?, String> joining(CharSequence delimiter) {
        return joining(delimiter, "", "");
    }

    public static Collector<CharSequence, ?, String> joining(CharSequence delimiter, CharSequence prefix, CharSequence suffix) {
        return new CollectorImpl(new -java_util_stream_Collector_joining_java_lang_CharSequence_delimiter_java_lang_CharSequence_prefix_java_lang_CharSequence_suffix_LambdaImpl0(delimiter, prefix, suffix), new -java_util_stream_Collector_joining_java_lang_CharSequence_delimiter_java_lang_CharSequence_prefix_java_lang_CharSequence_suffix_LambdaImpl1(), new -java_util_stream_Collector_joining_java_lang_CharSequence_delimiter_java_lang_CharSequence_prefix_java_lang_CharSequence_suffix_LambdaImpl2(), new -java_util_stream_Collector_joining_java_lang_CharSequence_delimiter_java_lang_CharSequence_prefix_java_lang_CharSequence_suffix_LambdaImpl3(), CH_NOID);
    }

    private static <K, V, M extends Map<K, V>> BinaryOperator<M> mapMerger(BinaryOperator<V> mergeFunction) {
        return new -java_util_function_BinaryOperator_mapMerger_java_util_function_BinaryOperator_mergeFunction_LambdaImpl0(mergeFunction);
    }

    static /* synthetic */ Map -java_util_stream_Collectors_lambda$19(BinaryOperator mergeFunction, Map m1, Map m2) {
        for (Entry<K, V> e : m2.entrySet()) {
            m1.merge(e.getKey(), e.getValue(), mergeFunction);
        }
        return m1;
    }

    public static <T, U, A, R> Collector<T, ?, R> mapping(Function<? super T, ? extends U> mapper, Collector<? super U, A, R> downstream) {
        return new CollectorImpl(downstream.supplier(), new -java_util_stream_Collector_mapping_java_util_function_Function_mapper_java_util_stream_Collector_downstream_LambdaImpl0(downstream.accumulator(), mapper), downstream.combiner(), downstream.finisher(), downstream.characteristics());
    }

    public static <T, A, R, RR> Collector<T, A, RR> collectingAndThen(Collector<T, A, R> downstream, Function<R, RR> finisher) {
        Set<Characteristics> characteristics;
        Collection characteristics2 = downstream.characteristics();
        if (characteristics2.contains(Characteristics.IDENTITY_FINISH)) {
            if (characteristics2.size() == 1) {
                characteristics = CH_NOID;
            } else {
                characteristics = EnumSet.copyOf(characteristics2);
                characteristics.remove(Characteristics.IDENTITY_FINISH);
                characteristics = Collections.unmodifiableSet(characteristics);
            }
        }
        return new CollectorImpl(downstream.supplier(), downstream.accumulator(), downstream.combiner(), downstream.finisher().andThen(finisher), characteristics);
    }

    public static <T> Collector<T, ?, Long> counting() {
        return reducing(Long.valueOf(0), new -java_util_stream_Collector_counting__LambdaImpl0(), new -java_util_stream_Collector_counting__LambdaImpl1());
    }

    public static <T> Collector<T, ?, Optional<T>> minBy(Comparator<? super T> comparator) {
        return reducing(BinaryOperator.minBy(comparator));
    }

    public static <T> Collector<T, ?, Optional<T>> maxBy(Comparator<? super T> comparator) {
        return reducing(BinaryOperator.maxBy(comparator));
    }

    public static <T> Collector<T, ?, Integer> summingInt(ToIntFunction<? super T> mapper) {
        return new CollectorImpl(new -java_util_stream_Collector_summingInt_java_util_function_ToIntFunction_mapper_LambdaImpl0(), new -java_util_stream_Collector_summingInt_java_util_function_ToIntFunction_mapper_LambdaImpl1(mapper), new -java_util_stream_Collector_summingInt_java_util_function_ToIntFunction_mapper_LambdaImpl2(), new -java_util_stream_Collector_summingInt_java_util_function_ToIntFunction_mapper_LambdaImpl3(), CH_NOID);
    }

    public static <T> Collector<T, ?, Long> summingLong(ToLongFunction<? super T> mapper) {
        return new CollectorImpl(new -java_util_stream_Collector_summingLong_java_util_function_ToLongFunction_mapper_LambdaImpl0(), new -java_util_stream_Collector_summingLong_java_util_function_ToLongFunction_mapper_LambdaImpl1(mapper), new -java_util_stream_Collector_summingLong_java_util_function_ToLongFunction_mapper_LambdaImpl2(), new -java_util_stream_Collector_summingLong_java_util_function_ToLongFunction_mapper_LambdaImpl3(), CH_NOID);
    }

    public static <T> Collector<T, ?, Double> summingDouble(ToDoubleFunction<? super T> mapper) {
        return new CollectorImpl(new -java_util_stream_Collector_summingDouble_java_util_function_ToDoubleFunction_mapper_LambdaImpl0(), new -java_util_stream_Collector_summingDouble_java_util_function_ToDoubleFunction_mapper_LambdaImpl1(mapper), new -java_util_stream_Collector_summingDouble_java_util_function_ToDoubleFunction_mapper_LambdaImpl2(), new -java_util_stream_Collector_summingDouble_java_util_function_ToDoubleFunction_mapper_LambdaImpl3(), CH_NOID);
    }

    static /* synthetic */ void -java_util_stream_Collectors_lambda$32(ToDoubleFunction mapper, double[] a, Object t) {
        sumWithCompensation(a, mapper.applyAsDouble(t));
        a[2] = a[2] + mapper.applyAsDouble(t);
    }

    static /* synthetic */ double[] -java_util_stream_Collectors_lambda$33(double[] a, double[] b) {
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
        return new CollectorImpl(new -java_util_stream_Collector_averagingInt_java_util_function_ToIntFunction_mapper_LambdaImpl0(), new -java_util_stream_Collector_averagingInt_java_util_function_ToIntFunction_mapper_LambdaImpl1(mapper), new -java_util_stream_Collector_averagingInt_java_util_function_ToIntFunction_mapper_LambdaImpl2(), new -java_util_stream_Collector_averagingInt_java_util_function_ToIntFunction_mapper_LambdaImpl3(), CH_NOID);
    }

    static /* synthetic */ void -java_util_stream_Collectors_lambda$36(ToIntFunction mapper, long[] a, Object t) {
        a[0] = a[0] + ((long) mapper.applyAsInt(t));
        a[1] = a[1] + 1;
    }

    static /* synthetic */ long[] -java_util_stream_Collectors_lambda$37(long[] a, long[] b) {
        a[0] = a[0] + b[0];
        a[1] = a[1] + b[1];
        return a;
    }

    static /* synthetic */ Double -java_util_stream_Collectors_lambda$38(long[] a) {
        return Double.valueOf(a[1] == 0 ? 0.0d : ((double) a[0]) / ((double) a[1]));
    }

    public static <T> Collector<T, ?, Double> averagingLong(ToLongFunction<? super T> mapper) {
        return new CollectorImpl(new -java_util_stream_Collector_averagingLong_java_util_function_ToLongFunction_mapper_LambdaImpl0(), new -java_util_stream_Collector_averagingLong_java_util_function_ToLongFunction_mapper_LambdaImpl1(mapper), new -java_util_stream_Collector_averagingLong_java_util_function_ToLongFunction_mapper_LambdaImpl2(), new -java_util_stream_Collector_averagingLong_java_util_function_ToLongFunction_mapper_LambdaImpl3(), CH_NOID);
    }

    static /* synthetic */ void -java_util_stream_Collectors_lambda$40(ToLongFunction mapper, long[] a, Object t) {
        a[0] = a[0] + mapper.applyAsLong(t);
        a[1] = a[1] + 1;
    }

    static /* synthetic */ long[] -java_util_stream_Collectors_lambda$41(long[] a, long[] b) {
        a[0] = a[0] + b[0];
        a[1] = a[1] + b[1];
        return a;
    }

    static /* synthetic */ Double -java_util_stream_Collectors_lambda$42(long[] a) {
        return Double.valueOf(a[1] == 0 ? 0.0d : ((double) a[0]) / ((double) a[1]));
    }

    public static <T> Collector<T, ?, Double> averagingDouble(ToDoubleFunction<? super T> mapper) {
        return new CollectorImpl(new -java_util_stream_Collector_averagingDouble_java_util_function_ToDoubleFunction_mapper_LambdaImpl0(), new -java_util_stream_Collector_averagingDouble_java_util_function_ToDoubleFunction_mapper_LambdaImpl1(mapper), new -java_util_stream_Collector_averagingDouble_java_util_function_ToDoubleFunction_mapper_LambdaImpl2(), new -java_util_stream_Collector_averagingDouble_java_util_function_ToDoubleFunction_mapper_LambdaImpl3(), CH_NOID);
    }

    static /* synthetic */ void -java_util_stream_Collectors_lambda$44(ToDoubleFunction mapper, double[] a, Object t) {
        sumWithCompensation(a, mapper.applyAsDouble(t));
        a[2] = a[2] + 1.0d;
        a[3] = a[3] + mapper.applyAsDouble(t);
    }

    static /* synthetic */ double[] -java_util_stream_Collectors_lambda$45(double[] a, double[] b) {
        sumWithCompensation(a, b[0]);
        sumWithCompensation(a, b[1]);
        a[2] = a[2] + b[2];
        a[3] = a[3] + b[3];
        return a;
    }

    static /* synthetic */ Double -java_util_stream_Collectors_lambda$46(double[] a) {
        double d = 0.0d;
        if (a[2] != 0.0d) {
            d = computeFinalSum(a) / a[2];
        }
        return Double.valueOf(d);
    }

    public static <T> Collector<T, ?, T> reducing(T identity, BinaryOperator<T> op) {
        return new CollectorImpl(boxSupplier(identity), new -java_util_stream_Collector_reducing_java_lang_Object_identity_java_util_function_BinaryOperator_op_LambdaImpl0(op), new -java_util_stream_Collector_reducing_java_lang_Object_identity_java_util_function_BinaryOperator_op_LambdaImpl1(op), new -java_util_stream_Collector_reducing_java_lang_Object_identity_java_util_function_BinaryOperator_op_LambdaImpl2(), CH_NOID);
    }

    private static <T> Supplier<T[]> boxSupplier(T identity) {
        return new -java_util_function_Supplier_boxSupplier_java_lang_Object_identity_LambdaImpl0(identity);
    }

    public static <T> Collector<T, ?, Optional<T>> reducing(BinaryOperator<T> op) {
        return new CollectorImpl(new -java_util_stream_Collector_reducing_java_util_function_BinaryOperator_op_LambdaImpl0(op), new -java_util_stream_Collector_reducing_java_util_function_BinaryOperator_op_LambdaImpl1(), new -java_util_stream_Collector_reducing_java_util_function_BinaryOperator_op_LambdaImpl2(), new -java_util_stream_Collector_reducing_java_util_function_BinaryOperator_op_LambdaImpl3(), CH_NOID);
    }

    static /* synthetic */ AnonymousClass1OptionalBox -java_util_stream_Collectors_lambda$53(AnonymousClass1OptionalBox a, AnonymousClass1OptionalBox b) {
        if (b.present) {
            a.accept(b.value);
        }
        return a;
    }

    public static <T, U> Collector<T, ?, U> reducing(U identity, Function<? super T, ? extends U> mapper, BinaryOperator<U> op) {
        return new CollectorImpl(boxSupplier(identity), new -java_util_stream_Collector_reducing_java_lang_Object_identity_java_util_function_Function_mapper_java_util_function_BinaryOperator_op_LambdaImpl0(op, mapper), new -java_util_stream_Collector_reducing_java_lang_Object_identity_java_util_function_Function_mapper_java_util_function_BinaryOperator_op_LambdaImpl1(op), new -java_util_stream_Collector_reducing_java_lang_Object_identity_java_util_function_Function_mapper_java_util_function_BinaryOperator_op_LambdaImpl2(), CH_NOID);
    }

    public static <T, K> Collector<T, ?, Map<K, List<T>>> groupingBy(Function<? super T, ? extends K> classifier) {
        return groupingBy(classifier, toList());
    }

    public static <T, K, A, D> Collector<T, ?, Map<K, D>> groupingBy(Function<? super T, ? extends K> classifier, Collector<? super T, A, D> downstream) {
        return groupingBy(classifier, new -java_util_stream_Collector_groupingBy_java_util_function_Function_classifier_java_util_stream_Collector_downstream_LambdaImpl0(), downstream);
    }

    public static <T, K, D, A, M extends Map<K, D>> Collector<T, ?, M> groupingBy(Function<? super T, ? extends K> classifier, Supplier<M> mapFactory, Collector<? super T, A, D> downstream) {
        BiConsumer<Map<K, A>, T> accumulator = new -java_util_stream_Collector_groupingBy_java_util_function_Function_classifier_java_util_function_Supplier_mapFactory_java_util_stream_Collector_downstream_LambdaImpl0(classifier, downstream.supplier(), downstream.accumulator());
        BinaryOperator<Map<K, A>> merger = mapMerger(downstream.combiner());
        Supplier<Map<K, A>> mangledFactory = mapFactory;
        if (downstream.characteristics().contains(Characteristics.IDENTITY_FINISH)) {
            return new CollectorImpl(mapFactory, accumulator, merger, CH_ID);
        }
        return new CollectorImpl(mapFactory, accumulator, merger, new -java_util_stream_Collector_groupingBy_java_util_function_Function_classifier_java_util_function_Supplier_mapFactory_java_util_stream_Collector_downstream_LambdaImpl1(downstream.finisher()), CH_NOID);
    }

    static /* synthetic */ Map -java_util_stream_Collectors_lambda$61(Function downstreamFinisher, Map intermediate) {
        intermediate.replaceAll(new Collectors$-java_util_Map_-java_util_stream_Collectors_lambda$61_java_util_function_Function_downstreamFinisher_java_util_Map_intermediate_LambdaImpl0(downstreamFinisher));
        M castResult = intermediate;
        return intermediate;
    }

    public static <T, K> Collector<T, ?, ConcurrentMap<K, List<T>>> groupingByConcurrent(Function<? super T, ? extends K> classifier) {
        return groupingByConcurrent(classifier, new -java_util_stream_Collector_groupingByConcurrent_java_util_function_Function_classifier_LambdaImpl0(), toList());
    }

    public static <T, K, A, D> Collector<T, ?, ConcurrentMap<K, D>> groupingByConcurrent(Function<? super T, ? extends K> classifier, Collector<? super T, A, D> downstream) {
        return groupingByConcurrent(classifier, new -java_util_stream_Collector_groupingByConcurrent_java_util_function_Function_classifier_java_util_stream_Collector_downstream_LambdaImpl0(), downstream);
    }

    public static <T, K, A, D, M extends ConcurrentMap<K, D>> Collector<T, ?, M> groupingByConcurrent(Function<? super T, ? extends K> classifier, Supplier<M> mapFactory, Collector<? super T, A, D> downstream) {
        BiConsumer<ConcurrentMap<K, A>, T> accumulator;
        Supplier<A> downstreamSupplier = downstream.supplier();
        BiConsumer<A, ? super T> downstreamAccumulator = downstream.accumulator();
        BinaryOperator<ConcurrentMap<K, A>> merger = mapMerger(downstream.combiner());
        Supplier<ConcurrentMap<K, A>> mangledFactory = mapFactory;
        if (downstream.characteristics().contains(Characteristics.CONCURRENT)) {
            accumulator = new -java_util_stream_Collector_groupingByConcurrent_java_util_function_Function_classifier_java_util_function_Supplier_mapFactory_java_util_stream_Collector_downstream_LambdaImpl0(classifier, downstreamSupplier, downstreamAccumulator);
        } else {
            accumulator = new -java_util_stream_Collector_groupingByConcurrent_java_util_function_Function_classifier_java_util_function_Supplier_mapFactory_java_util_stream_Collector_downstream_LambdaImpl1(classifier, downstreamSupplier, downstreamAccumulator);
        }
        if (downstream.characteristics().contains(Characteristics.IDENTITY_FINISH)) {
            return new CollectorImpl(mapFactory, accumulator, merger, CH_CONCURRENT_ID);
        }
        return new CollectorImpl(mapFactory, accumulator, merger, new -java_util_stream_Collector_groupingByConcurrent_java_util_function_Function_classifier_java_util_function_Supplier_mapFactory_java_util_stream_Collector_downstream_LambdaImpl2(downstream.finisher()), CH_CONCURRENT_NOID);
    }

    static /* synthetic */ void -java_util_stream_Collectors_lambda$67(Function classifier, Supplier downstreamSupplier, BiConsumer downstreamAccumulator, ConcurrentMap m, Object t) {
        A resultContainer = m.computeIfAbsent(Objects.requireNonNull(classifier.apply(t), "element cannot be mapped to a null key"), new Collectors$-void_-java_util_stream_Collectors_lambda$67_java_util_function_Function_classifier_java_util_function_Supplier_downstreamSupplier_java_util_function_BiConsumer_downstreamAccumulator_java_util_concurrent_ConcurrentMap_m_java_lang_Object_t_LambdaImpl0(downstreamSupplier));
        synchronized (resultContainer) {
            downstreamAccumulator.accept(resultContainer, t);
        }
    }

    static /* synthetic */ ConcurrentMap -java_util_stream_Collectors_lambda$69(Function downstreamFinisher, ConcurrentMap intermediate) {
        intermediate.replaceAll(new Collectors$-java_util_concurrent_ConcurrentMap_-java_util_stream_Collectors_lambda$69_java_util_function_Function_downstreamFinisher_java_util_concurrent_ConcurrentMap_intermediate_LambdaImpl0(downstreamFinisher));
        M castResult = intermediate;
        return intermediate;
    }

    public static <T> Collector<T, ?, Map<Boolean, List<T>>> partitioningBy(Predicate<? super T> predicate) {
        return partitioningBy(predicate, toList());
    }

    public static <T, D, A> Collector<T, ?, Map<Boolean, D>> partitioningBy(Predicate<? super T> predicate, Collector<? super T, A, D> downstream) {
        BiConsumer<Partition<A>, T> accumulator = new -java_util_stream_Collector_partitioningBy_java_util_function_Predicate_predicate_java_util_stream_Collector_downstream_LambdaImpl0(downstream.accumulator(), predicate);
        BinaryOperator<Partition<A>> merger = new -java_util_stream_Collector_partitioningBy_java_util_function_Predicate_predicate_java_util_stream_Collector_downstream_LambdaImpl1(downstream.combiner());
        Supplier<Partition<A>> supplier = new -java_util_stream_Collector_partitioningBy_java_util_function_Predicate_predicate_java_util_stream_Collector_downstream_LambdaImpl2(downstream);
        if (downstream.characteristics().contains(Characteristics.IDENTITY_FINISH)) {
            return new CollectorImpl(supplier, accumulator, merger, CH_ID);
        }
        return new CollectorImpl(supplier, accumulator, merger, new -java_util_stream_Collector_partitioningBy_java_util_function_Predicate_predicate_java_util_stream_Collector_downstream_LambdaImpl3(downstream), CH_NOID);
    }

    static /* synthetic */ void -java_util_stream_Collectors_lambda$71(BiConsumer downstreamAccumulator, Predicate predicate, Partition result, Object t) {
        downstreamAccumulator.accept(predicate.test(t) ? result.forTrue : result.forFalse, t);
    }

    public static <T, K, U> Collector<T, ?, Map<K, U>> toMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper) {
        return toMap(keyMapper, valueMapper, throwingMerger(), new -java_util_stream_Collector_toMap_java_util_function_Function_keyMapper_java_util_function_Function_valueMapper_LambdaImpl0());
    }

    public static <T, K, U> Collector<T, ?, Map<K, U>> toMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper, BinaryOperator<U> mergeFunction) {
        return toMap(keyMapper, valueMapper, mergeFunction, new -java_util_stream_Collector_toMap_java_util_function_Function_keyMapper_java_util_function_Function_valueMapper_java_util_function_BinaryOperator_mergeFunction_LambdaImpl0());
    }

    public static <T, K, U, M extends Map<K, U>> Collector<T, ?, M> toMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper, BinaryOperator<U> mergeFunction, Supplier<M> mapSupplier) {
        return new CollectorImpl(mapSupplier, new -java_util_stream_Collector_toMap_java_util_function_Function_keyMapper_java_util_function_Function_valueMapper_java_util_function_BinaryOperator_mergeFunction_java_util_function_Supplier_mapSupplier_LambdaImpl0(keyMapper, valueMapper, mergeFunction), mapMerger(mergeFunction), CH_ID);
    }

    public static <T, K, U> Collector<T, ?, ConcurrentMap<K, U>> toConcurrentMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper) {
        return toConcurrentMap(keyMapper, valueMapper, throwingMerger(), new -java_util_stream_Collector_toConcurrentMap_java_util_function_Function_keyMapper_java_util_function_Function_valueMapper_LambdaImpl0());
    }

    public static <T, K, U> Collector<T, ?, ConcurrentMap<K, U>> toConcurrentMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper, BinaryOperator<U> mergeFunction) {
        return toConcurrentMap(keyMapper, valueMapper, mergeFunction, new -java_util_stream_Collector_toConcurrentMap_java_util_function_Function_keyMapper_java_util_function_Function_valueMapper_java_util_function_BinaryOperator_mergeFunction_LambdaImpl0());
    }

    public static <T, K, U, M extends ConcurrentMap<K, U>> Collector<T, ?, M> toConcurrentMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends U> valueMapper, BinaryOperator<U> mergeFunction, Supplier<M> mapSupplier) {
        return new CollectorImpl(mapSupplier, new -java_util_stream_Collector_toConcurrentMap_java_util_function_Function_keyMapper_java_util_function_Function_valueMapper_java_util_function_BinaryOperator_mergeFunction_java_util_function_Supplier_mapSupplier_LambdaImpl0(keyMapper, valueMapper, mergeFunction), mapMerger(mergeFunction), CH_CONCURRENT_ID);
    }

    public static <T> Collector<T, ?, IntSummaryStatistics> summarizingInt(ToIntFunction<? super T> mapper) {
        return new CollectorImpl(new -java_util_stream_Collector_summarizingInt_java_util_function_ToIntFunction_mapper_LambdaImpl0(), new -java_util_stream_Collector_summarizingInt_java_util_function_ToIntFunction_mapper_LambdaImpl1(mapper), new -java_util_stream_Collector_summarizingInt_java_util_function_ToIntFunction_mapper_LambdaImpl2(), CH_ID);
    }

    public static <T> Collector<T, ?, LongSummaryStatistics> summarizingLong(ToLongFunction<? super T> mapper) {
        return new CollectorImpl(new -java_util_stream_Collector_summarizingLong_java_util_function_ToLongFunction_mapper_LambdaImpl0(), new -java_util_stream_Collector_summarizingLong_java_util_function_ToLongFunction_mapper_LambdaImpl1(mapper), new -java_util_stream_Collector_summarizingLong_java_util_function_ToLongFunction_mapper_LambdaImpl2(), CH_ID);
    }

    public static <T> Collector<T, ?, DoubleSummaryStatistics> summarizingDouble(ToDoubleFunction<? super T> mapper) {
        return new CollectorImpl(new -java_util_stream_Collector_summarizingDouble_java_util_function_ToDoubleFunction_mapper_LambdaImpl0(), new -java_util_stream_Collector_summarizingDouble_java_util_function_ToDoubleFunction_mapper_LambdaImpl1(mapper), new -java_util_stream_Collector_summarizingDouble_java_util_function_ToDoubleFunction_mapper_LambdaImpl2(), CH_ID);
    }
}
