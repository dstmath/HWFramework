package android.test;

import android.app.Activity;
import android.content.Intent;

@Deprecated
public abstract class ActivityInstrumentationTestCase2<T extends Activity> extends ActivityTestCase {
    Class<T> mActivityClass;
    Intent mActivityIntent;
    boolean mInitialTouchMode;

    @Deprecated
    public ActivityInstrumentationTestCase2(String pkg, Class<T> activityClass) {
        this(activityClass);
    }

    public ActivityInstrumentationTestCase2(Class<T> activityClass) {
        this.mInitialTouchMode = false;
        this.mActivityIntent = null;
        this.mActivityClass = activityClass;
    }

    @Override // android.test.ActivityTestCase
    public T getActivity() {
        Activity a = (T) super.getActivity();
        if (a == null) {
            getInstrumentation().setInTouchMode(this.mInitialTouchMode);
            String targetPackage = getInstrumentation().getTargetContext().getPackageName();
            Intent intent = this.mActivityIntent;
            a = intent == null ? (T) launchActivity(targetPackage, this.mActivityClass, null) : (T) launchActivityWithIntent(targetPackage, this.mActivityClass, intent);
            setActivity(a);
        }
        return (T) a;
    }

    public void setActivityIntent(Intent i) {
        this.mActivityIntent = i;
    }

    public void setActivityInitialTouchMode(boolean initialTouchMode) {
        this.mInitialTouchMode = initialTouchMode;
    }

    /* access modifiers changed from: protected */
    public void setUp() throws Exception {
        super.setUp();
        this.mInitialTouchMode = false;
        this.mActivityIntent = null;
    }

    /* access modifiers changed from: protected */
    public void tearDown() throws Exception {
        Activity a = super.getActivity();
        if (a != null) {
            a.finish();
            setActivity(null);
        }
        scrubClass(ActivityInstrumentationTestCase2.class);
        super.tearDown();
    }

    /* access modifiers changed from: protected */
    public void runTest() throws Throwable {
        try {
            if (getClass().getMethod(getName(), null).isAnnotationPresent(UiThreadTest.class)) {
                getActivity();
            }
        } catch (Exception e) {
        }
        super.runTest();
    }
}
