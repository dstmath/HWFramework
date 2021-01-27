package ohos.agp.render.render3d;

import java.util.Optional;
import ohos.agp.render.render3d.systems.MorphingSystem;
import ohos.agp.render.render3d.util.SceneUtil;

public interface Scene {
    SceneNode cloneNode(SceneNode sceneNode, boolean z);

    Entity createEntity();

    SceneNode createNode();

    void destroyEntity(Entity entity);

    void destroyNode(SceneNode sceneNode);

    Engine getEngine();

    MorphingSystem getMorphingSystem();

    Optional<SceneNode> getNode(Entity entity);

    SceneNode getRootNode();

    SceneUtil getSceneUtil();
}
