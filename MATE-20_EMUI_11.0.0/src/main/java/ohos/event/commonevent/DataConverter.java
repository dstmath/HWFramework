package ohos.event.commonevent;

import android.content.Intent;
import android.content.IntentFilter;
import dalvik.system.PathClassLoader;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import ohos.event.EventConstant;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.RemoteException;
import ohos.utils.Parcel;

public class DataConverter {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) EventConstant.COMMON_EVENT_DOMAIN, TAG);
    private static final String TAG = "DataConverter";
    private static ConcurrentHashMap<String, Object> actionObjectMap = new ConcurrentHashMap<>();
    private static CommonEventBaseConverter baseConverter = new CommonEventBaseConverter();

    public static Optional<Intent> createAndroidIntent(boolean z, ohos.aafwk.content.Intent intent) {
        if (intent == null) {
            return Optional.empty();
        }
        Optional<Intent> convertIntentToAospIntent = baseConverter.convertIntentToAospIntent(intent);
        String action = intent.getAction();
        if (isEmptyStr(action)) {
            return convertIntentToAospIntent;
        }
        Object obj = actionObjectMap.get(action);
        if (obj == null && ActionMapper.queryZAction(action)) {
            obj = getActionClassName(z, action);
        }
        if (obj instanceof CommonEventBaseConverter) {
            Optional<Intent> convertIntentToAospIntent2 = ((CommonEventBaseConverter) obj).convertIntentToAospIntent(intent);
            if (convertIntentToAospIntent2.isPresent()) {
                return convertIntentToAospIntent2;
            }
        }
        return convertIntentToAospIntent;
    }

    public static Optional<ohos.aafwk.content.Intent> createZidaneIntent(boolean z, Intent intent) {
        if (intent == null) {
            return Optional.empty();
        }
        Optional<ohos.aafwk.content.Intent> convertAospIntentToIntent = baseConverter.convertAospIntentToIntent(intent);
        String zAction = ActionMapper.getZAction(intent.getAction());
        if (isEmptyStr(zAction)) {
            return convertAospIntentToIntent;
        }
        Object obj = actionObjectMap.get(zAction);
        if (obj == null) {
            obj = getActionClassName(z, zAction);
        }
        if (obj instanceof CommonEventBaseConverter) {
            Optional<ohos.aafwk.content.Intent> convertAospIntentToIntent2 = ((CommonEventBaseConverter) obj).convertAospIntentToIntent(intent);
            if (convertAospIntentToIntent2.isPresent()) {
                return convertAospIntentToIntent2;
            }
        }
        return convertAospIntentToIntent;
    }

    public static Optional<IntentFilter> createAndroidIntentFilter(boolean z, int i, ohos.aafwk.content.IntentFilter intentFilter) {
        if (intentFilter == null) {
            return Optional.empty();
        }
        Optional<IntentFilter> of = Optional.of(new IntentFilter());
        baseConverter.convertIntentFilterToAospIntentFilter(intentFilter, of.get());
        int countActions = intentFilter.countActions();
        for (int i2 = 0; i2 < countActions; i2++) {
            String action = intentFilter.getAction(i2);
            if (!isEmptyStr(action)) {
                Object obj = actionObjectMap.get(action);
                if (obj == null && ActionMapper.queryZAction(action)) {
                    obj = getActionClassName(z, action);
                }
                if (obj instanceof CommonEventBaseConverter) {
                    ((CommonEventBaseConverter) obj).convertIntentFilterToAospIntentFilter(intentFilter, of.get());
                }
            }
        }
        of.ifPresent(new Consumer(i) {
            /* class ohos.event.commonevent.$$Lambda$DataConverter$TM6URQPf0_QwiVy0aPSXDXtWPm4 */
            private final /* synthetic */ int f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ((IntentFilter) obj).setPriority(this.f$0);
            }
        });
        return of;
    }

    private static Object getActionClassName(boolean z, String str) {
        CommonEventConvertInfo commonEventConvertInfo;
        ClassLoader classLoader;
        if (z) {
            Parcel create = Parcel.create();
            CesAdapterManager.nativeGetActionClassName(str, create);
            CommonEventConvertInfo commonEventConvertInfo2 = new CommonEventConvertInfo();
            if (!create.readSequenceable(commonEventConvertInfo2)) {
                HiLog.debug(LABEL, "getActionClassName read parcel failed. input action=%{public}s", new Object[]{str});
                create.reclaim();
                return null;
            }
            create.reclaim();
            commonEventConvertInfo = commonEventConvertInfo2;
        } else {
            try {
                commonEventConvertInfo = CesManagerProxy.getCesManagerProxy().getActionClassName(str);
            } catch (RemoteException unused) {
                HiLog.warn(LABEL, "getActionClassName occur remote exception.", new Object[0]);
                commonEventConvertInfo = null;
            }
        }
        if (commonEventConvertInfo == null) {
            return null;
        }
        String jarName = commonEventConvertInfo.getJarName();
        if (!isValidJarFile(jarName)) {
            return null;
        }
        String className = commonEventConvertInfo.getClassName();
        if (isEmptyStr(className)) {
            return null;
        }
        if (z) {
            try {
                classLoader = Thread.currentThread().getContextClassLoader();
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException unused2) {
                HiLog.warn(LABEL, "getActionClassName load class occur exception.", new Object[0]);
                return null;
            }
        } else {
            classLoader = new PathClassLoader(jarName, Thread.currentThread().getContextClassLoader());
        }
        if (classLoader == null) {
            return null;
        }
        Object newInstance = classLoader.loadClass(className).newInstance();
        saveConvertInfo(newInstance, commonEventConvertInfo.getActions());
        return newInstance;
    }

    private static boolean isValidJarFile(String str) {
        if (isEmptyStr(str)) {
            return false;
        }
        try {
            File file = new File(str);
            if (!file.getCanonicalPath().startsWith("/system/framework/") || !file.exists()) {
                return false;
            }
            return true;
        } catch (IOException | SecurityException unused) {
            HiLog.warn(LABEL, "isValidJarFile getCanonicalPath occur exception.", new Object[0]);
            return false;
        }
    }

    private static boolean isEmptyStr(String str) {
        return str == null || str.isEmpty();
    }

    private static void saveConvertInfo(Object obj, List<String> list) {
        if (!(obj == null || list == null)) {
            for (String str : list) {
                if (!isEmptyStr(str) && actionObjectMap.putIfAbsent(str, obj) != null) {
                    HiLog.debug(LABEL, "saveConvertInfo already saved.", new Object[0]);
                }
            }
        }
    }

    private DataConverter() {
    }
}
