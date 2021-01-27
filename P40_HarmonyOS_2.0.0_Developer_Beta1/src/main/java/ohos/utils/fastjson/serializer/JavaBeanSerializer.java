package ohos.utils.fastjson.serializer;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import ohos.global.icu.util.ULocale;
import ohos.telephony.TelephoneNumberUtils;
import ohos.utils.fastjson.JSONException;
import ohos.utils.fastjson.PropertyNamingStrategy;
import ohos.utils.fastjson.annotation.JSONType;
import ohos.utils.fastjson.util.FieldInfo;
import ohos.utils.fastjson.util.TypeUtils;

public class JavaBeanSerializer implements ObjectSerializer {
    private static final char[] false_chars = {'f', 'a', 'l', 's', 'e'};
    private static final char[] true_chars = {'t', 'r', ULocale.UNICODE_LOCALE_EXTENSION, 'e'};
    protected int features;
    private final FieldSerializer[] getters;
    private final FieldSerializer[] sortedGetters;
    protected final String typeKey;
    protected final String typeName;

    public JavaBeanSerializer(Class<?> cls) {
        this(cls, (PropertyNamingStrategy) null);
    }

    public JavaBeanSerializer(Class<?> cls, PropertyNamingStrategy propertyNamingStrategy) {
        this(cls, cls.getModifiers(), null, false, true, true, true, propertyNamingStrategy);
    }

    public JavaBeanSerializer(Class<?> cls, String... strArr) {
        this(cls, cls.getModifiers(), map(strArr), false, true, true, true, null);
    }

    private static Map<String, String> map(String... strArr) {
        HashMap hashMap = new HashMap();
        for (String str : strArr) {
            hashMap.put(str, str);
        }
        return hashMap;
    }

