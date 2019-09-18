package java.util.stream;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.stream.DistinctOps;
import java.util.stream.ReferencePipeline;
import java.util.stream.Sink;
import java.util.stream.StreamSpliterators;

final class DistinctOps {
    private DistinctOps() {
    }

    static <T> ReferencePipeline<T, T> makeRef(AbstractPipeline<?, T, ?> upstream) {
        return new ReferencePipeline.StatefulOp<T, T>(upstream, StreamShape.REFERENCE, StreamOpFlag.IS_DISTINCT | StreamOpFlag.NOT_SIZED) {
            /* access modifiers changed from: package-private */
            public <P_IN> Node<T> reduce(PipelineHelper<T> helper, Spliterator<P_IN> spliterator) {
                return Nodes.node(ReduceOps.makeRef($$Lambda$VQnU3Jki1RCSS5BYg_Kf6hQAY.INSTANCE, $$Lambda$zcFI7bYCRDtB1UMy72aPExbc6R4.INSTANCE, $$Lambda$r6LgDiay3Ow5w51ifJiV4dn8S84.INSTANCE).evaluateParallel(helper, spliterator));
            }

            /* JADX WARNING: type inference failed for: r7v0, types: [java.util.Spliterator, java.util.Spliterator<P_IN>] */
            /* JADX WARNING: type inference failed for: r8v0, types: [java.util.function.IntFunction<T[]>, java.util.function.IntFunction] */
            /* JADX WARNING: Unknown variable types count: 2 */
            public <P_IN> Node<T> opEvaluateParallel(PipelineHelper<T> helper, Spliterator<P_IN> r7, IntFunction<T[]> r8) {
                if (StreamOpFlag.DISTINCT.isKnown(helper.getStreamAndOpFlags())) {
                    return helper.evaluate(r7, false, r8);
                }
                if (StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags())) {
                    return reduce(helper, r7);
                }
                AtomicBoolean seenNull = new AtomicBoolean(false);
                ConcurrentHashMap<T, Boolean> map = new ConcurrentHashMap<>();
                ForEachOps.makeRef(new Consumer(map) {
                    private final /* synthetic */ ConcurrentHashMap f$1;

                    {
                        this.f$1 = r2;
                    }

                    public final void accept(Object obj) {
                        DistinctOps.AnonymousClass1.lambda$opEvaluateParallel$0(AtomicBoolean.this, this.f$1, obj);
                    }
                }, false).evaluateParallel(helper, r7);
                Set<T> keys = map.keySet();
                if (seenNull.get()) {
                    keys = new HashSet<>((Collection<? extends T>) keys);
                    keys.add(null);
                }
                return Nodes.node(keys);
            }

            static /* synthetic */ void lambda$opEvaluateParallel$0(AtomicBoolean seenNull, ConcurrentHashMap map, Object t) {
                if (t == null) {
                    seenNull.set(true);
                } else {
                    map.putIfAbsent(t, Boolean.TRUE);
                }
            }

            /* JADX WARNING: type inference failed for: r4v0, types: [java.util.Spliterator, java.util.Spliterator<P_IN>] */
            /* JADX WARNING: Unknown variable types count: 1 */
            public <P_IN> Spliterator<T> opEvaluateParallelLazy(PipelineHelper<T> helper, Spliterator<P_IN> r4) {
                if (StreamOpFlag.DISTINCT.isKnown(helper.getStreamAndOpFlags())) {
                    return helper.wrapSpliterator(r4);
                }
                if (StreamOpFlag.ORDERED.isKnown(helper.getStreamAndOpFlags())) {
                    return reduce(helper, r4).spliterator();
                }
                return new StreamSpliterators.DistinctSpliterator(helper.wrapSpliterator(r4));
            }

            public Sink<T> opWrapSink(int flags, Sink<T> sink) {
                Objects.requireNonNull(sink);
                if (StreamOpFlag.DISTINCT.isKnown(flags)) {
                    return sink;
                }
                return StreamOpFlag.SORTED.isKnown(flags) ? new Sink.ChainedReference<T, T>(sink) {
                    T lastSeen;
                    boolean seenNull;

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
                        if (t == null) {
                            if (!this.seenNull) {
                                this.seenNull = true;
                                Sink sink = this.downstream;
                                this.lastSeen = null;
                                sink.accept(null);
                            }
                        } else if (this.lastSeen == null || !t.equals(this.lastSeen)) {
                            Sink sink2 = this.downstream;
                            this.lastSeen = t;
                            sink2.accept(t);
                        }
                    }
                } : new Sink.ChainedReference<T, T>(sink) {
                    Set<T> seen;

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
                };
            }
        };
    }
}
