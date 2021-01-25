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

    public ActivityUnitTestCase(Class<T> activityClass) {
        this.mActivityClass = activityClass;
    }

    @Override // android.test.ActivityTestCase
    public T getActivity() {
        return (T) super.getActivity();
    }

    /* access modifiers changed from: protected */
    public void setUp() throws Exception {
        super.setUp();
        this.mActivityContext = getInstrumentation().getTargetContext();
    }

    /* access modifiers changed from: protected */
    public T startActivity(Intent intent, Bundle savedInstanceState, Object lastNonConfigurationInstance) {
        assertFalse("Activity already created", this.mCreated);
        if (!this.mAttached) {
            assertNotNull(this.mActivityClass);
            setActivity(null);
            Activity activity = null;
            try {
                if (this.mApplication == null) {
                    setApplication(new MockApplication());
                }
                intent.setComponent(new ComponentName(this.mActivityClass.getPackage().getName(), this.mActivityClass.getName()));
                ActivityInfo info = new ActivityInfo();
                CharSequence title = this.mActivityClass.getName();
                this.mMockParent = new MockParent();
                activity = getInstrumentation().newActivity(this.mActivityClass, this.mActivityContext, null, this.mApplication, intent, info, title, this.mMockParent, null, lastNonConfigurationInstance);
            } catch (Exception e) {
                Log.w(TAG, "Catching exception", e);
                assertNotNull(null);
            }
            assertNotNull(activity);
            setActivity(activity);
            this.mAttached = true;
        }
        T result = getActivity();
        if (result != null) {
            getInstrumentation().callActivityOnCreate(getActivity(), savedInstanceState);
            this.mCreated = true;
        }
        return result;
    }

    /* access modifiers changed from: protected */
    public void tearDown() throws Exception {
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
        MockParent mockParent = this.mMockParent;
        if (mockParent != null) {
            return mockParent.mRequestedOrientation;
        }
        return 0;
    }

    public Intent getStartedActivityIntent() {
        MockParent mockParent = this.mMockParent;
        if (mockParent != null) {
            return mockParent.mStartedActivityIntent;
        }
        return null;
    }

    public int getStartedActivityRequest() {
        MockParent mockParent = this.mMockParent;
        if (mockParent != null) {
            return mockParent.mStartedActivityRequest;
        }
        return 0;
    }

    public boolean isFinishCalled() {
        MockParent mockParent = this.mMockParent;
        if (mockParent != null) {
            return mockParent.mFinished;
        }
        return false;
    }

    public int getFinishedActivityRequest() {
        MockParent mockParent = this.mMockParent;
        if (mockParent != null) {
            return mockParent.mFinishedActivityRequest;
        }
        return 0;
    }

    private static class MockParent extends Activity {
        public boolean mFinished;
        public int mFinishedActivityRequest;
        public int mRequestedOrientation;
        public Intent mStartedActivityIntent;
        public int mStartedActivityRequest;

        private MockParent() {
            this.mRequestedOrientation = 0;
            this.mStartedActivityIntent = null;
            this.mStartedActivityRequest = -1;
            this.mFinished = false;
            this.mFinishedActivityRequest = -1;
        }

        @Override // android.app.Activity
        public void setRequestedOrientation(int requestedOrientation) {
            this.mRequestedOrientation = requestedOrientation;
        }

        @Override // android.app.Activity
        public int getRequestedOrientation() {
            return this.mRequestedOrientation;
        }

        @Override // android.app.Activity
        public Window getWindow() {
            return null;
        }

        @Override // android.app.Activity
        public void startActivityFromChild(Activity child, Intent intent, int requestCode) {
            this.mStartedActivityIntent = intent;
            this.mStartedActivityRequest = requestCode;
        }

        @Override // android.app.Activity
        public void finishFromChild(Activity child) {
            this.mFinished = true;
        }

        @Override // android.app.Activity
        public void finishActivityFromChild(Activity child, int requestCode) {
            this.mFinished = true;
            this.mFinishedActivityRequest = requestCode;
        }
    }
}
