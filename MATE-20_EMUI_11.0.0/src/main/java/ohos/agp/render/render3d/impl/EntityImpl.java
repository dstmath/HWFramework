package ohos.agp.render.render3d.impl;

import java.util.Optional;
import ohos.agp.render.render3d.Component;
import ohos.agp.render.render3d.Entity;
import ohos.agp.render.render3d.impl.SceneImpl;

/* access modifiers changed from: package-private */
public final class EntityImpl implements Entity {
    private static final String EXCEPTION_TAG = "Unknown Component type: ";
    private static final String TAG = "core: EntityImpl";
    private CoreEntity mNativeEntity;
    private SceneImpl mScene;

    private EntityImpl(SceneImpl sceneImpl, CoreEntity coreEntity) {
        this.mScene = sceneImpl;
        this.mNativeEntity = coreEntity;
    }

    static Entity getEntity(SceneImpl sceneImpl, CoreEntity coreEntity) {
        return new EntityImpl(sceneImpl, coreEntity);
    }

    static CoreEntity getNativeEntity(Entity entity) {
        if (entity == null) {
            return new CoreEntity();
        }
        if (entity instanceof EntityImpl) {
            return ((EntityImpl) entity).mNativeEntity;
        }
        throw new IllegalArgumentException();
    }

    @Override // ohos.agp.render.render3d.Entity
    public int getId() {
        CoreEntity coreEntity = this.mNativeEntity;
        if (coreEntity == null) {
            return Integer.MAX_VALUE;
        }
        long id = coreEntity.getId();
        if (id == Core.getInvalidEntity()) {
            return Integer.MAX_VALUE;
        }
        return (int) id;
    }

    @Override // ohos.agp.render.render3d.Entity
    public boolean isValid() {
        return Core.isValid(this.mNativeEntity);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass() || !(obj instanceof EntityImpl)) {
            return false;
        }
        EntityImpl entityImpl = (EntityImpl) obj;
        return this.mNativeEntity == entityImpl.mNativeEntity || getId() == entityImpl.getId();
    }

    public int hashCode() {
        CoreEntity coreEntity = this.mNativeEntity;
        if (coreEntity == null) {
            return 0;
        }
        return (int) coreEntity.getId();
    }

    @Override // ohos.agp.render.render3d.Entity
    public <ComponentType extends Component> ComponentType addComponent(Class<ComponentType> cls) {
        if (this.mNativeEntity != null) {
            SceneImpl.ComponentManager<?, ? extends CoreComponentManager> componentManager = this.mScene.getComponentManager(cls);
            if (componentManager != null) {
                componentManager.create(this.mNativeEntity);
                Optional<?> optional = componentManager.get(this.mNativeEntity);
                if (optional.isPresent()) {
                    Component component = (Component) optional.get();
                    if (cls.isInstance(component)) {
                        return cls.cast(component);
                    }
                }
                throw new IllegalStateException();
            }
            throw new IllegalArgumentException(EXCEPTION_TAG + cls.getSimpleName());
        }
        throw new IllegalStateException();
    }

    @Override // ohos.agp.render.render3d.Entity
    public <ComponentType extends Component> Optional<ComponentType> getComponent(Class<ComponentType> cls) {
        if (this.mNativeEntity == null) {
            throw new IllegalStateException();
        } else if (cls != null) {
            SceneImpl.ComponentManager<?, ? extends CoreComponentManager> componentManager = this.mScene.getComponentManager(cls);
            if (componentManager != null) {
                Optional<?> optional = componentManager.get(this.mNativeEntity);
                if (!optional.isPresent()) {
                    return Optional.empty();
                }
                Component component = (Component) optional.get();
                if (cls.isInstance(component)) {
                    return Optional.of(cls.cast(component));
                }
                throw new IllegalStateException();
            }
            throw new IllegalArgumentException(EXCEPTION_TAG + cls.getSimpleName());
        } else {
            throw new NullPointerException();
        }
    }

    @Override // ohos.agp.render.render3d.Entity
    public <ComponentType extends Component> void setComponent(ComponentType componenttype) {
        if (this.mNativeEntity != null) {
            SceneImpl.ComponentManager<?, ? extends CoreComponentManager> componentManager = this.mScene.getComponentManager(componenttype.getClass());
            if (componentManager != null) {
                componentManager.set(this.mNativeEntity, componenttype);
                return;
            }
            throw new IllegalArgumentException(EXCEPTION_TAG + componenttype.getClass().getSimpleName());
        }
        throw new IllegalStateException();
    }
}
