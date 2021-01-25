package ohos.utils.fastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import ohos.utils.fastjson.parser.DefaultJSONParser;
import ohos.utils.fastjson.parser.JSONLexer;
import ohos.utils.fastjson.parser.deserializer.ObjectDeserializer;
import ohos.utils.fastjson.util.TypeUtils;

public class BigDecimalCodec implements ObjectSerializer, ObjectDeserializer {
    public static final BigDecimalCodec instance = new BigDecimalCodec();

    private BigDecimalCodec() {
    }

    @Override // ohos.utils.fastjson.serializer.ObjectSerializer
    public void write(JSONSerializer jSONSerializer, Object obj, Object obj2, Type type) throws IOException {
        SerializeWriter serializeWriter = jSONSerializer.out;
        if (obj == null) {
            if ((serializeWriter.features & SerializerFeature.WriteNullNumberAsZero.mask) != 0) {
                serializeWriter.write(48);
            } else {
                serializeWriter.writeNull();
            }
        } else if (obj instanceof BigInteger) {
            serializeWriter.write(((BigInteger) obj).toString());
        } else {
            BigDecimal bigDecimal = (BigDecimal) obj;
            serializeWriter.write(bigDecimal.toString());
            if ((serializeWriter.features & SerializerFeature.WriteClassName.mask) != 0 && type != BigDecimal.class && bigDecimal.scale() == 0) {
                serializeWriter.write(46);
            }
        }
    }

    @Override // ohos.utils.fastjson.parser.deserializer.ObjectDeserializer
    public <T> T deserialze(DefaultJSONParser defaultJSONParser, Type type, Object obj) {
        JSONLexer jSONLexer = defaultJSONParser.lexer;
        int i = jSONLexer.token();
        if (i == 2) {
            if (type == BigInteger.class) {
                String numberString = jSONLexer.numberString();
                jSONLexer.nextToken(16);
                return (T) new BigInteger(numberString, 10);
            }
            T t = (T) jSONLexer.decimalValue();
            jSONLexer.nextToken(16);
            return t;
        } else if (i == 3) {
            T t2 = (T) jSONLexer.decimalValue();
            jSONLexer.nextToken(16);
            if (type != BigInteger.class) {
                return t2;
            }
            int scale = t2.scale();
            if (scale >= -100 && scale <= 100) {
                return (T) t2.toBigInteger();
            }
            throw new NumberFormatException();
        } else {
            Object parse = defaultJSONParser.parse();
            if (parse == null) {
                return null;
            }
            return type == BigInteger.class ? (T) TypeUtils.castToBigInteger(parse) : (T) TypeUtils.castToBigDecimal(parse);
        }
    }
}
