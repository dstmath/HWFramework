package com.huawei.agpengine.impl;

import com.huawei.agpengine.Component;
import com.huawei.agpengine.Entity;
import com.huawei.agpengine.impl.SceneImpl;
import com.huawei.agpengine.property.PropertyData;
import java.util.Optional;

/* access modifiers changed from: package-private */
public final class EntityImpl implements Entity {
    private static final String TAG = "core: EntityImpl";
    private int mId = -1;
    private SceneImpl mScene;

    EntityImpl(SceneImpl scene, int nativeId) {
        this.mScene = scene;
        this.mId = nativeId;
    }

    @Override // com.huawei.agpengine.Entity
    public int getId() {
        return this.mId;
    }

    @Override // com.huawei.agpengine.Entity
    public boolean isValid() {
        int i = this.mId;
        if (i == -1) {
            return false;
        }
        return Core.isEntityValid(i);
    }

    @Override // com.huawei.agpengine.Entity
    public boolean isAlive() {
        if (this.mId == -1) {
            return false;
        }
        return this.mScene.isAlive(this);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass() || !(obj instanceof Entity)) {
            return false;
        }
        if (this.mId == ((Entity) obj).getId()) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return this.mId;
    }

    public String toString() {
        return Integer.toString(this.mId);
    }

    @Override // com.huawei.agpengine.Entity
    public <ComponentType extends Component> Optional<ComponentType> getComponent(Class<ComponentType> componentType) {
        if (componentType != null) {
            Optional<?> optional = this.mScene.getComponentManager(componentType).get(this.mId);
            if (!optional.isPresent()) {
                return Optional.empty();
            }
            Component component = (Component) optional.get();
            if (componentType.isInstance(component)) {
                return Optional.of(componentType.cast(component));
            }
            throw new IllegalStateException("Internal graphics engine error");
        }
        throw new NullPointerException("componentType must not be null.");
    }

    @Override // com.huawei.agpengine.Entity
    public <ComponentType extends Component> void setComponent(ComponentType component) {
        if (component != null) {
            this.mScene.getComponentManager(component.getClass()).set(this.mId, component);
            return;
        }
        throw new NullPointerException("component must not be null.");
    }

    @Override // com.huawei.agpengine.Entity
    public <ComponentType extends Component> ComponentType addComponent(Class<ComponentType> componentType) {
        if (componentType != null) {
            SceneImpl.ComponentManager<?, ? extends CoreComponentManager> componentManager = this.mScene.getComponentManager(componentType);
            componentManager.create(this.mId);
            Optional<?> optional = componentManager.get(this.mId);
            if (optional.isPresent()) {
                Component component = (Component) optional.get();
                if (componentType.isInstance(component)) {
                    return componentType.cast(component);
                }
            }
            throw new IllegalStateException("Internal graphics engine error");
        }
        throw new NullPointerException("componentType must not be null.");
    }

    @Override // com.huawei.agpengine.Entity
    public Optional<PropertyData> addComponent(String componentName) {
        CoreComponentManager manager = this.mScene.getNativeComponentManager(componentName);
        if (manager == null) {
            return Optional.empty();
        }
        manager.create(this.mId);
        return getComponentPropertyData(componentName);
    }

    @Override // com.huawei.agpengine.Entity
    public boolean removeComponent(String componentName) {
        CoreComponentManager manager = this.mScene.getNativeComponentManager(componentName);
        if (manager != null) {
            return manager.destroy(this.mId);
        }
        return false;
    }

    private CorePropertyHandle getPropertyHandle(String componentName) {
        CoreComponentManager manager = this.mScene.getNativeComponentManager(componentName);
        if (manager == null) {
            return null;
        }
        long componentId = manager.getComponentId(this.mId);
        if (((int) componentId) == -1) {
            return null;
        }
        return manager.getPropertyData(componentId);
    }

    @Override // com.huawei.agpengine.Entity
    public Optional<PropertyData> getComponentPropertyData(String componentName) {
        CorePropertyHandle propertyHandle = getPropertyHandle(componentName);
        if (propertyHandle == null) {
            return Optional.empty();
        }
        PropertyDataImpl data = new PropertyDataImpl(propertyHandle);
        if (!PropertyDataImpl.readFromPropertyHandle(propertyHandle, data.getData())) {
            return Optional.empty();
        }
        return Optional.of(data);
    }

    @Override // com.huawei.agpengine.Entity
    public boolean setComponentPropertyData(String componentName, PropertyData componentData) {
        CorePropertyHandle propertyHandle = getPropertyHandle(componentName);
        if (propertyHandle == null) {
            return false;
        }
        if (PropertyApiImpl.nativeEquals(componentData.getOwner(), propertyHandle.owner())) {
            return PropertyDataImpl.writeToPropertyHandle(propertyHandle, componentData.getData());
        }
        throw new IllegalArgumentException("PropertyApi mismatch.");
    }

    @Override // com.huawei.agpengine.Entity
    public String[] getComponents() {
        return this.mScene.getComponents(this.mId);
    }
}
