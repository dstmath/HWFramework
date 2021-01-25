package ohos.utils.fastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;
import ohos.utils.fastjson.parser.DefaultJSONParser;
import ohos.utils.fastjson.parser.JSONLexer;
import ohos.utils.fastjson.parser.deserializer.ObjectDeserializer;
import ohos.utils.fastjson.util.TypeUtils;

public final class BooleanCodec implements ObjectSerializer, ObjectDeserializer {
    public static final BooleanCodec instance = new BooleanCodec();

    private BooleanCodec() {
    }

    @Override // ohos.utils.fastjson.serializer.ObjectSerializer
    public void write(JSONSerializer jSONSerializer, Object obj, Object obj2, Type type) throws IOException {
        SerializeWriter serializeWriter = jSONSerializer.out;
        Boolean bool = (Boolean) obj;
        if (bool == null) {
            if ((serializeWriter.features & SerializerFeature.WriteNullBooleanAsFalse.mask) != 0) {
                serializeWriter.write("false");
            } else {
                serializeWriter.writeNull();
            }
        } else if (bool.booleanValue()) {
            serializeWriter.write("true");
        } else {
            serializeWriter.write("false");
        }
    }

    @Override // ohos.utils.fastjson.parser.deserializer.ObjectDeserializer
    public <T> T deserialze(DefaultJSONParser defaultJSONParser, Type type, Object obj) {
        JSONLexer jSONLexer = defaultJSONParser.lexer;
        int i = jSONLexer.token();
        if (i == 6) {
            jSONLexer.nextToken(16);
            return (T) Boolean.TRUE;
        } else if (i == 7) {
            jSONLexer.nextToken(16);
            return (T) Boolean.FALSE;
        } else if (i == 2) {
            int intValue = jSONLexer.intValue();
            jSONLexer.nextToken(16);
            return intValue == 1 ? (T) Boolean.TRUE : (T) Boolean.FALSE;
        } else {
            Object parse = defaultJSONParser.parse();
            if (parse == null) {
                return null;
            }
            return (T) TypeUtils.castToBoolean(parse);
        }
    }
}
