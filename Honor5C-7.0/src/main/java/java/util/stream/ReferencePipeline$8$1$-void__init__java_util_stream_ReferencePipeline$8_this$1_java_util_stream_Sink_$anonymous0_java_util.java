package java.util.stream;

import java.util.function.IntConsumer;

final /* synthetic */ class ReferencePipeline$8$1$-void__init__java_util_stream_ReferencePipeline$8_this$1_java_util_stream_Sink_$anonymous0_java_util_function_Function_val$mapper_LambdaImpl0 implements IntConsumer {
    private /* synthetic */ Sink val$-lambdaCtx;

    public /* synthetic */ ReferencePipeline$8$1$-void__init__java_util_stream_ReferencePipeline$8_this$1_java_util_stream_Sink_$anonymous0_java_util_function_Function_val$mapper_LambdaImpl0(Sink sink) {
        this.val$-lambdaCtx = sink;
    }

    public void accept(int arg0) {
        this.val$-lambdaCtx.accept(arg0);
    }
}
