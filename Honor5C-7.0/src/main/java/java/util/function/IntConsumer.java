package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface IntConsumer {

    final /* synthetic */ class -java_util_function_IntConsumer_andThen_java_util_function_IntConsumer_after_LambdaImpl0 implements IntConsumer {
        private /* synthetic */ IntConsumer val$after;
        private /* synthetic */ IntConsumer val$this;

        public /* synthetic */ -java_util_function_IntConsumer_andThen_java_util_function_IntConsumer_after_LambdaImpl0(IntConsumer intConsumer, IntConsumer intConsumer2) {
            this.val$this = intConsumer;
            this.val$after = intConsumer2;
        }

        public void accept(int arg0) {
            this.val$this.-java_util_function_IntConsumer_lambda$1(this.val$after, arg0);
        }
    }

    void accept(int i);

    IntConsumer andThen(IntConsumer after) {
        Objects.requireNonNull(after);
        return new -java_util_function_IntConsumer_andThen_java_util_function_IntConsumer_after_LambdaImpl0(this, after);
    }

    /* synthetic */ void -java_util_function_IntConsumer_lambda$1(IntConsumer after, int t) {
        accept(t);
        after.accept(t);
    }
}
