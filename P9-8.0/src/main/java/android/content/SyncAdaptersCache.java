package android.content;

import android.accounts.AccountManager;
import android.content.pm.RegisteredServicesCache;
import android.content.pm.RegisteredServicesCache.ServiceInfo;
import android.content.pm.XmlSerializerAndParser;
import android.content.res.Resources;
import android.content.res.TypedArray;
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
    @GuardedBy("mServicesLock")
    private SparseArray<ArrayMap<String, String[]>> mAuthorityToSyncAdapters = new SparseArray();

    static class MySerializer implements XmlSerializerAndParser<SyncAdapterType> {
        MySerializer() {
        }

        public void writeAsXml(SyncAdapterType item, XmlSerializer out) throws IOException {
            out.attribute(null, "authority", item.authority);
            out.attribute(null, AccountManager.KEY_ACCOUNT_TYPE, item.accountType);
        }

        public SyncAdapterType createFromXml(XmlPullParser parser) throws IOException, XmlPullParserException {
            return SyncAdapterType.newKey(parser.getAttributeValue(null, "authority"), parser.getAttributeValue(null, AccountManager.KEY_ACCOUNT_TYPE));
        }
    }

    public SyncAdaptersCache(Context context) {
        super(context, "android.content.SyncAdapter", "android.content.SyncAdapter", ATTRIBUTES_NAME, sSerializer);
    }

    public SyncAdapterType parseServiceAttributes(Resources res, String packageName, AttributeSet attrs) {
        TypedArray sa = res.obtainAttributes(attrs, R.styleable.SyncAdapter);
        try {
            String authority = sa.getString(2);
            String accountType = sa.getString(1);
            if (authority == null || accountType == null) {
                sa.recycle();
                return null;
            }
            SyncAdapterType syncAdapterType = new SyncAdapterType(authority, accountType, sa.getBoolean(3, true), sa.getBoolean(4, true), sa.getBoolean(6, false), sa.getBoolean(5, false), sa.getString(0), packageName);
            sa.recycle();
            return syncAdapterType;
        } catch (Throwable th) {
            sa.recycle();
        }
    }

    protected void onServicesChangedLocked(int userId) {
        synchronized (this.mServicesLock) {
            ArrayMap<String, String[]> adapterMap = (ArrayMap) this.mAuthorityToSyncAdapters.get(userId);
            if (adapterMap != null) {
                adapterMap.clear();
            }
        }
        super.onServicesChangedLocked(userId);
    }

    public String[] getSyncAdapterPackagesForAuthority(String authority, int userId) {
        synchronized (this.mServicesLock) {
            ArrayMap<String, String[]> adapterMap = (ArrayMap) this.mAuthorityToSyncAdapters.get(userId);
            if (adapterMap == null) {
                adapterMap = new ArrayMap();
                this.mAuthorityToSyncAdapters.put(userId, adapterMap);
            }
            if (adapterMap.containsKey(authority)) {
                String[] strArr = (String[]) adapterMap.get(authority);
                return strArr;
            }
            Collection<ServiceInfo<SyncAdapterType>> serviceInfos = getAllServices(userId);
            ArrayList<String> packages = new ArrayList();
            for (ServiceInfo<SyncAdapterType> serviceInfo : serviceInfos) {
                if (authority.equals(((SyncAdapterType) serviceInfo.type).authority) && serviceInfo.componentName != null) {
                    packages.add(serviceInfo.componentName.getPackageName());
                }
            }
            String[] syncAdapterPackages = new String[packages.size()];
            packages.toArray(syncAdapterPackages);
            adapterMap.put(authority, syncAdapterPackages);
            return syncAdapterPackages;
        }
    }

    protected void onUserRemoved(int userId) {
        synchronized (this.mServicesLock) {
            this.mAuthorityToSyncAdapters.remove(userId);
        }
        super.onUserRemoved(userId);
    }
}
