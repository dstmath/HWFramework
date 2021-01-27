package ohos.agp.transition;

import ohos.agp.components.ComponentContainer;

public class TransitionComponents extends TransitionScene {
    public TransitionComponents(ComponentContainer componentContainer, ComponentContainer componentContainer2) {
        super(componentContainer, componentContainer2);
    }

    public long getNativeTransitionViewsPtr() {
        return super.getNativeTransitionScenePtr();
    }
}
