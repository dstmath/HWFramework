package android.test;

import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.test.mock.MockApplication;
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

    protected void setUp() throws Exception {
        super.setUp();
        this.mSystemContext = getContext();
    }

    protected void setupService() {
        this.mService = null;
        try {
            this.mService = (Service) this.mServiceClass.newInstance();
        } catch (Exception e) {
            assertNotNull(this.mService);
        }
        if (getApplication() == null) {
            setApplication(new MockApplication());
        }
        this.mService.attach(getContext(), null, this.mServiceClass.getName(), null, getApplication(), null);
        assertNotNull(this.mService);
        this.mServiceId = new Random().nextInt();
        this.mServiceAttached = true;
    }

    protected void startService(Intent intent) {
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

    protected IBinder bindService(Intent intent) {
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

    protected void shutdownService() {
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

    protected void tearDown() throws Exception {
        shutdownService();
        this.mService = null;
        scrubClass(ServiceTestCase.class);
        super.tearDown();
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
