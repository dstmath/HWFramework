package ohos.agp.render.render3d;

import java.util.List;
import java.util.Optional;
import ohos.agp.render.render3d.math.Quaternion;
import ohos.agp.render.render3d.math.Vector3;

public interface SceneNode {
    Optional<SceneNode> getChild(String str);

    List<SceneNode> getChildren();

    Entity getEntity();

    String getName();

    Optional<SceneNode> getParent();

    Vector3 getPosition();

    Quaternion getRotation();

    Vector3 getScale();

    Scene getScene();

    boolean isAncestorOf(SceneNode sceneNode);

    boolean isEnabled();

    boolean isExported();

    Optional<SceneNode> lookupNodeByComponent(Class<? extends Component> cls);

    Optional<SceneNode> lookupNodeByName(String str);

    Optional<SceneNode> lookupNodeByPath(String str);

    List<SceneNode> lookupNodesByComponent(Class<? extends Component> cls);

    void setEnabled(boolean z);

    void setExported(boolean z);

    void setName(String str);

    void setParent(SceneNode sceneNode);

    void setPosition(Vector3 vector3);

    void setRotation(Quaternion quaternion);

    void setScale(Vector3 vector3);
}
