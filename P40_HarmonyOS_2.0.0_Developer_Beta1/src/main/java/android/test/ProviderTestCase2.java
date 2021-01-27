package android.test;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ProviderInfo;
import android.content.res.Resources;
import android.database.DatabaseUtils;
import android.test.mock.MockContentProvider;
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
        private MockContext2() {
        }

        public Resources getResources() {
            return ProviderTestCase2.this.getContext().getResources();
        }

        public File getDir(String name, int mode) {
            Context context = ProviderTestCase2.this.getContext();
            return context.getDir("mockcontext2_" + name, mode);
        }

        /* JADX DEBUG: Multi-variable search result rejected for r0v0, resolved type: android.test.ProviderTestCase2$MockContext2 */
        /* JADX WARN: Multi-variable type inference failed */
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

    /* JADX WARN: Type inference failed for: r2v0, types: [android.content.Context, android.test.ProviderTestCase2$MockContext2] */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Unknown variable types count: 1 */
    public void setUp() throws Exception {
        ProviderTestCase2.super.setUp();
        this.mResolver = new MockContentResolver();
        this.mProviderContext = new IsolatedContext(this.mResolver, new RenamingDelegatingContext(new MockContext2(), getContext(), "test."));
        this.mProvider = (T) createProviderForTest(this.mProviderContext, this.mProviderClass, this.mProviderAuthority);
        this.mResolver.addProvider(this.mProviderAuthority, getProvider());
    }

    static <T extends ContentProvider> T createProviderForTest(Context context, Class<T> providerClass, String authority) throws IllegalAccessException, InstantiationException {
        T instance = providerClass.newInstance();
        ProviderInfo providerInfo = new ProviderInfo();
        providerInfo.authority = authority;
        MockContentProvider.attachInfoForTesting(instance, context, providerInfo);
        return instance;
    }

    /* access modifiers changed from: protected */
    public void tearDown() throws Exception {
        this.mProvider.shutdown();
        ProviderTestCase2.super.tearDown();
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
