package android.app;

import android.content.res.AssetManager;
import android.content.res.CompatibilityInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.ResourcesImpl;
import android.content.res.ResourcesKey;
import android.hardware.display.DisplayManagerGlobal;
import android.hwtheme.HwThemeManager;
import android.net.wifi.wifipro.NetworkHistoryUtils;
import android.os.IBinder;
import android.os.Trace;
import android.rog.AppRogInfo;
import android.util.ArrayMap;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.util.Slog;
import android.view.Display;
import android.view.DisplayAdjustments;
import com.android.internal.util.ArrayUtils;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.function.Predicate;

public class ResourcesManager {
    private static final boolean DEBUG = false;
    static final String TAG = "ResourcesManager";
    private static final Predicate<WeakReference<Resources>> sEmptyReferencePredicate = null;
    private static ResourcesManager sResourcesManager;
    private final WeakHashMap<IBinder, ActivityResources> mActivityResourceReferences;
    private final ArrayMap<Pair<Integer, DisplayAdjustments>, WeakReference<Display>> mDisplays;
    private CompatibilityInfo mResCompatibilityInfo;
    private final Configuration mResConfiguration;
    private final ArrayMap<ResourcesKey, WeakReference<ResourcesImpl>> mResourceImpls;
    private final ArrayList<WeakReference<Resources>> mResourceReferences;
    private boolean mRogEnable;
    private AppRogInfo mRogInfo;

    private static class ActivityResources {
        public final ArrayList<WeakReference<Resources>> activityResources;
        public final Configuration overrideConfig;

        private ActivityResources() {
            this.overrideConfig = new Configuration();
            this.activityResources = new ArrayList();
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.app.ResourcesManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.app.ResourcesManager.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.ResourcesManager.<clinit>():void");
    }

    public ResourcesManager() {
        this.mResConfiguration = new Configuration();
        this.mResourceImpls = new ArrayMap();
        this.mResourceReferences = new ArrayList();
        this.mActivityResourceReferences = new WeakHashMap();
        this.mDisplays = new ArrayMap();
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void invalidatePath(String path) {
        synchronized (this) {
            int count = 0;
            int i = 0;
            while (true) {
                if (i >= this.mResourceImpls.size()) {
                    Log.i(TAG, "Invalidated " + count + " asset managers that referenced " + path);
                } else if (((ResourcesKey) this.mResourceImpls.keyAt(i)).isPathReferenced(path)) {
                    ResourcesImpl res = (ResourcesImpl) ((WeakReference) this.mResourceImpls.removeAt(i)).get();
                    if (res != null) {
                        res.flushLayoutCache();
                    }
                    count++;
                } else {
                    i++;
                }
            }
        }
    }

    public Configuration getConfiguration() {
        Configuration configuration;
        synchronized (this) {
            configuration = this.mResConfiguration;
        }
        return configuration;
    }

    DisplayMetrics getDisplayMetrics() {
        return getDisplayMetrics(0, DisplayAdjustments.DEFAULT_DISPLAY_ADJUSTMENTS);
    }

    protected DisplayMetrics getDisplayMetrics(int displayId, DisplayAdjustments da) {
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
            return DEBUG;
        }
        this.mResCompatibilityInfo.applyToConfiguration(displayDensity, compatConfiguration);
        this.mResCompatibilityInfo.applyToConfigurationExt(null, displayDensity, compatConfiguration);
        return true;
    }

