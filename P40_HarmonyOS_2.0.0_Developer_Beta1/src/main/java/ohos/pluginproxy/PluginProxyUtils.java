package ohos.pluginproxy;

import android.app.ActivityThread;
import android.app.Application;
import android.content.ContentProvider;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import ark.system.ClassLoaderCreator;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import ohos.app.AbilityContext;
import ohos.global.resource.RawFileEntry;
import ohos.global.resource.Resource;
import ohos.global.resource.ResourceManager;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.pluginproxy.annoation.Singleton;
import ohos.pluginproxy.annoation.SingletonImpl;
import ohos.utils.zson.ZSONArray;
import ohos.utils.zson.ZSONObject;

public final class PluginProxyUtils {
    static final String ABILITY_CONTEXT_CLASS = "ohos.app.AbilityContext";
    private static final Map<String, ClassMapInfo> CLASS_LOADER_MAP = new HashMap();
    private static final String CLASS_MAP_CONFIG_FILE_NAME = "class_map_config.json";
    private static final String CONFIG_ACLS_NAME_KEY = "aClsName";
    private static final String CONFIG_CLASS_MAP_KEY = "classMap";
    private static final String CONFIG_HCLS_NAME_KEY = "hClsName";
    static final String CONTEXT_CLASS = "ohos.app.Context";
    private static final int DEFAULT_BUFFER_SIZE = 1024;
    private static final HiLogLabel LABEL_LOG = new HiLogLabel(3, 218108160, "PluginProxyUtils");
    private static final Object LOCK = new Object();
    private static Context context = null;

    public static Object createProxyObject(ohos.app.Context context2, String str, Class<?> cls, Object... objArr) {
        if (context2 == null || isEmptyStr(str) || cls == null) {
            HiLog.debug(LABEL_LOG, "createProxyObject parameters invalid.", new Object[0]);
            return null;
        } else if (!context2.isAllowClassMap()) {
            HiLog.debug(LABEL_LOG, "createProxyObject allowClassMap switch disabled.", new Object[0]);
            return null;
        } else {
            synchronized (LOCK) {
                File filesDir = context2.getFilesDir();
                if (filesDir != null) {
                    if (filesDir.exists()) {
                        String str2 = filesDir.getPath() + File.separator + str;
                        ClassMapInfo classMapInfo = CLASS_LOADER_MAP.get(str2);
                        if (classMapInfo != null) {
                            HiLog.debug(LABEL_LOG, "createProxyObject already exec.", new Object[0]);
                            return innerCreateProxyObject(cls, classMapInfo, objArr);
                        } else if (!copyApkFromRawFile(context2, str)) {
                            HiLog.debug(LABEL_LOG, "createProxyObject copyApkFromRawFile failed.", new Object[0]);
                            return null;
                        } else {
                            Map<String, String> parseClassConfig = parseClassConfig(str2, CLASS_MAP_CONFIG_FILE_NAME);
                            if (parseClassConfig == null) {
                                HiLog.debug(LABEL_LOG, "createProxyObject config class map parse failed.", new Object[0]);
                                return null;
                            }
                            ClassMapInfo classMapInfo2 = new ClassMapInfo(ClassLoaderCreator.createClassLoader(str2, context2.getClassloader()), parseClassConfig);
                            if (CLASS_LOADER_MAP.putIfAbsent(str2, classMapInfo2) == null) {
                                HiLog.debug(LABEL_LOG, "createProxyObject save classMapInfo success.", new Object[0]);
                            }
                            return innerCreateProxyObject(cls, classMapInfo2, objArr);
                        }
                    }
                }
                return null;
            }
        }
    }

