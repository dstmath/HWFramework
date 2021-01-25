package com.huawei.agpengine.impl;

import com.huawei.agpengine.BuildConfig;
import com.huawei.agpengine.Component;
import com.huawei.agpengine.Entity;
import com.huawei.agpengine.Scene;
import com.huawei.agpengine.SceneNode;
import com.huawei.agpengine.components.NodeComponent;
import com.huawei.agpengine.math.Quaternion;
import com.huawei.agpengine.math.Vector3;
import com.huawei.agpengine.property.PropertyData;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/* access modifiers changed from: package-private */
public class SceneNodeImpl implements SceneNode {
    private static final String MODEL_ID_COMPONENT = "RSDZModelIdComponent";
    private CoreSceneNode mNativeSceneNode;
    private SceneImpl mScene;

    SceneNodeImpl(SceneImpl scene, CoreSceneNode nativeSceneNode) {
        if (nativeSceneNode != null) {
            this.mScene = scene;
            this.mNativeSceneNode = nativeSceneNode;
            return;
        }
        throw new NullPointerException("Internal graphics engine error");
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
        SceneNodeImpl node = (SceneNodeImpl) obj;
        CoreSceneNode coreSceneNode = this.mNativeSceneNode;
        if (coreSceneNode == node.mNativeSceneNode || coreSceneNode.getEntity() == node.mNativeSceneNode.getEntity()) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return this.mNativeSceneNode.getEntity();
    }

    @Override // com.huawei.agpengine.SceneNode
    public String getName() {
        return this.mNativeSceneNode.getName();
    }

    @Override // com.huawei.agpengine.SceneNode
    public void setName(String name) {
        if (name != null) {
            this.mNativeSceneNode.setName(name);
            return;
        }
        throw new NullPointerException("name must not be null.");
    }

    @Override // com.huawei.agpengine.SceneNode
    public Scene getScene() {
        return this.mScene;
    }

    @Override // com.huawei.agpengine.SceneNode
    public Optional<SceneNode> getParent() {
        return this.mScene.getNode(this.mNativeSceneNode.getParent());
    }

    @Override // com.huawei.agpengine.SceneNode
    public void setParent(SceneNode parentNode) {
        Optional<CoreSceneNode> nativeParent = SceneImpl.getNativeSceneNode(parentNode);
        if (nativeParent.isPresent()) {
            this.mNativeSceneNode.setParent(nativeParent.get());
        } else {
            setParent(this.mScene.getRootNode());
        }
    }

    @Override // com.huawei.agpengine.SceneNode
    public Vector3 getPosition() {
        return Swig.get(this.mNativeSceneNode.getPosition());
    }

    @Override // com.huawei.agpengine.SceneNode
    public void setPosition(Vector3 position) {
        this.mNativeSceneNode.setPosition(Swig.set(position));
    }

    @Override // com.huawei.agpengine.SceneNode
    public Quaternion getRotation() {
        return Swig.get(this.mNativeSceneNode.getRotation());
    }

    @Override // com.huawei.agpengine.SceneNode
    public void setRotation(Quaternion rotation) {
        this.mNativeSceneNode.setRotation(Swig.set(rotation));
    }

    @Override // com.huawei.agpengine.SceneNode
    public Vector3 getScale() {
        return Swig.get(this.mNativeSceneNode.getScale());
    }

    @Override // com.huawei.agpengine.SceneNode
    public void setScale(Vector3 scale) {
        this.mNativeSceneNode.setScale(Swig.set(scale));
    }

    @Override // com.huawei.agpengine.SceneNode
    public void setEnabled(boolean isEnabled) {
        this.mNativeSceneNode.setEnabled(isEnabled);
    }

    @Override // com.huawei.agpengine.SceneNode
    public boolean isEnabled() {
        return this.mNativeSceneNode.getEnabled();
    }

    @Override // com.huawei.agpengine.SceneNode
    public void setExported(boolean isExported) {
        Entity entity = getEntity();
        Optional<NodeComponent> nodeComp = entity.getComponent(NodeComponent.class);
        if (nodeComp.isPresent()) {
            nodeComp.get().setExported(isExported);
            entity.setComponent(nodeComp.get());
        }
    }

    @Override // com.huawei.agpengine.SceneNode
    public boolean isExported() {
        Optional<NodeComponent> nodeComp = getEntity().getComponent(NodeComponent.class);
        if (nodeComp.isPresent()) {
            return nodeComp.get().isExported();
        }
        return true;
    }

    @Override // com.huawei.agpengine.SceneNode
    public List<SceneNode> getChildren() {
        CoreSceneNodeArrayView nativeArray = this.mNativeSceneNode.getChildren();
        int childCount = (int) nativeArray.size();
        List<SceneNode> nodeList = new ArrayList<>(childCount);
        for (int i = 0; i < childCount; i++) {
            nodeList.add(this.mScene.getNodeNotNull(nativeArray.get((long) i)));
        }
        return nodeList;
    }

    @Override // com.huawei.agpengine.SceneNode
    public boolean isAncestorOf(SceneNode node) {
        Optional<CoreSceneNode> nativeNode = SceneImpl.getNativeSceneNode(node);
        if (nativeNode.isPresent()) {
            return this.mNativeSceneNode.isAncestorOf(nativeNode.get());
        }
        return false;
    }

    @Override // com.huawei.agpengine.SceneNode
    public Optional<SceneNode> getChild(String name) {
        return this.mScene.getNode(this.mNativeSceneNode.getChild(name));
    }

    @Override // com.huawei.agpengine.SceneNode
    public Optional<SceneNode> lookupNodeByPath(String path) {
        return this.mScene.getNode(this.mNativeSceneNode.lookupNodeByPath(path));
    }

    @Override // com.huawei.agpengine.SceneNode
    public Optional<SceneNode> lookupNodeByName(String name) {
        return this.mScene.getNode(this.mNativeSceneNode.lookupNodeByName(name));
    }

    @Override // com.huawei.agpengine.SceneNode
    public Optional<SceneNode> lookupNodeByComponent(Class<? extends Component> componentType) {
        return this.mScene.getNode(this.mNativeSceneNode.lookupNodeByComponent(this.mScene.getComponentManager(componentType).mManager));
    }

    @Override // com.huawei.agpengine.SceneNode
    public List<SceneNode> lookupNodesByComponent(Class<? extends Component> componentType) {
        CoreSceneNodeArray nativeArray = this.mNativeSceneNode.lookupNodesByComponent(this.mScene.getComponentManager(componentType).mManager);
        int childCount = nativeArray.size();
        List<SceneNode> nodeList = new ArrayList<>(childCount);
        for (int i = 0; i < childCount; i++) {
            nodeList.add(this.mScene.getNodeNotNull(nativeArray.get(i)));
        }
        return nodeList;
    }

    @Override // com.huawei.agpengine.SceneNode
    public Entity getEntity() {
        return new EntityImpl(this.mScene, this.mNativeSceneNode.getEntity());
    }

    @Override // com.huawei.agpengine.SceneNode
    public String getModelId() {
        Optional<PropertyData> dataOpt = getEntity().getComponentPropertyData(MODEL_ID_COMPONENT);
        if (dataOpt.isPresent()) {
            return (String) dataOpt.get().get("modelId", String.class);
        }
        return BuildConfig.FLAVOR;
    }
}
