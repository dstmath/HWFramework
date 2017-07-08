package java.util.function;

import java.util.Objects;

@FunctionalInterface
public interface Predicate<T> {

    final /* synthetic */ class -java_util_function_Predicate_and_java_util_function_Predicate_other_LambdaImpl0 implements Predicate {
        private /* synthetic */ Predicate val$other;
        private /* synthetic */ Predicate val$this;

        public /* synthetic */ -java_util_function_Predicate_and_java_util_function_Predicate_other_LambdaImpl0(Predicate predicate, Predicate predicate2) {
            this.val$this = predicate;
            this.val$other = predicate2;
        }

        public boolean test(Object arg0) {
            return this.val$this.-java_util_function_Predicate_lambda$1(this.val$other, arg0);
        }
    }

    final /* synthetic */ class -java_util_function_Predicate_isEqual_java_lang_Object_targetRef_LambdaImpl0 implements Predicate {
        public boolean test(Object arg0) {
            return Objects.isNull(arg0);
        }
    }

    final /* synthetic */ class -java_util_function_Predicate_isEqual_java_lang_Object_targetRef_LambdaImpl1 implements Predicate {
        private /* synthetic */ Object val$targetRef;

        public /* synthetic */ -java_util_function_Predicate_isEqual_java_lang_Object_targetRef_LambdaImpl1(Object obj) {
            this.val$targetRef = obj;
        }

        public boolean test(Object arg0) {
            return this.val$targetRef.equals(arg0);
        }
    }

    final /* synthetic */ class -java_util_function_Predicate_negate__LambdaImpl0 implements Predicate {
        private /* synthetic */ Predicate val$this;

        public /* synthetic */ -java_util_function_Predicate_negate__LambdaImpl0(Predicate predicate) {
            this.val$this = predicate;
        }

        public boolean test(Object arg0) {
            return this.val$this.-java_util_function_Predicate_lambda$2(arg0);
        }
    }

    final /* synthetic */ class -java_util_function_Predicate_or_java_util_function_Predicate_other_LambdaImpl0 implements Predicate {
        private /* synthetic */ Predicate val$other;
        private /* synthetic */ Predicate val$this;

        public /* synthetic */ -java_util_function_Predicate_or_java_util_function_Predicate_other_LambdaImpl0(Predicate predicate, Predicate predicate2) {
            this.val$this = predicate;
            this.val$other = predicate2;
        }

        public boolean test(Object arg0) {
            return this.val$this.-java_util_function_Predicate_lambda$3(this.val$other, arg0);
        }
    }

    boolean test(T t);

    Predicate<T> and(Predicate<? super T> other) {
        Objects.requireNonNull(other);
        return new -java_util_function_Predicate_and_java_util_function_Predicate_other_LambdaImpl0(this, other);
    }

    /* synthetic */ boolean -java_util_function_Predicate_lambda$1(Predicate other, Object t) {
        return test(t) ? other.test(t) : false;
    }

    /* synthetic */ boolean -java_util_function_Predicate_lambda$2(Object t) {
        return !test(t);
    }

    Predicate<T> negate() {
        return new -java_util_function_Predicate_negate__LambdaImpl0();
    }

    Predicate<T> or(Predicate<? super T> other) {
        Objects.requireNonNull(other);
        return new -java_util_function_Predicate_or_java_util_function_Predicate_other_LambdaImpl0(this, other);
    }

    /* synthetic */ boolean -java_util_function_Predicate_lambda$3(Predicate other, Object t) {
        return !test(t) ? other.test(t) : true;
    }

    static <T> Predicate<T> isEqual(Object targetRef) {
        if (targetRef == null) {
            return new -java_util_function_Predicate_isEqual_java_lang_Object_targetRef_LambdaImpl0();
        }
        return new -java_util_function_Predicate_isEqual_java_lang_Object_targetRef_LambdaImpl1(targetRef);
    }
}
