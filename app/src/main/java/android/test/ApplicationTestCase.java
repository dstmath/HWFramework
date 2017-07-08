package android.test;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;

@Deprecated
public abstract class ApplicationTestCase<T extends Application> extends AndroidTestCase {
    private T mApplication;
    Class<T> mApplicationClass;
    private boolean mAttached;
    private boolean mCreated;
    private Context mSystemContext;

    public ApplicationTestCase(Class<T> applicationClass) {
        this.mAttached = false;
        this.mCreated = false;
        this.mApplicationClass = applicationClass;
    }

    public T getApplication() {
        return this.mApplication;
    }

    protected void setUp() throws Exception {
        super.setUp();
        this.mSystemContext = getContext();
    }

    private void setupApplication() {
        this.mApplication = null;
        try {
            this.mApplication = Instrumentation.newApplication(this.mApplicationClass, getContext());
        } catch (Exception e) {
            assertNotNull(this.mApplication);
        }
        this.mAttached = true;
    }

    protected final void createApplication() {
        assertFalse(this.mCreated);
        if (!this.mAttached) {
            setupApplication();
        }
        assertNotNull(this.mApplication);
        this.mApplication.onCreate();
        this.mCreated = true;
    }

    protected final void terminateApplication() {
        if (this.mCreated) {
            this.mApplication.onTerminate();
        }
    }

    protected void tearDown() throws Exception {
        terminateApplication();
        this.mApplication = null;
        scrubClass(ApplicationTestCase.class);
        super.tearDown();
    }

    public Context getSystemContext() {
        return this.mSystemContext;
    }

    public final void testApplicationTestCaseSetUpProperly() throws Exception {
        setupApplication();
        assertNotNull("Application class could not be instantiated successfully", this.mApplication);
    }
}
