package android.test.suitebuilder.annotation;

import android.test.suitebuilder.TestMethod;
import com.android.internal.util.Predicate;
import com.android.internal.util.Predicates;
import java.lang.annotation.Annotation;

public class HasAnnotation implements Predicate<TestMethod> {
    private Predicate<TestMethod> hasMethodOrClassAnnotation;

    public HasAnnotation(Class<? extends Annotation> annotationClass) {
        this.hasMethodOrClassAnnotation = Predicates.or(new Predicate[]{new HasMethodAnnotation(annotationClass), new HasClassAnnotation(annotationClass)});
    }

    public boolean apply(TestMethod testMethod) {
        return this.hasMethodOrClassAnnotation.apply(testMethod);
    }
}
