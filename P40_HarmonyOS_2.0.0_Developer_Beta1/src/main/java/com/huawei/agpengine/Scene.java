package com.huawei.agpengine;

import com.huawei.agpengine.property.PropertyData;
import com.huawei.agpengine.systems.MorphingSystem;
import com.huawei.agpengine.util.SceneUtil;
import java.util.Optional;

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

    Optional<PropertyData> getSystemPropertyData(String str);

    boolean setSystemPropertyData(String str, PropertyData propertyData);
}
