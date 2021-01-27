package com.huawei.agpengine;

import com.huawei.agpengine.property.PropertyData;
import java.util.Optional;

public interface Entity {
    public static final int INVALID_ENTITY = -1;

    <ComponentType extends Component> ComponentType addComponent(Class<ComponentType> cls);

    Optional<PropertyData> addComponent(String str);

    <ComponentType extends Component> Optional<ComponentType> getComponent(Class<ComponentType> cls);

    Optional<PropertyData> getComponentPropertyData(String str);

    String[] getComponents();

    int getId();

    boolean isAlive();

    boolean isValid();

    boolean removeComponent(String str);

    <ComponentType extends Component> void setComponent(ComponentType componenttype);

    boolean setComponentPropertyData(String str, PropertyData propertyData);
}
