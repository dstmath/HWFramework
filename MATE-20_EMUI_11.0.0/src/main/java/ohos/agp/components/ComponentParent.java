package ohos.agp.components;

import ohos.agp.components.ComponentContainer;

public interface ComponentParent {
    void bringChildToFront(Component component);

    ComponentParent getComponentParent();

    int indexOfChild(Component component);

    boolean onDrag(Component component, DragEvent dragEvent);

    void removeComponent(Component component);

    void removeComponentAt(int i);

    void removeComponents(int i, int i2);

    void requestLayout();

    ComponentContainer.LayoutConfig verifyLayoutConfig(ComponentContainer.LayoutConfig layoutConfig);
}
