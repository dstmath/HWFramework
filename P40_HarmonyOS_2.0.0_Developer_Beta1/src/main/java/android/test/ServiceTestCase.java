package android.test;

import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.test.mock.MockApplication;
import android.test.mock.MockService;
import java.util.Random;

@Deprecated
public abstract class ServiceTestCase<T extends Service> extends AndroidTestCase {
    private Application mApplication;
    private T mService;
    private boolean mServiceAttached = false;
    private boolean mServiceBound = false;
    Class<T> mServiceClass;
    private boolean mServiceCreated = false;
    private int mServiceId;
    private Intent mServiceIntent = null;
    private boolean mServiceStarted = false;
    private Context mSystemContext;

    public ServiceTestCase(Class<T> serviceClass) {
        this.mServiceClass = serviceClass;
    }

    public T getService() {
        return this.mService;
    }

    /* access modifiers changed from: protected */
    public void setUp() throws Exception {
        ServiceTestCase.super.setUp();
        this.mSystemContext = getContext();
    }

    /* access modifiers changed from: protected */
    public void setupService() {
        this.mService = null;
        try {
            this.mService = this.mServiceClass.newInstance();
        } catch (Exception e) {
            assertNotNull(this.mService);
        }
        if (getApplication() == null) {
            setApplication(new MockApplication());
        }
        MockService.attachForTesting(this.mService, getContext(), this.mServiceClass.getName(), getApplication());
        assertNotNull(this.mService);
        this.mServiceId = new Random().nextInt();
        this.mServiceAttached = true;
    }

    /* access modifiers changed from: protected */
    public void startService(Intent intent) {
        if (!this.mServiceAttached) {
            setupService();
        }
        assertNotNull(this.mService);
        if (!this.mServiceCreated) {
            this.mService.onCreate();
            this.mServiceCreated = true;
        }
        this.mService.onStartCommand(intent, 0, this.mServiceId);
        this.mServiceStarted = true;
    }

    /* access modifiers changed from: protected */
    public IBinder bindService(Intent intent) {
        if (!this.mServiceAttached) {
            setupService();
        }
        assertNotNull(this.mService);
        if (!this.mServiceCreated) {
            this.mService.onCreate();
            this.mServiceCreated = true;
        }
        this.mServiceIntent = intent.cloneFilter();
        IBinder result = this.mService.onBind(intent);
        this.mServiceBound = true;
        return result;
    }

    /* access modifiers changed from: protected */
    public void shutdownService() {
        if (this.mServiceStarted) {
            this.mService.stopSelf();
            this.mServiceStarted = false;
        } else if (this.mServiceBound) {
            this.mService.onUnbind(this.mServiceIntent);
            this.mServiceBound = false;
        }
        if (this.mServiceCreated) {
            this.mService.onDestroy();
            this.mServiceCreated = false;
        }
    }

    /* access modifiers changed from: protected */
    public void tearDown() throws Exception {
        shutdownService();
        this.mService = null;
        scrubClass(ServiceTestCase.class);
        ServiceTestCase.super.tearDown();
    }

    public void setApplication(Application application) {
        this.mApplication = application;
    }

    public Application getApplication() {
        return this.mApplication;
    }

    public Context getSystemContext() {
        return this.mSystemContext;
    }

    public void testServiceTestCaseSetUpProperly() throws Exception {
        setupService();
        assertNotNull("service should be launched successfully", this.mService);
    }
}
