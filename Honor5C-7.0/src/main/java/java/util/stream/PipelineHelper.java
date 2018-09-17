package java.util.stream;

import java.util.Spliterator;
import java.util.function.IntFunction;
import java.util.stream.Node.Builder;

public abstract class PipelineHelper<P_OUT> {
    abstract <P_IN> void copyInto(Sink<P_IN> sink, Spliterator<P_IN> spliterator);

    abstract <P_IN> void copyIntoWithCancel(Sink<P_IN> sink, Spliterator<P_IN> spliterator);

    public abstract <P_IN> Node<P_OUT> evaluate(Spliterator<P_IN> spliterator, boolean z, IntFunction<P_OUT[]> intFunction);

    abstract <P_IN> long exactOutputSizeIfKnown(Spliterator<P_IN> spliterator);

    abstract StreamShape getSourceShape();

    public abstract int getStreamAndOpFlags();

    abstract Builder<P_OUT> makeNodeBuilder(long j, IntFunction<P_OUT[]> intFunction);

    abstract <P_IN, S extends Sink<P_OUT>> S wrapAndCopyInto(S s, Spliterator<P_IN> spliterator);

    public abstract <P_IN> Sink<P_IN> wrapSink(Sink<P_OUT> sink);

    abstract <P_IN> Spliterator<P_OUT> wrapSpliterator(Spliterator<P_IN> spliterator);
}