    private static Object innerCreateProxyObject(Class<?> cls, ClassMapInfo classMapInfo, Object[] objArr) {
        Class[] clsArr;
        Object[] objArr2;
        if (cls != null) {
            try {
                if (classMapInfo.classMap != null) {
                    if (classMapInfo.loader != null) {
                        String str = classMapInfo.classMap.get(cls.getName());
                        Class<?> cls2 = Class.forName(str, true, classMapInfo.loader);
                        if (str != null) {
                            if (str.equals(cls2.getName())) {
                                int length = objArr != null ? objArr.length : 0;
                                if (length <= 0) {
                                    objArr2 = null;
                                    clsArr = null;
                                } else if ((length & 1) != 0) {
                                    HiLog.debug(LABEL_LOG, "innerCreateProxyObject args' length invalid", new Object[0]);
                                    return null;
                                } else {
                                    int i = length >> 1;
                                    clsArr = new Class[i];
                                    objArr2 = new Object[i];
                                    if (!generateParamsClassAndParamsValue(clsArr, objArr2, objArr)) {
                                        HiLog.debug(LABEL_LOG, "innerCreateProxyObject parse args failed.", new Object[0]);
                                        return null;
                                    }
                                }
                                Object instanceObject = getInstanceObject(cls2, clsArr, objArr2);
                                if (instanceObject != null) {
                                    return createProxyObject(cls, new HarmonyProxyGenHandler(cls2, instanceObject));
                                }
                                HiLog.debug(LABEL_LOG, "innerCreateProxyObject map object is null.", new Object[0]);
                                return null;
                            }
                        }
                        HiLog.debug(LABEL_LOG, "innerCreateProxyObject the mapClass invalid.", new Object[0]);
                        return null;
                    }
                }
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
                HiLog.warn(LABEL_LOG, "innerCreateProxyObject occur exception %{public}s", e.getMessage());
                return null;
            }
        }
        HiLog.debug(LABEL_LOG, "innerCreateProxyObject input class parameter invalid.", new Object[0]);
        return null;
    }

