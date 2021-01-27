package ohos.media.sessioncore.adapter;

import android.os.IBinder;
import android.service.media.IMediaBrowserService;
import java.lang.reflect.Field;
import ohos.aafwk.content.Intent;
import ohos.app.Context;
import ohos.media.sessioncore.adapter.AVBrowserServiceAdapter;
import ohos.media.sessioncore.adapter.IAVBrowserService;
import ohos.media.utils.log.Logger;
import ohos.media.utils.log.LoggerFactory;

public final class AVBrowserServiceHelper {
    private static final Object LOCK = new Object();
    private static final Logger LOGGER = LoggerFactory.getMediaLogger(AVBrowserServiceHelper.class);
    private static IAVBrowserService.Connection serviceConnection;

    private AVBrowserServiceHelper() {
    }

    public static void connectService(Context context, Intent intent, IAVBrowserService.Connection connection) {
        serviceConnection = connection;
        context.startAbility(intent, 1);
    }

    public static void initService(Context context) {
        if (serviceConnection != null) {
            AVBrowserServiceAdapter instance = AVBrowserServiceAdapter.getInstance();
            if (initServiceInternal(instance, context)) {
                serviceConnection.onConnected(instance);
            } else {
                serviceConnection.onConnectFailed();
            }
        } else {
            LOGGER.error("Call connectService first", new Object[0]);
            throw new IllegalStateException("Call connectService first");
        }
    }

    private static boolean initServiceInternal(AVBrowserServiceAdapter aVBrowserServiceAdapter, Context context) {
        synchronized (LOCK) {
            try {
                LOGGER.info("connectService initialize service", new Object[0]);
                AVBrowserServiceAdapter.Wrapper serviceWrapper = AVBrowserServiceAdapter.getInstance().getServiceWrapper();
                if (serviceWrapper == null) {
                    LOGGER.warn("connectService failed, start ability failed", new Object[0]);
                    return false;
                }
                serviceWrapper.onCreate();
                Object hostContext = context.getHostContext();
                if (!(hostContext instanceof android.content.Context)) {
                    LOGGER.warn("connectService failed, context instance error", new Object[0]);
                    return false;
                }
                setValue(serviceWrapper, "mBase", hostContext);
                Object value = getValue(serviceWrapper, "mBinder");
                if (!(value instanceof IBinder)) {
                    LOGGER.warn("connectService failed, binder instance error", new Object[0]);
                    return false;
                }
                aVBrowserServiceAdapter.setBrowserService(IMediaBrowserService.Stub.asInterface((IBinder) value));
                LOGGER.info("connectService success and callback", new Object[0]);
                return true;
            } catch (Exception e) {
                LOGGER.warn("connectService failed and callback, e: %{public}s", e);
                return false;
            }
        }
    }

    public static void disconnectService(Context context, IAVBrowserService.Connection connection) {
        LOGGER.info("disconnectService success and callback", new Object[0]);
        connection.onDisconnected();
    }

    private static Object getValue(Object obj, String str) throws Exception {
        Field field = null;
        try {
            field = getField(obj.getClass(), str);
            field.setAccessible(true);
            Object obj2 = field.get(obj);
            field.setAccessible(false);
            return obj2;
        } catch (Throwable th) {
            if (field != null) {
                field.setAccessible(false);
            }
            throw th;
        }
    }

    private static void setValue(Object obj, String str, Object obj2) throws Exception {
        Field field = null;
        try {
            field = getField(obj.getClass(), str);
            field.setAccessible(true);
            field.set(obj, obj2);
            field.setAccessible(false);
        } catch (Throwable th) {
            if (field != null) {
                field.setAccessible(false);
            }
            throw th;
        }
    }

    private static Field getField(Class<?> cls, String str) throws Exception {
        try {
            return cls.getDeclaredField(str);
        } catch (NoSuchFieldException e) {
            Class<? super Object> superclass = cls.getSuperclass();
            if (superclass != null) {
                return getField(superclass, str);
            }
            throw e;
        }
    }
}
