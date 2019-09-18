package java.util.stream;

import java.util.Spliterator;
import java.util.function.IntFunction;
import java.util.stream.Node;

public abstract class PipelineHelper<P_OUT> {
    /* access modifiers changed from: package-private */
    public abstract <P_IN> void copyInto(Sink<P_IN> sink, Spliterator<P_IN> spliterator);

    /* access modifiers changed from: package-private */
    public abstract <P_IN> void copyIntoWithCancel(Sink<P_IN> sink, Spliterator<P_IN> spliterator);

    public abstract <P_IN> Node<P_OUT> evaluate(Spliterator<P_IN> spliterator, boolean z, IntFunction<P_OUT[]> intFunction);

    /* access modifiers changed from: package-private */
    public abstract <P_IN> long exactOutputSizeIfKnown(Spliterator<P_IN> spliterator);

    /* access modifiers changed from: package-private */
    public abstract StreamShape getSourceShape();

    public abstract int getStreamAndOpFlags();

    /* access modifiers changed from: package-private */
    public abstract Node.Builder<P_OUT> makeNodeBuilder(long j, IntFunction<P_OUT[]> intFunction);

    /* access modifiers changed from: package-private */
    public abstract <P_IN, S extends Sink<P_OUT>> S wrapAndCopyInto(S s, Spliterator<P_IN> spliterator);

    public abstract <P_IN> Sink<P_IN> wrapSink(Sink<P_OUT> sink);

    /* access modifiers changed from: package-private */
    public abstract <P_IN> Spliterator<P_OUT> wrapSpliterator(Spliterator<P_IN> spliterator);
}
