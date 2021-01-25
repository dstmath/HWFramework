package ohos.utils.fastjson.parser.deserializer;

import java.lang.reflect.Type;
import ohos.utils.fastjson.parser.DefaultJSONParser;

public interface ObjectDeserializer {
    <T> T deserialze(DefaultJSONParser defaultJSONParser, Type type, Object obj);
}
