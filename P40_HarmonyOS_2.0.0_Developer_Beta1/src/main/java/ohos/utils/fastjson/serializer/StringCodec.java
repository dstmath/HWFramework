package ohos.utils.fastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;
import ohos.utils.fastjson.parser.DefaultJSONParser;
import ohos.utils.fastjson.parser.deserializer.ObjectDeserializer;

public class StringCodec implements ObjectSerializer, ObjectDeserializer {
    public static StringCodec instance = new StringCodec();

    private StringCodec() {
    }

    @Override // ohos.utils.fastjson.serializer.ObjectSerializer
    public void write(JSONSerializer jSONSerializer, Object obj, Object obj2, Type type) throws IOException {
        String str = (String) obj;
        SerializeWriter serializeWriter = jSONSerializer.out;
        if (str == null) {
            serializeWriter.writeNull();
        } else {
            serializeWriter.writeString(str);
        }
    }

    @Override // ohos.utils.fastjson.parser.deserializer.ObjectDeserializer
    public <T> T deserialze(DefaultJSONParser defaultJSONParser, Type type, Object obj) {
        return (T) defaultJSONParser.parseString();
    }
}
