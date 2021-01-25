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

    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00bd, code lost:
        if ("theme".equals(r9) != false) goto L_0x00c1;
     */
    public static <T> int getAResId(int i, Class<?> cls, T t) {
        String str;
        String str2;
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
        if (str == null) {
            HiLog.error(LABEL, "name of the resource is null", new Object[0]);
            return 0;
        }
        String str3 = "style";
        if ("ResourceTable".equals(cls.getSimpleName())) {
            int indexOf = str.indexOf(95);
            char c = 65535;
            if (indexOf == -1) {
                return 0;
            }
            String lowerCase = str.substring(0, indexOf).toLowerCase(Locale.ENGLISH);
            str = str.substring(indexOf + 1);
            int hashCode = lowerCase.hashCode();
            if (hashCode != -791090288) {
                if (hashCode != 110327241) {
                    if (hashCode == 1118509956 && lowerCase.equals("animation")) {
                        c = 2;
                    }
                } else if (lowerCase.equals("theme")) {
                    c = 1;
                }
            } else if (lowerCase.equals("pattern")) {
                c = 0;
            }
            if (!(c == 0 || c == 1)) {
                if (c != 2) {
                    str3 = lowerCase;
                } else {
                    str2 = "animator";
                }
            }
            return Package.getAResId(str, str3, t);
        }
        str2 = cls.getSimpleName().toLowerCase(Locale.ENGLISH);
        str3 = str2;
        return Package.getAResId(str, str3, t);
    }
}