    public JavaBeanSerializer(Class<?> cls, int i, Map<String, String> map, boolean z, boolean z2, boolean z3, boolean z4, PropertyNamingStrategy propertyNamingStrategy) {
        PropertyNamingStrategy propertyNamingStrategy2;
        String str;
        String str2;
        PropertyNamingStrategy naming;
        this.features = 0;
        String[] strArr = null;
        JSONType jSONType = z2 ? (JSONType) cls.getAnnotation(JSONType.class) : null;
        if (jSONType != null) {
            this.features = SerializerFeature.of(jSONType.serialzeFeatures());
            str2 = jSONType.typeName();
            if (str2.length() == 0) {
                str2 = null;
                str = null;
            } else {
                Class<? super Object> superclass = cls.getSuperclass();
                String str3 = null;
                while (superclass != null && superclass != Object.class) {
                    JSONType jSONType2 = (JSONType) superclass.getAnnotation(JSONType.class);
                    if (jSONType2 == null) {
                        break;
                    }
                    str3 = jSONType2.typeKey();
                    if (str3.length() != 0) {
                        break;
                    }
                    superclass = superclass.getSuperclass();
                }
                str = str3;
                for (Class<?> cls2 : cls.getInterfaces()) {
                    JSONType jSONType3 = (JSONType) cls2.getAnnotation(JSONType.class);
                    if (jSONType3 != null) {
                        str = jSONType3.typeKey();
                        if (str.length() != 0) {
                            break;
                        }
                    }
                }
                if (str != null && str.length() == 0) {
                    str = null;
                }
            }
            propertyNamingStrategy2 = (propertyNamingStrategy != null || (naming = jSONType.naming()) == PropertyNamingStrategy.CamelCase) ? propertyNamingStrategy : naming;
        } else {
            propertyNamingStrategy2 = propertyNamingStrategy;
            str2 = null;
            str = null;
        }
        this.typeName = str2;
        this.typeKey = str;
        List<FieldInfo> computeGetters = TypeUtils.computeGetters(cls, i, z, jSONType, map, false, z3, z4, propertyNamingStrategy2);
        ArrayList arrayList = new ArrayList();
        for (FieldInfo fieldInfo : computeGetters) {
            arrayList.add(new FieldSerializer(fieldInfo));
        }
        this.getters = (FieldSerializer[]) arrayList.toArray(new FieldSerializer[arrayList.size()]);
        strArr = jSONType != null ? jSONType.orders() : strArr;
        if (strArr == null || strArr.length == 0) {
            FieldSerializer[] fieldSerializerArr = this.getters;
            FieldSerializer[] fieldSerializerArr2 = new FieldSerializer[fieldSerializerArr.length];
            System.arraycopy(fieldSerializerArr, 0, fieldSerializerArr2, 0, fieldSerializerArr.length);
            Arrays.sort(fieldSerializerArr2);
            if (Arrays.equals(fieldSerializerArr2, this.getters)) {
                this.sortedGetters = this.getters;
            } else {
                this.sortedGetters = fieldSerializerArr2;
            }
        } else {
            List<FieldInfo> computeGetters2 = TypeUtils.computeGetters(cls, i, z, jSONType, map, true, z3, z4, propertyNamingStrategy2);
            ArrayList arrayList2 = new ArrayList();
            for (FieldInfo fieldInfo2 : computeGetters2) {
                arrayList2.add(new FieldSerializer(fieldInfo2));
            }
            this.sortedGetters = (FieldSerializer[]) arrayList2.toArray(new FieldSerializer[arrayList2.size()]);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:225:0x0337, code lost:
        if ((r7 & r6.features) == 0) goto L_0x0423;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:275:0x03db, code lost:
        if (r6.isEnabled(ohos.utils.fastjson.serializer.SerializerFeature.WriteMapNullValue) == false) goto L_0x0423;
     */
    /* JADX WARNING: Removed duplicated region for block: B:150:0x0223 A[Catch:{ Exception -> 0x05d6, all -> 0x05d0 }] */
    /* JADX WARNING: Removed duplicated region for block: B:166:0x0254 A[Catch:{ Exception -> 0x05d6, all -> 0x05d0 }] */
    /* JADX WARNING: Removed duplicated region for block: B:171:0x0270 A[Catch:{ Exception -> 0x05d6, all -> 0x05d0 }] */
    /* JADX WARNING: Removed duplicated region for block: B:174:0x0277 A[Catch:{ Exception -> 0x05d6, all -> 0x05d0 }] */
    /* JADX WARNING: Removed duplicated region for block: B:175:0x027d A[Catch:{ Exception -> 0x05d6, all -> 0x05d0 }] */
    /* JADX WARNING: Removed duplicated region for block: B:192:0x02af A[Catch:{ Exception -> 0x05d6, all -> 0x05d0 }, LOOP:4: B:190:0x02a9->B:192:0x02af, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:362:0x04fc  */
    /* JADX WARNING: Removed duplicated region for block: B:442:0x0651 A[SYNTHETIC, Splitter:B:442:0x0651] */
    @Override // ohos.utils.fastjson.serializer.ObjectSerializer
    public void write(JSONSerializer jSONSerializer, Object obj, Object obj2, Type type) throws IOException {
        FieldSerializer[] fieldSerializerArr;
        Throwable th;
        SerialContext serialContext;
        Exception exc;
        Exception e;
        boolean z;
        List<ValueFilter> list;
        List<PropertyFilter> list2;
        List<NameFilter> list3;
        List<PropertyPreFilter> list4;
        boolean z2;
        boolean z3;
        int i;
        boolean z4;
        int i2;
        boolean z5;
        boolean z6;
        String str;
        Object obj3;
        String str2;
        FieldInfo fieldInfo;
        boolean z7;
        int i3;
        int i4;
        int i5;
        Object obj4;
        Iterator<NameFilter> it;
        Object obj5;
        Object valueOf;
        Object obj6;
        Object obj7 = obj;
        SerializeWriter serializeWriter = jSONSerializer.out;
        if (obj7 == null) {
            serializeWriter.writeNull();
        } else if ((jSONSerializer.context == null || (jSONSerializer.context.features & SerializerFeature.DisableCircularReferenceDetect.mask) == 0) && jSONSerializer.references != null && jSONSerializer.references.containsKey(obj7)) {
            jSONSerializer.writeReference(obj);
        } else {
            if ((serializeWriter.features & SerializerFeature.SortField.mask) != 0) {
                fieldSerializerArr = this.sortedGetters;
            } else {
                fieldSerializerArr = this.getters;
            }
            SerialContext serialContext2 = jSONSerializer.context;
            if ((serializeWriter.features & SerializerFeature.DisableCircularReferenceDetect.mask) == 0) {
                jSONSerializer.context = new SerialContext(serialContext2, obj7, obj2, this.features);
                if (jSONSerializer.references == null) {
                    jSONSerializer.references = new IdentityHashMap<>();
                }
                jSONSerializer.references.put(obj7, jSONSerializer.context);
            }
            boolean z8 = ((this.features & SerializerFeature.BeanToArray.mask) == 0 && (serializeWriter.features & SerializerFeature.BeanToArray.mask) == 0) ? false : true;
            char c = z8 ? '[' : '{';
            char c2 = z8 ? ']' : '}';
            try {
                int i6 = serializeWriter.count + 1;
                if (i6 > serializeWriter.buf.length) {
                    try {
                        if (serializeWriter.writer == null) {
                            serializeWriter.expandCapacity(i6);
                        } else {
                            serializeWriter.flush();
                            i6 = 1;
                        }
                    } catch (Exception e2) {
                        exc = e2;
                        serialContext = serialContext2;
                        String str3 = "write javaBean error, fastjson version 1.1.71";
                        if (obj2 != null) {
                        }
                        throw new JSONException(str3, exc);
                    } catch (Throwable th2) {
                        th = th2;
                        serialContext = serialContext2;
                        jSONSerializer.context = serialContext;
                        throw th;
                    }
                }
                serializeWriter.buf[serializeWriter.count] = c;
                serializeWriter.count = i6;
                if (fieldSerializerArr.length > 0 && (serializeWriter.features & SerializerFeature.PrettyFormat.mask) != 0) {
                    jSONSerializer.incrementIndent();
                    jSONSerializer.println();
                }
                if (!(((this.features & SerializerFeature.WriteClassName.mask) == 0 && ((serializeWriter.features & SerializerFeature.WriteClassName.mask) == 0 || (type == null && (serializeWriter.features & SerializerFeature.NotWriteRootClassName.mask) != 0 && (jSONSerializer.context == null || jSONSerializer.context.parent == null)))) ? false : true) || obj.getClass() == type) {
                    z = false;
                } else {
                    serializeWriter.writeFieldName(this.typeKey != null ? this.typeKey : jSONSerializer.config.typeKey, false);
                    String str4 = this.typeName;
                    if (str4 == null) {
                        str4 = obj.getClass().getName();
                    }
                    jSONSerializer.write(str4);
                    z = true;
                }
                char c3 = z ? ',' : 0;
                if (jSONSerializer.beforeFilters != null) {
                    for (BeforeFilter beforeFilter : jSONSerializer.beforeFilters) {
                        c3 = beforeFilter.writeBefore(jSONSerializer, obj7, c3);
                    }
                }
                boolean z9 = c3 == ',';
                boolean z10 = (serializeWriter.features & SerializerFeature.QuoteFieldNames.mask) != 0 && (serializeWriter.features & SerializerFeature.UseSingleQuotes.mask) == 0;
                boolean z11 = (serializeWriter.features & SerializerFeature.UseSingleQuotes.mask) != 0;
                boolean z12 = (SerializerFeature.NotWriteDefaultValue.mask & serializeWriter.features) != 0;
                List<PropertyFilter> list5 = jSONSerializer.propertyFilters;
                List<NameFilter> list6 = jSONSerializer.nameFilters;
                boolean z13 = z9;
                List<ValueFilter> list7 = jSONSerializer.valueFilters;
                List<PropertyPreFilter> list8 = jSONSerializer.propertyPreFilters;
                int i7 = 0;
                while (i7 < fieldSerializerArr.length) {
                    try {
                        try {
                            FieldSerializer fieldSerializer = fieldSerializerArr[i7];
                            FieldInfo fieldInfo2 = fieldSerializer.fieldInfo;
                            Class<?> cls = fieldInfo2.fieldClass;
                            String str5 = fieldInfo2.name;
                            if ((SerializerFeature.SkipTransientField.mask & serializeWriter.features) == 0 || fieldInfo2.field == null || !fieldInfo2.fieldTransient) {
                                if (this.typeKey == null || !this.typeKey.equals(str5)) {
                                    if (list8 != null) {
                                        Iterator<PropertyPreFilter> it2 = list8.iterator();
                                        while (true) {
                                            if (it2.hasNext()) {
                                                if (!it2.next().apply(jSONSerializer, obj7, str5)) {
                                                    z2 = false;
                                                    break;
                                                }
                                            } else {
                                                break;
                                            }
                                        }
                                    }
                                    z2 = true;
                                    if (z2) {
                                        long j = 0;
                                        if (fieldInfo2.fieldAccess) {
                                            if (cls == Integer.TYPE) {
                                                i2 = fieldInfo2.field.getInt(obj7);
                                                i = null;
                                                z5 = true;
                                            } else if (cls == Long.TYPE) {
                                                j = fieldInfo2.field.getLong(obj7);
                                                i = null;
                                                z5 = true;
                                                i2 = 0;
                                            } else if (cls == Boolean.TYPE) {
                                                i = null;
                                                z4 = fieldInfo2.field.getBoolean(obj7);
                                                z5 = true;
                                                i2 = 0;
                                                z3 = false;
                                                if (list5 != null) {
                                                    if (z5) {
                                                        list4 = list8;
                                                        if (cls == Integer.TYPE) {
                                                            valueOf = Integer.valueOf(i2);
                                                        } else if (cls == Long.TYPE) {
                                                            valueOf = Long.valueOf(j);
                                                        } else if (cls == Boolean.TYPE) {
                                                            valueOf = Boolean.valueOf(z4);
                                                        }
                                                        obj5 = valueOf;
                                                        z3 = true;
                                                        for (PropertyFilter propertyFilter : list5) {
                                                            list2 = list5;
                                                            if (!propertyFilter.apply(obj7, str5, obj5)) {
                                                                i = obj5;
                                                                z6 = false;
                                                                break;
                                                            }
                                                            list5 = list2;
                                                        }
                                                        list2 = list5;
                                                        i = obj5;
                                                    } else {
                                                        list4 = list8;
                                                    }
                                                    obj5 = i;
                                                    while (r27.hasNext()) {
                                                    }
                                                    list2 = list5;
                                                    i = obj5;
                                                } else {
                                                    list4 = list8;
                                                    list2 = list5;
                                                }
                                                z6 = true;
                                                if (!z6) {
                                                    list = list7;
                                                    list3 = list6;
                                                } else {
                                                    if (list6 != null) {
                                                        if (z5 && !z3) {
                                                            if (cls == Integer.TYPE) {
                                                                obj4 = Integer.valueOf(i2);
                                                            } else if (cls == Long.TYPE) {
                                                                obj4 = Long.valueOf(j);
                                                            } else if (cls == Boolean.TYPE) {
                                                                obj4 = Boolean.valueOf(z4);
                                                            }
                                                            z3 = true;
                                                            list3 = list6;
                                                            str = str5;
                                                            for (it = list6.iterator(); it.hasNext(); it = it) {
                                                                str = it.next().process(obj7, str, obj4);
                                                            }
                                                            i = obj4;
                                                        }
                                                        obj4 = i;
                                                        list3 = list6;
                                                        str = str5;
                                                        while (it.hasNext()) {
                                                        }
                                                        i = obj4;
                                                    } else {
                                                        list3 = list6;
                                                        str = str5;
                                                    }
                                                    if (list7 != null) {
                                                        if (z5 && !z3) {
                                                            if (cls == Integer.TYPE) {
                                                                i = Integer.valueOf(i2);
                                                            } else if (cls == Long.TYPE) {
                                                                i = Long.valueOf(j);
                                                            } else if (cls == Boolean.TYPE) {
                                                                i = Boolean.valueOf(z4);
                                                            }
                                                            z3 = true;
                                                        }
                                                        Object obj8 = i;
                                                        for (Iterator<ValueFilter> it3 = list7.iterator(); it3.hasNext(); it3 = it3) {
                                                            obj8 = it3.next().process(obj7, str5, obj8);
                                                        }
                                                        obj3 = i;
                                                        i = obj8;
                                                    } else {
                                                        obj3 = i;
                                                    }
                                                    if (!z3 || i != null) {
                                                        list = list7;
                                                        fieldInfo = fieldInfo2;
                                                        str2 = "";
                                                    } else {
                                                        list = list7;
                                                        int i8 = this.features | fieldInfo2.serialzeFeatures | serializeWriter.features;
                                                        if (cls == Boolean.class) {
                                                            int i9 = SerializerFeature.WriteNullBooleanAsFalse.mask;
                                                            fieldInfo = fieldInfo2;
                                                            int i10 = SerializerFeature.WriteMapNullValue.mask | i9;
                                                            str2 = (z8 || (i8 & i10) != 0) ? "" : "";
                                                            if (!((i8 & i9) == 0 && (serializeWriter.features & i9) == 0)) {
                                                                i = false;
                                                            }
                                                        } else {
                                                            fieldInfo = fieldInfo2;
                                                            str2 = "";
                                                            if (cls == String.class) {
                                                                int i11 = SerializerFeature.WriteNullStringAsEmpty.mask;
                                                                int i12 = SerializerFeature.WriteMapNullValue.mask | i11;
                                                                if (!(!z8 && (i8 & i12) == 0 && (i12 & serializeWriter.features) == 0)) {
                                                                    if (!((i8 & i11) == 0 && (serializeWriter.features & i11) == 0)) {
                                                                        i = str2;
                                                                    }
                                                                }
                                                            } else if (Number.class.isAssignableFrom(cls)) {
                                                                int i13 = SerializerFeature.WriteNullNumberAsZero.mask;
                                                                int i14 = SerializerFeature.WriteMapNullValue.mask | i13;
                                                                if (!(!z8 && (i8 & i14) == 0 && (i14 & serializeWriter.features) == 0)) {
                                                                    if (!((i8 & i13) == 0 && (serializeWriter.features & i13) == 0)) {
                                                                        i = 0;
                                                                    }
                                                                }
                                                            } else if (Collection.class.isAssignableFrom(cls)) {
                                                                int i15 = SerializerFeature.WriteNullListAsEmpty.mask;
                                                                int i16 = SerializerFeature.WriteMapNullValue.mask | i15;
                                                                if (!(!z8 && (i8 & i16) == 0 && (i16 & serializeWriter.features) == 0)) {
                                                                    if (!((i8 & i15) == 0 && (serializeWriter.features & i15) == 0)) {
                                                                        i = Collections.emptyList();
                                                                    }
                                                                }
                                                            } else if (!z8) {
                                                                if (!fieldSerializer.writeNull) {
                                                                }
                                                            }
                                                        }
                                                    }
                                                    if (!z3 || i == null || !z12 || (!((cls == Byte.TYPE || cls == Short.TYPE || cls == Integer.TYPE || cls == Long.TYPE || cls == Float.TYPE || cls == Double.TYPE) && (i instanceof Number) && ((Number) i).byteValue() == 0) && (cls != Boolean.TYPE || !(i instanceof Boolean) || ((Boolean) i).booleanValue()))) {
                                                        if (z13) {
                                                            int i17 = serializeWriter.count + 1;
                                                            if (i17 > serializeWriter.buf.length) {
                                                                if (serializeWriter.writer == null) {
                                                                    serializeWriter.expandCapacity(i17);
                                                                } else {
                                                                    serializeWriter.flush();
                                                                    i17 = 1;
                                                                }
                                                            }
                                                            serializeWriter.buf[serializeWriter.count] = TelephoneNumberUtils.PAUSE;
                                                            serializeWriter.count = i17;
                                                            if ((serializeWriter.features & SerializerFeature.PrettyFormat.mask) != 0) {
                                                                jSONSerializer.println();
                                                            }
                                                        }
                                                        if (str != str5) {
                                                            if (!z8) {
                                                                serializeWriter.writeFieldName(str, true);
                                                            }
                                                            jSONSerializer.write(i);
                                                        } else if (obj3 != i) {
                                                            if (!z8) {
                                                                fieldSerializer.writePrefix(jSONSerializer);
                                                            }
                                                            jSONSerializer.write(i);
                                                        } else {
                                                            if (!z8) {
                                                                if (z10) {
                                                                    char[] cArr = fieldSerializer.name_chars;
                                                                    int length = cArr.length;
                                                                    int i18 = serializeWriter.count + length;
                                                                    if (i18 > serializeWriter.buf.length) {
                                                                        if (serializeWriter.writer == null) {
                                                                            serializeWriter.expandCapacity(i18);
                                                                        } else {
                                                                            i4 = length;
                                                                            i5 = 0;
                                                                            do {
                                                                                int length2 = serializeWriter.buf.length - serializeWriter.count;
                                                                                System.arraycopy(cArr, i5, serializeWriter.buf, serializeWriter.count, length2);
                                                                                serializeWriter.count = serializeWriter.buf.length;
                                                                                serializeWriter.flush();
                                                                                i4 -= length2;
                                                                                i5 += length2;
                                                                            } while (i4 > serializeWriter.buf.length);
                                                                            i3 = i4;
                                                                            System.arraycopy(cArr, i5, serializeWriter.buf, serializeWriter.count, i4);
                                                                            serializeWriter.count = i3;
                                                                        }
                                                                    }
                                                                    i3 = i18;
                                                                    i4 = length;
                                                                    i5 = 0;
                                                                    System.arraycopy(cArr, i5, serializeWriter.buf, serializeWriter.count, i4);
                                                                    serializeWriter.count = i3;
                                                                } else {
                                                                    fieldSerializer.writePrefix(jSONSerializer);
                                                                }
                                                            }
                                                            if (!z5 || z3) {
                                                                if (z8) {
                                                                    fieldSerializer.writeValue(jSONSerializer, i);
                                                                } else if (cls == String.class) {
                                                                    int i19 = fieldSerializer.features | this.features;
                                                                    if (i != null) {
                                                                        String str6 = (String) i;
                                                                        if (z11) {
                                                                            serializeWriter.writeStringWithSingleQuote(str6);
                                                                        } else {
                                                                            serializeWriter.writeStringWithDoubleQuote(str6, 0, true);
                                                                        }
                                                                    } else if ((serializeWriter.features & SerializerFeature.WriteNullStringAsEmpty.mask) == 0 && (SerializerFeature.WriteNullStringAsEmpty.mask & i19) == 0) {
                                                                        serializeWriter.writeNull();
                                                                    } else {
                                                                        serializeWriter.writeString(str2);
                                                                    }
                                                                } else if (!fieldInfo.isEnum) {
                                                                    fieldSerializer.writeValue(jSONSerializer, i);
                                                                } else if (i == null) {
                                                                    serializeWriter.writeNull();
                                                                } else if ((serializeWriter.features & SerializerFeature.WriteEnumUsingToString.mask) != 0) {
                                                                    String str7 = ((Enum) i).toString();
                                                                    if ((serializeWriter.features & SerializerFeature.UseSingleQuotes.mask) != 0) {
                                                                        serializeWriter.writeStringWithSingleQuote(str7);
                                                                    } else {
                                                                        serializeWriter.writeStringWithDoubleQuote(str7, 0, false);
                                                                    }
                                                                } else {
                                                                    serializeWriter.writeInt(((Enum) i).ordinal());
                                                                }
                                                                z13 = true;
                                                                i7++;
                                                                obj7 = obj;
                                                                fieldSerializerArr = fieldSerializerArr;
                                                                z11 = z11;
                                                                z10 = z10;
                                                                z12 = z12;
                                                                list8 = list4;
                                                                list6 = list3;
                                                                list5 = list2;
                                                                list7 = list;
                                                            } else if (cls == Integer.TYPE) {
                                                                if (i2 == Integer.MIN_VALUE) {
                                                                    serializeWriter.write("-2147483648");
                                                                } else {
                                                                    int i20 = 0;
                                                                    while ((i2 < 0 ? -i2 : i2) > SerializeWriter.sizeTable[i20]) {
                                                                        i20++;
                                                                    }
                                                                    int i21 = i20 + 1;
                                                                    if (i2 < 0) {
                                                                        i21++;
                                                                    }
                                                                    int i22 = serializeWriter.count + i21;
                                                                    if (i22 > serializeWriter.buf.length) {
                                                                        if (serializeWriter.writer == null) {
                                                                            serializeWriter.expandCapacity(i22);
                                                                        } else {
                                                                            char[] cArr2 = new char[i21];
                                                                            SerializeWriter.getChars((long) i2, i21, cArr2);
                                                                            serializeWriter.write(cArr2, 0, cArr2.length);
                                                                            z7 = true;
                                                                            if (!z7) {
                                                                                SerializeWriter.getChars((long) i2, i22, serializeWriter.buf);
                                                                                serializeWriter.count = i22;
                                                                            }
                                                                        }
                                                                    }
                                                                    z7 = false;
                                                                    if (!z7) {
                                                                    }
                                                                }
                                                            } else if (cls == Long.TYPE) {
                                                                jSONSerializer.out.writeLong(j);
                                                            } else if (cls == Boolean.TYPE) {
                                                                if (z4) {
                                                                    jSONSerializer.out.write(true_chars, 0, true_chars.length);
                                                                } else {
                                                                    jSONSerializer.out.write(false_chars, 0, false_chars.length);
                                                                }
                                                            }
                                                        }
                                                        z13 = true;
                                                        i7++;
                                                        obj7 = obj;
                                                        fieldSerializerArr = fieldSerializerArr;
                                                        z11 = z11;
                                                        z10 = z10;
                                                        z12 = z12;
                                                        list8 = list4;
                                                        list6 = list3;
                                                        list5 = list2;
                                                        list7 = list;
                                                    }
                                                }
                                                i7++;
                                                obj7 = obj;
                                                fieldSerializerArr = fieldSerializerArr;
                                                z11 = z11;
                                                z10 = z10;
                                                z12 = z12;
                                                list8 = list4;
                                                list6 = list3;
                                                list5 = list2;
                                                list7 = list;
                                            } else {
                                                obj6 = fieldInfo2.field.get(obj7);
                                            }
                                            z4 = false;
                                            z3 = false;
                                            if (list5 != null) {
                                            }
                                            z6 = true;
                                            if (!z6) {
                                            }
                                            i7++;
                                            obj7 = obj;
                                            fieldSerializerArr = fieldSerializerArr;
                                            z11 = z11;
                                            z10 = z10;
                                            z12 = z12;
                                            list8 = list4;
                                            list6 = list3;
                                            list5 = list2;
                                            list7 = list;
                                        } else {
                                            obj6 = fieldSerializer.getPropertyValue(obj7);
                                        }
                                        i = obj6;
                                        z5 = false;
                                        i2 = 0;
                                        z4 = false;
                                        z3 = true;
                                        if (list5 != null) {
                                        }
                                        z6 = true;
                                        if (!z6) {
                                        }
                                        i7++;
                                        obj7 = obj;
                                        fieldSerializerArr = fieldSerializerArr;
                                        z11 = z11;
                                        z10 = z10;
                                        z12 = z12;
                                        list8 = list4;
                                        list6 = list3;
                                        list5 = list2;
                                        list7 = list;
                                    }
                                }
                            }
                            list4 = list8;
                            list = list7;
                            list3 = list6;
                            list2 = list5;
                            i7++;
                            obj7 = obj;
                            fieldSerializerArr = fieldSerializerArr;
                            z11 = z11;
                            z10 = z10;
                            z12 = z12;
                            list8 = list4;
                            list6 = list3;
                            list5 = list2;
                            list7 = list;
                        } catch (Exception e3) {
                            exc = e3;
                            serialContext = serialContext2;
                            String str32 = "write javaBean error, fastjson version 1.1.71";
                            if (obj2 != null) {
                                try {
                                    str32 = str32 + ", fieldName : " + obj2;
                                } catch (Throwable th3) {
                                    th = th3;
                                    th = th;
                                    jSONSerializer.context = serialContext;
                                    throw th;
                                }
                            }
                            throw new JSONException(str32, exc);
                        } catch (Throwable th4) {
                            th = th4;
                            serialContext = serialContext2;
                            jSONSerializer.context = serialContext;
                            throw th;
                        }
                    } catch (Exception e4) {
                        e = e4;
                        serialContext = serialContext2;
                        exc = e;
                        String str322 = "write javaBean error, fastjson version 1.1.71";
                        if (obj2 != null) {
                        }
                        throw new JSONException(str322, exc);
                    } catch (Throwable th5) {
                        th = th5;
                        serialContext = serialContext2;
                        th = th;
                        jSONSerializer.context = serialContext;
                        throw th;
                    }
                }
                char c4 = TelephoneNumberUtils.PAUSE;
                if (jSONSerializer.afterFilters != null) {
                    if (!z13) {
                        c4 = 0;
                    }
                    char c5 = c4;
                    for (AfterFilter afterFilter : jSONSerializer.afterFilters) {
                        c5 = afterFilter.writeAfter(jSONSerializer, obj, c5);
                    }
                }
                if (fieldSerializerArr.length > 0 && (serializeWriter.features & SerializerFeature.PrettyFormat.mask) != 0) {
                    jSONSerializer.decrementIdent();
                    jSONSerializer.println();
                }
                int i23 = serializeWriter.count + 1;
                if (i23 > serializeWriter.buf.length) {
                    if (serializeWriter.writer == null) {
                        serializeWriter.expandCapacity(i23);
                    } else {
                        serializeWriter.flush();
                        i23 = 1;
                    }
                }
                serializeWriter.buf[serializeWriter.count] = c2;
                serializeWriter.count = i23;
                jSONSerializer.context = serialContext2;
            } catch (Exception e5) {
                e = e5;
                serialContext = serialContext2;
                exc = e;
                String str3222 = "write javaBean error, fastjson version 1.1.71";
                if (obj2 != null) {
                }
                throw new JSONException(str3222, exc);
            } catch (Throwable th6) {
                th = th6;
                serialContext = serialContext2;
                th = th;
                jSONSerializer.context = serialContext;
                throw th;
            }
        }
    }

    public Map<String, Object> getFieldValuesMap(Object obj) throws Exception {
        LinkedHashMap linkedHashMap = new LinkedHashMap(this.sortedGetters.length);
        FieldSerializer[] fieldSerializerArr = this.sortedGetters;
        for (FieldSerializer fieldSerializer : fieldSerializerArr) {
            linkedHashMap.put(fieldSerializer.fieldInfo.name, fieldSerializer.getPropertyValue(obj));
        }
        return linkedHashMap;
    }
}
