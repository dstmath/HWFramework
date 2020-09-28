package android.content;

import android.annotation.UnsupportedAppUsage;
import android.content.pm.RegisteredServicesCache;
import android.content.pm.XmlSerializerAndParser;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.util.SparseArray;
import com.android.internal.R;
import com.android.internal.annotations.GuardedBy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class SyncAdaptersCache extends RegisteredServicesCache<SyncAdapterType> {
    private static final String ATTRIBUTES_NAME = "sync-adapter";
    private static final String SERVICE_INTERFACE = "android.content.SyncAdapter";
    private static final String SERVICE_META_DATA = "android.content.SyncAdapter";
    private static final String TAG = "Account";
    private static final MySerializer sSerializer = new MySerializer();
    @GuardedBy({"mServicesLock"})
    private SparseArray<ArrayMap<String, String[]>> mAuthorityToSyncAdapters = new SparseArray<>();

    @UnsupportedAppUsage
    public SyncAdaptersCache(Context context) {
        super(context, "android.content.SyncAdapter", "android.content.SyncAdapter", ATTRIBUTES_NAME, sSerializer);
    }

    @Override // android.content.pm.RegisteredServicesCache
    public SyncAdapterType parseServiceAttributes(Resources res, String packageName, AttributeSet attrs) {
        TypedArray sa = res.obtainAttributes(attrs, R.styleable.SyncAdapter);
        try {
            String authority = sa.getString(2);
            String accountType = sa.getString(1);
            if (!TextUtils.isEmpty(authority)) {
                if (!TextUtils.isEmpty(accountType)) {
                    SyncAdapterType syncAdapterType = new SyncAdapterType(authority, accountType, sa.getBoolean(3, true), sa.getBoolean(4, true), sa.getBoolean(6, false), sa.getBoolean(5, false), sa.getString(0), packageName);
                    sa.recycle();
                    return syncAdapterType;
                }
            }
            return null;
        } finally {
            sa.recycle();
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.content.pm.RegisteredServicesCache
    public void onServicesChangedLocked(int userId) {
        synchronized (this.mServicesLock) {
            ArrayMap<String, String[]> adapterMap = this.mAuthorityToSyncAdapters.get(userId);
            if (adapterMap != null) {
                adapterMap.clear();
            }
        }
        super.onServicesChangedLocked(userId);
    }

    public String[] getSyncAdapterPackagesForAuthority(String authority, int userId) {
        synchronized (this.mServicesLock) {
            ArrayMap<String, String[]> adapterMap = this.mAuthorityToSyncAdapters.get(userId);
            if (adapterMap == null) {
                adapterMap = new ArrayMap<>();
                this.mAuthorityToSyncAdapters.put(userId, adapterMap);
            }
            if (adapterMap.containsKey(authority)) {
                return adapterMap.get(authority);
            }
            Collection<RegisteredServicesCache.ServiceInfo<SyncAdapterType>> serviceInfos = getAllServices(userId);
            ArrayList<String> packages = new ArrayList<>();
            for (RegisteredServicesCache.ServiceInfo<SyncAdapterType> serviceInfo : serviceInfos) {
                if (authority.equals(serviceInfo.type.authority) && serviceInfo.componentName != null) {
                    packages.add(serviceInfo.componentName.getPackageName());
                }
            }
            String[] syncAdapterPackages = new String[packages.size()];
            packages.toArray(syncAdapterPackages);
            adapterMap.put(authority, syncAdapterPackages);
            return syncAdapterPackages;
        }
    }

    /* access modifiers changed from: protected */
    @Override // android.content.pm.RegisteredServicesCache
    public void onUserRemoved(int userId) {
        synchronized (this.mServicesLock) {
            this.mAuthorityToSyncAdapters.remove(userId);
        }
        super.onUserRemoved(userId);
    }

    static class MySerializer implements XmlSerializerAndParser<SyncAdapterType> {
        MySerializer() {
        }

        public void writeAsXml(SyncAdapterType item, XmlSerializer out) throws IOException {
            out.attribute(null, ContactsContract.Directory.DIRECTORY_AUTHORITY, item.authority);
            out.attribute(null, "accountType", item.accountType);
        }

        @Override // android.content.pm.XmlSerializerAndParser
        public SyncAdapterType createFromXml(XmlPullParser parser) throws IOException, XmlPullParserException {
            return SyncAdapterType.newKey(parser.getAttributeValue(null, ContactsContract.Directory.DIRECTORY_AUTHORITY), parser.getAttributeValue(null, "accountType"));
        }
    }
}
