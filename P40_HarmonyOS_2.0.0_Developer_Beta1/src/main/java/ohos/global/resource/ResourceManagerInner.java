package ohos.global.resource;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import ohos.global.configuration.Configuration;
import ohos.global.configuration.DeviceCapability;
import ohos.global.innerkit.asset.Package;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class ResourceManagerInner {
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218111488, "ResourceManagerInner");
    private static final Object LOCK = new Object();
    private static volatile ResourceManagerImpl sysResManagerImpl;
    private ResourceManagerImpl resManagerImpl;

    public boolean init(Package r2, ResourcePath[] resourcePathArr, Configuration configuration, DeviceCapability deviceCapability) throws IOException {
        this.resManagerImpl = new ResourceManagerImpl();
        return this.resManagerImpl.init(r2, resourcePathArr, configuration, deviceCapability);
    }

    public static ResourceManager getSystemResourceManager() throws IOException {
        if (sysResManagerImpl == null) {
            synchronized (LOCK) {
                if (sysResManagerImpl == null) {
                    ResourceManagerImpl resourceManagerImpl = new ResourceManagerImpl();
                    resourceManagerImpl.initSys(new Configuration(), new DeviceCapability());
                    sysResManagerImpl = resourceManagerImpl;
                }
            }
        }
        return sysResManagerImpl;
    }

    public ResourceManager getResourceManager() {
        ResourceManagerImpl resourceManagerImpl = this.resManagerImpl;
        if (resourceManagerImpl != null) {
            return resourceManagerImpl;
        }
        HiLog.error(LABEL, "resManagerImpl is null", new Object[0]);
        return null;
    }

    public final void release() {
        this.resManagerImpl.release();
    }

    public List<ResourcePath> getResourcePath() {
        ResourceManagerImpl resourceManagerImpl = this.resManagerImpl;
        if (resourceManagerImpl == null) {
            return new ArrayList();
        }
        return resourceManagerImpl.getResourcePath();
    }

    public boolean addResourcePath(ResourcePath resourcePath) throws IOException {
        ResourceManagerImpl resourceManagerImpl = this.resManagerImpl;
        if (resourceManagerImpl == null) {
            return false;
        }
        return resourceManagerImpl.addResourcePath(resourcePath);
    }

    public static <T> int getAResId(int i, Class<?> cls, T t) {
        String str;
        Field[] fields = cls.getFields();
        int length = fields.length;
        int i2 = 0;
        while (true) {
            if (i2 >= length) {
                str = null;
                break;
            }
            Field field = fields[i2];
            if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers()) && "int".equals(field.getType().getName())) {
                try {
                    if (field.getInt(cls) == i) {
                        str = field.getName();
                        break;
                    }
                } catch (IllegalAccessException unused) {
                    continue;
                }
            }
            i2++;
        }
        if (str != null) {
            return getAResIdByType(str, cls, t);
        }
        HiLog.error(LABEL, "name of the resource is null", new Object[0]);
        return 0;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:46:0x00bc, code lost:
        if ("theme".equals(r7) != false) goto L_0x00c0;
     */
    private static <T> int getAResIdByType(String str, Class<?> cls, T t) {
        String str2;
        String str3 = "style";
        if ("ResourceTable".equals(cls.getSimpleName())) {
            int indexOf = str.indexOf(95);
            char c = 65535;
            if (indexOf == -1) {
                return 0;
            }
            String lowerCase = str.substring(0, indexOf).toLowerCase(Locale.ENGLISH);
            str = str.substring(indexOf + 1);
            switch (lowerCase.hashCode()) {
                case -985163900:
                    if (lowerCase.equals("plural")) {
                        c = 6;
                        break;
                    }
                    break;
                case -791090288:
                    if (lowerCase.equals("pattern")) {
                        c = 0;
                        break;
                    }
                    break;
                case 64711720:
                    if (lowerCase.equals("boolean")) {
                        c = '\t';
                        break;
                    }
                    break;
                case 97526364:
                    if (lowerCase.equals("float")) {
                        c = 5;
                        break;
                    }
                    break;
                case 103772132:
                    if (lowerCase.equals("media")) {
                        c = 7;
                        break;
                    }
                    break;
                case 110327241:
                    if (lowerCase.equals("theme")) {
                        c = 1;
                        break;
                    }
                    break;
                case 280343272:
                    if (lowerCase.equals("graphic")) {
                        c = '\b';
                        break;
                    }
                    break;
                case 566720458:
                    if (lowerCase.equals("intarray")) {
                        c = 4;
                        break;
                    }
                    break;
                case 1118509956:
                    if (lowerCase.equals("animation")) {
                        c = 2;
                        break;
                    }
                    break;
                case 1787751112:
                    if (lowerCase.equals("strarray")) {
                        c = 3;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                case 1:
                    break;
                case 2:
                    str2 = "animator";
                    break;
                case 3:
                case 4:
                    str2 = "array";
                    break;
                case 5:
                    str2 = "dimen";
                    break;
                case 6:
                    str2 = "plurals";
                    break;
                case 7:
                case '\b':
                    str2 = "drawable";
                    break;
                case '\t':
                    str2 = "bool";
                    break;
                default:
                    str3 = lowerCase;
                    break;
            }
            return Package.getAResId(str, str3, t);
        }
        str2 = cls.getSimpleName().toLowerCase(Locale.ENGLISH);
        str3 = str2;
        return Package.getAResId(str, str3, t);
    }
}