    static Object createProxyObject(Class<?> cls, ProxyGenHandler proxyGenHandler) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        return Proxy.getProxyClass(cls.getClassLoader(), cls).getConstructor(InvocationHandler.class).newInstance(proxyGenHandler);
    }

    static String getMappingClassName(ClassLoader classLoader, String str) {
        if (classLoader == null || str == null) {
            return null;
        }
        synchronized (LOCK) {
            for (Map.Entry<String, ClassMapInfo> entry : CLASS_LOADER_MAP.entrySet()) {
                ClassMapInfo value = entry.getValue();
                if (value != null && classLoader.equals(value.loader)) {
                    if (value.classMap == null) {
                        return null;
                    }
                    return value.classMap.get(str);
                }
            }
            return null;
        }
    }

    private static Object getInstanceObject(Class<?> cls, Class<?>[] clsArr, Object[] objArr) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        if (cls.isAnnotationPresent(Singleton.class)) {
            HiLog.debug(LABEL_LOG, "getInstanceObject Singleton condition.", new Object[0]);
            Method[] declaredMethods = cls.getDeclaredMethods();
            for (Method method : declaredMethods) {
                if (method.getAnnotation(SingletonImpl.class) != null) {
                    if (!Modifier.isPublic(method.getModifiers())) {
                        method.setAccessible(true);
                    }
                    return method.invoke(null, objArr);
                }
            }
            return null;
        }
        HiLog.debug(LABEL_LOG, "getInstanceObject getConstructor condition.", new Object[0]);
        Constructor<?> constructor = cls.getConstructor(clsArr);
        if (!Modifier.isPublic(constructor.getModifiers())) {
            constructor.setAccessible(true);
        }
        return constructor.newInstance(objArr);
    }

    private static boolean generateParamsClassAndParamsValue(Class<?>[] clsArr, Object[] objArr, Object[] objArr2) throws ClassNotFoundException {
        int length = clsArr.length;
        for (int i = 0; i < length; i++) {
            if (!(objArr2[i] instanceof Class)) {
                HiLog.debug(LABEL_LOG, "generateParamsClassAndParamsValue args included invalid class object.", new Object[0]);
                return false;
            }
            Class<?> cls = (Class) objArr2[i];
            if (ABILITY_CONTEXT_CLASS.equals(cls.getName()) || CONTEXT_CLASS.equals(cls.getName())) {
                HiLog.debug(LABEL_LOG, "handle input class is Context.", new Object[0]);
                clsArr[i] = Class.forName("android.content.Context");
            } else {
                HiLog.debug(LABEL_LOG, "handle class is other type:%{public}s.", cls.getName());
                clsArr[i] = cls;
            }
            Object obj = objArr2[i + length];
            if (obj == null) {
                HiLog.debug(LABEL_LOG, "generateParamsClassAndParamsValue args included invalid value object.", new Object[0]);
                return false;
            }
            objArr[i] = convertParameter(obj);
        }
        return true;
    }

    private static Object convertParameter(Object obj) {
        if (obj instanceof AbilityContext) {
            HiLog.debug(LABEL_LOG, "convertParameter param is ohos.app.AbilityContext.", new Object[0]);
            Object abilityShell = ((AbilityContext) obj).getAbilityShell();
            if (abilityShell == null) {
                HiLog.debug(LABEL_LOG, "convertParameter param is ablilityPackage.", new Object[0]);
                return getAospContext();
            } else if (!(abilityShell instanceof ContentProvider)) {
                return abilityShell;
            } else {
                HiLog.debug(LABEL_LOG, "convertParameter param is ContentProvider.", new Object[0]);
                return ((ContentProvider) abilityShell).getContext();
            }
        } else if (obj instanceof ohos.app.Context) {
            HiLog.debug(LABEL_LOG, "convertParameter param is ohos.app.Context.", new Object[0]);
            return getAospContext();
        } else {
            HiLog.debug(LABEL_LOG, "convertParameter param is other type:%{public}s.", obj);
            return obj;
        }
    }

    static Object[] convertParameters(Object[] objArr) {
        if (objArr == null) {
            return new Object[0];
        }
        int length = objArr.length;
        Object[] objArr2 = new Object[length];
        for (int i = 0; i < length; i++) {
            Object obj = objArr[i];
            if (obj != null) {
                objArr2[i] = convertParameter(obj);
            }
        }
        return objArr2;
    }

    private static Object getAospContext() {
        Application currentApplication;
        if (context == null && (currentApplication = ActivityThread.currentApplication()) != null) {
            context = currentApplication.getApplicationContext();
        }
        return context;
    }

    private static boolean isEmptyStr(String str) {
        return str == null || str.isEmpty();
    }

    private static void closeStream(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException unused) {
                HiLog.debug(LABEL_LOG, "closeStream failed.", new Object[0]);
            }
        }
    }

    private static Resources getPluginResources(String str) {
        try {
            AssetManager assetManager = (AssetManager) AssetManager.class.newInstance();
            assetManager.getClass().getMethod("addAssetPath", String.class).invoke(assetManager, str);
            Object aospContext = getAospContext();
            Context context2 = aospContext instanceof Context ? (Context) aospContext : null;
            if (context2 == null) {
                return null;
            }
            Resources resources = context2.getResources();
            return new Resources(assetManager, resources.getDisplayMetrics(), resources.getConfiguration());
        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException unused) {
            HiLog.warn(LABEL_LOG, "getPluginResources occur exception.", new Object[0]);
            return null;
        }
    }

    private static boolean copyApkFromRawFile(ohos.app.Context context2, String str) {
        Throwable th;
        Resource resource;
        FileOutputStream fileOutputStream;
        File file = new File(context2.getFilesDir(), str);
        if (file.exists()) {
            return true;
        }
        ResourceManager resourceManager = context2.getResourceManager();
        if (resourceManager == null) {
            return false;
        }
        RawFileEntry rawFileEntry = resourceManager.getRawFileEntry("resources/rawfile/" + str);
        if (rawFileEntry == null) {
            return false;
        }
        Resource resource2 = null;
        try {
            resource = rawFileEntry.openRawFile();
            if (resource == null) {
                closeStream(resource);
                closeStream(null);
                return false;
            }
            try {
                fileOutputStream = new FileOutputStream(file);
            } catch (IOException unused) {
                fileOutputStream = null;
                resource2 = resource;
                try {
                    HiLog.warn(LABEL_LOG, "copyApkFromRawFile occur exception.", new Object[0]);
                    closeStream(resource2);
                    closeStream(fileOutputStream);
                    return false;
                } catch (Throwable th2) {
                    th = th2;
                    resource = resource2;
                    closeStream(resource);
                    closeStream(fileOutputStream);
                    throw th;
                }
            } catch (Throwable th3) {
                fileOutputStream = null;
                th = th3;
                closeStream(resource);
                closeStream(fileOutputStream);
                throw th;
            }
            try {
                byte[] bArr = new byte[1024];
                while (true) {
                    int read = resource.read(bArr);
                    if (read != -1) {
                        fileOutputStream.write(bArr, 0, read);
                    } else {
                        fileOutputStream.flush();
                        HiLog.debug(LABEL_LOG, "copyApkFromRawFile success", new Object[0]);
                        closeStream(resource);
                        closeStream(fileOutputStream);
                        return true;
                    }
                }
            } catch (IOException unused2) {
                resource2 = resource;
                HiLog.warn(LABEL_LOG, "copyApkFromRawFile occur exception.", new Object[0]);
                closeStream(resource2);
                closeStream(fileOutputStream);
                return false;
            } catch (Throwable th4) {
                th = th4;
                closeStream(resource);
                closeStream(fileOutputStream);
                throw th;
            }
        } catch (IOException unused3) {
            fileOutputStream = null;
            HiLog.warn(LABEL_LOG, "copyApkFromRawFile occur exception.", new Object[0]);
            closeStream(resource2);
            closeStream(fileOutputStream);
            return false;
        } catch (Throwable th5) {
            fileOutputStream = null;
            th = th5;
            resource = null;
            closeStream(resource);
            closeStream(fileOutputStream);
            throw th;
        }
    }

    /* JADX INFO: finally extract failed */
    private static Map<String, String> parseClassConfig(String str, String str2) {
        Resources pluginResources = getPluginResources(str);
        if (pluginResources == null) {
            return Collections.emptyMap();
        }
        AssetManager assets = pluginResources.getAssets();
        if (assets == null) {
            return Collections.emptyMap();
        }
        try {
            InputStream open = assets.open(str2);
            byte[] bArr = new byte[1024];
            StringBuilder sb = new StringBuilder();
            while (true) {
                int read = open.read(bArr);
                if (read == -1) {
                    break;
                }
                sb.append(new String(bArr, 0, read, Charset.defaultCharset()));
            }
            HashMap hashMap = new HashMap();
            ZSONArray zSONArray = ZSONObject.stringToZSON(sb.toString()).getZSONArray(CONFIG_CLASS_MAP_KEY);
            for (int i = 0; i < zSONArray.size(); i++) {
                ZSONObject zSONObject = zSONArray.getZSONObject(i);
                String string = zSONObject.getString(CONFIG_HCLS_NAME_KEY);
                String string2 = zSONObject.getString(CONFIG_ACLS_NAME_KEY);
                hashMap.put(string, string2);
                HiLog.debug(LABEL_LOG, "parseClassConfig content:%{public}s, %{public}s ", string, string2);
            }
            closeStream(open);
            return hashMap;
        } catch (IOException | IndexOutOfBoundsException e) {
            HiLog.warn(LABEL_LOG, "parseClassConfig occur exception: %{public}s", e.getMessage());
            closeStream(null);
            return Collections.emptyMap();
        } catch (Throwable th) {
            closeStream(null);
            throw th;
        }
    }

    private PluginProxyUtils() {
    }

    /* access modifiers changed from: private */
    public static class ClassMapInfo {
        Map<String, String> classMap;
        ClassLoader loader;

        ClassMapInfo(ClassLoader classLoader, Map<String, String> map) {
            this.loader = classLoader;
            if (map != null) {
                this.classMap = new HashMap(map);
            }
        }
    }
}
