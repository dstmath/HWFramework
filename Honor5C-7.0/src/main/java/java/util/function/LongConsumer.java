package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface LongConsumer {

    final /* synthetic */ class -java_util_function_LongConsumer_andThen_java_util_function_LongConsumer_after_LambdaImpl0 implements LongConsumer {
        private /* synthetic */ LongConsumer val$after;
        private /* synthetic */ LongConsumer val$this;

        public /* synthetic */ -java_util_function_LongConsumer_andThen_java_util_function_LongConsumer_after_LambdaImpl0(LongConsumer longConsumer, LongConsumer longConsumer2) {
            this.val$this = longConsumer;
            this.val$after = longConsumer2;
        }

        public void accept(long arg0) {
            this.val$this.-java_util_function_LongConsumer_lambda$1(this.val$after, arg0);
        }
    }

    void accept(long j);

    LongConsumer andThen(LongConsumer after) {
        Objects.requireNonNull(after);
        return new -java_util_function_LongConsumer_andThen_java_util_function_LongConsumer_after_LambdaImpl0(this, after);
    }

    /* synthetic */ void -java_util_function_LongConsumer_lambda$1(LongConsumer after, long t) {
        accept(t);
        after.accept(t);
    }
}
