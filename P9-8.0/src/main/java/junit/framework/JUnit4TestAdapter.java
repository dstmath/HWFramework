package junit.framework;

import java.util.List;
import org.junit.Ignore;
import org.junit.runner.Describable;
import org.junit.runner.Description;
import org.junit.runner.Request;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sortable;
import org.junit.runner.manipulation.Sorter;

public class JUnit4TestAdapter implements Test, Filterable, Sortable, Describable {
    private final JUnit4TestAdapterCache fCache;
    private final Class<?> fNewTestClass;
    private final Runner fRunner;

    public JUnit4TestAdapter(Class<?> newTestClass) {
        this(newTestClass, JUnit4TestAdapterCache.getDefault());
    }

    public JUnit4TestAdapter(Class<?> newTestClass, JUnit4TestAdapterCache cache) {
        this.fCache = cache;
        this.fNewTestClass = newTestClass;
        this.fRunner = Request.classWithoutSuiteMethod(newTestClass).getRunner();
    }

    public int countTestCases() {
        return this.fRunner.testCount();
    }

    public void run(TestResult result) {
        this.fRunner.run(this.fCache.getNotifier(result, this));
    }

    public List<Test> getTests() {
        return this.fCache.asTestList(getDescription());
    }

    public Class<?> getTestClass() {
        return this.fNewTestClass;
    }

    public Description getDescription() {
        return removeIgnored(this.fRunner.getDescription());
    }

    private Description removeIgnored(Description description) {
        if (isIgnored(description)) {
            return Description.EMPTY;
        }
        Description result = description.childlessCopy();
        for (Description each : description.getChildren()) {
            Description child = removeIgnored(each);
            if (!child.isEmpty()) {
                result.addChild(child);
            }
        }
        return result;
    }

    private boolean isIgnored(Description description) {
        return description.getAnnotation(Ignore.class) != null;
    }

    public String toString() {
        return this.fNewTestClass.getName();
    }

    public void filter(Filter filter) throws NoTestsRemainException {
        filter.apply(this.fRunner);
    }

    public void sort(Sorter sorter) {
        sorter.apply(this.fRunner);
    }
}
