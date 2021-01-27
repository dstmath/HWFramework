package ohos.utils.fastjson.parser;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ohos.utils.fastjson.JSONException;
import ohos.utils.fastjson.PropertyNamingStrategy;
import ohos.utils.fastjson.annotation.JSONCreator;
import ohos.utils.fastjson.annotation.JSONField;
import ohos.utils.fastjson.annotation.JSONType;
import ohos.utils.fastjson.serializer.SerializerFeature;
import ohos.utils.fastjson.util.FieldInfo;
import ohos.utils.fastjson.util.TypeUtils;

/* access modifiers changed from: package-private */
public class JavaBeanInfo {
    final Constructor<?> creatorConstructor;
    public final String[] creatorConstructorParameters;
    final Constructor<?> defaultConstructor;
    final int defaultConstructorParameterSize;
    final Method factoryMethod;
    final FieldInfo[] fields;
    final JSONType jsonType;
    boolean ordered = false;
    public final int parserFeatures;
    final FieldInfo[] sortedFields;
    final boolean supportBeanToArray;
    public final String typeKey;
    public final long typeKeyHashCode;
    public final String typeName;

    JavaBeanInfo(Class<?> cls, Constructor<?> constructor, Constructor<?> constructor2, Method method, FieldInfo[] fieldInfoArr, FieldInfo[] fieldInfoArr2, JSONType jSONType, String[] strArr) {
        int i;
        boolean z;
        int i2 = 0;
        this.defaultConstructor = constructor;
        this.creatorConstructor = constructor2;
        this.factoryMethod = method;
        this.fields = fieldInfoArr;
        this.jsonType = jSONType;
        if (strArr == null || strArr.length != fieldInfoArr.length) {
            this.creatorConstructorParameters = strArr;
        } else {
            this.creatorConstructorParameters = null;
        }
        if (jSONType != null) {
            String typeName2 = jSONType.typeName();
            this.typeName = typeName2.length() <= 0 ? cls.getName() : typeName2;
            String typeKey2 = jSONType.typeKey();
            this.typeKey = typeKey2.length() <= 0 ? null : typeKey2;
            i = 0;
            for (Feature feature : jSONType.parseFeatures()) {
                i |= feature.mask;
            }
        } else {
            this.typeName = cls.getName();
            this.typeKey = null;
            i = 0;
        }
        String str = this.typeKey;
        if (str == null) {
            this.typeKeyHashCode = 0;
        } else {
            this.typeKeyHashCode = TypeUtils.fnv_64_lower(str);
        }
        this.parserFeatures = i;
        if (jSONType != null) {
            Feature[] parseFeatures = jSONType.parseFeatures();
            z = false;
            for (Feature feature2 : parseFeatures) {
                if (feature2 == Feature.SupportArrayToBean) {
                    z = true;
                }
            }
        } else {
            z = false;
        }
        this.supportBeanToArray = z;
        FieldInfo[] computeSortedFields = computeSortedFields(fieldInfoArr, fieldInfoArr2);
        this.sortedFields = Arrays.equals(fieldInfoArr, computeSortedFields) ? fieldInfoArr : computeSortedFields;
        if (constructor != null) {
            i2 = constructor.getParameterTypes().length;
        } else if (method != null) {
            i2 = method.getParameterTypes().length;
        }
        this.defaultConstructorParameterSize = i2;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:71:0x00b4, code lost:
        r3 = false;
     */
    private FieldInfo[] computeSortedFields(FieldInfo[] fieldInfoArr, FieldInfo[] fieldInfoArr2) {
        String[] orders;
        boolean z;
        boolean z2;
        boolean z3;
        boolean z4;
        JSONType jSONType = this.jsonType;
        if (!(jSONType == null || (orders = jSONType.orders()) == null || orders.length == 0)) {
            int i = 0;
            while (true) {
                if (i >= orders.length) {
                    z = true;
                    break;
                }
                int i2 = 0;
                while (true) {
                    if (i2 >= fieldInfoArr2.length) {
                        z4 = false;
                        break;
                    } else if (fieldInfoArr2[i2].name.equals(orders[i])) {
                        z4 = true;
                        break;
                    } else {
                        i2++;
                    }
                }
                if (!z4) {
                    z = false;
                    break;
                }
                i++;
            }
            if (!z) {
                return fieldInfoArr2;
            }
            if (orders.length == fieldInfoArr.length) {
                int i3 = 0;
                while (true) {
                    if (i3 >= orders.length) {
                        z3 = true;
                        break;
                    } else if (!fieldInfoArr2[i3].name.equals(orders[i3])) {
                        z3 = false;
                        break;
                    } else {
                        i3++;
                    }
                }
                if (z3) {
                    return fieldInfoArr2;
                }
                FieldInfo[] fieldInfoArr3 = new FieldInfo[fieldInfoArr2.length];
                for (int i4 = 0; i4 < orders.length; i4++) {
                    int i5 = 0;
                    while (true) {
                        if (i5 >= fieldInfoArr2.length) {
                            break;
                        } else if (fieldInfoArr2[i5].name.equals(orders[i4])) {
                            fieldInfoArr3[i4] = fieldInfoArr2[i5];
                            break;
                        } else {
                            i5++;
                        }
                    }
                }
                this.ordered = true;
                return fieldInfoArr3;
            }
            FieldInfo[] fieldInfoArr4 = new FieldInfo[fieldInfoArr2.length];
            for (int i6 = 0; i6 < orders.length; i6++) {
                int i7 = 0;
                while (true) {
                    if (i7 >= fieldInfoArr2.length) {
                        break;
                    } else if (fieldInfoArr2[i7].name.equals(orders[i6])) {
                        fieldInfoArr4[i6] = fieldInfoArr2[i7];
                        break;
                    } else {
                        i7++;
                    }
                }
            }
            int length = orders.length;
            for (int i8 = 0; i8 < fieldInfoArr2.length; i8++) {
                int i9 = 0;
                while (true) {
                    if (i9 >= fieldInfoArr4.length || i9 >= length) {
                        break;
                    } else if (fieldInfoArr4[i8].equals(fieldInfoArr2[i9])) {
                        z2 = true;
                        break;
                    } else {
                        i9++;
                    }
                }
                if (!z2) {
                    fieldInfoArr4[length] = fieldInfoArr2[i8];
                    length++;
                }
            }
            this.ordered = true;
        }
        return fieldInfoArr2;
    }

    static boolean addField(List<FieldInfo> list, FieldInfo fieldInfo, boolean z) {
        if (!z) {
            int size = list.size();
            for (int i = 0; i < size; i++) {
                FieldInfo fieldInfo2 = list.get(i);
                if (fieldInfo2.name.equals(fieldInfo.name) && (!fieldInfo2.getOnly || fieldInfo.getOnly)) {
                    return false;
                }
            }
        }
        list.add(fieldInfo);
        return true;
    }

    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r0v51, types: [java.lang.reflect.Type[]] */
    /* JADX WARN: Type inference failed for: r0v82, types: [java.lang.reflect.Type[]] */
    /* JADX WARNING: Code restructure failed: missing block: B:344:0x07a0, code lost:
        if (r1.length() > 0) goto L_0x07bf;
     */
    /* JADX WARNING: Removed duplicated region for block: B:238:0x0539  */
    /* JADX WARNING: Removed duplicated region for block: B:242:0x056c  */
    /* JADX WARNING: Removed duplicated region for block: B:245:0x0571  */
    /* JADX WARNING: Removed duplicated region for block: B:258:0x05de  */
    /* JADX WARNING: Unknown variable types count: 2 */
    public static JavaBeanInfo build(Class<?> cls, int i, Type type, boolean z, boolean z2, boolean z3, boolean z4, PropertyNamingStrategy propertyNamingStrategy) {
        Constructor<?> constructor;
        Method method;
        Method[] methodArr;
        Method[] methodArr2;
        Constructor<?> constructor2;
        HashMap hashMap;
        Field[] fieldArr;
        int i2;
        Method[] methodArr3;
        Field[] fieldArr2;
        Constructor<?> constructor3;
        Method method2;
        int i3;
        int i4;
        String str;
        PropertyNamingStrategy propertyNamingStrategy2;
        int i5;
        int i6;
        Method[] methodArr4;
        int i7;
        int i8;
        Field[] fieldArr3;
        HashMap hashMap2;
        Constructor<?> constructor4;
        Method method3;
        Class<?> returnType;
        HashMap hashMap3;
        Field[] fieldArr4;
        int i9;
        int i10;
        Method method4;
        HashMap hashMap4;
        String str2;
        Field field;
        boolean z5;
        PropertyNamingStrategy propertyNamingStrategy3;
        JSONField jSONField;
        int i11;
        int i12;
        Constructor<?> constructor5;
        JSONField jSONField2;
        int i13;
        int i14;
        String str3;
        JSONField jSONField3;
        JSONField jSONField4;
        int i15;
        Constructor<?> constructor6;
        ArrayList arrayList = new ArrayList();
        HashMap hashMap5 = new HashMap();
        Constructor<?>[] declaredConstructors = cls.getDeclaredConstructors();
        boolean isKotlin = TypeUtils.isKotlin(cls);
        int i16 = i & 1024;
        int i17 = 0;
        if (i16 != 0 || (declaredConstructors.length != 1 && isKotlin)) {
            constructor = null;
        } else {
            try {
                constructor6 = cls.getDeclaredConstructor(new Class[0]);
            } catch (Exception unused) {
                constructor6 = null;
            }
            if (constructor6 == null && cls.isMemberClass() && (i & 8) == 0) {
                int length = declaredConstructors.length;
                int i18 = 0;
                while (true) {
                    if (i18 >= length) {
                        break;
                    }
                    Constructor<?> constructor7 = declaredConstructors[i18];
                    Class<?>[] parameterTypes = constructor7.getParameterTypes();
                    if (parameterTypes.length == 1 && parameterTypes[0].equals(cls.getDeclaringClass())) {
                        constructor = constructor7;
                        break;
                    }
                    i18++;
                    constructor6 = constructor6;
                }
            }
            constructor = constructor6;
        }
        String[] strArr = null;
        if (z) {
            methodArr = null;
            method = null;
        } else {
            ArrayList arrayList2 = new ArrayList();
            Class<?> cls2 = cls;
            Method method5 = null;
            while (cls2 != null && cls2 != Object.class) {
                Method[] declaredMethods = cls2.getDeclaredMethods();
                int length2 = declaredMethods.length;
                Method method6 = method5;
                int i19 = i17;
                while (i19 < length2) {
                    Method method7 = declaredMethods[i19];
                    int modifiers = method7.getModifiers();
                    if ((modifiers & 8) != 0) {
                        if (method7.isAnnotationPresent(JSONCreator.class)) {
                            if (method6 == null) {
                                method6 = method7;
                            } else {
                                throw new JSONException("multi-json creator");
                            }
                        }
                    } else if ((modifiers & 2) == 0) {
                        i15 = length2;
                        if ((modifiers & 256) == 0 && (modifiers & 4) == 0) {
                            arrayList2.add(method7);
                        }
                        i19++;
                        declaredMethods = declaredMethods;
                        length2 = i15;
                    }
                    i15 = length2;
                    i19++;
                    declaredMethods = declaredMethods;
                    length2 = i15;
                }
                cls2 = cls2.getSuperclass();
                method5 = method6;
                i17 = 0;
            }
            Method[] methodArr5 = new Method[arrayList2.size()];
            arrayList2.toArray(methodArr5);
            methodArr = methodArr5;
            method = method5;
        }
        Field[] declaredFields = cls.getDeclaredFields();
        boolean z6 = cls.isInterface() || i16 != 0;
        if (constructor == null || z6) {
            int length3 = declaredConstructors.length;
            int i20 = 0;
            while (true) {
                if (i20 >= length3) {
                    constructor5 = null;
                    break;
                }
                constructor5 = declaredConstructors[i20];
                if (((JSONCreator) constructor5.getAnnotation(JSONCreator.class)) != null) {
                    break;
                }
                i20++;
                length3 = length3;
            }
            String str4 = "illegal json creator";
            if (constructor5 != null) {
                TypeUtils.setAccessible(cls, constructor5, i);
                Class<?>[] parameterTypes2 = constructor5.getParameterTypes();
                Class<?>[] genericParameterTypes = z4 ? constructor5.getGenericParameterTypes() : parameterTypes2;
                Annotation[][] parameterAnnotations = constructor5.getParameterAnnotations();
                int i21 = 0;
                while (i21 < parameterTypes2.length) {
                    Annotation[] annotationArr = parameterAnnotations[i21];
                    int length4 = annotationArr.length;
                    int i22 = 0;
                    while (true) {
                        if (i22 >= length4) {
                            jSONField4 = null;
                            break;
                        }
                        Annotation annotation = annotationArr[i22];
                        if (annotation instanceof JSONField) {
                            jSONField4 = (JSONField) annotation;
                            break;
                        }
                        i22++;
                        length4 = length4;
                        annotationArr = annotationArr;
                    }
                    if (jSONField4 != null) {
                        Class<?> cls3 = parameterTypes2[i21];
                        Class<?> cls4 = genericParameterTypes[i21];
                        Field field2 = TypeUtils.getField(cls, jSONField4.name(), declaredFields, hashMap5);
                        if (field2 != null) {
                            TypeUtils.setAccessible(cls, field2, i);
                        }
                        addField(arrayList, new FieldInfo(jSONField4.name(), cls, cls3, cls4, field2, jSONField4.ordinal(), SerializerFeature.of(jSONField4.serialzeFeatures())), z);
                        i21++;
                        str4 = str4;
                        declaredFields = declaredFields;
                        constructor5 = constructor5;
                        hashMap5 = hashMap5;
                        parameterTypes2 = parameterTypes2;
                        methodArr = methodArr;
                        constructor = constructor;
                    } else {
                        throw new JSONException(str4);
                    }
                }
                constructor2 = constructor5;
                methodArr2 = methodArr;
                fieldArr = declaredFields;
                FieldInfo[] fieldInfoArr = new FieldInfo[arrayList.size()];
                arrayList.toArray(fieldInfoArr);
                FieldInfo[] fieldInfoArr2 = new FieldInfo[fieldInfoArr.length];
                System.arraycopy(fieldInfoArr, 0, fieldInfoArr2, 0, fieldInfoArr.length);
                Arrays.sort(fieldInfoArr2);
                if (z2) {
                    JSONType jSONType = (JSONType) cls.getAnnotation(JSONType.class);
                }
                String[] strArr2 = new String[fieldInfoArr.length];
                for (int i23 = 0; i23 < fieldInfoArr.length; i23++) {
                    strArr2[i23] = fieldInfoArr[i23].name;
                }
                strArr = strArr2;
                hashMap = hashMap5;
                constructor = constructor;
                i2 = 0;
            } else {
                constructor2 = constructor5;
                methodArr2 = methodArr;
                HashMap hashMap6 = hashMap5;
                fieldArr = declaredFields;
                if (method != null) {
                    TypeUtils.setAccessible(cls, method, i);
                    Class<?>[] parameterTypes3 = method.getParameterTypes();
                    if (parameterTypes3.length > 0) {
                        Class<?>[] genericParameterTypes2 = z4 ? method.getGenericParameterTypes() : parameterTypes3;
                        Annotation[][] parameterAnnotations2 = method.getParameterAnnotations();
                        int i24 = 0;
                        while (i24 < parameterTypes3.length) {
                            Annotation[] annotationArr2 = parameterAnnotations2[i24];
                            int length5 = annotationArr2.length;
                            int i25 = 0;
                            while (true) {
                                if (i25 >= length5) {
                                    jSONField3 = null;
                                    break;
                                }
                                Annotation annotation2 = annotationArr2[i25];
                                if (annotation2 instanceof JSONField) {
                                    jSONField3 = (JSONField) annotation2;
                                    break;
                                }
                                i25++;
                            }
                            if (jSONField3 != null) {
                                addField(arrayList, new FieldInfo(jSONField3.name(), cls, parameterTypes3[i24], genericParameterTypes2[i24], TypeUtils.getField(cls, jSONField3.name(), fieldArr, hashMap6), jSONField3.ordinal(), SerializerFeature.of(jSONField3.serialzeFeatures())), z);
                                i24++;
                                genericParameterTypes2 = genericParameterTypes2;
                                hashMap6 = hashMap6;
                                parameterTypes3 = parameterTypes3;
                            } else {
                                throw new JSONException(str4);
                            }
                        }
                        FieldInfo[] fieldInfoArr3 = new FieldInfo[arrayList.size()];
                        arrayList.toArray(fieldInfoArr3);
                        FieldInfo[] fieldInfoArr4 = new FieldInfo[fieldInfoArr3.length];
                        System.arraycopy(fieldInfoArr3, 0, fieldInfoArr4, 0, fieldInfoArr3.length);
                        Arrays.sort(fieldInfoArr4);
                        return new JavaBeanInfo(cls, null, null, method, fieldInfoArr3, Arrays.equals(fieldInfoArr3, fieldInfoArr4) ? fieldInfoArr3 : fieldInfoArr4, z2 ? (JSONType) cls.getAnnotation(JSONType.class) : null, null);
                    }
                    hashMap = hashMap6;
                } else {
                    hashMap = hashMap6;
                    if (!z6) {
                        if (!isKotlin || declaredConstructors.length <= 0) {
                            throw new JSONException("default constructor not found. " + cls);
                        }
                        String[] koltinConstructorParameters = TypeUtils.getKoltinConstructorParameters(cls);
                        if (koltinConstructorParameters != null) {
                            Constructor<?> constructor8 = constructor2;
                            for (Constructor<?> constructor9 : declaredConstructors) {
                                Class<?>[] parameterTypes4 = constructor9.getParameterTypes();
                                if ((parameterTypes4.length <= 0 || !parameterTypes4[parameterTypes4.length - 1].getName().equals("kotlin.jvm.internal.DefaultConstructorMarker")) && (constructor8 == null || constructor8.getParameterTypes().length < parameterTypes4.length)) {
                                    constructor8 = constructor9;
                                }
                            }
                            constructor8.setAccessible(true);
                            TypeUtils.setAccessible(cls, constructor8, i);
                            Class<?>[] parameterTypes5 = constructor8.getParameterTypes();
                            Class<?>[] genericParameterTypes3 = z4 ? constructor8.getGenericParameterTypes() : parameterTypes5;
                            Annotation[][] parameterAnnotations3 = constructor8.getParameterAnnotations();
                            int i26 = 0;
                            while (i26 < parameterTypes5.length) {
                                String str5 = koltinConstructorParameters[i26];
                                Annotation[] annotationArr3 = parameterAnnotations3[i26];
                                int length6 = annotationArr3.length;
                                int i27 = 0;
                                while (true) {
                                    if (i27 >= length6) {
                                        jSONField2 = null;
                                        break;
                                    }
                                    Annotation annotation3 = annotationArr3[i27];
                                    if (annotation3 instanceof JSONField) {
                                        jSONField2 = (JSONField) annotation3;
                                        break;
                                    }
                                    i27++;
                                    annotationArr3 = annotationArr3;
                                }
                                Class<?> cls5 = parameterTypes5[i26];
                                Class<?> cls6 = genericParameterTypes3[i26];
                                Field field3 = TypeUtils.getField(cls, str5, fieldArr, hashMap);
                                if (field3 != null && jSONField2 == null) {
                                    jSONField2 = (JSONField) field3.getAnnotation(JSONField.class);
                                }
                                if (jSONField2 != null) {
                                    i14 = jSONField2.ordinal();
                                    i13 = SerializerFeature.of(jSONField2.serialzeFeatures());
                                    String name = jSONField2.name();
                                    if (name.length() != 0) {
                                        str5 = name;
                                    }
                                    str3 = str5;
                                } else {
                                    str3 = str5;
                                    i14 = 0;
                                    i13 = 0;
                                }
                                addField(arrayList, new FieldInfo(str3, cls, cls5, cls6, field3, i14, i13), z);
                                i26++;
                                constructor8 = constructor8;
                                parameterTypes5 = parameterTypes5;
                            }
                            constructor2 = constructor8;
                            FieldInfo[] fieldInfoArr5 = new FieldInfo[arrayList.size()];
                            arrayList.toArray(fieldInfoArr5);
                            FieldInfo[] fieldInfoArr6 = new FieldInfo[fieldInfoArr5.length];
                            i2 = 0;
                            System.arraycopy(fieldInfoArr5, 0, fieldInfoArr6, 0, fieldInfoArr5.length);
                            Arrays.sort(fieldInfoArr6);
                            String[] strArr3 = new String[fieldInfoArr5.length];
                            for (int i28 = 0; i28 < fieldInfoArr5.length; i28++) {
                                strArr3[i28] = fieldInfoArr5[i28].name;
                            }
                            strArr = strArr3;
                            constructor = constructor;
                        } else {
                            throw new JSONException("default constructor not found. " + cls);
                        }
                    }
                }
                i2 = 0;
                constructor = constructor;
            }
        } else {
            methodArr2 = methodArr;
            hashMap = hashMap5;
            constructor2 = null;
            i2 = 0;
            fieldArr = declaredFields;
        }
        if (constructor != null) {
            TypeUtils.setAccessible(cls, constructor, i);
        }
        int i29 = 4;
        if (!z) {
            Method[] methodArr6 = methodArr2;
            int length7 = methodArr6.length;
            int i30 = i2;
            while (i30 < length7) {
                Method method8 = methodArr6[i30];
                String name2 = method8.getName();
                if (name2.length() >= i29 && ((returnType = method8.getReturnType()) == Void.TYPE || returnType == method8.getDeclaringClass())) {
                    if (method8.getParameterTypes().length == 1) {
                        JSONField jSONField5 = z3 ? (JSONField) method8.getAnnotation(JSONField.class) : null;
                        if (jSONField5 == null && z3) {
                            jSONField5 = TypeUtils.getSupperMethodAnnotation(cls, method8);
                        }
                        if (jSONField5 == null) {
                            i8 = i30;
                            i7 = length7;
                            methodArr4 = methodArr6;
                            constructor4 = constructor;
                            method3 = method;
                            hashMap3 = hashMap;
                            fieldArr4 = fieldArr;
                            method4 = method8;
                            i10 = 0;
                            i9 = 0;
                        } else if (jSONField5.deserialize()) {
                            i10 = jSONField5.ordinal();
                            i9 = SerializerFeature.of(jSONField5.serialzeFeatures());
                            if (jSONField5.name().length() != 0) {
                                i8 = i30;
                                i7 = length7;
                                constructor4 = constructor;
                                methodArr4 = methodArr6;
                                method3 = method;
                                hashMap3 = hashMap;
                                fieldArr4 = fieldArr;
                                addField(arrayList, new FieldInfo(jSONField5.name(), method8, null, cls, type, i10, i9, jSONField5, null, z4), z);
                                TypeUtils.setAccessible(cls, method8, i);
                                fieldArr3 = fieldArr4;
                                hashMap2 = hashMap3;
                            } else {
                                i8 = i30;
                                i7 = length7;
                                methodArr4 = methodArr6;
                                constructor4 = constructor;
                                method3 = method;
                                hashMap3 = hashMap;
                                fieldArr4 = fieldArr;
                                method4 = method8;
                            }
                        }
                        if (name2.startsWith("set")) {
                            char charAt = name2.charAt(3);
                            if (Character.isUpperCase(charAt)) {
                                if (TypeUtils.compatibleWithJavaBean) {
                                    str2 = TypeUtils.decapitalize(name2.substring(3));
                                    hashMap4 = hashMap3;
                                    field = TypeUtils.getField(cls, str2, fieldArr4, hashMap4);
                                    if (field == null) {
                                        z5 = true;
                                    } else if (method4.getParameterTypes()[0] == Boolean.TYPE) {
                                        StringBuilder sb = new StringBuilder();
                                        sb.append("is");
                                        sb.append(Character.toUpperCase(str2.charAt(0)));
                                        z5 = true;
                                        sb.append(str2.substring(1));
                                        field = TypeUtils.getField(cls, sb.toString(), fieldArr4, hashMap4);
                                    } else {
                                        z5 = true;
                                    }
                                    if (field != null) {
                                        JSONField jSONField6 = z3 ? (JSONField) field.getAnnotation(JSONField.class) : null;
                                        if (jSONField6 != null) {
                                            i10 = jSONField6.ordinal();
                                            i9 = SerializerFeature.of(jSONField6.serialzeFeatures());
                                            if (jSONField6.name().length() != 0) {
                                                hashMap2 = hashMap4;
                                                fieldArr3 = fieldArr4;
                                                addField(arrayList, new FieldInfo(jSONField6.name(), method4, field, cls, type, i10, i9, jSONField5, jSONField6, z4), z);
                                                i30 = i8 + 1;
                                                method = method3;
                                                constructor = constructor4;
                                                hashMap = hashMap2;
                                                fieldArr = fieldArr3;
                                                length7 = i7;
                                                methodArr6 = methodArr4;
                                                i29 = 4;
                                            } else {
                                                hashMap2 = hashMap4;
                                                fieldArr3 = fieldArr4;
                                                if (jSONField5 == null) {
                                                    propertyNamingStrategy3 = propertyNamingStrategy;
                                                    i12 = i10;
                                                    i11 = i9;
                                                    jSONField = jSONField6;
                                                    if (propertyNamingStrategy3 != null) {
                                                        str2 = propertyNamingStrategy3.translate(str2);
                                                    }
                                                    addField(arrayList, new FieldInfo(str2, method4, null, cls, type, i12, i11, jSONField, null, z4), z);
                                                    TypeUtils.setAccessible(cls, method4, i);
                                                    i30 = i8 + 1;
                                                    method = method3;
                                                    constructor = constructor4;
                                                    hashMap = hashMap2;
                                                    fieldArr = fieldArr3;
                                                    length7 = i7;
                                                    methodArr6 = methodArr4;
                                                    i29 = 4;
                                                }
                                                propertyNamingStrategy3 = propertyNamingStrategy;
                                                jSONField = jSONField5;
                                                i12 = i10;
                                                i11 = i9;
                                                if (propertyNamingStrategy3 != null) {
                                                }
                                                addField(arrayList, new FieldInfo(str2, method4, null, cls, type, i12, i11, jSONField, null, z4), z);
                                                TypeUtils.setAccessible(cls, method4, i);
                                                i30 = i8 + 1;
                                                method = method3;
                                                constructor = constructor4;
                                                hashMap = hashMap2;
                                                fieldArr = fieldArr3;
                                                length7 = i7;
                                                methodArr6 = methodArr4;
                                                i29 = 4;
                                            }
                                        }
                                    }
                                    hashMap2 = hashMap4;
                                    fieldArr3 = fieldArr4;
                                    propertyNamingStrategy3 = propertyNamingStrategy;
                                    jSONField = jSONField5;
                                    i12 = i10;
                                    i11 = i9;
                                    if (propertyNamingStrategy3 != null) {
                                    }
                                    addField(arrayList, new FieldInfo(str2, method4, null, cls, type, i12, i11, jSONField, null, z4), z);
                                    TypeUtils.setAccessible(cls, method4, i);
                                    i30 = i8 + 1;
                                    method = method3;
                                    constructor = constructor4;
                                    hashMap = hashMap2;
                                    fieldArr = fieldArr3;
                                    length7 = i7;
                                    methodArr6 = methodArr4;
                                    i29 = 4;
                                } else {
                                    str2 = Character.toLowerCase(name2.charAt(3)) + name2.substring(4);
                                }
                            } else if (charAt == '_') {
                                str2 = name2.substring(4);
                            } else if (charAt == 'f') {
                                str2 = name2.substring(3);
                            } else if (name2.length() >= 5 && Character.isUpperCase(name2.charAt(4))) {
                                str2 = TypeUtils.decapitalize(name2.substring(3));
                            }
                            hashMap4 = hashMap3;
                            field = TypeUtils.getField(cls, str2, fieldArr4, hashMap4);
                            if (field == null) {
                            }
                            if (field != null) {
                            }
                            hashMap2 = hashMap4;
                            fieldArr3 = fieldArr4;
                            propertyNamingStrategy3 = propertyNamingStrategy;
                            jSONField = jSONField5;
                            i12 = i10;
                            i11 = i9;
                            if (propertyNamingStrategy3 != null) {
                            }
                            addField(arrayList, new FieldInfo(str2, method4, null, cls, type, i12, i11, jSONField, null, z4), z);
                            TypeUtils.setAccessible(cls, method4, i);
                            i30 = i8 + 1;
                            method = method3;
                            constructor = constructor4;
                            hashMap = hashMap2;
                            fieldArr = fieldArr3;
                            length7 = i7;
                            methodArr6 = methodArr4;
                            i29 = 4;
                        }
                        fieldArr3 = fieldArr4;
                        hashMap2 = hashMap3;
                    }
                    i8 = i30;
                    i7 = length7;
                    methodArr4 = methodArr6;
                    constructor4 = constructor;
                    method3 = method;
                    fieldArr3 = fieldArr;
                    hashMap2 = hashMap;
                    i30 = i8 + 1;
                    method = method3;
                    constructor = constructor4;
                    hashMap = hashMap2;
                    fieldArr = fieldArr3;
                    length7 = i7;
                    methodArr6 = methodArr4;
                    i29 = 4;
                } else {
                    i8 = i30;
                    i7 = length7;
                    methodArr4 = methodArr6;
                    constructor4 = constructor;
                    method3 = method;
                    fieldArr3 = fieldArr;
                    hashMap2 = hashMap;
                }
                i30 = i8 + 1;
                method = method3;
                constructor = constructor4;
                hashMap = hashMap2;
                fieldArr = fieldArr3;
                length7 = i7;
                methodArr6 = methodArr4;
                i29 = 4;
            }
            methodArr3 = methodArr6;
            constructor3 = constructor;
            method2 = method;
            fieldArr2 = fieldArr;
        } else {
            constructor3 = constructor;
            method2 = method;
            fieldArr2 = fieldArr;
            methodArr3 = methodArr2;
        }
        ArrayList<Field> arrayList3 = new ArrayList(fieldArr2.length);
        for (Field field4 : fieldArr2) {
            int modifiers2 = field4.getModifiers();
            if ((modifiers2 & 8) == 0) {
                if ((modifiers2 & 16) != 0) {
                    Class<?> type2 = field4.getType();
                    if (!(Map.class.isAssignableFrom(type2) || Collection.class.isAssignableFrom(type2))) {
                    }
                }
                if ((field4.getModifiers() & 1) != 0) {
                    arrayList3.add(field4);
                }
            }
        }
        Class<? super Object> superclass = cls.getSuperclass();
        while (superclass != null && superclass != Object.class) {
            Field[] declaredFields2 = superclass.getDeclaredFields();
            for (Field field5 : declaredFields2) {
                int modifiers3 = field5.getModifiers();
                if ((modifiers3 & 8) == 0) {
                    if ((modifiers3 & 16) != 0) {
                        Class<?> type3 = field5.getType();
                        if (!(Map.class.isAssignableFrom(type3) || Collection.class.isAssignableFrom(type3))) {
                        }
                    }
                    if ((modifiers3 & 1) != 0) {
                        arrayList3.add(field5);
                    }
                }
            }
            superclass = superclass.getSuperclass();
        }
        for (Field field6 : arrayList3) {
            String name3 = field6.getName();
            int size = arrayList.size();
            boolean z7 = false;
            for (int i31 = 0; i31 < size; i31++) {
                if (((FieldInfo) arrayList.get(i31)).name.equals(name3)) {
                    z7 = true;
                }
            }
            if (!z7) {
                JSONField jSONField7 = z3 ? (JSONField) field6.getAnnotation(JSONField.class) : null;
                if (jSONField7 != null) {
                    int ordinal = jSONField7.ordinal();
                    int of = SerializerFeature.of(jSONField7.serialzeFeatures());
                    if (jSONField7.name().length() != 0) {
                        name3 = jSONField7.name();
                    }
                    propertyNamingStrategy2 = propertyNamingStrategy;
                    i6 = ordinal;
                    i5 = of;
                } else {
                    propertyNamingStrategy2 = propertyNamingStrategy;
                    i6 = 0;
                    i5 = 0;
                }
                if (propertyNamingStrategy2 != null) {
                    name3 = propertyNamingStrategy2.translate(name3);
                }
                TypeUtils.setAccessible(cls, field6, i);
                addField(arrayList, new FieldInfo(name3, null, field6, cls, type, i6, i5, null, jSONField7, z4), z);
            }
        }
        if (!z) {
            int length8 = methodArr3.length;
            int i32 = 0;
            while (i32 < length8) {
                Method method9 = methodArr3[i32];
                String name4 = method9.getName();
                if (name4.length() >= 4 && name4.startsWith("get")) {
                    if (Character.isUpperCase(name4.charAt(3)) && method9.getParameterTypes().length == 0) {
                        Class<?> returnType2 = method9.getReturnType();
                        if (Collection.class.isAssignableFrom(returnType2) || Map.class.isAssignableFrom(returnType2)) {
                            JSONField jSONField8 = z3 ? (JSONField) method9.getAnnotation(JSONField.class) : null;
                            if (jSONField8 != null) {
                                str = jSONField8.name();
                            }
                            str = Character.toLowerCase(name4.charAt(3)) + name4.substring(4);
                            i4 = i32;
                            i3 = length8;
                            addField(arrayList, new FieldInfo(str, method9, null, cls, type, 0, 0, jSONField8, null, z4), z);
                            TypeUtils.setAccessible(cls, method9, i);
                        }
                    }
                    i4 = i32;
                    i3 = length8;
                } else {
                    i4 = i32;
                    i3 = length8;
                }
                i32 = i4 + 1;
                length8 = i3;
            }
        }
        FieldInfo[] fieldInfoArr7 = new FieldInfo[arrayList.size()];
        arrayList.toArray(fieldInfoArr7);
        FieldInfo[] fieldInfoArr8 = new FieldInfo[fieldInfoArr7.length];
        System.arraycopy(fieldInfoArr7, 0, fieldInfoArr8, 0, fieldInfoArr7.length);
        Arrays.sort(fieldInfoArr8);
        return new JavaBeanInfo(cls, constructor3, constructor2, method2, fieldInfoArr7, fieldInfoArr8, z2 ? (JSONType) cls.getAnnotation(JSONType.class) : null, strArr);
    }
}