    public Display getAdjustedDisplay(int displayId, DisplayAdjustments displayAdjustments) {
        DisplayAdjustments displayAdjustmentsCopy;
        if (displayAdjustments != null) {
            displayAdjustmentsCopy = new DisplayAdjustments(displayAdjustments);
        } else {
            displayAdjustmentsCopy = new DisplayAdjustments();
        }
        Pair<Integer, DisplayAdjustments> key = Pair.create(Integer.valueOf(displayId), displayAdjustmentsCopy);
        AppRogInfo rogInfo = this.mRogInfo;
        boolean rogEnable = this.mRogEnable;
        synchronized (this) {
            Display display;
            WeakReference<Display> wd = (WeakReference) this.mDisplays.get(key);
            if (wd != null) {
                display = (Display) wd.get();
                if (display != null) {
                    return display;
                }
            }
            DisplayManagerGlobal dm = DisplayManagerGlobal.getInstance();
            if (dm == null) {
                return null;
            }
            display = dm.getCompatibleDisplay(displayId, (DisplayAdjustments) key.second);
            if (display != null) {
                display.setRogInfo(rogInfo, rogEnable);
                this.mDisplays.put(key, new WeakReference(display));
            }
            return display;
        }
    }

    protected AssetManager createAssetManager(ResourcesKey key) {
        int i = 0;
        AssetManager assets = new AssetManager();
        if (key.mResDir == null || assets.addAssetPath(key.mResDir) != 0) {
            if (key.mSplitResDirs != null) {
                for (String splitResDir : key.mSplitResDirs) {
                    if (assets.addAssetPath(splitResDir) == 0) {
                        throw new NotFoundException("failed to add split asset path " + splitResDir);
                    }
                }
            }
            if (key.mOverlayDirs != null) {
                for (String idmapPath : key.mOverlayDirs) {
                    assets.addOverlayPath(idmapPath);
                }
            }
            if (key.mLibDirs != null) {
                String[] strArr = key.mLibDirs;
                int length = strArr.length;
                while (i < length) {
                    String libDir = strArr[i];
                    if (libDir.endsWith(".apk") && assets.addAssetPathAsSharedLibrary(libDir) == 0) {
                        Log.w(TAG, "Asset path '" + libDir + "' does not exist or contains no resources.");
                    }
                    i++;
                }
            }
            return assets;
        }
        throw new NotFoundException("failed to add asset path " + key.mResDir);
    }

    private Configuration generateConfig(ResourcesKey key, DisplayMetrics dm) {
        boolean isDefaultDisplay = key.mDisplayId == 0 ? true : DEBUG;
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
        DisplayMetrics dm = getDisplayMetrics(key.mDisplayId, daj);
        return new ResourcesImpl(assets, dm, generateConfig(key, dm), daj);
    }

    private ResourcesImpl findResourcesImplForKeyLocked(ResourcesKey key) {
        ResourcesImpl impl;
        WeakReference<ResourcesImpl> weakImplRef = (WeakReference) this.mResourceImpls.get(key);
        if (weakImplRef != null) {
            impl = (ResourcesImpl) weakImplRef.get();
        } else {
            impl = null;
        }
        if (impl == null || !impl.getAssets().isUpToDate()) {
            return null;
        }
        return impl;
    }

    private ResourcesImpl findOrCreateResourcesImplForKeyLocked(ResourcesKey key) {
        ResourcesImpl impl = findResourcesImplForKeyLocked(key);
        if (impl != null) {
            return impl;
        }
        impl = createResourcesImpl(key);
        this.mResourceImpls.put(key, new WeakReference(impl));
        return impl;
    }

    private ResourcesKey findKeyForResourceImplLocked(ResourcesImpl resourceImpl) {
        int refCount = this.mResourceImpls.size();
        for (int i = 0; i < refCount; i++) {
            ResourcesImpl impl;
            WeakReference<ResourcesImpl> weakImplRef = (WeakReference) this.mResourceImpls.valueAt(i);
            if (weakImplRef != null) {
                impl = (ResourcesImpl) weakImplRef.get();
            } else {
                impl = null;
            }
            if (impl != null && resourceImpl == impl) {
                return (ResourcesKey) this.mResourceImpls.keyAt(i);
            }
        }
        return null;
    }

    private ActivityResources getOrCreateActivityResourcesStructLocked(IBinder activityToken) {
        ActivityResources activityResources = (ActivityResources) this.mActivityResourceReferences.get(activityToken);
        if (activityResources != null) {
            return activityResources;
        }
        activityResources = new ActivityResources();
        this.mActivityResourceReferences.put(activityToken, activityResources);
        return activityResources;
    }

