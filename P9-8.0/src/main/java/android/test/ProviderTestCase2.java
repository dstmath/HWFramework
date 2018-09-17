package android.test;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.content.res.Resources;
import android.database.DatabaseUtils;
import android.test.mock.MockContentResolver;
import android.test.mock.MockContext;
import java.io.File;

public abstract class ProviderTestCase2<T extends ContentProvider> extends AndroidTestCase {
    private T mProvider;
    String mProviderAuthority;
    Class<T> mProviderClass;
    private IsolatedContext mProviderContext;
    private MockContentResolver mResolver;

    private class MockContext2 extends MockContext {
        /* synthetic */ MockContext2(ProviderTestCase2 this$0, MockContext2 -this1) {
            this();
        }

        private MockContext2() {
        }

        public Resources getResources() {
            return ProviderTestCase2.this.getContext().getResources();
        }

        public File getDir(String name, int mode) {
            return ProviderTestCase2.this.getContext().getDir("mockcontext2_" + name, mode);
        }

        public Context getApplicationContext() {
            return this;
        }
    }

    public ProviderTestCase2(Class<T> providerClass, String providerAuthority) {
        this.mProviderClass = providerClass;
        this.mProviderAuthority = providerAuthority;
    }

    public T getProvider() {
        return this.mProvider;
    }

    protected void setUp() throws Exception {
        super.setUp();
        this.mResolver = new MockContentResolver();
        String filenamePrefix = "test.";
        this.mProviderContext = new IsolatedContext(this.mResolver, new RenamingDelegatingContext(new MockContext2(this, null), getContext(), "test."));
        this.mProvider = createProviderForTest(this.mProviderContext, this.mProviderClass, this.mProviderAuthority);
        this.mResolver.addProvider(this.mProviderAuthority, getProvider());
    }

    static <T extends ContentProvider> T createProviderForTest(Context context, Class<T> providerClass, String authority) throws IllegalAccessException, InstantiationException {
        ContentProvider instance = (ContentProvider) providerClass.newInstance();
        ProviderInfo providerInfo = new ProviderInfo();
        providerInfo.authority = authority;
        instance.attachInfoForTesting(context, providerInfo);
        return instance;
    }

    protected void tearDown() throws Exception {
        this.mProvider.shutdown();
        super.tearDown();
    }

    public MockContentResolver getMockContentResolver() {
        return this.mResolver;
    }

    public IsolatedContext getMockContext() {
        return this.mProviderContext;
    }

    public static <T extends ContentProvider> ContentResolver newResolverWithContentProviderFromSql(Context targetContext, String filenamePrefix, Class<T> providerClass, String authority, String databaseName, int databaseVersion, String sql) throws IllegalAccessException, InstantiationException {
        MockContentResolver resolver = new MockContentResolver();
        Context context = new IsolatedContext(resolver, new RenamingDelegatingContext(new MockContext(), targetContext, filenamePrefix));
        DatabaseUtils.createDbFromSqlStatements(context, databaseName, databaseVersion, sql);
        resolver.addProvider(authority, createProviderForTest(context, providerClass, authority));
        return resolver;
    }
}
