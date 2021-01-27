package ohos.utils.fastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Member;
import java.util.Collection;
import ohos.utils.fastjson.JSONException;
import ohos.utils.fastjson.annotation.JSONField;
import ohos.utils.fastjson.util.FieldInfo;

public final class FieldSerializer implements Comparable<FieldSerializer> {
    protected final int features;
    public final FieldInfo fieldInfo;
    protected final String format;
    protected char[] name_chars;
    private RuntimeSerializerInfo runtimeInfo;
    protected final boolean writeNull;

    public FieldSerializer(FieldInfo fieldInfo2) {
        boolean z;
        this.fieldInfo = fieldInfo2;
        JSONField annotation = fieldInfo2.getAnnotation();
        String str = null;
        if (annotation != null) {
            z = false;
            for (SerializerFeature serializerFeature : annotation.serialzeFeatures()) {
                if (serializerFeature == SerializerFeature.WriteMapNullValue) {
                    z = true;
                }
            }
            String trim = annotation.format().trim();
            str = trim.length() != 0 ? trim : str;
            this.features = SerializerFeature.of(annotation.serialzeFeatures());
        } else {
            this.features = 0;
            z = false;
        }
        this.writeNull = z;
        this.format = str;
        String str2 = fieldInfo2.name;
        int length = str2.length();
        this.name_chars = new char[(length + 3)];
        str2.getChars(0, str2.length(), this.name_chars, 1);
        char[] cArr = this.name_chars;
        cArr[0] = '\"';
        cArr[length + 1] = '\"';
        cArr[length + 2] = ':';
    }

    public void writePrefix(JSONSerializer jSONSerializer) throws IOException {
        SerializeWriter serializeWriter = jSONSerializer.out;
        int i = serializeWriter.features;
        if ((SerializerFeature.QuoteFieldNames.mask & i) == 0) {
            serializeWriter.writeFieldName(this.fieldInfo.name, true);
        } else if ((i & SerializerFeature.UseSingleQuotes.mask) != 0) {
            serializeWriter.writeFieldName(this.fieldInfo.name, true);
        } else {
            char[] cArr = this.name_chars;
            serializeWriter.write(cArr, 0, cArr.length);
        }
    }

    public Object getPropertyValue(Object obj) throws Exception {
        Member member;
        try {
            return this.fieldInfo.get(obj);
        } catch (Exception e) {
            if (this.fieldInfo.method != null) {
                member = this.fieldInfo.method;
            } else {
                member = this.fieldInfo.field;
            }
            throw new JSONException("get property error。 " + (member.getDeclaringClass().getName() + "." + member.getName()), e);
        }
    }

    public void writeValue(JSONSerializer jSONSerializer, Object obj) throws Exception {
        Class<?> cls;
        String str = this.format;
        if (str != null) {
            jSONSerializer.writeWithFormat(obj, str);
            return;
        }
        if (this.runtimeInfo == null) {
            if (obj == null) {
                cls = this.fieldInfo.fieldClass;
            } else {
                cls = obj.getClass();
            }
            this.runtimeInfo = new RuntimeSerializerInfo(jSONSerializer.config.get(cls), cls);
        }
        RuntimeSerializerInfo runtimeSerializerInfo = this.runtimeInfo;
        if (obj != null) {
            Class<?> cls2 = obj.getClass();
            if (cls2 == runtimeSerializerInfo.runtimeFieldClass) {
                runtimeSerializerInfo.fieldSerializer.write(jSONSerializer, obj, this.fieldInfo.name, this.fieldInfo.fieldType);
            } else {
                jSONSerializer.config.get(cls2).write(jSONSerializer, obj, this.fieldInfo.name, this.fieldInfo.fieldType);
            }
        } else if ((this.features & SerializerFeature.WriteNullNumberAsZero.mask) != 0 && Number.class.isAssignableFrom(runtimeSerializerInfo.runtimeFieldClass)) {
            jSONSerializer.out.write(48);
        } else if ((this.features & SerializerFeature.WriteNullBooleanAsFalse.mask) != 0 && Boolean.class == runtimeSerializerInfo.runtimeFieldClass) {
            jSONSerializer.out.write("false");
        } else if ((this.features & SerializerFeature.WriteNullListAsEmpty.mask) == 0 || !Collection.class.isAssignableFrom(runtimeSerializerInfo.runtimeFieldClass)) {
            runtimeSerializerInfo.fieldSerializer.write(jSONSerializer, null, this.fieldInfo.name, runtimeSerializerInfo.runtimeFieldClass);
        } else {
            jSONSerializer.out.write("[]");
        }
    }

    /* access modifiers changed from: package-private */
    public static class RuntimeSerializerInfo {
        ObjectSerializer fieldSerializer;
        Class<?> runtimeFieldClass;

        public RuntimeSerializerInfo(ObjectSerializer objectSerializer, Class<?> cls) {
            this.fieldSerializer = objectSerializer;
            this.runtimeFieldClass = cls;
        }
    }

    public int compareTo(FieldSerializer fieldSerializer) {
        return this.fieldInfo.compareTo(fieldSerializer.fieldInfo);
    }
}
