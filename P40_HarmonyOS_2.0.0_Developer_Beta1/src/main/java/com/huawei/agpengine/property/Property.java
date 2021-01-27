package com.huawei.agpengine.property;

import java.nio.ByteBuffer;

public interface Property {

    public interface Serializer {
        Class getJavaType(Property property);

        <Type> Type read(Property property, ByteBuffer byteBuffer, Class<Type> cls);

        <Type> int write(Property property, ByteBuffer byteBuffer, Type type);
    }

    int getCount();

    String getName();

    int getOffset();

    Serializer getSerializer();

    int getSize();

    String getTypeName();
}
