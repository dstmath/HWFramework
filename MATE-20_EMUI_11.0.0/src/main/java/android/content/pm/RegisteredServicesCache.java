package android.content.pm;

import android.Manifest;
import android.annotation.UnsupportedAppUsage;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Environment;
import android.os.Handler;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.AtomicFile;
import android.util.AttributeSet;
import android.util.IntArray;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.Xml;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.FastXmlSerializer;
import com.google.android.collect.Lists;
import com.google.android.collect.Maps;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public abstract class RegisteredServicesCache<V> {
    private static final boolean DEBUG = false;
    protected static final String REGISTERED_SERVICES_DIR = "registered_services";
    private static final String TAG = "PackageManager";
    private final String mAttributesName;
    public final Context mContext;
    private final BroadcastReceiver mExternalReceiver = new BroadcastReceiver() {
        /* class android.content.pm.RegisteredServicesCache.AnonymousClass2 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            RegisteredServicesCache.this.handlePackageEvent(intent, 0);
        }
    };
    private Handler mHandler;
    private final String mInterfaceName;
    private RegisteredServicesCacheListener<V> mListener;
    private final String mMetaDataName;
    private final BroadcastReceiver mPackageReceiver = new BroadcastReceiver() {
        /* class android.content.pm.RegisteredServicesCache.AnonymousClass1 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            int uid = intent.getIntExtra(Intent.EXTRA_UID, -1);
            if (uid != -1) {
                RegisteredServicesCache.this.handlePackageEvent(intent, UserHandle.getUserId(uid));
            }
        }
    };
    private final XmlSerializerAndParser<V> mSerializerAndParser;
    protected final Object mServicesLock = new Object();
    private final BroadcastReceiver mUserRemovedReceiver = new BroadcastReceiver() {
        /* class android.content.pm.RegisteredServicesCache.AnonymousClass3 */

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) {
            RegisteredServicesCache.this.onUserRemoved(intent.getIntExtra(Intent.EXTRA_USER_HANDLE, -1));
        }
    };
    @GuardedBy({"mServicesLock"})
    private final SparseArray<UserServices<V>> mUserServices = new SparseArray<>(2);

    public abstract V parseServiceAttributes(Resources resources, String str, AttributeSet attributeSet);

    /* access modifiers changed from: private */
    public static class UserServices<V> {
        @GuardedBy({"mServicesLock"})
        boolean mBindInstantServiceAllowed;
        @GuardedBy({"mServicesLock"})
        boolean mPersistentServicesFileDidNotExist;
        @GuardedBy({"mServicesLock"})
        final Map<V, Integer> persistentServices;
        @GuardedBy({"mServicesLock"})
        Map<V, ServiceInfo<V>> services;

        private UserServices() {
            this.persistentServices = Maps.newHashMap();
            this.services = null;
            this.mPersistentServicesFileDidNotExist = true;
            this.mBindInstantServiceAllowed = false;
        }
    }

    @GuardedBy({"mServicesLock"})
    private UserServices<V> findOrCreateUserLocked(int userId) {
        return findOrCreateUserLocked(userId, true);
    }

    @GuardedBy({"mServicesLock"})
    private UserServices<V> findOrCreateUserLocked(int userId, boolean loadFromFileIfNew) {
        UserInfo user;
        UserServices<V> services = this.mUserServices.get(userId);
        if (services == null) {
            services = new UserServices<>();
            this.mUserServices.put(userId, services);
            if (!(!loadFromFileIfNew || this.mSerializerAndParser == null || (user = getUser(userId)) == null)) {
                AtomicFile file = createFileForUser(user.id);
                if (file.getBaseFile().exists()) {
                    InputStream is = null;
                    try {
                        is = file.openRead();
                        readPersistentServicesLocked(is);
                    } catch (Exception e) {
                        Log.w(TAG, "Error reading persistent services for user " + user.id, e);
                    } catch (Throwable th) {
                        IoUtils.closeQuietly(is);
                        throw th;
                    }
                    IoUtils.closeQuietly(is);
                }
            }
        }
        return services;
    }

    @UnsupportedAppUsage
    public RegisteredServicesCache(Context context, String interfaceName, String metaDataName, String attributeName, XmlSerializerAndParser<V> serializerAndParser) {
        this.mContext = context;
        this.mInterfaceName = interfaceName;
        this.mMetaDataName = metaDataName;
        this.mAttributesName = attributeName;
        this.mSerializerAndParser = serializerAndParser;
        migrateIfNecessaryLocked();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addDataScheme("package");
        this.mContext.registerReceiverAsUser(this.mPackageReceiver, UserHandle.ALL, intentFilter, null, null);
        IntentFilter sdFilter = new IntentFilter();
        sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
        sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
        this.mContext.registerReceiver(this.mExternalReceiver, sdFilter);
        IntentFilter userFilter = new IntentFilter();
        sdFilter.addAction(Intent.ACTION_USER_REMOVED);
        this.mContext.registerReceiver(this.mUserRemovedReceiver, userFilter);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void handlePackageEvent(Intent intent, int userId) {
        String action = intent.getAction();
        boolean isRemoval = Intent.ACTION_PACKAGE_REMOVED.equals(action) || Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE.equals(action);
        boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
        if (!isRemoval || !replacing) {
            int[] uids = null;
            if (Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE.equals(action) || Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE.equals(action)) {
                uids = intent.getIntArrayExtra(Intent.EXTRA_CHANGED_UID_LIST);
            } else {
                int uid = intent.getIntExtra(Intent.EXTRA_UID, -1);
                if (uid > 0) {
                    uids = new int[]{uid};
                }
            }
            generateServicesMap(uids, userId);
        }
    }

    public void invalidateCache(int userId) {
        synchronized (this.mServicesLock) {
            findOrCreateUserLocked(userId).services = null;
            onServicesChangedLocked(userId);
        }
    }

    public void dump(FileDescriptor fd, PrintWriter fout, String[] args, int userId) {
        synchronized (this.mServicesLock) {
            UserServices<V> user = findOrCreateUserLocked(userId);
            if (user.services != null) {
                fout.println("RegisteredServicesCache: " + user.services.size() + " services");
                Iterator<ServiceInfo<V>> it = user.services.values().iterator();
                while (it.hasNext()) {
                    fout.println("  " + it.next());
                }
            } else {
                fout.println("RegisteredServicesCache: services not loaded");
            }
        }
    }

    public RegisteredServicesCacheListener<V> getListener() {
        RegisteredServicesCacheListener<V> registeredServicesCacheListener;
        synchronized (this) {
            registeredServicesCacheListener = this.mListener;
        }
        return registeredServicesCacheListener;
    }

    public void setListener(RegisteredServicesCacheListener<V> listener, Handler handler) {
        if (handler == null) {
            handler = new Handler(this.mContext.getMainLooper());
        }
        synchronized (this) {
            this.mHandler = handler;
            this.mListener = listener;
        }
    }

    private void notifyListener(V type, int userId, boolean removed) {
        RegisteredServicesCacheListener<V> listener;
        Handler handler;
        synchronized (this) {
            listener = this.mListener;
            handler = this.mHandler;
        }
        if (listener != null) {
            handler.post(new Runnable(type, userId, removed) {
                /* class android.content.pm.$$Lambda$RegisteredServicesCache$lDXmLhKoG7lZpIyDOuPYOrjzDYY */
                private final /* synthetic */ Object f$1;
                private final /* synthetic */ int f$2;
                private final /* synthetic */ boolean f$3;

                {
                    this.f$1 = r2;
                    this.f$2 = r3;
                    this.f$3 = r4;
                }

                @Override // java.lang.Runnable
                public final void run() {
                    RegisteredServicesCache.lambda$notifyListener$0(RegisteredServicesCacheListener.this, this.f$1, this.f$2, this.f$3);
                }
            });
        }
    }

    static /* synthetic */ void lambda$notifyListener$0(RegisteredServicesCacheListener listener2, Object type, int userId, boolean removed) {
        try {
            listener2.onServiceChanged(type, userId, removed);
        } catch (Throwable th) {
            Slog.wtf(TAG, "Exception from onServiceChanged", th);
        }
    }

    public static class ServiceInfo<V> {
        public final ComponentInfo componentInfo;
        @UnsupportedAppUsage
        public final ComponentName componentName;
        @UnsupportedAppUsage
        public final V type;
        @UnsupportedAppUsage
        public final int uid;

        public ServiceInfo(V type2, ComponentInfo componentInfo2, ComponentName componentName2) {
            this.type = type2;
            this.componentInfo = componentInfo2;
            this.componentName = componentName2;
            this.uid = componentInfo2 != null ? componentInfo2.applicationInfo.uid : -1;
        }

        public String toString() {
            return "ServiceInfo: " + ((Object) this.type) + ", " + this.componentName + ", uid " + this.uid;
        }
    }

    public ServiceInfo<V> getServiceInfo(V type, int userId) {
        ServiceInfo<V> serviceInfo;
        synchronized (this.mServicesLock) {
            UserServices<V> user = findOrCreateUserLocked(userId);
            if (user.services == null) {
                generateServicesMap(null, userId);
            }
            serviceInfo = user.services.get(type);
        }
        return serviceInfo;
    }

    public Collection<ServiceInfo<V>> getAllServices(int userId) {
        Collection<ServiceInfo<V>> unmodifiableCollection;
        synchronized (this.mServicesLock) {
            UserServices<V> user = findOrCreateUserLocked(userId);
            if (user.services == null) {
                generateServicesMap(null, userId);
            }
            unmodifiableCollection = Collections.unmodifiableCollection(new ArrayList(user.services.values()));
        }
        return unmodifiableCollection;
    }

    public void updateServices(int userId) {
        List<ServiceInfo<V>> allServices;
        synchronized (this.mServicesLock) {
            UserServices<V> user = findOrCreateUserLocked(userId);
            if (user.services != null) {
                allServices = new ArrayList<>(user.services.values());
            } else {
                return;
            }
        }
        IntArray updatedUids = null;
        for (ServiceInfo<V> service : allServices) {
            long versionCode = (long) service.componentInfo.applicationInfo.versionCode;
            ApplicationInfo newAppInfo = null;
            try {
                newAppInfo = this.mContext.getPackageManager().getApplicationInfoAsUser(service.componentInfo.packageName, 0, userId);
            } catch (PackageManager.NameNotFoundException e) {
            }
            if (newAppInfo == null || ((long) newAppInfo.versionCode) != versionCode) {
                if (updatedUids == null) {
                    updatedUids = new IntArray();
                }
                updatedUids.add(service.uid);
            }
        }
        if (updatedUids != null && updatedUids.size() > 0) {
            generateServicesMap(updatedUids.toArray(), userId);
        }
    }

    public boolean getBindInstantServiceAllowed(int userId) {
        boolean z;
        this.mContext.enforceCallingOrSelfPermission(Manifest.permission.MANAGE_BIND_INSTANT_SERVICE, "getBindInstantServiceAllowed");
        synchronized (this.mServicesLock) {
            z = findOrCreateUserLocked(userId).mBindInstantServiceAllowed;
        }
        return z;
    }

    public void setBindInstantServiceAllowed(int userId, boolean allowed) {
        this.mContext.enforceCallingOrSelfPermission(Manifest.permission.MANAGE_BIND_INSTANT_SERVICE, "setBindInstantServiceAllowed");
        synchronized (this.mServicesLock) {
            findOrCreateUserLocked(userId).mBindInstantServiceAllowed = allowed;
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public boolean inSystemImage(int callerUid) {
        String[] packages = this.mContext.getPackageManager().getPackagesForUid(callerUid);
        if (packages != null) {
            for (String name : packages) {
                try {
                    if ((this.mContext.getPackageManager().getPackageInfo(name, 0).applicationInfo.flags & 1) != 0) {
                        return true;
                    }
                } catch (PackageManager.NameNotFoundException e) {
                    return false;
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public List<ResolveInfo> queryIntentServices(int userId) {
        PackageManager pm = this.mContext.getPackageManager();
        int flags = 786560;
        synchronized (this.mServicesLock) {
            if (findOrCreateUserLocked(userId).mBindInstantServiceAllowed) {
                flags = 786560 | 8388608;
            }
        }
        return pm.queryIntentServicesAsUser(new Intent(this.mInterfaceName), flags, userId);
    }

    /* JADX DEBUG: Multi-variable search result rejected for r16v0, resolved type: android.content.pm.RegisteredServicesCache<V> */
    /* JADX WARN: Multi-variable type inference failed */
    private void generateServicesMap(int[] changedUids, int userId) {
        ArrayList<ServiceInfo<V>> serviceInfos = new ArrayList<>();
        for (ResolveInfo resolveInfo : queryIntentServices(userId)) {
            try {
                ServiceInfo<V> info = parseServiceInfo(resolveInfo);
                if (info == null) {
                    Log.w(TAG, "Unable to load service info " + resolveInfo.toString());
                } else {
                    serviceInfos.add(info);
                }
            } catch (IOException | XmlPullParserException e) {
                Log.w(TAG, "Unable to load service info " + resolveInfo.toString(), e);
            }
        }
        synchronized (this.mServicesLock) {
            try {
                UserServices<V> user = findOrCreateUserLocked(userId);
                boolean firstScan = user.services == null;
                if (firstScan) {
                    user.services = Maps.newHashMap();
                }
                new StringBuilder();
                boolean changed = false;
                Iterator<ServiceInfo<V>> it = serviceInfos.iterator();
                while (it.hasNext()) {
                    ServiceInfo<V> info2 = it.next();
                    Integer previousUid = user.persistentServices.get(info2.type);
                    if (previousUid == null) {
                        changed = true;
                        user.services.put(info2.type, info2);
                        user.persistentServices.put(info2.type, Integer.valueOf(info2.uid));
                        if (!user.mPersistentServicesFileDidNotExist || !firstScan) {
                            notifyListener(info2.type, userId, false);
                        }
                    } else if (previousUid.intValue() == info2.uid) {
                        user.services.put(info2.type, info2);
                    } else if (inSystemImage(info2.uid) || !containsTypeAndUid(serviceInfos, info2.type, previousUid.intValue())) {
                        user.services.put(info2.type, info2);
                        user.persistentServices.put(info2.type, Integer.valueOf(info2.uid));
                        notifyListener(info2.type, userId, false);
                        changed = true;
                    }
                }
                ArrayList<V> toBeRemoved = Lists.newArrayList();
                for (V v1 : user.persistentServices.keySet()) {
                    if (!containsType(serviceInfos, v1)) {
                        if (containsUid(changedUids, user.persistentServices.get(v1).intValue())) {
                            toBeRemoved.add(v1);
                        }
                    }
                }
                Iterator<V> it2 = toBeRemoved.iterator();
                while (it2.hasNext()) {
                    V v12 = it2.next();
                    changed = true;
                    user.persistentServices.remove(v12);
                    user.services.remove(v12);
                    notifyListener(v12, userId, true);
                }
                if (changed) {
                    onServicesChangedLocked(userId);
                    writePersistentServicesLocked(user, userId);
                }
            } catch (Throwable th) {
                th = th;
                throw th;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onServicesChangedLocked(int userId) {
    }

    private boolean containsUid(int[] changedUids, int uid) {
        return changedUids == null || ArrayUtils.contains(changedUids, uid);
    }

    private boolean containsType(ArrayList<ServiceInfo<V>> serviceInfos, V type) {
        int N = serviceInfos.size();
        for (int i = 0; i < N; i++) {
            if (serviceInfos.get(i).type.equals(type)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsTypeAndUid(ArrayList<ServiceInfo<V>> serviceInfos, V type, int uid) {
        int N = serviceInfos.size();
        for (int i = 0; i < N; i++) {
            ServiceInfo<V> serviceInfo = serviceInfos.get(i);
            if (serviceInfo.type.equals(type) && serviceInfo.uid == uid) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public ServiceInfo<V> parseServiceInfo(ResolveInfo service) throws XmlPullParserException, IOException {
        XmlResourceParser parser;
        ServiceInfo si = service.serviceInfo;
        ComponentName componentName = new ComponentName(si.packageName, si.name);
        PackageManager pm = this.mContext.getPackageManager();
        XmlResourceParser parser2 = null;
        try {
            parser = si.loadXmlMetaData(pm, this.mMetaDataName);
        } catch (PackageManager.NameNotFoundException e) {
            throw new XmlPullParserException("Unable to load resources for pacakge " + si.packageName);
        } catch (Throwable th) {
            if (0 != 0) {
                parser2.close();
            }
            throw th;
        }
        if (parser != null) {
            AttributeSet attrs = Xml.asAttributeSet(parser);
            while (true) {
                int type = parser.next();
                if (type == 1 || type == 2) {
                    break;
                }
            }
            if (this.mAttributesName.equals(parser.getName())) {
                V v = parseServiceAttributes(pm.getResourcesForApplication(si.applicationInfo), si.packageName, attrs);
                if (v == null) {
                    parser.close();
                    return null;
                }
                ServiceInfo<V> serviceInfo = new ServiceInfo<>(v, service.serviceInfo, componentName);
                parser.close();
                return serviceInfo;
            }
            throw new XmlPullParserException("Meta-data does not start with " + this.mAttributesName + " tag");
        }
        throw new XmlPullParserException("No " + this.mMetaDataName + " meta-data");
    }

    private void readPersistentServicesLocked(InputStream is) throws XmlPullParserException, IOException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(is, StandardCharsets.UTF_8.name());
        int eventType = parser.getEventType();
        while (eventType != 2 && eventType != 1) {
            eventType = parser.next();
        }
        if ("services".equals(parser.getName())) {
            int eventType2 = parser.next();
            do {
                if (eventType2 == 2 && parser.getDepth() == 2 && "service".equals(parser.getName())) {
                    V service = this.mSerializerAndParser.createFromXml(parser);
                    if (service != null) {
                        int uid = Integer.parseInt(parser.getAttributeValue(null, "uid"));
                        findOrCreateUserLocked(UserHandle.getUserId(uid), false).persistentServices.put(service, Integer.valueOf(uid));
                    } else {
                        return;
                    }
                }
                eventType2 = parser.next();
            } while (eventType2 != 1);
        }
    }

    private void migrateIfNecessaryLocked() {
        if (this.mSerializerAndParser != null) {
            File syncDir = new File(new File(getDataDirectory(), "system"), REGISTERED_SERVICES_DIR);
            AtomicFile oldFile = new AtomicFile(new File(syncDir, this.mInterfaceName + ".xml"));
            if (oldFile.getBaseFile().exists()) {
                File marker = new File(syncDir, this.mInterfaceName + ".xml.migrated");
                if (!marker.exists()) {
                    InputStream is = null;
                    try {
                        is = oldFile.openRead();
                        this.mUserServices.clear();
                        readPersistentServicesLocked(is);
                    } catch (Exception e) {
                        Log.w(TAG, "Error reading persistent services, starting from scratch", e);
                    } catch (Throwable th) {
                        IoUtils.closeQuietly(is);
                        throw th;
                    }
                    IoUtils.closeQuietly(is);
                    try {
                        for (UserInfo user : getUsers()) {
                            UserServices<V> userServices = this.mUserServices.get(user.id);
                            if (userServices != null) {
                                writePersistentServicesLocked(userServices, user.id);
                            }
                        }
                        marker.createNewFile();
                    } catch (Exception e2) {
                        Log.w(TAG, "Migration failed", e2);
                    }
                    this.mUserServices.clear();
                }
            }
        }
    }

    private void writePersistentServicesLocked(UserServices<V> user, int userId) {
        if (this.mSerializerAndParser != null) {
            AtomicFile atomicFile = createFileForUser(userId);
            try {
                FileOutputStream fos = atomicFile.startWrite();
                XmlSerializer out = new FastXmlSerializer();
                out.setOutput(fos, StandardCharsets.UTF_8.name());
                out.startDocument(null, true);
                out.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                out.startTag(null, "services");
                for (Map.Entry<V, Integer> service : user.persistentServices.entrySet()) {
                    out.startTag(null, "service");
                    out.attribute(null, "uid", Integer.toString(service.getValue().intValue()));
                    this.mSerializerAndParser.writeAsXml(service.getKey(), out);
                    out.endTag(null, "service");
                }
                out.endTag(null, "services");
                out.endDocument();
                atomicFile.finishWrite(fos);
            } catch (IOException e1) {
                Log.w(TAG, "Error writing accounts", e1);
                if (0 != 0) {
                    atomicFile.failWrite(null);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public void onUserRemoved(int userId) {
        synchronized (this.mServicesLock) {
            this.mUserServices.remove(userId);
        }
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public List<UserInfo> getUsers() {
        return UserManager.get(this.mContext).getUsers(true);
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public UserInfo getUser(int userId) {
        return UserManager.get(this.mContext).getUserInfo(userId);
    }

    private AtomicFile createFileForUser(int userId) {
        File userDir = getUserSystemDirectory(userId);
        return new AtomicFile(new File(userDir, "registered_services/" + this.mInterfaceName + ".xml"));
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public File getUserSystemDirectory(int userId) {
        return Environment.getUserSystemDirectory(userId);
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public File getDataDirectory() {
        return Environment.getDataDirectory();
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public Map<V, Integer> getPersistentServices(int userId) {
        return findOrCreateUserLocked(userId).persistentServices;
    }
}
