package com.huawei.agpengine.impl;

import com.huawei.agpengine.math.Matrix4x4;
import com.huawei.agpengine.math.Quaternion;
import com.huawei.agpengine.math.Vector2;
import com.huawei.agpengine.math.Vector3;
import com.huawei.agpengine.math.Vector4;
import com.huawei.agpengine.property.Property;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/* access modifiers changed from: package-private */
public class PropertyImpl implements Property {
    private static final String TYPE_BOOL = "bool";
    private static final String TYPE_CHAR = "char";
    private static final String TYPE_DOUBLE = "double";
    private static final String TYPE_ENTITY = "Entity";
    private static final String TYPE_ERROR_TEXT = "No matching Type found.";
    private static final String TYPE_FLOAT = "float";
    private static final String TYPE_GPU_RESOURCE_HANDLE = "GpuResourceHandle";
    private static final String TYPE_INT16 = "int16_t";
    private static final String TYPE_INT32 = "int32_t";
    private static final String TYPE_INT64 = "int64_t";
    private static final String TYPE_INT8 = "int8_t";
    private static final String TYPE_MATH_MAT4X4 = "Math::Mat4X4";
    private static final String TYPE_MATH_QUAT = "Math::Quat";
    private static final String TYPE_MATH_VEC2 = "Math::Vec2";
    private static final String TYPE_MATH_VEC3 = "Math::Vec3";
    private static final String TYPE_MATH_VEC4 = "Math::Vec4";
    private static final String TYPE_UINT16 = "uint16_t";
    private static final String TYPE_UINT32 = "uint32_t";
    private static final String TYPE_UINT64 = "uint64_t";
    private static final String TYPE_UINT8 = "uint8_t";
    private static Property.Serializer sFloatSerializer = new Property.Serializer() {
        /* class com.huawei.agpengine.impl.PropertyImpl.AnonymousClass2 */

        @Override // com.huawei.agpengine.property.Property.Serializer
        public Class getJavaType(Property property) {
            int sizePerElement = property.getSize() / property.getCount();
            if (sizePerElement == 4) {
                return PropertyImpl.getType(property, Float[].class);
            }
            if (sizePerElement == 8) {
                return PropertyImpl.getType(property, Double[].class);
            }
            throw new IllegalArgumentException(PropertyImpl.TYPE_ERROR_TEXT);
        }

        @Override // com.huawei.agpengine.property.Property.Serializer
        public <Type> Type read(Property property, ByteBuffer data, Class<Type> type) {
            int sizePerElement = property.getSize() / property.getCount();
            if (sizePerElement == 4) {
                return (Type) PropertyImpl.castTo(Float.valueOf(data.getFloat()), type);
            }
            if (sizePerElement == 8) {
                return (Type) PropertyImpl.castTo(Double.valueOf(data.getDouble()), type);
            }
            throw new IllegalArgumentException(PropertyImpl.TYPE_ERROR_TEXT);
        }

        @Override // com.huawei.agpengine.property.Property.Serializer
        public <Type> int write(Property property, ByteBuffer data, Type value) {
            int sizePerElement = property.getSize() / property.getCount();
            if (sizePerElement == 4) {
                data.putFloat(value.floatValue());
                return 4;
            } else if (sizePerElement == 8) {
                data.putDouble(value.doubleValue());
                return 8;
            } else {
                throw new IllegalArgumentException(PropertyImpl.TYPE_ERROR_TEXT);
            }
        }
    };
    private static Property.Serializer sIntSerializer = new Property.Serializer() {
        /* class com.huawei.agpengine.impl.PropertyImpl.AnonymousClass1 */

        @Override // com.huawei.agpengine.property.Property.Serializer
        public Class getJavaType(Property property) {
            int sizePerElement = property.getSize() / property.getCount();
            if (sizePerElement == 1) {
                return PropertyImpl.getType(property, Byte[].class);
            }
            if (sizePerElement == 2) {
                return PropertyImpl.getType(property, Short[].class);
            }
            if (sizePerElement == 4) {
                return PropertyImpl.getType(property, Integer[].class);
            }
            if (sizePerElement == 8) {
                return PropertyImpl.getType(property, Long[].class);
            }
            throw new IllegalArgumentException(PropertyImpl.TYPE_ERROR_TEXT);
        }

        @Override // com.huawei.agpengine.property.Property.Serializer
        public <Type> Type read(Property property, ByteBuffer data, Class<Type> type) {
            int sizePerElement = property.getSize() / property.getCount();
            if (sizePerElement == 1) {
                return (Type) PropertyImpl.castTo(Byte.valueOf(data.get()), type);
            }
            if (sizePerElement == 2) {
                return (Type) PropertyImpl.castTo(Short.valueOf(data.getShort()), type);
            }
            if (sizePerElement == 4) {
                return (Type) PropertyImpl.castTo(Integer.valueOf(data.getInt()), type);
            }
            if (sizePerElement == 8) {
                return (Type) PropertyImpl.castTo(Long.valueOf(data.getLong()), type);
            }
            throw new IllegalArgumentException(PropertyImpl.TYPE_ERROR_TEXT);
        }

        @Override // com.huawei.agpengine.property.Property.Serializer
        public <Type> int write(Property property, ByteBuffer data, Type value) {
            int sizePerElement = property.getSize() / property.getCount();
            if (sizePerElement == 1) {
                data.put(value.byteValue());
                return 1;
            } else if (sizePerElement == 2) {
                data.putShort(value.shortValue());
                return 2;
            } else if (sizePerElement == 4) {
                data.putInt(value.intValue());
                return 4;
            } else if (sizePerElement == 8) {
                data.putLong(value.longValue());
                return 8;
            } else {
                throw new IllegalArgumentException(PropertyImpl.TYPE_ERROR_TEXT);
            }
        }
    };
    private static Property.Serializer sMiscSerializer = new Property.Serializer() {
        /* class com.huawei.agpengine.impl.PropertyImpl.AnonymousClass4 */

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        @Override // com.huawei.agpengine.property.Property.Serializer
        public Class getJavaType(Property property) {
            char c;
            String typeName = property.getTypeName();
            switch (typeName.hashCode()) {
                case -1645839905:
                    if (typeName.equals(PropertyImpl.TYPE_MATH_QUAT)) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case -1645706330:
                    if (typeName.equals(PropertyImpl.TYPE_MATH_VEC2)) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case -1645706329:
                    if (typeName.equals(PropertyImpl.TYPE_MATH_VEC3)) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case -1645706328:
                    if (typeName.equals(PropertyImpl.TYPE_MATH_VEC4)) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case -1236663496:
                    if (typeName.equals(PropertyImpl.TYPE_MATH_MAT4X4)) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case 3029738:
                    if (typeName.equals(PropertyImpl.TYPE_BOOL)) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            if (c == 0) {
                return PropertyImpl.getType(property, Boolean[].class);
            }
            if (c == 1) {
                return PropertyImpl.getType(property, Vector2[].class);
            }
            if (c == 2) {
                return PropertyImpl.getType(property, Vector3[].class);
            }
            if (c == 3) {
                return PropertyImpl.getType(property, Vector4[].class);
            }
            if (c == 4) {
                return PropertyImpl.getType(property, Quaternion[].class);
            }
            if (c == 5) {
                return PropertyImpl.getType(property, Matrix4x4[].class);
            }
            throw new IllegalArgumentException(PropertyImpl.TYPE_ERROR_TEXT);
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        @Override // com.huawei.agpengine.property.Property.Serializer
        public <Type> Type read(Property property, ByteBuffer data, Class<Type> type) {
            char c;
            String typeName = property.getTypeName();
            boolean z = false;
            switch (typeName.hashCode()) {
                case -1645839905:
                    if (typeName.equals(PropertyImpl.TYPE_MATH_QUAT)) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case -1645706330:
                    if (typeName.equals(PropertyImpl.TYPE_MATH_VEC2)) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case -1645706329:
                    if (typeName.equals(PropertyImpl.TYPE_MATH_VEC3)) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case -1645706328:
                    if (typeName.equals(PropertyImpl.TYPE_MATH_VEC4)) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case -1236663496:
                    if (typeName.equals(PropertyImpl.TYPE_MATH_MAT4X4)) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case 3029738:
                    if (typeName.equals(PropertyImpl.TYPE_BOOL)) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            if (c == 0) {
                if (data.get() != 0) {
                    z = true;
                }
                return (Type) PropertyImpl.castTo(Boolean.valueOf(z), type);
            } else if (c == 1) {
                return (Type) PropertyImpl.castTo(new Vector2(data.getFloat(), data.getFloat()), type);
            } else {
                if (c == 2) {
                    return (Type) PropertyImpl.castTo(new Vector3(data.getFloat(), data.getFloat(), data.getFloat()), type);
                }
                if (c == 3) {
                    return (Type) PropertyImpl.castTo(new Vector4(data.getFloat(), data.getFloat(), data.getFloat(), data.getFloat()), type);
                }
                if (c == 4) {
                    return (Type) PropertyImpl.castTo(new Quaternion(data.getFloat(), data.getFloat(), data.getFloat(), data.getFloat()), type);
                }
                if (c == 5) {
                    Matrix4x4 mat = new Matrix4x4();
                    for (int i = 0; i < 16; i++) {
                        mat.set(i, data.getFloat());
                    }
                    return (Type) PropertyImpl.castTo(mat, type);
                }
                throw new IllegalArgumentException(PropertyImpl.TYPE_ERROR_TEXT);
            }
        }

        /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
        @Override // com.huawei.agpengine.property.Property.Serializer
        public <Type> int write(Property property, ByteBuffer data, Type value) {
            char c;
            String typeName = property.getTypeName();
            switch (typeName.hashCode()) {
                case -1645839905:
                    if (typeName.equals(PropertyImpl.TYPE_MATH_QUAT)) {
                        c = 4;
                        break;
                    }
                    c = 65535;
                    break;
                case -1645706330:
                    if (typeName.equals(PropertyImpl.TYPE_MATH_VEC2)) {
                        c = 1;
                        break;
                    }
                    c = 65535;
                    break;
                case -1645706329:
                    if (typeName.equals(PropertyImpl.TYPE_MATH_VEC3)) {
                        c = 2;
                        break;
                    }
                    c = 65535;
                    break;
                case -1645706328:
                    if (typeName.equals(PropertyImpl.TYPE_MATH_VEC4)) {
                        c = 3;
                        break;
                    }
                    c = 65535;
                    break;
                case -1236663496:
                    if (typeName.equals(PropertyImpl.TYPE_MATH_MAT4X4)) {
                        c = 5;
                        break;
                    }
                    c = 65535;
                    break;
                case 3029738:
                    if (typeName.equals(PropertyImpl.TYPE_BOOL)) {
                        c = 0;
                        break;
                    }
                    c = 65535;
                    break;
                default:
                    c = 65535;
                    break;
            }
            if (c == 0) {
                data.put(value.booleanValue() ? (byte) 1 : 0);
                return 1;
            } else if (c == 1) {
                Type vec = value;
                data.putFloat(vec.getX());
                data.putFloat(vec.getY());
                return 8;
            } else if (c == 2) {
                Type vec2 = value;
                data.putFloat(vec2.getX());
                data.putFloat(vec2.getY());
                data.putFloat(vec2.getZ());
                return 12;
            } else if (c == 3) {
                Type vec3 = value;
                data.putFloat(vec3.getX());
                data.putFloat(vec3.getY());
                data.putFloat(vec3.getZ());
                data.putFloat(vec3.getW());
                return 16;
            } else if (c == 4) {
                Type quat = value;
                data.putFloat(quat.getX());
                data.putFloat(quat.getY());
                data.putFloat(quat.getZ());
                data.putFloat(quat.getW());
                return 16;
            } else if (c == 5) {
                Type mat = value;
                for (int i = 0; i < 16; i++) {
                    data.putFloat(mat.get(i));
                }
                return 64;
            } else {
                throw new IllegalArgumentException(PropertyImpl.TYPE_ERROR_TEXT);
            }
        }
    };
    private static Property.Serializer sStringSerializer = new Property.Serializer() {
        /* class com.huawei.agpengine.impl.PropertyImpl.AnonymousClass3 */

        @Override // com.huawei.agpengine.property.Property.Serializer
        public Class getJavaType(Property property) {
            return String.class;
        }

        @Override // com.huawei.agpengine.property.Property.Serializer
        public <Type> Type read(Property property, ByteBuffer data, Class<Type> type) {
            byte[] stringBytes = new byte[property.getCount()];
            data.get(stringBytes);
            int stringLength = stringBytes.length;
            int i = 0;
            while (true) {
                if (i >= stringBytes.length) {
                    break;
                } else if (stringBytes[i] == 0) {
                    stringLength = i;
                    break;
                } else {
                    i++;
                }
            }
            return (Type) PropertyImpl.castTo(new String(stringBytes, 0, stringLength, StandardCharsets.UTF_8), type);
        }

        @Override // com.huawei.agpengine.property.Property.Serializer
        public <Type> int write(Property property, ByteBuffer data, Type value) {
            if (!(value instanceof String)) {
                return 0;
            }
            byte[] propertyBytes = new byte[property.getCount()];
            byte[] valueBytes = value.getBytes(StandardCharsets.UTF_8);
            System.arraycopy(valueBytes, 0, propertyBytes, 0, Math.min(propertyBytes.length, valueBytes.length));
            data.put(propertyBytes);
            return propertyBytes.length;
        }
    };
    private static Map<String, Property.Serializer> sTypeMap = new HashMap(20);
    private final CoreProperty mNativeProperty;
    private final Property.Serializer mSerializer;

    static {
        sTypeMap.put(TYPE_BOOL, sMiscSerializer);
        sTypeMap.put(TYPE_CHAR, sStringSerializer);
        sTypeMap.put(TYPE_INT8, sIntSerializer);
        sTypeMap.put(TYPE_INT16, sIntSerializer);
        sTypeMap.put(TYPE_INT32, sIntSerializer);
        sTypeMap.put(TYPE_INT64, sIntSerializer);
        sTypeMap.put(TYPE_UINT8, sIntSerializer);
        sTypeMap.put(TYPE_UINT16, sIntSerializer);
        sTypeMap.put(TYPE_UINT32, sIntSerializer);
        sTypeMap.put(TYPE_UINT64, sIntSerializer);
        sTypeMap.put(TYPE_FLOAT, sFloatSerializer);
        sTypeMap.put(TYPE_DOUBLE, sFloatSerializer);
        sTypeMap.put(TYPE_MATH_VEC2, sMiscSerializer);
        sTypeMap.put(TYPE_MATH_VEC3, sMiscSerializer);
        sTypeMap.put(TYPE_MATH_VEC4, sMiscSerializer);
        sTypeMap.put(TYPE_MATH_QUAT, sMiscSerializer);
        sTypeMap.put(TYPE_MATH_MAT4X4, sMiscSerializer);
        sTypeMap.put(TYPE_ENTITY, sIntSerializer);
        sTypeMap.put(TYPE_GPU_RESOURCE_HANDLE, sIntSerializer);
    }

    PropertyImpl(CoreProperty nativeProperty) {
        if (nativeProperty != null) {
            this.mNativeProperty = nativeProperty;
            CorePropertyTypeDecl type = nativeProperty.getType();
            if (type != null) {
                this.mSerializer = sTypeMap.get(type.getName());
                return;
            }
            throw new IllegalStateException("Internal graphics engine error");
        }
        throw new NullPointerException("Internal graphics engine error");
    }

    /* access modifiers changed from: private */
    public static <TypeIn, TypeOut> TypeOut castTo(TypeIn object, Class<TypeOut> type) {
        if (type.isInstance(object)) {
            return type.cast(object);
        }
        throw new IllegalArgumentException("Expecting type '" + type.getSimpleName() + "', found '" + object.getClass().getSimpleName() + "'");
    }

    /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: java.lang.Class<Type[]> */
    /* JADX WARN: Multi-variable type inference failed */
    /* access modifiers changed from: private */
    public static <Type> Class<?> getType(Property property, Class<Type[]> arrayType) {
        if (property.getCount() > 1) {
            return arrayType;
        }
        return arrayType.getComponentType();
    }

    /* access modifiers changed from: package-private */
    public CoreProperty getNativeProperty() {
        return this.mNativeProperty;
    }

    @Override // com.huawei.agpengine.property.Property
    public String getName() {
        return this.mNativeProperty.getName();
    }

    @Override // com.huawei.agpengine.property.Property
    public String getTypeName() {
        CorePropertyTypeDecl type = this.mNativeProperty.getType();
        if (type != null) {
            return type.getName();
        }
        throw new IllegalStateException("Internal graphics engine error");
    }

    @Override // com.huawei.agpengine.property.Property
    public Property.Serializer getSerializer() {
        return this.mSerializer;
    }

    @Override // com.huawei.agpengine.property.Property
    public int getOffset() {
        return this.mNativeProperty.getOffset().intValue();
    }

    @Override // com.huawei.agpengine.property.Property
    public int getSize() {
        return (int) this.mNativeProperty.getSize();
    }

    @Override // com.huawei.agpengine.property.Property
    public int getCount() {
        return (int) this.mNativeProperty.getCount();
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        if (getCount() == 1) {
            stringBuilder.append(getTypeName());
            stringBuilder.append(" ");
            stringBuilder.append(getName());
        } else {
            stringBuilder.append(getTypeName());
            stringBuilder.append("[");
            stringBuilder.append(getCount());
            stringBuilder.append("] ");
            stringBuilder.append(getName());
        }
        return stringBuilder.toString();
    }
}
