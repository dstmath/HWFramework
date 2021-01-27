package ohos.utils.zson.annotation;

import ohos.utils.StringUtils;
import ohos.utils.fastjson.serializer.NameFilter;

public class ZSONFieldFilter implements NameFilter {
    @Override // ohos.utils.fastjson.serializer.NameFilter
    public String process(Object obj, String str, Object obj2) {
        try {
            ZSONField zSONField = (ZSONField) obj.getClass().getDeclaredField(str).getAnnotation(ZSONField.class);
            if (zSONField == null) {
                return str;
            }
            return StringUtils.isEmpty(zSONField.name()) ? str : zSONField.name();
        } catch (NoSuchFieldException | SecurityException unused) {
            return str;
        }
    }
}
