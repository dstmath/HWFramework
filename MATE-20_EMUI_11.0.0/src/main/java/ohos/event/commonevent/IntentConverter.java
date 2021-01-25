package ohos.event.commonevent;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import java.io.Serializable;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import ohos.aafwk.content.IntentParams;
import ohos.bundle.AbilityInfo;
import ohos.bundle.ElementName;
import ohos.bundle.ShellInfo;
import ohos.event.EventConstant;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.net.UriConverter;
import ohos.utils.net.Uri;

public class IntentConverter {
    private static final HiLogLabel LABEL = new HiLogLabel(3, (int) EventConstant.COMMON_EVENT_DOMAIN, TAG);
    private static final String TAG = "IntentConverter";

    public static Optional<Intent> createAndroidIntent(ohos.aafwk.content.Intent intent, ShellInfo shellInfo) {
        if (intent == null) {
            return Optional.empty();
        }
        Intent intent2 = new Intent();
        String action = intent.getAction();
        if (action != null) {
            intent2.setAction(action);
        }
        intent2.setFlags(intent.getFlags());
        Set<String> entities = intent.getEntities();
        if (entities != null) {
            for (String str : entities) {
                intent2.addCategory(str);
            }
        }
        createComponentName(intent.getElement(), shellInfo).ifPresent(new Consumer(intent2) {
            /* class ohos.event.commonevent.$$Lambda$IntentConverter$X8sIYmThMT5HsYXdajrc7kuVZqM */
            private final /* synthetic */ Intent f$0;

            {
                this.f$0 = r1;
            }

            @Override // java.util.function.Consumer
            public final void accept(Object obj) {
                this.f$0.setComponent((ComponentName) obj);
            }
        });
        IntentParams params = intent.getParams();
        if (params != null) {
            convertIntentParamsToBundle(params.getParams()).ifPresent(new Consumer(intent2) {
                /* class ohos.event.commonevent.$$Lambda$IntentConverter$ScLLNJBrvmciSb9gCBUWuhz1m58 */
                private final /* synthetic */ Intent f$0;

                {
                    this.f$0 = r1;
                }

                @Override // java.util.function.Consumer
                public final void accept(Object obj) {
                    this.f$0.putExtras((Bundle) obj);
                }
            });
        }
        String bundle = intent.getBundle();
        if (bundle != null && !bundle.isEmpty()) {
            intent2.setPackage(bundle);
        }
        Uri uri = intent.getUri();
        android.net.Uri uri2 = null;
        if (uri != null) {
            uri2 = UriConverter.convertToAndroidUri(uri);
        }
        String stringParam = intent.getStringParam("mime-type");
        if (uri2 != null && stringParam != null) {
            intent2.setDataAndType(uri2, stringParam);
        } else if (uri2 != null) {
            intent2.setData(uri2);
        } else if (stringParam != null) {
            intent2.setType(stringParam);
        }
        return Optional.of(intent2);
    }

    public static Optional<ohos.aafwk.content.Intent> createZidaneIntent(Intent intent, AbilityInfo abilityInfo) {
        if (intent == null) {
            return Optional.empty();
        }
        ohos.aafwk.content.Intent intent2 = new ohos.aafwk.content.Intent();
        String action = intent.getAction();
        if (action != null) {
            intent2.setAction(action);
        }
        intent2.setFlags(intent.getFlags());
        Set<String> categories = intent.getCategories();
        if (categories != null) {
            for (String str : categories) {
                intent2.addEntity(str);
            }
        }
        createElementName(intent.getComponent(), abilityInfo).ifPresent(new Consumer() {
            /* class ohos.event.commonevent.$$Lambda$FG1GMK_2J97cptU0pyS04JgYyow */

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
        Uri uri = null;
        if (data != null) {
            uri = UriConverter.convertToZidaneUri(data);
        }
        intent2.setUri(uri);
        String type = intent.getType();
        if (type != null) {
            intent2.setParam("mime-type", type);
        }
        return Optional.of(intent2);
    }

    public static Optional<ElementName> createElementName(ComponentName componentName, AbilityInfo abilityInfo) {
        String str;
        String str2;
        if (componentName == null && abilityInfo == null) {
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

    public static Optional<Bundle> convertIntentParamsToBundle(Map<String, Object> map) {
        if (map == null) {
            return Optional.empty();
        }
        Bundle bundle = new Bundle();
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
                } else if (value instanceof Serializable) {
                    bundle.putSerializable(key, (Serializable) value);
                } else {
                    HiLog.warn(LABEL, "IntentConverter::convertIntentParamsToBundle value is other type.", new Object[0]);
                }
            }
        }
        return Optional.of(bundle);
    }

    public static Optional<IntentParams> convertBundleToIntentParams(Bundle bundle) {
        if (bundle == null) {
            return Optional.empty();
        }
        IntentParams intentParams = new IntentParams();
        Set<String> keySet = bundle.keySet();
        if (keySet != null) {
            for (String str : keySet) {
                Object obj = bundle.get(str);
                if (obj == null || (obj instanceof Serializable)) {
                    intentParams.setParam(str, obj);
                } else if (obj instanceof Bundle) {
                    intentParams.setParam(str, convertBundleToIntentParams((Bundle) obj).orElse(null));
                } else {
                    HiLog.warn(LABEL, "IntentConverter::createZidaneIntent unknown type %{public}s", new Object[]{obj.getClass().getName()});
                }
            }
        }
        return Optional.of(intentParams);
    }

    private IntentConverter() {
    }
}
