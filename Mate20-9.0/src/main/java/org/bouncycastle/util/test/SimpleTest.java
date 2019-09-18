package org.bouncycastle.util.test;

import java.io.PrintStream;
import org.bouncycastle.util.Arrays;

public abstract class SimpleTest implements Test {
    protected static void runTest(Test test) {
        runTest(test, System.out);
    }

    protected static void runTest(Test test, PrintStream printStream) {
        TestResult perform = test.perform();
        printStream.println(perform.toString());
        if (perform.getException() != null) {
            perform.getException().printStackTrace(printStream);
        }
    }

    private TestResult success() {
        return SimpleTestResult.successful(this, "Okay");
    }

    /* access modifiers changed from: protected */
    public boolean areEqual(byte[] bArr, byte[] bArr2) {
        return Arrays.areEqual(bArr, bArr2);
    }

    /* access modifiers changed from: protected */
    public boolean areEqual(byte[][] bArr, byte[][] bArr2) {
        if (bArr == null && bArr2 == null) {
            return true;
        }
        if (bArr == null || bArr2 == null || bArr.length != bArr2.length) {
            return false;
        }
        for (int i = 0; i < bArr.length; i++) {
            if (!areEqual(bArr[i], bArr2[i])) {
                return false;
            }
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void fail(String str) {
        throw new TestFailedException(SimpleTestResult.failed(this, str));
    }

    /* access modifiers changed from: protected */
    public void fail(String str, Object obj, Object obj2) {
        throw new TestFailedException(SimpleTestResult.failed(this, str, obj, obj2));
    }

    /* access modifiers changed from: protected */
    public void fail(String str, Throwable th) {
        throw new TestFailedException(SimpleTestResult.failed(this, str, th));
    }

    public abstract String getName();

    /* access modifiers changed from: protected */
    public void isEquals(int i, int i2) {
        if (i != i2) {
            throw new TestFailedException(SimpleTestResult.failed(this, "no message"));
        }
    }

    /* access modifiers changed from: protected */
    public void isEquals(long j, long j2) {
        if (j != j2) {
            throw new TestFailedException(SimpleTestResult.failed(this, "no message"));
        }
    }

    /* access modifiers changed from: protected */
    public void isEquals(Object obj, Object obj2) {
        if (!obj.equals(obj2)) {
            throw new TestFailedException(SimpleTestResult.failed(this, "no message"));
        }
    }

    /* access modifiers changed from: protected */
    public void isEquals(String str, long j, long j2) {
        if (j != j2) {
            throw new TestFailedException(SimpleTestResult.failed(this, str));
        }
    }

    /* access modifiers changed from: protected */
    public void isEquals(String str, Object obj, Object obj2) {
        if (obj != null || obj2 != null) {
            if (obj == null) {
                throw new TestFailedException(SimpleTestResult.failed(this, str));
            } else if (obj2 == null) {
                throw new TestFailedException(SimpleTestResult.failed(this, str));
            } else if (!obj.equals(obj2)) {
                throw new TestFailedException(SimpleTestResult.failed(this, str));
            }
        }
    }

    /* access modifiers changed from: protected */
    public void isEquals(String str, boolean z, boolean z2) {
        if (z != z2) {
            throw new TestFailedException(SimpleTestResult.failed(this, str));
        }
    }

    /* access modifiers changed from: protected */
    public void isTrue(String str, boolean z) {
        if (!z) {
            throw new TestFailedException(SimpleTestResult.failed(this, str));
        }
    }

    /* access modifiers changed from: protected */
    public void isTrue(boolean z) {
        if (!z) {
            throw new TestFailedException(SimpleTestResult.failed(this, "no message"));
        }
    }

    public TestResult perform() {
        try {
            performTest();
            return success();
        } catch (TestFailedException e) {
            return e.getResult();
        } catch (Exception e2) {
            return SimpleTestResult.failed(this, "Exception: " + e2, e2);
        }
    }

    public abstract void performTest() throws Exception;
}
