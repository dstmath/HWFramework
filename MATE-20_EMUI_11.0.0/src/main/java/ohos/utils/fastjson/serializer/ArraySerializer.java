package ohos.utils.fastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;

/* access modifiers changed from: package-private */
public final class ArraySerializer implements ObjectSerializer {
    private final ObjectSerializer compObjectSerializer;
    private final Class<?> componentType;

    ArraySerializer(Class<?> cls, ObjectSerializer objectSerializer) {
        this.componentType = cls;
        this.compObjectSerializer = objectSerializer;
    }

    @Override // ohos.utils.fastjson.serializer.ObjectSerializer
    public final void write(JSONSerializer jSONSerializer, Object obj, Object obj2, Type type) throws IOException {
        SerializeWriter serializeWriter = jSONSerializer.out;
        if (obj != null) {
            int i = 0;
            if (obj instanceof boolean[]) {
                boolean[] zArr = (boolean[]) obj;
                serializeWriter.write(91);
                while (i < zArr.length) {
                    if (i != 0) {
                        serializeWriter.write(44);
                    }
                    serializeWriter.write(zArr[i]);
                    i++;
                }
                serializeWriter.write(93);
            } else if (obj instanceof byte[]) {
                serializeWriter.writeByteArray((byte[]) obj);
            } else if (obj instanceof char[]) {
                serializeWriter.writeString(new String((char[]) obj));
            } else if (obj instanceof double[]) {
                double[] dArr = (double[]) obj;
                int length = dArr.length - 1;
                if (length == -1) {
                    serializeWriter.append((CharSequence) "[]");
                    return;
                }
                serializeWriter.write(91);
                while (i < length) {
                    double d = dArr[i];
                    if (Double.isNaN(d)) {
                        serializeWriter.writeNull();
                    } else {
                        serializeWriter.append((CharSequence) Double.toString(d));
                    }
                    serializeWriter.write(44);
                    i++;
                }
                double d2 = dArr[length];
                if (Double.isNaN(d2)) {
                    serializeWriter.writeNull();
                } else {
                    serializeWriter.append((CharSequence) Double.toString(d2));
                }
                serializeWriter.write(93);
            } else if (obj instanceof float[]) {
                float[] fArr = (float[]) obj;
                int length2 = fArr.length - 1;
                if (length2 == -1) {
                    serializeWriter.append((CharSequence) "[]");
                    return;
                }
                serializeWriter.write(91);
                while (i < length2) {
                    float f = fArr[i];
                    if (Float.isNaN(f)) {
                        serializeWriter.writeNull();
                    } else {
                        serializeWriter.append((CharSequence) Float.toString(f));
                    }
                    serializeWriter.write(44);
                    i++;
                }
                float f2 = fArr[length2];
                if (Float.isNaN(f2)) {
                    serializeWriter.writeNull();
                } else {
                    serializeWriter.append((CharSequence) Float.toString(f2));
                }
                serializeWriter.write(93);
            } else if (obj instanceof int[]) {
                int[] iArr = (int[]) obj;
                serializeWriter.write(91);
                while (i < iArr.length) {
                    if (i != 0) {
                        serializeWriter.write(44);
                    }
                    serializeWriter.writeInt(iArr[i]);
                    i++;
                }
                serializeWriter.write(93);
            } else if (obj instanceof long[]) {
                long[] jArr = (long[]) obj;
                serializeWriter.write(91);
                while (i < jArr.length) {
                    if (i != 0) {
                        serializeWriter.write(44);
                    }
                    serializeWriter.writeLong(jArr[i]);
                    i++;
                }
                serializeWriter.write(93);
            } else if (obj instanceof short[]) {
                short[] sArr = (short[]) obj;
                serializeWriter.write(91);
                while (i < sArr.length) {
                    if (i != 0) {
                        serializeWriter.write(44);
                    }
                    serializeWriter.writeInt(sArr[i]);
                    i++;
                }
                serializeWriter.write(93);
            } else {
                Object[] objArr = (Object[]) obj;
                int length3 = objArr.length;
                SerialContext serialContext = jSONSerializer.context;
                jSONSerializer.setContext(serialContext, obj, obj2, 0);
                try {
                    serializeWriter.write(91);
                    while (i < length3) {
                        if (i != 0) {
                            serializeWriter.write(44);
                        }
                        Object obj3 = objArr[i];
                        if (obj3 == null) {
                            if (!serializeWriter.isEnabled(SerializerFeature.WriteNullStringAsEmpty) || !(obj instanceof String[])) {
                                serializeWriter.append((CharSequence) "null");
                            } else {
                                serializeWriter.writeString("");
                            }
                        } else if (obj3.getClass() == this.componentType) {
                            this.compObjectSerializer.write(jSONSerializer, obj3, Integer.valueOf(i), null);
                        } else {
                            jSONSerializer.config.get(obj3.getClass()).write(jSONSerializer, obj3, Integer.valueOf(i), null);
                        }
                        i++;
                    }
                    serializeWriter.write(93);
                } finally {
                    jSONSerializer.context = serialContext;
                }
            }
        } else if ((serializeWriter.features & SerializerFeature.WriteNullListAsEmpty.mask) != 0) {
            serializeWriter.write("[]");
        } else {
            serializeWriter.writeNull();
        }
    }
}
