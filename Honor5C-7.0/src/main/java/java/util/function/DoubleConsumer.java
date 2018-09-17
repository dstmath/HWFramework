package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface DoubleConsumer {

    final /* synthetic */ class -java_util_function_DoubleConsumer_andThen_java_util_function_DoubleConsumer_after_LambdaImpl0 implements DoubleConsumer {
        private /* synthetic */ DoubleConsumer val$after;
        private /* synthetic */ DoubleConsumer val$this;

        public /* synthetic */ -java_util_function_DoubleConsumer_andThen_java_util_function_DoubleConsumer_after_LambdaImpl0(DoubleConsumer doubleConsumer, DoubleConsumer doubleConsumer2) {
            this.val$this = doubleConsumer;
            this.val$after = doubleConsumer2;
        }

        public void accept(double arg0) {
            this.val$this.-java_util_function_DoubleConsumer_lambda$1(this.val$after, arg0);
        }
    }

    void accept(double d);

    DoubleConsumer andThen(DoubleConsumer after) {
        Objects.requireNonNull(after);
        return new -java_util_function_DoubleConsumer_andThen_java_util_function_DoubleConsumer_after_LambdaImpl0(this, after);
    }

    /* synthetic */ void -java_util_function_DoubleConsumer_lambda$1(DoubleConsumer after, double t) {
        accept(t);
        after.accept(t);
    }
}
