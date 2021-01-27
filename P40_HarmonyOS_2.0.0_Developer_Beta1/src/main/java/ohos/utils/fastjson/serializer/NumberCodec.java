package ohos.utils.fastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DecimalFormat;
import ohos.utils.fastjson.parser.DefaultJSONParser;
import ohos.utils.fastjson.parser.JSONLexer;
import ohos.utils.fastjson.parser.deserializer.ObjectDeserializer;
import ohos.utils.fastjson.util.TypeUtils;

public class NumberCodec implements ObjectSerializer, ObjectDeserializer {
    public static final NumberCodec instance = new NumberCodec();
    private DecimalFormat decimalFormat;

    private NumberCodec() {
        this.decimalFormat = null;
    }

    public NumberCodec(DecimalFormat decimalFormat2) {
        this.decimalFormat = null;
        this.decimalFormat = decimalFormat2;
    }

    public NumberCodec(String str) {
        this(new DecimalFormat(str));
    }

    @Override // ohos.utils.fastjson.serializer.ObjectSerializer
    public void write(JSONSerializer jSONSerializer, Object obj, Object obj2, Type type) throws IOException {
        String str;
        SerializeWriter serializeWriter = jSONSerializer.out;
        if (obj == null) {
            if ((serializeWriter.features & SerializerFeature.WriteNullNumberAsZero.mask) != 0) {
                serializeWriter.write(48);
            } else {
                serializeWriter.writeNull();
            }
        } else if (obj instanceof Float) {
            float floatValue = ((Float) obj).floatValue();
            if (Float.isNaN(floatValue)) {
                serializeWriter.writeNull();
            } else if (Float.isInfinite(floatValue)) {
                serializeWriter.writeNull();
            } else {
                String f = Float.toString(floatValue);
                if (f.endsWith(".0")) {
                    f = f.substring(0, f.length() - 2);
                }
                serializeWriter.write(f);
                if ((serializeWriter.features & SerializerFeature.WriteClassName.mask) != 0) {
                    serializeWriter.write(70);
                }
            }
        } else {
            double doubleValue = ((Double) obj).doubleValue();
            if (Double.isNaN(doubleValue)) {
                serializeWriter.writeNull();
            } else if (Double.isInfinite(doubleValue)) {
                serializeWriter.writeNull();
            } else {
                DecimalFormat decimalFormat2 = this.decimalFormat;
                if (decimalFormat2 == null) {
                    str = Double.toString(doubleValue);
                    if (str.endsWith(".0")) {
                        str = str.substring(0, str.length() - 2);
                    }
                } else {
                    str = decimalFormat2.format(doubleValue);
                }
                serializeWriter.append((CharSequence) str);
                if ((serializeWriter.features & SerializerFeature.WriteClassName.mask) != 0) {
                    serializeWriter.write(68);
                }
            }
        }
    }

    @Override // ohos.utils.fastjson.parser.deserializer.ObjectDeserializer
    public <T> T deserialze(DefaultJSONParser defaultJSONParser, Type type, Object obj) {
        JSONLexer jSONLexer = defaultJSONParser.lexer;
        int i = jSONLexer.token();
        if (i == 2) {
            if (type == Double.TYPE || type == Double.class) {
                String numberString = jSONLexer.numberString();
                jSONLexer.nextToken(16);
                return (T) Double.valueOf(Double.parseDouble(numberString));
            } else if (type == Float.TYPE || type == Float.class) {
                String numberString2 = jSONLexer.numberString();
                jSONLexer.nextToken(16);
                return (T) Float.valueOf(Float.parseFloat(numberString2));
            } else {
                long longValue = jSONLexer.longValue();
                jSONLexer.nextToken(16);
                return (type == Short.TYPE || type == Short.class) ? (T) Short.valueOf((short) ((int) longValue)) : (type == Byte.TYPE || type == Byte.class) ? (T) Byte.valueOf((byte) ((int) longValue)) : (longValue < -2147483648L || longValue > 2147483647L) ? (T) Long.valueOf(longValue) : (T) Integer.valueOf((int) longValue);
            }
        } else if (i != 3) {
            Object parse = defaultJSONParser.parse();
            if (parse == null) {
                return null;
            }
            return (type == Double.TYPE || type == Double.class) ? (T) TypeUtils.castToDouble(parse) : (type == Float.TYPE || type == Float.class) ? (T) TypeUtils.castToFloat(parse) : (type == Short.TYPE || type == Short.class) ? (T) TypeUtils.castToShort(parse) : (type == Byte.TYPE || type == Byte.class) ? (T) TypeUtils.castToByte(parse) : (T) TypeUtils.castToBigDecimal(parse);
        } else if (type == Double.TYPE || type == Double.class) {
            String numberString3 = jSONLexer.numberString();
            jSONLexer.nextToken(16);
            return (T) Double.valueOf(Double.parseDouble(numberString3));
        } else if (type == Float.TYPE || type == Float.class) {
            String numberString4 = jSONLexer.numberString();
            jSONLexer.nextToken(16);
            return (T) Float.valueOf(Float.parseFloat(numberString4));
        } else {
            T t = (T) jSONLexer.decimalValue();
            jSONLexer.nextToken(16);
            return (type == Short.TYPE || type == Short.class) ? (T) Short.valueOf(t.shortValueExact()) : (type == Byte.TYPE || type == Byte.class) ? (T) Byte.valueOf(t.byteValueExact()) : t;
        }
    }
}
