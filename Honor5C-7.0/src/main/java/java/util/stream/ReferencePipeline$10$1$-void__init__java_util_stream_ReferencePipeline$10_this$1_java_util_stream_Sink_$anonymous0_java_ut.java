package java.util.stream;

import java.util.function.LongConsumer;

final /* synthetic */ class ReferencePipeline$10$1$-void__init__java_util_stream_ReferencePipeline$10_this$1_java_util_stream_Sink_$anonymous0_java_util_function_Function_val$mapper_LambdaImpl0 implements LongConsumer {
    private /* synthetic */ Sink val$-lambdaCtx;

    public /* synthetic */ ReferencePipeline$10$1$-void__init__java_util_stream_ReferencePipeline$10_this$1_java_util_stream_Sink_$anonymous0_java_util_function_Function_val$mapper_LambdaImpl0(Sink sink) {
        this.val$-lambdaCtx = sink;
    }

    public void accept(long arg0) {
        this.val$-lambdaCtx.accept(arg0);
    }
}
