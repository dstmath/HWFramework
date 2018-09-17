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
        return launchActivityWithIntent(pkg, activityCls, intent);
    }

    public final <T extends Activity> T launchActivityWithIntent(String pkg, Class<T> activityCls, Intent intent) {
        intent.setClassName(pkg, activityCls.getName());
        intent.addFlags(268435456);
        T activity = getInstrumentation().startActivitySync(intent);
        getInstrumentation().waitForIdleSync();
        return activity;
    }

    public void runTestOnUiThread(final Runnable r) throws Throwable {
        final Throwable[] exceptions = new Throwable[1];
        getInstrumentation().runOnMainSync(new Runnable() {
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

    protected void runTest() throws Throwable {
        String fName = getName();
        TestCase.assertNotNull(fName);
        Method method = null;
        try {
            method = getClass().getMethod(fName, (Class[]) null);
        } catch (NoSuchMethodException e) {
            TestCase.fail("Method \"" + fName + "\" not found");
        }
        if (!Modifier.isPublic(method.getModifiers())) {
            TestCase.fail("Method \"" + fName + "\" should be public");
        }
        int runCount = 1;
        boolean isRepetitive = false;
        if (method.isAnnotationPresent(FlakyTest.class)) {
            runCount = ((FlakyTest) method.getAnnotation(FlakyTest.class)).tolerance();
        } else if (method.isAnnotationPresent(RepetitiveTest.class)) {
            runCount = ((RepetitiveTest) method.getAnnotation(RepetitiveTest.class)).numIterations();
            isRepetitive = true;
        }
        if (method.isAnnotationPresent(UiThreadTest.class)) {
            final int tolerance = runCount;
            final boolean repetitive = isRepetitive;
            final Method testMethod = method;
            final Throwable[] exceptions = new Throwable[1];
            getInstrumentation().runOnMainSync(new Runnable() {
                public void run() {
                    try {
                        InstrumentationTestCase.this.runMethod(testMethod, tolerance, repetitive);
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

    /* JADX WARNING: Removed duplicated region for block: B:29:0x007c A:{RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:11:0x0028  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void runMethod(Method runMethod, int tolerance, boolean isRepetitive) throws Throwable {
        Throwable exception;
        int runCount = 0;
        while (true) {
            Bundle iterations;
            try {
                runMethod.invoke(this, (Object[]) null);
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
                    iterations.putInt("currentiterations", runCount);
                    getInstrumentation().sendStatus(2, iterations);
                }
            } catch (Throwable e2) {
                e2.fillInStackTrace();
                exception = e2;
                runCount++;
                if (isRepetitive) {
                    iterations = new Bundle();
                    iterations.putInt("currentiterations", runCount);
                    getInstrumentation().sendStatus(2, iterations);
                }
            } catch (Throwable th) {
                runCount++;
                if (isRepetitive) {
                    iterations = new Bundle();
                    iterations.putInt("currentiterations", runCount);
                    getInstrumentation().sendStatus(2, iterations);
                }
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
        Instrumentation instrumentation = getInstrumentation();
        for (String key : keysSequence.split(" ")) {
            String key2;
            int keyCount;
            int repeater = key2.indexOf(42);
            if (repeater == -1) {
                keyCount = 1;
            } else {
                try {
                    keyCount = Integer.parseInt(key2.substring(0, repeater));
                } catch (NumberFormatException e) {
                    Log.w("ActivityTestCase", "Invalid repeat count: " + key2);
                }
            }
            if (repeater != -1) {
                key2 = key2.substring(repeater + 1);
            }
            int j = 0;
            while (j < keyCount) {
                try {
                    try {
                        instrumentation.sendKeyDownUpSync(KeyEvent.class.getField("KEYCODE_" + key2).getInt(null));
                    } catch (SecurityException e2) {
                    }
                    j++;
                } catch (NoSuchFieldException e3) {
                    Log.w("ActivityTestCase", "Unknown keycode: KEYCODE_" + key2);
                } catch (IllegalAccessException e4) {
                    Log.w("ActivityTestCase", "Unknown keycode: KEYCODE_" + key2);
                }
            }
        }
        instrumentation.waitForIdleSync();
    }

    public void sendKeys(int... keys) {
        Instrumentation instrumentation = getInstrumentation();
        for (int sendKeyDownUpSync : keys) {
            try {
                instrumentation.sendKeyDownUpSync(sendKeyDownUpSync);
            } catch (SecurityException e) {
            }
        }
        instrumentation.waitForIdleSync();
    }

    public void sendRepeatedKeys(int... keys) {
        int count = keys.length;
        if ((count & 1) == 1) {
            throw new IllegalArgumentException("The size of the keys array must be a multiple of 2");
        }
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
    }

    protected void tearDown() throws Exception {
        Runtime.getRuntime().gc();
        Runtime.getRuntime().runFinalization();
        Runtime.getRuntime().gc();
        super.tearDown();
    }
}
