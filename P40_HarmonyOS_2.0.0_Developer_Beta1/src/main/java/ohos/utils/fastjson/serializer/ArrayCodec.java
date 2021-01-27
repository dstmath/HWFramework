package ohos.utils.fastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Type;
import ohos.utils.fastjson.JSON;
import ohos.utils.fastjson.JSONArray;
import ohos.utils.fastjson.parser.DefaultJSONParser;
import ohos.utils.fastjson.parser.JSONLexer;
import ohos.utils.fastjson.parser.deserializer.ObjectDeserializer;
import ohos.utils.fastjson.util.TypeUtils;

public final class ArrayCodec implements ObjectSerializer, ObjectDeserializer {
    public static final ArrayCodec instance = new ArrayCodec();

    private ArrayCodec() {
    }

    @Override // ohos.utils.fastjson.serializer.ObjectSerializer
    public final void write(JSONSerializer jSONSerializer, Object obj, Object obj2, Type type) throws IOException {
        SerializeWriter serializeWriter = jSONSerializer.out;
        Object[] objArr = (Object[]) obj;
        if (obj != null) {
            int length = objArr.length;
            int i = length - 1;
            if (i == -1) {
                serializeWriter.append((CharSequence) "[]");
                return;
            }
            SerialContext serialContext = jSONSerializer.context;
            int i2 = 0;
            jSONSerializer.setContext(serialContext, obj, obj2, 0);
            try {
                serializeWriter.write(91);
                if ((serializeWriter.features & SerializerFeature.PrettyFormat.mask) != 0) {
                    jSONSerializer.incrementIndent();
                    jSONSerializer.println();
                    while (i2 < length) {
                        if (i2 != 0) {
                            serializeWriter.write(44);
                            jSONSerializer.println();
                        }
                        jSONSerializer.write(objArr[i2]);
                        i2++;
                    }
                    jSONSerializer.decrementIdent();
                    jSONSerializer.println();
                    serializeWriter.write(93);
                    return;
                }
                Class<?> cls = null;
                ObjectSerializer objectSerializer = null;
                while (i2 < i) {
                    Object obj3 = objArr[i2];
                    if (obj3 == null) {
                        serializeWriter.append((CharSequence) "null,");
                    } else {
                        if (jSONSerializer.references == null || !jSONSerializer.references.containsKey(obj3)) {
                            Class<?> cls2 = obj3.getClass();
                            if (cls2 == cls) {
                                objectSerializer.write(jSONSerializer, obj3, null, null);
                            } else {
                                objectSerializer = jSONSerializer.config.get(cls2);
                                objectSerializer.write(jSONSerializer, obj3, null, null);
                                cls = cls2;
                            }
                        } else {
                            jSONSerializer.writeReference(obj3);
                        }
                        serializeWriter.write(44);
                    }
                    i2++;
                }
                Object obj4 = objArr[i];
                if (obj4 == null) {
                    serializeWriter.append((CharSequence) "null]");
                } else {
                    if (jSONSerializer.references == null || !jSONSerializer.references.containsKey(obj4)) {
                        jSONSerializer.writeWithFieldName(obj4, Integer.valueOf(i));
                    } else {
                        jSONSerializer.writeReference(obj4);
                    }
                    serializeWriter.write(93);
                }
                jSONSerializer.context = serialContext;
            } finally {
                jSONSerializer.context = serialContext;
            }
        } else if ((serializeWriter.features & SerializerFeature.WriteNullListAsEmpty.mask) != 0) {
            serializeWriter.write("[]");
        } else {
            serializeWriter.writeNull();
        }
    }

    @Override // ohos.utils.fastjson.parser.deserializer.ObjectDeserializer
    public <T> T deserialze(DefaultJSONParser defaultJSONParser, Type type, Object obj) {
        JSONLexer jSONLexer = defaultJSONParser.lexer;
        int i = jSONLexer.token();
        if (i == 8) {
            jSONLexer.nextToken(16);
            return null;
        } else if (type == char[].class) {
            if (i == 4) {
                String stringVal = jSONLexer.stringVal();
                jSONLexer.nextToken(16);
                return (T) stringVal.toCharArray();
            } else if (i != 2) {
                return (T) JSON.toJSONString(defaultJSONParser.parse()).toCharArray();
            } else {
                Number integerValue = jSONLexer.integerValue();
                jSONLexer.nextToken(16);
                return (T) integerValue.toString().toCharArray();
            }
        } else if (i == 4) {
            T t = (T) jSONLexer.bytesValue();
            jSONLexer.nextToken(16);
            return t;
        } else {
            Class<?> componentType = ((Class) type).getComponentType();
            JSONArray jSONArray = new JSONArray();
            defaultJSONParser.parseArray(componentType, jSONArray, obj);
            return (T) toObjectArray(defaultJSONParser, componentType, jSONArray);
        }
    }

    private <T> T toObjectArray(DefaultJSONParser defaultJSONParser, Class<?> cls, JSONArray jSONArray) {
        if (jSONArray == null) {
            return null;
        }
        int size = jSONArray.size();
        T t = (T) Array.newInstance(cls, size);
        for (int i = 0; i < size; i++) {
            Object obj = jSONArray.get(i);
            if (obj == jSONArray) {
                Array.set(t, i, t);
            } else {
                if (!cls.isArray()) {
                    obj = TypeUtils.cast(obj, (Class<Object>) cls, defaultJSONParser.config);
                } else if (!cls.isInstance(obj)) {
                    obj = toObjectArray(defaultJSONParser, cls, (JSONArray) obj);
                }
                Array.set(t, i, obj);
            }
        }
        jSONArray.setRelatedArray(t);
        jSONArray.setComponentType(cls);
        return t;
    }
}
