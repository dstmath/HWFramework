package ohos.agp.render.render3d.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import ohos.agp.render.render3d.Component;
import ohos.agp.render.render3d.Entity;
import ohos.agp.render.render3d.Scene;
import ohos.agp.render.render3d.SceneNode;
import ohos.agp.render.render3d.components.NodeComponent;
import ohos.agp.render.render3d.math.Quaternion;
import ohos.agp.render.render3d.math.Vector3;

/* access modifiers changed from: package-private */
public class SceneNodeImpl implements SceneNode {
    private CoreSceneNode mNativeSceneNode;
    private SceneImpl mScene;

    SceneNodeImpl(SceneImpl sceneImpl, CoreSceneNode coreSceneNode) {
        if (coreSceneNode != null) {
            this.mScene = sceneImpl;
            this.mNativeSceneNode = coreSceneNode;
            return;
        }
        throw new NullPointerException();
    }

    /* access modifiers changed from: package-private */
    public CoreSceneNode getNativeSceneNode() {
        return this.mNativeSceneNode;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass() || !(obj instanceof SceneNodeImpl)) {
            return false;
        }
        SceneNodeImpl sceneNodeImpl = (SceneNodeImpl) obj;
        CoreSceneNode coreSceneNode = this.mNativeSceneNode;
        return coreSceneNode == sceneNodeImpl.mNativeSceneNode || coreSceneNode.getEntity().getId() == sceneNodeImpl.mNativeSceneNode.getEntity().getId();
    }

    public int hashCode() {
        return (int) this.mNativeSceneNode.getEntity().getId();
    }

    @Override // ohos.agp.render.render3d.SceneNode
    public String getName() {
        return this.mNativeSceneNode.getName();
    }

    @Override // ohos.agp.render.render3d.SceneNode
    public void setName(String str) {
        if (str != null) {
            this.mNativeSceneNode.setName(str);
            return;
        }
        throw new NullPointerException();
    }

    @Override // ohos.agp.render.render3d.SceneNode
    public Scene getScene() {
        return this.mScene;
    }

    @Override // ohos.agp.render.render3d.SceneNode
    public Optional<SceneNode> getParent() {
        return this.mScene.getNode(this.mNativeSceneNode.getParent());
    }

    @Override // ohos.agp.render.render3d.SceneNode
    public void setParent(SceneNode sceneNode) {
        Optional<CoreSceneNode> nativeSceneNode = SceneImpl.getNativeSceneNode(sceneNode);
        if (nativeSceneNode.isPresent()) {
            this.mNativeSceneNode.setParent(nativeSceneNode.get());
        } else {
            setParent(this.mScene.getRootNode());
        }
    }

    @Override // ohos.agp.render.render3d.SceneNode
    public Vector3 getPosition() {
        return Swig.get(this.mNativeSceneNode.getPosition());
    }

    @Override // ohos.agp.render.render3d.SceneNode
    public void setPosition(Vector3 vector3) {
        this.mNativeSceneNode.setPosition(Swig.set(vector3));
    }

    @Override // ohos.agp.render.render3d.SceneNode
    public Quaternion getRotation() {
        return Swig.get(this.mNativeSceneNode.getRotation());
    }

    @Override // ohos.agp.render.render3d.SceneNode
    public void setRotation(Quaternion quaternion) {
        this.mNativeSceneNode.setRotation(Swig.set(quaternion));
    }

    @Override // ohos.agp.render.render3d.SceneNode
    public Vector3 getScale() {
        return Swig.get(this.mNativeSceneNode.getScale());
    }

    @Override // ohos.agp.render.render3d.SceneNode
    public void setScale(Vector3 vector3) {
        this.mNativeSceneNode.setScale(Swig.set(vector3));
    }

    @Override // ohos.agp.render.render3d.SceneNode
    public void setEnabled(boolean z) {
        this.mNativeSceneNode.setEnabled(z);
    }

    @Override // ohos.agp.render.render3d.SceneNode
    public boolean isEnabled() {
        return this.mNativeSceneNode.getEnabled();
    }

    @Override // ohos.agp.render.render3d.SceneNode
    public void setExported(boolean z) {
        Entity entity = getEntity();
        Optional component = entity.getComponent(NodeComponent.class);
        if (component.isPresent()) {
            ((NodeComponent) component.get()).setExported(z);
            entity.setComponent((NodeComponent) component.get());
        }
    }

    @Override // ohos.agp.render.render3d.SceneNode
    public boolean isExported() {
        Optional component = getEntity().getComponent(NodeComponent.class);
        if (component.isPresent()) {
            return ((NodeComponent) component.get()).isExported();
        }
        return true;
    }

    @Override // ohos.agp.render.render3d.SceneNode
    public List<SceneNode> getChildren() {
        CoreSceneNodeArrayView children = this.mNativeSceneNode.getChildren();
        int size = (int) children.size();
        ArrayList arrayList = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            arrayList.add(this.mScene.getNodeNotNull(children.get((long) i)));
        }
        return arrayList;
    }

    @Override // ohos.agp.render.render3d.SceneNode
    public boolean isAncestorOf(SceneNode sceneNode) {
        Optional<CoreSceneNode> nativeSceneNode = SceneImpl.getNativeSceneNode(sceneNode);
        if (nativeSceneNode.isPresent()) {
            return this.mNativeSceneNode.isAncestorOf(nativeSceneNode.get());
        }
        return false;
    }

    @Override // ohos.agp.render.render3d.SceneNode
    public Optional<SceneNode> getChild(String str) {
        return this.mScene.getNode(this.mNativeSceneNode.getChild(str));
    }

    @Override // ohos.agp.render.render3d.SceneNode
    public Optional<SceneNode> lookupNodeByPath(String str) {
        return this.mScene.getNode(this.mNativeSceneNode.lookupNodeByPath(str));
    }

    @Override // ohos.agp.render.render3d.SceneNode
    public Optional<SceneNode> lookupNodeByName(String str) {
        return this.mScene.getNode(this.mNativeSceneNode.lookupNodeByName(str));
    }

    @Override // ohos.agp.render.render3d.SceneNode
    public Optional<SceneNode> lookupNodeByComponent(Class<? extends Component> cls) {
        return this.mScene.getNode(this.mNativeSceneNode.lookupNodeByComponent(this.mScene.getComponentManager(cls).mManager));
    }

    @Override // ohos.agp.render.render3d.SceneNode
    public List<SceneNode> lookupNodesByComponent(Class<? extends Component> cls) {
        CoreSceneNodeArray lookupNodesByComponent = this.mNativeSceneNode.lookupNodesByComponent(this.mScene.getComponentManager(cls).mManager);
        int size = lookupNodesByComponent.size();
        ArrayList arrayList = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            arrayList.add(this.mScene.getNodeNotNull(lookupNodesByComponent.get(i)));
        }
        return arrayList;
    }

    @Override // ohos.agp.render.render3d.SceneNode
    public Entity getEntity() {
        return EntityImpl.getEntity(this.mScene, this.mNativeSceneNode.getEntity());
    }
}
