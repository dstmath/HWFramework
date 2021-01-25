package ohos.utils.fastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import ohos.utils.fastjson.JSONException;
import ohos.utils.fastjson.parser.DefaultJSONParser;
import ohos.utils.fastjson.parser.JSONLexer;
import ohos.utils.fastjson.parser.deserializer.ObjectDeserializer;
import ohos.utils.fastjson.util.TypeUtils;

public final class IntegerCodec implements ObjectSerializer, ObjectDeserializer {
    public static IntegerCodec instance = new IntegerCodec();

    private IntegerCodec() {
    }

    @Override // ohos.utils.fastjson.serializer.ObjectSerializer
    public void write(JSONSerializer jSONSerializer, Object obj, Object obj2, Type type) throws IOException {
        SerializeWriter serializeWriter = jSONSerializer.out;
        Number number = (Number) obj;
        if (number != null) {
            if (obj instanceof Long) {
                serializeWriter.writeLong(number.longValue());
            } else {
                serializeWriter.writeInt(number.intValue());
            }
            if ((serializeWriter.features & SerializerFeature.WriteClassName.mask) != 0) {
                Class<?> cls = number.getClass();
                if (cls == Byte.class) {
                    serializeWriter.write(66);
                } else if (cls == Short.class) {
                    serializeWriter.write(83);
                } else if (cls == Long.class) {
                    long longValue = number.longValue();
                    if (longValue <= 2147483647L && longValue >= -2147483648L && type != Long.class) {
                        serializeWriter.write(76);
                    }
                }
            }
        } else if ((serializeWriter.features & SerializerFeature.WriteNullNumberAsZero.mask) != 0) {
            serializeWriter.write(48);
        } else {
            serializeWriter.writeNull();
        }
    }

    @Override // ohos.utils.fastjson.parser.deserializer.ObjectDeserializer
    public <T> T deserialze(DefaultJSONParser defaultJSONParser, Type type, Object obj) {
        T t;
        T t2;
        JSONLexer jSONLexer = defaultJSONParser.lexer;
        int i = jSONLexer.token();
        if (i == 8) {
            jSONLexer.nextToken(16);
            return null;
        } else if (i == 2) {
            if (type == Long.TYPE || type == Long.class) {
                t2 = (T) Long.valueOf(jSONLexer.longValue());
            } else {
                try {
                    t2 = (T) Integer.valueOf(jSONLexer.intValue());
                } catch (NumberFormatException e) {
                    throw new JSONException("int value overflow, field : " + obj, e);
                }
            }
            jSONLexer.nextToken(16);
            return t2;
        } else {
            if (i == 3) {
                BigDecimal decimalValue = jSONLexer.decimalValue();
                jSONLexer.nextToken(16);
                t = (type == Long.TYPE || type == Long.class) ? (T) Long.valueOf(decimalValue.longValueExact()) : (T) Integer.valueOf(decimalValue.intValueExact());
            } else {
                Object parse = defaultJSONParser.parse();
                try {
                    if (type != Long.TYPE) {
                        if (type != Long.class) {
                            t = (T) TypeUtils.castToInt(parse);
                        }
                    }
                    t = (T) TypeUtils.castToLong(parse);
                } catch (Exception e2) {
                    throw new JSONException("cast error, field : " + obj + ", value " + parse, e2);
                }
            }
            return t;
        }
    }
}
