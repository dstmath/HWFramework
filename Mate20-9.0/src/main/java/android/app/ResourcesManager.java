package android.app;

import android.content.pm.PackageParser;
import android.content.res.ApkAssets;
import android.content.res.AssetManager;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.ResourcesImpl;
import android.content.res.ResourcesKey;
import android.hardware.display.DisplayManagerGlobal;
import android.hwtheme.HwThemeManager;
import android.os.IBinder;
import android.os.Process;
import android.os.SystemProperties;
import android.os.Trace;
import android.util.ArrayMap;
import android.util.DisplayMetrics;
import android.util.HwPCUtils;
import android.util.Log;
import android.util.LruCache;
import android.util.Pair;
import android.util.Slog;
import android.view.Display;
import android.view.DisplayAdjustments;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.IndentingPrintWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.function.Predicate;

public class ResourcesManager {
    private static final boolean DEBUG = false;
    private static final boolean ENABLE_LRU_CACHE = SystemProperties.getBoolean("persist.sys.enable_apk_assets_lru_cache", true);
    private static final String FWK_ANDROID_TAG = "frameworkoverlaydark";
    private static final String FWK_EXT_TAG = "frameworkhwext";
    static final String TAG = "ResourcesManager";
    private static final Predicate<WeakReference<Resources>> sEmptyReferencePredicate = $$Lambda$ResourcesManager$QJ7UiVk_XS90KuXAsIjIEym1DnM.INSTANCE;
    private static ArrayMap<String, Integer> sHwThemeType = new ArrayMap<>();
    private static ResourcesManager sResourcesManager;
    private static final Object sThemeTypeLock = new Object();
    private final WeakHashMap<IBinder, ActivityResources> mActivityResourceReferences = new WeakHashMap<>();
    private final ArrayMap<Pair<Integer, DisplayAdjustments>, WeakReference<Display>> mAdjustedDisplays = new ArrayMap<>();
    private final ArrayMap<ApkKey, WeakReference<ApkAssets>> mCachedApkAssets = new ArrayMap<>();
    private final LruCache<ApkKey, ApkAssets> mLoadedApkAssets = new LruCache<>(3);
    private CompatibilityInfo mResCompatibilityInfo;
    private final Configuration mResConfiguration = new Configuration();
    private final ArrayMap<ResourcesKey, WeakReference<ResourcesImpl>> mResourceImpls = new ArrayMap<>();
    private final ArrayList<WeakReference<Resources>> mResourceReferences = new ArrayList<>();

    private static class ActivityResources {
        public final ArrayList<WeakReference<Resources>> activityResources;
        public final Configuration overrideConfig;

        private ActivityResources() {
            this.overrideConfig = new Configuration();
            this.activityResources = new ArrayList<>();
        }
    }

    private static class ApkKey {
        public final boolean overlay;
        public final String path;
        public final boolean sharedLib;

        ApkKey(String path2, boolean sharedLib2, boolean overlay2) {
            this.path = path2;
            this.sharedLib = sharedLib2;
            this.overlay = overlay2;
        }

        public int hashCode() {
            return (31 * ((31 * ((31 * 1) + this.path.hashCode())) + Boolean.hashCode(this.sharedLib))) + Boolean.hashCode(this.overlay);
        }

        public boolean equals(Object obj) {
            boolean z = false;
            if (!(obj instanceof ApkKey)) {
                return false;
            }
            ApkKey other = (ApkKey) obj;
            if (this.path.equals(other.path) && this.sharedLib == other.sharedLib && this.overlay == other.overlay) {
                z = true;
            }
            return z;
        }
    }

    public void setHwThemeType(String resDir, int type) {
        if (resDir != null) {
            synchronized (sThemeTypeLock) {
                sHwThemeType.put(resDir, Integer.valueOf(type));
            }
        }
    }

    static /* synthetic */ boolean lambda$static$0(WeakReference weakRef) {
        return weakRef == null || weakRef.get() == null;
    }

    public static ResourcesManager getInstance() {
        ResourcesManager resourcesManager;
        synchronized (ResourcesManager.class) {
            if (sResourcesManager == null) {
                sResourcesManager = new ResourcesManager();
            }
            resourcesManager = sResourcesManager;
        }
        return resourcesManager;
    }

    public void invalidatePath(String path) {
        synchronized (this) {
            int count = 0;
            int i = 0;
            while (i < this.mResourceImpls.size()) {
                ResourcesKey key = this.mResourceImpls.keyAt(i);
                if (key.isPathReferenced(path)) {
                    cleanupResourceImpl(key);
                    count++;
                } else {
                    i++;
                }
            }
            Log.i(TAG, "Invalidated " + count + " asset managers that referenced " + path);
        }
    }

    public Configuration getConfiguration() {
        Configuration configuration;
        synchronized (this) {
            configuration = this.mResConfiguration;
        }
        return configuration;
    }

