package ohos.utils.fastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;

public interface JSONSerializable {
    void write(JSONSerializer jSONSerializer, Object obj, Type type) throws IOException;
}