    private Resources getOrCreateResourcesForActivityLocked(IBinder activityToken, ClassLoader classLoader, ResourcesImpl impl) {
        Resources resources;
        ActivityResources activityResources = getOrCreateActivityResourcesStructLocked(activityToken);
        int refCount = activityResources.activityResources.size();
        for (int i = 0; i < refCount; i++) {
            resources = (Resources) ((WeakReference) activityResources.activityResources.get(i)).get();
            if (resources != null && Objects.equals(resources.getClassLoader(), classLoader) && resources.getImpl() == impl) {
                return resources;
            }
        }
        resources = HwThemeManager.getResources(classLoader);
        if (resources != null) {
            resources.setImpl(impl);
        }
        activityResources.activityResources.add(new WeakReference(resources));
        return resources;
    }

    private Resources getOrCreateResourcesLocked(ClassLoader classLoader, ResourcesImpl impl) {
        Resources resources;
        int refCount = this.mResourceReferences.size();
        for (int i = 0; i < refCount; i++) {
            resources = (Resources) ((WeakReference) this.mResourceReferences.get(i)).get();
            if (resources != null && Objects.equals(resources.getClassLoader(), classLoader) && resources.getImpl() == impl) {
                return resources;
            }
        }
        resources = HwThemeManager.getResources(classLoader);
        if (resources != null) {
            resources.setImpl(impl);
        }
        this.mResourceReferences.add(new WeakReference(resources));
        return resources;
    }

    public Resources createBaseActivityResources(IBinder activityToken, String resDir, String[] splitResDirs, String[] overlayDirs, String[] libDirs, int displayId, Configuration overrideConfig, CompatibilityInfo compatInfo, ClassLoader classLoader) {
        try {
            Configuration configuration;
            Trace.traceBegin(Trace.TRACE_TAG_RESOURCES, "ResourcesManager#createBaseActivityResources");
            if (overrideConfig != null) {
                configuration = new Configuration(overrideConfig);
            } else {
                configuration = null;
            }
            ResourcesKey key = new ResourcesKey(resDir, splitResDirs, overlayDirs, libDirs, displayId, configuration, compatInfo);
            if (classLoader == null) {
                classLoader = ClassLoader.getSystemClassLoader();
            }
            synchronized (this) {
                getOrCreateActivityResourcesStructLocked(activityToken);
            }
            updateResourcesForActivity(activityToken, overrideConfig);
            Resources orCreateResources = getOrCreateResources(activityToken, key, classLoader);
            return orCreateResources;
        } finally {
            Trace.traceEnd(Trace.TRACE_TAG_RESOURCES);
        }
    }