    /* access modifiers changed from: package-private */
    public DisplayMetrics getDisplayMetrics() {
        return getDisplayMetrics(0, DisplayAdjustments.DEFAULT_DISPLAY_ADJUSTMENTS);
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public DisplayMetrics getDisplayMetrics(int displayId, DisplayAdjustments da) {
        DisplayMetrics dm = new DisplayMetrics();
        Display display = getAdjustedDisplay(displayId, da);
        if (display != null) {
            display.getMetrics(dm);
        } else {
            dm.setToDefaults();
        }
        return dm;
    }

    private static void applyNonDefaultDisplayMetricsToConfiguration(DisplayMetrics dm, Configuration config) {
        config.touchscreen = 1;
        config.densityDpi = dm.densityDpi;
        config.screenWidthDp = (int) (((float) dm.widthPixels) / dm.density);
        config.screenHeightDp = (int) (((float) dm.heightPixels) / dm.density);
        int sl = Configuration.resetScreenLayout(config.screenLayout);
        if (dm.widthPixels > dm.heightPixels) {
            config.orientation = 2;
            config.screenLayout = Configuration.reduceScreenLayout(sl, config.screenWidthDp, config.screenHeightDp);
        } else {
            config.orientation = 1;
            config.screenLayout = Configuration.reduceScreenLayout(sl, config.screenHeightDp, config.screenWidthDp);
        }
        config.smallestScreenWidthDp = config.screenWidthDp;
        config.compatScreenWidthDp = config.screenWidthDp;
        config.compatScreenHeightDp = config.screenHeightDp;
        config.compatSmallestScreenWidthDp = config.smallestScreenWidthDp;
    }

    public boolean applyCompatConfigurationLocked(int displayDensity, Configuration compatConfiguration) {
        if (this.mResCompatibilityInfo == null || this.mResCompatibilityInfo.supportsScreen()) {
            return false;
        }
        this.mResCompatibilityInfo.applyToConfiguration(displayDensity, compatConfiguration);
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0049, code lost:
        return r4;
     */
    private Display getAdjustedDisplay(int displayId, DisplayAdjustments displayAdjustments) {
        Pair<Integer, DisplayAdjustments> key = Pair.create(Integer.valueOf(displayId), displayAdjustments != null ? new DisplayAdjustments(displayAdjustments) : new DisplayAdjustments());
        synchronized (this) {
            WeakReference<Display> wd = this.mAdjustedDisplays.get(key);
            if (wd != null) {
                Display display = (Display) wd.get();
                if (display != null) {
                    return display;
                }
            }
            DisplayManagerGlobal dm = DisplayManagerGlobal.getInstance();
            if (dm == null) {
                return null;
            }
            Display display2 = dm.getCompatibleDisplay(displayId, (DisplayAdjustments) key.second);
            if (display2 != null) {
                this.mAdjustedDisplays.put(key, new WeakReference(display2));
            }
        }
    }

    public Display getAdjustedDisplay(int displayId, Resources resources) {
        synchronized (this) {
            DisplayManagerGlobal dm = DisplayManagerGlobal.getInstance();
            if (dm == null) {
                return null;
            }
            Display compatibleDisplay = dm.getCompatibleDisplay(displayId, resources);
            return compatibleDisplay;
        }
    }

    private void cleanupResourceImpl(ResourcesKey removedKey) {
        ResourcesImpl res = (ResourcesImpl) this.mResourceImpls.remove(removedKey).get();
        if (res != null) {
            res.flushLayoutCache();
        }
    }

    private static String overlayPathToIdmapPath(String path) {
        return "/data/resource-cache/" + path.substring(1).replace('/', '@') + "@idmap";
    }

    private ApkAssets loadApkAssets(String path, boolean sharedLib, boolean overlay) throws IOException {
        ApkAssets apkAssets;
        ApkKey newKey = new ApkKey(path, sharedLib, overlay);
        boolean isSystemServer = false;
        if (ENABLE_LRU_CACHE) {
            isSystemServer = ActivityThread.isSystem();
        }
        if (isSystemServer) {
            ApkAssets apkAssets2 = this.mLoadedApkAssets.get(newKey);
            if (apkAssets2 != null) {
                return apkAssets2;
            }
        }
        WeakReference<ApkAssets> apkAssetsRef = this.mCachedApkAssets.get(newKey);
        if (apkAssetsRef != null) {
            ApkAssets apkAssets3 = (ApkAssets) apkAssetsRef.get();
            if (apkAssets3 != null) {
                if (isSystemServer) {
                    this.mLoadedApkAssets.put(newKey, apkAssets3);
                }
                return apkAssets3;
            }
            this.mCachedApkAssets.remove(newKey);
        }
        if (overlay) {
            apkAssets = ApkAssets.loadOverlayFromPath(overlayPathToIdmapPath(path), false);
        } else {
            apkAssets = ApkAssets.loadFromPath(path, false, sharedLib);
        }
        if (isSystemServer) {
            this.mLoadedApkAssets.put(newKey, apkAssets);
        }
        this.mCachedApkAssets.put(newKey, new WeakReference(apkAssets));
        return apkAssets;
    }

    /* access modifiers changed from: protected */
    @VisibleForTesting
    public AssetManager createAssetManager(ResourcesKey key) {
        AssetManager.Builder builder = new AssetManager.Builder();
        if (key.mResDir != null) {
            try {
                builder.addApkAssets(loadApkAssets(key.mResDir, false, false));
            } catch (IOException e) {
                Log.e(TAG, "failed to add asset path " + key.mResDir);
                return null;
            }
        }
        Integer type = null;
        if (key.mResDir != null) {
            synchronized (sThemeTypeLock) {
                type = sHwThemeType.get(key.mResDir);
            }
        }
        if (key.mSplitResDirs != null) {
            String[] strArr = key.mSplitResDirs;
            int length = strArr.length;
            int i = 0;
            while (i < length) {
                try {
                    builder.addApkAssets(loadApkAssets(strArr[i], false, false));
                    i++;
                } catch (IOException e2) {
                    Log.e(TAG, "failed to add split asset path " + splitResDir);
                    return null;
                }
            }
        }
        boolean isNeedSuppprotDeepType = key.mResDir != null && (!HwPCUtils.enabled() || ActivityThread.currentActivityThread() == null || !HwPCUtils.isValidExtDisplayId(ActivityThread.currentActivityThread().getDisplayId()));
        if (key.mOverlayDirs != null) {
            for (String idmapPath : key.mOverlayDirs) {
                if (idmapPath != null) {
                    try {
                        if (idmapPath.contains("frameworkhwext") || idmapPath.contains(FWK_ANDROID_TAG)) {
                            if (isNeedSuppprotDeepType && type != null) {
                                if (type.intValue() != 0) {
                                    if (idmapPath.contains(HwThemeManager.DARK_TAG) && (type.intValue() & 1) == 1) {
                                        builder.addApkAssets(loadApkAssets(idmapPath, false, true));
                                        builder.getAssets().setDeepType(type.intValue());
                                    }
                                    if (idmapPath.contains(HwThemeManager.HONOR_TAG) && HwThemeManager.isHonorProduct() && (type.intValue() & 16) == 16) {
                                        builder.addApkAssets(loadApkAssets(idmapPath, false, true));
                                        builder.getAssets().setDeepType(type.intValue());
                                    }
                                    if (idmapPath.contains(HwThemeManager.NOVA_TAG) && HwThemeManager.isNovaProduct() && (type.intValue() & 256) == 256) {
                                        builder.addApkAssets(loadApkAssets(idmapPath, false, true));
                                        builder.getAssets().setDeepType(type.intValue());
                                    }
                                }
                            }
                        }
                    } catch (IOException e3) {
                        Log.w(TAG, "failed to add overlay path " + idmapPath);
                    }
                }
                builder.addApkAssets(loadApkAssets(idmapPath, false, true));
            }
        }
        if (key.mLibDirs != null) {
            for (String libDir : key.mLibDirs) {
                if (libDir.endsWith(PackageParser.APK_FILE_EXTENSION)) {
                    try {
                        builder.addApkAssets(loadApkAssets(libDir, true, false));
                    } catch (IOException e4) {
                        Log.w(TAG, "Asset path '" + libDir + "' does not exist or contains no resources.");
                    }
                }
            }
        }
        return builder.build();
    }

    private static <T> int countLiveReferences(Collection<WeakReference<T>> collection) {
        int count = 0;
        Iterator<WeakReference<T>> it = collection.iterator();
        while (it.hasNext()) {
            WeakReference<T> ref = it.next();
            if ((ref != null ? ref.get() : null) != null) {
                count++;
            }
        }
        return count;
    }

    public void dump(String prefix, PrintWriter printWriter) {
        synchronized (this) {
            IndentingPrintWriter pw = new IndentingPrintWriter(printWriter, "  ");
            for (int i = 0; i < prefix.length() / 2; i++) {
                pw.increaseIndent();
            }
            pw.println("ResourcesManager:");
            pw.increaseIndent();
            if (ENABLE_LRU_CACHE && ActivityThread.isSystem()) {
                pw.print("cached apks: total=");
                pw.print(this.mLoadedApkAssets.size());
                pw.print(" created=");
                pw.print(this.mLoadedApkAssets.createCount());
                pw.print(" evicted=");
                pw.print(this.mLoadedApkAssets.evictionCount());
                pw.print(" hit=");
                pw.print(this.mLoadedApkAssets.hitCount());
                pw.print(" miss=");
                pw.print(this.mLoadedApkAssets.missCount());
                pw.print(" max=");
                pw.print(this.mLoadedApkAssets.maxSize());
                pw.println();
            }
            pw.print("total apks: ");
            pw.println(countLiveReferences(this.mCachedApkAssets.values()));
            pw.print("resources: ");
            int references = countLiveReferences(this.mResourceReferences);
            for (ActivityResources activityResources : this.mActivityResourceReferences.values()) {
                references += countLiveReferences(activityResources.activityResources);
            }
            pw.println(references);
            pw.print("resource impls: ");
            pw.println(countLiveReferences(this.mResourceImpls.values()));
            if (ENABLE_LRU_CACHE) {
                pw.print("callerPid=" + Process.myPid() + ", isSystemServer=" + ActivityThread.isSystem());
            }
        }
    }

    private Configuration generateConfig(ResourcesKey key, DisplayMetrics dm) {
        boolean isDefaultDisplay = key.mDisplayId == 0;
        boolean hasOverrideConfig = key.hasOverrideConfiguration();
        if (isDefaultDisplay && !hasOverrideConfig) {
            return getConfiguration();
        }
        Configuration config = new Configuration(getConfiguration());
        if (!isDefaultDisplay) {
            applyNonDefaultDisplayMetricsToConfiguration(dm, config);
        }
        if (!hasOverrideConfig) {
            return config;
        }
        config.updateFrom(key.mOverrideConfiguration);
        return config;
    }

    private ResourcesImpl createResourcesImpl(ResourcesKey key) {
        DisplayAdjustments daj = new DisplayAdjustments(key.mOverrideConfiguration);
        daj.setCompatibilityInfo(key.mCompatInfo);
        AssetManager assets = createAssetManager(key);
        if (assets == null) {
            return null;
        }
        DisplayMetrics dm = getDisplayMetrics(key.mDisplayId, daj);
        return new ResourcesImpl(assets, dm, generateConfig(key, dm), daj);
    }

    private ResourcesImpl findResourcesImplForKeyLocked(ResourcesKey key) {
        WeakReference<ResourcesImpl> weakImplRef = this.mResourceImpls.get(key);
        ResourcesImpl impl = weakImplRef != null ? (ResourcesImpl) weakImplRef.get() : null;
        if (impl == null || !impl.getAssets().isUpToDate()) {
            return null;
        }
        return impl;
    }

    private ResourcesImpl findOrCreateResourcesImplForKeyLocked(ResourcesKey key) {
        ResourcesImpl impl = findResourcesImplForKeyLocked(key);
        if (impl == null) {
            impl = createResourcesImpl(key);
            if (impl != null) {
                this.mResourceImpls.put(key, new WeakReference(impl));
            }
        }
        return impl;
    }

    private ResourcesKey findKeyForResourceImplLocked(ResourcesImpl resourceImpl) {
        int refCount = this.mResourceImpls.size();
        int i = 0;
        while (true) {
            ResourcesImpl impl = null;
            if (i >= refCount) {
                return null;
            }
            WeakReference<ResourcesImpl> weakImplRef = this.mResourceImpls.valueAt(i);
            if (weakImplRef != null) {
                impl = (ResourcesImpl) weakImplRef.get();
            }
            if (impl != null && resourceImpl == impl) {
                return this.mResourceImpls.keyAt(i);
            }
            i++;
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0018, code lost:
        return r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0033, code lost:
        return r1;
     */
    public boolean isSameResourcesOverrideConfig(IBinder activityToken, Configuration overrideConfig) {
        ActivityResources activityResources;
        synchronized (this) {
            if (activityToken != null) {
                try {
                    activityResources = this.mActivityResourceReferences.get(activityToken);
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                activityResources = null;
            }
            boolean z = false;
            if (activityResources != null) {
                if (!Objects.equals(activityResources.overrideConfig, overrideConfig)) {
                    if (overrideConfig == null || activityResources.overrideConfig == null || overrideConfig.diffPublicOnly(activityResources.overrideConfig) != 0) {
                    }
                }
                z = true;
            } else if (overrideConfig == null) {
                z = true;
            }
        }
    }

    private ActivityResources getOrCreateActivityResourcesStructLocked(IBinder activityToken) {
        ActivityResources activityResources = this.mActivityResourceReferences.get(activityToken);
        if (activityResources != null) {
            return activityResources;
        }
        ActivityResources activityResources2 = new ActivityResources();
        this.mActivityResourceReferences.put(activityToken, activityResources2);
        return activityResources2;
    }

    private Resources getOrCreateResourcesForActivityLocked(IBinder activityToken, ClassLoader classLoader, ResourcesImpl impl, CompatibilityInfo compatInfo) {
        ActivityResources activityResources = getOrCreateActivityResourcesStructLocked(activityToken);
        int refCount = activityResources.activityResources.size();
        for (int i = 0; i < refCount; i++) {
            Resources resources = (Resources) activityResources.activityResources.get(i).get();
            if (resources != null && Objects.equals(resources.getClassLoader(), classLoader) && resources.getImpl() == impl) {
                return resources;
            }
        }
        Resources resources2 = HwThemeManager.getResources(classLoader);
        if (resources2 != null) {
            resources2.setImpl(impl);
        }
        activityResources.activityResources.add(new WeakReference(resources2));
        return resources2;
    }

    private Resources getOrCreateResourcesLocked(ClassLoader classLoader, ResourcesImpl impl, CompatibilityInfo compatInfo) {
        int refCount = this.mResourceReferences.size();
        for (int i = 0; i < refCount; i++) {
            Resources resources = (Resources) this.mResourceReferences.get(i).get();
            if (resources != null && Objects.equals(resources.getClassLoader(), classLoader) && resources.getImpl() == impl) {
                return resources;
            }
        }
        Resources resources2 = HwThemeManager.getResources(classLoader);
        if (resources2 != null) {
            resources2.setImpl(impl);
        }
        this.mResourceReferences.add(new WeakReference(resources2));
        return resources2;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:28:0x004c, code lost:
        r0 = th;
     */
    public Resources createBaseActivityResources(IBinder activityToken, String resDir, String[] splitResDirs, String[] overlayDirs, String[] libDirs, int displayId, Configuration overrideConfig, CompatibilityInfo compatInfo, ClassLoader classLoader) {
        IBinder iBinder = activityToken;
        Configuration configuration = overrideConfig;
        try {
            Trace.traceBegin(8192, "ResourcesManager#createBaseActivityResources");
            ResourcesKey resourcesKey = new ResourcesKey(resDir, splitResDirs, overlayDirs, libDirs, displayId, configuration != null ? new Configuration(configuration) : null, compatInfo);
            ResourcesKey key = resourcesKey;
            ClassLoader classLoader2 = classLoader != null ? classLoader : ClassLoader.getSystemClassLoader();
            try {
                synchronized (this) {
                    try {
                        getOrCreateActivityResourcesStructLocked(iBinder);
                    } catch (Throwable th) {
                        th = th;
                    }
                }
                updateResourcesForActivity(iBinder, configuration, displayId, false);
                Resources orCreateResources = getOrCreateResources(iBinder, key, classLoader2);
                Trace.traceEnd(8192);
                return orCreateResources;
            } catch (Throwable th2) {
                th = th2;
                int i = displayId;
                Trace.traceEnd(8192);
                throw th;
            }
        } catch (Throwable th3) {
            th = th3;
            int i2 = displayId;
            ClassLoader classLoader3 = classLoader;
            Trace.traceEnd(8192);
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0079, code lost:
        return r1;
     */
    private Resources getOrCreateResources(IBinder activityToken, ResourcesKey key, ClassLoader classLoader) {
        Resources resources;
        synchronized (this) {
            if (activityToken != null) {
                try {
                    ActivityResources activityResources = getOrCreateActivityResourcesStructLocked(activityToken);
                    ArrayUtils.unstableRemoveIf(activityResources.activityResources, sEmptyReferencePredicate);
                    if (key.hasOverrideConfiguration() && !activityResources.overrideConfig.equals(Configuration.EMPTY)) {
                        Configuration temp = new Configuration(activityResources.overrideConfig);
                        temp.updateFrom(key.mOverrideConfiguration);
                        key.mOverrideConfiguration.setTo(temp);
                    }
                    ResourcesImpl resourcesImpl = findResourcesImplForKeyLocked(key);
                    if (resourcesImpl != null) {
                        Resources orCreateResourcesForActivityLocked = getOrCreateResourcesForActivityLocked(activityToken, classLoader, resourcesImpl, key.mCompatInfo);
                        return orCreateResourcesForActivityLocked;
                    }
                } catch (Throwable th) {
                    throw th;
                }
            } else {
                ArrayUtils.unstableRemoveIf(this.mResourceReferences, sEmptyReferencePredicate);
                ResourcesImpl resourcesImpl2 = findResourcesImplForKeyLocked(key);
                if (resourcesImpl2 != null) {
                    Resources orCreateResourcesLocked = getOrCreateResourcesLocked(classLoader, resourcesImpl2, key.mCompatInfo);
                    return orCreateResourcesLocked;
                }
            }
            ResourcesImpl resourcesImpl3 = createResourcesImpl(key);
            if (resourcesImpl3 == null) {
                return null;
            }
            this.mResourceImpls.put(key, new WeakReference(resourcesImpl3));
            if (activityToken != null) {
                resources = getOrCreateResourcesForActivityLocked(activityToken, classLoader, resourcesImpl3, key.mCompatInfo);
            } else {
                resources = getOrCreateResourcesLocked(classLoader, resourcesImpl3, key.mCompatInfo);
            }
        }
    }

    public Resources getResources(IBinder activityToken, String resDir, String[] splitResDirs, String[] overlayDirs, String[] libDirs, int displayId, Configuration overrideConfig, CompatibilityInfo compatInfo, ClassLoader classLoader) {
        Configuration configuration = overrideConfig;
        try {
            Trace.traceBegin(8192, "ResourcesManager#getResources");
            ResourcesKey resourcesKey = new ResourcesKey(resDir, splitResDirs, overlayDirs, libDirs, displayId, configuration != null ? new Configuration(configuration) : null, compatInfo);
            try {
                Resources orCreateResources = getOrCreateResources(activityToken, resourcesKey, classLoader != null ? classLoader : ClassLoader.getSystemClassLoader());
                Trace.traceEnd(8192);
                return orCreateResources;
            } catch (Throwable th) {
                th = th;
                Trace.traceEnd(8192);
                throw th;
            }
        } catch (Throwable th2) {
            th = th2;
            IBinder iBinder = activityToken;
            ClassLoader classLoader2 = classLoader;
            Trace.traceEnd(8192);
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:50:0x010e, code lost:
        android.os.Trace.traceEnd(8192);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x0114, code lost:
        return;
     */
    public void updateResourcesForActivity(IBinder activityToken, Configuration overrideConfig, int displayId, boolean movedToDifferentDisplay) {
        ActivityResources activityResources;
        Configuration configuration = overrideConfig;
        try {
            Trace.traceBegin(8192, "ResourcesManager#updateResourcesForActivity");
            synchronized (this) {
                ActivityResources activityResources2 = getOrCreateActivityResourcesStructLocked(activityToken);
                if (!Objects.equals(activityResources2.overrideConfig, configuration) || movedToDifferentDisplay) {
                    Configuration oldConfig = new Configuration(activityResources2.overrideConfig);
                    if (configuration != null) {
                        activityResources2.overrideConfig.setTo(configuration);
                    } else {
                        activityResources2.overrideConfig.unset();
                    }
                    boolean activityHasOverrideConfig = !activityResources2.overrideConfig.equals(Configuration.EMPTY);
                    int refCount = activityResources2.activityResources.size();
                    int i = 0;
                    while (i < refCount) {
                        Resources resources = (Resources) activityResources2.activityResources.get(i).get();
                        if (resources != null) {
                            ResourcesKey oldKey = findKeyForResourceImplLocked(resources.getImpl());
                            if (oldKey == null) {
                                Slog.e(TAG, "can't find ResourcesKey for resources impl=" + resources.getImpl());
                            } else {
                                Configuration rebasedOverrideConfig = new Configuration();
                                if (configuration != null) {
                                    rebasedOverrideConfig.setTo(configuration);
                                }
                                if (activityHasOverrideConfig && oldKey.hasOverrideConfiguration()) {
                                    rebasedOverrideConfig.updateFrom(Configuration.generateDelta(oldConfig, oldKey.mOverrideConfiguration));
                                }
                                activityResources = activityResources2;
                                ResourcesKey resourcesKey = new ResourcesKey(oldKey.mResDir, oldKey.mSplitResDirs, oldKey.mOverlayDirs, oldKey.mLibDirs, displayId, rebasedOverrideConfig, oldKey.mCompatInfo);
                                ResourcesKey newKey = resourcesKey;
                                ResourcesImpl resourcesImpl = findResourcesImplForKeyLocked(newKey);
                                if (resourcesImpl == null) {
                                    resourcesImpl = createResourcesImpl(newKey);
                                    if (resourcesImpl != null) {
                                        this.mResourceImpls.put(newKey, new WeakReference(resourcesImpl));
                                    }
                                }
                                if (!(resourcesImpl == null || resourcesImpl == resources.getImpl())) {
                                    resources.setImpl(resourcesImpl);
                                }
                                i++;
                                activityResources2 = activityResources;
                            }
                        }
                        activityResources = activityResources2;
                        i++;
                        activityResources2 = activityResources;
                    }
                    if (HwPCUtils.enabled() && HwPCUtils.isValidExtDisplayId(displayId) && configuration != null && !configuration.equals(Configuration.EMPTY)) {
                        ActivityThread.currentActivityThread().updateOverrideConfig(configuration);
                        ActivityThread.currentActivityThread().applyConfigurationToResources(configuration);
                    }
                } else {
                    Trace.traceEnd(8192);
                }
            }
        } catch (Throwable th) {
            Trace.traceEnd(8192);
            throw th;
        }
    }

    public final boolean applyConfigurationToResourcesLocked(Configuration config, CompatibilityInfo compat) {
        DisplayMetrics defaultDisplayMetrics;
        Configuration configuration = config;
        CompatibilityInfo compatibilityInfo = compat;
        try {
            Trace.traceBegin(8192, "ResourcesManager#applyConfigurationToResourcesLocked");
            if (this.mResConfiguration.isOtherSeqNewer(configuration) || compatibilityInfo != null) {
                int changes = this.mResConfiguration.updateFrom(configuration);
                this.mAdjustedDisplays.clear();
                ResourcesImpl resourcesImpl = null;
                if (!HwPCUtils.enabled() || ActivityThread.currentActivityThread() == null || !HwPCUtils.isValidExtDisplayId(ActivityThread.currentActivityThread().mDisplayId)) {
                    defaultDisplayMetrics = getDisplayMetrics();
                } else {
                    DisplayAdjustments adj = new DisplayAdjustments(configuration);
                    if (HwPCUtils.enabledInPad() && (ActivityThread.currentActivityThread().getOverrideConfig() == null || ActivityThread.currentActivityThread().getOverrideConfig().equals(Configuration.EMPTY))) {
                        Configuration newConfiguration = new Configuration(configuration);
                        newConfiguration.windowConfiguration.setAppBounds(null);
                        adj = new DisplayAdjustments(newConfiguration);
                    }
                    defaultDisplayMetrics = getDisplayMetrics(ActivityThread.currentActivityThread().mDisplayId, adj);
                }
                if (compatibilityInfo != null && (this.mResCompatibilityInfo == null || !this.mResCompatibilityInfo.equals(compatibilityInfo))) {
                    this.mResCompatibilityInfo = compatibilityInfo;
                    changes |= 3328;
                }
                Resources.updateSystemConfiguration(configuration, defaultDisplayMetrics, compatibilityInfo);
                ApplicationPackageManager.configurationChanged();
                Configuration tmpConfig = null;
                boolean z = true;
                int i = this.mResourceImpls.size() - 1;
                while (i >= 0) {
                    ResourcesKey key = this.mResourceImpls.keyAt(i);
                    WeakReference<ResourcesImpl> weakImplRef = this.mResourceImpls.valueAt(i);
                    ResourcesImpl r = weakImplRef != null ? (ResourcesImpl) weakImplRef.get() : resourcesImpl;
                    if (r == null) {
                        this.mResourceImpls.removeAt(i);
                    } else if (!HwPCUtils.enabled() || ActivityThread.currentActivityThread() == null || !HwPCUtils.isValidExtDisplayId(ActivityThread.currentActivityThread().mDisplayId)) {
                        if (ActivityThread.DEBUG_CONFIGURATION) {
                            Slog.v(TAG, "Changing resources " + r + " config to: " + configuration);
                        }
                        int displayId = key.mDisplayId;
                        boolean isDefaultDisplay = displayId == 0 ? z : false;
                        DisplayMetrics dm = defaultDisplayMetrics;
                        boolean hasOverrideConfiguration = key.hasOverrideConfiguration();
                        if (isDefaultDisplay) {
                            if (!hasOverrideConfiguration) {
                                r.updateConfiguration(configuration, dm, compatibilityInfo);
                            }
                        }
                        if (tmpConfig == null) {
                            tmpConfig = new Configuration();
                        }
                        tmpConfig.setTo(configuration);
                        DisplayAdjustments daj = r.getDisplayAdjustments();
                        if (compatibilityInfo != null) {
                            daj = new DisplayAdjustments(daj);
                            daj.setCompatibilityInfo(compatibilityInfo);
                        }
                        DisplayMetrics dm2 = getDisplayMetrics(displayId, daj);
                        if (!isDefaultDisplay) {
                            applyNonDefaultDisplayMetricsToConfiguration(dm2, tmpConfig);
                        }
                        if (hasOverrideConfiguration) {
                            tmpConfig.updateFrom(key.mOverrideConfiguration);
                        }
                        r.updateConfiguration(tmpConfig, dm2, compatibilityInfo);
                    } else {
                        r.updateConfiguration(configuration, defaultDisplayMetrics, compatibilityInfo);
                    }
                    i--;
                    resourcesImpl = null;
                    z = true;
                }
                return changes != 0;
            }
            if (ActivityThread.DEBUG_CONFIGURATION) {
                Slog.v(TAG, "Skipping new config: curSeq=" + this.mResConfiguration.seq + ", newSeq=" + configuration.seq);
            }
            Trace.traceEnd(8192);
            return false;
        } finally {
            Trace.traceEnd(8192);
        }
    }

    public void appendLibAssetForMainAssetPath(String assetPath, String libAsset) {
        int implCount;
        String str = libAsset;
        synchronized (this) {
            try {
                ArrayMap<ResourcesImpl, ResourcesKey> updatedResourceKeys = new ArrayMap<>();
                int implCount2 = this.mResourceImpls.size();
                int i = 0;
                int i2 = 0;
                while (i2 < implCount2) {
                    ResourcesKey key = this.mResourceImpls.keyAt(i2);
                    WeakReference<ResourcesImpl> weakImplRef = this.mResourceImpls.valueAt(i2);
                    ResourcesImpl impl = weakImplRef != null ? (ResourcesImpl) weakImplRef.get() : null;
                    if (impl == null) {
                        String str2 = assetPath;
                    } else if (Objects.equals(key.mResDir, assetPath) && !ArrayUtils.contains(key.mLibDirs, str)) {
                        String[] newLibAssets = new String[(1 + (key.mLibDirs != null ? key.mLibDirs.length : i))];
                        if (key.mLibDirs != null) {
                            System.arraycopy(key.mLibDirs, i, newLibAssets, i, key.mLibDirs.length);
                        }
                        newLibAssets[newLibAssetCount - 1] = str;
                        String str3 = key.mResDir;
                        String[] strArr = key.mSplitResDirs;
                        String[] strArr2 = key.mOverlayDirs;
                        int i3 = key.mDisplayId;
                        Configuration configuration = key.mOverrideConfiguration;
                        implCount = implCount2;
                        CompatibilityInfo compatibilityInfo = key.mCompatInfo;
                        ResourcesKey resourcesKey = key;
                        ResourcesKey key2 = r12;
                        ResourcesKey resourcesKey2 = new ResourcesKey(str3, strArr, strArr2, newLibAssets, i3, configuration, compatibilityInfo);
                        updatedResourceKeys.put(impl, key2);
                        i2++;
                        implCount2 = implCount;
                        str = libAsset;
                        i = 0;
                    }
                    implCount = implCount2;
                    i2++;
                    implCount2 = implCount;
                    str = libAsset;
                    i = 0;
                }
                String str4 = assetPath;
                int i4 = implCount2;
                redirectResourcesToNewImplLocked(updatedResourceKeys);
            } catch (Throwable th) {
                th = th;
                throw th;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public final void applyNewResourceDirsLocked(String baseCodePath, String[] newResourceDirs) {
        try {
            Trace.traceBegin(8192, "ResourcesManager#applyNewResourceDirsLocked");
            ArrayMap<ResourcesImpl, ResourcesKey> updatedResourceKeys = new ArrayMap<>();
            int implCount = this.mResourceImpls.size();
            for (int i = 0; i < implCount; i++) {
                ResourcesKey key = this.mResourceImpls.keyAt(i);
                WeakReference<ResourcesImpl> weakImplRef = this.mResourceImpls.valueAt(i);
                ResourcesImpl impl = weakImplRef != null ? (ResourcesImpl) weakImplRef.get() : null;
                if (impl != null) {
                    if (key.mResDir != null) {
                        try {
                            if (key.mResDir.equals(baseCodePath)) {
                            }
                        } catch (Throwable th) {
                            th = th;
                            Trace.traceEnd(8192);
                            throw th;
                        }
                    } else {
                        String str = baseCodePath;
                    }
                    ResourcesKey resourcesKey = new ResourcesKey(key.mResDir, key.mSplitResDirs, newResourceDirs, key.mLibDirs, key.mDisplayId, key.mOverrideConfiguration, key.mCompatInfo);
                    updatedResourceKeys.put(impl, resourcesKey);
                } else {
                    String str2 = baseCodePath;
                }
            }
            String str3 = baseCodePath;
            redirectResourcesToNewImplLocked(updatedResourceKeys);
            Trace.traceEnd(8192);
        } catch (Throwable th2) {
            th = th2;
            String str4 = baseCodePath;
            Trace.traceEnd(8192);
            throw th;
        }
    }

    private void redirectResourcesToNewImplLocked(ArrayMap<ResourcesImpl, ResourcesKey> updatedResourceKeys) {
        if (!updatedResourceKeys.isEmpty()) {
            int resourcesCount = this.mResourceReferences.size();
            int i = 0;
            while (true) {
                Resources r = null;
                if (i < resourcesCount) {
                    WeakReference<Resources> ref = this.mResourceReferences.get(i);
                    if (ref != null) {
                        r = (Resources) ref.get();
                    }
                    if (r != null) {
                        ResourcesKey key = updatedResourceKeys.get(r.getImpl());
                        if (key != null) {
                            ResourcesImpl impl = findOrCreateResourcesImplForKeyLocked(key);
                            if (impl != null) {
                                r.setImpl(impl);
                            } else {
                                throw new Resources.NotFoundException("failed to redirect ResourcesImpl");
                            }
                        } else {
                            continue;
                        }
                    }
                    i++;
                } else {
                    for (ActivityResources activityResources : this.mActivityResourceReferences.values()) {
                        int resCount = activityResources.activityResources.size();
                        int i2 = 0;
                        while (true) {
                            if (i2 < resCount) {
                                WeakReference<Resources> ref2 = activityResources.activityResources.get(i2);
                                Resources r2 = ref2 != null ? (Resources) ref2.get() : null;
                                if (r2 != null) {
                                    ResourcesKey key2 = updatedResourceKeys.get(r2.getImpl());
                                    if (key2 != null) {
                                        ResourcesImpl impl2 = findOrCreateResourcesImplForKeyLocked(key2);
                                        if (impl2 != null) {
                                            r2.setImpl(impl2);
                                        } else {
                                            throw new Resources.NotFoundException("failed to redirect ResourcesImpl");
                                        }
                                    }
                                }
                                i2++;
                            }
                        }
                    }
                    return;
                }
            }
        }
    }
}
