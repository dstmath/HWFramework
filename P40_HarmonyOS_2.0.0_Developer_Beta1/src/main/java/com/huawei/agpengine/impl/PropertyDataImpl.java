package com.huawei.agpengine.impl;

import com.huawei.agpengine.property.Property;
import com.huawei.agpengine.property.PropertyApi;
import com.huawei.agpengine.property.PropertyData;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/* access modifiers changed from: package-private */
public class PropertyDataImpl implements PropertyData {
    private static final String QUOTATION_MARK = "'";
    private final ByteBuffer mData;
    private final PropertyApi mOwner;

    PropertyDataImpl(CorePropertyHandle propertyHandle) {
        this.mOwner = new PropertyApiImpl(propertyHandle.owner());
        this.mData = ByteBuffer.allocateDirect((int) propertyHandle.size());
        this.mData.order(ByteOrder.nativeOrder());
    }

    static boolean readFromPropertyHandle(CorePropertyHandle nativeHandle, ByteBuffer data) {
        if (((long) data.capacity()) == nativeHandle.size()) {
            return Core.getPropertyData(nativeHandle, new CoreWriteableByteArrayView(data));
        }
        throw new IllegalArgumentException("Internal graphics engine error");
    }

    static boolean writeToPropertyHandle(CorePropertyHandle nativeHandle, ByteBuffer data) {
        if (((long) data.capacity()) == nativeHandle.size()) {
            return Core.setPropertyData(nativeHandle, new CoreByteArrayView(data));
        }
        throw new IllegalArgumentException("Internal graphics engine error");
    }

    private String quoteText(String text) {
        return QUOTATION_MARK + text + QUOTATION_MARK;
    }

    @Override // com.huawei.agpengine.property.PropertyData
    public PropertyApi getOwner() {
        return this.mOwner;
    }

    @Override // com.huawei.agpengine.property.PropertyData
    public ByteBuffer getData() {
        return this.mData;
    }

    @Override // com.huawei.agpengine.property.PropertyData
    public <Type> Type get(String propertyName, Class<Type> type) {
        return (Type) get(getOwner().getProperty(propertyName), type);
    }

    @Override // com.huawei.agpengine.property.PropertyData
    public <Type> Type get(Property property, Class<Type> type) {
        if (property instanceof PropertyImpl) {
            PropertyImpl prop = (PropertyImpl) property;
            CoreProperty nativeProp = prop.getNativeProperty();
            this.mData.position(nativeProp.getOffset().intValue());
            if (type.equals(byte[].class)) {
                byte[] bytes = new byte[((int) nativeProp.getSize())];
                this.mData.get(bytes);
                return type.cast(bytes);
            }
            Property.Serializer ps = prop.getSerializer();
            if (ps != null) {
                int elementCount = property.getCount();
                if (elementCount == 1 || "char".equals(property.getTypeName())) {
                    return type.cast(ps.read(property, this.mData, type));
                }
                Class<?> componentType = type.getComponentType();
                Object objects = Array.newInstance(componentType, elementCount);
                for (int i = 0; i < elementCount; i++) {
                    Array.set(objects, i, ps.read(property, this.mData, componentType));
                }
                return type.cast(objects);
            }
            throw new IllegalStateException("No serializer found for type " + quoteText(type.getSimpleName()));
        }
        throw new IllegalArgumentException("Unsupported implementation.");
    }

    @Override // com.huawei.agpengine.property.PropertyData
    public <Type> void set(String propertyName, Type value) {
        set(getOwner().getProperty(propertyName), value);
    }

    @Override // com.huawei.agpengine.property.PropertyData
    public <Type> void set(Property property, Type value) {
        this.mData.position(property.getOffset());
        if (value != null) {
            Property.Serializer ps = property.getSerializer();
            if (ps == null) {
                this.mData.put((byte[]) value);
            } else if (property.getCount() == 1 || "char".equals(property.getTypeName())) {
                ps.write(property, this.mData, value);
            } else {
                for (Object element : (Object[]) value) {
                    ps.write(property, this.mData, element);
                }
            }
        } else {
            throw new NullPointerException("value must not be null.");
        }
    }

    @Override // com.huawei.agpengine.property.PropertyData
    public String valueAsString(Property property) {
        Class<byte[]> cls;
        Property.Serializer ser = property.getSerializer();
        if (ser != null) {
            cls = ser.getJavaType(property);
        } else {
            cls = byte[].class;
        }
        return stringValueOf(get(property, cls));
    }

    private <Type> String stringValueOf(Type value) {
        if (value instanceof Object[]) {
            return Arrays.toString((Object[]) value);
        }
        if (value instanceof byte[]) {
            return Arrays.toString((byte[]) value);
        }
        if (value instanceof String) {
            return quoteText(value.toString());
        }
        return String.valueOf(value);
    }

    @Override // com.huawei.agpengine.property.PropertyData
    public String propertyAsString(Property property) {
        return quoteText(property.getName()) + " : " + valueAsString(property);
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        PropertyApi api = getOwner();
        int propertyCount = api.getPropertyCount();
        for (int i = 0; i < propertyCount; i++) {
            result.append(propertyAsString(api.getProperty(i)));
            result.append(System.lineSeparator());
        }
        return result.toString();
    }
}
