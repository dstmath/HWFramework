package com.huawei.agpengine.property;

import java.nio.ByteBuffer;

public interface PropertyData {
    <Type> Type get(Property property, Class<Type> cls);

    <Type> Type get(String str, Class<Type> cls);

    ByteBuffer getData();

    PropertyApi getOwner();

    String propertyAsString(Property property);

    <Type> void set(Property property, Type type);

    <Type> void set(String str, Type type);

    String valueAsString(Property property);
}
