package android.test.suitebuilder;

import android.test.InstrumentationTestCase;
import android.test.suitebuilder.annotation.HasAnnotation;
import android.test.suitebuilder.annotation.LargeTest;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;
import android.test.suitebuilder.annotation.Smoke;
import android.test.suitebuilder.annotation.Suppress;
import com.android.internal.util.Predicate;
import com.android.internal.util.Predicates;

public class TestPredicates {
    public static final Predicate<TestMethod> REJECT_INSTRUMENTATION = Predicates.not(SELECT_INSTRUMENTATION);
    public static final Predicate<TestMethod> REJECT_SUPPRESSED = Predicates.not(new HasAnnotation(Suppress.class));
    public static final Predicate<TestMethod> SELECT_INSTRUMENTATION = new AssignableFrom(InstrumentationTestCase.class);
    public static final Predicate<TestMethod> SELECT_LARGE = new HasAnnotation(LargeTest.class);
    public static final Predicate<TestMethod> SELECT_MEDIUM = new HasAnnotation(MediumTest.class);
    public static final Predicate<TestMethod> SELECT_SMALL = new HasAnnotation(SmallTest.class);
    public static final Predicate<TestMethod> SELECT_SMOKE = new HasAnnotation(Smoke.class);
}