    private Resources getOrCreateResources(IBinder activityToken, ResourcesKey key, ClassLoader classLoader) {
        synchronized (this) {
            ResourcesImpl resourcesImpl;
            ResourcesImpl existingResourcesImpl;
            Resources resources;
            if (activityToken != null) {
                ActivityResources activityResources = getOrCreateActivityResourcesStructLocked(activityToken);
                ArrayUtils.unstableRemoveIf(activityResources.activityResources, sEmptyReferencePredicate);
                if (key.hasOverrideConfiguration() && !activityResources.overrideConfig.equals(Configuration.EMPTY)) {
                    Configuration temp = new Configuration(activityResources.overrideConfig);
                    temp.updateFrom(key.mOverrideConfiguration);
                    key.mOverrideConfiguration.setTo(temp);
                }
                resourcesImpl = findResourcesImplForKeyLocked(key);
                if (resourcesImpl != null) {
                    Resources orCreateResourcesForActivityLocked = getOrCreateResourcesForActivityLocked(activityToken, classLoader, resourcesImpl);
                    return orCreateResourcesForActivityLocked;
                }
                resourcesImpl = createResourcesImpl(key);
                synchronized (this) {
                    existingResourcesImpl = findResourcesImplForKeyLocked(key);
                    if (existingResourcesImpl == null) {
                        if (!(resourcesImpl == null || resourcesImpl.getAssets() == null)) {
                            resourcesImpl.getAssets().close();
                        }
                        resourcesImpl = existingResourcesImpl;
                    } else {
                        this.mResourceImpls.put(key, new WeakReference(resourcesImpl));
                    }
                    if (activityToken == null) {
                        resources = getOrCreateResourcesForActivityLocked(activityToken, classLoader, resourcesImpl);
                    } else {
                        resources = getOrCreateResourcesLocked(classLoader, resourcesImpl);
                    }
                }
                return resources;
            }
            ArrayUtils.unstableRemoveIf(this.mResourceReferences, sEmptyReferencePredicate);
            resourcesImpl = findResourcesImplForKeyLocked(key);
            if (resourcesImpl != null) {
                orCreateResourcesForActivityLocked = getOrCreateResourcesLocked(classLoader, resourcesImpl);
                return orCreateResourcesForActivityLocked;
            }
            resourcesImpl = createResourcesImpl(key);
            synchronized (this) {
                existingResourcesImpl = findResourcesImplForKeyLocked(key);
                if (existingResourcesImpl == null) {
                    this.mResourceImpls.put(key, new WeakReference(resourcesImpl));
                } else {
                    resourcesImpl.getAssets().close();
                    resourcesImpl = existingResourcesImpl;
                }
                if (activityToken == null) {
                    resources = getOrCreateResourcesLocked(classLoader, resourcesImpl);
                } else {
                    resources = getOrCreateResourcesForActivityLocked(activityToken, classLoader, resourcesImpl);
                }
            }
            return resources;
        }
    }

    public Resources getResources(IBinder activityToken, String resDir, String[] splitResDirs, String[] overlayDirs, String[] libDirs, int displayId, Configuration overrideConfig, CompatibilityInfo compatInfo, ClassLoader classLoader) {
        try {
            Configuration configuration;
            Trace.traceBegin(Trace.TRACE_TAG_RESOURCES, "ResourcesManager#getResources");
            if (overrideConfig != null) {
                configuration = new Configuration(overrideConfig);
            } else {
                configuration = null;
            }
            ResourcesKey key = new ResourcesKey(resDir, splitResDirs, overlayDirs, libDirs, displayId, configuration, compatInfo);
            if (classLoader == null) {
                classLoader = ClassLoader.getSystemClassLoader();
            }
            Resources orCreateResources = getOrCreateResources(activityToken, key, classLoader);
            return orCreateResources;
        } finally {
            Trace.traceEnd(Trace.TRACE_TAG_RESOURCES);
        }
    }

    public void updateResourcesForActivity(IBinder activityToken, Configuration overrideConfig) {
        try {
            Trace.traceBegin(Trace.TRACE_TAG_RESOURCES, "ResourcesManager#updateResourcesForActivity");
            synchronized (this) {
                ActivityResources activityResources = getOrCreateActivityResourcesStructLocked(activityToken);
                if (Objects.equals(activityResources.overrideConfig, overrideConfig)) {
                    return;
                }
                Configuration oldConfig = new Configuration(activityResources.overrideConfig);
                if (overrideConfig != null) {
                    activityResources.overrideConfig.setTo(overrideConfig);
                } else {
                    activityResources.overrideConfig.setToDefaults();
                }
                boolean activityHasOverrideConfig = activityResources.overrideConfig.equals(Configuration.EMPTY) ? DEBUG : true;
                int refCount = activityResources.activityResources.size();
                for (int i = 0; i < refCount; i++) {
                    Resources resources = (Resources) ((WeakReference) activityResources.activityResources.get(i)).get();
                    if (resources != null) {
                        ResourcesKey oldKey = findKeyForResourceImplLocked(resources.getImpl());
                        if (oldKey == null) {
                            Slog.e(TAG, "can't find ResourcesKey for resources impl=" + resources.getImpl());
                        } else {
                            Configuration rebasedOverrideConfig = new Configuration();
                            if (overrideConfig != null) {
                                rebasedOverrideConfig.setTo(overrideConfig);
                            }
                            if (activityHasOverrideConfig && oldKey.hasOverrideConfiguration()) {
                                rebasedOverrideConfig.updateFrom(Configuration.generateDelta(oldConfig, oldKey.mOverrideConfiguration));
                            }
                            ResourcesKey newKey = new ResourcesKey(oldKey.mResDir, oldKey.mSplitResDirs, oldKey.mOverlayDirs, oldKey.mLibDirs, oldKey.mDisplayId, rebasedOverrideConfig, oldKey.mCompatInfo);
                            ResourcesImpl resourcesImpl = findResourcesImplForKeyLocked(newKey);
                            if (resourcesImpl == null) {
                                resourcesImpl = createResourcesImpl(newKey);
                                this.mResourceImpls.put(newKey, new WeakReference(resourcesImpl));
                            }
                            if (resourcesImpl != resources.getImpl()) {
                                resources.setImpl(resourcesImpl);
                            }
                        }
                    }
                }
                Trace.traceEnd(Trace.TRACE_TAG_RESOURCES);
            }
        } finally {
            Trace.traceEnd(Trace.TRACE_TAG_RESOURCES);
        }
    }

