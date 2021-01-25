package android.test;

import android.app.Activity;

@Deprecated
public abstract class SingleLaunchActivityTestCase<T extends Activity> extends InstrumentationTestCase {
    private static Activity sActivity;
    private static boolean sActivityLaunchedFlag = false;
    private static int sTestCaseCounter = 0;
    Class<T> mActivityClass;
    String mPackage;

    public SingleLaunchActivityTestCase(String pkg, Class<T> activityClass) {
        this.mPackage = pkg;
        this.mActivityClass = activityClass;
        sTestCaseCounter++;
    }

    public T getActivity() {
        return (T) sActivity;
    }

    /* access modifiers changed from: protected */
    public void setUp() throws Exception {
        SingleLaunchActivityTestCase.super.setUp();
        if (!sActivityLaunchedFlag) {
            getInstrumentation().setInTouchMode(false);
            sActivity = launchActivity(this.mPackage, this.mActivityClass, null);
            sActivityLaunchedFlag = true;
        }
    }

    /* access modifiers changed from: protected */
    public void tearDown() throws Exception {
        sTestCaseCounter--;
        if (sTestCaseCounter == 0) {
            sActivity.finish();
        }
        SingleLaunchActivityTestCase.super.tearDown();
    }

    public void testActivityTestCaseSetUpProperly() throws Exception {
        assertNotNull("activity should be launched successfully", sActivity);
    }
}
