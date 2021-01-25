package ohos.utils.fastjson.parser;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import ohos.utils.fastjson.parser.DefaultJSONParser;
import ohos.utils.fastjson.parser.deserializer.FieldDeserializer;
import ohos.utils.fastjson.parser.deserializer.ObjectDeserializer;
import ohos.utils.fastjson.serializer.DateCodec;
import ohos.utils.fastjson.util.FieldInfo;
import ohos.utils.fastjson.util.ParameterizedTypeImpl;
import ohos.utils.fastjson.util.TypeUtils;

public class DefaultFieldDeserializer extends FieldDeserializer {
    protected ObjectDeserializer fieldValueDeserilizer;

    public DefaultFieldDeserializer(ParserConfig parserConfig, Class<?> cls, FieldInfo fieldInfo) {
        super(cls, fieldInfo, 2);
    }

    public ObjectDeserializer getFieldValueDeserilizer(ParserConfig parserConfig) {
        if (this.fieldValueDeserilizer == null) {
            this.fieldValueDeserilizer = parserConfig.getDeserializer(this.fieldInfo.fieldClass, this.fieldInfo.fieldType);
        }
        return this.fieldValueDeserilizer;
    }

    /* JADX WARNING: Removed duplicated region for block: B:25:0x008a  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0098  */
    @Override // ohos.utils.fastjson.parser.deserializer.FieldDeserializer
    public void parseField(DefaultJSONParser defaultJSONParser, Object obj, Type type, Map<String, Object> map) {
        Object obj2;
        Class<?> cls;
        if (this.fieldValueDeserilizer == null) {
            this.fieldValueDeserilizer = defaultJSONParser.config.getDeserializer(this.fieldInfo.fieldClass, this.fieldInfo.fieldType);
        }
        Type type2 = this.fieldInfo.fieldType;
        boolean z = type instanceof ParameterizedType;
        if (z) {
            ParseContext parseContext = defaultJSONParser.contex;
            if (parseContext != null) {
                parseContext.type = type;
            }
            type2 = FieldInfo.getFieldType(this.clazz, type, type2);
            this.fieldValueDeserilizer = defaultJSONParser.config.getDeserializer(type2);
        }
        if ((type2 instanceof ParameterizedType) && z) {
            ParameterizedType parameterizedType = (ParameterizedType) type2;
            ParameterizedType parameterizedType2 = (ParameterizedType) type;
            Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            Type rawType = parameterizedType2.getRawType();
            if ((rawType instanceof Class) && TypeUtils.getArgument(actualTypeArguments, ((Class) rawType).getTypeParameters(), parameterizedType2.getActualTypeArguments())) {
                type2 = new ParameterizedTypeImpl(actualTypeArguments, parameterizedType.getOwnerType(), parameterizedType.getRawType());
            }
        }
        String str = this.fieldInfo.format;
        if (str != null) {
            ObjectDeserializer objectDeserializer = this.fieldValueDeserilizer;
            if (objectDeserializer instanceof DateCodec) {
                obj2 = ((DateCodec) objectDeserializer).deserialze(defaultJSONParser, type2, this.fieldInfo.name, str);
                if (defaultJSONParser.resolveStatus != 1) {
                    DefaultJSONParser.ResolveTask lastResolveTask = defaultJSONParser.getLastResolveTask();
                    lastResolveTask.fieldDeserializer = this;
                    lastResolveTask.ownerContext = defaultJSONParser.contex;
                    defaultJSONParser.resolveStatus = 0;
                    return;
                } else if (obj == null) {
                    map.put(this.fieldInfo.name, obj2);
                    return;
                } else if (obj2 != null || ((cls = this.fieldInfo.fieldClass) != Byte.TYPE && cls != Short.TYPE && cls != Float.TYPE && cls != Double.TYPE)) {
                    setValue(obj, obj2);
                    return;
                } else {
                    return;
                }
            }
        }
        obj2 = this.fieldValueDeserilizer.deserialze(defaultJSONParser, type2, this.fieldInfo.name);
        if (defaultJSONParser.resolveStatus != 1) {
        }
    }
}