    public final boolean applyConfigurationToResourcesLocked(Configuration config, CompatibilityInfo compat) {
        try {
            Trace.traceBegin(Trace.TRACE_TAG_RESOURCES, "ResourcesManager#applyConfigurationToResourcesLocked");
            if (!this.mResConfiguration.isOtherSeqNewer(config) && compat == null) {
                return DEBUG;
            }
            int changes = this.mResConfiguration.updateFrom(config);
            this.mDisplays.clear();
            DisplayMetrics defaultDisplayMetrics = getDisplayMetrics();
            if (compat != null && (this.mResCompatibilityInfo == null || !this.mResCompatibilityInfo.equals(compat))) {
                this.mResCompatibilityInfo = compat;
                changes |= 3328;
            }
            Resources.updateSystemConfiguration(config, defaultDisplayMetrics, compat);
            ApplicationPackageManager.configurationChanged();
            Configuration tmpConfig = null;
            for (int i = this.mResourceImpls.size() - 1; i >= 0; i--) {
                ResourcesKey key = (ResourcesKey) this.mResourceImpls.keyAt(i);
                ResourcesImpl r = (ResourcesImpl) ((WeakReference) this.mResourceImpls.valueAt(i)).get();
                if (r != null) {
                    int displayId = key.mDisplayId;
                    boolean isDefaultDisplay = displayId == 0 ? true : DEBUG;
                    DisplayMetrics dm = defaultDisplayMetrics;
                    boolean hasOverrideConfiguration = key.hasOverrideConfiguration();
                    if (!isDefaultDisplay || hasOverrideConfiguration) {
                        if (tmpConfig == null) {
                            tmpConfig = new Configuration();
                        }
                        tmpConfig.setTo(config);
                        if (!isDefaultDisplay) {
                            DisplayAdjustments daj = r.getDisplayAdjustments();
                            if (compat != null) {
                                DisplayAdjustments daj2 = new DisplayAdjustments(daj);
                                daj2.setCompatibilityInfo(compat);
                                daj = daj2;
                            }
                            dm = getDisplayMetrics(displayId, daj);
                            applyNonDefaultDisplayMetricsToConfiguration(dm, tmpConfig);
                        }
                        if (hasOverrideConfiguration) {
                            tmpConfig.updateFrom(key.mOverrideConfiguration);
                        }
                        r.updateConfiguration(tmpConfig, dm, compat);
                    } else {
                        r.updateConfiguration(config, defaultDisplayMetrics, compat);
                    }
                } else {
                    this.mResourceImpls.removeAt(i);
                }
            }
            boolean z = changes != 0 ? true : DEBUG;
            Trace.traceEnd(Trace.TRACE_TAG_RESOURCES);
            return z;
        } finally {
            Trace.traceEnd(Trace.TRACE_TAG_RESOURCES);
        }
    }

