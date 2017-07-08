package java.util.stream;

import java.util.function.Function;
import java.util.function.Supplier;

final /* synthetic */ class Collectors$-void_-java_util_stream_Collectors_lambda$65_java_util_function_Function_classifier_java_util_function_Supplier_downstreamSupplier_java_util_function_BiConsumer_downstreamAccumulator_java_util_concurrent_ConcurrentMap_m_java_lang_Object_t_LambdaImpl0 implements Function {
    private /* synthetic */ Supplier val$downstreamSupplier;

    public /* synthetic */ Collectors$-void_-java_util_stream_Collectors_lambda$65_java_util_function_Function_classifier_java_util_function_Supplier_downstreamSupplier_java_util_function_BiConsumer_downstreamAccumulator_java_util_concurrent_ConcurrentMap_m_java_lang_Object_t_LambdaImpl0(Supplier supplier) {
        this.val$downstreamSupplier = supplier;
    }

    public Object apply(Object arg0) {
        return this.val$downstreamSupplier.get();
    }
}
