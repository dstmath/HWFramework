package android.test.mock;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.IContentProvider;
import android.database.ContentObserver;
import android.net.Uri;
import com.google.android.collect.Maps;
import java.util.Map;

public class MockContentResolver extends ContentResolver {
    Map<String, ContentProvider> mProviders;

    public MockContentResolver() {
        this(null);
    }

    public MockContentResolver(Context context) {
        super(context);
        this.mProviders = Maps.newHashMap();
    }

    public void addProvider(String name, ContentProvider provider) {
        this.mProviders.put(name, provider);
    }

    protected IContentProvider acquireProvider(Context context, String name) {
        return acquireExistingProvider(context, name);
    }

    protected IContentProvider acquireExistingProvider(Context context, String name) {
        ContentProvider provider = (ContentProvider) this.mProviders.get(name);
        if (provider != null) {
            return provider.getIContentProvider();
        }
        return null;
    }

    public boolean releaseProvider(IContentProvider provider) {
        return true;
    }

    protected IContentProvider acquireUnstableProvider(Context c, String name) {
        return acquireProvider(c, name);
    }

    public boolean releaseUnstableProvider(IContentProvider icp) {
        return releaseProvider(icp);
    }

    public void unstableProviderDied(IContentProvider icp) {
    }

    public void notifyChange(Uri uri, ContentObserver observer, boolean syncToNetwork) {
    }
}
