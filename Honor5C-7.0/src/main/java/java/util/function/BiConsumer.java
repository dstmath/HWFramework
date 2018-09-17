package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface BiConsumer<T, U> {

    final /* synthetic */ class -java_util_function_BiConsumer_andThen_java_util_function_BiConsumer_after_LambdaImpl0 implements BiConsumer {
        private /* synthetic */ BiConsumer val$after;
        private /* synthetic */ BiConsumer val$this;

        public /* synthetic */ -java_util_function_BiConsumer_andThen_java_util_function_BiConsumer_after_LambdaImpl0(BiConsumer biConsumer, BiConsumer biConsumer2) {
            this.val$this = biConsumer;
            this.val$after = biConsumer2;
        }

        public void accept(Object arg0, Object arg1) {
            this.val$this.-java_util_function_BiConsumer_lambda$1(this.val$after, arg0, arg1);
        }
    }

    void accept(T t, U u);

    BiConsumer<T, U> andThen(BiConsumer<? super T, ? super U> after) {
        Objects.requireNonNull(after);
        return new -java_util_function_BiConsumer_andThen_java_util_function_BiConsumer_after_LambdaImpl0(this, after);
    }

    /* synthetic */ void -java_util_function_BiConsumer_lambda$1(BiConsumer after, Object l, Object r) {
        accept(l, r);
        after.accept(l, r);
    }
}
