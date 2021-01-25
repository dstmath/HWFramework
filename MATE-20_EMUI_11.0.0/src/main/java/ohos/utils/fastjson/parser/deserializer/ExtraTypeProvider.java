package ohos.utils.fastjson.parser.deserializer;

import java.lang.reflect.Type;

public interface ExtraTypeProvider extends ParseProcess {
    Type getExtraType(Object obj, String str);
}
