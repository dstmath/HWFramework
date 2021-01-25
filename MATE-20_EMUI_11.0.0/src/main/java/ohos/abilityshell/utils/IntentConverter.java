package ohos.abilityshell.utils;

import android.app.RemoteInput;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.ArrayMap;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import ohos.aafwk.content.IntentParams;
import ohos.appexecfwk.utils.AppLog;
import ohos.bundle.AbilityInfo;
import ohos.bundle.ElementName;
import ohos.bundle.ShellInfo;
import ohos.hiviewdfx.HiLogLabel;
import ohos.net.UriConverter;
import ohos.utils.Sequenceable;
import ohos.utils.adapter.IntentConstantMapper;
import ohos.utils.adapter.PacMapUtils;
import ohos.utils.net.Uri;

public class IntentConverter {
    private static final String MAGIC_KEY_RAW_PARAMS = "harmony_aafwk_raw_params";
    private static final String NOTIFICATION_USER_INPUT = "harmony_notification_user_input";
    private static final HiLogLabel SHELL_LABEL = new HiLogLabel(3, 218108160, "AbilityShell");

    private IntentConverter() {
    }

    public static Optional<Intent> createAndroidIntent(ohos.aafwk.content.Intent intent, ShellInfo shellInfo) {
        if (intent == null) {
            return Optional.empty();
        }
        Intent intent2 = new Intent();
        String action = intent.getAction();
        if (action != null) {
            intent2.setAction((String) IntentConstantMapper.convertToAndroidAction(action).orElse(action));
        }
        intent2.setFlags(intent.getFlags());
        Set<String> entities = intent.getEntities();
        if (entities != null) {
            for (String str : entities) {
                intent2.addCategory((String) IntentConstantMapper.convertToAndroidEntity(str).orElse(str));
            }
        }
        createComponentName(intent.getElement(), shellInfo).ifPresent(new Consumer(intent2) {
            /* class ohos.abilityshell.utils.$$Lambda$IntentConverter$X8sIYmThMT5HsYXdajrc7kuVZqM */
            private final /* synthetic */ Intent f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                Intent unused = this.f$0.setComponent((ComponentName) obj);
            }
        });
        IntentParams params = intent.getParams();
        if (params != null) {
            convertIntentParamsToBundle(params).ifPresent(new Consumer(intent2) {
                /* class ohos.abilityshell.utils.$$Lambda$IntentConverter$ScLLNJBrvmciSb9gCBUWuhz1m58 */
                private final /* synthetic */ Intent f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    Intent unused = this.f$0.putExtras((Bundle) obj);
                }
            });
        }
        String bundle = intent.getBundle();
        if (bundle != null) {
            intent2.setPackage(bundle);
        }
        Uri uri = intent.getUri();
        android.net.Uri convertToAndroidUri = uri != null ? UriConverter.convertToAndroidUri(uri) : null;
        String stringParam = (params == null || !params.isUnpacked()) ? null : intent.getStringParam("mime-type");
        if (convertToAndroidUri != null && stringParam != null) {
            intent2.setDataAndType(convertToAndroidUri, stringParam);
        } else if (convertToAndroidUri != null) {
            intent2.setData(convertToAndroidUri);
        } else if (stringParam != null) {
            intent2.setType(stringParam);
        } else {
            AppLog.d(SHELL_LABEL, "IntentConverter::createAndroidIntent uri and mime-type are null", new Object[0]);
        }
        ohos.aafwk.content.Intent picker = intent.getPicker();
        if (picker != null) {
            intent2.setSelector(createAndroidIntent(picker, null).orElse(null));
        }
        return Optional.of(intent2);
    }

    /* JADX WARNING: Removed duplicated region for block: B:31:0x00af  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00ba  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00cd  */
    public static Optional<ohos.aafwk.content.Intent> createZidaneIntent(Intent intent, AbilityInfo abilityInfo) {
        Uri uri;
        String type;
        Intent selector;
        Bundle resultsFromIntent;
        if (intent == null) {
            return Optional.empty();
        }
        ohos.aafwk.content.Intent intent2 = new ohos.aafwk.content.Intent();
        String action = intent.getAction();
        if (action != null) {
            intent2.setAction((String) IntentConstantMapper.convertToZidaneAction(action).orElse(action));
        }
        intent2.setFlags(intent.getFlags());
        Set<String> categories = intent.getCategories();
        if (categories != null) {
            for (String str : categories) {
                intent2.addEntity((String) IntentConstantMapper.convertToZidaneEntity(str).orElse(str));
            }
        }
        createElementName(intent.getComponent(), abilityInfo).ifPresent(new Consumer() {
            /* class ohos.abilityshell.utils.$$Lambda$FG1GMK_2J97cptU0pyS04JgYyow */

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                ohos.aafwk.content.Intent.this.setElement((ElementName) obj);
            }
        });
        Bundle extras = intent.getExtras();
        Optional<IntentParams> convertBundleToIntentParams = convertBundleToIntentParams(extras);
        if (convertBundleToIntentParams.isPresent()) {
            if (!(extras == null || extras.getClassLoader() == null)) {
                convertBundleToIntentParams.get().setClassLoader(extras.getClassLoader());
            }
            intent2.setParams(convertBundleToIntentParams.get());
        }
        String str2 = intent.getPackage();
        if (str2 != null) {
            intent2.setBundle(str2);
        }
        android.net.Uri data = intent.getData();
        if (data != null) {
            try {
                uri = UriConverter.convertToZidaneUri(data);
            } catch (IllegalArgumentException | NullPointerException | UnsupportedOperationException unused) {
                AppLog.e(SHELL_LABEL, "convert uri is invalid", new Object[0]);
            }
            intent2.setUri(uri);
            type = intent.getType();
            if (type != null) {
                intent2.setParam("mime-type", type);
            }
            selector = intent.getSelector();
            if (selector != null) {
                intent2.setPicker(createZidaneIntent(selector, null).orElse(null));
            }
            resultsFromIntent = RemoteInput.getResultsFromIntent(intent);
            if (resultsFromIntent != null) {
                intent2.setParam(NOTIFICATION_USER_INPUT, (Sequenceable) PacMapUtils.convertFromBundle(resultsFromIntent));
            }
            return Optional.of(intent2);
        }
        uri = null;
        intent2.setUri(uri);
        type = intent.getType();
        if (type != null) {
        }
        selector = intent.getSelector();
        if (selector != null) {
        }
        resultsFromIntent = RemoteInput.getResultsFromIntent(intent);
        if (resultsFromIntent != null) {
        }
        return Optional.of(intent2);
    }

    public static Optional<ElementName> createElementName(ComponentName componentName, AbilityInfo abilityInfo) {
        String str;
        String str2;
        if (componentName == null && abilityInfo == null) {
            AppLog.e(SHELL_LABEL, "IntentConverter::createElementName param invalid", new Object[0]);
            return Optional.empty();
        }
        if (abilityInfo != null) {
            str2 = abilityInfo.getBundleName();
            str = abilityInfo.getClassName();
        } else {
            String packageName = componentName.getPackageName();
            str = componentName.getClassName();
            str2 = packageName;
        }
        if (str2 == null || str == null) {
            return Optional.empty();
        }
        return Optional.of(new ElementName("", str2, str));
    }

    public static Optional<ComponentName> createComponentName(ElementName elementName, ShellInfo shellInfo) {
        String str;
        String str2;
        if (elementName == null && shellInfo == null) {
            AppLog.e(SHELL_LABEL, "IntentConverter::createComponentName param invalid", new Object[0]);
            return Optional.empty();
        }
        if (shellInfo != null) {
            str2 = shellInfo.getPackageName();
            str = shellInfo.getName();
        } else {
            String bundleName = elementName.getBundleName();
            str = elementName.getAbilityName();
            str2 = bundleName;
        }
        if (str2 == null || str == null) {
            return Optional.empty();
        }
        return Optional.of(new ComponentName(str2, str));
    }

    private static Optional<Bundle> convertIntentParamsToBundle(IntentParams intentParams) {
        if (intentParams.isUnpacked()) {
            return convertIntentParamsToBundle(intentParams.getParams());
        }
        return convertIntentParamsToBundle(intentParams.getRawParams());
    }

    private static Optional<Bundle> convertIntentParamsToBundle(byte[] bArr) {
        AppLog.d(SHELL_LABEL, "IntentConverter::convertIntentParamsToBundle raw params", new Object[0]);
        if (bArr == null) {
            return Optional.empty();
        }
        Bundle bundle = new Bundle();
        bundle.putByteArray(MAGIC_KEY_RAW_PARAMS, bArr);
        return Optional.of(bundle);
    }

    private static Optional<Bundle> convertIntentParamsToBundle(Map<String, Object> map) {
        if (map == null) {
            return Optional.empty();
        }
        Bundle bundle = new Bundle();
        ArrayMap<String, Object> reflectGetInnerMap = reflectGetInnerMap(bundle);
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value != null) {
                if (value instanceof Boolean) {
                    bundle.putBoolean(key, ((Boolean) value).booleanValue());
                } else if (value instanceof Byte) {
                    bundle.putByte(key, ((Byte) value).byteValue());
                } else if (value instanceof Character) {
                    bundle.putChar(key, ((Character) value).charValue());
                } else if (value instanceof Short) {
                    bundle.putShort(key, ((Short) value).shortValue());
                } else if (value instanceof Integer) {
                    bundle.putInt(key, ((Integer) value).intValue());
                } else if (value instanceof Long) {
                    bundle.putLong(key, ((Long) value).longValue());
                } else if (value instanceof Float) {
                    bundle.putFloat(key, ((Float) value).floatValue());
                } else if (value instanceof Double) {
                    bundle.putDouble(key, ((Double) value).doubleValue());
                } else if (value instanceof String) {
                    bundle.putString(key, (String) value);
                } else if (value instanceof CharSequence) {
                    bundle.putCharSequence(key, (CharSequence) value);
                } else if (value instanceof boolean[]) {
                    bundle.putBooleanArray(key, (boolean[]) value);
                } else if (value instanceof byte[]) {
                    bundle.putByteArray(key, (byte[]) value);
                } else if (value instanceof char[]) {
                    bundle.putCharArray(key, (char[]) value);
                } else if (value instanceof short[]) {
                    bundle.putShortArray(key, (short[]) value);
                } else if (value instanceof int[]) {
                    bundle.putIntArray(key, (int[]) value);
                } else if (value instanceof long[]) {
                    bundle.putLongArray(key, (long[]) value);
                } else if (value instanceof float[]) {
                    bundle.putFloatArray(key, (float[]) value);
                } else if (value instanceof double[]) {
                    bundle.putDoubleArray(key, (double[]) value);
                } else if (value instanceof String[]) {
                    bundle.putStringArray(key, (String[]) value);
                } else if (value instanceof IntentParams) {
                    bundle.putBundle(key, convertIntentParamsToBundle(((IntentParams) value).getParams()).orElse(null));
                } else if (value instanceof Sequenceable) {
                    bundle.putParcelable(key, new SequenceableWrapper((Sequenceable) value));
                } else if (value instanceof Sequenceable[]) {
                    bundle.putParcelableArray(key, SequenceableWrapper.wrapArray((Sequenceable[]) value));
                } else if ((value instanceof List) && reflectGetInnerMap != null) {
                    wrapSequenceableForList((List) value);
                    reflectGetInnerMap.put(key, value);
                } else if (value instanceof Serializable) {
                    bundle.putSerializable(key, (Serializable) value);
                } else {
                    AppLog.w(SHELL_LABEL, "IntentConverter::convertIntentParamsToBundle unknown type %{public}s", value.getClass().getName());
                }
            }
        }
        return Optional.of(bundle);
    }

    private static Optional<IntentParams> convertBundleToIntentParams(byte[] bArr) {
        AppLog.d(SHELL_LABEL, "IntentConverter::convertBundleToIntentParams raw params", new Object[0]);
        IntentParams intentParams = new IntentParams();
        intentParams.setRawParams(bArr);
        return Optional.of(intentParams);
    }

    private static Optional<IntentParams> convertBundleToIntentParams(Bundle bundle) {
        if (bundle == null) {
            return Optional.empty();
        }
        if (bundle.containsKey(MAGIC_KEY_RAW_PARAMS)) {
            return convertBundleToIntentParams(bundle.getByteArray(MAGIC_KEY_RAW_PARAMS));
        }
        IntentParams intentParams = new IntentParams();
        Set<String> keySet = bundle.keySet();
        if (keySet != null) {
            for (String str : keySet) {
                Object obj = bundle.get(str);
                if (obj == null || (obj instanceof CharSequence)) {
                    intentParams.setParam(str, obj);
                } else if (obj instanceof Bundle) {
                    intentParams.setParam(str, convertBundleToIntentParams((Bundle) obj).orElse(null));
                } else if (obj instanceof SequenceableWrapper) {
                    intentParams.setParam(str, ((SequenceableWrapper) obj).getWrappedSequenceable(bundle.getClassLoader()));
                } else if (obj instanceof Parcelable[]) {
                    intentParams.setParam(str, SequenceableWrapper.unwrapArray((Parcelable[]) obj, bundle.getClassLoader()));
                } else if (obj instanceof List) {
                    unwrapSequenceableForList((List) obj, bundle.getClassLoader());
                    intentParams.setParam(str, obj);
                } else if (obj instanceof Serializable) {
                    intentParams.setParam(str, obj);
                } else {
                    AppLog.w(SHELL_LABEL, "IntentConverter::createZidaneIntent unknown type %{public}s", obj.getClass().getName());
                }
            }
        }
        return Optional.of(intentParams);
    }

    private static void wrapSequenceableForList(List list) {
        for (int i = 0; i < list.size(); i++) {
            Object obj = list.get(i);
            if (obj != null && (obj instanceof Sequenceable)) {
                list.set(i, new SequenceableWrapper((Sequenceable) obj));
            }
        }
    }

    private static void unwrapSequenceableForList(List list, ClassLoader classLoader) {
        for (int i = 0; i < list.size(); i++) {
            Object obj = list.get(i);
            if (obj != null && (obj instanceof SequenceableWrapper)) {
                list.set(i, ((SequenceableWrapper) obj).getWrappedSequenceable(classLoader));
            }
        }
    }

    private static ArrayMap<String, Object> reflectGetInnerMap(Bundle bundle) {
        try {
            Method declaredMethod = Bundle.class.getSuperclass().getDeclaredMethod("getMap", new Class[0]);
            declaredMethod.setAccessible(true);
            return (ArrayMap) declaredMethod.invoke(bundle, new Object[0]);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            AppLog.e(SHELL_LABEL, "IntentConverter::reflectGetInnerMap fail: %{public}s", e.getMessage());
            return null;
        }
    }
}
