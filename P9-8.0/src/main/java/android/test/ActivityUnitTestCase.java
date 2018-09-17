package android.test;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.test.mock.MockApplication;
import android.util.Log;
import android.view.Window;

@Deprecated
public abstract class ActivityUnitTestCase<T extends Activity> extends ActivityTestCase {
    private static final String TAG = "ActivityUnitTestCase";
    private Class<T> mActivityClass;
    private Context mActivityContext;
    private Application mApplication;
    private boolean mAttached = false;
    private boolean mCreated = false;
    private MockParent mMockParent;

    private static class MockParent extends Activity {
        public boolean mFinished;
        public int mFinishedActivityRequest;
        public int mRequestedOrientation;
        public Intent mStartedActivityIntent;
        public int mStartedActivityRequest;

        /* synthetic */ MockParent(MockParent -this0) {
            this();
        }

        private MockParent() {
            this.mRequestedOrientation = 0;
            this.mStartedActivityIntent = null;
            this.mStartedActivityRequest = -1;
            this.mFinished = false;
            this.mFinishedActivityRequest = -1;
        }

        public void setRequestedOrientation(int requestedOrientation) {
            this.mRequestedOrientation = requestedOrientation;
        }

        public int getRequestedOrientation() {
            return this.mRequestedOrientation;
        }

        public Window getWindow() {
            return null;
        }

        public void startActivityFromChild(Activity child, Intent intent, int requestCode) {
            this.mStartedActivityIntent = intent;
            this.mStartedActivityRequest = requestCode;
        }

        public void finishFromChild(Activity child) {
            this.mFinished = true;
        }

        public void finishActivityFromChild(Activity child, int requestCode) {
            this.mFinished = true;
            this.mFinishedActivityRequest = requestCode;
        }
    }

    public ActivityUnitTestCase(Class<T> activityClass) {
        this.mActivityClass = activityClass;
    }

    public T getActivity() {
        return super.getActivity();
    }

    protected void setUp() throws Exception {
        super.setUp();
        this.mActivityContext = getInstrumentation().getTargetContext();
    }

    protected T startActivity(Intent intent, Bundle savedInstanceState, Object lastNonConfigurationInstance) {
        assertFalse("Activity already created", this.mCreated);
        if (!this.mAttached) {
            assertNotNull(this.mActivityClass);
            setActivity(null);
            Activity newActivity = null;
            try {
                if (this.mApplication == null) {
                    setApplication(new MockApplication());
                }
                intent.setComponent(new ComponentName(this.mActivityClass.getPackage().getName(), this.mActivityClass.getName()));
                ActivityInfo info = new ActivityInfo();
                CharSequence title = this.mActivityClass.getName();
                this.mMockParent = new MockParent();
                newActivity = getInstrumentation().newActivity(this.mActivityClass, this.mActivityContext, null, this.mApplication, intent, info, title, this.mMockParent, null, lastNonConfigurationInstance);
            } catch (Exception e) {
                Log.w(TAG, "Catching exception", e);
                assertNotNull(null);
            }
            assertNotNull(newActivity);
            setActivity(newActivity);
            this.mAttached = true;
        }
        T result = getActivity();
        if (result != null) {
            getInstrumentation().callActivityOnCreate(getActivity(), savedInstanceState);
            this.mCreated = true;
        }
        return result;
    }

    protected void tearDown() throws Exception {
        setActivity(null);
        scrubClass(ActivityInstrumentationTestCase.class);
        super.tearDown();
    }

    public void setApplication(Application application) {
        this.mApplication = application;
    }

    public void setActivityContext(Context activityContext) {
        this.mActivityContext = activityContext;
    }

    public int getRequestedOrientation() {
        if (this.mMockParent != null) {
            return this.mMockParent.mRequestedOrientation;
        }
        return 0;
    }

    public Intent getStartedActivityIntent() {
        if (this.mMockParent != null) {
            return this.mMockParent.mStartedActivityIntent;
        }
        return null;
    }

    public int getStartedActivityRequest() {
        if (this.mMockParent != null) {
            return this.mMockParent.mStartedActivityRequest;
        }
        return 0;
    }

    public boolean isFinishCalled() {
        if (this.mMockParent != null) {
            return this.mMockParent.mFinished;
        }
        return false;
    }

    public int getFinishedActivityRequest() {
        if (this.mMockParent != null) {
            return this.mMockParent.mFinishedActivityRequest;
        }
        return 0;
    }
}
