package com.huawei.agpengine.impl;

import com.huawei.agpengine.property.PropertyApi;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class PropertyApiImpl implements PropertyApi {
    private final CorePropertyApi mNativeApi;
    private List<PropertyImpl> mProperties;
    private Map<String, PropertyImpl> mPropertyMap;

    PropertyApiImpl(CorePropertyApi nativeApi) {
        if (nativeApi != null) {
            this.mNativeApi = nativeApi;
            return;
        }
        throw new NullPointerException("Internal graphics engine error");
    }

    private void update() {
        if (this.mProperties == null) {
            int propertyCount = (int) this.mNativeApi.propertyCount();
            this.mProperties = new ArrayList(propertyCount);
            this.mPropertyMap = new HashMap(propertyCount);
            for (int i = 0; i < propertyCount; i++) {
                CoreProperty nativeProperty = this.mNativeApi.metaData((long) i);
                if (nativeProperty != null) {
                    String name = nativeProperty.getName();
                    if (name != null) {
                        PropertyImpl property = new PropertyImpl(nativeProperty);
                        this.mProperties.add(property);
                        this.mPropertyMap.put(name, property);
                    } else {
                        throw new IllegalStateException("Internal graphics engine error");
                    }
                } else {
                    throw new IllegalStateException("Internal graphics engine error");
                }
            }
        }
    }

    @Override // com.huawei.agpengine.property.PropertyApi
    public int getPropertyCount() {
        update();
        return this.mProperties.size();
    }

    @Override // com.huawei.agpengine.property.PropertyApi
    public PropertyImpl getProperty(int index) {
        update();
        return this.mProperties.get(index);
    }

    @Override // com.huawei.agpengine.property.PropertyApi
    public PropertyImpl getProperty(String propertyName) {
        update();
        return this.mPropertyMap.get(propertyName);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != getClass() || !(obj instanceof PropertyApiImpl)) {
            return false;
        }
        if (CorePropertyApi.getCptr(this.mNativeApi) == CorePropertyApi.getCptr(((PropertyApiImpl) obj).mNativeApi)) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return Long.hashCode(CorePropertyApi.getCptr(this.mNativeApi));
    }

    static boolean nativeEquals(PropertyApi api, CorePropertyApi nativeApi) {
        if (!(api instanceof PropertyApiImpl) || CorePropertyApi.getCptr(((PropertyApiImpl) api).mNativeApi) != CorePropertyApi.getCptr(nativeApi)) {
            return false;
        }
        return true;
    }
}
