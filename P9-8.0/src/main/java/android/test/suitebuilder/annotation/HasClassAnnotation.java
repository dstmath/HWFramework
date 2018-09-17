package android.test.suitebuilder.annotation;

import android.test.suitebuilder.TestMethod;
import com.android.internal.util.Predicate;
import java.lang.annotation.Annotation;

class HasClassAnnotation implements Predicate<TestMethod> {
    private Class<? extends Annotation> annotationClass;

    public HasClassAnnotation(Class<? extends Annotation> annotationClass) {
        this.annotationClass = annotationClass;
    }

    public boolean apply(TestMethod testMethod) {
        return testMethod.getEnclosingClass().getAnnotation(this.annotationClass) != null;
    }
}
