package ohos.agp.components;

import ohos.agp.components.ComponentContainer;

public interface ComponentParent {
    int getChildIndex(Component component);

    ComponentParent getComponentParent();

    void moveChildToFront(Component component);

    boolean onDrag(Component component, DragEvent dragEvent);

    void postLayout();

    void removeComponent(Component component);

    void removeComponentAt(int i);

    void removeComponents(int i, int i2);

    ComponentContainer.LayoutConfig verifyLayoutConfig(ComponentContainer.LayoutConfig layoutConfig);
}
