package java.util.stream;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.ReferencePipeline.StatefulOp;
import java.util.stream.Sink.ChainedReference;

final class DistinctOps {

    /* renamed from: java.util.stream.DistinctOps.1 */
    static class AnonymousClass1 extends StatefulOp<T, T> {

        final /* synthetic */ class -java_util_stream_Node_opEvaluateParallel_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_java_util_function_IntFunction_generator_LambdaImpl0 implements Consumer {
            private /* synthetic */ ConcurrentHashMap val$map;
            private /* synthetic */ AtomicBoolean val$seenNull;

            public /* synthetic */ -java_util_stream_Node_opEvaluateParallel_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_java_util_function_IntFunction_generator_LambdaImpl0(AtomicBoolean atomicBoolean, ConcurrentHashMap concurrentHashMap) {
                this.val$seenNull = atomicBoolean;
                this.val$map = concurrentHashMap;
            }

            public void accept(Object arg0) {
                AnonymousClass1.-java_util_stream_DistinctOps$1_lambda$4(this.val$seenNull, this.val$map, arg0);
            }
        }

        final /* synthetic */ class -java_util_stream_Node_reduce_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_LambdaImpl0 implements Supplier {
            public Object get() {
                return new LinkedHashSet();
            }
        }

        final /* synthetic */ class -java_util_stream_Node_reduce_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_LambdaImpl1 implements BiConsumer {
            public void accept(Object arg0, Object arg1) {
                ((LinkedHashSet) arg0).add(arg1);
            }
        }

        final /* synthetic */ class -java_util_stream_Node_reduce_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_LambdaImpl2 implements BiConsumer {
            public void accept(Object arg0, Object arg1) {
                ((LinkedHashSet) arg0).addAll((LinkedHashSet) arg1);
            }
        }

        /* renamed from: java.util.stream.DistinctOps.1.1 */
        class AnonymousClass1 extends ChainedReference<T, T> {
            T lastSeen;
            boolean seenNull;

            AnonymousClass1(Sink $anonymous0) {
                super($anonymous0);
            }

            public void begin(long size) {
                this.seenNull = false;
                this.lastSeen = null;
                this.downstream.begin(-1);
            }

            public void end() {
                this.seenNull = false;
                this.lastSeen = null;
                this.downstream.end();
            }

            public void accept(T t) {
                Sink sink;
                if (t == null) {
                    if (!this.seenNull) {
                        this.seenNull = true;
                        sink = this.downstream;
                        this.lastSeen = null;
                        sink.accept(null);
                    }
                } else if (this.lastSeen == null || !t.equals(this.lastSeen)) {
                    sink = this.downstream;
                    this.lastSeen = t;
                    sink.accept(t);
                }
            }
        }

        /* renamed from: java.util.stream.DistinctOps.1.2 */
        class AnonymousClass2 extends ChainedReference<T, T> {
            Set<T> seen;

            AnonymousClass2(Sink $anonymous0) {
                super($anonymous0);
            }

            public void begin(long size) {
                this.seen = new HashSet();
                this.downstream.begin(-1);
            }

            public void end() {
                this.seen = null;
                this.downstream.end();
            }

            public void accept(T t) {
                if (!this.seen.contains(t)) {
                    this.seen.add(t);
                    this.downstream.accept(t);
                }
            }
        }

        AnonymousClass1(AbstractPipeline $anonymous0, StreamShape $anonymous1, int $anonymous2) {
            super($anonymous0, $anonymous1, $anonymous2);
        }

        <P_IN> Node<T> reduce(PipelineHelper<T> helper, Spliterator<P_IN> spliterator) {
            return Nodes.node((Collection) ReduceOps.makeRef(new -java_util_stream_Node_reduce_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_LambdaImpl0(), new -java_util_stream_Node_reduce_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_LambdaImpl1(), new -java_util_stream_Node_reduce_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_LambdaImpl2()).evaluateParallel(helper, spliterator));
        }

        public <P_IN> Node<T> opEvaluateParallel(PipelineHelper<T> helper, Spliterator<P_IN> spliterator, IntFunction<T[]> generator) {
            if (StreamOpFlag.DISTINCT.isKnown(helper.getStreamAndOpFlags())) {
                return helper.evaluate(spliterator, false, generator);
            }
            if (StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags())) {
                return reduce(helper, spliterator);
            }
            AtomicBoolean seenNull = new AtomicBoolean(false);
            ConcurrentHashMap<T, Boolean> map = new ConcurrentHashMap();
            ForEachOps.makeRef(new -java_util_stream_Node_opEvaluateParallel_java_util_stream_PipelineHelper_helper_java_util_Spliterator_spliterator_java_util_function_IntFunction_generator_LambdaImpl0(seenNull, map), false).evaluateParallel(helper, spliterator);
            Collection keys = map.keySet();
            if (seenNull.get()) {
                Set<T> keys2 = new HashSet(keys);
                keys2.add(null);
                keys = keys2;
            }
            return Nodes.node(keys);
        }

        static /* synthetic */ void -java_util_stream_DistinctOps$1_lambda$4(AtomicBoolean seenNull, ConcurrentHashMap map, Object t) {
            if (t == null) {
                seenNull.set(true);
            } else {
                map.putIfAbsent(t, Boolean.TRUE);
            }
        }

        public <P_IN> Spliterator<T> opEvaluateParallelLazy(PipelineHelper<T> helper, Spliterator<P_IN> spliterator) {
            if (StreamOpFlag.DISTINCT.isKnown(helper.getStreamAndOpFlags())) {
                return helper.wrapSpliterator(spliterator);
            }
            if (StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags())) {
                return reduce(helper, spliterator).spliterator();
            }
            return new DistinctSpliterator(helper.wrapSpliterator(spliterator));
        }

        public Sink<T> opWrapSink(int flags, Sink<T> sink) {
            Objects.requireNonNull(sink);
            if (StreamOpFlag.DISTINCT.isKnown(flags)) {
                return sink;
            }
            return StreamOpFlag.SORTED.isKnown(flags) ? new AnonymousClass1(sink) : new AnonymousClass2(sink);
        }
    }

    private DistinctOps() {
    }

    static <T> ReferencePipeline<T, T> makeRef(AbstractPipeline<?, T, ?> upstream) {
        return new AnonymousClass1(upstream, StreamShape.REFERENCE, StreamOpFlag.IS_DISTINCT | StreamOpFlag.NOT_SIZED);
    }
}
