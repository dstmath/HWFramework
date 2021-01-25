package android.test;

import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;

@Deprecated
public abstract class ApplicationTestCase<T extends Application> extends AndroidTestCase {
    private T mApplication;
    Class<T> mApplicationClass;
    private boolean mAttached = false;
    private boolean mCreated = false;
    private Context mSystemContext;

    public ApplicationTestCase(Class<T> applicationClass) {
        this.mApplicationClass = applicationClass;
    }

    public T getApplication() {
        return this.mApplication;
    }

    /* access modifiers changed from: protected */
    public void setUp() throws Exception {
        ApplicationTestCase.super.setUp();
        this.mSystemContext = getContext();
    }

    private void setupApplication() {
        this.mApplication = null;
        try {
            this.mApplication = (T) Instrumentation.newApplication(this.mApplicationClass, getContext());
        } catch (Exception e) {
            assertNotNull(this.mApplication);
        }
        this.mAttached = true;
    }

    /* access modifiers changed from: protected */
    public final void createApplication() {
        assertFalse(this.mCreated);
        if (!this.mAttached) {
            setupApplication();
        }
        assertNotNull(this.mApplication);
        this.mApplication.onCreate();
        this.mCreated = true;
    }

    /* access modifiers changed from: protected */
    public final void terminateApplication() {
        if (this.mCreated) {
            this.mApplication.onTerminate();
        }
    }

    /* access modifiers changed from: protected */
    public void tearDown() throws Exception {
        terminateApplication();
        this.mApplication = null;
        scrubClass(ApplicationTestCase.class);
        ApplicationTestCase.super.tearDown();
    }

    public Context getSystemContext() {
        return this.mSystemContext;
    }

    public final void testApplicationTestCaseSetUpProperly() throws Exception {
        setupApplication();
        assertNotNull("Application class could not be instantiated successfully", this.mApplication);
    }
}
