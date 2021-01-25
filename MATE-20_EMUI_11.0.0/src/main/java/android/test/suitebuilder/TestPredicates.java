package android.test.suitebuilder;

import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.Smoke;
import android.test.suitebuilder.annotation.Suppress;
import com.android.internal.util.Predicate;
import java.lang.annotation.Annotation;

public class TestPredicates {
    static final Predicate<TestMethod> REJECT_INSTRUMENTATION = not(new AssignableFrom(InstrumentationTestCase.class));
    static final Predicate<TestMethod> REJECT_SUPPRESSED = not(hasAnnotation(Suppress.class));
    static final Predicate<TestMethod> SELECT_SMOKE = hasAnnotation(Smoke.class);

    public static Predicate<TestMethod> hasAnnotation(Class<? extends Annotation> annotationClass) {
        return new HasAnnotation(annotationClass);
    }

    /* access modifiers changed from: private */
    public static class HasAnnotation implements Predicate<TestMethod> {
        private final Class<? extends Annotation> annotationClass;

        private HasAnnotation(Class<? extends Annotation> annotationClass2) {
            this.annotationClass = annotationClass2;
        }

        public boolean apply(TestMethod testMethod) {
            return (testMethod.getAnnotation(this.annotationClass) == null && testMethod.getEnclosingClass().getAnnotation((Class<A>) this.annotationClass) == null) ? false : true;
        }
    }

    public static <T> Predicate<T> not(Predicate<? super T> predicate) {
        return new NotPredicate(predicate);
    }

    /* access modifiers changed from: private */
    public static class NotPredicate<T> implements Predicate<T> {
        private final Predicate<? super T> predicate;

        private NotPredicate(Predicate<? super T> predicate2) {
            this.predicate = predicate2;
        }

        public boolean apply(T t) {
            return !this.predicate.apply(t);
        }
    }
}
