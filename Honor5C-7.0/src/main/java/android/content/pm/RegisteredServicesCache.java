package android.content.pm;

import android.common.HwFrameworkMonitor;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.hwtheme.HwThemeManager;
import android.os.Environment;
import android.os.Handler;
import android.os.UserHandle;
import android.os.UserManager;
import android.rms.iaware.DataContract.BaseProperty;
import android.service.quicksettings.TileService;
import android.util.AtomicFile;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.util.Xml;
import com.android.internal.annotations.GuardedBy;
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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
    private final BroadcastReceiver mExternalReceiver;
    private Handler mHandler;
    private final String mInterfaceName;
    private RegisteredServicesCacheListener<V> mListener;
    private final String mMetaDataName;
    private final BroadcastReceiver mPackageReceiver;
    private final XmlSerializerAndParser<V> mSerializerAndParser;
    protected final Object mServicesLock;
    private final BroadcastReceiver mUserRemovedReceiver;
    @GuardedBy("mServicesLock")
    private final SparseArray<UserServices<V>> mUserServices;

    /* renamed from: android.content.pm.RegisteredServicesCache.4 */
    class AnonymousClass4 implements Runnable {
        final /* synthetic */ RegisteredServicesCacheListener val$listener2;
        final /* synthetic */ boolean val$removed;
        final /* synthetic */ Object val$type;
        final /* synthetic */ int val$userId;

        AnonymousClass4(RegisteredServicesCacheListener val$listener2, Object val$type, int val$userId, boolean val$removed) {
            this.val$listener2 = val$listener2;
            this.val$type = val$type;
            this.val$userId = val$userId;
            this.val$removed = val$removed;
        }

        public void run() {
            this.val$listener2.onServiceChanged(this.val$type, this.val$userId, this.val$removed);
        }
    }

    public static class ServiceInfo<V> {
        public final ComponentInfo componentInfo;
        public final ComponentName componentName;
        public final V type;
        public final int uid;

        public ServiceInfo(V type, ComponentInfo componentInfo, ComponentName componentName) {
            this.type = type;
            this.componentInfo = componentInfo;
            this.componentName = componentName;
            this.uid = componentInfo != null ? componentInfo.applicationInfo.uid : -1;
        }

        public String toString() {
            return "ServiceInfo: " + this.type + ", " + this.componentName + ", uid " + this.uid;
        }
    }

    private static class UserServices<V> {
        @GuardedBy("mServicesLock")
        boolean mPersistentServicesFileDidNotExist;
        @GuardedBy("mServicesLock")
        final Map<V, Integer> persistentServices;
        @GuardedBy("mServicesLock")
        Map<V, ServiceInfo<V>> services;

        private UserServices() {
            this.persistentServices = Maps.newHashMap();
            this.services = null;
            this.mPersistentServicesFileDidNotExist = true;
        }
    }

    public abstract V parseServiceAttributes(Resources resources, String str, AttributeSet attributeSet);

    @GuardedBy("mServicesLock")
    private UserServices<V> findOrCreateUserLocked(int userId) {
        return findOrCreateUserLocked(userId, true);
    }

    @GuardedBy("mServicesLock")
    private UserServices<V> findOrCreateUserLocked(int userId, boolean loadFromFileIfNew) {
        UserServices<V> services = (UserServices) this.mUserServices.get(userId);
        if (services == null) {
            services = new UserServices();
            this.mUserServices.put(userId, services);
            if (loadFromFileIfNew && this.mSerializerAndParser != null) {
                UserInfo user = getUser(userId);
                if (user != null) {
                    AtomicFile file = createFileForUser(user.id);
                    if (file.getBaseFile().exists()) {
                        AutoCloseable autoCloseable = null;
                        try {
                            autoCloseable = file.openRead();
                            readPersistentServicesLocked(autoCloseable);
                        } catch (Exception e) {
                            Log.w(TAG, "Error reading persistent services for user " + user.id, e);
                        } finally {
                            IoUtils.closeQuietly(autoCloseable);
                        }
                    }
                }
            }
        }
        return services;
    }

    public RegisteredServicesCache(Context context, String interfaceName, String metaDataName, String attributeName, XmlSerializerAndParser<V> serializerAndParser) {
        this.mServicesLock = new Object();
        this.mUserServices = new SparseArray(2);
        this.mPackageReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                int uid = intent.getIntExtra(Intent.EXTRA_UID, -1);
                if (uid != -1) {
                    RegisteredServicesCache.this.handlePackageEvent(intent, UserHandle.getUserId(uid));
                }
            }
        };
        this.mExternalReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                RegisteredServicesCache.this.handlePackageEvent(intent, 0);
            }
        };
        this.mUserRemovedReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                RegisteredServicesCache.this.onUserRemoved(intent.getIntExtra(Intent.EXTRA_USER_HANDLE, -1));
            }
        };
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
        intentFilter.addDataScheme(HwFrameworkMonitor.KEY_PACKAGE);
        this.mContext.registerReceiverAsUser(this.mPackageReceiver, UserHandle.ALL, intentFilter, null, null);
        IntentFilter sdFilter = new IntentFilter();
        sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE);
        sdFilter.addAction(Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE);
        this.mContext.registerReceiver(this.mExternalReceiver, sdFilter);
        IntentFilter userFilter = new IntentFilter();
        sdFilter.addAction(Intent.ACTION_USER_REMOVED);
        this.mContext.registerReceiver(this.mUserRemovedReceiver, userFilter);
    }

    private final void handlePackageEvent(Intent intent, int userId) {
        boolean isRemoval;
        String action = intent.getAction();
        if (Intent.ACTION_PACKAGE_REMOVED.equals(action)) {
            isRemoval = true;
        } else {
            isRemoval = Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE.equals(action);
        }
        boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, DEBUG);
        if (!isRemoval || !replacing) {
            int[] uids = null;
            if (Intent.ACTION_EXTERNAL_APPLICATIONS_AVAILABLE.equals(action) || Intent.ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE.equals(action)) {
                uids = intent.getIntArrayExtra(Intent.EXTRA_CHANGED_UID_LIST);
            } else {
                if (intent.getIntExtra(Intent.EXTRA_UID, -1) > 0) {
                    uids = new int[]{intent.getIntExtra(Intent.EXTRA_UID, -1)};
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
                for (ServiceInfo<?> info : user.services.values()) {
                    fout.println("  " + info);
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
        synchronized (this) {
            RegisteredServicesCacheListener<V> listener = this.mListener;
            Handler handler = this.mHandler;
        }
        if (listener != null) {
            RegisteredServicesCacheListener<V> listener2 = listener;
            handler.post(new AnonymousClass4(listener, type, userId, removed));
        }
    }

    public ServiceInfo<V> getServiceInfo(V type, int userId) {
        ServiceInfo<V> serviceInfo;
        synchronized (this.mServicesLock) {
            UserServices<V> user = findOrCreateUserLocked(userId);
            if (user.services == null) {
                generateServicesMap(null, userId);
            }
            serviceInfo = (ServiceInfo) user.services.get(type);
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

    protected boolean inSystemImage(int callerUid) {
        String[] packages = this.mContext.getPackageManager().getPackagesForUid(callerUid);
        if (packages == null) {
            Log.w(TAG, "Attempt to get length of null array");
            return DEBUG;
        }
        int length = packages.length;
        int i = 0;
        while (i < length) {
            try {
                if ((this.mContext.getPackageManager().getPackageInfo(packages[i], 0).applicationInfo.flags & 1) != 0) {
                    return true;
                }
                i++;
            } catch (NameNotFoundException e) {
                return DEBUG;
            }
        }
        return DEBUG;
    }

    protected List<ResolveInfo> queryIntentServices(int userId) {
        return this.mContext.getPackageManager().queryIntentServicesAsUser(new Intent(this.mInterfaceName), 786560, userId);
    }

    private void generateServicesMap(int[] changedUids, int userId) {
        ArrayList<ServiceInfo<V>> serviceInfos = new ArrayList();
        for (ResolveInfo resolveInfo : queryIntentServices(userId)) {
            ServiceInfo<V> info;
            try {
                info = parseServiceInfo(resolveInfo);
                if (info == null) {
                    Log.w(TAG, "Unable to load service info " + resolveInfo.toString());
                } else {
                    serviceInfos.add(info);
                }
            } catch (Exception e) {
                Log.w(TAG, "Unable to load service info " + resolveInfo.toString(), e);
            }
        }
        synchronized (this.mServicesLock) {
            UserServices<V> user = findOrCreateUserLocked(userId);
            boolean firstScan = user.services == null ? true : DEBUG;
            if (firstScan) {
                user.services = Maps.newHashMap();
            }
            StringBuilder changes = new StringBuilder();
            boolean changed = DEBUG;
            for (ServiceInfo<V> info2 : serviceInfos) {
                Integer previousUid = (Integer) user.persistentServices.get(info2.type);
                if (previousUid == null) {
                    changed = true;
                    user.services.put(info2.type, info2);
                    user.persistentServices.put(info2.type, Integer.valueOf(info2.uid));
                    if (!(user.mPersistentServicesFileDidNotExist ? firstScan : DEBUG)) {
                        notifyListener(info2.type, userId, DEBUG);
                    }
                } else {
                    if (previousUid.intValue() == info2.uid) {
                        user.services.put(info2.type, info2);
                    } else {
                        if (!inSystemImage(info2.uid)) {
                            if (containsTypeAndUid(serviceInfos, info2.type, previousUid.intValue())) {
                            }
                        }
                        changed = true;
                        user.services.put(info2.type, info2);
                        user.persistentServices.put(info2.type, Integer.valueOf(info2.uid));
                        notifyListener(info2.type, userId, DEBUG);
                    }
                }
            }
            ArrayList<V> toBeRemoved = Lists.newArrayList();
            for (V v1 : user.persistentServices.keySet()) {
                if (!containsType(serviceInfos, v1)) {
                    if (containsUid(changedUids, ((Integer) user.persistentServices.get(v1)).intValue())) {
                        toBeRemoved.add(v1);
                    }
                }
            }
            for (V v12 : toBeRemoved) {
                changed = true;
                user.persistentServices.remove(v12);
                user.services.remove(v12);
                notifyListener(v12, userId, true);
            }
            if (changed) {
                onServicesChangedLocked(userId);
                writePersistentServicesLocked(user, userId);
            }
        }
    }

    protected void onServicesChangedLocked(int userId) {
    }

    private boolean containsUid(int[] changedUids, int uid) {
        return changedUids != null ? ArrayUtils.contains(changedUids, uid) : true;
    }

    private boolean containsType(ArrayList<ServiceInfo<V>> serviceInfos, V type) {
        int N = serviceInfos.size();
        for (int i = 0; i < N; i++) {
            if (((ServiceInfo) serviceInfos.get(i)).type.equals(type)) {
                return true;
            }
        }
        return DEBUG;
    }

    private boolean containsTypeAndUid(ArrayList<ServiceInfo<V>> serviceInfos, V type, int uid) {
        int N = serviceInfos.size();
        for (int i = 0; i < N; i++) {
            ServiceInfo<V> serviceInfo = (ServiceInfo) serviceInfos.get(i);
            if (serviceInfo.type.equals(type) && serviceInfo.uid == uid) {
                return true;
            }
        }
        return DEBUG;
    }

    protected ServiceInfo<V> parseServiceInfo(ResolveInfo service) throws XmlPullParserException, IOException {
        ServiceInfo si = service.serviceInfo;
        ComponentName componentName = new ComponentName(si.packageName, si.name);
        PackageManager pm = this.mContext.getPackageManager();
        XmlResourceParser xmlResourceParser = null;
        try {
            xmlResourceParser = si.loadXmlMetaData(pm, this.mMetaDataName);
            if (xmlResourceParser == null) {
                throw new XmlPullParserException("No " + this.mMetaDataName + " meta-data");
            }
            AttributeSet attrs = Xml.asAttributeSet(xmlResourceParser);
            int type;
            do {
                type = xmlResourceParser.next();
                if (type == 1) {
                    break;
                }
            } while (type != 2);
            if (this.mAttributesName.equals(xmlResourceParser.getName())) {
                V v = parseServiceAttributes(pm.getResourcesForApplication(si.applicationInfo), si.packageName, attrs);
                if (v == null) {
                    if (xmlResourceParser != null) {
                        xmlResourceParser.close();
                    }
                    return null;
                }
                ServiceInfo<V> serviceInfo = new ServiceInfo(v, service.serviceInfo, componentName);
                if (xmlResourceParser != null) {
                    xmlResourceParser.close();
                }
                return serviceInfo;
            }
            throw new XmlPullParserException("Meta-data does not start with " + this.mAttributesName + " tag");
        } catch (NameNotFoundException e) {
            throw new XmlPullParserException("Unable to load resources for pacakge " + si.packageName);
        } catch (Throwable th) {
            if (xmlResourceParser != null) {
                xmlResourceParser.close();
            }
        }
    }

    private void readPersistentServicesLocked(InputStream is) throws XmlPullParserException, IOException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(is, StandardCharsets.UTF_8.name());
        int eventType = parser.getEventType();
        while (eventType != 2 && eventType != 1) {
            eventType = parser.next();
        }
        if ("services".equals(parser.getName())) {
            eventType = parser.next();
            while (true) {
                if (eventType == 2 && parser.getDepth() == 2) {
                    if (TileService.EXTRA_SERVICE.equals(parser.getName())) {
                        V service = this.mSerializerAndParser.createFromXml(parser);
                        if (service != null) {
                            int uid = Integer.parseInt(parser.getAttributeValue(null, BaseProperty.UID));
                            findOrCreateUserLocked(UserHandle.getUserId(uid), DEBUG).persistentServices.put(service, Integer.valueOf(uid));
                        } else {
                            return;
                        }
                    }
                }
                eventType = parser.next();
                if (eventType == 1) {
                    return;
                }
            }
        }
    }

    private void migrateIfNecessaryLocked() {
        if (this.mSerializerAndParser != null) {
            File syncDir = new File(new File(getDataDirectory(), HwThemeManager.HWT_USER_SYSTEM), REGISTERED_SERVICES_DIR);
            AtomicFile oldFile = new AtomicFile(new File(syncDir, this.mInterfaceName + ".xml"));
            if (oldFile.getBaseFile().exists()) {
                File marker = new File(syncDir, this.mInterfaceName + ".xml.migrated");
                if (!marker.exists()) {
                    AutoCloseable autoCloseable = null;
                    try {
                        autoCloseable = oldFile.openRead();
                        this.mUserServices.clear();
                        readPersistentServicesLocked(autoCloseable);
                    } catch (Exception e) {
                        Log.w(TAG, "Error reading persistent services, starting from scratch", e);
                    } finally {
                        IoUtils.closeQuietly(autoCloseable);
                    }
                    try {
                        for (UserInfo user : getUsers()) {
                            UserServices<V> userServices = (UserServices) this.mUserServices.get(user.id);
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
            FileOutputStream fileOutputStream = null;
            try {
                fileOutputStream = atomicFile.startWrite();
                XmlSerializer out = new FastXmlSerializer();
                out.setOutput(fileOutputStream, StandardCharsets.UTF_8.name());
                out.startDocument(null, Boolean.valueOf(true));
                out.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
                out.startTag(null, "services");
                for (Entry<V, Integer> service : user.persistentServices.entrySet()) {
                    out.startTag(null, TileService.EXTRA_SERVICE);
                    out.attribute(null, BaseProperty.UID, Integer.toString(((Integer) service.getValue()).intValue()));
                    this.mSerializerAndParser.writeAsXml(service.getKey(), out);
                    out.endTag(null, TileService.EXTRA_SERVICE);
                }
                out.endTag(null, "services");
                out.endDocument();
                atomicFile.finishWrite(fileOutputStream);
            } catch (IOException e1) {
                Log.w(TAG, "Error writing accounts", e1);
                if (fileOutputStream != null) {
                    atomicFile.failWrite(fileOutputStream);
                }
            }
        }
    }

    protected void onUserRemoved(int userId) {
        synchronized (this.mServicesLock) {
            this.mUserServices.remove(userId);
        }
    }

    protected List<UserInfo> getUsers() {
        return UserManager.get(this.mContext).getUsers(true);
    }

    protected UserInfo getUser(int userId) {
        return UserManager.get(this.mContext).getUserInfo(userId);
    }

    private AtomicFile createFileForUser(int userId) {
        return new AtomicFile(new File(getUserSystemDirectory(userId), "registered_services/" + this.mInterfaceName + ".xml"));
    }

    protected File getUserSystemDirectory(int userId) {
        return Environment.getUserSystemDirectory(userId);
    }

    protected File getDataDirectory() {
        return Environment.getDataDirectory();
    }

    protected Map<V, Integer> getPersistentServices(int userId) {
        return findOrCreateUserLocked(userId).persistentServices;
    }
}