    public void appendLibAssetForMainAssetPath(String assetPath, String libAsset) {
        synchronized (this) {
            int i;
            ArrayMap<ResourcesImpl, ResourcesKey> updatedResourceKeys = new ArrayMap();
            int implCount = this.mResourceImpls.size();
            for (i = 0; i < implCount; i++) {
                ResourcesImpl impl = (ResourcesImpl) ((WeakReference) this.mResourceImpls.valueAt(i)).get();
                ResourcesKey key = (ResourcesKey) this.mResourceImpls.keyAt(i);
                if (!(impl == null || key.mResDir == null || !key.mResDir.equals(assetPath) || ArrayUtils.contains(key.mLibDirs, libAsset))) {
                    int newLibAssetCount = (key.mLibDirs != null ? key.mLibDirs.length : 0) + 1;
                    String[] newLibAssets = new String[newLibAssetCount];
                    if (key.mLibDirs != null) {
                        System.arraycopy(key.mLibDirs, 0, newLibAssets, 0, key.mLibDirs.length);
                    }
                    newLibAssets[newLibAssetCount - 1] = libAsset;
                    updatedResourceKeys.put(impl, new ResourcesKey(key.mResDir, key.mSplitResDirs, key.mOverlayDirs, newLibAssets, key.mDisplayId, key.mOverrideConfiguration, key.mCompatInfo));
                }
            }
            if (updatedResourceKeys.isEmpty()) {
                return;
            }
            int resourcesCount = this.mResourceReferences.size();
            for (i = 0; i < resourcesCount; i++) {
                Resources r = (Resources) ((WeakReference) this.mResourceReferences.get(i)).get();
                if (r != null) {
                    key = (ResourcesKey) updatedResourceKeys.get(r.getImpl());
                    if (key != null) {
                        r.setImpl(findOrCreateResourcesImplForKeyLocked(key));
                    }
                }
            }
            for (ActivityResources activityResources : this.mActivityResourceReferences.values()) {
                int resCount = activityResources.activityResources.size();
                for (i = 0; i < resCount; i++) {
                    r = (Resources) ((WeakReference) activityResources.activityResources.get(i)).get();
                    if (r != null) {
                        key = (ResourcesKey) updatedResourceKeys.get(r.getImpl());
                        if (key != null) {
                            r.setImpl(findOrCreateResourcesImplForKeyLocked(key));
                        }
                    }
                }
            }
        }
    }

    public final void applyRogToResources(AppRogInfo rogInfo, boolean rogEnable) {
        int i;
        setRogInfo(rogInfo, rogEnable);
        ApplicationPackageManager.configurationChanged();
        DisplayMetrics metrics = getDisplayMetrics();
        if (rogInfo != null) {
            int i2;
            Configuration configuration = this.mResConfiguration;
            if (rogEnable) {
                i2 = (int) ((((float) metrics.noncompatDensityDpi) / rogInfo.mRogScale) + NetworkHistoryUtils.RECOVERY_PERCENTAGE);
            } else {
                i2 = metrics.noncompatDensityDpi;
            }
            configuration.densityDpi = i2;
        }
        for (i = this.mResourceImpls.size() - 1; i >= 0; i--) {
            ResourcesImpl r = (ResourcesImpl) ((WeakReference) this.mResourceImpls.valueAt(i)).get();
            if (r != null) {
                r.setRogInfo(rogInfo, rogEnable);
            }
        }
        int size = this.mDisplays.size();
        for (i = 0; i < size; i++) {
            Display display = (Display) ((WeakReference) this.mDisplays.valueAt(i)).get();
            if (display != null) {
                display.setRogInfo(rogInfo, rogEnable);
            }
        }
    }

    public void setRogInfo(AppRogInfo rogInfo, boolean rogEnable) {
        this.mRogInfo = rogInfo;
        this.mRogEnable = rogEnable;
    }

    public AppRogInfo getRogInfo() {
        return this.mRogInfo;
    }
}
