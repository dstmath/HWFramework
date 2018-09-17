package java.util.stream;

import java.util.function.BiFunction;
import java.util.function.Function;

final /* synthetic */ class Collectors$-java_util_Map_-java_util_stream_Collectors_lambda$61_java_util_function_Function_downstreamFinisher_java_util_Map_intermediate_LambdaImpl0 implements BiFunction {
    private /* synthetic */ Function val$downstreamFinisher;

    public /* synthetic */ Collectors$-java_util_Map_-java_util_stream_Collectors_lambda$61_java_util_function_Function_downstreamFinisher_java_util_Map_intermediate_LambdaImpl0(Function function) {
        this.val$downstreamFinisher = function;
    }

    public Object apply(Object arg0, Object arg1) {
        return this.val$downstreamFinisher.apply(arg1);
    }
}
