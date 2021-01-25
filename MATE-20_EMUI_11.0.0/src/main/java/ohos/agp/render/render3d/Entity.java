package ohos.agp.render.render3d;

import java.util.Optional;

public interface Entity {
    public static final int INVALID_ENTITY = Integer.MAX_VALUE;

    <ComponentType extends Component> ComponentType addComponent(Class<ComponentType> cls);

    <ComponentType extends Component> Optional<ComponentType> getComponent(Class<ComponentType> cls);

    int getId();

    boolean isValid();

    <ComponentType extends Component> void setComponent(ComponentType componenttype);
}
