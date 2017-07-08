package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface Consumer<T> {

    final /* synthetic */ class -java_util_function_Consumer_andThen_java_util_function_Consumer_after_LambdaImpl0 implements Consumer {
        private /* synthetic */ Consumer val$after;
        private /* synthetic */ Consumer val$this;

        public /* synthetic */ -java_util_function_Consumer_andThen_java_util_function_Consumer_after_LambdaImpl0(Consumer consumer, Consumer consumer2) {
            this.val$this = consumer;
            this.val$after = consumer2;
        }

        public void accept(Object arg0) {
            this.val$this.-java_util_function_Consumer_lambda$1(this.val$after, arg0);
        }
    }

    void accept(T t);

    Consumer<T> andThen(Consumer<? super T> after) {
        Objects.requireNonNull(after);
        return new -java_util_function_Consumer_andThen_java_util_function_Consumer_after_LambdaImpl0(this, after);
    }

    /* synthetic */ void -java_util_function_Consumer_lambda$1(Consumer after, Object t) {
        accept(t);
        after.accept(t);
    }
}
