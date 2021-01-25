package ohos.global.resource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import ohos.global.config.ConfigManager;
import ohos.global.config.ConfigManagerImpl;
import ohos.global.configuration.Configuration;
import ohos.global.configuration.DeviceCapability;
import ohos.global.configuration.LocaleProfile;
import ohos.global.icu.text.PluralRules;
import ohos.global.icu.util.ULocale;
import ohos.global.innerkit.asset.Package;
import ohos.global.resource.solidxml.SolidXmllmpl;
import ohos.global.resource.solidxml.Theme;
import ohos.global.resource.solidxml.ThemeImpl;
import ohos.global.resource.solidxml.TypedAttributeImpl;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.media.camera.mode.adapter.utils.constant.ConstantValue;

public class ResourceManagerImpl extends ResourceManager {
    private static final int DEFAULT_DENSITY = 160;
    private static final String DENSITY_INDEPENDENT_PIXEL = "dp";
    private static final String[] DIMENSION_UNIT_STRS = {PIXEL_UNIT, DENSITY_INDEPENDENT_PIXEL, SCALE_INDEPENDENT_PIXEL};
    private static final String HARMONY_PATTERN_REFERENCE_PREFIX = "@ohos:theme/";
    private static final String[][] HARMONY_REFERENCE_PREFIX = {new String[]{"@ohos:bool/", "@ohos:color/", "@ohos:dimen/", "@ohos:integer/", "@ohos:string/", "@ohos:layout/", "@ohos:drawable/"}, new String[]{"$ohos:boolean:", "$ohos:color:", "$ohos:float:", "$ohos:integer:", "$ohos:string:", "$ohos:layout:", "$ohos:media:"}};
    private static final String HARMONY_THEME_REFERENCE_PREFIX = "@ohos:theme/";
    private static final int ID_MASK = -16777216;
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218111488, "ResourceManagerImpl");
    private static final Object LOCK = new Object();
    private static final int MAX_CACHE_SIZE = 10;
    private static final int MIN_SYSTEM_ID = 117440512;
    private static final String PATTERN_REFERENCE_PREFIX = "@theme/";
    private static final String PIXEL_UNIT = "px";
    private static final String[][] REFERENCE_PREFIX = {new String[]{"@string/", "@bool/", "@color/", "@dimen/", "@integer/", "@layout/", "@drawable/"}, new String[]{"$string:", "$boolean:", "$color:", "$float:", "$integer:", "$layout:", "$media:"}};
    private static final Pattern REGEX_UNIT = Pattern.compile("^(?<value>-?\\d+(\\.\\d+)?)(?<unit>px|dp|sp)?$");
    private static final String RESOURCE_INDEX_NAME = "resources.index";
    private static final String SCALE_INDEPENDENT_PIXEL = "sp";
    private static final String SYSTEM_MODULE_NAME = "entry";
    private static final String THEME_REFERENCE_PREFIX = "@theme/";
    private static long sysHandle;
    private static volatile boolean sysInit = false;
    private CacheMap<Integer, Object> cacheMap = new CacheMap<>(10);
    private ConfigManagerImpl configManager;
    private long handle;
    private PluralRules pluralRules;
    private DeviceCapability resCapability;
    private Configuration resConfig;
    private Package resPackage = new Package();
    private List<ResourcePath> resPathList = new ArrayList();
    private String unit = PIXEL_UNIT;

    private native boolean nativeAddIndex(long j, byte[] bArr);

    private static native String[] nativeFindValidAndSort(String str, String[] strArr);

    private native int nativeGetBestLocale(long j, String[] strArr, int[] iArr);

    private native Object nativeGetElement(long j, int i);

    private native String nativeGetIdentifier(long j, int i);

    private native long nativeGetResource(long j, int i);

    private native long nativeGetSolidXml();

    private native String nativeGetSolidXmlPath(long j, int i);

    private native String nativeGetString(long j, int i);

    private native Object[] nativeGetStringArray(long j, int i);

    private native void nativeRelease(long j);

    private native long nativeSetup(Locale locale, int i, int i2, int i3);

    private native void nativeUpdateConfig(long j, Locale locale, int i, int i2, int i3);

    static {
        System.loadLibrary("resourcemanager_jni.z");
    }

    public boolean init(Package r5, ResourcePath[] resourcePathArr, Configuration configuration, DeviceCapability deviceCapability) throws IOException {
        boolean z;
        HiLog.debug(LABEL, "enter Init", new Object[0]);
        if (resourcePathArr == null || resourcePathArr.length == 0 || r5 == null) {
            HiLog.error(LABEL, "ResourceManager Init, resource paths is null, init failed", new Object[0]);
            return false;
        }
        synchronized (LOCK) {
            this.resPackage = r5;
            this.handle = setupManager(configuration, deviceCapability);
            if (this.handle == 0) {
                HiLog.error(LABEL, "handle failed", new Object[0]);
                return false;
            }
            this.configManager = new ConfigManagerImpl();
            int i = 0;
            while (true) {
                z = true;
                if (i >= resourcePathArr.length) {
                    break;
                }
                if (addResourceInternal(resourcePathArr[i])) {
                    this.resPathList.add(resourcePathArr[i]);
                } else {
                    HiLog.error(LABEL, "nativeAddIndex failed, index = %{public}d", Integer.valueOf(i));
                }
                i++;
            }
            computePreferedLocale(this.handle, false);
            initSystemResouce();
            this.cacheMap.clear();
            if (this.resPathList.size() <= 0) {
                z = false;
            }
            return z;
        }
    }

    public boolean initSys(Configuration configuration, DeviceCapability deviceCapability) throws IOException {
        HiLog.debug(LABEL, "enter InitSys", new Object[0]);
        synchronized (LOCK) {
            initConfigInner(configuration, deviceCapability);
            initSystemResouce();
            this.cacheMap.clear();
        }
        return true;
    }

    public List<ResourcePath> getResourcePath() {
        ArrayList arrayList = new ArrayList(this.resPathList.size());
        for (ResourcePath resourcePath : this.resPathList) {
            ResourcePath resourcePath2 = new ResourcePath();
            resourcePath2.setResourcePath(resourcePath.getResourcePath(), resourcePath.getAaName());
            arrayList.add(resourcePath2);
        }
        return arrayList;
    }

    public boolean addResourcePath(ResourcePath resourcePath) throws IOException {
        if (resourcePath == null || resourcePath.getAaName() == null) {
            HiLog.error(LABEL, "new Resource Path empty", new Object[0]);
            return false;
        }
        HiLog.debug(LABEL, "add new Resource Path %{pbulic}s", resourcePath.getAaName());
        String aaName = resourcePath.getAaName();
        for (ResourcePath resourcePath2 : this.resPathList) {
            if (aaName.equals(resourcePath2.getAaName())) {
                return true;
            }
        }
        boolean addResourceInternal = addResourceInternal(resourcePath);
        if (addResourceInternal) {
            this.resPathList.add(resourcePath);
        } else {
            HiLog.error(LABEL, "nativeAddIndex failed", new Object[0]);
        }
        return addResourceInternal;
    }

    @Override // ohos.global.resource.ResourceManager
    public ResourceImpl getResource(int i) throws NotExistException, IOException {
        long j = this.handle;
        if ((-16777216 & i) == 117440512) {
            j = sysHandle;
        }
        long nativeGetResource = nativeGetResource(j, i);
        if (nativeGetResource == 0) {
            return null;
        }
        return new ResourceImpl(this.resPackage, nativeGetResource, j == sysHandle);
    }

    @Override // ohos.global.resource.ResourceManager
    public String getIdentifier(int i) throws NotExistException, IOException {
        long j = this.handle;
        if ((-16777216 & i) == 117440512) {
            j = sysHandle;
        }
        return nativeGetIdentifier(j, i);
    }

    private String getString(int i) throws NotExistException, IOException, WrongTypeException {
        long j = this.handle;
        if ((-16777216 & i) == 117440512) {
            j = sysHandle;
        }
        return tryDereferrence(nativeGetString(j, i));
    }

    @Override // ohos.global.resource.ResourceManager
    public Theme getTheme(int i) throws NotExistException, IOException, WrongTypeException {
        String[] stringArray = getElement(i).getStringArray();
        Stack stack = new Stack();
        if (stringArray == null || stringArray.length == 0) {
            return new ThemeImpl(new HashMap());
        }
        stack.push(stringArray);
        while (true) {
            String[] strArr = (String[]) stack.peek();
            if (strArr == null || strArr.length <= 0) {
                break;
            }
            String str = "@ohos:theme/";
            if (!(strArr[0].startsWith("@theme/") || strArr[0].startsWith(str))) {
                break;
            }
            if (strArr[0].startsWith("@theme/")) {
                str = "@theme/";
            }
            try {
                String[] stringArray2 = getElement(Integer.parseInt(strArr[0].substring(str.length()))).getStringArray();
                if (stringArray2 != null) {
                    if (stringArray2.length == 0) {
                        break;
                    }
                    stack.push(stringArray2);
                    if (stack.size() <= 0) {
                        break;
                    }
                } else {
                    break;
                }
            } catch (NumberFormatException unused) {
                HiLog.error(LABEL, "Theme reference invalid", new Object[0]);
            }
        }
        HashMap hashMap = new HashMap();
        do {
            String[] strArr2 = (String[]) stack.pop();
            for (int i2 = (strArr2.length <= 0 || !strArr2[0].startsWith("@theme/")) ? 0 : 1; i2 < strArr2.length - 1; i2 += 2) {
                hashMap.put(strArr2[i2], new TypedAttributeImpl(this, strArr2[i2], strArr2[i2 + 1]));
            }
        } while (stack.size() != 0);
        return new ThemeImpl(hashMap);
    }

    @Override // ohos.global.resource.ResourceManager
    public String getMediaPath(int i) throws NotExistException, IOException, WrongTypeException {
        return getString(i);
    }

    @Override // ohos.global.resource.ResourceManager
    public SolidXmllmpl getSolidXml(int i) throws NotExistException, IOException, WrongTypeException {
        if (this.cacheMap.containsKey(Integer.valueOf(i))) {
            Object obj = this.cacheMap.get(Integer.valueOf(i));
            if (obj instanceof SolidXmllmpl) {
                return (SolidXmllmpl) obj;
            }
        }
        String string = getString(i);
        if (string != null) {
            if ((-16777216 & i) == 117440512) {
                string = Package.SYS_RESOURCE_PREFIX + string;
            }
            SolidXmllmpl solidXmllmpl = new SolidXmllmpl(this, this.resPackage, string);
            this.cacheMap.put(Integer.valueOf(i), solidXmllmpl);
            return solidXmllmpl;
        }
        throw new NotExistException("the resId is not exsit, resId is " + String.valueOf(i));
    }

    private boolean hasExtension(String str) {
        int lastIndexOf = str.lastIndexOf(47);
        if (lastIndexOf != -1) {
            str = str.substring(lastIndexOf + 1);
        }
        if (str.lastIndexOf(46) != -1) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0085, code lost:
        r5 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0086, code lost:
        $closeResource(r1, r4);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0089, code lost:
        throw r5;
     */
    @Override // ohos.global.resource.ResourceManager
    public RawFileEntry getRawFileEntry(String str) {
        if (str == null) {
            return new RawFileEntryImpl(this.resPackage, str);
        }
        ArrayList<String> arrayList = new ArrayList(this.resPathList.size() + 1);
        arrayList.add(str);
        for (ResourcePath resourcePath : this.resPathList) {
            String aaName = resourcePath.getAaName();
            if (aaName != null && aaName.length() > 0) {
                arrayList.add(aaName + File.separator + str);
            }
        }
        if (hasExtension(str)) {
            for (String str2 : arrayList) {
                try {
                    InputStream open = this.resPackage.open(str2);
                    if (open != null) {
                        RawFileEntryImpl rawFileEntryImpl = new RawFileEntryImpl(this.resPackage, str2);
                        $closeResource(null, open);
                        return rawFileEntryImpl;
                    } else if (open != null) {
                        $closeResource(null, open);
                    }
                } catch (IOException unused) {
                    HiLog.error(LABEL, "getRawFileEntry failed, try next moduler", new Object[0]);
                }
            }
        } else {
            for (String str3 : arrayList) {
                try {
                    if (this.resPackage.list(str3).length > 0) {
                        return new RawFileEntryImpl(this.resPackage, str3);
                    }
                } catch (IOException unused2) {
                    HiLog.error(LABEL, "getRawFileEntry failed, try next moduler", new Object[0]);
                }
            }
        }
        return new RawFileEntryImpl(this.resPackage, str);
    }

    private static /* synthetic */ void $closeResource(Throwable th, AutoCloseable autoCloseable) {
        if (th != null) {
            try {
                autoCloseable.close();
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
        } else {
            autoCloseable.close();
        }
    }

    @Override // ohos.global.resource.ResourceManager
    public ConfigManager getConfigManager() {
        ConfigManagerImpl configManagerImpl = this.configManager;
        if (configManagerImpl != null) {
            return configManagerImpl;
        }
        HiLog.error(LABEL, "configManager is null, pls call the function of init", new Object[0]);
        return null;
    }

    @Override // ohos.global.resource.ResourceManager
    public Configuration getConfiguration() {
        Configuration configuration;
        synchronized (LOCK) {
            configuration = new Configuration(this.resConfig);
        }
        return configuration;
    }

    @Override // ohos.global.resource.ResourceManager
    public DeviceCapability getDeviceCapability() {
        DeviceCapability deviceCapability;
        synchronized (LOCK) {
            deviceCapability = new DeviceCapability(this.resCapability);
        }
        return deviceCapability;
    }

    @Override // ohos.global.resource.ResourceManager
    public void updateConfiguration(Configuration configuration, DeviceCapability deviceCapability) {
        synchronized (LOCK) {
            initConfigInner(configuration, deviceCapability);
            computePreferedLocale(this.handle, false);
            computePreferedLocale(sysHandle, true);
            this.pluralRules = PluralRules.forLocale(this.resConfig.getFirstLocale());
        }
    }

    public void release() {
        nativeRelease(this.handle);
        this.handle = 0;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:30:0x007b, code lost:
        r8 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x007c, code lost:
        if (r6 != null) goto L_0x007e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x007e, code lost:
        $closeResource(r7, r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0081, code lost:
        throw r8;
     */
    private boolean addResourceInternal(ResourcePath resourcePath) throws IOException {
        String aaName = resourcePath.getAaName();
        if (aaName == null) {
            aaName = "";
        }
        String[] list = this.resPackage.list(aaName);
        if (list == null) {
            return false;
        }
        int i = 0;
        for (String str : list) {
            String str2 = RESOURCE_INDEX_NAME;
            if (str.endsWith(str2)) {
                if (!aaName.isEmpty()) {
                    str2 = resourcePath.getAaName() + "/" + str;
                }
                try {
                    InputStream open = this.resPackage.open(str2);
                    if (open == null) {
                        HiLog.error(LABEL, "pkg open resources fail", new Object[0]);
                        if (open == null) {
                        }
                    } else {
                        byte[] bArr = new byte[open.available()];
                        if (open.read(bArr) == -1) {
                            HiLog.error(LABEL, "read from IO reach the end", new Object[0]);
                        }
                        if (nativeAddIndex(this.handle, bArr)) {
                            i++;
                        }
                    }
                    $closeResource(null, open);
                } catch (FileNotFoundException unused) {
                    HiLog.error(LABEL, "load index failed.", new Object[0]);
                }
            }
        }
        if (i > 0) {
            return true;
        }
        return false;
    }

    public float parseFloat(String str) throws WrongTypeException {
        int i;
        Matcher matcher = REGEX_UNIT.matcher(str);
        if (matcher.find()) {
            try {
                String group = matcher.group("unit");
                float parseFloat = Float.parseFloat(matcher.group("value"));
                if (this.resCapability.screenDensity / DEFAULT_DENSITY > 0) {
                    i = this.resCapability.screenDensity / DEFAULT_DENSITY;
                } else {
                    i = ResourceUtils.getDensity();
                }
                if (DENSITY_INDEPENDENT_PIXEL.equals(group)) {
                    return parseFloat * ((float) i);
                }
                if (!SCALE_INDEPENDENT_PIXEL.equals(group)) {
                    return parseFloat;
                }
                return parseFloat * ((float) i) * (this.resConfig.fontRatio != ConstantValue.MIN_ZOOM_VALUE ? this.resConfig.fontRatio : 1.0f);
            } catch (IllegalArgumentException | IllegalStateException unused) {
                throw new WrongTypeException("float format not correct.");
            }
        } else {
            throw new WrongTypeException("float not match the regex pattern.");
        }
    }

    /* access modifiers changed from: package-private */
    public String tryDereferrence1(String str) throws WrongTypeException {
        String substring;
        if (!(str == null || str.length() == 0 || !(str.startsWith("@") || str.startsWith("$")))) {
            int length = REFERENCE_PREFIX[0].length;
            for (int i = 0; i < length; i++) {
                if (str.startsWith(REFERENCE_PREFIX[0][i])) {
                    substring = str.substring(REFERENCE_PREFIX[0][i].length());
                } else if (str.startsWith(REFERENCE_PREFIX[1][i])) {
                    substring = str.substring(REFERENCE_PREFIX[1][i].length());
                } else if (str.startsWith(HARMONY_REFERENCE_PREFIX[0][i])) {
                    substring = str.substring(HARMONY_REFERENCE_PREFIX[0][i].length());
                } else if (str.startsWith(HARMONY_REFERENCE_PREFIX[1][i])) {
                    substring = str.substring(HARMONY_REFERENCE_PREFIX[1][i].length());
                }
                try {
                    Integer valueOf = Integer.valueOf(Integer.parseInt(substring));
                    Object nativeGetElement = nativeGetElement((valueOf.intValue() & -16777216) == 117440512 ? sysHandle : this.handle, valueOf.intValue());
                    if (nativeGetElement instanceof String) {
                        return (String) nativeGetElement;
                    }
                    throw new WrongTypeException("dereferrence error");
                } catch (NumberFormatException unused) {
                    HiLog.error(LABEL, "dereferrence format error", new Object[0]);
                }
            }
        }
        return str;
    }

    private String tryDereferrence(String str) {
        String substring;
        if (!(str == null || str.length() == 0 || !(str.startsWith("@") || str.startsWith("$")))) {
            int length = REFERENCE_PREFIX[0].length;
            for (int i = 0; i < length; i++) {
                if (str.startsWith(REFERENCE_PREFIX[0][i])) {
                    substring = str.substring(REFERENCE_PREFIX[0][i].length());
                } else if (str.startsWith(REFERENCE_PREFIX[1][i])) {
                    substring = str.substring(REFERENCE_PREFIX[1][i].length());
                } else if (str.startsWith(HARMONY_REFERENCE_PREFIX[0][i])) {
                    substring = str.substring(HARMONY_REFERENCE_PREFIX[0][i].length());
                } else if (str.startsWith(HARMONY_REFERENCE_PREFIX[1][i])) {
                    substring = str.substring(HARMONY_REFERENCE_PREFIX[1][i].length());
                }
                try {
                    Integer valueOf = Integer.valueOf(Integer.parseInt(substring));
                    return nativeGetString((valueOf.intValue() & -16777216) == 117440512 ? sysHandle : this.handle, valueOf.intValue());
                } catch (NumberFormatException unused) {
                    HiLog.error(LABEL, "dereferrence format error", new Object[0]);
                }
            }
        }
        return str;
    }

    private float dereferenceDimen(String str) {
        int length = DIMENSION_UNIT_STRS.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                break;
            } else if (str.endsWith(DIMENSION_UNIT_STRS[i])) {
                this.unit = DIMENSION_UNIT_STRS[i];
                break;
            } else {
                i++;
            }
        }
        String substring = str.substring(0, str.length() - this.unit.length());
        this.unit = str.substring(str.length() - this.unit.length());
        try {
            return Float.parseFloat(substring);
        } catch (NumberFormatException unused) {
            HiLog.error(LABEL, "dimenValue format error", new Object[0]);
            return ConstantValue.MIN_ZOOM_VALUE;
        }
    }

    private void initConfigInner(Configuration configuration, DeviceCapability deviceCapability) {
        if (configuration == null) {
            HiLog.warn(LABEL, "ResourceManager Init, config is null, use default config", new Object[0]);
            this.resConfig = new Configuration();
            this.resConfig.setLocaleProfile(new LocaleProfile(new Locale[]{Locale.getDefault()}));
        } else {
            this.resConfig = new Configuration(configuration);
            if (configuration.getFirstLocale() == null) {
                this.resConfig.setLocaleProfile(new LocaleProfile(new Locale[]{Locale.getDefault()}));
            }
        }
        if (deviceCapability == null) {
            this.resCapability = new DeviceCapability();
        } else {
            this.resCapability = new DeviceCapability(deviceCapability);
        }
    }

    private long setupManager(Configuration configuration, DeviceCapability deviceCapability) {
        initConfigInner(configuration, deviceCapability);
        return nativeSetup(this.resConfig.getFirstLocale(), this.resConfig.direction, this.resCapability.screenDensity, this.resCapability.deviceType);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0087, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0088, code lost:
        if (r5 != null) goto L_0x008a;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x008a, code lost:
        $closeResource(r6, r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x008d, code lost:
        throw r7;
     */
    private void initSystemResouce() {
        if (!sysInit) {
            synchronized (LOCK) {
                if (!sysInit) {
                    this.resPackage.loadSystemResource("/system/framework/SystemResources.hap");
                    sysHandle = setupManager(this.resConfig, this.resCapability);
                    if (sysHandle == 0) {
                        HiLog.error(LABEL, "sysHandle failed", new Object[0]);
                        return;
                    }
                    for (String str : new String[]{"ohos:resources.index", "ohos:entry/resources.index"}) {
                        try {
                            InputStream open = this.resPackage.open(str);
                            if (open == null) {
                                HiLog.error(LABEL, "sys pkg open resources fail", new Object[0]);
                                if (open != null) {
                                    $closeResource(null, open);
                                }
                                return;
                            }
                            byte[] bArr = new byte[open.available()];
                            if (open.read(bArr) == -1) {
                                HiLog.error(LABEL, "sys read from IO reach the end", new Object[0]);
                            }
                            if (!nativeAddIndex(sysHandle, bArr)) {
                                HiLog.error(LABEL, "sys nativeAddIndex failed", new Object[0]);
                            }
                            $closeResource(null, open);
                        } catch (IOException unused) {
                            HiLog.error(LABEL, "read system resource index failed", new Object[0]);
                        }
                    }
                    computePreferedLocale(sysHandle, true);
                    sysInit = true;
                }
            }
        }
    }

    @Override // ohos.global.resource.ResourceManager
    public Element getElement(int i) throws NotExistException, IOException, WrongTypeException {
        long j = this.handle;
        if ((-16777216 & i) == 117440512) {
            j = sysHandle;
        }
        return new ElementImpl(nativeGetElement(j, i), this);
    }

    public static ArrayList<String> findValidAndSort(String str, List<String> list) throws LocaleFallBackException {
        if (list == null || str == null) {
            return new ArrayList<>();
        }
        String[] nativeFindValidAndSort = nativeFindValidAndSort(str, (String[]) list.toArray(new String[0]));
        ArrayList<String> arrayList = new ArrayList<>();
        for (String str2 : nativeFindValidAndSort) {
            arrayList.add(str2);
        }
        return arrayList;
    }

    public Object getAAssetManager() {
        return this.resPackage.getAAssetManager();
    }

    private void computePreferedLocale(long j, boolean z) {
        Locale[] locales = this.resConfig.getLocaleProfile().getLocales();
        int length = locales.length;
        if (length == 0) {
            locales = new Locale[]{Locale.getDefault()};
            length = 1;
        }
        String[] strArr = new String[length];
        for (int i = 0; i < length; i++) {
            strArr[i] = locales[i].toLanguageTag();
        }
        int nativeGetBestLocale = nativeGetBestLocale(j, strArr, new int[]{this.resConfig.direction, this.resCapability.screenDensity, this.resCapability.deviceType});
        if (!(z || nativeGetBestLocale == 0)) {
            Locale locale = locales[nativeGetBestLocale];
            while (nativeGetBestLocale > 0) {
                locales[nativeGetBestLocale] = locales[nativeGetBestLocale - 1];
                nativeGetBestLocale--;
            }
            locales[0] = locale;
            this.resConfig.setLocaleProfile(new LocaleProfile(locales));
            Locale.setDefault(locale);
            this.resConfig.isLayoutRTL = ULocale.forLocale(locale).isRightToLeft();
            nativeUpdateConfig(sysHandle, this.resConfig.getFirstLocale(), this.resConfig.direction, this.resCapability.screenDensity, this.resCapability.deviceType);
            nativeUpdateConfig(this.handle, this.resConfig.getFirstLocale(), this.resConfig.direction, this.resCapability.screenDensity, this.resCapability.deviceType);
        }
    }
}
