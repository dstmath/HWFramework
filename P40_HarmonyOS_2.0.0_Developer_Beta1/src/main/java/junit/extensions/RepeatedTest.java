package junit.extensions;

import junit.framework.Test;
import junit.framework.TestResult;

public class RepeatedTest extends TestDecorator {
    private int fTimesRepeat;

    public RepeatedTest(Test test, int repeat) {
        super(test);
        if (repeat >= 0) {
            this.fTimesRepeat = repeat;
            return;
        }
        throw new IllegalArgumentException("Repetition count must be >= 0");
    }

    @Override // junit.extensions.TestDecorator, junit.framework.Test
    public int countTestCases() {
        return super.countTestCases() * this.fTimesRepeat;
    }

    @Override // junit.extensions.TestDecorator, junit.framework.Test
    public void run(TestResult result) {
        for (int i = 0; i < this.fTimesRepeat && !result.shouldStop(); i++) {
            super.run(result);
        }
    }

    @Override // junit.extensions.TestDecorator
    public String toString() {
        return super.toString() + "(repeated)";
    }
}
