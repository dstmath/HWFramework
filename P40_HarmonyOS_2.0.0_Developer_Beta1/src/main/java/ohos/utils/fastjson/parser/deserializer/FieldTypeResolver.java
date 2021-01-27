package ohos.utils.fastjson.parser.deserializer;

import java.lang.reflect.Type;

public interface FieldTypeResolver extends ParseProcess {
    Type resolve(Object obj, String str);
}
