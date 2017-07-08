package android.content;

import android.content.pm.RegisteredServicesCache;
import android.content.pm.RegisteredServicesCache.ServiceInfo;
import android.content.pm.XmlSerializerAndParser;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.provider.ContactsContract.Directory;
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
    private static final MySerializer sSerializer = null;
    @GuardedBy("mServicesLock")
    private SparseArray<ArrayMap<String, String[]>> mAuthorityToSyncAdapters;

    static class MySerializer implements XmlSerializerAndParser<SyncAdapterType> {
        MySerializer() {
        }

        public void writeAsXml(SyncAdapterType item, XmlSerializer out) throws IOException {
            out.attribute(null, Directory.DIRECTORY_AUTHORITY, item.authority);
            out.attribute(null, Directory.ACCOUNT_TYPE, item.accountType);
        }

        public SyncAdapterType createFromXml(XmlPullParser parser) throws IOException, XmlPullParserException {
            return SyncAdapterType.newKey(parser.getAttributeValue(null, Directory.DIRECTORY_AUTHORITY), parser.getAttributeValue(null, Directory.ACCOUNT_TYPE));
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.content.SyncAdaptersCache.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.content.SyncAdaptersCache.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.SyncAdaptersCache.<clinit>():void");
    }

    public SyncAdaptersCache(Context context) {
        super(context, SERVICE_META_DATA, SERVICE_META_DATA, ATTRIBUTES_NAME, sSerializer);
        this.mAuthorityToSyncAdapters = new SparseArray();
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
            SyncAdapterType syncAdapterType = new SyncAdapterType(authority, accountType, sa.getBoolean(3, true), sa.getBoolean(4, true), sa.getBoolean(6, false), sa.getBoolean(5, false), sa.getString(0));
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
