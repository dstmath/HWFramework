package android.test;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import junit.framework.TestCase;

@Deprecated
public class InstrumentationTestCase extends TestCase {
    private Instrumentation mInstrumentation;

    public void injectInstrumentation(Instrumentation instrumentation) {
        this.mInstrumentation = instrumentation;
    }

    @Deprecated
    public void injectInsrumentation(Instrumentation instrumentation) {
        injectInstrumentation(instrumentation);
    }

    public Instrumentation getInstrumentation() {
        return this.mInstrumentation;
    }

    public final <T extends Activity> T launchActivity(String pkg, Class<T> activityCls, Bundle extras) {
        Intent intent = new Intent("android.intent.action.MAIN");
        if (extras != null) {
            intent.putExtras(extras);
        }
        return (T) launchActivityWithIntent(pkg, activityCls, intent);
    }

    public final <T extends Activity> T launchActivityWithIntent(String pkg, Class<T> activityCls, Intent intent) {
        intent.setClassName(pkg, activityCls.getName());
        intent.addFlags(268435456);
        T activity = (T) getInstrumentation().startActivitySync(intent);
        getInstrumentation().waitForIdleSync();
        return activity;
    }

    public void runTestOnUiThread(final Runnable r) throws Throwable {
        final Throwable[] exceptions = new Throwable[1];
        getInstrumentation().runOnMainSync(new Runnable() {
            /* class android.test.InstrumentationTestCase.AnonymousClass1 */

            public void run() {
                try {
                    r.run();
                } catch (Throwable throwable) {
                    exceptions[0] = throwable;
                }
            }
        });
        if (exceptions[0] != null) {
            throw exceptions[0];
        }
    }

    /* access modifiers changed from: protected */
    @Override // junit.framework.TestCase
    public void runTest() throws Throwable {
        String fName = getName();
        assertNotNull(fName);
        final Method method = null;
        try {
            method = getClass().getMethod(fName, null);
        } catch (NoSuchMethodException e) {
            fail("Method \"" + fName + "\" not found");
        }
        if (!Modifier.isPublic(method.getModifiers())) {
            fail("Method \"" + fName + "\" should be public");
        }
        final int runCount = 1;
        final boolean isRepetitive = false;
        if (method.isAnnotationPresent(FlakyTest.class)) {
            runCount = ((FlakyTest) method.getAnnotation(FlakyTest.class)).tolerance();
        } else if (method.isAnnotationPresent(RepetitiveTest.class)) {
            runCount = ((RepetitiveTest) method.getAnnotation(RepetitiveTest.class)).numIterations();
            isRepetitive = true;
        }
        if (method.isAnnotationPresent(UiThreadTest.class)) {
            final Throwable[] exceptions = new Throwable[1];
            getInstrumentation().runOnMainSync(new Runnable() {
                /* class android.test.InstrumentationTestCase.AnonymousClass2 */

                public void run() {
                    try {
                        InstrumentationTestCase.this.runMethod(method, runCount, isRepetitive);
                    } catch (Throwable throwable) {
                        exceptions[0] = throwable;
                    }
                }
            });
            if (exceptions[0] != null) {
                throw exceptions[0];
            }
            return;
        }
        runMethod(method, runCount, isRepetitive);
    }

    private void runMethod(Method runMethod, int tolerance) throws Throwable {
        runMethod(runMethod, tolerance, false);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x004c A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x004d  */
    private void runMethod(Method runMethod, int tolerance, boolean isRepetitive) throws Throwable {
        Throwable exception;
        Bundle iterations;
        int runCount = 0;
        while (true) {
            try {
                runMethod.invoke(this, null);
                exception = null;
                runCount++;
                if (isRepetitive) {
                    iterations = new Bundle();
                    iterations.putInt("currentiterations", runCount);
                    getInstrumentation().sendStatus(2, iterations);
                }
            } catch (InvocationTargetException e) {
                e.fillInStackTrace();
                exception = e.getTargetException();
                runCount++;
                if (isRepetitive) {
                    iterations = new Bundle();
                }
            } catch (IllegalAccessException e2) {
                e2.fillInStackTrace();
                exception = e2;
                runCount++;
                if (isRepetitive) {
                    iterations = new Bundle();
                }
            } catch (Throwable th) {
                int runCount2 = runCount + 1;
                if (isRepetitive) {
                    Bundle iterations2 = new Bundle();
                    iterations2.putInt("currentiterations", runCount2);
                    getInstrumentation().sendStatus(2, iterations2);
                }
                throw th;
            }
            if (runCount >= tolerance || (!isRepetitive && exception == null)) {
                if (exception == null) {
                    throw exception;
                }
                return;
            }
        }
        if (exception == null) {
        }
    }

    public void sendKeys(String keysSequence) {
        int keyCount;
        String[] keys = keysSequence.split(" ");
        Instrumentation instrumentation = getInstrumentation();
        for (String key : keys) {
            int repeater = key.indexOf(42);
            if (repeater == -1) {
                keyCount = 1;
            } else {
                try {
                    keyCount = Integer.parseInt(key.substring(0, repeater));
                } catch (NumberFormatException e) {
                    Log.w("ActivityTestCase", "Invalid repeat count: " + key);
                }
            }
            if (repeater != -1) {
                key = key.substring(repeater + 1);
            }
            for (int j = 0; j < keyCount; j++) {
                try {
                    try {
                        instrumentation.sendKeyDownUpSync(KeyEvent.class.getField("KEYCODE_" + key).getInt(null));
                    } catch (SecurityException e2) {
                    }
                } catch (NoSuchFieldException e3) {
                    Log.w("ActivityTestCase", "Unknown keycode: KEYCODE_" + key);
                } catch (IllegalAccessException e4) {
                    Log.w("ActivityTestCase", "Unknown keycode: KEYCODE_" + key);
                }
            }
        }
        instrumentation.waitForIdleSync();
    }

    public void sendKeys(int... keys) {
        int count = keys.length;
        Instrumentation instrumentation = getInstrumentation();
        for (int i = 0; i < count; i++) {
            try {
                instrumentation.sendKeyDownUpSync(keys[i]);
            } catch (SecurityException e) {
            }
        }
        instrumentation.waitForIdleSync();
    }

    public void sendRepeatedKeys(int... keys) {
        int count = keys.length;
        if ((count & 1) != 1) {
            Instrumentation instrumentation = getInstrumentation();
            for (int i = 0; i < count; i += 2) {
                int keyCount = keys[i];
                int keyCode = keys[i + 1];
                for (int j = 0; j < keyCount; j++) {
                    try {
                        instrumentation.sendKeyDownUpSync(keyCode);
                    } catch (SecurityException e) {
                    }
                }
            }
            instrumentation.waitForIdleSync();
            return;
        }
        throw new IllegalArgumentException("The size of the keys array must be a multiple of 2");
    }

    /* access modifiers changed from: protected */
    @Override // junit.framework.TestCase
    public void tearDown() throws Exception {
        Runtime.getRuntime().gc();
        Runtime.getRuntime().runFinalization();
        Runtime.getRuntime().gc();
        super.tearDown();
    }
}
