package ohos.utils.fastjson.parser;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import ohos.utils.fastjson.parser.deserializer.ObjectDeserializer;

/* access modifiers changed from: package-private */
public class JavaObjectDeserializer implements ObjectDeserializer {
    public static final JavaObjectDeserializer instance = new JavaObjectDeserializer();

    JavaObjectDeserializer() {
    }

    @Override // ohos.utils.fastjson.parser.deserializer.ObjectDeserializer
    public <T> T deserialze(DefaultJSONParser defaultJSONParser, Type type, Object obj) {
        if (!(type instanceof GenericArrayType)) {
            return (T) defaultJSONParser.parse(obj);
        }
        Type genericComponentType = ((GenericArrayType) type).getGenericComponentType();
        if (genericComponentType instanceof TypeVariable) {
            genericComponentType = ((TypeVariable) genericComponentType).getBounds()[0];
        }
        ArrayList arrayList = new ArrayList();
        defaultJSONParser.parseArray(genericComponentType, arrayList);
        if (!(genericComponentType instanceof Class)) {
            return (T) arrayList.toArray();
        }
        T t = (T) ((Object[]) Array.newInstance((Class) genericComponentType, arrayList.size()));
        arrayList.toArray(t);
        return t;
    }
}
