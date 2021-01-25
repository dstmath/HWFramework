package junit.extensions;

import junit.framework.Protectable;
import junit.framework.Test;
import junit.framework.TestResult;

public class TestSetup extends TestDecorator {
    public TestSetup(Test test) {
        super(test);
    }

    @Override // junit.extensions.TestDecorator, junit.framework.Test
    public void run(final TestResult result) {
        result.runProtected(this, new Protectable() {
            /* class junit.extensions.TestSetup.AnonymousClass1 */

            @Override // junit.framework.Protectable
            public void protect() throws Exception {
                TestSetup.this.setUp();
                TestSetup.this.basicRun(result);
                TestSetup.this.tearDown();
            }
        });
    }

    /* access modifiers changed from: protected */
    public void setUp() throws Exception {
    }

    /* access modifiers changed from: protected */
    public void tearDown() throws Exception {
    }
}
