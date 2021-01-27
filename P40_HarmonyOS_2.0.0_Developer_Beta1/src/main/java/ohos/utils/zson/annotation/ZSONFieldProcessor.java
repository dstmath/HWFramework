package ohos.utils.zson.annotation;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import ohos.utils.fastjson.parser.deserializer.ExtraProcessor;

public class ZSONFieldProcessor implements ExtraProcessor {
    private Map<String, Field> zsonFields = new HashMap();

    public ZSONFieldProcessor(Class<?> cls) {
        Field[] declaredFields = cls.getDeclaredFields();
        for (Field field : declaredFields) {
            ZSONField zSONField = (ZSONField) field.getDeclaredAnnotation(ZSONField.class);
            if (zSONField != null) {
                this.zsonFields.put(zSONField.name(), field);
            }
        }
    }

    @Override // ohos.utils.fastjson.parser.deserializer.ExtraProcessor
    public void processExtra(Object obj, String str, Object obj2) {
        if (this.zsonFields.containsKey(str)) {
            Field field = this.zsonFields.get(str);
            try {
                field.setAccessible(true);
                field.set(obj, obj2);
            } catch (IllegalAccessException | IllegalArgumentException unused) {
            }
        }
    }
}
